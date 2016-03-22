
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
package com.versant.core.jdo.junit.test0.model;

/**
 * Class for panos.louridas@investment-bank.gr test case.
 *
 * @keep-all
 */
public class PanR {

    private PanA refA1;
    private PanA refA2;
    private int anotherField;
    private int yetAnotherField;

    public PanR() {
    }

    public String toString() {
        return "PanR [refA1=" + refA1 + ", refA2=" + refA2 +
                ", anotherField=" + anotherField +
                ", yetAnotherField=" + yetAnotherField + "]";
    }

    public PanA getRefA1() {
        return refA1;
    }

    public void setRefA1(PanA refA1) {
        this.refA1 = refA1;
    }

    public PanA getRefA2() {
        return refA2;
    }

    public void setRefA2(PanA refA2) {
        this.refA2 = refA2;
    }

    public int getAnotherField() {
        return anotherField;
    }

    public void setAnotherField(int anotherField) {
        this.anotherField = anotherField;
    }

    public int getYetAnotherField() {
        return yetAnotherField;
    }

    public void setYetAnotherField(int yetAnotherField) {
        this.yetAnotherField = yetAnotherField;
    }

}

