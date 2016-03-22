
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
import com.versant.core.jdbc.sql.conv.CharacterStreamConverter;
import com.versant.core.jdbc.sql.conv.DateTimestampConverter;
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
 * Support for DB2.
 */
public class DB2SqlDriver extends SqlDriver {

    private CharacterStreamConverter.Factory characterStreamConverterFactory
            = new CharacterStreamConverter.Factory();
    private boolean isAS400;

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "db2";
    }
    
    public boolean isPutOrderColsInSelect() {
        return true;
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
            case Types.BIGINT:
                return new JdbcTypeMapping("BIGINT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("TIMESTAMP",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("CLOB",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        characterStreamConverterFactory);
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("BLOB",
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

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping) ans.get(Date.class)).setConverterFactory(dtcf);

        return ans;
    }

    /**
     * Create a default name generator instance for JdbcStore's using this
     * driver.
     */
    public JdbcNameGenerator createJdbcNameGenerator() {
        DefaultJdbcNameGenerator n = createDefaultJdbcNameGenerator();
        n.setMaxColumnNameLength(30);
        n.setMaxTableNameLength(128);
        n.setMaxConstraintNameLength(18);
        n.setMaxIndexNameLength(18);
        return n;
    }

    /**
     * Should PreparedStatement batching be used for this database and
     * JDBC driver?
     */
    public boolean isPreparedStatementPoolingOK() {
        return false;
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

    public void customizeForDriver(Driver jdbcDriver) {
        String n = jdbcDriver.getClass().getName();
        if (n.indexOf("as400") >= 0) {
            isAS400 = true;
        }
    }

    /**
     * Can batching be used if the statement contains a column with the
     * given JDBC type?
     */
    public boolean isBatchingSupportedForJdbcType(int jdbcType) {
        switch (jdbcType) {
            case Types.CLOB:
            case Types.LONGVARCHAR:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return false;
        }
        return true;
    }

    /**
     * Does the JDBC driver support scrollable result sets?
     */
    public boolean isScrollableResultSetSupported() {
        return true;
    }

    /**
     * Is it ok to convert simple 'exists (select ...)' clauses under an
     * 'or' into outer joins?
     */
    public boolean isOptimizeExistsUnderOrToOuterJoin() {
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
     * May the ON clauses for joins in a subquery reference columns from the
     * enclosing query? DB2 does not allow this.
     */
    public boolean isSubQueryJoinMayUseOuterQueryCols() {
        return false;
    }

    /**
     * Does this database support comments embedded in SQL?
     */
    public boolean isCommentSupported() {
        return false;
    }

    /**
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        stat.execute("DROP TABLE " + table);
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
        s.append(" FOREIGN KEY ");
        s.append(c.name);
        s.append('(');
        appendColumnNameList(c.srcCols, s);
        s.append(") REFERENCES ");
        s.append(c.dest.name);
        s.append('(');
        appendColumnNameList(c.dest.pk, s);
        s.append(')');
    }

    /**
     * Generate a 'create table' statement for t.
     */
    public void generateCreateTable(JdbcTable t, Statement stat, PrintWriter out, boolean comments)
            throws SQLException {
        CharBuf s = new CharBuf();
        s.append("CREATE TABLE ");
        s.append(t.name);
        s.append(" (");
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            appendCreateColumn(t, cols[i], s, comments);
            s.append(' ');
        }
        appendPrimaryKeyConstraint(t, s);
        s.append(")");
        String sql = s.toString();
        if (out != null) print(out, sql);
        if (stat != null) stat.execute(sql);
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
     *
     * @param exp   This is the expression that joins the tables
     * @param outer If true then this is an outer join
     */
    public void appendSqlFromJoin(JdbcTable table, String alias, SqlExp exp,
                                  boolean outer, CharBuf s) {
        if (outer)
            s.append(" LEFT JOIN ");
        else
            s.append(" JOIN ");
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
        return "SELECT * FROM SYSIBM.SYSDUMMY1";
    }

    /**
     * Gets the current user's schema
     */
    protected String getSchema(Connection con) {
        String schema = null;
        String sql = "SELECT CURRENT SCHEMA  FROM SYSIBM.SYSDUMMY1";
        try {
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                schema = rs.getString(1);
            }
            try {
                statement.close();
            } catch (SQLException e) {
            }
        } catch (SQLException sqle) {
            //hide
        }
        return schema;
    }

    /**
     * Get whatever needs to be appended to a SELECT statement to lock the
     * rows if this makes sense for the database. This must have a leading
     * space if not empty.
     */
    public char[] getSelectForUpdate() {
        return null;
    }

    private String[] typesNames = new String[]{
        "BIGINT", "LONG VARCHAR FOR BIT DATA", "VARCHAR() FOR BIT DATA", "CHAR() FOR BIT DATA", "ROWID",
        "LONG VARCHAR", "CHAR", "CHARACTER", "NUMERIC", "DECIMAL", "INTEGER", "SMALLINT", "FLOAT", "REAL", "DOUBLE",
        "VARG","VARCHAR", "DATE", "TIME", "TIMESTAMP","TIMESTMP", "BLOB", "CLOB", "DBCLOB"
    };

    private int[] typesValues = new int[]{
        -5, -4, -3, -2, -2,
        -1, 1, 1, 2, 3, 4, 5, 6, 7, 8,
        12, 12, 91, 92, 93, 93, 2004, 2005, 2005
    };

    private int getJdbcType(String type) {
        for (int i = 0; i < typesNames.length; i++) {
            if (typesNames[i].equals(type)) {
                return typesValues[i];
            }
        }
        return Types.OTHER;
    }

    /**
     * Get the JdbcTables from the database for the given database con.
     *
     * @param con
     * @return HashMap of tablename.toLowerCase() as key and JdbcTable as value
     * @throws SQLException on DB errors
     */
    public HashMap getDBSchema(Connection con, ControlParams params) throws SQLException {
        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables


        HashMap synonymMap = new HashMap();
        String schemaName = getSchema(con);

        try {
            String synonymSql = null;
            String tmpSynonymDB2 =
                    "SELECT BASE_NAME, " +
                    "       NAME " +
                    "  FROM SYSIBM.SYSTABLES " +
                    " WHERE TYPE = 'A'";
            String tmpSynonymAS400 =
                    "SELECT BASE_TABLE_NAME," +
                    "       TABLE_NAME " +
                    "  FROM SYSTABLES " +
                    " WHERE TABLE_TYPE = 'A'" +
                    (schemaName == null ? "": "   AND TABLE_SCHEMA = '"+ schemaName +"'");
            if (isAS400) {
                synonymSql = tmpSynonymAS400;
            } else {
                synonymSql = tmpSynonymDB2;
            }

            Statement statSynonym = con.createStatement();
            ResultSet rsSynonym = statSynonym.executeQuery(synonymSql);
            while (rsSynonym.next()) {
                synonymMap.put(rsSynonym.getString(1).toLowerCase(), rsSynonym.getString(2).toLowerCase());
            }
            // clean up
            if (rsSynonym != null) {
                try {
                    rsSynonym.close();
                } catch (SQLException e) {
                }
            }
            if (statSynonym != null) {
                try {
                    statSynonym.close();
                } catch (SQLException e) {
                }
            }
        } catch (SQLException e) {
            //hide it all, we do not want throw exeptions if
        }

        // now we do columns
        String tableName = null;

        String columnSql = null;
        String tmpColumnDB2 =
                " SELECT TABNAME, COLNAME, TYPENAME, LENGTH,  SCALE,  COLNO, 'Y' " +
                "   FROM SYSCAT.COLUMNS  " +
                "  WHERE NULLS LIKE '%Y%'  " +
                "    AND NOT TABSCHEMA IN ('SYSIBM','SYSCAT','SYSSTAT')  " +
                "UNION ALL  " +
                " SELECT TABNAME, COLNAME, TYPENAME, LENGTH,  SCALE,  COLNO, 'N' " +
                "   FROM SYSCAT.COLUMNS  " +
                "  WHERE NULLS LIKE '%N%'  " +
                "    AND NOT TABSCHEMA IN ('SYSIBM','SYSCAT','SYSSTAT')  " +
                "UNION ALL  " +
                " SELECT B.NAME, A.COLNAME, A.TYPENAME, A.LENGTH,  A.SCALE,  A.COLNO, 'Y' " +
                "   FROM SYSCAT.COLUMNS A,  " +
                "        SYSIBM.SYSTABLES B   " +
                "  WHERE NULLS LIKE '%Y%'  " +
                "    AND B.BASE_NAME = A.TABNAME   " +
                "    AND A.TABSCHEMA = B.CREATOR  " +
                "    AND NOT A.TABSCHEMA in ('SYSIBM','SYSCAT','SYSSTAT')  " +
                "UNION ALL  " +
                " SELECT B.NAME, A.COLNAME, A.TYPENAME, A.LENGTH,  A.SCALE, A.COLNO, 'N' " +
                "   FROM SYSCAT.COLUMNS A,  " +
                "        SYSIBM.SYSTABLES B   " +
                "  WHERE NULLS LIKE '%N%'  " +
                "    AND B.BASE_NAME = A.TABNAME  " +
                "    AND A.TABSCHEMA = B.CREATOR  " +
                "    AND NOT A.TABSCHEMA in ('SYSIBM','SYSCAT','SYSSTAT') " +
                "  ORDER BY 1, 6 FOR FETCH ONLY";
        String tmpColumnAS400 =
                "SELECT c.TABLE_NAME , " +
                "       c.COLUMN_NAME , " +
                "       c.DATA_TYPE, " +
                "       c.LENGTH , " +
                "       c.NUMERIC_SCALE, " +
                "       c.ORDINAL_POSITION, " +
                "       c.IS_NULLABLE " +
                "  FROM SYSCOLUMNS c, " +
                "       SYSTABLES t " +
                " WHERE c.TABLE_NAME = t.TABLE_NAME " +
                (schemaName == null ? "" : "  AND t.TABLE_SCHEMA = '" +
                schemaName + "' AND c.TABLE_SCHEMA = '" +
                schemaName + "'") +
                "   AND t.SYSTEM_TABLE = 'N' " +
                "   AND t.TABLE_TYPE = 'T' " +
                " ORDER BY 1,6 " +
                "   FOR FETCH ONLY ";

        if (isAS400){
            columnSql = tmpColumnAS400;
        } else {
            columnSql = tmpColumnDB2;
        }
        Statement statCol = con.createStatement();
        ResultSet rsColumn = statCol.executeQuery(columnSql);
        ArrayList columns = null;

        while (rsColumn.next()) {

            String temptableName = rsColumn.getString(1);

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

            col.name = rsColumn.getString(2).trim();
            col.sqlType = rsColumn.getString(3).trim();
            col.jdbcType = getJdbcType(col.sqlType);
            col.length = rsColumn.getInt(4);
            col.scale = rsColumn.getInt(5);
            col.nulls = ("Y".equals(rsColumn.getString(7).trim()) ? true : false);

            switch (col.jdbcType) {
                case java.sql.Types.BIT:
                case java.sql.Types.TINYINT:
                case java.sql.Types.SMALLINT:
                case java.sql.Types.INTEGER:
                case java.sql.Types.BIGINT:
                case java.sql.Types.CLOB:
                case java.sql.Types.BLOB:
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
        if (columns != null) {
            JdbcColumn[] jdbcColumns = new JdbcColumn[columns.size()];
            if (jdbcColumns != null) {
                columns.toArray(jdbcColumns);
                JdbcTable colJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                colJdbcTable.cols = jdbcColumns;
                columns.clear();
            }
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

                String pkSql = null;
                String tmpPkDB2=
                        "SELECT DISTINCT IT.TABNAME as TABLE_NAME, " +
                        "       KT.COLNAME as COLUMN_NAME, " +
                        "       KT.COLSEQ as KEY_SEQ, " +
                        "       IT.INDNAME as PK_NAME " +
                        "  FROM SYSCAT.INDEXCOLUSE KT, " +
                        "       SYSCAT.INDEXES IT " +
                        " WHERE IT.UNIQUERULE = 'P' " +
                        "   AND IT.INDSCHEMA = KT.INDSCHEMA " +
                        "   AND KT.INDNAME = IT.INDNAME " +
                        " ORDER BY TABLE_NAME,PK_NAME,KEY_SEQ FOR FETCH ONLY";
                String tmpPkAS400 =
                        " SELECT DISTINCT T1.TABLE_NAME, " +
                        "       T1.COLUMN_NAME, " +
                        "       T1.ORDINAL_POSITION as KEY_SEQ, " +
                        "       T1.CONSTRAINT_NAME as PK_NAME " +
                        "  FROM SYSKEYCST T1, " +
                        "       SYSCST T2 " +
                        " WHERE T1.CONSTRAINT_NAME  = T2.CONSTRAINT_NAME " +
                        "   AND T1.CONSTRAINT_SCHEMA  = T2.CONSTRAINT_SCHEMA " +
                        "   AND T2.CONSTRAINT_TYPE = 'PRIMARY KEY' " +
                        (schemaName == null ? "" : "AND T1.TABLE_SCHEMA = '" + schemaName + "'") +
                        " ORDER BY 1,4,3 " +
                        "   FOR FETCH ONLY ";
                if (isAS400) {
                    pkSql = tmpPkAS400;
                } else {
                    pkSql = tmpPkDB2;
                }

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
                String indexSql = null;
                String tmpIndexDB2 =
                        "SELECT IT.TABNAME as TABLE_NAME, " +
                        "       IU.COLNAME as COLUMN_NAME, " +
                        "       IT.INDNAME as INDEX_NAME, " +
                        "       1 as TYPE, " +
                        "       IU.COLSEQ as ORDINAL_POSITION," +
                        "       0 as UNIQUE " +
                        "  FROM SYSCAT.INDEXCOLUSE IU, " +
                        "       SYSCAT.INDEXES IT " +
                        " WHERE IU.INDNAME = IT.INDNAME " +
                        "   AND IU.INDSCHEMA = IT.INDSCHEMA  " +
                        "   AND IT.INDEXTYPE = 'CLUS' " +
                        "   AND IT.TABSCHEMA <> 'SYSIBM' " +
                        "   AND IT.UNIQUE_COLCOUNT = -1 " +
                        "UNION ALL " +
                        "SELECT IT.TABNAME as TABLE_NAME, " +
                        "       IU.COLNAME as COLUMN_NAME, " +
                        "       IT.INDNAME as INDEX_NAME, " +
                        "       3 as TYPE, " +
                        "       IU.COLSEQ as ORDINAL_POSITION," +
                        "       1 as UNIQUE" +
                        "  FROM SYSCAT.INDEXCOLUSE IU, " +
                        "       SYSCAT.INDEXES IT " +
                        " WHERE IU.INDNAME = IT.INDNAME " +
                        "   AND IU.INDSCHEMA = IT.INDSCHEMA " +
                        "   AND IT.INDEXTYPE = 'CLUS' " +
                        "   AND IT.TABSCHEMA <> 'SYSIBM' " +
                        "   AND IT.UNIQUE_COLCOUNT <> -1 " +
                        "   AND IT.UNIQUERULE = 'U'" +
                        "UNION ALL " +
                        "SELECT IT.TABNAME as TABLE_NAME, " +
                        "       IU.COLNAME as COLUMN_NAME, " +
                        "       IT.INDNAME as INDEX_NAME, " +
                        "       3 as TYPE, " +
                        "       IU.COLSEQ as ORDINAL_POSITION," +
                        "       0 as UNIQUE" +
                        "  FROM SYSCAT.INDEXCOLUSE IU, " +
                        "       SYSCAT.INDEXES IT " +
                        " WHERE IU.INDNAME = IT.INDNAME " +
                        "   AND IU.INDSCHEMA = IT.INDSCHEMA " +
                        "   AND IT.INDEXTYPE = 'REG' " +
                        "   AND IT.TABSCHEMA <> 'SYSIBM' " +
                        "   AND IT.UNIQUE_COLCOUNT = -1 " +
                        "UNION ALL    " +
                        "SELECT IT.TABNAME as TABLE_NAME, " +
                        "       IU.COLNAME as COLUMN_NAME, " +
                        "       IT.INDNAME as INDEX_NAME, " +
                        "       3 as TYPE, " +
                        "       IU.COLSEQ as ORDINAL_POSITION," +
                        "       1 as UNIQUE" +
                        "  FROM SYSCAT.INDEXCOLUSE IU, " +
                        "       SYSCAT.INDEXES IT " +
                        " WHERE IU.INDNAME = IT.INDNAME " +
                        "   AND IU.INDSCHEMA = IT.INDSCHEMA " +
                        "   AND IT.INDEXTYPE = 'REG' " +
                        "   AND IT.TABSCHEMA <> 'SYSIBM' " +
                        "   AND IT.UNIQUE_COLCOUNT <> -1 " +
                        "   AND IT.UNIQUERULE = 'U'" +
                        " ORDER BY TABLE_NAME, INDEX_NAME FOR FETCH ONLY";
                String tmpIndexAS400 =
                        "SELECT i.TBNAME AS TABLE_NAME , " +
                        "       k.COLNAME AS COLUMN_NAME , " +
                        "       i.NAME AS INDEX_NAME, " +
                        "       3 AS TYPE , " +
                        "       k.COLSEQ AS ORDINAL_POSITION, " +
                        "       CASE UNIQUERULE " +
                        "          WHEN 'D' THEN 0 else 1 " +
                        "          END AS UNIQUE " +
                        "  FROM SYSINDEXES i, " +
                        "       SYSKEYS k " +
                        " WHERE CREATOR = IXCREATOR " +
                        "   AND NAME  = IXNAME " +
                        (schemaName == null ? "" : "   AND TABLE_SCHEMA = '" + schemaName + "'") +
                        " ORDER BY 1, 3, 5  FOR FETCH ONLY";

                if (isAS400) {
                    indexSql = tmpIndexAS400;
                } else {
                    indexSql = tmpIndexDB2;
                }

                Statement statIndex = con.createStatement();
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
                            index.unique = rsIndex.getBoolean(6);
                            short indexType = rsIndex.getShort(4);
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
                if (indexJdbcTable != null && indexes != null) {
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
                if (isAS400){
                    String fkSql =
                            "SELECT DISTINCT PK.TABLE_NAME as PKTABLE_NAM, " + //1
                            "       PK.COLUMN_NAME as PKCOLUMN_NAME, " +       //2
                            "       FK.TABLE_NAME as FKTABLE_NAME, " +         //3
                            "       FK.COLUMN_NAME as FKCOLUMN_NAME, " +       //4
                            "       FK.ORDINAL_POSITION as COL_NUM, " +        //5
                            "       FK.CONSTRAINT_NAME as FK_NAME , " +        //6
                            "       PK.CONSTRAINT_NAME as PK_NAME  " +         //7
                            "  FROM SYSCST C, " +
                            "       SYSKEYCST PK, " +
                            "       SYSREFCST R, " +
                            "       SYSKEYCST FK " +
                            " WHERE C.CONSTRAINT_NAME  = PK.CONSTRAINT_NAME  " +
                            "   AND C.CONSTRAINT_SCHEMA  = PK.CONSTRAINT_SCHEMA  " +
                            "   AND C.CONSTRAINT_NAME  = R.UNIQUE_CONSTRAINT_NAME " +
                            "   AND C.CONSTRAINT_SCHEMA  = R.UNIQUE_CONSTRAINT_SCHEMA " +
                            "   AND R.CONSTRAINT_NAME  = FK.CONSTRAINT_NAME  " +
                            "   AND R.CONSTRAINT_SCHEMA  = FK.CONSTRAINT_SCHEMA  " +
                            "   AND PK.ORDINAL_POSITION = FK.ORDINAL_POSITION " +
                            (schemaName == null ?
                                "" :
                                "   AND FK.TABLE_SCHEMA = '" + schemaName + "'") +
                            " ORDER BY 3,6,5 " +
                            "   FOR FETCH ONLY ";
                    Statement statFK = con.createStatement();
                    ResultSet rsFKs = statFK.executeQuery(fkSql);

                    HashMap constraintNameMap = null;
                    ArrayList constraints = null;
                    while (rsFKs.next()) {
                        String temptableName = rsFKs.getString(3);
                        if (tableName == null) { // this is the first one
                            tableName = temptableName;
                            constraintNameMap = new HashMap();
                            constraints = new ArrayList();
                        }


                        if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                            JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                            JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                            constraints.toArray(jdbcConstraints);
                            jdbcTable.constraints = jdbcConstraints;


                            tableName = temptableName;
                            constraintNameMap.clear();
                            constraints.clear();
                        }

                        String fkName = rsFKs.getString(6);
                        JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);

                        if (jdbcTable == null) continue;

                        if (constraintNameMap.containsKey(fkName)) {
                            JdbcConstraint constraint = null;
                            for (Iterator iter = constraints.iterator(); iter.hasNext();) {
                                JdbcConstraint jdbcConstraint = (JdbcConstraint) iter.next();
                                if (jdbcConstraint.name.equals(fkName)) {
                                    constraint = jdbcConstraint;
                                }
                            }

                            JdbcColumn[] tempConstraintColumns = constraint.srcCols;
                            JdbcColumn[] constraintColumns = new JdbcColumn[tempConstraintColumns.length + 1];
                            System.arraycopy(tempConstraintColumns, 0, constraintColumns, 0, tempConstraintColumns.length);
                            String colName = rsFKs.getString(4);
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equals(jdbcColumn.name)) {
                                    constraintColumns[tempConstraintColumns.length] = jdbcColumn;
                                    jdbcColumn.foreignKey = true;
                                }
                            }
                            constraint.srcCols = constraintColumns;
                        } else {
                            constraintNameMap.put(fkName, null);
                            JdbcConstraint constraint = new JdbcConstraint();
                            constraint.name = fkName;
                            constraint.src = jdbcTable;
                            String colName = rsFKs.getString(4);
                            JdbcColumn[] constraintColumns = new JdbcColumn[1];
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equals(jdbcColumn.name)) {
                                    constraintColumns[0] = jdbcColumn;
                                    jdbcColumn.foreignKey = true;
                                }
                            }
                            constraint.srcCols = constraintColumns;
                            constraint.dest = (JdbcTable) jdbcTableMap.get(rsFKs.getString(1));
                            constraints.add(constraint);
                        }
                    }

                    JdbcTable constraintsjdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                    if (constraintsjdbcTable != null) {
                        JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                        constraints.toArray(jdbcConstraints);
                        constraintsjdbcTable.constraints = jdbcConstraints;
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
                } else {         //jenifer 8514360 vinny
                    String fkSql =
                            "SELECT RT.REFTABNAME as PKTABLE_NAME, " +  //1
                            "       RT.PK_COLNAMES as PKCOLUMN_NAME, " +//2
                            "       RT.TABNAME as FKTABLE_NAME, " +     //3
                            "       RT.FK_COLNAMES as FKCOLUMN_NAME, " +//4
                            "       RT.COLCOUNT as COLCOUNT, " +        //5
                            "       RT.CONSTNAME as FK_NAME, " +        //6
                            "       RT.REFKEYNAME as PK_NAME " +        //7
                            "  FROM SYSCAT.REFERENCES RT " +
                            " ORDER BY FKTABLE_NAME, FK_NAME";

                    Statement statFK = con.createStatement();
                    ResultSet rsFKs = statFK.executeQuery(fkSql);

                    HashMap constraintNameMap = new HashMap();


                    while (rsFKs.next()) {
                        String constName = rsFKs.getString("FK_NAME");
                        String srcTableName = rsFKs.getString("FKTABLE_NAME");
                        String destTableName = rsFKs.getString("PKTABLE_NAME");
                        ArrayList srcColNames = new ArrayList();

                        if (rsFKs.getInt("COLCOUNT") == 1) {
                            srcColNames.add(rsFKs.getString("FKCOLUMN_NAME").trim());
                        } else {
                            StringTokenizer st = new StringTokenizer(rsFKs.getString("FKCOLUMN_NAME").trim(), " ");
                            while (st.hasMoreTokens()) {
                                srcColNames.add(st.nextToken().trim());
                            }
                        }
                        JdbcTable srcJdbcTable = (JdbcTable) jdbcTableMap.get(srcTableName);
                        if (srcJdbcTable == null) {
                            continue;
                        }
                        JdbcTable destJdbcTable = (JdbcTable) jdbcTableMap.get(destTableName);
                        if (destJdbcTable == null) {
                            continue;
                        }

                        JdbcConstraint jdbcConstraint = new JdbcConstraint();
                        jdbcConstraint.name = constName;
                        jdbcConstraint.src = srcJdbcTable;
                        jdbcConstraint.dest = destJdbcTable;
                        JdbcColumn[] constraintColumns = new JdbcColumn[srcColNames.size()];
                        int j = 0;
                        for (Iterator iter = srcColNames.iterator(); iter.hasNext(); j++) {
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
                        if (constraintNameMap.containsKey(srcJdbcTable)) {
                            ArrayList list = (ArrayList) constraintNameMap.get(srcJdbcTable);
                            list.add(jdbcConstraint);
                        } else {
                            ArrayList list = new ArrayList();
                            list.add(jdbcConstraint);
                            constraintNameMap.put(srcJdbcTable, list);
                        }
                    }
                    for (Iterator iter = constraintNameMap.keySet().iterator(); iter.hasNext();) {
                        JdbcTable jdbcTable = (JdbcTable) iter.next();
                        ArrayList list = (ArrayList) constraintNameMap.get(jdbcTable);
                        if (list != null) {
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
        }

        HashMap returnMap = new HashMap();
        Collection col = jdbcTableMap.values();
        String name = null;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            JdbcTable table = (JdbcTable) iterator.next();
            name = table.name.toLowerCase();
            returnMap.put(name, table);
            if (synonymMap.containsKey(name)) {
                returnMap.put(synonymMap.get(name), table);
            }
        }
        fixAllNames(returnMap);
        return returnMap;
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
            if (c.nulls) {
                s.append(getRunCommand());
            } else {
                appendCreateColumnNulls(t, c, s);
                s.append(" DEFAULT");
                s.append(getRunCommand());


                s.append("UPDATE ");
                s.append(t.name);
                s.append(" SET ");
                s.append(c.name);
                s.append(" = ");
                s.append(getDefaultForType(c));
                s.append(getRunCommand());
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
        s.append(" ALTER ");
        s.append(c.name);
        s.append(" SET DATA TYPE ");
        appendColumnType(c, s);


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
        s.append(" DROP FOREIGN KEY ");
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
        s.append("DROP INDEX ");
        s.append(idx.name);
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
        s.append(" DROP PRIMARY KEY");
    }

    boolean isDirectDropColumnSupported() {
        return false;
    }

    boolean isDirectTypeColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        return false;
    }

    boolean isDirectNullColumnChangesSupported() {
        return false;
    }

    boolean isDirectScaleColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        return false;
    }

    boolean isDirectLenghtColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        if (ourCol.jdbcType == java.sql.Types.VARCHAR &&
                dbCol.jdbcType == java.sql.Types.VARCHAR) {
            if (dbCol.length < ourCol.length) {
                return true;
            }
        }
        return false;
    }

    protected void fixColumnsNonDirect(TableDiff tableDiff, PrintWriter out) {

        JdbcTable ourTable = tableDiff.getOurTable();
        String tempTableName = getTempTableName(ourTable, 128);


        CharBuf s = new CharBuf();
        s.append("CREATE TABLE ");
        s.append(tempTableName);  //ourTable.name
        s.append(" (");
        JdbcColumn[] cols = ourTable.getColsForCreateTable();
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            appendCreateColumn(ourTable, cols[i], s, false);
            s.append(' ');
        }
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
                        s.append("CAST(NULL");
                        s.append(" AS ");
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
                        s.append("CASE ");
                        s.append("\n");//new line
                        s.append("            WHEN ");
                        s.append(cols[i].name);
                        s.append(" IS NOT NULL THEN CAST(");
                        s.append(cols[i].name);
                        s.append(" AS ");
                        appendColumnType(cols[i], s);
                        s.append(")");
                        s.append("\n");//new line
                        s.append("            ELSE CAST(");
                        s.append(getDefaultForType(diff.getOurCol()));
                        s.append(" AS ");
                        appendColumnType(cols[i], s);
                        s.append(")");
                        s.append("\n");//new line
                        s.append("       END CASE");
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
                        s.append("CASE ");
                        s.append("\n");//new line
                        s.append("            WHEN ");
                        s.append(cols[i].name);
                        s.append(" IS NOT NULL THEN ");
                        s.append(cols[i].name);
                        s.append("\n");//new line
                        s.append("            ELSE ");
                        s.append(getDefaultForType(diff.getOurCol()));
                        s.append("\n");//new line
                        s.append("       END CASE");
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

        s.append("RENAME TABLE ");
        s.append(tempTableName);
        s.append(" TO ");
        s.append(ourTable.name);
        s.append(getRunCommand());

        out.println(s.toString());


    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {
        String tempTableName = getTempTableName(t, 128);

//        s.append(comment("create a temp table to store old table values."));
//        s.append("\n");
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


//        s.append(comment("insert a distinct list into the temp table."));
//        s.append("\n");
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


//        s.append(comment("drop main table."));
//        s.append("\n");
        s.append("DROP TABLE ");
        s.append(t.name);
        s.append(getRunCommand());

//        s.append(comment("rename temp table to main table."));
//        s.append("\n");
        s.append("RENAME TABLE ");
        s.append(tempTableName);
        s.append(" TO ");
        s.append(t.name);

    }

    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {

        String mainTempTableName = getTempTableName(t, 128);
        String minTempTableName = getTempTableName(t, 128);
        String identityColumnName = getTempColumnName(t);


        JdbcColumn indexColumn = null;
        JdbcColumn sequenceColumn = null;
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            if (isAddSequenceColumn(cols[i])) {
            } else if (t.isInPrimaryKey(cols[i].name)) {
                indexColumn = cols[i];
            }
        }


