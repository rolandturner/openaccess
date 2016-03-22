
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
public class OrderItem {

    private String val;
    private int order;
    private List productions = new ArrayList();
    private List stringList = new ArrayList();
    private long parentLongVal;
    private long longVal;

    public long getParentLongVal() {
        return parentLongVal;
    }

    public void setParentLongVal(long parentLongVal) {
        this.parentLongVal = parentLongVal;
    }

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

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

    public List getStringList() {
        return stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }
}
