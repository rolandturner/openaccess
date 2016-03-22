
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

import com.versant.core.common.Debug;
import com.versant.core.jdbc.sql.exp.AndExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.util.CharBuf;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;

/**
 * A table in a JDBC database.
 */
public class JdbcTable implements Serializable, Comparable {

    /**
     * Cached sql to delete row from table.
     */
    public String deleteRowSql;

    public String name;
    public SqlDriver sqlDriver;
    /**
     * All the columns making up this table in 'create table' order.
     */
    public JdbcColumn[] cols;
    /**
     * The primary key of this table. Note that these may be compound columns
     * consisting of multiple JdbcSimpleColumns.
     */
    public JdbcColumn[] pk;
    /**
     * How many simple columns make up the primary key of this table?
     */
    public int pkSimpleColumnCount;
    /**
     * The primary key of this table flattened into its JdbcColumn's.
     */
    public JdbcColumn[] pkSimpleCols;
    /**
     * The name of this tables primary key constraint.
     */
    public String pkConstraintName;
    /**
     * All the indexes.
     */
    public JdbcIndex[] indexes;
    /**
     * All the referential integrity constraints.
     */
    public JdbcConstraint[] constraints;
    /**
     * Comment info for the SQL script (e.g. class or field this table for).
     */
    public String comment;

    private JdbcColumn lockRowColumn;

    public JdbcTable() {
    }

    public List getConstraintList() {
        if (constraints == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList list = new ArrayList(constraints.length);
        for (int i = 0; i < constraints.length; i++) {
            JdbcConstraint constraint = constraints[i];
            list.add(constraint);
        }
        return list;
    }

    public List getColumnList() {
        if (cols == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList list = new ArrayList(cols.length);
        for (int i = 0; i < cols.length; i++) {
            JdbcColumn col = cols[i];
            list.add(col);
        }
        return list;
    }

    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Tables with the same name and store are equal.
     */
    public boolean equals(Object o) {
        if (o instanceof JdbcTable) {
            JdbcTable t = (JdbcTable)o;
            return name.equals(t.name);
        } else {
            return false;
        }
    }

    /**
     * Sort by name.
     */
    public int compareTo(Object o) {
        return name != null ? o != null ?
                name.compareTo(((JdbcTable)o).name) : 1 : -1;
    }

    /**
     * Format the primary key as a String for debugging.
     */
    public String formatPkString() {
        if (pk == null) return "(null)";
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < pk.length; i++) {
            if (i > 0) s.append(", ");
            s.append(pk[i]);
        }
        return s.toString();
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this + " PK " + formatPkString() + " constraint " +
                pkConstraintName);
        String is = indent + "  ";
        if (cols != null) {
            out.println(is + cols.length + " col(s)");
            for (int i = 0; i < cols.length; i++) {
                out.println(is + "[" + i + "] " + cols[i]);
            }
        }
        if (indexes != null) {
            out.println(is + indexes.length + " index(es)");
            for (int i = 0; i < indexes.length; i++) {
                out.println(is + "[" + i + "] " + indexes[i]);
            }
        }
        if (constraints != null) {
            out.println(is + constraints.length + " constraint(s)");
            for (int i = 0; i < constraints.length; i++) {
                out.println(is + "[" + i + "] " + constraints[i]);
            }
        }
    }

    /**
     * Find a primary key column by name. Returns column found or null if
     * none.
     */
    public JdbcColumn findPkColumn(String columnName) {
        for (int i = pk.length - 1; i >= 0; i--) {
            JdbcColumn c = pk[i];
            if (c.name.equals(columnName)) return c;
        }
        return null;
    }

    /**
     * Get the names of all the JdbcColumn's in the primary key.
     */
    public String[] getPkNames() {
        int pklen = pk == null ? 0 : pk.length;
        String[] a = new String[pklen];
        for (int i = 0; i < pklen; i++) {
            a[i] = pk[i].name;
        }
        return a;
    }

    /**
     * Set the table reference on all of our columns to us.
     */
    public void setTableOnCols() {
        for (int i = 0; i < cols.length; i++) cols[i].table = this;
        for (int i = 0; i < pk.length; i++) pk[i].table = this;
    }

    /**
     * Append part of a where clause for our primary key columns to us (e.g
     * pk1 = ? and pk2 = ?).
     */
    public void appendWherePK(CharBuf s) {
        JdbcColumn.appendEqualsParam(s, pkSimpleCols, sqlDriver);
    }

    /**
     * Append part of an insert column name list for our primary key columns
     * to s (e.g 'pk1, pk2').
     */
    public void appendInsertPKColumnList(CharBuf s) {
        int nc = pkSimpleCols.length;
        s.append(pkSimpleCols[0].name);
        for (int i = 1; i < nc; i++) {
            s.append(", ");
            s.append(pkSimpleCols[i].name);
        }
    }

    /**
     * Append part of an insert value list for our primary key columns
     * to s (e.g '?, ?').
     */
    public void appendInsertPKValueList(CharBuf s) {
        s.append("?");
        int nc = pkSimpleCols.length;
        for (int i = 1; i < nc; i++) s.append(", ?");
    }

