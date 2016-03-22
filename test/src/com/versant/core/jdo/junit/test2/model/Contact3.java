
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

public class Contact3 {
    
    private Name3 name;
    private Name3 surname;
    private String phone;
    private String fax;
    private String email;


    public Contact3() {
    }

    public Contact3(String name, int nameId, String surname, int surnameId, String phone, String fax, String email) {
        this.name = new Name3(nameId, name);
        this.surname = new Name3(surnameId, surname);
        this.phone = phone;
        this.fax = fax;
        this.email = email;
    }

    public Name3 getName() {
        return name;
    }

    public void setName(Name3 name) {
        this.name = name;
    }

    public Name3 getSurname() {
        return surname;
    }

    public void setSurname(Name3 surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(name.getId() + " " + name.getName());
        buffer.append(" ");
        buffer.append(surname.getId() + " " + surname.getName());
        buffer.append(" (");
        buffer.append(email);
        buffer.append(")");
        return (buffer.toString());
    }

}

