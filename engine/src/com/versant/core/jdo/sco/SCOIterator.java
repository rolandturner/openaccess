
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
import java.util.Iterator;

import com.versant.core.jdo.VersantStateManager;

/**
 * @keep-all
 */
public class SCOIterator implements Iterator {

    private Iterator delegateIter;
    private PersistenceCapable owner;
    private VersantStateManager stateManager;
    private int fieldNo;

    public SCOIterator(Iterator delegateIter, VersantStateManager stateManager, PersistenceCapable owner, int fieldNo) {
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

    public void remove() {
        delegateIter.remove();
        makeDirty();
    }

    private void makeDirty() {
        if (stateManager != null && owner != null) {
            stateManager.makeDirty(owner, fieldNo);
        }
    }
}
