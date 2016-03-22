
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

/**
 */
public class FKColEntryA_A extends FKColEntryA {

    private String valAA;

    public FKColEntryA_A(A a, String valBase, String valA, String valAA) {
        super(a, valBase, valA);
        this.valAA = valAA;
    }

    public String getValAA() {
        return valAA;
    }

    public void setValAA(String valAA) {
        this.valAA = valAA;
    }
}
