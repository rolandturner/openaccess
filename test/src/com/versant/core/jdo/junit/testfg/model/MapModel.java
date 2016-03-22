
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

import java.util.HashMap;
import java.util.Map;

/**
 * @keep-all
 */
public class MapModel {

    /**
     * String-Order mapping
     */
    private Map stringOrderMap = new HashMap();
    private Map stringStringMap = new HashMap();
    private Map orderOrderMap = new HashMap();
    private Map orderStringMap = new HashMap();
    private int ordering;
    private String val;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Map getOrderStringMap() {
        return orderStringMap;
    }

    public void setOrderStringMap(Map orderStringMap) {
        this.orderStringMap = orderStringMap;
    }

    public Map getStringOrderMap() {
        return stringOrderMap;
    }

    public void setStringOrderMap(Map stringOrderMap) {
        this.stringOrderMap = stringOrderMap;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    public Map getStringStringMap() {
        return stringStringMap;
    }

    public void setStringStringMap(Map stringStringMap) {
        this.stringStringMap = stringStringMap;
    }

    public Map getOrderOrderMap() {
        return orderOrderMap;
    }

    public void setOrderOrderMap(Map orderOrderMap) {
        this.orderOrderMap = orderOrderMap;
    }
}

