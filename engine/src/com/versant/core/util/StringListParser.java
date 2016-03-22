
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

import java.util.Map;

/**
 * This class is a simple lexical analyzer for a list of Strings, ints and
 * doubles separated by a delim character. It is much faster than
 * StringTokenizer for lists of numbers etc as it does not create a String
 * for each token.
 */
public final class StringListParser {

    public static final char DEFAULT_DELIM = ',';

    private String s;
    private int slen;
    private char delim;
    private int pos;

    public StringListParser(char delim) {
        this.delim = delim;
    }

    public StringListParser(String s, char delim) {
        this.delim = delim;
        setString(s);
    }

    public StringListParser(String s) {
        this(s, DEFAULT_DELIM);
    }

    public StringListParser() {
        this(DEFAULT_DELIM);
    }

    /**
     * Set the String we are parsing.
     */
    public void setString(String s) {
        this.s = s;
        pos = 0;
        slen = s.length();
    }

    /**
     * Get the String we are parsing.
     */
    public String getString() {
        return s;
    }

    /**
     * Get the next String from the string.
     *
     * @throws IllegalStateException if there are no more or invalid format
     */
    public String nextString() throws IllegalStateException {
        if (!hasNext()) {
            throw new IllegalStateException("Expected String at end: " + s);
        }
        int start = pos;
        for (; pos < slen;) {
            char c = s.charAt(pos++);
            if (c == delim) return s.substring(start, pos - 1);
        }
        return s.substring(start);
    }

    /**
     * Get the next double quoted String from the string.
     *
     * @throws IllegalStateException if there are no more or invalid format
     */
    public String nextQuotedString() throws IllegalStateException {
        if (!hasNext()) {
            throw new IllegalStateException(
                    "Expected quoted String at end: " + s);
        }
        if (s.charAt(pos) == '-') {
            if (++pos < slen) ++pos;  // skip the delim
            return null;
        }
        StringBuffer a = new StringBuffer();
        ++pos;  // skip the opening double quote
        for (; pos < slen;) {
            char c = s.charAt(pos++);
            if (c == '"') {
                if (pos >= slen) break;
                c = s.charAt(pos++);
                if (c != '"') break;
            }
            a.append(c);
        }
        return a.toString();
    }

    /**
     * Get the next Class from the string.
     *
     * @throws IllegalStateException if there are no more or invalid format
     */
    public Class nextClass(ClassLoader cl)
            throws IllegalStateException, ClassNotFoundException {
        if (!hasNext()) {
            throw new IllegalStateException("Expected class name at end: " + s);
        }
        if (s.charAt(pos) == '-') {
            if (++pos < slen) ++pos;  // skip the delim
            return null;
        }
        int start = pos;
        for (; pos < slen;) {
            char c = s.charAt(pos++);
            if (c == delim) break;
        }
        String name = s.substring(start, pos - 1);
        return BeanUtils.loadClass(name, true, cl);
    }

    /**
     * Get the next int from the string.
     *
     * @throws IllegalStateException if there are no more or invalid format
     */
    public int nextInt() throws IllegalStateException {
        if (!hasNext()) {
            throw new IllegalStateException("Expected int at end: " + s);
        }
        int ibuf = 0;
        boolean neg = false;
        if (pos < slen && s.charAt(pos) == '-') {
            neg = true;
            pos++;
        }
        for (; pos < slen;) {
            char c = s.charAt(pos++);
            if (c >= '0' && c <= '9') {
                ibuf = ibuf * 10 + (c - '0');
                //cat.debug("c = '" + c + "' ibuf " + ibuf);
            } else if (c == delim) {
                break;
            } else {
                throw new IllegalStateException(
                        "Expected int at pos " + (pos - 1) + ": " + s);
            }
        }
        return neg ? -ibuf : ibuf;
    }

    /**
     * Get the next boolean from the string.
     *
     * @throws IllegalStateException if there are no more or invalid format
     */
    public boolean nextBoolean() {
        if (!hasNext()) {
            throw new IllegalStateException("Expected boolean at end: " + s);
        }
        char c = s.charAt(pos++);
        if (pos < slen) pos++;  // skip the delim
        if (c == 'Y') {
            return true;
        } else if (c == 'N') return false;
        throw new IllegalStateException("Invalid boolean character '" + c +
                "' in " + s);
    }

    /**
     * Get the next double from the string.
     *
     * @throws IllegalStateException if there are no more or invalid format
     */
    public double nextDouble() throws IllegalStateException {
        if (!hasNext()) {
            throw new IllegalStateException("Expected double at end: " + s);
        }
        double dbuf = 0.0;
        boolean neg = false;
        if (pos < slen && s.charAt(pos) == '-') {
            neg = true;
            pos++;
        }
        for (; pos < slen;) {
            char c = s.charAt(pos++);
            if (c >= '0' && c <= '9') {
                dbuf = dbuf * 10.0 + (c - '0');
            } else if (c == delim) {
                return neg ? -dbuf : dbuf;
            } else if (c == '.') {
                break;
            } else {
                throw new IllegalStateException(
                        "Expected double at pos " + (pos - 1) + ": " + s);
            }
        }
        double m = 10.0;
        double f = 0.0;
        for (; pos < slen;) {
            char c = s.charAt(pos++);
            if (c >= '0' && c <= '9') {
                f = f * 10.0 + (c - '0');
                m = m * 10.0;
            } else if (c == delim) {
                break;
            } else {
                throw new IllegalStateException(
                        "Expected double at pos " + (pos - 1) + ": " + s);
            }
        }
        dbuf = dbuf + f / m;
        return neg ? -dbuf : dbuf;
    }

    /**
     * Read the rest of the String as key,value pairs.
     */
    public void nextProperties(Map map) {
        for (; hasNext();) {
            String key = nextString();
            String value = nextQuotedString();
            map.put(key, value);
        }
    }

    /**
     * Are there more Strings, ints or doubles?
     */
    public final boolean hasNext() {
        return pos < slen;
    }

    public void nextProperties(IntObjectHashMap map) {
        for (; hasNext();) {
            int key = nextInt();
            String value = nextQuotedString();
            map.put(key, value);
        }
    }
}
