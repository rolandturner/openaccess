
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
import java.util.Map;
import java.util.TreeMap;

/**
 * Detached SCO for TreeMap.
 */
public class DetachSCOTreeMap extends TreeMap implements Serializable, VersantSimpleSCO {

    private PersistenceCapable owner;
    private VersantStateManager stateManager;
    private int fieldNo;

    private DetachSCOTreeMap(PersistenceCapable owner,
                             VersantFieldMetaData fmd) {
        super(fmd.getComparator());
        this.owner = owner;
        this.fieldNo = fmd.getManagedFieldNo();
    }

    public DetachSCOTreeMap(PersistenceCapable owner, VersantStateManager stateManager,
                            VersantFieldMetaData fmd, Map beforeMap) {
        this(owner, fmd);
        putAll(beforeMap);
        this.stateManager = stateManager;
    }

    public DetachSCOTreeMap(PersistenceCapable owner, VersantStateManager stateManager,
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

    public Class getJavaType() {
        return /*CHFC*/TreeMap.class/*RIGHTPAR*/;
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

    public Object getOwner() {
        return owner;
    }

    public void makeTransient() {
        owner = null;
        stateManager = null;
    }

    public void makeDirty() {
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
}
