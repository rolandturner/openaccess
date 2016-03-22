
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
public class Child {

    private int childID;
    private String name;

    public Child() {
    }

    public int getChildID() {
        return childID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ID implements Serializable {

        public int childID;

        public ID(String s) {
            childID = Integer.parseInt(s);
        }

        public ID() {
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (childID != id.childID) return false;

            return true;
        }

        public int hashCode() {
            return childID;
        }

        public String toString() {
            return Integer.toString(childID);
        }

    }

}
