
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
 */
public class SimpleRefC {

    private String valC1;
    private String valC2;
    private String valC3;
    private SimpleRefD refD;

    public SimpleRefC() {
    }

    public SimpleRefC(SimpleRefD refD) {
        this.refD = refD;
    }

    public SimpleRefD getRefD() {
        return refD;
    }

    public void setRefD(SimpleRefD refD) {
        this.refD = refD;
    }

    public String getValC1() {
        return valC1;
    }

    public void setValC1(String valC1) {
        this.valC1 = valC1;
    }

    public String getValC2() {
        return valC2;
    }

    public void setValC2(String valC2) {
        this.valC2 = valC2;
    }

    public String getValC3() {
        return valC3;
    }

    public void setValC3(String valC3) {
        this.valC3 = valC3;
    }
}

