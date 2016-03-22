
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
package com.versant.core.jdo.junit.test2.model;


/**
 */
public class Ref2 {
    private int amount2;
    private String ref2Val;
    private Ref3 ref3;

    public Ref2(String ref2Val, Ref3 ref3) {
        this.ref2Val = ref2Val;
        this.ref3 = ref3;
    }

    public Ref2(int amount2, Ref3 ref3) {
        this.amount2 = amount2;
        this.ref3 = ref3;
    }

    public int getAmount2() {
        return amount2;
    }

    public void setAmount2(int amount2) {
        this.amount2 = amount2;
    }

    public Ref3 getRef3() {
        return ref3;
    }

    public void setRef3(Ref3 ref3) {
        this.ref3 = ref3;
    }

    public String getRef2Val() {
        return ref2Val;
    }

    public void setRef2Val(String ref2Val) {
        this.ref2Val = ref2Val;
    }
}
