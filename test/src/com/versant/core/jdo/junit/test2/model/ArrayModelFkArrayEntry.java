
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

/**
 */
public class ArrayModelFkArrayEntry {
    private ArraysModel parent;
    private String val;
    private int ordVal;

    public ArrayModelFkArrayEntry(ArraysModel parent) {
        this.parent = parent;
    }

    public ArrayModelFkArrayEntry(ArraysModel parent, int ord) {
        this.parent = parent;
        this.ordVal = ord;
    }

    public ArrayModelFkArrayEntry() {
    }

    public int getOrdVal() {
        return ordVal;
    }

    public void setOrdVal(int ordVal) {
        this.ordVal = ordVal;
    }

    public ArraysModel getParent() {
        return parent;
    }

    public void setParent(ArraysModel parent) {
        this.parent = parent;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
