
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
package com.versant.core.jdbc.sql.conv;

import com.versant.core.jdbc.JdbcConverter;

/**
 * This converter converts char[] to and from SQL. It converts the char[]
 * to and from a byte[] and delegates to a nested converter.
 * TODO This could be done much faster with java.nio buffers.
 * @keep-all
 */
public class CharArrayConverter extends TypeAsBytesConverterBase {

    public static class Factory extends TypeAsBytesConverterBase.Factory {

        protected JdbcConverter createConverter(JdbcConverter nested) {
            return new CharArrayConverter(nested);
        }

    }

    public CharArrayConverter(JdbcConverter nested) {
        super(nested);
    }

    /**
     * Convert a byte[] into an instance of our value class.
     */
    protected Object fromByteArray(byte[] buf) {
        int n = buf.length / 2;
        char[] a = new char[n];
        int i = 0, j = 0;
        for (; i < n; ) {
            a[i++] = (char)(((buf[j++] & 0xFF) << 8) + (buf[j++] & 0xFF));
        }
        return a;
    }

    /**
     * Convert an instance of our value class into a byte[].
     */
    protected byte[] toByteArray(Object value) {
        if (value == null) return null;
        char[] a = (char[])value;
        int n = a.length;
        byte[] buf = new byte[n * 2];
        int i = 0, j = 0;
        for (; i < n; ) {
            char x = a[i++];
            buf[j++] = (byte)((x >>> 8) & 0xFF);
            buf[j++] = (byte)(x & 0xFF);
        }
        return buf;
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/char[].class/*RIGHTPAR*/;
    }

}

