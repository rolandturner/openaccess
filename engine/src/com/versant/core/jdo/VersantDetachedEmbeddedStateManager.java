
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

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.BindingSupportImpl;

import javax.jdo.spi.StateManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.PersistenceManager;
import java.util.BitSet;

/**
 * StateManager for a detached embedded instance.
 */
public class VersantDetachedEmbeddedStateManager implements StateManager {
    private FieldMetaData owningFmd;
    private PersistenceCapable embeddedPc;
    private PersistenceCapable owningPc;

    private static ThreadLocal tLocal = new ThreadLocal();



    public VersantDetachedEmbeddedStateManager(PersistenceCapable owningPc,
            FieldMetaData owningFmd) {
        this.owningFmd = owningFmd;
        embeddedPc = JDOImplHelper.getInstance().newInstance(
                owningFmd.typeMetaData.cls, this);
        this.owningPc = owningPc;
    }

    public PersistenceCapable getEmbeddedPc() {
        return embeddedPc;
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

    public Object replacingObjectField(PersistenceCapable pc, int field) {
        return getDStruct().objectField;
    }

    public boolean replacingBooleanField(PersistenceCapable pc, int field) {
        return getDStruct().booleanField;
    }

    public char replacingCharField(PersistenceCapable pc, int field) {
        return getDStruct().charField;
    }

    public byte replacingByteField(PersistenceCapable pc, int field) {
        return getDStruct().byteField;
    }

    public short replacingShortField(PersistenceCapable pc, int field) {
        return getDStruct().shortField;
    }

    public int replacingIntField(PersistenceCapable pc, int field) {
        return getDStruct().intField;
    }

    public long replacingLongField(PersistenceCapable pc, int field) {
        return getDStruct().longField;
    }

    public float replacingFloatField(PersistenceCapable pc, int field) {
        return getDStruct().floatField;
    }

    public double replacingDoubleField(PersistenceCapable pc, int field) {
        return getDStruct().doubleField;
    }

    public String replacingStringField(PersistenceCapable pc, int field) {
        return getDStruct().stringField;
    }

    private DataStruct getDStruct() {

        DataStruct dStruct = (DataStruct) tLocal.get();
        if (dStruct == null) {
            tLocal.set(dStruct = new DataStruct());
        }
        return dStruct;


    }

    private void unsetDStruct() {

        tLocal.set(null);


    }

    public void setObjectField(PersistenceCapable pc, int field,
            Object currentValue, Object newValue) {
        try {
            getDStruct().objectField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setBooleanField(PersistenceCapable pc, int field,
            boolean currentValue, boolean newValue) {
        try {
            getDStruct().booleanField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setCharField(PersistenceCapable pc, int field,
            char currentValue, char newValue) {
        try {
            getDStruct().charField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setByteField(PersistenceCapable pc, int field,
            byte currentValue, byte newValue) {
        try {
            getDStruct().byteField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setShortField(PersistenceCapable pc, int field,
            short currentValue, short newValue) {
        try {
            getDStruct().shortField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setIntField(PersistenceCapable pc, int field, int currentValue,
            int newValue) {
        try {
            getDStruct().intField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setLongField(PersistenceCapable pc, int field,
            long currentValue, long newValue) {
        try {
            getDStruct().longField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setFloatField(PersistenceCapable pc, int field,
            float currentValue, float newValue) {
        try {
            getDStruct().floatField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setDoubleField(PersistenceCapable pc, int field,
            double currentValue, double newValue) {
        try {
            getDStruct().doubleField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public void setStringField(PersistenceCapable pc, int field,
            String currentValue, String newValue) {
        try {
            getDStruct().stringField = newValue;
            ((VersantDetachable)pc).versantMakeDirty(field);
            ((VersantDetachable)pc).versantSetLoaded(field);
            pc.jdoReplaceField(field);
        } finally {
            unsetDStruct();
        }
    }

    public StateManager replacingStateManager(PersistenceCapable pc,
            StateManager sm) {
        return sm;
    }



    public byte replacingFlags(PersistenceCapable pc) {
        return 0;
    }

    public boolean isDirty(PersistenceCapable pc) {
        return ((VersantDetachable)pc).versantIsDirty();
    }

    public boolean isTransactional(PersistenceCapable pc) {
        return false;
    }

    public boolean isPersistent(PersistenceCapable pc) {
        return true;
    }

    public boolean isNew(PersistenceCapable pc) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDeleted(PersistenceCapable pc) {
        return false;
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        return null;
    }

    public void makeDirty(PersistenceCapable pc, String fieldName) {
        ((VersantDetachable)pc).versantMakeDirty(fieldName);
    }

    /**
     * A embedded instance does not have an id. So this does not return anything.
     * @param pc
     */
    public Object getObjectId(PersistenceCapable pc) {
        return null;
    }

    public Object getTransactionalObjectId(PersistenceCapable pc) {
        return null;
    }

    public Object getVersion(PersistenceCapable pc) {
        return ((VersantDetachable)owningPc).versantGetVersion();
    }

    public boolean isLoaded(PersistenceCapable pc, int field) {
        if (((VersantDetachable)pc).versantIsLoaded(field)) {
            return true;
        } else {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
    }

    public void preSerialize(PersistenceCapable pc) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    public int getIntField(PersistenceCapable pc, int field, int currentValue) {
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

    public void providedBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedCharField(PersistenceCapable pc, int field,
            char currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedShortField(PersistenceCapable pc, int field,
            short currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedIntField(PersistenceCapable pc, int field,
            int currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedLongField(PersistenceCapable pc, int field,
            long currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedStringField(PersistenceCapable pc, int field,
            String currentValue) {
    }

    public void providedObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
    }

    public Object[] replacingDetachedState(Detachable pc, Object[] state) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class DataStruct {
        public transient boolean booleanField;
        public transient char charField;
        public transient byte byteField;
        public transient short shortField;
        public transient int intField;
        public transient long longField;
        public transient float floatField;
        public transient double doubleField;
        public transient String stringField;
        public transient Object objectField;
    }
}
