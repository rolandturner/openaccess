
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

import com.versant.core.metadata.*;
import com.versant.core.jdo.*;
import com.versant.core.common.*;

import javax.jdo.spi.Detachable;
import javax.jdo.spi.StateManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.PersistenceManager;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * StateManager used for ejb3
 *
 * State transitions.
 *
 * Implement a scheme for extended persistence context where by the pc instance never contains
 * any of its fields. The fields are always held by the state.
 * Advantages:
 * The advantage of this is that we never have to push data to the pc instance except when it is detached.
 * It is much faster to process the data on the state that to obtain it from the pc instance.
 * We do not have to go through the double operation on set().
 *
 * Cons:
 * There will always be read/write interception.
 *
 * DetachOnCommit:
 * For this we will ensure that a pc instance always contains all it data. Then on commit we should be able to
 * disconnect the instances.
 *
 */
public class StateManagerImp implements VersantStateManager {
    private static final int STATE_TRANSIENT = 0;
    private static final int STATE_T_CLEAN = 8;
    private static final int STATE_T_DIRTY = 12;
    private static final int STATE_HOLLOW = 16;
    private static final int STATE_P_NON_TX = STATE_HOLLOW;
    private static final int STATE_P_CLEAN = 24;
    private static final int STATE_P_DIRTY = 28;
    private static final int STATE_P_DEL = 29;
    private static final int STATE_P_NEW = 30;
    private static final int STATE_P_NEW_DEL = 31;

    private static final int MASK_DELETE = 1;
    private static final int MASK_NEW = 2;
    private static final int MASK_DIRTY = 4;
    private static final int MASK_TX = 8;
    private static final int MASK_PERSISTENT = 16;

    private static final int MASK_DELETE_TX_DIRTY = MASK_DELETE + MASK_TX + MASK_DIRTY;

    /**
     * A mask field that specifies the current lifecycle.
     */
    private int stateM;
    /**
     * The synch value of the last tx this instance was synched.
     */
    private int txSynch;


    private EMProxy emProxy;
    OID oid;
    ClassMetaData cmd;
    private ModelMetaData modelMetaData;
    State state;
    private State origState;
    PersistenceCapable pc;
    private byte jdoFlags;
    /**
     * array that indicates which fields the pc instance contains.
     */
//    private boolean[] loadedFields;
    private boolean addedForDelete;
    /**
     * If the entity is marked to be removed.
     */
    private boolean removed;
    private State toStoreState;
    private LocalCache cache;
    /**
     * This field is set if the instance is marked for eviction while it is dirty. This still
     * will then be evicted on commit.
     */
    private boolean toBeEvictedFlag;
    private EntityLifecycleManager entityListener;
    private PostPersistCallback postPersistCallback;

    public StateManagerImp(EMProxy em, ModelMetaData modelMetaData,
                           EntityLifecycleManager entityListener,
                           PostPersistCallback postPersistCallback) {
        this.emProxy = em;
        this.modelMetaData = modelMetaData;
        this.entityListener = entityListener;
        this.postPersistCallback = postPersistCallback;
    }

    /**
     * Determine the current status of the statemanager. The status is context sensitive ie. it depends on the
     * tx value of the pm if the status is p-clean or p-non-tx
     */
    int getStatus(EntityManagerImp eManager) {
        return 0;
    }

    /**
     * Accepts a {@link LifeCycleStatus.NEW} entity and manage it.
     *
     * @param pc
     */
    public void manageNew(PersistenceCapable pc, LocalCache cache, int newOidVal) {
        stateM = STATE_P_NEW;

        this.pc = pc;
        pc.jdoReplaceStateManager(this);
        cmd = modelMetaData.getClassMetaData(pc.getClass());
//        loadedFields = new boolean[cmd.stateFields.length];

        //create the state instance
        state = cmd.createState();
        state.setClassMetaData(cmd);

        //ask to provide all managed fields to state
        pc.jdoProvideFields(cmd.allManagedFieldNosArray);
        //create a new OID and add to cache by newOid
        oid = cache.addNewInstance(createOidForNew(newOidVal), this);

        //mark all fields as READ_OK
        updateJdoFlag(pc, PersistenceCapable.LOAD_REQUIRED);
        //set all fields as loaded
//        setBooleanArray(loadedFields, true);
        this.cache = cache;
    }

