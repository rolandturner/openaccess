
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
import java.util.StringTokenizer;

/**
 * For testing application identity with a non-primitive PK.
 *
 * @keep-all
 */
public class CompositeAppIdString {

    private short idNo;
    private String id1;
    private String name;
    private CompositeAppIdString friend;

    public CompositeAppIdString() {
    }

    public CompositeAppIdString(short idNo, String id1, String name) {
        this.idNo = idNo;
        this.id1 = id1;
        this.name = name;
    }

    public CompositeAppIdString getFriend() {
        return friend;
    }

    public void setFriend(CompositeAppIdString friend) {
        this.friend = friend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ID implements Serializable {

        public short idNo;
        public String id1;

        public ID() {
        }

        public ID(String id) {
            StringTokenizer st = new StringTokenizer(id, "|");
            if (st.hasMoreTokens()) {
                idNo = Short.parseShort(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                id1 = st.nextToken();
            }
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (idNo != id.idNo) return false;
            if (!id1.equals(id.id1)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int)idNo;
            result = 29 * result + id1.hashCode();
            return result;
        }

        public String toString() {
            return "" + idNo + "|" + id1;
        }

    }

}

