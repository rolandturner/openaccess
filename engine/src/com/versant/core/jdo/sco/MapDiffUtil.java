
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

import javax.jdo.spi.PersistenceCapable;
import java.util.*;

import com.versant.core.common.OID;
import com.versant.core.common.*;

/**
 * This is a util used to extract a diff from to map's.
 * <p/>
 * This is not thread safe.
 *
 */
public final class MapDiffUtil {

    private VersantFieldMetaData fmd;
    private boolean keyIsPC;
    private boolean valueIsPC;
    private List toDelete = new ArrayList();
    private Map toInsert = new java.util.HashMap();
    private static final Map EMPTY_BEFORE_MAP = new HashMap();

    public MapDiffUtil(VersantFieldMetaData fmd) {
        this.fmd = fmd;
        valueIsPC = fmd.isElementTypePC();
        keyIsPC = fmd.isKeyTypePC();
    }

    private void addForDelete(Object key) {
        toDelete.add(key);
    }

    private void addForInsert(Object key, Object value) {
        toInsert.put(key, value);
    }

    private void addForUpdate(Object key, Object value) {
        toDelete.add(key);
        toInsert.put(key, value);
    }

    /**
     * @param currentMap The dirty map
     * @param beforeMap  The map as before the changes. Null or Empty if first insert.
     * @return
     */
    public MapDiff getDiff(Map currentMap, Map beforeMap,
            PersistenceContext pm) {
//[BEGIN CHANGE: Pinaki]
// VDS Datastore requires the entire content of the map rather than the difference
// during flush. The corresponding flag is set in FieldMetaData during definition.
        
        if (fmd.isIncludeAllDataInDiff()){
            if (currentMap==null) return null;
            MapDiff mapDiff = new MapDiff(fmd);
            int mapSize = currentMap.size();
            mapDiff.insertedKeys   = new Object[mapSize];
            mapDiff.insertedValues = new Object[mapSize];
            int i = 0;
            for (Iterator keys=currentMap.keySet().iterator(); keys.hasNext();i++){
                Object key   = keys.next();
                Object value = currentMap.get(key);
                    mapDiff.insertedKeys[i] = (keyIsPC) ? 
                            pm.getInternalOID((PersistenceCapable)key)
                            : key;
                    mapDiff.insertedValues[i] = (valueIsPC) ?
                            pm.getInternalOID((PersistenceCapable)value)
                            : value;
            }
            return mapDiff;
        }
//[END CHANGE: Pinaki]
        if (beforeMap == null) {
            beforeMap = EMPTY_BEFORE_MAP;
        }
        doAdded(currentMap, beforeMap);
        doRemoved(currentMap, beforeMap);
        doUpdates(currentMap, beforeMap);

        MapDiff mapDiff = new MapDiff(fmd);
        doDeletes(mapDiff, pm);
        if (!keyIsPC && !valueIsPC) {
            mapDiff.insertedKeys = toInsert.keySet().toArray();
            mapDiff.insertedValues = toInsert.values().toArray();
        } else if (keyIsPC && valueIsPC) {
            Object[] insKeys = new OID[toInsert.size()];
            Object[] insVals = new OID[toInsert.size()];

            int c = 0;
            Set insertedSet = toInsert.entrySet();
            for (Iterator iterator = insertedSet.iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Object key = entry.getKey();
                key = pm.getInternalOID((PersistenceCapable)key);
                Object val = entry.getValue();
                val = pm.getInternalOID((PersistenceCapable)val);
                insKeys[c] = key;
                insVals[c++] = val;
            }
            mapDiff.insertedKeys = insKeys;
            mapDiff.insertedValues = insVals;
        } else if (keyIsPC) {
            Object[] insKeys = new OID[toInsert.size()];
            Object[] insVals = toInsert.values().toArray();

            int c = 0;
            Set insertedSet = toInsert.keySet();
            for (Iterator iterator = insertedSet.iterator(); iterator.hasNext();) {
                Object key = iterator.next();
                key = pm.getInternalOID((PersistenceCapable)key);
                insKeys[c++] = key;
            }
            mapDiff.insertedKeys = insKeys;
            mapDiff.insertedValues = insVals;

        } else if (valueIsPC) {
            Object[] insKeys = toInsert.keySet().toArray();
            Object[] insVals = new OID[toInsert.size()];
            int c = 0;
            Set insertedSet = toInsert.keySet();
            for (Iterator iterator = insertedSet.iterator(); iterator.hasNext();) {
                Object value = toInsert.get(iterator.next());
                value = pm.getInternalOID((PersistenceCapable)value);
                insVals[c++] = value;
            }
            mapDiff.insertedKeys = insKeys;
            mapDiff.insertedValues = insVals;

        }
        toInsert.clear();
        toDelete.clear();
        return mapDiff;
    }

    private void doDeletes(MapDiff mapDiff, PersistenceContext pm) {
        Object[] delArray;
        if (keyIsPC) {
            delArray = new OID[toDelete.size()];
            for (int i = 0; i < toDelete.size(); i++) {
                delArray[i] = pm.getInternalOID((PersistenceCapable)toDelete.get(i));
            }
        } else {
            delArray = toDelete.toArray();
        }
        mapDiff.deletedKeys = delArray;
    }

    private void doAdded(Map currentMap, Map beforeMap) {
        Set newKeys = new java.util.HashSet(currentMap.keySet());
        newKeys.removeAll(beforeMap.keySet());
        for (Iterator iterator = newKeys.iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            Object val = currentMap.get(key);
            addForInsert(key, val);
        }
    }

    private void doRemoved(Map currentMap, Map beforeMap) {
        Set removedKeys = new java.util.HashSet(beforeMap.keySet());
        removedKeys.removeAll(currentMap.keySet());
        for (Iterator iterator = removedKeys.iterator(); iterator.hasNext();) {
            addForDelete(iterator.next());
        }
    }

    private void doUpdates(Map currentMap, Map beforeMap) {
        Set keys = currentMap.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            if (beforeMap.containsKey(key)) {
                Object currentVal = currentMap.get(key);
                Object beforeVal = beforeMap.get(key);
                if (currentVal != null) {
                    if (!currentVal.equals(beforeVal)) {
                        addForUpdate(key, currentVal);
                    }
                } else if (currentVal == null) {
                    if (beforeVal != null) {
                        addForUpdate(key, currentVal);
                    }
                }
            }
        }
    }
}

