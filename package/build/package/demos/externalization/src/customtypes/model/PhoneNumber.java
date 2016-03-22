
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
package customtypes.model;

/**
 * <p>Custom data type to hold a phone number with area code. It provides
 * validation and formatting of the number. PhoneNumber's are immutable.</p>
 */
public class PhoneNumber {

    private String countryCode;
    private String areaCode;
    private String number;

    /**
     * Create from a String formatted like +27 21 6703940. JDO Genie uses
     * this method to create an instance from a String read from the
     * database.
     */
    public PhoneNumber(String s) {
        if (s == null || s.length() == 0) {
            countryCode = areaCode = number = "";
        } else {
            int i = s.indexOf(' ');
            countryCode = s.substring(1, i);
            int j = s.indexOf(' ', i + 1);
            areaCode = s.substring(i + 1, j);
            number = s.substring(j + 1);
        }
    }

    public PhoneNumber(String countryCode, String areaCode, String number) {
        this.countryCode = countryCode;
        this.areaCode = areaCode;
        this.number = number;
    }

    /**
     * JDO Genie uses this method to get the String to store in the database.
     */
    public String toString() {
        return "+" + countryCode + " " + areaCode + " " + number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getNumber() {
        return number;
    }

}
