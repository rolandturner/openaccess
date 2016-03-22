
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
package com.versant.core.jdo.junit.test3.model.attachdetach;

import java.io.Serializable;

public class PolyRefParent implements Comparable, Serializable {

    private int no;
    private SimpleFieldsBase simpleFields;

    public PolyRefParent() {
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public SimpleFieldsBase getSimpleFields() {
        return simpleFields;
    }

    public void setSimpleFields(SimpleFieldsBase simpleFields) {
        this.simpleFields = simpleFields;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolyRefParent)) return false;

        final PolyRefParent ad_c = (PolyRefParent)o;

        if (no != ad_c.no) return false;
        if (simpleFields != null ? !simpleFields.equals(ad_c.simpleFields) : ad_c.simpleFields != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = no;
        result = 29 * result + (simpleFields != null ? simpleFields.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Ad_C{" +
                "no=" + no +
                ", simpleFields=" + simpleFields +
                "}";
    }

    public int compareTo(Object o) {
        if (this == o) return 0;
        if (!(o instanceof PolyRefParent)) return -1;

        final PolyRefParent ad_c = (PolyRefParent)o;

        return no - ad_c.no;
    }

    public Object clone() throws CloneNotSupportedException {
        PolyRefParent c = new PolyRefParent();
        c.no = no;
        c.simpleFields = simpleFields;
        return c;
    }
}
