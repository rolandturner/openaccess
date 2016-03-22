
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
import com.versant.core.jdbc.sql.conv.DateTimestampConverter;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.util.CharBuf;

import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;

import com.versant.core.util.CharBuf;

/**
 * A driver for Hypersonic SQL.
 */
public class HypersonicSqlDriver extends SqlDriver {

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "hypersonic";
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
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("LONGVARBINARY",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        bytesConverterFactory);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONGVARCHAR",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
        }
        return super.getTypeMapping(jdbcType);
    }

    public boolean isScrollableResultSetSupported() {
        return true;
    }

    /**
     * Get the default field mappings for this driver. These map java classes
     * to column properties. Subclasses should override this, call super() and
     * replace mappings as needed.
     */
    public HashMap getJavaTypeMappings() {
        HashMap ans = super.getJavaTypeMappings();

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(Date.class)).setConverterFactory(dtcf);

        return ans;
    }

    /**
     * Generate a 'create table' statement for t.
     */
    public void generateCreateTable(JdbcTable t, Statement stat, PrintWriter out,
            boolean comments)
            throws SQLException {
        CharBuf s = new CharBuf();
        if (comments && isCommentSupported() && t.comment != null) {
            s.append(comment(t.comment));
            s.append('\n');
        }
        s.append("CREATE CACHED TABLE ");
        s.append(t.name);
        s.append(" (\n");
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first) {
                first = false;
            } else {
                s.append("\n");
            }
            s.append("    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        s.append("\n    ");
        appendPrimaryKeyConstraint(t, s);
        appendIndexesInCreateTable(t, s);
        s.append("\n)");
        appendTableType(t, s);
        String sql = s.toString();
        if (out != null) print(out, sql);
        if (stat != null) stat.execute(sql);
    }

    /**
     * Can the tx isolation level be set on this database?
     */
    public boolean isSetTransactionIsolationLevelSupported() {
        return false;
    }

    /**
     * Does this driver use the ANSI join syntax (i.e. the join clauses appear
     * in the from list e.g. postgres)?
     */
    public boolean isAnsiJoinSyntax() {
        return true;
    }

    /**
     * May the ON clauses for joins in a subquery reference columns from the
     * enclosing query? DB2 does not allow this.
     */
    public boolean isSubQueryJoinMayUseOuterQueryCols() {
        return false;
    }

    /**
     * Is null a valid value for a column with a foreign key constraint?
     */
    public boolean isNullForeignKeyOk() {
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

    /**
     * Does the JDBC driver support Statement.setFetchSize()?
     */
    public boolean isFetchSizeSupported() {
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
     * Does the LIKE operator only support literal string and column
     * arguments (e.g. Informix)?
     */
    public boolean isLikeStupid() {
        return true;
    }

    /**
     * Should PreparedStatement pooling be used for this database and
     * JDBC driver?
     */
    public boolean isPreparedStatementPoolingOK() {
        return false;
    }

    /**
     * Does the JDBC driver support statement batching for inserts?
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
        if (c.nulls) {
            s.append(" NULL");
        } else {
            s.append(" NOT NULL");
        }
    }

    /**
     * Add the primary key constraint part of a create table statement to s.
     */
    protected void appendPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("CONSTRAINT ");
        s.append(t.pkConstraintName);
        s.append(" PRIMARY KEY (");
        appendColumnNameList(t.pk, s);
        s.append(')');
    }

    public void generateConstraints(JdbcTable t, Statement stat,
            PrintWriter out, boolean comments) throws SQLException {
        // current hypersonic release corrupts the database if there are
        // ref constraints
    }

    public boolean checkDDL(ArrayList tables, Connection con,
            PrintWriter errors, PrintWriter fix, ControlParams params)
            throws SQLException {
        // current hypersonic release corrupts the database if there are
        // ref constraints
        params.setCheckConstraint(false);
        return super.checkDDL(tables, con, errors, fix, params);
    }

    /**
     * Append an 'add constraint' statement for c.
     // current hypersonic release corrupts the database if there are
     // ref constraints
     protected void appendRefConstraint(CharBuf s, JdbcConstraint c) {
     s.append("ALTER TABLE ");
     s.append(c.src.name);
     s.append(" ADD CONSTRAINT ");
     s.append(c.name);
     s.append(" FOREIGN KEY (");
     appendColumnNameList(c.srcCols, s);
     s.append(") REFERENCES ");
     s.append(c.dest.name);
     s.append('(');
     appendColumnNameList(c.dest.pk, s);
     s.append(')');
     }
     */

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

    protected boolean isValidSchemaTable(String tableName) {
        if (tableName.startsWith("SYSTEM_")) {
            return false;
        }
        return true;
    }

    /**
     * Append the from list entry for a table that is the right hand table
     * in a join i.e. it is being joined to.
     *
     * @param exp   This is the expression that joins the tables
     * @param outer If true then this is an outer join
     */
    public void appendSqlFromJoin(JdbcTable table, String alias, SqlExp exp,
            boolean outer, CharBuf s) {
        if (outer) {
            s.append(" LEFT JOIN ");
        } else {
            s.append(" JOIN ");
        }
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
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        ResultSet rs = null;
        try {
            stat = con.createStatement();
            rs = stat.executeQuery(
                    "SELECT FKTABLE_NAME,FK_NAME\n" +
                    "  FROM SYSTEM_CROSSREFERENCE\n" +
                    " WHERE PKTABLE_NAME = '" + table.toUpperCase().trim() + "'");
            ArrayList a = new ArrayList();
            for (; rs.next();) {
                String tableName = rs.getString(1);
                String conName = rs.getString(2);
                a.add(
                        "ALTER TABLE " + tableName + " DROP CONSTRAINT " + conName);

            }
            rs.close();
            for (Iterator i = a.iterator(); i.hasNext();) {
                String sql = (String)i.next();
                try {
                    stat.execute(sql);
                } catch (SQLException e) {
                    /*some times it's a bit slow to update it's system tables and we get a exeption*/
                }
            }
            stat.execute("DROP TABLE " + table);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException x) {
                    // ignore
                }
            }
        }
    }

    boolean isDirectTypeColumnChangesSupported(JdbcColumn ourCol,
            JdbcColumn dbCol) {
        return false;
    }

    boolean isDirectNullColumnChangesSupported() {
        return false;
    }

    boolean isDirectScaleColumnChangesSupported(JdbcColumn ourCol,
            JdbcColumn dbCol) {
        return false;
    }

    boolean isDirectLenghtColumnChangesSupported(JdbcColumn ourCol,
            JdbcColumn dbCol) {
        return false;
    }

    boolean isDropPrimaryKeySupported() {
        return false;
    }

    boolean isDropConstraintsForDropTableSupported() {
        return false;
    }

    /**
     * Append a column that needs to be added.
     */
    protected void appendAddNewColumn(JdbcTable t, JdbcColumn c,
            CharBuf s, boolean comments) {
        if (comments && isCommentSupported() && c.comment != null) {
            s.append(comment("add column for field " + c.comment));
        }

        s.append("\n");
        if (isAddSequenceColumn(c)) {
            addSequenceColumn(t, c, s, comments);
        } else {
            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" ADD COLUMN ");
            s.append(c.name);
            s.append(' ');
            appendColumnType(c, s);
            if (!c.nulls) {
                s.append(" DEFAULT ");
                String _default = getDefaultForType(c);
                if (_default.startsWith("'")) {
                    s.append(_default);
                } else {
                    s.append("'");
                    s.append(_default);
                    s.append("'");
                }
                s.append(" NOT NULL");
            }
            s.append(getRunCommand());
        }
    }

    /**
     * Append a column that needs to be added.
     */
    protected void appendDropColumn(TableDiff tableDiff, JdbcColumn c,
            CharBuf s, boolean comments) {
        if (comments && isCommentSupported()) {
            s.append(comment("dropping unknown column " + c.name));
        }
        s.append("\n");
        if (isDropSequenceColumn(tableDiff, c)) {
            dropSequenceColumn(tableDiff.getOurTable(), c, s, comments);
        } else {
            s.append("ALTER TABLE ");
            s.append(tableDiff.getOurTable().name);
            s.append(" DROP COLUMN ");
            s.append(c.name);
        }
    }

    /**
     * Append an 'drop constraint' statement for c.
     */
    protected void appendRefDropConstraint(CharBuf s, JdbcConstraint c,
            boolean comments) {
        s.append("ALTER TABLE ");
        s.append(c.src.name);
        s.append(" DROP CONSTRAINT ");
        s.append(c.name);
    }

    /**
     * Generate a 'drop index' statement for idx.
     */
    protected void appendDropIndex(CharBuf s, JdbcTable t, JdbcIndex idx,
            boolean comments) {
        s.append("DROP INDEX ");
        s.append(idx.name);
    }

    protected void fixColumnsNonDirect(TableDiff tableDiff, PrintWriter out) {
        JdbcTable ourTable = tableDiff.getOurTable();
        String tempTableName = getTempTableName(ourTable, 31);

        CharBuf s = new CharBuf();
        s.append("CREATE CACHED TABLE ");
        s.append(tempTableName);  //ourTable.name
        s.append(" (");
        JdbcColumn[] cols = ourTable.getColsForCreateTable();
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(ourTable, cols[i], s, false);
        }
        s.append("\n    ");
        appendPrimaryKeyConstraint(ourTable, s);
        s.append(")");
        s.append(getRunCommand());

        s.append("INSERT INTO ");
        s.append(tempTableName);  //ourTable.name
        s.append(" (");
        for (int i = 0; i < nc; i++) {
            s.append(cols[i].name);
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append(") ");

        s.append("\n");//new line

        s.append("SELECT ");
        for (int i = 0; i < nc; i++) {
            ColumnDiff diff = getColumnDiffForName(tableDiff, cols[i].name);
            if (diff == null) {
                if (i != 0) {
                    s.append("       ");
                }
                s.append(cols[i].name);
            } else {
                if (diff.isMissingCol()) {
                    if (diff.getOurCol().nulls) {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append("CAST( NULL AS ");
                        appendColumnType(cols[i], s);
                        s.append(")");

                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append(getDefaultForType(diff.getOurCol()));
                    }

                } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && diff.isNullDiff()) {
                    if (cols[i].nulls) {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append("CAST(");
                        s.append(cols[i].name);
                        s.append(" AS ");
                        appendColumnType(cols[i], s);
                        s.append(")");
                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        /* IFNULL(exp,value) (if exp is null, value is returned else exp) */

                        s.append("CAST( ");
                        s.append("IFNULL(");
                        s.append(cols[i].name);
                        s.append(",");
                        s.append(getDefaultForType(diff.getOurCol()));
                        s.append(") AS ");
                        appendColumnType(cols[i], s);
                        s.append(")");
                    }

                } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && !diff.isNullDiff()) {
                    if (i != 0) {
                        s.append("       ");
                    }
                    s.append("CAST(");
                    s.append(cols[i].name);
                    s.append(" AS ");
                    appendColumnType(cols[i], s);
                    s.append(")");
                } else if (diff.isNullDiff()) {
                    if (cols[i].nulls) {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append(cols[i].name);
                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append("IFNULL(");
                        s.append(cols[i].name);
                        s.append(",");
                        s.append(getDefaultForType(diff.getOurCol()));
                        s.append(")");
                    }
                }
            }

            if ((i + 1) != nc) {
                s.append(", ");
                s.append("\n");//new line
            }
        }
        s.append("\n");//new line
        s.append("  FROM ");
        s.append(ourTable.name);
        s.append(getRunCommand());

        s.append("DROP TABLE ");
        s.append(ourTable.name);
        s.append(getRunCommand());

        s.append("ALTER TABLE ");
        s.append(tempTableName);
        s.append(" RENAME TO ");
        s.append(ourTable.name);
        s.append(getRunCommand());

        out.println(s.toString());
    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {
        String tempTableName = getTempTableName(t, 31);

        s.append(comment("create a temp table to store old table values."));
        s.append("\n");

        s.append("CREATE CACHED TABLE ");
        s.append(tempTableName);
        s.append(" (\n");
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first) {
                first = false;
            } else {
                s.append("\n");
            }
            s.append("    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        s.append("\n    ");
        appendPrimaryKeyConstraint(t, s);
        s.append("\n)");
        s.append(getRunCommand());

        s.append(comment("insert a distinct list into the temp table."));
        s.append("\n");
        s.append("INSERT INTO ");
        s.append(tempTableName);
        s.append("(");
        for (int i = 0; i < nc; i++) {
            s.append(cols[i].name);
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append(")");
        s.append("\nSELECT DISTINCT ");
        for (int i = 0; i < nc; i++) {
            if (i != 0) {
                s.append("\n       ");
            }
            s.append(cols[i].name);
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append("\n  FROM ");
        s.append(t.name);

        s.append(getRunCommand());

        s.append(comment("drop main table."));
        s.append("\n");
        s.append("DROP TABLE ");
        s.append(t.name);
        s.append(getRunCommand());

        s.append(comment("rename temp table to main table."));
        s.append("\n");
        s.append("ALTER TABLE ");
        s.append(tempTableName);
        s.append(" RENAME TO ");
        s.append(t.name);

    }

    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {

        String mainTempTableName = getTempTableName(t, 31);
        String minTempTableName = getTempTableName(t, 31);
        String identityColumnName = getTempColumnName(t);

        JdbcColumn indexColumn = null;
        JdbcColumn sequenceColumn = null;
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            if (isAddSequenceColumn(cols[i])) {
                sequenceColumn = cols[i];
            } else if (t.isInPrimaryKey(cols[i].name)) {
                indexColumn = cols[i];
            }
        }

        s.append(
                comment(
                        "Generate a sequence number so that we can implement a List."));
        s.append("\n");
        s.append(comment("create a temp table with a extra identity column."));
        s.append("\n");
        s.append("CREATE CACHED TABLE ");
        s.append(mainTempTableName);
        s.append(" (\n    ");
        // create identity column
        s.append(identityColumnName);
        s.append(" IDENTITY,");
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        int lastIndex = s.toString().lastIndexOf(',');
        s.replace(lastIndex, lastIndex + 1, ' ');// we take the last ',' out.
        s.append("\n)");

        s.append(getRunCommand());

        s.append(
                comment(
                        "insert a '0' in the sequence column and copy the rest of the old table into the temp table."));
        s.append("\n");
        s.append("INSERT INTO ");
        s.append(mainTempTableName);
        s.append("(");
        for (int i = 0; i < nc; i++) {
            s.append(cols[i].name);
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append(")");
        s.append("\nSELECT ");
        for (int i = 0; i < nc; i++) {
            if (i != 0) {
                s.append("\n       ");
            }
            if (isAddSequenceColumn(cols[i])) {
                s.append('0');
            } else {
                s.append(cols[i].name);
            }
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append("\n  FROM ");
        s.append(t.name);
        s.append("\n ORDER BY ");
        s.append(indexColumn.name);

        s.append(getRunCommand());

        s.append(comment("create a temp table to store the minimum id."));
        s.append("\n");
        s.append("CREATE CACHED TABLE ");
        s.append(minTempTableName);
        s.append(" (\n    ");
        s.append(indexColumn.name);
        s.append(' ');
        appendColumnType(indexColumn, s);
        appendCreateColumnNulls(t, indexColumn, s);
        s.append(",\n    ");
        s.append("min_id");
        s.append(" INTEGER\n)");

        s.append(getRunCommand());

        s.append(comment("store the minimum id."));
        s.append("\n");
        s.append("INSERT INTO ");
        s.append(minTempTableName);
        s.append(" (");
        s.append(indexColumn.name);
        s.append(", ");
        s.append("min_id");
        s.append(")\n");
        s.append("SELECT ");
        s.append(indexColumn.name);
        s.append(",\n       ");
        s.append("MIN(");
        s.append(identityColumnName);
        s.append(")\n");
        s.append("  FROM ");
        s.append(mainTempTableName);
        s.append("\n");
        s.append(" GROUP BY ");
        s.append(indexColumn.name);

        s.append(getRunCommand());

        s.append(comment("drop main table " + t.name + "."));
        s.append("\n");
        s.append("DROP TABLE ");
        s.append(t.name);

        s.append(getRunCommand());

        s.append(comment("recreate table " + t.name + "."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(t.name);
        s.append(" (\n");
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first) {
                first = false;
            } else {
                s.append("\n");
            }
            s.append("    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        s.append("\n    ");
        appendPrimaryKeyConstraint(t, s);
        s.append("\n)");
        appendTableType(t, s);

        s.append(getRunCommand());

        s.append(
                comment(
                        "populate table " + t.name + " with the new sequence column."));
        s.append("\n");
        s.append("INSERT INTO ");
        s.append(t.name);
        s.append("(");
        for (int i = 0; i < nc; i++) {
            s.append(cols[i].name);
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append(")");
        s.append("\nSELECT ");
        for (int i = 0; i < nc; i++) {
            if (i != 0) {
                s.append("\n       ");
            }

            if (isAddSequenceColumn(cols[i])) {
                s.append("(a.");
                s.append(identityColumnName);
                s.append(" - b.min_id)");
            } else {
                s.append("a.");
                s.append(cols[i].name);
            }

            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append("\n  FROM ");
        s.append(mainTempTableName);
        s.append(" a,\n       ");
        s.append(minTempTableName);
        s.append(" b\n WHERE a.");
        s.append(indexColumn.name);
        s.append(" = b.");
        s.append(indexColumn.name);

        s.append(getRunCommand());

        s.append(comment("drop temp tables."));
        s.append("\n");
        s.append("DROP TABLE ");
        s.append(mainTempTableName);
        s.append(getRunCommand());

        s.append("DROP TABLE ");
        s.append(minTempTableName);

        s.append(getRunCommand());
    }

}
