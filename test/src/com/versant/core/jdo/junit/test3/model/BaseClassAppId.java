
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
package com.versant.core.jdo.junit.test3.model;

import java.io.Serializable;

/**
 * For testing indexing of columns in a hierarchy.
 *
 * @keep-all
 */
public class BaseClassAppId {

    private int pka; // pk
    private int pkb; // pk
    private String name;

    public BaseClassAppId(int pka, int pkb, String name) {
        this.pka = pka;
        this.pkb = pkb;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPka() {
        return pka;
    }

    public int getPkb() {
        return pkb;
    }

    public static final class ID implements Serializable {

        public int pka, pkb;

        public ID(int pka, int pkb) {
            this.pka = pka;
            this.pkb = pkb;
        }

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            pka = Integer.parseInt(s.substring(0, i));
            pkb = Integer.parseInt(s.substring(i + 1));
        }

        public boolean equals(Object o) {
            if (!(o instanceof ID)) return false;

            ID id = (ID)o;

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
            return pka + "-" + pkb;
        }

    }

}

