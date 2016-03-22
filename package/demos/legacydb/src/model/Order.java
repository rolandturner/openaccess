
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
package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;

/**
 * An Order placed with a Supplier.
 */
public class Order {

    private int branchNo;   // pk
    private int orderNo;    // pk
    private Branch branch;
    private Supplier supplier;
    private Date orderDate = new Date();
    private List lines = new ArrayList(); // inverse OrderLine.order
    private int lastLineNo;

    public Order(Branch branch) {
        this.branch = branch;
        branchNo = branch.getBranchNo();
    }

    public Order(Branch branch, int orderNo) {
        this(branch);
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

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public List getLines() {
        return new ArrayList(lines);
    }

    public void addOrderLine(Item item, int qty) {
        OrderLine line = new OrderLine(this, ++lastLineNo, item, qty);
        lines.add(line);
    }

    public void removeOrderLine(OrderLine line) {
        if(lines.remove(line)){
            if (line.getOrder() == this) line.setOrder(null);
        }
    }

    public String toString() {
        return Integer.toString(orderNo);
    }

    /**
     * Application identity objectid-class.
     */
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

