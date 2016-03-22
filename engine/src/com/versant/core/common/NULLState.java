
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
package com.versant.core.common;

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.jdo.*;
import com.versant.core.server.OIDGraph;
import com.versant.core.util.IntArray;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

import javax.jdo.spi.PersistenceCapable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.util.Date;

/**
 * This state is represents an oid that does not exist in the datastore but is referenced from
 * some other instance. It will only be used if refs to this class must be
 * treated as null when broken.
 * <p/>
 * This class is used as a singleton as it acts as a placeholder. It remains stateless.
 */
public final class NULLState extends State {

    public final static NULLState NULL_STATE = new NULLState();

    public NULLState() {
    }

    private void throwExc() {
        throw BindingSupportImpl.getInstance().internal(
                "method should not be invoked");
    }

    public boolean isCacheble() {
        return false;
    }

    public boolean isFieldNullorZero(int stateFieldNo) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addRefs(VersantPersistenceManagerImp sm, PCStateMan pcStateMan) {
        throwExc();
    }

    public boolean checkKeyFields(OID oid) {
        throwExc();
        return false;
    }

    public void clear() {
        throwExc();
    }

    public void clearApplicationIdentityFields() {
        throwExc();
    }

    public void clearCollectionFields() {
        throwExc();
    }

    public void clearDirtyFields() {
        throwExc();
    }

    public void clearNonAutoSetFields() {
        throwExc();
    }

    public void clearNonFilled(State state) {
        throwExc();
    }

    public void clearSCOFields() {
        throwExc();
    }

    public void clearTransactionNonPersistentFields() {
        throwExc();
    }

    public int compareToPass1(State state) {
        throwExc();
        return 0;
    }

    public boolean containFields(int[] stateFieldNos) {
        throwExc();
        return false;
    }

    public boolean containFieldsAbs(int[] absFieldNos) {
        throwExc();
        return false;
    }

    public boolean containsApplicationIdentityFields() {
        throwExc();
        return false;
    }

    public boolean containsFetchGroup(FetchGroup fetchGroup) {
        throwExc();
        return false;
    }

    public boolean containsField(int stateFieldNo) {
        throwExc();
        return false;
    }

    public boolean containsFieldAbs(int absFieldNo) {
        throwExc();
        return false;
    }

    public boolean containsPass1Fields() {
        throwExc();
        return false;
    }

    public boolean containsPass2Fields() {
        throwExc();
        return false;
    }

    public boolean containsValidAppIdFields() {
        throwExc();
        return false;
    }

    public void copyOptimisticLockingField(State state) {
        throwExc();
    }

    public void copyFields(OID oid) {
        throwExc();
    }

    public void copyFieldsForOptimisticLocking(State state,
            VersantPersistenceManagerImp sm) {
        throwExc();
    }

    public void copyKeyFields(OID oid) {
        throwExc();
    }

    public void copyKeyFieldsUpdate(OID oid) {
        throwExc();
    }

    public void findDirectEdges(OIDGraph graph,
            IntArray edges) {
        throwExc();
    }

    public boolean getBooleanField(int stateFieldNo) {
        throwExc();
        return false;
    }

    public boolean getBooleanFieldAbs(int absFieldNo) {
        throwExc();
        return false;
    }

    public byte getByteField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public byte getByteFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public char getCharField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public char getCharFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public int getClassIndex() {
        throwExc();
        return 0;
    }

    public ClassMetaData getClassMetaData() {
        throwExc();
        return null;
    }

    public ClassMetaData getClassMetaData(ModelMetaData jmd) {
        throwExc();
        return null;
    }

    public State getCopy() {
        throwExc();
        return null;
    }

    public boolean fillToStoreState(State stateToStore, PersistenceContext sm,
            VersantStateManager pcStateMan) {
        throwExc();
        return false;
    }

    public double getDoubleField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public double getDoubleFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public int getFieldNos(int[] buf) {
        throwExc();
        return 0;
    }

    public float getFloatField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public float getFloatFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public Object getInternalObjectField(int field) {
        throwExc();
        return null;
    }

