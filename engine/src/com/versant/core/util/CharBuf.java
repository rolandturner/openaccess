
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

/**
 * Fast StringBuffer replacement that allows direct access to the underlying
 * char[]. This is based com.sosnoski.util.array.CharArray from
 * Sosnoski Software Solutions, Inc.
 */
public final class CharBuf {

    private char[] buf;
    private int size;

    public CharBuf() {
        this(64);
    }

    public CharBuf(int capacity) {
        buf = new char[capacity];
    }

    public CharBuf(String s) {
        this(s.length());
        append(s);
    }

    public CharBuf(CharBuf s) {
        this(s.size);
        size = s.size;
        System.arraycopy(s.buf, 0, buf, 0, size);
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    private void ensureCapacity(int len) {
        if (size + len > buf.length) {
            int n = buf.length * 3 / 2 + 1;
            if (size + len > n) {
                n = size + len;
            }
            char[] a = new char[n];
            System.arraycopy(buf, 0, a, 0, size);
            buf = a;
        }
    }

    /**
     * Append ch and return its index.
     */
    public int append(char ch) {
		ensureCapacity(size + 1);
		int start = size;
        buf[size++] = ch;
		return start;
	}

    /**
     * Append i and return its index.
     */
    public int append(int i) {
        return append(Integer.toString(i));
    }

    /**
     * Append s and return its index.
     */
    public int append(String s) {
        return append(s.toCharArray());
	}

    /**
     * Append a and return its index.
     */
    public int append(char[] a) {
        int n = a.length;
        ensureCapacity(size + n);
		int start = size;
        System.arraycopy(a, 0, buf, size, n);
        size += n;
		return start;
	}

    /**
     * Replace characters from i onwards with supplied characters. This does
     * not do any error checking or make any attempt to expand the buffer
     * for performance reasons.
     */
    public void replace(int i, char[] text) {
        System.arraycopy(text, 0, buf, i, text.length);
    }

    /**
     * Replace characters from first to last - 1 with c. This does
     * not do any error checking for performance reasons.
     */
    public void replace(int first, int last, char c) {
        for (int i = first; i < last; i++) {
            buf[i] = c;
        }
    }

    public String toString() {
        return new String(buf, 0, size);
    }

    /**
     * Constructs and returns a simple array containing the same data as held
     * in a portion of this growable array.
     */
    public char[] toArray(int offset, int length) {
        char[] a = new char[length];
        System.arraycopy(buf, offset, a, 0, length);
        return a;
    }

    public void setSize(int sz) {
        size = sz;
    }

    /**
     * Construct a <code>String</code> from a portion of the character sequence
     * present.
     */
    public String toString(int offset, int length) {
        return new String(buf, offset, length);
    }

    /**
     * Insert the characters from a <code>char[]</code> into the array.
     */
    public void insert(int offset, char[] text) {
        adjust(offset, offset, text.length);
        System.arraycopy(text, 0, buf, offset, text.length);
    }

    /**
     * Insert the characters from a <code>String</code> into the array.
     */
    public void insert(int offset, String text) {
        adjust(offset, offset, text.length());
        text.getChars(0, text.length(), buf, offset);
    }

    /**
     * Replace a character range in the array with the characters from a
     * <code>String</code>.
     */
    public void replace(int from, int to, String text) {
        adjust(from, to, text.length());
        text.getChars(0, text.length(), buf, from);
    }

    /**
     * Replace a character range in the array with the characters from a
     * <code>char[]</code>.
     */
    public void replace(int from, int to, char[] text) {
        adjust(from, to, text.length);
        System.arraycopy(text, 0, buf, from, text.length);
    }

    /**
     * Adjust the characters in the array to make room for an insertion or
     * replacement. Depending on the relative sizes of the range being
     * replaced and the range being inserted, this may move characters past
     * the start of the replacement range up or down in the array.
     *
     * @param from index number of first character to be replaced
     * @param to index number past last character to be replaced
     * @param length length of character range being inserted
     */
    protected void adjust(int from, int to, int length) {
        if (from >= 0 && to < size && from <= to) {
            int change = from + length - to;
            if (change > 0) {
                ensureCapacity(size + change);
            }
            if (to < size){
                System.arraycopy(buf, to, buf, to + change, size - to);
                size += change;
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("Invalid remove range");
        }
    }

    /**
     * Set the value at an index position in the array.
     */
    public void set(int index, char value) {
        buf[index] = value;
    }

    /**
     * Remove a range of value from the array. The index positions for values
     * above the range removed are decreased by the number of values removed.
     *
     * @param from index number of first value to be removed
     * @param to index number past last value to be removed
     */
    public void remove(int from, int to) {
        if (from >= 0 && to <= size && from <= to) {
            if (to < size){
                int change = from - to;
                System.arraycopy(buf, to, buf, from, size - to);
                size += change;
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("Invalid remove range");
        }
    }

}
