
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

/** 
 * A Country.
 */
public class Country {

    private String code;
    private String name;

    public static final ID ZA = new ID("ZA");
    public static final ID NA = new ID("NA");
    public static final ID US = new ID("US");
    public static final ID UK = new ID("UK");

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements Serializable {

        public String code;

        public ID(String code) {
            this.code = code;
        }

        public ID() {
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (!code.equals(id.code)) return false;

            return true;
        }

        public int hashCode() {
            return code.hashCode();
        }

        public String toString() {
            return code;
        }

    }

}

