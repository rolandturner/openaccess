
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
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.ColumnExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.JdbcUtils;
import com.versant.core.jdbc.JdbcConverter;
import com.versant.core.util.CharBuf;
import com.versant.core.common.State;

import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.versant.core.common.BindingSupportImpl;

/**
 * A field that fits into a single logical column. Note that this includes
 * embedded fields like primitive arrays[] (byte[], int[], Integer[] etc.).
 */
public class JdbcSimpleField extends JdbcField {

    /**
     * The column used to store this field.
     */
    public JdbcColumn col;

    private boolean oracleStyleLOB;
    private String oracleStyleLOBNotNullString;

    /**
     * Set the table field on all our main table columns.
     */
    public void setMainTable(JdbcTable table) {
        super.setMainTable(table);
        col.setTable(table);
    }

    /**
     * Make sure all of this fields main table columns have names.
     */
    public void nameColumns(String tableName, JdbcNameGenerator nameGen) {
        if (col.name == null) {
            col.name = nameGen.generateFieldColumnName(tableName, fmd.name, false);
        } else if (!nameGen.isColumnInTable(tableName, col.name)) {
            try {
                nameGen.addColumnName(tableName, col.name);
            } catch (IllegalArgumentException e) {
                throw BindingSupportImpl.getInstance().runtime(
                    "Invalid jdbc-column-name for field " + fmd.name + ": " +
                    e.getMessage() + "\n" + getContext());
            }
        }
    }

    /**
     * Init the mainTableCols field to all our main table columns.
     */
    public void initMainTableCols() {
        mainTableCols = new JdbcColumn[]{col};
        JdbcConverter converter = col.converter;
        if (converter != null) {
            oracleStyleLOB = converter.isOracleStyleLOB();
            oracleStyleLOBNotNullString = converter.getOracleStyleLOBNotNullString();
        } else {
            oracleStyleLOB = false;
        }
        super.initMainTableCols();
    }

    /**
     * Flatten all of this fields main table columns to a.
     */
    public void addMainTableCols(ArrayList a) {
        a.add(col);
    }

    /**
     * Append part of an update statement for us to s (e.g col = ?).
     */
    public boolean appendUpdate(CharBuf s, State state) {
        s.append(col.name);
        s.append('=');
        if (oracleStyleLOB) {
            if (state.isNull(stateFieldNo)) {
                s.append("null");
            } else {
                s.append(oracleStyleLOBNotNullString);
            }
            return true;
        } else {
            s.append('?');
            return false;
        }
    }

    /**
     * Append part of a where clause for us to s (e.g cola = ? and colb = ?).
     * This is used for generating the where clause for changed locking.
     */
    public void appendWhere(CharBuf s, SqlDriver sqlDriver) {
        s.append(col.name);
        s.append("=");
        sqlDriver.appendWhereParam(s, col);
    }

    /**
     * Append part of a is null where clause for us to s (e.g cola is null
     * and colb is null).
     * This is used for generating the where clause for changed locking.
     */
    public void appendWhereIsNull(CharBuf s, SqlDriver sqlDriver) {
        s.append(col.name);
        s.append(" is null");
    }

    /**
     * Append part of the insert list for us to s (e.g. cola, colb)).
     */
    public void appendInsertColumnList(CharBuf s) {
        s.append(col.name);
    }

    /**
     * Append part of the insert value list for us to s (e.g. ?, ?)). This
     * must return true if a replacable parameter was <b>not</b> added (e.g.
     * columns using Oracle LOBs which put in empty_clob() or whatever).
     */
    public boolean appendInsertValueList(CharBuf s, State state) {
        if (oracleStyleLOB) {
            if (state.isNull(stateFieldNo)) s.append("null");
            else s.append(oracleStyleLOBNotNullString);
            return true;
        } else {
            s.append('?');
            return false;
        }
    }

    /**
     * Convert this field into a list of ColumnExp's or null if this is
     * not possible.
     */
    public ColumnExp toColumnExp(SelectExp se, boolean joinToSuper) {
        if (joinToSuper) return new ColumnExp(col, SelectExp.createJoinToSuperTable(se, this), this);
        else return new ColumnExp(col, se, this); 
    }

    public ColumnExp createOwningTableColumnExpList(SelectExp se) {
        return toColumnExp(se, true);
//        return new ColumnExp(col, se, this);
    }

    /**
     * Set this field on a PreparedStatement. This is used to set parameters
     * for queries.
     * @return Index of the parameter after the last one we set in ps
     */
    public int setQueryParam(PreparedStatement ps, int firstParam, Object value)
            throws SQLException {
        if (col.converter != null) {
            col.converter.set(ps, firstParam++, col, value);
        } else {
            JdbcUtils.set(ps, firstParam++, value, col.javaTypeCode, col.jdbcType);
        }
        return firstParam;
    }

    /**
     * Does this field require the sucky Oracle LOB support on insert/update?
     */
    public boolean isOracleStyleLOB() {
        return oracleStyleLOB;
    }

}


