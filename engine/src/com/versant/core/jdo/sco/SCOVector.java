
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
import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.PersistenceContext;

/**
 * A SCO implementation of a Vector.
 */
public final class SCOVector extends Vector implements VersantManagedSCOCollection, VersantAdvancedSCO {

    private transient PersistenceCapable owner;
    private final transient VersantFieldMetaData fmd;
    private transient VersantStateManager stateManager;
    private final transient boolean isMaster;
    private final transient boolean isMany;
    private final transient int inverseFieldNo;
    private transient Object[] originalData;
    private transient boolean beenReset;

    public SCOVector(PersistenceCapable owner, VersantStateManager stateManager,
                     VersantFieldMetaData fmd, Object[] originalData) {
        this.owner = owner;
        this.fmd = fmd;
        this.isMaster = fmd.isManaged() && fmd.isMaster();
        this.isMany = fmd.isManaged() && fmd.isManyToMany();
        this.stateManager = stateManager;
        this.inverseFieldNo = fmd.getInverseFieldNo();
        this.originalData = originalData;
        int n = originalData == null ? 0 : originalData.length;
        if (n > 0) ensureCapacity(n);
        if (!owner.jdoIsNew()) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add(o);
            }
        } else if (isMaster) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add(o);
                SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
            }
        } else if (isMany) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add(o);
                SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
            }
        } else {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add(o);
            }
        }
    }

    public synchronized void setSize(int newSize) {
        if (newSize > elementCount) {
            throw BindingSupportImpl.getInstance().nullElement("setSize called with " + newSize + " > " + elementCount + " and " +
                    "null elements are not allowed: " + fmd.getQName());
        }
        if (isMaster) {
            for (int i = newSize; i < elementCount; i++) {
                SCOInverseUtil.removeMasterOnDetail(elementData[i], owner, inverseFieldNo);
            }
            super.setSize(newSize);
            makeDirty();
        } else if (isMany) {
            for (int i = newSize; i < elementCount; i++) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(elementData[i], inverseFieldNo, owner);
            }
            super.setSize(newSize);
            makeDirty();
        } else {
            super.setSize(newSize);
            makeDirty();
        }

    }

    public synchronized void setElementAt(Object obj, int index) {
        if (isMaster) {
            if (index >= elementCount) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(index + " >= " +
                        elementCount);
            }
            if (elementData[index] != null) {
                SCOInverseUtil.removeMasterOnDetail(elementData[index], owner, inverseFieldNo);
            }
            SCOInverseUtil.addMasterOnDetail(obj, owner, inverseFieldNo);
            super.setElementAt(obj, index);
            makeDirty();
        } else if (isMany) {
            if (index >= elementCount) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(index + " >= " +
                        elementCount);
            }
            if (elementData[index] != null) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(elementData[index], inverseFieldNo, owner);
            }
            SCOInverseUtil.addToOtherSideOfManyToMany(obj, inverseFieldNo, owner);
            super.setElementAt(obj, index);
            makeDirty();
        } else {
            super.setElementAt(obj, index);
            makeDirty();
        }

    }

    public synchronized void removeElementAt(int index) {

        if (isMaster) {
            if (index >= elementCount) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(index + " >= " +
                        elementCount);
            } else if (index < 0) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(index);
            }
            SCOInverseUtil.removeMasterOnDetail(elementData[index], owner, inverseFieldNo);
        } else if (isMany) {
            if (index >= elementCount) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(index + " >= " +
                        elementCount);
            } else if (index < 0) {
                throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds(index);
            }
            SCOInverseUtil.removeFromOtherSideOfManyToMany(elementData[index], inverseFieldNo, owner);
        }
        super.removeElementAt(index);

        makeDirty();
    }

    public synchronized void insertElementAt(Object obj, int index) {
        if (isMaster) {
            super.insertElementAt(obj, index);
            SCOInverseUtil.addMasterOnDetail(obj, owner, inverseFieldNo);
            makeDirty();
        } else if (isMany) {
            super.insertElementAt(obj, index);
            SCOInverseUtil.addToOtherSideOfManyToMany(obj, inverseFieldNo, owner);
            makeDirty();
        } else {
            makeDirty();
            super.insertElementAt(obj, index);
        }
    }

    public synchronized void addElement(Object obj) {
        if (isMaster) {
            super.addElement(obj);
            makeDirty();
            SCOInverseUtil.addMasterOnDetail(obj, owner, inverseFieldNo);
        } else if (isMany) {
            super.addElement(obj);
            makeDirty();
            SCOInverseUtil.addToOtherSideOfManyToMany(obj, inverseFieldNo, owner);
        } else {
            super.addElement(obj);
            makeDirty();
        }
    }

    public synchronized void removeAllElements() {

        modCount++;
        // Let gc do its work
        for (int i = 0; i < elementCount; i++) {
            if (isMaster) {
                SCOInverseUtil.removeMasterOnDetail(elementData[i], owner, inverseFieldNo);
            } else if (isMany) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(elementData[i], inverseFieldNo, owner);
            }
            elementData[i] = null;
        }

        elementCount = 0;
        makeDirty();
    }

    public synchronized Object set(int index, Object element) {
        if (isMaster) {
            Object obj = super.set(index, element);
            if (obj != null) {
                SCOInverseUtil.removeMasterOnDetail(obj, owner, inverseFieldNo);
            }
            SCOInverseUtil.addMasterOnDetail(element, owner, inverseFieldNo);
            makeDirty();
            return obj;
        } else if (isMany) {
            Object obj = super.set(index, element);
            if (obj != null) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(obj, inverseFieldNo, owner);
            }
            SCOInverseUtil.addToOtherSideOfManyToMany(element, inverseFieldNo, owner);
            makeDirty();
            return obj;
        } else {
            Object obj = super.set(index, element);
            makeDirty();
            return obj;
        }
    }

    public synchronized boolean add(Object o) {
        if (isMaster) {
            super.add(o);
            makeDirty();
            SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
            return true;
        } else if (isMany) {
            super.add(o);
            makeDirty();
            SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
            return true;
        } else {
            super.add(o);
            makeDirty();
            return true;
        }
    }

    public synchronized Object remove(int index) {
        Object obj = super.remove(index);
        if (isMaster) {
            SCOInverseUtil.removeMasterOnDetail(obj, owner, inverseFieldNo);
        } else if (isMany) {
            SCOInverseUtil.removeFromOtherSideOfManyToMany(obj, inverseFieldNo, owner);
        }
        makeDirty();
        return obj;
    }

    public synchronized boolean addAll(Collection c) {
        if (isMaster) {
            boolean added = false;
            ensureCapacity(elementCount + c.size());
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o == null) {
                } else {
                    added = super.add(o);
                    SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                }
            }
            if (added) {
                makeDirty();
            }
            return added;
        } else if (isMany) {
            boolean added = false;
            ensureCapacity(elementCount + c.size());
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o == null) {
                } else {
                    added = super.add(o);
                    SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                }
            }
            if (added) {
                makeDirty();
            }
            return added;
        } else if (super.addAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public synchronized boolean removeAll(Collection c) {
        boolean modified = false;
        Iterator e = super.iterator();
        Object o = null;
        while (e.hasNext()) {
            o = e.next();
            if (c.contains(o)) {
                e.remove();
                modified = true;
                if (isMaster) {
                    SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                } else if (isMany) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                }
            }
        }
        if (modified) makeDirty();
        return modified;
    }

    public synchronized boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = super.iterator();
        Object o = null;
        while (e.hasNext()) {
            o = e.next();
            if (!c.contains(o)) {
                e.remove();
                modified = true;
                if (isMaster) {
                    SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
                } else if (isMany) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
                }
            }
        }

        if (modified) {
            makeDirty();
        }
        return modified;
    }

    public synchronized boolean addAll(int index, Collection c) {
        if (isMaster) {
            boolean added = false;
            ensureCapacity(elementCount + c.size());
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o == null) {
                } else {
                    super.insertElementAt(o, index++);
                    SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                    added = true;
                }
            }
            if (added) {
                makeDirty();
            }
            return added;
        } else if (isMany) {
            boolean added = false;
            ensureCapacity(elementCount + c.size());
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o == null) {
                } else {
                    super.insertElementAt(o, index++);
                    SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                    added = true;
                }
            }
            if (added) {
                makeDirty();
            }
            return added;
        } else if (super.addAll(index, c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        if (isMaster) {
            List removeList = super.subList(fromIndex, toIndex);
            for (int i = 0; i < removeList.size(); i++) {
                SCOInverseUtil.removeMasterOnDetail(get(i), owner, inverseFieldNo);
            }
        } else if (isMany) {
            List removeList = super.subList(fromIndex, toIndex);
            for (int i = 0; i < removeList.size(); i++) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(get(i), inverseFieldNo, owner);
            }
        }

        super.removeRange(fromIndex, toIndex);
        makeDirty();
    }

    public ListIterator listIterator() {
        return new SCOListIterator(super.listIterator(), stateManager, owner, fmd.getManagedFieldNo());
    }

    public ListIterator listIterator(int index) {
        return new SCOListIterator(super.listIterator(index), stateManager, owner, fmd.getManagedFieldNo());
    }

    public Iterator iterator() {
        return new SCOIterator(super.iterator(), stateManager, owner, fmd.getManagedFieldNo());
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
        if (super.removeElement(o)) {
            makeDirty();
        }
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
