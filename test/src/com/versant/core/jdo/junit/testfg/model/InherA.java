
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
public class InherA {

    private List stringListA = new ArrayList();
    private List inherAList = new ArrayList();
    private int order;
    private String val;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List getStringListA() {
        return stringListA;
    }

    public void setStringListA(List stringListA) {
        this.stringListA = stringListA;
    }

    public List getInherAList() {
        return inherAList;
    }

    public void setInherAList(List inherAList) {
        this.inherAList = inherAList;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

}
