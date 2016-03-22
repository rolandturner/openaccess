
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

import com.versant.core.jdo.junit.test2.model.refpk.Order;

import java.io.Serializable;

/**
 * For testing application identity with references as part of the PK.
 * @see com.versant.core.jdo.test.TestApplicationPK2#testAppIdWithReferenceField
 * @keep-all
 */
public class OrderLine {

    private int branchNo;   // pk
    private int orderNo;    // pk
    private int lineNo;     // pk
    private Branch branch;
    private Order order;
    private Item item;
    private int qty;

    public OrderLine(Order order, int lineNo, Item item, int qty) {
        this.order = order;
        orderNo = order.getOrderNo();
        this.lineNo = lineNo;
        branch = order.getBranch();
        branchNo = branch.getBranchNo();
        this.item = item;
        this.qty = qty;
    }

    public int getBranchNo() {
        return branchNo;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public int getLineNo() {
        return lineNo;
    }

    public Branch getBranch() {
        return branch;
    }

    public Order getOrder() {
        return order;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String toString() {
        return branchNo + "/" + branch + "/" + orderNo + "/" +
                lineNo + " item " + item + " x " + qty;
    }

    public static class ID implements Serializable {

        public int branchNo;
        public int orderNo;
        public int lineNo;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            int j = s.lastIndexOf(',');
            branchNo = Integer.parseInt(s.substring(0, i));
            orderNo = Integer.parseInt(s.substring(i + 1, j));
            lineNo = Integer.parseInt(s.substring(j + 1));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (branchNo != id.branchNo) return false;
            if (lineNo != id.lineNo) return false;
            if (orderNo != id.orderNo) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = branchNo;
            result = 29 * result + orderNo;
            result = 29 * result + lineNo;
            return result;
        }

        public String toString() {
            return branchNo + "," + orderNo + "," + lineNo;
        }

    }

}
