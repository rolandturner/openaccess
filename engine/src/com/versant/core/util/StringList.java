
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
import java.util.Iterator;

/**
 * A list of ints, doubles and Strings separated by delimeters. This class
 * builds a list that can be parsed with StringListParser.
 */
public final class StringList {

    private char delim;
    private StringBuffer buf;
    private boolean first = true;

    public StringList() {
        this(StringListParser.DEFAULT_DELIM, 40);
    }

    public StringList(int initialSize) {
        this(StringListParser.DEFAULT_DELIM, initialSize);
    }

    public StringList(char delim, int initialSize) {
        this.delim = delim;
        buf = new StringBuffer(initialSize);
    }

    /**
     * Append an int.
     */
    public void append(int x) {
        if (first) first = false;
        else buf.append(delim);
        buf.append(x);
    }

    /**
     * Append a boolean.
     */
    public void append(boolean x) {
        if (first) first = false;
        else buf.append(delim);
        buf.append(x ? 'Y' : 'N');
    }

    /**
     * Append a double.
     */
    public void append(double x) {
        if (first) first = false;
        else buf.append(delim);
        buf.append(x);
    }

    /**
     * Append a String without escaping any delims in the String. The
     * String may not contain any delim characters.
     */
    public void append(String s) {
        if (first) first = false;
        else buf.append(delim);
        buf.append(s);
    }

    /**
     * Append a Class (may be null).
     */
    public void append(Class c) {
        if (first) first = false;
        else buf.append(delim);
        if (c == null) buf.append('-');
        else buf.append(c.getName());
    }

    /**
     * Append a double quoted String escaping any embedded double quotes.
     * If the string is null then a single hyphen is appended.
     */
    public void appendQuoted(String s) {
        if (first) first = false;
        else buf.append(delim);
        if (s == null) {
            buf.append('-');
        } else {
            buf.append('"');
            int n = s.length();
            for (int i = 0; i < n; i++) {
                char c = s.charAt(i);
                if (c == '"') buf.append('"');
                buf.append(c);
            }
            buf.append('"');
        }
    }

    /**
     * Append a Map of properties.
     */
    public void appendProperties(Map props) {
        for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            append((String)e.getKey());
            appendQuoted((String)e.getValue());
        }
    }

    /**
     * Get the completed string.
     */
    public String toString() { return buf.toString(); }

    /**
     * Reset the list. This empties it.
     */
    public void reset() {
        buf.setLength(0);
        first = true;
    }

    /**
     * Get the number of characters in the buffer.
     */
    public int length() { return buf.length(); }

}