    public Object getInternalObjectFieldAbs(int field) {
        throwExc();
        return null;
    }

    public int getIntField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public int getIntFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public long getLongField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public long getLongFieldInternal(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public long getLongFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public Object getObjectField(int stateFieldNo, PersistenceCapable owningPC,
            VersantPMProxy sm, OID oid) {
        throwExc();
        return null;
    }

    public Object getObjectFieldAbs(int absFieldNo,
            PersistenceCapable owningPC, VersantPMProxy sm,
            OID oid) {
        throwExc();
        return null;
    }

    public int getPass1FieldNos(int[] buf) {
        throwExc();
        return 0;
    }

    public int getPass2FieldNos(int[] buf) {
        throwExc();
        return 0;
    }

    public short getShortField(int stateFieldNo) {
        throwExc();
        return 0;
    }

    public short getShortFieldAbs(int absFieldNo) {
        throwExc();
        return 0;
    }

    public String getStringField(int stateFieldNo) {
        throwExc();
        return null;
    }

    public String getStringFieldAbs(int absFieldNo) {
        throwExc();
        return null;
    }

    public String getVersion() {
        throwExc();
        return null;
    }

    public boolean hasSameFields(State state) {
        throwExc();
        return false;
    }

    public boolean hasSameNullFields(State state, State mask) {
        throwExc();
        return false;
    }

    public boolean isDirty() {
        throwExc();
        return false;
    }

    public boolean isDirty(int fieldNo) {
        throwExc();
        return false;
    }

    public boolean isEmpty() {
        throwExc();
        return false;
    }

    public boolean isHollow() {
        throwExc();
        return false;
    }

    public boolean isNull(int stateFieldNo) {
        throwExc();
        return false;
    }

    public void makeClean() {
        throwExc();
    }

    public void makeDirtyAbs(int fieldNo) {
        throwExc();
    }

    public void makeDirty(int stateFieldNo) {
        throwExc();
    }

    public State newInstance() {
        throwExc();
        return null;
    }

    public boolean replaceNewObjectOIDs(int[] fieldNos, int fieldNosLength) {
        throwExc();
        return false;
    }

    public int replaceSCOFields(PersistenceCapable owner,
            VersantPMProxy sm, int[] absFieldNos) {
        throwExc();
        return 0;
    }

    public void retrieve(VersantPersistenceManagerImp sm) {
        throwExc();
    }

    public void setBooleanField(int stateFieldNo, boolean newValue) {
        throwExc();
    }

    public void setBooleanFieldAbs(int absFieldNo, boolean newValue) {
        throwExc();
    }

    public void setByteField(int stateFieldNo, byte newValue) {
        throwExc();
    }

    public void setByteFieldAbs(int absFieldNo, byte newValue) {
        throwExc();
    }

    public void setCharField(int stateFieldNo, char newValue) {
        throwExc();
    }

    public void setCharFieldAbs(int absFieldNo, char newValue) {
        throwExc();
    }

    public void setClassMetaData(ClassMetaData cmd) {
        throwExc();
    }

    public void setDoubleField(int stateFieldNo, double newValue) {
        throwExc();
    }

    public void setDoubleFieldAbs(int absFieldNo, double newValue) {
        throwExc();
    }

    public void setFloatField(int stateFieldNo, float newValue) {
        throwExc();
    }

    public void setFloatFieldAbs(int absFieldNo, float newValue) {
        throwExc();
    }

    public void setInternalBooleanField(int field, boolean newValue) {
        throwExc();
    }

    public void setInternalBooleanFieldAbs(int field, boolean newValue) {
        throwExc();
    }

    public void setInternalByteField(int field, byte newValue) {
        throwExc();
    }

    public void setInternalByteFieldAbs(int field, byte newValue) {
        throwExc();
    }

    public void setInternalCharField(int field, char newValue) {
        throwExc();
    }

    public void setInternalCharFieldAbs(int field, char newValue) {
        throwExc();
    }

    public void setInternalDoubleField(int field, double newValue) {
        throwExc();
    }

    public void setInternalDoubleFieldAbs(int field, double newValue) {
        throwExc();
    }

    public void setInternalFloatField(int field, float newValue) {
        throwExc();
    }

    public void setInternalFloatFieldAbs(int field, float newValue) {
        throwExc();
    }

    public void setInternalIntField(int field, int newValue) {
        throwExc();
    }

    public void setInternalIntFieldAbs(int field, int newValue) {
        throwExc();
    }

    public void setInternalLongField(int field, long newValue) {
        throwExc();
    }

    public void setInternalLongFieldAbs(int field, long newValue) {
        throwExc();
    }

    public void setInternalObjectField(int field, Object newValue) {
        throwExc();
    }

    public void setInternalObjectFieldAbs(int field, Object newValue) {
        throwExc();
    }

    public void setInternalShortField(int field, short newValue) {
        throwExc();
    }

    public void setInternalShortFieldAbs(int field, short newValue) {
        throwExc();
    }

    public void setInternalStringField(int field, String newValue) {
        throwExc();
    }

    public void setInternalStringFieldAbs(int field, String newValue) {
        throwExc();
    }

    public void setIntField(int stateFieldNo, int newValue) {
        throwExc();
    }

    public void setIntFieldAbs(int absFieldNo, int newValue) {
        throwExc();
    }

    public void setLongField(int stateFieldNo, long newValue) {
        throwExc();
    }

    public void setLongFieldAbs(int absFieldNo, long newValue) {
        throwExc();
    }

    public void setObjectField(int stateFieldNo, Object newValue) {
        throwExc();
    }

    public void setObjectFieldAbs(int absFieldNo, Object newValue) {
        throwExc();
    }

    public void setObjectFieldUnresolved(int field, Object newValue) {
        throwExc();
    }

    public void setObjectFieldUnresolvedAbs(int field, Object newValue) {
        throwExc();
    }

    public void setShortField(int stateFieldNo, short newValue) {
        throwExc();
    }

    public void setShortFieldAbs(int absFieldNo, short newValue) {
        throwExc();
    }

    public void setStringField(int stateFieldNo, String newValue) {
        throwExc();
    }

    public void setStringFieldAbs(int absFieldNo, String newValue) {
        throwExc();
    }

    public void unmanageSCOFields() {
        throwExc();
    }

    public void updateAutoSetFieldsCreated(Date now) {
        throwExc();
    }

    public void updateAutoSetFieldsModified(Date now, State oldState) {
        throwExc();
    }

    public void updateFrom(State state) {
        throwExc();
    }

    public void updateNonFilled(State state) {
        throwExc();
    }

    public void readExternal(ObjectInput in) {
        throwExc();
    }

    public void writeExternal(ObjectOutput out) {
        throwExc();
    }

    /**
     * Add all states referenced by fields in fg to the dcs.
     */
    public void addFetchGroupStatesToDCS(FetchGroup fg,
            DetachStateContainer dcs, VersantPersistenceManagerImp pm,
            OID oid) {
        throwExc();
    }

    public boolean isResolvedForClient(int stateFieldNo) {
        throwExc();
        return false;
    }

    public Object getOptimisticLockingValue() {
        throwExc();
        return null;
    }

    public void setFilled(int field) {
        throwExc();
    }

    public void fillForRead(State dest,
            VersantPersistenceManagerImp sm) {
        throwExc();
    }

    public void clearFilledFlags() {
        throwExc();
    }

    public void addOneToManyInverseFieldsForL2Evict(
            VersantPersistenceManagerImp pm) {
        throwExc();
    }

    public int getPass1FieldRefFieldNosWithNewOids(int[] stateFieldNoBuf) {
        throwExc();
        return 0;
    }

    public void writeExternal(OIDObjectOutput os) throws IOException {
        throwExc();
    }

    public void readExternal(OIDObjectInput is) throws IOException,
            ClassNotFoundException {
        throwExc();
    }
}
