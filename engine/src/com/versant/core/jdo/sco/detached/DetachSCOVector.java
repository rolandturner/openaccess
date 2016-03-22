
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
import com.versant.core.jdo.sco.SCOIterator;
import com.versant.core.jdo.sco.SCOListIterator;
import com.versant.core.jdo.VersantStateManager;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import com.versant.core.common.BindingSupportImpl;

/**
 * A Detached SCO implementation of a Vector.
 */
public final class DetachSCOVector extends Vector implements Serializable, VersantSimpleSCO {

    private PersistenceCapable owner;
    private int fierldNo;
    private VersantStateManager stateManager;

    public DetachSCOVector(PersistenceCapable owner, VersantStateManager stateManager,
                           VersantFieldMetaData fmd, Object[] originalData) {
        this.owner = owner;
        this.fierldNo = fmd.getManagedFieldNo();
        this.stateManager = stateManager;
        int n = originalData == null ? 0 : originalData.length;
        if (n > 0) ensureCapacity(n);
        if (!owner.jdoIsNew()) {
            for (int i = 0; i < n; i++) {
                Object o = originalData[i];
                if (o == null) break;
                super.add(o);
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
        return BindingSupportImpl.getInstance().nullElement("Null element not allowed.");
    }

    public synchronized void setSize(int newSize) {
        if (newSize > elementCount) {
            throw BindingSupportImpl.getInstance().nullElement("setSize called with " + newSize + " > " + elementCount + " and " +
                    "null elements are not allowed.");
        }
        super.setSize(newSize);
        makeDirty();
    }

    public synchronized void setElementAt(Object obj, int index) {
        if (obj == null) throw createNPE();
        super.setElementAt(obj, index);
        makeDirty();
    }

    public synchronized void removeElementAt(int index) {
        super.removeElementAt(index);
        makeDirty();
    }

    public synchronized void insertElementAt(Object obj, int index) {
        if (obj == null) throw createNPE();
        makeDirty();
        super.insertElementAt(obj, index);
    }

    public synchronized void addElement(Object obj) {
        if (obj == null) throw createNPE();
        super.addElement(obj);
        makeDirty();
    }

    public synchronized void removeAllElements() {
        modCount++;
        // Let gc do its work
        for (int i = 0; i < elementCount; i++) {
            elementData[i] = null;
        }
        elementCount = 0;
        makeDirty();
    }

    public synchronized Object set(int index, Object element) {
        if (element == null) throw createNPE();
        Object obj = super.set(index, element);
        makeDirty();
        return obj;
    }

    public synchronized boolean add(Object o) {
        if (o == null) throw createNPE();
        super.add(o);
        makeDirty();
        return true;
    }

    public synchronized Object remove(int index) {
        Object obj = super.remove(index);
        makeDirty();
        return obj;
    }

    public synchronized boolean addAll(Collection c) {
        if (super.addAll(c)) {
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
            }
        }
        if (modified) {
            makeDirty();
        }
        return modified;
    }

    public synchronized boolean addAll(int index, Collection c) {
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

    public ListIterator listIterator() {
        return new SCOListIterator(super.listIterator(), stateManager, owner, fierldNo);
    }

    public ListIterator listIterator(int index) {
        return new SCOListIterator(super.listIterator(index), stateManager, owner, fierldNo);
    }

    public Iterator iterator() {
        return new SCOIterator(super.iterator(), stateManager, owner, fierldNo);
    }

    private void makeDirty() {
        if (stateManager != null && owner != null) {
            stateManager.makeDirty(owner, fierldNo);
        }
    }

    public void makeTransient() {
        owner = null;
        stateManager = null;
    }
}
