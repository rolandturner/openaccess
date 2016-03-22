
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
import java.util.Date;

/**
 *
 */
public class SimpleFieldsBase implements Serializable {

    private int simpleFieldsBaseId; // pk
    protected boolean booleanField;
    protected char charField;
    protected byte byteField;
    protected short shortField;
    protected int intField;
    protected long longField;
    protected float floatField;
    protected double doubleField;
    protected String stringField;
    protected Date dateField;

    public SimpleFieldsBase(int simpleFieldsBaseId) {
        this.simpleFieldsBaseId = simpleFieldsBaseId;
    }

    public int getSimpleFieldsBaseId() {
        return simpleFieldsBaseId;
    }

    public boolean isBooleanField() {
        return booleanField;
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField = booleanField;
    }

    public char getCharField() {
        return charField;
    }

    public void setCharField(char charField) {
        this.charField = charField;
    }

    public byte getByteField() {
        return byteField;
    }

    public void setByteField(byte byteField) {
        this.byteField = byteField;
    }

    public short getShortField() {
        return shortField;
    }

    public void setShortField(short shortField) {
        this.shortField = shortField;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public long getLongField() {
        return longField;
    }

    public void setLongField(long longField) {
        this.longField = longField;
    }

    public float getFloatField() {
        return floatField;
    }

    public void setFloatField(float floatField) {
        this.floatField = floatField;
    }

    public double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(double doubleField) {
        this.doubleField = doubleField;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public void setSimpleFieldsBaseId(int simpleFieldsBaseId) {
        this.simpleFieldsBaseId = simpleFieldsBaseId;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements java.io.Serializable {

        public int simpleFieldsBaseId;

        public ID() {
        }

        public ID(String s) {
            int i, p = 0;
            simpleFieldsBaseId = Integer.parseInt(s.substring(p));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleFields.ID)) return false;

            final SimpleFields.ID id = (SimpleFields.ID)o;

            if (this.simpleFieldsBaseId != id.simpleFieldsBaseId) return false;
            return true;
        }

        public int hashCode() {
            int result = 0;
            result = 29 * result + (int)simpleFieldsBaseId;
            return result;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(simpleFieldsBaseId);
            return buffer.toString();
        }
    }
}
