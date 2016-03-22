
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

import com.versant.core.jdo.junit.test2.model.poly.PersistentIFace;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

/**
 * Simple address.
 */
public class Address implements PersistentIFace {

    private String street;
    private String city;

    public Address(String street, String city) {
        this.street = street;
        this.city = city;
    }

    public Address(String street) {
        this(street, null);
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

    public Object getOID() {
        PersistenceManager pm = JDOHelper.getPersistenceManager(this);
        if (pm == null) return null;
        return pm.getObjectId(this);
    }

    public String toString() {
        return street + " " + city;
    }

}
