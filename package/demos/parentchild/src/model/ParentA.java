
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
 * Parent class.
 */
public class ParentA implements Parent {

    private int ida;
    private int idb;

    private String name;
    private Set children = new HashSet(); // inverse Child.parent

    public ParentA(int ida, int idb, String name) {
        this.ida = ida;
        this.idb = idb;
        this.name = name;
    }

    public int getIda() {
        return ida;
    }

    public int getIdb() {
        return idb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getChildren() {
        return children;
    }

    public Child newChild(int idc, String name) {
        Child c = new Child(this, idc, name);
        children.add(c);
        return c;
    }

    /**
     * Parent objectid-class.
     */
    public static class ID implements Serializable {

        public int ida;
        public int idb;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            ida = Integer.parseInt(s.substring(0, i));
            idb = Integer.parseInt(s.substring(i + 1));
        }

        public String toString() {
            return ida + "-" + idb;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (ida != id.ida) return false;
            if (idb != id.idb) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = ida;
            result = 29 * result + idb;
            return result;
        }

    }

}


