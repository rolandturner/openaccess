
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
package com.versant.core.jdo.junit.test3.model.attachdetach;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class SimpleFields extends SimpleFieldsBase implements Serializable {

    private boolean[] booleansField = new boolean[0];
    private char[] charsField = new char[0];
    private byte[] bytesField = new byte[0];
    private short[] shortsField = new short[0];
    private int[] intsField = new int[0];
    private long[] longsField = new long[0];
    private float[] floatsField = new float[0];
    private double[] doublesField = new double[0];

    public SimpleFields(int id) {
        super(id);
    }

    public SimpleFields(int id, int intField, String stringField,
            int[] intsField) {
        super(id);
        this.intField = intField;
        this.stringField = stringField;
        this.intsField = intsField;
    }

    public boolean[] getBooleansField() {
        return booleansField;
    }

    public void setBooleansField(boolean[] booleansField) {
        this.booleansField = booleansField;
    }

    public char[] getCharsField() {
        return charsField;
    }

    public void setCharsField(char[] charsField) {
        this.charsField = charsField;
    }

    public byte[] getBytesField() {
        return bytesField;
    }

    public void setBytesField(byte[] bytesField) {
        this.bytesField = bytesField;
    }

    public short[] getShortsField() {
        return shortsField;
    }

    public void setShortsField(short[] shortsField) {
        this.shortsField = shortsField;
    }

    public int[] getIntsField() {
        return intsField;
    }

    public void setIntsField(int[] intsField) {
        this.intsField = intsField;
    }

    public long[] getLongsField() {
        return longsField;
    }

    public void setLongsField(long[] longsField) {
        this.longsField = longsField;
    }

    public float[] getFloatsField() {
        return floatsField;
    }

    public void setFloatsField(float[] floatsField) {
        this.floatsField = floatsField;
    }

    public double[] getDoublesField() {
        return doublesField;
    }

    public void setDoublesField(double[] doublesField) {
        this.doublesField = doublesField;
    }

    public Date getDateField() {
        return dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("Ad_A (");
        buffer.append(System.identityHashCode(this)).append("){");
        buffer.append("\n charField=");
        try {
            buffer.append(charField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, byteField=");
        try {
            buffer.append(byteField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, shortField=");
        try {
            buffer.append(shortField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, intField=");
        try {
            buffer.append(intField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, longField=");
        try {
            buffer.append(longField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, floatField=");
        try {
            buffer.append(floatField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, doubleField=");
        try {
            buffer.append(doubleField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, stringField='" + "'");
        try {
            buffer.append(stringField + "'");
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, dateField='" + "'");
        try {
            buffer.append(dateField + "'");
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, charsField=");
        try {
            buffer.append(charsField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, bytesField=");
        try {
            buffer.append(bytesField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, shortsField=");
        try {
            buffer.append(shortsField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, intsField=");
        try {
            buffer.append(intsField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, longsField=");
        try {
            buffer.append(longsField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, floatsField=");
        try {
            buffer.append(floatsField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("\n, doublesField=");
        try {
            buffer.append(doublesField);
        } catch (Throwable x) {
            buffer.append("[not allowed]");
        }
        buffer.append("}");
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleFields)) return false;

        final SimpleFields simpleFields = (SimpleFields)o;

        if (booleanField != simpleFields.booleanField) return false;
        if (byteField != simpleFields.byteField) return false;
        if (charField != simpleFields.charField) return false;
        if (doubleField != simpleFields.doubleField) return false;
        if (floatField != simpleFields.floatField) return false;
        if (intField != simpleFields.intField) return false;
        if (longField != simpleFields.longField) return false;
        if (shortField != simpleFields.shortField) return false;
        if (!Arrays.equals(booleansField, simpleFields.booleansField)) return false;
        if (!Arrays.equals(bytesField, simpleFields.bytesField)) return false;
        if (!Arrays.equals(charsField, simpleFields.charsField)) return false;
        if (!Arrays.equals(doublesField, simpleFields.doublesField)) return false;
        if (!Arrays.equals(floatsField, simpleFields.floatsField)) return false;
        if (!Arrays.equals(intsField, simpleFields.intsField)) return false;
        if (!Arrays.equals(longsField, simpleFields.longsField)) return false;
        if (!Arrays.equals(shortsField, simpleFields.shortsField)) return false;
        if (stringField != null ? !stringField.equals(simpleFields.stringField) : simpleFields.stringField != null) return false;
        if (dateField != null ? !dateField.equals(simpleFields.dateField) : simpleFields.dateField != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        long temp;
        result = (booleanField ? 1 : 0);
        result = 29 * result + (int)charField;
        result = 29 * result + (int)byteField;
        result = 29 * result + (int)shortField;
        result = 29 * result + intField;
        result = 29 * result + (int)(longField ^ (longField >>> 32));
        result = 29 * result + floatField != +0.0f ? Float.floatToIntBits(
                floatField) : 0;
        temp = doubleField != +0.0d ? Double.doubleToLongBits(doubleField) : 0l;
        result = 29 * result + (int)(temp ^ (temp >>> 32));
        result = 29 * result + (stringField != null ? stringField.hashCode() : 0);
        result = 29 * result + (dateField != null ? dateField.hashCode() : 0);
        return result;
    }
}
