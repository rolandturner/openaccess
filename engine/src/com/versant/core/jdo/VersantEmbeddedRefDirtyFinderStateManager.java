
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
import com.versant.core.metadata.ClassMetaData;

import javax.jdo.spi.StateManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.Detachable;
import javax.jdo.PersistenceManager;
import java.util.BitSet;

/**
 * The purpose of this statemanager is to find out if there are any embedded
 * instances that are dirty as navigatable from a root instance.
 */
public class VersantEmbeddedRefDirtyFinderStateManager implements StateManager {
    private VersantDetachable providedDetachable;

    public boolean checkDirty(VersantDetachable vd, ClassMetaData rootCmd) {
        if (vd.versantIsDirty()) return true;

        StateManager oldSM = vd.versantGetDetachedStateManager();
        try {
            vd.jdoReplaceStateManager(this);
            final FieldMetaData[] fields = rootCmd.managedFields;
            for (int i = 0; i < fields.length; i++) {
                final FieldMetaData fmd = fields[i];
                if (fmd.isEmbeddedRef()) {
                    vd.jdoProvideField(fmd.managedFieldNo);
                    if (checkDirtyImp(providedDetachable, fmd)) {
                        return true;
                    }
                }
            }
        } finally {
            vd.jdoReplaceStateManager(oldSM);
        }
        return false;
    }

    private boolean checkDirtyImp(VersantDetachable vds, FieldMetaData embeddedRefFmd) {
        StateManager oldsm = vds.versantGetDetachedStateManager();
        vds.jdoReplaceStateManager(this);
        try {
            FieldMetaData[] fmds = embeddedRefFmd.embeddedFmds;
            for (int i = 0; i < fmds.length; i++) {
                FieldMetaData fmd = fmds[i];
                if (fmd.isEmbeddedRef()) {
                    vds.jdoProvideField(fmd.origFmd.managedFieldNo);
                    if (vds.versantIsDirty()) {
                        return true;
                    }
                    checkDirtyImp(providedDetachable, fmd);
                }
            }
        } finally {
             vds.jdoReplaceStateManager(oldsm);
        }
        return false;
    }

    public void providedObjectField(PersistenceCapable pc, int field,
            Object currentValue) {
        providedDetachable = (VersantDetachable) currentValue;
    }

    public StateManager replacingStateManager(PersistenceCapable pc,
            StateManager sm) {
        return sm;
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

    public void setBooleanField(PersistenceCapable pc, int field,
            boolean currentValue, boolean newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCharField(PersistenceCapable pc, int field,
            char currentValue, char newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setByteField(PersistenceCapable pc, int field,
            byte currentValue, byte newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setShortField(PersistenceCapable pc, int field,
            short currentValue, short newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setIntField(PersistenceCapable pc, int field, int currentValue,
            int newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setLongField(PersistenceCapable pc, int field,
            long currentValue, long newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setFloatField(PersistenceCapable pc, int field,
            float currentValue, float newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDoubleField(PersistenceCapable pc, int field,
            double currentValue, double newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStringField(PersistenceCapable pc, int field,
            String currentValue, String newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setObjectField(PersistenceCapable pc, int field,
            Object currentValue, Object newValue) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    public boolean replacingBooleanField(PersistenceCapable pc, int field) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public char replacingCharField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte replacingByteField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public short replacingShortField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int replacingIntField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long replacingLongField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public float replacingFloatField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double replacingDoubleField(PersistenceCapable pc, int field) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String replacingStringField(PersistenceCapable pc, int field) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object replacingObjectField(PersistenceCapable pc, int field) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object[] replacingDetachedState(Detachable pc, Object[] state) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
