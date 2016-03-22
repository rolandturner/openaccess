
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

import com.versant.core.common.OID;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.VersantPMProxy;

/**
 * Base class for QueryResult implementations that throws an exception for
 * all mutating methods. It also manages the next and prev pointers. All
 * methods from the interface have abstract methods here to avoid problems
 * with the IBM VMs.
 * 
 * @see QueryResult
 * @see ForwardQueryResult
 * @see RandomAccessQueryResult
 * @see MemoryQueryResult
 */
public abstract class QueryResultBase implements QueryResult {

    private QueryResult next;
    private QueryResult prev;

    public final QueryResult getNext() {
        return next;
    }

    public final void setNext(QueryResult next) {
        this.next = next;
    }

    public final QueryResult getPrev() {
        return prev;
    }

    public final void setPrev(QueryResult prev) {
        this.prev = prev;
    }

    public abstract void close();

    public abstract void setParams(Object[] params);

    public abstract Iterator createInternalIterNoFlush();

    public boolean add(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public boolean remove(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public boolean addAll(Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public boolean addAll(int index, Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public boolean removeAll(Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public boolean retainAll(Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public void clear() {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public Object set(int index, Object element) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public void add(int index, Object element) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public Object remove(int index) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("Modification not allowed");
    }

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract boolean contains(Object o);

    public abstract Iterator iterator();

    public abstract Object[] toArray();

    public abstract Object[] toArray(Object a[]);

    public abstract boolean containsAll(Collection c);

    public abstract Object get(int index);

    public abstract int indexOf(Object o);

    public abstract int lastIndexOf(Object o);

    public abstract ListIterator listIterator();

    public abstract ListIterator listIterator(int index);

    public abstract List subList(int fromIndex, int toIndex);

    public static Object resolveRow(Object row, VersantPMProxy pm) {
        if (row == null) return null;
        if (row instanceof OID) {
            return pm.getObjectById(row, false);
        } else if (row instanceof Object[]) {
            Object[] data = (Object[])row;
            for (int i = 0; i < data.length; i++) {
                Object o = data[i];
                if (o instanceof OID) {
                    data[i] = pm.getObjectById(o, false);
                }
            }
        }
        return row;
    }
}
