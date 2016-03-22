
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
package com.versant.core.vds.util;

/**
 * Represents immutable datastore identity
 * It is important that Loid.equals() method uses equality by reference
 * as it will be used as a key to WeakHashMap.
 */
public final class Loid {

    public static final long NULL = 0L;
    private long _value;

    public Loid(long value) {
        _value = value;
    }

    public boolean isNull() {
        return (_value == 0);
    }

    /**
     * Constructs loid
     *
     * @param loid the String representation of a LOID
     */
    public Loid(String loid) {
        this(Loid.asValue(loid));
    }

    /**
     * Gets database identifier
     *
     * @return (int) database identifier
     */
    public int getDatabaseId() {
        return (int)(_value >> 48);
    }

    /**
     * Gets object identifier part 1
     *
     * @return (int) objectId1
     */
    public int getObjectId1() {
        return (int)(_value >> 32) & 0xFFFF;
    }

    /**
     * Gets object identifier part 2
     *
     * @return (int) objectId2
     */
    public long getObjectId2() {
        return (long)((_value << 32) >> 32);
    }

    public static String asStringKeiron(final long loid) {
        if (loid == 0) {
            return "0.0.0";
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append((loid >> 48) & 0xFFFF);
        buffer.append('.');
        buffer.append(((loid << 16) >> 48) & 0xFFFF);
        buffer.append('.');
        buffer.append(((loid << 32) >> 32) & 0xFFFFFFFFL);

        return buffer.toString();
    }

    public static String asString(final long loid) {
        if (loid == 0) {
            return "0.0.0";
        }

        // max length is 23 characters 32767.65535.4294967295
        char[] s = new char[23];
        int pos = getChars(loid & 0xFFFFFFFFl, s, 23);
        s[--pos] = '.';
        pos = getChars((int)(loid >> 32) & 0xFFFF, s, pos);
        s[--pos] = '.';
        pos = getChars((int)(loid >> 48), s, pos);
        return new String(s, pos, 23 - pos);
    }

    /**
     * Hacked from java.lang.Long.
     */
    private static int getChars(long i, char[] buf, int charPos) {
        long q;
        int r;

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = digitOnes[r];
            buf[--charPos] = digitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int)i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = digitOnes[r];
            buf[--charPos] = digitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (; ;) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf[--charPos] = digits[r];
            i2 = q2;
            if (i2 == 0) break;
        }
        return charPos;
    }

    /**
     * Hacked from java.lang.Integer. Only works for 0 <= i <= 65535.
     */
    private static int getChars(int i, char[] buf, int charPos) {
        int q, r;
        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (; ;) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) break;
        }
        return charPos;
    }

    final static char[] digits = {
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z'
    };

    final static char[] digitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    final static char[] digitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    /**
     * Format LOIDs as a space separated list. If there are more than max
     * loids then and max > 0 then ... is appended instead of the extras.
     */
    public static String asString(long[] loids, int max) {
        if (loids == null) return "<null>";
        StringBuffer s = new StringBuffer();
        int n = loids.length;
        if (n > 0) {
            s.append(asString(loids[0]));
            if (max > 0 && n > max) n = max;
            for (int i = 1; i < n; i++) {
                s.append(' ');
                s.append(asString(loids[i]));
            }
            if (max > 0 && max < loids.length) s.append(" ...");
        }
        return s.toString();
    }

    public String toString() {
        return Loid.asString(_value);
    }

    public boolean equals(Object obj) {
        return obj instanceof Loid && _value == ((Loid)obj)._value;
    }

    public static long asValue(String s) {
        try {
            int pos = 0;
            int max = s.length();
            int dbId, objId1;
            long objId2;
            char c;

            // max 5 digits for dbId
            dbId = s.charAt(pos++) - '0';
            if (dbId < 0 || dbId > 9) throw invalidLoid(s);
            for (; ;) {
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                dbId = dbId * 10 + (c - '0');
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                dbId = dbId * 10 + (c - '0');
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                dbId = dbId * 10 + (c - '0');
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                dbId = dbId * 10 + (c - '0');
                c = s.charAt(pos++);
                if (dbId > 0x7FFF || c != '.') throw invalidLoid(s);
                break;
            }

            // max 5 digits for objId1
            objId1 = s.charAt(pos++) - '0';
            if (objId1 < 0 || objId1 > 9) throw invalidLoid(s);
            for (; ;) {
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId1 = objId1 * 10 + (c - '0');
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId1 = objId1 * 10 + (c - '0');
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId1 = objId1 * 10 + (c - '0');
                c = s.charAt(pos++);
                if (c == '.') break;
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId1 = objId1 * 10 + (c - '0');
                c = s.charAt(pos++);
                if (dbId > 0xFFFF || c != '.') throw invalidLoid(s);
                break;
            }

            // max 10 digits for objId2
            c = s.charAt(pos++);
            if (c < '0' || c > '9') throw invalidLoid(s);
            objId2 = c - '0';
            for (; ;) {
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (pos == max) break;
                c = s.charAt(pos++);
                if (c < '0' || c > '9') throw invalidLoid(s);
                objId2 = objId2 * 10 + (c - '0');
                if (dbId > 0xFFFFFFFFl || pos != max) throw invalidLoid(s);
                break;
            }

            return (((long)dbId) << 48) + (((long)objId1) << 32) + objId2;
        } catch (StringIndexOutOfBoundsException e) {
            throw invalidLoid(s);
        }
    }

    private static IllegalArgumentException invalidLoid(String s) {
        return new IllegalArgumentException("Invalid LOID: '" + s + "'");
    }

    public int hashCode() {
        return Loid.hashCode(_value);
    }

    public static final int hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }

    public long value() {
        return _value;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            try {
                Loid loid = new Loid(Long.parseLong(args[i]));
                System.err.println(loid);
            } finally {}
        }
    }
}
