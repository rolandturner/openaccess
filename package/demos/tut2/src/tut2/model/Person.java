
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A person with firstName, lastName and address fields. A history of the
 * person's old addresses is kept in an oldAddressList.
 */
public class Person {

    private String firstName;
    private String lastName;
    private Address address;
    private List oldAddressList = new ArrayList();

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Person() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Set a new address preserving the current address by adding it to
     * the oldAddressList.
     */
    public void setAddress(Address address) {
        if (this.address != null) oldAddressList.add(this.address);
        this.address = address;
    }

    public List getOldAddressList() {
        return Collections.unmodifiableList(oldAddressList);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(firstName);
        s.append(' ');
        s.append(lastName);
        if (address != null && address.getCountry() != null) {
            s.append(", ");
            s.append(address.getCountry());
        }
        return s.toString();
    }

}


