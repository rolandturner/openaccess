
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
import java.util.List;

/**
 * For testing many-to-many with List's. This has a List of UserL
 * which has a List of GroupL.
 *
 * @keep-all
 */
public class GroupL implements Comparable {

    private String name;
    private List users = new ArrayList(); // of UserL

    public GroupL(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(UserL u) {
        users.add(u);
    }

    public void remove(UserL u) {
        users.remove(u);
    }

    public List getUsers() {
        return users;
    }

    /**
     * Get list of users as a space separated String.
     */
    public String getUsersString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) s.append(' ');
            s.append(users.get(i));
        }
        return s.toString();
    }

    /**
     * Order by name.
     */
    public int compareTo(Object o) {
        return name.compareTo(((GroupL)o).name);
    }

    public String toString() {
        return name;
    }
}
