
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



import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.*;
import com.versant.core.storagemanager.ExecuteQueryReturn;

/**
 * This is a proxy for VersantPersistenceManagerImp with all methods
 * synchronized for multithreaded access.
 *
 * @see PMProxy
 * @see UnsynchronizedPMProxy
 */
public final class SynchronizedPMProxy extends PMProxy {

    private VersantPersistenceManagerImp realPM;

    public SynchronizedPMProxy(VersantPersistenceManagerImp realPM) {
        this.realPM = realPM;
    }

    public synchronized VersantStateManager getVersantStateManager(PersistenceCapable pc) {
        return realPM.getVersantStateManager(pc);
    }

    public synchronized Object getObjectByIdForState(OID oid, int stateFieldNo,
            int navClassIndex, OID fromOID) {
        return realPM.getObjectByIdForState(oid, stateFieldNo, navClassIndex, fromOID);
    }

    public synchronized QueryResultWrapper executeQuery(CompiledQuery cq, Object[] params) {
        return realPM.getStorageManager().executeQuery(realPM, null, cq, params);
    }

    public synchronized QueryResultContainer getNextQueryResult(QueryResultWrapper aQrs, int skipAmount) {
        return realPM.getStorageManager().fetchNextQueryResult(realPM,
                ((ExecuteQueryReturn)aQrs).getRunningQuery(), skipAmount);
    }

    public synchronized void flushIfDepOn(int[] bits) {
        realPM.flushIfDepOn(bits);
    }

    public synchronized void processLocalCacheReferenceQueue() {
        realPM.processLocalCacheReferenceQueue();
    }

    public synchronized void addToCache(StatesReturned container) {
        realPM.addToCache(container);
    }

    public synchronized void closeQuery(QueryResultWrapper qrw) {
        realPM.getStorageManager().closeQuery(((ExecuteQueryReturn)qrw).getRunningQuery());
    }

    public synchronized QueryResultContainer getAbsolute(QueryResultWrapper qrsIF, int index, int fetchAmount) {
        return realPM.getStorageManager().fetchRandomAccessQueryResult(realPM,
                ((ExecuteQueryReturn)qrsIF).getRunningQuery(), index,
                fetchAmount);
    }

    public synchronized int getResultCount(QueryResultWrapper qrsIF) {
        return realPM.getStorageManager().getRandomAccessQueryCount(realPM,
                ((ExecuteQueryReturn)qrsIF).getRunningQuery());
    }

    public synchronized QueryResultContainer getAllQueryResults(CompiledQuery cq,
            Object[] params) {
        return realPM.getStorageManager().executeQueryAll(realPM, null, cq, params);
    }

    public synchronized void setMasterOnDetail(PersistenceCapable detail, int managedFieldNo,
            PersistenceCapable master, boolean removeFromCurrentMaster) {
        realPM.setMasterOnDetail(detail, managedFieldNo, master, removeFromCurrentMaster);
    }

    public synchronized Object getObjectField(PersistenceCapable pc, int fieldNo) {
        return realPM.getObjectField(pc, fieldNo);
    }

    public synchronized int getQueryRowCount(CompiledQuery cq, Object[] params) {
        return realPM.getStorageManager().executeQueryCount(null, cq, params);
    }

    public synchronized Object getOptimisticLockingValue(Object o) {
        checkClosed();
        return realPM.getOptimisticLockingValue(o);
    }

    public synchronized void setPmCacheRefType(Object pc, int type) {
        checkClosed();
        realPM.setPmCacheRefType(pc, type);
    }

    public synchronized void setPmCacheRefType(Object[] pcs, int type) {
        checkClosed();
        realPM.setPmCacheRefType(pcs, type);
    }

    public synchronized void setPmCacheRefType(Collection col, int type) {
        checkClosed();
        realPM.setPmCacheRefType(col, type);
    }

    public synchronized void setPmCacheRefType(int type) {
        checkClosed();
        realPM.setPmCacheRefType(type);
    }

    public synchronized int getPmCacheRefType() {
        checkClosed();
        return realPM.getPmCacheRefType();
    }

