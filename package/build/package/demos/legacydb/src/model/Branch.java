
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
package model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * A branch of a retail store.
 */
public class Branch {

    private int branchNo = 0;   // pk
    private String name;
    private Country country;
    private String countryCode; // mapped to same column as country

    public Branch(int branchNo, String name) {
        this.branchNo = branchNo;
        this.name = name;
    }

    public int getBranchNo() {
        return branchNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name + " " + countryCode;
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

    /**
     * Application identity objectid-class.
     */
    public static class ID implements Serializable {

        public int branchNo;

        public ID() {
        }

        public ID(String s) {
            branchNo = Integer.parseInt(s);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (branchNo != id.branchNo) return false;

            return true;
        }

        public int hashCode() {
            return branchNo;
        }

        public String toString() {
            return Integer.toString(branchNo);
        }

    }

}

