
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
 * This is a nonmutable state that is used to obtain the default values for fields.
 */
public class InitState extends State {

    public boolean isCacheble() {
        return false;
    }

    public boolean isFieldNullorZero(int stateFieldNo) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean checkKeyFields(OID oid) {
        throw createBadMethodException();
    }

    public void setInternalBooleanFieldAbs(int field, boolean newValue) {
        throw createBadMethodException();
    }

    public void findDirectEdges(OIDGraph graph,
            IntArray edges) {
        throw createBadMethodException();
    }

    public void setInternalIntField(int field, int newValue) {
        throw createBadMethodException();
    }

    public int getClassIndex() {
        throw createBadMethodException();
    }

    public void makeDirtyAbs(int fieldNo) {
        throw createBadMethodException();
    }

    public void makeDirty(int stateFieldNo) {
        throw createBadMethodException();
    }

    public void setDoubleField(int stateFieldNo, double newValue) {
        throw createBadMethodException();
    }

    public int replaceSCOFields(PersistenceCapable owner,
            VersantPMProxy sm, int[] absFieldNos) {
        throw createBadMethodException();
    }

    public void setCharField(int stateFieldNo, char newValue) {
        throw createBadMethodException();
    }

    public void copyOptimisticLockingField(State state) {
        throw createBadMethodException();
    }

    public void clear() {
    }

    public void setStringField(int stateFieldNo, String newValue) {
        throw createBadMethodException();
    }

    public void setBooleanFieldAbs(int absFieldNo, boolean newValue) {
        throw createBadMethodException();
    }

    public void updateNonFilled(State state) {
        throw createBadMethodException();
    }

    public boolean isHollow() {
        throw createBadMethodException();
    }

    public void setInternalDoubleFieldAbs(int field, double newValue) {
        throw createBadMethodException();
    }

    public byte getByteField(int stateFieldNo) {
        return 0;
    }

    public void clearCollectionFields() {
        throw createBadMethodException();
    }

    public void setInternalShortFieldAbs(int field, short newValue) {
        throw createBadMethodException();
    }

    public void clearDirtyFields() {
        throw createBadMethodException();
    }

    public int compareToPass1(State state) {
        throw createBadMethodException();
    }

    public boolean fillToStoreState(State stateToStore, PersistenceContext sm, VersantStateManager pcStateMan) {
        throw createBadMethodException();
    }

    public void updateAutoSetFieldsCreated(Date now) {
        throw createBadMethodException();
    }

    public boolean getBooleanFieldAbs(int absFieldNo) {
        return false;
    }

    public void copyFieldsForOptimisticLocking(State state,
            VersantPersistenceManagerImp sm) {
        throw createBadMethodException();
    }

    public void setByteFieldAbs(int absFieldNo, byte newValue) {
        throw createBadMethodException();
    }

    public char getCharField(int stateFieldNo) {
        return 0;
    }

    public void setInternalBooleanField(int field, boolean newValue) {
        throw createBadMethodException();
    }

    public boolean containFieldsAbs(int[] absFieldNos) {
        throw createBadMethodException();
    }

    public int getPass1FieldRefFieldNosWithNewOids(int[] stateFieldNoBuf) {
        throw createBadMethodException();
    }

    public void setBooleanField(int stateFieldNo, boolean newValue) {
        throw createBadMethodException();
    }

    public void setStringFieldAbs(int absFieldNo, String newValue) {
        throw createBadMethodException();
    }

    public long getLongFieldAbs(int absFieldNo) {
        return 0;
    }

    public void setCharFieldAbs(int absFieldNo, char newValue) {
        throw createBadMethodException();
    }

    public void setInternalByteFieldAbs(int field, byte newValue) {
        throw createBadMethodException();
    }

    public void makeClean() {
        throw createBadMethodException();
    }

    public boolean containsFieldAbs(int absFieldNo) {
        return true;
    }

    public void setClassMetaData(ClassMetaData cmd) {
        throw createBadMethodException();
    }