    /**
     * If default fetch group fields will be intercepted on a
     * Persistent-Non-Transaction instance. If this is disabled then such a
     * instance will not be 'refreshed' if accessed in a datastore transaction.
     */
    public synchronized boolean isInterceptDfgFieldAccess() {
        return realPM.isInterceptDfgFieldAccess();
    }

    public synchronized void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess) {
        realPM.setInterceptDfgFieldAccess(interceptDfgFieldAccess);
    }

    public synchronized VersantPersistenceManagerImp getRealPM() {
        checkClosed();
        return realPM;
    }

    VersantPersistenceManagerImp getRealPMNoCheck() {
        return realPM;
    }

    public synchronized void resetPM() {
        realPM = null;
    }

    public synchronized boolean isRealPMNull() {
        return realPM == null;
    }

    public synchronized List versantAllDirtyInstances() {
        checkClosed();
        return realPM.versantAllDirtyInstances();
    }

    private void checkClosed() {
        if (realPM == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The pm is closed");
        }
    }

    public synchronized boolean isDirty() {
        checkClosed();
        return realPM.isDirty();
    }

    public synchronized void cancelQueryExecution() {
        checkClosed();
        realPM.cancelQueryExecution();
    }

    public synchronized Object getObjectByIDString(String value, boolean toValidate) {
        checkClosed();
        return realPM.getObjectByIDString(value, toValidate);
    }

    public synchronized void loadFetchGroup(Object pc, String name) {
        checkClosed();
        realPM.loadFetchGroup(pc, name);
    }

    public synchronized void flush() {
        checkClosed();
        realPM.flush();
    }

    public synchronized void flush(boolean retainState) {
        checkClosed();
        realPM.flush(retainState);
    }

    public synchronized void makeTransientRecursive(Object pc) {
        checkClosed();
        realPM.makeTransientRecursive(pc);
    }

    public synchronized Connection getJdbcConnection(String datastore) {
        checkClosed();
        return realPM.getJdbcConnection(datastore);
    }

    public synchronized String getConnectionURL(String dataStore) {
        checkClosed();
        return realPM.getConnectionURL(dataStore);
    }

    public synchronized String getConnectionDriverName(String dataStore) {
        checkClosed();
        return realPM.getConnectionDriverName(dataStore);
    }

    public synchronized boolean isClosed() {
        return realPM == null;
    }

    public synchronized void close() {
        if (realPM == null) {
            return;
        }
        realPM.close();
    }

    public synchronized Transaction currentTransaction() {
        checkClosed();
        return this;
    }

    public synchronized void evict(Object o) {
        checkClosed();
        realPM.evict(o);
    }

    public synchronized void evictAll(Object[] objects) {
        checkClosed();
        realPM.evictAll(objects);
    }

    public synchronized void evictAll(Collection collection) {
        checkClosed();
        realPM.evictAll(collection);
    }

    public synchronized void evictAll() {
        checkClosed();
        realPM.evictAll();
    }

    public synchronized void refresh(Object o) {
        checkClosed();
        realPM.refresh(o);
    }

    public synchronized void refreshAll(Object[] objects) {
        checkClosed();
        realPM.refreshAll(objects);
    }

    public synchronized void refreshAll(Collection collection) {
        checkClosed();
        realPM.refreshAll(collection);
    }

    public synchronized void refreshAll() {
        checkClosed();
        realPM.refreshAll();
    }

    public synchronized Query newQuery() {
        checkClosed();
        return realPM.newQuery();
    }

    public synchronized Query newQuery(Object o) {
        checkClosed();
        return realPM.newQuery(o);
    }

    public synchronized Query newQuery(String s, Object o) {
        checkClosed();
        return realPM.newQuery(s, o);
    }

    public synchronized Query newQuery(Class aClass) {
        checkClosed();
        return realPM.newQuery(aClass);
    }

    public synchronized Query newQuery(Extent extent) {
        checkClosed();
        return realPM.newQuery(extent);
    }

    public synchronized Query newQuery(Class aClass, Collection collection) {
        checkClosed();
        return realPM.newQuery(aClass, collection);
    }

    public synchronized Query newQuery(Class aClass, String s) {
        checkClosed();
        return realPM.newQuery(aClass, s);
    }

    public synchronized Query newQuery(Class aClass, Collection collection, String s) {
        checkClosed();
        return realPM.newQuery(aClass, collection, s);
    }

    public synchronized Query newQuery(Extent extent, String s) {
        checkClosed();
        return realPM.newQuery(extent, s);
    }

    public synchronized Extent getExtent(Class aClass, boolean b) {
        checkClosed();
        return realPM.getExtent(aClass, b);
    }

    public synchronized Object getObjectById(Object o, boolean b) {
        checkClosed();
        return realPM.getObjectById(o, b);
    }

    public synchronized Object getObjectId(Object o) {
        checkClosed();
        return realPM.getObjectId(o);
    }

    public synchronized Object getTransactionalObjectId(Object o) {
        checkClosed();
        return realPM.getTransactionalObjectId(o);
    }

    public synchronized Object newObjectIdInstance(Class aClass, String s) {
        checkClosed();
        return realPM.newObjectIdInstance(aClass, s);
    }

    public synchronized void makePersistent(Object o) {
        checkClosed();
        realPM.makePersistent(o);
    }

    public synchronized void makePersistentAll(Object[] objects) {
        checkClosed();
        realPM.makePersistentAll(objects);
    }

    public synchronized void makePersistentAll(Collection collection) {
        checkClosed();
        realPM.makePersistentAll(collection);
    }

    public synchronized void deletePersistent(Object o) {
        checkClosed();
        realPM.deletePersistent(o);
    }

    public synchronized void deletePersistentAll(Object[] objects) {
        checkClosed();
        realPM.deletePersistentAll(objects);
    }

    public synchronized void deletePersistentAll(Collection collection) {
        checkClosed();
        realPM.deletePersistentAll(collection);
    }

    public synchronized void makeTransient(Object o) {
        checkClosed();
        realPM.makeTransient(o);
    }

    public synchronized void makeTransientAll(Object[] objects) {
        checkClosed();
        realPM.makeTransientAll(objects);
    }

    public synchronized void makeTransientAll(Collection collection) {
        checkClosed();
        realPM.makeTransientAll(collection);
    }

    public synchronized void makeTransactional(Object o) {
        checkClosed();
        realPM.makeTransactional(o);
    }

    public synchronized void makeTransactionalAll(Object[] objects) {
        checkClosed();
        realPM.makeTransactionalAll(objects);
    }

    public synchronized void makeTransactionalAll(Collection collection) {
        checkClosed();
        realPM.makeTransactionalAll(collection);
    }

    public synchronized void makeNontransactional(Object o) {
        checkClosed();
        realPM.makeNontransactional(o);
    }

    public synchronized void makeNontransactionalAll(Object[] objects) {
        checkClosed();
        realPM.makeNontransactionalAll(objects);
    }

    public synchronized void makeNontransactionalAll(Collection collection) {
        checkClosed();
        realPM.makeNontransactionalAll(collection);
    }

    public synchronized void retrieve(Object o) {
        checkClosed();
        realPM.retrieve(o);
    }

    public synchronized void retrieveAll(Collection pcs, boolean DFGOnly) {
        checkClosed();
        realPM.retrieveAll(pcs, DFGOnly);
    }

    public synchronized void retrieveAll(Object[] pcs, boolean DFGOnly) {
        checkClosed();
        realPM.retrieveAll(pcs, DFGOnly);
    }

    public synchronized void retrieveAll(Collection collection) {
        checkClosed();
        realPM.retrieveAll(collection);
    }

    public synchronized void retrieveAll(Object[] objects) {
        checkClosed();
        realPM.retrieveAll(objects);
    }

    public synchronized void setUserObject(Object o) {
        checkClosed();
        realPM.setUserObject(o);
    }

    public synchronized Object getUserObject() {
        checkClosed();
        return realPM.getUserObject();
    }

    public synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
        checkClosed();
        return realPM.getPersistenceManagerFactory();
    }

    public synchronized Class getObjectIdClass(Class aClass) {
        checkClosed();
        return realPM.getObjectIdClass(aClass);
    }

    public synchronized void setMultithreaded(boolean b) {
        checkClosed();
        realPM.setMultithreaded(b);
    }

    public synchronized boolean getMultithreaded() {
        checkClosed();
        return realPM.getMultithreaded();
    }

    public synchronized void setIgnoreCache(boolean b) {
        checkClosed();
        realPM.setIgnoreCache(b);
    }

    public synchronized boolean getIgnoreCache() {
        checkClosed();
        return realPM.getIgnoreCache();
    }


    public synchronized void commit(Xid xid, boolean b) throws XAException {
        checkClosed();
        realPM.commit(xid, b);
    }

    public synchronized void end(Xid xid, int i) throws XAException {
        checkClosed();
        realPM.end(xid, i);
    }

    public synchronized void forget(Xid xid) throws XAException {
        checkClosed();
        realPM.forget(xid);
    }

    public synchronized int getTransactionTimeout() throws XAException {
        checkClosed();
        return realPM.getTransactionTimeout();
    }

    public synchronized boolean isSameRM(XAResource xaResource) throws XAException {
        if (realPM == null) return false;
        return realPM.isSameRM(xaResource);
    }

    public synchronized int prepare(Xid xid) throws XAException {
        checkClosed();
        return realPM.prepare(xid);
    }

    public synchronized Xid[] recover(int i) throws XAException {
        checkClosed();
        return realPM.recover(i);
    }

    public synchronized void rollback(Xid xid) throws XAException {
        checkClosed();
        realPM.rollback(xid);
    }

    public synchronized boolean setTransactionTimeout(int i) throws XAException {
        checkClosed();
        return realPM.setTransactionTimeout(i);
    }

    public synchronized void start(Xid xid, int i) throws XAException {
        checkClosed();
        realPM.start(xid, i);
    }

    public synchronized void afterCompletion(int i) {
        checkClosed();
        realPM.afterCompletion(i);
    }

    public synchronized void beforeCompletion() {
        checkClosed();
        realPM.beforeCompletion();
    }


    public synchronized OID getInternalOID(final PersistenceCapable pc) {
        checkClosed();
        return realPM.getInternalOID(pc);
    }

    public synchronized PCStateMan getInternalSM(final PersistenceCapable pc) {
        checkClosed();
        return realPM.getInternalSM(pc);
    }

    public synchronized PCStateMan getInternalSM(OID oid) {
        checkClosed();
        return realPM.getInternalSM(oid);
    }

    public synchronized State getInternaleState(PersistenceCapable pc) {
        checkClosed();
        return realPM.getInternaleState(pc);
    }

    public synchronized void addTxStateObject(PCStateMan stateObject) {
        checkClosed();
        realPM.addTxStateObject(stateObject);
    }

    public synchronized void removeTxStateObject(PCStateMan stateObject) {
        checkClosed();
        realPM.removeTxStateObject(stateObject);
    }

    public synchronized boolean isOptimistic() {
        checkClosed();
        return realPM.getOptimistic();
    }

    public synchronized boolean isRetainValues() {
        checkClosed();
        return realPM.getRetainValues();
    }

    public synchronized boolean isRestoreValues() {
        checkClosed();
        return realPM.getRestoreValues();
    }

    public synchronized boolean isActive() {
        checkClosed();
        return realPM.isActive();
    }

    public synchronized void begin() {
        checkClosed();
        realPM.begin();
    }

    public synchronized void commit() {
        checkClosed();
        realPM.commit();
    }

    public synchronized boolean getNontransactionalRead() {
        checkClosed();
        return realPM.getNontransactionalRead();
    }

    public synchronized boolean getNontransactionalWrite() {
        checkClosed();
        return realPM.getNontransactionalWrite();
    }

    public synchronized boolean getOptimistic() {
        checkClosed();
        return realPM.getOptimistic();
    }

    public synchronized PersistenceManager getPersistenceManager() {
        checkClosed();
        return this;
    }

    public synchronized boolean getRestoreValues() {
        checkClosed();
        return realPM.getRestoreValues();
    }

    public synchronized boolean getRetainValues() {
        checkClosed();
        return realPM.getRetainValues();
    }


    public synchronized Synchronization getSynchronization() {
        checkClosed();
        return realPM.getSynchronization();
    }


    public synchronized void rollback() {
        checkClosed();
        realPM.rollback();
    }

    public synchronized void setNontransactionalRead(boolean b) {
        checkClosed();
        realPM.setNontransactionalRead(b);
    }

    public synchronized void setNontransactionalWrite(boolean b) {
        checkClosed();
        realPM.setNontransactionalWrite(b);
    }

    public synchronized void setOptimistic(boolean b) {
        checkClosed();
        realPM.setOptimistic(b);
    }

    public synchronized void setRestoreValues(boolean b) {
        checkClosed();
        realPM.setRestoreValues(b);
    }

    public synchronized void setRetainValues(boolean b) {
        checkClosed();
        realPM.setRetainValues(b);
    }


    public synchronized void setSynchronization(Synchronization synchronization) {
        checkClosed();
        realPM.setSynchronization(synchronization);
    }


    public synchronized void setDatastoreTxLocking(int mode) {
        checkClosed();
        realPM.setDatastoreTxLocking(mode);
    }

    public synchronized int getDatastoreTxLocking() {
        checkClosed();
        return realPM.getDatastoreTxLocking();
    }

    public synchronized void setRetainConnectionInOptTx(boolean on) {
        checkClosed();
        realPM.setRetainConnectionInOptTx(on);
    }

    public synchronized Object getObjectByIdFromCache(Object oid) {
        checkClosed();
        return realPM.getObjectByIdFromCache(oid);
    }

    public synchronized boolean isHollow(Object pc) {
        checkClosed();
        return realPM.isHollow(pc);
    }

    public synchronized boolean hasIdentity(Object pc) {
        checkClosed();
        return realPM.hasIdentity(pc);
    }

    public synchronized Object newObjectIdInstance(Class pcClass, String str,
            boolean resolved) {
        checkClosed();
        return realPM.newObjectIdInstance(pcClass, str, resolved);
    }

    public synchronized Object getObjectByIDString(String value, boolean toValidate,
            boolean resolved) {
        checkClosed();
        return realPM.getObjectByIDString(value, toValidate, resolved);
    }

    public synchronized void logEvent(int level, String description, int ms) {
        checkClosed();
        realPM.logEvent(level, description, ms);
    }

    public synchronized Query versantNewNamedQuery(Class cls, String queryName) {
        checkClosed();
        return realPM.versantNewNamedQuery(cls, queryName);
    }

    public synchronized boolean isCheckModelConsistencyOnCommit() {
        checkClosed();
        return realPM.isCheckModelConsistencyOnCommit();
    }

    public synchronized void setCheckModelConsistencyOnCommit(boolean on) {
        checkClosed();
        realPM.setCheckModelConsistencyOnCommit(on);
    }

    public synchronized void checkModelConsistency() {
        checkClosed();
        realPM.checkModelConsistency();
    }

    public synchronized int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex) {
        checkClosed();
        return realPM.getObjectsById(oids, length, data, stateFieldNo,
                classMetaDataIndex);
    }

    public synchronized Collection versantDetachCopy(Collection pcs, String fetchGroup) {
        checkClosed();
        return realPM.versantDetachCopy(pcs, fetchGroup);
    }

    public synchronized Collection versantAttachCopy(Collection detached,
            boolean makeTransactional) {
        checkClosed();
        return realPM.versantAttachCopy(detached, makeTransactional);
    }

    public synchronized Collection versantAttachCopy(Collection detached,
            boolean makeTransactional, boolean shallow) {
        return realPM.versantAttachCopy(detached, makeTransactional, shallow);
    }

    public synchronized void evictFromL2CacheAfterCommit(Object o) {
        checkClosed();
        realPM.evictFromL2CacheAfterCommit(o);
    }

    public synchronized void evictAllFromL2CacheAfterCommit(Object[] a) {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit(a);
    }

    public synchronized void evictAllFromL2CacheAfterCommit(Collection c) {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit(c);
    }

    public synchronized void evictAllFromL2CacheAfterCommit(Class cls,
            boolean includeSubclasses) {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit(cls, includeSubclasses);
    }

    public synchronized void evictAllFromL2CacheAfterCommit() {
        checkClosed();
        realPM.evictAllFromL2CacheAfterCommit();
    }

    public synchronized void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        checkClosed();
        realPM.addInstanceLifecycleListener(listener, classes);
    }

    public synchronized void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        checkClosed();
        realPM.removeInstanceLifecycleListener(listener);
    }

    public synchronized Object attachCopy(Object o, boolean b) {
        checkClosed();
        return realPM.attachCopy(o, b);
    }

    public synchronized Collection attachCopyAll(Collection collection, boolean b) {
        checkClosed();
        return realPM.attachCopyAll(collection, b);
    }

    public synchronized Object[] attachCopyAll(Object[] objects, boolean b) {
        checkClosed();
        return realPM.attachCopyAll(objects, b);
    }

    public synchronized void checkConsistency() {
        checkClosed();
        realPM.checkConsistency();
    }

    public synchronized Object detachCopy(Object o) {
        checkClosed();
        return realPM.detachCopy(o);
    }

    public synchronized Collection detachCopyAll(Collection collection) {
        checkClosed();
        return realPM.detachCopyAll(collection);
    }

    public synchronized Object[] detachCopyAll(Object[] objects) {
        checkClosed();
        return realPM.detachCopyAll(objects);
    }

    public synchronized JDOConnection getDataStoreConnection() {
        checkClosed();
        return realPM.getDataStoreConnection();
    }

    public synchronized Extent getExtent(Class aClass) {
        checkClosed();
        return realPM.getExtent(aClass);
    }

    public synchronized FetchPlan getFetchPlan() {
        checkClosed();
        return realPM.getFetchPlan();
    }

    public synchronized Object getObjectById(Class aClass, Object o) {
        checkClosed();
        return realPM.getObjectById(aClass, o);
    }

    public synchronized Object getObjectById(Object o) {
        checkClosed();
        return realPM.getObjectById(o);
    }

    public synchronized Collection getObjectsById(Collection collection) {
        checkClosed();
        return realPM.getObjectsById(collection);
    }

    public synchronized Collection getObjectsById(Collection collection, boolean b) {
        checkClosed();
        return realPM.getObjectsById(collection, b);
    }

    public synchronized Object[] getObjectsById(Object[] objects) {
        checkClosed();
        return realPM.getObjectsById(objects);
    }

    public synchronized Object[] getObjectsById(Object[] objects, boolean b) {
        checkClosed();
        return realPM.getObjectsById(objects, b);
    }

    public synchronized Sequence getSequence(String s) {
        checkClosed();
        return realPM.getSequence(s);
    }

    public synchronized Object getUserObject(Object o) {
        checkClosed();
        return realPM.getUserObject(o);
    }

    public synchronized Object newInstance(Class aClass) {
        checkClosed();
        return realPM.newInstance(aClass);
    }

    public synchronized Query newNamedQuery(Class aClass, String s) {
        checkClosed();
        return realPM.newNamedQuery(aClass, s);
    }

    public synchronized Object newObjectIdInstance(Class aClass, Object o) {
        checkClosed();
        return realPM.newObjectIdInstance(aClass, o);
    }

    public synchronized Query newQuery(String s) {
        checkClosed();
        return realPM.newQuery(s);
    }

    public synchronized Object putUserObject(Object o, Object o1) {
        checkClosed();
        return realPM.putUserObject(o, o1);
    }

    public synchronized void refreshAll(JDOException e) {
        checkClosed();
        realPM.refreshAll(e);
    }

    public synchronized Object removeUserObject(Object o) {
        checkClosed();
        return realPM.removeUserObject(o);
    }

    public synchronized boolean getRollbackOnly() {
        checkClosed();
        return realPM.getRollbackOnly();
    }

    public synchronized void setRollbackOnly() {
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
