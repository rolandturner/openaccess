
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

import java.util.Vector;

/**
 * For testing many-to-many with Vector's. This has a Vector of UserV
 * which has a HashSet of GroupV.
 *
 * @keep-all
 */
public class GroupV implements Comparable {

    private String name;
    private Vector users = new Vector(); // of UserAL

    public GroupV(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(UserV u) {
        users.add(u);
    }

    public void remove(UserV u) {
        users.remove(u);
    }

    public Vector getUsers() {
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
        return name.compareTo(((GroupV)o).name);
    }

    public String toString() {
        return name;
    }
}