    public void clearNonFilled(State state) {
        throw createBadMethodException();
    }

    public void setInternalFloatFieldAbs(int field, float newValue) {
        throw createBadMethodException();
    }

    public void setInternalObjectFieldAbs(int field, Object newValue) {
        throw createBadMethodException();
    }

    public void clearNonAutoSetFields() {
        throw createBadMethodException();
    }

    public String getStringFieldAbs(int absFieldNo) {
        return null;
    }

    public void setIntFieldAbs(int absFieldNo, int newValue) {
        throw createBadMethodException();
    }

    public int getFieldNos(int[] buf) {
        throw createBadMethodException();
    }

    public void updateAutoSetFieldsModified(Date now, State oldState) {
        throw createBadMethodException();
    }

    public void setInternalStringFieldAbs(int field, String newValue) {
        throw createBadMethodException();
    }

    public double getDoubleFieldAbs(int absFieldNo) {
        return 0;
    }

    public void setDoubleFieldAbs(int absFieldNo, double newValue) {
        throw createBadMethodException();
    }

    public void clearSCOFields() {
        throw createBadMethodException();
    }

    public boolean hasSameFields(State state) {
        throw createBadMethodException();
    }

    public short getShortFieldAbs(int absFieldNo) {
        return 0;
    }

    public void setShortField(int stateFieldNo, short newValue) {
        throw createBadMethodException();
    }

    public String getStringField(int stateFieldNo) {
        return null;
    }

    public void copyKeyFields(OID oid) {
        throw createBadMethodException();
    }

    public byte getByteFieldAbs(int absFieldNo) {
        return 0;
    }

    public void setFloatFieldAbs(int absFieldNo, float newValue) {
        throw createBadMethodException();
    }

    public void setInternalShortField(int field, short newValue) {
        throw createBadMethodException();
    }

    public boolean containsValidAppIdFields() {
        throw createBadMethodException();
    }

    public void addRefs(VersantPersistenceManagerImp sm, PCStateMan pcStateMan) {
    }

    public Object getObjectField(int stateFieldNo, PersistenceCapable owningPC,
            VersantPMProxy sm, OID oid) {
        return null;
    }

    public Object getInternalObjectField(int field) {
        return null;
    }

    public void copyKeyFieldsUpdate(OID oid) {
        throw createBadMethodException();
    }

    public void setInternalCharField(int field, char newValue) {
        throw createBadMethodException();
    }

    public void setInternalFloatField(int field, float newValue) {
        throw createBadMethodException();
    }

    public void setInternalIntFieldAbs(int field, int newValue) {
        throw createBadMethodException();
    }

    public void setInternalCharFieldAbs(int field, char newValue) {
        throw createBadMethodException();
    }

    public double getDoubleField(int stateFieldNo) {
        return 0;
    }

    public float getFloatFieldAbs(int absFieldNo) {
        return 0;
    }

    public int getPass1FieldNos(int[] buf) {
        throw createBadMethodException();
    }

    public ClassMetaData getClassMetaData(ModelMetaData jmd) {
        throw createBadMethodException();
    }

    public ClassMetaData getClassMetaData() {
        throw createBadMethodException();
    }

    public char getCharFieldAbs(int absFieldNo) {
        return 0;
    }

    public State getCopy() {
        throw createBadMethodException();
    }

    public void setInternalDoubleField(int field, double newValue) {
        throw createBadMethodException();
    }

    public void setInternalLongFieldAbs(int field, long newValue) {
        throw createBadMethodException();
    }

    public void clearApplicationIdentityFields() {
        throw createBadMethodException();
    }

    public boolean isEmpty() {
        throw createBadMethodException();
    }

    public float getFloatField(int stateFieldNo) {
        return 0;
    }

    public void setInternalLongField(int field, long newValue) {
        throw createBadMethodException();
    }

    public void setLongFieldAbs(int absFieldNo, long newValue) {
        throw createBadMethodException();
    }

    public void setObjectField(int stateFieldNo, Object newValue) {
        throw createBadMethodException();
    }

