
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

import com.versant.core.common.Debug;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.conv.AsciiStreamConverter;
import com.versant.core.jdbc.sql.conv.BooleanConverter;
import com.versant.core.jdbc.sql.conv.DateTimestampConverter;
import com.versant.core.jdbc.sql.conv.InputStreamConverter;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.util.CharBuf;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Driver for MySQL.
 */
public final class MySqlSqlDriver extends SqlDriver {

    private AsciiStreamConverter.Factory asciiStreamConverterFactory
            = new AsciiStreamConverter.Factory();
    private InputStreamConverter.Factory inputStreamConverterFactory
            = new InputStreamConverter.Factory();

    private boolean refConstraintsNotSupported = true;
    private int major;
    private int minor;
    private String minorPatchLevel;
    private String rawVersion;

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "mysql";
    }

    public int getMajorVersion() {
        return major;
    }

    public int getMinorVersion() {
        return minor;
    }

    public String getMinorVersionPatchLevel() {
        return minorPatchLevel;
    }

    public String getVersion() {
        return rawVersion;
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
            case Types.FLOAT:
            case Types.REAL:
                return new JdbcTypeMapping("FLOAT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("DATETIME",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONGTEXT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        asciiStreamConverterFactory);
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("LONGBLOB",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        inputStreamConverterFactory);
            case Types.VARBINARY:
                return new JdbcTypeMapping("TINYBLOB",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        bytesConverterFactory);
        }
        return super.getTypeMapping(jdbcType);
    }

    /**
     * Get the default field mappings for this driver. These map java classes
     * to column properties. Subclasses should override this, call super() and
     * replace mappings as needed.
     */
    public HashMap getJavaTypeMappings() {
        HashMap ans = super.getJavaTypeMappings();

        BooleanConverter.Factory bcf = new BooleanConverter.Factory();
        ((JdbcJavaTypeMapping) ans.get(Boolean.TYPE)).setConverterFactory(bcf);
        ((JdbcJavaTypeMapping) ans.get(Boolean.class)).setConverterFactory(bcf);

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping) ans.get(Date.class)).setConverterFactory(dtcf);

        return ans;
    }

    /**
     * Use the index of the column in the 'group by' expression.
     */
    public boolean useColumnIndexForGroupBy() {
        return true;
    }

    public boolean isCustomizeForServerRequired() {
        return true;
    }

    /**
     * Find out what version of MySQL con is for and adapt.
     */
    public void customizeForServer(Connection con) throws SQLException {
        try {
            extractVersionInfo(rawVersion = getVersion(con));
        } catch (NumberFormatException e) {
            if (Debug.DEBUG) e.printStackTrace(System.out);
        }
    }

    /**
     * Extract version info from a String. Expected format 'major.minor.minorPatchLevel'
     * where both major and minor will be interpreted as int and minorPatchLevel
     * as a String.
     */
    private void extractVersionInfo(String s) {
        if (Debug.DEBUG) System.out.println("s = " + s);
        int i = s.indexOf('.');
        major = Integer.parseInt(s.substring(0, i));
        if (Debug.DEBUG) System.out.println("major = " + major);
        int j = s.indexOf('.', i + 1);
        minor = Integer.parseInt(s.substring(i + 1, j));
        if (Debug.DEBUG) System.out.println("minor = " + minor);
        minorPatchLevel = s.substring(j + 1);
        if (Debug.DEBUG) {
            System.out.println("minorPatchLevel = " + minorPatchLevel);
        }
    }

    private String getVersion(Connection con) throws SQLException {
        Statement stat = null;
        ResultSet rs = null;
        try {
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT version()");
            rs.next();
            String ver = rs.getString(1);
            con.commit();
            return ver;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Create a default name generator instance for JdbcStore's using this
     * driver.
     */
    public JdbcNameGenerator createJdbcNameGenerator() {
        DefaultJdbcNameGenerator n = createDefaultJdbcNameGenerator();
        n.setMaxColumnNameLength(64);
        n.setMaxTableNameLength(64);
        n.setMaxConstraintNameLength(64);
        n.setMaxIndexNameLength(64);
        return n;
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

    /**
     * Hook for drivers that have to append a table type to the create table
     * statement (e.g. MySQL).
     */
    protected void appendTableType(JdbcTable t, CharBuf s) {
        s.append(" TYPE = InnoDB");
    }

    /**
     * Hook for drivers that must create indexes in the create table
     * statement (e.g. MySQL).
     */
    protected void appendIndexesInCreateTable(JdbcTable t, CharBuf s) {
//        if (t.indexes == null) return;
//        for (int i = 0; i < t.indexes.length; i++) {
//            JdbcIndex idx = t.indexes[i];
//            s.append(",\n    ");
//            if (idx.unique) {
//                s.append("UNIQUE ");
//            }else {
//                s.append("INDEX ");
//            }
//            s.append(idx.name);
//            s.append(' ');
//            s.append('(');
//            s.append(idx.cols[0].name);
//            int n = idx.cols.length;
//            for (int j = 1; j < n; j++) {
//                s.append(',');
//                s.append(' ');
//                s.append(idx.cols[j].name);
//            }
//            s.append(')');
//        }
    }

    /**
     * Generate a 'create index' statement for idx.
     */
    protected void appendCreateIndex(CharBuf s, JdbcTable t, JdbcIndex idx,
                                     boolean comments) {
        if (comments && isCommentSupported() && idx.comment != null) {
            s.append(comment(idx.comment));
            s.append('\n');
        }
        s.append("ALTER TABLE ");
        s.append(t.name);
        if (idx.unique) {
            s.append(" ADD UNIQUE ");
        } else {
            s.append(" ADD INDEX ");
        }
        s.append(idx.name);
        s.append('(');
        s.append(idx.cols[0].name);
        int n = idx.cols.length;
        for (int i = 1; i < n; i++) {
            s.append(',');
            s.append(' ');
            s.append(idx.cols[i].name);
        }
        s.append(')');
    }

    /**
     * Generate the 'add constraint' statements for t.
     */
    public void generateConstraints(JdbcTable t, Statement stat,
                                    PrintWriter out, boolean comments)
            throws SQLException {
        if (!refConstraintsNotSupported) {
            super.generateConstraints(t, stat, out, comments);
        }
    }

    /**
     * Append an 'add constraint' statement for c.
     */
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

    /**
     * Append the from list entry for a table that is the right hand table
     * in a join i.e. it is being joined to.
     *
     * @param exp   This is the expression that joins the tables
     * @param outer If true then this is an outer join
     */
    public void appendSqlFromJoin(JdbcTable table, String alias, SqlExp exp,
                                  boolean outer, CharBuf s) {
        if (exp == null) {
            s.append(" CROSS JOIN ");
        } else if (outer) {
            s.append(" LEFT JOIN ");
        } else {
            s.append(" INNER JOIN ");
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
     * Append a join expression.
     */
    public void appendSqlJoin(String leftAlias, JdbcColumn left,
                              String rightAlias, JdbcColumn right, boolean outer,
                              CharBuf s) {
        s.append(leftAlias);
        s.append('.');
        s.append(left.name);
        s.append(' ');
        s.append('=');
        s.append(' ');
        s.append(rightAlias);
        s.append('.');
        s.append(right.name);
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
     * Must 'exists (select ...)' clauses be converted into a join and
     * distinct be added to the select (e.g. MySQL) ?
     */
    public boolean isConvertExistsToDistinctJoin() {
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
     * Must add expressions (+, -, string concat) be wrapped in brackets?
     */
    public boolean isExtraParens() {
        return true;
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
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return "SELECT version()";
    }

    /**
     * Does this database support autoincrement or serial columns?
     */
    public boolean isAutoIncSupported() {
        return true;
    }

    /**
     * Append the column auto increment part of a create table statement for a
     * column.
     */
    protected void appendCreateColumnAutoInc(JdbcTable t, JdbcColumn c,
                                             CharBuf s) {
        s.append(" AUTO_INCREMENT");
    }

    /**
     * Retrieve the value of the autoinc or serial column for a row just
     * inserted using stat on con.
     */
    public Object getAutoIncColumnValue(JdbcTable classTable,
            Connection con, Statement stat) throws SQLException {
        long id = ((com.mysql.jdbc.Statement)stat).getLastInsertID();
        switch (classTable.pk[0].javaTypeCode) {
            case MDStatics.BYTE:
            case MDStatics.BYTEW:
                return new Byte((byte) id);
            case MDStatics.SHORT:
            case MDStatics.SHORTW:
                return new Short((short) id);
            case MDStatics.LONG:
            case MDStatics.LONGW:
                return new Long(id);
            case MDStatics.BIGDECIMAL:
                return new BigDecimal(id);
            case MDStatics.BIGINTEGER:
                return new BigInteger(Long.toString(id));
        }
        return new Integer((int) id);
    }

    public boolean checkDDL(ArrayList tables, Connection con,
                            PrintWriter errors, PrintWriter fix, ControlParams params)
            throws SQLException {
        if (refConstraintsNotSupported) {
            params.setCheckConstraint(false);
        }
        return super.checkDDL(tables, con, errors, fix, params);
    }

    protected String getCatalog(Connection con) throws SQLException {
        String catalog = null;
        Statement stat = null;
        ResultSet rs = null;

        try {
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT DATABASE()");
            if (rs.next()) {
                catalog = rs.getString(1);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                }
            }
        }

        return catalog;
    } /*


ALTER [IGNORE] TABLE tbl_name alter_specification [, alter_specification ...]

alter_specification:
ADD [COLUMN] create_definition [FIRST | AFTER column_name ]
| ADD [COLUMN] (create_definition, create_definition,...)
| ADD INDEX [index_name] (index_col_name,...)
| ADD PRIMARY KEY (index_col_name,...)
| ADD UNIQUE [index_name] (index_col_name,...)
| ADD FULLTEXT [index_name] (index_col_name,...)
| ADD [CONSTRAINT symbol] FOREIGN KEY [index_name] (index_col_name,...)
[reference_definition]
| ALTER [COLUMN] col_name {SET DEFAULT literal | DROP DEFAULT}
| CHANGE [COLUMN] old_col_name create_definition
[FIRST | AFTER column_name]
| MODIFY [COLUMN] create_definition [FIRST | AFTER column_name]
| DROP [COLUMN] col_name
| DROP PRIMARY KEY
| DROP INDEX index_name
| DISABLE KEYS
| ENABLE KEYS
| RENAME [TO] new_tbl_name
| ORDER BY col
| table_options

    */

    /**
     * Add a Sequence column to implement a list
     * <p/>
     * <p/>
     * <p/>
     * /**
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
            s.append(" NULL");
            if (c.autoinc) {
                appendCreateColumnAutoInc(t, c, s);
            }
            s.append(getRunCommand());
            if (!c.nulls) {
                s.append("UPDATE ");
                s.append(t.name);
                s.append(" SET ");
                s.append(c.name);
                s.append(" = ");
                s.append(getDefaultForType(c));
                s.append(getRunCommand());

                s.append("ALTER TABLE ");
                s.append(t.name);
                s.append(" CHANGE COLUMN ");
                s.append(c.name);
                s.append(' ');
                s.append(c.name);
                s.append(' ');
                appendColumnType(c, s);
                appendCreateColumnNulls(t, c, s);
                if (c.autoinc) {
                    appendCreateColumnAutoInc(t, c, s);
                }
                s.append(getRunCommand());
            }
        }

    }

    /**
     * Append a column that needs to be added.
     */
    protected void appendModifyColumn(TableDiff tableDiff, ColumnDiff diff,
                                      CharBuf s, boolean comments) {
        JdbcTable t = tableDiff.getOurTable();
        JdbcColumn c = diff.getOurCol();
        if (comments && isCommentSupported() && c.comment != null) {
            s.append(comment("modify column for field " + c.comment));
        }
        if (comments && isCommentSupported() && c.comment == null) {
            s.append(comment("modify column " + c.name));
        }

        s.append("\n");
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" CHANGE COLUMN ");
        s.append(c.name);
        s.append(' ');
        s.append(c.name);
        s.append(' ');
        appendColumnType(c, s);
        appendCreateColumnNulls(t, c, s);
        if (c.autoinc) {
            appendCreateColumnAutoInc(t, c, s);
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
//        if (comments && isCommentSupported()) {
//            s.append(comment("dropping unknown constraint " + c.name));
//            s.append('\n');
//        }
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
//        if (comments && isCommentSupported()) {
//            s.append(comment("dropping unknown index "+ idx.name));
//            s.append('\n');
//        }
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" DROP INDEX ");
        s.append(idx.name);
    }

    /**
     * Add the primary key constraint in isolation.
     */
    protected void addPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" ADD PRIMARY KEY (");
        appendColumnNameList(t.pk, s);
        s.append(')');
    }

    /**
     * Drop the primary key constraint in isolation.
     */
    protected void dropPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" DROP PRIMARY KEY");
    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
                                      boolean comments) {
        String tempTableName = getTempTableName(t, 64);

        s.append(comment("create a temp table to store old table values."));
        s.append("\n");
        s.append("CREATE TABLE ");
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
        appendTableType(t, s);
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

        String mainTempTableName = getTempTableName(t, 64);
        String minTempTableName = getTempTableName(t, 64);
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

        s.append(comment("Generate a sequence number so that " +
                "we can implement a List."));
        s.append("\n");
        s.append(comment("create a temp table with a extra " +
                "identity column."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(mainTempTableName);
        s.append(" (\n    ");
        // create identity column
        s.append(identityColumnName);
        s.append(" BIGINT NOT NULL AUTO_INCREMENT,");
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        s.append("\n    CONSTRAINT ");
        s.append(t.pkConstraintName);
        s.append(" PRIMARY KEY (");
        s.append(identityColumnName);
        s.append(")\n)");

        s.append(getRunCommand());

        s.append(comment("insert a '0' in the sequence " +
                "column and copy the rest of the old table " +
                "into the temp table."));
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
        s.append("CREATE TABLE ");
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

        s.append(comment("populate table " + t.name +
                " with the new sequence column."));
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
