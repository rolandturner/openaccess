
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

import com.versant.core.common.*;
import com.versant.core.jdo.sco.VersantSCOCollection;
import com.versant.core.metadata.*;
import com.versant.core.jdo.query.mem.MemQueryCompiler;

import javax.jdo.*;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.lang.reflect.Modifier;
import java.lang.ref.Reference;
import java.sql.Connection;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.storagemanager.ApplicationContext;

/**
 * This is a global StateManager instance for a PersistenceManagerImp. It will
 * assign a state managing object to a managed PersistenceCapable instance.
 */
public final class VersantPersistenceManagerImp
        implements VersantPersistenceManager, ApplicationContext, Transaction,
        PersistenceContext
        , XAResource, Synchronization  {

    /**
     * Double linked list of dirty instances.
     *
     * @see PCStateMan#next
     * @see PCStateMan#prev
     */
    private PCStateMan txDirtyListHead;
    private PCStateMan txDirtyListTail;

    public VersantPersistenceManagerImp prev;
    public VersantPersistenceManagerImp next;
    public boolean idle;

    /**
     * The list of instances that has been marked for delete in the current tx.
     */
    private final DeletePacket toBeDeleted;
    /**
     * This is used to transport the dirty stuff to the server.
     */
    public final StatesToStore storeOidStateContainer;
    /**
     * while busy with a retrieve graph this will be set to true
     */
    private boolean retrieveing;
    /**
     * This is the instances that has been retrieved already.
     */
    private final Set retrieveSet = new HashSet();

    /**
     * The user object set by a client.
     */
    private Object userObject;
    /**
     * Map of user objects attached to this PM.
     */
    private Map userMap;

    /**
     * A Transaction may be associated with an Synchronization instance. If so this will be set to
     * the supplied intstance
     *
     * @see #setSynchronization
     */

    private Synchronization synchronizationInstance;


    /**
     * This is a cheat to quickly get the sm for a pc instance.
     * When getPM is called on the stateManager it sets itself to the
     */
    public PCStateMan requestedPCState;
    public final ModelMetaData modelMetaData;
    private LocalPMCache cache;
    public final JDOImplHelper jdoImplHelper = JDOImplHelper.getInstance();

    /**
     * Indication if tx is active.
     */
    private boolean transactionActive;
    private boolean busyWithRollback;
    private boolean rollbackOnly;
    private int txnCounter = 0;             //number of txn; incremented at commit()/rollback(); package visibility for performance
    private boolean doRefreshPNTObjects;

    /**
     * The counter that is used in creating NewObjectOID.
     */
    private int counter = 0;

    private boolean retainValues;
    private boolean restoreValues;
    private boolean optimistic;
    private boolean nontransactionalRead;
    private boolean nontransactionalWrite;
    private boolean ignoreCache;
    private boolean multithreaded;

    /**
     * If PersistenceManager is closed.
     */
    private boolean closed;

    /**
     * The factory that created this pm.
     */
    private final VersantPMFInternal pmf;

    private final MemQueryCompiler memQueryCompiler;

    /**
     * This will force the instance to be thrown away by the pool.
     */
    private boolean mustNotPool;
    private boolean inPool;

    /**
     * This is the proxy that is used for all client access.
     * This is used to decouple us from any instances left lying around.
     * This proxy ref must be reset to a new instance every time that it
     * is taken from the pool to be given out to a client.
     */
    private PMProxy proxy;

    /**
     * This is a bitset of all dirty classes.
     */
    private final CmdBitSet dirtyCmdBits;
    private final StorageManager sm;

    /**
     * This variable is used to control the strict adherence to the spec. when
     * transaction commits. If false then instances read in the tx but which is
     * p-nontx will be hollowed.
     */
    private boolean strict;
    private boolean interceptDfgFieldAccess = true;

    private boolean checkModelConsistencyOnCommit;

    public static final String LANGUAGE_SQL = "SQL";
    public static final String LANGUAGE_SQL2 = "JAVAX.JDO.QUERY.SQL";
    public static final String LANGUAGE_EJBQL = "EJBQL";


    // These fields keep track of OIDs/instances of objectid-class'es and
    // classes to evict from l2 cache if the transaction commits (epc = Evict
    // Post Commit).
    private Object[] epcObjects;
    private int epcObjectCount;
    private int[] epcClasses;
    private int epcClassCount;
    private boolean[] epcClassPresent;
    private boolean epcAll;

    private LifecycleListenerManager[] listeners;

    private Reference activeReference;

    public VersantPersistenceManagerImp(VersantPMFInternal pmf,
            ModelMetaData modelMetaData, StorageManager sm,
            LocalPMCache jdoManagedCache,
            MemQueryCompiler memQueryCompiler) {
        this.pmf = pmf;
        this.modelMetaData = modelMetaData;
        this.sm = sm;
        this.storeOidStateContainer = new StatesToStore(modelMetaData);
        this.toBeDeleted = new DeletePacket(modelMetaData);
        dirtyCmdBits = new CmdBitSet(modelMetaData);
        this.cache = jdoManagedCache;
        this.cache.setPm(this);
        this.memQueryCompiler = memQueryCompiler;
    }

    private void createProxy() {
        if (multithreaded) {
            proxy = new SynchronizedPMProxy(this);
        } else {
            proxy = new UnsynchronizedPMProxy(this);
        }
    }

    public boolean isInterceptDfgFieldAccess() {
        return interceptDfgFieldAccess;
    }

    public void setInterceptDfgFieldAccess(boolean on) {
        if (interceptDfgFieldAccess == on) return;
        cache.setInterceptDfgFieldAccess(
                interceptDfgFieldAccess = on);
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public PMProxy getProxy() {
        return proxy;
    }

    public boolean isInPool() {
        return inPool;
    }

    public void setMustNotPool(boolean mustNotPool) {
        this.mustNotPool = mustNotPool;
    }

    public boolean isMustNotPool() {
        return mustNotPool;
    }

    public LifecycleListenerManager[] getListeners() {
        return listeners;
    }

    /**
     * This is called by the pool instance the moment comes in or out of the
     * pool.
     */
    public void setInPool(boolean inPool) {
        this.inPool = inPool;
        if (inPool) {
            proxy = null;
        } else {
            createProxy();
            if (managed) managedClosed = false;
        }
    }

    /**
     * Reset the proxy so that if a client has a weak/soft ref to pm he will
     * get a closed exception. Then return us to our PMF.
     */
    protected void finalize() throws Throwable {
        boolean txWasActive = transactionActive;
        if (transactionActive) {
            rollback();
        }
        if (proxy != null)
            proxy.resetPM();
        pmf.pmClosedNotification(this, true, txWasActive);
    }

    /**
     * Sets the master on the detail in a Master/Detail relationship. If
     * removeFromCurrentMaster is true then the detail is removed from its
     * current master (if any and if different).
     */
    public void setMasterOnDetail(PersistenceCapable detail, int managedFieldNo,
            PersistenceCapable master, boolean removeFromCurrentMaster) {
        getInternalSM(detail).setMaster(managedFieldNo, master,
                removeFromCurrentMaster);
    }

    /**
     * Get a collection field from a pc. This is used to implement many-to-many
     * relationships.
     */
    public VersantSCOCollection getCollectionField(PersistenceCapable pc,
            int fieldNo) {
        PCStateMan sm = getInternalSM(pc);
        return (VersantSCOCollection)sm.getObjectField(null, fieldNo, null);
    }

    public Object getObjectField(PersistenceCapable pc,
            int fieldNo) {
        PCStateMan sm = getInternalSM(pc);
        return sm.getObjectField(null, fieldNo, null);
    }

    public String getConnectionURL(String dataStore) {
        try {
            return pmf.getConnectionURL();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public void cancelQueryExecution() {
        // todo implement jdoConnection.cancelQueryExecution();
    }

    public String getConnectionDriverName(String dataStore) {
        try {
            return pmf.getConnectionDriverName();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Connection getJdbcConnection(String datastore) {
        if (!transactionActive) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "A JDBC Connection may only be obtained within a active JDO transaction.");
        }
        try {
            return (Connection)sm.getDatastoreConnection();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     *
     */
    public void flushIfDepOn(int[] cmdBits) {
        if (cmdBits != null && dirtyCmdBits.containsAny(cmdBits)) flushRetainState();
    }

    public void loadFetchGroup(Object pc, String name) {
        try {
            if (!(pc instanceof PersistenceCapable)) {
                String msg;
                if (pc == null) {
                    msg = "Instance is null";
                } else {
                    msg = "Instance is not a persistence class: " +
                            pc.getClass().getName();
                }
                throw BindingSupportImpl.getInstance().runtime(msg);
            }
            PCStateMan sm = getInternalSM((PersistenceCapable)pc);
            if (sm == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("Instance is not managed by JDO (it is transient): " +
                        Utils.toString(pc));
            }
            sm.loadFetchGroup(name);
        } catch (RuntimeException e) {
            handleException(e);
        }
    }

    public boolean isRetainValues() {
        return retainValues;
    }

    public boolean isRestoreValues() {
        return restoreValues;
    }

    public boolean isOptimistic() {
        return optimistic;
    }

    public boolean isNontransactionalRead() {
        return nontransactionalRead;
    }

    public boolean isNontransactionalWrite() {
        return nontransactionalWrite;
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    public void evict(Object pc) {
        try {
            if (pc == null) return;
            PersistenceCapable persistenceCapable = checkPersCapable(pc);
            PersistenceManager pm = persistenceCapable.jdoGetPersistenceManager();
            // This instance is not managed. So return.
            if (pm == null) return;
            checkPM(pm, proxy);
            getInternalSM(persistenceCapable).evict();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void evictAll() {
        try {
            cache.evict();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void evictAll(Object[] pcs) {
        for (int i = pcs.length - 1; i >= 0; i--) {
            evict(pcs[i]);
        }
    }

    public void evictAll(Collection pcs) {
        try {
            Object[] persistenceCapables = new Object[pcs.size()];
            pcs.toArray(persistenceCapables);
            for (int i = persistenceCapables.length - 1; i >= 0; i--) {
                evict(persistenceCapables[i]);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Object getObjectId(Object pc) {
        try {
            if (pc == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "The supplied Object param is null");
            }

            if (!(pc instanceof PersistenceCapable))
            // pmPreCheck throws exception in this case
            {
                return null;
            }

            PCStateMan pcStateObject = pmPreCheck(pc);
            if (pcStateObject != null) {
                return pcStateObject.getObjectId(null);
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            handleException(e);
            return null;
        }
    }

    public Object getExternalOID(OID oid) {
        PCStateMan sm = cache.getByOID(oid, false);
        if (sm != null) {
            return sm.getObjectId(null);
        } else {
            ClassMetaData classMetaData = oid.getAvailableClassMetaData();
            if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
                return new VersantOid(oid, modelMetaData, oid.isResolved());
            } else if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                return oid.createObjectIdClassInstance();
            } else {
                throw BindingSupportImpl.getInstance().unsupported();
            }
        }
    }

    public Object getObjectByIDString(String value, boolean toValidate) {
        return getObjectByIDString(value, toValidate, true);
    }

    public Object getObjectByIDString(String value, boolean toValidate,
            boolean resolved) {
        try {
            OID oid = modelMetaData.newOIDFromIDString(value, resolved);
            return getObjectById(oid, toValidate);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Object getTransactionalObjectId(Object pc) {
        try {
            return getObjectId(pc);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }


//    public void batchDelete(Query q, Object[] params) {
//        VesantQueryImp clientQuery = (VesantQueryImp) q;
//        clientQuery.getQParamsForBatchProc(params);
//        throw new NotImplementedException();
//    }

    public boolean isClosed() {
        checkInPool();
        if (!managed) {
            return closed;
        } else {
            return managedClosed;
        }
    }

    /**
     * This is called by the user to close the pm. If in a managed environment the managedClosed
     * flag will be set.
     * <p/>
     * If pooling is enabled the the instance must be reset to
     * be put back in the pool.
     * <p/>
     * If pooling is not enabled then the instance must be reset to avoid mem leaks.
     * The instance will never be used again.
     * <p/>
     * The current transaction will be rolled back if active.
     * This means that synchronization events will be fired.
     */
    public synchronized void close() {
        checkClosed();
        if (!managed) {
            checkCloseWithActiveTx();
            boolean txWasActive = transactionActive;
            if (transactionActive) {
                rollback();
            }
            proxy.resetPM();
            pmf.pmClosedNotification(this, false, txWasActive);
        } else {
            managedClosed = true;
        }
    }

    private void checkCloseWithActiveTx() {
        if (transactionActive && !pmf.isAllowPmCloseWithTxOpen()) {
            throw BindingSupportImpl.getInstance().invalidOperation(

                    "May not close 'PersistenceManager' with active transaction");


        }
    }

    /**
     * Destroy the PM. It cannot be reused after this call.
     */
    public void destroy() {
        try {
            sm.reset();
            resetEpcFields();
            reset();
            cache.clear();
            this.closed = true;
        } catch (Exception e) {
            handleException(e);
        }
    }

    public boolean isActualClosed() {
        return closed;
    }

    public Transaction currentTransaction() {
        return this;
    }

    public void refresh(Object pc) {
        try {
            /**
             * if pc is transient / non-managed then return.
             */
            if (isTransient(pc)) return;
            pmPreCheck(pc).refresh();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setPmCacheRefType(Object pc, int type) {
        pmPreCheck(pc).cacheEntry.changeToRefType(cache.queue, type);
    }

    public void setPmCacheRefType(Object[] pcs, int type) {
        for (int i = 0; i < pcs.length; i++) {
            if (pcs[i] != null) setPmCacheRefType(pcs[i], type);
        }
    }

    public void setPmCacheRefType(Collection col, int type) {
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o != null) setPmCacheRefType(o, type);
        }
    }

    public void setPmCacheRefType(int type) {
        cache.setCurrentRefType(type);
    }

    public int getPmCacheRefType() {
        return cache.getCurrentRefType();
    }

    public void refreshAll(Object[] pcs) {
        try {
            for (int i = 0; i < pcs.length; i++) {
                refresh(pcs[i]);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void refreshAll(Collection pcs) {
        if (pcs instanceof QueryResult) {
            cache.setOverWriteMode(true);
            try {
                Iterator iter = ((QueryResult)pcs).createInternalIterNoFlush();
                while (iter.hasNext()) iter.next();
            } catch (Exception e) {
                handleException(e);
            } finally {
                cache.setOverWriteMode(false);
            }
        } else {
            try {
                for (Iterator iterator = pcs.iterator(); iterator.hasNext();) {
                    refresh(iterator.next());
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    public void refreshAll() {
        try {
            if (transactionActive) {
                cache.doRefresh(strict);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Query newQuery() {
        try {
            return new VersantQueryImp(proxy);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Object compiled) {
//        if (compiled instanceof JavaQuery) {
//            return newQuery((JavaQuery) compiled);
//        }
        try {
            VersantQueryImp other;
            try {
                other = (VersantQueryImp)compiled;
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "The supplied instance is not supported to re-create a query from.");
            }
            return new VersantQueryImp(proxy, other);
        } catch (RuntimeException e) {
            handleException(e);
            return null;
        }
    }

//    public Query newQuery(JavaQuery javaQuery) {
//            try {
//                JavaQueryParams query = null;
//                try {
//                    query = (JavaQueryParams) javaQuery;
//                } catch (ClassCastException e) {
//                    throw BindingSupportImpl.getInstance().invalidOperation(
//                            "JavaQuery has not been enhanced.");
//                }
//                VesantQueryImp clientQuery = new VesantQueryImp(proxy);
//                clientQuery.setClass(query.getQueryClass());
//                clientQuery.setFilter(query.getFilter());
//                clientQuery.setOrdering(query.getOrdering());
//                clientQuery.declareParameters(query.getParameters());
//                clientQuery.declareVariables(query.getVariables());
//                return clientQuery;
//            } catch (Exception e) {
//                handleException(e);
//                return null;
//            }
//    }
//
//    public Query newNamedQuery(Class cls, String queryName) {
//        return null;
//    }

    public Query newQuery(String language, Object query) {
        try {
            if (language != null) {
                language = language.toUpperCase();
            }
            if (LANGUAGE_SQL.equals(language) ||
                    LANGUAGE_SQL2.equals(language)) {
                VersantQueryImp clientQuery = new VersantQueryImp(proxy,
                        QueryDetails.LANGUAGE_SQL);
                clientQuery.setFilter((String)query);
                return clientQuery;

            } else if (LANGUAGE_EJBQL.equals(language)) {
                VersantQueryImp clientQuery = new VersantQueryImp(proxy,
                        QueryDetails.LANGUAGE_EJBQL);
                clientQuery.setFilter((String)query);
                return clientQuery;
            } else {
                return newQuery(query);
            }
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Class cls) {
        try {
            VersantQueryImp clientQuery = new VersantQueryImp(proxy);
            clientQuery.setClass(cls);
            return clientQuery;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Extent extent) {
        try {
            VersantQueryImp clientQuery = new VersantQueryImp(proxy);
            clientQuery.setCandidates(extent);
            return clientQuery;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Extent extent, String filter) {
        try {
            VersantQueryImp clientQuery = new VersantQueryImp(proxy);
            clientQuery.setCandidates(extent);
            clientQuery.setFilter(filter);
            return clientQuery;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Class cls, Collection cln) {
        try {
            VersantQueryImp clientQuery = new VersantQueryImp(proxy);
            clientQuery.setClass(cls);
            clientQuery.setCandidates(cln);
            return clientQuery;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Class cls, String filter) {
        try {
            VersantQueryImp clientQuery = new VersantQueryImp(proxy);
            clientQuery.setClass(cls);
            clientQuery.setFilter(filter);
            return clientQuery;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Query newQuery(Class cls, Collection cln, String filter) {
        try {
            VersantQueryImp clientQuery = new VersantQueryImp(proxy);
            clientQuery.setClass(cls);
            clientQuery.setCandidates(cln);
            clientQuery.setFilter(filter);
            return clientQuery;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Class getObjectIdClass(Class cls) {
        try {
            if (cls == null) {
                return null;
//            throw new JDOUserException("The supplied Class param is null");
            }
            Class pcClass = /*CHFC*/PersistenceCapable.class/*RIGHTPAR*/;
            if (!pcClass.isAssignableFrom(cls)) {
                return null;
            }
            if (Modifier.isAbstract(cls.getModifiers())) {
                return null;
            }
            ClassMetaData cmd = modelMetaData.getClassMetaData(cls);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "The class is not specified as " + PersistenceCapable.class.getName() + " for the application");
            }
            if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                return cmd.objectIdClass;
            } else {
                return /*CHFC*/VersantOid.class/*RIGHTPAR*/;
            }
        } catch (RuntimeException e) {
            handleException(e);
            return null;
        }
    }

    public Object newObjectIdInstance(Class cls, String s) {
        return newObjectIdInstance(cls, s, true);
    }

    public Object newObjectIdInstance(Class cls, String s, boolean resolved) {
        try {
            if (s == null || s.length() == 0) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Please supply an non-null, non-empty String");
            }
            if (cls == null) {  // assume datastore identity
                return new VersantOid(
                        modelMetaData.newOIDFromIDString(s, resolved),
                        modelMetaData, resolved);
            }
            ClassMetaData cmd = modelMetaData.getClassMetaData(cls);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("There is no metadata registered for class '" +
                        cls.getName() + "'");
            }
            if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                if (cmd.top.isSingleIdentity) {
                    return AppIdUtils.createSingleFieldIdentity(cmd.top.objectIdClass, cls, s);
                } else {
                    return jdoImplHelper.newObjectIdInstance(cls, s);
                }
            } else if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
                return new VersantOid(
                        modelMetaData.newOIDFromIDString(s, resolved),
                        modelMetaData, resolved);
            } else {
                throw BindingSupportImpl.getInstance().invalidOperation("Class '" + cls.getName() +
                        " uses non-durable identity");
            }
        } catch (RuntimeException e) {
            handleException(e);
            return null;
        }
    }

    public void retrieve(Object o) {
        try {
            PCStateMan pcStateObject = pmPreCheck(o);
            if (pcStateObject != null) {
                try {
                    if (Debug.DEBUG) {
                        if (retrieveing) {
                            throw BindingSupportImpl.getInstance().internal(
                                    "Retrieveing is already set");
                        }
                    }
                    retrieveing = true;
                    if (Debug.DEBUG) {
                        if (retrieveSet.contains(pcStateObject.pc)) {
                            throw BindingSupportImpl.getInstance().internal(
                                    "RetrieveSet already contains pc");
                        }
                    }
                    retrieveSet.add(pcStateObject.pc);
                    pcStateObject.retrieve(this);
                } finally {
                    retrieveing = false;
                    retrieveSet.clear();
                }
            }
        } catch (RuntimeException e) {
            if (BindingSupportImpl.getInstance().isOwnInternalException(e)) {
                handleException(e);
            } else {
                throw e;
            }
        }
    }

    public void retrieveImp(Object o) {
        if (o == null) return;
        PCStateMan pcStateObject = pmPreCheck(o);
        if (pcStateObject != null &&
                !retrieveSet.contains(pcStateObject.pc)) {
            retrieveSet.add(pcStateObject.pc);
            pcStateObject.retrieve(this);
        }
    }

    public void retrieveAllImp(Object[] toRetrieve) {
        for (int i = 0; i < toRetrieve.length; i++) {
            retrieveImp(toRetrieve[i]);
        }
    }

    public void retrieveAllImp(Collection toRetrieve) {
        retrieveAllImp(toRetrieve.toArray());
    }

    public void retrieveAll(Collection collection) {
        try {
            retrieveAll(collection.toArray());
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void retrieveAll(Collection collection, boolean b) {
        retrieveAll(collection);
    }

    public void retrieveAll(Object[] objects, boolean b) {
        retrieveAll(objects);
    }

    public void retrieveAll(Object[] objects) {
        try {
            try {
                if (Debug.DEBUG) {
                    if (retrieveing) {
                        throw BindingSupportImpl.getInstance().internal(
                                "Retrieveing is already set");
                    }
                }
                retrieveing = true;
                for (int i = 0; i < objects.length; i++) {
                    retrieveImp(objects[i]);
                }
            } finally {
                retrieveing = false;
                retrieveSet.clear();
            }
        } catch (RuntimeException e) {
            if (BindingSupportImpl.getInstance().isOwnInternalException(e)) {
                handleException(e);
            } else {
                throw e;
            }
        }
    }

    /**
     * todo keep extent's around instead of creating a new one.
     *
     * @param persistenceCapableClass
     * @param subclasses
     */
    public Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        try {
            return new ExtentImp(persistenceCapableClass, subclasses, proxy);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Object getObjectById(Object oid, boolean validate) {
        if (oid == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The supplied oid is null");
        }

        PersistenceCapable pc;
        try {
            OID nOID = extractOID(oid);
            PCStateMan stateMan = cache.getByOID(nOID, true);
            if (stateMan == null) {
                if (!nOID.isNew()) {
                    /**
                     * A call to getObjectById(id, false) with a where
                     * (idType == appId) and (oid.cmd.usekeygen == true)
                     * will always be validated against the db. This is done
                     * because an inconsistent mapping
                     * may arise.
                     *
                     * Scenario: A new instance of this type is created and made
                     * persistent. This instance is now in the
                     * managed cache with a internal tmp oid.
                     * A call to getObjectById(id, false) is made with a id
                     * value of 2 for example. This instance will also
                     * end up in the managed cache with a real app id with value 2.
                     * On a call to commit the keygen will allocate a value to
                     * the new id instance. If this value is also
                     * assigned the value '2' then there will both instances
                     * wants the same id but the are already assigned
                     * different pc instance values.
                     *
                     * Also added check for an unresolved oid that is in a hierarchy.
                     * Such oids are also always resolved.
                     * This could be changed later to keep such an instance in an
                     * initialised state.
                     */
                    if (validate
                            || (nOID.getAvailableClassMetaData().useKeyGen && nOID.getAvailableClassMetaData(
                            ).identityType == MDStatics.IDENTITY_TYPE_APPLICATION)
                            || (!nOID.isResolved() && nOID.getAvailableClassMetaData(
                            ).isInHierarchy())) {
                        checkNonTxRead();
                        OID nOIDcopy = nOID.copy();
                        stateMan = getStateMan(nOIDcopy, 0, -1, -1, validate);

                        if (stateMan == null) {
                            throw BindingSupportImpl.getInstance().objectNotFound(
                                    "No row for " +
                                    nOID.getAvailableClassMetaData().storeClass + " " + nOID.toSString());
                        }

                        pc = stateMan.pc;
                        stateMan.loadDFGIntoPC(this);
                    } else {
                        //create the sm
                        PCStateMan sm = getStateObject();
                        sm.init(nOID, this);
                        pc = sm.pc;
                        cache.add(sm);
                    }
                } else {
                    throw BindingSupportImpl.getInstance().objectNotFound(
                            "No row for " + nOID.toSString());
                }
            } else {
                pc = stateMan.pc;

                if (validate && !stateMan.isTransactional(null)) {
                    checkNonTxRead();
                    if (stateMan.isHollow()) {
                        stateMan.loadDfgFromHollow();
                    } else {
                        getState(stateMan.oid, null,
                                stateMan.getClassMetaData().getFetchGroup(
                                                FetchGroup.HOLLOW_NAME).index,
                                null, -1, true);
                    }
                }

            }
            return pc;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public OID extractOID(Object oid) {
        OID nOID;
        if (oid instanceof VersantOid) {
            nOID = modelMetaData.convertJDOGenieOIDtoOID((VersantOid)oid);
            nOID = convertNewToActual(nOID);
        } else if (oid instanceof OID) {
            nOID = convertNewToActual((OID)oid);
        } else {
            nOID = modelMetaData.convertFromAppIdToOID(oid);
        }
        return nOID;
    }

    /**
     * This is use by State implementations when they need to retrieve an
     * instance. This will typically be a result of graph navigation.
     * The oid parameter is not used when an embedded reference field is
     * being retrieved.
     */
    public Object getObjectByIdForState(OID oid, int stateFieldNo,
            int navClassIndex, OID fromOID) {
        try {
            PersistenceCapable pc;
            FieldMetaData fmd = fromOID.getAvailableClassMetaData().stateFields[stateFieldNo];
            if (fmd.embedded) {
                //create a managed instance of the embedded reference
                PCStateMan owningSM = cache.getByOID(fromOID, false);
                if (fmd.nullIndicatorFmd != null) {
                    if (owningSM.state.isFieldNullorZero(fmd.nullIndicatorFmd.stateFieldNo)) {
                        return null;
                    }
                }
                EmbeddedStateManager embeddedSm = owningSM.createEmbeddedSM(fmd);
                pc = embeddedSm.pc;
            } else {
                PCStateMan stateMan = cache.getByOID(oid, true);
                if (stateMan == null) {
                    stateMan = getStateMan(oid, 0, stateFieldNo, navClassIndex,
                            false);
                    if (stateMan != null) {
                        pc = stateMan.pc;
                        stateMan.loadDFGIntoPC(this);
                    } else {
                        pc = null;
                    }
                } else {
                    pc = stateMan.pc;
                    if (!stateMan.isTransactional(null)) {
                        checkNonTxRead();
                        //TODO why loadDfg. Is it not better to leave the state hollow
                        if (stateMan.isHollow()) stateMan.loadDfgFromHollow();
                    }
                }
            }
            return pc;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public final void checkNonTxRead() {
        if (!(transactionActive || nontransactionalRead)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Must set nonTransactionalRead to true");
        }
    }

    public final void checkNonTxWrite() {
        if (!(transactionActive || nontransactionalWrite)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Active transaction required for write operation");
        }
    }

    public boolean doBeforeState(boolean isTransient, boolean isTransactional) {
        return ((restoreValues || isTransient || isTransactional) && transactionActive);
    }

    private OID convertNewToActual(OID oid) {
        if (!oid.isNew()) {
            oid.getAvailableClassMetaData();
            return oid;
        }
        return oid.getAvailableOID();
    }

    public void makePersistent(final Object o) {
        try {
            if (o == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "makePersistent called with null object");
            }
            checkActiveTx();
            PCStateMan root = txDirtyListHead;
            makeReachablePersistent(o);
            if (root == null) {
                root = txDirtyListTail;
            } else {
                root = root.next;
            }

            for (; root != null; root = root.next) root.addRefs();

            if (hasCreateListeners()) {
                firePostCreate(o);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Make an instance found in a reachability search persistent. This skips
     * some checks done by makePersistent for speed. Note that this does not
     * add reachable instances.<p>
     */
    public void makeReachablePersistent(final Object o) {
        if (o == null) return;
        PersistenceCapable pc = checkPersCapable(o);
        PersistenceManager pm = pc.jdoGetPersistenceManager();
        if (pm == null) {
            reManage(pc, assignOID(pc), false);
        } else {
            PMProxy pmProxy = (PMProxy)pm;
            if (pmProxy.getRealPM() != this) {
                throw BindingSupportImpl.getInstance().invalidOperation("Object is managed by " + pm + " (this is " + proxy + "): " +
                        pc.getClass() + ": " + Utils.toString(pc));
                }
                }
    }

    public void makePersistentAll(final Object[] pcs) {
        try {
            ArrayList failed = null;
            for (int i = 0; i < pcs.length; i++) {
                Object pc = pcs[i];
                try {
                    makePersistent(pc);
                } catch (RuntimeException e) {
                    if (BindingSupportImpl.getInstance().isOwnException(e)) {
                        if (BindingSupportImpl.getInstance().getFailedObject(e) == null) {
                            e = BindingSupportImpl.getInstance().exception(
                                    e.getMessage(), e, pc);
                        }
                        if (failed == null) failed = new ArrayList();
                        failed.add(e);
                    } else {
                        throw e;
                    }
                }
            }
            if (failed != null) {
                int n = failed.size();
                if (n == 1) {
                    throw (Exception)failed.get(0);
                } else {
                    Throwable[] a = new Throwable[n];
                    failed.toArray(a);
                    throw BindingSupportImpl.getInstance().exception(
                            n + " instances failed to persist",
                            a);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makePersistentAll(final Collection pcs) {
        try {
            ArrayList failed = null;
            for (Iterator i = pcs.iterator(); i.hasNext();) {
                Object pc = i.next();
                try {
                    makePersistent(pc);
                } catch (RuntimeException e) {
                    if (BindingSupportImpl.getInstance().isOwnException(e)) {
                        if (BindingSupportImpl.getInstance().getFailedObject(e) == null) {
                            e = BindingSupportImpl.getInstance().exception(
                                    e.getMessage(), e, pc);
                        }
                        if (failed == null) failed = new ArrayList();
                        failed.add(e);
                    } else {
                        throw e;
                    }
                }
            }
            if (failed != null) {
                int n = failed.size();
                if (n == 1) {
                    throw (Exception)failed.get(0);
                } else {
                    Throwable[] a = new Throwable[n];
                    failed.toArray(a);
                    throw BindingSupportImpl.getInstance().exception(
                            n + " instances failed to persist",
                            a);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @param pcs
     */
    public void deletePersistentAll(Object[] pcs) {
        try {
            for (int i = 0; i < pcs.length; i++) {
                deletePersistent(pcs[i]);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @param pc
     */
    public void deletePersistent(Object pc) {
        checkActiveTx();
        if (isTransient(pc)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The instance is transient");
        }
        try {
            pmPreCheck(pc).deletePersistent();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @param pcs
     */
    public void deletePersistentAll(Collection pcs) {
        try {
            for (Iterator iterator = pcs.iterator(); iterator.hasNext();) {
                deletePersistent(iterator.next());
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * TODO: Remove the instance from the weak caches.
     *
     * @param pc
     */
    public void makeTransient(Object pc) {
        checkPersCapable(pc);
        try {
            if (isTransient(pc)) {
                //the instance is already transient.
                return;
            }
            pmPreCheck(pc).makeTransient();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makeTransientRecursive(Object pc) {
        checkPersCapable(pc);
        try {
            if (isTransient(pc)) {
                //the instance is already transient.
                return;
            }
            pmPreCheck(pc).makeTransientRecursive();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * TODO: Remove the instance from the weak caches.
     *
     * @param pcs
     */
    public void makeTransientAll(Object[] pcs) {
        try {
            Map failed = new HashMap();
            for (int i = 0; i < pcs.length; i++) {
                Object pc = pcs[i];
                try {
                    makeTransient(pc);
                } catch (Exception e) {
                    failed.put(pc, e);
                }
            }
            if (failed.size() > 0) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Errors occured with makeTransientAll: " + failed);
            }
        } catch (RuntimeException e) {
            handleException(e);
        }
    }

    /**
     * TODO: Remove the instance from the weak caches.
     *
     * @param pcs
     */
    public void makeTransientAll(Collection pcs) {
        try {
            if (pcs == null) return;
            makeTransientAll(pcs.toArray());
        } catch (Exception e) {
            handleException(e);
        }
    }

    private boolean isTransient(Object pc) {
        if (JDOHelper.getPersistenceManager(pc) == null) {
            return true;
        }
        return false;
    }

    public void makeTransactional(Object pc) {
        try {
            if (JDOHelper.getPersistenceManager(pc) == null) {
                //this is a transient instance
                reManage((PersistenceCapable)pc,
                        assignOID((PersistenceCapable)pc), true);
            }
            PCStateMan pcStateObject = pmPreCheck(pc);
            pcStateObject.makeTransactional();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makeTransactionalAll(Object[] pcs) {
        if (pcs == null) return;
        try {
            Map failed = new HashMap(pcs.length);
            for (int i = 0; i < pcs.length; i++) {
                Object pc = pcs[i];
                try {
                    makeTransactional(pc);
                } catch (Exception e) {
                    failed.put(pc, e);
                }
            }
            if (failed.size() > 0) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Errors occured with makePersistentAll:" + failed);
            }
        } catch (RuntimeException e) {
            handleException(e);
        }
    }

    public void makeTransactionalAll(Collection pcs) {
        if (pcs == null) return;
        try {
            makeTransactionalAll(pcs.toArray());
        } catch (Exception e) {
            handleException(e);
        }
    }

    private final void makeNonTransactionalImp(Object pc) {
        PCStateMan pcStateObject = pmPreCheck(pc);
        pcStateObject.makeNonTransactional();
    }

    public void makeNontransactional(Object pc) {
        try {
            makeNonTransactionalImp(pc);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makeNontransactionalAll(Object[] pcs) {
        try {
            for (int i = 0; i < pcs.length; i++) {
                makeNonTransactionalImp(pcs[i]);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makeNontransactionalAll(Collection pcs) {
        for (Iterator iterator = pcs.iterator(); iterator.hasNext();) {
            makeNonTransactionalImp(iterator.next());
        }
    }

    public void setUserObject(Object o) {
        try {
            sm.setUserObject(userObject = o);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public Object getUserObject() {
        return userObject;
    }

    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmf;
    }

    public void setMultithreaded(boolean flag) {
        // Changing from multithreaded false to true is not allowed. This must
        // be done at the PMF level before the PM is created.
        if (flag && !multithreaded) {
            throw BindingSupportImpl.getInstance().invalidOperation("PM.setMultithreaded(true) is not allowed if the PM " +
                    "was created from a PMF with multithreaded false");
        }
        // ignore the true -> false transition as the extra synchronization
        // has only a performance impact
    }

    public boolean getMultithreaded() {
        return multithreaded;
    }

    public void setMultithreadedImp(boolean flag) {
        multithreaded = flag;
        createProxy();
    }

    public void setIgnoreCache(boolean flag) {
        this.ignoreCache = flag;
    }

    public boolean getIgnoreCache() {
        return this.ignoreCache;
    }

    /**
     * Does a preCheck on a object claimed to be PersistanceCapable and managed by
     * this pm.
     */
    private final PCStateMan pmPreCheck(final Object pc) {
        return pmPreCheck(checkPersCapable(pc));

    }

    private PCStateMan pmPreCheck(PersistenceCapable pc) {
        if (!checkManagedBy(pc)) {
            return null;
        }
        PCStateMan pcState = getInternalSM(pc);
        if (Debug.DEBUG) {
            if (pcState == null && JDOHelper.getPersistenceManager(pc) != null) {
                throw BindingSupportImpl.getInstance().internal(
                        "The pm is set on instance but is not in weak list");
            }
        }
        return pcState;
    }

    private final boolean checkManagedBy(PersistenceCapable pc) {
        PMProxy pm = (PMProxy)pc.jdoGetPersistenceManager();
        if (pm != null) {
            if (pm == proxy) return true;
            throw BindingSupportImpl.getInstance().invalidOperation("Object is managed by " + pm + " (this is " + proxy + "): " +
                    pc.getClass() + ": " + Utils.toString(pc));
        }
        return false;
    }

    private final void checkActiveTx() {
        if (!isActive()) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "No active transaction.");
        }
    }

    public void begin() {
        if( doRefreshPNTObjects )
        {
            txnCounter++;
            //set all pnt objects to load-required
            cache.doMarkReloadNeeded();
        }
        if (managed) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "May not call begin in managed transaction environment");
        }
        beginImp();
    }

    private void beginImp() {
        if (transactionActive) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The transaction is already active");
        }
        try {
            transactionActive = true;
            rollbackOnly = false;
            sm.begin(optimistic);
            if (!optimistic && !interceptDfgFieldAccess) {
                cache.setInterceptDfgFieldAccess(true);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * This is for client to rollback the connection. This will try to
     * orderly rollback the data. The pm will be reset if an internal exception
     * happens during rollback. The pm must be reset as new to avoid inconsistent behaviour.
     * <p/>
     * NB
     * TODO ensure that all tx instances are rolledback properly.
     * If for instance the are already removed out of the tx list at commit
     * time then they can not undergo a rollback.
     */
    public void rollback() {
        if (Debug.DEBUG) {
            System.out.println(
                    ">>>>>>>>>>> JdoGeniePersistenceManagerImp.rollback <<<<<<<<<<<<<<<");
        }
        if (managed) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "May not call rollback in managed transaction environment");
        }
        rollbackImp();
    }

    /**
     * Reset this PM after an internal error of some kind. This just flags
     * it to not go back in the pool.
     */
    private void fatalReset() {
        mustNotPool = true;
    }

    /**
     * This is called by JDOConnectionImpProxy when it handles a
     * JDOFatalException from a call to the JDOConnection.
     */
    public void rollbackForFatalExceptionInJDOConnectionProxy() {
        if (managed || !transactionActive) return;
        rollbackImp();
    }

    /**
     * This is an orderly rollback.
     */
    private void rollbackImp() {
        if (busyWithRollback) return;
        checkActiveTx();
        resetEpcFields();
        try {
            busyWithRollback = true;

            if (synchronizationInstance != null) {
                synchronizationInstance.afterCompletion(
                        Status.STATUS_ROLLING_BACK);
            }

            try {
                cache.doRollback(retainValues);
                sm.rollback();
            } finally {
                reset();
            }

            if (synchronizationInstance != null) {
                synchronizationInstance.afterCompletion(
                        Status.STATUS_ROLLEDBACK);
            }

        } catch (Exception e) {
            fatalReset();
            if (BindingSupportImpl.getInstance().isOwnException(e)) {
                throw (RuntimeException)e;
            } else {
                throw BindingSupportImpl.getInstance().internal(e.getMessage(),
                        e);
            }
        } finally {
            transactionActive = false;
            busyWithRollback = false;
            rollbackOnly = false;
        }
    }

    public void setRestoreValues(boolean b) {
        checkPropChange();
        this.restoreValues = b;
    }

    public boolean getRestoreValues() {
        return restoreValues;
    }

    /**
     * Check the consistency of all instances in the local cache. Currently
     * this makes sure that all birectional relationships have been completed
     * properly (both sides in sync) but other checks may will be added in
     * future. This method is very slow and should only be used for debugging
     * during development.
     *
     * @see #setCheckModelConsistencyOnCommit(boolean)
     */
    public void checkModelConsistency() {
        cache.checkModelConsistency();
    }

    /**
     * Add the OID and State for deletion.
     */
    public void addForDelete(OID oid, State state) {
        if (Debug.DEBUG) {
            // make sure untyped OIDs are not added
            if (oid.getAvailableClassMetaData() == null) {
                BindingSupportImpl.getInstance().internal("oid is untyped: " +
                        oid);
            }
        }
        toBeDeleted.add(oid, state);
    }

    /**
     * This calls commit on all the transactional objects. It ensures
     * that the instances undergo the correct state changes.
     */
    public void commit() {
        if (managed) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "May not call commit in managed transaction environment");
        }
        if (!transactionActive) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Transaction is not active");
        }
        internalCommit(false);
    }

    private void dumpTxDirtyList(String msg) {
        System.out.println("--- txDirtyListHead: " + msg);
        for (PCStateMan o = txDirtyListTail; o != null; o = o.next) {
            System.out.println(o.pc.getClass().getName() + "@" +
                    Integer.toHexString(System.identityHashCode(o.pc)) +
                    ": " + o.pc);
        }
        System.out.println("---");
    }

    private void internalCommit(boolean phase) {
        if (rollbackOnly) {
            Exception x = null;
            try {
                rollbackImp();
            } catch (Exception e) {
                x = e;
            }
            String msg = "Transaction has been marked as \"rollback-only\". " +
                    "Commit cannot be performed.";
            if (x != null){
                throw BindingSupportImpl.getInstance().fatalDatastore(msg,x);
            } else {
                throw BindingSupportImpl.getInstance().fatalDatastore(msg);
            }
        }
        StatesReturned sc = null;

        if (synchronizationInstance != null) {
            synchronizationInstance.beforeCompletion();
            synchronizationInstance.afterCompletion(Status.STATUS_COMMITTING);
        }

        try {
            prepareForStore(true);

            sc = sm.store(storeOidStateContainer, toBeDeleted, retainValues,
                    phase
                        ? StorageManager.STORE_OPTION_PREPARE
                        : StorageManager.STORE_OPTION_COMMIT,
                    false);

            resetEpcFields();
            updateOIDsAndDoAutoS(sc);
            cache.doCommit(retainValues);

            invokePostStore();

            if (!phase) {

                if (synchronizationInstance != null) {
                    synchronizationInstance.afterCompletion(
                            Status.STATUS_COMMITTED);
                }

            }

            reset();
            transactionActive = false;
        } catch (Exception e) {
            handleException(e);
        } finally {
            if (sc != null) {
                sc.clear();
            }
        }
    }

    /**
     * Prepare to store all dirty instances for a commit or flush. This finds
     * all reachable instances and invokes preStore lifecycle listeners and
     * jdoPreStore instance callbacks.
     */
    private void prepareForStore(boolean commit) {
        boolean preStoreCalled = false;
        for (PCStateMan i = txDirtyListTail; i != null; i = i.next) {

            i.addRefs();
            preStoreCalled |= i.doJDOPreStore(listeners);
        }
        for (; preStoreCalled;) {
            PCStateMan root = txDirtyListHead;
            for (PCStateMan i = txDirtyListTail; i != null; i = i.next) {

                i.addRefs();
            }
            if (root == txDirtyListHead) break;
            preStoreCalled = false;
            for (PCStateMan i = root; i != null; i = i.next) {
                preStoreCalled |= i.doJDOPreStore(listeners);
            }
        }

        storeOidStateContainer.clear();
        toBeDeleted.clear();
        for (PCStateMan i = txDirtyListTail; i != null; i = i.next) {
            i.prepareCommitOrFlush(commit);
        }
        if (checkModelConsistencyOnCommit) {
            checkModelConsistency();
        }

        if (commit) { // fill in the epc stuff
            storeOidStateContainer.epcAll = epcAll;
            storeOidStateContainer.epcOids = epcObjectCount > 0
                    ? modelMetaData.convertToOID(epcObjects, epcObjectCount)
                    : null;
            storeOidStateContainer.epcClasses = epcClasses;
            storeOidStateContainer.epcClassCount = epcClassCount;
        }
    }

    private void invokePostStore() {
        if (hasStoreListeners()) {
            for (PCStateMan i = txDirtyListTail; i != null; i = i.next) {
                if (i.pc != null) {
                    int index = i.getIndex();
                    if (listeners[index] != null){
                        listeners[index].firePostStore(i.pc);
                    }
                }
            }
        }
    }

    /**
     * This flushes everything to the store. After this state interogation
     * will not be correct and the behaviour is not inline with the spec.
     * This is used for big batch procedures that needs to free the memory.
     */
    public void flush() {
        StatesReturned sc = null;
        try {
            prepareForStore(true);

            sc = sm.store(storeOidStateContainer, toBeDeleted,
                    false, StorageManager.STORE_OPTION_FLUSH, true);

            updateOIDsAndDoAutoS(sc);

            cache.doCommit(retainValues);

            invokePostStore();

            resetEpcFields();
            reset();
        } catch (Exception x) {
            handleException(x);
        } finally {
            if (sc != null) {
                sc.clear();
            }
        }
    }

    public void flush(boolean retainValues) {
        if (retainValues) {
            flushRetainState();
        } else {
            flush();
        }
    }

    public List versantAllDirtyInstances() {
        if (txDirtyListTail == null) return Collections.EMPTY_LIST;
        List l = new ArrayList();
        for (PCStateMan i = txDirtyListTail; i != null; i = i.next) {
            l.add(i.pc);
        }
        return l;
    }

    /**
     * This is used to flush the current changes to the store.
     * All state interogation will still work after this is called.
     * The data is just flushed to the store for queries to work.
     * <p/>
     * The implication of this is that the datastore connection will be pinned
     * to the jdoConnection for the life time of the transaction.
     * <p/>
     * This method will be a no-op if the transaction is not active.
     */
    public void flushRetainState() {
        if (!transactionActive) return;
        StatesReturned sc = null;

        storeOidStateContainer.clear();
        toBeDeleted.clear();
        try {
            prepareForStore(false);

            if (storeOidStateContainer.isEmpty() && toBeDeleted.size() == 0) {
                return;
            }

            sc = sm.store(storeOidStateContainer, toBeDeleted, true,
                    StorageManager.STORE_OPTION_FLUSH, false);

            updateOIDsAndDoAutoS(sc);
            for (PCStateMan i = txDirtyListTail; i != null; i = i.next) {
                i.flushCommit();
            }

            invokePostStore();

            storeOidStateContainer.clear();
            toBeDeleted.clear();
        } catch (Exception x) {
            handleException(x);
        } finally {
            try {
                if (sc != null) sc.clear();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    /**
     * This will do everything except actually commit to the store.
     */
    public void phaseCommit1() {
        internalCommit(true);
    }

    /**
     * Do the actual commit on the store.
     */
    public void phaseCommit2() {
        sm.commit();

        if (synchronizationInstance != null) {
            synchronizationInstance.afterCompletion(Status.STATUS_COMMITTED);
        }

    }

    public boolean isActive() {
        return transactionActive;
    }

    public final boolean isActiveDS() {
        return transactionActive && !optimistic;
    }

    public final boolean isActiveOptimistic() {
        return transactionActive && optimistic;
    }

    public void setNontransactionalRead(boolean nontransactionalRead) {
        checkPropChange();
        this.nontransactionalRead = nontransactionalRead;
    }

    public boolean getNontransactionalRead() {
        return nontransactionalRead;
    }

    public void setNontransactionalWrite(boolean nontransactionalWrite) {
        checkPropChange();
        this.nontransactionalWrite = nontransactionalWrite;
    }

    public boolean getNontransactionalWrite() {
        return nontransactionalWrite;
    }

    public void setRetainValues(boolean retainValues) {
//        checkPropChange();
        this.retainValues = retainValues;
    }

    /**
     * Check for changing props in active tx.
     */
    private final void checkPropChange() {
        if (transactionActive) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "May not be changed in active transaction");
        }
    }

    public boolean getRetainValues() {
        return retainValues;
    }

    public void setOptimistic(boolean optimistic) {
        checkPropChange();
        this.optimistic = optimistic;
    }

    public boolean getOptimistic() {
        return optimistic;
    }


    public void setSynchronization(Synchronization sync) {
        this.synchronizationInstance = sync;
    }

    public Synchronization getSynchronization() {
        return synchronizationInstance;
    }


    public PersistenceManager getPersistenceManager() {
        return VersantPersistenceManagerImp.this;
    }

//==================================Transaction imp end=======================


    /**
     * Create a new oid for a pc instance.
     */
    private NewObjectOID assignOID(PersistenceCapable pc) {
        ClassMetaData cmd = modelMetaData.getClassMetaData(/*CHFC*/pc.getClass()/*RIGHTPAR*/);
        if (cmd == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "There is no metadata registered for " + pc.getClass());
        }
        if (cmd.instancesNotAllowed) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Instances of " + cmd.qname + " may not be persistent or managed");
        }
        NewObjectOID oid = cmd.createNewObjectOID();
        oid.init(++counter);
        return oid;
    }

    /**
     * This is called by the JDOManagedCache when an instance is added
     * but it is not already managed.
     */
    public PCStateMan reManage(OID oid, State state) {
        if (oid.getAvailableClassMetaData() == null) {
            // replace untyped OID with a real one since we have a state
            OID tmp = state.getClassMetaData(modelMetaData).createOID(true);
            tmp.setLongPrimaryKey(oid.getLongPrimaryKey());
            oid = tmp;
        }
        PCStateMan stateObject = getStateObject();
        stateObject.init(oid, state.getClassMetaData(modelMetaData), state, this);
        return stateObject;
    }

    private final void reManage(PersistenceCapable pc, OID oid,
            boolean isTransactional) {
        PCStateMan sm = getStateObject();
        pc.jdoReplaceStateManager(createStateManagerProxy(sm));
        sm.init(pc, oid, isTransactional);
        sm.getRealOIDIfAppId();

        // check for app identity instance with same pk as instance already in
        // local pm cache
        if (!isTransactional
                && sm.getClassMetaData().identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            OID realOID = ((NewObjectOID)sm.oid).realOID;
            if (realOID != null && cache.contains(realOID)) {

                // throwing duplicateKey would be problematic for
                // java, because duplicateKey is fatal
                throw BindingSupportImpl.getInstance().
                        runtime("Instance of " + sm.getClassMetaData().qname +
                        " with identity '" + realOID + "' already exists in" +
                        " the local PM cache");



            }
        }

        //The refs to the wrapper is kept around to avoid gc of the instance
        cache.add(sm);
        if (Debug.DEBUG) {
            if (sm.cacheEntry == null) {
                throw BindingSupportImpl.getInstance().internal(
                        "cacheEntry must be initialized");
            }
        }
    }

    /**
     * Create a proxy for a PCStateMan or return it as is if no proxy is
     * needed. This is used to synchronize StateManager access when
     * multithreading is required.
     */
    public StateManager createStateManagerProxy(PCStateMan sm) {
        if (multithreaded) {
            return new SynchronizedStateManagerProxy(proxy, sm);
        } else {
            return sm;
        }
    }

    /**
     * Add an transactional dirty instance to the list.
     */
    public void addTxStateObject(PCStateMan stateObject) {
        if (Debug.DEBUG) {
            if (!stateObject.isTx()) {
                throw BindingSupportImpl.getInstance().internal(
                        "The instance is not Transactional");
            }
        }

        if (stateObject.isDirty()) {
            if (!stateObject.isInDirtyList(txDirtyListHead)) {
                addTxDirty(stateObject);
            }
        } else {
            if (stateObject.isInDirtyList(txDirtyListHead)) {
                removeTxDirty(stateObject);
            }
        }
    }

    /**
     * Add pc to the dirty list.
     */
    private void addTxDirty(PCStateMan pc) {
        if (txDirtyListHead == null) {
            txDirtyListHead = txDirtyListTail = pc;
            pc.next = null;
            pc.prev = null;
        } else {
            pc.prev = txDirtyListHead;
            txDirtyListHead.next = pc;
            pc.next = null;
            txDirtyListHead = pc;
        }
        dirtyCmdBits.add(pc.getClassMetaData());
        pc.inDirtyList = true;
    }

    /**
     * Remove pc from the dirty list.
     */
    private void removeTxDirty(PCStateMan pc) {
        if (Debug.DEBUG) {
            if (!pc.isInDirtyList(txDirtyListHead)) {
                throw BindingSupportImpl.getInstance().internal(
                        "not in dirty list: " + pc);
            }
        }
        if (txDirtyListTail == pc) {
            txDirtyListTail = pc.next;
        } else {
            pc.prev.next = pc.next;
        }
        if (txDirtyListHead == pc) {
            txDirtyListHead = pc.prev;
        } else {
            pc.next.prev = pc.prev;
        }
        dirtyCmdBits.remove(pc.getClassMetaData());
    }

    /**
     * Clear the dirty list.
     */
    public void clearTxDirtyList() {
        for (PCStateMan i = txDirtyListTail; i != null;) {
            PCStateMan next = i.next;
            i.prev = null;
            i.next = null;
            i = next;
            dirtyCmdBits.clear();
        }
        txDirtyListHead = txDirtyListTail = null;
    }

    public void removeTxStateObject(PCStateMan stateObject) {
        if (stateObject.isInDirtyList(txDirtyListHead)) {
            removeTxDirty(stateObject);
        }
    }

    private final void reset() {
        storeOidStateContainer.clear();
        clearTxDirtyList();
    }

    /**
     * This will reset the pm to be returned to the pool.
     */
    public void resetForPooling() {
        if (Debug.DEBUG) {
            if (transactionActive) {
                throw BindingSupportImpl.getInstance().fatal(
                        "The tx must be inactive");
            }
        }
        managed = false;
        cache.clear();
        sm.reset();
        sm.setUserObject(userObject = null);
        rollbackOnly = false;
        userMap = null;

        synchronizationInstance = null;

        resetEpcFields();
        reset();
    }

    /**
     * Check to see if the pm has not been closed by the user.
     */
    private void checkClosed() {
        checkInPool();
        if (closed) {
            throw BindingSupportImpl.getInstance().invalidOperation(

                    "The 'PersistenceManager' is already closed");


            }
		}

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        return this;
    }

    private PCStateMan getStateObject() {
        return new PCStateMan(cache, modelMetaData, proxy);
    }

//    public Collection mapFrom(Object data, Class cls) {
//        if (data instanceof ResultSet) {
//            return null;
//        } else {
//            throw BindingSupportImpl.getInstance().unsupported();
//        }
//    }
//
//    public Collection mapFrom(Object data, Class cls, String[] customMapping) {
//        if (data instanceof ResultSet) {
//            return null;
//        } else {
//            throw BindingSupportImpl.getInstance().unsupported();
//        }
//    }

//============================debug methods ============================================================================

    public boolean isPNonTx(Object pc) {
        return pmPreCheck(pc).isPNonTx();
    }

    public boolean isPClean(Object pc) {
        return pmPreCheck(pc).isPClean();
    }

    public boolean isPNew(Object pc) {
        return pmPreCheck(pc).isPNew();
    }

    public boolean isPNewDeleted(Object pc) {
        return pmPreCheck(pc).isPNewDeleted();
    }

    public boolean isPDeleted(Object pc) {
        return pmPreCheck(pc).isPDeleted();
    }

    public boolean isTClean(Object pc) {
        return pmPreCheck(pc).isTClean();
    }

    public boolean isTDirty(Object pc) {
        return pmPreCheck(pc).isTDirty();
    }

    public boolean isPDirty(Object pc) {
        return pmPreCheck(pc).isPDirty();
    }

    /**
     * Return the internal oid representation for this pc instance.
     * This oid is the actual oid and not a clone. If pc is null then null
     * is returned.
     */
    public OID getInternalOID(final PersistenceCapable pc) {
        if (pc == null) return null;
        return getInternalSM(pc).oid;
    }

    public VersantStateManager getVersantStateManager(PersistenceCapable pc) {
        return getInternalSM(pc);
    }

    /**
     * This is used internally to obtain the PCStateObject for a PersistanceCapable
     * instance. This instance must be managed by this pm.
     */
    public PCStateMan getInternalSM(final PersistenceCapable pc) {
        if (pc == null) return null;
        requestedPCState = null;
        pc.jdoGetPersistenceManager();
        if (requestedPCState == null) {
            throw BindingSupportImpl.getInstance().internal("Instance not managed: " +
                    pc.getClass().getName() + "@" +
                    Integer.toHexString(System.identityHashCode(pc)) + ": " +
                    pc.toString());
        }
        return requestedPCState;
    }

    public PCStateMan getSMIfManaged(PersistenceCapable pc) {
        if (pc == null) return null;
        requestedPCState = null;
        pc.jdoGetPersistenceManager();
        return requestedPCState;
    }

    /**
     * This is used internally to obtain the PCStateObject for a PersistanceCapable
     * instance. This instance must be managed by this pm.
     */
    public PCStateMan getInternalSM(OID oid) {
        if (oid == null) return null;
        return getInternalSM((PersistenceCapable)getObjectById(oid, false));
    }

    /**
     * This is used for debug. Not to be used else where.
     */
    public State getInternaleState(PersistenceCapable pc) {
        PCStateMan pcState = getInternalSM(pc);
        if (pcState == null) return null;
        return pcState.state;
    }

    public void dump(OID oid) {
        if (Debug.DEBUG) {
            PCStateMan pcStateObject = cache.getByOID(oid, true);
            if (pcStateObject != null) {
                pcStateObject.dump();
            } else {
                if (Debug.DEBUG) {
                    Debug.OUT.println("######## null for dump#######");
                }
            }
        }
    }

    /**
     * Are there any dirty instances?
     */
    public boolean isDirty() {
        return txDirtyListHead != null;
    }

    /**
     * This is used from getObjectByID where the object by id is not in the managed cache.
     * If there there is no jdo instance for the supplied instance then null is returned.
     *
     * @param ignoreLocalPmCache This is used to indicate that we should not check the
     *                           if the state is in the localCache on the serverside. The serverSide might
     *                           have a ref to the local cache.
     */
    private PCStateMan getStateMan(OID aOID, int fgIndex, int fieldNo,
            int navClassIndex, boolean ignoreLocalPmCache) {
        StatesReturned container = null;
        try {
            container = getStateJdoConnection(aOID, null, fgIndex,
                    aOID.getAvailableClassId(),
                    ignoreLocalPmCache, fieldNo, navClassIndex);

            if (Debug.DEBUG) {
                if (container.get(container.getDirectOID()) == NULLState.NULL_STATE) {
                    //this instance does not exist in db
                    if (container.size() != 1) {
                        throw BindingSupportImpl.getInstance().internal("Then directOid of the container is null " +
                                "so there should not be any other instances");
                    }
                }
            }

            return addAndReturnFirstDirect(container);
        } finally {
            if (container != null) container.clear();
        }
    }

    /**
     * This translates the old JDOConnection style call into a StorageManager
     * call. It should be nuked at some point.
     */
    private StatesReturned getStateJdoConnection(OID oid, State current,
            int fetchGroup, int classId, boolean ignoreLocalPmCache,
            int fieldNo, int navClassIndex) {
        // todo use ignoreLocalPmCache to modify context

        if (classId < 0) classId = oid.getAvailableClassId();
        ClassMetaData cmd = oid.getAvailableClassMetaData();
        FetchGroup fg = cmd == null ? null : cmd.getFetchGroup(fetchGroup, classId);

        FieldMetaData triggerField;
        if (navClassIndex >= 0) {   // navigated stateFieldNo
            ClassMetaData navCmd = modelMetaData.classes[navClassIndex];
            triggerField = navCmd.stateFields[fieldNo];
        } else if (fieldNo >= 0 && cmd != null) {  // missing absFieldNo
            triggerField = cmd.managedFields[fieldNo];
        } else {
            triggerField = null;
        }

        return sm.fetch(this, oid, current, fg, triggerField);
    }

    /**
     * This is called by sm's if they require a field. This will update the
     * local cache with the requested data (State instances). The returned
     * container reference must be kept until all the required State instances
     * in the local cache have been referenced or they might be GCed.
     * This is important for collections of PC and other fields that
     * involve fetching State instances and loading them into the local cache.
     * They are only hard referenced when the SCO instance has been created.
     */
    public StatesReturned getState(OID oid, State current, int fgi, FieldMetaData fmd,
        int navClassIndex, boolean ignoreLocalPmCache) {
        StatesReturned container = getStateJdoConnection(oid,
                current == null || current.isEmpty() ? null : current,
                fgi, oid.getAvailableClassId(),
                ignoreLocalPmCache, fmd == null ? -1: fmd.managedFieldNo, navClassIndex);

        if (Debug.DEBUG) {
            if (container.get(container.getDirectOID()) == NULLState.NULL_STATE) {
                //this instance does not exist in db
                if (container.size() != 1) {
                    throw BindingSupportImpl.getInstance().internal("Then directOid of the container is null " +
                            "so there should not be any other instances");
                }
            }
        }
        addToCache(container);
        return container;
    }

    /**
     * This is called by sm's when doing a refresh. It will ensure that the
     * data comes from the database when in a datastore transaction.
     */
    public void getStateForRefresh(OID oid, State current, int fgi) {
        StatesReturned container = null;
        try {
            container = getStateJdoConnection(oid,
                    current == null || current.isEmpty() ? null : current,
                    fgi, oid.getAvailableClassId(), true,
                    -1, -1);
            addToCache(container);
        } finally {
            if (container != null) container.clear();
        }
    }

    private void fillCacheWith(OIDArray oids, int fgi, int stateFieldNo,
            int navClassIndex) {
        StatesReturned container = null;
        try {
            FieldMetaData triggerField;
            if (navClassIndex >= 0) {   // navigated stateFieldNo
                ClassMetaData navCmd = modelMetaData.classes[navClassIndex];
                triggerField = navCmd.stateFields[stateFieldNo];
            } else {
                triggerField = null;
            }
            container = sm.fetch(this, oids, triggerField);
            addToCache(container);
        } finally {
            if (container != null) container.clear();
        }
    }

    /**
     * This is the pm is not the current PM.
     */
    private static final void checkPM(PersistenceManager otherPM,
            PersistenceManager currentPM) {
        if (otherPM != null && otherPM != currentPM) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The instance is not managed by this PersistenceManager");
        }
    }

    private static final PersistenceCapable checkPersCapable(Object o) {
        try {
            return (PersistenceCapable)o;
        } catch (ClassCastException e) {
            throw BindingSupportImpl.getInstance().invalidOperation("The supplied instance is not of type " +
                    PersistenceCapable.class.getName() +
                    " (" + o.getClass().getName() + ")");
        }
    }

    /**
     * This util method is used by collection types to preload their pc
     * entries. It tests to determine if the states refered to by the oids is
     * in the managed cache. If not they must be bulk loaded from server.
     * The scenario in which this is likely to happen is when the collection
     * is not in the default fetch group and the state is in cache with the
     * collection filled in. If this collection field is read then the
     * pcstateman will determine that the stateField is filled and hence not
     * ask the server for it.
     */
    public void checkToPreFetch(Object[] oids, int stateFieldNo,
            int navClassIndex) {
        if (oids != null && oids.length > 0 && oids[0] != null
                && cache.getByOID((OID)oids[0], true) == null) {
            OIDArray oidArray = new OIDArray();
            for (int i = 0; i < oids.length && oids[i] != null; i++) {
                oidArray.add((OID)oids[i]);
            }
            fillCacheWith(oidArray, 0, stateFieldNo, navClassIndex);
        }
    }

    /**
     * This does general exception handling. It ensures that a rollback
     * is done for Fatal exceptions.
     * CR:
     * If called with an instance of JDOUserException it's a no-op
     * and the passed instance is thrown.
     */
    private final void handleException(Exception x) {
        if (BindingSupportImpl.getInstance().isOwnFatalException(x) && isActive()) {
            try {
                rollbackImp();
            } catch (Exception e) {
                // discard as we are already busy processing an earlier
                // exception
            }
        }
        if (BindingSupportImpl.getInstance().isOwnInternalException(x)) {
            fatalReset();
            throw (RuntimeException)x;
        } else if (BindingSupportImpl.getInstance().isOwnException(x)) {
            throw (RuntimeException)x;
        } else {
            fatalReset();
            throw BindingSupportImpl.getInstance().internal(x.getMessage(), x);
        }
    }

    /**
     * Convert all PC, VersantOid and objectid-class params to OIDs. This
     * makes it possible to pass an OID instead of a PC instance for PC
     * parameters.
     */
    public void convertPcParamsToOID(Object[] params) {
        if (params == null) return;
        int n = params.length;
        for (int i = 0; i < n; i++) {
            Object param = params[i];
            if (param == null) continue;
            if (param instanceof Collection) {
                List l = new ArrayList((Collection)param);
                for (int j = 0; j < l.size(); j++) {
                    Object o = l.get(j);
                    o = convertPcParamsToOIDImp(o, i);
                    if (o instanceof OID) {
                        l.set(j, o);
                    }
                }
                params[i] = l;
            } else {
                params[i] = convertPcParamsToOIDImp(param, i);
            }
        }
    }

    private Object convertPcParamsToOIDImp(Object param, int paramIndex) {
        if (param == null) return param;
        if (param instanceof PersistenceCapable) {
            PersistenceCapable pc = (PersistenceCapable)param;
            if (pc.jdoGetPersistenceManager() != proxy) {
                if (pc.jdoGetPersistenceManager() != null) {
                    throw BindingSupportImpl.getInstance().invalidOperation("PC parameter " + paramIndex + " is managed by " + pc.jdoGetPersistenceManager() +
                            " (this is " + proxy + "): " +
                            pc.getClass() + ": " + Utils.toString(pc));
                } else {
                    throw BindingSupportImpl.getInstance().invalidOperation("PC parameter " + paramIndex + " is transient: " +
                            pc.getClass() + ": " + Utils.toString(pc));
                }
            }
            param = getInternalOID((PersistenceCapable)param);
        } else if (param instanceof VersantOid) {
            // datastore identity OID parameter
            OID oid = modelMetaData.convertJDOGenieOIDtoOID((VersantOid)param);
            param = convertNewToActual(oid);
        } else {
            ClassMetaData cmd =
                    modelMetaData.getClassMetaDataForObjectIdClass(
                            /*CHFC*/param.getClass()/*RIGHTPAR*/);
            if (cmd != null) { // app identity objectid-class parameter
                OID oid = cmd.createOID(false);
                oid.fillFromPK(param);
                param = oid;
            }
        }
        return param;
    }


//==========================XAResource impl==================================



    private int txTimeout = 3000;
    private Xid xid = null;


    private int txState;
    public static final int TX_INACTIVE = 0;
    public static final int TX_STARTED = 1;
    public static final int TX_FAIL = 2;
    public static final int TX_PREPARED = 4;
    public static final int TX_SUSPENDED = 8;

    /**
     * If the pm is managed by a TransactionManager.
     */
    private boolean managed;
    /**
     * This is a flag to indicate if it was closed by the client in a managed env.
     */
    private boolean managedClosed = false;

    /**
     * Is this pm managed by a container.
     *
     */
    public boolean isManaged() {
        return managed;
    }

    /**
     * set the state of the pm to be in a container.
     *
     * @param managed
     */
    public void setManaged(boolean managed) {
        this.managed = managed;
        this.managedClosed = false;
    }

    /**
     * Called to associate the resource with a transaction.
     * <p/>
     * If the flags argument is {@link #TMNOFLAGS}, the transaction must not
     * previously have been seen by this resource manager, or an
     * {@link javax.transaction.xa.XAException} with error code XAER_DUPID will be thrown.
     * <p/>
     * If the flags argument is {@link #TMJOIN}, the resource will join a
     * transaction previously seen by its resource manager.
     * <p/>
     * If the flags argument is {@link #TMRESUME} the resource will
     * resume the transaction association that was suspended with
     * end(TMSUSPEND).
     *
     * @param xid   The id of the transaction to associate with.
     * @param flags Must be either {@link #TMNOFLAGS}, {@link #TMJOIN}
     *              or {@link #TMRESUME}.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public void start(Xid xid, int flags) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println("***** JdoGeniePersistenceManagerImp.start ***** flag = "
                    + getFlagString(flags) + " for \n" + this);
            System.out.println("xid = " + xid);
        }
        checkInPool();
        switch (flags) {
            case TMNOFLAGS:
            case TMJOIN:
                begin(xid);
                break;
            case TMRESUME:
                if (checkId(xid)) {
                    resume(xid);
                }
                break;
            default:
                throw new XAException(
                        "Unsupported state for method start state = " + flags);
        }
    }



    private boolean checkId(Xid xid) {
        return this.xid.equals(xid);
    }



    private void resume(Xid xid) throws XAException {
        checkInPool();
        if (this.txState != TX_SUSPENDED) {
            throw new XAException(
                    "Could resume a transaction that was not suspended");
        }
        this.txState = TX_STARTED;
    }



    private void begin(Xid xid) throws XAException {
        checkInPool();
        if (this.txState == TX_INACTIVE) {
            this.xid = xid;
            try {
                beginImp();
                this.txState = TX_STARTED;
            } catch (Exception e) {
                throw new XAException(
                        "Could not begin a transaction : " + e.getMessage());
            }
        } else if (this.txState == TX_STARTED
                || this.txState == TX_PREPARED
                || this.txState == TX_SUSPENDED) {
            // Transaction on this pm has started already. Since beans will
            // share this pm, there will be multple calls to begin.
            return;
        } else {
            throw new XAException(
                    "Could not begin a transaction in state = " + txState);
        }
    }


    /**
     * Prepare to commit the work done on this resource in the given
     * transaction.
     * <p/>
     * This method cannot return a status indicating that the transaction
     * should be rolled back. If the resource wants the transaction to
     * be rolled back, it should throw an <code>XAException</code> at the
     * caller.
     *
     * @param xid The id of the transaction to prepare to commit work for.
     * @return Either {@link #XA_OK} or {@link #XA_RDONLY}.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public int prepare(Xid xid) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.prepare *****");
        }
        checkInPool();
        if (checkId(xid)) {
            if (txState == TX_STARTED) {
                try {
                    internalCommit(true);
                    this.txState = TX_PREPARED;
                    return XA_OK;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new XAException(
                            "Could not prepare commit : " + ex.getMessage());
                }
            } else if (this.txState == TX_PREPARED || this.txState == TX_SUSPENDED) {
                return XA_OK;
            } else {
                throw new XAException(
                        "Wrong state to commit phase one on : state = " + this.txState);
            }
        }
        return XA_OK;
    }


    /**
     * Commit the work done on this resource in the given transaction.
     * <p/>
     * If the <code>onePhase</code> argument is true, one-phase
     * optimization is being used, and the {@link #prepare(Xid) prepare}
     * method must not have been called for this transaction.
     * Otherwise, this is the second phase of the two-phase commit protocol.
     *
     * @param xid      The id of the transaction to commit work for.
     * @param onePhase If true, the transaction manager is using one-phase
     *                 optimization.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println("***** JdoGeniePersistenceManagerImp.commit ***** onePhase: " + onePhase
                    + " for \n" + this);
        }
        checkInPool();
        if (checkId(xid)) {
            try {
                if (onePhase && txState == TX_STARTED) {
                    internalCommit(false);
                } else if (this.txState == TX_PREPARED) {
                    phaseCommit2();
                } else if (this.txState == TX_INACTIVE) {
                    return;
                } else {
                    throw new XAException("Unable to commit unexpected state: state = " +
                            txState + " for xid = " + xid);
                }

                this.txState = TX_INACTIVE;
                this.xid = null;
            } catch (XAException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new XAException("Could not commit : " + ex.getMessage());
            }
        }
    }


    private void checkInPool() {
        if (inPool) {
            throw BindingSupportImpl.getInstance().fatal(
                    "The pm is in the pool");
        }
    }

    public boolean isInTx() {
        return this.txState != TX_INACTIVE;
    }

    /**
     * Roll back the work done on this resource in the given transaction.
     *
     * @param xid The id of the transaction to rollback for.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public void rollback(Xid xid) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.rollback *****");
        }
        checkInPool();
        if (checkId(xid)) {
            try {
                if (this.txState != TX_INACTIVE) {
                    rollbackImp();
                    this.txState = TX_INACTIVE;
                }
                this.xid = null;
            } catch (Exception e) {
                throw new XAException("Could not rollback: " + e.getMessage());
            }
        }
    }


    /**
     * Called to disassociate the resource from a transaction.
     * <p/>
     * If the flags argument is {@link #TMSUCCESS}, the portion of work
     * was done sucessfully.
     * <p/>
     * If the flags argument is {@link #TMFAIL}, the portion of work
     * failed. The resource manager may mark the transaction for
     * rollback only to avoid the transaction being committed.
     * <p/>
     * If the flags argument is {@link #TMSUSPEND} the resource will
     * temporarily suspend the transaction association. The transaction
     * must later be re-associated by giving the {@link #TMRESUME} state
     * to the {@link #start(Xid,int) start} method.
     *
     * @param xid   The id of the transaction to disassociate from.
     * @param flags Must be either {@link #TMSUCCESS}, {@link #TMFAIL}
     *              or {@link #TMSUSPEND}.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public void end(Xid xid, int flags) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println("***** JdoGeniePersistenceManagerImp.end ***** flag: " + getFlagString(
                    flags)
                    + " for \n" + this);
            System.out.println("xid = " + xid);
        }
        checkInPool();
        if (checkId(xid)) {
            switch (flags) {
                case TMSUCCESS:
                    this.txState = TX_STARTED;
                    break;
                case TMFAIL:
                    this.txState = TX_FAIL;
                    break;
                case TMSUSPEND:
                    this.txState = TX_SUSPENDED;
                    break;
                default:
                    throw new XAException(
                            "Unable to end transaction = " + xid + " unhandled flag = " + flags);
            }
        }
    }


    private String getFlagString(int flag) {
        switch (flag) {
            case 8388608:
                return "TMENDRSCAN";
            case 536870912:
                return "TMFAIL";
            case 2097152:
                return "TMJOIN";
            case 0:
                return "TMNOFLAGS";
            case 1073741824:
                return "TMONEPHASE";
            case 134217728:
                return "TMRESUME";
            case 16777216:
                return "TMSTARTRSCAN";
            case 67108864:
                return "TMSUCCESS";
            case 33554432:
                return "TMSUSPEND";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Tells the resource manager to forget about a heuristic decision.
     *
     * @param xid The id of the transaction that was ended with a heuristic
     *            decision.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public void forget(Xid xid) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.forget *****");
        }
        checkInPool();
        if (this.xid.equals(xid)) {
            this.txState = TX_STARTED;
        }
    }


    /**
     * Get the current transaction timeout value for this resource.
     *
     * @return The current timeout value, in seconds.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public int getTransactionTimeout() throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.getTransactionTimeout *****");
        }
        checkInPool();
        return txTimeout;
    }


    /**
     * This method does not check for inPool because Weblogic seems to keep
     * a ref around of used ones to try and re-use it. This happens even though
     * it was delisted from transaction.
     * <p/>
     * Tells the caller if this resource has the same resource manager
     * as the argument resource.
     * <p/>
     * The transaction manager needs this method to be able to decide
     * if the {@link #start(Xid,int) start} method should be given the
     * {@link #TMJOIN} state.
     *
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.isSameRM *****");
            Debug.OUT.println("***** isSame: this = " + this);
            Debug.OUT.println("***** isSame: other = " + this);
        }
        return xaResource == this;
    }


    /**
     * Return a list of transactions that are in a prepared or heuristically
     * state.
     * <p/>
     * This method looks not only at the resource it is invoked on, but
     * also on all other resources managed by the same resource manager.
     * It is intended to be used by the application server when recovering
     * after a server crash.
     * <p/>
     * A recovery scan is done with one or more calls to this method.
     * At the first call, {@link #TMSTARTRSCAN} must be in the
     * <code>state</code> argument to indicate that the scan should be started.
     * During the recovery scan, the resource manager maintains an internal
     * cursor that keeps track of the progress of the recovery scan.
     * To end the recovery scan, the {@link #TMENDRSCAN} must be passed
     * in the <code>state</code> argument.
     *
     * @param flag Must be either {@link #TMNOFLAGS}, {@link #TMSTARTRSCAN},
     *             {@link #TMENDRSCAN} or <code>TMSTARTRSCAN|TMENDRSCAN</code>.
     * @return An array of zero or more transaction ids.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public Xid[] recover(int flag) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println("***** JdoGeniePersistenceManagerImp.recover ***** flag = " + getFlagString(
                    flag));
        }
        checkInPool();
        return txState == TX_PREPARED ? new Xid[]{xid} : null;
    }


    /**
     * Set the transaction timeout value for this resource.
     * <p/>
     * If the <code>seconds</code> argument is <code>0</code>, the
     * timeout value is set to the default timeout value of the resource
     * manager.
     * <p/>
     * Not all resource managers support setting the timeout value.
     * If the resource manager does not support setting the timeout
     * value, it should return false.
     *
     * @param seconds The timeout value, in seconds.
     * @return True if the timeout value could be set, otherwise false.
     * @throws javax.transaction.xa.XAException
     *          If an error occurred.
     */

    public boolean setTransactionTimeout(int seconds) throws XAException {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.setTransactionTimeout *****");
        }
        checkInPool();
        if (seconds < -1) {
            return false;
        } else {
            this.txTimeout = seconds;
        }
        return true;
    }



//====================Synchronization imp==============================



    public void afterCompletion(int status) {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.afterCompletion *****");
        }
        proxy.resetPM();
        pmf.pmClosedNotification(this, false, false);
    }



    public void beforeCompletion() {
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "***** JdoGeniePersistenceManagerImp.beforeCompletion *****");
        }
    }


    /**
     * Set the locking mode for datastore transactions. This method may only
     * be called when no transaction is active. You can set the default value
     * for this property using the Workbench or edit your .jdogenie file
     * directly (datastore.tx.locking property).
     *
     * @see #LOCKING_NONE
     * @see #LOCKING_FIRST
     * @see #LOCKING_ALL
     */
    public void setDatastoreTxLocking(int mode) {
        int policy;
        switch (mode) {
            case VersantPersistenceManager.LOCKING_NONE:
                policy = StorageManager.LOCK_POLICY_NONE;
                break;
            case VersantPersistenceManager.LOCKING_FIRST:
                policy = StorageManager.LOCK_POLICY_FIRST;
                break;
            case VersantPersistenceManager.LOCKING_ALL:
                policy = StorageManager.LOCK_POLICY_ALL;
                break;
            default:
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Invalid datastoreTxLocking mode: " + mode);
        }
        sm.setLockingPolicy(policy);
    }

    /**
     * Get the locking mode for datastore transactions.
     */
    public int getDatastoreTxLocking() {
        switch (sm.getLockingPolicy()) {
            case StorageManager.LOCK_POLICY_NONE:
                return VersantPersistenceManager.LOCKING_NONE;
            case StorageManager.LOCK_POLICY_FIRST:
                return VersantPersistenceManager.LOCKING_FIRST;
            case StorageManager.LOCK_POLICY_ALL:
                return VersantPersistenceManager.LOCKING_ALL;
        }
        return VersantPersistenceManager.LOCKING_NONE;
    }

    public void logEvent(int level, String description, int ms) {
        sm.logEvent(level, description, ms);
    }

    /**
     * Return the instance for oid if it is present in the local PM cache
     * otherwise return null. Note that the instances might still be hollow
     * and touching its fields will cause a fetch from the level 2 cache
     * or database.
     *
     * @see #isHollow(Object)
     */
    public Object getObjectByIdFromCache(Object oid) {
        if (oid == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The oid is null");
        }
        try {
            PCStateMan stateMan = cache.getByOID(extractOID(oid),
                    true);
            return stateMan == null ? null : stateMan.pc;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * Is the instance hollow? Hollow instances are managed but their fields
     * have not been loaded from the level 2 cache or database.
     *
     * @see #getObjectByIdFromCache(Object)
     */
    public boolean isHollow(Object pc) {
        PCStateMan pcStateObject = pmPreCheck(pc);
        return pcStateObject != null && pcStateObject.isHollow();
    }

    /**
     * Does the instance have an identity? New instances are only assigned
     * an identity on commit or flush or when the application executes an
     * operation that requires the identity.
     */
    public boolean hasIdentity(Object pc) {
        return !pmPreCheck(pc).oid.isNew();
    }

    /**
     * Process the ReferenceQueue for the local cache. This is called when
     * a query fetches data from the server to ensure that long running
     * read-only transactions do not leak SoftReferences.
     */
    public void processLocalCacheReferenceQueue() {
        cache.processReferenceQueue();
    }

    /**
     * This method makes detached copies of the parameter instances and returns
     * the copies as the result of the method. The order of instances in the
     * parameter Collection's iteration corresponds to the order of corresponding
     * instances in the returned Collection's iteration.
     * <p/>
     * The Collection of instances is first made persistent, and the reachability
     * algorithm is run on the instances. This ensures that the closure of all
     * of the instances in the the parameter Collection is persistent.
     * <p/>
     * For each instance in the parameter Collection, a corresponding detached
     * copy is created. Each field in the persistent instance is handled based on
     * its type and whether the field is contained in the fetch group for the
     * persistence-capable class. If there are duplicates in the parameter
     * Collection, the corresponding detached copy is used for each such duplicate.
     */
    public Collection versantDetachCopy(Collection pcs, String fetchGroup) {
        if (pcs instanceof QueryResult) {
            //this is done to resolve the queryresult to avoid extra queries
            // when iterating over it
            pcs.size();
        }
        boolean hasDetachListeners = hasDetachListeners();
        boolean hasRealDetachListeners = false;

        if (fetchGroup == null) fetchGroup = FetchGroup.DFG_NAME;
        for (Iterator pcIt = pcs.iterator(); pcIt.hasNext();) {
            Object o = pcIt.next();
            if (!(o instanceof VersantDetachable))
                continue;
            VersantDetachable pc = (VersantDetachable)o;
            requestedPCState = null;
            pc.jdoGetPersistenceManager();
            if (requestedPCState == null) {
                makePersistent(pc);
            }
            if (hasDetachListeners) {
                int index = pmf.getClassIndex(/*CHFC*/pc.getClass()/*RIGHTPAR*/);
                if (listeners[index] != null) {
                    hasRealDetachListeners |= listeners[index].firePreDetach(pc);
                }
            }

        }
        flushRetainState();
        // load the container with the initial root collection
        DetachStateContainer dsc = new DetachStateContainer(this);
        for (Iterator pcIt = pcs.iterator(); pcIt.hasNext();) {
            PersistenceCapable pc = (PersistenceCapable)pcIt.next();
            if (pc == null) continue;
            PCStateMan sm = getInternalSM(pc);
            ClassMetaData cmd = sm.state.getClassMetaData(modelMetaData);
            FetchGroup fg = cmd.getFetchGroup(fetchGroup);
            if (fg == null) {
                fg = cmd.fetchGroups[0];
            }
            dsc.add(sm.oid, sm.state, fg);
        }

        detachCopy(dsc);
        ArrayList copyList = new ArrayList(pcs.size());
        for (Iterator pcIt = pcs.iterator(); pcIt.hasNext();) {
            Object o = pcIt.next();
            if (!(o instanceof VersantDetachable)) {
                copyList.add(null);
                continue;
            }
            VersantDetachable pc = (VersantDetachable)o;
            requestedPCState = null;
            pc.jdoGetPersistenceManager();
            if (requestedPCState == null) {
                copyList.add(pc);
            }
            Object detached = dsc.getDetachCopy(pc);
            copyList.add(detached);
            if (hasRealDetachListeners) {
                firePostDetach(detached, pc);
            }
        }
        return copyList;
    }

    private void detachCopy(DetachStateContainer dsc) {
        // find all states reachable via the fetch group
        for (; dsc.hasNextFetchGroup();) {
            State state = dsc.getNextFetchGroupState();
            FetchGroup fg = dsc.getNextFetchGroup();
            OID oid = dsc.getNextFetchGroupOID();

            // if we have to load the fetch group then there is no need to check
            // any of its fields - all of the States referenced by the fetch
            // group will have been added to the StatesReturned on the
            // server and will be added to our dcs
            if (!oid.isNew() && (state == null || !state.containsFetchGroup(fg))) {
                StatesReturned con = getStateForDetach(oid, fg.index);
                addToDetachStateContainer(con, dsc);
                state = con.get(oid);
                addToCache(con);
                dsc.add(oid, state, fg);
            }
            ClassMetaData cmd = state.getClassMetaData(modelMetaData);
            state.addFetchGroupStatesToDCS(fg, dsc, this, oid, cmd);
        }
        dsc.createPcClasses(modelMetaData);
    }

    public List versantCollectReachable(Collection roots, String fetchGroup) {
        if (fetchGroup == null) {
            fetchGroup = FetchGroup.REF_NAME;
        }
        checkActiveTx();

        Set result = new HashSet();
        List todo = new ArrayList(roots);

        while (todo.size() > 0) // non-recursive!
        {
            Object o = todo.remove(0);
            String currentFetchGroup = fetchGroup;
            if (o instanceof Object[]) { // added by sm.collectReachable
                Object[] tmp = (Object[])o;
                currentFetchGroup = (String)tmp[1];
                o = tmp[0];
            }

            if (!(o instanceof PersistenceCapable))
                continue;
            PersistenceCapable pc = (PersistenceCapable)o;

            requestedPCState = null;
            pc.jdoGetPersistenceManager();
            if (requestedPCState == null) {
                makePersistent(pc);
            }
            PCStateMan sm = pmPreCheck(pc);
            if (!result.contains(sm) && !sm.isDeleted(pc)) {
                if (currentFetchGroup != null)
                // nextFetchGroup was defined for the referencing field
                {
                    sm.collectReachable(currentFetchGroup, todo);
                }
                result.add(sm);
            }
        }
        List pcresult = new ArrayList();
        for (Iterator it = result.iterator(); it.hasNext();) {
            pcresult.add(((PCStateMan)it.next()).pc);
        }
        return pcresult;
    }

    /**
     * This is called by sm's if they require a field.
     * This will update the managedCache with the requested data.
     */
    public StatesReturned getStateForDetach(OID oid, int fgi) {
        return getStateJdoConnection(oid, null, fgi,
                oid.getAvailableClassId(), false,
                -1, -1);
    }

    public State getStateFromLocalCacheById(Object oid) {
        if (oid == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The supplied oid is null");
        }
        try {
            OID nOID = extractOID(oid);
            PCStateMan stateMan = cache.getByOID(nOID, true);
            if (stateMan == null) {
                return null;
            }
            return stateMan.state;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * This util method is used by collection types to preload their pc
     * entries. It tests to determine if the states refered to by the oids is
     * in the managed cache. If not they must be bulk loaded from server.
     * The scenario in which this is likely to happen is when the collection
     * is not in the default fetch group and the state is in cache with the
     * collection filled in. If this collection field is read then the
     * pcstateman will determine that the stateField is filled and hence not
     * ask the server for it.
     */
    public int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex) {
        if (oids == null || oids.length <= 0) return 0;
        OIDArray oidArray = new OIDArray();
        for (int i = 0; i < length; i++) {
            Object o = oids[i];
//            if (o == null) {
//                length = i;
//                break;
//            }
            if (!(o instanceof OID)) {
                data[i] = o;
                continue;
            }
            OID oid = (OID)o;
            PCStateMan pcStateMan = cache.getByOID(oid, true);
            if (pcStateMan != null) {
                data[i] = pcStateMan.pc;
            } else {
                oidArray.add(oid);
                data[i] = null;
            }
        }
        if (!oidArray.isEmpty()) {
            FieldMetaData triggerField;
            if (classMetaDataIndex >= 0) {   // navigated stateFieldNo
                ClassMetaData navCmd = modelMetaData.classes[classMetaDataIndex];
                triggerField = navCmd.stateFields[stateFieldNo];
            } else {
                triggerField = null;
            }
            StatesReturned container = sm.fetch(this, oidArray, triggerField);
            // keep a reference to each of the returned PCStateMan's so
            // that they are not GCed before we reference them
            PCStateMan[] nogc = addToCacheAndManage(container);
            for (int i = 0; i < length; i++) {
                if (data[i] == null) {
                    data[i] = cache.getByOID((OID)oids[i], true).pc;
                }
            }
            if (nogc == null) {
                // dummy code to keep IDE happy and to hopefully prevent
                // any overzealous optimization from removing nogc
            }
        }
        return length;
    }

    /**
     * Construct a new query instance with the given candidate class from a
     * named query. The query name given must be the name of a query defined
     * in metadata. The metadata is searched for the specified name.
     * This is a JDO 2 preview feature.
     */
    public Query versantNewNamedQuery(Class cls, String queryName) {
        try {
            ClassMetaData cmd = modelMetaData.getClassMetaData(cls);
            if (cmd == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("Class " + cls.getName() +
                        " is not persistent");
            }
            QueryDetails qp = cmd.getNamedQuery(queryName);
            if (qp == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("No query called '" + queryName +
                        "' has been defined in the meta data for Class " +
                        cmd.qname);
            }
            return new VersantQueryImp(proxy, qp);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * Must bidirectional relationships be checked for consistency
     * on commit or flush?
     */
    public boolean isCheckModelConsistencyOnCommit() {
        return checkModelConsistencyOnCommit;
    }

    public void setCheckModelConsistencyOnCommit(boolean on) {
        this.checkModelConsistencyOnCommit = on;
    }

    /**
     * This method applies the changes contained in the collection of detached
     * instances to the corresponding persistent instances in the cache and
     * returns a collection of persistent instances that exactly corresponds to
     * the parameter instances. The order of instances in the parameter
     * Collection's iteration corresponds to the order of corresponding
     * instances in the returned Collection's iteration.
     * <p/>
     * Changes made to instances while detached are applied to the corresponding
     * persistent instances in the cache. New instances associated with the
     * detached instances are added to the persistent instances in the
     * corresponding place.
     */
    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional) {
        return versantAttachCopy(detached, makeTransactional, false);
    }

    /**
     * This method applies the changes contained in the collection of detached
     * instances to the corresponding persistent instances in the cache and
     * returns a collection of persistent instances that exactly corresponds to
     * the parameter instances. The order of instances in the parameter
     * Collection's iteration corresponds to the order of corresponding
     * instances in the returned Collection's iteration.
     * <p/>
     * Changes made to instances while detached are applied to the corresponding
     * persistent instances in the cache. New instances associated with the
     * detached instances are added to the persistent instances in the
     * corresponding place.
     *
     * @param detached VersantDetachable objects to attach in the current
     *                 transaction
     * @param shallow  attach only the objects in 'detached' Collection and not
     *                 reachable objects if true.
     */
    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional, boolean shallow) {
        AttachStateContainer asc = new AttachStateContainer(this);
        boolean hasAttachListeners = hasAttachListeners();
        boolean hasRealAttachListeners = false;
        for (Iterator pcIt = detached.iterator(); pcIt.hasNext();) {
            VersantDetachable detachable = (VersantDetachable)pcIt.next();
            if (detachable == null) continue;
            if (hasAttachListeners) {
                hasRealAttachListeners |= firePreAttach(detachable);
            }
            asc.addVersantDetachable(detachable);
        }
        attachCopy(asc, shallow);
        ArrayList attached = new ArrayList();
        for (Iterator pcIt = detached.iterator(); pcIt.hasNext();) {
            VersantDetachable pc = (VersantDetachable)pcIt.next();
            if (pc == null) {
                attached.add(pc);
                continue;
            }
            Object newPC = getObjectById(getOID(pc), false);
            if (hasRealAttachListeners) {
                firePostAttach(newPC, pc);
            }
            attached.add(newPC);
        }
        return attached;
    }

    private void attachCopy(AttachStateContainer asc, boolean shallow) {
        if (!shallow) {
            AttachNavStateManager ansm = new AttachNavStateManager(asc);
            for (int c = 0; c < asc.getDetachedSize(); c++) {
                VersantDetachable detachable = asc.getVersantDetachable(c);
                if (detachable.jdoGetPersistenceManager() != null) continue;
                /**
                 * I don't think that this adds any value, as this does not stop
                 * someone from changing it while we are busy. We are thread safe
                 * as far as thread goes that operate on this pm. The issue is that
                 * if any other pm wants to attach this same instance then we
                 * will have an issue.
                 */
                VersantDetachedStateManager dsm = detachable.versantGetDetachedStateManager();
                detachable.jdoReplaceStateManager(ansm);
                detachable.jdoReplaceStateManager(ansm);
                ClassMetaData cmd = modelMetaData.getClassMetaData(
                        /*CHFC*/detachable.getClass()/*RIGHTPAR*/);
                for (; cmd != null; cmd = cmd.pcSuperMetaData) {
                    for (int i = 0; i < cmd.fields.length; i++) {
                        FieldMetaData field = cmd.fields[i];
                        if (field.fake) continue;
                        if (field.isEmbeddedRef()) continue;
                        switch (field.category) {
                            case FieldMetaData.CATEGORY_REF:
                            case FieldMetaData.CATEGORY_POLYREF:
                            case FieldMetaData.CATEGORY_COLLECTION:
                            case FieldMetaData.CATEGORY_MAP:
                            case FieldMetaData.CATEGORY_ARRAY:
                                if (dsm != null) {
                                    if (detachable.versantIsLoaded(i)
                                            || detachable.versantIsDirty(i)) {
                                        detachable.jdoProvideField(
                                            field.managedFieldNo);
                                    }
                                } else {
                                    detachable.jdoProvideField(
                                            field.managedFieldNo);
                                }
                        }
                    }
                }
                detachable.jdoReplaceStateManager(dsm);
                detachable.jdoReplaceStateManager(dsm);
            }
        }

        AttachCopyStateManager acsm = new AttachCopyStateManager(this);
        for (int c = 0; c < asc.getDetachedSize(); c++) {
            OID oid = asc.getOID(c);
            VersantDetachable detachable = asc.getVersantDetachable(c);
            if (detachable.jdoGetPersistenceManager() != null) continue;
            boolean notNew = !oid.isNew();
            //if the instance hold embedded instances then we must still traverse
            //the embedded graphshould check if the instance contains embedded
            //references, else continue here
            if (notNew && !detachable.versantIsDirty() && !isDirty(detachable)) continue;
            VersantDetachedStateManager dsm = detachable.versantGetDetachedStateManager();
            detachable.jdoReplaceStateManager(acsm);
            detachable.jdoReplaceStateManager(acsm);
            PCStateMan sm = getInternalSM(oid);
            if (notNew && sm.state.isHollow()) {
                StatesReturned con = getStateForDetach(oid, 0);
                addToCache(con);
            }
            acsm.setState(sm);
            //Here is an issue: we must know if the owning class or any of the embedded
            //instance is dirty to determine if this is
            if (notNew) {
                checkDetachedVersion(detachable, sm, oid);
            }
            ClassMetaData cmd = modelMetaData.getClassMetaData(
                    /*CHFC*/detachable.getClass()/*RIGHTPAR*/);
            for (; cmd != null; cmd = cmd.pcSuperMetaData) {
                for (int i = 0; i < cmd.fields.length; i++) {
                    final FieldMetaData fmd = cmd.fields[i];
                    if (fmd.fake) continue;
                    if (fmd.isEmbeddedRef()) {
                        //must do this irrespective it the field is dirty or not
                        if (notNew && detachable.versantIsDirty(fmd.managedFieldNo)) {
                            sm.makeDirty(sm.pc, fmd.managedFieldNo);
                        }
                        detachable.jdoProvideField(fmd.managedFieldNo);
                        continue;
                    } else if (notNew && !detachable.versantIsDirty(
                            fmd.getManagedFieldNo())) {
                        continue;
                    }
                    switch (fmd.category) {
                        case FieldMetaData.CATEGORY_SIMPLE:
                        case FieldMetaData.CATEGORY_POLYREF:
                        case FieldMetaData.CATEGORY_EXTERNALIZED:
                        case FieldMetaData.CATEGORY_REF:
                        case FieldMetaData.CATEGORY_COLLECTION:
                        case FieldMetaData.CATEGORY_ARRAY:
                        case FieldMetaData.CATEGORY_MAP:
                            detachable.jdoProvideField(fmd.managedFieldNo);
                            if (notNew) {
                                sm.makeDirty(detachable, fmd.managedFieldNo);
                            }
                    }
                }
            }

            if (notNew) {
                sm.resetLoadedFields();
                sm.setLoadRequired();
            }
            detachable.jdoReplaceStateManager(dsm);
        }
        Collection deleted = asc.getDeleted();
        for (Iterator it = deleted.iterator(); it.hasNext();) {
            try {
                Object o = getObjectById(it.next(), true);
                deletePersistent(o);
            } catch (JDOObjectNotFoundException e) {
                // Do Nothing
            }
        }
    }

    /**
     * Check if the detached or any of its embedded references is dirty.
     * @param detachable
     */
    private boolean isDirty(VersantDetachable detachable) {
        if (detachable.versantIsDirty()) return true;
        VersantEmbeddedRefDirtyFinderStateManager vdirtSm = new VersantEmbeddedRefDirtyFinderStateManager();
        return vdirtSm.checkDirty(detachable, modelMetaData.getClassMetaData(
                /*CHFC*/detachable.getClass()/*RIGHTPAR*/));
    }

    private void checkDetachedVersion(VersantDetachable detachable,
            PCStateMan sm, OID oid) {
        final Object dVersion = detachable.versantGetVersion();
        final Object aVersion = sm.getOptimisticLockingValue();
        if (aVersion == null && sm.getClassMetaData().optimisticLockingField != null) {
            throw BindingSupportImpl.getInstance().internal("Optimistic locking value not available for "
                    + sm.getClassMetaData().qname);
        }
        if (aVersion != null && !aVersion.equals(dVersion)) {
            throw BindingSupportImpl.getInstance().concurrentUpdate(
                    "The object (" + oid.toStringImp() +
                    ") has been updated since its been detached " +
                    "(current='" + aVersion + "', detached='" + dVersion + "')", oid);
        }
    }

    OID getOID(VersantDetachable detachable) {
        Object oid = detachable.versantGetOID();
        if (oid == null) {
            PersistenceManager pm = detachable.jdoGetPersistenceManager();
            if (pm != null) {
                if (pm instanceof PMProxy) {
                    pm = ((PMProxy)pm).getRealPM();
                }
                if (pm instanceof VersantPersistenceManagerImp) {
                    OID internalOID = ((VersantPersistenceManagerImp)pm).getInternalOID(
                            detachable);
                    detachable.versantSetOID(getExternalOID(internalOID));
                    return internalOID;
                } else {
                    throw BindingSupportImpl.getInstance().runtime(
                            "Can't attach a managed " +
                            "instance that is not managed by Versant Open Access");
                }
            }
            PCStateMan sm = getStateObject();
            NewObjectOID nOID = assignOID(detachable);
            sm.init(detachable, nOID, this);
            cache.add(sm);
            detachable.versantSetOID(nOID);
            return sm.oid.getAvailableOID();
        } else {
            return extractOID(oid);
        }
    }


    final PCStateMan getStateManager(Object o) {
        return cache.getByOID(
                extractOID(((PersistenceCapable)o).jdoGetObjectId()), false);
    }

    final PCStateMan getStateManagerById(Object oid) {
        return cache.getByOID(extractOID(oid), false);
    }

    public void setRetainConnectionInOptTx(boolean on) {
        sm.setConnectionPolicy(on
            ? StorageManager.CON_POLICY_PIN_FOR_TX
            : StorageManager.CON_POLICY_RELEASE);
    }

    /**
     * Clear all epc fields.
     */
    private void resetEpcFields() {
        epcAll = false;
        epcObjects = null;
        epcObjectCount = 0;
        epcClasses = null;
        epcClassCount = 0;
        epcClassPresent = null;
    }

    public void evictFromL2CacheAfterCommit(Object o) {
        checkActiveTx();
        evictFromL2CacheAfterCommitImp(o);
    }

    /**
     * Version of {@link #evictFromL2CacheAfterCommit} without the active tx
     * check for interna use.
     */
    public void evictFromL2CacheAfterCommitImp(Object o) {
        if (epcAll) return;
        if (o instanceof PersistenceCapable) {
            PCStateMan sm = pmPreCheck((PersistenceCapable)o);
            if (sm == null || sm.isNew(null)) {
                return; // no need to evict transient or new objects
            }
            o = sm.oid;
        }
        if (epcObjects == null) {
            epcObjects = new Object[4];
        } else if (epcObjectCount == epcObjects.length) {
            Object[] a = new Object[epcObjects.length * 3 / 2 + 1];
            System.arraycopy(epcObjects, 0, a, 0, epcObjects.length);
            epcObjects = a;
        }
        epcObjects[epcObjectCount++] = o;
    }

    public void evictAllFromL2CacheAfterCommit(final Object[] data) {
        checkActiveTx();
        int n = data.length;
        if (n == 0 || epcAll) return;
        ensureCapacityEpcObjects(n);
        for (int i = 0; i < n; i++) {
            Object o = data[i];
            if (o instanceof PersistenceCapable) {
                PCStateMan sm = pmPreCheck((PersistenceCapable)o);
                if (sm == null || sm.isNew(null)) {
                    continue; // no need to evict transient or new objects
                }
                o = sm.oid;
            }
            epcObjects[epcObjectCount++] = o;
        }
    }

    public void evictAllFromL2CacheAfterCommit(final Collection data) {
        checkActiveTx();
        int n = data.size();
        if (n == 0 || epcAll) return;
        ensureCapacityEpcObjects(n);
        for (Iterator i = data.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof PersistenceCapable) {
                PCStateMan sm = pmPreCheck((PersistenceCapable)o);
                if (sm == null || sm.isNew(null)) {
                    continue; // no need to evict transient or new objects
                }
                o = sm.oid;
            }
            epcObjects[epcObjectCount++] = o;
        }
    }

    private void ensureCapacityEpcObjects(int delta) {
        if (epcObjects == null) {
            epcObjects = new Object[delta + 4];
        } else if (epcObjectCount + delta >= epcObjects.length) {
            Object[] a = new Object[epcObjectCount + delta];
            System.arraycopy(epcObjects, 0, a, 0, epcObjects.length);
            epcObjects = a;
        }
    }

    public void evictAllFromL2CacheAfterCommit(final Class cls,
            final boolean includeSubclasses) {
        checkActiveTx();
        ClassMetaData cmd = modelMetaData.getClassMetaData(cls);
        if (cmd == null) {
            BindingSupportImpl.getInstance().runtime(
                    "Class is not persistent: " + cls);
        }
        evictAllFromL2CacheAfterCommitImp(cmd, includeSubclasses);
    }

    private void evictAllFromL2CacheAfterCommitImp(ClassMetaData cmd,
            final boolean includeSubclasses) {
        if (epcAll) return;
        if (epcClasses == null) {
            epcClasses = new int[4];
            epcClassPresent = new boolean[modelMetaData.classes.length];
        }
        int ci = cmd.index;
        if (!epcClassPresent[ci]) {
            if (epcClassCount == epcClasses.length) {
                int[] a = new int[epcClasses.length * 2];
                System.arraycopy(epcClasses, 0, a, 0, epcClasses.length);
                epcClasses = a;
            }
            epcClasses[epcClassCount++] = ci;
            epcClassPresent[ci] = true;
        }
        if (includeSubclasses) {
            ClassMetaData[] subs = cmd.pcSubclasses;
            if (subs != null) {
                for (int i = subs.length - 1; i >= 0; i--) {
                    evictAllFromL2CacheAfterCommitImp(subs[i],
                            includeSubclasses);
                }
            }
        }
    }

    public void evictAllFromL2CacheAfterCommit() {
        checkActiveTx();
        epcAll = true;
        epcObjects = null;
        epcObjectCount = 0;
        epcClasses = null;
        epcClassCount = 0;
        epcClassPresent = null;
    }

    public Object getOptimisticLockingValue(Object o) {
        return getInternalSM((PersistenceCapable)o).getOptimisticLockingValue();
    }

    public void setListeners(LifecycleListenerManager[] listeners) {
        this.listeners = listeners;
    }

    /**
     * Get our StorageManager.
     */
    public StorageManager getStorageManager() {
        return sm;
    }

    public LocalPMCache getCache() {
        return cache;
    }

    public void setCache(LocalPMCache cache) {
        this.cache = cache;
    }

    public MemQueryCompiler getMemQueryCompiler() {
        return memQueryCompiler;
    }

    public Reference getActiveReference() {
        return activeReference;
    }

    public void setActiveReference(Reference activeReference) {
        this.activeReference = activeReference;
    }

    /**
     * Iterate through all the oid-state pairs returned from the server
     * for a store operation and update the local states.
     * This will also bring back real oids for new oids involved.
     */
    private void updateOIDsAndDoAutoS(StatesReturned container) {
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            OID oid = (OID)e.getKey();
            State state = (State)e.getValue();

            // This gets the sm with the oid from the container.
            // This is done for the case of a newOID which is not
            // mapped in the cache via its real oid.
            // The sm may not be null because the only way for it to have been sent
            // to the server for commit was if it was dirty and hence we must have
            // a hard refs to it.
            PCStateMan sm;
            if (oid instanceof NewObjectOID) {
                sm = cache.getByNewObjectOID((NewObjectOID)oid);
                if (sm == null) {
                    sm = cache.getByOID(oid, false);
                    if (sm == null) continue;
                } else {
                    ((NewObjectOID)sm.oid).setRealOid(oid.getRealOID());
                }
            } else {
                sm = cache.getByOID(oid, false);
                if (sm == null) continue;
            }

            // if the state returned is not null then it contains auto set fields that
            // needs must be updated the sm's current state.
            // this must only happen if retainValues is set to true and the state
            // contains autoset fields. version number is a autoset field.
            if (state != null) {
                sm.updateAutoFields(state);
            }
        }
    }

    /**
     * Add all the OIDs and States in the container to the cache.
     */
    public void addToCache(StatesReturned container) {
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            cache.addStateOnly((OID)e.getKey(), (State)e.getValue());
        }
    }

    /**
     * Add all the OIDs and States in the container to the cache. The
     * PCStateMan's are kept and returned to prevent the new instances
     * being GCed before they are referenced.
     */
    private PCStateMan[] addToCacheAndManage(StatesReturned container) {
        PCStateMan[] smArray = new PCStateMan[1];
        PCStateMan[] ans = new PCStateMan[container.size()];
        int c = 0;
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            ans[c++] = cache.add((OID)e.getKey(), (State)e.getValue(), smArray);
        }
        return ans;
    }

    /**
     * Add all the OIDs and States in the container to the cache and return
     * the PCStateMan for the first direct State. This method is specifically written
     * to keep a hard ref to created sm so that it can not be gc'd before it is returned.
     */
    private PCStateMan addAndReturnFirstDirect(StatesReturned container) {
        OID firstDirectOID = container.getDirectOID();
        if (firstDirectOID == null) {
            return null;
        }
        PCStateMan[] sm = new PCStateMan[1];
        cache.add(firstDirectOID, container.get(firstDirectOID), sm);
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            cache.addStateOnly((OID)e.getKey(), (State)e.getValue());
        }
        return sm[0];
    }

    /**
     * Add all the OIDs and States in the container to dcs.
     */
    private void addToDetachStateContainer(StatesReturned container,
            DetachStateContainer dcs) {
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            dcs.add((OID)e.getKey(), (State)e.getValue());
        }
    }

    /**
     * The StorageManager calls this method to check if we need prefetched
     * data or not.
     */
    public boolean isStateRequired(OID oid, FetchGroup fetchGroup) {
        State state = cache.getStateByOID(oid);
        if (state != null) {
            if (fetchGroup == null) {
                fetchGroup = state.getClassMetaData().fetchGroups[0];
            }
            return !state.containsFetchGroup(fetchGroup);
        }
        return true;
    }
    // ------------------ JDO2 implementation --------------------


    public void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        if (listeners == null) {
            listeners = new LifecycleListenerManager[pmf.getJDOMetaData().classes.length + 1];
        }

        if (classes != null) {
            int[] indexs = pmf.getJDOMetaData().convertToClassIndexes(classes, true);
            for (int i = 0; i < indexs.length; i++) {
                int index = indexs[i];
                if (listeners[index] == null) {
                    listeners[index] = new LifecycleListenerManager(listener);
                } else {
                    listeners[index] = listeners[index].add(listener);
                }
                // setup the all listener
                index = pmf.getJDOMetaData().classes.length;
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

    public Object attachCopy(Object pc, boolean makeTransactional) {
        AttachStateContainer asc = new AttachStateContainer(this);
        VersantDetachable detachable = (VersantDetachable) pc;
        if (detachable == null) return null;
        boolean hasAttachListeners = false;
        int index = -1;
        if (hasAttachListeners()) {
            index = pmf.getClassIndex(/*CHFC*/pc.getClass()/*RIGHTPAR*/);
            if (listeners[index] != null) {
                hasAttachListeners = listeners[index].firePreAttach(detachable);
            }
        }
        asc.addVersantDetachable(detachable);
        attachCopy(asc, false); //todo jdo2 where do I find if this is a shallow copy
        Object newPC = getObjectById(getOID(detachable), false);
        if (hasAttachListeners) {
            listeners[index].firePostAttach(newPC, detachable);
        }
        return newPC;
    }

    public Collection attachCopyAll(Collection detached, boolean makeTransactional) {
        return versantAttachCopy(detached, makeTransactional, false);
    }

    public Object[] attachCopyAll(Object[] detached, boolean makeTransactional) {
        boolean hasAttachListeners = hasAttachListeners();
        boolean hasRealAttachListeners = false;
        AttachStateContainer asc = new AttachStateContainer(this);
        for (int i = 0; i < detached.length; i++) {
            VersantDetachable detachable = (VersantDetachable) detached[i];
            if (detachable == null) continue;
            if (hasAttachListeners) {
                hasRealAttachListeners |= firePreAttach(detachable);
            }
            asc.addVersantDetachable(detachable);
        }
        attachCopy(asc, false);
        Object[] attached = new Object[detached.length];
        for (int i = 0; i < detached.length; i++) {
            VersantDetachable pc = (VersantDetachable) detached[i];
            if (pc == null) {
                attached[i] = null;
                continue;
            }
            Object newPC = getObjectById(getOID(pc), false);
            attached[i] = newPC;
            if (hasRealAttachListeners) {
                firePostAttach(newPC, pc);
            }
        }
        return attached;
    }

    public void checkConsistency() {
        checkModelConsistency();
    }

    public Object detachCopy(Object o) {
        String fetchGroup = FetchGroup.DFG_NAME;
        if (!(o instanceof VersantDetachable)){
            return null;
        }
        VersantDetachable vd = (VersantDetachable) o;
        requestedPCState = null;
        vd.jdoGetPersistenceManager();
        if (requestedPCState == null) {
            makePersistent(vd);
        }

        boolean hasDetachListeners = false;
        int index = -1;
        if (hasDetachListeners()){
            index = pmf.getClassIndex(/*CHFC*/vd.getClass()/*RIGHTPAR*/);
            if (listeners[index] != null) {
                hasDetachListeners = listeners[index].firePreDetach(vd);
            }
        }

        flushRetainState();
        // load the container with the initial root collection
        DetachStateContainer dsc = new DetachStateContainer(this);
        PersistenceCapable pc = vd;
        PCStateMan sm = getInternalSM(pc);
        ClassMetaData cmd = sm.state.getClassMetaData(modelMetaData);
        FetchGroup fg = cmd.getFetchGroup(fetchGroup);
        if (fg == null) {
            fg = cmd.fetchGroups[0];
        }
        dsc.add(sm.oid, sm.state, fg);
        detachCopy(dsc);
        Object newPC = dsc.getDetachCopy(pc);
        if (hasDetachListeners) {
            listeners[index].firePostDetach(newPC, pc);
        }
        return newPC;
    }

    public Collection detachCopyAll(Collection pcs) {
        return versantDetachCopy(pcs, null);
    }

    public Object[] detachCopyAll(Object[] pcs) {
        String fetchGroup = FetchGroup.DFG_NAME;
        boolean hasDetachListeners = hasDetachListeners();
        boolean hasRealDetachListeners = false;
        for (int i = 0; i < pcs.length; i++) {
            Object o = pcs[i];
            if (!(o instanceof VersantDetachable))
                continue;
            VersantDetachable pc = (VersantDetachable) o;
            requestedPCState = null;
            pc.jdoGetPersistenceManager();
            if (requestedPCState == null) {
                makePersistent(pc);
            }
            if (hasDetachListeners) {
                int index = pmf.getClassIndex(/*CHFC*/pc.getClass()/*RIGHTPAR*/);
                if (listeners[index] != null) {
                    hasRealDetachListeners |= listeners[index].firePreDetach(pc);
                }
            }
        }
        flushRetainState();
        // load the container with the initial root collection
        DetachStateContainer dsc = new DetachStateContainer(this);
        for (int i = 0; i < pcs.length; i++) {
            PersistenceCapable pc = (PersistenceCapable)pcs[i];
            if (pc == null) continue;
            PCStateMan sm = getInternalSM(pc);
            ClassMetaData cmd = sm.state.getClassMetaData(modelMetaData);
            FetchGroup fg = cmd.getFetchGroup(fetchGroup);
            if (fg == null) {
                fg = cmd.fetchGroups[0];
            }
            dsc.add(sm.oid, sm.state, fg);

        }
        detachCopy(dsc);
        Object[] copys = new Object[pcs.length];
        for (int i = 0; i < pcs.length; i++) {
            Object o = pcs[i];
            if (!(o instanceof VersantDetachable)) {
                copys[i] = null;
                continue;
            }
            VersantDetachable pc = (VersantDetachable) o;
            Object detached = dsc.getDetachCopy(pc);
            copys[i] = detached;
            if (hasRealDetachListeners) {
                firePostDetach(detached, pc);
            }
        }
        return copys;
    }

    public JDOConnection getDataStoreConnection() {
        return (JDOConnection) getJdbcConnection(null);
    }

    /**
     * Equivalent to getExtent(persistenceCapableClass, true)
     */
    public Extent getExtent(Class persistenceCapableClass) {
        return getExtent(persistenceCapableClass, true);
    }

    public FetchPlan getFetchPlan() {
        // TODO jdo2 getFetchPlan
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 FetchPlan support not implemented");
    }

    public Object getObjectById(Class cls, Object key) {
        return getObjectById(newObjectIdInstance(cls, key), true);
    }

    public Object getObjectById(Object oid) {
        return getObjectById(oid, true);
    }

    public Collection getObjectsById(Collection oids) {
        return getObjectsById(oids, true);
    }

    public Collection getObjectsById(Collection oids, boolean validate) {
        if (oids == null || oids.size() == 0) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "getObjectsById() does not accept null id values, " +
                    "or empty collections");
        }

        Collection objects = new ArrayList(oids.size());
        Iterator iter = oids.iterator();
        while (iter.hasNext()) {
            Object oid = iter.next();
            // Just use getObjectById to get the object
            objects.add(getObjectById(oid, validate));
        }
        return objects;
    }

    public Object[] getObjectsById(Object[] oids) {
        return getObjectsById(oids, true);
    }

    public Object[] getObjectsById(Object[] oids, boolean validate) {
        if (oids == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "getObjectsById() does not accept null arrays");
        }
        Object[] objects = new Object[oids.length];
        for (int i = 0; i < oids.length; i++) {
            // Just use getObjectById to get the object
            objects[i] = getObjectById(oids[i], validate);
        }
        return objects;
    }

    public Sequence getSequence(String s) {
        // TODO jdo2 getSequence
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 getSequence not implemented");
    }

    public Object getUserObject(Object o) {
        return userObject;
    }

    public Object newInstance(Class aClass) {
        // TODO jdo2 newInstance
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 newInstance(Class) not implemented");
    }

    public Query newNamedQuery(Class cls, String queryName) {
        return versantNewNamedQuery(cls, queryName);
    }

    public Object newObjectIdInstance(Class cls, Object key) {
        if (key instanceof String){
           return newObjectIdInstance(cls, (String)key);
        } else {
            if (cls != null) {
                ClassMetaData cmd = modelMetaData.getClassMetaData(cls);
                if (cmd == null) {
                    throw BindingSupportImpl.getInstance().invalidOperation("There is no metadata registered for class '" +
                            cls.getName() + "'");
                }
                if (cmd.top.isSingleIdentity) {
                    return AppIdUtils.createSingleFieldIdentity(cmd.top.objectIdClass, cls, key);
                }
            }
            if (cls == null) {
                throw BindingSupportImpl.getInstance().exception(
                        "Class is null");
            } else {
                throw BindingSupportImpl.getInstance().exception(
                        "Class is not Single Identity and key is not a String ");
            }
        }
    }

    public Query newQuery(String query) {
        VersantQueryImp clientQuery = new VersantQueryImp(proxy,
                QueryDetails.LANGUAGE_JDOQL);
        clientQuery.setFilter(query);
        return newQuery("javax.jdo.query.JDOQL", clientQuery);
    }

    public Object putUserObject(Object key, Object value) {
        checkClosed();
        if (key == null) {
            return null;
        }
        if (userMap == null) {
            userMap = new HashMap();
        }
        if (value == null) {
            // Remove the object
            return userMap.remove(key);
        } else {
            // Put the object
            return userMap.put(key, value);
        }
    }

    public void refreshAll(JDOException e) {
        Object obj = e.getFailedObject();
        if (obj != null) {
            refresh(obj);
        }
        Throwable[] nestedExcs = e.getNestedExceptions();
        if (nestedExcs != null) {
            for (int i = 0; i < nestedExcs.length; i++) {
                if (nestedExcs[i] instanceof JDOException) {
                    refreshAll((JDOException) nestedExcs[i]);
                }
            }
        }
    }

    public Object removeUserObject(Object key) {
        checkClosed();
        if (key == null) {
            return null;
        }
        if (userMap == null) {
            return null;
        }
        return userMap.remove(key);
    }

    public boolean getRollbackOnly() {
        return rollbackOnly;
    }

    public void setRollbackOnly() {
        // Only apply to active transactions
        if (transactionActive) {
            rollbackOnly = true;
        }
    }

    public final boolean doRefreshPNTObjects(int counter) {
        //doRefreshPNTObjects == false => counter==txnCounter,
        //proof: see begin() method + initial values of counters
        return counter != txnCounter;
    }

    public final int getTxnCounter() {
        return txnCounter;
    }

    public final void setRefreshPNTObjects(boolean doRefresh) {
        this.doRefreshPNTObjects = doRefresh;
    }

    /**
     * Do we have any Store lifecycle listeners?
     */
    public boolean hasStoreListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasStoreListeners();
    }

    /**
     * Do we have any Attach lifecycle listeners?
     */
    public boolean hasAttachListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasAttachListeners();
    }

    /**
     * Fire a pre attach event
     */
    public boolean firePreAttach(Object o) {
        int index = pmf.getClassIndex(/*CHFC*/o.getClass()/*RIGHTPAR*/);
        if (listeners[index] != null) {
            return listeners[index].firePreAttach(o);
        }
        return false;
    }


    /**
     * Fire a post attach event
     */
    public void firePostAttach(Object src, Object target) {
        int index = pmf.getClassIndex(/*CHFC*/target.getClass()/*RIGHTPAR*/);
        if (listeners[index] != null ) {
            listeners[index].firePostAttach(src, target);
        }
    }

    /**
     * Do we have any Create lifecycle listeners?
     */
    public boolean hasCreateListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasCreateListeners();
    }

    /**
     * Fire a post create event
     */
    public void firePostCreate(Object src) {
        int index = pmf.getClassIndex(/*CHFC*/src.getClass()/*RIGHTPAR*/);
        if (listeners[index] != null) {
            listeners[index].firePostCreate(src);
        }
    }

    /**
     * Do we have any Detach lifecycle listeners?
     */
    public boolean hasDetachListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasDetachListeners();
    }

    /**
     * Fire a post detach event
     */
    public void firePostDetach(Object src, Object target) {
        int index = pmf.getClassIndex(/*CHFC*/target.getClass()/*RIGHTPAR*/);
        if (listeners[index] != null) {
            listeners[index].firePostDetach(src, target);
        }
    }

    /**
     * Do we have any Clear lifecycle listeners?
     */
    public boolean hasClearListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasClearListeners();
    }

    /**
     * Do we have any Dirty lifecycle listeners?
     */
    public boolean hasDirtyListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasDirtyListeners();
    }

    /**
     * Do we have any Load lifecycle listeners?
     */
    public boolean hasLoadListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasLoadListeners();
    }

    /**
     * Do we have any Delete lifecycle listeners?
     */
    public boolean hasDeleteListeners() {
        return listeners != null &&
                listeners[listeners.length - 1].hasDeleteListeners();
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

