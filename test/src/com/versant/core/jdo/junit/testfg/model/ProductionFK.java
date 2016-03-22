
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

/**
 * @keep-all
 */
public class ProductionFK {

    private OrderItemFK owner;
    private String val;
    private int ordVal;
    private long parentLongVal;

    public ProductionFK(String val) {
        this.val = val;
    }

    public ProductionFK(OrderItemFK owner, String val, int ordVal,
            long pLongVal) {
        this.owner = owner;
        this.val = val;
        this.ordVal = ordVal;
        this.parentLongVal = pLongVal;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public OrderItemFK getOwner() {
        return owner;
    }

    public void setOwner(OrderItemFK owner) {
        this.owner = owner;
    }

    public int getOrdVal() {
        return ordVal;
    }

    public void setOrdVal(int ordVal) {
        this.ordVal = ordVal;
    }

    public long getParentLongVal() {
        return parentLongVal;
    }

    public void setParentLongVal(long parentLongVal) {
        this.parentLongVal = parentLongVal;
    }
}

