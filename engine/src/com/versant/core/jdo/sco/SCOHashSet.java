
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

import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.jdo.VersantStateManager;
import com.versant.core.common.CollectionDiff;
import com.versant.core.common.VersantFieldMetaData;

import javax.jdo.spi.PersistenceCapable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.PersistenceContext;

/**
 * SCO for a HashSet.
 *
 * @keep-all
 */
public class SCOHashSet extends HashSet implements VersantManagedSCOCollection, VersantAdvancedSCO {

    private transient PersistenceCapable owner;
    private transient VersantStateManager stateManager;
    private final transient VersantFieldMetaData fmd;
    private final transient boolean isMaster;
    private final transient boolean isMany;
    private final transient int inverseFieldNo;
    private transient Object[] originalData;
    private boolean beenReset;

    public SCOHashSet(PersistenceCapable owner, VersantStateManager stateManager,
                      VersantFieldMetaData fmd, Object[] originalData) {
        this.isMaster = fmd.isManaged() && fmd.isMaster();
        this.inverseFieldNo = fmd.getInverseFieldNo();
        this.owner = owner;
        this.stateManager = stateManager;
        this.fmd = fmd;
        this.isMany = fmd.isManaged() && fmd.isManyToMany();
        this.originalData = originalData;
        int n = originalData == null ? 0 : originalData.length;
        if (!owner.jdoIsNew()) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                if (o == null) break;
                super.add(o);
            }
        } else if (isMaster) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                if (o == null) throw createNPE();
                super.add(o);
                SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
            }
        } else if (isMany) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                if (o == null) throw createNPE();
                super.add(o);
                SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
            }
        } else {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                if (o == null) throw createNPE();
                super.add(o);
            }
        }
    }

    private RuntimeException createNPE() {
        return BindingSupportImpl.getInstance().nullElement("Null element not allowed: " + fmd.getQName());
    }

    public boolean add(Object o) {
        if (o == null) throw createNPE();
        if (isMaster) {
            boolean result = super.add(o);
            if (result) {
                makeDirty();
                SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
            }
            return result;
        } else if (isMany) {
            boolean result = super.add(o);
            if (result) {
                makeDirty();
                SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
            }
            return result;
        } else {
            boolean result = super.add(o);
            if (result) makeDirty();
            return result;
        }
    }

    public boolean remove(Object o) {
        boolean result = super.remove(o);
        if (result) {
            makeDirty();
            if (isMaster) {
                SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
            } else if (isMany) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
            }
        }
        return result;
    }

    public void clear() {
        if (isMaster) {
            for (Iterator iter = super.iterator(); iter.hasNext();) {
                SCOInverseUtil.removeMasterOnDetail(iter.next(), owner, inverseFieldNo);
            }
        } else if (isMany) {
            for (Iterator iter = super.iterator(); iter.hasNext();) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(iter.next(), inverseFieldNo, owner);
            }
        }
        super.clear();
        makeDirty();
    }

    public boolean retainAll(Collection c) {
        if (isMaster) {
            boolean modified = false;
            Iterator e = super.iterator();
            while (e.hasNext()) {
                Object o = e.next();
                if (!c.contains(o)) {
                    e.remove();
                    SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                    modified = true;
                }
            }
            if (modified) makeDirty();
            return modified;
        } else if (isMany) {
            boolean modified = false;
            Iterator e = super.iterator();
            while (e.hasNext()) {
                Object o = e.next();
                if (!c.contains(o)) {
                    e.remove();
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                    modified = true;
                }
            }
            if (modified) makeDirty();
            return modified;
        } else if (super.retainAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection c) {
        if (isMaster) {
            boolean modified = false;
            Object o = null;
            if (size() > c.size()) {
                for (Iterator i = c.iterator(); i.hasNext();) {
                    o = i.next();
                    if (o != null) {
                        modified |= super.remove(o);
                        SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                    }
                }
            } else {
                for (Iterator i = super.iterator(); i.hasNext();) {
                    o = i.next();
                    if (c.contains(o)) {
                        i.remove();
                        modified = true;
                        SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                    }
                }
            }
            if (modified) makeDirty();
            return modified;
        } else if (isMany) {
            boolean modified = false;
            Object o = null;
            if (size() > c.size()) {
                for (Iterator i = c.iterator(); i.hasNext();) {
                    o = i.next();
                    if (o != null) {
                        boolean ans = super.remove(o);
                        if (ans) {
                            modified = true;
                            SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                        }
                    }
                }
            } else {
                for (Iterator i = super.iterator(); i.hasNext();) {
                    o = i.next();
                    if (c.contains(o)) {
                        i.remove();
                        modified = true;
                        SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                    }
                }
            }
            if (modified) makeDirty();
            return modified;
        } else if (super.removeAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public boolean addAll(Collection c) {
        if (isMaster) {
            boolean modified = false;
            Object o = null;
            Iterator e = c.iterator();
            while (e.hasNext()) {
                o = e.next();
                if (o == null) {
                    throw createNPE();
                } else if (super.add(o)) {
                    modified = true;
                    SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                }
            }
            if (modified) makeDirty();
            return modified;
        } else if (isMany) {
            boolean modified = false;
            Object o = null;
            Iterator e = c.iterator();
            while (e.hasNext()) {
                o = e.next();
                if (o == null) {
                    throw createNPE();
                } else if (super.add(o)) {
                    modified = true;
                    SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                }
            }
            if (modified) makeDirty();
            return modified;
        } else if (super.addAll(c)) {
            makeDirty();
            return true;
        }
        return false;

    }

    public Iterator iterator() {
        return new SCOIterator(super.iterator(), stateManager, owner, fmd.getManagedFieldNo());
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
        beenReset = true;
        originalData = toArray();
    }

    public CollectionDiff getCollectionDiff(PersistenceContext pm) {
        Object[] data = toArray();
        return CollectionDiffUtil.getUnorderedCollectionDiff(fmd, pm,
                data, data.length, (owner.jdoIsNew() && !beenReset) ? null : originalData);
    }

    public void manyToManyAdd(Object o) {
        if (super.add(o)) makeDirty();
    }

    public void manyToManyRemove(Object o) {
        if (super.remove(o)) makeDirty();
    }

    /**
     * Is the collection ordered.
     */
    public boolean isOrdered() {
        return fmd.isOrdered();
    }

    /**
     * Put references to all the values into collectionData. If the
     * values are PC instances then the instances themselves or their
     * OIDs may be stored in collectionData.
     */
    public CollectionData fillCollectionData(CollectionData collectionData) {
        int size = size();
        collectionData.valueCount = size;
        Object[] newData;
        Object[] values = collectionData.values;
        if (values == null || values.length < size) {
            newData = new Object[size];
        } else {
            newData = values;
        }
        int i = 0;
        for (Iterator it = this.iterator(); it.hasNext();) {
            newData[i++] = it.next();
        }
        collectionData.values = newData;
        return collectionData;
    }
}
