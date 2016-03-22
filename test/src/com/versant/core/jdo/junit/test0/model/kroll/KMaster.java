
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
package com.versant.core.jdo.junit.test0.model.kroll;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 */
public class KMaster {
    private int id1;
    private int id2;
    private long lVal;

    public KMaster(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    private String val;
    private List details = new ArrayList();

    public int getId1() {
        return id1;
    }

    public int getId2() {
        return id2;
    }

    public long getlVal() {
        return lVal;
    }

    public void setlVal(long lVal) {
        this.lVal = lVal;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public List getDetails() {
        return details;
    }

    public void setDetails(List details) {
        this.details = details;
    }

    public static class PK implements Serializable {
        public int id1;
        public int id2;

        public PK() {
        }

        public PK(String id) {
            int index = id.indexOf("-");
            id1 = Integer.parseInt(id.substring(0, index));
            id2 = Integer.parseInt(id.substring(index + 1));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PK)) return false;

            final PK pk = (PK) o;

            if (id1 != pk.id1) return false;
            if (id2 != pk.id2) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = id1;
            result = 29 * result + id2;
            return result;
        }

        public String toString() {
            return "" + id1 + "-" + id2;
        }

    }
}
