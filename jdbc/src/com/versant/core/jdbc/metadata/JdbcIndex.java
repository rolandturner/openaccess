
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
package com.versant.core.jdbc.metadata;

import java.io.Serializable;

/**
 * An index for a Table.
 */
public class JdbcIndex implements Serializable {

    public String name;
    public JdbcColumn[] cols;
    public boolean unique;
    public boolean clustered;
    public String comment;

    public JdbcIndex() {
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(name);
        s.append('(');
        s.append(cols[0]);
        for (int i = 1; i < cols.length; i++) {
            s.append(',');
            s.append(' ');
            s.append(cols[i]);
        }
        s.append(')');
        if (unique) s.append(" unique");
        if (clustered) s.append(" clustered");
        if (cols[0].table != null) s.append(" to table " + cols[0].table.name);
        return s.toString();
    }

    /**
     * Set the columns for this index and set the partOfIndex flag on each
     * column.
     */
    public void setCols(JdbcColumn[] cols) {
        this.cols = cols;
        for (int i = cols.length - 1; i >= 0; i--) {
            cols[i].partOfIndex = true;
        }
    }

    /**
     * Do we have the same columns as x
     */
    public boolean hasSameColumns(JdbcIndex x) {
        int n = cols.length;
        if (n != x.cols.length) return false;
        for (int i = 0; i < n; i++) {
            if (!cols[i].equals(x.cols[i])) return false;
        }
        return true;
    }

    /**
     * Indexes are equal if they have the same columns.
     */
    public boolean equals(Object o) {
        return o instanceof JdbcIndex && hasSameColumns((JdbcIndex)o);
    }

    /**
     * Hashcode depends on columns.
     */
    public int hashCode() {
        int ans = 0;
        for (int i = cols.length - 1; i >= 0; i--) {
            if (cols[i].name != null) {
                ans += cols[i].name.hashCode() * 29;
            }
        }
        return ans;
    }

}


