
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
package com.versant.core.jdo.junit.test0.model;

import java.util.Locale;

/**
 */
public class ComplexAppIdPK implements java.io.Serializable {

    public String sID;
    public Locale lID;
    public boolean bID;
    public boolean cID;

    public ComplexAppIdPK() {
    }

    public String toString() {
        return sID + "|" + lID + "|" + bID + "|" + cID;
    }

    private Locale parseLocale(String s) {
        String lang, country, variant;
        int i = s.indexOf('_');
        if (i < 0) {
            lang = s;
            country = variant = "";
        } else {
            lang = s.substring(0, i);
            int j = s.indexOf('_', i + 1);
            if (j < 0) {
                country = s.substring(i + 1);
                variant = "";
            } else {
                country = s.substring(i + 1, j);
                variant = s.substring(j + 1);
            }
        }
        return new Locale(lang, country, variant);
    }

    public ComplexAppIdPK(String s) {
        int i = s.indexOf('|');
        int j = s.indexOf('|', i + 1);
        int k = s.indexOf('|', j + 1);
        sID = s.substring(0, i);
        lID = parseLocale(s.substring(i + 1, j));
        bID = "true".equals(s.substring(j + 1, k));
        cID = "true".equals(s.substring(k + 1));
    }

    public int hashCode() {
        return sID.hashCode() + lID.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplexAppIdPK)) return false;
        ComplexAppIdPK other = (ComplexAppIdPK)o;
        if (!other.sID.equals(sID)) return false;
        if (!other.lID.equals(lID)) return false;
        if (other.bID != bID) return false;
        if (other.cID != cID) return false;
        return true;
    }

}