    public void setInternalStringField(int field, String newValue) {
        throw createBadMethodException();
    }

    public boolean containsPass2Fields() {
        throw createBadMethodException();
    }

    public boolean isDirty() {
        throw createBadMethodException();
    }

    public boolean isDirty(int fieldNo) {
        throw createBadMethodException();
    }

    public void retrieve(VersantPersistenceManagerImp sm) {
        throw createBadMethodException();
    }

    public void clearTransactionNonPersistentFields() {
        throw createBadMethodException();
    }

    public void setObjectFieldAbs(int absFieldNo, Object newValue) {
        throw createBadMethodException();
    }

    public void setObjectFieldUnresolved(int field, Object newValue) {
        throw createBadMethodException();
    }

    public void setObjectFieldUnresolvedAbs(int field, Object newValue) {
        throw createBadMethodException();
    }

    public void setInternalObjectField(int field, Object newValue) {
        throw createBadMethodException();
    }

    public void setShortFieldAbs(int absFieldNo, short newValue) {
        throw createBadMethodException();
    }

    public boolean containsApplicationIdentityFields() {
        throw createBadMethodException();
    }

    public Object getInternalObjectFieldAbs(int field) {
        return null;
    }

    public State newInstance() {
        throw createBadMethodException();
    }

    public short getShortField(int stateFieldNo) {
        return 0;
    }

    public boolean containsFetchGroup(FetchGroup fetchGroup) {
        throw createBadMethodException();
    }

    public Object getObjectFieldAbs(int absFieldNo,
            PersistenceCapable owningPC, VersantPMProxy sm,
            OID oid) {
        return null;
    }

    public void setFloatField(int stateFieldNo, float newValue) {
        throw createBadMethodException();
    }

    public void updateFrom(State state) {
        throw createBadMethodException();
    }

    public int getPass2FieldNos(int[] buf) {
        throw createBadMethodException();
    }

    public boolean containsPass1Fields() {
        throw createBadMethodException();
    }

    public void setIntField(int stateFieldNo, int newValue) {
        throw createBadMethodException();
    }

    public boolean replaceNewObjectOIDs(int[] fieldNos, int fieldNosLength) {
        throw createBadMethodException();
    }

    public boolean hasSameNullFields(State state, State mask) {
        throw createBadMethodException();
    }

    public void unmanageSCOFields() {
        throw createBadMethodException();
    }

    public int getIntField(int stateFieldNo) {
        return 0;
    }

    public int getIntFieldAbs(int absFieldNo) {
        return 0;
    }

    public void setInternalByteField(int field, byte newValue) {
        throw createBadMethodException();
    }

    public String getVersion() {
        throw createBadMethodException();
    }

    public long getLongField(int stateFieldNo) {
        return 0;
    }

    public long getLongFieldInternal(int stateFieldNo) {
        return 0;
    }

    public void copyFields(OID oid) {
        throw createBadMethodException();
    }

    public boolean isNull(int stateFieldNo) {
        throw createBadMethodException();
    }

    public boolean getBooleanField(int stateFieldNo) {
        return false;
    }

    public void setLongField(int stateFieldNo, long newValue) {
        throw createBadMethodException();
    }

    public boolean containFields(int[] stateFieldNos) {
        throw createBadMethodException();
    }

    public boolean containsField(int stateFieldNo) {
        return true;
    }

    public void setByteField(int stateFieldNo, byte newValue) {
        throw createBadMethodException();
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
        throw createBadMethodException();
    }

    /**
     * Is this state field nummber resolved for the Client
     */
    public boolean isResolvedForClient(int stateFieldNo) {
        throw createBadMethodException();
    }

    /**
     * The value of the version field on the pc.
     * This will return null if there are no version fields.
     */
    public Object getOptimisticLockingValue() {
        throw createBadMethodException();
    }

    private RuntimeException createBadMethodException() {
        return BindingSupportImpl.getInstance().internal(
                "This method should not be called");
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