    /**
     * Append part of an insert column name list for all our columns
     * to s (e.g 'cola, colb, colc').
     */
    public void appendInsertColumnList(CharBuf s) {
        int nc = cols.length;
        s.append(cols[0].name);
        for (int i = 1; i < nc; i++) {
            s.append(", ");
            s.append(cols[i].name);
        }
    }

    /**
     * Append part of an insert value list for all our columns
     * to s (e.g '?, ?, ?').
     */
    public void appendInsertValueList(CharBuf s) {
        s.append("?");
        int nc = cols.length;
        for (int i = 1; i < nc; i++) s.append(", ?");
    }

    /**
     * Get a pk = ? expression for this table.
     */
    public SqlExp createPkEqualsParamExp(SelectExp se) {
        int nc = pkSimpleCols.length;
        if (nc == 1) {
            return pkSimpleCols[0].createEqualsParamExp(se);
        } else {
            SqlExp list = pkSimpleCols[0].createEqualsParamExp(se);
            SqlExp pos = list;
            for (int i = 1; i < nc; i++) {
                pos = pos.setNext(pkSimpleCols[i].createEqualsParamExp(se));
            }
            return new AndExp(list);
        }
    }

    /**
     * Get an order by list to order by our primary key.
     */
    public SqlExp createOrderByPKList(SelectExp se) {
        SqlExp ans = new OrderExp(pkSimpleCols[0].toSqlExp(se), false);
        int nc = pkSimpleCols.length;
        if (nc > 1) {
            SqlExp e = ans;
            for (int i = 1; i < nc; i++) {
                e = e.setNext(new OrderExp(pkSimpleCols[i].toSqlExp(se), false));
            }
        }
        return ans;
    }

    /**
     * Set the primary key of this table. This will fill in pkSimpleCols
     * and pkSimpleColumnCount as well.
     */
    public void setPk(JdbcColumn[] pk) {
        this.pk = pk;
        pkSimpleCols = pk;
        pkSimpleColumnCount = pkSimpleCols.length;
    }

    /**
     * Is there a columnName in the primary key of this table?
     */
    public boolean isInPrimaryKey(String columnName) {
        for (int i = pk.length - 1; i >= 0; i--) {
            if (pk[i].name.equals(columnName)) return true;
        }
        return false;
    }

    /**
     * Get columns for a create table statement. This will remove any
     * duplicate and shared columns from cols. If there are multiple
     * shared=false columns with the same name and one of them is a foreign
     * key then it is used.
     */
    public JdbcColumn[] getColsForCreateTable() {
        ArrayList a = new ArrayList(cols.length);
        HashMap map = new HashMap();
        for (int i = 0; i < cols.length; i++) {
            JdbcColumn c = cols[i];
            if (c.shared) continue;
            JdbcColumn o = (JdbcColumn)map.get(c.name);
            if (o != null && (o.foreignKey || !c.foreignKey)) continue;
            map.put(c.name, c);
            a.add(c);
        }
        JdbcColumn[] ans = new JdbcColumn[a.size()];
        a.toArray(ans);
        return ans;
    }

    /**
     * Get the column that is the best choice for dummy update locking. This
     * will choose a simple non-indexed non-primary key column if possible.
     */
    public JdbcColumn getLockRowColumn() {
        if (lockRowColumn == null) {
            lockRowColumn = pk[0];
            for (int i = cols.length - 1; i >= 0; i--) {
                lockRowColumn = chooseLockRowCol(cols[i], lockRowColumn);
            }
        }
        return lockRowColumn;
    }

    private static JdbcColumn chooseLockRowCol(JdbcColumn a, JdbcColumn b) {
        // choose a non-primary key column
        if (!a.pk && b.pk) return a;
        if (a.pk && !b.pk) return b;

        // choose an unindexed column
        if (!a.partOfIndex && b.partOfIndex) return a;
        if (a.partOfIndex && !b.partOfIndex) return b;

        // choose a non-foreign key column
        if (!a.foreignKey && b.foreignKey) return a;
        if (a.foreignKey && !b.foreignKey) return b;

        // choose the column that is cheapest to update
        int diff = JdbcTypes.getUpdateCost(a.jdbcType) -
                JdbcTypes.getUpdateCost(b.jdbcType);
        if (diff < 0) return a;
        if (diff > 0) return b;

        return b;
    }

    public void addConstraints(ArrayList cons) {
        int length = constraints != null ? constraints.length : 0;
        int size = cons.size();
        HashSet set = new HashSet((length + size) * 2);
        for (int i = 0; i < length; i++) {
            JdbcConstraint constraint = constraints[i];
            if (!set.contains(constraint)) {
                set.add(constraint);
            }
        }
        for (int i = 0; i < size; i++) {
            Object o = cons.get(i);
            if (!set.contains(o)) {
                set.add(o);
            }
        }
        constraints = new JdbcConstraint[set.size()];
        set.toArray(constraints);
    }

    public void nameConstraints(JdbcNameGenerator nameGenerator) {
        if (constraints != null) {
            for (int i = 0; i < constraints.length; i++) {
                JdbcConstraint constraint = constraints[i];
                if (constraint.name == null) {
                    constraint.generateName(nameGenerator);
                }
            }
        }
    }
}
