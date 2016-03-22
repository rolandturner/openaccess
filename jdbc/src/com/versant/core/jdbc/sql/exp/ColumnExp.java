
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
package com.versant.core.jdbc.sql.exp;

import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.util.CharBuf;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdo.query.ParamNode;
import com.versant.core.common.Debug;

import java.util.Map;

import com.versant.core.common.BindingSupportImpl;

import javax.jdo.JDOFatalInternalException;

/**
 * The name of a column.
 */
public class ColumnExp extends LeafExp {

    /**
     * This may be null in which case this expression refers to all columns
     * (i.e. a 'select *').
     */
    public JdbcColumn col;
    /**
     * The table this column belongs to. This provides the correct alias.
     */
    public SelectExp selectExp;
    /**
     * The field this expression is for (null if none).
     */
    public JdbcField jdbcField;
    /**
     * The class this expression is for (-1 if none).
     */
    public ClassMetaData cmd;

    private String asValue;

    public ColumnExp(JdbcColumn col, SelectExp selectExp, JdbcField jdbcField) {
        if (Debug.DEBUG) {
            if (selectExp == null) {
                throw BindingSupportImpl.getInstance().internal("The selectExp is null");
            }
            if (col.table != selectExp.table) {
                throw BindingSupportImpl.getInstance().internal(jdbcField.fmd.name + ": The col's table '"
                        + col.table
                        + "' is not the same as the selectExp's table '"
                        + selectExp.table + "'");
            }
        }
        this.col = col;
        this.selectExp = selectExp;
        this.jdbcField = jdbcField;
    }

    public ColumnExp() {
    }

    public ColumnExp(String asValue) {
        if (asValue != null && asValue.length() > 0) {
            this.asValue = asValue;
        } else {
            this.asValue = null;
        }
    }

    public SqlExp createInstance() {
        return new ColumnExp();
    }

    public SqlExp getClone(SqlExp columnExp, Map cloneMap) {
        super.getClone(columnExp, cloneMap);
        ColumnExp cst = (ColumnExp) columnExp;

        cst.col = col;
        cst.jdbcField = jdbcField;
        cst.cmd = cmd;
        if (selectExp != null) cst.selectExp = (SelectExp) createClone(selectExp, cloneMap);

        return columnExp;
    }

    public ClassMetaData getCmd() {
        return cmd;
    }

    public void setCmd(ClassMetaData cmd) {
        this.cmd = cmd;
    }

    public String toString() {
        return super.toString() + " " + col + " " + selectExp;
    }

    /**
     * Append SQL for this node to s.
     *
     * @param driver The driver being used
     * @param s Append the SQL here
     * @param leftSibling
     */
    public void appendSQLImp(SqlDriver driver, CharBuf s, SqlExp leftSibling) {
        if (isAliasedColumn() && jdbcField == null) {
            s.append(" " + asValue + " ");
        } else {
            driver.appendSqlColumn(col, selectExp.alias, s);
            if (isAliasedColumn()) {
                s.append(driver.getAliasPrepend() + asValue);
            }
        }
    }

    /**
     * What is the JDBC type of this expression (0 if unknown)?
     */
    public int getJdbcType() {
        return col.jdbcType;
    }

    /**
     * What is the java type code of this expression (0 if unknown)?
     */
    public int getJavaTypeCode() {
        return col.javaTypeCode;
    }

    /**
     * What is the class index for this expression (-1 if unknown)?
     * @see ParamNode
     */
    public int getClassIndex() {
        if (cmd == null)
            return -1;
        else
            return cmd.index;
    }

    /**
     * Replace any references to old with nw. This is used when redundant
     * joins are removed.
     */
    public void replaceSelectExpRef(SelectExp old, SelectExp nw) {
        if (nw == null) {
            throw new JDOFatalInternalException();
        }
        if (selectExp == old) {
            selectExp = nw;
        } else {
            SelectExp other = nw.findTableRecursive(selectExp.table,
                    selectExp.jdbcField);
            if (other != null) selectExp = other;
        }
    }

    /**
     * If this expression involves a single table only then return the
     * SelectExp for the table (e.g. a.col1 == 10 returns a, a.col1 = b.col2
     * returns null). This is used to detect expressions that can be moved
     * into the ON list for the join to the table involved.
     * @param exclude
     */
    public SelectExp getSingleSelectExp(SelectExp exclude) {
        return selectExp;
    }

    /**
     * Create an aliases for any subtables we may have.
     */
    public int createAlias(int index) {
        if (selectExp == null) {
            return index;
        } else {
            return selectExp.createAlias(index);
        }
    }

    public boolean isAliasedColumn() {
        return asValue != null;
    }

    public String getAsValue() {
        return asValue;
    }

    public void setColAlias(String columnAlias) {
        if (columnAlias != null && columnAlias.length() > 0) {
            this.asValue = columnAlias;
        } else {
            this.asValue = null;
        }
    }
}
