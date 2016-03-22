
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
package com.versant.core.common;

import com.versant.core.jdo.JDOListIterator;

import java.util.*;

/**
 * This is an non-mutable list implementation used for Query results.
 */
public class PCList implements List {
    /** The underlying array used for storing the data. */
    private final Object[] m_baseArray;

    private final int startIndex;
    private final int endIndex;

    private final int calculatedSize;

    public PCList(Object[] m_baseArray) {
        this.m_baseArray = m_baseArray;
        startIndex = 0;
        endIndex = m_baseArray.length;
        calculatedSize = m_baseArray.length;
    }

    public PCList(Object[] m_baseArray, int startIndex, int endIndex) {
        if (startIndex > endIndex) throw BindingSupportImpl.getInstance().indexOutOfBounds("The startIndex is greater than end index");
        if (m_baseArray.length < endIndex) throw BindingSupportImpl.getInstance().indexOutOfBounds("The endIndex to big");
        if (startIndex < 0) throw BindingSupportImpl.getInstance().indexOutOfBounds("The startIndex is negative");

        this.m_baseArray = m_baseArray;
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        calculatedSize = endIndex - startIndex;
    }

    public final boolean add(Object value) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public void add(int index, Object value) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public final Object get(int index) {
        if (index < calculatedSize) {
            return m_baseArray[index + startIndex];
        } else {
            throw BindingSupportImpl.getInstance().indexOutOfBounds("Invalid index value");
        }
    }

    public final Object set(int index, Object value) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public final Iterator iterator() {
        return new InternalListIter(0);
    }

    public Object[] toArray() {
        final Object[] values = m_baseArray;
        int size = size();
        Object[] a = new Object[size];

        for (int i = startIndex; i < size; i++) {
            a[i] = values[i];
        }

        return a;
    }

    public Object[] toArray(Object a[]) {
        final Object[] values = m_baseArray;
        int size = size();

        if (size > a.length) {
            a = (Object[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }

        for (int i = startIndex; i < size; i++) {
            a[i] = values[i];
        }

        if (a.length > size) a[size] = null;
        return a;
    }

    public boolean contains(Object o) {
        final Object[] values = m_baseArray;
        final int n = size();
        for (int i = startIndex; i < n; i++) {
            if (values[i].equals(o)) return true;
        }
        return false;
    }

    public int indexOf(Object o) {
        final Object[] values = m_baseArray;
        final int n = size();
        for (int i = startIndex; i < n; i++) {
            if (values[i].equals(o)) return i;
        }
        return -1;
    }

    public ListIterator listIterator(int index) {
        return new InternalListIter(index + startIndex);
    }

    public boolean retainAll(Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public boolean containsAll(Collection c) {
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            if (!contains(iterator.next())) return false;
        }
        return true;
    }

    public boolean addAll(int index, Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public boolean addAll(Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public int lastIndexOf(Object o) {
        //we don't contain null's
        if (o == null) return -1;

        final Object[] values = m_baseArray;
        for (int i = (endIndex - 1); i >= startIndex; i--) {
            if (values[i].equals(o)) return i;
        }
        return -1;
    }

    public boolean isEmpty() {
        return (size() == 0);
    }

    public boolean removeAll(Collection c) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public List subList(int fromIndex, int toIndex) {
        return new PCList(m_baseArray, startIndex + fromIndex, startIndex + toIndex);
    }

    public int hashCode() {
        int hashCode = 1;

        final Object[] values = m_baseArray;
        final int n = size();
        for (int i = startIndex; i < n; i++) {
            hashCode = 31 * hashCode + values[i].hashCode();
        }
        return hashCode;
    }

    public ListIterator listIterator() {
        return new InternalListIter(startIndex);
    }

    public void clear() {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public int size() {
        return calculatedSize;
    }

    public Object remove(int index) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public boolean remove(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation("");
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof List))
            return false;

        ListIterator e1 = listIterator();
        ListIterator e2 = ((List) o).listIterator();
        while(e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    private class InternalListIter implements ListIterator, JDOListIterator {
        private int nextIndex;
        private boolean closed;

        public InternalListIter(int nextIndex) {
            this.nextIndex = nextIndex;
        }

        public boolean hasNext() {
            if (closed) return false;
            return nextIndex < calculatedSize;
        }

        public void add(Object o) {
            throw BindingSupportImpl.getInstance().unsupportedOperation("");
        }

        public Object next() {
            if (closed) {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
            if (nextIndex >= calculatedSize) {
                throw BindingSupportImpl.getInstance().noSuchElement("Iterated past end of Collection with size '"
                        + calculatedSize + "'");
            }
            try {
                return m_baseArray[nextIndex++];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw BindingSupportImpl.getInstance().noSuchElement("Iterated past end of Collection with size '"
                        + calculatedSize + "'");
            }
        }

        public void remove() {
            throw BindingSupportImpl.getInstance().unsupportedOperation("");
        }

        public int previousIndex() {
            if (closed) {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
            return nextIndex - 1;
        }

        public void set(Object o) {
            throw BindingSupportImpl.getInstance().unsupportedOperation("");
        }

        public Object previous() {
            if (closed) {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
            return m_baseArray[nextIndex-- - 1];
        }

        public boolean hasPrevious() {
            if (closed) return false;
            return nextIndex > 0;
        }

        public int nextIndex() {
            if (closed) {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
            return nextIndex;
        }

        public void close() {
            closed = true;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");

        Iterator i = iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Object o = i.next();
            buf.append(o == this ? "(this Collection)" : String.valueOf(o));
            hasNext = i.hasNext();
            if (hasNext)
                buf.append(", ");
        }

        buf.append("]");
        return buf.toString();
    }
}

