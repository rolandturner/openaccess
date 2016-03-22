
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
import com.versant.core.common.State;
import com.versant.core.common.BindingSupportImpl;

import javax.jdo.spi.StateManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.Detachable;
import javax.jdo.PersistenceManager;
import java.util.BitSet;

/**
 * StateManager that is used to detach for embedded instance while the detaching is taking place.
 */
public class VersantDetachingEmbeddedStateManager implements StateManager {
    private State state;
    private FieldMetaData owningFmd;
    private DetachStateContainer detachStateContainer;

    public VersantDetachingEmbeddedStateManager(State state, FieldMetaData owningFmd,
            DetachStateContainer detachStateContainer) {
        if (state == null) throw new IllegalArgumentException("The state may not be null");
        this.state = state;
        this.owningFmd = owningFmd;
        this.detachStateContainer = detachStateContainer;
    }

    private FieldMetaData getFmd(int fieldNo) {
        return owningFmd.managedEmbeddedFields[fieldNo];
    }

    public Object replacingObjectField(PersistenceCapable pc, int field) {
        ((VersantDetachable)pc).versantSetLoaded(field);
        return detachStateContainer.getObjectFieldImp(state, getFmd(field),
                (VersantDetachable) pc);
    }

    public boolean replacingBooleanField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getBooleanField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }

    private void handleException(Exception x) {
        if( BindingSupportImpl.getInstance().isOwnException(x) ) {
            throw (RuntimeException)x;
        } else {
            throw BindingSupportImpl.getInstance().internal(x.getMessage(), x);
        }
    }

    public char replacingCharField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getCharField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public byte replacingByteField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getByteField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public short replacingShortField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getShortField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public int replacingIntField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getIntField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0;
    }

    public long replacingLongField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getLongField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0l;
    }

    public float replacingFloatField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getFloatField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0f;
    }

    public double replacingDoubleField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getDoubleField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return 0d;
    }

    public String replacingStringField(PersistenceCapable pc, int field) {
        try {
            ((VersantDetachable)pc).versantSetLoaded(field);
            return state.getStringField(getFmd(field).stateFieldNo);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public void setObjectField(PersistenceCapable pc, int field,
            Object currentValue, Object newValue) {
    }

    public void setBooleanField(PersistenceCapable pc, int field,
            boolean currentValue, boolean newValue) {
    }

    public void setCharField(PersistenceCapable pc, int field,
            char currentValue, char newValue) {
    }

    public void setByteField(PersistenceCapable pc, int field,
            byte currentValue, byte newValue) {
    }

    public void setShortField(PersistenceCapable pc, int field,
            short currentValue, short newValue) {
    }

    public void setIntField(PersistenceCapable pc, int field, int currentValue,
            int newValue) {
    }

    public void setLongField(PersistenceCapable pc, int field,
            long currentValue, long newValue) {
    }

    public void setFloatField(PersistenceCapable pc, int field,
            float currentValue, float newValue) {
    }

    public void setDoubleField(PersistenceCapable pc, int field,
            double currentValue, double newValue) {
    }

    public void setStringField(PersistenceCapable pc, int field,
            String currentValue, String newValue) {
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

    public StateManager replacingStateManager(PersistenceCapable pc,
            StateManager sm) {
        return sm;
    }

    public byte replacingFlags(PersistenceCapable pc) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDirty(PersistenceCapable pc) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTransactional(PersistenceCapable pc) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isPersistent(PersistenceCapable pc) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNew(PersistenceCapable pc) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isDeleted(PersistenceCapable pc) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable pc) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void makeDirty(PersistenceCapable pc, String fieldName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getObjectId(PersistenceCapable pc) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getTransactionalObjectId(PersistenceCapable pc) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getVersion(PersistenceCapable pc) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLoaded(PersistenceCapable pc, int field) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void preSerialize(PersistenceCapable pc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBooleanField(PersistenceCapable pc, int field,
            boolean currentValue) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public char getCharField(PersistenceCapable pc, int field,
            char currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte getByteField(PersistenceCapable pc, int field,
            byte currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public short getShortField(PersistenceCapable pc, int field,
            short currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIntField(PersistenceCapable pc, int field, int currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getLongField(PersistenceCapable pc, int field,
            long currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public float getFloatField(PersistenceCapable pc, int field,
            float currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getDoubleField(PersistenceCapable pc, int field,
            double currentValue) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getStringField(PersistenceCapable pc, int field,
            String currentValue) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void providedObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object[] replacingDetachedState(Detachable pc, Object[] state) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
