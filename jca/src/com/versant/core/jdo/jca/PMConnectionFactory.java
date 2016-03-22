
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
package com.versant.core.jdo.jca;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.VersantConnectionPoolFullException;
import com.versant.core.jdo.*;
import com.versant.core.logging.LogEvent;
import com.versant.core.metric.Metric;
import com.versant.core.metric.MetricSnapshotPacket;
import com.versant.core.server.DataStoreInfo;

import javax.jdo.*;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * This is the PMF that is given to the client in a managed environment.
 * i.e. The users view of the pmf.
 * This is the <connectionfactory-impl-class> in the jca.
 */
public class PMConnectionFactory extends BorlandHack
        implements javax.resource.cci.ConnectionFactory{

    private ManagedPMConnectionFactory mcf;
    private ConnectionManager cm;
    private Reference reference;

    public PMConnectionFactory(ManagedPMConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    /**
     * NB. Do not take this method out!!!
     * This is for borland, if this method is not here, then it complains
     */
    public PMConnectionFactory(ManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = (ManagedPMConnectionFactory)mcf;
        this.cm = cm;
    }


    /**
     * Util method to obtain the pmf. If the pmf is null then try and obtain
     * the pmf from the managedconnection. The pmf must be available from the mc
     * at this time.
     */
    private VersantPersistenceManagerFactory getPmf() {
        try {
            return mcf.getPMF(false);
        } catch (Exception e) {
            throw new JDOException("Unable to obtain pmf ", e);
        }
    }

    //Implementation for javax.resource.cci.ConnectionFactory//
    public Connection getConnection() {
        checkMC();
        return getConnection(null);
    }

    public ResourceAdapterMetaData getMetaData() {
        checkMC();
        return new ResourceAdapterMetaDataImpl();
    }

    public RecordFactory getRecordFactory() {
        checkMC();
        throw new JDOUnsupportedOptionException("not supported");
    }

    public Connection getConnection(ConnectionSpec properties) {
        checkMC();
        return (Connection) getPersistenceManager();
    }

    public void setReference(Reference reference) {
        checkMC();
        this.reference = reference;
    }

    public Reference getReference() {
        checkMC();
        return reference;
    }

    //Implementation of javax.jdo.PersistenceManager//
    public PersistenceManager getPersistenceManager() {
        return getPersistenceManager(null, null);
    }

    public void close() {
        mcf = null;
        cm = null;
    }

    public PersistenceManager getPersistenceManager(String user, String password) {
        checkMC();
        PMRequestInfo cri = mcf.createRequestInfo();
        try {
            return (PersistenceManager) cm.allocateConnection(mcf, cri);
        } catch (ResourceException re) {
            re.printStackTrace();
            throw new JDOFatalException(re.getMessage());
        }
    }

    public void setConnectionUserName(String s) {
        checkMC();
    }

    public String getConnectionUserName() {
        checkMC();
        return getPmf().getConnectionUserName();
    }

    public void setConnectionPassword(String s) {
        checkMC();
    }

    public void setConnectionURL(String s) {
        checkMC();
    }

    public String getConnectionURL() {
        checkMC();
        return getPmf().getConnectionURL();
    }

    public void setConnectionDriverName(String s) {
        checkMC();
    }

    public String getConnectionDriverName() {
        checkMC();
        return getPmf().getConnectionDriverName();
    }

    public void setConnectionFactoryName(String s) {
        checkMC();
    }

    public String getConnectionFactoryName() {
        checkMC();
        return getPmf().getConnectionFactoryName();
    }

    public void setConnectionFactory(Object o) {
        checkMC();
    }

    public Object getConnectionFactory() {
        checkMC();
        return getPmf().getConnectionFactory();
    }

    public void setConnectionFactory2Name(String s) {
        checkMC();
    }

    public String getConnectionFactory2Name() {
        checkMC();
        return getPmf().getConnectionFactory2Name();
    }

    public void setConnectionFactory2(Object o) {
        checkMC();
    }

    public Object getConnectionFactory2() {
        checkMC();
        return getPmf().getConnectionFactory2();
    }

    public void setMultithreaded(boolean b) {
        checkMC();
    }

    public boolean getMultithreaded() {
        checkMC();
        return getPmf().getMultithreaded();
    }

    public void setMapping(String s) {
        checkMC();
        getPmf().setMapping(s); // todo jdo2 is this right
    }

    public String getMapping() {
        checkMC();
        return getPmf().getMapping();
    }

    public void setOptimistic(boolean b) {
        checkMC();
    }

    public boolean getOptimistic() {
        checkMC();
        return getPmf().getOptimistic();
    }

    public void setRetainValues(boolean b) {
        checkMC();
    }

    public boolean getRetainValues() {
        checkMC();
        return getPmf().getRetainValues();
    }

    public void setRestoreValues(boolean b) {
        checkMC();
    }

    public boolean getRestoreValues() {
        return getPmf().getRestoreValues();
    }

    public void setNontransactionalRead(boolean b) {
        checkMC();
    }

    public boolean getNontransactionalRead() {
        checkMC();
        return getPmf().getNontransactionalRead();
    }

    public void setNontransactionalWrite(boolean b) {
        checkMC();
    }

    public boolean getNontransactionalWrite() {
        checkMC();
        return getPmf().getNontransactionalWrite();
    }

    public void setIgnoreCache(boolean b) {
        checkMC();
    }

    public boolean getIgnoreCache() {
        checkMC();
        return getPmf().getIgnoreCache();
    }

    public Properties getProperties() {
        checkMC();
        return getPmf().getProperties();
    }

    public Collection supportedOptions() {
        checkMC();
        return getPmf().supportedOptions();
    }

    public boolean isClosed() {
        checkMC();
        return getPmf().isClosed();
    }

    public DataStoreCache getDataStoreCache() {
        checkMC();
        return getPmf().getDataStoreCache();
    }

    private void checkMC() {
        if (mcf == null) {
            throw new JDOUserException("PersistenceManagerFactory is closed!");
        }
    }

    public int getTimeout() throws ResourceException {
        return 0;
    }

    public PrintWriter setLogWriter() throws ResourceException {
        return null;
    }

    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
    }

    public void setTimeout(int i) throws ResourceException {
    }

    public java.sql.Connection getJdbcConnection(String datastore) throws SQLException,
            VersantConnectionPoolFullException {
        checkMC();
        return getPmf().getJdbcConnection(datastore);
    }

    public void clearConnectionPool(String datastore) {
        checkMC();
        getPmf().clearConnectionPool(datastore);
    }

    public void setUserObject(Object o) {
        checkMC();
        getPmf().setUserObject(o);
    }

    public Object getUserObject() {
        checkMC();
        return getPmf().getUserObject();
    }

    public boolean isInterceptDfgFieldAccess() {
        checkMC();
        return getPmf().isInterceptDfgFieldAccess();
    }

    public void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess) {
        checkMC();
        getPmf().setInterceptDfgFieldAccess(interceptDfgFieldAccess);
    }

    public boolean isAllowPmCloseWithTxOpen() {
        checkMC();
        return getPmf().isAllowPmCloseWithTxOpen();
    }

    public void setAllowPmCloseWithTxOpen(boolean allowed) {
        checkMC();
        getPmf().setAllowPmCloseWithTxOpen(allowed);
    }

    public boolean isCheckModelConsistencyOnCommit() {
        checkMC();
        return getPmf().isCheckModelConsistencyOnCommit();
    }

    public void setCheckModelConsistencyOnCommit(boolean on) {
        checkMC();
        getPmf().setCheckModelConsistencyOnCommit(on);
    }

    public void setServerUserObject(Object o) {
        checkMC();
        getPmf().setServerUserObject(o);
    }

    public Object getServerUserObject() {
        checkMC();
        return getPmf().getServerUserObject();
    }

    public void shutdown() {
        checkMC();
        getPmf().shutdown();
    }

    public LogEvent[] getNewPerfEvents(int lastId) {
        checkMC();
        return getPmf().getNewPerfEvents(lastId);
    }

    public PmfStatus getPmfStatus() {
        checkMC();
        return getPmf().getPmfStatus();
    }

    public Metric[] getMetrics() {
        checkMC();
        return getPmf().getMetrics();
    }

    public MetricSnapshotPacket getNewMetricSnapshots(int lastId) {
        checkMC();
        return getPmf().getNewMetricSnapshots(lastId);
    }

    public MetricSnapshotPacket getMostRecentMetricSnapshot(int lastId) {
        checkMC();
        return getPmf().getMostRecentMetricSnapshot(lastId);
    }

    public void setUserMetric(String name, int value) {
        checkMC();
        getPmf().setUserMetric(name, value);
    }

    public void incUserMetric(String name, int delta) {
        checkMC();
        getPmf().incUserMetric(name, delta);
    }

    public int getUserMetric(String name) {
        checkMC();
        return getPmf().getUserMetric(name);
    }

    public void logEvent(int level, String description, int ms) {
        checkMC();
        getPmf().logEvent(level, description, ms);
    }

    public void doSystemGC() {
        checkMC();
        getPmf().doSystemGC();
    }

    public PropertyInfo getServerConfiguration() {
        checkMC();
        return getPmf().getServerConfiguration();
    }

    public String setServerProperty(String[] beanPath, String value) {
        checkMC();
        return getPmf().setServerProperty(beanPath, value);
    }

    public RemoteClientStatus[] getRemoteClients() {
        checkMC();
        return getPmf().getRemoteClients();
    }

    public List getPersistenceManagers() {
        checkMC();
        return getPmf().getPersistenceManagers();
    }

    public void evict(Object oid) {
        checkMC();
        getPmf().evict(oid);
    }

    public void evictAll(Object[] oids) {
        checkMC();
        getPmf().evictAll(oids);
    }

    public void evictAll(Collection oids) {
        checkMC();
        getPmf().evictAll(oids);
    }

    public void evictAll(Class cls, boolean includeSubclasses) {
        checkMC();
        getPmf().evictAll(cls, includeSubclasses);
    }

    public void evictAll() {
        checkMC();
        getPmf().evictAll();
    }

    public boolean isInCache(Object oid) {
        checkMC();
        return getPmf().isInCache(oid);
    }

    public int getClassID(Class cls) {
        checkMC();
        return getPmf().getClassID(cls);
    }

    public Class getClassForID(int classid) {
        checkMC();
        return getPmf().getClassForID(classid);
    }

    public Object getJdbcClassID(Class cls) {
        checkMC();
        return getPmf().getJdbcClassID(cls);
    }

    public Class getClassForJdbcID(Class baseClass, Object jdbcClassid) {
        checkMC();
        return getPmf().getClassForJdbcID(baseClass, jdbcClassid);
    }

    public int getClassIndex(Class cls) {
        checkMC();
        return getPmf().getClassIndex(cls);
    }

    public Class getClassForIndex(int index) {
        checkMC();
        return getPmf().getClassForIndex(index);
    }

    public int[] getClassIndexes(Class[] classes, boolean includeSubclasses) {
        checkMC();
        return getPmf().getClassIndexes(classes, includeSubclasses);
    }

    public void registerSCOPersistenceDelegates(Object encoder) {
        checkMC();
        getPmf().registerSCOPersistenceDelegates(encoder);
    }

    public int getPmCacheRefType() {
        checkMC();
        return getPmf().getPmCacheRefType();
    }

    public void setPmCacheRefType(int pmCacheRefType) {
        checkMC();
        getPmf().setPmCacheRefType(pmCacheRefType);
    }

    public DataStoreInfo getDataStoreInfo(String datastore) {
        checkMC();
        return getPmf().getDataStoreInfo(datastore);
    }


    public void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        checkMC();
        getPmf().addInstanceLifecycleListener(listener, classes);
    }

    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        checkMC();
        getPmf().removeInstanceLifecycleListener(listener);
    }

    public Object getEntityManagerFactory() {
        checkMC();
        return getPmf().getEntityManagerFactory();
    }

	public boolean getDetachAllOnCommit() {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}

	public void setDetachAllOnCommit(boolean arg0) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}
}

