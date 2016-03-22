
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
package com.versant.core.jdo.junit.testfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @keep-all
 */
public class OrderFK {

    private String val;
    private long longVal;
    private int ordVal;

    private List orderItems = new ArrayList();
    private RefAB refAB;
    private RefBC refBC;

    public RefAB getRefAB() {
        return refAB;
    }

    public void setRefAB(RefAB refAB) {
        this.refAB = refAB;
    }

    public RefBC getRefBC() {
        return refBC;
    }

    public void setRefBC(RefBC refBC) {
        this.refBC = refBC;
    }

    public List getOrderItems() {
        return orderItems;
    }

    public int getOrdVal() {
        return ordVal;
    }

    public void setOrdVal(int ordVal) {
        this.ordVal = ordVal;
    }

    public void setOrderItems(List orderItems) {
        this.orderItems = orderItems;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public void setLongVal(long orderLong) {
        this.longVal = orderLong;
    }

    public long getLongVal() {
        return longVal;
    }
}

