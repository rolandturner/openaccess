
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
package com.versant.core.jdo.junit.test2.model.unmanaged;

import java.util.ArrayList;
import java.util.Collections;

/**
 * For testing many-to-many with ArrayLists's. This has an ArrayList of
 * GroupAL which as an ArrayList of UserAL.
 */
public class UserALManaged {

	private String name;
	private ArrayList groups = new ArrayList(); // inverse GroupALManaged.users

	public UserALManaged(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public void add(GroupALManaged g) {
        groups.add(g);
    }

    public void remove(GroupALManaged g) {
        groups.remove(g);
    }

    public ArrayList getGroups() {
        return groups;
    }

    /**
     * Get list of groups as a space separated String.
     */
    public String getGroupsString() {
        ArrayList a = new ArrayList(groups);
        Collections.sort(a);
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < a.size(); i++) {
            if (i > 0) s.append(' ');
            s.append(a.get(i));
        }
        return s.toString();
    }

    public String toString() {
        return name;
    }

}
