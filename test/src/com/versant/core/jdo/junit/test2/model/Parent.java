
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

import java.io.Serializable;

/** 
 * For testing Parent-Child zero ref problem (forums 609).
 * @keep-all
 */
public class Parent {

    private int parentID;
    private Child child;
    private String name;

    public Parent() {
    }

    public int getParentID() {
        return parentID;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ID implements Serializable {

        public int parentID;

        public ID(String s) {
            parentID = Integer.parseInt(s);
        }

        public ID() {
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (parentID != id.parentID) return false;

            return true;
        }

        public int hashCode() {
            return parentID;
        }

        public String toString() {
            return Integer.toString(parentID);
        }

    }

}
