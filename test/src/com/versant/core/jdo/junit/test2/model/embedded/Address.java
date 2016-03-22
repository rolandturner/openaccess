
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
package com.versant.core.jdo.junit.test2.model.embedded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 */
public class Address {
    private String street;
    private String city;
    private Address nonEmbeddedAddress;
    private Country country;
    private List stringList = new ArrayList();
    private List primsList = new ArrayList();
    private Person owner;
    private Map stringAddressMap = new HashMap();
    private Address[] addArray;
    private int streetNumber;

    public Address[] getAddArray() {
        return addArray;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public void setAddArray(Address[] addArray) {
        this.addArray = addArray;
    }

    public Map getStringAddressMap() {
        return stringAddressMap;
    }

    public void setStringAddressMap(Map stringAddressMap) {
        this.stringAddressMap = stringAddressMap;
    }

    public Address() {
    }

    public Address(String street, String city) {
        this.street = street;
        this.city = city;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public List getPrims() {
        return primsList;
    }

    public void setPrims(List prims) {
        this.primsList = prims;
    }

    public List getStringList() {
        return stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Address getNonEmbeddedAddress() {
        return nonEmbeddedAddress;
    }

    public void setNonEmbeddedAddress(Address nonEmbeddedAddress) {
        this.nonEmbeddedAddress = nonEmbeddedAddress;
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
}
