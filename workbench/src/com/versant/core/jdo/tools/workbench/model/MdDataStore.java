
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
package com.versant.core.jdo.tools.workbench.model;


import com.versant.core.jdo.sco.VersantSCOFactoryRegistry;
import com.versant.core.util.StringList;
import com.versant.core.metadata.MetaDataUtils;

import com.versant.core.common.config.ConfigParser;
import com.versant.core.metadata.MetaDataEnums;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.jdbc.JdbcKeyGeneratorFactory;
import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.*;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.util.StringList;
import com.versant.core.util.StringListParser;
import com.versant.core.common.Utils;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;


/**
 * Info on a DataStore for a MdProject.
 */
public class MdDataStore extends MdBase
        implements MdChangeListener, JdoExtensionKeys {

    private boolean dirty;
    private MdProject project;

    private String selectedDB;
    private SqlDriver sqlDriver;
    private JdbcTypeMapping[] driverTypeMappings = new JdbcTypeMapping[0];
    // this directly maps JDBC type codes to mappings from sqlDriver
    private Map driverJavaTypeMappings = new HashMap();
    // this maps java class names to mappings from sqlDriver;

    private static final int FIRST_TYPE = Types.BIT;
    private static final int LAST_TYPE =

    Types.REF;



    private String name;
    private int type;
    private String dbT;
    private String urlT = "";
    private String driverT = "";
    private String userT = "";
    private String passwordT = "";
    private String propertiesT = "";
    private String maxActiveT = Integer.toString(
            ConfigParser.DEFAULT_STORE_MAX_ACTIVE);
    private String maxIdleT = Integer.toString(
            ConfigParser.DEFAULT_STORE_MAX_IDLE);
    private String minIdleT = Integer.toString(
            ConfigParser.DEFAULT_STORE_MIN_IDLE);
    private String reservedT = Integer.toString(
            ConfigParser.DEFAULT_STORE_RESERVED);
    private String initSQLT = "";
    private String validateSQLT = "";
    private String waitForConOnStartupT = "false";
    private String testOnAllocT = "false";
    private String testOnReleaseT = "false";
    private String checkSchemaOnStartupT = "false";
    private String testOnExceptionT = "true";
    private String testWhenIdleT;
    private String retryIntervalMsT = "1000";
    private String retryCountT = Integer.toString(
            ConfigParser.DEFAULT_STORE_RETRY_COUNT);
    private String psCacheMaxT = "";
    private String conTimeoutT;
    private String maxConAgeT;
    private String testIntervalT;
    private String blockWhenFullT;
    private String isolationLevel;

    private String jdbcOptimisticLocking;
    private String readOnly;
    private String cacheStrategy;
    private String jdbcDoNotCreateTable;
    private String refsInDefaultFetchGroup;
    private String createOIDAtMakePersistent;
    private String jdbcDisableStatementBatching;
    private String jdbcDisablePsPool;
    private String jdbcInheritance;
    private String jdbcClassId;
    private String managedOneToMany;
    private String managedManyToMany;

    private String jdbcKeyGenerator;
    private MdPropertySet jdbcKeyGeneratorProps;

    private String jdbcNameGenerator;
    private MdPropertySet jdbcNameGeneratorProps;
    private ControlParams jdbcMigrationControl;
    private MdPropertySet jdbcMigrationControlProps;

    public static final int TYPE_JDBC = 1;
    public static final int TYPE_MEM = 2;
    public static final int TYPE_VDS = 4;

    private List classes = new ArrayList();
    private List fields = new ArrayList();

    private List typeMappings = new ArrayList();
    private List javaTypeMappings = new ArrayList();
    private List scoMappings = new ArrayList(); // of MdSCOMapping

    private HashMap typeMappingMap = new HashMap();
    // MdJdbcTypeMapping.Key -> MdJdbcTypeMapping
    private HashMap javaTypeMappingMap = new HashMap();
    // MdJdbcJavaTypeMapping.Key -> MdJdbcJavaTypeMapping
    private HashMap scoMappingsMap = new HashMap();
    // String SCO classname -> String factory name

    public MdDataStore(MdProject project, String name) throws Exception {
        this.project = project;
        this.name = name;
        jdbcKeyGeneratorProps = new MdPropertySet(project, false);
        jdbcNameGeneratorProps = new MdPropertySet(project, false);
        jdbcMigrationControlProps = new MdPropertySet(project, false);
        createJdbcKeyGenerator();
        createJdbcNameGenerator();
        createJdbcMigrationControlGenerator();
        project.addMdChangeListener(this);
    }

    public void nuke() {
        project.removeMdChangeListener(this);
        removeMdChangeListener(project);
        removeMdDsTypeChangedListener(project);
    }

    public MdProject getProject() {
        return project;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            fireMdChangeEvent(project, this);
        }
    }

    /**
     * Save everything except our JDBC and Java type mappings to p.
     * Allways se javax.jdo.option.ConnectionURL and so on if they were
     * originally present
     */
    public void save(PropertySaver p, boolean forWorkbench, boolean verbose){
        String url = getUrl();
        String nameFromURL = null;
        String driverFromURL = null;
        if (url != null) {
            nameFromURL = SqlDriver.getNameFromURL(url);
            driverFromURL = SqlDriver.getDriverFromURL(url);
        }
        boolean isvds = isVds();
        if (!isvds) {
            p.add(ConfigParser.STD_CON_DRIVER_NAME,
                    forWorkbench ? getDriver() : getDriverT(), driverFromURL);
        }
        p.add(ConfigParser.STD_CON_USER_NAME,
                forWorkbench ? getUser() : getUserT());
        p.add(ConfigParser.STD_CON_PASSWORD,
                forWorkbench ? getPassword() : getPasswordT());
        p.add(ConfigParser.STD_CON_URL,
                forWorkbench ? url : getUrlT());
        p.add(ConfigParser.STORE_PROPERTIES,
                forWorkbench ? getPropertiesEncoded() : getPropertiesTEncoded());
        p.add(ConfigParser.STORE_DB,
                forWorkbench ? getDb() : getDbT(),
                isvds ? "versant" : nameFromURL);
        p.add(ConfigParser.STORE_MAX_ACTIVE,
                forWorkbench ? getMaxActive() : getMaxActiveT(),
                ConfigParser.DEFAULT_STORE_MAX_ACTIVE);
        p.add(ConfigParser.STORE_MAX_IDLE,
                forWorkbench ? getMaxIdle() : getMaxIdleT(),
                ConfigParser.DEFAULT_STORE_MAX_IDLE);
        p.add(ConfigParser.STORE_MIN_IDLE,
                forWorkbench ? getMinIdle() : getMinIdleT(),
                ConfigParser.DEFAULT_STORE_MIN_IDLE);
        p.add(ConfigParser.STORE_RESERVED,
                forWorkbench ? getReserved() : getReservedT(),
                ConfigParser.DEFAULT_STORE_RESERVED);

        p.add(ConfigParser.STORE_INIT_SQL,
                forWorkbench ? getInitSQL() : getInitSQLT());
        p.add(ConfigParser.STORE_WAIT_FOR_CON_ON_STARTUP,
                forWorkbench ? "false" : waitForConOnStartupT,
                getBooleanString(
                        ConfigParser.DEFAULT_STORE_WAIT_FOR_CON_ON_STARTUP));
        p.add(ConfigParser.STORE_VALIDATE_SQL,
                forWorkbench ? getValidateSQL() : getValidateSQLT());
        p.add(ConfigParser.STORE_TEST_ON_ALLOC,
                forWorkbench ? getTestOnAlloc() : testOnAllocT,
                getBooleanString(ConfigParser.DEFAULT_STORE_TEST_ON_ALLOC));
        p.add(ConfigParser.STORE_TEST_ON_RELEASE,
                forWorkbench ? getTestOnRelease() : testOnReleaseT,
                getBooleanString(ConfigParser.DEFAULT_STORE_TEST_ON_RELEASE));
        p.add(ConfigParser.STORE_TEST_ON_EXCEPTION,
                forWorkbench ? getTestOnException() : testOnExceptionT,
                getBooleanString(ConfigParser.DEFAULT_STORE_TEST_ON_EXCEPTION));
        p.add(ConfigParser.STORE_TEST_WHEN_IDLE,
                forWorkbench ? getTestWhenIdle() : testWhenIdleT, true);
        p.add(ConfigParser.STORE_VALIDATE_MAPPING_ON_STARTUP,
                forWorkbench ? "false" : checkSchemaOnStartupT,
                getBooleanString(isVds()));
        p.add(ConfigParser.STORE_RETRY_INTERVAL_MS,
                forWorkbench ? getRetryIntervalMs() : retryIntervalMsT,
                ConfigParser.DEFAULT_STORE_RETRY_INTERVAL_MS);
        p.add(ConfigParser.STORE_RETRY_COUNT,
                forWorkbench ? getRetryCount() : retryCountT,
                ConfigParser.DEFAULT_STORE_RETRY_COUNT);
        p.add(ConfigParser.STORE_DISABLE_BATCHING,
                jdbcDisableStatementBatching, false);
        p.add(ConfigParser.STORE_DISABLE_PS_CACHE,
                jdbcDisablePsPool, false);
        p.add(ConfigParser.STORE_PS_CACHE_MAX,
                forWorkbench ? getPsCacheMax() : psCacheMaxT, 0);
        p.add(ConfigParser.STORE_TEST_INTERVAL,
                forWorkbench ? getTestInterval() : testIntervalT, 120);
        p.add(ConfigParser.STORE_CON_TIMEOUT,
                forWorkbench ? getConTimeout() : conTimeoutT, 120);
        p.add(ConfigParser.STORE_MAX_CON_AGE,
                forWorkbench ? getMaxConAge() : maxConAgeT,
                ConfigParser.DEFAULT_MAX_CON_AGE);
        p.add(ConfigParser.STORE_ISOLATION_LEVEL, isolationLevel,
                ConfigParser.ISOLATION_LEVEL_READ_COMMITTED);
        p.add(ConfigParser.STORE_BLOCK_WHEN_FULL,
                forWorkbench ? getBlockWhenFull() : blockWhenFullT, true);

        String be = ConfigParser.STORE_EXT;
        save(p, be, JdoExtensionKeys.JDBC_OPTIMISTIC_LOCKING,
                jdbcOptimisticLocking, "version");
        save(p, be, JdoExtensionKeys.READ_ONLY, readOnly, "false");
        save(p, be, JdoExtensionKeys.CACHE_STRATEGY, cacheStrategy, "yes");
        save(p, be, JdoExtensionKeys.JDBC_INHERITANCE, jdbcInheritance,
                JdbcMetaDataEnums.INHERITANCE_FLAT);
        save(p, be, JdoExtensionKeys.JDBC_CLASS_ID, jdbcClassId,
                JdoExtension.HASH_VALUE);
        save(p, be, JdoExtensionKeys.JDBC_KEY_GENERATOR, jdbcKeyGenerator,
                jdbcKeyGeneratorProps.getValues(),
                jdbcKeyGeneratorProps.getDefaultValues());
        save(p, be, JdoExtensionKeys.JDBC_DO_NOT_CREATE_TABLE,
                jdbcDoNotCreateTable, "false");
        save(p, be, JdoExtensionKeys.OIDS_IN_DEFAULT_FETCH_GROUP,
                refsInDefaultFetchGroup, "true");
        save(p, be, JdoExtensionKeys.CREATE_OID_AT_MAKE_PERSISTENT,
                createOIDAtMakePersistent, null);

        p.add(ConfigParser.STORE_MANAGED_ONE_TO_MANY,
                managedOneToMany,
                ConfigParser.DEFAULT_STORE_MANAGED_ONE_TO_MANY);
        p.add(ConfigParser.STORE_MANAGED_MANY_TO_MANY,
                managedManyToMany,
                ConfigParser.DEFAULT_STORE_MANAGED_MANY_TO_MANY);

        p.add(ConfigParser.STORE_NAMEGEN, jdbcNameGenerator,
                jdbcNameGeneratorProps.getValues(),
                jdbcNameGeneratorProps.getDefaultValues());
        p.add(ConfigParser.STORE_MIGRATION_CONTROLS, null,
                jdbcMigrationControlProps.getValues(),
                jdbcMigrationControlProps.getDefaultValues());
        if (!forWorkbench) {
            setDirty(false);
        }
    }

    private void save(PropertySaver p, String base, int ext, String value,
            String def) {
        p.add(base + JdoExtension.toKeyString(ext), value, def);
    }

    private void save(PropertySaver p, String base, int ext, String cname,
            HashMap props, Map defaults) {
        p.add(base + JdoExtension.toKeyString(ext), cname, props, defaults);
    }

    /**
     * Load everything except out JDBC and Java type mappings from p. Do
     * you load the db property yet as this must be set last.
     * Allways accept standard javax.jdo.option.ConnectionURL and so on.
     */
    public void load(Properties p) throws Exception {

        String url = p.getProperty(ConfigParser.STD_CON_URL);
        setUrlT(url);
        String driverFromURL = SqlDriver.getDriverFromURL(url);
        setDriverT(p.getProperty(ConfigParser.STD_CON_DRIVER_NAME,driverFromURL));

        if(!MdUtils.isStringNotEmpty(getDriverT())){
            setDriverT(driverFromURL);
        }

        setUserT(p.getProperty(ConfigParser.STD_CON_USER_NAME));
        setPasswordT(p.getProperty(ConfigParser.STD_CON_PASSWORD));

        setPropertiesT(p.getProperty(ConfigParser.STORE_PROPERTIES));
        setMaxActiveT(p.getProperty(ConfigParser.STORE_MAX_ACTIVE,
                Integer.toString(ConfigParser.DEFAULT_STORE_MAX_ACTIVE)));
        setMaxIdleT(p.getProperty(ConfigParser.STORE_MAX_IDLE,
                Integer.toString(ConfigParser.DEFAULT_STORE_MAX_IDLE)));
        setMinIdleT(p.getProperty(ConfigParser.STORE_MIN_IDLE,
                Integer.toString(ConfigParser.DEFAULT_STORE_MIN_IDLE)));
        setReservedT(p.getProperty(ConfigParser.STORE_RESERVED,
                Integer.toString(ConfigParser.DEFAULT_STORE_RESERVED)));
        initSQLT = p.getProperty(ConfigParser.STORE_INIT_SQL);
        waitForConOnStartupT = p.getProperty(
                ConfigParser.STORE_WAIT_FOR_CON_ON_STARTUP);
        validateSQLT = p.getProperty(ConfigParser.STORE_VALIDATE_SQL);
        testOnAllocT = p.getProperty(ConfigParser.STORE_TEST_ON_ALLOC);
        testOnReleaseT = p.getProperty(ConfigParser.STORE_TEST_ON_RELEASE);
        testOnExceptionT = p.getProperty(ConfigParser.STORE_TEST_ON_EXCEPTION);
        testWhenIdleT = p.getProperty(ConfigParser.STORE_TEST_WHEN_IDLE);
        checkSchemaOnStartupT = p.getProperty(
                ConfigParser.STORE_VALIDATE_MAPPING_ON_STARTUP);
        retryIntervalMsT = p.getProperty(ConfigParser.STORE_RETRY_INTERVAL_MS);
        retryCountT = p.getProperty(ConfigParser.STORE_RETRY_COUNT);
        psCacheMaxT = p.getProperty(ConfigParser.STORE_PS_CACHE_MAX);
        testIntervalT = p.getProperty(ConfigParser.STORE_TEST_INTERVAL);
        conTimeoutT = p.getProperty(ConfigParser.STORE_CON_TIMEOUT);
        maxConAgeT = p.getProperty(ConfigParser.STORE_MAX_CON_AGE);
        blockWhenFullT = p.getProperty(ConfigParser.STORE_BLOCK_WHEN_FULL);

        isolationLevel =
                p.getProperty(ConfigParser.STORE_ISOLATION_LEVEL);
        jdbcDisableStatementBatching =
                p.getProperty(ConfigParser.STORE_DISABLE_BATCHING);
        jdbcDisablePsPool =
                p.getProperty(ConfigParser.STORE_DISABLE_PS_CACHE);

        String be = ConfigParser.STORE_EXT;
        jdbcOptimisticLocking = get(p, be,
                JdoExtensionKeys.JDBC_OPTIMISTIC_LOCKING);
        readOnly = get(p, be, JdoExtensionKeys.READ_ONLY);
        cacheStrategy = get(p, be, JdoExtensionKeys.CACHE_STRATEGY);
        jdbcInheritance = get(p, be, JdoExtensionKeys.JDBC_INHERITANCE);
        jdbcClassId = get(p, be, JdoExtensionKeys.JDBC_CLASS_ID);
        if (jdbcClassId == null) {
            // for backwards compatibility with old jdbc-inheritance-no-classid
            String s = p.getProperty(
                    be + ConfigParser.JDBC_INHERITANCE_NO_CLASSID);
            if ("true".equals(s)) jdbcClassId = JdoExtension.NO_VALUE;
        }
        jdbcDoNotCreateTable = get(p, be,
                JdoExtensionKeys.JDBC_DO_NOT_CREATE_TABLE);
        refsInDefaultFetchGroup = get(p, be,
                JdoExtensionKeys.OIDS_IN_DEFAULT_FETCH_GROUP);
        createOIDAtMakePersistent = get(p, be,
                JdoExtensionKeys.CREATE_OID_AT_MAKE_PERSISTENT);

        managedOneToMany = p.getProperty(
                ConfigParser.STORE_MANAGED_ONE_TO_MANY);
        managedManyToMany = p.getProperty(
                ConfigParser.STORE_MANAGED_MANY_TO_MANY);

        jdbcKeyGenerator = getClassAndProps(p, be,
                JdoExtensionKeys.JDBC_KEY_GENERATOR, jdbcKeyGeneratorProps);
        jdbcNameGenerator = getClassAndProps(p,
                ConfigParser.STORE_NAMEGEN, jdbcNameGeneratorProps);
        getClassAndProps(p, ConfigParser.STORE_MIGRATION_CONTROLS,
                jdbcMigrationControlProps);
        setDirty(false);
    }

    private String getBooleanString(boolean b) {
        return b ? "true" : "false";
    }

    private String getClassAndProps(Properties p, String base, int ext,
            MdPropertySet props) {
        return getClassAndProps(p, base + JdoExtension.toKeyString(ext), props);
    }

    private String getClassAndProps(Properties p, String property,
            MdPropertySet props) {
        props.clear();
        String s = p.getProperty(property);
        HashMap values = props.getValues();
        for (Iterator it = props.getDefaultValues().keySet().iterator();
             it.hasNext();) {
            Object k = it.next();
            String v = p.getProperty(property + "." + k);
            if (v != null) {
                values.put(k, v);
            }
        }
        return MdUtils.isStringNotEmpty(s) ? s : null;
    }

    private String get(Properties p, String base, int ext) {
        return p.getProperty(base + JdoExtension.toKeyString(ext));
    }

    /**
     * The token replacement properties file has been changed.
     */
    public void tokenPropsChanged() {
        checkDBType();
        if (selectedDB == null) {
            updateSqlDriver();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name.equals(name)) return;
        this.name = name;
        setDirty(true);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        int oldType = this.type;
        this.type = type;
        if (oldType != type) {
            setDirty(true);
            fireDataStoreTypeChanged(oldType, type);
        }
    }

    public String getDb() {
        return resolveToken(dbT);
    }

    private String resolveToken(String s) {
        return project.resolveToken(s);
    }

    public String getDbT() {
        return dbT;
    }

    public void setDbT(String dbT) {
        this.dbT = dbT;
        setDirty(true);
        checkDBType();
        if (selectedDB == null) {
            updateSqlDriver();
        }
    }

    private void checkDBType() {
        String db = getDb();
        if (!MdUtils.isStringNotEmpty(db)) {
            String url = getUrl();
            if (Utils.isVersantURL(url)) {
                db = "versant";
            } else {
                db = SqlDriver.getNameFromURL(url);
            }

        }
        if (Utils.isVersantDatabaseType(db)) {
            setType(TYPE_VDS);
        } else {
            setType(TYPE_JDBC);
        }
    }

    public String getUrl() {
        return resolveToken(urlT);
    }

    public String getUrlT() {
        return urlT;
    }

    public void setUrlT(String urlT) {
        this.urlT = urlT;
        setDirty(true);
        checkDBType();
    }

    public String getDriver() {
        return resolveToken(driverT);
    }

    public String getDriverT() {
        return driverT;
    }

    public void setDriverT(String driverT) {
        this.driverT = driverT;
        setDirty(true);
    }


    public void fillFromDriver(MdDriver d) {
        setDriverT(d.getDriverClass());
        setDbT(d.getDatabase());
        setUrlT(d.getSampleURL());
        setPropertiesTEncoded(d.getProperties());
    }


    public String getUser() {
        return resolveToken(userT);
    }

    public String getUserT() {
        return userT;
    }

    public void setUserT(String userT) {
        this.userT = userT;
        setDirty(true);
    }

    public String getPassword() {
        return resolveToken(passwordT);
    }

    public String getPasswordT() {
        return passwordT;
    }

    public void setPasswordT(String passwordT) {
        this.passwordT = passwordT;
        setDirty(true);
    }

    public String getProperties() {
        if (propertiesT == null) return null;
        return resolveToken(propertiesT).replace(';', '\n');
    }

    public String getPropertiesT() {
        return propertiesT == null ? "" : propertiesT.replace(';', '\n');
    }

    public void setPropertiesT(String propertiesT) {
        if (propertiesT == null) {
            this.propertiesT = null;
        } else {
            this.propertiesT = propertiesT.replace('\n', ';');
        }
    }

    public String getPropertiesEncoded() {
        return resolveToken(propertiesT);
    }

    public String getPropertiesTEncoded() {
        return propertiesT;
    }

    public void setPropertiesTEncoded(String propertiesT) {
        if (propertiesT == null) propertiesT = "";
        this.propertiesT = propertiesT;
    }

    public void validate() throws Exception {
        if (name == null || name.length() == 0) {
            throw new MdVetoException(
                    "You must capture the name of the data store");
        }
    }

    /**
     * Replace all properties with appropriate ant filter tokens and write
     * current properties to optional property saver.
     */
    public void fillWithAntTokens(String prefix, PropertySaver p)
            throws Exception {
        String k = prefix + "DB";
        if (p != null) p.add(k, dbT);
        dbT = '@' + k + '@';
        k = prefix + "URL";
        if (p != null) p.add(k, urlT);
        urlT = '@' + k + '@';
        k = prefix + "DRIVER";
        if (p != null) p.add(k, driverT);
        driverT = '@' + k + '@';
        k = prefix + "USER";
        if (p != null) p.add(k, userT);
        userT = '@' + k + '@';
        k = prefix + "PASSWORD";
        if (p != null) p.add(k, passwordT);
        passwordT = '@' + k + '@';
        k = prefix + "PROPERTIES";
        if (p != null) p.add(k, propertiesT);
        propertiesT = '@' + k + '@';
        k = prefix + "MAX.ACTIVE";
        if (p != null) p.add(k, maxActiveT);
        maxActiveT = '@' + k + '@';
        k = prefix + "MAX.IDLE";
        if (p != null) p.add(k, maxIdleT);
        maxIdleT = '@' + k + '@';
        k = prefix + "MIN.IDLE";
        if (p != null) p.add(k, minIdleT);
        minIdleT = '@' + k + '@';
        k = prefix + "RESERVED";
        if (p != null) p.add(k, reservedT);
        reservedT = '@' + k + '@';
        if (initSQLT != null) {
            k = prefix + "INIT.SQL";
            if (p != null) p.add(k, initSQLT);
            initSQLT = '@' + k + '@';
        }
        if (validateSQLT != null) {
            k = prefix + "VALIDATE.SQL";
            if (p != null) p.add(k, validateSQLT);
            validateSQLT = '@' + k + '@';
        }
        if (waitForConOnStartupT != null) {
            k = prefix + "WAIT.FOR.CON.ON.STARTUP";
            if (p != null) p.add(k, waitForConOnStartupT);
            waitForConOnStartupT = '@' + k + '@';
        }
        if (testOnAllocT != null) {
            k = prefix + "TEST.ON.ALLOC";
            if (p != null) p.add(k, testOnAllocT);
            testOnAllocT = '@' + k + '@';
        }
        if (testOnReleaseT != null) {
            k = prefix + "TEST.ON.RELEASE";
            if (p != null) p.add(k, testOnReleaseT);
            testOnReleaseT = '@' + k + '@';
        }
        if (testOnExceptionT != null) {
            k = prefix + "TEST.ON.EXCEPTION";
            if (p != null) p.add(k, testOnExceptionT);
            testOnExceptionT = '@' + k + '@';
        }
        if (testWhenIdleT != null) {
            k = prefix + "TEST.WHEN.IDLE";
            if (p != null) p.add(k, testWhenIdleT);
            testWhenIdleT = '@' + k + '@';
        }
        if (checkSchemaOnStartupT != null) {
            k = prefix + "CHECK.SCHEMA.ON.STARTUP";
            if (p != null) p.add(k, checkSchemaOnStartupT);
            checkSchemaOnStartupT = '@' + k + '@';
        }
        if (retryIntervalMsT != null) {
            k = prefix + "RETRY.INTERVAL";
            if (p != null) p.add(k, retryIntervalMsT);
            retryIntervalMsT = '@' + k + '@';
        }
        if (retryCountT != null) {
            k = prefix + "RETRY.COUNT";
            if (p != null) p.add(k, retryCountT);
            retryCountT = '@' + k + '@';
        }
        if (psCacheMaxT != null) {
            k = prefix + "PS.CACHE.MAX";
            if (p != null) p.add(k, psCacheMaxT);
            psCacheMaxT = '@' + k + '@';
        }
        if (testIntervalT != null) {
            k = prefix + "TEST.INTERVAL";
            if (p != null) p.add(k, testIntervalT);
            testIntervalT = '@' + k + '@';
        }
        if (conTimeoutT != null) {
            k = prefix + "CON.TIMEOUT";
            if (p != null) p.add(k, conTimeoutT);
            conTimeoutT = '@' + k + '@';
        }
        if (maxConAgeT != null) {
            k = prefix + "CON.LIFESPAN";
            if (p != null) p.add(k, maxConAgeT);
            maxConAgeT = '@' + k + '@';
        }
        setDirty(true);
    }

    /**
     * Load our driver using the supplied ClassLoader. This will deregister
     * it from the DriverManager using a horrible hack to make sure that the
     * class doing the deregister'ing is from the same classloader that
     * loaded it.
     */
    public Driver loadDriver(ClassLoader cl) throws Exception {
        String name = getDriver();
        if (name == null || name.trim().length() == 0) {
            name = SqlDriver.getDriverFromURL(getUrl());
        }

        java.lang.Class cls = java.lang.Class.forName(name, true, cl);


        Driver d = (Driver)cls.newInstance();

        Class unloader = Class.forName(MdClassLoader.DRIVER_UNLOADER_NAME,
                true, cl);
        Method m = unloader.getMethod("unload", new Class[]{Driver.class});
        m.invoke(null, new Object[]{d});

        return d;
    }

    /**
     * Attempt to connect using the supplied ClassLoader.
     */
    public Connection connect(ClassLoader cl) throws Exception {
        Driver d = loadDriver(cl);
        Properties p = new Properties();
        String s = getUser();
        if (s != null) p.setProperty("user", s);
        s = getPassword();
        if (s != null) p.setProperty("password", s);
        MdUtils.parseProperties(getPropertiesEncoded(), p);
        return d.connect(getUrl(), p);
    }

    public String getMaxActive() {
        return resolveToken(maxActiveT);
    }

    public String getMaxActiveT() {
        return maxActiveT;
    }

    public void setMaxActiveT(String maxActiveT) {
        this.maxActiveT = maxActiveT;
        setDirty(true);
    }

    public String getMaxIdle() {
        return resolveToken(maxIdleT);
    }

    public String getMaxIdleT() {
        return maxIdleT;
    }

    public void setMaxIdleT(String maxIdleT) {
        this.maxIdleT = maxIdleT;
        setDirty(true);
    }

    public String getMinIdle() {
        return resolveToken(minIdleT);
    }

    public String getMinIdleT() {
        return minIdleT;
    }

    public void setMinIdleT(String minIdleT) {
        this.minIdleT = minIdleT;
        setDirty(true);
    }

    public String getReserved() {
        return resolveToken(reservedT);
    }

    public String getReservedT() {
        return reservedT;
    }

    public void setReservedT(String reservedT) {
        this.reservedT = reservedT;
        setDirty(true);
    }

    public String getInitSQL() {
        return resolveToken(initSQLT);
    }

    public String getInitSQLT() {
        return initSQLT;
    }

    public void setInitSQLT(String initSQLT) {
        this.initSQLT = initSQLT;
        setDirty(true);
    }

    public String getValidateSQL() {
        return resolveToken(validateSQLT);
    }

    public String getValidateSQLT() {
        return validateSQLT;
    }

    public void setValidateSQLT(String validateSQLT) {
        this.validateSQLT = validateSQLT;
        setDirty(true);
    }

    public String getWaitForConOnStartup() {
        return resolveToken(waitForConOnStartupT);
    }

    public MdValue getWaitForConOnStartupT() {
        return createBoolMdValue(waitForConOnStartupT, "false");
    }

    private MdValue createBoolMdValue(String text, String def) {
        MdValue v = new MdValue(text);
        v.setDefText(def);
        v.setPickList(PickLists.BOOLEAN);
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setWaitForConOnStartupT(MdValue v) {
        waitForConOnStartupT = v.getText();
        setDirty(true);
    }

    public String getTestOnAlloc() {
        return resolveToken(testOnAllocT);
    }

    public MdValue getTestOnAllocT() {
        return createBoolMdValue(testOnAllocT, "false");
    }

    public void setTestOnAllocT(MdValue v) {
        this.testOnAllocT = v.getText();
        setDirty(true);
    }

    public String getTestOnRelease() {
        return resolveToken(testOnReleaseT);
    }

    public MdValue getTestOnReleaseT() {
        return createBoolMdValue(testOnReleaseT, "false");
    }

    public String getCheckSchemaOnStartup() {
        return resolveToken(checkSchemaOnStartupT);
    }

    public MdValue getCheckSchemaOnStartupT() {
        return createBoolMdValue(checkSchemaOnStartupT, "false");
    }

    public void setTestOnReleaseT(MdValue v) {
        this.testOnReleaseT = v.getText();
        setDirty(true);
    }

    public void setCheckSchemaOnStartupT(MdValue v) {
        this.checkSchemaOnStartupT = v.getText();
        setDirty(true);
    }

    public String getTestOnException() {
        return resolveToken(testOnExceptionT);
    }

    public MdValue getTestOnExceptionT() {
        return createBoolMdValue(testOnExceptionT, "true");
    }

    public void setTestOnExceptionT(MdValue v) {
        this.testOnExceptionT = v.getText();
        setDirty(true);
    }

    public String getTestWhenIdle() {
        return resolveToken(testWhenIdleT);
    }

    public MdValue getTestWhenIdleT() {
        return createBoolMdValue(testWhenIdleT, "true");
    }

    public void setTestWhenIdleT(MdValue v) {
        this.testWhenIdleT = v.getText();
        setDirty(true);
    }

    public String getBlockWhenFull() {
        return resolveToken(blockWhenFullT);
    }

    public MdValue getBlockWhenFullT() {
        return createBoolMdValue(blockWhenFullT, "true");
    }

    public void setBlockWhenFullT(MdValue v) {
        this.blockWhenFullT = v.getText();
        setDirty(true);
    }

    public String getRetryIntervalMs() {
        return resolveToken(retryIntervalMsT);
    }

    public MdValue getRetryIntervalMsT() {
        MdValue v = new MdValue(retryIntervalMsT);
        v.setDefText("1000");
        return v;
    }

    public void setRetryIntervalMsT(MdValue v) {
        this.retryIntervalMsT = v.getText();
        setDirty(true);
    }

    public String getRetryCount() {
        return resolveToken(retryCountT);
    }

    public MdValue getRetryCountT() {
        MdValue v = new MdValue(retryCountT);
        v.setDefText("10");
        return v;
    }

    public void setRetryCountT(MdValue v) {
        this.retryCountT = v.getText();
        setDirty(true);
    }

    public String getPsCacheMax() {
        return resolveToken(psCacheMaxT);
    }

    public String getPsCacheMaxT() {
        return psCacheMaxT;
    }

    public void setPsCacheMaxT(String psCacheMaxT) {
        this.psCacheMaxT = psCacheMaxT;
        setDirty(true);
    }

    public String getTestInterval() {
        return resolveToken(testIntervalT);
    }

    public MdValue getTestIntervalT() {
        MdValue v = new MdValue(testIntervalT);
        v.setDefText("120");
        return v;
    }

    public void setTestIntervalT(MdValue v) {
        this.testIntervalT = v.getText();
        setDirty(true);
    }

    public String getConTimeout() {
        return resolveToken(conTimeoutT);
    }

    public MdValue getConTimeoutT() {
        MdValue v = new MdValue(conTimeoutT);
        v.setDefText("120");
        return v;
    }

    public void setConTimeoutT(MdValue v) {
        this.conTimeoutT = v.getText();
        setDirty(true);
    }

    public String getMaxConAge() {
        return resolveToken(maxConAgeT);
    }

    public MdValue getMaxConAgeT() {
        MdValue v = new MdValue(maxConAgeT);
        v.setDefText(Integer.toString(ConfigParser.DEFAULT_MAX_CON_AGE));
        return v;
    }

    public void setMaxConAgeT(MdValue v) {
        this.maxConAgeT = v.getText();
        setDirty(true);
    }

    public String toString() {
        return getName();
    }

    public void metaDataChanged(MdChangeEvent e) {
        if (e.hasFlagSet(MdChangeEvent.FLAG_CLASSES_CHANGED)) {
            syncClassesAndFields();
        }
    }

    private void syncClassesAndFields() {
        List all = project.getAllClasses();
        classes.clear();
        int n = all.size();
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)all.get(i);
            c.setMdDataStore(this);
            classes.add(c);
        }
        Collections.sort(classes);
        syncFields();
    }

    /**
     * Sync our list of fields with our classes.
     */
    public void syncFields() {
        fields.clear();
        int n = classes.size();
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)classes.get(i);
            fields.addAll(c.getFieldList());
        }
    }

    /**
     * The class has been reanalyzed. This is called by MdClass when it
     * analyzes itself. Sync our list of fields with our classes.
     */
    public void classAnalyzed(MdClass c) {
        syncFields();
    }

    public List getClasses() {
        return classes;
    }

    public List getFields() {
        return fields;
    }

    public String getJdbcOptimisticLockingStr() {
        if (jdbcOptimisticLocking == null) return "version";
        return jdbcOptimisticLocking;
    }

    public MdValue getJdbcOptimisticLocking() {
        MdValue v = new MdValue(jdbcOptimisticLocking);
        v.setPickList(PickLists.JDBC_OPTIMISTIC_LOCKING);
        v.setDefText("version");
        return v;
    }

    public void setJdbcOptimisticLocking(MdValue v) {
        jdbcOptimisticLocking = v.getText();
    }

    public MdValue getIsolationLevel() {
        MdValue v = new MdValue(isolationLevel);
        v.setPickList(PickLists.ISOLATION_LEVEL);
        v.setDefText(ConfigParser.ISOLATION_LEVEL_READ_COMMITTED);
        return v;
    }

    public void setIsolationLevel(MdValue v) {
        isolationLevel = v.getText();
    }

    public String getReadOnlyStr() {
        if (readOnly == null) return "false";
        return readOnly;
    }

    public MdValue getReadOnly() {
        MdValue v = new MdValue(readOnly);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setReadOnly(MdValue v) {
        readOnly = v.getText();
    }

    public String getCacheStrategyStr() {
        if (cacheStrategy == null) return "yes";
        return cacheStrategy;
    }

    public MdValue getCacheStrategy() {
        MdValue v = new MdValue(cacheStrategy);
        v.setPickList(PickLists.CACHE_STRATEGY);
        v.setDefText("yes");
        return v;
    }

    public void setCacheStrategy(MdValue v) {
        cacheStrategy = v.getText();
    }

    public String getJdbcInheritanceStr() {
        if (jdbcInheritance == null) {
            return JdbcMetaDataEnums.INHERITANCE_FLAT;
        }
        return jdbcInheritance;
    }

    public MdValue getJdbcInheritance() {
        MdValue v = new MdValue(jdbcInheritance);
        v.setPickList(PickLists.JDBC_INHERITANCE);
        v.setDefText(JdbcMetaDataEnums.INHERITANCE_FLAT);
        return v;
    }

    public void setJdbcInheritance(MdValue v) {
        jdbcInheritance = v.getText();
    }

    public MdValue getJdbcClassId() {
        MdValue v = new MdValue(jdbcClassId);
        v.setPickList(PickLists.JDBC_CLASS_ID);
        v.setDefText(JdoExtension.HASH_VALUE);
        return v;
    }

    public void setJdbcClassId(MdValue v) {
        jdbcClassId = v.getText();
    }

    public boolean getInheritanceNoClassIdBool() {
        return JdoExtension.NO_VALUE.equals(jdbcClassId);
    }

    public String getJdbcKeyGeneratorStr() {
        if (jdbcKeyGenerator == null) return JdbcMetaDataBuilder.KEYGEN_HIGHLOW;
        return jdbcKeyGenerator;
    }

    public MdValue getJdbcKeyGenerator() {
        MdValue v = new MdValue(jdbcKeyGenerator);
        v.setPickList(PickLists.JDBC_KEY_GENERATOR);
        v.setDefText(JdbcMetaDataBuilder.KEYGEN_HIGHLOW);
        v.setOnlyFromPickList(false);
        return v;
    }

    private void createJdbcKeyGenerator() throws Exception {
        JdbcKeyGeneratorFactory f =
                createJdbcKeyGeneratorFactory(jdbcKeyGenerator);
        jdbcKeyGeneratorProps.setBean(f == null ? null : f.createArgsBean());
    }

    public JdbcKeyGeneratorFactory createJdbcKeyGeneratorFactory(String name) {
        JdbcKeyGeneratorFactory f;
        if (name != null) {
            if (name.equals(JdbcMetaDataBuilder.KEYGEN_HIGHLOW)) {
                return new HighLowJdbcKeyGenerator.Factory();
            } else if (name.equals(JdbcMetaDataBuilder.KEYGEN_AUTOINC)) {
                return new AutoIncJdbcKeyGenerator.Factory();
            } else {

                Object o = getProject().newInstance(name);
                f = (JdbcKeyGeneratorFactory)o;


            }
        } else {
            f = new HighLowJdbcKeyGenerator.Factory();
        }
        return f;
    }

    public void setJdbcKeyGenerator(MdValue v) throws Exception {
        jdbcKeyGenerator = v.getText();
        createJdbcKeyGenerator();
    }

    public MdValue getJdbcNameGenerator() {
        MdClassNameValue v = new MdClassNameValue(jdbcNameGenerator);
        v.setOnlyFromPickList(false);
        v.setDefText(DefaultJdbcNameGenerator.class.getName());
        v.setInvalid(jdbcNameGeneratorProps.getBean() == null);
        return v;
    }

    private void createJdbcNameGenerator() {
        JdbcNameGenerator ng;
        if (jdbcNameGenerator != null) {

            ng = (JdbcNameGenerator)project.newInstance(jdbcNameGenerator);


        } else if (sqlDriver != null) {
            ng = sqlDriver.createJdbcNameGenerator();
        } else {
            ng = new DefaultJdbcNameGenerator();
        }
        try {
            jdbcNameGeneratorProps.setBean(ng);
        } catch (Exception e) {
            project.getLogger().error(e);
        }
    }

    private void createJdbcMigrationControlGenerator() {
        if (jdbcMigrationControl == null) {
            jdbcMigrationControl = new ControlParams();
            try {
                jdbcMigrationControlProps.setBean(jdbcMigrationControl);
            } catch (Exception e) {
                project.getLogger().error(e);
            }
        }
    }

    public ControlParams getMigrations() {
        return jdbcMigrationControl;
    }

    public void setJdbcNameGenerator(MdValue v) throws Exception {
        this.jdbcNameGenerator = v.getText();
        createJdbcNameGenerator();
    }

    public String getJdbcDoNotCreateTableStr() {
        if (jdbcDoNotCreateTable == null) return "false";
        return jdbcDoNotCreateTable;
    }

    public MdValue getJdbcDoNotCreateTable() {
        MdValue v = new MdValue(jdbcDoNotCreateTable);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setJdbcDoNotCreateTable(MdValue v) {
        jdbcDoNotCreateTable = v.getText();
    }

    public String getRefsInDefaultFetchGroupStr() {
        if (refsInDefaultFetchGroup == null) return "true";
        return refsInDefaultFetchGroup;
    }

    public boolean getRefsInDefaultFetchGroupBoolean() {
        if (refsInDefaultFetchGroup == null) return true;
        return refsInDefaultFetchGroup.equals("true");
    }

    public MdValue getRefsInDefaultFetchGroup() {
        MdValue v = new MdValue(refsInDefaultFetchGroup);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("true");
        return v;
    }

    public void setRefsInDefaultFetchGroup(MdValue v) {
        refsInDefaultFetchGroup = v.getText();
    }

    public boolean getManagedOneToManyBool() {
        return managedOneToMany != null && "true".equals(managedOneToMany);
    }

    public MdValue getManagedOneToMany() {
        MdValue v = new MdValue(managedOneToMany);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("true");
        return v;
    }

    public void setManagedOneToMany(MdValue v) {
        managedOneToMany = v.getText();
    }

    public boolean getManagedManyToManyBool() {
        return "true".equals(managedManyToMany);
    }

    public MdValue getManagedManyToMany() {
        MdValue v = new MdValue(managedManyToMany);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setManagedManyToMany(MdValue v) {
        managedManyToMany = v.getText();
    }

    public MdValue getJdbcDisableStatementBatching() {
        MdValue v = new MdValue(jdbcDisableStatementBatching);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setJdbcDisableStatementBatching(MdValue v) {
        this.jdbcDisableStatementBatching = v.getText();
    }

    public MdValue getJdbcDisablePsPool() {
        MdValue v = new MdValue(jdbcDisablePsPool);
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setJdbcDisablePsPool(MdValue v) {
        this.jdbcDisablePsPool = v.getText();
    }

    public String getSelectedDBImp() {
        return selectedDB;
    }

    public MdValue getSelectedDB() {
        MdValue v = new MdValue(selectedDB);
        v.setPickList(PickLists.DB);
        v.setDefText("all");
        return v;
    }

    public void setSelectedDB(MdValue v) {
        String s = v.getText();
        if (s == null && selectedDB == null) return;
        if (s != null && selectedDB != null && s.equals(selectedDB)) return;
        selectedDB = s;
        checkDBType();
        updateSqlDriver();
        for (int i = classes.size() - 1; i >= 0; i--) {
            MdClass c = (MdClass)classes.get(i);
            c.selectedDBChanged(selectedDB);
        }
    }

    /**
     * One or more of our properties may have changed. Update any cached
     * info (e.g. type mappings etc). This is called after the properties
     * dialog has been displayed.
     */
    public void updateForMiscChanges() {
        sqlDriver = null;
        checkDBType();
        updateSqlDriver();
        for (int i = classes.size() - 1; i >= 0; i--) {
            MdClass c = (MdClass)classes.get(i);
            c.selectedDBChanged(selectedDB);
        }
        try {
            createJdbcKeyGenerator();
        } catch (Exception e) {
            project.getLogger().error(e);
        }
    }

    /**
     * Make sure the sqlDriver is in sync with the selectedDB or the
     * project db (if selectedDB is null). Update mapping information sourced
     * from the driver.
     */
    private void updateSqlDriver() {
        if (type != TYPE_JDBC) return;
        String sdb = selectedDB == null ? getDb() : selectedDB;
        if (sqlDriver != null && sqlDriver.getName().equals(sdb)) return;

        driverTypeMappings = new JdbcTypeMapping[LAST_TYPE - FIRST_TYPE + 1];
        driverJavaTypeMappings = null;

        typeMappings.clear();

        try {
            if (isVds()) return;
            sqlDriver = SqlDriver.createSqlDriver(sdb, null);
            JdbcTypeMapping[] a = sqlDriver.getTypeMappings();
            for (int i = a.length - 1; i >= 0; i--) {
                JdbcTypeMapping t = a[i];
                driverTypeMappings[t.getJdbcType() - FIRST_TYPE] = t;
            }

            // Update the list of type mappings to match the driver mappings.
            // Note that mappings for the selectedDB are used so these will
            // be the all databases mappings if selectedDB is null.
            try {
                int n = a.length;
                for (int i = 0; i < n; i++) {
                    JdbcTypeMapping t = a[i];
                    MdJdbcTypeMapping m = getTypeMapping(t.getJdbcType(),
                            selectedDB);
                    typeMappings.add(m);
                }
            } catch (Exception e) {
                project.getLogger().error(e);
            }
            Collections.sort(typeMappings);

            // convert the key for the driver mappings from Class to String
            Map map = sqlDriver.getJavaTypeMappings();
            driverJavaTypeMappings = new HashMap(map.size() * 2 + 1);
            for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry)i.next();
                Class t = (Class)e.getKey();
                String key;
                if (t.isArray()) {
                    key = t.getComponentType().getName() + "[]";
                } else {
                    key = t.getName();
                }
                driverJavaTypeMappings.put(key, e.getValue());
            }

            // collect all the mappings for the selected database into the
            // javaTypeMappings list from the driverJavaTypeMappings
            javaTypeMappings.clear();
            try {
                for (Iterator i = driverJavaTypeMappings.keySet().iterator();
                     i.hasNext();) {
                    String javaType = (String)i.next();
                    MdJdbcJavaTypeMapping m = getJavaTypeMapping(javaType,
                            selectedDB);
                    javaTypeMappings.add(m);
                }
            } catch (Exception e) {
                project.getLogger().error(e);
            }

            // now put in mappings for types not in driverTypeMappings
            // (custom types)
            for (Iterator i = javaTypeMappingMap.keySet().iterator();
                 i.hasNext();) {
                MdJdbcJavaTypeMapping.Key key = (MdJdbcJavaTypeMapping.Key)i.next();
                if (driverJavaTypeMappings.containsKey(key.getJavaType())) continue;
                String db = key.getDatabase();
                if (db == null || db.equals(sdb)) {
                    MdJdbcJavaTypeMapping tm =
                            (MdJdbcJavaTypeMapping)javaTypeMappingMap.get(key);
                    tm.setCustom(true);
                    javaTypeMappings.add(tm);
                }
            }

            Collections.sort(javaTypeMappings);

        } catch (Exception e) {
            sqlDriver = null;
            project.getLogger().error(e);
        }
        createJdbcNameGenerator();
        createJdbcMigrationControlGenerator();
    }

    public boolean isVds() {
        return type == TYPE_VDS;
    }

    public boolean isJDBC() {
        return type == TYPE_JDBC;
    }

    /**
     * Add a new java type mapping.
     */
    public void addJavaTypeMapping(MdJdbcJavaTypeMapping tm) {
        javaTypeMappings.add(tm);
        Collections.sort(javaTypeMappings);
        javaTypeMappingMap.put(tm.getKey(), tm);
    }

    /**
     * Remove a custom java type mapping.
     */
    public void removeJavaTypeMapping(MdJdbcJavaTypeMapping tm) {
        if (!tm.isCustom()) {
            throw new IllegalArgumentException("Not a custom mapping: " + tm);
        }
        javaTypeMappingMap.remove(tm.getKey());
        javaTypeMappings.remove(tm);
    }

    /**
     * Lookup the mapping supplied by the current sqlDriver for the given
     * JDBC type code or null if none.
     */
    public JdbcTypeMapping lookupDriverTypeMapping(int jdbcType) {
        return driverTypeMappings[jdbcType - FIRST_TYPE];
    }

    /**
     * Lookup the mapping supplied by the current sqlDriver for the given
     * Java type or null if none.
     */
    public JdbcJavaTypeMapping lookupDriverJavaTypeMapping(String javaType) {
        if (driverJavaTypeMappings == null) return null;
        return (JdbcJavaTypeMapping)driverJavaTypeMappings.get(javaType);
    }

    /**
     * Write a create script for the schema to pout
     */
    public void generateCreateScript(PrintWriter pout, boolean comments)
            throws Exception {
        getProject().compileMetaData(false, false);
        JdbcStorageManagerFactory smf = getProject().getJdbcStorageManagerFactory();
        smf.getSqlDriver().generateDDL(
                smf.getJdbcMetaData().getTables(), null, pout, comments);
    }

    /**
     * Write a create script for the schema to pout
     */
    public boolean checkSchema(PrintWriter perror, PrintWriter pfix)
            throws Exception {
        getProject().compileMetaData(false, false);
        JdbcStorageManagerFactory smf = getProject().getJdbcStorageManagerFactory();
        Connection con = null;
        try {
            con = getProject().getJdbcConnection(false, false);
            return smf.getSqlDriver().checkDDL(
                    smf.getJdbcMetaData().getTables(), con, perror, pfix,
                    jdbcMigrationControl);
        } finally {
            getProject().returnJdbcConnection(con);
        }
    }

    /**
     * Returns a list of JdbcTables for the current database
     */
    public HashMap getDatabaseMetaData() throws Exception {
        JdbcStorageManagerFactory smf = getProject().getJdbcStorageManagerFactory();
        Connection con = null;
        try {
            con = getProject().getJdbcConnection(false, false);
            return smf.getSqlDriver().getDatabaseMetaData(
                    smf.getJdbcMetaData().getTables(true), con);
        } finally {
            getProject().returnJdbcConnection(con);
        }
    }

    /**
     * return the current Jdbc tables, for the meta data
     */
    public ArrayList getCurrentJdbcMetaData() throws Exception {
        return getProject().getJdbcStorageManagerFactory().
                getJdbcMetaData().getTables();
    }

    /**
     * Recreate the database schema. This will drop and recreate all tables.
     * The create script is written to pout if this is not null.
     */
    public void recreateSchema(PrintWriter pout, boolean comments, Logger logger)
            throws Exception {
        getProject().compileMetaData(false, false);
        JdbcStorageManagerFactory smf = getProject().getJdbcStorageManagerFactory();
        Connection con = null;
        try {
            con = getProject().getJdbcConnection(false, true);
            dropAllTables(smf, con, getUrl(), logger);
            logger.info("Creating tables");
            smf.getSqlDriver().generateDDL(smf.getJdbcMetaData().getTables(),
                    con, pout, comments);
            logger.info("Finished creating tables");
        } finally {
            getProject().returnJdbcConnection(con);
        }
    }

    private void dropAllTables(JdbcStorageManagerFactory smf,
            Connection con, String url, Logger logger) throws Exception {
        logger.info("Dropping all tables on " + url);
        HashMap dbTableNames = getDatabaseTableNames(smf.getSqlDriver(), con);
        ArrayList a = smf.getJdbcMetaData().getTables();
        for (int i = 0; i < a.size(); i++) {
            JdbcTable t = (JdbcTable)a.get(i);
            String name = (String)dbTableNames.get(t.name.toLowerCase());
            if (name != null) {
                logger.info("  Dropping " + name);
                smf.getSqlDriver().dropTable(con, name);
            }
        }
    }

    private HashMap getDatabaseTableNames(SqlDriver sqlDriver, Connection con)
            throws SQLException {
        ArrayList a = sqlDriver.getTableNames(con);
        int n = a.size();
        HashMap ans = new HashMap(n * 2);
        for (int i = 0; i < a.size(); i++) {
            String t = (String)a.get(i);
            ans.put(t.toLowerCase(), t);
        }
        return ans;
    }

    /**
     * Load our mappings from p. This is called when the project is loaded.
     */
    public void loadMappings(Properties p) throws Exception {
        loadTypeMappings(p);
        loadJavaTypeMappings(p);
        loadSCOMappings(p);
    }

    /**
     * Save our mappings to p. This is called when the project is saved.
     */
    public void saveMappings(PropertySaver p) {
        saveTypeMappings(p);
        saveJavaTypeMappings(p);
        saveSCOMappings(p);
    }

    private void saveTypeMappings(PropertySaver p) {
        ArrayList a = new ArrayList();
        for (Iterator i = typeMappingMap.values().iterator(); i.hasNext();) {
            MdJdbcTypeMapping m = (MdJdbcTypeMapping)i.next();
            if (!m.isEmpty()) a.add(m);
        }
        Collections.sort(a);
        int n = a.size();
        if (n == 0) return;
        StringList s = new StringList();
        for (int i = 0; i < n; i++) {
            MdJdbcTypeMapping m = (MdJdbcTypeMapping)a.get(i);
            s.reset();
            m.write(s);
            p.add(ConfigParser.STORE_TYPE_MAPPING + i, s.toString());
        }
    }

    private void loadTypeMappings(Properties p) {
        typeMappingMap = new HashMap();
        int n = ConfigParser.MAX_STORE_TYPE_MAPPING_COUNT;
        String s = null;
        StringListParser lp = new StringListParser();
        for (int i = 0; i < n; i++) {
            s = p.getProperty(ConfigParser.STORE_TYPE_MAPPING + i);
            if (s == null) continue;
            MdJdbcTypeMapping m = new MdJdbcTypeMapping(this);
            lp.setString(s);
            m.read(lp);
            typeMappingMap.put(m.getKey(), m);
        }
    }

    private void saveJavaTypeMappings(PropertySaver p) {
        ArrayList a = new ArrayList();
        for (Iterator i = javaTypeMappingMap.values().iterator();
             i.hasNext();) {
            MdJdbcJavaTypeMapping m = (MdJdbcJavaTypeMapping)i.next();
            if (!m.isEmpty()) a.add(m);
        }
        Collections.sort(a);
        int n = a.size();
        if (n == 0) return;
        StringList s = new StringList();
        for (int i = 0; i < n; i++) {
            MdJdbcJavaTypeMapping m = (MdJdbcJavaTypeMapping)a.get(i);
            s.reset();
            m.write(s);
            p.add(ConfigParser.STORE_JAVATYPE_MAPPING + i, s.toString());
        }
    }

    private void loadJavaTypeMappings(Properties p) {
        javaTypeMappingMap = new HashMap();
        String s = null;
        int n = ConfigParser.MAX_STORE_JAVATYPE_MAPPING_COUNT;
        StringListParser lp = new StringListParser();
        for (int i = 0; i < n; i++) {
            s = p.getProperty(ConfigParser.STORE_JAVATYPE_MAPPING + i);
            if (s == null) continue;
            MdJdbcJavaTypeMapping m = new MdJdbcJavaTypeMapping(this);
            lp.setString(s);
            m.read(lp);
            javaTypeMappingMap.put(m.getKey(), m);
        }
    }

    private void saveSCOMappings(PropertySaver p) {
        int n = scoMappings.size();
        int count = 0;
        for (int i = 0; i < n; i++) {
            MdSCOMapping m = (MdSCOMapping)scoMappings.get(i);
            if (m.getFactoryClassNameStr() == null) continue;
            StringList l = new StringList();
            l.append(m.getScoClassName());
            l.append(m.getFactoryClassNameStr());
            p.add(ConfigParser.STORE_SCO_FACTORY_MAPPING + count,
                    l.toString());
            count++;
        }
    }

    private void loadSCOMappings(Properties p) {
        scoMappings.clear();

        HashMap defmap = new HashMap();
        VersantSCOFactoryRegistry.fillMapWithDefaults(defmap);

        // load all the custom mappings from the project
        HashSet done = new HashSet();
        int sfc = ConfigParser.MAX_STORE_SCO_FACTORY_COUNT;
        for (int x = 0; x < sfc; x++) {
            String s = p.getProperty(
                    ConfigParser.STORE_SCO_FACTORY_MAPPING + x);
            if (s != null) {
                StringListParser lp = new StringListParser(s);
                MdSCOMapping m = new MdSCOMapping(this, lp.nextString(),
                        lp.nextString());
                scoMappings.add(m);
                done.add(m.getScoClassName());
            }
        }

        // now add all default mappings not customized
        for (Iterator i = defmap.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            Class key = (Class)e.getKey();
            String name = key.getName();
            if (done.contains(name)) continue;
            MdSCOMapping m = new MdSCOMapping(this, name, null);
            scoMappings.add(m);
        }

        Collections.sort(scoMappings);
        // clear map of SCO mappings when the list changes so it will be
        // rebuilt on the next access
        scoMappingsMap.clear();
    }

    /**
     * Get or create a mapping for jdbcType and database (null for all).
     */
    public MdJdbcTypeMapping getTypeMapping(int jdbcType, String database) {
        MdJdbcTypeMapping.Key key = new MdJdbcTypeMapping.Key(jdbcType,
                database);
        MdJdbcTypeMapping ans = (MdJdbcTypeMapping)typeMappingMap.get(key);
        if (ans == null) {
            ans = new MdJdbcTypeMapping(this);
            ans.setJdbcType(jdbcType);
            ans.setDatabase(database);
            typeMappingMap.put(key, ans);
        }
        return ans;
    }

    /**
     * Get or create a java type mapping for jdbcType and database (null for all).
     */
    public MdJdbcJavaTypeMapping getJavaTypeMapping(String javaType,
            String database) {
        MdJdbcJavaTypeMapping.Key key = new MdJdbcJavaTypeMapping.Key(javaType,
                database);
        MdJdbcJavaTypeMapping ans = (MdJdbcJavaTypeMapping)javaTypeMappingMap.get(
                key);
        if (ans == null) {
            ans = new MdJdbcJavaTypeMapping(this);
            ans.setJavaType(javaType);
            ans.setDatabase(database);
            javaTypeMappingMap.put(key, ans);
        }
        return ans;
    }

    public List getTypeMappings() {
        return typeMappings;
    }

    public List getJavaTypeMappings() {
        return javaTypeMappings;
    }

    private int toJdbcType(String s) {
        if (s == null) return 0;
        try {
            return JdbcTypes.parse(s);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Register any extended types with mdutils so fields of those types will
     * be considered persistent.
     */
    public void registerExtendedTypes(MetaDataUtils mdutils) {
        int n = javaTypeMappings.size();
        for (int i = 0; i < n; i++) {
            MdJdbcJavaTypeMapping m = (MdJdbcJavaTypeMapping)javaTypeMappings.get(
                    i);
            String jt = m.getJavaType();
            Class cls = project.loadClass(jt);
            if (cls == null) continue;
            if (m.getEnabledBool()
                    && !mdutils.isPersistentType(cls,
                             Collections.EMPTY_MAP 

                            )) {
                mdutils.registerStoreType(cls);
            }
        }
    }

    /**
     * Fill in the defaults for col based on javaType and its JDBC type
     * (if any).
     */
    public void updateColDefs(String javaType, MdColumn col) {
        String sdb = col.getDb();
        if (sdb == null) sdb = selectedDB;

        MdJdbcJavaTypeMapping.Key fkey = new MdJdbcJavaTypeMapping.Key(
                javaType, sdb);
        MdJdbcJavaTypeMapping fm = (MdJdbcJavaTypeMapping)javaTypeMappingMap.get(
                fkey);

        // if the jdbcType of the column has been set then look for a mapping
        // for that type and use its properties
        int jdbcType = toJdbcType(col.getTypeStr());
        if (jdbcType != 0) {
            MdJdbcTypeMapping.Key tkey = new MdJdbcTypeMapping.Key(jdbcType,
                    sdb);
            MdJdbcTypeMapping tm = (MdJdbcTypeMapping)typeMappingMap.get(tkey);
            if (tm != null) {
                col.setDef(new MdColumn.SimpleDefaults(
                        fm == null ? null : fm.getJdbcTypeStr(),
                        tm.getSqlTypeStr(),
                        tm.getLengthStr(), tm.getScaleStr(), tm.getNullsStr(),
                        tm.getConverterStr()));
            } else {
                col.setDef(null);
            }
            return;
        }

        // use properties from the java type mapping
        if (fm != null) {
            col.setDef(new MdColumn.SimpleDefaults(fm.getJdbcTypeStr(), fm.getSqlTypeStr(),
                    fm.getLengthStr(), fm.getScaleStr(), fm.getNullsStr(),
                    fm.getConverterStr()));
        } else {
            col.setDef(null);
        }
    }

    /**
     * Fill in the defaults for col based on its JDBC type (if any).
     */
    public void updateColDefsJdbc(MdColumn col, String defJdbcType) {
        String sdb = col.getDb();
        if (sdb == null) sdb = selectedDB;

        int jdbcType = toJdbcType(col.getTypeStr());
        if (jdbcType != 0) {
            MdJdbcTypeMapping.Key tkey = new MdJdbcTypeMapping.Key(jdbcType,
                    sdb);
            MdJdbcTypeMapping tm = (MdJdbcTypeMapping)typeMappingMap.get(tkey);
            if (tm != null) {
                col.setDef(new MdColumn.SimpleDefaults(defJdbcType,
                        tm.getSqlTypeStr(),
                        tm.getLengthStr(), tm.getScaleStr(), tm.getNullsStr(),
                        tm.getConverterStr()));
                return;
            }
        }
        col.setDef(null);
    }

    public List getJdbcNameGeneratorPropsList() {
        return jdbcNameGeneratorProps.getPropertyList();
    }

    public List getJdbcKeyGeneratorPropsList() {
        return jdbcKeyGeneratorProps.getPropertyList();
    }

    public List getJdbcMigrationControlParamsPropsList() {
        return jdbcMigrationControlProps.getPropertyList();
    }

    public String getSqlDriverName() {
        if (sqlDriver != null) {
            return sqlDriver.getName();
        }
        return null;
    }

    /**
     * Get the query plan for a sql query.
     *
     * @param cl  The classloader to use
     * @param sql The parameter sql to be executed
     */
    public String getQueryPlan(ClassLoader cl, String sql) throws Exception {
        Connection con = connect(cl);
        String queryPlan = null;
        PreparedStatement ps;
        try {
            String qp = sqlDriver.prepareForGetQueryPlan(con, sql);
            ps = con.prepareStatement(qp);
            queryPlan = sqlDriver.getQueryPlan(con, ps);
        } finally {
            sqlDriver.cleanupForGetQueryPlan(con);
        }
        return queryPlan;
    }

    public List getScoMappings() {
        return scoMappings;
    }

    /**
     * Get the default SCO factory for fieldType.
     */
    public String getDefaultSCOFactory(Class fieldType) {
        if (fieldType == null) {
            return null;
        }
        if (scoMappingsMap.isEmpty()) {
            for (int i = scoMappings.size() - 1; i >= 0; i--) {
                MdSCOMapping m = (MdSCOMapping)scoMappings.get(i);
                String s = m.getFactoryClassNameStr();
                if (s == null) {
                    s = getOriginalDefaultSCOFactory(
                            getProject().loadClass(m.getScoClassName()));
                }
                scoMappingsMap.put(m.getScoClassName(), s);
            }
        }
        return (String)scoMappingsMap.get(fieldType.getName());
    }

    /**
     * Get the original default SCO factory for a field with type.
     */
    public String getOriginalDefaultSCOFactory(Class type) {
        if (type == null) return null;
        return VersantSCOFactoryRegistry.getDefaultMapping(type);
    }

    /**
     * Get all the valid standard SCO factories for a field with type.
     */
    public List getValidSCOFactoryList(Class type) {
        if (type == null) return null;
        return VersantSCOFactoryRegistry.getValidSCOFactoryList(type);
    }

    public void addMdDsTypeChangedListener(MdDsTypeChangedListener listener) {
        listenerList.addListener(listener);
    }

    public void removeMdDsTypeChangedListener(MdDsTypeChangedListener listener) {
        listenerList.removeListener(listener);
    }

    public void fireDataStoreTypeChanged(int oldType, int newType) {
        MdDsTypeChangedEvent event = new MdDsTypeChangedEvent(this,
                getProject(), this, oldType, newType, 0);
        Iterator it = listenerList.getListeners(/*CHFC*/MdChangeListener.class/*RIGHTPAR*/);
        while (it.hasNext() && !event.isConsumed()) {
            MdDsTypeChangedListener listener = (MdDsTypeChangedListener)it.next();
            listener.dataStoreTypeChanged(event);
        }
    }
}
