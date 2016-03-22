
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

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.versant.core.common.BindingSupportImpl;

/**
 * Detached LinkedList SCO implementation.
 */
public final class DetachSCOLinkedList extends LinkedList implements Serializable, VersantSimpleSCO {

    private PersistenceCapable owner;
    private int fieldNo;
    private VersantStateManager stateManager;

    public DetachSCOLinkedList(PersistenceCapable owner, VersantStateManager stateManager,
                               VersantFieldMetaData fmd, Object[] originalData) {
        this.owner = owner;
        this.fieldNo = fmd.getManagedFieldNo();
        this.stateManager = stateManager;
        int n = originalData == null ? 0 : originalData.length;
        for (int i = 0; i < n; i++) {
            Object o = originalData[i];
            if (o == null) throw createNPE();
            super.add(o);
        }
    }

    public Object removeFirst() {
        Object result = super.removeFirst();
        makeDirty();
        return result;
    }

    public Object removeLast() {
        Object result = super.removeLast();
        makeDirty();
        return result;
    }

    private RuntimeException createNPE() {
        return BindingSupportImpl.getInstance().nullElement("Null element not allowed.");
    }

    public void addFirst(Object o) {
        if (o == null) throw createNPE();
        super.addFirst(o);
        makeDirty();
    }

    public void addLast(Object o) {
        if (o == null) throw createNPE();
        super.addLast(o);
        makeDirty();
    }

    public boolean addAll(int index, Collection c) {
        if (super.addAll(index, c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
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
            }
        }
        if (modified) makeDirty();
        return modified;
    }

    public Object set(int index, Object element) {
        if (element == null) throw createNPE();
        Object result = super.set(index, element);
        makeDirty();
        return result;
    }

    public ListIterator listIterator(int index) {
        return new ListIteratorImp(super.listIterator(index));
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
            i.set(o);
            DetachSCOLinkedList.this.makeDirty();
        }

        public void remove() {
            i.remove();
            DetachSCOLinkedList.this.makeDirty();
        }

        public void add(Object o) {
            i.add(o);
            DetachSCOLinkedList.this.makeDirty();
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
        if (o == null) throw createNPE();
        super.add(o);
        makeDirty();
        return true;
    }

    public void add(int index, Object element) {
        if (element == null) throw createNPE();
        super.add(index, element);
        makeDirty();
    }

    public Object remove(int index) {
        Object result = super.remove(index);
        if (result != null) {
            makeDirty();
        }
        return result;
    }

    public boolean remove(Object o) {
        if (super.remove(o)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public void clear() {
        super.clear();
        makeDirty();
    }

    private void makeDirty() {
        if (stateManager != null && owner != null) {
            stateManager.makeDirty(owner, fieldNo);
        }
    }

    public void makeTransient() {
        owner = null;
        stateManager = null;
    }
}
