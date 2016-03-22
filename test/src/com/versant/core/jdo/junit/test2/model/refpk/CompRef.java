
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
package com.versant.core.jdo.junit.test2.model.refpk;

import java.io.Serializable;

/**
 * @see com.versant.core.jdo.test.TestApplicationPK2#testCompositePkRefQuerySimple
 * @keep-all
 */
public class CompRef {

    private int pka;
    private int pkb;
    private String name;
    private CompRef ref;

    public CompRef(int pka, int pkb, String name, CompRef ref) {
        this.pka = pka;
        this.pkb = pkb;
        this.name = name;
        this.ref = ref;
    }

    public int getPka() {
        return pka;
    }

    public int getPkb() {
        return pkb;
    }

    public String getName() {
        return name;
    }

    public CompRef getRef() {
        return ref;
    }

    public String toString() {
        return pka + "/" + pkb + "/" + name + "/" + ref;
    }

    public static class ID implements Serializable {

        public int pka;
        public int pkb;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            pka = Integer.parseInt(s.substring(0, i));
            pkb = Integer.parseInt(s.substring(i + 1));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (pka != id.pka) return false;
            if (pkb != id.pkb) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = pka;
            result = 29 * result + pkb;
            return result;
        }

        public String toString() {
            return pka + "," + pkb;
        }

    }

}
