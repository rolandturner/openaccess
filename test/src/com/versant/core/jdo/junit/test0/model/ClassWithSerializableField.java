
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
public class ClassWithSerializableField {
    private String val;
    private SerializableReference sr;   //persistent
    private SerializableReference2 sr2; //non-persistent

    public SerializableReference2 getSr2() {
        return sr2;
    }

    public void setSr2(SerializableReference2 sr2) {
        this.sr2 = sr2;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public SerializableReference getSr() {
        return sr;
    }

    public void setSr(SerializableReference sr) {
        this.sr = sr;
    }
}
