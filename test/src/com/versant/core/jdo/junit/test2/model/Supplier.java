
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
 * Simple Supplier with a List of Contacts.
 *
 * @keep-all
 */
public class Supplier {

    private String name;
    private List contacts = new ArrayList(); // of Contact
    private SupplierRegister register;

    public Supplier(String name) {
        this.name = name;
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

    public SupplierRegister getRegister() {
        return register;
    }

    public void setRegister(SupplierRegister register) {
        this.register = register;
    }

    public String toString() {
        return name;
    }

}
