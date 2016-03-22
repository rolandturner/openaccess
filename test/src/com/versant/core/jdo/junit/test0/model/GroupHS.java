
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
import java.util.Collections;
import java.util.HashSet;

/**
 * For testing many-to-many with HashSet's.
 *
 * @keep-all
 */
public class GroupHS implements Comparable {

    private String name;
    private HashSet users = new HashSet();

    public GroupHS(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(UserHS u) {
        users.add(u);
    }

    public void remove(UserHS u) {
        users.remove(u);
    }

    public HashSet getUsers() {
        return users;
    }

    /**
     * Get sorted list of users as a space separated String.
     */
    public String getUsersString() {
        ArrayList a = new ArrayList(users);
        Collections.sort(a);
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < a.size(); i++) {
            if (i > 0) s.append(' ');
            s.append(a.get(i));
        }
        return s.toString();
    }

    /**
     * Order by name.
     */
    public int compareTo(Object o) {
        return name.compareTo(((GroupHS)o).name);
    }

    public String toString() {
        return name;
    }
}
