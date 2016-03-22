
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

import com.versant.core.common.Utils;

import javax.jdo.Transaction;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Extent;
import javax.jdo.spi.PersistenceCapable;

import javax.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
 
import java.sql.Connection;
import java.util.Collection;
import java.util.List;



import com.versant.core.common.OID;
import com.versant.core.common.*;

/**
 * Base class for proxies for VersantPersistenceManagerImp. This solves the case
 * where a client closes the pm and it returns to the pool, but the client
 * keeps a pc instance from the closed pm around. Synchronization is also
 * provided by a synchronized subclass.
 *
 * Note that all the methods from the interfaces are repeated here as abstract
 * because the IBM VMs barf otherwise.
 *
 * @see SynchronizedPMProxy
 * @see UnsynchronizedPMProxy
 */
public abstract class PMProxy implements VersantPMProxy, VersantPersistenceManager, Transaction

        , XAResource, Synchronization
 
 {

    public abstract void setPmCacheRefType(Object pc, int type);

    public abstract void setPmCacheRefType(Object[] pcs, int type);

    public abstract void setPmCacheRefType(Collection col, int type);

    public abstract void setPmCacheRefType(int type);

    public abstract int getPmCacheRefType();

    public abstract boolean isInterceptDfgFieldAccess();

    public abstract void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess);

    public abstract VersantPersistenceManagerImp getRealPM();

    /**
     * Do not check to see if the PM has been closed.
     */
    abstract VersantPersistenceManagerImp getRealPMNoCheck();

    public abstract void resetPM();

    public abstract List versantAllDirtyInstances();

    public abstract boolean isDirty();

    public abstract void cancelQueryExecution();

    public abstract Object getObjectByIDString(String value, boolean toValidate);

    public abstract void loadFetchGroup(Object pc, String name);

    public abstract void flush();

    public abstract void flush(boolean retainState);

    public abstract void makeTransientRecursive(Object pc);

    public abstract Connection getJdbcConnection(String datastore);

    public abstract String getConnectionURL(String dataStore);

    public abstract String getConnectionDriverName(String dataStore);

    public abstract boolean isClosed();

    public abstract void close();

    public abstract Transaction currentTransaction();

    public abstract void evict(Object o);

    public abstract void evictAll(Object[] objects);

    public abstract void evictAll(Collection collection);

    public abstract void evictAll();

    public abstract void refresh(Object o);

    public abstract void refreshAll(Object[] objects);

    public abstract void refreshAll(Collection collection);

    public abstract void refreshAll();

    public abstract Query newQuery();

    public abstract Query newQuery(Object o);

    public abstract Query newQuery(String s, Object o);

    public abstract Query newQuery(Class aClass);

    public abstract Query newQuery(Extent extent);

    public abstract Query newQuery(Class aClass, Collection collection);

    public abstract Query newQuery(Class aClass, String s);

    public abstract Query newQuery(Class aClass, Collection collection, String s);

    public abstract Query newQuery(Extent extent, String s);

    public abstract Extent getExtent(Class aClass, boolean b);

    public abstract Object getObjectById(Object o, boolean b);

    public abstract Object getObjectId(Object o);

    public abstract Object getTransactionalObjectId(Object o);

    public abstract Object newObjectIdInstance(Class aClass, String s);

    public abstract void makePersistent(Object o);

    public abstract void makePersistentAll(Object[] objects);

    public abstract void makePersistentAll(Collection collection);

    public abstract void deletePersistent(Object o);

    public abstract void deletePersistentAll(Object[] objects);

    public abstract void deletePersistentAll(Collection collection);

    public abstract void makeTransient(Object o);

    public abstract void makeTransientAll(Object[] objects);

    public abstract void makeTransientAll(Collection collection);

    public abstract void makeTransactional(Object o);

    public abstract void makeTransactionalAll(Object[] objects);

    public abstract void makeTransactionalAll(Collection collection);

    public abstract void makeNontransactional(Object o);

    public abstract void makeNontransactionalAll(Object[] objects);

    public abstract void makeNontransactionalAll(Collection collection);

    public abstract void retrieve(Object o);

    public abstract void retrieveAll(Collection pcs, boolean DFGOnly);

    public abstract void retrieveAll(Object[] pcs, boolean DFGOnly);

    public abstract void retrieveAll(Collection collection);

    public abstract void retrieveAll(Object[] objects);

    public abstract void setUserObject(Object o);

    public abstract Object getUserObject();

    public abstract PersistenceManagerFactory getPersistenceManagerFactory();

    public abstract Class getObjectIdClass(Class aClass);

    public abstract void setMultithreaded(boolean b);

    public abstract boolean getMultithreaded();

    public abstract void setIgnoreCache(boolean b);

    public abstract boolean getIgnoreCache();


    public abstract void commit(Xid xid, boolean b) throws XAException;

    public abstract void end(Xid xid, int i) throws XAException;

    public abstract void forget(Xid xid) throws XAException;

    public abstract int getTransactionTimeout() throws XAException;

    public abstract boolean isSameRM(XAResource xaResource) throws XAException;

    public abstract int prepare(Xid xid) throws XAException;

    public abstract Xid[] recover(int i) throws XAException;

    public abstract void rollback(Xid xid) throws XAException;

    public abstract boolean setTransactionTimeout(int i) throws XAException;

    public abstract void start(Xid xid, int i) throws XAException;

    public abstract void afterCompletion(int i);

    public abstract void beforeCompletion();


    public abstract OID getInternalOID(final PersistenceCapable pc);

    public abstract PCStateMan getInternalSM(final PersistenceCapable pc);

    public abstract boolean isActive();

    public abstract void begin();

    public abstract void commit();

    public abstract boolean getNontransactionalRead();

    public abstract boolean getNontransactionalWrite();

    public abstract boolean getOptimistic();

    public abstract PersistenceManager getPersistenceManager();

    public abstract boolean getRestoreValues();

    public abstract boolean getRetainValues();


    public abstract Synchronization getSynchronization();


    public abstract void rollback();

    public abstract void setNontransactionalRead(boolean b);

    public abstract void setNontransactionalWrite(boolean b);

    public abstract void setOptimistic(boolean b);

    public abstract void setRestoreValues(boolean b);

    public abstract void setRetainValues(boolean b);


    public abstract void setSynchronization(Synchronization synchronization);


    public abstract void setDatastoreTxLocking(int mode);

    public abstract int getDatastoreTxLocking();

    public abstract void setRetainConnectionInOptTx(boolean on);

    public abstract Object getObjectByIdFromCache(Object oid);

    public abstract boolean isHollow(Object pc);

    public abstract boolean hasIdentity(Object pc);

    public abstract Object newObjectIdInstance(Class pcClass, String str,
            boolean resolved);

    public abstract Object getObjectByIDString(String value, boolean toValidate,
            boolean resolved);

    public abstract void logEvent(int level, String description, int ms);

    public abstract Query versantNewNamedQuery(Class cls, String queryName);

    public abstract boolean isCheckModelConsistencyOnCommit();

    public abstract void setCheckModelConsistencyOnCommit(boolean on);

    public abstract void checkModelConsistency();

    /**
     * Get a user friendly string identifying this PM for use in error
     * messages and so on.
     */
    public String toMsgString() {
        StringBuffer s = new StringBuffer();
        s.append("0x");
        s.append(Integer.toHexString(System.identityHashCode(this)));
        VersantPersistenceManagerImp realPM = getRealPMNoCheck();
        if (realPM == null) {
            s.append(" CLOSED");
        } else {
            Object userObject = realPM.getUserObject();
            if (userObject != null) {
                s.append(' ');
                s.append(Utils.toString(userObject));
            }
        }
        return s.toString();
    }

    /**
     * Include identity hashcode to help clients identify the PM they are
     * using.
     */
    public String toString() {
        return "PM " + toMsgString();
    }

    public abstract int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex);

    public abstract Collection versantDetachCopy(Collection pcs, String fetchGroup);

    public abstract Collection versantAttachCopy(Collection detached,
            boolean makeTransactional);

    public abstract Collection versantAttachCopy(Collection detached,
            boolean makeTransactional, boolean shallow);

    public abstract void evictFromL2CacheAfterCommit(Object o);

    public abstract void evictAllFromL2CacheAfterCommit(Object[] a);

    public abstract void evictAllFromL2CacheAfterCommit(Collection c);

    public abstract void evictAllFromL2CacheAfterCommit(Class cls,
            boolean includeSubclasses);

    public abstract void evictAllFromL2CacheAfterCommit();

    public final PersistenceCapable cast2persistent(Object o) 
    {   // see VersantPersistenceManagerImp.checkPersCapable
        try {
            return (PersistenceCapable)o;
        } catch (ClassCastException e) {
            throw BindingSupportImpl.getInstance().invalidOperation("The supplied instance is not of type " +
                    PersistenceCapable.class.getName() +
                    " (" + o.getClass().getName() + ")");
        }
    }


}
