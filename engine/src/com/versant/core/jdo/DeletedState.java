
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.server.OIDGraph;

import javax.jdo.spi.PersistenceCapable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import com.versant.core.common.*;
import com.versant.core.util.IntArray;
import com.versant.core.util.OIDObjectOutput;
import com.versant.core.util.OIDObjectInput;

/**
 * This is a nonmutable state that is used for instance that is marked or deletion.
 * This will trap any read/write to and from state.
 */
public class DeletedState extends State {

    public boolean isCacheble() {
        return false;
    }

    public boolean isFieldNullorZero(int stateFieldNo) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean checkKeyFields(OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalBooleanFieldAbs(int field, boolean newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void findDirectEdges(OIDGraph graph,
            IntArray edges) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalIntField(int field, int newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getClassIndex() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void makeDirtyAbs(int fieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void makeDirty(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setDoubleField(int stateFieldNo, double newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int replaceSCOFields(PersistenceCapable owner,
            VersantPMProxy sm, int[] absFieldNos) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setCharField(int stateFieldNo, char newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void copyOptimisticLockingField(State state) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clear() {
    }

    public void setStringField(int stateFieldNo, String newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setBooleanFieldAbs(int absFieldNo, boolean newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void updateNonFilled(State state) {
    }

    public boolean isHollow() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalDoubleFieldAbs(int field, double newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public byte getByteField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearCollectionFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalShortFieldAbs(int field, short newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearDirtyFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int compareToPass1(State state) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean fillToStoreState(State stateToStore, PersistenceContext sm,
            VersantStateManager pcStateMan) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void updateAutoSetFieldsCreated(Date now) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean getBooleanFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void copyFieldsForOptimisticLocking(State state,
            VersantPersistenceManagerImp sm) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setByteFieldAbs(int absFieldNo, byte newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public char getCharField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalBooleanField(int field, boolean newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containFieldsAbs(int[] absFieldNos) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setBooleanField(int stateFieldNo, boolean newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setStringFieldAbs(int absFieldNo, String newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public long getLongFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setCharFieldAbs(int absFieldNo, char newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalByteFieldAbs(int field, byte newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }


    public void makeClean() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setClassMetaData(ClassMetaData cmd) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearNonFilled(State state) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalFloatFieldAbs(int field, float newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalObjectFieldAbs(int field, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearNonAutoSetFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public String getStringFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setIntFieldAbs(int absFieldNo, int newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getFieldNos(int[] buf) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void updateAutoSetFieldsModified(Date now, State oldState) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalStringFieldAbs(int field, String newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public double getDoubleFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setDoubleFieldAbs(int absFieldNo, double newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearSCOFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean hasSameFields(State state) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public short getShortFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setShortField(int stateFieldNo, short newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public String getStringField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void copyKeyFields(OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public byte getByteFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setFloatFieldAbs(int absFieldNo, float newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalShortField(int field, short newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsValidAppIdFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void addRefs(VersantPersistenceManagerImp sm, PCStateMan pcStateMan) {
    }

    public Object getObjectField(int stateFieldNo, PersistenceCapable owningPC,
            VersantPMProxy sm, OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public Object getInternalObjectField(int field) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void copyKeyFieldsUpdate(OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalCharField(int field, char newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalFloatField(int field, float newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalIntFieldAbs(int field, int newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalCharFieldAbs(int field, char newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public double getDoubleField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public float getFloatFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getPass1FieldNos(int[] buf) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public ClassMetaData getClassMetaData(ModelMetaData jmd) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public ClassMetaData getClassMetaData() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public char getCharFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public State getCopy() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalDoubleField(int field, double newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalLongFieldAbs(int field, long newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearApplicationIdentityFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean isEmpty() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public float getFloatField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalLongField(int field, long newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setLongFieldAbs(int absFieldNo, long newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setObjectField(int stateFieldNo, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalStringField(int field, String newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsPass2Fields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean isDirty() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean isDirty(int fieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void retrieve(VersantPersistenceManagerImp sm) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void clearTransactionNonPersistentFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setObjectFieldAbs(int absFieldNo, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setObjectFieldUnresolved(int field, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setObjectFieldUnresolvedAbs(int field, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalObjectField(int field, Object newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setShortFieldAbs(int absFieldNo, short newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsApplicationIdentityFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public Object getInternalObjectFieldAbs(int field) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public State newInstance() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public short getShortField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsFetchGroup(FetchGroup fetchGroup) {
        return true;
    }

    public Object getObjectFieldAbs(int absFieldNo,
            PersistenceCapable owningPC, VersantPMProxy sm,
            OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setFloatField(int stateFieldNo, float newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void updateFrom(State state) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getPass2FieldNos(int[] buf) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsPass1Fields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setIntField(int stateFieldNo, int newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getPass1FieldRefFieldNosWithNewOids(int[] stateFieldNoBuf) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean replaceNewObjectOIDs(int[] fieldNos, int fieldNosLength) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean hasSameNullFields(State state, State mask) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void unmanageSCOFields() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getIntField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public int getIntFieldAbs(int absFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setInternalByteField(int field, byte newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public String getVersion() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public long getLongField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public long getLongFieldInternal(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void copyFields(OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean isNull(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean getBooleanField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setLongField(int stateFieldNo, long newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containFields(int[] stateFieldNos) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public boolean containsField(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void setByteField(int stateFieldNo, byte newValue) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        //To change body of implemented methods use Options | File Templates.
    }

    /**
     * Add all states referenced by fields in fg to the dcs.
     */
    public void addFetchGroupStatesToDCS(FetchGroup fg,
            DetachStateContainer dcs, VersantPersistenceManagerImp pm,
            OID oid) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    /**
     * Is this state field nummber resolved for the Client
     */
    public boolean isResolvedForClient(int stateFieldNo) {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    /**
     * The value of the version field on the pc.
     * This will return null if there are no version fields.
     */
    public Object getOptimisticLockingValue() {
        throw BindingSupportImpl.getInstance().invalidOperation(
                "Not allowed to read/write to a instance marked for deletion");
    }

    private RuntimeException createBadMethodException() {
        return BindingSupportImpl.getInstance().internal(
                "method should not be called");
    }

    public void setFilled(int field) {
        throw createBadMethodException();
    }

    public void fillForRead(State dest,
            VersantPersistenceManagerImp sm) {
        throw createBadMethodException();
    }

    public void clearFilledFlags() {
        throw createBadMethodException();
    }

    public void addOneToManyInverseFieldsForL2Evict(
            VersantPersistenceManagerImp pm) {
        throw createBadMethodException();
    }

    public void writeExternal(OIDObjectOutput os) throws IOException {
        throw createBadMethodException();
    }

    public void readExternal(OIDObjectInput is) throws IOException,
            ClassNotFoundException {
        throw createBadMethodException();
    }
}
