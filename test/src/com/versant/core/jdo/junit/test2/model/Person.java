
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
 * For testing sharing of simple columns.
 * @keep-all
 */
public class Person {

    private String name;
    private Country country;
    private String countryCode; // shared mapping to country column
    private int age;
    private int val;

    public Person(String name, Country country) {
        this.name = name;
        setCountry(country);
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
        countryCode = country == null ? null : country.getCode();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String toString() {
        return name;
    }

}
