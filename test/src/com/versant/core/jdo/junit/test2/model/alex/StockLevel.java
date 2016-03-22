
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
package com.versant.core.jdo.junit.test2.model.alex;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * @keep-all
 */
public class StockLevel {

    private short compno;
    private String eanCode;
    private String branchNo;

    private StockIndex stockIndex;
    private AlexBranch branch;

    public StockLevel(AlexBranch branch, String eanCode, StockIndex stockIndex) {
        this.branch = branch;
        this.eanCode = eanCode;
        this.stockIndex = stockIndex;
        compno = branch.getCompno();
        branchNo = branch.getBranchNo();
    }

    public short getCompno() {
        return compno;
    }

    public String getEanCode() {
        return eanCode;
    }

    public String getBranchNo() {
        return branchNo;
    }

    public StockIndex getStockIndex() {
        return stockIndex;
    }

    public AlexBranch getBranch() {
        return branch;
    }

    public static class ID implements Serializable {

        public short compno;
        public String eanCode;
        public String branchNo;

        public ID() {
        }

        public ID(String oid) {
            StringTokenizer st = new StringTokenizer(oid, "-");
            compno = Short.parseShort(st.nextToken());
            eanCode = st.nextToken();
            branchNo = st.nextToken();
        }

        public String toString() {
            return compno + "-" + eanCode + "-" + branchNo;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (compno != id.compno) return false;
            if (!branchNo.equals(id.branchNo)) return false;
            if (!eanCode.equals(id.eanCode)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int)compno;
            result = 29 * result + eanCode.hashCode();
            result = 29 * result + branchNo.hashCode();
            return result;
        }
    }
}
