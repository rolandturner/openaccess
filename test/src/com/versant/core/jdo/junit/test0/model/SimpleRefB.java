
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
 * @keep-all
 */
public class SimpleRefB {

    private String valB1;
    private String valB2;
    private String valB3;
    private SimpleRefC simpleRefC;

    public SimpleRefB() {
    }

    public SimpleRefB(SimpleRefC simpleRefC) {
        this.simpleRefC = simpleRefC;
    }

    public SimpleRefC getSimpleRefC() {
        return simpleRefC;
    }

    public void setSimpleRefC(SimpleRefC simpleRefC) {
        this.simpleRefC = simpleRefC;
    }

    public String getValB1() {
        return valB1;
    }

    public void setValB1(String valB1) {
        this.valB1 = valB1;
    }

    public String getValB2() {
        return valB2;
    }

    public void setValB2(String valB2) {
        this.valB2 = valB2;
    }

    public String getValB3() {
        return valB3;
    }

    public void setValB3(String valB3) {
        this.valB3 = valB3;
    }
}

