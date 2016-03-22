
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
import com.versant.core.common.CollectionDiff;
import com.versant.core.common.VersantFieldMetaData;

import javax.jdo.spi.PersistenceCapable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.PersistenceContext;
import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.VersantStateManager;

/**
 * SCO for ArrayList.
 */
public final class SCOArrayList extends ArrayList implements VersantManagedSCOCollection, VersantAdvancedSCO {

    private transient PersistenceCapable owner;
    private final transient int managed;
    private final transient int inverseFieldNo;
    private final transient VersantFieldMetaData fmd;
    private transient VersantStateManager stateManager;
    private transient Object[] originalData;
    private transient boolean beenReset;

    public SCOArrayList(PersistenceCapable owner, VersantStateManager stateManager,
                        VersantFieldMetaData fmd, Object[] originalData) {
        this.owner = owner;
        if (fmd.isManaged()) {
            if (fmd.isMaster()) {
                managed = MANAGED_ONE_TO_MANY;
            } else if (fmd.isManyToMany()) {
                managed = MANAGED_MANY_TO_MANY;
            } else {
                managed = MANAGED_NONE;
            }
        } else {
            managed = MANAGED_NONE;
        }
        this.inverseFieldNo = fmd.getInverseFieldNo();
        this.stateManager = stateManager;
        this.fmd = fmd;
        this.originalData = originalData;
        int n = originalData == null ? 0 : originalData.length;
        if (n > 0) ensureCapacity(n);
        if (!owner.jdoIsNew()) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
//                if (o == null) break;

                super.add( o);
            }
        } else {
            switch (managed) {
                case MANAGED_ONE_TO_MANY:
                    for (int i = 0; i < n; i++) {
                        Object o = originalData[i];
//                        if (o == null) throw createNPE();
                        super.add( o);
                        SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                    }
                    break;
                case MANAGED_MANY_TO_MANY:
                    for (int i = 0; i < n; i++) {
                        Object o = originalData[i];
//                        if (o == null) throw createNPE();
                        super.add( o);
                        SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                    }
                    break;
                default:
                    for (int i = 0; i < n; i++) {
                        Object o = originalData[i];
//                        if (o == null) throw createNPE();
                        super.add( o);
                    }
            }
            ;
        }
    }

    private RuntimeException createNPE() {
        return BindingSupportImpl.getInstance().nullElement("Null element not allowed: " + fmd.getQName());
    }

    public Object set(int index, Object element) {
//        if (element == null) throw createNPE();
        Object result;
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                result = super.set(index, element);
                makeDirty();
                if (result != null) {
                    SCOInverseUtil.removeMasterOnDetail(result, owner, inverseFieldNo);
                }
                SCOInverseUtil.addMasterOnDetail(element, owner, inverseFieldNo);
                return result;
            case MANAGED_MANY_TO_MANY:
                result = super.set(index, element);
                makeDirty();
                if (result != null) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(result, inverseFieldNo, owner);
                }
                SCOInverseUtil.addToOtherSideOfManyToMany(element, inverseFieldNo, owner);
                return result;
        }
        result = super.set(index, element);
        makeDirty();
        return result;
    }

    public boolean add(Object o) {
//        if (o == null) throw createNPE();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                super.add(o);
                makeDirty();
                SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                return true;
            case MANAGED_MANY_TO_MANY:
                super.add(o);
                makeDirty();
                SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                return true;
        }
        super.add(o);
        makeDirty();
        return true;
    }

    public void add(int index, Object element) {
//        if (element == null) throw createNPE();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                super.add(index, element);
                makeDirty();
                SCOInverseUtil.addMasterOnDetail(element, owner, inverseFieldNo);
                return;
            case MANAGED_MANY_TO_MANY:
                super.add(index, element);
                makeDirty();
                SCOInverseUtil.addToOtherSideOfManyToMany(element, inverseFieldNo, owner);
                return;
        }
        super.add(index, element);
        makeDirty();
    }

    public Object remove(int index) {
        Object result = super.remove(index);
        if (result == null) return null; // ok as we do not allow nulls in list
        makeDirty();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                SCOInverseUtil.removeMasterOnDetail(result, owner, inverseFieldNo);
                break;
            case MANAGED_MANY_TO_MANY:
                SCOInverseUtil.removeFromOtherSideOfManyToMany(result, inverseFieldNo, owner);
                break;
        }
        return result;
    }

    public boolean remove(Object o) {
        // Finding the index and removing it is faster as ArrayList leaves this
        // to AbstractCollection which creates an Iterator to search for the
        // element and calls Iterator.remove when it is found. This in turn
        // calls remove(index) on us. Braindead but true!
        int i = indexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    public boolean addAll(Collection c) {
        Iterator i;
        Object o;
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                i = c.iterator();
                if (!i.hasNext()) return false;
                ensureCapacity(c.size() + size());
                o = null;
                for (; ;) {
                    o = i.next();
//                    if (o == null) {
//                        throw createNPE();
//                    } else {
                        super.add(o);
                        SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
//                    }
                    if (!i.hasNext()) break;
                }
                makeDirty();
                return true;
            case MANAGED_MANY_TO_MANY:
                i = c.iterator();
                if (!i.hasNext()) return false;
                ensureCapacity(c.size() + size());
                o = null;
                for (; ;) {
                    o = i.next();
//                    if (o == null) {
//                        throw createNPE();
//                    } else {
                        super.add(o);
                        SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
//                    }
                    if (!i.hasNext()) break;
                }
                makeDirty();
                return true;
        }
        ;
        if (super.addAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public void clear() {
        int n;
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                n = size();
                for (int i = 0; i < n; i++) {
                    SCOInverseUtil.removeMasterOnDetail(get(i), owner, inverseFieldNo);
                }
                break;
            case MANAGED_MANY_TO_MANY:
                n = size();
                for (int i = 0; i < n; i++) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(get(i), inverseFieldNo, owner);
                }
                break;
        }
        super.clear();
        makeDirty();
    }

    public boolean addAll(int index, Collection c) {
        boolean result;
        int colSize;
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                result = false;
                colSize = c.size();
                ensureCapacity(size() + colSize);
                for (Iterator iter = c.iterator(); iter.hasNext();) {
                    Object o = iter.next();
//                    if (o == null) {
//                        throw createNPE();
//                    } else {
                        super.add(index++, o);
                        SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                        result = true;
//                    }
                }
                if (result) makeDirty();
                return result;
            case MANAGED_MANY_TO_MANY:
                result = false;
                colSize = c.size();
                ensureCapacity(size() + colSize);
                for (Iterator iter = c.iterator(); iter.hasNext();) {
                    Object o = iter.next();
//                    if (o == null) {
//                        throw createNPE();
//                    } else {
                        super.add(index++, o);
                        SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                        result = true;
//                    }
                }
                if (result) makeDirty();
                return result;
        }
        ;
        result = super.addAll(index, c);
        if (result) makeDirty();
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                for (int i = fromIndex; i < toIndex; i++) {
                    SCOInverseUtil.removeMasterOnDetail(get(i), owner, inverseFieldNo);
                }
                break;
            case MANAGED_MANY_TO_MANY:
                for (int i = fromIndex; i < toIndex; i++) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(get(i), inverseFieldNo, owner);
                }
                break;
        }
        super.removeRange(fromIndex, toIndex);
        makeDirty();
    }

    public boolean removeAll(Collection c) {
        boolean modified;
        Iterator e;
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                modified = false;
                e = super.iterator();
                while (e.hasNext()) {
                    Object o = e.next();
                    if (c.contains(o)) {
                        e.remove();
                        SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                        modified = true;
                    }
                }
                if (modified) makeDirty();
                return modified;
            case MANAGED_MANY_TO_MANY:
                modified = false;
                e = super.iterator();
                while (e.hasNext()) {
                    Object o = e.next();
                    if (c.contains(o)) {
                        e.remove();
                        SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                        modified = true;
                    }
                }
                if (modified) makeDirty();
                return modified;
        }
        if (super.removeAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = super.iterator();
        while (e.hasNext()) {
            Object o = e.next();
            if (!c.contains(o)) {
                e.remove();
                modified = true;
                switch (managed) {
                    case MANAGED_ONE_TO_MANY:
                        SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                        break;
                    case MANAGED_MANY_TO_MANY:
                        SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                        break;
                }
            }
        }
        if (modified) makeDirty();
        return modified;
    }

    public CollectionDiff getCollectionDiff(PersistenceContext pm) {
        Object[] data = toArray();
        if (fmd.isOrdered()) {
            return CollectionDiffUtil.getOrderedCollectionDiff(fmd, pm,
                    data, data.length, (owner.jdoIsNew() && !beenReset) ? null : originalData);
        } else {
            return CollectionDiffUtil.getUnorderedCollectionDiff(fmd, pm,
                    data, data.length, (owner.jdoIsNew() && !beenReset) ? null : originalData);
        }
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

    public void manyToManyAdd(Object o) {
        super.add(o);
        makeDirty();
    }

    public void manyToManyRemove(Object o) {
// We cannot just call super.remove(o). ArrayList leaves this up to
// AbstractCollection which creates an Iterator to search for the
// element and calls Iterator.remove when it is found. This in turn
// calls remove(index) on us. Braindead but true!
        int i = indexOf(o);
        if (i < 0) return;
        super.remove(i);
        makeDirty();
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
        for (int i = 0; i < size; i++) {
            newData[i] = get(i);
        }
        collectionData.values = newData;
        return collectionData;
    }
}
