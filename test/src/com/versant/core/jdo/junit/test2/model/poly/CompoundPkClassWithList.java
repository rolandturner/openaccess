
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
package com.versant.core.jdo.junit.test2.model.poly;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * For testing compound pk polyrefs with lists.
 * @keep-all
 */
public class CompoundPkClassWithList {

    private int pkA;
    private int pkB;
    private String name;
    private List list = new ArrayList(); // of Contact

    public CompoundPkClassWithList(int pkA, int pkB, String name) {
        this.pkA = pkA;
        this.pkB = pkB;
        this.name = name;
    }

    public int getPkA() {
        return pkA;
    }

    public int getPkB() {
        return pkB;
    }

    public String getName() {
        return name;
    }

    public List getList() {
        return list;
    }

    public static class ID implements Serializable {

        public int pkA;
        public int pkB;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            pkA = Integer.parseInt(s.substring(0, i));
            pkB = Integer.parseInt(s.substring(i + 1));
        }

        public String toString() {
            return pkA + "-" + pkB;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (pkA != id.pkA) return false;
            if (pkB != id.pkB) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = pkA;
            result = 29 * result + pkB;
            return result;
        }

    }

}
