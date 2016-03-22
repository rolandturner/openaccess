
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

import com.versant.core.common.Debug;
import com.versant.core.common.Utils;
import com.versant.core.metadata.*;
import com.versant.core.common.*;
import com.versant.core.jdo.sco.VersantSimpleSCO;

import javax.jdo.InstanceCallbacks;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.BitSet;

import com.versant.core.common.BindingSupportImpl;



/**
 * JDO Genie State manager.
 */
public final class PCStateMan implements VersantStateManager
 {

    private static final int EVENT_ROLLBACK = 0;
    private static final int EVENT_COMMIT = 1;

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

    private boolean readInTx;

    private static DeletedState DELETED_STATE = new DeletedState();
    private static InitState INIT_STATE = new InitState();

    /**
     * The current values.
     */
    public State state;
    /**
     * This state is kept if concurrency checking is done by using changed checking.
     */
    private State origState;
    /**
     * This is used to rollback to.
     */
    private State beforeState;
    /**
     * This is used to carry the fields to store to the server. This must later
     * be replaced by an pool.
     */
    private State toStoreState;
    /**
     * This is used for stores that require that all fake and ref fields are
     * sent to the server for reads on secondary fields stored as SCOs in VDS.
     */
    private State forReadState;
    /**
     * The PersistenceCapable instance that is managed.
     */
    public PersistenceCapable pc;
    /**
     * The OID of the managed instance.
     */
    public OID oid;
    /**
     * The loadedFields of the managed instance.
     */
    private boolean[] loadedFields;
    /**
     * The classmetadata for the managed instance.
     */
    private ClassMetaData classMetaData;
    /**
     * field to replace the flags of the pc instance.
     */
    private byte jdoFlags = PersistenceCapable.LOAD_REQUIRED;
    /**
     * A mask field that specifies the current lifecycle.
     */
    private int stateM;
    /**
     * A flag that is set if this instance is to be evicted at commit.
     */
    public boolean toBeEvictedFlag;
    /**
     * If the pc instance is of type InstanceCallbacks then this will be an already casted
     * ref.
     */

    private InstanceCallbacks instanceCallbacks;




    public final QueryStateWrapper queryStateWrapper;

    private PMProxy pmProxy;
    private ModelMetaData jmd;

    /**
     * The wrapper for this instance in the managed cache.
     */
    public PMCacheEntry cacheEntry;

    private LocalPMCache jdoManagedCache;

    private final JDOImplHelper jdoImplHelper = JDOImplHelper.getInstance();
    /**
     * If this pc instance has been marked for deletion in this tx.
     */
    private boolean addedForDelete;

    // These are used by JdoGeniePersistenceManagerImp to form linked lists
    public PCStateMan prev;
    public PCStateMan next;
    public boolean inDirtyList;

    public boolean doChangeChecking;
    private boolean rfgLoaded;
    /**
     * This is to determine if the dfg has been loaded to the pc.
     */
    private boolean dfgLoaded;
    private int[] scoFieldArray;
    /**
     * If this sm has been prepared in the current commit cycle. This field must
     * be cleared after commit/rollback
     */
    private int preparedStatus;

    /**
     * If this is not null then this is an embedded sm.
     */
    public ClassMetaData owner;

    private VersantPersistenceManagerImp getPm() {
        return pmProxy.getRealPM();
    }

    public PMProxy getPmProxy() {
        return pmProxy;
    }

    public PCStateMan(LocalPMCache jdoManagedCache, ModelMetaData jmd,
            PMProxy perMan) {
        this.jdoManagedCache = jdoManagedCache;
        this.jmd = jmd;
        this.pmProxy = perMan;
        queryStateWrapper = new QueryStateWrapper(this, perMan);
    }

    private void init(PersistenceCapable pc, OID oid, ClassMetaData cmd) {

        if (pc instanceof InstanceCallbacks) {
            instanceCallbacks = (InstanceCallbacks)pc;
        }


        this.classMetaData = cmd;
        scoFieldArray = new int[classMetaData.scoFieldNos.length];
        queryStateWrapper.setCmd(cmd);
        this.pc = pc;
        this.oid = oid;

        origState = cmd.createState();
        origState.setClassMetaData(cmd);

        state = cmd.createState();
        state.setClassMetaData(cmd);
        loadedFields = new boolean[cmd.stateFields.length];
        doChangeChecking = classMetaData.changedOptimisticLocking;
    }

    private State createStateImp() {
        State state = classMetaData.createState();
        state.setClassMetaData(classMetaData);
        return state;
    }

    private State createToStoreState() {
        if (toStoreState == null) {
            toStoreState = classMetaData.createState();
            toStoreState.setClassMetaData(classMetaData);
        } else {
            toStoreState.clear();
        }
        return toStoreState;
    }

    /**
     * This initialises the pcStateObject when it is being pushed in from the server
     * as a by product of a query or navigation.
     */
    public void init(OID oid, ClassMetaData cmd, State aState,
            VersantPersistenceManagerImp rpm) {
        init(jdoImplHelper.newInstance(cmd.cls, rpm.createStateManagerProxy(this)),
                oid, cmd);
        oid.resolve(aState);
        state.updateNonFilled(aState);
        updated(rpm, false);
        maintainOrigState(aState, rpm);
        setLoadRequired();
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            state.copyFields(oid);
            pc.jdoReplaceFields(classMetaData.pkFieldNos);
        }
    }

    public EmbeddedStateManager createEmbeddedSM(FieldMetaData fmd) {
        EmbeddedStateManager embeddedSm = new EmbeddedStateManager(this, jmd,
                jdoImplHelper, fmd);
        embeddedSm.setLoadRequired();
        return embeddedSm;
    }

    public EmbeddedStateManager createEmbeddedSM(PersistenceCapable embeddedPC,
            FieldMetaData fmd) {
        EmbeddedStateManager embeddedSm = new EmbeddedStateManager(this,
                embeddedPC, jmd, fmd);
        embeddedPC.jdoReplaceStateManager(embeddedSm);
        //ask for all the data from the embedded instance
        embeddedPC.jdoProvideFields(embeddedSm.cmd.allManagedFieldNosArray);
        embeddedSm.setLoadRequired();
        return embeddedSm;
    }


    public void init(VersantDetachable d, OID oid, VersantPersistenceManagerImp rpm) {
        ClassMetaData cmd = oid.getAvailableClassMetaData();
        init(pc, oid, cmd);
        d.jdoReplaceStateManager(this);
        d.jdoProvideFields(cmd.allManagedFieldNosArray);
        d.jdoReplaceStateManager(null);
        pc = jdoImplHelper.newInstance(cmd.cls,
                rpm.createStateManagerProxy(this));
        stateM = STATE_P_NEW;
    }

    public ClassMetaData getClassMetaData() {
        return classMetaData;
    }

    public int getIndex() {
        return classMetaData.index;
    }

    private final void maintainOrigState(State toCopy,
            VersantPersistenceManagerImp rpm) {
        if (doChangeChecking) {
            origState.copyFieldsForOptimisticLocking(toCopy, rpm);
        } else {
            origState.copyOptimisticLockingField(toCopy);
        }
    }

    /**
     * This is to create a hollow sm for a getObjectById with validate = false.
     * Must the oid be resolved.
     *
     * @param oid
     */
    public void init(OID oid, VersantPersistenceManagerImp rpm) {
        if (!oid.isResolved() && oid.getAvailableClassMetaData().isInHierarchy()) {
            throw BindingSupportImpl.getInstance().internal("The oid '" + oid.toStringImp()
                    + "' is not resolved to a exact instance type");
        }
        ClassMetaData cmd = oid.getAvailableClassMetaData();
        init(jdoImplHelper.newInstance(cmd.cls, this), oid, cmd);
        oid.resolve(state);
        jdoManagedCache.createCacheKey(this);

        setLoadRequired();
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            state.copyFields(oid);
            pc.jdoReplaceFields(classMetaData.pkFieldNos);
            updated(rpm, false);
        } else {
            stateM = STATE_HOLLOW;
        }
    }

    /**
     * This is to init the sm for a new OID. Therefore either an P-New instance
     * or a transient instance.
     * <p/>
     * TODO must test a transient instance that refs other instances. What must happen to them.
     * Must they also propagate to transient.
     */
    public void init(PersistenceCapable pc, OID oid, boolean isTransactional) {
        if (Debug.DEBUG) {
            if (!oid.isNew()) {
                throw BindingSupportImpl.getInstance().internal(
                        "The oid must be of type 'new'");
            }
        }
        init(pc, oid, oid.getClassMetaData());
        try {
            if (isTransactional) {
                stateM = STATE_T_CLEAN;
            } else {
                stateM = STATE_P_NEW;
            }

            oid.resolve(state);
            //request all the fields from the pc instance
            pc.jdoProvideFields(classMetaData.allManagedFieldNosArray);
            state.addOneToManyInverseFieldsForL2Evict(getPm());
            //fill the real-oid if possible
            getRealOIDIfAppId();
            jdoManagedCache.createCacheKey(this);

            //convert all sco fields to be to sco instance and tell pc to reload them
            replaceSCOFields();

            jdoFlags = PersistenceCapable.READ_OK;
            pc.jdoReplaceFlags();
            dfgLoaded = true;
            setAllFieldsToLoaded();
        } catch (Exception e) {
            /**
             * Do clean up if exception occured.
             */
            try {
                getPm().removeTxStateObject(this);
            } catch (Exception e1) {
                //ignore
            }

            if (Debug.DEBUG) {
                Debug.ERR.println(e.getMessage());
                e.printStackTrace(Debug.ERR);
            }
            handleException(e);
        }
    }

    public OID getOID() {
        return oid;
    }

    public PersistenceCapable getPersistenceCapable() {
        return pc;
    }

    /**
     * This will return a internal oid created from the state if
     * - app id
     * - keygen == null
     * or state containsvalid pk fields
     */
    public OID getRealOIDIfAppId() {
        if (!oid.isNew()) return null;
        if (!classMetaData.postInsertKeyGenerator
                && classMetaData.identityType == MDStatics.IDENTITY_TYPE_APPLICATION
                && state.containsValidAppIdFields()) {
            NewObjectOID cNOid = (NewObjectOID)oid;
            if (cNOid.realOID != null) return cNOid.realOID;

            final OID rOid = classMetaData.createOID(true);
            state.copyKeyFields(rOid);

            /**
             * This is to keap the real oid from being gc'd.
             */
            cNOid.realOID = rOid;
            return rOid;
        }
        return null;
    }

    /**
     * This will initialise for transient managed use.
     *
     * @param pc
     * @param oid
     */
    public void initTransient(PersistenceCapable pc, OID oid) {
        if (Debug.DEBUG) {
            if (!oid.isNew()) {
                throw BindingSupportImpl.getInstance().internal(
                        "The oid must be of type 'new'");
            }
        }
        init(pc, oid, oid.getClassMetaData());
        stateM = STATE_T_CLEAN;

    }

    private void replaceSCOFields() {
        int count = state.replaceSCOFields(pc, pmProxy, scoFieldArray);
        for (int i = count - 1; i >= 0; i--) {
            pc.jdoReplaceField(scoFieldArray[i]);
        }
    }




    public void dump() {
        if (Debug.DEBUG) {
            Debug.OUT.println("\n\n<PCStateObject oid = " + oid.toSString()
                    + "\nstateM = " + stateM
                    + "\nstate = " + state
//                    + "\nbeforeState = " + beforeState
                    + "\norigState = " + origState
                    + "\nretainValues = " + getPm().isRetainValues()
                    + "\nOptimistic = " + getPm().isOptimistic()
                    + "\ntxActive = " + getPm().isActive()
                    + "###################### end #######################\n\n");
        }
    }

    public boolean isLoaded(PersistenceCapable pc, FieldMetaData fmd) {
        if (Debug.DEBUG) {
            checkDfgLoaded();
            if (!fmd.embeddedFakeField && dfgLoaded && fmd.isJDODefaultFetchGroup()
                    && !pmProxy.getRealPM().isInterceptDfgFieldAccess()) {

                throw BindingSupportImpl.getInstance().internal("Default fetch group " +
                        "fields interception is turned off, but a isLoaded " +
                        "call was still generated for it");

            }

            if ((jdoFlags == PersistenceCapable.READ_OK) && !dfgLoaded) {
                throw BindingSupportImpl.getInstance().internal(
                        "READ_OK is set but the dfg is not loaded.");
            }
        }

        checkTxDSReadOnPNonTx();
        return isLoadedImp(fmd.stateFieldNo);
    }

    public boolean isLoaded(PersistenceCapable pc, int field) {
        return isLoaded(pc, getFMD(field));

    }

    public void addToProcessList() {
        addToProcessList(getPm());
    }

    private void addToProcessList(VersantPersistenceManagerImp rpm) {
        //if no active tx then ignore
        if (!rpm.isActive()) return;
        pmProxy.getRealPM().getCache().addForProcessing(this);
        readInTx = true;
    }

    public boolean isLoadedImp(int field) {
        try {
            return loadedFields[field];
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    private void checkDfgLoaded() {
        if (dfgLoaded &&
                !state.containFields(classMetaData.dfgStateFieldNos)) {
            throw BindingSupportImpl.getInstance().internal("The default Fetch Group fields" +
                    " are supposed to be loaded to the pc instance, but the state" +
                    "does not contain it");
        }
    }

    /**
     * This is the same as isLoaded but will not trigger any state
     * transitions.
     */
    public boolean isLoadedInternal(PersistenceCapable pc, int field) {
        final FieldMetaData fmd = getFMD(field);
        if (fmd.category == MDStatics.CATEGORY_REF || fmd.category == MDStatics.CATEGORY_POLYREF) {
            return loadedFields[field];
        } else {
            return state.containsField(fmd.stateFieldNo);
        }
    }

    /**
     * This is to check if a user is doing a read in an dataStore tx on
     * a p-non-tx instance. Such an instance must be cleared of its state
     * and re-read from the store. It's state must then propagate as normal
     * to P-Clean.
     */
    private final void checkTxDSReadOnPNonTx() {
        if( isPNonTx() )
        {
            VersantPersistenceManagerImp pm = getPm();
            if ( pm.isActiveDS() || (pm.isActiveOptimistic() && isOutdatedInCurrentTxn(pm)) ) {
                changeToHollowState(true);
            }
        }
    }

    private boolean isOutdatedInCurrentTxn(VersantPersistenceManagerImp rpm) {
        return rpm.doRefreshPNTObjects( cacheEntry.txnCounter );
    }

    final void setLoaded(FieldMetaData fmd) {
        if (Debug.DEBUG) {
            if (!fmd.embedded && stateM != STATE_TRANSIENT
                    && !state.containsField(fmd.stateFieldNo)
                    && fmd.category != MDStatics.CATEGORY_TRANSACTIONAL) {
                throw BindingSupportImpl.getInstance().internal(
                        "The field " + fmd.name + " is not contained in the state");
            }
        }
        loadedFields[fmd.stateFieldNo] = true;
    }

    public void evict() {
        switch (stateM) {
            case STATE_P_CLEAN:
                changeToHollowState();
                cacheEntry.changeToRefType(jdoManagedCache.queue,
                        VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);
                break;
            case STATE_P_NON_TX:
                changeToHollowState();
                cacheEntry.changeToRefType(jdoManagedCache.queue,
                        VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);
                break;
            default:
                toBeEvictedFlag = true;
        }
    }

    public void retrieveReferencesHollow(VersantPersistenceManagerImp rpm) {
        loadAllPersistentFieldsToPCReferencesHollow(rpm);
    }

    /**
     * This will load all the managed-persistent fields to the PC instance.
     * Transactional fields are ignored.
     */
    private void loadAllPersistentFieldsToPCReferencesHollow(VersantPersistenceManagerImp rpm) {
        // A new instance does not have any lazy fields that need fetching.
        if (isNew()) return;
        FetchGroup retrieveFG = classMetaData.getFetchGroup(
                FetchGroup.RETRIEVE_REFERENCES_HOLLOW_NAME);
        if (!state.containsFetchGroup(retrieveFG)) {
            rpm.getState(oid,
                    retrieveFG.sendFieldsOnFetch ? getForReadState() : null,
                    retrieveFG.index, null, -1, false);
        }

        for (int i = 0; i < classMetaData.managedFields.length; i++) {
            FieldMetaData fmd = classMetaData.managedFields[i];
            if (fmd.persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
                if (! fmd.isEmbeddedRef())
                    pc.jdoReplaceField(fmd.managedFieldNo);
            }

        }
        callJDOPostLoad(rpm.getListeners());
        dfgLoaded = true;   //this is true, even if the referenced objects are hollow
    }
    
    /**
     * This is an recursive operation. It will retrieve everything that is
     * reachable.
     */
    public void retrieve(VersantPersistenceManagerImp rpm) {
        loadAllPersistentFieldsToPC(rpm);
        state.retrieve(rpm);
    }

    /**
     * This will load all the managed-persistent fields to the PC instance.
     * Transactional fields are ignored.
     */
    private void loadAllPersistentFieldsToPC(VersantPersistenceManagerImp rpm) {
        // A new instance does not have any lazy fields that need fetching.
        if (isNew()) return;
        FetchGroup retrieveFG = classMetaData.getFetchGroup(
                FetchGroup.RETRIEVE_NAME);
        if (!state.containsFetchGroup(retrieveFG)) {
            rpm.getState(oid,
                    retrieveFG.sendFieldsOnFetch ? getForReadState() : null,
                    retrieveFG.index, null, -1, false);
        }

        for (int i = 0; i < classMetaData.allManagedFieldNosArray.length; i++) {
            FieldMetaData fmd = classMetaData.managedFields[classMetaData.allManagedFieldNosArray[i]];
            if (fmd.persistenceModifier == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) {
                pc.jdoReplaceField(fmd.managedFieldNo);
            }

        }
        callJDOPostLoad(rpm.getListeners());
        dfgLoaded = true;
    }

    /**
     * Return a state containing the current values of all fake fields. If
     * the instance is hollow then null is returned.
     */
    private State getForReadState() {
        if (state.isEmpty()) return null;
        if (forReadState == null) forReadState = classMetaData.createState();
        if (forReadState.isEmpty()) state.fillForRead(forReadState, getPm());
        return forReadState;
    }

    public void deletePersistent() {
        if (isDeleted()) return;
        if (isTransientManaged()) {
            throw BindingSupportImpl.getInstance().invalidOperation("The deletion of a transient " +
                    "managed instance is not allowed.");
        }

        // do notification if required
        final VersantPersistenceManagerImp pm = getPm();
        if (!isDirty() && classMetaData.notifyDataStoreOnDirtyOrDelete
                && pm.isActiveDS()) {
            pm.getStorageManager().notifyDirty(oid);
        }
        boolean hasDeleteListeners = false;
        int index = -1;
        LifecycleListenerManager[] listeners = pm.getListeners();
        if (pm.hasDeleteListeners()) {
            index = state.getClassIndex();
            if (listeners[index] != null) {
                hasDeleteListeners = listeners[index].firePreDelete(pc);
            }
        }

        // invoke jdoPreDelete

        if (instanceCallbacks != null) {
            instanceCallbacks.jdoPreDelete();
        }
/*END_JVAVONLY*/


        //the loaded fields is reset for a deleted instance to force it to try and reread their
        //fields and so catch illegal read/write ops on the instance.
        resetLoadedFields();
        setLoadRequired();

        rfgLoaded = false;
        dfgLoaded = false;

        clearMtMCollections();

        // mark us as deleted
        stateM |= MASK_DELETE_TX_DIRTY;
        pm.addTxStateObject(this);
        addToProcessList(pm);

        deleteDependents();

        // Make sure the fake fields with LOIDs for secondary fields are
        // sent to the server if using VDS and the instance was clean
        // before delete and has secondary fields. The forRead state is
        // included in the DeletePacket if the tx commits.
        if (jmd.sendStateOnDelete && classMetaData.hasSecondaryFields && !isDirty()) {
            forReadState = getForReadState();
        } else {
            forReadState = null;
        }

        origState.clear();
        state.clear();
        if (beforeState != null) beforeState.clear();

        //replace the state with DELETED_STATE. This will ensure that user
        //exception is thrown on field access.
        state = DELETED_STATE;
        if (hasDeleteListeners) {
            listeners[index].firePostDelete(pc);
        }
    }

    /**
     * This is invoked from deletePersistent to delete all the dependent
     * references of this instance.
     */
    private void deleteDependents() {
        // make sure that all dependent fields are in state if there are any
        FetchGroup dep = classMetaData.depFetchGroup;
        if (dep == null) return;
        if (!oid.isNew() && !state.containsFetchGroup(dep)) {
            getPm().getState(oid,
                    dep.sendFieldsOnFetch ? getForReadState() : null,
                    dep.index, null, -1, false);
        }

        // delete all objects referenced by dependent fields
        for (; dep != null; dep = dep.superFetchGroup) {
            FetchGroupField[] fields = dep.fields;
            int len = fields.length;
            for (int j = 0; j < len; j++) {
                FetchGroupField field = fields[j];
                FieldMetaData fmd = field.fmd;
                switch (fmd.category) {
                    case MDStatics.CATEGORY_ARRAY:
                        followArray(fmd);
                        break;
                    case MDStatics.CATEGORY_COLLECTION:
                        followCollection(fmd);
                        break;
                    case MDStatics.CATEGORY_MAP:
                        followMap(fmd);
                        break;
                    case MDStatics.CATEGORY_POLYREF:
                    case MDStatics.CATEGORY_REF:
                        followRef(fmd);
                        break;
                }
            }
        }
    }

    public void collectReachable(String fetchGroup, Collection result) {
		FetchGroup fg = classMetaData.getFetchGroup(fetchGroup);

        if (fg == null)
			return;

		boolean isDFG = (fg == classMetaData.fetchGroups[0]);
		boolean deep = fetchGroup.equals(FetchGroup.REF_NAME);
		boolean depend = fetchGroup.equals(FetchGroup.DEP_NAME);

        // make sure everything required has been fetched
        if (!oid.isNew() && !state.containsFetchGroup(fg) && !deep) {
			// todo: for FetchGroup.REF_NAME, a NPE is thrown for
			// collection fields, because FetchGroup.nextFetchGroup is null
            getPm().getState(oid, fg.sendFieldsOnFetch ? getForReadState() : null,
						 fg.index, null, -1, false);
        }

        // collect all referenced objects
        for (; fg != null; fg = fg.superFetchGroup) {
            FetchGroupField[] fields = fg.fields;
            int len = fields.length;
            for (int i = 0; i < len; i++) {
				FetchGroupField fgField = fields[i];
				if (isDFG && !fgField.fmd.isJDODefaultFetchGroup())
					continue;

				int fieldNo = fgField.fmd.stateFieldNo;
				int managedFieldNo = fgField.fmd.managedFieldNo;
                if (managedFieldNo < 0)
                    continue;

				if (!deep && !oid.isNew())
					pc.jdoReplaceField(managedFieldNo);

				String nextFetchGroup = fgField.nextFetchGroup==null?
					null:fgField.nextFetchGroup.name;
				String nextKeyFetchGroup = fgField.nextKeyFetchGroup==null?
					null:fgField.nextKeyFetchGroup.name;
				if (deep) // nextFetchGroup is null
					nextFetchGroup = nextKeyFetchGroup = FetchGroup.REF_NAME;
				else if (depend) // nextFetchGroup is default
					nextFetchGroup = nextKeyFetchGroup = FetchGroup.DEP_NAME;
                switch (fgField.fmd.category) {
                    case MDStatics.CATEGORY_ARRAY:
						if (deep && !oid.isNew())
							doRead(fields[i].fmd); // todo: remove
						Object[] arr = (Object[])
							state.getObjectField(fieldNo, pc, pmProxy, oid);
						if (arr == null) continue;
						for (int j=0; j<arr.length; j++){
							Object o = arr[j];
							if (o instanceof PersistenceCapable)
								result.add(new Object[] {o, nextFetchGroup});
						}
                        break;
                    case MDStatics.CATEGORY_COLLECTION:
						if (deep && !oid.isNew())
							doRead(fields[i].fmd); // todo: remove
						Collection c = (Collection)
							state.getObjectField(fieldNo, pc, pmProxy, oid);
						if (c == null) continue;
						for (Iterator it = c.iterator(); it.hasNext();) {
							Object o = it.next();
							if (o instanceof PersistenceCapable)
								result.add(new Object[] {o, nextFetchGroup});
						}
                        break;
                    case MDStatics.CATEGORY_MAP:
						if (deep && !oid.isNew())
							doRead(fields[i].fmd); // todo:remove
						Map m = (Map)
							state.getObjectField(fieldNo, pc, pmProxy, oid);
						if (m == null) continue;
						for (Iterator it = m.entrySet().iterator();
							 it.hasNext();) {
							Map.Entry e = (Map.Entry)it.next();
							Object o = e.getKey();
							if (o instanceof PersistenceCapable) {
								result.add(new Object[] {o, nextKeyFetchGroup});
							}
							o = e.getValue();
							if (o instanceof PersistenceCapable) {
								result.add(new Object[] {o, nextFetchGroup});
							}
						}
                        break;
                    case MDStatics.CATEGORY_POLYREF:
                    case MDStatics.CATEGORY_REF:
						if (deep && !oid.isNew())
							doRead(fields[i].fmd); // todo: remove
						Object o = state.getObjectField(fieldNo, pc, pmProxy, oid);
						if (o instanceof PersistenceCapable)
							result.add(new Object[] {o, nextFetchGroup});
                        break;
                }
            }
        }
    }

    /**
     * Clear any many-to-many managed collections. This makes sure we
     * are removed from the other side of any of these before we are
     * deleted.
     */
    private void clearMtMCollections() {
        FetchGroup mm = classMetaData.managedManyToManyFetchGroup;
        if (mm != null) {

            // make sure the collections have been fetched
            if (!oid.isNew() && !state.containsFetchGroup(mm)) {
                getPm().getState(oid,
                        mm.sendFieldsOnFetch ? getForReadState() : null,
                        mm.index, null, -1, false);
            }

            // now clear all of them skipping non-collections (e.g. version)
            for (; mm != null; mm = mm.superFetchGroup) {
                FetchGroupField[] fields = mm.fields;
                int len = fields.length;
                for (int j = 0; j < len; j++) {
                    FetchGroupField field = fields[j];
                    FieldMetaData fmd = field.fmd;
                    if (fmd.category == MDStatics.CATEGORY_COLLECTION) {
                        Collection c = (Collection)state.getObjectField(
                                fmd.stateFieldNo, pc, pmProxy, oid);
                        if (c != null) c.clear();
                    }
                }
            }
        }
    }

    private void followRef(FieldMetaData fmd) {
        Object o = state.getObjectField(fmd.stateFieldNo, pc, pmProxy, oid);
        if (o != null) deletePersistent(o);
    }

    private void followCollection(FieldMetaData fmd) {
        Collection c = (Collection)state.getObjectField(fmd.stateFieldNo, pc,
                pmProxy, oid);
        if (c == null) return;
        for (Iterator i = c.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o != null) deletePersistent(o);
        }
    }

    private void followArray(FieldMetaData fmd) {
        Object[] arr = (Object[])state.getObjectField(fmd.stateFieldNo, pc,
													  pmProxy, oid);
        if (arr == null) return;
        for (int i=0; i<arr.length; i++) {
            Object o = arr[i];
            if (o != null) deletePersistent(o);
        }
    }


    private void followMap(FieldMetaData fmd) {
        Map m = (Map)state.getObjectField(fmd.stateFieldNo, pc, pmProxy, oid);
        if (m == null) return;
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            if (fmd.dependentKeys) deletePersistent(e.getKey());
            if (fmd.dependentValues) {
                Object o = e.getValue();
                if (o != null) deletePersistent(o);
            }
        }
    }

    /**
     * This is used when following dependent fields on delete. It avoids
     * all the repeat checks done by perMan.deletePersistent(o).
     */
    private void deletePersistent(Object o) {
        getPm().getInternalSM((PersistenceCapable)o).deletePersistent();
    }

    /**
     * The instance is only cleared of object references and not the primitives.
     * The loaded fields and the JDO_FLAGS is reset to force the pc to ask the sm for new values.
     * <p/>
     * For application identity the state will not transition to hollow as the pk
     * fields must remain.
     */
    private final void changeToHollowState(boolean clearBeforeState) {
        if (state == null) return;
        if (isHollow()) return;
        boolean hasClearListeners = false;
        int index = -1;
        LifecycleListenerManager[] listeners = getPm().getListeners();

        // add jdoPreClear
        if (listeners != null &&
                listeners[listeners.length -1].hasClearListeners()) {
            index = state.getClassIndex();
            if (listeners[index] != null) {
                hasClearListeners = listeners[index].firePreClear(pc);
            }
        }

        if (instanceCallbacks != null) {
            instanceCallbacks.jdoPreClear();
        }

        stateM = STATE_HOLLOW;

        state.unmanageSCOFields();
        state.clear();
        State tmpState = state;

        state = INIT_STATE;
        //ask pc to replace only the instances that hold refs to other pc instances
        //this helps to gc instances quicker
        pc.jdoReplaceFields(classMetaData.absPCTypeFields);
        state = tmpState;

        origState.clear();
        if (clearBeforeState && beforeState != null) {
            beforeState.clear();
        }

        setLoadRequired();
        resetLoadedFields();
        rfgLoaded = false;
        dfgLoaded = false;

        //this check is already contained in replaceApplicationPKFields()
        //if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            replaceApplicationPKFields();
        //}

        if (forReadState != null) forReadState.clearFilledFlags();

        if (hasClearListeners) listeners[index].firePostClear(pc);

        if (Debug.DEBUG) {
            if (!isHollow() && classMetaData.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) {
                if (Debug.DEBUG) {
                    Debug.OUT.println("isEmpty = " + state.isEmpty());
                }
                dump();
                throw BindingSupportImpl.getInstance().internal(
                        "The instance is not hollow");
            }
        }
    }

    /**
     * The instance is not cleared of its values. The loaded fields and the JDO_FLAGS is
     * reset to force the pc to ask the sm for new values.
     */
    public void changeToHollowState() {
        changeToHollowState(false);
    }

    public void rollback() {
        if (isTx()) {
            rollbackImp();
        } else if (readInTx) {
            changeToHollowState(true);
        }
        clearTxFlags();
    }

    public void commit(VersantPersistenceManagerImp pm) {
        if (isTx()) {
            commitImp(pm);
        } else if (readInTx) {
            changeToHollowState(true);
            readInTx = false;
        }
        clearTxFlags();
    }

    //cr: read access to pk fields is not mediated, so it should
    //not be necessary here to take special care for pk fields
    public final void resetLoadedFields() {
        final boolean[] lfs = loadedFields;
        for (int i = lfs.length - 1; i >= 0; i--) {
            lfs[i] = false;
        }
    }  
    
    //also see changeToHollowState, called from LocalPMCache
    public final void reset()
    {
        resetLoadedFields();
        setLoadRequired();

        rfgLoaded = false;
        dfgLoaded = false;  
        
        replaceApplicationPKFields();             
    }

    private void setAllFieldsToLoaded() {
        final boolean[] lfs = loadedFields;
        for (int i = lfs.length - 1; i >= 0; i--) {
            lfs[i] = true;
        }
    }

    private final void changeToTransient(int event) {
        stateM = STATE_TRANSIENT;
        if (event == EVENT_ROLLBACK) {
            if (getPm().isRestoreValues() && beforeState != null) {
                state.updateFrom(beforeState);
            }
            pc.jdoReplaceFields(classMetaData.allManagedFieldNosArray);
        } else if (event == EVENT_COMMIT) {
            State tmpState = state;
            state = INIT_STATE;
            pc.jdoReplaceFields(classMetaData.allManagedFieldNosArray);
            state = tmpState;
        }
        unManage();
    }

    /**
     * Must ensure that all resources is clean up.
     */
    private final void unManage() {
        jdoManagedCache.remove(this);
        setSMToNull();
        state.unmanageSCOFields();
        pc = null;
        oid = null;
        state = null;
        beforeState = null;
        origState = null;
        toStoreState = null;
    }

    private final void setSMToNull() {
        /**
         * change state to transient so that call back to replacingStateManager will return null.
         * and remove it from the txObjects list.
         */
        stateM = STATE_TRANSIENT;
        pc.jdoReplaceStateManager(null);
    }

    /**
     * Check if the instance is in a managed transient state.
     */
    public boolean isTransientManaged() {
        return (((stateM ^ STATE_T_CLEAN) == 0) || ((stateM ^ STATE_T_DIRTY) == 0));
    }

    public boolean isPersistentNew() {
        return ((stateM ^ STATE_P_NEW) == 0);
    }

    public void loadFetchGroup(String name) {
        if (oid.isNew()) {
            return;
        }
        FetchGroup fg = classMetaData.getFetchGroup(name);
        if (fg == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "fetch group '" + name + "' is not defined");
        }
        if (!state.containsFetchGroup(fg)) {
            getPm().getState(this.oid,
                    fg.sendFieldsOnFetch ? getForReadState() : null,
                    fg.index, null, -1, false);
        }
    }

    /**
     * This is here to do any preparation work before the field can be read.
     * It will check if the fetchGroup for the field is filled in on the state. If not
     * then the required values must be fetched from the server.
     * <p/>
     * Any state changes must also be done.
     * <p/>
     * The returned container reference must be kept until all the required
     * State instances in the local cache have been referenced or they might
     * be GCed. This is important for collections of PC and other fields that
     * involve fetching State instances and loading them into the local cache.
     * They are only hard referenced when the SCO instance has been created.
     */
    private StatesReturned doRead(FieldMetaData fmd) {
        if (Debug.DEBUG) {
            if (isTransientManaged()) {
                throw BindingSupportImpl.getInstance().internal(
                        "A transactional instance must not call this");
            }
        }
		VersantPersistenceManagerImp rpm = getPm();
		rpm.checkNonTxRead();

		addToProcessList(rpm);

        if (fmd.isEmbeddedRef()) {
            return null;
        }

        StatesReturned container = null;
        if (!state.containsField(fmd.stateFieldNo)) {
            if (Debug.DEBUG) {
                if (oid.isNew()) {
                    throw BindingSupportImpl.getInstance().internal(
                            "A new OID is not supposed to reach here");
                }
                Debug.OUT.println("\n\n>>>>>>>>>>>The field not contained: field = " + fmd.managedFieldNo
                        + " fieldName = " + fmd.name
                        + " for " + classMetaData.qname);
            }

            FetchGroup fg = fmd.fetchGroup;
            container = rpm.getState(this.oid,
                    fg.sendFieldsOnFetch ? getForReadState() : null,
                    fg.index, fmd, -1, false);
            if (Debug.DEBUG) {
                if (!state.containsFetchGroup(
                        fmd.fetchGroup)) {
                    System.out.println("bad state:\n" + state);
                    throw BindingSupportImpl.getInstance().internal(
                            "State does not contain the requested fg");
                }
            }
        }

        if (!fmd.embeddedFakeField) {
            if (fmd.isJDODefaultFetchGroup()) {
                //replace non loaded dfg fields
                if (state.containFields(classMetaData.dfgStateFieldNos)) {
                    loadDFGIntoPC(rpm);
                }
            } else {
                pc.jdoReplaceField(fmd.managedFieldNo);
                setLoaded(fmd);
                updated(rpm, true);
            }
        }

        if (!oid.isNew()) {
            if (!(doChangeChecking || rfgLoaded)) {
                loadRequiredFetchGroup();
            }
        }

        return container;
    }

    /**
     * Loads the required fg into the state if not already done.
     */
    private void loadRequiredFetchGroup() {
        if (rfgLoaded) return;
        final FetchGroup reqFetchGroup = classMetaData.reqFetchGroup;
        if (reqFetchGroup != null && !state.containsFetchGroup(reqFetchGroup)) {
            getPm().getState(this.oid, null, reqFetchGroup.index, null, -1,
                    false);
        }
        rfgLoaded = true;
    }

    /**
     * This gets called from the enhanced pc instances. This is only for
     * Application identity key fields. It gives us a chance to
     * fill the field with a valid keygened value.
     */
    public void fillNewAppPKField(int fieldNo) {
        if (oid.isNew()) {
            NewObjectOID newObjectOID = (NewObjectOID)oid;
            if (newObjectOID.realOID == null) {
                if (classMetaData.postInsertKeyGenerator) {
                    getPm().flushRetainState();
                } else {
                    newObjectOID.realOID =
                            getPm().getStorageManager().createOID(classMetaData);
                    newObjectOID.realOID.getClassMetaData();
                    state.copyFields(newObjectOID.realOID);
                    pc.jdoReplaceFields(classMetaData.pkFieldNos);
                }
            }
        }
    }

    private FieldMetaData getFMD(int fieldNo) {
        return classMetaData.managedFields[fieldNo];
    }

    /**
     * This is here to do any prep. work for a write operation. It must ensure that if a field
     * is written to but is not currently loaded and it is using changed checking that it is first loaded
     * from the server.
     */
    private final void doWrite(FieldMetaData fmd, boolean mustLoad) {
        VersantPersistenceManagerImp pm = this.getPm();
        //if this is a transactional field and we should not need to go to the db
        if (fmd.category == MDStatics.CATEGORY_TRANSACTIONAL) {
            if (pm.isActive()) makeInternalDirty();
            return;
        }
        pm.checkNonTxWrite();

        if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_APPLICATION
                && fmd.primaryKey) {
            throw BindingSupportImpl.getInstance().unsupported(
                    "Change of identity is not suppoted");
        }

        // if we need to notify the server before we go dirty for the first
        // time do so now
        if (classMetaData.notifyDataStoreOnDirtyOrDelete && !isDirty()
                && pm.isActiveDS()) {
            pm.getStorageManager().notifyDirty(oid);
        }

        // Check for a write operation that will result in a change from p-non-tx to p-dirty.
        // If so then a snapshot of the current state must be taken and the state reloaded from
        // the store.
        // The current state must only be preserved if restoreValues is set to true.
        // If retainValues is set to false then the instance will always be hollow after a commit
        // or a rollback and therefore it overrides restoreValues.
        if (isPNonTx() && pm.isActive()) {
            if (beforeState != null) {
                beforeState.clear();
                if (pm.isRestoreValues()) beforeState.updateFrom(state);
            }
            // If is is an dataStore tx then the state must be cleared and reloaded from the
            // store. This is done to ensure that the state is in sync with the
            // dataStore.
            if (!pm.isOptimistic()) changeToHollowState(false);
        }

        // Make sure that the state contains required fields before doing the
        // write. For JDBC this will ensure that the optimisic locking field
        // (if any) is loaded. For VDS this will load all fields.
        if (!oid.isNew()) {
            if (mustLoad && !state.containsField(fmd.stateFieldNo)) {
                FetchGroup fg = fmd.fetchGroup;
                pm.getState(this.oid,
                        fg.sendFieldsOnFetch ? getForReadState() : null,
                        fg.index, fmd, -1, false);
            } else if (!(doChangeChecking || rfgLoaded)) {
                loadRequiredFetchGroup();
            } else if (doChangeChecking && !state.containsField(fmd.stateFieldNo)) {
                FetchGroup fg = fmd.fetchGroup;
                pm.getState(this.oid,
                        fg.sendFieldsOnFetch ? getForReadState() : null,
                        fg.index, fmd, -1, false);
            }
        }

        if (getPm().isActive()) {
            if (!isTx())
                updateDfgFieldMediation();
            stateM |= MASK_TX;
            stateM |= MASK_DIRTY;
            pm.addTxStateObject(this);
            addToProcessList(pm);
        }
        loadedFields[fmd.stateFieldNo] = false;
    }

    private void makeInternalDirty() {
        stateM |= MASK_TX;
        stateM |= MASK_DIRTY;
        VersantPersistenceManagerImp pm = getPm();
        pm.addTxStateObject(this);
        addToProcessList(pm);
    }

    /**
     * This will be called when ever the state has been updated as result of a
     * query/navigation that brought extra info back for this state.
     * This method will ensure that the proper state changes takes place.
     */
    final void updated(VersantPersistenceManagerImp rpm,
            boolean addToProccessList) {
        if (rpm.isActive()) {
            if (rpm.isOptimistic()) {
                /**
                 * Must change to pers-non-tx
                 * eg. This will change a hollow instance to p-non-tx
                 */
                stateM |= MASK_PERSISTENT;
            } else {
                if (!isTx()) updateDfgFieldMediation();
                stateM |= MASK_TX;
                stateM |= MASK_PERSISTENT;
                rpm.addTxStateObject(this);
            }

            if (addToProccessList) {
                addToProcessList(rpm);
            }
        } else {
            stateM |= MASK_PERSISTENT;
        }
    }

    /**
     * This is a new State that was received from the server via indirect means.
     * This state must be updated with the supplied state. Any state changes nec must
     * also be done.
     *
     * @param suppliedState
     */
    public PCStateMan updateWith(State suppliedState,
            VersantPersistenceManagerImp pm, boolean overWrite) {
        if (overWrite) {
            changeToHollowState();
        }
        updated(pm, true);
        if (suppliedState != state) {
            state.updateNonFilled(suppliedState);
            if (doChangeChecking || origState.isEmpty()) {
                maintainOrigState(suppliedState, pm);
            }
        }
        return this;
    }

    public boolean isTx() {
        return ((stateM & MASK_TX) != 0);
    }

    public boolean readInTx(boolean strict) {
        if (strict) return isTx();
        return readInTx || isTx();
    }

    public boolean isDirty() {
        return ((stateM & MASK_DIRTY) != 0);
    }

    private final boolean isNew() {
        return ((stateM & MASK_NEW) != 0);
    }

    public boolean isHollow() {
        return (state.isEmpty() && ((stateM ^ STATE_HOLLOW) == 0));
    }

    private boolean isDeleted() {
        return (stateM & MASK_DELETE) != 0;
    }

    public boolean isPNonTx() {
        return (((stateM ^ STATE_P_NON_TX) == 0) && !state.isEmpty());
    }

    public boolean isPClean() {
        return (!state.isDirty() && ((stateM ^ STATE_P_CLEAN) == 0));
    }

    public boolean isPNew() {
        return ((stateM ^ STATE_P_NEW) == 0);
    }

    public boolean isPNewDeleted() {
        return ((stateM ^ STATE_P_NEW_DEL) == 0);
    }

    public boolean isPDeleted() {
        return ((stateM ^ STATE_P_DEL) == 0);
    }

    public boolean isTClean() {
        return ((stateM ^ STATE_T_CLEAN) == 0);
    }

    public boolean isTDirty() {
        return ((stateM ^ STATE_T_DIRTY) == 0);
    }

    public boolean isPDirty() {
        return (state.isDirty() && ((stateM ^ STATE_P_DIRTY) == 0));
    }

    /*
    private String stateMtoString() {
        switch (stateM) {
            case STATE_TRANSIENT:
                return "STATE_TRANSIENT";
            case STATE_T_CLEAN:
                return "STATE_T_CLEAN";
            case STATE_T_DIRTY:
                return "STATE_T_DIRTY";
            case STATE_HOLLOW:
                return "STATE_HOLLOW";
            case STATE_P_CLEAN:
                return "STATE_P_CLEAN";
            case STATE_P_DIRTY:
                return "STATE_P_DIRTY";
            case STATE_P_DEL:
                return "STATE_P_DEL";
            case STATE_P_NEW:
                return "STATE_P_NEW";
            case STATE_P_NEW_DEL:
                return "STATE_P_NEW_DEL";
        }
        return "UNKNOWN(" + stateM + ")";
    }
    */

    private void rollbackImp() {
        try {
            switch (stateM) {
                case STATE_TRANSIENT:
                    break;
                case STATE_T_CLEAN:
                    break;
                case STATE_T_DIRTY:
                    if (beforeState != null) {
                        State s = state;
                        state = beforeState;
                        beforeState = s;
                        state.updateNonFilled(beforeState);
                        beforeState.clear();
                    }
                    pc.jdoReplaceFields(classMetaData.allManagedFieldNosArray);
                    if (forReadState != null) forReadState.clearFilledFlags();
                    origState.clear();
                    stateM = STATE_T_CLEAN;
                    break;
                case STATE_HOLLOW:
                    break;
                case STATE_P_CLEAN:
                    if (getPm().isRestoreValues()) {
                        stateM = STATE_P_NON_TX;
                    } else {
                        changeToHollowState(true);
                    }
                    break;
                case STATE_P_DEL:
                    //the state was replaced with DELETED_STATE so create a new one
                    state = createStateImp();
                    changeToHollowState(true);
                    break;
                case STATE_P_DIRTY:
                    if (getPm().isRestoreValues()) {
                        stateM = STATE_P_NON_TX;
                        state.clearDirtyFields();
                        if (beforeState != null) {
                            beforeState.clearSCOFields();
                            state.updateFrom(beforeState);
                            beforeState.clear();
                        }
                        state.clearSCOFields();
                        if (forReadState != null) forReadState.clearFilledFlags();
                        setLoadRequired();
                        resetLoadedFields();

                        //there is no read interrogation for tx transient fields and therefore
                        //they must be replaced now.
                        pc.jdoReplaceFields(classMetaData.txfieldManagedFieldNos);

                        rfgLoaded = false;
                        dfgLoaded = false;
                    } else {
                        if (beforeState != null) {
                            State tmpState = state;
                            state = beforeState;
                            pc.jdoReplaceFields(classMetaData.txfieldManagedFieldNos);
                            state = tmpState;
                        }
                        changeToHollowState(true);
                    }
                    break;
                case STATE_P_NEW:
                    changeToTransient(EVENT_ROLLBACK);
                    break;
                case STATE_P_NEW_DEL:
                    state = createStateImp();
                    changeToTransient(EVENT_ROLLBACK);
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "The state is unreachable");
            }
        } finally {
            if (toStoreState != null) toStoreState.clear();
            clearTxFlags();
        }
    }

    /**
     * This will load the defaulFetchGroup fields into the state and in
     * the mananged instance. This is only called to change from hollow.
     */
    public void loadDfgFromHollow() {
        if (Debug.DEBUG) {
            if (!isHollow()) {
                throw BindingSupportImpl.getInstance().internal(
                        "This is only allowed to be called on hollow instances");
            }
        }
        VersantPersistenceManagerImp pm = getPm();
        pm.getState(this.oid, null, 0, null, -1, false);
        loadDFGIntoPC(pm);
    }

    /**
     * This will load all the dfg fields into the PC instance.
     */
    public void loadDFGIntoPC(VersantPersistenceManagerImp rpm) {
        if (dfgLoaded) {
            if (Debug.DEBUG) checkDfgLoaded();
            return;
        }
        pc.jdoReplaceFields(classMetaData.dfgAbsFieldNos);
        updated(rpm, true);
        callJDOPostLoad(rpm.getListeners());

        /**
         * Why only if it is transactional ??
         *
         * This is not set because in the case of a datastore tx with non-tx
         * read's allowed, and then setting the read ok flag will cause it not
         * to ask the sm for the next field access. This means if a ds tx starts
         * and the fields is accessed again that the sm will not reload the fields
         * from the db.
         */

        //todo must also check if this is a subclass of a horizontal class
        // where dfg fields have been changed
        if (classMetaData.horizontalCMD == null && (isTx() || !rpm.isInterceptDfgFieldAccess())) {
            jdoFlags = PersistenceCapable.READ_OK;
            pc.jdoReplaceFlags();
        }
        dfgLoaded = true;
        if (Debug.DEBUG) checkDfgLoaded();
    }

    private void callJDOPostLoad(LifecycleListenerManager[] listeners) {
        if (!dfgLoaded) {
            if (instanceCallbacks != null) {

                instanceCallbacks.jdoPostLoad();


            }
            if (listeners != null &&
                    listeners[listeners.length - 1].hasLoadListeners()) {
                int index = state.getClassIndex();
                if (listeners[index] != null) {
                    listeners[index].firePostLoad(pc);
                }
            }
        }
    }

    private boolean callJdoPreDirty(){
        if (isPClean()) {
            if (getPm().hasDirtyListeners()) {
                LifecycleListenerManager[] listeners = getPm().getListeners();
                int index = state.getClassIndex();
                if (listeners[index] != null) {
                    return listeners[index].firePreDirty(pc);
                }
            }
        }
        return false;
    }

    private void callJdoPostDirty() {
        getPm().getListeners()[state.getClassIndex()].firePostDirty(pc);
    }

    /**
     * Utils method to reset the flag of the pc instance to indicate that fields
     * must be reloaded.
     */
    void setLoadRequired() {
        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
        pc.jdoReplaceFlags();
    }
    
    void setLoadRequiredIfNeeded() {
        //LOAD_REQUIRED == 1, READ_OK == -1, READ_WRITE_OK == 0
        if( jdoFlags < PersistenceCapable.LOAD_REQUIRED )
        {
            jdoFlags = PersistenceCapable.LOAD_REQUIRED;
            pc.jdoReplaceFlags();
        }
    }    

    public void setInterceptDfgFieldAccess(boolean on) {
        if (isPNonTx()) {
            if (on) {
                setLoadRequired();
            } else {
                updateDfgFieldMediation();
            }
        }
    }

    public void makeTransient() {
        if (isDirty(null)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The instance is dirty.");
        }
        if (isTransientManaged()) return;
        //fill the pc instance with data in state.
        final int[] stateFieldNos = new int[classMetaData.stateFieldNos.length];
        final int n = state.getFieldNos(stateFieldNos);
        for (int i = 0; i < n; i++) {
            final FieldMetaData fmd = classMetaData.stateFields[stateFieldNos[i]];
            int cat = fmd.category;
            if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) continue;
            if (fmd.managedFieldNo < 0) continue;
            if (!loadedFields[fmd.managedFieldNo]) {
                pc.jdoReplaceField(fmd.managedFieldNo);
            }
        }
        unManage();
        getPm().removeTxStateObject(this);
    }

    public void makeTransientRecursive() {
        if (isDirty(null)) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The instance is dirty.");
        }
        if (isTransientManaged()) return;

        VersantPersistenceManagerImp pm = getPm();

        final int[] stateFieldNos = new int[classMetaData.stateFieldNos.length];
        for (int i = 0; i < state.getFieldNos(stateFieldNos); i++) {
            final FieldMetaData fmd = classMetaData.stateFields[stateFieldNos[i]];
            if (fmd.managedFieldNo < 0) continue;
            int cat = fmd.category;
            if (!loadedFields[fmd.managedFieldNo]) {
                if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) continue;
                pc.jdoReplaceField(fmd.managedFieldNo);
            } else {
                if (cat == MDStatics.CATEGORY_REF || cat == MDStatics.CATEGORY_POLYREF) {
                    getPm().makeTransientRecursive(state.getObjectField(
                            fmd.stateFieldNo, pc, this.pmProxy, oid));
                }
            }
        }

        unManage();
        pm.removeTxStateObject(this);
    }

    public void makeTransactional() {
        /**
         * This first case cover both ds and optimistic tx.
         * For ds the state is cleaned and for optimistic tx
         * the refresh depends on the user.
         */
        if (((stateM ^ STATE_P_NON_TX) == 0)) {
            VersantPersistenceManagerImp pm = getPm();
            if (!pm.isOptimistic()) {
                changeToHollowState(true);
            } else {
                updateDfgFieldMediation();
            }
            stateM = STATE_P_CLEAN;
            pm.addTxStateObject(this);
            return;
        } else if (((stateM ^ STATE_TRANSIENT) == 0)) {
            stateM = STATE_T_CLEAN;
        }
    }

    /**
     * Update the mediation of dfg fields. This method should be called when
     * transitioning from p-non-tx to a tx state.
     */
    private void updateDfgFieldMediation() {
        if (dfgLoaded) {
            //must reset the dfg field mediation.
            jdoFlags = PersistenceCapable.READ_OK;
            pc.jdoReplaceFlags();
        }
    }

    public void makeNonTransactional() {
        if (isDirty()) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "A Dirty instance may not be made NonTransactional");
        }
        if (isTClean()) {
            changeToTransient(EVENT_COMMIT);
        } else if (isPClean()) {
            stateM = STATE_P_NON_TX;
        }
    }

    /**
     * The idea behind refresh is to reload the already loaded data from the server.
     * This is mostly needed for optismistic transactions to minimize the concurrent
     * updates.
     * <p/>
     * The only state change that occurs is for P-Dirty instances that change to
     * P-Clean.
     * <p/>
     * This call is a no-op for all new instances, because there is nothing to
     * relaod from the server.
     * Hollow instances are also an no-op because nothing has been fetched.
     * <p/>
     * For all the rest this is a relead of the currently loaded fields.
     */
    public void refresh() {
        /**
         * All new instances are no-ops because there is nothing in the store
         * to refresh from.
         */
        if (isNew()) {
            return;
        } else {
            VersantPersistenceManagerImp pm = getPm();
            if (pm.isActive()) {
                if (((stateM ^ STATE_P_DIRTY) == 0)) {
                    if (pm.isOptimistic()) {
                        stateM = STATE_P_NON_TX;
                    } else {
                        stateM = STATE_P_CLEAN;
                    }
                    pm.addTxStateObject(this);
                }
            }
            changeToHollowState(true);
            pm.getStateForRefresh(this.oid,
                    classMetaData.fetchGroups[0].sendFieldsOnFetch ? getForReadState() : null,
                    0);
        }
    }

    /**
     * Check the model consistency of all of the fields in our State.
     * This currently only makes sure that bidirectional relationships have
     * been properly completed but other checks may be added in future.
     */
    public void checkModelConsistency() {
        if (isDeleted()) return;
        FieldMetaData[] fields = classMetaData.fields;
        for (int fieldNo = 0; fieldNo < fields.length; fieldNo++) {
            FieldMetaData fmd = fields[fieldNo];
            if (fmd.isMaster) {         // one side of one-to-many
                checkOneToManyMaster(fieldNo);
            } else if (fmd.isDetail) {  // many side of one-to-many
                checkOneToManyDetail(fieldNo);
            } else if (fmd.isManyToMany) {
                checkManyToMany(fieldNo);
            }
        }
    }

    /**
     * Check the one side of a one-to-many. Make sure that all of the details
     * added to the collection have the correct master set.
     */
    private void checkOneToManyMaster(int fieldNo) {
        FieldMetaData fmd = classMetaData.fields[fieldNo];
        VersantPersistenceManagerImp realPM = getPm();
        Collection col = (Collection)state.getObjectField(fmd.stateFieldNo,
                pc, pmProxy, oid);
        if (col == null) return;
        // check that all of the objects in col have us as the master
        int index = 0;
        for (Iterator i = col.iterator(); i.hasNext(); index++) {
            Object detail = i.next();
            if (detail == null) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent one-to-many: " +
                        "null object at index " + index + " in collection " + fmd.getQName() +
                        " on " + toErrString(oid, pc));
            }
            PCStateMan detailSM = realPM.getInternalSM(
                    (PersistenceCapable)detail);
            if (detailSM.isDeleted()) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent one-to-many: " +
                        "deleted object " + toErrString(detailSM.oid, null) +
                        " at index " + index + " in collection " + fmd.getQName() +
                        " on " + toErrString(oid, pc));
            }
            if (fmd.inverseFieldMetaData.fake) return;
            Object master = detailSM.getObjectField(null, fmd.inverseFieldNo,
                    null);
            if (master != pc) {
                StringBuffer s = new StringBuffer();
                s.append("Inconsistent one-to-many: object ");
                s.append(toErrString(detailSM.oid, detailSM.pc));
                s.append(" at index ");
                s.append(index);
                s.append(" in collection ");
                s.append(fmd.getQName());
                s.append(" on ");
                s.append(toErrString(oid, pc));
                if (master == null) {
                    s.append(" has null ");
                } else {
                    s.append(" has wrong ");
                }
                s.append(fmd.inverseFieldMetaData.getQName());
                if (master != null) {
                    s.append(' ');
                    PCStateMan masterSM = realPM.getInternalSM(
                            (PersistenceCapable)master);
                    s.append(toErrString(masterSM.oid, master));
                }
                throw BindingSupportImpl.getInstance().runtime(s.toString());
            }
        }
    }

    /**
     * Check the many side of a one-to-many. Make sure that the master has
     * been set correctly and that the master has us in its list of details.
     */
    private void checkOneToManyDetail(int fieldNo) {
        FieldMetaData fmd = classMetaData.fields[fieldNo];
        VersantPersistenceManagerImp realPM = getPm();
        Object master = state.getObjectField(fmd.stateFieldNo, pc, pmProxy,
                oid);
        if (master == null) {
            if (fmd.nullValue == MDStatics.NULL_VALUE_EXCEPTION) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent one-to-many: " +
                        "'many' object " + toErrString(oid, pc) + " field " +
                        fmd.getQName() + " is null");
            }
            // the master not set case is caught in checkOneToManyMaster
            // as it is ok to have a null master if the detail
            // is not part of any master's collection
            return;
        }
        if (master instanceof OID) {
            try {
                master = realPM.getObjectById(master, true);
            } catch (RuntimeException e) {
                if (BindingSupportImpl.getInstance().isOwnException(e)) {
                    throw BindingSupportImpl.getInstance().runtime("Inconsistent one-to-many: " +
                            "'many' object " + toErrString(oid, pc) + " field " +
                            fmd.getQName() + " references invalid 'one' object: " +
                            e.getMessage(), e);
                } else {
                    throw e;
                }
            }
        }
        PCStateMan masterSM = realPM.getInternalSM((PersistenceCapable)master);
