
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

/**
 * @keep-all
 */
public class Style {

    private short compno;
    private String skuCode;
    private String shortCode;
    private boolean dumpFlag;

    public Style(short compno, String skuCode, String shortCode) {
        this.compno = compno;
        this.skuCode = skuCode;
        this.shortCode = shortCode;
    }

    public short getCompno() {
        return compno;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getShortCode() {
        return shortCode;
    }

    public boolean isDumpFlag() {
        return dumpFlag;
    }

    public void setDumpFlag(boolean dumpFlag) {
        this.dumpFlag = dumpFlag;
    }

    public static class ID implements Serializable {

        public short compno;
        public String skuCode;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            compno = Short.parseShort(s.substring(0, i));
            skuCode = s.substring(i + 1);
        }

        public String toString() {
            return compno + "-" + skuCode;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (compno != id.compno) return false;
            if (!skuCode.equals(id.skuCode)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int)compno;
            result = 29 * result + skuCode.hashCode();
            return result;
        }
    }

}
