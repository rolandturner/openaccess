
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
public class AlexBranch {

    private short compno;
    private String branchNo;
    private String shortName;

    public AlexBranch(short compno, String branchNo, String shortName) {
        this.compno = compno;
        this.branchNo = branchNo;
        this.shortName = shortName;
    }

    public short getCompno() {
        return compno;
    }

    public String getBranchNo() {
        return branchNo;
    }

    public String getShortName() {
        return shortName;
    }

    public static class ID implements Serializable {

        public short compno;
        public String branchNo;

        public ID() {
        }

        public ID(String s) {
            int i = s.indexOf('-');
            compno = Short.parseShort(s.substring(0, i));
            branchNo = s.substring(i + 1);
        }

        public String toString() {
            return compno + "-" + branchNo;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID)o;

            if (compno != id.compno) return false;
            if (!branchNo.equals(id.branchNo)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (int)compno;
            result = 29 * result + branchNo.hashCode();
            return result;
        }
    }

}
