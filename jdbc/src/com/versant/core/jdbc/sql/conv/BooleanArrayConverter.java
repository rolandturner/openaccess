
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
 * This converter converts boolean[] to and from SQL. It converts the boolean[]
 * to and from a byte[] and delegates to a nested converter.
 * TODO This could be done much faster with java.nio buffers.
 * @keep-all
 */
public class BooleanArrayConverter extends TypeAsBytesConverterBase {

    public static class Factory extends TypeAsBytesConverterBase.Factory {

        protected JdbcConverter createConverter(JdbcConverter nested) {
            return new BooleanArrayConverter(nested);
        }

    }

    public BooleanArrayConverter(JdbcConverter nested) {
        super(nested);
    }

    /**
     * Convert a byte[] into an instance of our value class.
     */
    protected Object fromByteArray(byte[] buf) {
        int n = buf.length;
        boolean[] a = new boolean[n];
        for (int i = 0; i < n; i++) {
            a[i] = buf[i] != 0;
        }
        return a;
    }

    /**
     * Convert an instance of our value class into a byte[].
     */
    protected byte[] toByteArray(Object value) {
        if (value == null) return null;
        boolean[] a = (boolean[])value;
        int n = a.length;
        byte[] buf = new byte[n];
        for (int i = 0; i < n; i++) {
            buf[i] = a[i] ? (byte)1 : (byte)0;
        }
        return buf;
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/boolean[].class/*RIGHTPAR*/;
    }

}

