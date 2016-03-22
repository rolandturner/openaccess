
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
package com.versant.core.jdo.sco;

import com.versant.core.jdo.VersantStateManager;
import com.versant.core.common.CollectionDiff;
import com.versant.core.common.VersantFieldMetaData;
import com.versant.core.common.PersistenceContext;

import javax.jdo.spi.PersistenceCapable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class SCOHashtable extends Hashtable implements VersantSCOMap, VersantAdvancedSCO {

    private transient PersistenceCapable owner;
    private transient VersantStateManager stateManager;
    private final transient VersantFieldMetaData fmd;
    private transient MapDiffUtil diffUtil;
    private transient Map beforeMap = new HashMap();

    private SCOHashtable(PersistenceCapable owner,
                         VersantFieldMetaData fmd) {
        this.owner = owner;
        this.fmd = fmd;
        diffUtil = new MapDiffUtil(fmd);
    }

    public SCOHashtable(PersistenceCapable owner, VersantStateManager stateManager,
                        VersantFieldMetaData fmd, Map beforeMap) {
        this(owner, fmd);
        if (!owner.jdoIsNew()) {
            this.beforeMap.putAll(beforeMap);
        }
        putAll(beforeMap);
        this.stateManager = stateManager;
    }

    public SCOHashtable(PersistenceCapable owner, VersantStateManager stateManager,
                        VersantFieldMetaData fmd, MapData mapData) {
        this(owner, fmd);
        int n = mapData.entryCount;
        Object[] keys = mapData.keys;
        Object[] values = mapData.values;
        for (int i = 0; i < n; i++) {
            beforeMap.put(keys[i], values[i]);
        }
        putAll(beforeMap);
        this.stateManager = stateManager;
    }

    public Object put(Object key, Object value) {
        makeDirty();
        return super.put(key, value);
    }

    public Object remove(Object key) {
        return checkModified(super.remove(key));
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

    public Object getOwner() {
        return owner;
    }

    public void makeTransient() {
        owner = null;
        stateManager = null;
    }

    public void makeDirty() {
        if (stateManager != null) {
            stateManager.makeDirty(owner, fmd.getManagedFieldNo());
        }
    }

    public void reset() {
        beforeMap.clear();
        beforeMap.putAll(this);
    }

    private Object checkModified(Object obj) {
        if (obj != null) {
            makeDirty();
        }
        return obj;
    }

    public CollectionDiff getMapDiff(PersistenceContext pm) {
        return diffUtil.getDiff(this, beforeMap, pm);
    }

    /**
     * Put references to all the keys and values into mapData. If the keys
     * and/or values are PC instances then the instances themselves or their
     * OIDs may be stored in mapData.
     */
    public MapData fillMapData(MapData mapData) {
        int size = size();
        mapData.entryCount = size;
        Object[] newKeys;
        Object[] oldKeys = mapData.keys;
        if (oldKeys == null || oldKeys.length < size) {
            newKeys = new Object[size];
        } else {
            newKeys = oldKeys;
        }
        Object[] newValues;
        Object[] oldValues = mapData.values;
        if (oldValues == null || oldValues.length < size) {
            newValues = new Object[size];
        } else {
            newValues = oldValues;
        }
        int i = 0;
        for (Iterator it = this.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            newKeys[i] = entry.getKey();
            newValues[i++] = entry.getValue();
        }
        mapData.keys = newKeys;
        mapData.values = newValues;
        return mapData;
    }
}
