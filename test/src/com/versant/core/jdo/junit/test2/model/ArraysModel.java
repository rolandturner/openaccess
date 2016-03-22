
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
package com.versant.core.jdo.junit.test2.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 */
public class ArraysModel {
    private String val;
    private String[] stringArray;
    private Locale[] locales;
    private int[] intArray;
    private Address[] addresses;
    //non-managed
    private ArrayModelFkArrayEntry[] fkArray1;
    //managed
    private ArrayModelFkArrayEntry2[] fkArray2;
    private List stringList = new ArrayList();

    public Locale[] getLocales() {
        return locales;
    }

    public void setLocales(Locale[] locales) {
        this.locales = locales;
    }

    public Address[] getAddresses() {
        return addresses;
    }

    public ArrayModelFkArrayEntry[] getFkArray1() {
        return fkArray1;
    }

    public void setFkArray1(ArrayModelFkArrayEntry[] fkArray1) {
        this.fkArray1 = fkArray1;
    }

    public List getStringList() {
        return stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }
}
