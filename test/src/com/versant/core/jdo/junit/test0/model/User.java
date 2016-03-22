
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
package com.versant.core.jdo.junit.test0.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @keep-all
 */
public class User {

    private String name;
    private ArrayList groups = new ArrayList();

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getGroups() {
        return groups;
    }

    public void addGroup(Groups group) {
        groups.add(groups);
    }

    public void listGroups() {
        System.out.println("\n----------------------------------------------");
        System.out.println("- User  : " + name);
        System.out.println("----------------------------------------------");
        for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
            Groups group = (Groups)iterator.next();
            System.out.println("- Group : " + group.getName());

        }
        System.out.println(
                "----------------------------------------------\n\n");
    }

}

