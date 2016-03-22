
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

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;

/**
 * Detached SCO for ArrayList.
 */
public final class DetachSCOArrayList extends ArrayList implements Serializable, VersantSimpleSCO {

    private int fieldNo;
    private VersantStateManager stateManager;
    private PersistenceCapable owner;

    public DetachSCOArrayList(PersistenceCapable owner, VersantStateManager stateManager,
                              VersantFieldMetaData fmd, Object[] originalData) {
        this.fieldNo = fmd.getManagedFieldNo();
        this.stateManager = stateManager;
        this.owner = owner;
        int n = originalData == null ? 0 : originalData.length;
        if (n > 0) ensureCapacity(n);
        for (int i = 0; i < n; i++) {
            Object o = originalData[i];
            if (o == null) throw createNPE();
            super.add(o);
        }
    }

    private RuntimeException createNPE() {
        return BindingSupportImpl.getInstance().nullElement("Null element not allowed.");
    }

    public Object set(int index, Object element) {
        if (element == null) throw createNPE();
        Object result = super.set(index, element);
        makeDirty();
        return result;
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
        if (result == null) return null; // ok as we do not allow nulls in list
        makeDirty();
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
        if (super.addAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public void clear() {
        super.clear();
        makeDirty();
    }

    public boolean addAll(int index, Collection c) {
        boolean result;
        result = super.addAll(index, c);
        if (result) makeDirty();
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        makeDirty();
    }

    public boolean removeAll(Collection c) {
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
            }
        }
        if (modified) makeDirty();
        return modified;
    }

    public void makeDirty() {
        if (stateManager != null && owner != null) {
            stateManager.makeDirty(owner, fieldNo);
        }
    }

    public Iterator iterator() {
        return new SCOIterator(super.iterator(), stateManager, owner, fieldNo);
    }

    public void makeTransient() {
        owner = null;
        stateManager = null;
    }
}
