
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

import java.io.Serializable;

/**
 * For testing application identity with references as part of the PK.
 * @keep-all
 */
public class Customer {

    private int branchNo = 0;   // pk
    private int customerNo;     // pk
    private Branch branch;
    private String name;

    public Customer(Branch branch, int customerNo, String name) {
        this.branch = branch;
        branchNo = branch.getBranchNo();
        this.customerNo = customerNo;
        this.name = name;
    }

    public int getBranchNo() {
        return branchNo;
    }

    public int getCustomerNo() {
        return customerNo;
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
        return branchNo + "/" + branch + "/" + customerNo + "/" + name;
    }

    public static class ID implements Serializable {

        public int branchNo;
        public int customerNo;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            branchNo = Integer.parseInt(s.substring(0, i));
            customerNo = Integer.parseInt(s.substring(i + 1));
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (branchNo != id.branchNo) return false;
            if (customerNo != id.customerNo) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = branchNo;
            result = 29 * result + customerNo;
            return result;
        }

        public String toString() {
            return branchNo + "," + customerNo;
        }

    }

}
