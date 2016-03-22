
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

import java.util.ArrayList;

/**
 * One side of unmanaged one-to-many. This code does not fix up the
 * back reference on OrderLineFakeAId as the tests need to check that this is
 * not done.
 */
public class OrderFakeAId {

    private String orderNo;
    private int customerId;
    private String name;
    private ArrayList lines = new ArrayList(); // inverse OrderLineFakeAId.order

    public OrderFakeAId(String orderNo, int customerId, String name) {
        this.orderNo = orderNo;
        this.customerId = customerId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList getLines() {
        return lines;
    }

    public void setLines(ArrayList lines) {
        this.lines = lines;
    }

    public void add(OrderLineFakeAId ol) {
        lines.add(ol);
    }

    public void remove(OrderLineFakeAId ol) {
        lines.remove(ol);
    }

    /**
     * Get list of OrderLines as a space separated String.
     */
    public String getLinesString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) s.append(' ');
            s.append(lines.get(i));
        }
        return s.toString();
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements java.io.Serializable {

        public String orderNo;
        public int customerId;

        public ID() {
        }

        public ID(String s) {
            int i, p = 0;
            i = s.indexOf('-', p);
            orderNo = s.substring(p, i);
            p = i + 1;
            customerId = Integer.parseInt(s.substring(p));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrderFakeAId.ID)) return false;

            final OrderFakeAId.ID id = (OrderFakeAId.ID)o;

            if (this.orderNo != null ? !orderNo.equals(id.orderNo) : id.orderNo != null) return false;
            if (this.customerId != id.customerId) return false;
            return true;
        }

        public int hashCode() {
            int result = 0;
            result = 29 * result + (orderNo != null ? orderNo.hashCode() : 0);
            result = 29 * result + (int)customerId;
            return result;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(orderNo);
            buffer.append('-');
            buffer.append(customerId);
            return buffer.toString();
        }
    }
}

