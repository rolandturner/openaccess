
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
 * A stock item for a Branch.
 */
public class Item {

    private int branchNo;       // pk
    private String itemCode;    // pk
    private String description;
    private Branch branch;

    public Item(Branch branch, String itemCode, String description) {
        this.branch = branch;
        branchNo = branch.getBranchNo();
        this.itemCode = itemCode;
        this.description = description;
    }

    public int getBranchNo() {
        return branchNo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public Branch getBranch() {
        return branch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return itemCode + " " + description;
    }

    /**
     * Application identity objectid-class.
     */
    public static class ID implements Serializable {

        public int branchNo;
        public String itemCode;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf(',');
            branchNo = Integer.parseInt(s.substring(0, i));
            itemCode = s.substring(i + 1);
        }

        public ID(Branch branch, String itemCode) {
            this.branchNo = branch.getBranchNo();
            this.itemCode = itemCode;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (branchNo != id.branchNo) return false;
            if (!itemCode.equals(id.itemCode)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = branchNo;
            result = 29 * result + itemCode.hashCode();
            return result;
        }

        public String toString() {
            return branchNo + "," + itemCode;
        }
    }

}

