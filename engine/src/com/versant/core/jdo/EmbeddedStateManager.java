
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;

import javax.jdo.spi.Detachable;
import javax.jdo.spi.StateManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.PersistenceManager;
import java.util.BitSet;



/**
 */
public class EmbeddedStateManager implements StateManager
 {
    public PCStateMan owner;
    public PersistenceCapable pc;
    public ClassMetaData cmd;
    public ModelMetaData jmd;
    public FieldMetaData owningFmd;
    /**
     * field to replace the flags of the pc instance.
     */
    private byte jdoFlags = PersistenceCapable.LOAD_REQUIRED;

    public EmbeddedStateManager(PCStateMan owner, ModelMetaData jmd,
            JDOImplHelper jdoImplHelper, FieldMetaData fmd) {
        Class cls = fmd.typeMetaData.cls;
        owningFmd = fmd;
        this.jmd = jmd;
        this.owner = owner;
        pc = jdoImplHelper.newInstance(cls, this);
        cmd = jmd.getClassMetaData(cls);
    }

    public EmbeddedStateManager(PCStateMan owner, PersistenceCapable pc,
            ModelMetaData jmd, FieldMetaData fmd) {
        this.owningFmd = fmd;
        this.jmd = jmd;
        this.owner = owner;
        this.pc = pc;
        cmd = jmd.getClassMetaData(/*CHFC*/pc.getClass()/*RIGHTPAR*/);
    }

    public void setLoadRequired() {
        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
        pc.jdoReplaceFlags();
    }

    public byte replacingFlags(PersistenceCapable pc) {
        return jdoFlags;
    }

    public StateManager replacingStateManager(PersistenceCapable pc, StateManager sm) {
        return sm;
    }

    public boolean isDirty(PersistenceCapable pc) {
        return owner.isDirty();
    }

    public boolean isTransactional(PersistenceCapable pc) {
        return owner.isTransactional(pc);
    }

    public boolean isPersistent(PersistenceCapable pc) {
        return owner.isPersistent(pc);
    }

    public boolean isNew(PersistenceCapable pc) {
        return owner.isNew(pc);
    }

    public boolean isDeleted(PersistenceCapable pc) {
        return owner.isDeleted(pc);
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        return owner.getPersistenceManager(pc);
    }

    public void makeDirty(PersistenceCapable pc, String fieldName) {
        owner.makeDirty(pc, fieldName);
    }

    public Object getObjectId(PersistenceCapable pc) {
        throwNotImplemented();
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getTransactionalObjectId(PersistenceCapable pc) {
        throwNotImplemented();
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLoaded(PersistenceCapable pc, int field) {
        return owner.isLoaded(pc, getFmd(field));
    }

    public void preSerialize(PersistenceCapable pc) {
        throwNotImplemented();
    }

    public boolean getBooleanField(PersistenceCapable pc, int field, boolean currentValue) {
        return owner.getBooleanFieldImp(pc, getFmd(field), currentValue);
    }

    public char getCharField(PersistenceCapable pc, int field, char currentValue) {
        return owner.getCharFieldImp(pc, getFmd(field), currentValue);
    }

    public byte getByteField(PersistenceCapable pc, int field, byte currentValue) {
        return owner.getByteFieldImp(pc, getFmd(field), currentValue);
    }

    public short getShortField(PersistenceCapable pc, int field, short currentValue) {
        return owner.getShortFieldImp(pc, getFmd(field), currentValue);
    }

    public int getIntField(PersistenceCapable pc, int field, int currentValue) {
        return owner.getIntFieldImp(pc, getFmd(field), currentValue);
    }

    public long getLongField(PersistenceCapable pc, int field, long currentValue) {
        return owner.getLongFieldImp(pc, getFmd(field), currentValue);
    }

    public float getFloatField(PersistenceCapable pc, int field, float currentValue) {
        return owner.getFloatFieldImp(pc, getFmd(field), currentValue);
    }

    public double getDoubleField(PersistenceCapable pc, int field, double currentValue) {
        return owner.getDoubleFieldImp(pc, getFmd(field), currentValue);
    }

    public String getStringField(PersistenceCapable pc, int field, String currentValue) {
        return owner.getStringFieldImp(pc, getFmd(field), currentValue);
    }

    public Object getObjectField(PersistenceCapable pc, int field, Object currentValue) {
        return owner.getObjectFieldImp(pc, getFmd(field), currentValue);
    }

    public void setBooleanField(PersistenceCapable pc, int field, boolean currentValue, boolean newValue) {
        owner.setBooleanFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setCharField(PersistenceCapable pc, int field, char currentValue, char newValue) {
        owner.setCharFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setByteField(PersistenceCapable pc, int field, byte currentValue, byte newValue) {
        owner.setByteFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setShortField(PersistenceCapable pc, int field, short currentValue, short newValue) {
        owner.setShortFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setIntField(PersistenceCapable pc, int field, int currentValue, int newValue) {
        owner.setIntFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setLongField(PersistenceCapable pc, int field, long currentValue, long newValue) {
        owner.setLongFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setFloatField(PersistenceCapable pc, int field, float currentValue, float newValue) {
        owner.setFloatFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setDoubleField(PersistenceCapable pc, int field, double currentValue, double newValue) {
        owner.setDoubleFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setStringField(PersistenceCapable pc, int field,
            String currentValue, String newValue) {
        owner.setStringFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }

    public void setObjectField(PersistenceCapable pc, int field, Object currentValue, Object newValue) {
        owner.setObjectFieldImp(this.pc, getFmd(field), currentValue, newValue);
        pc.jdoReplaceField(field);
    }



    private FieldMetaData getFmd(int fieldNo) {
        return owningFmd.managedEmbeddedFields[fieldNo];
    }

    public void providedBooleanField(PersistenceCapable pc, int field, boolean currentValue) {
        owner.providedBooleanFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedCharField(PersistenceCapable pc, int field, char currentValue) {
        owner.providedCharFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedByteField(PersistenceCapable pc, int field, byte currentValue) {
        owner.providedByteFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedShortField(PersistenceCapable pc, int field, short currentValue) {
        owner.providedShortFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedIntField(PersistenceCapable pc, int field, int currentValue) {
        owner.providedIntFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedLongField(PersistenceCapable pc, int field, long currentValue) {
        owner.providedLongFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedFloatField(PersistenceCapable pc, int field, float currentValue) {
        owner.providedFloatFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedDoubleField(PersistenceCapable pc, int field, double currentValue) {
        owner.providedDoubleFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedStringField(PersistenceCapable pc, int field, String currentValue) {
        owner.providedStringFieldImp(pc, getFmd(field), currentValue);
    }

    public void providedObjectField(PersistenceCapable pc, int field, Object currentValue) {
        owner.providedObjectFieldImp(pc, getFmd(field), currentValue);
    }

    public boolean replacingBooleanField(PersistenceCapable pc, int field) {
        return owner.replacingBooleanFieldImp(pc, getFmd(field));
    }

    public char replacingCharField(PersistenceCapable pc, int field) {
        return owner.replacingCharFieldImp(pc, getFmd(field));
    }

    public byte replacingByteField(PersistenceCapable pc, int field) {
        return owner.replacingByteFieldImp(pc, getFmd(field));
    }

    public short replacingShortField(PersistenceCapable pc, int field) {
        return owner.replacingShortFieldImp(pc, getFmd(field));
    }

    public int replacingIntField(PersistenceCapable pc, int field) {
        return owner.replacingIntFieldImp(pc, getFmd(field));
    }

    public long replacingLongField(PersistenceCapable pc, int field) {
        return owner.replacingLongFieldImp(pc, getFmd(field));
    }

    public float replacingFloatField(PersistenceCapable pc, int field) {
        return owner.replacingFloatFieldImp(pc, getFmd(field));
    }

    public double replacingDoubleField(PersistenceCapable pc, int field) {
        return owner.replacingDoubleFieldImp(pc, getFmd(field));
    }

    public String replacingStringField(PersistenceCapable pc, int field) {
        return owner.replacingStringFieldImp(pc, getFmd(field));
    }

    public Object replacingObjectField(PersistenceCapable pc, int field) {
        return owner.replacingObjectFieldImp(pc, getFmd(field));
    }

    public Object getVersion(PersistenceCapable pc) {
        return owner.getVersion(pc);
    }

    public void providedLoadedFieldList(PersistenceCapable pc, BitSet bitSet) {
        owner.providedLoadedFieldList(pc,bitSet);
    }

    public void providedModifiedFieldList(PersistenceCapable pc, BitSet bitSet) {
        owner.providedModifiedFieldList(pc, bitSet);
    }

    public BitSet replacingLoadedFieldList(PersistenceCapable pc, BitSet bitSet) {
        return owner.replacingLoadedFieldList(pc, bitSet);
    }

    public BitSet replacingModifiedFieldList(PersistenceCapable pc, BitSet bitSet) {
        return owner.replacingModifiedFieldList(pc, bitSet);
    }

    public Object replacingObjectId(PersistenceCapable pc, Object o) {
        return owner.replacingObjectId(pc, o);
    }

    public Object replacingVersion(PersistenceCapable pc, Object o) {
        return owner.replacingVersion(pc, o);
    }

    private void throwNotImplemented() {
        throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
    }

	public Object[] replacingDetachedState(Detachable arg0, Object[] arg1) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}









}
