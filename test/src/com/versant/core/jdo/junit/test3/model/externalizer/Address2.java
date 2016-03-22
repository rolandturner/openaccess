
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
package com.versant.core.jdo.junit.test3.model.externalizer;

import java.io.Serializable;

/**
 * For testing externalization. This class is persistent but the reference
 * to it on ExtContainer is set to use externalization.
 */
public class Address2 implements Serializable {

    private String street;
    private String city;

    public Address2() {
    }

    public Address2(String street, String city) {
        this.street = street;
        this.city = city;
    }

    public Address2(String s) {
        int i = s.indexOf('/');
        street = s.substring(0, i);
        city = s.substring(i + 1);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String toString() {
        return street + "/" + city;
    }

}

