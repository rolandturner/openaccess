
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
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.*;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import javax.resource.cci.*;
import javax.resource.spi.ConnectionEvent;
import javax.resource.ResourceException;

/**
 * This is a proxy to the pm that is given to the client in a managed environment.
 * i.e. The user's view of the connection.
 * This is the <connection-impl-class> in the jca
 */
public class PMConnection implements javax.resource.cci.Connection,
        VersantPersistenceManager {

    private VersantPersistenceManager pm;
    private ManagedPMConnection mc;
    private TransactionWrapper txWrapper;

    public PMConnection(VersantPersistenceManager pm, ManagedPMConnection mc) {
        this.pm = pm;
        this.mc = mc;
        txWrapper = new TransactionWrapper(mc, pm, this);
    }

    /**
     * Associate this client connection with the managed connection. This is called
     * from {@link ManagedPMConnection#associateConnection(Object)}
     */
    void associateMe(ManagedPMConnection mc) {
        this.mc = mc;
    }

    public boolean isClosed() {
        return mc == null;
    }

    public void close() {
        if (mc == null) return; //already closed
        ManagedPMConnection lmc = mc;
        mc = null;
        pm = null;
        txWrapper.close();
        lmc.fireConnectionEvent(ConnectionEvent.CONNECTION_CLOSED, this);
    }

    private void checkMC() {
        if (mc == null) {
            throw new JDOUserException("PersistenceManager closed!");
        }
    }

    public Object getOptimisticLockingValue(Object o) {
        checkMC();
        return pm.getOptimisticLockingValue(o);
    }

    public Transaction currentTransaction() {
        checkMC();
        return txWrapper;
    }

    public void evict(Object o) {
        checkMC();
        pm.evict(o);
    }

    public void evictAll(Object[] objects) {
        checkMC();
        pm.evictAll(objects);
    }

    public void evictAll(Collection collection) {
        checkMC();
        pm.evictAll(collection);
    }

    public void evictAll() {
        checkMC();
        pm.evictAll();
    }

    public void refresh(Object o) {
        checkMC();
        pm.refresh(o);
    }

    public void refreshAll(Object[] objects) {
        checkMC();
        pm.refreshAll(objects);
    }

    public void refreshAll(Collection collection) {
        checkMC();
        pm.refreshAll(collection);
    }

    public void refreshAll() {
        checkMC();
        pm.refreshAll();
    }

    public void refreshAll(JDOException e) {
        checkMC();
        pm.refreshAll(e);
    }

    public Query newQuery() {
        checkMC();
        return pm.newQuery();
    }

    public Query newQuery(Object o) {
        checkMC();
        return pm.newQuery(o);
    }

    public Query newQuery(String s) {
        checkMC();
        return pm.newQuery(s);
    }

    public Query newQuery(String s, Object o) {
        checkMC();
        return pm.newQuery(s, o);
    }

    public Query newQuery(Class aClass) {
        checkMC();
        return pm.newQuery(aClass);
    }

    public Query newQuery(Extent extent) {
        checkMC();
        return pm.newQuery(extent);
    }

    public Query newQuery(Class aClass, Collection collection) {
        checkMC();
        return pm.newQuery(aClass, collection);
    }

    public Query newQuery(Class aClass, String s) {
        checkMC();
        return pm.newQuery(aClass, s);
    }

    public Query newQuery(Class aClass, Collection collection, String s) {
        checkMC();
        return pm.newQuery(aClass, collection, s);
    }

    public Query newQuery(Extent extent, String s) {
        checkMC();
        return pm.newQuery(extent, s);
    }

    public Query newNamedQuery(Class aClass, String s) {
        checkMC();
        return pm.newNamedQuery(aClass, s);
    }

    public Extent getExtent(Class aClass, boolean b) {
        checkMC();
        return pm.getExtent(aClass, b);
    }

    public Extent getExtent(Class aClass) {
        checkMC();
        return pm.getExtent(aClass);
    }

    public Object getObjectById(Object o, boolean b) {
        checkMC();
        return pm.getObjectById(o, b);
    }

    public Object getObjectById(Class aClass, Object o) {
        checkMC();
        return pm.getObjectById(aClass, o);
    }

    public Object getObjectById(Object o) {
        checkMC();
        return pm.getObjectById(o);
    }

    public Object getObjectId(Object o) {
        checkMC();
        return pm.getObjectId(o);
    }

    public Object getTransactionalObjectId(Object o) {
        checkMC();
        return pm.getTransactionalObjectId(o);
    }

    public Object newObjectIdInstance(Class aClass, Object o) {
        checkMC();
        return pm.newObjectIdInstance(aClass, o);
    }

    public Collection getObjectsById(Collection collection, boolean b) {
        checkMC();
        return pm.getObjectsById(collection, b);
    }

    public Collection getObjectsById(Collection collection) {
        checkMC();
        return pm.getObjectsById(collection);
    }

    public Object[] getObjectsById(Object[] objects, boolean b) {
        checkMC();
        return pm.getObjectsById(objects, b);
    }

    public Object[] getObjectsById(Object[] objects) {
        checkMC();
        return pm.getObjectsById(objects);
    }

    public Object newObjectIdInstance(Class aClass, String s) {
        checkMC();
        return pm.newObjectIdInstance(aClass, s);
    }

    public void makePersistent(Object o) {
        checkMC();
        pm.makePersistent(o);
    }

    public void makePersistentAll(Object[] objects) {
        checkMC();
        pm.makePersistentAll(objects);
    }

    public void makePersistentAll(Collection collection) {
        checkMC();
        pm.makePersistentAll(collection);
    }

    public void deletePersistent(Object o) {
        checkMC();
        pm.deletePersistent(o);
    }

    public void deletePersistentAll(Object[] objects) {
        checkMC();
        pm.deletePersistentAll(objects);
    }

    public void deletePersistentAll(Collection collection) {
        checkMC();
        pm.deletePersistentAll(collection);
    }

    public void makeTransient(Object o) {
        checkMC();
        pm.makeTransient(o);
    }

    public void makeTransientAll(Object[] objects) {
        checkMC();
        pm.makeTransientAll(objects);
    }

    public void makeTransientAll(Collection collection) {
        checkMC();
        pm.makeTransientAll(collection);
    }

    public void makeTransactional(Object o) {
        checkMC();
        pm.makeTransactional(o);
    }

    public void makeTransactionalAll(Object[] objects) {
        checkMC();
        pm.makeTransactionalAll(objects);
    }

    public void makeTransactionalAll(Collection collection) {
        checkMC();
        pm.makeTransactionalAll(collection);
    }

    public void makeNontransactional(Object o) {
        checkMC();
        pm.makeNontransactional(o);
    }

    public void makeNontransactionalAll(Object[] objects) {
        checkMC();
        pm.makeNontransactionalAll(objects);
    }

    public void makeNontransactionalAll(Collection collection) {
        checkMC();
        pm.makeNontransactionalAll(collection);
    }

    public void retrieve(Object o) {
        checkMC();
        pm.retrieve(o);
    }

    public void retrieveAll(Collection pcs, boolean DFGOnly) {
        checkMC();
        pm.retrieveAll(pcs, DFGOnly);
    }

    public void retrieveAll(Object[] pcs, boolean DFGOnly) {
        checkMC();
        pm.retrieveAll(pcs, DFGOnly);
    }

    public void retrieveAll(Collection collection) {
        checkMC();
        pm.retrieveAll(collection);
    }

    public void retrieveAll(Object[] objects) {
        checkMC();
        pm.retrieveAll(objects);
    }

    public void setUserObject(Object o) {
        checkMC();
        pm.setUserObject(o);
    }

    public Object getUserObject() {
        checkMC();
        return pm.getUserObject();
    }

    public PersistenceManagerFactory getPersistenceManagerFactory() {
        checkMC();
        return pm.getPersistenceManagerFactory();
    }

    public Class getObjectIdClass(Class aClass) {
        checkMC();
        return pm.getObjectIdClass(aClass);
    }

    public void setMultithreaded(boolean b) {
        checkMC();
        pm.setMultithreaded(b);
    }

    public boolean getMultithreaded() {
        checkMC();
        return pm.getMultithreaded();
    }

    public void setIgnoreCache(boolean b) {
        checkMC();
        pm.setIgnoreCache(b);
    }

    public boolean getIgnoreCache() {
        checkMC();
        return pm.getIgnoreCache();
    }

    public Object detachCopy(Object o) {
        checkMC();
        return pm.detachCopy(o);
    }

    public Collection detachCopyAll(Collection collection) {
        checkMC();
        return pm.detachCopyAll(collection);
    }

    public Object[] detachCopyAll(Object[] objects) {
        checkMC();
        return pm.detachCopyAll(objects);
    }

    public Object attachCopy(Object o, boolean b) {
        checkMC();
        return pm.attachCopy(o,b);
    }

    public Collection attachCopyAll(Collection collection, boolean b) {
        checkMC();
        return pm.attachCopyAll(collection, b);
    }

    public Object[] attachCopyAll(Object[] objects, boolean b) {
        checkMC();
        return pm.attachCopyAll(objects, b);
    }

    public Object putUserObject(Object o, Object o1) {
        checkMC();
        return pm.putUserObject(o, o1);
    }

    public Object getUserObject(Object o) {
        checkMC();
        return pm.getUserObject(o);
    }

    public Object removeUserObject(Object o) {
        checkMC();
        return pm.removeUserObject(o);
    }

    public PersistenceManager getPersistenceManager() {
        checkMC();
        return pm;
    }

    public ManagedPMConnection getMConnection() {
        checkMC();
        return mc;
    }

    public String toString() {
        return "PMConnection - @ " + (pm == null ? "null" : pm.toString());
    }

    public ResultSetInfo getResultSetInfo() {
        checkMC();
        throw BindingSupportImpl.getInstance().unsupported();
    }

    public ConnectionMetaData getMetaData() {
        checkMC();
        throw BindingSupportImpl.getInstance().unsupported();
    }

    public LocalTransaction getLocalTransaction() throws javax.resource.ResourceException {
        checkMC();
        throw BindingSupportImpl.getInstance().unsupported();
    }

    public Interaction createInteraction() {
        checkMC();
        throw BindingSupportImpl.getInstance().unsupported();
    }

    private void log(String msg) {
        System.out.println(Thread.currentThread().getName() + " " + msg);
    }

    public boolean getAutoCommit() throws ResourceException {
        checkMC();
        throw BindingSupportImpl.getInstance().unsupported();
    }

    public void setAutoCommit(boolean b) throws ResourceException {
        checkMC();
        throw BindingSupportImpl.getInstance().unsupported();
    }

    public boolean isInterceptDfgFieldAccess() {
        checkMC();
        return pm.isInterceptDfgFieldAccess();
    }

    public void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess) {
        checkMC();
        pm.setInterceptDfgFieldAccess(interceptDfgFieldAccess);
    }

    public void cancelQueryExecution() {
        checkMC();
        pm.cancelQueryExecution();
    }

    public boolean isDirty() {
        checkMC();
        return pm.isDirty();
    }

    public Object getObjectByIDString(String value, boolean toValidate) {
        checkMC();
        return pm.getObjectByIDString(value, toValidate);
    }

    public Object newObjectIdInstance(Class pcClass, String str,
            boolean resolved) {
        checkMC();
        return pm.newObjectIdInstance(pcClass, str, resolved);
    }

    public Object getObjectByIDString(String value, boolean toValidate,
            boolean resolved) {
        checkMC();
        return pm.getObjectByIDString(value, toValidate, resolved);
    }

    public void loadFetchGroup(Object pc, String name) {
        checkMC();
        pm.loadFetchGroup(pc, name);
    }

    public void flush() {
        checkMC();
        pm.flush();
    }

    public void checkConsistency() {
        checkMC();
        pm.checkConsistency();
    }

    public FetchPlan getFetchPlan() {
        checkMC();
        return pm.getFetchPlan();
    }

    public Object newInstance(Class aClass) {
        checkMC();
        return pm.newInstance(aClass);
    }

    public Sequence getSequence(String s) {
        checkMC();
        return pm.getSequence(s);
    }

    public JDOConnection getDataStoreConnection() {
        checkMC();
        return pm.getDataStoreConnection();
    }

    public void flush(boolean retainValues) {
        checkMC();
        pm.flush(retainValues);
    }

    public java.sql.Connection getJdbcConnection(String datastore)
            throws VersantConnectionPoolFullException {
        checkMC();
        return pm.getJdbcConnection(datastore);
    }

    public String getConnectionURL(String dataStore) {
        checkMC();
        return pm.getConnectionURL(dataStore);
    }

    public String getConnectionDriverName(String dataStore) {
        checkMC();
        return pm.getConnectionDriverName(dataStore);
    }

    public void makeTransientRecursive(Object pc) {
        checkMC();
        pm.makeTransientRecursive(pc);
    }

    public List versantAllDirtyInstances() {
        checkMC();
        return pm.versantAllDirtyInstances();
    }

    public void setDatastoreTxLocking(int mode) {
        checkMC();
        pm.setDatastoreTxLocking(mode);
    }

    public int getDatastoreTxLocking() {
        checkMC();
        return pm.getDatastoreTxLocking();
    }

    public void setRetainConnectionInOptTx(boolean on) {
        checkMC();
        pm.setRetainConnectionInOptTx(on);
    }

    public Object getObjectByIdFromCache(Object oid) {
        checkMC();
        return pm.getObjectByIdFromCache(oid);
    }

    public boolean isHollow(Object pc) {
        checkMC();
        return pm.isHollow(pc);
    }

    public boolean hasIdentity(Object pc) {
        checkMC();
        return pm.hasIdentity(pc);
    }

    public void logEvent(int level, String description, int ms) {
        checkMC();
        pm.logEvent(level, description, ms);
    }

    public int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex) {
        checkMC();
        return pm.getObjectsById(oids, length, data, stateFieldNo, classMetaDataIndex);
    }

    public Query versantNewNamedQuery(Class cls, String queryName) {
        checkMC();
        return pm.versantNewNamedQuery(cls, queryName);
    }

    public Collection versantDetachCopy(Collection pcs, String fetchGroup) {
        checkMC();
        return pm.versantDetachCopy(pcs, fetchGroup);
    }

    public boolean isCheckModelConsistencyOnCommit() {
        checkMC();
        return pm.isCheckModelConsistencyOnCommit();
    }

    public void setCheckModelConsistencyOnCommit(boolean on) {
        checkMC();
        pm.setCheckModelConsistencyOnCommit(on);
    }

    public void checkModelConsistency() {
        checkMC();
        pm.checkModelConsistency();
    }

    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional) {
        checkMC();
        return pm.versantAttachCopy(detached, makeTransactional);
    }

    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional, boolean shallow) {
        checkMC();
        return pm.versantAttachCopy(detached, makeTransactional, shallow);
    }

    public void setPmCacheRefType(Object pc, int type) {
        checkMC();
        pm.setPmCacheRefType(pc, type);
    }

    public void setPmCacheRefType(Object[] pcs, int type) {
        checkMC();
        pm.setPmCacheRefType(pcs, type);
    }

    public void setPmCacheRefType(Collection col, int type) {
        checkMC();
        pm.setPmCacheRefType(col, type);
    }

    public void setPmCacheRefType(int type) {
        checkMC();
        pm.setPmCacheRefType(type);
    }

    public int getPmCacheRefType() {
        checkMC();
        return pm.getPmCacheRefType();
    }

    public void evictFromL2CacheAfterCommit(Object o) {
        checkMC();
        pm.evictFromL2CacheAfterCommit(o);
    }

    public void evictAllFromL2CacheAfterCommit(Object[] a) {
        checkMC();
        pm.evictAllFromL2CacheAfterCommit(a);
    }

    public void evictAllFromL2CacheAfterCommit(Collection c) {
        checkMC();
        pm.evictAllFromL2CacheAfterCommit(c);
    }

    public void evictAllFromL2CacheAfterCommit(Class cls,
            boolean includeSubclasses) {
        checkMC();
        pm.evictAllFromL2CacheAfterCommit(cls, includeSubclasses);
    }

    public void evictAllFromL2CacheAfterCommit() {
        checkMC();
        pm.evictAllFromL2CacheAfterCommit();
    }

    public void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        checkMC();
        pm.addInstanceLifecycleListener(listener, classes);
    }

    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        checkMC();
        pm.removeInstanceLifecycleListener(listener);
    }

	public void retrieve(Object arg0, boolean arg1) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
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
