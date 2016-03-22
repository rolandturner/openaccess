
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
package com.versant.core.jdo;

import java.util.ListIterator;

import com.versant.core.common.BindingSupportImpl;

public class IteratorWrapper implements JDOListIterator {
    private ListIterator iterator;
    private boolean closed;

    public IteratorWrapper(ListIterator iterator) {
        this.iterator = iterator;
    }

    public boolean hasNext() {
        if (closed) return false;
        return iterator.hasNext();
    }

    public Object next() {
        if (closed) {
            throw BindingSupportImpl.getInstance().noSuchElement("");
        }
        return iterator.next();
    }

    public void remove() {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public void add(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public void set(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public int previousIndex() {
        if (closed) {
            throw BindingSupportImpl.getInstance().noSuchElement("");
        }
        return iterator.previousIndex();
    }

    public int nextIndex() {
        if (closed) {
            throw BindingSupportImpl.getInstance().noSuchElement("");
        }
        return iterator.nextIndex();
    }

    public Object previous() {
        if (closed) {
            throw BindingSupportImpl.getInstance().noSuchElement("");
        }
        return iterator.previous();
    }

    public boolean hasPrevious() {
        if (closed) return false;
        return iterator.hasPrevious();
    }

    public void close() {
        this.closed = true;
        iterator = null;
    }
}