//        if(fmd.inverseFieldMetaData.fake)return;
        Object o = masterSM.getObjectField(null, fmd.inverseFieldNo, null);
        if (o instanceof Collection) {
            if (!((Collection)o).contains(pc)) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent one-to-many: " +
                        "'many' object " + toErrString(oid, pc) +
                        " is not in collection on 'one' instance " +
                        toErrString(masterSM.oid, masterSM.pc) + " " +
                        fmd.inverseFieldMetaData.getQName());
            }
        } else {
            PersistenceCapable[] pcs = (PersistenceCapable[])o;
            boolean contains = false;
            for (int i = 0; i < pcs.length; i++) {
                PersistenceCapable persistenceCapable = pcs[i];
                if (persistenceCapable == pc) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent one-to-many: " +
                        "'many' object " + toErrString(oid, pc) +
                        " is not in collection on 'one' instance " +
                        toErrString(masterSM.oid, masterSM.pc) + " " +
                        fmd.inverseFieldMetaData.getQName());
            }
        }
    }

    /**
     * Check a many-to-many. Make sure that each collection is the inverse
     * of the other.
     */
    private void checkManyToMany(int fieldNo) {
        FieldMetaData fmd = classMetaData.fields[fieldNo];
        VersantPersistenceManagerImp realPM = getPm();
        Collection col = (Collection)state.getObjectField(fmd.stateFieldNo,
                pc, pmProxy, oid);
        if (col == null) return;
        // check that all of the objects in col have us in their own col
        int index = 0;
        for (Iterator i = col.iterator(); i.hasNext(); index++) {
            Object other = i.next();
            if (other == null) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent many-to-many: " +
                        "null object at index " + index + " in collection " + fmd.getQName() +
                        " on " + toErrString(oid, pc));
            }
            PCStateMan otherSM = realPM.getInternalSM(
                    (PersistenceCapable)other);
            if (otherSM.isDeleted()) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent many-to-many: " +
                        "deleted object " + toErrString(otherSM.oid, null) +
                        " at index " + index + " in collection " + fmd.getQName() +
                        " on " + toErrString(oid, pc));
            }
            Collection otherCol = (Collection)otherSM.getObjectField(null,
                    fmd.inverseFieldNo, null);
            if (!otherCol.contains(pc)) {
                throw BindingSupportImpl.getInstance().runtime("Inconsistent many-to-many: " +
                        "object " + toErrString(oid, pc) + " at index " + index +
                        "in collection " + fmd.getQName() + " contains " +
                        toErrString(otherSM.oid, otherSM.pc) + " but " +
                        fmd.inverseFieldMetaData.getQName() +
                        " does not contain it");
            }
        }
    }

    /**
     * Format an oid and its associated pc instance (can be null) into a nice
     * String for error messages.
     */
    private static String toErrString(OID oid, Object pc) {
        StringBuffer s = new StringBuffer();
        s.append('[');
        if (oid.isNew()) {
            s.append("new");
        } else {
            s.append(oid.toStringImp());
        }
        if (pc != null) {
            s.append(' ');
            s.append(Utils.toString(pc));
        }
        s.append(']');
        return s.toString();
    }

    /**
     * If the PC instance implements InstanceCallbacks and has not been deleted
     * then call its jdoPreStore method and also invoke the store lifecycle
     * callback if listeners is not null. Returns true if jdoPreStore was
     * called or there were store listeners.
     */
    public boolean doJDOPreStore(LifecycleListenerManager[] listeners) {
        if ((stateM & MASK_DELETE) == 0) {
            int index = state.getClassIndex();
            boolean ans = listeners != null &&
                    listeners[index] != null &&
                    listeners[index].firePreStore(pc);
            if (instanceCallbacks != null) {

                instanceCallbacks.jdoPreStore();


                return true;
            }
            return ans;
        }
        return false;
    }

    /**
     * This is called on all transactional objects so that they can prepare for
     * the commit or flush. If the state was deleted it will call add itself
     * to the list of instances to be deleted etc.
     */
    public void prepareCommitOrFlush(boolean commit) {
        addToProcessList();
        /**
         * Add all the instance that must be deleted to the toBeDeleted collection.
         * If the oid is new then it must not be added because it is not in the db and therefore
         * not to be removed.
         */
        if (isDeleted()) {
            addForDelete();
            //ignore
        } else if (isDirty()) {

            // If delete-orphans is true and this class contains refs used to
            // complete collections mapped using a foreign key in the element
            // class and all of these references are null and we are doing
            // a commit then delete this instance.
            if (commit && classMetaData.deleteOrphans
                    && classMetaData.fkCollectionRefStateFieldNos != null) {
                int[] a = classMetaData.fkCollectionRefStateFieldNos;
                int i;
                for (i = a.length - 1; i >= 0; i--) {
                    int sno = a[i];
                    if (!state.containsField(sno) || !state.isNull(sno)) break;
                }
                if (i < 0) {
                    deletePersistent();
                    addForDelete();
                    return;
                }
            }
            preparedStatus = 1;

            // clear transactional fields if this is a commit
            if (commit) state.clearTransactionNonPersistentFields();

            VersantPersistenceManagerImp realPM = getPm();
            State toStoreState = createToStoreState();
            boolean isNew = oid.isNew();

            if (!oid.isResolved()) oid.resolve(state);
            origState.clearCollectionFields();

            // If nothing is copied to toStoreState then only transactional
            // fields were dirty. If this is a commit and the instance is new
            // then persist it anyway.
            if (!state.fillToStoreState(toStoreState, realPM, this)
                    && (!commit || !isNew)) {
                return;
            }
            addToStoreOidContainer(toStoreState, realPM, isNew);
        }
    }

    private void addToStoreOidContainer(State toStoreState,
            VersantPersistenceManagerImp realPM, boolean aNew) {
        if (doChangeChecking) {
            if (origState != null) origState.clearNonFilled(toStoreState);
            realPM.storeOidStateContainer.add(oid, toStoreState,
                    origState,
                    aNew && classMetaData.postInsertKeyGenerator);
        } else {
            if (aNew) {
                realPM.storeOidStateContainer.add(oid, toStoreState, null,
                        classMetaData.postInsertKeyGenerator);
            } else {
                realPM.storeOidStateContainer.add(oid, toStoreState,
                        origState,
                        false);
            }
        }
        preparedStatus = 2;
    }

    private void addForDelete() {
        if (!oid.isNew() && !addedForDelete) {
            // The forReadState must have been previously filled with whatever
            // needs to be sent back to the server with the OID.
            if (oid.getAvailableClassMetaData() == null) {
                // create a typed OID as untyped OIDs cannot be deleted
                OID o = classMetaData.createOID(true);
                o.setLongPrimaryKey(oid.getLongPrimaryKey());
                getPm().addForDelete(o, forReadState);
            } else {
                getPm().addForDelete(oid, forReadState);
            }
            addedForDelete = true;
        }
    }

    public String toString() {
        return "SM@" + System.identityHashCode(this) + " "
                + (oid == null ? "null" : oid.toStringImp())
                + " cacheKey: " + (cacheEntry == null ? "NULL" : cacheEntry.toString())
                + " inProcessList: " + pmProxy.getRealPM().getCache().inProcessList(this);
    }

    /**
     * This is called after the server side commit is done. This will ensure
     * that the instance changes to the correct state.
     */
    private void commitImp(VersantPersistenceManagerImp rpm) {
        try {
            switch (stateM) {
                case STATE_TRANSIENT:
                    break;
                case STATE_T_CLEAN:
                    break;
                case STATE_T_DIRTY:
                    stateM = STATE_T_CLEAN;
                    if (beforeState != null) beforeState.clear();
                    if (toStoreState != null) toStoreState.clear();
                    break;
                case STATE_HOLLOW:
                    break;
                case STATE_P_NEW:
                    changeTmpOIDToRealOID();
                case STATE_P_CLEAN:
                case STATE_P_DIRTY:
                    if (rpm.isRetainValues()) {
                        if (!toBeEvictedFlag) {
                            changeToPNonTxForCommit(rpm);
                        } else {
                            changeToHollowState();
                            cacheEntry.changeToRefType(jdoManagedCache.queue,
                                    VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);
                        }
                    } else {
                        changeToHollowState();
                        if (toBeEvictedFlag) {
                            cacheEntry.changeToRefType(jdoManagedCache.queue,
                                    VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK);
                        }
                    }
                    if (beforeState != null) beforeState.clear();
                    if (toStoreState != null) toStoreState.clear();
                    break;
                case STATE_P_DEL:
                case STATE_P_NEW_DEL:
                    state = createStateImp();
                    deleteImpForCommit();
                    break;
                default:
                    throw BindingSupportImpl.getInstance().internal(
                            "The state is unreachable");
            }
        } finally {
            clearTxFlags();
        }
    }

    private void clearTxFlags() {
        addedForDelete = false;
        readInTx = false;
        toBeEvictedFlag = false;
        preparedStatus = 0;
    }

    public void flushCommit() {
        switch (stateM) {
            case STATE_TRANSIENT:
                break;
            case STATE_T_CLEAN:
                break;
            case STATE_T_DIRTY:
                break;
            case STATE_HOLLOW:
                break;
            case STATE_P_NEW:
                changeTmpOIDToRealOID();
            case STATE_P_CLEAN:
            case STATE_P_DIRTY:
                this.state.makeClean();
                replaceSCOFields();
                break;
            case STATE_P_DEL:
            case STATE_P_NEW_DEL:
                break;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "The state is unreachable");
        }
    }

    public void updateAutoFields(State autoS) {
        if (classMetaData.hasAutoSetFields) {
            this.state.updateFrom(autoS);
            this.origState.updateFrom(autoS);
        }
    }

    /**
     * Helper method for commit to do work for deleting an instance. This must remove all
     * resources held by this instance.
     */
    public void deleteImpForCommit() {
        changeToTransient(EVENT_COMMIT);
    }

    /**
     * This changes the current 'new' oid to an actual oid retrieved from the store. This is
     * called as part of a commit from a PNew state.
     */
    private void changeTmpOIDToRealOID() {
        if (!oid.isNew()) return;
        if (cacheEntry.mappedOID != oid.getRealOID()) {
            jdoManagedCache.addRealOID(this);
        }
        this.oid = this.oid.getRealOID();
        replaceApplicationPKFields();
    }

    /**
     * This is called from commit and retainValues is set.
     * The state must be P-Non-Tx after this.
     */
    public void changeToPNonTxForCommit(VersantPersistenceManagerImp rpm) {
        stateM = STATE_P_NON_TX;
        dfgLoaded = false;
        setLoadRequired();
        resetLoadedFields();
        this.state.makeClean();

        replaceSCOFields();

        if (doChangeChecking) {
            this.origState.clear();
            maintainOrigState(state, rpm);
        } else {
            this.origState.clear();
        }
        toBeEvictedFlag = false;
    }

    /**
     * Make all PC instances referenced by us persistent. This is called by
     * the PM on makePersistent and commit as part of the reachability
     * search.<p>
     */
    public void addRefs() {
        state.addRefs(getPm(), this);
    }

    /**
     * This is currently only called for an instance that transitioned from new to
     * persistent but is a good method to call if a application pk has changed.
     * <p/>
     * This is called from the commit method after the new oid is received for
     * the state.
     */
    private final void replaceApplicationPKFields() {
        if (classMetaData.identityType != MDStatics.IDENTITY_TYPE_APPLICATION) return;
        this.state.copyFields(oid);
        pc.jdoReplaceFields(classMetaData.pkFieldNos);
    }

    public byte replacingFlags(PersistenceCapable pc) {
        return jdoFlags;
    }

    public StateManager replacingStateManager(PersistenceCapable pc,
            StateManager sm) {
        /**
         * The instance is transitioning to transient.
         */
        if (stateM == STATE_TRANSIENT) {
            return null;
        }
        return sm;
    }

