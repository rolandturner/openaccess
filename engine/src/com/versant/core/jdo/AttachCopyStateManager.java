
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

import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ClassMetaData;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 *
 */
public class AttachCopyStateManager implements VersantStateManager {

    private VersantPersistenceManagerImp pm;
    private State state;
    private ClassMetaData cmd;
    private PCStateMan sm;

    public AttachCopyStateManager(VersantPersistenceManagerImp pm) {
        this.pm = pm;
    }

    public void setState(PCStateMan sm) {
        this.state = sm.state;
        this.cmd = state.getClassMetaData();
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
        return getPcOID((VersantDetachable)persistenceCapable, pm).isNew();
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
        return getPcOID((VersantDetachable)persistenceCapable, pm);
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
            Object o, Object o1) {
    }

    public void providedBooleanField(PersistenceCapable persistenceCapable,
            int i, boolean b) {
        state.setBooleanFieldAbs(i, b);
    }

    public void providedCharField(PersistenceCapable persistenceCapable, int i,
            char c) {
        state.setCharFieldAbs(i, c);
    }

    public void providedByteField(PersistenceCapable persistenceCapable, int i,
            byte b) {
        state.setByteFieldAbs(i, b);
    }

    public void providedShortField(PersistenceCapable persistenceCapable,
            int i, short i1) {
        state.setShortFieldAbs(i, i1);
    }

    public void providedIntField(PersistenceCapable persistenceCapable, int i,
            int i1) {
        state.setIntFieldAbs(i, i1);
    }

    public void providedLongField(PersistenceCapable persistenceCapable, int i,
            long l) {
        state.setLongFieldAbs(i, l);
    }

    public void providedFloatField(PersistenceCapable persistenceCapable,
            int i, float v) {
        state.setFloatFieldAbs(i, v);
    }

    public void providedDoubleField(PersistenceCapable persistenceCapable,
            int i, double v) {
        state.setDoubleFieldAbs(i, v);
    }

    public void providedStringField(PersistenceCapable persistenceCapable,
            int i, String s) {
        state.setStringFieldAbs(i, s);
    }

    public void providedObjectField(PersistenceCapable pc,
            int i, Object o) {
        providedObjectFieldImp(o, getFieldMetaData(i),
                ((VersantDetachable)pc).versantIsDirty(i));
    }

    public void providedObjectFieldImp(Object o, FieldMetaData fieldMetaData,
            boolean dirty) {
        if (fieldMetaData.classMetaData != cmd) {
            throw BindingSupportImpl.getInstance().internal(
                    "There is a fieldMetadata mismatch");
        }
        if (fieldMetaData.isEmbeddedRef()) {
            providedEmbeddedRef(fieldMetaData, o, dirty);
        } else {
            if (o == null) {
                state.setObjectField(fieldMetaData.stateFieldNo, o);
            } else {
                state.setObjectField(fieldMetaData.stateFieldNo,
                        getUnresolved(o, fieldMetaData, pm));
            }
        }
    }

    private FieldMetaData getFieldMetaData(int managedFieldNo) {
        return cmd.managedFields[managedFieldNo];
    }

    private void providedEmbeddedRef(FieldMetaData fmd, Object o, boolean dirtyRef) {
        final VersantDetachable embeddedIntance = (VersantDetachable) o;
        if (o == null) {
            /**
             * The embedded ref is null.
             * Must check if dirty of not.
             * If dirty then was set to null, else was null on detach. If so
             * then we can stop here.
             */
            //must find out if the embeddedRef is dirty.
            if (dirtyRef) {
                /**
                 * The instance was set to null, so it and all its sub embedded
                 * references must somehow be set to default values, as if it
                 * was deleted.
                 */
            }
        } else {
            VersantDetachable ePc = (VersantDetachable) o;
            //replace the sm with an attaching sm.

            if (dirtyRef) {
                state.setObjectField(fmd.stateFieldNo, o);
            }
            final StateManager oldSM = ePc.versantGetDetachedStateManager();
            try {
                ePc.jdoReplaceStateManager(
                        new AttachCopyEmbeddedStateManager(fmd));
                FieldMetaData[] eFields = fmd.embeddedFmds;
                for (int j = 0; j < eFields.length; j++) {
                    FieldMetaData eField = eFields[j];
                    //if the oldSm == null then the instance was set and therefore the
                    //fields does not have to be set.
                    if (!dirtyRef && embeddedIntance.versantIsDirty(eField.origFmd.managedFieldNo)) {
                        sm.makeDirtyImp(eField);
                        ePc.jdoProvideField(eField.origFmd.managedFieldNo);
                    }
                    if (eField.isEmbeddedRef()) {
                        ePc.jdoProvideField(eField.origFmd.managedFieldNo);
                    }
                }
            } finally {
                ePc.jdoReplaceStateManager(oldSM);
            }
        }
    }

