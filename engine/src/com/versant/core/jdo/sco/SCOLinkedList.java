
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.PersistenceContext;

/**
 * LinkedList SCO implementation.
 *
 * @keep-all
 */
public final class SCOLinkedList extends LinkedList implements VersantManagedSCOCollection, VersantAdvancedSCO {

    private transient PersistenceCapable owner;
    private final transient VersantFieldMetaData fmd;
    private transient VersantStateManager stateManager;
    private final transient boolean isMaster;
    private final transient boolean isMany;
    private final transient int inverseFieldNo;
    private transient Object[] originalData;
    private transient boolean beenReset;

    public SCOLinkedList(PersistenceCapable owner, VersantStateManager stateManager,
                         VersantFieldMetaData fmd, Object[] originalData) {
        this.owner = owner;
        this.isMaster = fmd.isManaged() && fmd.isMaster();
        this.inverseFieldNo = fmd.getInverseFieldNo();
        this.isMany = fmd.isManaged() && fmd.isManyToMany();
        this.fmd = fmd;
        this.stateManager = stateManager;
        this.originalData = originalData;
        int n = originalData == null ? 0 : originalData.length;
        if (!owner.jdoIsNew()) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];

                super.add( o);
            }
        } else if (isMaster) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add( o);
                SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
            }
        } else if (isMany) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add( o);
                SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
            }
        } else {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                super.add( o);
            }
        }
    }

    public Object removeFirst() {
        Object result = super.removeFirst();
        if (isMaster) {
            SCOInverseUtil.removeMasterOnDetail(result, owner, inverseFieldNo);
        } else if (isMany) {
            SCOInverseUtil.removeFromOtherSideOfManyToMany(result, inverseFieldNo, owner);
        }
        makeDirty();
        return result;
    }

    public Object removeLast() {
        Object result = super.removeLast();
        if (isMaster) {
            SCOInverseUtil.removeMasterOnDetail(result, owner, inverseFieldNo);
        } else if (isMany) {
            SCOInverseUtil.removeFromOtherSideOfManyToMany(result, inverseFieldNo, owner);
        }
        makeDirty();
        return result;
    }

    public void addFirst(Object o) {
        if (isMaster) {
            super.addFirst(o);
            makeDirty();
            SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
        } else if (isMany) {
            super.addFirst(o);
            makeDirty();
            SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
        } else {
            super.addFirst(o);
            makeDirty();
        }
    }

    public void addLast(Object o) {
        if (isMaster) {
            super.addLast(o);
            makeDirty();
            SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
        } else if (isMany) {
            super.addLast(o);
            makeDirty();
            SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
        } else {
            super.addLast(o);
            makeDirty();
        }

    }

    public boolean addAll(int index, Collection c) {
        if (isMaster) {
            boolean result = false;
            c.size(); // call this because super calls this
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o == null) {
                } else {
                    super.add(index++, o);
                    SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                    result = true;
                }
            }
            if (result) {
                makeDirty();
            }
            return result;
        } else if (isMany) {
            boolean result = false;
            c.size(); // call this because super calls this
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o == null) {
                } else {
                    super.add(index++, o);
                    SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo, owner);
                    result = true;
                }
            }
            if (result) {
                makeDirty();
            }
            return result;
        } else {
            if (super.addAll(index, c)) {
                makeDirty();
                return true;
            }
            return false;
        }
    }

    protected void removeRange(int fromIndex, int toIndex) {
        if (isMaster) {
// get unwrapped iterator from super so we can do remove
            ListIterator iter = super.listIterator(fromIndex);
            for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
                SCOInverseUtil.removeMasterOnDetail(iter.next(), owner, inverseFieldNo);
                iter.remove();
            }
        } else if (isMany) {
// get unwrapped iterator from super so we can do remove
            ListIterator iter = super.listIterator(fromIndex);
            for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(iter.next(), inverseFieldNo, owner);
                iter.remove();
            }
        } else {
            super.removeRange(fromIndex, toIndex);
        }
        makeDirty();
    }

    public boolean removeAll(Collection c) {
        boolean modified = false;
// get unwrapped iterator from super so we can do remove
        Iterator e = super.listIterator(0);
        while (e.hasNext()) {
            Object o = e.next();
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

    public boolean retainAll(Collection c) {
        boolean modified = false;
        // get an unwrapped Iterator so we can call remove
        Iterator e = super.listIterator(0);
        while (e.hasNext()) {
            Object o = e.next();
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

        if (modified) makeDirty();
        return modified;
    }

    public Object set(int index, Object element) {
        if (isMaster) {
            Object result = super.set(index, element);
            if (result != null) {
                SCOInverseUtil.removeMasterOnDetail(result, owner, inverseFieldNo);
            }
            SCOInverseUtil.addMasterOnDetail(element, owner, inverseFieldNo);
            makeDirty();
            return result;
        } else if (isMany) {
            Object result = super.set(index, element);
            if (result != null) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(result, inverseFieldNo, owner);
            }
            SCOInverseUtil.addToOtherSideOfManyToMany(element, inverseFieldNo, owner);
            makeDirty();
            return result;
        } else {
            Object result = super.set(index, element);
            makeDirty();
            return result;
        }
    }

    public ListIterator listIterator(int index) {
        return new ListIteratorImp(super.listIterator(index));
    }

    private boolean isManaged() {
        return isMaster || isMany;
    }

    /**
     * The set call in our superclass ListIterator is the only modification
     * operation that does not delegate to the list. This is disallowed for
     * managed relationships as there is no way for us to get at the current
     * element.
     */
    private class ListIteratorImp implements ListIterator {

        private ListIterator i;

        public void set(Object o) {
            if (SCOLinkedList.this.isManaged()) {
                throw BindingSupportImpl.getInstance().runtime("ListIterator.set(Object) is not supported for " +
                        "managed relationships using LinkedList: " +
                        SCOLinkedList.this.fmd.getQName());
            }
            i.set(o);
            SCOLinkedList.this.makeDirty();
        }

        public void remove() {
            if (SCOLinkedList.this.isManaged()) {
                throw BindingSupportImpl.getInstance().runtime("ListIterator.remove() is not supported for " +
                        "managed relationships using LinkedList: " +
                        SCOLinkedList.this.fmd.getQName());
            }
            i.remove();
            SCOLinkedList.this.makeDirty();
        }

        public void add(Object o) {
            if (SCOLinkedList.this.isManaged()) {
                throw BindingSupportImpl.getInstance().runtime("ListIterator.add(Object) is not supported for " +
                        "managed relationships using LinkedList: " +
                        SCOLinkedList.this.fmd.getQName());
            }
            i.add(o);
            SCOLinkedList.this.makeDirty();
        }

        public ListIteratorImp(ListIterator i) {
            this.i = i;
        }

        public boolean hasNext() {
            return i.hasNext();
        }

        public Object next() {
            return i.next();
        }

        public boolean hasPrevious() {
            return i.hasPrevious();
        }

        public Object previous() {
            return i.previous();
        }

        public int nextIndex() {
            return i.nextIndex();
        }

        public int previousIndex() {
            return i.previousIndex();
        }
    }

    public boolean add(Object o) {
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

    public void add(int index, Object element) {
        if (isMaster) {
            super.add(index, element);
            makeDirty();
            SCOInverseUtil.addMasterOnDetail(element, owner, inverseFieldNo);
        } else if (isMany) {
            super.add(index, element);
            makeDirty();
            SCOInverseUtil.addToOtherSideOfManyToMany(element, inverseFieldNo, owner);
        } else {
            super.add(index, element);
            makeDirty();
        }

    }

    public Object remove(int index) {
        Object result = super.remove(index);
        if (result != null) {
            makeDirty();
            if (isMaster) {
                SCOInverseUtil.removeMasterOnDetail(result, owner, inverseFieldNo);
            } else if (isMany) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(result, inverseFieldNo, owner);
            }
        }
        return result;
    }

    public boolean remove(Object o) {
        if (super.remove(o)) {
            makeDirty();

            if (isMaster) {
                SCOInverseUtil.removeMasterOnDetail(o, owner, inverseFieldNo);
            } else if (isMany) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(o, inverseFieldNo, owner);
            }

            return true;
        }
        return false;
    }

    public void clear() {
        if (isMaster) {
            for (Iterator iter = super.listIterator(0); iter.hasNext();) {
                SCOInverseUtil.removeMasterOnDetail(iter.next(), owner, inverseFieldNo);
            }
        } else if (isMany) {
            for (Iterator iter = super.listIterator(0); iter.hasNext();) {
                SCOInverseUtil.removeFromOtherSideOfManyToMany(iter.next(), inverseFieldNo, owner);
            }
        }
        super.clear();
        makeDirty();
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
        if (super.remove(o)) {
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
