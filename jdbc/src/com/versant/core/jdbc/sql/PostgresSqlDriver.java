
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
import com.versant.core.jdbc.sql.conv.BooleanConverter;
import com.versant.core.jdbc.sql.conv.DateTimestampConverter;
import com.versant.core.jdbc.sql.conv.NoMinCharConverter;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.util.CharBuf;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * A driver for Postgres.
 */
public class PostgresSqlDriver extends SqlDriver {
    public static final String SQLPARAM_NUMERIC_CAST = "CAST(? as numeric)";
    public static final String SQLPARAM_REAL_CAST = "CAST(? as real)";

    public final static char[] CHAR_SQLPARAM_NUMERIC_CAST = SQLPARAM_NUMERIC_CAST.toCharArray();
    public final static char[] CHAR_SQLPARAM_REAL_CAST = SQLPARAM_REAL_CAST.toCharArray();

    private boolean dropTableRequiresCascade;
    private String version = null;
    private HashMap sqlTypeCache;
    private HashMap pgTypeCache;

    private static final String jdbc1Types[] = {
        "int2", "int4", "oid", "int8", "cash", "money", "numeric", "float4", "float8", "bpchar",
        "char", "char2", "char4", "char8", "char16", "varchar", "text", "name", "filename", "bytea",
        "bool", "date", "time", "abstime", "timestamp", "timestamptz"
    };
    private static final int jdbc1Typei[] = {
        5, 4, 4, -5, 8, 8, 2, 7, 8, 1,
        1, 1, 1, 1, 1, 12, 2005, 12, 12, 2004,
        -7, 91, 92, 93, 93, 93
    };

