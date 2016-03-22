
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
 * An Address.
 */
public class Address2 {

    private String street;
    private String city;
    private Country2 country;

    public Address2() {
    }

    public Address2(String street, String city, Country2 country) {
        this.street = street;
        this.city = city;
        this.country = country;
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

    public Country2 getCountry() {
        return country;
    }

    public void setCountry(Country2 country) {
        this.country = country;
    }

    public String toString() {
        return street + " " + city;
    }

}

