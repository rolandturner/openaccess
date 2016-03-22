
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
package tut1.model;

public class Customer {

    private String code;
    private String name;

    public Customer() {
    }

    public Customer(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return Our customer's name.
     */
    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;

        final Customer customer = (Customer) o;

        if (code != null ? !code.equals(customer.code) : customer.code != null) return false;

        return true;
    }

    public int hashCode() {
        return (code != null ? code.hashCode() : 0);
    }
}
