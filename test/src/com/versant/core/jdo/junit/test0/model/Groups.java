
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
public class Groups {

    private String name;
    private ArrayList users = new ArrayList();

    public Groups(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getUsers() {
        return users;
    }

    public void addUsers(User user) {
        users.add(users);
    }

    public void listUsers() {
        System.out.println("\n----------------------------------------------");
        System.out.println("- Group : " + name);
        System.out.println("----------------------------------------------");
        for (Iterator iterator = users.iterator(); iterator.hasNext();) {
            User user = (User)iterator.next();
            System.out.println("- User  : " + user.getName());

        }
        System.out.println(
                "----------------------------------------------\n\n");
    }
}

