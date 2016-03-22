
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
 * For advanced testing of column sharing and composite pks.
 * @see com.versant.core.jdo.test.TestApplicationPK2#testCompositePkRefQueryNormal
 * @keep-all
 */
public class TriRef {

    private int pka;
    private int pkb;
    private int pkc;
    private String name;
    private TriRef ref;                 // no columns shared
    private TriRef refMiddle;           // pkb shared
    private TriRef refLast;             // pkc shared
    private TriRef refMiddleLast;       // pkb and pkc shared
    private TriRef refFirstMiddle;      // pka and pkb shared
    private TriRef refAllShared;        // shares all columns with refNormal

    public TriRef(int pka, int pkb, int pkc, String name) {
        this.pka = pka;
        this.pkb = pkb;
        this.pkc = pkc;
        this.name = name;
    }

    public int getPka() {
        return pka;
    }

    public int getPkb() {
        return pkb;
    }

    public int getPkc() {
        return pkc;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public TriRef getRef() {
        return ref;
    }

    public void setRef(TriRef ref) {
        this.ref = ref;
        refAllShared = ref;
    }

    public TriRef getRefMiddle() {
        return refMiddle;
    }

    public void setRefMiddle(TriRef refMiddle) {
        this.refMiddle = refMiddle;
    }

    public TriRef getRefLast() {
        return refLast;
    }

    public void setRefLast(TriRef refLast) {
        this.refLast = refLast;
    }

    public TriRef getRefMiddleLast() {
        return refMiddleLast;
    }

    public void setRefMiddleLast(TriRef refMiddleLast) {
        this.refMiddleLast = refMiddleLast;
    }

    public TriRef getRefFirstMiddle() {
        return refFirstMiddle;
    }

    public void setRefFirstMiddle(TriRef refFirstMiddle) {
        this.refFirstMiddle = refFirstMiddle;
    }

    public void setRefAllShared(TriRef refAllShared) {
        setRef(refAllShared);
    }

    public TriRef getRefAllShared() {
        return refAllShared;
    }

    public static class ID implements Serializable {

        public int pka;
        public int pkb;
        public int pkc;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            int j = s.lastIndexOf(',');
            pka = Integer.parseInt(s.substring(0, i));
            pkb = Integer.parseInt(s.substring(i + 1, j));
            pkc = Integer.parseInt(s.substring(j + 1));
        }

        public boolean equals(Object o) {
            if (!(o instanceof ID)) return false;

            ID id = (ID)o;

            if (pka != id.pka) return false;
            if (pkb != id.pkb) return false;
            if (pkc != id.pkc) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = pka;
            result = 29 * result + pkb;
            result = 29 * result + pkc;
            return result;
        }

        public String toString() {
            return pka + "," + pkb + "," + pkc;
        }

    }

}
