
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.core.jdbc;

import com.versant.core.storagemanager.*;
import com.versant.core.logging.LogEventStore;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;

import com.versant.core.metadata.generator.OIDSrcGenerator;
import com.versant.core.metadata.generator.StateSrcGenerator;
import com.versant.core.jdbc.conn.ExternalJdbcConnectionSource;

import com.versant.core.common.config.ConfigInfo;
import com.versant.core.common.*;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.conn.JDBCConnectionPool;
import com.versant.core.jdbc.metadata.JdbcMetaData;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.server.DataStoreInfo;
import com.versant.core.server.CompiledQueryCache;
import com.versant.core.metric.HasMetrics;

import com.versant.core.compiler.ClassSpec;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


import java.util.*;
import java.sql.Driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Constructor;
import java.io.IOException;

/**
 * Creates JdbcStorageManager's.
 */
public final class JdbcStorageManagerFactory
        implements StorageManagerFactory, HasMetrics {

    private final LogEventStore pes;
    private final StorageCache cache;
    private JdbcConnectionSource conSrc;
    private final ModelMetaData jmd;
    private final JdbcConfig jdbcConfig;
    private final SqlDriver sqlDriver;
    private final CompiledQueryCache compiledQueryCache;
    private final boolean hyperdrive;
    private Driver jdbcDriver;

    private DataSource ds1;
    private DataSource ds2;


    public JdbcStorageManagerFactory(StorageManagerFactoryBuilder b) {
        this.pes = b.getLogEventStore();
        this.cache = b.getCache();
        this.compiledQueryCache = b.getCompiledQueryCache();
        ConfigInfo config = b.getConfig();
        this.jdbcConfig = new JdbcConfigParser().parse(config.props);

        boolean createPool = !b.isOnlyMetaData();
        ClassLoader loader = b.getLoader();

        if (createPool) {

            if (!b.isIgnoreConFactoryProperties()) {
                ds1 = lookupDataSource(config.connectionFactoryName);
                ds2 = ds1 == null ? null : lookupDataSource(config.connectionFactory2Name);
            } else {

                jdbcDriver = SqlDriver.createJdbcDriver(jdbcConfig.driver, loader);

            }

        } else {
            jdbcDriver = null;
        }

        sqlDriver = SqlDriver.createSqlDriver(jdbcConfig.db, jdbcDriver);
        if (createPool) {
            createPool(loader);
        } else {
            conSrc = null;
        }

        // build meta data
        JdbcMetaDataBuilder mdb = new JdbcMetaDataBuilder(config,
                jdbcConfig, loader, sqlDriver, b.isContinueAfterMetaDataError());
        jmd = mdb.buildMetaData(config.jdoMetaData);

        // generate source for hyperdrive classes if needed
        hyperdrive = config.hyperdrive;

        if (hyperdrive) {
            OIDSrcGenerator oidGen = new JdbcOIDGenerator(jmd);
            StateSrcGenerator stateGen = new JdbcStateGenerator();
            HashMap classSpecs = b.getClassSpecs();
            for (int i = jmd.classes.length - 1; i >= 0; i--) {
                ClassMetaData cmd = jmd.classes[i];
                if (cmd.horizontal) continue;
                ClassSpec spec = oidGen.generateOID(cmd);
                classSpecs.put(spec.getQName(), spec);
                spec = stateGen.generateState(cmd);
                classSpecs.put(spec.getQName(), spec);
            }
        }

    }

    
	public DataSource lookupDataSource(String name) {
        if (name == null || name.length() == 0) return null;
        DataSource ds1 = null;
        //do a lookup for the external datasource. we will need to know if this datasource
        //provides already enlisted connection as this would determine if we may commit/rollback on them etc.
        try {
            InitialContext context = new InitialContext();
            Object o1 = context.lookup(name);
            if (o1 == null) return null;
            if (o1 instanceof DataSource) {
                ds1 = (DataSource) o1;
//                System.out.println(name + " is a DataSource");
//                if (ds1 instanceof XADataSource) {
//                    System.out.println(name + " is a XADataSource");
//                }
            }
        } catch (NamingException e) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Unable to lookup '" + name + "': " + e, e);
        }
        return ds1;
    }


    public void init(boolean full, ClassLoader loader) {

        if (hyperdrive) {
            if (full) {
                installHyperdriveStateAndOIDFactory(loader);
            }
        } else { // not using generated hyperdrive classes
            installGenericStateAndOIDFactory();
        }


        Connection con = null;
        try {
            conSrc.init();
            if (sqlDriver.isCustomizeForServerRequired()) {
                con = conSrc.getConnection(true, false);
                sqlDriver.customizeForServer(con);
            }
            if (full) {
                ClassMetaData[] classes = jmd.classes;
                for (int j = 0; j < classes.length; j++) {
                    ClassMetaData cmd = classes[j];
                    if (cmd.top != cmd || cmd.embeddedOnly) {
                        continue;
                    }
                    if (cmd.embeddedOnly) continue;
                    JdbcKeyGenerator kg = ((JdbcClass)cmd.storeClass).jdbcKeyGenerator;
                    if (kg == null) {
                        continue;
                    }
                    if (con == null) {
                        con = conSrc.getConnection(true, false);
                    }
                    kg.init(cmd.qname, ((JdbcClass)cmd.storeClass).table, con);
                    con.commit();
                }
            }
        } catch (SQLException e) {
            throw BindingSupportImpl.getInstance().datastore(e.toString(),
                    e);
        } finally {
            if (con != null) {
                try {
                    conSrc.returnConnection(con);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Install a single StateAndOIDFactory for all classes that uses hand
     * written State and OID classes.
     */
    private void installGenericStateAndOIDFactory() {
        StateAndOIDFactory f = new GenericFactory();
        ClassMetaData[] classes = jmd.classes;
        for (int i = 0; i < classes.length; i++) {
            classes[i].stateAndOIDFactory = f;
        }
    }

    /**
     * Install a StateAndOIDFactory for each class that uses the generated
     * State and OID classes.
     */

    private void installHyperdriveStateAndOIDFactory(ClassLoader loader) {
        try {
            ClassMetaData[] classes = jmd.classes;
            for (int i = 0; i < classes.length; i++) {
                ClassMetaData cmd = classes[i];
                if (cmd.horizontal) {
                    continue;
                }
                Class oidClass = Class.forName(cmd.oidClassName, true, loader);
                Class stateClass = Class.forName(cmd.stateClassName, true, loader);
                if (cmd.isInHierarchy()) {
                    cmd.stateAndOIDFactory =
                            new HyperdriveFactoryHierarchy(oidClass,
                                    stateClass);
                } else {
                    cmd.stateAndOIDFactory =
                            new HyperdriveFactory(oidClass,
                                    stateClass);
                }
            }
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal(e.toString(), e);
        }
    }


    public void destroy() {
        if (conSrc != null) {
            conSrc.destroy();
        }
        compiledQueryCache.clear();
    }

    /**
     * If we do not have a JdbcConnectionSource then create a connection
     * pool otherwise do nothing.
     */
    public void createPool(ClassLoader loader) {
        if (conSrc == null) {

            if (ds1 != null) {
                if (ds2 != null) {
                    conSrc = new ExternalJdbcConnectionSource(ds1,
                            jdbcConfig.enlistedConnections, ds2, null, pes);
                } else {
                    //try and create a internal if there is properties
                    if (jdbcDriver == null) {
                        jdbcDriver = SqlDriver.createJdbcDriver(jdbcConfig.driver, loader);
                    }
                    JDBCConnectionPool interPool = null;
                    if (jdbcConfig.url != null) {
                        //must check if there is any properties to create this
                        interPool = new JDBCConnectionPool(jdbcConfig, pes, jdbcDriver, sqlDriver);
                    }
                    conSrc = new ExternalJdbcConnectionSource(ds1,
                            jdbcConfig.enlistedConnections, ds2, interPool, pes);
                }
            } else {

                if (jdbcDriver == null) {
                    jdbcDriver = SqlDriver.createJdbcDriver(jdbcConfig.driver, loader);
                    if (sqlDriver != null) {
                        sqlDriver.customizeForDriver(jdbcDriver);
                    }
                }
                conSrc = new JDBCConnectionPool(jdbcConfig, pes, jdbcDriver,
                        sqlDriver);

            }

        }
    }

    public StorageManager getStorageManager() {
        return new JdbcStorageManager(jmd, conSrc, sqlDriver, cache,
                compiledQueryCache, pes, jdbcConfig);
    }

    public void returnStorageManager(StorageManager sm) {
        sm.destroy();
    }

    public boolean isEnlistedDataSource() {
        return jdbcConfig.enlistedConnections;
    }

    public LogEventStore getPerfEventStore() {
        return pes;
    }

    public StorageCache getCache() {
        return cache;
    }

    public JdbcConnectionSource getConnectionSource() {
        return conSrc;
    }

    public ModelMetaData getModelMetaData() {
        return jmd;
    }

    public SqlDriver getSqlDriver() {
        return sqlDriver;
    }

    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public Object getDatastoreConnection() {
        try {
            return conSrc.getConnection(false, false);
        } catch (SQLException e) {
            throw BindingSupportImpl.getInstance().datastore(e.toString(), e);
        }
    }

    public void closeIdleDatastoreConnections() {
        conSrc.closeIdleConnections();
    }

    public JdbcMetaData getJdbcMetaData() {
        return (JdbcMetaData)jmd.jdbcMetaData;
    }

    public DataStoreInfo getDataStoreInfo() {
        DataStoreInfo info = new DataStoreInfo();
        info.setDataStoreType(sqlDriver.getName());
        info.setName("main");
        info.setAutoIncSupported(sqlDriver.isAutoIncSupported());
        info.setJdbc(true);
        info.setMajorVersion(sqlDriver.getMajorVersion());
        info.setPreparedStatementPoolingOK(
                sqlDriver.isPreparedStatementPoolingOK());
        info.setScrollableResultSetSupported(
                sqlDriver.isScrollableResultSetSupported());
        info.setSelectForUpdate(sqlDriver.getSelectForUpdate() == null
                ? null
                : new String(sqlDriver.getSelectForUpdate()));
        return info;
    }

    public void addMetrics(List list) {
        if (conSrc instanceof HasMetrics) {
            ((HasMetrics)conSrc).addMetrics(list);
        }
        if (cache instanceof HasMetrics) {
            ((HasMetrics)cache).addMetrics(list);
        }
    }

    public void sampleMetrics(int[][] buf, int pos) {
        if (conSrc instanceof HasMetrics) {
            ((HasMetrics)conSrc).sampleMetrics(buf, pos);
        }
        if (cache instanceof HasMetrics) {
            ((HasMetrics)cache).sampleMetrics(buf, pos);
        }
    }

    public StorageManagerFactory getInnerStorageManagerFactory() {
        return null;
    }

    public void supportedOptions(Set options) {
    }

    public CompiledQueryCache getCompiledQueryCache() {
        return compiledQueryCache;
    }

    /**
     * Factory that returns instances of the non-generated State and OID
     * classes.
     */
    private static class GenericFactory
            implements StateAndOIDFactory {

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            return new JdbcGenericOID(cmd, resolved);
        }

        public State createState(ClassMetaData cmd) {
            return new JdbcGenericState(cmd);
        }

        public NewObjectOID createNewObjectOID(ClassMetaData cmd) {
            return new JdbcNewObjectOID(cmd);
        }

        public OID createUntypedOID() {
            throw BindingSupportImpl.getInstance().unsupported(
                "Untyped OIDs are not supported by the datastore");
        }
    }

    /**
     * Factory for classes not in a hierachy.
     */
    private static class HyperdriveFactory implements StateAndOIDFactory {

        protected final Class stateClass;
        protected final Class oidClass;

        public HyperdriveFactory(Class oidClass, Class stateClass) {
            this.oidClass = oidClass;
            this.stateClass = stateClass;
        }

        public State createState(ClassMetaData cmd) {
            try {
                return (State)stateClass.newInstance();
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            try {
                return (OID)oidClass.newInstance();
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }

        public NewObjectOID createNewObjectOID(ClassMetaData cmd) {
            return new JdbcNewObjectOID(cmd);
        }

        public OID createUntypedOID() {
            throw BindingSupportImpl.getInstance().unsupported(
                "Untyped OIDs are not supported by the datastore");
        }
    }

    /**
     * Factory for classes in a hierarchy.
     */
    private static class HyperdriveFactoryHierarchy
            extends HyperdriveFactory {

        private transient Constructor oidCon;

        public HyperdriveFactoryHierarchy(Class oidClass, Class stateClass) {
            super(oidClass, stateClass);
            init();
        }

        private void init() {
            try {
                oidCon = oidClass.getConstructor(new Class[]{/*CHFC*/ClassMetaData.class/*RIGHTPAR*/,
                    /*CHFC*/Boolean.TYPE/*RIGHTPAR*/});
            } catch (NoSuchMethodException e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }

        private void writeObject(java.io.ObjectOutputStream out)
                throws IOException {
            out.defaultWriteObject();
        }

        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            init();
        }

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            try {
                return (OID)oidCon.newInstance(new Object[]{cmd,
                    resolved ? Boolean.TRUE : Boolean.FALSE});
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }
    }

}

