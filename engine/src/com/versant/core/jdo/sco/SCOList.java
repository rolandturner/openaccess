
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

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

import com.versant.core.common.*;
import com.versant.core.metadata.FieldMetaData;

/**
 * SCO for List. This is faster than SCOArrayList.
 */
public final class SCOList extends AbstractList
        implements VersantManagedSCOCollection, Serializable,
        VersantAdvancedSCO {

    // Same as ArrayList
    private static final long serialVersionUID = 8683452581122892189L;

    private transient Object[] data;
    private int size;

    private transient PersistenceCapable owner;
    private final transient int managed;
    private final transient int inverseFieldNo;
    private final transient VersantFieldMetaData fmd;
    private transient VersantStateManager stateManager;
    private transient Object[] originalData;
    private transient boolean beenReset;

    public SCOList(PersistenceCapable owner, VersantStateManager stateManager,
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
        if (n == 0) {
            data = new Object[10];
        } else {
            data = new Object[n];
            if (owner.jdoIsNew()) {
                size = n;
                switch (managed) {
                    case MANAGED_ONE_TO_MANY:
                        for (int i = 0; i < n; i++) {
                            Object o = originalData[i];
                            if (Debug.DEBUG) {
                                checkNull(o);
                            }
                            SCOInverseUtil.addMasterOnDetail(o, owner,
                                    inverseFieldNo);
                            data[i] = o;
                        }
                        break;
                    case MANAGED_MANY_TO_MANY:
                        for (int i = 0; i < n; i++) {
                            Object o = originalData[i];
                            if (Debug.DEBUG) {
                                checkNull(o);
                            }
                            SCOInverseUtil.addToOtherSideOfManyToMany(o,
                                    inverseFieldNo, owner);
                            data[i] = o;
                        }
                        break;
                    default:
                        for (int i = 0; i < n; i++) {
                            Object o = originalData[i];
                            if (Debug.DEBUG) {
                                checkNull(o);
                            }
                            data[i] = o;
                        }
                }
                ;
            } else {
                int i;
                for (i = 0; i < n; i++) {
                    Object o = originalData[i];
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    data[i] = o;
                }
                size = i;
            }
        }
    }

    public Object get(int index) {
        checkIndex(index);
        return data[index];
    }

    public int size() {
        return size;
    }

    private RuntimeException createNPE() {
        return BindingSupportImpl.getInstance().nullElement(
                "Null element not allowed: " + fmd.getQName());
    }

    public boolean add(Object o) {
        if (Debug.DEBUG) {
            checkNull(o);
        }

        if (size == data.length) expand();
        data[size++] = o;
        makeDirty();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                break;
            case MANAGED_MANY_TO_MANY:
                SCOInverseUtil.addToOtherSideOfManyToMany(o, inverseFieldNo,
                        owner);
                break;
        }
        return true;
    }

    private void checkNull(Object o) {
        if (!((FieldMetaData)fmd).ordered && o == null) {
            throw createNPE();
        }
    }

    private void expand() {
        modCount++;
        Object[] a = new Object[(size * 3) / 2 + 1];
        System.arraycopy(data, 0, a, 0, size);
        data = a;
    }

    private void ensureCapacity(int minSize) {
        modCount++;
        int n = (size * 3) / 2 + 1;
        if (n < minSize) n = minSize;
        Object[] a = new Object[n];
        System.arraycopy(data, 0, a, 0, size);
        data = a;
    }

    public void add(int index, Object element) {
        if (Debug.DEBUG) {
            checkNull(element);
        }
        if (index > size || index < 0) {
            throw BindingSupportImpl.getInstance().indexOutOfBounds(
                    "Index: " + index + ", Size: " + size);
        }
        if (size == data.length) expand();
        if (index < size) {
            System.arraycopy(data, index, data, index + 1, size - index);
        }
        data[index] = element;
        size++;
        makeDirty();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                SCOInverseUtil.addMasterOnDetail(element, owner,
                        inverseFieldNo);
                break;
            case MANAGED_MANY_TO_MANY:
                SCOInverseUtil.addToOtherSideOfManyToMany(element,
                        inverseFieldNo, owner);
                break;
        }
    }

    private void checkIndex(int index) {
        if (index >= size || index < 0) {
            throw BindingSupportImpl.getInstance().indexOutOfBounds(
                    "Index: " + index + ", Size: " + size);
        }
    }

    public Object set(int index, Object element) {
        if (Debug.DEBUG) {
            checkNull(element);
        }
        checkIndex(index);
        Object result = data[index];
        data[index] = element;
        makeDirty();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                if (result != null) {
                    SCOInverseUtil.removeMasterOnDetail(result, owner,
                            inverseFieldNo);
                }
                SCOInverseUtil.addMasterOnDetail(element, owner,
                        inverseFieldNo);
                break;
            case MANAGED_MANY_TO_MANY:
                if (result != null) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(result,
                            inverseFieldNo, owner);
                }
                SCOInverseUtil.addToOtherSideOfManyToMany(element,
                        inverseFieldNo, owner);
                break;
        }
        return result;
    }

    public Object remove(int index) {
        checkIndex(index);
        modCount++;
        makeDirty();
        return removeImp(index);
    }

    private Object removeImp(int index) {
        Object result = data[index];
        int n = size - index - 1;
        if (n > 0) System.arraycopy(data, index + 1, data, index, n);
        data[--size] = null;
        if (result == null) return null; // ok as we do not allow nulls in list
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                SCOInverseUtil.removeMasterOnDetail(result, owner,
                        inverseFieldNo);
                break;
            case MANAGED_MANY_TO_MANY:
                SCOInverseUtil.removeFromOtherSideOfManyToMany(result,
                        inverseFieldNo, owner);
                break;
        }
        return result;
    }

    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (data[i] == o) return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(data[i])) return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (data[i] == o) return i;
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(data[i])) return i;
            }
        }
        return -1;
    }

    public void clear() {
        modCount++;
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                for (int i = 0; i < size; i++) {
                    SCOInverseUtil.removeMasterOnDetail(data[i], owner,
                            inverseFieldNo);
                    data[i] = null;
                }
                break;
            case MANAGED_MANY_TO_MANY:
                for (int i = 0; i < size; i++) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(data[i],
                            inverseFieldNo, owner);
                    data[i] = null;
                }
                break;
            default:
                for (int i = 0; i < size; i++) data[i] = null;
        }
        size = 0;
        makeDirty();
    }

    public boolean addAll(int index, Collection c) {
        if (index > size || index < 0) {
            throw BindingSupportImpl.getInstance().indexOutOfBounds(
                    "Index: " + index + ", Size: " + size);
        }
        int numNew = c.size();
        if (numNew == 0) return false;
        int newSize = size + numNew;
        if (newSize > data.length) ensureCapacity(newSize);

        int n = size - index;
        if (n > 0) System.arraycopy(data, index, data, index + numNew, n);

        Iterator e = c.iterator();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                for (int i = 0; i < numNew; i++) {
                    Object o = e.next();
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                    data[index++] = o;
                }
                break;
            case MANAGED_MANY_TO_MANY:
                for (int i = 0; i < numNew; i++) {
                    Object o = e.next();
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    SCOInverseUtil.addToOtherSideOfManyToMany(o,
                            inverseFieldNo, owner);
                    data[index++] = o;
                }
                break;
            default:
                for (int i = 0; i < numNew; i++) {
                    Object o = e.next();
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    data[index++] = o;
                }
        }

        size += numNew;
        makeDirty();
        return true;
    }

    public boolean addAll(Collection c) {
        int numNew = c.size();
        if (numNew == 0) return false;
        int newSize = size + numNew;
        if (newSize > data.length) ensureCapacity(newSize);

        Iterator e = c.iterator();
        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                for (int i = 0; i < numNew; i++) {
                    Object o = e.next();
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    SCOInverseUtil.addMasterOnDetail(o, owner, inverseFieldNo);
                    data[size++] = o;
                }
                break;
            case MANAGED_MANY_TO_MANY:
                for (int i = 0; i < numNew; i++) {
                    Object o = e.next();
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    SCOInverseUtil.addToOtherSideOfManyToMany(o,
                            inverseFieldNo, owner);
                    data[size++] = o;
                }
                break;
            default:
                for (int i = 0; i < numNew; i++) {
                    Object o = e.next();
                    if (Debug.DEBUG) {
                        checkNull(o);
                    }
                    data[size++] = o;
                }
        }

        makeDirty();
        return true;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;

        switch (managed) {
            case MANAGED_ONE_TO_MANY:
                for (int i = fromIndex; i < toIndex; i++) {
                    SCOInverseUtil.removeMasterOnDetail(data[i], owner,
                            inverseFieldNo);
                }
                break;
            case MANAGED_MANY_TO_MANY:
                for (int i = fromIndex; i < toIndex; i++) {
                    SCOInverseUtil.removeFromOtherSideOfManyToMany(data[i],
                            inverseFieldNo, owner);
                }
                break;
        }

        int numMoved = size - toIndex;
        if (numMoved > 0) {
            System.arraycopy(data, toIndex, data, fromIndex,
                    numMoved);
        }

        int newSize = size - (toIndex - fromIndex);
        while (size != newSize) data[--size] = null;

        makeDirty();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (data[i] == o) return true;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(data[i])) return true;
            }
        }
        return false;
    }

    public Object[] toArray() {
        Object[] a = new Object[size];
        System.arraycopy(data, 0, a, 0, size);
        return a;
    }

    public Object[] toArray(Object a[]) {
        if (a.length < size) {
            a = (Object[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }
        System.arraycopy(data, 0, a, 0, size);
        if (a.length > size) a[size] = null;
        return a;
    }

    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    public Object clone() {
        try {
            SCOList v = (SCOList)super.clone();
            v.data = new Object[size];
            System.arraycopy(data, 0, v.data, 0, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Same format as ArrayList.
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(data.length);
        for (int i = 0; i < size; i++) s.writeObject(data[i]);
    }

    /**
     * Same format as ArrayList.
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int arrayLength = s.readInt();
        data = new Object[arrayLength];
        for (int i = 0; i < size; i++) data[i] = s.readObject();
    }

    public CollectionDiff getCollectionDiff(PersistenceContext pm) {
        if (fmd.isOrdered()) {
            return CollectionDiffUtil.getOrderedCollectionDiff(fmd, pm,
                    data, size,
                    (owner.jdoIsNew() && !beenReset) ? null : originalData);
        } else {
            return CollectionDiffUtil.getUnorderedCollectionDiff(fmd, pm,
                    data, size,
                    (owner.jdoIsNew() && !beenReset) ? null : originalData);
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
        if (size == data.length) expand();
        data[size++] = o;
        makeDirty();
    }

    public void manyToManyRemove(Object o) {
        int i = indexOf(o);
        if (i < 0) return;
        modCount++;
        int n = size - i - 1;
        if (n > 0) System.arraycopy(data, i + 1, data, i, n);
        data[--size] = null;
        makeDirty();
    }

    public boolean containsAll(Collection c) {
        for (Iterator i = c.iterator(); i.hasNext();) {
            if (indexOf(i.next()) < 0) return false;
        }
        return true;
    }

    public boolean removeAll(Collection c) {
        int mc = modCount;
        for (Iterator i = c.iterator(); i.hasNext();) {
            int index = indexOf(i.next());
            if (index >= 0) {
                removeImp(index);
                modCount++;
            }
        }
        if (mc != modCount) {
            makeDirty();
            return true;
        } else {
            return false;
        }
    }

    public boolean retainAll(Collection c) {
        int mc = modCount;
        for (int i = 0; i < size;) {
            if (c.contains(data[i])) {
                i++;
            } else {
                removeImp(i);
                modCount++;
            }
        }
        if (mc != modCount) {
            makeDirty();
            return true;
        } else {
            return false;
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
        System.arraycopy(data, 0, newData, 0, size);
        collectionData.values = newData;
        return collectionData;
    }
}
