
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

import com.versant.core.jdbc.sql.JdbcNameGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

/**
 * A database foreign key constraint on a table.
 *
 * @keep-public
 */
public final class JdbcConstraint implements Serializable {

    public String name;
    /**
     * The table containing the foreign key column.
     */
    public JdbcTable src;
    /**
     * The column(s) in src making up the foreign key. These must match
     * the pk of dest (same order etc.).
     */
    public JdbcColumn[] srcCols;
    /**
     * The table the foreign key references.
     */
    public JdbcTable dest;

    public JdbcConstraint() {
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(name);
        s.append('(');
        s.append(JdbcColumn.toNameString(srcCols));
        s.append(") references ");
        s.append(dest.name);
        s.append('(');
        s.append(JdbcColumn.toNameString(dest.pk));
        s.append(')');
        return s.toString();
    }

    /**
     * Generate a name for this constraint using namegen.
     */
    public void generateName(JdbcNameGenerator namegen) {
        name = namegen.generateRefConstraintName(src.name, dest.name, JdbcColumn.getColumnNames(
                srcCols),
                dest.getPkNames());
    }

    public List getColumnList() {
        if (srcCols == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList list = new ArrayList(srcCols.length);
        for (int i = 0; i < srcCols.length; i++) {
            JdbcColumn col = srcCols[i];
            list.add(col);
        }
        return list;
    }

    public boolean isSameConstraint(JdbcConstraint constraint) {
        return false;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JdbcConstraint)) return false;

        final JdbcConstraint jdbcConstraint = (JdbcConstraint)o;

        if (dest != null ? !dest.equals(jdbcConstraint.dest) : jdbcConstraint.dest != null) return false;
        if (src != null ? !src.equals(jdbcConstraint.src) : jdbcConstraint.src != null) return false;
        if (!Arrays.equals(srcCols, jdbcConstraint.srcCols)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (src != null ? src.hashCode() : 0);
        if (srcCols != null) {
            for (int i = 0; i < srcCols.length; i++) {
                JdbcColumn srcCol = srcCols[i];
                result = 29 * result + (srcCol != null ? srcCol.hashCode() : 0);
            }
        }
        result = 29 * result + (dest != null ? dest.hashCode() : 0);
        return result;
    }
}


