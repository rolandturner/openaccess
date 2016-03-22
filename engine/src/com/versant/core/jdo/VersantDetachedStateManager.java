
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

import javax.jdo.PersistenceManager;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.BitSet;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.OID;

/**
 * State manager shared by a graph of detached instances.
 */
public class VersantDetachedStateManager implements VersantStateManager,
        Serializable {

    private transient boolean booleanField;
    private transient char charField;
    private transient byte byteField;
    private transient short shortField;
    private transient int intField;
    private transient long longField;
    private transient float floatField;
    private transient double doubleField;
    private transient String stringField;
    private transient Object objectField;
    private HashSet deleted = new HashSet();

    public byte replacingFlags(PersistenceCapable pc) {
        return 0;
    }

    public StateManager replacingStateManager(PersistenceCapable pc,
            StateManager sm) {
        return sm;
    }

    public boolean isDirty(PersistenceCapable pc) {
        return ((VersantDetachable)pc).versantIsDirty();
    }

    public boolean isTransactional(PersistenceCapable pc) {
        return false;
    }

    public boolean isPersistent(PersistenceCapable pc) {
        return false;
    }

    public boolean isNew(PersistenceCapable pc) {
        return false;
    }

    public boolean isDeleted(PersistenceCapable pc) {
        return deleted.contains(((VersantDetachable)pc).versantGetOID());
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        return null;
    }

    public void makeDirty(PersistenceCapable pc, String s) {
        ((VersantDetachable)pc).versantMakeDirty(s);
    }

    public Object getObjectId(PersistenceCapable pc) {
        return ((VersantDetachable)pc).versantGetOID();
    }

    public Object getTransactionalObjectId(PersistenceCapable pc) {
        return null;
    }

    public boolean isLoaded(PersistenceCapable pc, int field) {
        return ((VersantDetachable)pc).versantIsLoaded(field);
    }

    public void preSerialize(PersistenceCapable pc) {
    }

    public boolean getBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public char getCharField(PersistenceCapable pc, int field,
            char currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public byte getByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public short getShortField(PersistenceCapable pc, int field,
            short currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public int getIntField(PersistenceCapable pc, int field,
            int currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public long getLongField(PersistenceCapable pc, int field,
            long currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public float getFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public double getDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public String getStringField(PersistenceCapable pc, int field,
            String currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public Object getObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        if (!isLoaded(pc, field)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return currentValue;
    }

    public void setBooleanField(PersistenceCapable pc, int field,
            boolean currentValue, boolean newValue) {
        booleanField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setCharField(PersistenceCapable pc, int field,
            char currentValue, char newValue) {
        charField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setByteField(PersistenceCapable pc, int field,
            byte currentValue, byte newValue) {
        byteField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setShortField(PersistenceCapable pc, int field,
            short currentValue, short newValue) {
        shortField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setIntField(PersistenceCapable pc, int field,
            int currentValue, int newValue) {
        intField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setLongField(PersistenceCapable pc, int field,
            long currentValue, long newValue) {
        longField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setFloatField(PersistenceCapable pc, int field,
            float currentValue, float newValue) {
        floatField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setDoubleField(PersistenceCapable pc, int field,
            double currentValue, double newValue) {
        doubleField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setStringField(PersistenceCapable pc, int field,
            String currentValue, String newValue) {
        stringField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void setObjectField(PersistenceCapable pc, int field,
            Object currentValue, Object newValue) {
        objectField = newValue;
        ((VersantDetachable)pc).versantMakeDirty(field);
        pc.jdoReplaceField(field);
    }

    public void providedBooleanField(PersistenceCapable pc,
            int field, boolean currentValue) {
    }

    public void providedCharField(PersistenceCapable pc, int field,
            char currentValue) {
    }

    public void providedByteField(PersistenceCapable pc, int field,
            byte currentValue) {
    }

    public void providedShortField(PersistenceCapable pc,
            int field, short currentValue) {
    }

    public void providedIntField(PersistenceCapable pc, int field,
            int currentValue) {
    }

    public void providedLongField(PersistenceCapable pc, int field,
            long currentValue) {
    }

    public void providedFloatField(PersistenceCapable pc,
            int field, float currentValue) {
    }

    public void providedDoubleField(PersistenceCapable pc,
            int field, double currentValue) {
    }

    public void providedStringField(PersistenceCapable pc,
            int field, String currentValue) {
    }

    public void providedObjectField(PersistenceCapable pc,
            int field, Object currentValue) {
    }

    public boolean replacingBooleanField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return booleanField;
    }

    public char replacingCharField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return charField;
    }

    public byte replacingByteField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return byteField;
    }

    public short replacingShortField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return shortField;
    }

    public int replacingIntField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return intField;
    }

    public float replacingFloatField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return floatField;
    }

    public double replacingDoubleField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return doubleField;
    }

    public long replacingLongField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return longField;
    }

    public String replacingStringField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return stringField;
    }

    public Object replacingObjectField(final PersistenceCapable pc,
            final int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return objectField;
    }

    public void makeDirty(PersistenceCapable pc,
            int managedFieldNo) {
        ((VersantDetachable)pc).versantMakeDirty(managedFieldNo);
    }

    public void fillNewAppPKField(int fieldNo) {}

    public void versantAddDeleted(Object oid) {
        deleted.add(oid);
    }

    public Collection versantGetDeleted() {
        return deleted;
    }

    public PersistenceCapable getPersistenceCapable() {
        return null;
    }

    public OID getOID() {
        return null;
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
