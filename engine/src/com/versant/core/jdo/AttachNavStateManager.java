
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

import javax.jdo.PersistenceManager;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.BitSet;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.OID;

/**
 *
 */
public class AttachNavStateManager implements VersantStateManager {

    private VersantPersistenceManagerImp pm;
    private AttachStateContainer asc;

    public AttachNavStateManager(VersantPersistenceManagerImp pm) {
        this.pm = pm;
    }

    public AttachNavStateManager(AttachStateContainer asc) {
        this.asc = asc;
    }

    public OID getOID() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PersistenceCapable getPersistenceCapable() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte replacingFlags(PersistenceCapable persistenceCapable) {
        return 0;
    }

    public StateManager replacingStateManager(PersistenceCapable persistenceCapable, StateManager stateManager) {
        return stateManager;
    }

    public boolean isDirty(PersistenceCapable persistenceCapable) {
        return ((VersantDetachable) persistenceCapable).versantIsDirty();
    }

    public boolean isTransactional(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isPersistent(PersistenceCapable persistenceCapable) {
        return false;
    }

    public boolean isNew(PersistenceCapable persistenceCapable) {
        return getPcOID(persistenceCapable).isNew();
    }

    public boolean isDeleted(PersistenceCapable persistenceCapable) {
        return false;
    }

    public PersistenceManager getPersistenceManager(PersistenceCapable persistenceCapable) {
        return null;
    }

    public void makeDirty(PersistenceCapable persistenceCapable, String s) {
        ((VersantDetachable) persistenceCapable).versantMakeDirty(s);
    }

    public Object getObjectId(PersistenceCapable persistenceCapable) {
        return getPcOID(persistenceCapable);
    }

    public Object getTransactionalObjectId(PersistenceCapable persistenceCapable) {
        return null;
    }

    public boolean isLoaded(PersistenceCapable persistenceCapable, int i) {
        return ((VersantDetachable) persistenceCapable).versantIsLoaded(i);
    }

    public void preSerialize(PersistenceCapable persistenceCapable) {

    }

    public boolean getBooleanField(PersistenceCapable persistenceCapable,
                                   int i, boolean b) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return b;
    }

    public char getCharField(PersistenceCapable persistenceCapable, int i,
                             char c) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return c;
    }

    public byte getByteField(PersistenceCapable persistenceCapable, int i,
                             byte b) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return b;
    }

    public short getShortField(PersistenceCapable persistenceCapable, int i,
                               short i1) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return i1;
    }

    public int getIntField(PersistenceCapable persistenceCapable, int i,
                           int i1) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return i1;
    }

    public long getLongField(PersistenceCapable persistenceCapable, int i,
                             long l) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return l;
    }

    public float getFloatField(PersistenceCapable persistenceCapable, int i,
                               float v) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return v;
    }

    public double getDoubleField(PersistenceCapable persistenceCapable, int i,
                                 double v) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return v;
    }

    public String getStringField(PersistenceCapable persistenceCapable, int i,
                                 String s) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
        return s;
    }

    public Object getObjectField(PersistenceCapable persistenceCapable, int i,
                                 Object o) {
        if (!isLoaded(persistenceCapable, i)) {
            throw BindingSupportImpl.getInstance().fieldDetached();
        }
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
    }

    public void providedCharField(PersistenceCapable persistenceCapable, int i,
                                  char c) {
    }

    public void providedByteField(PersistenceCapable persistenceCapable, int i,
                                  byte b) {
    }

    public void providedShortField(PersistenceCapable persistenceCapable,
                                   int i, short i1) {
    }

    public void providedIntField(PersistenceCapable persistenceCapable, int i,
                                 int i1) {
    }

    public void providedLongField(PersistenceCapable persistenceCapable, int i,
                                  long l) {
    }

    public void providedFloatField(PersistenceCapable persistenceCapable,
                                   int i, float v) {
    }

    public void providedDoubleField(PersistenceCapable persistenceCapable,
                                    int i, double v) {
    }

    public void providedStringField(PersistenceCapable persistenceCapable,
                                    int i, String s) {
    }

    public void providedObjectField(PersistenceCapable persistenceCapable,
                                    int i, Object o) {
        addObject(o);
    }

    private void addObject(Object o) {
        if (o == null) return;
        //todo this needs to change
        if (o.getClass().isArray()) {
            Class type = /*CHFC*/o.getClass().getComponentType()/*RIGHTPAR*/;
            if (type != null && !type.isPrimitive()) {
                int length = java.lang.reflect.Array.getLength(o);
                for (int x = 0; x < length; x++) {
                    addObject(java.lang.reflect.Array.get(o, x));
                }
            }
        } else if (o instanceof Collection) {
            Collection col = (Collection) o;
            for (Iterator it = col.iterator(); it.hasNext();) {
                addObject(it.next());
            }
        } else if (o instanceof Map) {
            Map map = (Map) o;
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                addObject(it.next());
            }
            for (Iterator it = map.values().iterator(); it.hasNext();) {
                addObject(it.next());
            }
        } else if (o instanceof VersantDetachable) {
            asc.addVersantDetachable((VersantDetachable) o);
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

    public void makeDirty(PersistenceCapable pc,
                          int managedFieldNo) {
        ((VersantDetachable) pc).versantMakeDirty(managedFieldNo);
    }

    private OID getPcOID(PersistenceCapable persistenceCapable) {
        return pm.extractOID(((VersantDetachable) persistenceCapable).versantGetOID());
    }

    public void fillNewAppPKField(int fieldNo) { }

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
