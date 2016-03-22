
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
package com.versant.core.vds;

import com.versant.core.storagemanager.*;
import com.versant.core.logging.LogEventStore;
import com.versant.core.server.DataStoreInfo;
import com.versant.core.server.CompiledQueryCache;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.*;
import com.versant.core.compiler.ClassSpec;
import com.versant.core.metric.HasMetrics;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.SchemaEditor;
import com.versant.odbms.model.UserSchemaModel;
import com.versant.odbms.model.schema.SchemaEditCollection;
import com.versant.odbms.model.schema.SchemaEdit;

import java.util.*;
import java.lang.reflect.Constructor;
import java.io.IOException;

/**
 * StorageManagerFactory implementation for Versant ODBMS.
 */
public class VdsStorageManagerFactory
        implements StorageManagerFactory, HasMetrics {

    private final LogEventStore pes;
    private final StorageCache cache;
    private final ModelMetaData jmd;
    private final VdsConnectionPool pool;
    private final VdsConfig vdsConfig;
    private final CompiledQueryCache compiledQueryCache;
    private final NamingPolicy namingPolicy;
    private final boolean hyperdrive;

    public VdsStorageManagerFactory(StorageManagerFactoryBuilder b) {
        this.pes = b.getLogEventStore();
        this.cache = b.getCache();
        ConfigInfo config = b.getConfig();
        this.vdsConfig = new VdsConfigParser().parse(config.props);
        compiledQueryCache = b.getCompiledQueryCache();
        // build meta data
        VdsMetaDataBuilder mdb = new VdsMetaDataBuilder(config, vdsConfig, b.getLoader(),
                b.isContinueAfterMetaDataError());
        jmd = mdb.buildMetaData(config.jdoMetaData);

        if (!b.isOnlyMetaData()) {
            pool = new VdsConnectionPool(vdsConfig, pes);
        } else {
            pool = null;
        }



        jmd.forceClassRegistration();
        cache.setJDOMetaData(jmd);
        namingPolicy = mdb.getVdsNamingPolicy();

        // generate source for hyperdrive classes if needed
        hyperdrive = config.hyperdrive;
        if (hyperdrive) {
            VdsOIDGenerator oidGen = new VdsOIDGenerator(jmd);
            VdsStateGenerator stateGen = new VdsStateGenerator();
            HashMap classSpecs = b.getClassSpecs();
            for (int i = jmd.classes.length - 1; i >= 0; i--) {
                ClassMetaData cmd = jmd.classes[i];
                ClassSpec spec = oidGen.generateOID(cmd);
                classSpecs.put(spec.getQName(), spec);
                spec = stateGen.generateState(cmd);
                classSpecs.put(spec.getQName(), spec);
            }
        }
    }

    public void init(boolean full, ClassLoader loader) {
        if (hyperdrive) {
            if (full) {
                installHyperdriveStateAndOIDFactory(loader);
            }
        } else { // not using generated hyperdrive classes
            installGenericStateAndOIDFactory();
        }

        if (full) {
            VdsConnection con = null;
            DatastoreManager dsi = null;
            try {
                con = pool.getConnection(false);
                dsi = con.getCon();
                if (!dsi.isTransactionActive()) dsi.beginTransaction();

                SchemaEditor editor = dsi.getSchemaEditor();
                SchemaEditCollection edits = editor.define(
                        (UserSchemaModel)jmd.vdsModel,
                        vdsConfig.isDynamicSchemaEvolution,
                        SchemaEditor.NO_OPTIONS);
                boolean allowsDefinition = vdsConfig.isDynamicSchemaDefinition;
                boolean allowsEvolution = vdsConfig.isDynamicSchemaEvolution;
                boolean needsDefinition = edits.getUndefinedClasses().length > 0;
                boolean needsEvolution = edits.isEvolutionRequired();

                if (allowsDefinition && allowsEvolution) {
                    if (needsEvolution) editor.evolve(
                            (UserSchemaModel)jmd.vdsModel, edits);
                    dsi.commitTransaction();
                } else if (allowsDefinition && !allowsEvolution) {
                    if (needsEvolution) {
                        dsi.rollbackTransaction();
                        throw BindingSupportImpl.getInstance().datastore(prepareSchemaDefinitionErrorMessage(
                                (UserSchemaModel)jmd.vdsModel, edits,
                                allowsDefinition, allowsEvolution));
                    } else {
                        dsi.commitTransaction();
                    }
                } else if (!allowsDefinition && allowsEvolution) {
                    if (needsDefinition) {
                        dsi.rollbackTransaction();
                        throw BindingSupportImpl.getInstance().datastore(prepareSchemaDefinitionErrorMessage(
                                (UserSchemaModel)jmd.vdsModel,
                                edits, allowsDefinition, allowsEvolution));
                    } else {
                        if (needsEvolution) editor.evolve((UserSchemaModel)jmd.vdsModel, edits);
                        dsi.commitTransaction();
                    }
                } else if (!allowsDefinition && !allowsEvolution) {
                    if (needsDefinition || needsEvolution) {
                        dsi.rollbackTransaction();
                        throw BindingSupportImpl.getInstance().datastore(prepareSchemaDefinitionErrorMessage(
                                (UserSchemaModel)jmd.vdsModel,
                                edits, allowsDefinition, allowsEvolution));
                    }
                }
            } catch (Exception x) {
                throw BindingSupportImpl.getInstance().internal(x.toString(), x);
            } finally {
                if (dsi != null && dsi.isTransactionActive()) {
                    try {
                        dsi.rollbackTransaction();
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (con != null) {
                    pool.returnConnection(con);
                }
            }
        }
    }

    private String prepareSchemaDefinitionErrorMessage(UserSchemaModel userModel,
            SchemaEditCollection edits,
            boolean allowsDefinition,
            boolean allowsEvolution) {
        SchemaEdit[] orderedEdits = edits.getOrderedEdits(userModel);
        String[] undefinedClassNames = edits.getUndefinedClasses();
        String[] droppedClassNames = edits.getDroppedClasses();
        StringBuffer tmp = new StringBuffer("Schema Definition Error\r\n");

        if (allowsDefinition && allowsEvolution) {
            tmp.append(
                    "Current configuration allows schema definition and evolution");
        } else if (allowsDefinition && !allowsEvolution) {
            tmp.append(
                    "Current configuration allows schema definition and but not evolution");
        } else if (!allowsDefinition && allowsEvolution) {
            tmp.append(
                    "Current configuration allows schema evolution and but not definition");
        } else {
            tmp.append(
                    "Current configuration does not allows schema definition or evolution");
        }
        tmp.append("\r\nHowever following updates are required\r\n");
        if (undefinedClassNames.length > 0) {
            tmp.append("New class(es) to be defined:\r\n");
        }
        for (int i = 0; i < undefinedClassNames.length; i++) {
            tmp.append("\t" + undefinedClassNames[i] + "\r\n");
        }
        if (droppedClassNames.length > 0) {
            tmp.append("Class(es) to be deleted:\r\n");
        }
        for (int i = 0; i < droppedClassNames.length; i++) {
            tmp.append("\t" + droppedClassNames[i] + "\r\n");
        }
        if (orderedEdits.length > 0) {
            tmp.append("Schema change(s) required:\r\n");
        }
        for (int i = 0; i < orderedEdits.length; i++) {
            tmp.append("\t" + orderedEdits[i] + "\r\n");

        }
        tmp.append("\r\nChange " + ConfigParser.VDS_SCHEMA_DEFINITION + " and/or " +
                ConfigParser.VDS_SCHEMA_EVOLUTION + "\r\n");
        tmp.append(
                "or use SchemaTool or Workbench or Ant tasks to define schema\r\n");
        return tmp.toString();
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


    public StorageManager getStorageManager() {
        return new VdsStorageManager(jmd, cache, compiledQueryCache, pes,
                vdsConfig, pool, namingPolicy);
    }

    public void returnStorageManager(StorageManager sm) {
        sm.destroy();
    }

    public void destroy() {
        if (pool != null) {
            try {
                pool.shutdown();
            } catch (Exception e) {
                // ignore
            }
        }
        compiledQueryCache.clear();
    }

    public Object getDatastoreConnection() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not supported on Versant ODBMS");
    }

    public void closeIdleDatastoreConnections() {
        if (pool != null) {
            pool.clear();
        }
    }

    public ModelMetaData getModelMetaData() {
        return jmd;
    }

    public DataStoreInfo getDataStoreInfo() {
        DataStoreInfo info = new DataStoreInfo();
        info.setDataStoreType("vds");
        info.setName("main");
        return info;
    }

    public VdsConnectionPool getPool() {
        return pool;
    }

    public StorageManagerFactory getInnerStorageManagerFactory() {
        return null;
    }

    public void addMetrics(List list) {
        if (cache instanceof HasMetrics) {
            ((HasMetrics)cache).addMetrics(list);
        }
    }

    public void sampleMetrics(int[][] buf, int pos) {
        if (cache instanceof HasMetrics) {
            ((HasMetrics)cache).sampleMetrics(buf, pos);
        }
    }

    public void supportedOptions(Set options) {
        options.remove("javax.jdo.option.ApplicationIdentity");
    }

    public CompiledQueryCache getCompiledQueryCache() {
        return compiledQueryCache;
    }

    /**
     * Factory that returns instances of the non-generated State and OID
     * classes.
     */
    private static class GenericFactory implements StateAndOIDFactory {

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            return new VdsGenericOID(cmd, resolved);
        }

        public State createState(ClassMetaData cmd) {
            return new VdsGenericState(cmd);
        }

        public NewObjectOID createNewObjectOID(ClassMetaData cmd) {
            return new NewObjectOID(cmd);
        }

        public OID createUntypedOID() {
            return new VdsUntypedOID();
        }
    }

    /**
     * Base class for factories using hyperdrive classes.
     */
    private static abstract class HyperdriveFactoryBase
            implements StateAndOIDFactory {

        private final Class stateClass;

        public HyperdriveFactoryBase(Class stateClass) {
            this.stateClass = stateClass;
        }

        public State createState(ClassMetaData cmd) {
            try {
                return (State)stateClass.newInstance();
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }

        public NewObjectOID createNewObjectOID(ClassMetaData cmd) {
            return new NewObjectOID(cmd);
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
            extends HyperdriveFactoryBase {

        private Constructor oidCon;

        public HyperdriveFactoryHierarchy(Class oidClass,
                Class stateClass) {
            super(stateClass);
            try {
                oidCon = oidClass.getConstructor(new Class[]{ClassMetaData.class,
                    Boolean.TYPE});
            } catch (NoSuchMethodException e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            try {
                return (OID)oidCon.newInstance(new Object[]{cmd,
                    resolved ? Boolean.TRUE : Boolean.FALSE});
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(oidCon.getDeclaringClass());
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
            try {
                oidCon = ((Class) in.readObject()).getConstructor(new Class[] {ClassMetaData.class, Boolean.TYPE});
            } catch (NoSuchMethodException e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }


    }

    /**
     * Factory for classes not in a hierarchy.
     */
    private static class HyperdriveFactory
            extends HyperdriveFactoryBase {

        private final Class oidClass;

        public HyperdriveFactory(Class oidClass, Class stateClass) {
            super(stateClass);
            this.oidClass = oidClass;
        }

        public OID createOID(ClassMetaData cmd, boolean resolved) {
            try {
                return (OID)oidClass.newInstance();
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }
    }

}

