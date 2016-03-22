
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
package com.versant.core.common;

import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.sco.CollectionDiffUtil;
import com.versant.core.jdo.sco.VersantSCOCollection;
import com.versant.core.jdo.sco.VersantSCOMap;
import com.versant.core.jdo.sco.MapDiffUtil;
import com.versant.core.jdo.PCStateMan;
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.MDStatics;

import javax.jdo.spi.PersistenceCapable;
import javax.jdo.PersistenceManager;
import java.util.*;

/**
 * A utility class that is used by State.
 */
public final class StateUtil {

    public static Date getPValueForSCO(Date date) {
        return date;
    }

    /**
     * Provide the diff for a Collection.
     */
    public static CollectionDiff getPValueForSCO(Collection col,
            PersistenceContext pm,
            VersantStateManager sm, FieldMetaData fmd) {
        if (fmd.isReadOnly) return null;
        CollectionDiff diff = null;
        if (col instanceof VersantSCOCollection) {
            diff = ((VersantSCOCollection)col).getCollectionDiff(pm);
        } else {
            diff = CollectionDiffUtil.getNonSCOCollectionDiff(col, pm, fmd);
        }
        FieldMetaData inverseFieldMetaData = fmd.inverseFieldMetaData;
        if (fmd.category == MDStatics.CATEGORY_COLLECTION &&
                inverseFieldMetaData != null && fmd.inverseFieldMetaData.fake) {
            Object[] values = diff.insertedValues;
            fixFakeFieldsOnChildren(values, pm, inverseFieldMetaData, sm,
                    false);
            values = ((UnorderedCollectionDiff)diff).deletedValues;
            fixFakeFieldsOnChildren(values, pm, inverseFieldMetaData, sm, true);
        }
        return diff;
    }

    private static void fixFakeFieldsOnChildren(Object[] values,
            PersistenceContext pm,
            FieldMetaData inverseFieldMetaData, VersantStateManager sm, boolean remove) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Object oid = values[i];
                if (oid == null) {
                    throw BindingSupportImpl.getInstance().invalidOperation(
                            "One-to-Many collections may not contain null values.");
                }
                PersistenceCapable pc = (PersistenceCapable)pm.getObjectById(
                        oid, false);
                pm.getInternalSM(pc).setFakeMaster(inverseFieldMetaData.stateFieldNo, sm, remove);
            }
        }
    }

    public static CollectionDiff getPValueForSCO(Map map,
            PersistenceContext pm, FieldMetaData fmd) {
        if (map instanceof VersantSCOMap) {
            return ((VersantSCOMap)map).getMapDiff(pm);
        } else {
            MapDiffUtil mapDiffUtil = new MapDiffUtil(fmd);
            CollectionDiff collectionDiff = mapDiffUtil.getDiff(map, null, pm);
            collectionDiff.status = CollectionDiff.STATUS_NEW;
            return collectionDiff;
        }
    }

    public static Object getPValueForRef(PersistenceCapable pc,
            PersistenceContext pm) {
        return pm.getInternalOID(pc);
    }

    public static void doReachable(Collection col,
            VersantPersistenceManagerImp sm) {
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o != null) sm.makeReachablePersistent(o);
        }
    }

    public static void doReachable(Map map, VersantPersistenceManagerImp sm,
            FieldMetaData fmd) {
        boolean keyIsRef = fmd.isMapKeyRef();
        boolean valueIsRef = fmd.isMapValueRef();
        final Set entrySet = map.entrySet();
        for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry)iterator.next();
            if (keyIsRef) sm.makeReachablePersistent(entry.getKey());
            if (valueIsRef) sm.makeReachablePersistent(entry.getValue());
        }
    }

    public static void doReachable(Object[] array,
            VersantPersistenceManagerImp sm) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) sm.makeReachablePersistent(array[i]);
        }
    }

    public static void doReachable(PersistenceCapable pc,
            VersantPersistenceManagerImp sm) {
        sm.makeReachablePersistent(pc);
    }

    public static void doReachableEmbeddedReference(PersistenceCapable pc,
            VersantPersistenceManagerImp pm, PCStateMan sm, FieldMetaData fmd) {
        PersistenceManager otherPm = pc.jdoGetPersistenceManager();
        if (otherPm != null) {
            otherPm = ((PMProxy)otherPm).getRealPM();
        }
        if (otherPm != null && otherPm != pm) {
            throw BindingSupportImpl.getInstance().unsupported(
                    "Sharing of embedded instances is not supported");
        }
        PCStateMan tmpSM = pm.getSMIfManaged(pc);
        if (tmpSM != null) {
            if (tmpSM == sm) {
                //already persistent

            } else {
                throw BindingSupportImpl.getInstance().unsupported(
                        "Sharing of embedded instances is not supported");
            }
        } else {
            sm.createEmbeddedSM(pc, fmd);
        }
    }

    public static void retrieve(Map map, VersantPersistenceManagerImp sm,
            FieldMetaData fmd) {
        boolean keyIsRef = fmd.isMapKeyRef();
        boolean valueIsRef = fmd.isMapValueRef();
        final Set entrySet = map.entrySet();
        for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry)iterator.next();
            if (keyIsRef) sm.retrieveImp(entry.getKey());
            if (valueIsRef) sm.retrieveImp(entry.getValue());
        }
    }

}

