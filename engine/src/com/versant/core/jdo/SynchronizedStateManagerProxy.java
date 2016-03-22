
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
import com.versant.core.common.OID;

import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.jdo.PersistenceManager;
import java.util.BitSet;



/**
 * Wraps a VersantStateManager adding synchronizing all methods on a lock
 * object. This is used for thread safe PMs.
 */
public final class SynchronizedStateManagerProxy
        implements VersantStateManager
 {

    private final Object lock;
    private final PCStateMan sm;

    public SynchronizedStateManagerProxy(Object lock, PCStateMan sm) {
        this.lock = lock;
        this.sm = sm;
    }

    public OID getOID() {
        return sm.getOID();
    }

    public PersistenceCapable getPersistenceCapable() {
        return sm.getPersistenceCapable();
    }

    public void makeDirty(PersistenceCapable persistenceCapable,
            int managedFieldNo) {
        synchronized (lock) {
            sm.makeDirty(persistenceCapable, managedFieldNo);
        }
    }

    public byte replacingFlags(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.replacingFlags(pc);
        }
    }

    public StateManager replacingStateManager(PersistenceCapable pc,
            StateManager smp) {
        synchronized (lock) {
            return sm.replacingStateManager(pc, smp);
        }
    }

    public boolean isDirty(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.isDirty(pc);
        }
    }

    public boolean isTransactional(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.isTransactional(pc);
        }
    }

    public boolean isPersistent(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.isPersistent(pc);
        }
    }

    public boolean isNew(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.isNew(pc);
        }
    }

    public boolean isDeleted(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.isDeleted(pc);
        }
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.getPersistenceManager(pc);
        }
    }

    public void makeDirty(PersistenceCapable pc, String fieldName) {
        synchronized (lock) {
            sm.makeDirty(pc, fieldName);
        }
    }

    public Object getObjectId(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.getObjectId(pc);
        }
    }

    public Object getTransactionalObjectId(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.getTransactionalObjectId(pc);
        }
    }

    public boolean isLoaded(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.isLoaded(pc, field);
        }
    }

    public void preSerialize(PersistenceCapable pc) {
        synchronized (lock) {
            sm.preSerialize(pc);
        }
    }

    public synchronized boolean getBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        synchronized (lock) {
            return sm.getBooleanField(pc, field, currentValue);
        }
    }

    public char getCharField(PersistenceCapable pc, int field,
            char currentValue) {
        synchronized (lock) {
            return sm.getCharField(pc, field, currentValue);
        }
    }

    public byte getByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        synchronized (lock) {
            return sm.getByteField(pc, field, currentValue);
        }
    }

    public short getShortField(PersistenceCapable pc, int field,
            short currentValue) {
        synchronized (lock) {
            return sm.getShortField(pc, field, currentValue);
        }
    }

    public int getIntField(PersistenceCapable pc, int field, int currentValue) {
        synchronized (lock) {
            return sm.getIntField(pc, field, currentValue);
        }
    }

    public long getLongField(PersistenceCapable pc, int field,
            long currentValue) {
        synchronized (lock) {
            return sm.getLongField(pc, field, currentValue);
        }
    }

    public float getFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        synchronized (lock) {
            return sm.getFloatField(pc, field, currentValue);
        }
    }

    public double getDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        synchronized (lock) {
            return sm.getDoubleField(pc, field, currentValue);
        }
    }

    public String getStringField(PersistenceCapable pc, int field,
            String currentValue) {
        synchronized (lock) {
            return sm.getStringField(pc, field,
                    currentValue);
        }
    }

    public Object getObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        synchronized (lock) {
            return sm.getObjectField(pc, field,
                    currentValue);
        }
    }

    public void setBooleanField(PersistenceCapable pc, int field,
            boolean currentValue, boolean newValue) {
        synchronized (lock) {
            sm.setBooleanField(pc, field, currentValue, newValue);
        }
    }

    public void setCharField(PersistenceCapable pc, int field, char currentValue,
            char newValue) {
        synchronized (lock) {
            sm.setCharField(pc, field, currentValue, newValue);
        }
    }

    public void setByteField(PersistenceCapable pc, int field, byte currentValue,
            byte newValue) {
        synchronized (lock) {
            sm.setByteField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setShortField(PersistenceCapable pc, int field, short currentValue,
            short newValue) {
        synchronized (lock) {
            sm.setShortField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setIntField(PersistenceCapable pc, int field, int currentValue,
            int newValue) {
        synchronized (lock) {
            sm.setIntField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setLongField(PersistenceCapable pc, int field, long currentValue,
            long newValue) {
        synchronized (lock) {
            sm.setLongField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setFloatField(PersistenceCapable pc, int field, float currentValue,
            float newValue) {
        synchronized (lock) {
            sm.setFloatField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setDoubleField(PersistenceCapable pc, int field, double currentValue,
            double newValue) {
        synchronized (lock) {
            sm.setDoubleField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setStringField(PersistenceCapable pc, int field, String currentValue,
            String newValue) {
        synchronized (lock) {
            sm.setStringField(pc, field, currentValue,
                    newValue);
        }
    }

    public void setObjectField(PersistenceCapable pc, int field, Object currentValue,
            Object newValue) {
        synchronized (lock) {
            sm.setObjectField(pc, field, currentValue,
                    newValue);
        }
    }

    public void providedBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        synchronized (lock) {
            sm.providedBooleanField(pc, field, currentValue);
        }
    }

    public void providedCharField(PersistenceCapable pc, int field,
            char currentValue) {
        synchronized (lock) {
            sm.providedCharField(pc, field, currentValue);
        }
    }

    public void providedByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        synchronized (lock) {
            sm.providedByteField(pc, field, currentValue);
        }
    }

    public void providedShortField(PersistenceCapable pc, int field,
            short currentValue) {
        synchronized (lock) {
            sm.providedShortField(pc, field, currentValue);
        }
    }

    public void providedIntField(PersistenceCapable pc, int field,
            int currentValue) {
        synchronized (lock) {
            sm.providedIntField(pc, field, currentValue);
        }
    }

    public void providedLongField(PersistenceCapable pc, int field,
            long currentValue) {
        synchronized (lock) {
            sm.providedLongField(pc, field, currentValue);
        }
    }

    public void providedFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        synchronized (lock) {
            sm.providedFloatField(pc, field, currentValue);
        }
    }

    public void providedDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        synchronized (lock) {
            sm.providedDoubleField(pc, field, currentValue);
        }
    }

    public void providedStringField(PersistenceCapable pc, int field,
            String currentValue) {
        synchronized (lock) {
            sm.providedStringField(pc, field, currentValue);
        }
    }

    public void providedObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        synchronized (lock) {
            sm.providedObjectField(pc, field, currentValue);
        }
    }

    public boolean replacingBooleanField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingBooleanField(pc, field);
        }
    }

    public char replacingCharField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingCharField(pc, field);
        }
    }

    public byte replacingByteField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingByteField(pc, field);
        }
    }

    public short replacingShortField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingShortField(pc, field);
        }
    }

    public int replacingIntField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingIntField(pc, field);
        }
    }

    public long replacingLongField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingLongField(pc, field);
        }
    }

    public float replacingFloatField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingFloatField(pc, field);
        }
    }

    public double replacingDoubleField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingDoubleField(pc, field);
        }
    }

    public String replacingStringField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingStringField(pc, field);
        }
    }

    public Object replacingObjectField(PersistenceCapable pc, int field) {
        synchronized (lock) {
            return sm.replacingObjectField(pc, field);
        }
    }

    public void fillNewAppPKField(int fieldNo) {
        synchronized (lock) {
            sm.fillNewAppPKField(fieldNo);
        }
    }

    public Object getVersion(PersistenceCapable pc) {
        synchronized (lock) {
            return sm.getVersion(pc);
        }
    }

    public void providedLoadedFieldList(PersistenceCapable pc, BitSet bitSet) {
        synchronized (lock) {
            sm.providedLoadedFieldList(pc, bitSet);
        }
    }

    public void providedModifiedFieldList(PersistenceCapable pc, BitSet bitSet) {
        synchronized (lock) {
            sm.providedModifiedFieldList(pc, bitSet);
        }
    }

    public BitSet replacingLoadedFieldList(PersistenceCapable pc, BitSet bitSet) {
        synchronized (lock) {
            return sm.replacingLoadedFieldList(pc, bitSet);
        }
    }

    public BitSet replacingModifiedFieldList(PersistenceCapable pc, BitSet bitSet) {
        synchronized (lock) {
            return sm.replacingModifiedFieldList(pc, bitSet);
        }
    }

    public Object replacingObjectId(PersistenceCapable pc, Object o) {
        synchronized (lock) {
            return sm.replacingObjectId(pc, o);
        }
    }

    public Object replacingVersion(PersistenceCapable pc, Object o) {
        synchronized (lock) {
            return sm.replacingVersion(pc, o);
        }
    }

	public Object[] replacingDetachedState(Detachable arg0, Object[] arg1) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}



}

