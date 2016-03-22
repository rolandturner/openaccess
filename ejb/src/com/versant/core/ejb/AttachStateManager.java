
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.OID;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.VersantDetachable;
import com.versant.core.ejb.common.EntrySet;
import com.versant.core.metadata.FieldMetaData;

import java.util.BitSet;

/**
 * StateManager that is used for merging detached instances.
 */
public class AttachStateManager implements VersantStateManager {

    private StateManagerImp sm;
    private EntrySet mergeSet;


    public AttachStateManager() {
    }

    public StateManagerImp getSm() {
        return sm;
    }

    public void setSm(StateManagerImp sm) {
        this.sm = sm;
    }

    public byte replacingFlags(PersistenceCapable persistenceCapable) {
        return 0;
    }

    public StateManager replacingStateManager(
            PersistenceCapable persistenceCapable, StateManager stateManager) {
        return stateManager;
    }

    public boolean isDirty(PersistenceCapable persistenceCapable) {
        return ((VersantDetachable)persistenceCapable).versantIsDirty();
    }

    public boolean isTransactional(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isPersistent(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isNew(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isDeleted(PersistenceCapable persistenceCapable) {
        return false;
    }

    public PersistenceManager getPersistenceManager(
            PersistenceCapable persistenceCapable) {
        return null;
    }

    public void makeDirty(PersistenceCapable persistenceCapable, String s) {
        ((VersantDetachable)persistenceCapable).versantMakeDirty(s);
    }

    public Object getObjectId(PersistenceCapable persistenceCapable) {
        return null;
    }

    public Object getTransactionalObjectId(
            PersistenceCapable persistenceCapable) {
        return null;
    }

    public boolean isLoaded(PersistenceCapable persistenceCapable, int i) {
        return ((VersantDetachable)persistenceCapable).versantIsLoaded(i);
    }

    public void preSerialize(PersistenceCapable persistenceCapable) {

    }

    public boolean getBooleanField(PersistenceCapable persistenceCapable,
            int i, boolean b) {
        return b;
    }

    public char getCharField(PersistenceCapable persistenceCapable, int i,
            char c) {
        return c;
    }

    public byte getByteField(PersistenceCapable persistenceCapable, int i,
            byte b) {
        return b;
    }

    public short getShortField(PersistenceCapable persistenceCapable, int i,
            short i1) {
        return i1;
    }

    public int getIntField(PersistenceCapable persistenceCapable, int i,
            int i1) {
        return i1;
    }

    public long getLongField(PersistenceCapable persistenceCapable, int i,
            long l) {
        return l;
    }

    public float getFloatField(PersistenceCapable persistenceCapable, int i,
            float v) {
        return v;
    }

    public double getDoubleField(PersistenceCapable persistenceCapable, int i,
            double v) {
        return v;
    }

    public String getStringField(PersistenceCapable persistenceCapable, int i,
            String s) {
        return s;
    }

    public Object getObjectField(PersistenceCapable persistenceCapable, int i,
            Object o) {
        return o;
    }

    public void setBooleanField(PersistenceCapable persistenceCapable, int i,
            boolean b, boolean b1) {
    }

    public void setCharField(PersistenceCapable persistenceCapable, int i,
            char c, char c1) {
    }

    public void setByteField(PersistenceCapable persistenceCapable, int i,
            byte b, byte b1) {
    }

    public void setShortField(PersistenceCapable persistenceCapable, int i,
            short i1, short i2) {
    }

    public void setIntField(PersistenceCapable persistenceCapable, int i,
            int i1, int i2) {
    }

    public void setLongField(PersistenceCapable persistenceCapable, int i,
            long l, long l1) {
    }

    public void setFloatField(PersistenceCapable persistenceCapable, int i,
            float v, float v1) {
    }

    public void setDoubleField(PersistenceCapable persistenceCapable, int i,
            double v, double v1) {
    }

    public void setStringField(PersistenceCapable persistenceCapable, int i,
            String s, String s1) {
    }

    public void setObjectField(PersistenceCapable persistenceCapable, int i,
            Object o, Object val) {
    }

    public void providedBooleanField(PersistenceCapable persistenceCapable,
            int fieldNo, boolean newVal) {
        sm.setBooleanField(null, fieldNo, newVal, newVal);
    }

    public void providedCharField(PersistenceCapable persistenceCapable, int fieldNo,
            char newVal) {
        sm.setCharField(null, fieldNo, newVal, newVal);
    }

    public void providedByteField(PersistenceCapable persistenceCapable, int fieldNo,
            byte newVal) {
        sm.setByteField(null, fieldNo, newVal, newVal);
    }

    public void providedShortField(PersistenceCapable persistenceCapable,
            int fieldNo, short newVal) {
        sm.setShortField(null, fieldNo, newVal, newVal);
    }

    public void providedIntField(PersistenceCapable persistenceCapable, int fieldNo,
            int newVal) {
        sm.setIntField(null, fieldNo, newVal, newVal);
    }

    public void providedLongField(PersistenceCapable persistenceCapable, int fieldNo,
            long newVal) {
        sm.setLongField(null, fieldNo, newVal, newVal);
    }

    public void providedFloatField(PersistenceCapable persistenceCapable,
            int fieldNo, float newVal) {
        sm.setFloatField(null, fieldNo, newVal, newVal);
    }

    public void providedDoubleField(PersistenceCapable persistenceCapable,
            int fieldNo, double newVal) {
        sm.setDoubleField(null, fieldNo, newVal, newVal);
    }

    public void providedStringField(PersistenceCapable persistenceCapable,
            int fieldNo, String newVal) {
        sm.setStringField(null, fieldNo, newVal, newVal);
    }

    public void providedObjectField(PersistenceCapable persistenceCapable,
            int fieldNo, Object newVal) {
//        FieldMetaData fmd = sm.getFmd(fieldNo);
//
        sm.setObjectField(persistenceCapable, fieldNo, newVal, newVal);
    }



    public boolean replacingBooleanField(PersistenceCapable persistenceCapable,
            int i) {
        return false;
    }

    public char replacingCharField(PersistenceCapable persistenceCapable,
            int i) {
        return 0;
    }

    public byte replacingByteField(PersistenceCapable persistenceCapable,
            int i) {
        return 0;
    }

    public short replacingShortField(PersistenceCapable persistenceCapable,
            int i) {
        return 0;
    }

    public int replacingIntField(PersistenceCapable persistenceCapable, int i) {
        return 0;
    }

    public long replacingLongField(PersistenceCapable persistenceCapable,
            int i) {
        return 0;
    }

    public float replacingFloatField(PersistenceCapable persistenceCapable,
            int i) {
        return 0;
    }

    public double replacingDoubleField(PersistenceCapable persistenceCapable,
            int i) {
        return 0;
    }

    public String replacingStringField(PersistenceCapable persistenceCapable,
            int i) {
        return null;
    }

    public Object replacingObjectField(PersistenceCapable persistenceCapable,
            int i) {
        return null;
    }

    public void fillNewAppPKField(int fieldNo) {}

    public void makeDirty(PersistenceCapable pc,
            int managedFieldNo) {
        ((VersantDetachable)pc).versantMakeDirty(managedFieldNo);
    }

    public OID getOID() {
        return null;
    }

    public PersistenceCapable getPersistenceCapable() {
        return null;
    }

    public Object getVersion(PersistenceCapable pc) {
        return null;  //todo jdo2, implement this method
    }

    public void providedLoadedFieldList(PersistenceCapable pc, BitSet bitSet) {
        //todo jdo2, implement this method
    }

    public void providedModifiedFieldList(PersistenceCapable pc, BitSet bitSet) {
        //todo jdo2, implement this method
    }

    public BitSet replacingLoadedFieldList(PersistenceCapable pc, BitSet bitSet) {
        return null;  //todo jdo2, implement this method
    }

    public BitSet replacingModifiedFieldList(PersistenceCapable pc, BitSet bitSet) {
        return null;  //todo jdo2, implement this method
    }

    public Object replacingObjectId(PersistenceCapable pc, Object o) {
        return null;  //todo jdo2, implement this method
    }

    public Object replacingVersion(PersistenceCapable pc, Object o) {
        return null;  //todo jdo2, implement this method
    }

	public Object[] replacingDetachedState(Detachable arg0, Object[] arg1) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}



}
