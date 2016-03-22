
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
package com.versant.core.jdo.junit.testHorizontal.model.horizontal;

import java.io.Serializable;

/**
 */
public class AppIdSub1 extends AppIdBase {
    private String val1;

    public String getVal1() {
        return val1;
    }

    public void setVal1(String val1) {
        this.val1 = val1;
    }

    public static class PK implements Serializable {
        public int pk;

        public PK(String pk) {
            this.pk = Integer.parseInt(pk);
        }

        public PK() {
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;

            final PK pk1 = (PK) o;

            if (pk != pk1.pk) return false;

            return true;
        }

        public int hashCode() {
            return pk;
        }

        public String toString() {
            return "" + pk;
        }
    }
}
