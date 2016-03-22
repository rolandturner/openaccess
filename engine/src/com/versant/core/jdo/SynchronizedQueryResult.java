
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

import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * QueryResult implementation that synchronizes all methods except the
 * next and prev accessors on a lock object. Synchronization for the next and
 * prev accessors is provided inside {@link VersantQueryImp}.
 */
public final class SynchronizedQueryResult implements QueryResult {

    private final Object lock;
    private final QueryResult q;

    public SynchronizedQueryResult(Object lock, QueryResult q) {
        this.lock = lock;
        this.q = q;
    }

    public QueryResult getNext() {
        return q.getNext();
    }

    public void setNext(QueryResult next) {
        q.setNext(next);
    }

    public QueryResult getPrev() {
        return q.getPrev();
    }

    public void setPrev(QueryResult prev) {
        q.setPrev(prev);
    }

    public void close() {
        synchronized (lock) {
            q.close();
        }
    }

    public void setParams(Object[] params) {
        synchronized (lock) {
            q.setParams(params);
        }
    }

    public Iterator createInternalIterNoFlush() {
        synchronized (lock) {
            return q.createInternalIterNoFlush();
        }
    }

    public int size() {
        synchronized (lock) {
            return q.size();
        }
    }

    public void clear() {
        synchronized (lock) {
            q.clear();
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return q.isEmpty();
        }
    }

    public Object[] toArray() {
        synchronized (lock) {
            return q.toArray();
        }
    }

    public Object get(int index) {
        synchronized (lock) {
            return q.get(index);
        }
    }

    public Object remove(int index) {
        synchronized (lock) {
            return q.remove(index);
        }
    }

    public void add(int index, Object element) {
        synchronized (lock) {
            q.add(index, element);
        }
    }

    public int indexOf(Object o) {
        synchronized (lock) {
            return q.indexOf(o);
        }
    }

    public int lastIndexOf(Object o) {
        synchronized (lock) {
            return q.lastIndexOf(o);
        }
    }

    public boolean add(Object o) {
        synchronized (lock) {
            return q.add(o);
        }
    }

    public boolean contains(Object o) {
        synchronized (lock) {
            return q.contains(o);
        }
    }

    public boolean remove(Object o) {
        synchronized (lock) {
            return q.remove(o);
        }
    }

    public boolean addAll(int index, Collection c) {
        synchronized (lock) {
            return q.addAll(index, c);
        }
    }

    public boolean addAll(Collection c) {
        synchronized (lock) {
            return q.addAll(c);
        }
    }

    public boolean containsAll(Collection c) {
        synchronized (lock) {
            return q.containsAll(c);
        }
    }

    public boolean removeAll(Collection c) {
        synchronized (lock) {
            return q.removeAll(c);
        }
    }

    public boolean retainAll(Collection c) {
        synchronized (lock) {
            return q.retainAll(c);
        }
    }

    public Iterator iterator() {
        synchronized (lock) {
            return q.iterator();
        }
    }

    public List subList(int fromIndex, int toIndex) {
        synchronized (lock) {
            return q.subList(fromIndex, toIndex);
        }
    }

    public ListIterator listIterator() {
        synchronized (lock) {
            return q.listIterator();
        }
    }

    public ListIterator listIterator(int index) {
        synchronized (lock) {
            return q.listIterator(index);
        }
    }

    public Object set(int index, Object element) {
        synchronized (lock) {
            return q.set(index, element);
        }
    }

    public Object[] toArray(Object a[]) {
        synchronized (lock) {
            return q.toArray(a);
        }
    }

}

