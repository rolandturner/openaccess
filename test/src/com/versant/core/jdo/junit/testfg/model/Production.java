
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
public class Production {

    private String val;
    private String val2;
    private int order;
    private long parentLongVal;

    public Production(String val) {
        this.val = val;
    }

    public Production(String val, String val2) {
        this.val = val;
        this.val2 = val2;
    }

    public Production(String val, String val2, long pLong) {
        this.val = val;
        this.val2 = val2;
        this.parentLongVal = pLong;
    }

    public long getParentLongVal() {
        return parentLongVal;
    }

    public void setParentLongVal(long parentLongVal) {
        this.parentLongVal = parentLongVal;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getVal2() {
        return val2;
    }

    public void setVal2(String val2) {
        this.val2 = val2;
    }
}