    private static char[] FOR_UPDATE = " FOR UPDATE OF ".toCharArray();

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "postgres";
    }

    public boolean isPutOrderColsInSelect() {
        return true;
    }

    public boolean isCustomizeForServerRequired() {
        return true;
    }

    public boolean isCommentSupported() {
        return true;
    }

    /**
     * Find out what version of Postgres con is for and adapt.
     */
    public void customizeForServer(Connection con) throws SQLException {
        String s = getVersion(con);
        int i = s.indexOf(' ') + 1;
        int j = s.indexOf('.');
        int k = j;
        for (;;) {
            char c = s.charAt(++k);
            if ((c < '0') || (c > '9')) break;
        }
        int major = Integer.parseInt(s.substring(i, j));
        int minor = Integer.parseInt(s.substring(j + 1, k));
        dropTableRequiresCascade = (major >= 7 && minor >= 3) || major >= 8;
    }

    private String parseVersion(String version){
        int i = version.indexOf(' ') + 1;
        int j = version.indexOf('.');
        int k = j;
        for (; ;) {
            char c = version.charAt(++k);
            if ((c < '0') || (c > '9')) break;
        }
        int major = Integer.parseInt(version.substring(i, j));
        int minor = Integer.parseInt(version.substring(j + 1, k));
        return major + "." + minor+".";

    }

    private String getVersion(Connection con) throws SQLException {
        if (version != null){
            return version;
        } else {
            Statement stat = null;
            ResultSet rs = null;
            try {
                stat = con.createStatement();
                rs = stat.executeQuery("SELECT version()");
                rs.next();
                String ver = rs.getString(1);
                con.commit();
                version = parseVersion(ver);
                return version;
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
            case Types.TINYINT:
                return new JdbcTypeMapping("SMALLINT",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("TEXT",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("BYTEA",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                    nullBytesAsBinaryConverterFactory);
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
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Boolean.TYPE/*RIGHTPAR*/)).setConverterFactory(bcf);
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Boolean.class/*RIGHTPAR*/)).setConverterFactory(bcf);

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Date.class/*RIGHTPAR*/)).setConverterFactory(dtcf);

        NoMinCharConverter.Factory f = new NoMinCharConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Character.class/*RIGHTPAR*/, Types.CHAR, 1, 0,
            JdbcJavaTypeMapping.TRUE, f));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Character.TYPE/*RIGHTPAR*/, Types.CHAR, 1, 0,
            JdbcJavaTypeMapping.FALSE, f));

        return ans;
    }

    public String getAliasPrepend() {
        return " as ";
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

    public boolean isFetchSizeSupported() {
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
     * Is null a valid value for a column with a foreign key constraint?
     */
    public boolean isNullForeignKeyOk() {
        return true;
    }

    public boolean isAutoIncSupported() {
        return true;
    }

    public Object getAutoIncColumnValue(JdbcTable classTable, Connection con,
            Statement stat) throws SQLException {
        String sql = "SELECT currval('" + classTable.name + "_" +
            classTable.pk[0].name + "_seq')";
        Statement s = null;
        ResultSet rs = null;
        try {
            s = con.createStatement();
            rs = s.executeQuery(sql);
            rs.next();
            if (classTable.pk[0].jdbcType == Types.BIGINT) {
                return new Long(rs.getLong(1));
            } else {
                return new Integer(rs.getInt(1));
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (s != null) {
                try {
                    s.close();
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
        n.setMaxColumnNameLength(31);
        n.setMaxTableNameLength(31);
        n.setMaxConstraintNameLength(31);
        n.setMaxIndexNameLength(31);
        return n;
    }

    /**
     * Append the part of a create table statement for a column.
     */
    protected void appendCreateColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {
        if (c.autoinc) {
            int si = s.size();
            s.append(c.name);
            if (c.jdbcType == Types.BIGINT) {
                s.append(" SERIAL8");
            } else {
                s.append(" SERIAL");
            }
            s.append(',');
            if (comments && c.comment != null) {
                s.append(' ');
                si += COMMENT_COL;
                for (; s.size() < si; s.append(' '));
                s.append(comment(c.comment));
            }
        } else {
            super.appendCreateColumn(t, c, s, comments);
        }
    }

    /**
     * Append the allow nulls part of the definition for a column in a
     * create table statement.
     */
    protected void appendCreateColumnNulls(JdbcTable t, JdbcColumn c,
            CharBuf s) {
        if (!c.nulls) s.append(" NOT NULL");
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
        s.append(")");
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
        if (exp == null) {
            s.append(" CROSS JOIN ");
        } else if (outer) {
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
     * Append a replacable parameter part of a where clause for the column.
     * This gives the driver a chance to embed type conversions and so on
     * for types not handled well by the JDBC driver (e.g. BigDecimals for
     * the postgres JDBC driver).
     */
    public void appendWhereParam(CharBuf s, JdbcColumn c) {
        switch (c.jdbcType) {
            case Types.NUMERIC:
                s.append("CAST(? as numeric)");
                break;
            case Types.REAL:
                s.append("CAST(? as real)");
                break;
            default:
                super.appendWhereParam(s, c);
        }
    }

    /**
     * Get a String for a replacable parameter. This gives the driver a
     * chance to embed type conversions and so on for types not handled well
     * by the JDBC driver (e.g. BigDecimals and the postgres JDBC driver).
     */
    public String getSqlParamString(int jdbcType) {
        switch (jdbcType) {
            case Types.NUMERIC:
                return SQLPARAM_NUMERIC_CAST;
            case Types.REAL:
                return SQLPARAM_REAL_CAST;
            default:
                return "?";
        }
    }

    public char[] getSqlParamStringChars(int jdbcType) {
        switch (jdbcType) {
            case Types.NUMERIC:
                return CHAR_SQLPARAM_NUMERIC_CAST;
            case Types.REAL:
                return CHAR_SQLPARAM_REAL_CAST;
            default:
                return DEFAULT_PARAM_CHARS;
        }
    }

    /**
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return "SELECT datname FROM pg_database";
    }

    /**
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        CharBuf s = new CharBuf(64);
        s.append("DROP TABLE ");
        s.append(table);
        if (dropTableRequiresCascade) s.append(" CASCADE");
        stat.execute(s.toString());
    }

    /**
     * Get whatever needs to be appended to a SELECT statement to lock the
     * rows if this makes sense for the database. This must have a leading
     * space if not empty.
     */
    public char[] getSelectForUpdate() {
        return FOR_UPDATE;
    }

    /**
     * Does 'SELECT FOR UPDATE' require the main table of the query to be
     * appended (ala postgres)?
     */
    public boolean isSelectForUpdateAppendTable() {
        return true;
    }

    /**
     * Can 'SELECT FOR UPDATE' be used with a DISTINCT?
     */
    public boolean isSelectForUpdateWithDistinctOk() {
        return false;
    }

    protected boolean isValidSchemaTable(String tableName) {
        String[] sysNames = new String[]{"pga_forms",
                                         "pga_queries",
                                         "pga_reports",
                                         "pga_schema",
                                         "pga_scripts"};

        for (int i = 0; i < sysNames.length; i++) {
            if (sysNames[i].equals(tableName)) {
                return false;
            }
        }
        return true;
    }




    /**
     * Get the JdbcTables from the database for the given database con.
     * @param con
     * @return HashMap of tablename.toLowerCase() as key and JdbcTable as value
     * @throws SQLException on DB errors
     */
    public HashMap getDBSchema(Connection con, ControlParams params) throws SQLException {
        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables
        // now we do columns
        params.setColumnsOnly(false);
        String tableName = null;
        initTypeCache(con);
        String columnSql = null;
        if (haveMinimumServerVersion("7.3",con)) {
            columnSql =
                    "SELECT n.nspname,c.relname,a.attname,a.atttypid,a.attnotnull,a.atttypmod,a.attlen,a.attnum,def.adsrc,dsc.description  " +
                    "  FROM pg_catalog.pg_namespace n  " +
                    "       JOIN pg_catalog.pg_class c ON (c.relnamespace = n.oid)  " +
                    "       JOIN pg_catalog.pg_attribute a ON (a.attrelid=c.oid)  " +
                    "       LEFT JOIN pg_catalog.pg_attrdef def ON (a.attrelid=def.adrelid AND a.attnum = def.adnum)  " +
                    "       LEFT JOIN pg_catalog.pg_description dsc ON (c.oid=dsc.objoid AND a.attnum = dsc.objsubid)  " +
                    "       LEFT JOIN pg_catalog.pg_class dc ON (dc.oid=dsc.classoid AND dc.relname='pg_class')  " +
                    "       LEFT JOIN pg_catalog.pg_namespace dn ON (dc.relnamespace=dn.oid AND dn.nspname='pg_catalog')  " +
                    " WHERE a.attnum > 0 " +
                    "   AND NOT a.attisdropped "+
                    "   AND n.nspname LIKE 'public'";

        } else if (haveMinimumServerVersion("7.1", con)) {
            columnSql =
                    "SELECT NULL::text AS nspname,c.relname,a.attname,a.atttypid,a.attnotnull,a.atttypmod,a.attlen,a.attnum,def.adsrc,dsc.description  " +
                    "  FROM pg_class c  " +
                    "       JOIN pg_attribute a ON (a.attrelid=c.oid)  " +
                    "       LEFT JOIN pg_attrdef def ON (a.attrelid=def.adrelid AND a.attnum = def.adnum)  " +
                    "       LEFT JOIN pg_description dsc ON (c.oid=dsc.objoid AND a.attnum = dsc.objsubid)  " +
                    "       LEFT JOIN pg_class dc ON (dc.oid=dsc.classoid AND dc.relname='pg_class')  " +
                    " WHERE a.attnum > 0 "+
                    "   AND c.relname NOT LIKE('pg_%')";
        } else {
            columnSql =
                    "SELECT NULL::text AS nspname,c.relname,a.attname,a.atttypid,a.attnotnull,a.atttypmod,a.attlen,a.attnum,NULL AS adsrc,NULL AS description  " +
                    "  FROM pg_class c, " +
                    "       pg_attribute a  " +
                    " WHERE a.attrelid=c.oid AND a.attnum > 0 ";
        }
        columnSql = columnSql + " ORDER BY nspname,relname,attnum ";

        Statement statCol = con.createStatement();
        ResultSet rsColumn = statCol.executeQuery(columnSql);
        ArrayList columns = null;

        while (rsColumn.next()) {

            String temptableName = rsColumn.getString(2);

            if (!isValidSchemaTable(temptableName)) {
                continue;
            }

            if (tableName == null) { // this is the first one
                tableName = temptableName;
                columns = new ArrayList();
                JdbcTable jdbcTable = new JdbcTable();
                jdbcTable.name = tableName;
                jdbcTableMap.put(tableName, jdbcTable);
            }

            if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                JdbcColumn[] jdbcColumns = new JdbcColumn[columns.size()];
                columns.toArray(jdbcColumns);
                JdbcTable jdbcTable0 = (JdbcTable) jdbcTableMap.get(tableName);
                jdbcTable0.cols = jdbcColumns;


                tableName = temptableName;
                columns.clear();
                JdbcTable jdbcTable1 = new JdbcTable();
                jdbcTable1.name = tableName;
                jdbcTableMap.put(tableName, jdbcTable1);
            }

            JdbcColumn col = new JdbcColumn();
            int typeOid = rsColumn.getInt("atttypid");
            col.name = rsColumn.getString(3);
            String pgType = getPGType(typeOid);
            col.sqlType = pgType;
            col.jdbcType = getSQLType(typeOid);
            col.scale = 0;
            if (pgType.equals("bpchar") || pgType.equals("varchar")) {
                int atttypmod = rsColumn.getInt("atttypmod");
                col.length = atttypmod == -1 ? 0 : atttypmod - 4;
            } else if (pgType.equals("numeric") || pgType.equals("decimal")) {
                int attypmod = rsColumn.getInt("atttypmod") - 4;  // was index 8
                col.length = attypmod >> 16 & 65535;
                col.scale = attypmod & 65535;
            } else if (pgType.equals("bit") || pgType.equals("varbit")) {
                col.length = rsColumn.getInt("atttypmod");
            } else {
                col.length = rsColumn.getInt("attlen");
            }
            col.nulls = !rsColumn.getBoolean("attnotnull");

            switch (col.jdbcType) {
                case java.sql.Types.BIT:
                case java.sql.Types.TINYINT:
                case java.sql.Types.SMALLINT:
                case java.sql.Types.BIGINT:
                case java.sql.Types.INTEGER:
                case java.sql.Types.DATE:
                case java.sql.Types.TIME:
                case java.sql.Types.TIMESTAMP:
                    col.length = 0;
                    col.scale = 0;
                default:
            }

            columns.add(col);
        }
        // we fin last table
        if (columns != null){
            JdbcColumn[] jdbcColumns = new JdbcColumn[columns.size()];
            columns.toArray(jdbcColumns);
            JdbcTable colJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
            if (colJdbcTable != null){
                colJdbcTable.cols = jdbcColumns;
            }
            columns.clear();
        }
        tableName = null;


        // clean up
        if (rsColumn != null) {
            try {
                rsColumn.close();
            } catch (SQLException e) {
            }
        }
        if (statCol != null) {
            try {
                statCol.close();
            } catch (SQLException e) {
            }
        }
        if (!params.checkColumnsOnly()) {
            if (params.isCheckPK()) {
                // now we do primaryKeys
                HashMap pkMap = null;

                String where = "AND ct.relname NOT LIKE('pg_%')";
                String from;
                if (haveMinimumServerVersion("7.3",con)) {
                    from = " FROM pg_catalog.pg_namespace n, " +
                            "pg_catalog.pg_class ct, " +
                            "pg_catalog.pg_class ci, " +
                            "pg_catalog.pg_attribute a, " +
                            "pg_catalog.pg_index i ";
                    where = " AND ct.relnamespace = n.oid " +
                            " AND n.nspname = 'public' ";
                } else {
                    from = " FROM pg_class ct, " +
                            "      pg_class ci, " +
                            "      pg_attribute a, " +
                            "      pg_index i ";
                }
                String pkSql =
                        " SELECT ct.relname AS TABLE_NAME, " +
                        "        a.attname AS COLUMN_NAME, " +
                        "        a.attnum AS KEY_SEQ, " +
                        "        ci.relname AS PK_NAME " +
                        from +
                        " WHERE ct.oid=i.indrelid " +
                        " AND ci.oid=i.indexrelid " +
                        " AND a.attrelid=ci.oid " +
                        " AND i.indisprimary " +
                        where +
                        " ORDER BY table_name, pk_name, key_seq";

                Statement statPK = con.createStatement();
                ResultSet rsPKs = statPK.executeQuery(pkSql);
                int pkCount = 0;
                String pkName = null;
                while (rsPKs.next()) {
                    String temptableName = rsPKs.getString(1);

                    if (!jdbcTableMap.containsKey(temptableName)) {
                        continue;
                    }

                    if (tableName == null) { // this is the first one
                        tableName = temptableName;
                        pkMap = new HashMap();
                    }

                    if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                        JdbcColumn[] pkColumns = new JdbcColumn[pkCount];
                        int indexOfPKCount = 0;
                        JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                        for (int i = 0; i < jdbcTable.cols.length; i++) {
                            JdbcColumn jdbcColumn = jdbcTable.cols[i];
                            if (pkMap.containsKey(jdbcColumn.name)) {
                                pkColumns[indexOfPKCount] = jdbcColumn;
                                jdbcColumn.pk = true;
                                indexOfPKCount++;
                            }
                        }
                        jdbcTable.pk = pkColumns;
                        jdbcTable.pkConstraintName = pkName;

                        // PK's become tables, remove them
                        jdbcTableMap.remove(pkName);

                        tableName = temptableName;
                        pkMap.clear();
                        pkCount = 0;
                    }
                    pkCount++;
                    pkMap.put(rsPKs.getString(2), null);
                    pkName = rsPKs.getString(4);
                }
                JdbcColumn[] pkColumns = new JdbcColumn[pkCount];
                int indexOfPKCount = 0;
                JdbcTable pkJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                if (pkJdbcTable != null) {
                    for (int i = 0; i < pkJdbcTable.cols.length; i++) {
                        JdbcColumn jdbcColumn = pkJdbcTable.cols[i];
                        if (pkMap.containsKey(jdbcColumn.name)) {
                            pkColumns[indexOfPKCount] = jdbcColumn;
                            jdbcColumn.pk = true;
                            indexOfPKCount++;
                        }
                    }
                    pkJdbcTable.pk = pkColumns;
                    pkJdbcTable.pkConstraintName = pkName;

                    // PK's become tables, remove them
                    jdbcTableMap.remove(pkName);
                }
                tableName = null;
                // clean up
                if (rsPKs != null) {
                    try {
                        rsPKs.close();
                    } catch (SQLException e) {
                    }
                }
                if (statPK != null) {
                    try {
                        statPK.close();
                    } catch (SQLException e) {
                    }
                }
            }
            if (params.isCheckIndex()) {
                // now we do index
                String where = "AND ct.relname NOT LIKE('pg_%') ";
                String from;
                if (haveMinimumServerVersion("7.3",con)) {
                    from = " FROM pg_catalog.pg_namespace n, \n" +
                            "      pg_catalog.pg_class ct, \n" +
                            "      pg_catalog.pg_class ci, \n" +
                            "      pg_catalog.pg_index i, \n" +
                            "      pg_catalog.pg_attribute a, \n" +
                            "      pg_catalog.pg_am am \n";
                    where = "  AND n.oid = ct.relnamespace \n" +
                            "  AND n.nspname = 'public' \n";
                } else {
                    from = " FROM pg_class ct, \n" +
                            "      pg_class ci, \n" +
                            "      pg_index i, \n" +
                            "      pg_attribute a, \n" +
                            "      pg_am am \n";
                }
                String indexSql =
                        "SELECT ct.relname AS TABLE_NAME, \n" +
                        "       a.attname AS COLUMN_NAME, \n" +
                        "       ci.relname AS INDEX_NAME, \n" +
                        "       NOT i.indisunique AS NON_UNIQUE, \n" +
                        "       CASE i.indisclustered \n" +
                        "       WHEN true THEN " + 1 + "\n" +
                        "       ELSE CASE am.amname \n" +
                        "           WHEN 'hash' THEN " + 2 + "\n" +
                        "           ELSE " + 3 + "\n" +
                        "           END \n" +
                        "       END AS TYPE, \n" +
                        "       a.attnum AS ORDINAL_POSITION \n" +
                        from +
                        " WHERE ct.oid = i.indrelid \n" +
                        "   AND ci.oid = i.indexrelid \n" +
                        "   AND a.attrelid = ci.oid \n" +
                        "   AND ci.relam=am.oid \n" +
                        where +
                        " ORDER BY TABLE_NAME, INDEX_NAME, ORDINAL_POSITION \n";
                Statement statIndex = con.createStatement();
//                System.out.println("\n\n\n\n");
//                System.out.println("--"+getVersion(con));
//                System.out.println(indexSql);
//                System.out.println("\n\n\n\n");
                ResultSet rsIndex = statIndex.executeQuery(indexSql);

                HashMap indexNameMap = null;
                ArrayList indexes = null;
                while (rsIndex.next()) {
                    String temptableName = rsIndex.getString(1);
                    if (tableName == null) { // this is the first one
                        tableName = temptableName;
                        indexNameMap = new HashMap();
                        indexes = new ArrayList();
                    }

                    String indexName = rsIndex.getString(3);
                    JdbcTable tempJdbcTable = (JdbcTable) jdbcTableMap.get(temptableName);


                    if (indexName != null && !indexName.equals(tempJdbcTable.pkConstraintName)) {
                        if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                            JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                            JdbcIndex[] jdbcIndexes = new JdbcIndex[indexes.size()];
                            indexes.toArray(jdbcIndexes);
                            jdbcTable.indexes = jdbcIndexes;


                            tableName = temptableName;
                            indexes.clear();
                            indexNameMap.clear();

                        }
                        JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                        if (indexNameMap.containsKey(indexName)) {
                            JdbcIndex index = null;
                            for (Iterator iter = indexes.iterator(); iter.hasNext();) {
                                JdbcIndex jdbcIndex = (JdbcIndex) iter.next();
                                if (jdbcIndex.name.equals(indexName)) {
                                    index = jdbcIndex;
                                }
                            }

                            JdbcColumn[] tempIndexColumns = index.cols;
                            JdbcColumn[] indexColumns = new JdbcColumn[tempIndexColumns.length + 1];
                            System.arraycopy(tempIndexColumns, 0, indexColumns, 0, tempIndexColumns.length);
                            String colName = rsIndex.getString(2);
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equals(jdbcColumn.name)) {
                                    indexColumns[tempIndexColumns.length] = jdbcColumn;
                                    jdbcColumn.partOfIndex = true;
                                }
                            }
                            index.setCols(indexColumns);
                        } else {
                            indexNameMap.put(indexName, null);
                            JdbcIndex index = new JdbcIndex();
                            index.name = indexName;
                            // index's become tables, remove them
                            jdbcTableMap.remove(indexName);
                            index.unique = !rsIndex.getBoolean(4);
                            short indexType = rsIndex.getShort(5);
                            switch (indexType) {
                                case DatabaseMetaData.tableIndexClustered:
                                    index.clustered = true;
                                    break;
                            }
                            String colName = rsIndex.getString(2);
                            JdbcColumn[] indexColumns = new JdbcColumn[1];
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equals(jdbcColumn.name)) {
                                    indexColumns[0] = jdbcColumn;
                                    jdbcColumn.partOfIndex = true;
                                }
                            }
                            index.setCols(indexColumns);
                            indexes.add(index);
                        }
                    }
                }
                JdbcTable indexJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                if (indexJdbcTable != null) {
                    JdbcIndex[] jdbcIndexes = new JdbcIndex[indexes.size()];
                    indexes.toArray(jdbcIndexes);
                    indexJdbcTable.indexes = jdbcIndexes;
                    indexes.clear();
                    indexNameMap.clear();
                }
                tableName = null;
                // clean up
                if (rsIndex != null) {
                    try {
                        rsIndex.close();
                    } catch (SQLException e) {
                    }
                }
                if (statIndex != null) {
                    try {
                        statIndex.close();
                    } catch (SQLException e) {
                    }
                }
            }
            if (params.isCheckConstraint()) {
                // now we do forign keys
                String where = "";

                String from;
                if (haveMinimumServerVersion("7.3",con)) {

                    from =
                            "  FROM pg_catalog.pg_namespace n1  " +
                            "       JOIN pg_catalog.pg_class c1 ON (c1.relnamespace = n1.oid)  " +
                            "       JOIN pg_catalog.pg_index i ON (c1.oid = i.indrelid)  " +
                            "       JOIN pg_catalog.pg_class ic ON (i.indexrelid = ic.oid)  " +
                            "       JOIN pg_catalog.pg_attribute a ON (ic.oid = a.attrelid),  " +
                            "       pg_catalog.pg_namespace n2 JOIN pg_catalog.pg_class c2 ON (c2.relnamespace = n2.oid),  " +
                            "       pg_catalog.pg_trigger t1 JOIN pg_catalog.pg_proc p1 ON (t1.tgfoid = p1.oid),  " +
                            "       pg_catalog.pg_trigger t2 JOIN pg_catalog.pg_proc p2 ON (t2.tgfoid = p2.oid) ";
                    where = " AND n2.nspname = 'public' ";

                } else {

                    from = " FROM pg_class c1  " +
                            "     JOIN pg_index i ON (c1.oid = i.indrelid)  " +
                            "     JOIN pg_class ic ON (i.indexrelid = ic.oid)  " +
                            "     JOIN pg_attribute a ON (ic.oid = a.attrelid),  " +
                            "     pg_class c2,  " +
                            "     pg_trigger t1 JOIN pg_proc p1 ON (t1.tgfoid = p1.oid),  " +
                            "     pg_trigger t2  JOIN pg_proc p2 ON (t2.tgfoid = p2.oid) ";
                }
                String fkSql =
                            "SELECT c1.relname as PKTABLE_NAME, " + //PKTABLE_NAME
                            "       c2.relname as FKTABLE_NAME, " + //FKTABLE_NAME
                            "       t1.tgconstrname, " +
                            "       a.attnum as keyseq, " +
                            "       ic.relname as fkeyname, " +
                            "       t1.tgdeferrable, " +
                            "       t1.tginitdeferred, " +
                            "       t1.tgnargs," +
                            "       t1.tgargs " +
                            from +
                            "WHERE (t1.tgrelid=c1.oid " +
                            "      AND t1.tgisconstraint " +
                            "      AND t1.tgconstrrelid=c2.oid " +
                            "      AND p1.proname LIKE 'RI\\\\_FKey\\\\_%\\\\_upd') " +
                            "  AND (t2.tgrelid=c1.oid " +
                            "      AND t2.tgisconstraint " +
                            "      AND t2.tgconstrrelid=c2.oid " +
                            "      AND p2.proname LIKE 'RI\\\\_FKey\\\\_%\\\\_del') " +
                            "  AND i.indisprimary " +
                            where +
                            "ORDER BY FKTABLE_NAME , tgconstrname, keyseq";
                Statement statFK = con.createStatement();
                ResultSet rsFKs = statFK.executeQuery(fkSql);

                HashMap constraintNameMap = new HashMap();
                HashMap doneMap = new HashMap();


                while (rsFKs.next()) {
                    String targs = rsFKs.getString(9);
                    StringTokenizer st = new StringTokenizer(targs, "\\000");

                    String constName = null;
                    String srcTableName = null;
                    String destTableName = null;
                    ArrayList srcColNames = new ArrayList();

                    if (st.hasMoreTokens()){ //0
                        constName = st.nextToken();
                    }
                    if (st.hasMoreTokens()) { //1
                        srcTableName = st.nextToken();
                    }
                    if (st.hasMoreTokens()) { //2
                        destTableName = st.nextToken();
                        st.nextToken();//3 UNSPECIFIED
                    }
                    while (st.hasMoreTokens()){
                        srcColNames.add(st.nextToken());
                        if (st.hasMoreTokens()){
                            st.nextToken();
                        }
                    }
                    JdbcTable srcJdbcTable = (JdbcTable) jdbcTableMap.get(srcTableName);


                    String doneName = srcTableName + constName;
                    if (srcJdbcTable == null){
                        doneMap.put(doneName, null);
                        continue;
                    } else if (doneMap.containsKey(doneName)){
                        continue;
                    } else {
                        doneMap.put(doneName,null);
                    }



                    JdbcTable destJdbcTable = (JdbcTable) jdbcTableMap.get(destTableName);
                    JdbcConstraint jdbcConstraint = new JdbcConstraint();
                    jdbcConstraint.name = constName;
                    jdbcConstraint.src = srcJdbcTable;
                    jdbcConstraint.dest = destJdbcTable;
                    JdbcColumn[] constraintColumns = new JdbcColumn[srcColNames.size()];
                    int j = 0;
                    for (Iterator iter = srcColNames.iterator(); iter.hasNext();j++) {
                        String colName = (String) iter.next();
                        for (int i = 0; i < srcJdbcTable.cols.length; i++) {
                            JdbcColumn jdbcColumn = srcJdbcTable.cols[i];
                            if (colName.equals(jdbcColumn.name)) {
                                constraintColumns[j] = jdbcColumn;
                                jdbcColumn.foreignKey = true;
                            }
                        }
                    }
                    jdbcConstraint.srcCols = constraintColumns;
                    if (constraintNameMap.containsKey(srcJdbcTable)){
                        ArrayList list = (ArrayList)constraintNameMap.get(srcJdbcTable);
                        list.add(jdbcConstraint);
                    } else {
                        ArrayList list = new ArrayList();
                        list.add(jdbcConstraint);
                        constraintNameMap.put(srcJdbcTable, list);
                    }
                }
                for (Iterator iter = constraintNameMap.keySet().iterator(); iter.hasNext();) {
                    JdbcTable jdbcTable = (JdbcTable) iter.next();
                    if (jdbcTable != null) {
                        ArrayList list = (ArrayList) constraintNameMap.get(jdbcTable);
                        JdbcConstraint[] jdbcConstraints = new JdbcConstraint[list.size()];
                        list.toArray(jdbcConstraints);
                        jdbcTable.constraints = jdbcConstraints;
                    }
                }

                if (rsFKs != null) {
                    try {
                        rsFKs.close();
                    } catch (SQLException e) {
                    }
                }
                if (statFK != null) {
                    try {
                        statFK.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }


        HashMap returnMap = new HashMap();
        Collection col = jdbcTableMap.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            JdbcTable table = (JdbcTable) iterator.next();
            returnMap.put(table.name.toLowerCase(), table);
        }
        fixAllNames(returnMap);
        return returnMap;
    }

    public boolean haveMinimumServerVersion(String ver, Connection con) throws SQLException {
        return getVersion(con).compareTo(ver) >= 0;
    }


    public int getSQLType(String pgTypeName) {
        int sqlType = 1111;
        for (int i = 0; i < jdbc1Types.length; i++) {
            if (!pgTypeName.equals(jdbc1Types[i])) {
                continue;
            }
            sqlType = jdbc1Typei[i];
            break;
        }

        return sqlType;
    }

    /**
     * remember to init the type cash first.
     * @param oid
     * @return
     * @throws SQLException
     */
    public String getPGType(int oid) throws SQLException {
        return (String) pgTypeCache.get(new Integer(oid));
    }

    /**
     * remember to init the type cash first.
     * @param oid
     * @return
     * @throws SQLException
     */
    public int getSQLType(int oid) throws SQLException {
        Integer sqlType = (Integer) sqlTypeCache.get(new Integer(oid));
        return sqlType.intValue();
    }

    private void initTypeCache(Connection con) throws SQLException {
        sqlTypeCache = new HashMap();
        pgTypeCache = new HashMap();

        String sql;
        if (haveMinimumServerVersion("7.3", con)) {
            sql = "SELECT typname,oid FROM pg_catalog.pg_type ";
        } else {
            sql = "SELECT typname,oid FROM pg_type ";
        }
        Statement stat = con.createStatement();
        ResultSet rs = stat.executeQuery(sql);
        String pgType;
        while (rs.next()) {
            pgType = rs.getString(1);
            Integer iOid = new Integer(rs.getInt(2));
            Integer sqlType = new Integer(getSQLType(pgType));
            sqlTypeCache.put(iOid, sqlType);
            pgTypeCache.put(iOid, pgType);
        }
        // do last type
        pgType = "opaque";
        Integer iOid = new Integer(0);
        Integer sqlType = new Integer(getSQLType(pgType));
        sqlTypeCache.put(iOid, sqlType);
        pgTypeCache.put(iOid, pgType);
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
            s.append(" ADD ");
            s.append(c.name);
            s.append(' ');
            appendColumnType(c, s);
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
                s.append(" ALTER ");
                s.append(c.name);
                s.append(" SET NOT NULL");
                s.append(getRunCommand());
            }
        }
    }


    /**
     * Append a column that needs to be added.
     */
    protected void appendModifyColumn(TableDiff tableDiff, ColumnDiff diff,  CharBuf s,
                                      boolean comments) {
        JdbcTable t = tableDiff.getOurTable();
        JdbcColumn c = diff.getOurCol();
        boolean length = diff.isLenghtDiff();
        boolean scale = diff.isScaleDiff();
        boolean nulls = diff.isNullDiff();
        boolean type = diff.isTypeDiff();
        if (comments && isCommentSupported() && c.comment != null) {
            s.append(comment("modify column for field " + c.comment));
        }
        if (comments && isCommentSupported() && c.comment == null) {
            s.append(comment("modify column " + c.name));
        }

        s.append("\n");
        if (length || scale || type){
            String tempcolumn = getTempColumnName(t);
            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" RENAME ");
            s.append(c.name);
            s.append(" TO ");
            s.append(tempcolumn);
            s.append(getRunCommand());

            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" ADD ");
            s.append(c.name);
            s.append(' ');
            appendColumnType(c, s);
            s.append(getRunCommand());

            s.append("UPDATE ");
            s.append(t.name);
            s.append(" SET ");
            s.append(c.name);
            s.append(" = ");
            s.append(tempcolumn);
            s.append("::");
            appendColumnType(c, s);
            s.append(getRunCommand());


            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" DROP COLUMN ");
            s.append(tempcolumn);

            if (!c.nulls) {
                s.append(getRunCommand());
                s.append("ALTER TABLE ");
                s.append(t.name);
                s.append(" ALTER COLUMN ");
                s.append(c.name);
                s.append(" SET NOT NULL");
            }

        } else if (nulls){
            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" ALTER COLUMN ");
            s.append(c.name);
            if (!c.nulls) {
                s.append(" SET NOT NULL");
            } else {
                s.append(" DROP NOT NULL");
            }
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
    protected void appendRefDropConstraint(CharBuf s, JdbcConstraint c, boolean comments) {
//        if (comments && isCommentSupported()) {
//            s.append(comment("dropping unknown constraint " + c.name));
//            s.append('\n');
//        }
        s.append("ALTER TABLE ");
        s.append(c.src.name);
        s.append(" DROP CONSTRAINT ");
        s.append(c.name);
        if (dropTableRequiresCascade){
            s.append(" CASCADE");
        }
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
        s.append("DROP INDEX ");
        s.append(idx.name);
        if (dropTableRequiresCascade) {
            s.append(" CASCADE");
        }
    }

    /**
     * Add the primary key constraint in isolation.
     */
    protected void addPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" ADD ");
        appendPrimaryKeyConstraint(t, s);
    }

    /**
     * Drop the primary key constraint in isolation.
     */
    protected void dropPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" DROP CONSTRAINT ");
        s.append(t.pkConstraintName);
        if (dropTableRequiresCascade) {
            s.append(" CASCADE");
        }
    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {
        String tempTableName = getTempTableName(t, 31);

        s.append(comment("create a temp table to store old table values."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(tempTableName);
        s.append(" (\n");
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first)
                first = false;
            else
                s.append("\n");
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
        if (dropTableRequiresCascade) s.append(" CASCADE");
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
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {

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


        s.append(comment("Generate a sequence number so that we can implement a List."));
        s.append("\n");
        s.append(comment("create a temp table with a extra identity column."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(mainTempTableName);
        s.append(" (\n    ");
        // create identity column
        s.append(identityColumnName);
        s.append(" SERIAL,");
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        int lastIndex = s.toString().lastIndexOf(',');
        s.replace(lastIndex, lastIndex + 1, ' ');// we take the last ',' out.
        s.append("\n)");


        s.append(getRunCommand());


        s.append(comment("insert a '0' in the sequence column and copy the rest of the old table into the temp table."));
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
        if (dropTableRequiresCascade) s.append(" CASCADE");

        s.append(getRunCommand());


        s.append(comment("recreate table " + t.name + "."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(t.name);
        s.append(" (\n");
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first)
                first = false;
            else
                s.append("\n");
            s.append("    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        s.append("\n    ");
        appendPrimaryKeyConstraint(t, s);
        s.append("\n)");
        appendTableType(t, s);


        s.append(getRunCommand());

        s.append(comment("populate table " + t.name + " with the new sequence column."));
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
        if (dropTableRequiresCascade) s.append(" CASCADE");
        s.append(getRunCommand());

        if (!dropTableRequiresCascade) {
            s.append(comment("drop sequence."));
            s.append("\n");
            s.append("DROP SEQUENCE ");
            s.append(mainTempTableName);
            s.append("_");
            s.append(identityColumnName);
            s.append("_seq");
            s.append(getRunCommand());
        }

        s.append("DROP TABLE ");
        s.append(minTempTableName);
        if (dropTableRequiresCascade) s.append(" CASCADE");
        s.append(getRunCommand());
    }


        /*
CREATE TABLE temp AS SELECT * FROM distributors;
DROP TABLE distributors;
CREATE TABLE distributors AS SELECT * FROM temp;
DROP TABLE temp;*/
//        protected void fixColumnsNonDirect (TableDiff tableDiff, PrintWriter out) {
//
//            JdbcTable ourTable = tableDiff.getOurTable();
//            String tempTableName = getTempTableName(ourTable, 128);
//
//
//            CharBuf s = new CharBuf();
//            s.append("CREATE TABLE ");
//            s.append(tempTableName);  //ourTable.name
//            s.append(" (");
//            JdbcColumn[] cols = ourTable.getColsForCreateTable();
//            int nc = cols.length;
//            for (int i = 0; i < nc; i++) {
//                appendCreateColumn(ourTable, cols[i], s, false);
//                s.append(' ');
//            }
//            appendPrimaryKeyConstraint(ourTable, s);
//            s.append(")");
//            s.append(getRunCommand());
//
//
//            s.append("INSERT INTO ");
//            s.append(tempTableName);  //ourTable.name
//            s.append(" (");
//            for (int i = 0; i < nc; i++) {
//                s.append(cols[i].name);
//                if ((i + 1) != nc) {
//                    s.append(", ");
//                }
//            }
//            s.append(") ");
//
//            s.append("\n");//new line
//
//            s.append("SELECT ");
//            for (int i = 0; i < nc; i++) {
//                ColumnDiff diff = getColumnDiffForName(tableDiff, cols[i].name);
//                if (diff == null) {
//                    if (i != 0) {
//                        s.append("       ");
//                    }
//                    s.append(cols[i].name);
//                } else {
//                    if (diff.isMissingCol()) {
//                        if (diff.getOurCol().nulls) {
//                            if (i != 0) {
//                                s.append("       ");
//                            }
//                            s.append("CAST(NULL");
//                            s.append(" AS ");
//                            appendColumnType(cols[i], s);
//                            s.append(")");
//
//                        } else {
//                            if (i != 0) {
//                                s.append("       ");
//                            }
//                            s.append(getDefaultForType(diff.getOurCol()));
//                        }
//
//                    } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && diff.isNullDiff()) {
//                        if (cols[i].nulls) {
//                            if (i != 0) {
//                                s.append("       ");
//                            }
//                            s.append("CAST(");
//                            s.append(cols[i].name);
//                            s.append(" AS ");
//                            appendColumnType(cols[i], s);
//                            s.append(")");
//                        } else {
//                            if (i != 0) {
//                                s.append("       ");
//                            }
//                            s.append("CASE ");
//                            s.append("\n");//new line
//                            s.append("            WHEN ");
//                            s.append(cols[i].name);
//                            s.append(" IS NOT NULL THEN CAST(");
//                            s.append(cols[i].name);
//                            s.append(" AS ");
//                            appendColumnType(cols[i], s);
//                            s.append(")");
//                            s.append("\n");//new line
//                            s.append("            ELSE CAST(");
//                            s.append(getDefaultForType(diff.getOurCol()));
//                            s.append(" AS ");
//                            appendColumnType(cols[i], s);
//                            s.append(")");
//                            s.append("\n");//new line
//                            s.append("       END CASE");
//                        }
//
//                    } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && !diff.isNullDiff()) {
//                        if (i != 0) {
//                            s.append("       ");
//                        }
//                        s.append("CAST(");
//                        s.append(cols[i].name);
//                        s.append(" AS ");
//                        appendColumnType(cols[i], s);
//                        s.append(")");
//                    } else if (diff.isNullDiff()) {
//                        if (cols[i].nulls) {
//                            if (i != 0) {
//                                s.append("       ");
//                            }
//                            s.append(cols[i].name);
//                        } else {
//                            if (i != 0) {
//                                s.append("       ");
//                            }
//                            s.append("CASE ");
//                            s.append("\n");//new line
//                            s.append("            WHEN ");
//                            s.append(cols[i].name);
//                            s.append(" IS NOT NULL THEN ");
//                            s.append(cols[i].name);
//                            s.append("\n");//new line
//                            s.append("            ELSE ");
//                            s.append(getDefaultForType(diff.getOurCol()));
//                            s.append("\n");//new line
//                            s.append("       END CASE");
//                        }
//                    }
//                }
//
//
//                if ((i + 1) != nc) {
//                    s.append(", ");
//                    s.append("\n");//new line
//                }
//            }
//            s.append("\n");//new line
//            s.append("  FROM ");
//            s.append(ourTable.name);
//            s.append(getRunCommand());
//
//
//            s.append("DROP TABLE ");
//            s.append(ourTable.name);
//            s.append(getRunCommand());
//
//            s.append("RENAME TABLE ");
//            s.append(tempTableName);
//            s.append(" TO ");
//            s.append(ourTable.name);
//            s.append(getRunCommand());
//
//            out.println(s.toString());
//
//
//        }

}
