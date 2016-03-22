
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
package com.versant.core.jdo.sco.detached;

import com.versant.core.jdo.VersantStateManager;
import com.versant.core.common.VersantFieldMetaData;
import com.versant.core.jdo.sco.VersantSimpleSCO;
import com.versant.core.jdo.sco.MapData;
import com.versant.core.jdo.VersantStateManager;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.*;

public class DetachSCOHashMap extends HashMap implements Serializable, VersantSimpleSCO {

    private PersistenceCapable owner;
    private VersantStateManager stateManager;
    private int fieldNo;

    private DetachSCOHashMap(PersistenceCapable owner,
                             VersantFieldMetaData fmd) {
        this.owner = owner;
        this.fieldNo = fmd.getManagedFieldNo();
    }

    public DetachSCOHashMap(PersistenceCapable owner, VersantStateManager stateManager,
                            VersantFieldMetaData fmd, Map beforeMap) {
        this(owner, fmd);
        putAll(beforeMap);
        this.stateManager = stateManager;
    }

    public DetachSCOHashMap(PersistenceCapable owner, VersantStateManager stateManager,
                            VersantFieldMetaData fmd, MapData mapData) {
        this(owner, fmd);
        int n = mapData.entryCount;
        Object[] keys = mapData.keys;
        Object[] values = mapData.values;
        for (int i = 0; i < n; i++) {
            put(keys[i], values[i]);
        }
        this.stateManager = stateManager;
    }

    public Set keySet() {
        final Set kSet = super.keySet();
        final class TmpKeySet extends AbstractSet {

            Set delegateSet = kSet;

            public Iterator iterator() {
                return delegateSet.iterator();
            }

            public int size() {
                return delegateSet.size();
            }

            public boolean contains(Object o) {
                return containsKey(o);
            }

            public void clear() {
                delegateSet.clear();
            }

            public boolean remove(Object o) {
                Object removed = DetachSCOHashMap.this.remove(o);
                return removed != null;
            }
        }
        return new TmpKeySet();
    }

    public Object remove(Object key) {
        return checkModified(super.remove(key));
    }

    public Object put(Object key, Object value) {
        makeDirty();
        return super.put(key, value);
    }

    public void putAll(Map t) {
        makeDirty();
        super.putAll(t);
    }

    public void clear() {
        final int size = size();
        super.clear();
        if (size != 0) makeDirty();
    }

    private void makeDirty() {
        if (stateManager != null && owner != null) {
            stateManager.makeDirty(owner, fieldNo);
        }
    }

    private Object checkModified(Object obj) {
        if (obj != null) {
            makeDirty();
        }
        return obj;
    }

    public void makeTransient() {
        owner = null;
        stateManager = null;
    }
}
