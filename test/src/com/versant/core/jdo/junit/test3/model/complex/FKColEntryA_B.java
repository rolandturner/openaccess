
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
public class FKColEntryA_B extends FKColEntryA {

    private String valAB;

    public FKColEntryA_B(A a, String valBase, String valA, String valAB) {
        super(a, valBase, valA);
        this.valAB = valAB;
    }

    public String getValAB() {
        return valAB;
    }

    public void setValAB(String valAB) {
        this.valAB = valAB;
    }
}
