
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
package com.versant.core.jdbc.sql;

import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.conv.*;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.UnaryFunctionExp;
import com.versant.core.util.CharBuf;

import java.util.Date;
import java.util.HashMap;
import java.sql.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * A driver for Daffodil pure Java database.
 */
public class DaffodilSqlDriver extends SqlDriver {

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "daffodil";
    }

    /**
     * Get the default type mapping for the supplied JDBC type code from
     * java.sql.Types or null if the type is not supported. There is no
     * need to set the database or jdbcType on the mapping as this is done
     * after this call returns. Subclasses should override this and to
     * customize type mappings.
     */
    protected JdbcTypeMapping getTypeMapping(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:
                return new JdbcTypeMapping("BOOLEAN", 0, 0, JdbcTypeMapping.TRUE,
                        JdbcTypeMapping.TRUE, null);
            case Types.TINYINT:
                return new JdbcTypeMapping("TINYINT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.BIGINT:
                return new JdbcTypeMapping("BIGINT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONG VARCHAR",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.CLOB:
                return new JdbcTypeMapping("CLOB",
                        1024, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.VARBINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
                return new JdbcTypeMapping("BLOB",
                        1024, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        bytesConverterFactory);
        }
        return super.getTypeMapping(jdbcType);
    }

    /**
     * Should PreparedStatement batching be used for this database and
     * JDBC driver?
     */
    public boolean isPreparedStatementPoolingOK() {
        return false;
    }

    /**
     * Get the default field mappings for this driver. These map java classes
     * to column properties. Subclasses should override this, call super() and
     * replace mappings as needed.
     */
    public HashMap getJavaTypeMappings() {
        HashMap ans = super.getJavaTypeMappings();

//        BooleanConverter.Factory bcf = new BooleanConverter.Factory();
//        ((JdbcJavaTypeMapping)ans.get(Boolean.TYPE)).setConverterFactory(bcf);
//        ((JdbcJavaTypeMapping)ans.get(Boolean.class)).setConverterFactory(bcf);

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(Date.class)).setConverterFactory(dtcf);

        return ans;
    }

    /**
     * Does the JDBC driver support statement batching?
     */
    public boolean isInsertBatchingSupported() {
        return true;
    }

    /**
     * Does the JDBC driver support statement batching for updates?
     */
    public boolean isUpdateBatchingSupported() {
        return true;
    }

    /**
     * Does the JDBC driver support scrollable result sets?
     */
    public boolean isScrollableResultSetSupported() {
        return true;
    }

    /**
     * Does this driver use the ANSI join syntax (i.e. the join clauses appear
     * in the from list e.g. postgres)?
     */
    public boolean isAnsiJoinSyntax() {
        return true;
    }

    /**
     * Is null a valid value for a column with a foreign key constraint?
     */
    public boolean isNullForeignKeyOk() {
        return true;
    }

    /**
     * Create a default name generator instance for JdbcStore's using this
     * driver.
     */
    public JdbcNameGenerator createJdbcNameGenerator() {
        DefaultJdbcNameGenerator n = createDefaultJdbcNameGenerator();
        n.setMaxColumnNameLength(128);
        n.setMaxTableNameLength(128);
        n.setMaxConstraintNameLength(128);
        n.setMaxIndexNameLength(128);
        return n;
    }


    /**
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        stat.execute("drop table " + table + " cascade");
    }

    /**
     * Add the primary key constraint part of a create table statement to s.
     */
    protected void appendPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("constraint ");
        s.append(t.pkConstraintName);
        s.append(" primary key (");
        appendColumnNameList(t.pk, s);
        s.append(')');
    }

    /**
     * Append an 'add constraint' statement for c.
     */
    protected void appendRefConstraint(final CharBuf s, final JdbcConstraint c) {
        s.append("alter table ");
        s.append(c.src.name);
        s.append(" add constraint ");
        s.append(c.name);
        s.append(" foreign key (");
        appendColumnNameList(c.srcCols, s);
        s.append(") references ");
        s.append(c.dest.name);
        s.append('(');
        appendColumnNameList(c.dest.pk, s);
        s.append(")");
    }

    /**
     * Append the from list entry for a table.
     */
    public void appendSqlFrom(JdbcTable table, String alias,
            CharBuf s) {
        s.append(table.name);
        if (alias != null) {
            s.append(" AS ");
            s.append(alias);
        }
    }

    /**
     * Get the name of a function that accepts one argument.
     * @see com.versant.core.jdbc.sql.exp.UnaryFunctionExp
     */
    public String getSqlUnaryFunctionName(int func) {
        switch (func) {
            case UnaryFunctionExp.FUNC_TO_LOWER_CASE:
                return "lcase";
        }
        throw BindingSupportImpl.getInstance().internal("Unknown func: " + func);
    }

    /**
     * Append the from list entry for a table that is the right hand table
     * in a join i.e. it is being joined to.
     * @param exp This is the expression that joins the tables
     * @param outer If true then this is an outer join
     */
    public void appendSqlFromJoin(JdbcTable table, String alias, SqlExp exp,
            boolean outer, CharBuf s) {
        if (outer) s.append(" LEFT JOIN ");
        else s.append(" JOIN ");
        s.append(table.name);
        if (alias != null) {
            s.append(" AS ");
            s.append(alias);
        }
        if (exp != null) {
            s.append(" ON (");
            exp.appendSQL(this, s, null);
            s.append(')');
        }
    }


    /**
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return null;
    }

    /**
     * Get default SQL used to init a connection or null if none required.
     */
    public String getConnectionInitSQL() {
        return null;
    }

    /**
     * Does this database support comments embedded in SQL?
     */
    public boolean isCommentSupported() {
        return false;
    }

    /**
     * Should indexes be used for columns in the order by list that are
     * also in the select list? This is used for databases that will not
     * order by a column that is duplicated in the select list (e.g. Oracle).
     */
    public boolean isUseIndexesForOrderCols() {
        return true;
    }

    /**
     * Get whatever needs to be appended to a SELECT statement to lock the
     * rows if this makes sense for the database. This must have a leading
     * space if not empty.
     */
    public char[] getSelectForUpdate() {
        return null;
    }

}
