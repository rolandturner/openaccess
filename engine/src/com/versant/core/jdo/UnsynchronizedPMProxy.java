
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

import com.versant.core.server.QueryResultWrapper;
import com.versant.core.server.CompiledQuery;

import javax.jdo.*;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.spi.PersistenceCapable;

import javax.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
 
import java.sql.Connection;
import java.util.Collection;
import java.util.List;



import com.versant.core.common.*;
import com.versant.core.storagemanager.ExecuteQueryReturn;

/**
 * This is a unsynchronized proxy to VersantPersistenceManagerImp.
 *
 * @see PMProxy
 * @see SynchronizedPMProxy
 */
public final class UnsynchronizedPMProxy extends PMProxy {

    private VersantPersistenceManagerImp realPM;

    public UnsynchronizedPMProxy(VersantPersistenceManagerImp realPM) {
        this.realPM = realPM;
    }

    public VersantStateManager getVersantStateManager(PersistenceCapable pc) {
        return realPM.getVersantStateManager(pc);
    }

    public Object getObjectByIdForState(OID oid, int stateFieldNo,
            int navClassIndex, OID fromOID) {
        return realPM.getObjectByIdForState(oid, stateFieldNo, navClassIndex, fromOID);
    }

    public void flushIfDepOn(int[] bits) {
        realPM.flushIfDepOn(bits);
    }

    public void processLocalCacheReferenceQueue() {
        realPM.processLocalCacheReferenceQueue();
    }

    public QueryResultWrapper executeQuery(CompiledQuery cq, Object[] params) {
        return realPM.getStorageManager().executeQuery(realPM, null, cq, params);
    }

    public QueryResultContainer getNextQueryResult(QueryResultWrapper aQrs, int skipAmount) {
        return realPM.getStorageManager().fetchNextQueryResult(realPM,
                ((ExecuteQueryReturn)aQrs).getRunningQuery(), skipAmount);
    }

    public void addToCache(StatesReturned container) {
        realPM.addToCache(container);
    }

    public void closeQuery(QueryResultWrapper qrw) {
        realPM.getStorageManager().closeQuery(((ExecuteQueryReturn)qrw).getRunningQuery());
    }

    public QueryResultContainer getAbsolute(QueryResultWrapper qrsIF, int index, int fetchAmount) {
        return realPM.getStorageManager().fetchRandomAccessQueryResult(realPM,
                ((ExecuteQueryReturn)qrsIF).getRunningQuery(), index,
                fetchAmount);
    }

    public int getResultCount(QueryResultWrapper qrsIF) {
        return realPM.getStorageManager().getRandomAccessQueryCount(realPM,
                ((ExecuteQueryReturn)qrsIF).getRunningQuery());
    }

    public QueryResultContainer getAllQueryResults(CompiledQuery cq,
            Object[] params) {
        return realPM.getStorageManager().executeQueryAll(realPM, null, cq, params);
    }

    public void setMasterOnDetail(PersistenceCapable detail, int managedFieldNo,
            PersistenceCapable master, boolean removeFromCurrentMaster) {
        realPM.setMasterOnDetail(detail, managedFieldNo, master, removeFromCurrentMaster);
    }

    public Object getObjectField(PersistenceCapable pc, int fieldNo) {
        return realPM.getObjectField(pc, fieldNo);
    }

    public int getQueryRowCount(CompiledQuery cq, Object[] params) {
        return realPM.getStorageManager().executeQueryCount(null, cq, params);
    }

    public Object getOptimisticLockingValue(Object o) {
        checkClosed();
        return realPM.getOptimisticLockingValue(o);
    }

    public void setPmCacheRefType(Object pc, int type) {
        checkClosed();
        realPM.setPmCacheRefType(pc, type);
    }

    public void setPmCacheRefType(Object[] pcs, int type) {
        checkClosed();
        realPM.setPmCacheRefType(pcs, type);
    }

    public void setPmCacheRefType(Collection col, int type) {
        checkClosed();
        realPM.setPmCacheRefType(col, type);
    }

    public void setPmCacheRefType(int type) {
        checkClosed();
        realPM.setPmCacheRefType(type);
    }

    public int getPmCacheRefType() {
        checkClosed();
        return realPM.getPmCacheRefType();
    }

    /**
     * If default fetch group fields will be intercepted on a
     * Persistent-Non-Transaction instance. If this is disabled then such a
     * instance will not be 'refreshed' if accessed in a datastore transaction.
     */
    public boolean isInterceptDfgFieldAccess() {
        return realPM.isInterceptDfgFieldAccess();
    }

