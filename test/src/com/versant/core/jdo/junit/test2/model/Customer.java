
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

import java.util.List;
import java.util.ArrayList;

/**
 * Simple Customer with a List of Contacts.
 */
public class Customer {

    private String name;
    private Industry industry;
    private List contacts = new ArrayList(); // of Contact

    public Customer(String name) {
        this.name = name;
        industry = new Industry("ind" + name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getContacts() {
        return new ArrayList(contacts);
    }

    public void addContact(Contact c) {
        contacts.add(c);
    }

    public void removeContact(Contact c) {
        contacts.remove(c);
    }

    public String toString() {
        return name;
    }

    public Industry getIndustry() {
        return industry;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }

}
