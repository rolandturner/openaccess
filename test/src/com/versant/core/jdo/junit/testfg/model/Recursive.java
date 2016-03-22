
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @keep-all
 */
public class Recursive {

    private String val;
    private int ord;
    private Recursive parent;
    private Recursive parentSet;
    private List recFkList = new ArrayList();
    private List recList = new ArrayList();
    private Set recFkSet = new HashSet();
    private Set recSet = new HashSet();
    private String tStamp;
    private String parentTStamp;

    public Recursive() {
    }

    public Recursive(String val) {
        this.val = val;
    }

    public String getParentTStamp() {
        return parentTStamp;
    }

    public void setParentTStamp(String parentTStamp) {
        this.parentTStamp = parentTStamp;
    }

    public String gettStamp() {
        return tStamp;
    }

    public void settStamp(String tStamp) {
        this.tStamp = tStamp;
    }

    public int getOrd() {
        return ord;
    }

    public void setOrd(int ord) {
        this.ord = ord;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public List getRecList() {
        return recList;
    }

    public void setRecList(List recList) {
        this.recList = recList;
    }

    public Recursive getParentSet() {
        return parentSet;
    }

    public void setParentSet(Recursive parentSet) {
        this.parentSet = parentSet;
    }

    public Recursive getParent() {
        return parent;
    }

    public void setParent(Recursive parent) {
        this.parent = parent;
    }

    public List getRecFkList() {
        return recFkList;
    }

    public void setRecFkList(List recFkList) {
        this.recFkList = recFkList;
    }

    public Set getRecFkSet() {
        return recFkSet;
    }

    public void setRecFkSet(Set recFkSet) {
        this.recFkSet = recFkSet;
    }

    public Set getRecSet() {
        return recSet;
    }

    public void setRecSet(Set recSet) {
        this.recSet = recSet;
    }
}

