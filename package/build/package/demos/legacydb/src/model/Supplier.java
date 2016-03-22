
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

import java.io.Serializable;

/**
 * A supplier for a Branch.
 */
public class Supplier {

    private int branchNo = 0;   // pk
    private int supplierNo;     // pk
    private Branch branch;
    private String name;

    public Supplier(Branch branch, int supplierNo, String name) {
        this.branch = branch;
        branchNo = branch.getBranchNo();
        this.supplierNo = supplierNo;
        this.name = name;
    }

    public int getBranchNo() {
        return branchNo;
    }

    public int getSupplierNo() {
        return supplierNo;
    }

    public Branch getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return supplierNo + " " + name;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements Serializable {

        public int branchNo;
        public int supplierNo;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            branchNo = Integer.parseInt(s.substring(0, i));
            supplierNo = Integer.parseInt(s.substring(i + 1));
        }

        public ID(Branch branch, int supplierNo) {
            this.branchNo = branch.getBranchNo();
            this.supplierNo = supplierNo;
        }

        public ID(Branch branch, String supplierNo) {
            this(branch, Integer.parseInt(supplierNo));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (branchNo != id.branchNo) return false;
            if (supplierNo != id.supplierNo) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = branchNo;
            result = 29 * result + supplierNo;
            return result;
        }

        public String toString() {
            return branchNo + "," + supplierNo;
        }

    }

}