    /**
     * Manage an existing instance.
     * @param oidVal
     * @param stateVal
     */
    public void manage(OID oidVal, State stateVal, LocalCache cache) {
        this.oid = oidVal;
        cmd = stateVal.getClassMetaData(modelMetaData);
        try {
            pc = (PersistenceCapable) cmd.cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("The class '"
                    + cmd.cls.getName()
                    + "' does not have a default constructor", e);
        }
//        loadedFields = new boolean[cmd.stateFields.length];

        pc.jdoReplaceStateManager(this);

        //create the state instance
        state = cmd.createState();
        state.setClassMetaData(cmd);
        state.updateFrom(stateVal);

        updateOrigState(stateVal);

        //add to cache
        cache.add(oidVal, this);

        updateJdoFlag(pc, PersistenceCapable.LOAD_REQUIRED);

        //fill the pk fields on the pc
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            state.copyFields(oid);
            pc.jdoReplaceFields(cmd.pkFieldNos);
            if (entityListener.hasPostLoadListeners()) {
                entityListener.firePostLoad(pc, state.getClassIndex());
            }
        }
    }

    private void updateOrigState(State aState) {
        if (aState == null) return;
        if (cmd.changedOptimisticLocking) {
            createOrigState();
            origState.updateNonFilled(aState);
        } else if (cmd.optimisticLockingField != null) {
            createOrigState();
            origState.copyOptimisticLockingField(aState);
        }
    }

    private void createOrigState() {
        if (origState == null) {
            origState = cmd.createState();
            origState.setClassMetaData(cmd);
        }
    }

    private void updateJdoFlag(PersistenceCapable pc, byte val) {
        jdoFlags = val;
        pc.jdoReplaceFlags();
    }

    private void setBooleanArray(boolean[] fields, boolean val) {
        for (int i = fields.length - 1; i >= 0; i--) {
            fields[i] = val;
        }
    }

    /**
     * Replace all the fields that should be sco's with new instances and reset on the pc instance
     */
    private void replaceSCOFields() {
        final int[] scoFieldArray = new int[cmd.scoFieldNos.length];
        int count = state.replaceSCOFields(pc, emProxy, scoFieldArray);
        for (int i = count - 1; i >= 0; i--) {
            pc.jdoReplaceField(scoFieldArray[i]);
        }
    }

    /**
     * Update the states that was involved in the transaction with the state information that was returned from
     * the datastore. This might include autoset fields and pk fields.
     *
     * This operation might be followed by an detach or the instance must be able to continue with the persistence context.
     */
    public void postStore(StatesReturned sr, boolean flush, boolean detachOnCommit) {
        if (removed) {
            /**
             * P-Deleted
             * ---------
             *
             * General:
             * There is probably no reason why an dirty instance may not be read, except for after a flush
             * any db access will throw a row not found. Therefore to be consistent, no reads are allowed on 'deleted' instances.
             *
             * Question:
             * Is an dirty object allowed to be deleted.
             *
             * Answer:
             * Yes.
             *
             * Question:
             * Is there an scenario where we want to first do an update to the to be deleted row and then deleted it.
             *
             * Answer:
             * No, that does make sense.
             *
             * - On delete the state of the pcInstance is replaced by an 'DeletedState'. This is an state that
             * throws exception on all access.
             *
             * - On prestore:
             * This can be called for a commit or a flush operation. In both cases the row is deleted from from the
             * underlying jdbc connection and therefore any further reads will cause an 'NoSuchObjectException' if it reaches
             * the store.
             *
             * If consistency checks are enabled then we should check that there are no instances that have dirty
             * references to an deleted state. The oid of the deleted instance is added to the toBeDeleted collection only.
             *
             * - On postStore:
             * If this stage is reached then there was no 'optimistic locking failure', but the transaction may still
             * rollback at a later stage. If the user has specified that a rollback should reset to 'before' values
             * the we must retain a hard reference to the instance until commit. If not the we can dereference the
             * deleted instance and it will be cleanup by gc.
             *
             * - On commit: At this stage we can dereference and unmanage it. clear the before state if used,
             * reset the pc values to there defaults, unset the statemanager.
             *
             * P-Dirty
             * -------
             *
             * General:
             * A P-Clean instance will transition to P-Dirty on a 'set'. Hard references must be kept to these instance
             * because the user might derefernce it.
             *
             * prestore: (called on instances in txDirty list)
             * called for commit of flush. The dirty fields is written to the store. The method will return a container
             * of oid-state for new instance and instances that contain autoset fields.
             * If 'retainValues' are set then we will not clear the instance of its state, else we can clear the
             * instance here of its state to conserve memory. If the 'restoreValues' is not set then we can also release
             * the hardreference to the instance. THis will allow the instance to be gc'd if not referenced by client.
             *
             * poststore: (called on instances in txDirty list) Q: Why not do it only on the list of oid-states returned from server
             * For now assume that it is ok to only process the data as returned from the server.
             * If reached then commit/flush operation was ok. This method is to update oid field values and autoset fields
             * as returned from the db.
             * If DetachOnCommit:
             * Do nonthing.
             *
             * If continuePContext while doing poststore:
             * The 'state' of the instance will depends if this was a flush or a commit.???
             *
             * P-New
             * -----
             *
             * General:
             * A transient instance becomes 'P-New' on manage. All the managed fields of a 'new' instance will be
             * persisted to the db on commit/flush.
             *
             * A p-new instance may need a beforestate if that is required per option. This state must contain all
             * the data as on 'manage'. We will replace all sco instance on manage.
             *
             * prestore:
             * called for commit or flush. All the fields are written to the store. The container will return the real
             * oid for this instance if it was not already available.
             *
             * poststore:
             * if the 'restoreValues' is not set then we do not need to keep a 'hard' reference to this instance.
             * if 'restoreValus' is set and this was a commit then the 'restoreFromState' my be cleared.
             * The state will change to p-clean.
             *
             * P-New-Deleted
             * -------------
             *
             * A p-new instance transitions to a p-new-deleted instance on delete. This means that there are no state
             * for the instance in the db or the connection.
             *
             * rollback:
             * apply the 'restoreFromState' if applicable
             *
             * prestore:
             * prestore: commit
             * - change to transient
             * prestore: flush
             * - nop
             *
             * poststore
             * - nop
             *
             * P-Non-Transactional:
             * -------------------
             *
             * This instance contains data that was read in a previous transaction. This state is reachable both
             * outside of a transaction and in a next transaction.
             *
             *
             */
            if (entityListener.hasPostRemoveListeners()) {
                entityListener.firePostRemove(pc, state.getClassIndex());
            }
            pc.jdoReplaceStateManager(null);
            state.clear();
            state = null;
            pc = null;
        } else {
            EntrySet.Entry e = sr.getEntry(oid);
            if (e != null) {
                updateAutoFields((State) e.getValue());
            }
            if (oid.isNew()) {
                //updates the real oid on the newOid and also update this.oid reference to the realOID
                oid = ((NewObjectOID)oid).setRealOid(((OID)e.getKey()).getRealOID());

                //fill the pk fields on the pc
                if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                    state.copyFields(oid);
                    pc.jdoReplaceFields(cmd.pkFieldNos);
                }
                if (postPersistCallback != null) {
                    postPersistCallback.postPersist(pc);
                }
                if (entityListener.hasPostPersistListeners()) {
                    entityListener.firePostPersist(pc, state.getClassIndex());
                }
            } else { // it was only updated
                if (entityListener.hasPostUpdateListeners()) {
                    entityListener.firePostUpdate(pc, state.getClassIndex());
                }
            }
            state.makeClean();
        }

