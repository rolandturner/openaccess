
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
package com.versant.core.jdo;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.core.common.OID;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.util.BeanUtils;
import com.versant.core.util.WeakBag;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEventStore;
import com.versant.core.metric.Metric;
import com.versant.core.metric.MetricSnapshotPacket;
import com.versant.core.server.DataStoreInfo;
import com.versant.core.jdo.query.mem.MemQueryCompiler;
import com.versant.core.storagemanager.*;

import javax.jdo.PersistenceManager;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;
import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/**
 * Base class for our PMF implementations.
 */
public abstract class PersistenceManagerFactoryBase
        implements VersantPMFInternal {

    protected Properties props;
    protected LogEventStore pes;
    protected ClassLoader loader;
    protected StorageCache cache;
    protected StorageManagerFactory smf;
    protected StorageManagerFactory innermostSmf;
    protected boolean jdbc;
    protected ModelMetaData jmd;
    protected MemQueryCompiler memQueryCompiler;
    protected PMPool pmPool;
    protected WeakBag activePMs = new WeakBag();
    protected Object userObject;
    protected ConfigInfo config;

    // the fields in the block below are used to configure PMs
    protected boolean retainValues;
    protected boolean restoreValues;
    protected boolean optimistic;
    protected boolean nontransactionalRead;
    protected boolean nontransactionalWrite;
    protected boolean ignoreCache;
    protected boolean multithreaded;
    protected boolean allowPmCloseWithOpenTx;
    protected boolean interceptDfgFieldAccess;
    protected boolean checkModelConsistencyOnCommit;
    //the default is set here: Java: false, .NET: true
    protected boolean refreshPersNontransactionalObjectsInNewTxn ;
    protected int pmCacheRefType;
    protected int datastoreTxLocking;
    protected int retainConnectionInOptTx;
    protected LifecycleListenerManager[] listeners;

    protected HashMap classToCmd;

    protected int pmCreatedCount;
    protected int pmClosedCount;
    protected int pmClosedAutoCount;
    protected int pmClosedAutoTxCount;

    private Field jdbcClassIdField;
    private Method jdbcFindClassMethod;

    private Object entityManagerFactory;
    private boolean closed; // is the pmf closed
    private DataStoreCacheImp dataStoreCache;

    public PersistenceManagerFactoryBase(Properties props,
            ClassLoader loader) {
        boolean ok = false;
        try {
            this.props = props = (Properties)props.clone();

            config = new ConfigParser().parse(props);

            if (config.hyperdrive) {

                this.loader = new HyperdriveLoader(loader);

            } else {
                this.loader = loader;
            }



            pes = createLogEventStore();
            cache = createStorageCache();
            smf = createStorageManagerFactory();

            for (innermostSmf = smf; ; ) {
                StorageManagerFactory next =
                        innermostSmf.getInnerStorageManagerFactory();
                if (next == null) break;
                innermostSmf = next;
            }
            jdbc = innermostSmf.getClass().getName().indexOf(
                    "JdbcStorageManagerFactory") >= 0;

            jmd = smf.getModelMetaData();
            jmd.checkForNonPCClasses();
            jmd.forceClassRegistration();

            memQueryCompiler = new MemQueryCompiler(jmd, this.loader);

            retainValues = config.retainValues;
            restoreValues = config.restoreValues;
            optimistic = config.optimistic;
            nontransactionalRead = config.nontransactionalRead;
            nontransactionalWrite = config.nontransactionalWrite;
            ignoreCache = config.ignoreCache;
            multithreaded = config.multithreaded;
            allowPmCloseWithOpenTx = config.allowPmCloseWithOpenTx;
            interceptDfgFieldAccess = config.interceptDfgFieldAccess;
            checkModelConsistencyOnCommit = config.checkModelConsistencyOnCommit;
            pmCacheRefType = config.pmCacheRefType;
            datastoreTxLocking = config.datastoreTxLocking;
            retainConnectionInOptTx = config.retainConnectionInOptTx;

            classToCmd = new HashMap();
            for (int i = 0; i < jmd.classes.length; i++) {
                ClassMetaData cmd = jmd.classes[i];
                classToCmd.put(cmd.cls,cmd);
            }

            if (config.pmpoolEnabled) {
                pmPool = new PMPool(this, config.pmpoolMaxIdle, pes);
            } else {
                pmPool = null;
            }
            ok = true;
        } finally {
            if (!ok) {
                try {
                    close();
                } catch (Throwable e) {
                    // ignore - already busy with an exception
                }
            }
        }
    }

    /**
     * Create our StorageManagerFactory.
     */
    protected abstract StorageManagerFactory createStorageManagerFactory();

    /**
     * Create and configure our LogEventStore.
     */
    protected LogEventStore createLogEventStore() {
        LogEventStore pes = new LogEventStore();
        BeanUtils.setProperties(pes, config.perfProps);
        return pes;
    }

    /**
     * Create our StorageCache implementation.
     */
    protected StorageCache createStorageCache() {
        if (config.useCache) {
            LRUStorageCache lruCache = new LRUStorageCache();
            lruCache.setQueryCacheEnabled(config.queryCacheEnabled);
            lruCache.setMaxQueries(config.maxQueriesToCache);
            lruCache.setMaxObjects(config.cacheMaxObjects);
            return lruCache;
        } else {
            return new NOPStorageCache();
        }
    }

    /**
     * Get the properties that we were created from.
     */
    public Properties getInitProperties() {
        return props;
    }

    /**
     * Accessor for whether the PMF is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    public DataStoreCache getDataStoreCache() {
        if (dataStoreCache == null) {
            dataStoreCache = new DataStoreCacheImp(this);
        }
        return dataStoreCache;
    }

    public synchronized void close() {
        List list = getPersistenceManagers();
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            PersistenceManager pm = (PersistenceManager)i.next();
            if (!pm.isClosed()) {
                try {
                    if (pm.currentTransaction().isActive()) {
                        pm.currentTransaction().rollback();
                    }
                } catch (Exception e) {
                    // ignore
                }
                try {
                    pm.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        if (cache != null) {
            Object ctx = cache.beginTx();
            cache.evictAll(ctx);
            cache.endTx(ctx);
            cache = null;
        }
        if (smf != null) {
            smf.destroy();
            smf = null;
        }
        jmd = null;
        pmPool = null;
        closed = true;
    }

    public PersistenceManager getPersistenceManager() {
        VersantPersistenceManagerImp pm;
        if (pmPool != null) {
            pm = pmPool.getPM();
        } else {
            pm = createVersantPersistenceManagerImp();
        }
        configurePM(pm);
        synchronized (activePMs) {
            activePMs.clean();
            pm.setActiveReference(activePMs.add(pm));
        }
        return pm.getProxy();
    }

    /**
     * Restore a PM to default settings.
     */
    protected void configurePM(VersantPersistenceManagerImp pm) {
        pm.setRetainValues(retainValues);
        pm.setRestoreValues(restoreValues);
        pm.setOptimistic(optimistic);
        pm.setNontransactionalRead(nontransactionalRead);
        pm.setNontransactionalWrite(nontransactionalWrite);
        pm.setIgnoreCache(ignoreCache);
        pm.setMultithreadedImp(multithreaded);
        pm.setInterceptDfgFieldAccess(interceptDfgFieldAccess || !optimistic);
        pm.setCheckModelConsistencyOnCommit(checkModelConsistencyOnCommit);
        pm.getCache().setCurrentRefType(pmCacheRefType);
        pm.setDatastoreTxLocking(datastoreTxLocking);
        pm.setRefreshPNTObjects(refreshPersNontransactionalObjectsInNewTxn);
        switch (retainConnectionInOptTx) {
            case MDStatics.TRUE:
                pm.setRetainConnectionInOptTx(true);
                break;
            case MDStatics.FALSE:
                pm.setRetainConnectionInOptTx(false);
                break;
        }
        if (this.listeners != null) {
            LifecycleListenerManager[] copy = new LifecycleListenerManager[listeners.length];
            System.arraycopy(listeners, 0, copy, 0, listeners.length);
            pm.setListeners(copy);
        } else {
            pm.setListeners(null);
        }
    }

    /**
     * Create a new, unconfigured, PM.
     *
     * @see #configurePM(VersantPersistenceManagerImp)
     */
    public synchronized VersantPersistenceManagerImp createVersantPersistenceManagerImp() {
        VersantPersistenceManagerImp pm = null;
        try {
            pm = newVersantPersistenceManagerImp(smf.getStorageManager());
            if (pes.isFiner()) {
                ServerLogEvent ev = new ServerLogEvent(
                        ServerLogEvent.PM_CREATED, null);
                ev.zeroTotalMs();
                pes.log(ev);
            }
            pmCreatedCount++;
            return pm;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    protected VersantPersistenceManagerImp newVersantPersistenceManagerImp(
            StorageManager sm) {
        return new VersantPersistenceManagerImp(this, jmd,
                sm, new LocalPMCache(101), memQueryCompiler);
    }

    public void pmClosedNotification(VersantPersistenceManagerImp pm,
            boolean fromFinalizer, boolean txWasActive) {
        activePMs.remove(pm.getActiveReference());
        pm.setActiveReference(null);
        if (fromFinalizer) {
            try {
                pm.destroy();
            } catch (Exception e) {
                // ignore
            }
            if (txWasActive && pes.isWarning() || pes.isFine()) {
                ServerLogEvent ev = new ServerLogEvent(
                        txWasActive
                            ? ServerLogEvent.PM_CLOSED_AUTO_TX
                            : ServerLogEvent.PM_CLOSED_AUTO,
                        null);
                ev.zeroTotalMs();
                pes.log(ev);
            }
        } else if (pmPool == null || pm.isMustNotPool()) {
            pm.destroy();
            if (pes.isFiner()) {
                ServerLogEvent ev = new ServerLogEvent(
                        ServerLogEvent.PM_CLOSED, null);
                ev.zeroTotalMs();
                pes.log(ev);
            }
        } else {
            pmPool.returnPM(pm);
        }
    }

    public PersistenceManager getPersistenceManager(String userid,
            String password) {
        return getPersistenceManager();
    }

    public void setConnectionUserName(String userName) {
        // ignore
    }

    public String getConnectionUserName() {
        return (String)props.get(ConfigParser.STD_CON_USER_NAME);
    }

    public void setConnectionPassword(String password) {
        // ignore
    }

    public void setConnectionURL(String URL) {
        // ignore
    }

    public String getConnectionURL() {
        return (String)props.get(ConfigParser.STD_CON_URL);
    }

    public void setConnectionDriverName(String driverName) {
        // ignore
    }

    public String getConnectionDriverName() {
        return (String)props.get(ConfigParser.STD_CON_DRIVER_NAME);
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        // ignore
    }

    public String getConnectionFactoryName() {
        return (String)props.get(ConfigParser.STD_CON_FACTORY_NAME);
    }

    public void setConnectionFactory(Object connectionFactory) {
        // ignore
    }

    public Object getConnectionFactory() {
        throw notImplemented();
    }

    public void setConnectionFactory2Name(String connectionFactoryName) {
        // ignore
    }

    public String getConnectionFactory2Name() {
        return (String)props.get(ConfigParser.STD_CON2_FACTORY_NAME);
    }

    public void setConnectionFactory2(Object connectionFactory) {
        // ignore
    }

    public Object getConnectionFactory2() {
        throw notImplemented();
    }

    public void setMultithreaded(boolean flag) {
        multithreaded = flag;
    }

    public boolean getMultithreaded() {
        return multithreaded;
    }

    public void setOptimistic(boolean flag) {
        optimistic = flag;
    }

    public boolean getOptimistic() {
        return optimistic;
    }

    public void setRetainValues(boolean flag) {
        retainValues = flag;
    }

    public boolean getRetainValues() {
        return retainValues;
    }

    public void setRestoreValues(boolean restoreValues) {
        this.restoreValues = restoreValues;
    }

    public boolean getRestoreValues() {
        return restoreValues;
    }

    public void setNontransactionalRead(boolean flag) {
        nontransactionalRead = flag;
    }

    public boolean getNontransactionalRead() {
        return nontransactionalRead;
    }

    public void setNontransactionalWrite(boolean flag) {
        nontransactionalWrite = flag;
    }

    public boolean getNontransactionalWrite() {
        return nontransactionalWrite;
    }

    public void setIgnoreCache(boolean flag) {
        ignoreCache = flag;
    }

    public boolean getIgnoreCache() {
        return ignoreCache;
    }

    public Properties getProperties() {
        Properties p = new Properties();
        props.setProperty("VendorName", "Versant");
        props.setProperty("VendorURL", "http://www.versant.com");
        props.setProperty("VersionNumber", Debug.VERSION);
        return p;
    }

    public Collection supportedOptions() {
        HashSet o = new HashSet();
        o.add("javax.jdo.option.TransientTransactional");
        o.add(ConfigParser.OPTION_NON_TRANSACTIONAL_READ);
        o.add(ConfigParser.OPTION_NON_TRANSACTIONAL_WRITE);
        o.add(ConfigParser.OPTION_RETAINVALUES);
        o.add(ConfigParser.OPTION_RESTORE_VALUES);
        o.add(ConfigParser.OPTION_OPTIMISTIC);
        o.add(ConfigParser.OPTION_MULTITHREADED);
        o.add("javax.jdo.option.ApplicationIdentity");
        o.add("javax.jdo.option.DatastoreIdentity");
        o.add("javax.jdo.option.ArrayList");
        o.add("javax.jdo.option.HashMap");
        o.add("javax.jdo.option.Hashtable");
        o.add("javax.jdo.option.LinkedList");
        o.add("javax.jdo.option.TreeMap");
        o.add("javax.jdo.option.TreeSet");
        o.add("javax.jdo.option.Vector");
        o.add("javax.jdo.option.Map");
        o.add("javax.jdo.option.List");
        o.add("javax.jdo.option.Array");
        o.add("javax.jdo.option.NullCollection");
        o.add("javax.jdo.query.JDOQL");
        smf.supportedOptions(o);
        return o;
    }

    public synchronized Connection getJdbcConnection(String datastore)
            throws SQLException {
        if (jdbc) {
            return (Connection)innermostSmf.getDatastoreConnection();
        } else {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Not supported by " + innermostSmf.getClass().getName());
        }
    }

    public synchronized void clearConnectionPool(String datastore) {
        smf.closeIdleDatastoreConnections();
    }

    public void setUserObject(Object o) {
        userObject = o;
    }

    public Object getUserObject() {
        return userObject;
    }

    public void setRefreshPersNonTxInNewTx(boolean refreshPNTs)
    {
        this.refreshPersNontransactionalObjectsInNewTxn = refreshPNTs;
    }
    
    public boolean isRefreshPersNonTxInNewTx()
    {
        return refreshPersNontransactionalObjectsInNewTxn;   
    }
    
    public boolean isInterceptDfgFieldAccess() {
        return interceptDfgFieldAccess;
    }

    public void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess) {
        this.interceptDfgFieldAccess = interceptDfgFieldAccess;
    }

    public boolean isAllowPmCloseWithTxOpen() {
        return allowPmCloseWithOpenTx;
    }

    public void setAllowPmCloseWithTxOpen(boolean allowed) {
        allowPmCloseWithOpenTx = allowed;
    }

    public boolean isCheckModelConsistencyOnCommit() {
        return checkModelConsistencyOnCommit;
    }

    public void setCheckModelConsistencyOnCommit(boolean on) {
        checkModelConsistencyOnCommit = on;
    }

    public void setServerUserObject(Object o) {
        setUserObject(o);
    }

    public Object getServerUserObject() {
        return getUserObject();
    }

    public void shutdown() {
        close();
    }

    public LogEvent[] getNewPerfEvents(int lastId) {
        return pes.copyEvents(lastId);
    }

    public PmfStatus getPmfStatus() {
        PmfStatus s = new PmfStatus();
        s.setServer(getConnectionURL());
        return s;
    }

    public abstract Metric[] getMetrics();

    public abstract MetricSnapshotPacket getNewMetricSnapshots(int lastId);

    public abstract MetricSnapshotPacket getMostRecentMetricSnapshot(int lastId);

    public abstract void setUserMetric(String name, int value);

    public abstract void incUserMetric(String name, int delta);

    public abstract int getUserMetric(String name);

    public void logEvent(int level, String description, int ms) {
        switch (level) {
             case VersantPersistenceManagerFactory.EVENT_ERRORS:
                 if (!pes.isSevere()) return;
                 break;
             case VersantPersistenceManagerFactory.EVENT_NORMAL:
                 if (!pes.isFine()) return;
                 break;
             case VersantPersistenceManagerFactory.EVENT_VERBOSE:
                 if (!pes.isFiner()) return;
                 break;
             case VersantPersistenceManagerFactory.EVENT_ALL:
                 if (!pes.isFinest()) return;
                 break;
         }
         ServerLogEvent ev = new ServerLogEvent(ServerLogEvent.USER,
                 description);
         ev.setTotalMs(ms);
         pes.log(ev);
    }

    public void doSystemGC() {
        System.gc();
    }

    public PropertyInfo getServerConfiguration() {
        return null;
    }

    public String setServerProperty(String[] beanPath, String value) {
        return null;
    }

    public RemoteClientStatus[] getRemoteClients() {
        return new RemoteClientStatus[0];
    }

    public List getPersistenceManagers() {
        List list;
        synchronized (activePMs) {
            list = activePMs.values();
        }
        ArrayList a = new ArrayList(list.size());
        for (Iterator i  = list.iterator(); i.hasNext(); ) {
            VersantPersistenceManagerImp pm = (VersantPersistenceManagerImp)i.next();
            PMProxy proxy = pm.getProxy();
            // proxy may be null if PM has been closed since we got list
            if (proxy != null) {
                a.add(proxy);
            }
        }
        return a;
    }

    public void evict(Object o) {
        PmfEvictEvent ev = null;
        if (pes.isFiner()) {
            pes.log(ev = new PmfEvictEvent(o));
        }
        try {
            OID oid = jmd.convertToOID(o);
            Object ctx = cache.beginTx();
            try {
                cache.evict(ctx, new OID[]{oid}, 0, 1, 0);
            } finally {
                cache.endTx(ctx);
            }
        } catch (Throwable e) {
            if (ev != null) ev.setErrorMsg(e);
            throw handleException(e);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void evictAll(Object[] oids) {
        PmfEvictEvent ev = null;
        if (pes.isFiner()) {
            pes.log(ev = new PmfEvictEvent(oids));
        }
        try {
            OID[] a = jmd.convertToOID(oids, oids.length);
            Object ctx = cache.beginTx();
            try {
                cache.evict(ctx, a, 0, a.length, 0);
            } finally {
                cache.endTx(ctx);
            }
        } catch (Throwable e) {
            if (ev != null) ev.setErrorMsg(e);
            throw handleException(e);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void evictAll(Collection oids) {
        OID[] a = new OID[oids.size()];
        int pos = 0;
        for (Iterator i = oids.iterator(); i.hasNext();) {
            a[pos++] = jmd.convertToOID(i.next());
        }
        PmfEvictEvent ev = null;
        if (pes.isFiner()) {
            pes.log(ev = new PmfEvictEvent(a));
        }
        try {
            Object ctx = cache.beginTx();
            try {
                cache.evict(ctx, a, 0, a.length, 0);
            } finally {
                cache.endTx(ctx);
            }
        } catch (Throwable e) {
            if (ev != null) ev.setErrorMsg(e);
            throw handleException(e);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void evictAll(Class cls, boolean includeSubclasses) {
        PmfEvictEvent ev = null;
        if (pes.isFiner()) {
            pes.log(ev = new PmfEvictEvent(cls, includeSubclasses));
        }
        try {
            ClassMetaData cmd = jmd.getClassMetaData(cls);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Not a persistent class: " + cls.getName());
            }
            ClassMetaData[] a;
            if (includeSubclasses) {
                a = jmd.getClassMetaDataForHierarchy(cmd);
            } else {
                a = new ClassMetaData[]{cmd};
            }
            Object ctx = cache.beginTx();
            try {
                cache.evict(ctx, a, a.length);
            } finally {
                cache.endTx(ctx);
            }
        } catch (Throwable e) {
            if (ev != null) ev.setErrorMsg(e);
            throw handleException(e);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void evictAll() {
        PmfEvictEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new PmfEvictEvent());
        }
        try {
            Object ctx = cache.beginTx();
            try {
                cache.evictAll(ctx);
            } finally {
                cache.endTx(ctx);
            }
        } catch (Throwable e) {
            if (ev != null) ev.setErrorMsg(e);
            throw handleException(e);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public boolean isInCache(Object oid) {
        return cache.contains(jmd.convertToOID(oid));
    }

    /**
     * Get the meta data for cls or throw an exception if there is none.
     */
    protected ClassMetaData getClassMetaData(Class cls) {
        ClassMetaData cmd = (ClassMetaData)classToCmd.get(cls);
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Class is not persistent: " + cls.getName());
        }
        return cmd;
    }

    public int getClassID(Class cls) {
        return getClassMetaData(cls).classId;
    }

    public Class getClassForID(int classid) {
        ClassMetaData cmd = jmd.getClassMetaData(classid);
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "No class found for classid: " + classid);
        }
        return cmd.cls;
    }

    public Object getJdbcClassID(Class cls) {
        if (jdbc) {
            ClassMetaData cmd = getClassMetaData(cls);
            try {
                // this must be done with reflection to avoid engine depending
                // on jdbc
                if (jdbcClassIdField == null) {
                    Class c = /*CHFC*/cmd.storeClass.getClass()/*RIGHTPAR*/;
                    jdbcClassIdField = c.getField("jdbcClassId");
                }
                return jdbcClassIdField.get(cmd.storeClass);
            } catch (Throwable e) {
                throw handleException(e);
            }
        } else {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Class is not stored using JDBC: " + cls.getName());
        }
    }

    public Class getClassForJdbcID(Class baseClass, Object jdbcClassid) {
        if (jdbc) {
            ClassMetaData cmd = getClassMetaData(baseClass).top;
            ClassMetaData ans;
            try {
                // this must be done with reflection to avoid engine depending
                // on jdbc
                if (jdbcFindClassMethod == null) {
                    Class cls = /*CHFC*/cmd.storeClass.getClass()/*RIGHTPAR*/;
                    jdbcFindClassMethod = cls.getMethod(
                            "findClass", new Class[]{/*CHFC*/Object.class/*RIGHTPAR*/});
                }
                ans = (ClassMetaData)jdbcFindClassMethod.invoke(
                        cmd.storeClass, new Object[]{jdbcClassid});
            } catch (Throwable e) {
                throw handleException(e);
            }
            if (ans == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "No class found in hierarchy " +
                        cmd.qname + " for jdbc-class-id: " + jdbcClassid);
            }
            return ans.cls;
        } else {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Class is not stored using JDBC: " + baseClass.getName());
        }
    }

    public int getClassIndex(Class cls) {
        return getClassMetaData(cls).index;
    }

    public Class getClassForIndex(int index) {
        try {
            return jmd.classes[index].cls;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Invalid class index: " + index);
        }
    }

    public int[] getClassIndexes(Class[] classes, boolean includeSubclasses) {
        return jmd.convertToClassIndexes(classes, includeSubclasses);
    }

    public void registerSCOPersistenceDelegates(Object encoder) {
        try {
            // this must be done with reflection so the code will compile
            // and run on JDK 1.3 VMs
            Class cls = Class.forName(
                    "com.versant.core.jdo.sco.PersistenceDelegateManager");
            Method method = cls.getDeclaredMethod("register",
                    new Class[]{Class.forName("java.beans.Encoder")});
            method.invoke(null, new Object[]{encoder});
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "JDK 1.4 or newer VM required");
        }
    }

    public int getPmCacheRefType() {
        return pmCacheRefType;
    }

    public void setPmCacheRefType(int pmCacheRefType) {
        this.pmCacheRefType = pmCacheRefType;
    }

    public void closeActivePMsForTesting() {
    }

    public DataStoreInfo getDataStoreInfo(String datastore) {
        return smf.getDataStoreInfo();
    }

    public void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        if (listeners == null){
            listeners = new LifecycleListenerManager[jmd.classes.length +1];
        }

        if (classes != null) {
            int[] indexs = jmd.convertToClassIndexes(classes, true);
            for (int i = 0; i < indexs.length; i++) {
                int index = indexs[i];
                if (listeners[index] == null) {
                    listeners[index] = new LifecycleListenerManager(listener);
                } else {
                    listeners[index] = listeners[index].add(listener);
                }
                // setup all listener
                index = jmd.classes.length;
                if (listeners[index] == null) {
                    listeners[index] = new LifecycleListenerManager(listener);
                } else {
                    listeners[index] = listeners[index].add(listener);
                }
            }
        } else {
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == null) {
                    listeners[i] = new LifecycleListenerManager(listener);
                } else {
                    listeners[i] = listeners[i].add(listener);
                }
            }
        }
    }

    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        if (listeners == null) {
            return;
        }

        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] != null) {
                listeners[i] = listeners[i].remove(listener);
            }
        }
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    protected RuntimeException handleException(Throwable e) {
		return handleException(e.toString(), e);
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    protected RuntimeException handleException(String msg, Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException)e).getTargetException();
        }
        if (BindingSupportImpl.getInstance().isError(e)
                && !BindingSupportImpl.getInstance().isOutOfMemoryError(e)) {
            throw (Error)e;
        }
        if (BindingSupportImpl.getInstance().isOwnException(e)) {
            return (RuntimeException)e;
        }
        return BindingSupportImpl.getInstance().internal(msg, e);
    }

    /**
     * Return a 'not implemented' exception.
     */
    protected RuntimeException notImplemented() {
        return BindingSupportImpl.getInstance().notImplemented("");
    }

    public StorageCache getStorageCache() {
        return cache;
    }

    public StorageManagerFactory getStorageManagerFactory() {
        return smf;
    }

    public ModelMetaData getJDOMetaData() {
        return jmd;
    }

    public LogEventStore getLogEventStore() {
        return pes;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public abstract boolean isLocal();

    public synchronized Object getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            // Get by reflection to avoid problems when not running on JDK 1.5.
            // This class cannot reference the EJB 3 stuff directly.
            try {
                Class emf = /*CHFC*/loader.loadClass(
                        "com.versant.core.ejb.EntityManagerFactoryImp")/*RIGHTPAR*/;
                Constructor cons = emf.getConstructor(new Class[] {
                        /*CHFC*/VersantPMFInternal.class/*RIGHTPAR*/ });
                entityManagerFactory = cons.newInstance(new Object[]{this});
            } catch (Throwable e) {
                BindingSupportImpl.getInstance().fatal(
                        "Unable to create EntityManagerFactory" + e, e);
            }
        }
        return entityManagerFactory;
    }

    /**
     * If the primary datasource returns already enlisted connections.
     */
    public boolean isEnlistedDataSource() {
        if (!jdbc) return false;
        return config.enlistedConnections;
    }

    /**
     * Classloader that will load hyperdrive classes before delegating to
     * the parent classloader.
     */

    public static class HyperdriveLoader extends ClassLoader {

        public HyperdriveLoader(ClassLoader parent) {
            super(parent);
        }

    }


}

