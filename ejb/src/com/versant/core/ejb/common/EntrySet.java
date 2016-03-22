
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
package com.versant.core.ejb.common;

import com.versant.core.common.BindingSupportImpl;

import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * This is a Hashed based collection that contain Entry elements.
 * Equality is key based.
 */
public class EntrySet {
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
    protected int modCount;
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
        m_keyTable = new Entry[capacity];
        initCapacity = capacity;
    }

    /**
     * Add this entry to the set.
     * @param ne
     */
    public Entry addEntry(Entry ne) {
        int i = indexFor(ne.hash, m_keyTable.length);

        for (Entry e = m_keyTable[i]; e != null; e = e.next) {
            if (e.hash == ne.hash && eq(ne.key, e.key)) {
                //same instance
                if (ne == e) return null;
                else throw new IllegalArgumentException("Entry with equal key is already in the set");
            }
        }
        modCount++;
        addEntry(ne, i);
        return ne;
    }

    public boolean add(Object o) {
        if (!contains(o)) {
            addEntry(createEntry(o));
            return true;
        }
        return false;
    }

    /**
     * If the instance is present then return the entry, else add it and return the
     * new entry.
     * @param o
     */
    public Entry addAndReturnEntry(Object o) {
        Entry e = get(o);
        if (e == null) {
            return addEntry(createEntry(o));
        }
        return e;
    }

    /**
     * This is an internal call to create an Entry.
     * @param key
     */
    public Entry createEntry(Object key) {
        return new Entry(key);
    }

    private void addEntry(Entry e, int index) {
        final Entry cHead = m_keyTable[index];
        if (cHead != null) cHead.prev = e;
        e.next = cHead;
        m_keyTable[index] = e;

        if (size++ >= threshold) {
            resize(2 * m_keyTable.length);
        }
        entryAdded(e);
    }

    public void remove(Entry toRemove) {
        final int i = indexFor(toRemove.hash, m_keyTable.length);
        for (Entry e = m_keyTable[i]; e != null; e = e.next) {
            if (e == toRemove) {
                removeFromBucket(e, i);
            }
        }
    }

    private void removeFromBucket(Entry e, int i) {
        /**
         * If this is the currect head of the bucket then set the head to its next.
         */
        if (e.prev == null) {
            m_keyTable[i] = e.next;
        } else if (e.next == null) {
            e.prev.next = null;
        } else {
            e.prev.next = e.next;
            e.next.prev = e.prev;
        }
        e.next = null;
        e.prev = null;
    }

    public boolean remove(Object key) {
        final int hash = key.hashCode();
        for (Entry e = m_keyTable[indexFor(hash, m_keyTable.length)]; e != null; e = e.next) {
            if (e.hash == hash && eq(key, e.key)) {
                removeFromBucket(e, indexFor(hash, m_keyTable.length));
                return true;
            }
        }
        return false;
    }

    /**
     * Callback when a entry was physically added
     */
    protected void entryAdded(Entry e) {
    }

    public boolean contains(Object key) {
        return (get(key) != null);
    }

    public Entry get(Object key) {
        final int hash = key.hashCode();
        for (Entry e = m_keyTable[indexFor(hash, m_keyTable.length)]; e != null; e = e.next) {
            if (e.hash == hash && eq(key, e.key)) {
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
    static boolean eq(Object x, Object y) {
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
        final Object key;
        private final int hash;
        private Entry next;
        public Entry prev;

        public Entry(Object key) {
            this.key = key;
            this.hash = key.hashCode();
        }

        public Object getKey() {
            return key;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            Entry e = (Entry)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || k1.equals(k2)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return hash;
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

}
