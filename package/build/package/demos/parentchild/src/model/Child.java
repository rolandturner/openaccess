
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
package model;

import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

/** 
 * Child class.
 */
public class Child {

    private int ida;
    private int idb;
    private int idc;

    private ParentA parentA; // mapped to ida, idb
    private ParentB parentB; // mapped to ida, idb
    private String name;

    public Child(Parent parent, int idc, String name) {
        if (parent instanceof ParentA) parentA = (ParentA)parent;
        else parentB = (ParentB)parent;
        ida = parent.getIda();
        idb = parent.getIdb();
        this.idc = idc;
        this.name = name;
    }

    public int getIda() {
        return ida;
    }

    public int getIdb() {
        return idb;
    }

    public int getIdc() {
        return idc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Child objectid-class.
     */
    public static class ID implements Serializable {

        public int ida;
        public int idb;
        public int idc;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            int j = s.indexOf('-', i + 1);
            ida = Integer.parseInt(s.substring(0, i));
            idb = Integer.parseInt(s.substring(i + 1, j));
            idc = Integer.parseInt(s.substring(j + 1));
        }

        public String toString() {
            return ida + "-" + idb + "-" + idc;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (ida != id.ida) return false;
            if (idb != id.idb) return false;
            if (idc != id.idc) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = ida;
            result = 29 * result + idb;
            result = 29 * result + idc;
            return result;
        }

    }

}
