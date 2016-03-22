
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
package com.versant.core.jdo.junit.test2.model.fake;

/**
 * Many side of unmanaged one-to-many.
 */
public class OrderLineFakeAId {

    public int lineNo;
    private int qty;

    public OrderLineFakeAId(int lineNo, int qty) {
        this.lineNo = lineNo;
        this.qty = qty;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String toString() {
        return Integer.toString(qty);
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements java.io.Serializable {

        public int lineNo;

        public ID() {
        }

        public ID(String s) {
            lineNo = Integer.parseInt(s);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrderFakeAId.ID)) return false;
            final OrderLineFakeAId.ID id = (OrderLineFakeAId.ID)o;
            return this.lineNo == id.lineNo;
        }

        public int hashCode() {
            return lineNo;
        }

        public String toString() {
            return String.valueOf(lineNo);
        }
    }
}