//        s.append(comment("Generate a sequence number so that we can implement a List."));
//        s.append("\n");
//        s.append(comment("create a temp table with a extra identity column."));
//        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(mainTempTableName);
        s.append(" (\n    ");
        // create identity column
        s.append(identityColumnName);
        s.append(" INTEGER GENERATED ALWAYS AS IDENTITY,");
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        int lastIndex = s.toString().lastIndexOf(',');
        s.replace(lastIndex, lastIndex + 1, ' ');// we take the last ',' out.
        s.append("\n)");


        s.append(getRunCommand());


//        s.append(comment("insert a '0' in the sequence column and copy the rest of the old table into the temp table."));
//        s.append("\n");
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


//        s.append(comment("create a temp table to store the minimum id."));
//        s.append("\n");
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


//        s.append(comment("store the minimum id."));
//        s.append("\n");
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


//        s.append(comment("drop main table " + t.name + "."));
//        s.append("\n");
        s.append("DROP TABLE ");
        s.append(t.name);

        s.append(getRunCommand());


//        s.append(comment("recreate table " + t.name + "."));
//        s.append("\n");
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

//        s.append(comment("populate table " + t.name + " with the new sequence column."));
//        s.append("\n");
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


//        s.append(comment("drop temp tables."));
//        s.append("\n");
        s.append("DROP TABLE ");
        s.append(mainTempTableName);
        s.append(getRunCommand());


        s.append("DROP TABLE ");
        s.append(minTempTableName);
        s.append(getRunCommand());
    }
}
