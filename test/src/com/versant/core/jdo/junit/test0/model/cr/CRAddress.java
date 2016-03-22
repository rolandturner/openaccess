
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
package com.versant.core.jdo.junit.test0.model.cr;

/**
 */
public class CRAddress {
    protected CRCity city = null;
    protected String street = "";

    public CRAddress() {
    }

    public CRAddress(CRCity c, String s) {
        city = c;
        street = s;
    }

    public CRCity getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public void setCity(CRCity c) {
        city = c;
    }

    public void setStreet(String s) {
        street = s;
    }

}
