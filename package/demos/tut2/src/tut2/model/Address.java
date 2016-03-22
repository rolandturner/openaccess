
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
package tut2.model;

/**
 * An address.
 */
public class Address {

    private String street;
    private String city;
    private String code;
    private Country country;

    public Address() {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String toString() {
        return street + ", " + city + ", " + code + ", " + country;
    }

    public int hashCode() {
        int hash = 0;
        if (street != null) hash += street.hashCode();
        if (city != null) hash += city.hashCode();
        if (code != null) hash += code.hashCode();
        if (country != null) hash += country.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {
        try {
            Address address = (Address) obj;
            boolean b;
            b = ((street == null && address.street == null) || (street != null && street.equals(address.street)));
            b = b && ((city == null && address.city == null) || (city != null && city.equals(address.city)));
            b = b && ((code == null && address.code == null) || (code != null && code.equals(address.code)));
            b = b && ((country == null && address.country == null) || (country != null && country.equals(address.country)));
            return b;
        } catch (Exception e) {
            return false;
        }
    }
}

