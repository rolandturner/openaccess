
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
import com.versant.core.jdo.VersantStateManager;

import javax.jdo.spi.PersistenceCapable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;

/**
 * Detached SCO for a HashSet.
 */
public class DetachSCOHashSet extends HashSet implements Serializable, VersantSimpleSCO {

    private PersistenceCapable owner;
    private VersantStateManager stateManager;
    private int fieldNo;

    public DetachSCOHashSet(PersistenceCapable owner, VersantStateManager stateManager,
                            VersantFieldMetaData fmd, Object[] originalData) {
        this.owner = owner;
        this.stateManager = stateManager;
        this.fieldNo = fmd.getManagedFieldNo();
        int n = originalData == null ? 0 : originalData.length;
        for (int i = 0; i < n; i++) {
            Object o = originalData[i];
            if (o == null) throw createNPE();
            super.add(o);
        }
    }

    private RuntimeException createNPE() {
        return BindingSupportImpl.getInstance().nullElement("Null element not allowed.");
    }

    public boolean add(Object o) {
        if (o == null) throw createNPE();
        boolean result = super.add(o);
        if (result) makeDirty();
        return result;
    }

    public boolean remove(Object o) {
        boolean result = super.remove(o);
        if (result) {
            makeDirty();
        }
        return result;
    }

    public void clear() {
        super.clear();
        makeDirty();
    }

    public boolean retainAll(Collection c) {
        if (super.retainAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection c) {
        if (super.removeAll(c)) {
            makeDirty();
            return true;
        }
        return false;
    }

    public boolean addAll(Collection c) {
        if (super.addAll(c)) {
            makeDirty();
            return true;
        }
        return false;

    }

    public Iterator iterator() {
        return new SCOIterator(super.iterator(), stateManager, owner, fieldNo);
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
