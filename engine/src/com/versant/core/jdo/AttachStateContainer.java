
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

import java.util.HashSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.versant.core.common.BindingSupportImpl;

/**
 *
 */
public class AttachStateContainer {

    private static final int INITIAL_SIZE = 16;

    private OID[] oids = new OID[INITIAL_SIZE];
    private VersantDetachable[] detachables = new VersantDetachable[INITIAL_SIZE];
    private PCStateMan[] stateMans = new PCStateMan[INITIAL_SIZE];
    private Map oidMap = new HashMap();
    private int capacity = INITIAL_SIZE;
    private int size;
    private VersantPersistenceManagerImp pm;
    private HashSet deleted = new HashSet();

    public AttachStateContainer(VersantPersistenceManagerImp pm) {
        this.pm = pm;
    }

    public int addVersantDetachable(VersantDetachable detachable) {
        VersantDetachedStateManager sm = detachable.versantGetDetachedStateManager();
        if (sm != null) {
            Collection delOIDs = ((VersantDetachedStateManager)sm).versantGetDeleted();
            if (!delOIDs.isEmpty()) {
                deleted.addAll(delOIDs);
                delOIDs.clear();
            }
        }
        OID oid = pm.getOID(detachable);
        Integer integer = (Integer)oidMap.get(oid);
        if (integer != null) {
            int index = integer.intValue();
            VersantDetachable oldDetachable = detachables[index];
            if (oldDetachable == detachable) return index;
            if (detachable.versantIsDirty()) {
                if (oldDetachable.versantIsDirty()) {
                    throw BindingSupportImpl.getInstance().concurrentUpdate("Duplicate oid(" +
                            oid + ") in attach graph", oid);
                } else {
                    detachables[index] = detachable;
                }
            } else {
                return index;
            }
        }
        if (size == capacity) {
            capacity = (capacity * 3) / 2 + 1;
            VersantDetachable[] nd = new VersantDetachable[capacity];
            System.arraycopy(detachables, 0, nd, 0, size);
            detachables = nd;
            OID[] noids = new OID[capacity];
            System.arraycopy(oids, 0, noids, 0, size);
            oids = noids;
            PCStateMan[] nsm = new PCStateMan[capacity];
            System.arraycopy(stateMans, 0, nsm, 0, size);
            stateMans = nsm;
        }
        oids[size] = oid;
        detachables[size] = detachable;
        oidMap.put(oid, new Integer(size));
        return size++;
    }

    public int getDetachedSize() {
        return size;
    }

    public VersantDetachable getVersantDetachable(int c) {
        return detachables[c];
    }

    public OID[] getOIDs() {
        return oids;
    }

    public State getState(int c) {
        return null;
    }

    public OID getOID(int c) {
        return oids[c];
    }

    public State getState(int c,
            VersantPersistenceManagerImp pm) {
        return pm.getStateFromLocalCacheById(getOID(c));

    }

    public void addPCStateMan(OID oid, PCStateMan sm) {
        Integer integer = (Integer)oidMap.get(oid);
        if (integer != null) {
            stateMans[integer.intValue()] = sm;
        }
    }

    public HashSet getDeleted() {
        return deleted;
    }
}
