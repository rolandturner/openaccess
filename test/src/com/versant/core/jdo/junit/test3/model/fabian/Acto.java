
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
public class Acto {

    private String pkActo;
    private String pkLic;
    private String pkSol;
    private int blah;
    private Liquid parent;

    public Acto(String pkActo, String pkLic, String pkSol, int blah) {
        this.pkActo = pkActo;
        this.pkLic = pkLic;
        this.pkSol = pkSol;
        this.blah = blah;
    }

    public Acto() {
    }

    public String getPkActo() {
        return pkActo;
    }

    public String getPkLic() {
        return pkLic;
    }

    public String getPkSol() {
        return pkSol;
    }

    public int getBlah() {
        return blah;
    }

    public void setBlah(int blah) {
        this.blah = blah;
    }

    public Liquid getParent() {
        return parent;
    }

    public void setParent(Liquid parent) {
        this.parent = parent;
    }

    public static class ID implements Serializable {

        public String pkActo;
        public String pkLic;
        public String pkSol;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            int j = s.indexOf('-', i + 1);
            pkActo = s.substring(0, i);
            pkLic = s.substring(i + 1, j);
            pkSol = s.substring(j + 1);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (!pkActo.equals(id.pkActo)) return false;
            if (!pkLic.equals(id.pkLic)) return false;
            if (!pkSol.equals(id.pkSol)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = pkActo.hashCode();
            result = 29 * result + pkLic.hashCode();
            result = 29 * result + pkSol.hashCode();
            return result;
        }

        public String toString() {
            return pkActo + "-" + pkLic + "-" + pkSol;
        }

    }

}

