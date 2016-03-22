
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
public class OrderItemFK {

    private OrderFK order;
    private String val;
    private List productions = new ArrayList();
    private int ordVal;
    private long longVal;
    private long parentLongVal;

    public List getProductions() {
        return productions;
    }

    public void setProductions(List productions) {
        this.productions = productions;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public OrderFK getOrder() {
        return order;
    }

    public void setOrder(OrderFK order) {
        this.order = order;
    }

    public int getOrdVal() {
        return ordVal;
    }

    public void setOrdVal(int ordVal) {
        this.ordVal = ordVal;
    }

    public void setLongVal(long oiLong) {
        this.longVal = oiLong;
    }

    public long getLongVal() {
        return longVal;
    }

    public long getParentLongVal() {
        return parentLongVal;
    }

    public void setParentLongVal(long orderLong) {
        this.parentLongVal = orderLong;
    }
}

