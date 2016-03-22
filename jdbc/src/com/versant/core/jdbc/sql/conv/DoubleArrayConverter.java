
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
 * This converter converts double[] to and from SQL. It converts the double[]
 * to and from a double[] and delegates to a nested converter.
 * TODO This could be done much faster with java.nio buffers.
 * @keep-all
 */
public class DoubleArrayConverter extends TypeAsBytesConverterBase {

    public static class Factory extends TypeAsBytesConverterBase.Factory {

        protected JdbcConverter createConverter(JdbcConverter nested) {
            return new DoubleArrayConverter(nested);
        }

    }

    public DoubleArrayConverter(JdbcConverter nested) {
        super(nested);
    }

    /**
     * Convert a byte[] into an instance of our value class.
     */
    protected Object fromByteArray(byte[] buf) {
        int n = buf.length / 8;
        double[] a = new double[n];
        int i = 0, j = 0;
        for (; i < n; ) {
            a[i++] = Double.longBitsToDouble(
                      ((long)(buf[j++] & 0xFF) << 56)
                    + ((long)(buf[j++] & 0xFF) << 48)
                    + ((long)(buf[j++] & 0xFF) << 40)
                    + ((long)(buf[j++] & 0xFF) << 32)
                    + ((long)(buf[j++] & 0xFF) << 24)
                    + ((buf[j++] & 0xFF) << 16)
                    + ((buf[j++] & 0xFF) << 8)
                    + (buf[j++] & 0xFF));
        }
        return a;
    }

    /**
     * Convert an instance of our value class into a byte[].
     */
    protected byte[] toByteArray(Object value) {
        if (value == null) return null;
        double[] a = (double[])value;
        int n = a.length;
        byte[] buf = new byte[n * 8];
        int i = 0, j = 0;
        for (; i < n; ) {

            long x = Double.doubleToRawLongBits(a[i++]);


            buf[j++] = (byte)((x >>> 56) & 0xFF);
            buf[j++] = (byte)((x >>> 48) & 0xFF);
            buf[j++] = (byte)((x >>> 40) & 0xFF);
            buf[j++] = (byte)((x >>> 32) & 0xFF);
            buf[j++] = (byte)((x >>> 24) & 0xFF);
            buf[j++] = (byte)((x >>> 16) & 0xFF);
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
        return /*CHFC*/double[].class/*RIGHTPAR*/;
    }

}

