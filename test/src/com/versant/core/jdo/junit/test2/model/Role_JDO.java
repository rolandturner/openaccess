
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
package com.versant.core.jdo.junit.test2.model;

import java.util.Date;
import java.io.Serializable;

/**
 */
public class Role_JDO {
    private int roleId;
    private String name;
    private String description;
    private Date lastUpdateTimestamp;

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public static class ID implements Serializable {
        public int roleId;

        public ID(int roleId) {
            this.roleId = roleId;
        }

        public ID(String id) {
            this.roleId = Integer.parseInt(id);
        }

        public ID() {
        }

        public String toString() {
            return "" + roleId;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (roleId != id.roleId) return false;

            return true;
        }

        public int hashCode() {
            return roleId;
        }
    }
}
