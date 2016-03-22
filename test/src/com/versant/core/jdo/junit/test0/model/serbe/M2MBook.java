
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
package com.versant.core.jdo.junit.test0.model.serbe;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class M2MBook {
    private String name;
    private List persons;


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass()).append("\nName             : ").append(getName());

        return sb.toString();
    }


    public static String getDefaultName1() {
        return "Book number 1";
    }

    public static String getDefaultName2() {
        return "Book number 2";
    }

    public static String getDefaultName3() {
        return "Book number 3";
    }

    public static String getDefaultName4() {
        return "New book 1";
    }

    public static String getDefaultName5() {
        return "New book 2";
    }

    public static String getDefaultName6() {
        return "Book number 6";
    }

    public static String getDefaultName7() {
        return "Book number 7";
    }


    public void addPerson(M2MPerson person) {
        if (this.persons == null) {
            this.persons = new ArrayList();
        }
        this.persons.add(person);
    }


    public void removePerson(M2MPerson person) {
        if (this.persons == null) {
            this.persons = new ArrayList();
        }
        this.persons.remove(person);
    }


    public String getName() {
        return this.name;
    }

    public List getPersons() {
        return this.persons;
    }

    public void setName(String string) {
        this.name = string;
    }

    public void setPersons(List list) {
        this.persons = list;
    }
}
