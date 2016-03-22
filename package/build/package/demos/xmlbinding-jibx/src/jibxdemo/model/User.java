
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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * A user with many Groups. This class includes a JiBX post-set method to
 * complete the other side of the many-to-many with Group.users after
 * JiBX has finished unmarshalling a User.
 */
public class User extends ModelObject {

    private String name;
    private HashSet groups = new HashSet(); // of Group.users

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a copy of the groups.
     */
    public List getGroups() {
        return new ArrayList(groups);
    }

    public void addGroup(Group g) {
        addGroupImp(g);
        g.addUserImp(this);
    }

    void addGroupImp(Group g) {
        groups.add(g);
    }

    public void removeGroup(Group g) {
        removeGroupImp(g);
        g.removeUserImp(this);
    }

    void removeGroupImp(Group g) {
        groups.remove(g);
    }

    /**
     * This is invoked by JiBX when it has finished unmarshalling a User.
     * This completes the many-to-many with Group.users. JiBX will only fill
     * User.groups.
     */
    public void jibxPostSet() {
        for (Iterator i = groups.iterator(); i.hasNext(); ) {
            Group g = (Group)i.next();
            g.addUserImp(this);
        }
    }
}