    public void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess) {
        realPM.setInterceptDfgFieldAccess(interceptDfgFieldAccess);
    }

    public VersantPersistenceManagerImp getRealPM() {
        checkClosed();
        return realPM;
    }

    VersantPersistenceManagerImp getRealPMNoCheck() {
        return realPM;
    }

    public void resetPM() {
        realPM = null;
    }

    public boolean isRealPMNull() {
        return realPM == null;
    }

    public List versantAllDirtyInstances() {
        checkClosed();
        return realPM.versantAllDirtyInstances();
    }

    private void checkClosed() {
        if (realPM == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The pm is closed");
        }
    }

    public boolean isDirty() {
        checkClosed();
        return realPM.isDirty();
    }

    public void cancelQueryExecution() {
        checkClosed();
        realPM.cancelQueryExecution();
    }

    public Object getObjectByIDString(String value, boolean toValidate) {
        checkClosed();
        return realPM.getObjectByIDString(value, toValidate);
    }

    public void loadFetchGroup(Object pc, String name) {
        checkClosed();
        realPM.loadFetchGroup(pc, name);
    }

    public void flush() {
        checkClosed();
        realPM.flush();
    }

    public void flush(boolean retainState) {
        checkClosed();
        realPM.flush(retainState);
    }

    public void makeTransientRecursive(Object pc) {
        checkClosed();
        realPM.makeTransientRecursive(pc);
    }

    public Connection getJdbcConnection(String datastore) {
        checkClosed();
        return realPM.getJdbcConnection(datastore);
    }

    public String getConnectionURL(String dataStore) {
        checkClosed();
        return realPM.getConnectionURL(dataStore);
    }

    public String getConnectionDriverName(String dataStore) {
        checkClosed();
        return realPM.getConnectionDriverName(dataStore);
    }

    public boolean isClosed() {
        return realPM == null;
    }

    public void close() {
        if (realPM == null) {
            return;
        }
        realPM.close();
    }

    public Transaction currentTransaction() {
        checkClosed();
        return this;
    }

    public void evict(Object o) {
        checkClosed();
        realPM.evict(o);
    }

    public void evictAll(Object[] objects) {
        checkClosed();
        realPM.evictAll(objects);
    }

    public void evictAll(Collection collection) {
        checkClosed();
        realPM.evictAll(collection);
    }

    public void evictAll() {
        checkClosed();
        realPM.evictAll();
    }

    public void refresh(Object o) {
        checkClosed();
        realPM.refresh(o);
    }

    public void refreshAll(Object[] objects) {
        checkClosed();
        realPM.refreshAll(objects);
    }

    public void refreshAll(Collection collection) {
        checkClosed();
        realPM.refreshAll(collection);
    }

    public void refreshAll() {
        checkClosed();
        realPM.refreshAll();
    }

    public Query newQuery() {
        checkClosed();
        return realPM.newQuery();
    }

    public Query newQuery(Object o) {
        checkClosed();
        return realPM.newQuery(o);
    }

    public Query newQuery(String s, Object o) {
        checkClosed();
        return realPM.newQuery(s, o);
    }

    public Query newQuery(Class aClass) {
        checkClosed();
        return realPM.newQuery(aClass);
    }

    public Query newQuery(Extent extent) {
        checkClosed();
        return realPM.newQuery(extent);
    }

    public Query newQuery(Class aClass, Collection collection) {
        checkClosed();
        return realPM.newQuery(aClass, collection);
    }

    public Query newQuery(Class aClass, String s) {
        checkClosed();
        return realPM.newQuery(aClass, s);
    }

    public Query newQuery(Class aClass, Collection collection, String s) {
        checkClosed();
        return realPM.newQuery(aClass, collection, s);
    }

    public Query newQuery(Extent extent, String s) {
        checkClosed();
        return realPM.newQuery(extent, s);
    }

    public Extent getExtent(Class aClass, boolean b) {
        checkClosed();
        return realPM.getExtent(aClass, b);
    }

    public Object getObjectById(Object o, boolean b) {
        checkClosed();
        return realPM.getObjectById(o, b);
    }

    public Object getObjectId(Object o) {
        checkClosed();
        return realPM.getObjectId(o);
    }

    public Object getTransactionalObjectId(Object o) {
        checkClosed();
        return realPM.getTransactionalObjectId(o);
    }

    public Object newObjectIdInstance(Class aClass, String s) {
        checkClosed();
        return realPM.newObjectIdInstance(aClass, s);
    }

    public void makePersistent(Object o) {
        checkClosed();
        realPM.makePersistent(o);
    }

    public void makePersistentAll(Object[] objects) {
        checkClosed();
        realPM.makePersistentAll(objects);
    }

    public void makePersistentAll(Collection collection) {
        checkClosed();
        realPM.makePersistentAll(collection);
    }

    public void deletePersistent(Object o) {
        checkClosed();
        realPM.deletePersistent(o);
    }

    public void deletePersistentAll(Object[] objects) {
        checkClosed();
        realPM.deletePersistentAll(objects);
    }

    public void deletePersistentAll(Collection collection) {
        checkClosed();
        realPM.deletePersistentAll(collection);
    }

    public void makeTransient(Object o) {
        checkClosed();
        realPM.makeTransient(o);
    }

    public void makeTransientAll(Object[] objects) {
        checkClosed();
        realPM.makeTransientAll(objects);
    }

    public void makeTransientAll(Collection collection) {
        checkClosed();
        realPM.makeTransientAll(collection);
    }

    public void makeTransactional(Object o) {
        checkClosed();
        realPM.makeTransactional(o);
    }

    public void makeTransactionalAll(Object[] objects) {
        checkClosed();
        realPM.makeTransactionalAll(objects);
    }

    public void makeTransactionalAll(Collection collection) {
        checkClosed();
        realPM.makeTransactionalAll(collection);
    }

    public void makeNontransactional(Object o) {
        checkClosed();
        realPM.makeNontransactional(o);
    }

    public void makeNontransactionalAll(Object[] objects) {
        checkClosed();
        realPM.makeNontransactionalAll(objects);
    }

    public void makeNontransactionalAll(Collection collection) {
        checkClosed();
        realPM.makeNontransactionalAll(collection);
    }

    public void retrieve(Object o) {
        checkClosed();
        realPM.retrieve(o);
    }

    public void retrieveAll(Collection pcs, boolean DFGOnly) {
        checkClosed();
        realPM.retrieveAll(pcs, DFGOnly);
    }

    public void retrieveAll(Object[] pcs, boolean DFGOnly) {
        checkClosed();
        realPM.retrieveAll(pcs, DFGOnly);
    }

    public void retrieveAll(Collection collection) {
        checkClosed();
        realPM.retrieveAll(collection);
    }

    public void retrieveAll(Object[] objects) {
        checkClosed();
        realPM.retrieveAll(objects);
    }

    public void setUserObject(Object o) {
        checkClosed();
        realPM.setUserObject(o);
    }

    public Object getUserObject() {
        checkClosed();
        return realPM.getUserObject();
    }

    public PersistenceManagerFactory getPersistenceManagerFactory() {
        checkClosed();
        return realPM.getPersistenceManagerFactory();
    }

    public Class getObjectIdClass(Class aClass) {
        checkClosed();
        return realPM.getObjectIdClass(aClass);
    }

    public void setMultithreaded(boolean b) {
        checkClosed();
        realPM.setMultithreaded(b);
    }

    public boolean getMultithreaded() {
        checkClosed();
        return realPM.getMultithreaded();
    }

    public void setIgnoreCache(boolean b) {
        checkClosed();
        realPM.setIgnoreCache(b);
    }

    public boolean getIgnoreCache() {
        checkClosed();
        return realPM.getIgnoreCache();
    }


    public void commit(Xid xid, boolean b) throws XAException {
        checkClosed();
        realPM.commit(xid, b);
    }

    public void end(Xid xid, int i) throws XAException {
        checkClosed();
        realPM.end(xid, i);
    }

    public void forget(Xid xid) throws XAException {
        checkClosed();
        realPM.forget(xid);
    }

    public int getTransactionTimeout() throws XAException {
        checkClosed();
        return realPM.getTransactionTimeout();
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (realPM == null) return false;
        return realPM.isSameRM(xaResource);
    }

    public int prepare(Xid xid) throws XAException {
        checkClosed();
        return realPM.prepare(xid);
    }

    public Xid[] recover(int i) throws XAException {
        checkClosed();
        return realPM.recover(i);
    }

    public void rollback(Xid xid) throws XAException {
        checkClosed();
        realPM.rollback(xid);
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        checkClosed();
        return realPM.setTransactionTimeout(i);
    }

    public void start(Xid xid, int i) throws XAException {
        checkClosed();
        realPM.start(xid, i);
    }

    public void afterCompletion(int i) {
        checkClosed();
        realPM.afterCompletion(i);
    }

    public void beforeCompletion() {
        checkClosed();
        realPM.beforeCompletion();
    }


    public OID getInternalOID(final PersistenceCapable pc) {
        checkClosed();
        return realPM.getInternalOID(pc);
    }

    public PCStateMan getInternalSM(final PersistenceCapable pc) {
        checkClosed();
        return realPM.getInternalSM(pc);
    }

    public PCStateMan getInternalSM(OID oid) {
        checkClosed();
        return realPM.getInternalSM(oid);
    }

    public State getInternaleState(PersistenceCapable pc) {
        checkClosed();
        return realPM.getInternaleState(pc);
    }

    public void addTxStateObject(PCStateMan stateObject) {
        checkClosed();
        realPM.addTxStateObject(stateObject);
    }

    public void removeTxStateObject(PCStateMan stateObject) {
        checkClosed();
        realPM.removeTxStateObject(stateObject);
    }

    public boolean isOptimistic() {
        checkClosed();
        return realPM.getOptimistic();
    }

    public boolean isRetainValues() {
        checkClosed();
        return realPM.getRetainValues();
    }

    public boolean isRestoreValues() {
        checkClosed();
        return realPM.getRestoreValues();
    }

    public boolean isActive() {
        checkClosed();
        return realPM.isActive();
    }

    public void begin() {
        checkClosed();
        realPM.begin();
    }

    public void commit() {
        checkClosed();
        realPM.commit();
    }

    public boolean getNontransactionalRead() {
        checkClosed();
        return realPM.getNontransactionalRead();
    }

    public boolean getNontransactionalWrite() {
        checkClosed();
        return realPM.getNontransactionalWrite();
    }

    public boolean getOptimistic() {
        checkClosed();
        return realPM.getOptimistic();
    }

    public PersistenceManager getPersistenceManager() {
        checkClosed();
        return this;
    }

    public boolean getRestoreValues() {
        checkClosed();
        return realPM.getRestoreValues();
    }

    public boolean getRetainValues() {
        checkClosed();
        return realPM.getRetainValues();
    }


    public Synchronization getSynchronization() {
        checkClosed();
        return realPM.getSynchronization();
    }


    public void rollback() {
        checkClosed();
        realPM.rollback();
    }

    public void setNontransactionalRead(boolean b) {
        checkClosed();
        realPM.setNontransactionalRead(b);
    }

    public void setNontransactionalWrite(boolean b) {
        checkClosed();
        realPM.setNontransactionalWrite(b);
    }

    public void setOptimistic(boolean b) {
        checkClosed();
        realPM.setOptimistic(b);
    }

    public void setRestoreValues(boolean b) {
        checkClosed();
        realPM.setRestoreValues(b);
    }

    public void setRetainValues(boolean b) {
        checkClosed();
        realPM.setRetainValues(b);
    }


    public void setSynchronization(Synchronization synchronization) {
        checkClosed();
        realPM.setSynchronization(synchronization);
    }


    public void setDatastoreTxLocking(int mode) {
        checkClosed();
        realPM.setDatastoreTxLocking(mode);
    }

    public int getDatastoreTxLocking() {
        checkClosed();
        return realPM.getDatastoreTxLocking();
    }

    public void setRetainConnectionInOptTx(boolean on) {
        checkClosed();
        realPM.setRetainConnectionInOptTx(on);
    }

    public Object getObjectByIdFromCache(Object oid) {
        checkClosed();
        return realPM.getObjectByIdFromCache(oid);
    }

    public boolean isHollow(Object pc) {
        checkClosed();
        return realPM.isHollow(pc);
    }

    public boolean hasIdentity(Object pc) {
        checkClosed();
        return realPM.hasIdentity(pc);
    }

    public Object newObjectIdInstance(Class pcClass, String str,
            boolean resolved) {
        checkClosed();
        return realPM.newObjectIdInstance(pcClass, str, resolved);
    }

    public Object getObjectByIDString(String value, boolean toValidate,
            boolean resolved) {
        checkClosed();
        return realPM.getObjectByIDString(value, toValidate, resolved);
    }

    public void logEvent(int level, String description, int ms) {
        checkClosed();
        realPM.logEvent(level, description, ms);
    }

    public Query versantNewNamedQuery(Class cls, String queryName) {
        checkClosed();
        return realPM.versantNewNamedQuery(cls, queryName);
    }

    public boolean isCheckModelConsistencyOnCommit() {
        checkClosed();
        return realPM.isCheckModelConsistencyOnCommit();
    }

    public void setCheckModelConsistencyOnCommit(boolean on) {
        checkClosed();
        realPM.setCheckModelConsistencyOnCommit(on);
    }

    public void checkModelConsistency() {
        checkClosed();
        realPM.checkModelConsistency();
    }

    public int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex) {
        checkClosed();
        return realPM.getObjectsById(oids, length, data, stateFieldNo,
                classMetaDataIndex);
    }

    public Collection versantDetachCopy(Collection pcs, String fetchGroup) {
        checkClosed();
        return realPM.versantDetachCopy(pcs, fetchGroup);
    }

    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional) {
        checkClosed();
        return realPM.versantAttachCopy(detached, makeTransactional);
    }

    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional, boolean shallow) {
        return realPM.versantAttachCopy(detached, makeTransactional, shallow);
    }

    public void evictFromL2CacheAfterCommit(Object o) {
        checkClosed();
        realPM.evictFromL2CacheAfterCommit(o);
    }

    public void evictAllFromL2CacheAfterCommit(Object[] a) {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit(a);
    }

    public void evictAllFromL2CacheAfterCommit(Collection c) {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit(c);
    }

    public void evictAllFromL2CacheAfterCommit(Class cls,
            boolean includeSubclasses) {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit(cls, includeSubclasses);
    }

    public void evictAllFromL2CacheAfterCommit() {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit();
    }

    public void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        checkClosed();
        realPM.addInstanceLifecycleListener(listener, classes);
    }

    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        checkClosed();
        realPM.removeInstanceLifecycleListener(listener);
    }

    public Object attachCopy(Object o, boolean b) {
        checkClosed();
        return realPM.attachCopy(o, b);
    }

    public Collection attachCopyAll(Collection collection, boolean b) {
        checkClosed();
        return realPM.attachCopyAll(collection, b);
    }

    public Object[] attachCopyAll(Object[] objects, boolean b) {
        checkClosed();
        return realPM.attachCopyAll(objects, b);
    }

    public void checkConsistency() {
        checkClosed();
        realPM.checkConsistency();
    }

    public Object detachCopy(Object o) {
        checkClosed();
        return realPM.detachCopy(o);
    }

    public Collection detachCopyAll(Collection collection) {
        checkClosed();
        return realPM.detachCopyAll(collection);
    }

    public Object[] detachCopyAll(Object[] objects) {
        checkClosed();
        return realPM.detachCopyAll(objects);
    }

    public JDOConnection getDataStoreConnection() {
        checkClosed();
        return realPM.getDataStoreConnection();
    }

    public Extent getExtent(Class aClass) {
        checkClosed();
        return realPM.getExtent(aClass);
    }

    public FetchPlan getFetchPlan() {
        checkClosed();
        return realPM.getFetchPlan();
    }

    public Object getObjectById(Class aClass, Object o) {
        checkClosed();
        return realPM.getObjectById(aClass, o);
    }

    public Object getObjectById(Object o) {
        checkClosed();
        return realPM.getObjectById(o);
    }

    public Collection getObjectsById(Collection collection) {
        checkClosed();
        return realPM.getObjectsById(collection);
    }

    public Collection getObjectsById(Collection collection, boolean b) {
        checkClosed();
        return realPM.getObjectsById(collection, b);
    }

    public Object[] getObjectsById(Object[] objects) {
        checkClosed();
        return realPM.getObjectsById(objects);
    }

    public Object[] getObjectsById(Object[] objects, boolean b) {
        checkClosed();
        return realPM.getObjectsById(objects, b);
    }

    public Sequence getSequence(String s) {
        checkClosed();
        return realPM.getSequence(s);
    }

    public Object getUserObject(Object o) {
        checkClosed();
        return realPM.getUserObject(o);
    }

    public Object newInstance(Class aClass) {
        checkClosed();
        return realPM.newInstance(aClass);
    }

    public Query newNamedQuery(Class aClass, String s) {
        checkClosed();
        return realPM.newNamedQuery(aClass, s);
    }

    public Object newObjectIdInstance(Class aClass, Object o) {
        checkClosed();
        return realPM.newObjectIdInstance(aClass, o);
    }

    public Query newQuery(String s) {
        checkClosed();
        return realPM.newQuery(s);
    }

    public Object putUserObject(Object o, Object o1) {
        checkClosed();
        return realPM.putUserObject(o, o1);
    }

    public void refreshAll(JDOException e) {
        checkClosed();
        realPM.refreshAll(e);
    }

    public Object removeUserObject(Object o) {
        checkClosed();
        return realPM.removeUserObject(o);
    }

    public boolean getRollbackOnly() {
        checkClosed();
        return realPM.getRollbackOnly();
    }

    public void setRollbackOnly() {
        checkClosed();
        realPM.setRollbackOnly();
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
