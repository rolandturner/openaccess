
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

import javax.jdo.InstanceCallbacks;

/**
 * @keep-all
 */
public class PrestoreModel implements InstanceCallbacks {

    private String a;
    private String b;
    public String newB;
    private Address address;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void jdoPostLoad() {
    }

    public void jdoPreStore() {
        b = newB;
        address = new Address(newB);
    }

    public void jdoPreClear() {
    }

    public void jdoPreDelete() {
    }

}


