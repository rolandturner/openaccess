
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

import com.versant.core.jdo.junit.test2.model.refpk.Branch;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;

/**
 * For testing application identity with references as part of the PK.
 * @see com.versant.core.jdo.test.TestApplicationPK2#testAppIdWithReferenceField
 * @keep-all
 */
public class Order {

    private int branchNo;   // pk
    private int orderNo;    // pk
    private Branch branch;
    private Customer customer;
    private Date orderDate = new Date();
    private List lines = new ArrayList(); // of OrderLine

    public Order(Branch branch, int orderNo) {
        this.branch = branch;
        branchNo = branch.getBranchNo();
        this.orderNo = orderNo;
    }

    public int getBranchNo() {
        return branchNo;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public Branch getBranch() {
        return branch;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public List getLines() {
        return new ArrayList(lines);
    }

    public void addOrderLine(Item item, int qty) {
        OrderLine line = new OrderLine(this, lines.size() + 1, item, qty);
        lines.add(line);
    }

    public void removeOrderLine(OrderLine line) {
        lines.remove(line);
    }

    public String toString() {
        return branchNo + "/" + branch +"/" + orderNo + " customer " + customer + " lines " + lines;
    }

    public static class ID implements Serializable {

        public int branchNo;
        public int orderNo;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            branchNo = Integer.parseInt(s.substring(0, i));
            orderNo = Integer.parseInt(s.substring(i + 1));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (branchNo != id.branchNo) return false;
            if (orderNo != id.orderNo) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = branchNo;
            result = 29 * result + orderNo;
            return result;
        }

        public String toString() {
            return branchNo + "," + orderNo;
        }

    }

}
