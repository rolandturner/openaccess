
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
package com.versant.core.util;

import java.io.IOException;
import java.io.Serializable;

/**
 * Specialized HashMap mapping int to Object. This is a cut and paste of
 * java.util.HashMap with the key hardcoded as int and some non-required
 * functionality removed.
 */
public final class IntObjectHashMap implements Serializable {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    private transient Entry[] table;

    /**
     * The number of key-value mappings contained in this identity hash map.
     */
    private transient int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    private int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    private final float loadFactor;

    /**
     * Constructs an empty <tt>IntObjectHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @throws IllegalArgumentException if the initial capacity is negative
     *                                  or the load factor is nonpositive.
     */
    public IntObjectHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }

        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
        table = new Entry[capacity];
    }

    /**
     * Constructs an empty <tt>IntObjectHashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public IntObjectHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>IntObjectHashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public IntObjectHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * Returns a string representation of this map.  The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's <tt>entrySet</tt> view's iterator, enclosed in braces
     * (<tt>"{}"</tt>).  Adjacent mappings are separated by the characters
     * <tt>", "</tt> (comma and space).  Each key-value mapping is rendered as
     * the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value.  Keys and values are converted to strings as by
     * <tt>String.valueOf(Object)</tt>.<p>
     * <p/>
     * This implementation creates an empty string buffer, appends a left
     * brace, and iterates over the map's <tt>entrySet</tt> view, appending
     * the string representation of each <tt>map.entry</tt> in turn.  After
     * appending each entry except the last, the string <tt>", "</tt> is
     * appended.  Finally a right brace is appended.  A string is obtained
     * from the stringbuffer, and returned.
     *
     * @return a String representation of this map.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        for (int i = 0; i < table.length; i++) {
            Entry e = table[i];
            for (; e != null; e = e.next) {
                int key = e.key;
                Object value = e.getValue();
                buf.append(key + "=" + (value == this ? "(this Map)" : value));
            }
        }
        buf.append("}");
        return buf.toString();
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
     * that the map contains no mapping for the key; it is also possible that
     * the map explicitly maps the key to <tt>null</tt>. The
     * <tt>containsKey</tt> method may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <tt>null</tt> if the map contains no mapping for this key.
     */
    public Object get(int key) {
        int i = key & (table.length - 1);
        Entry e = table[i];
        while (true) {
            if (e == null) {
                return e;
            }
            if (key == e.key) {
                return e.value;
            }
            e = e.next;
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param key The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     */
    public boolean containsKey(int key) {
        int i = key & (table.length - 1);
        Entry e = table[i];
        while (e != null) {
            if (key == e.key) {
                return true;
            }
            e = e.next;
        }
        return false;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the IntObjectHashMap previously associated
     *         <tt>null</tt> with the specified key.
     */
    public Object put(int key, Object value) {
        int i = key & (table.length - 1);
        for (Entry e = table[i]; e != null; e = e.next) {
            if (key == e.key) {
                Object oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        addEntry(key, value, i);
        return null;
    }

    /**
     * This method is used instead of put by constructors and
     * pseudoconstructors (clone, readObject).  It does not resize the table,
     * check for comodification, etc.  It calls createEntry rather than
     * addEntry.
     */
    private void putForCreate(int key, Object value) {
        int i = key & (table.length - 1);

        /**
         * Look for preexisting entry for key.  This will never happen for
         * clone or deserialize.  It will only happen for construction if the
         * input Map is a sorted map whose ordering is inconsistent w/ equals.
         */
        for (Entry e = table[i]; e != null; e = e.next) {
            if (key == e.key) {
                e.value = value;
                return;
            }
        }
        createEntry(key, value, i);
    }

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     * <p/>
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *                    must be greater than current capacity unless current
     *                    capacity is MAXIMUM_CAPACITY (in which case value
     *                    is irrelevant).
     */
    private void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * loadFactor);
    }

    /**
     * Transfer all entries from current table to newTable.
     */
    private void transfer(Entry[] newTable) {
        Entry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry next = e.next;
                    int i = e.key & (newCapacity - 1);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    public Object remove(int key) {
        Entry e = removeEntryForKey(key);
        return e == null ? e : e.value;
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the IntObjectHashMap.  Returns null if the IntObjectHashMap contains no mapping
     * for this key.
     */
    private Entry removeEntryForKey(int key) {
        int i = key & (table.length - 1);
        Entry prev = table[i];
        Entry e = prev;

        while (e != null) {
            Entry next = e.next;
            if (key == e.key) {
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        Entry tab[] = table;
        for (int i = 0; i < tab.length; i++) {
            tab[i] = null;
        }
        size = 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(Object value) {
        Entry tab[] = table;
        for (int i = 0; i < tab.length; i++) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class Entry {

        final int key;
        Object value;
        Entry next;

        /**
         * Create new entry.
         */
        public Entry(int k, Object v, Entry n) {
            value = v;
            next = n;
            key = k;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object newValue) {
            Object oldValue = value;
            value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry)o;
            if (key == e.key) {
                if (value == e.value || (value != null && value.equals(e.value))) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return key ^ (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + getValue();
        }

    }

    /**
     * Add a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this
     * method to resize the table if appropriate.
     * <p/>
     * Subclass overrides this to alter the behavior of put method.
     */
    private void addEntry(int key, Object value, int bucketIndex) {
        table[bucketIndex] = new Entry(key, value, table[bucketIndex]);
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
    }

    /**
     * Like addEntry except that this version is used when creating entries
     * as part of Map construction or "pseudo-construction" (cloning,
     * deserialization).  This version needn't worry about resizing the table.
     * <p/>
     * Subclass overrides this to alter the behavior of IntObjectHashMap(Map),
     * clone, and readObject.
     */
    private void createEntry(int key, Object value, int bucketIndex) {
        table[bucketIndex] = new Entry(key, value, table[bucketIndex]);
        size++;
    }

    /**
     * Save the state of the <tt>IntObjectHashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the IntObjectHashMap (the length of the
     * bucket array) is emitted (int), followed  by the
     * <i>size</i> of the IntObjectHashMap (the number of key-value
     * mappings), followed by the key (Object) and value (Object)
     * for each key-value mapping represented by the IntObjectHashMap
     * The key-value mappings are emitted in the order that they
     * are returned by <tt>entrySet().iterator()</tt>.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        s.writeInt(table.length);

        // Write out size (number of Mappings)
        s.writeInt(size);

        // Write out keys and values (alternating)
        int c = 0;
        for (int i = 0; c < size && i < table.length; i++) {
            Entry e = table[i];
            for (; e != null; e = e.next, ++c) {
                s.writeInt(e.key);
                s.writeObject(e.getValue());
            }
        }
    }

    /**
     * Reconstitute the <tt>IntObjectHashMap</tt> instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        // Read in the threshold, loadfactor, and any hidden stuff
        s.defaultReadObject();

        // Read in number of buckets and allocate the bucket array;
        int numBuckets = s.readInt();
        table = new Entry[numBuckets];

        // Read in size (number of Mappings)
        int size = s.readInt();

        // Read the keys and values, and put the mappings in the IntObjectHashMap
        for (int i = 0; i < size; i++) {
            int key = s.readInt();
            Object value = s.readObject();
            putForCreate(key, value);
        }
    }

}
