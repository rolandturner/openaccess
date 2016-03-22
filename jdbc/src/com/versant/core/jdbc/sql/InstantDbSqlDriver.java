
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
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.util.CharBuf;

import java.io.PrintWriter;

import com.versant.core.util.CharBuf;

/**
 * A driver for Instant DB.
 */
public class InstantDbSqlDriver extends SqlDriver {

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "instantdb";
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
        n.setMaxColumnNameLength(31);
        n.setMaxTableNameLength(31);
        n.setMaxConstraintNameLength(31);
        n.setMaxIndexNameLength(31);
        return n;
    }

    /**
     * Append the allow nulls part of the definition for a column in a
     * create table statement.
     */
    protected void appendCreateColumnNulls(JdbcTable t, JdbcColumn c,
            CharBuf s) {
        if (c.nulls) s.append(" null");
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
    protected void appendRefConstraint(CharBuf s, JdbcConstraint c) {
    }

    /**
     * Write an SQL statement to a script with appropriate separator.
     */
    protected void print(PrintWriter out, String sql) {
        out.print(sql);
        out.println(";");
        out.println();
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

}
