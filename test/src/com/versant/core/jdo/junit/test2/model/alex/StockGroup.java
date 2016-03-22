
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
public class StockGroup {

    private short compno;
    private String groupCode;
    private String groupName;

    public StockGroup(short compno, String groupCode, String groupName) {
        this.compno = compno;
        this.groupCode = groupCode;
        this.groupName = groupName;
    }

    public short getCompno() {
        return compno;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public static class ID implements Serializable {

        public short compno;
        public String groupCode;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            compno = Short.parseShort(s.substring(0, i));
            groupCode = s.substring(i + 1);
        }

        public String toString() {
            return compno + "-" + groupCode;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (compno != id.compno) return false;
            if (!groupCode.equals(id.groupCode)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int)compno;
            result = 29 * result + groupCode.hashCode();
            return result;
        }
    }

}
