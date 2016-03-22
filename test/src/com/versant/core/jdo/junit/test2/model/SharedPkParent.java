
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
 */
public class SharedPkParent {
    private int id1;
    private String val;
    private SharedPkChild child;

    public SharedPkParent(int id1, String val) {
        this.id1 = id1;
        this.val = val;
    }

    public int getId1() {
        return id1;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public SharedPkChild getChild() {
        return child;
    }

    public void setChild(SharedPkChild child) {
        this.child = child;
    }

    public static class Id implements Serializable {
        public int id1;

        public Id() {
        }

        public Id(String val) {
            id1 = Integer.parseInt(val);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id)) return false;

            final Id id = (Id)o;

            if (id1 != id.id1) return false;

            return true;
        }

        public int hashCode() {
            return id1;
        }

        public String toString() {
            return "" + id1;
        }

    }

}
