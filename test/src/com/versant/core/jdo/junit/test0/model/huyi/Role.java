
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
package com.versant.core.jdo.junit.test0.model.huyi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 */
public class Role  implements Serializable {
    final static long serialVersionUID = 20040910l;
    private String roleName;
    private HashMap priviledgeMap = new HashMap();
    private String description;

    public String getRoleName() {
        return roleName;
    }

    public Role() {
        super();
    }
    public Role(String roleName) {
        this.roleName = roleName;
    }
    public static Role newInstance(String roleName) {
        Role role = new Role(roleName);
        return role;
    }
    public void addPriviledge(SystemPriviledge priviledge) {
        priviledgeMap.put(priviledge.getName(), priviledge);
    }
    public static class PrimaryKey
            implements Serializable {
        public String roleName;

        public PrimaryKey() {
        }
        public PrimaryKey(String value) {
            StringTokenizer token = new StringTokenizer(value, "::");
            token.nextToken(); // className
            roleName = token.nextToken(); // roleName
        }
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (! (object instanceof PrimaryKey)) {
                return false;
            }
            PrimaryKey other = (PrimaryKey) object;
            return roleName.equals(other.roleName);
        }
        public int hashCode() {
            return roleName.hashCode();
        }
        public String toString() {
            return this.getClass().getName() + "::" + this.roleName;
        }
    }
}
