
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
package com.versant.core.ejb;

import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.jdo.*;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.storagemanager.ApplicationContext;
import com.versant.core.common.*;
import com.versant.core.common.PersistenceContext;
import com.versant.core.ejb.common.EntrySet;
import com.versant.core.ejb.common.IdentityEntry;

import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;
import javax.persistence.*;
import javax.persistence.Query;
import java.util.*;
import java.lang.reflect.Field;

/**
 * Implementation of ejb3 EntityManager.
 */
public class EntityManagerImp implements EntityManager, EntityTransaction,
        ApplicationContext, PersistenceContext {
    final StorageManager storageMan;
    ModelMetaData modelMetaData;
    private EMProxy proxy;
    /**
     * The objects that must be commited at the end of the tx.
     */
//    private Set<StateManagerImp> txObjects = new HashSet<StateManagerImp>();
    private LinkedListEntrySet txObjects = new LinkedListEntrySet();
    private LocalCache cache = new LocalCache();
    /**
     * The list of instances that has been marked for delete in the current tx.
     */
    private final DeletePacket toBeDeleted;
    /**
     * This is used to transport the dirty stuff to the server.
     */
    public final StatesToStore storeOidStateContainer;
    private boolean txActive;
    /**
     * int val used for creating newoids
     */
    private int newOidCounter;
    private boolean closed;
    /**
     * Set if the currect context is extended. This means that we will not do
     * 'detachOnCommit' on a commit call, and
     * we will do a 'detach' on the 'close' method.
     */
    private boolean extendedPContext;
    /**
     * The entityListener is used for callbacks and listeners.
     */
    private EntityLifecycleManager entityListener;
    /**
     * This is an field that is used to determine if managed instance are out of
     * sync with the current transaction. This
     * is updated when ever a new transaction is started.
     */
    private int txVersion = 0;
    private boolean retainValues;
    private PostPersistCallback postPersistCallback;

    public EntityManagerImp(StorageManager sm, ModelMetaData modelMetaData,
                            EntityLifecycleManager entityListener,
                            PersistenceContextType persContextType,
                            PostPersistCallback postPersistCallback) {
        this.storageMan = sm;
        this.modelMetaData = modelMetaData;
        this.entityListener = entityListener;
        this.postPersistCallback = postPersistCallback;
        toBeDeleted = new DeletePacket(modelMetaData);
        storeOidStateContainer = new StatesToStore(modelMetaData);
        this.extendedPContext = persContextType == PersistenceContextType.EXTENDED;
    }

    public EntityTransaction getTransaction() {
        return this;
    }

    /**
     * The StorageManager calls this method to check if we need prefetched
     * data or not.
     */
    public boolean isStateRequired(OID oid, FetchGroup fetchGroup) {
        Object value = cache.get(oid);
        if (value == null) return true;
        State state;
        if (value instanceof State) {
            state = (State) value;
        } else {
            state = ((StateManagerImp)value).state;
        }

        if (state != null) {
            if (fetchGroup == null) {
                fetchGroup = state.getClassMetaData().fetchGroups[0];
            }
            return !state.containsFetchGroup(fetchGroup);
        }
        return true;
    }

    public void close() {
        if (txActive) {
            throw new IllegalStateException("May not close EntityManager with " +
                    "an active transaction. Please commit/rollback");
        }
        if (extendedPContext) {

        }
        closed = true;
        if (proxy != null) {
            proxy.detach();
            proxy = null;
        }
    }

    public boolean isOpen() {
        return !closed;
    }

    /**
     * " If X is a new entity, it becomes managed. The entity X will be entered
     *  into the database at or before transaction commit or as a result of the
     *  flush operation .
     *
     * " If X is a preexisting managed entity, it is ignored by the persist
     *  operation. However, the persist operation is cascaded to entities
     *  referenced by X, if the relationships from X to these other entities
     *  is annotated with the cascade=PERSIST or cascade=ALL annotation member
     *  value.
     *
     * " If X is a removed entity, it becomes managed.
     *
     * " If X is a detached object, an IllegalArgumentException will be thrown
     *  by the container (or the transaction commit will fail).
     *
     * " For all entities Y referenced by a relationship from X, if the
     *  relationship to Y has been annotated with the cascade member
     *  value cascade=PERSIST or cascade=ALL, the persist operation is
     *  applied to Y.
     * @param entity
     */
    public void persist(Object entity) {
        if (entity == null) return;
        checkForActiveTransaction();

        //create a '==' based set
        final IdentityEntry tailPointer = new IdentityEntry("HEAD");
        EntrySet mergeSet = new EntrySet() {
            public IdentityEntry tail = tailPointer;

            public Entry createEntry(Object key) {
                IdentityEntry ne = new IdentityEntry(key);
                tail.nextEntry = ne;
                tail = ne;
                return ne;
            }
        };

        mergeSet.add(entity);
        for (IdentityEntry ie = tailPointer.nextEntry; ie != null; ie = ie.nextEntry) {
            persistImp(ie.getKey(), mergeSet);
        }
    }

    /**
     * Add all the OIDs and States in the container to the cache.
     */
    public void addToCache(StatesReturned container) {
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            com.versant.core.common.EntrySet.Entry e = (com.versant.core.common.EntrySet.Entry)i.next();
            cache.add((OID)e.getKey(), (State)e.getValue());
        }
    }

    private void persistImp(Object entity, EntrySet mergeSet) {
        if (entity == null) return;
        LifeCycleStatus lfs = getLifeCycleStatus(entity);
        switch(lfs) {
            case NEW:
                if (entityListener.hasPrePersistListeners()) {
                    entityListener.firePrePersist(entity);  
                }
                manage(entity).persistReferences(mergeSet);
                break;
            case MANAGED:
                getStateManager((PersistenceCapable) entity).persistReferences(mergeSet);
                break;
            case DETACHED:
                throw new IllegalArgumentException(
                        "The supplied instance is a detached instance. " +
                        "Please make use of the 'merge' operation.");
        }
    }

    /**
     * Check that an transaction is active.
     * @throws javax.persistence.TransactionRequiredException
     */
    private void checkForActiveTransaction() {
        if (!txActive) throw new TransactionRequiredException();
    }

    /**
     * Takes a new instance (ie not managed or detached) and manage it.
     * This is not a recursive operation.
     * @param entity
     */
    private StateManagerImp manage(Object entity) {
        StateManagerImp sm = new StateManagerImp(proxy, modelMetaData,
                entityListener, postPersistCallback);
        sm.manageNew((PersistenceCapable) entity, cache, newOidCounter++);
        txObjects.add(sm);
        return sm;
    }

    /**
     * Determine the status {@link LifeCycleStatus} of the supplied entity.
     * @param entity
     */
    public LifeCycleStatus getLifeCycleStatus(Object entity) {
        if (!(entity instanceof PersistenceCapable)) {
            throw new IllegalArgumentException("The supplied instance '"
                    + entity.getClass().getName() + "' is not a manageble type");
        }
        PersistenceCapable pc = (PersistenceCapable) entity;
        VersantStateManager vsm = getVersantStateManager(pc);
        if (vsm == null) {
            return LifeCycleStatus.NEW;
        }

        if (vsm instanceof VersantDetachedStateManager) {
            return LifeCycleStatus.DETACHED;
        } else if (vsm instanceof StateManagerImp) {
            StateManagerImp sm = (StateManagerImp) vsm;
            EntityManagerImp em = sm.getEm();
            if (em != this) {
                throw new IllegalArgumentException("The supplied entity is not " +
                        "managed does by this EntityManager");
            }
            if (sm.isRemoved()) {
                return LifeCycleStatus.REMOVED;
            }
            return LifeCycleStatus.MANAGED;
        } else {
            throw BindingSupportImpl.getInstance().internal("Unknown status");
        }
    }

    /**
     * Detach all the managed instance.
     */
    private void detachOnCommit() {
        VersantDetachedStateManager vdsm = new VersantDetachedStateManager();
        Iterator iter = cache.getCacheIterator();
        while (iter.hasNext()) {
            LocalCache.CacheEntry entry = (LocalCache.CacheEntry) iter.next();
            if (entry.getValue() instanceof StateManagerImp) {
                ((StateManagerImp) entry.getValue()).detachOnCommit(vdsm);
            }
        }
    }

    /**
     * Try and obtain the entitymanager for the pc instance.
     */
    private StateManagerImp getStateManager(PersistenceCapable pc) {
        return (StateManagerImp) getVersantStateManager(pc);
    }

    /**
     * " If X is a detached entity, it is copied onto a pre-existing managed
     * entity instance X' of the same identity or a new managed copy of X is created.
     *
     * " If X is a new entity instance, a new managed entity instance X' is created
     * and the state of X is copied into the new managed entity instance X'.
     *
     * " If X is a removed entity instance, an IllegalArgumentException will be
     * thrown by the container (or the transaction commit will fail).
     *
     * " If X is a managed entity, it is ignored by the merge operation, however,
     * the merge operation is cascaded to entities referenced by relationships
     * from X if these relationships have been annotated with the cascade member
     * value cascade=MERGE or cascade=ALL annotation.
     *
     * " For all entities Y referenced by relationships from X having the cascade
     * member value cascade=MERGE or cascade=ALL, Y is merged recursively as Y'.
     * For all such Y referenced by X, X' is set to reference Y'.
     * (Note that if X is managed then X is the same object as X'.)
     *
     * " If X is an entity merged to X', with a reference to another entity Y,
     * where cascade=MERGE or cascade=ALL is not specified, then navigation of
     * the same association from X' yields a reference to a managed object Y'
     * with the same persistent identity as Y.
     *
     * " Fields or properties of type java.sql.Blob and java.sql.Clob are
     * ignored by the merge operation.
     *
     * @param entity
     * @return the instance that the state was merged to
     */
    public <T> T merge(T entity) {
        //create a '==' based set
        final IdentityEntry tailPointer = new IdentityEntry("HEAD");
        EntrySet mergeSet = new EntrySet() {
            public IdentityEntry tail = tailPointer;

            public Entry createEntry(Object key) {
                IdentityEntry ne = new IdentityEntry(key);
                tail.nextEntry = ne;
                tail = ne;
                return ne;
            }
        };

        mergeSet.add(entity);
        T result = (T) mergeInternal(entity, mergeSet).pc;
        getStateManager((PersistenceCapable) result).mergeReferences(mergeSet);

        for (IdentityEntry ie = tailPointer.nextEntry.nextEntry; ie != null; ie = ie.nextEntry) {
            mergeInternal(ie.getKey(), mergeSet).mergeReferences(mergeSet);
        }
        return result;
    }

    /**
     * Merge in the provided entity.
     * @param entity
     * @param mergeSet
     */
    public StateManagerImp mergeInternal(Object entity, EntrySet mergeSet) {
        IdentityEntry e = (IdentityEntry) mergeSet.addAndReturnEntry(entity);
        if (e.value != null) {
            return (StateManagerImp) e.value;
        }
        switch(getLifeCycleStatus(entity)) {
            case NEW:
                return (StateManagerImp) (e.value = manage(entity));
            case MANAGED:
                checkManagedBy(entity);
                return (StateManagerImp) (e.value = getStateManager((PersistenceCapable)entity));
            case DETACHED:
                VersantDetachable de = (VersantDetachable) entity;
                OID oid = extractOID(de.versantGetOID());
                StateManagerImp sm = getSMById(oid);

                //check for version
                if (sm.cmd.optimisticLockingField != null &&
                        !de.versantGetVersion().equals(sm.getOptimisticLockingValue())) {
                    throw new RuntimeException("Conncurrent update");
                }

                mergeFromDetached(de, sm);
                return (StateManagerImp) (e.value = sm);
            case REMOVED:
                throw new IllegalStateException(
                        "'merge' operation not allowed on '"
                        + getLifeCycleStatus(entity) + "' instances");
            default:
                throw BindingSupportImpl.getInstance().internal("Unhandled lifecycle state '" +getLifeCycleStatus(entity)+"'");

        }
    }

    public <T> T mergeImp(T entity, EntrySet mergeSet) {
        switch(getLifeCycleStatus(entity)) {
            case NEW:
                return (T)manage(entity).mergeReferences(mergeSet).pc;
            case MANAGED:
                checkManagedBy(entity);
                return (T)getStateManager((PersistenceCapable)entity).mergeReferences(mergeSet).pc;
            case DETACHED:
                VersantDetachable de = (VersantDetachable) entity;
                OID oid = extractOID(de.versantGetOID());
                StateManagerImp sm = getSMById(oid);

                //check for version
                if (sm.cmd.optimisticLockingField != null &&
                        !de.versantGetVersion().equals(sm.getOptimisticLockingValue())) {
                    throw new RuntimeException("Conncurrent update");
                }
                mergeFromDetached(de, sm);
                sm.mergeReferences(mergeSet);
                return (T) sm.pc;
            case REMOVED:
                throw new IllegalStateException(
                        "'merge' operation not allowed on '"
                        + getLifeCycleStatus(entity) + "' instances");
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "Unhandled lifecycle state '" +getLifeCycleStatus(entity)+"'");

        }
    }

    /**
     * Merge from the detached instance to the managed instance.
     */
    private void mergeFromDetached(VersantDetachable de, StateManagerImp sm) {
        //only copy its dirty fields
        if (de.versantIsDirty()) {
            VersantDetachedStateManager dsm = de.versantGetDetachedStateManager();
            AttachStateManager asm = new AttachStateManager();
            asm.setSm(sm);
            try {
                de.jdoReplaceStateManager(asm);
                FieldMetaData[] mFields =  sm.cmd.managedFields;
                for (int i = 0; i < mFields.length; i++) {
                    FieldMetaData mField = mFields[i];
                    if (de.versantIsDirty(mField.managedFieldNo)) {
                        de.jdoProvideField(mField.managedFieldNo);
                    }
                }
            } finally {
                de.jdoReplaceStateManager(dsm);
            }
        }
    }

    /**
     * Check that the managedVal is managedBy this EntityManager.
     * @param managedVal
     */
    private void checkManagedBy(Object managedVal) throws UserException {
        if (!contains(managedVal)) {
            //throw exception
        }
    }

    /**
     * Return managed instance for supplied oid. If instance not found in
     * localCache then manage it.
     *
     * @param oid
     */
    private StateManagerImp getSMById(OID oid) {
        StateManagerImp sm = getSmFromCache(oid);
        if (sm == null) {
            ClassMetaData cmd = oid.getAvailableClassMetaData();
            StatesReturned sr = null;
            try {
                sr = storageMan.fetch(this, oid, null, cmd.fetchGroups[0], null);
            } catch (JDOObjectNotFoundException e) {
                throw throwEntityNotFound(oid);
            }
            sm = new StateManagerImp(proxy, modelMetaData, entityListener,
                    postPersistCallback);
            oid = sr.getDirectOID();

            sm.manage(oid, sr.get(oid), cache);
        }
        return sm;
    }

    /**
     * Called to create a managed instance from the oid-state pair.
     */
    StateManagerImp manage(OID oid, State state) {
        StateManagerImp sm = new StateManagerImp(proxy, modelMetaData,
                entityListener, postPersistCallback);
        sm.manage(oid, state, cache);
        return sm;
    }

    /**
     * Update the sm with the data as per fg. This will be done by a fetch from the
     * server.
     */
    void fetchState(StateManagerImp sm, FetchGroup fg) {
        StatesReturned sr = null;
        try {
            sr = storageMan.fetch(this, sm.oid, null, fg, null);
        } catch (JDOObjectNotFoundException e) {
            throw throwEntityNotFound(sm.oid);
        }
        sm.updateState(sr.get(sm.oid));
    }

    public Object getObjectByIdForState(OID oid, int stateFieldNo, int navClassIndex, OID fromOID) {
        try {
            PersistenceCapable pc = null;
            FieldMetaData fmd = fromOID.getAvailableClassMetaData().stateFields[stateFieldNo];
            if (fmd.embedded) {
                if (true) throw BindingSupportImpl.getInstance().notImplemented(
                        "Embedded support is not supported yet");
//                //create a managed instance of the embedded reference
//                StateManagerImp sm = getSmFromCache(fromOID);
//                if (fmd.nullIndicatorFmd != null) {
//                    if (sm.state.isFieldNullorZero(fmd.nullIndicatorFmd.stateFieldNo)) {
//                        return null;
//                    }
//                }
//                EmbeddedStateManager embeddedSm = sm.createEmbeddedSM(fmd);
//                pc = embeddedSm.pc;
            } else {
                pc = getSMById(oid).pc;
            }
            return pc;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * Convert an external Identity Object to an internal one.
     * @param oid The external intstance
     */
    private OID extractOID(Object oid) {
        if (oid instanceof VersantOid) {
            return modelMetaData.convertJDOGenieOIDtoOID((VersantOid)oid);
        } else {
            return modelMetaData.convertFromAppIdToOID(oid);
        }
    }

    /**
     * Mark the instance for deletion.
     * @param entity
     */
    public void remove(Object entity) {
        checkForActiveTransaction();
        LifeCycleStatus lfs = getLifeCycleStatus(entity);
        switch (lfs) {
            case DETACHED:
            case REMOVED:
                throw new IllegalArgumentException("May not remove 'DETACHED' or 'REMOVED' instances");
            case MANAGED:
                StateManagerImp sm = getStateManager((PersistenceCapable) entity);
                sm.remove();
                break;
            case NEW:
                break;
            default:
                throw new RuntimeException("Unhandled lifecycle state: '"
                        + lfs + "'");
        }
    }

    /**
     * Find by primary key.
     */
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        ClassMetaData cmd = modelMetaData.getClassMetaData(entityClass);
        if (cmd == null) {
            throw new IllegalArgumentException("The class '"
                    + entityClass.getName()
                    + "' is not a persistable class");
        }
        OID oid = cmd.createOID(false).fillFromIDObject(primaryKey);
        StateManagerImp sm = getSmFromCache(oid);
        if (sm == null) {
            StatesReturned sr;
            try {
                sr = storageMan.fetch(this, oid, null, cmd.fetchGroups[0], null);
            } catch (JDOObjectNotFoundException e) {
                throw throwEntityNotFound(entityClass, primaryKey);
            }
            sm = new StateManagerImp(proxy, modelMetaData, entityListener,
                    postPersistCallback);
            oid = sr.getDirectOID();
            sm.manage(oid, sr.get(oid), cache);
        }
        return (T) sm.getPersistenceCapable();
    }

    /**
     * Return a managed instance for the oid-state pair.
     */
    private StateManagerImp getSmFor(OID oid, State state) {
        StateManagerImp sm = getSmFromCache(oid);
        if (sm == null) {
            sm = manage(oid, state);
        }
        return sm;
    }

    /**
     * Get a managed instance from the cache. If the instance is not managed,but
     * there is a oid-state pair in the cache then manage and return it.
     */
    private StateManagerImp getSmFromCache(OID oid) {
        Object o = cache.get(oid);
        if (o == null) return null;
        if (o instanceof StateManagerImp) return (StateManagerImp) o;
        if (o instanceof State) {
            return manage(oid, (State) o);
        }
        throw BindingSupportImpl.getInstance().internal(
                "Unhandled instance type '" + o.getClass().getName() + "' in local cache");

    }

    private static RuntimeException throwEntityNotFound(Class entityClass, Object primaryKey) {
        return new EntityNotFoundException("Entity '"
                + entityClass.getName() + "' with id '" + primaryKey + "' can not be found.");
    }

    private static RuntimeException throwEntityNotFound(OID oid) {
        return new EntityNotFoundException("Entity '"
                + oid.getAvailableClassMetaData().qname + "' with id '" + oid
                + "' can not be found.");
    }

    /**
     * Flush all 'dirty' instances to the store.
     */
    public void flush() {
        checkForActiveTransaction();
        StatesReturned sc = null;
        try {
            //prepare the sm's for to store the states
            prepareForStore(true);
            //send the state to the server
            sc = storageMan.store(storeOidStateContainer, toBeDeleted, false,
                    StorageManager.STORE_OPTION_FLUSH, false);
            postStore(sc, true);
        } catch (Exception e) {
            handleException(e);
        } finally {
            if (sc != null) {
                sc.clear();
            }
            storeOidStateContainer.clear();
            toBeDeleted.clear();
            txObjects.clear();
        }
    }

    public void refresh(Object entity) {
        throw new RuntimeException("Not Implemented");
    }

    /**
     * Check if the instance belongs to the current persistence context.
     */
    public boolean contains(Object entity) {
        if (entity instanceof PersistenceCapable) {
            VersantStateManager vsm = getVersantStateManager((PersistenceCapable) entity);
            if (vsm instanceof StateManagerImp) {
                if (((StateManagerImp) vsm).getEm() == this) {
                    return false;
                }
            }
        }
        return false;
    }

    public Query createQuery(String ejbqlString) {
        return new VersantEjbQueryImp(proxy, ejbqlString);
    }

    public Query createNamedQuery(String name) {
        return null;
    }

    public Query createNativeQuery(String sqlString) {
        return null;
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        return null;
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return null;
    }

    public void begin() {
        if (txActive) {
            throw new RuntimeException("The transaction is alread active");
        }
        txVersion++;
        storageMan.begin(true);
        if (proxy == null) proxy = new EMProxy(this);
        txActive = true;
    }

    /**
     * Commit the active transaction. Everything must be detached at the end
     * of the operation. This would mean that the pc instance must contain the needed
     * data for it to be detached.
     */
    public void commit() {
        checkForActiveTransaction();
        //do persist on reachable instances
        for (Iterator iterator = txObjects.createLinkedListIterator(); iterator.hasNext();) {
            persist(((StateManagerImp)((LinkedListEntrySet.LinkedEntry)iterator.next()).getKey()).pc);
        }

        StatesReturned sc = null;
        try {
            //prepare the sm's for to store the states
            prepareForStore(true);
            //send the state to the server
            sc = storageMan.store(storeOidStateContainer, toBeDeleted, false,
                    StorageManager.STORE_OPTION_COMMIT,
                    false);


            postStore(sc, false);
            if (extendedPContext) {

            } else {
                detachOnCommit();
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            txActive = false;
            if (!extendedPContext) {
                if (proxy != null) {
                    proxy.detach();
                    proxy = null;
                    proxy = new EMProxy(this);
                }
                cache.clear();
            }
            if (sc != null) {
                sc.clear();
            }
            storeOidStateContainer.clear();
            toBeDeleted.clear();
            txObjects.clear();

        }
    }

    private void postStore(StatesReturned sc, boolean flush) {
        //give each instance a change to
        for (Iterator iterator = txObjects.createLinkedListIterator(); iterator.hasNext();) {
            ((StateManagerImp)((LinkedListEntrySet.LinkedEntry)iterator.next()).getKey()).postStore(sc, flush, false);
        }
    }

    /**
     * All public method must be wrapped with this.
     * @param e
     */
    private void handleException(Exception e) {
        e.printStackTrace(System.out);
        throw new RuntimeException(e);
    }

    /**
     * Prepare to store all dirty instances for a commit or flush. This finds
     * all reachable instances and invokes preStore lifecycle listeners and
     * jdoPreStore instance callbacks.
     */
    private void prepareForStore(boolean commit) {
        storeOidStateContainer.clear();
        toBeDeleted.clear();
        for (Iterator iterator = txObjects.createLinkedListIterator(); iterator.hasNext();) {
            ((StateManagerImp)((LinkedListEntrySet.LinkedEntry)iterator.next()).getKey()).
                    prepareCommitOrFlush(commit, toBeDeleted, storeOidStateContainer);
        }
    }

    public void rollback() {
        if (!txActive) {
            throw new RuntimeException("There is no active transaction");
        }
        txActive = false;
        storageMan.rollback();
        if (proxy != null) {
            proxy.detach();
            proxy = null;
            proxy = new EMProxy(this);
        }
    }

    public boolean isActive() {
        return txActive;
    }

    /**
     * TODO: Refactor this out of the common api.
     */
    public PersistenceManager getPersistenceManager() {
        throw new RuntimeException("This method should not be called on EntityManager");
    }

    public Object getObjectById(Object o, boolean b) {
        if (o instanceof OID) {
            return getSMById((OID) o).pc;
        } else {
            throw BindingSupportImpl.getInstance().unsupported("unsupported operation");
        }
    }

    /**
     * Add sm to the list of transactional instances. These are all the instances
     * that needs to be part of the commit operation.
     * @param sm
     */
    void addToTxList(StateManagerImp sm) {
        if (txActive) {
            txObjects.add(sm);
        }
    }

    public void cancelQueryExecution() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDirty() {
        return !(txObjects.size() == 0);
    }

    public void flushIfDepOn(int[] cmdBits) {
        flush();
    }

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
        storageMan.setLockingPolicy(policy);
    }

    public int getDatastoreTxLocking() {
        switch (storageMan.getLockingPolicy()) {
            case StorageManager.LOCK_POLICY_NONE:
                return VersantPersistenceManager.LOCKING_NONE;
            case StorageManager.LOCK_POLICY_FIRST:
                return VersantPersistenceManager.LOCKING_FIRST;
            case StorageManager.LOCK_POLICY_ALL:
                return VersantPersistenceManager.LOCKING_ALL;
        }
        return VersantPersistenceManager.LOCKING_NONE;
    }

    public int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex) {
        if (oids == null || oids.length <= 0) return 0;
        OIDArray oidArray = new OIDArray();
        for (int i = 0; i < length; i++) {
            Object o = oids[i];
            if (!(o instanceof OID)) {
                data[i] = o;
                continue;
            }
            OID oid = (OID)o;
            StateManagerImp sm = getSmFromCache(oid);
            if (sm != null) {
                data[i] = sm.pc;
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
            StatesReturned container = storageMan.fetch(this, oidArray, triggerField);
            // keep a reference to each of the returned PCStateMan's so
            // that they are not GCed before we reference them
            StateManagerImp[] nogc = addToCacheAndManage(container);
            for (int i = 0; i < length; i++) {
                if (data[i] == null) {
                    data[i] = getSmFromCache((OID)oids[i]).pc;
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
     * Add all the OIDs and States in the container to the cache. The
     * PCStateMan's are kept and returned to prevent the new instances
     * being GCed before they are referenced.
     */
    private StateManagerImp[] addToCacheAndManage(StatesReturned container) {
        StateManagerImp[] ans = new StateManagerImp[container.size()];
        int c = 0;
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            com.versant.core.common.EntrySet.Entry e = (com.versant.core.common.EntrySet.Entry)i.next();
            ans[c] = getSmFor((OID)e.getKey(), (State)e.getValue());
        }
        return ans;
    }

    public OID getInternalOID(PersistenceCapable pc) {
        StateManagerImp sm = getStateManager(pc);
        if (sm == null) return null;
        return sm.getOID();
    }

    /**
     * TODO: remove this method for the common API.
     */
    public PCStateMan getInternalSM(PersistenceCapable pc) {
        throw new RuntimeException("This is not supposed to be called on EntityManager");
    }

    public VersantStateManager getVersantStateManager(PersistenceCapable pc) {
        VersantStateManager em;
        try {
            Field f;
            Class cls = pc.getClass();
            while (true) {
                try {
                    f = cls.getDeclaredField("jdoStateManager");
                    break;
                } catch (NoSuchFieldException e) {
                    cls = cls.getSuperclass();
                    if (!PersistenceCapable.class.isAssignableFrom(cls)) {
                        throw e;
                    }
                }
            }
            f.setAccessible(true);
            em = (VersantStateManager) f.get(pc);
        } catch (Exception e) {
            System.out.println("pc.getClass().getName() = " + pc.getClass().getName());
            throw new RuntimeException(e.getMessage(), e);
        }
        return em;
    }


    public StorageManager getStorageManager() {
        return storageMan;
    }


    /**
     * TODO: move to base class
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
            param = modelMetaData.convertJDOGenieOIDtoOID((VersantOid)param).getAvailableOID();
        } else {
            ClassMetaData cmd =
                    modelMetaData.getClassMetaDataForObjectIdClass(
                            param.getClass());
            if (cmd != null) { // app identity objectid-class parameter
                OID oid = cmd.createOID(false);
                oid.fillFromPK(param);
                param = oid;
            }
        }
        return param;
    }

    /**
     * Called if sm is was involved in an operation that made its state dirty. If there is no active transaction
     * then the instance will not be added to the txList that is used to control what gets commited at the end of the
     * transaction.
     *
     * @param sm
     * @param fmd
     */
    void makeDirty(StateManagerImp sm, FieldMetaData fmd) {
        if (txActive) addToTxList(sm);
    }

    public boolean isRetainValues() {
        return retainValues;
    }

    public void setRetainValues(boolean value) {
        this.retainValues = value;
    }
}
