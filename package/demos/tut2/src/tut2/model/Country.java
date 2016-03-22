
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
 * A country.
 */
public class Country {

    private String isoCode;
    private String name;

    public Country(String isoCode, String name) {
        this.isoCode = isoCode;
        this.name = name;
    }

    public Country() {
    }

    public String getIsoCode() {
        return isoCode;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        try {
            return name.equals(((Country) obj).name);
        } catch (Exception e) {
            return false;
        }
    }

}

