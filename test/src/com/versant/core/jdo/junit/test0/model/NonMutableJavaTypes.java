
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
package com.versant.core.jdo.junit.test0.model;

import java.util.Locale;

/**
 * @keep-all
 */
public class NonMutableJavaTypes {

    private byte byteValue;
    private Byte byteWValue;
    private short shortValue;
    private Short shortWValue;
    private int intValue;
    private Integer intWValue;
    private char charValue;
    private long longValue;
    private float floatValue;
    private double doubleValue;
    private boolean booleanValue;
    private Boolean booleanWValue;
    private String stringValue;
    private Locale locale;

    public Boolean getBooleanWValue() {
        return booleanWValue;
    }

    public void setBooleanWValue(Boolean booleanWValue) {
        this.booleanWValue = booleanWValue;
    }

    public Byte getByteWValue() {
        return byteWValue;
    }

    public void setByteWValue(Byte byteWValue) {
        this.byteWValue = byteWValue;
    }

    public Short getShortWValue() {
        return shortWValue;
    }

    public void setShortWValue(Short shortWValue) {
        this.shortWValue = shortWValue;
    }

    public Integer getIntWValue() {
        return intWValue;
    }

    public void setIntWValue(Integer intWValue) {
        this.intWValue = intWValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(byte byteValue) {
        this.byteValue = byteValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        this.shortValue = shortValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public char getCharValue() {
        return charValue;
    }

    public void setCharValue(char charValue) {
        this.charValue = charValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}

