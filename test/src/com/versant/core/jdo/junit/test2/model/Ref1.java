
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
public class Ref1 {
    private String ref1Val;
    private int amount;
    private Ref2 ref2;

    public Ref1(String ref1Val, Ref2 ref2) {
        this.ref1Val = ref1Val;
        this.ref2 = ref2;
    }

    public Ref1(int amount, Ref2 ref2) {
        this.amount = amount;
        this.ref2 = ref2;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getRef1Val() {
        return ref1Val;
    }

    public void setRef1Val(String ref1Val) {
        this.ref1Val = ref1Val;
    }

    public Ref2 getRef2() {
        return ref2;
    }

    public void setRef2(Ref2 ref2) {
        this.ref2 = ref2;
    }


}