    public Object getUnresolved(Object o, FieldMetaData fmd, VersantPersistenceManagerImp pm) {
        switch (fmd.category) {
            case MDStatics.CATEGORY_COLLECTION:
                if (o instanceof Collection) {
                    Collection col = (Collection)o;
                    ArrayList oids = new ArrayList(col.size());
                    if (fmd.isElementTypePC()) {
                        for (Iterator it = col.iterator(); it.hasNext();) {
                            VersantDetachable detachable = (VersantDetachable)it.next();
                            oids.add(getPC(detachable, pm));
                        }
                    } else {
                        oids.addAll(col);
                    }
                    col = createNewCol(fmd);
                    col.addAll(oids);
                    return col;
                }
                break;
            case MDStatics.CATEGORY_ARRAY:
                if (!o.getClass().isArray()) return o;
                Class type = /*CHFC*/o.getClass().getComponentType()/*RIGHTPAR*/;
                int length = java.lang.reflect.Array.getLength(o);
                Object newArray = java.lang.reflect.Array.newInstance(type, length);
                System.arraycopy(o, 0, newArray, 0, length);
                if (fmd.isElementTypePC()) {
                    Object[] objects = (Object[])newArray;
                    for (int i = 0; i < objects.length; i++) {
                        objects[i] = getPC((VersantDetachable)objects[i], pm);
                    }
                }
                return newArray;
            case MDStatics.CATEGORY_MAP:
                if (o instanceof Map) {
                    Object[] keys;
                    Object[] values;

                    Map map = (Map)o;
                    int size = map.size();
                    keys = new Object[size];
                    values = new Object[size];
                    int x = 0;
                    for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                        Object o1 = it.next();
                        if (fmd.isKeyTypePC()) {
                            keys[x] = getPC((VersantDetachable)o1, pm);
                        } else {
                            keys[x] = o1;
                        }
                        if (fmd.isElementTypePC()) {
                            values[x] = getPC((VersantDetachable)map.get(o1),
                                    pm);
                        } else {
                            values[x] = map.get(o1);
                        }
                        x++;
                    }
                    map = createNewMap(fmd);
                    for (int i = 0; i < size; i++) {
                        map.put(keys[i], values[i]);
                    }
                    return map;
                }
                break;
            case MDStatics.CATEGORY_REF:
            case MDStatics.CATEGORY_POLYREF:
                VersantDetachable detachable = (VersantDetachable)o;
                return getPC(detachable, pm);
        }
        return o;
    }

    private static Object getPC(VersantDetachable detachable, VersantPersistenceManagerImp pm) {
        OID pcOID = getPcOID(detachable, pm);
        return pm.getObjectById(pcOID, false);
    }

    private static Map createNewMap(FieldMetaData fmd) {
        switch (fmd.typeCode) {
            case MDStatics.MAP:
            case MDStatics.HASHMAP:
                return new HashMap();
            case MDStatics.HASHTABLE:
                return new Hashtable();
            case MDStatics.TREEMAP:
            case MDStatics.SORTEDMAP:
                return new TreeMap();
            default:
                throw BindingSupportImpl.getInstance().notImplemented("Creating a Map instance for field " +
                        fmd.getName() + " of type " + MDStaticUtils.toSimpleName(
                                fmd.typeCode) +
                        " is not supported");
        }
    }

    private static Collection createNewCol(FieldMetaData fmd) {
        switch (fmd.typeCode) {
            case MDStatics.HASHSET:
            case MDStatics.SET:
                return new HashSet();
            case MDStatics.TREESET:
            case MDStatics.SORTEDSET:
                return new TreeSet();
            case MDStatics.COLLECTION:
            case MDStatics.LIST:
            case MDStatics.ARRAYLIST:
                return new ArrayList();
            case MDStatics.LINKEDLIST:
                return new LinkedList();
            case MDStatics.VECTOR:
                return new Vector();
            default:
                throw BindingSupportImpl.getInstance().notImplemented("Creating a Collection instance for field " +
                        fmd.getName() + " of type " + MDStaticUtils.toSimpleName(
                                fmd.typeCode) +
                        " is not supported");
        }
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

    private static OID getPcOID(VersantDetachable persistenceCapable,
            VersantPersistenceManagerImp pm) {
        return pm.getOID(persistenceCapable);
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




    /**
     * A sm to help with the attaching of embedded instances.
     */
    public class AttachCopyEmbeddedStateManager implements StateManager {
        private FieldMetaData fmd;

        public AttachCopyEmbeddedStateManager(FieldMetaData fmd) {
            this.fmd = fmd;
        }

        private FieldMetaData getFmd(int fieldNo) {
            return fmd.managedEmbeddedFields[fieldNo];
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

        public void providedBooleanField(PersistenceCapable pc, int field,
                boolean currentValue) {
            state.setBooleanField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedCharField(PersistenceCapable pc, int field,
                char currentValue) {
            state.setCharField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedByteField(PersistenceCapable pc, int field,
                byte currentValue) {
            state.setByteField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedShortField(PersistenceCapable pc, int field,
                short currentValue) {
            state.setShortField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedIntField(PersistenceCapable pc, int field,
                int currentValue) {
            state.setIntField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedLongField(PersistenceCapable pc, int field,
                long currentValue) {
            state.setLongField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedFloatField(PersistenceCapable pc, int field,
                float currentValue) {
            state.setFloatField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedDoubleField(PersistenceCapable pc, int field,
                double currentValue) {
            state.setDoubleField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedStringField(PersistenceCapable pc, int field,
                String currentValue) {
            state.setStringField(getFmd(field).stateFieldNo, currentValue);
        }

        public void providedObjectField(PersistenceCapable pc, int field,
                Object currentValue) {
            providedObjectFieldImp(currentValue, getFmd(field),
                    ((VersantDetachable)pc).versantIsDirty(field));
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

}
