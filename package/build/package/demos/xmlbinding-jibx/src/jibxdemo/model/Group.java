
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
package jibxdemo.model;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

/**
 * A group with many Users.
 */
public class Group extends ModelObject {

    private String description;
    private HashSet users = new HashSet(); // of User.groups

    public Group() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get a copy of the users.
     */
    public List getUsers() {
        return new ArrayList(users);
    }

    public void addUser(User u) {
        addUserImp(u);
        u.addGroupImp(this);
    }

    void addUserImp(User user) {
        users.add(user);
    }

    public void removeUser(User u) {
        removeUserImp(u);
        u.removeGroupImp(this);
    }

    void removeUserImp(User user) {
        users.remove(user);
    }
}