//            try {
//                switch (stateM) {
//                    case STATE_TRANSIENT:
//                        break;
//                    case STATE_T_CLEAN:
//                        break;
//                    case STATE_T_DIRTY:
//                        stateM = STATE_T_CLEAN;
//                        if (beforeState != null) beforeState.clear();
//                        if (toStoreState != null) toStoreState.clear();
//                        break;
//                    case STATE_HOLLOW:
//                        break;
//                    case STATE_P_NEW:
////                        changeTmpOIDToRealOID();
//                    case STATE_P_CLEAN:
//                    case STATE_P_DIRTY:
//                        if (emProxy.isRetainValues()) {
//                            if (!toBeEvictedFlag) {
//                                changeToPNonTxForCommit(emProxy.getEm());
//                            } else {
//                                changeToHollowState();
//                                cacheEntry.changeToRefType(jdoManagedCache.queue,
//                                        VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);
//                            }
//                        } else {
//                            //do this lazily
////                            changeToHollowState();
////                            if (toBeEvictedFlag) {
//////                                cacheEntry.changeToRefType(jdoManagedCache.queue,
//////                                        VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);
////                            }
//                        }
////                    if (beforeState != null) beforeState.clear();
//                        if (toStoreState != null) toStoreState.clear();
//                        break;
//                    case STATE_P_DEL:
//                    case STATE_P_NEW_DEL:
//                        state = createStateImp();
//                        deleteImpForCommit();
//                        break;
//                    default:
//                        throw BindingSupportImpl.getInstance().internal(
//                                "The state is unreachable");
//                }
//            } finally {
////            clearTxFlags();
//            }
    }

    /**
     * Hollow the instance.
     */
    private void changeToHollowState() {
    }

