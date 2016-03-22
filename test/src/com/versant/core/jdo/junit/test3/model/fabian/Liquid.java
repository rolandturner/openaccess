
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
package com.versant.core.jdo.junit.test3.model.fabian;

import java.io.Serializable;

/**
 * For testing Fabian Ceballos's bug.
 */
public class Liquid {

    private String pkLic;
    private String pkSol;
    private int data;

    public Liquid(String pkLic, String pkSol, int data) {
        this.pkLic = pkLic;
        this.pkSol = pkSol;
        this.data = data;
    }

    public Liquid() {
    }

    public String getPkLic() {
        return pkLic;
    }

    public String getPkSol() {
        return pkSol;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public static class ID implements Serializable {

        public String pkLic;
        public String pkSol;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            pkLic = s.substring(0, i);
            pkSol = s.substring(i + 1);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (!pkLic.equals(id.pkLic)) return false;
            if (!pkSol.equals(id.pkSol)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = pkLic.hashCode();
            result = 29 * result + pkSol.hashCode();
            return result;
        }

        public String toString() {
            return pkLic + "-" + pkSol;
        }

    }

}

