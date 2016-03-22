
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

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.Serializable;

/**
 * @keep-all
 */
public class StockIndex {

    private short compno;
    private String eanCode;
    private Style style;
    private StockGroup stockGroup;

    private List stockLevelList = new ArrayList(); // StockLevel.stockIndex

    public StockIndex(short compno, String eanCode, Style style, StockGroup stockGroup) {
        this.compno = compno;
        this.eanCode = eanCode;
        this.style = style;
        this.stockGroup = stockGroup;
    }

    public StockLevel createStockLevel(AlexBranch branch) {
        StockLevel ans = new StockLevel(branch, eanCode, this);
        stockLevelList.add(ans);
        return ans;
    }

    public short getCompno() {
        return compno;
    }

    public String getEanCode() {
        return eanCode;
    }

    public Style getStyle() {
        return style;
    }

    public StockGroup getStockGroup() {
        return stockGroup;
    }

    public List getStockLevelList() {
        return stockLevelList;
    }

    public static class ID implements Serializable {

        public short compno;
        public String eanCode;

        public ID() {
        }

        public ID(String oid) {
            StringTokenizer st = new StringTokenizer(oid, "-");
            compno = Short.parseShort(st.nextToken());
            eanCode = st.nextToken();
        }

        public String toString() {
            return compno + "-" + eanCode;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (compno != id.compno) return false;
            if (!eanCode.equals(id.eanCode)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int)compno;
            result = 29 * result + eanCode.hashCode();
            return result;
        }
    }
}
