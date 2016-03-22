
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

import com.versant.core.jdo.PMProxy;

import javax.jdo.spi.PersistenceCapable;
import java.util.Collection;

/**
 * Utility methods to help SCOs implement inverse mappings.
 */
public class SCOInverseUtil {

    /**
     * Change the master reference on detailObject. This will remove the
     * detail from its current master (if any and if different).
     */
    public static void addMasterOnDetail(Object detailObject,
                                         PersistenceCapable owner, int detailFieldNo) {
        if (owner == null) return;
        PersistenceCapable detail = (PersistenceCapable) detailObject;
        if (!detail.jdoIsPersistent()) {
            owner.jdoGetPersistenceManager().makePersistent(detail);
        }
        ((PMProxy)owner.jdoGetPersistenceManager()).setMasterOnDetail(detail, detailFieldNo, owner, true);
    }

    /**
     * Clear the master reference on detailObject. This will <b>not</b> remove
     * the detail from its current master. This can be done in bulk or more
     * efficiently by the SCO.
     */
    public static void removeMasterOnDetail(Object detailObject,
                                            PersistenceCapable owner, int detailFieldNo) {
        if (owner == null) return;
        PersistenceCapable detail = (PersistenceCapable) detailObject;
        if (!detail.jdoIsPersistent()) {
            owner.jdoGetPersistenceManager().makePersistent(detail);
        }
        ((PMProxy)owner.jdoGetPersistenceManager()).setMasterOnDetail(detail, detailFieldNo, null, false);
    }

    /**
     * Add to the other half of a many-to-many relationship.
     *
     * @param inverse        The instance on the other side of the relationship
     * @param inverseFieldNo The collection field on inverse to add to
     * @param toAdd          The instance to add to the collection on inverse
     */
    public static void addToOtherSideOfManyToMany(Object inverse, int inverseFieldNo,
                                                  PersistenceCapable toAdd) {
        if (toAdd == null) return;
        final PMProxy pm = (PMProxy) toAdd.jdoGetPersistenceManager();

        PersistenceCapable pcInverse = (PersistenceCapable) inverse;
        if (!pcInverse.jdoIsPersistent()) pm.makePersistent(pcInverse);

        Object otherSide = pm.getObjectField(pcInverse, inverseFieldNo);
        if (otherSide instanceof VersantManagedSCOCollection) {
            ((VersantManagedSCOCollection) otherSide).manyToManyAdd(toAdd);
        } else {
            ((Collection) otherSide).add(toAdd);
        }
    }

    /**
     * Remove from the other half of a many-to-many relationship.
     *
     * @param inverse        The instance on the other side of the relationship
     * @param inverseFieldNo The collection field on inverse to remove from
     * @param toRemove       The instance to remve from the collection on inverse
     */
    public static void removeFromOtherSideOfManyToMany(Object inverse,
                                                       int inverseFieldNo, PersistenceCapable toRemove) {
        if (toRemove == null) return;
        PMProxy pm = (PMProxy) toRemove.jdoGetPersistenceManager();

        Object otherSide = pm.getObjectField((PersistenceCapable) inverse, inverseFieldNo);
        if (otherSide instanceof VersantManagedSCOCollection) {
            ((VersantManagedSCOCollection) otherSide).manyToManyRemove(toRemove);
        } else {
            ((Collection) otherSide).remove(toRemove);
        }
    }

}
