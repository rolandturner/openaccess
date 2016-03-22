
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
public class FKColEntryBase {

    private A a;
    private String valBase;

    public FKColEntryBase(String valBase) {
        this.valBase = valBase;
    }

    public FKColEntryBase(A a, String valBase) {
        this.a = a;
        this.valBase = valBase;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public String getValBase() {
        return valBase;
    }

    public void setValBase(String valBase) {
        this.valBase = valBase;
    }
}
