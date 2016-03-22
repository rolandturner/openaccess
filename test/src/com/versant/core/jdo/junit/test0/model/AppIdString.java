
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

import java.io.Serializable;

/**
 * For testing application identity with a non-primitive PK.
 *
 * @keep-all
 */
public class AppIdString {

    private String id;
    private String name;
    private AppIdString friend;

    public AppIdString() {
    }

    public AppIdString(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public AppIdString getFriend() {
        return friend;
    }

    public void setFriend(AppIdString friend) {
        this.friend = friend;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ID implements Serializable {

        public String id;

        public ID() {
        }

        public ID(String id) {
            this.id = id;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id1 = (ID)o;

            if (!id.equals(id1.id)) return false;

            return true;
        }

        public int hashCode() {
            return id.hashCode();
        }

        public String toString() {
            return id;
        }

    }

}

