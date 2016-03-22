
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
import java.util.ListIterator;

import com.versant.core.jdo.VersantStateManager;

/**
 * This is an ListIter that calls makeDirty on the sco list when a mutate method is called.
 *
 * @keep-all
 */
public class SCOListIterator implements ListIterator {

    private ListIterator delegateIter;
    private PersistenceCapable owner;
    private VersantStateManager stateManager;
    private int fieldNo;

    public SCOListIterator(ListIterator delegateIter, VersantStateManager stateManager, PersistenceCapable owner, int fieldNo) {
        this.delegateIter = delegateIter;
        this.stateManager = stateManager;
        this.owner = owner;
        this.fieldNo = fieldNo;
    }

    public boolean hasNext() {
        return delegateIter.hasNext();
    }

    public Object next() {
        return delegateIter.next();
    }

    public boolean hasPrevious() {
        return delegateIter.hasPrevious();
    }

    public Object previous() {
        return delegateIter.previous();
    }

    public int nextIndex() {
        return delegateIter.nextIndex();
    }

    public int previousIndex() {
        return delegateIter.previousIndex();
    }

    public void remove() {
        delegateIter.remove();
        makeDirty();
    }

    public void set(Object o) {
        delegateIter.set(o);
        makeDirty();
    }

    public void add(Object o) {
        delegateIter.add(o);
        makeDirty();
    }

    private void makeDirty() {
        if (stateManager != null && owner != null) {
            stateManager.makeDirty(owner, fieldNo);
        }
    }
}


