
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

import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * This is a Hashed based collection that contain Entry elements.
 */
public final class EntrySet {
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * The load factor used when none specified in constructor.
     **/
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * The load factor for the hash table.
     */
    final float loadFactor;
    /**
     * The next size value at which to resize (capacity * load factor).
     */
    int threshold;
    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Array of value table slots.
     */
    private Entry[] m_keyTable;

    /**
     * The number of key-value mappings contained in this identity hash map.
     */
    transient int size;
    private int modCount;
    private final int initCapacity;

    public EntrySet() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        m_keyTable = new Entry[DEFAULT_INITIAL_CAPACITY];
        initCapacity = DEFAULT_INITIAL_CAPACITY;
    }

    public EntrySet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public EntrySet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
        initCapacity = capacity;
        m_keyTable = new Entry[capacity];
    }

    public Object add(OID key, State value, boolean direct) {
        int hash = key.hashCode();
        int i = indexFor(hash, m_keyTable.length);

        for (Entry e = m_keyTable[i]; e != null; e = e.next) {
            if (e.hash == hash && eq(key, e.key)) {
                Object oldValue = e.value;
                e.value = value;
                e.direct = direct;
                return oldValue;
            }
        }
        modCount++;
        addEntry(hash, key, value, direct, i);
        return null;
    }

//    public Entry add(Entry ne) {
//        int i = indexFor(ne.hash, m_keyTable.length);
//
//        for (Entry e = m_keyTable[i]; e != null; e = e.next) {
//            if (e.hash == ne.hash && eq(ne.key, e.key)) {
//                //same instance
//                if (ne == e) return null;
//
//            }
//        }
//        modCount++;
//        addEntry(hash, key, value, direct, i);
//        return null;
//    }

    private void addEntry(int hash, OID key, State value, boolean direct, int bucketIndex) {
        m_keyTable[bucketIndex] = new Entry(hash, key, value, direct, m_keyTable[bucketIndex]);
        if (size++ >= threshold) {
            resize(2 * m_keyTable.length);
        }
    }

    public boolean contains(OID oid) {
        return (get(oid) != null);
    }

    public Entry get(OID oid) {
//        oid = oid.getAvailableOID();
        final int hash = oid.hashCode();
        for (Entry e = m_keyTable[indexFor(hash, m_keyTable.length)]; e != null; e = e.next) {
            if (e.hash == hash && eq(oid, e.key)) {
                return e;
            }
        }
        return null;
    }

    public Iterator iterator() {
        return new EntryIterator();
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    public Iterator newKeyIterator()   {
        return new KeyIterator();
    }

    public Iterator newValueIterator()   {
        return new ValueIterator();
    }

    public Iterator newEntryIterator()   {
        return new EntryIterator();
    }

    void resize(int newCapacity) {
        Entry[] oldTable = m_keyTable;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        m_keyTable = newTable;
        threshold = (int)(newCapacity * loadFactor);
    }

    /**
     * Transfer all entries from current table to newTable.
     */
    void transfer(Entry[] newTable) {
        Entry[] src = m_keyTable;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    /**
     * Check for equality of non-null reference x and possibly-null y.
     */
    static boolean eq(OID x, OID y) {
        return x == y || x.equals(y);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length) {
        return h & (length-1);
    }

    /**
     * How many keys are in the cache?
     */
    public int size() {
        return size;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        modCount++;
        Entry[] tab = m_keyTable;
        for (int i = 0; i < tab.length; i++) {
            tab[i] = null;
        }
        threshold = (int)(initCapacity * loadFactor);
        size = 0;
    }

    public static class Entry {
        final OID key;
        State value;
        final int hash;
        Entry next;
        boolean direct;

        /**
         * Create new entry.
         */
        Entry(int h, OID k, State v, boolean d, Entry n) {
            value = v;
            next = n;
            key = k;
            hash = h;
            this.direct = d;
        }

        public void setValue(Object value) {
            this.value = (State) value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            Entry e = (Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        public int hashCode() {
            return key.hashCode();
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }

        public void setDirect(boolean b) {
            direct = true;
        }

        public boolean isDirect() {
            return direct;
        }
    }

    class Iter implements Iterator {
        /**
         * The amount that we have iterated over.
         */
        private int iterCount;

        private Entry lastEntry;
        private int currentIndex;

        public Iter() {
        }

        public void remove() {
            throw BindingSupportImpl.getInstance().invalidOperation("Remove not allowed");
        }

        public boolean hasNext() {
            return (iterCount < size);
        }

        private Entry getFirstHeadFrom(int index) {
            for(int i = index; i < size; i++) {
                if (m_keyTable[i] != null) {
                    return m_keyTable[i];
                }
            }
            return null;
        }

        public Object next() {
            if (hasNext()) {
                if (lastEntry != null) {
                    lastEntry = lastEntry.next;
                    if (lastEntry == null) {
                        lastEntry = getFirstHeadFrom(currentIndex++);
                    }
                } else {
                    lastEntry = getFirstHeadFrom(currentIndex++);
                }
                if (lastEntry == null) {
                    throw BindingSupportImpl.getInstance().noSuchElement("");
                }
                iterCount++;
                return lastEntry;
            } else {
                throw BindingSupportImpl.getInstance().noSuchElement("");
            }
        }
    }

    private abstract class HashIterator implements Iterator {
        Entry next;                  // next entry to return
        int expectedModCount;        // For fast-fail
        int index;                   // current slot
        Entry current;               // current entry

        HashIterator() {
            expectedModCount = modCount;
            Entry[] t = m_keyTable;
            int i = t.length;
            Entry n = null;
            if (size != 0) { // advance to first entry
                while (i > 0 && (n = t[--i]) == null);
            }
            next = n;
            index = i;
        }

        public boolean hasNext() {
            return next != null;
        }

        Entry nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry e = next;
            if (e == null)
                throw new NoSuchElementException();

            Entry n = e.next;
            Entry[] t = m_keyTable;
            int i = index;
            while (n == null && i > 0) {
                n = t[--i];
            }
            index = i;
            next = n;
            return current = e;
        }

        public void remove() {
        }
    }

    private class EntryIterator extends HashIterator {
        public Object next() {
            return nextEntry();
        }
    }

    private class KeyIterator extends HashIterator {
        public Object next() {
            return nextEntry().getKey();
        }
    }

    private class ValueIterator extends HashIterator {
        public Object next() {
            return nextEntry().value;
        }
    }

}
