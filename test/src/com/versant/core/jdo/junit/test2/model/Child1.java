
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
public class Child1 {
    private int id;
    private Parent1 parent1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Parent1 getParent1() {
        return parent1;
    }

    public void setParent1(Parent1 parent1) {
        this.parent1 = parent1;
    }

    public static class ID implements Serializable {
        public int id;

        public ID() {
        }

        public ID(String idNo) {
            this.id = Integer.parseInt(idNo);
        }

        public int hashCode() {
            return id;
        }

        public boolean equals(Object obj) {
            if (obj instanceof ID) {
                ID other = (ID)obj;
                if (other.id == id) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return String.valueOf(id);
        }
    }
}