//    /**
//     * The instance is only cleared of object references and not the primitives.
//     * The loaded fields and the JDO_FLAGS is reset to force the pc to ask the sm for new values.
//     * <p/>
//     * For application identity the state will not transition to hollow as the pk
//     * fields must remain.
//     */
//    private final void changeToHollowState(boolean clearBeforeState) {
//        if (state == null) return;
//        if (isHollow()) return;
////
////        if (instanceCallbacks != null) {
////            instanceCallbacks.jdoPreClear();
////        }
////
//        stateM = STATE_HOLLOW;
//
//        state.unmanageSCOFields();
//        state.clear();
//        State tmpState = state;
//
//        state = INIT_STATE;
//        //ask pc to replace only the instances that hold refs to other pc instances
//        //this helps to gc instances quicker
//        pc.jdoReplaceFields(classMetaData.absPCTypeFields);
//        state = tmpState;
//
//        origState.clear();
//        if (clearBeforeState && beforeState != null) {
//            beforeState.clear();
//        }
//
//        setLoadRequired();
//        resetLoadedFields();
//        rfgLoaded = false;
//        dfgLoaded = false;
//
//        if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
//            replaceApplicationPKFields();
//        }
//
//        if (forReadState != null) forReadState.clearFilledFlags();
//
//        if (Debug.DEBUG) {
//            if (!isHollow() && classMetaData.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) {
//                if (Debug.DEBUG) {
//                    Debug.OUT.println("isEmpty = " + state.isEmpty());
//                }
//                dump();
//                throw BindingSupportImpl.getInstance().internal(
//                        "The instance is not hollow");
//            }
//        }
//    }

    /**
     * Change state to p-non-tx after a commit.
     * @param em
     */
    private void changeToPNonTxForCommit(EntityManagerImp em) {
    }


    /**
     * Must ensure that all resources is clean up.
     */
    private final void unManage() {
        cache.remove(oid);
        pc.jdoReplaceStateManager(null);
        state.unmanageSCOFields();
        pc = null;
        oid = null;
        state = null;
        origState = null;
        toStoreState = null;
    }

    /**
     * Called on commit of a
     */
    private void continueContextOnCommit() {
    }

    /**
     * Called on commit to detach the pc. This is called on all reachable pc instances.
     * TODO: Think about a scheme whereby we do not have to process all reachable instance to detach them.
     * Maybe this can be done by Changing the pm that the StateManager belongs to to indicate that they are now
     * detached.
     * An option might be to store the state on the pc instance and to enhance it in such an way that for detached instance
     * it will read the data from the underlying state. The sm can still reference the state as per normal.
     * -issues:
     * * The fields will always have to be in the pc, or we must allow it to be fetched lasily even in an detached env.
     *
     *
     * - All the default fg fields and the fields that was navigated must be loaded to the pc instance.
     * - The statemanager is replaced to that of the detached sm.
     */
    public void detachOnCommit(VersantDetachedStateManager dsm) {
        //already detached
        if (state == null) return;
        //do not detach if removed
        if (removed) {
            pc.jdoReplaceStateManager(null);
        } else {
            //load dfg if non loaded
            if (!state.containsFetchGroup(cmd.fetchGroups[0])) {
                getEm().fetchState(this, cmd.fetchGroups[0]);
            }

            state.replaceSCOFields(pc, emProxy, new int[cmd.scoFieldNos.length]);
            final int[] filledManagedFieldNos = getFilledFieldNos();
            //System.out.println("\n\n\n\nStateManagerImp.detachOnCommit");
            for (int i = 0; i < filledManagedFieldNos.length; i++) {
                int filledManagedFieldNo = filledManagedFieldNos[i];
                //System.out.println("filledManagedFieldNo = " + filledManagedFieldNo);
            }
            pc.jdoReplaceFields(filledManagedFieldNos);

            ((VersantDetachable)pc).versantSetOID(getExternalOID());
            ((VersantDetachable)pc).versantSetVersion(state.getOptimisticLockingValue());
            ((VersantDetachable)pc).jdoReplaceStateManager(dsm);

            //ensure that all the currently loaded field and the dfg fetchgroup is loaded
            //fill the pc with the dfg
            //todo: Find a more efficient way of doing this
//            pc.jdoReplaceFields(cmd.dfgAbsFieldNos);
//            for (int i = 0; i < loadedFields.length; i++) {
//                if (loadedFields[i]) {
//                    if (cmd.stateFields[i].managedFieldNo >= 0) pc.jdoReplaceField(cmd.stateFields[i].managedFieldNo);
//                }
//            }

            //todo add a batch update for loaded fields
            for (int i = 0; i < filledManagedFieldNos.length; i++) {
                ((VersantDetachable)pc).versantSetLoaded(
                        filledManagedFieldNos[i]);
            }
        }
        state.clear();
        state = null;
        pc = null;
    }

    /**
     * Return int[] of managedField nos that is contained in the state.
     */
    private int[] getFilledFieldNos() {
        //System.out.println("StateManagerImp.getFilledFieldNos");
        int[] stateFieldNos = new int[cmd.stateFields.length];
        int scount = state.getFieldNos(stateFieldNos);
        if (stateFieldNos.length != scount) {
            int[] tmpFieldNos = new int[scount];
            System.arraycopy(stateFieldNos, 0, tmpFieldNos, 0, scount);
            stateFieldNos = tmpFieldNos;
        }

        int[] filledManagedFieldNos = new int[scount];
        int mCount = 0;
        for (int i = 0; i < scount; i++) {
            FieldMetaData fmd = cmd.stateFields[stateFieldNos[i]];
            if (fmd.managedFieldNo == -1) continue;
            filledManagedFieldNos[mCount++] = fmd.managedFieldNo;
        }

        if (mCount != scount) {
            int[] tmp = new int[mCount];
            System.arraycopy(filledManagedFieldNos, 0, tmp, 0, mCount);
            filledManagedFieldNos = tmp;
        }
        return filledManagedFieldNos;
    }

    public Object getOptimisticLockingValue() {
        if (oid.isNew()) return null;
//        getEm().checkNonTxRead();
//        loadRequiredFetchGroup();
        return state.getOptimisticLockingValue();
    }

    private Object getExternalOID() {
        if (oid.isNew()) throw new RuntimeException("Not to be called on new oid");
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            return new VersantOid(oid, modelMetaData, oid.isResolved());
        } else if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            if (cmd.objectIdClass == null) {
                return new VersantOid(oid, modelMetaData, oid.isResolved());
            } else {
                return oid.createObjectIdClassInstance();
            }
        } else {
            throw BindingSupportImpl.getInstance().unsupported();
        }
    }

    EntityManagerImp getEm() {
        return emProxy.getEm();
    }


    public void makeDirty(PersistenceCapable persistenceCapable, int managedFieldNo) {
        emProxy.getEm().makeDirty(this, getFmd(managedFieldNo));
        state.makeDirtyAbs(managedFieldNo);
    }

    public void fillNewAppPKField(int fieldNo) {
        if (oid.isNew()) {
            NewObjectOID newObjectOID = (NewObjectOID)oid;
            if (newObjectOID.realOID == null) {
                if (cmd.postInsertKeyGenerator) {
                    getEm().flush();
                } else {
                    newObjectOID.realOID = getEm().storageMan.createOID(cmd);
                    state.copyFields(newObjectOID.realOID);
                    pc.jdoReplaceFields(cmd.pkFieldNos);
                }
            }
        }
    }

    /**
     * Update the state with autoset fields returned from the store operation.
     */
    private void updateAutoFields(State autoS) {
        if (autoS != null && cmd.hasAutoSetFields) {
            this.state.updateFrom(autoS);
        }
        updateOrigState(autoS);
    }

    public OID getOID() {
        return oid;
    }

    public PersistenceCapable getPersistenceCapable() {
        return pc;
    }

    /**
     * Manage all the references as per spec. Should look at the annotations
     * and decide on which references to follow.
     */
    public void manageReferences() {
    }

    /**
     * Must create an oid for a {@link LifeCycleStatus.NEW} instance. Depending on
     *the key generation etc we will also create the real oid if the info is available.
     */
    private NewObjectOID createOidForNew(int newOIDVal) {
        NewObjectOID newOID = cmd.createNewObjectOID();
        newOID.idNo = newOIDVal;
        getRealOIDIfAppId(cmd, newOID, state);
        return newOID;
    }

    /**
     * Create the real oid for this instance if possible. This is done by looking
     * at
     */
    public static OID getRealOIDIfAppId(ClassMetaData cmd, NewObjectOID newOID,
            State state) {
        if (newOID.realOID != null) return newOID.realOID;
        if (!cmd.postInsertKeyGenerator
                && cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION
                && state.containsValidAppIdFields()) {

            final OID rOid = cmd.createOID(true);
            state.copyKeyFields(rOid);

            newOID.realOID = rOid;
            return rOid;
        }
        return null;
    }

    /**
     * If this instance is marked for removal/Deletion
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Mark this entity as removed 
     */
    public void remove() {
        if (entityListener.hasPreRemoveListeners()) {
            entityListener.firePreRemove(pc, state.getClassIndex());
        }
        emProxy.getEm().addToTxList(this);
        removed = true;
    }

    FieldMetaData getFmd(int absFieldNo) {
        return cmd.managedFields[absFieldNo];
    }

    /**
     * This is called on all transactional objects so that they can prepare for
     * the commit or flush. If the state was deleted it will call add itself
     * to the list of instances to be deleted etc.
     */
    public void prepareCommitOrFlush(boolean commit, DeletePacket toDelete,
            StatesToStore toStore) {
        /**
         * Add all the instance that must be deleted to the toBeDeleted collection.
         * If the oid is new then it must not be added because it is not in the db and therefore
         * not to be removed.
         */
        if (isRemoved()) {
            addForDelete(toDelete);
        } else if (isDirty()) {
            // clear transactional fields if this is a commit
            if (commit) state.clearTransactionNonPersistentFields();

            State toStoreState = createToStoreState();
            oid.resolve(state);

            // If nothing is copied to toStoreState then only transactional
            // fields were dirty. If this is a commit and the instance is new
            // then persist it anyway.
            if (!state.fillToStoreState(toStoreState, getEm(), this)
                    && (!commit || !oid.isNew())) {
                return;
            }
            addToStoreOidContainer(toStoreState, oid.isNew(), toStore);
        }
    }

    private void addToStoreOidContainer(State aState, boolean aNew,
            StatesToStore toStore) {
        if (aNew) {
            toStore.add(oid, aState, null,
                    aNew && cmd.postInsertKeyGenerator);
        } else {
            if (entityListener.hasPreUpdateListeners()) {
                entityListener.firePreUpdate(pc, state.getClassIndex());
            }
            toStore.add(oid, aState, origState,
                    aNew && cmd.postInsertKeyGenerator);
        }
    }

    /**
     * Add this instance to the todeletelist. This must not be done for instances
     * that is new. Instance that is flushed must be included.
     */
    private void addForDelete(DeletePacket toDelete) {
        if (!oid.isNew() && !addedForDelete) {
            toDelete.add(oid, null);
            addedForDelete = true;
        }
    }

    private State createToStoreState() {
        if (toStoreState == null) {
            toStoreState = cmd.createState();
            toStoreState.setClassMetaData(cmd);
        } else {
            toStoreState.clear();
        }
        return toStoreState;
    }

    /**
     * If this sm must be flushed to the store.
     */
    public boolean isDirty() {
        return oid.isNew() || state.isDirty();
    }


    public OID getInternalOID(PersistenceCapable pc) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getObjectById(Object oid, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PCStateMan getInternalSM(PersistenceCapable pc) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PersistenceManager getPersistenceManager() {
        throw BindingSupportImpl.getInstance().internal("");
//        return getEm();
    }









    public byte replacingFlags(PersistenceCapable persistenceCapable) {
        return jdoFlags;
    }

    public StateManager replacingStateManager(PersistenceCapable persistenceCapable,
            StateManager stateManager) {
        return stateManager;
    }

    public boolean isDirty(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isTransactional(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isPersistent(PersistenceCapable persistenceCapable) {
        return false;
    }

    private final boolean isNew() {
        return ((stateM & MASK_NEW) != 0);
    }

    public boolean isNew(PersistenceCapable persistenceCapable) {
        return isNew();
    }

    public boolean isDeleted(PersistenceCapable persistenceCapable) {
        return false;
    }

    public PersistenceManager getPersistenceManager(
            PersistenceCapable persistenceCapable) {
        throw new RuntimeException();
    }

    public void makeDirty(PersistenceCapable persistenceCapable, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getObjectId(PersistenceCapable persistenceCapable) {
        return null;
    }

    public Object getTransactionalObjectId(PersistenceCapable persistenceCapable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLoaded(PersistenceCapable persistenceCapable, int i) {
        //System.out.println("StateManagerImp.isLoaded");

        /**
         * Check state transition here.
         * If the instance should be hollow after commit then it would be done on commit for dirty instances, else
         * it will be done on first access.
         *
         * - Must be able to know to hollow an instance on first access after tx commit.
         * - Must be able to hollow instance on first access in next tx.
         *
         */

        /**
         * HOLLOW_ON_COMMIT_LAZY
         * - no active tx
         * - hollow_on_commit_lazy
         * - not currently hollow
         * - not already done
         */
//        if (!isHollow() && ) {
//        }

        return false;
//        return loadedFields[getFmd(i).stateFieldNo];
    }

    /**
     * This will clear the state of all data so that an reload will be triggered on next access. The instance
     * should also be cleared, but not of its app-id fields. LOAD_REQUIRED' must be set to ensure that the
     * pc instance asks for its data on next access.
     */
    private void makeHollow() {
        state.clear();
        updateJdoFlag(pc, PersistenceCapable.LOAD_REQUIRED);
    }

    /**
     * If there is no fields currently loaded for the instance.
     * - the pc contains none of its fields except app-id fields
     * - the state is empty
     *
     * Question:
     * Is a application identity instance hollow if only its identity fields is filled?
     *
     * I think that this should be true. We would not want to reload the state if it only contains the id fields. These
     * fields can not change anyway.
     */
    private boolean isHollow() {
        return state.isEmpty();
    }

    public void preSerialize(PersistenceCapable persistenceCapable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBooleanField(PersistenceCapable persistenceCapable, int i,
            boolean b) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getBooleanField(fmd.stateFieldNo);
    }

    public char getCharField(PersistenceCapable persistenceCapable, int i,
            char c) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getCharField(fmd.stateFieldNo);
    }

    public byte getByteField(PersistenceCapable persistenceCapable, int i,
            byte b) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getByteField(fmd.stateFieldNo);
    }

    public short getShortField(PersistenceCapable persistenceCapable, int i,
            short i1) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getShortField(fmd.stateFieldNo);
    }

    public int getIntField(PersistenceCapable persistenceCapable, int i,
            int i1) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getIntField(fmd.stateFieldNo);
    }

    public long getLongField(PersistenceCapable persistenceCapable, int i,
            long l) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getLongField(fmd.stateFieldNo);
    }

    public float getFloatField(PersistenceCapable persistenceCapable, int i,
            float v) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getFloatField(fmd.stateFieldNo);
    }

    public double getDoubleField(PersistenceCapable persistenceCapable, int i,
            double v) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getDoubleField(fmd.stateFieldNo);
    }

    public String getStringField(PersistenceCapable persistenceCapable, int i,
            String s) {
        FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getStringField(fmd.stateFieldNo);
    }

    public Object getObjectField(PersistenceCapable persistenceCapable,
            int i, Object o) {
        final FieldMetaData fmd = getFmd(i);
        doRead(fmd);
        return state.getObjectField(fmd.stateFieldNo, pc, emProxy, oid);
    }

    /**
     * Called on read access to an field. This method must ensure that the field is fetched from the server if the state
     * does not contain it.
     * @param fmd
     */
    private void doRead(FieldMetaData fmd) {
//        loadedFields[fmd.stateFieldNo] = true;
        if (!state.containsField(fmd.stateFieldNo)) {
            fetchState(fmd.fetchGroup);
        }
    }

    private void fetchState(FetchGroup fg) {
        emProxy.getEm().fetchState(this, fg);
    }

    public void setBooleanField(PersistenceCapable persistenceCapable, int i,
            boolean currentVal, boolean newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setBooleanField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setCharField(PersistenceCapable persistenceCapable, int i,
            char currentVal, char newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setCharField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setByteField(PersistenceCapable persistenceCapable, int i,
            byte currentVal, byte newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setByteField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setShortField(PersistenceCapable persistenceCapable, int i,
            short currentVal, short newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setShortField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setIntField(PersistenceCapable persistenceCapable, int i,
            int currentVal, int newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setIntField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setLongField(PersistenceCapable persistenceCapable, int i,
            long currentVal, long newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setLongField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setFloatField(PersistenceCapable persistenceCapable, int i,
            float currentVal, float newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setFloatField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setDoubleField(PersistenceCapable persistenceCapable, int i,
            double currentVal, double newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setDoubleField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setStringField(PersistenceCapable persistenceCapable, int i,
            String currentVal, String newVal) {
        emProxy.getEm().makeDirty(this, getFmd(i));
        state.setStringField(getFmd(i).stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    public void setObjectField(PersistenceCapable persistenceCapable, int i,
            Object currentVal, Object newVal) {
        FieldMetaData fmd = getFmd(i);
        emProxy.getEm().makeDirty(this, fmd);
        newVal = replaceScoField(fmd, newVal);
        state.setObjectField(fmd.stateFieldNo, newVal);
//        pc.jdoReplaceField(i);
    }

    private Object replaceScoField(FieldMetaData fmd, Object newVal) {
        if (fmd.scoField) {
            newVal = fmd.createSCO(emProxy, this, fmd, pc, newVal);
        }
        return newVal;
    }

    public void providedBooleanField(PersistenceCapable persistenceCapable,
                                     int i, boolean val) {
        state.setBooleanField(getFmd(i).stateFieldNo, val);
    }

    public void providedCharField(PersistenceCapable persistenceCapable, int i, char val) {
        state.setCharField(getFmd(i).stateFieldNo, val);
    }

    public void providedByteField(PersistenceCapable persistenceCapable, int i, byte val) {
        state.setByteField(getFmd(i).stateFieldNo, val);
    }

    public void providedShortField(PersistenceCapable persistenceCapable, int i, short val) {
        state.setShortField(getFmd(i).stateFieldNo, val);
    }

    public void providedIntField(PersistenceCapable persistenceCapable, int i, int val) {
        state.setIntField(getFmd(i).stateFieldNo, val);
    }

    public void providedLongField(PersistenceCapable persistenceCapable, int i, long val) {
        state.setLongField(getFmd(i).stateFieldNo, val);
    }

    public void providedFloatField(PersistenceCapable persistenceCapable, int i, float val) {
        state.setFloatField(getFmd(i).stateFieldNo, val);
    }

    public void providedDoubleField(PersistenceCapable persistenceCapable, int i, double val) {
        state.setDoubleField(getFmd(i).stateFieldNo, val);
    }

    public void providedStringField(PersistenceCapable persistenceCapable, int i, String val) {
        state.setStringField(getFmd(i).stateFieldNo, val);
    }

    public void providedObjectField(PersistenceCapable persistenceCapable, int i, Object val) {
        state.setObjectField(getFmd(i).stateFieldNo, replaceScoField(getFmd(i), val));
    }

    public boolean replacingBooleanField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getBooleanField(getFmd(i).stateFieldNo);
    }

    public char replacingCharField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getCharField(getFmd(i).stateFieldNo);
    }

    public byte replacingByteField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getByteField(getFmd(i).stateFieldNo);
    }

    public short replacingShortField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getShortField(getFmd(i).stateFieldNo);
    }

    public int replacingIntField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getIntField(getFmd(i).stateFieldNo);
    }

    public long replacingLongField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getLongField(getFmd(i).stateFieldNo);
    }

    public float replacingFloatField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getFloatField(getFmd(i).stateFieldNo);
    }

    public double replacingDoubleField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getDoubleField(getFmd(i).stateFieldNo);
    }

    public String replacingStringField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getStringField(getFmd(i).stateFieldNo);
    }

    public Object replacingObjectField(PersistenceCapable persistenceCapable, int i) {
        FieldMetaData fmd = getFmd(i);
//        loadedFields[fmd.stateFieldNo] = true;
        return state.getObjectField(getFmd(i).stateFieldNo, pc, emProxy, oid);
    }

    /**
     * Follow persistent fields that contain references to persistent instances.
     */
    public void persistReferences(com.versant.core.ejb.common.EntrySet mergeSet) {
        if (!state.isDirty()) return;
        FieldMetaData[] fmdsToPersist = cmd.managedFields;
        for (int i = 0; i < fmdsToPersist.length; i++) {
            FieldMetaData fmd = fmdsToPersist[i];
            if ((fmd.cascadeType & MDStatics.CASCADE_PERSIST) == 0) continue;
            if (!state.isDirty(fmd.stateFieldNo)) continue;
            switch (fmd.category) {
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    Object ref = state.getObjectField(fmd.stateFieldNo, pc,
                            emProxy, oid);
                    if (ref != null) mergeSet.add(ref);
                    break;
                case MDStatics.CATEGORY_COLLECTION:
                    if (fmd.elementTypeMetaData != null) {
                        Object val = state.getInternalObjectField(fmd.stateFieldNo);
                        if (val instanceof Collection) {
                            Collection col = (Collection) val;
                            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                                mergeSet.add(iterator.next());
                            }
                        }
                    }
            }
        }
    }

    public StateManagerImp mergeReferences(com.versant.core.ejb.common.EntrySet mergeSet) {
        FieldMetaData[] fmdsToPersist = cmd.managedFields;
        for (int i = 0; i < fmdsToPersist.length; i++) {
            FieldMetaData fmd = fmdsToPersist[i];
            if ((fmd.cascadeType & MDStatics.CASCADE_MERGE) == 0) continue;
            switch (fmd.category) {
                case MDStatics.CATEGORY_REF:
                case MDStatics.CATEGORY_POLYREF:
                    if (state.isDirty(fmd.stateFieldNo)) {
                        Object ref = state.getObjectField(fmd.stateFieldNo, pc,
                                emProxy, oid);
                        if (ref != null) {
                            mergeSet.add(ref);
                            state.setInternalObjectField(fmd.stateFieldNo,
                                    getEm().mergeInternal(ref, mergeSet).pc);
//                            loadedFields[fmd.stateFieldNo] = false;
                        }

                    }

            }
        }
        return this;
    }

    public void updateState(State stateVal) {
        this.state.updateNonFilled(stateVal);
    }

    public Object getVersion(PersistenceCapable persistenceCapable) {
        return null;  //todo jdo2, implement this method
    }

    public void providedLoadedFieldList(PersistenceCapable persistenceCapable, BitSet bitSet) {
        //todo jdo2, implement this method
    }

    public void providedModifiedFieldList(PersistenceCapable persistenceCapable, BitSet bitSet) {
        //todo jdo2, implement this method
    }

    public BitSet replacingLoadedFieldList(PersistenceCapable persistenceCapable, BitSet bitSet) {
        return null;  //todo jdo2, implement this method
    }

    public BitSet replacingModifiedFieldList(PersistenceCapable persistenceCapable, BitSet bitSet) {
        return null;  //todo jdo2, implement this method
    }

    public Object replacingObjectId(PersistenceCapable persistenceCapable, Object o) {
        return null;  //todo jdo2, implement this method
    }

    public Object replacingVersion(PersistenceCapable persistenceCapable, Object o) {
        return null;  //todo jdo2, implement this method
    }

	public Object[] replacingDetachedState(Detachable arg0, Object[] arg1) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}
}