//==============================state checks====================================

    public boolean isDirty(PersistenceCapable pc) {
        return ((stateM & MASK_DIRTY) != 0);
    }

    public boolean isTransactional(PersistenceCapable pc) {
        return ((stateM & MASK_TX) != 0);
    }

    public boolean isPersistent(PersistenceCapable pc) {
        return ((stateM & MASK_PERSISTENT) != 0);
    }

    public boolean isNew(PersistenceCapable pc) {
        return ((stateM & MASK_NEW) != 0);
    }

    public boolean isDeleted(PersistenceCapable pc) {
        return ((stateM & MASK_DELETE) != 0);
    }

//==============================================================================

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        getPm().requestedPCState = this;
        return pmProxy;
    }

    public void makeDirty(PersistenceCapable pc, String fieldName) {
        try {
            makeDirtyImp(oid.getClassMetaData().getFieldMetaData(
                    fieldName));
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makeDirty(PersistenceCapable persistenceCapable, int fieldNo) {
        try {
            makeDirtyImp(getFMD(fieldNo));
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void makeDirtyImp(FieldMetaData fmd) {
        try {
            boolean hasListener = callJdoPreDirty();
            doWrite(fmd, false);
            state.makeDirty(fmd.stateFieldNo);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Get the LOID of this instance. This should only be called for classes
     * stored in VDS. This will assign it real LOID if it does not already
     * have one.
     */
    public long getLOID() {
        return getRealOID().getLongPrimaryKey();
    }

    public Object getObjectId(PersistenceCapable pcParam) {
        try {
            if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
                return new VersantOid(this, jmd, oid.isResolved());
            } else if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                if (oid.isNew() && classMetaData.postInsertKeyGenerator) {
                    getPm().flushRetainState();
                }
                // If this is called directly after a commit with retain values to false
                // then the state does not contiain any field and hence
                if (oid.isNew()) {
                    if (classMetaData.useKeyGen) {
                        NewObjectOID newOID = (NewObjectOID)oid;
                        if (newOID.realOID == null) {
                            newOID.realOID = getPm().getStorageManager().createOID(
                                    classMetaData);
                            newOID.realOID.getClassMetaData();
                            jdoManagedCache.addRealOID(this);
                            state.copyFields(newOID.realOID);
                            pc.jdoReplaceFields(classMetaData.pkFieldNos);
                        }
                    } else {
                        NewObjectOID newOID = (NewObjectOID)oid;
                        if (newOID.realOID == null) {
                            newOID.realOID = classMetaData.createOID(true);
                            newOID.realOID.getClassMetaData();
                            state.copyKeyFields(newOID.realOID);
                            jdoManagedCache.addRealOID(this);
                        }
                    }
                }
                Object pcID = pc.jdoNewObjectIdInstance();
                if (!classMetaData.top.isSingleIdentity) {
                    pc.jdoCopyKeyFieldsToObjectId(pcID);
                }
                return pcID;
            } else {
                throw BindingSupportImpl.getInstance().internal("Unknown identity type: '" + classMetaData.identityType +
                        "' for " + classMetaData.qname);
            }
        } catch (Exception e) {
            handleException(e);
            return null; // keep compiler happy
        }
    }

    /**
     * This is called from VersantOid if it need the
     */
    public OID getRealOID() {
        try {
            if (classMetaData.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
                if (oid.isNew()) {
                    NewObjectOID newOID = (NewObjectOID)oid;
                    OID realOid = newOID.realOID;
                    if (realOid == null) {
                        if (oid.isNew() && classMetaData.postInsertKeyGenerator) {
                            getPm().flushRetainState();
                        }
                        newOID.realOID = realOid = getPm().getStorageManager().createOID(
                                classMetaData);
                        realOid.resolve(state);
                        realOid.getClassMetaData();
                        jdoManagedCache.addRealOID(this);
                    }
                    return newOID.realOID;
                }
                return oid;
            } else {
                throw BindingSupportImpl.getInstance().internal("This method should only be " +
                        "called for DataStore identity");
            }
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public Object getTransactionalObjectId(PersistenceCapable pc) {
        return getObjectId(pc);
    }

    /**
     * This will only load the fields for the pc instance. It does not force aload and not any reachable pc instances.
     *
     * @param pc
     */
    public void preSerialize(PersistenceCapable pc) {
        loadAllPersistentFieldsToPC(getPm());
    }

//==============================getXXXFields====================================

    public boolean getBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        try {
            return getBooleanFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    public boolean getBooleanFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            boolean currentValue) {
        try {
            doRead(fmd);
            return state.getBooleanField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    public char getCharField(PersistenceCapable pc, int field,
            char currentValue) {
        try {
            return getCharFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public char getCharFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            char currentValue) {
        try {
            doRead(fmd);
            return state.getCharField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public byte getByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        try {
            return getByteFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public byte getByteFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            byte currentValue) {
        try {
            doRead(fmd);
            return state.getByteField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public short getShortField(PersistenceCapable pc, int field,
            short currentValue) {
        try {
            return getShortFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public short getShortFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            short currentValue) {
        try {
            doRead(fmd);
            return state.getShortField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public int getIntField(PersistenceCapable pc, int field, int currentValue) {
        try {
            return getIntFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public int getIntFieldImp(PersistenceCapable pc, FieldMetaData fmd, int currentValue) {
        try {
            doRead(fmd);
            return state.getIntField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public float getFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        try {
            return getFloatFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public float getFloatFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            float currentValue) {
        try {
            doRead(fmd);
            return state.getFloatField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public double getDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        try {
            return getDoubleFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }
    public double getDoubleFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            double currentValue) {
        try {
            doRead(fmd);
            return state.getDoubleField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public long getLongField(PersistenceCapable pc, int field,
            long currentValue) {
        try {
            return getLongFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public long getLongFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            long currentValue) {
        try {
            doRead(fmd);
            return state.getLongField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public String getStringField(PersistenceCapable pc, int field,
            String currentValue) {
       return getStringFieldImp(pc, getFMD(field), currentValue);
    }

    public String getStringFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            String currentValue) {
        try {
            doRead(fmd);
            return state.getStringField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public Object getObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        return getObjectFieldImp(pc, getFMD(field), currentValue);
    }

    public Object getObjectFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            Object currentValue) {
        try {
            StatesReturned nogc = doRead(fmd);
            // Keep a reference to the fetched States (if any) to make sure
            // they do not get GCed after being loaded into the local cache.
            // This is important for collections of PC and other fields that
            // involve fetching State instances and loading them into the local
            // cache. They are only hard referenced when the SCO instance has
            // been created by the next line.
            Object ans = state.getObjectField(fmd.stateFieldNo, this.pc, pmProxy, oid);
            if (nogc == null) {
                // dummy code to keep IDE happy and to hopefully prevent
                // any overzealous optimization from removing nogc
            }
            return ans;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }
//================================setXXXField===================================

    public void setBooleanField(PersistenceCapable _pc, int field,
            boolean currentValue, boolean newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setBooleanFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setBooleanFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            boolean currentValue, boolean newValue) {
        try {
            if (isTransientManaged()) {
                state.setBooleanField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setBooleanField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setCharField(PersistenceCapable _pc, int field,
            char currentValue, char newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setCharFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setCharFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            char currentValue, char newValue) {
        try {
            if (isTransientManaged()) {
                state.setCharField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setCharField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setByteField(PersistenceCapable _pc, int field,
            byte currentValue, byte newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setByteFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setByteFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            byte currentValue, byte newValue) {
        try {
            if (isTransientManaged()) {
                state.setByteField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setByteField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setShortField(PersistenceCapable _pc, int field,
            short currentValue, short newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setShortFieldImp(_pc, getFMD(field), currentValue, newValue);
            _pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setShortFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            short currentValue, short newValue) {
        try {
            if (isTransientManaged()) {
                state.setShortField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setShortField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setIntField(PersistenceCapable _pc, int field, int currentValue,
            int newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setIntFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setIntFieldImp(PersistenceCapable _pc, FieldMetaData fmd, int currentValue,
            int newValue) {
        try {
            if (isTransientManaged()) {
                state.setIntField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setIntField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setFloatField(PersistenceCapable _pc, int field,
            float currentValue, float newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setFloatFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setFloatFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            float currentValue, float newValue) {
        try {
            if (isTransientManaged()) {
                state.setFloatField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setFloatField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setDoubleField(PersistenceCapable _pc, int field,
            double currentValue, double newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setDoubleFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setDoubleFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            double currentValue, double newValue) {
        try {
            if (isTransientManaged()) {
                state.setDoubleField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setDoubleField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setLongField(PersistenceCapable _pc, int field,
            long currentValue, long newValue) {
        try {
            boolean hasListener = callJdoPreDirty();
            setLongFieldImp(this.pc, getFMD(field), currentValue, newValue);
            this.pc.jdoReplaceField(field);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
        }
    }

    public void setLongFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            long currentValue, long newValue) {
        try {
            if (isTransientManaged()) {
                state.setLongField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setLongField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
        }
    }

    public void setStringField(PersistenceCapable _pc, int field,
            String currentValue, String newValue) {
        boolean hasListener = callJdoPreDirty();
        setStringFieldImp(this.pc, getFMD(field) , currentValue, newValue);
        this.pc.jdoReplaceField(field);
        if (hasListener) callJdoPostDirty();
    }

    public void setStringFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            String currentValue, String newValue) {
        try {
            if (isTransientManaged()) {
                state.setStringField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue) return;
                doWrite(fmd, false);
                state.setStringField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void setObjectField(PersistenceCapable _pc, int field,
            Object currentValue, Object newValue) {
        boolean hasListener = callJdoPreDirty();
        setObjectFieldImp(this.pc, getFMD(field), currentValue, newValue);
        this.pc.jdoReplaceField(field);
        if (hasListener) callJdoPostDirty();
    }

    public void setObjectFieldImp(PersistenceCapable _pc, FieldMetaData fmd,
            Object currentValue, Object newValue) {
        try {
            if (isTransientManaged()) {
                state.setObjectField(fmd.stateFieldNo, newValue);
                if (getPm().isActive()) {
                    stateM |= MASK_DIRTY;
                    setBeforeState(fmd, currentValue);
                }
                addToProcessList();
            } else {
                if (loadedFields[fmd.stateFieldNo] && newValue == currentValue)
                    return;
                doWrite(fmd, fmd.isDetail && fmd.managed);
                if (fmd.isDetail) {
                    PersistenceCapable currentMaster = (PersistenceCapable)getObjectFieldImp(
                            null, fmd, null);
                    VersantPersistenceManagerImp pm = getPm();
                    if (fmd.managed) {
                        int mastColFieldNo = fmd.inverseFieldNo;
                        if (currentMaster != null) {
                            PCStateMan masterSM = pm.getInternalSM(
                                    currentMaster);
                            if (masterSM.classMetaData.managedFields[mastColFieldNo].type.getComponentType() != null) {
                                PersistenceCapable[] pcs = (PersistenceCapable[])masterSM.getObjectField(
                                        null,
                                        mastColFieldNo, null);
                                if (pcs != null) {
                                    for (int i = 0; i < pcs.length; i++) {
                                        PersistenceCapable pcInst = pcs[i];
                                        if (pcInst == this.pc) {
                                            pcs[i] = null;
                                        }
                                    }
                                }
                            } else {
                                ((Collection)masterSM.getObjectField(null,
                                        mastColFieldNo, null)).remove(this.pc);
                            }
                        }
                        if (newValue != null) {
                            PersistenceCapable newMaster = (PersistenceCapable)newValue;
                            if (!newMaster.jdoIsPersistent()) {
                                this.pc.jdoGetPersistenceManager().makePersistent(
                                        newMaster);
                            }
                            PCStateMan masterSM = pm.getInternalSM(newMaster);
                            ((Collection)masterSM.getObjectField(null,
                                    mastColFieldNo, null)).add(this.pc);
                        }
                    } else {
                        // Make sure current master and new value is evicted
                        // on commit.
                        // This prevents stale data from remaining in the L2
                        // cache when only the back reference is updated.
                        if (currentMaster != null) {
                            pm.evictFromL2CacheAfterCommitImp(currentMaster);
                        }
                        if (newValue != null) {
                            pm.evictFromL2CacheAfterCommitImp(newValue);
                        }
                    }
                } else if (fmd.category == MDStatics.CATEGORY_COLLECTION
                        && fmd.inverseFieldMetaData != null
                        && fmd.inverseFieldMetaData.fake) {
                    if (!state.containsField(fmd.stateFieldNo)) {
                        getPm().getState(this.oid, fmd.fetchGroup.sendFieldsOnFetch ? getForReadState() : null,
                                fmd.fetchGroup.index, fmd, -1, false);
                    }
                    FieldMetaData inverseFieldMetaData = fmd.inverseFieldMetaData;
                    Object o = state.getInternalObjectField(fmd.stateFieldNo);
                    if (o != null) {
                        if (o instanceof VersantSimpleSCO) {
                            VersantPersistenceManagerImp rpm = getPm();
                            Collection col = (Collection)o;
                            for (Iterator iterator = col.iterator();
                                 iterator.hasNext();) {
                                PCStateMan detailSm = jdoManagedCache.getByOID(
                                        rpm.getInternalOID(
                                                (PersistenceCapable)iterator.next()),
                                        true);
                                detailSm.makeInternalDirty();
                                detailSm.state.setObjectField(
                                        inverseFieldMetaData.stateFieldNo,
                                        null);
                            }
                        } else {
                            Object[] oa = (Object[])o;
                            for (int i = 0; i < oa.length; i++) {
                                if (oa[i] == null) break;
                                PCStateMan detailSm = jdoManagedCache.getByOID(
                                        (OID)oa[i],
                                        true);
                                detailSm.makeInternalDirty();
                                detailSm.state.setObjectField(
                                        inverseFieldMetaData.stateFieldNo,
                                        null);
                            }
                        }
                    }
                }
                state.setObjectField(fmd.stateFieldNo, newValue);
                setBeforeState(fmd, currentValue);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * This is called from managed SCO classes to ensure that the back
     * reference of a newly added detail is updated property. It is
     * not possible to just call setObject in this case as this will result
     * in an endless loop as setObject adds the detail to its master.
     * If removeFromCurrentMaster is true then this instance is removed
     * from its current master (if any and if different).
     */
    public void setMaster(int field, Object newMaster,
            boolean removeFromCurrentMaster) {
        try {
            boolean hasListener = callJdoPreDirty();
            Object currentMaster = getObjectField(null, field, null);
            if (loadedFields[field] && newMaster == currentMaster) return;
            doWrite(getFMD(field), false);
            if (removeFromCurrentMaster && currentMaster != null) {
                int mastColFieldNo = getFMD(field).inverseFieldNo;
                PCStateMan masterSM = getPm().getInternalSM(
                        (PersistenceCapable)currentMaster);
                ((Collection)masterSM.getObjectField(null,
                        mastColFieldNo, null)).remove(pc);
            }
            state.setObjectFieldAbs(field, newMaster);
            pc.jdoReplaceField(field);
            setBeforeState(getFMD(field), currentMaster);
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * This is called from managed SCO classes to ensure that the back
     * reference of a newly added detail is updated property. It is
     * not possible to just call setObject in this case as this will result
     * in an endless loop as setObject adds the detail to its master.
     * If removeFromCurrentMaster is true then this instance is removed
     * from its current master (if any and if different).
     */
    public void setFakeMaster(int stateFieldNo, VersantStateManager master,
            boolean remove) {
        VersantPersistenceManagerImp rpm = getPm();

        try {
            boolean hasListener = callJdoPreDirty();
            if (isPrepared()) {
                if (inToStoreList()) {
                    //already prepared so add oid to the tostorestate
                    setOidOnState(toStoreState, stateFieldNo, master, remove);
                } else {
                    //prepared was called but no dirty fields was found
                    //so prepare must be called again
                    if (setOidOnState(state, stateFieldNo, master, remove)) {
                        state.fillToStoreState(toStoreState, rpm, this);
                    }
                    //must add to toStoreList
                    addToStoreOidContainer(toStoreState, rpm,
                            master.getOID().isNew());
                }
            } else {
                setOidOnState(state, stateFieldNo, master, remove);
            }
            makeInternalDirty();
            if (hasListener) callJdoPostDirty();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private boolean setOidOnState(State state, int stateFieldNo, VersantStateManager master,
            boolean remove) {
        if (state instanceof DeletedState) {
            return false;
        }
        if (remove) {
            Object o = state.getInternalObjectField(stateFieldNo);
            if (o != null && !o.equals(master.getOID()) && o != master.getPersistenceCapable()) {
                return false;
            }
        }
        if (remove) {
            state.setObjectField(stateFieldNo, null);
        } else {
            state.setObjectFieldUnresolved(stateFieldNo, master.getOID());
        }
        return true;
    }

    private boolean inToStoreList() {
        return preparedStatus == 2;
    }

    private boolean isPrepared() {
        return preparedStatus > 0;
    }

//===================================providedXXXFields============================

    public void providedBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        try {
            providedBooleanFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedBooleanFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            boolean currentValue) {
        try {
            state.setBooleanField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedCharField(PersistenceCapable pc, int field,
            char currentValue) {
        try {
            providedCharFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedCharFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            char currentValue) {
        try {
            state.setCharField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        try {
            providedByteFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedByteFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            byte currentValue) {
        try {
            state.setByteField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedShortField(PersistenceCapable pc, int field,
            short currentValue) {
        try {
            providedShortFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedShortFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            short currentValue) {
        try {
            state.setShortField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedIntField(PersistenceCapable pc, int field,
            int currentValue) {
        try {
            providedIntFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedIntFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            int currentValue) {
        try {
            state.setIntField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        try {
            providedFloatFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedFloatFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            float currentValue) {
        try {
            state.setFloatField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        try {
            providedDoubleFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedDoubleFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            double currentValue) {
        try {
            state.setDoubleField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedLongField(PersistenceCapable pc, int field,
            long currentValue) {
        try {
            providedLongFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedLongFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            long currentValue) {
        try {
            state.setLongField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedStringField(PersistenceCapable pc, int field,
            String currentValue) {
        try {
            providedStringFieldImp(pc, getFMD(field), currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedStringFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            String currentValue) {
        try {
            state.setStringField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void providedObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        providedObjectFieldImp(pc, getFMD(field), currentValue);
    }

    public void providedObjectFieldImp(PersistenceCapable pc, FieldMetaData fmd,
            Object currentValue) {
        try {
            state.setObjectField(fmd.stateFieldNo, currentValue);
            setBeforeState(fmd, currentValue);
        } catch (Exception e) {
            handleException(e);
        }
    }

//===================================replacingXXXFields============================

    public boolean replacingBooleanField(final PersistenceCapable pc,
            final int field) {
        return replacingBooleanFieldImp(pc, getFMD(field));
    }

    public boolean replacingBooleanFieldImp(final PersistenceCapable pc,
            FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getBooleanField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    public char replacingCharField(final PersistenceCapable pc,
            final int field) {
        return replacingCharFieldImp(pc, getFMD(field));
    }

    public char replacingCharFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getCharField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public byte replacingByteField(final PersistenceCapable pc,
            final int field) {
        return replacingByteFieldImp(pc, getFMD(field));
    }

    public byte replacingByteFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getByteField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public short replacingShortField(final PersistenceCapable pc,
            final int field) {
        return replacingShortFieldImp(pc, getFMD(field));
    }

    public short replacingShortFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getShortField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public int replacingIntField(final PersistenceCapable pc, final int field) {
        return replacingIntFieldImp(pc, getFMD(field));
    }

    public int replacingIntFieldImp(final PersistenceCapable pc, final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getIntField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public float replacingFloatField(final PersistenceCapable pc,
            final int field) {
        return replacingFloatFieldImp(pc, getFMD(field));
    }

    public float replacingFloatFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getFloatField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public double replacingDoubleField(final PersistenceCapable pc,
            final int field) {
        return replacingDoubleFieldImp(pc, getFMD(field));
    }

    public double replacingDoubleFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getDoubleField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public long replacingLongField(final PersistenceCapable pc,
            final int field) {
        return replacingLongFieldImp(pc, getFMD(field));
    }

    public long replacingLongFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getLongField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public String replacingStringField(final PersistenceCapable pc, final int field) {
        return replacingStringFieldImp(pc, getFMD(field));
    }

    public String replacingStringFieldImp(final PersistenceCapable pc, FieldMetaData fmd) {
        try {
            setLoaded(fmd);
            return state.getStringField(fmd.stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public Object replacingObjectField(final PersistenceCapable pc,
            final int field) {
        return replacingObjectFieldImp(pc, getFMD(field));
    }

    public Object replacingObjectFieldImp(final PersistenceCapable pc,
            final FieldMetaData fmd) {
        try {
            if (stateM == STATE_TRANSIENT) {
                return state.getInternalObjectField(fmd.stateFieldNo);
            } else {
                setLoaded(fmd);
                return state.getObjectField(fmd.stateFieldNo, this.pc, pmProxy, oid);
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    private void setBeforeState(FieldMetaData fmd, long oldValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setLongField(fmd.stateFieldNo, oldValue);
        }
    }

    private boolean doBeforeState(FieldMetaData fmd) {
        if (!getPm().doBeforeState(isTransientManaged(),
                fmd.category == MDStatics.CATEGORY_TRANSACTIONAL)) {
            return false;
        }
        if (beforeState == null) {
            beforeState = createStateImp();
        }
        return true;
    }

    private void setBeforeState(FieldMetaData fmd, int oldValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setIntField(fmd.stateFieldNo, oldValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, double currentValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setDoubleField(fmd.stateFieldNo, currentValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, String newValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setStringField(fmd.stateFieldNo, newValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, Object newValue) {
        /**
         * Only mutable fields of PNew instance values kept for rollback
         * Other states fields are refetched from db.
         */
        if (!isNew() && fmd.category != MDStatics.CATEGORY_TRANSACTIONAL) return;
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setObjectField(fmd.stateFieldNo, newValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, char currentValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setCharField(fmd.stateFieldNo, currentValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, byte currentValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setByteField(fmd.stateFieldNo, currentValue);
        }
    }

    public void setBeforeState(FieldMetaData fmd, boolean currentValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setBooleanField(fmd.stateFieldNo, currentValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, short currentValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setShortField(fmd.stateFieldNo, currentValue);
        }
    }

    private void setBeforeState(FieldMetaData fmd, float currentValue) {
        if (!doBeforeState(fmd)) return;
        if (!beforeState.containsField(fmd.stateFieldNo)) {
            beforeState.setFloatField(fmd.stateFieldNo, currentValue);
        }
    }

    public final void handleException(Exception x) {
        if (BindingSupportImpl.getInstance().isOwnException(x)) {
            throw (RuntimeException)x;
        } else {
            throw BindingSupportImpl.getInstance().internal(x.getMessage(), x);
        }
    }

    public boolean isInDirtyList(PCStateMan head) {
        return head == this || next != null || prev != null;
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







    public Object getOptimisticLockingValue() {
        if (oid.isNew()) return null;
        getPm().checkNonTxRead();
        loadRequiredFetchGroup();
        return state.getOptimisticLockingValue();
    }

	public Object[] replacingDetachedState(Detachable arg0, Object[] arg1) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}

}
