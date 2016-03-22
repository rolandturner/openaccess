
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
package com.versant.core.jdo.junit.test3.model.complex;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class A {

    private String val;
    private String sameNameVal;
    private List stringListBase = new ArrayList();
    private RefBase baseRef;
    private RefA refA;
    private RefB refB;
    private RefBase dfgBaseRef;
    private List refBList = new ArrayList();
    private List fkCol = new ArrayList();

    public List getFkCol() {
        return fkCol;
    }

    public void setFkCol(List fkCol) {
        this.fkCol = fkCol;
    }

    public List getRefBList() {
        return refBList;
    }

    public void setRefBList(List refBList) {
        this.refBList = refBList;
    }

    public RefBase getDfgBaseRef() {
        return dfgBaseRef;
    }

    public void setDfgBaseRef(RefBase dfgBaseRef) {
        this.dfgBaseRef = dfgBaseRef;
    }

    public RefA getRefA() {
        return refA;
    }

    public void setRefA(RefA refA) {
        this.refA = refA;
    }

    public RefB getRefB() {
        return refB;
    }

    public void setRefB(RefB refB) {
        this.refB = refB;
    }

    public RefBase getBaseRef() {
        return baseRef;
    }

    public void setBaseRef(RefBase baseRef) {
        this.baseRef = baseRef;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getSameNameVal() {
        return sameNameVal;
    }

    public void setSameNameVal(String sameNameVal) {
        this.sameNameVal = sameNameVal;
    }

    public List getStringListBase() {
        return stringListBase;
    }

    public void setStringListBase(List stringListBase) {
        this.stringListBase = stringListBase;
    }
}
