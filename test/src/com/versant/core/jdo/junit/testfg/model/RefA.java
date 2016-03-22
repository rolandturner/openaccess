
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
public class RefA {

    private RefAB refAB;
    private RefAD refAD;
    private String val;
    private int order;

    public RefAB getRefAB() {
        return refAB;
    }

    public void setRefAB(RefAB refAB) {
        this.refAB = refAB;
    }

    public RefAD getRefAD() {
        return refAD;
    }

    public void setRefAD(RefAD refAD) {
        this.refAD = refAD;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

