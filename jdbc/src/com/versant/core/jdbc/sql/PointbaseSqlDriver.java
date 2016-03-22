
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

import com.versant.core.common.BindingSupportImpl;

/**
 * A driver for Pointbase.
 */
public class PointbaseSqlDriver extends SqlDriver {

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "pointbase";
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
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONG VARCHAR",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.CLOB:
                return new JdbcTypeMapping("CLOB",
                        1024, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
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

    public boolean isClearBatchRequired() {
        return true;
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

    public boolean isPreparedStatementPoolingOK() {
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

    /**
     * Should indexes be used for columns in the order by list that are
     * also in the select list? This is used for databases that will not
     * order by a column that is duplicated in the select list (e.g. Oracle).
     */
    public boolean isUseIndexesForOrderCols() {
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
        s.append(") MATCH FULL");
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
        if (outer) {
            s.append(" LEFT OUTER JOIN ");
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
     * Append the column type part of a create table statement for a column.
     */
    protected void appendColumnType(JdbcColumn c, CharBuf s,
            boolean useZeroScale) {
        if (c.sqlType == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "sqlType is null: " + c);
        }
        s.append(c.sqlType);
        if (c.sqlType.equals("BLOB") || c.sqlType.equals("CLOB")) {
            if (c.length == 0) {
                s.append('(');
                s.append(1024);
                s.append(" K)");
            } else {
                s.append('(');
                s.append(c.length);
                s.append(" K)");
            }
        } else if (c.length != 0 || c.scale != 0) {
            s.append('(');
            s.append(c.length);
            if (c.scale != 0) {
                s.append(',');
                s.append(c.scale);
            }
            s.append(')');
        }
    }

    public String getConnectionValidateSQL() {
        return "SELECT databasename FROM sysdatabases";
    }

    /**
     * Get con ready for a getQueryPlan call. Example: On Sybase this will
     * do a 'set showplan 1' and 'set noexec 1'. Also make whatever changes
     * are necessary to sql to prepare it for a getQueryPlan call. Example:
     * On Oracle this will prepend 'explain '. The cleanupForGetQueryPlan
     * method must be called in a finally block if this method is called.
     *
     * @see #cleanupForGetQueryPlan
     * @see #getQueryPlan
     */
    public String prepareForGetQueryPlan(Connection con, String sql) {
        try {
            Statement statement = con.createStatement();
            statement.execute("SET PLANONLY ON");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return sql;
    }

    /**
     * Get the query plan for ps and cleanup anything done in
     * prepareForGetQueryPlan. Return null if this is not supported.
     *
     * @see #prepareForGetQueryPlan
     * @see #cleanupForGetQueryPlan
     */
    public String getQueryPlan(Connection con, PreparedStatement ps) {
        StringBuffer buff = new StringBuffer();
        Statement stat = null;
        ResultSet rs = null;
        try {

            ps.execute();
            stat = con.createStatement();
            stat.execute("SET PLANONLY OFF");
            rs = stat.executeQuery("select * from PLAN_TABLE");

            double totalCost = 0;
            int block = -1;
            while (rs != null && rs.next()) {
                int blockNum = rs.getInt("BLOCK");// BLOCK
                int stepNum = rs.getInt("STEP");// STEP
                String operation = rs.getString("OPERATION");//OPERATION
                String accessMethod = rs.getString("ACCESS_METHOD");//ACCESS_METHOD
                String tablename = rs.getString("TABLENAME");//TABLENAME
                String indexname = rs.getString("INDEXNAME");//INDEXNAME
                double cost = rs.getDouble("COST");// COST
                double outputRows = rs.getDouble("OUTPUTROWS");// OUTPUTROWS
                String expression = rs.getString("EXPRESSIONS");//EXPRESSIONS

                if (block != blockNum) {
                    if (block != -1) {
                        buff.append("\n");
                    }
                    buff.append("Block " + blockNum);
                    block = blockNum;
                }
                buff.append("\n      Step " + stepNum);

                buff.append("\n            " + operation);
                if (accessMethod != null) {
                    buff.append(" using " + accessMethod);
                }
                if (tablename != null) {
                    buff.append("\n            on table " + tablename);
                    if (indexname != null) {
                        buff.append(" using index " + indexname);
                    }
                }
                buff.append("\n            output rows : " + outputRows);
                if (expression != null) {
                    buff.append("\n            expression  : " + expression);
                }
                buff.append("\n            cost = " + cost);
                totalCost += cost;
            }

            buff.append("\n\nTOTAL COST = " + totalCost);
        } catch (Exception sqle) {
//            sqle.printStackTrace();
        } finally {
            try {
                rs.close();
                stat.close();
            } catch (Exception e) {}
        }
        return buff.toString();
    }

    /**
     * Cleanup anything done in prepareForGetQueryPlan. Example: On Sybase this
     * will do a 'set showplan 0' and 'set noexec 0'.
     *
     * @see #prepareForGetQueryPlan
     * @see #getQueryPlan
     */
    public void cleanupForGetQueryPlan(Connection con) {
        try {
            Statement statement = con.createStatement();
            statement.execute("SET PLANONLY OFF");
            statement.execute("DELETE FROM PLAN_TABLE");
            statement.execute("DELETE FROM PLAN_QUERIES");

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * Retrieve the value of the autoinc or serial column for a row just
     * inserted using stat on con.
     */
    public Object getAutoIncColumnValue(JdbcTable classTable,
            Connection con, Statement stat) throws SQLException {
        ResultSet rs = null;
        try {
            // must do the cast so that this works on JDK 1.3
            if (stat instanceof com.pointbase.net.netJDBCStatement) {
                // client server version
                rs = ((com.pointbase.net.netJDBCStatement)stat).getGeneratedKeys();
            } else {
                // embedded version
                rs = ((com.pointbase.jdbc.jdbcStatement)stat).getGeneratedKeys();
            }
            if (!rs.next()) {
                throw BindingSupportImpl.getInstance().datastore("No row returned for " +
                        "stat.getGeneratedKeys() after insert for identity column: " +
                        classTable.name + "." + classTable.pk[0].name);
            }
            return classTable.pk[0].get(rs, 1);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Format a comment.
     */
    public String comment(String msg) {
        return "/* " + msg + " */";
    }

    /**
     * Get whatever needs to be appended to a SELECT statement to lock the
     * rows if this makes sense for the database. This must have a leading
     * space if not empty.
     */
    public char[] getSelectForUpdate() {
        return null;
    }

    public boolean checkDDL(ArrayList tables, Connection con,
            PrintWriter errors, PrintWriter fix, ControlParams params)
            throws SQLException {
        params.setCheckIndex(false);
        return super.checkDDL(tables, con, errors, fix, params);
    }

    /**
     * Get the JdbcTable from the database for the given database connection and table name.
     */
    public HashMap getDBSchema(Connection con, ControlParams params)
            throws SQLException {

        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables

        // now we do columns
        String tableName = null;
        String colSql =
                " SELECT  TABLENAME AS TABLE_NAME,\n" +
                "         COLUMNNAME AS COLUMN_NAME,\n" +
                "         CAST(COLUMNTYPE AS SMALLINT) AS DATA_TYPE,\n" +
                "         SYSSQLDATATYPES.NAME AS TYPE_NAME,\n" +
                "         COLUMNLENGTH AS COLUMN_SIZE,\n" +
                "         COLUMNSCALE AS DECIMAL_DIGITS,\n" +
                "         ISNULLABLE AS NULLABLE,\n" +
                "         ORDINALPOSITION + 1 AS ORDINAL_POSITION\n" +
                "    FROM POINTBASE.SYSTABLES, \n" +
                "         POINTBASE.SYSCOLUMNS, \n" +
                "         POINTBASE.SYSSCHEMATA,\n" +
                "         POINTBASE.SYSSQLDATATYPES\n" +
                "   WHERE SYSTABLES.TABLEID = SYSCOLUMNS.TABLEID\n" +
                "     AND SYSTABLES.SCHEMAID = SYSSCHEMATA.SCHEMAID\n" +
                "     AND SYSSQLDATATYPES.SQLTYPE = COLUMNTYPE\n" +
                "     AND SYSSCHEMATA.SCHEMAID <> 4\n" +
                "   ORDER BY TABLE_NAME, ORDINAL_POSITION";
        Statement statCol = con.createStatement();
        ResultSet rsColumn = statCol.executeQuery(colSql);
        ArrayList currentColumns = null;

        while (rsColumn.next()) {

            String temptableName = rsColumn.getString("TABLE_NAME");

            if (!isValidSchemaTable(temptableName)) {
                continue;
            }

            if (tableName == null) { // this is the first one
                tableName = temptableName;
                currentColumns = new ArrayList();
                JdbcTable jdbcTable = new JdbcTable();
                jdbcTable.name = tableName;
                jdbcTableMap.put(tableName, jdbcTable);
            }

            if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                JdbcColumn[] jdbcColumns = new JdbcColumn[currentColumns.size()];
                currentColumns.toArray(jdbcColumns);
                JdbcTable jdbcTable0 = (JdbcTable)jdbcTableMap.get(tableName);
                jdbcTable0.cols = jdbcColumns;

                tableName = temptableName;
                currentColumns.clear();
                JdbcTable jdbcTable1 = new JdbcTable();
                jdbcTable1.name = tableName;
                jdbcTableMap.put(tableName, jdbcTable1);
            }

            JdbcColumn col = new JdbcColumn();

            col.name = rsColumn.getString("COLUMN_NAME");
            col.sqlType = rsColumn.getString("TYPE_NAME");
            col.jdbcType = rsColumn.getInt("DATA_TYPE");
            col.length = rsColumn.getInt("COLUMN_SIZE");
            col.scale = rsColumn.getInt("DECIMAL_DIGITS");
            col.nulls = rsColumn.getBoolean("NULLABLE");

            if (col.jdbcType == 16) {
                col.jdbcType = java.sql.Types.BIT;
            } else if (col.jdbcType == 9) {
                col.jdbcType = java.sql.Types.BIGINT;
            } else if (col.jdbcType == 40) {
                col.jdbcType = java.sql.Types.CLOB;
            } else if (col.jdbcType == 30) {
                col.jdbcType = java.sql.Types.BLOB;
            }

            switch (col.jdbcType) {
                case java.sql.Types.BIT:
                case java.sql.Types.TINYINT:
                case java.sql.Types.SMALLINT:
                case java.sql.Types.INTEGER:
                case java.sql.Types.BIGINT:
                case java.sql.Types.DATE:
                case java.sql.Types.TIME:
                case java.sql.Types.TIMESTAMP:
                    col.length = 0;
                    col.scale = 0;
                default:
            }

            currentColumns.add(col);
        }
        // we fin last table
        if (currentColumns != null) {
            JdbcColumn[] lastJdbcColumns = new JdbcColumn[currentColumns.size()];
            currentColumns.toArray(lastJdbcColumns);
            JdbcTable colJdbcTable = (JdbcTable)jdbcTableMap.get(tableName);
            colJdbcTable.cols = lastJdbcColumns;
            currentColumns.clear();

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

                String pkSql =
                        "SELECT TABLENAME AS TABLE_NAME, \n" +
                        "       COLUMNNAME AS COLUMN_NAME,\n" +
                        "       SYSINDEXKEYS.ORDINALPOSITION+1 AS KEY_SEQ, \n" +
                        "       INDEXNAME AS PK_NAME \n" +
                        "  FROM POINTBASE.SYSTABLES, \n" +
                        "       POINTBASE.SYSINDEXES, \n" +
                        "       POINTBASE.SYSINDEXKEYS, \n" +
                        "       POINTBASE.SYSCOLUMNS, \n" +
                        "       POINTBASE.SYSSCHEMATA\n" +
                        " WHERE SYSTABLES.TABLEID = SYSINDEXES.TABLEID\n" +
                        "   AND SYSINDEXES.INDEXID = SYSINDEXKEYS.INDEXID\n" +
                        "   AND SYSCOLUMNS.COLUMNID = SYSINDEXKEYS.COLUMNID\n" +
                        "   AND SYSTABLES.TABLEID = SYSCOLUMNS.TABLEID\n" +
                        "   AND SYSTABLES.SCHEMAID = SYSSCHEMATA.SCHEMAID\n" +
                        "   AND SYSINDEXES.INDEXTYPE = 1\n" +
                        "   AND SYSSCHEMATA.SCHEMAID <> 4\n" +
                        " ORDER BY 1,4,3";

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
                        JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                                tableName);
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
                JdbcTable pkJdbcTable = (JdbcTable)jdbcTableMap.get(tableName);
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
                // now we do index  /////////////////////////////////////////////////////////////////////////
                String indexSql =

                        "SELECT  TABLENAME AS TABLE_NAME, \n" +
                        "        COLUMNNAME AS COLUMN_NAME,\n" +
                        "        INDEXNAME AS INDEX_NAME, \n" +
                        "        INDEXTYPE AS NON_UNIQUE,  \n" +
                        "        '3' AS TYPE, \n" +
                        "        SYSINDEXKEYS.ORDINALPOSITION + 1 AS ORDINAL_POSITION \n" +
                        "   FROM POINTBASE.SYSTABLES, \n" +
                        "        POINTBASE.SYSINDEXES, \n" +
                        "        POINTBASE.SYSINDEXKEYS, \n" +
                        "        POINTBASE.SYSCOLUMNS\n" +
                        "  WHERE SYSTABLES.TABLEID = SYSINDEXES.TABLEID\n" +
                        "    AND SYSINDEXES.INDEXID = SYSINDEXKEYS.INDEXID\n" +
                        "    AND SYSCOLUMNS.COLUMNID = SYSINDEXKEYS.COLUMNID\n" +
                        "    AND SYSTABLES.TABLEID = SYSCOLUMNS.TABLEID\n" +
                        "    AND SYSINDEXES.INDEXTYPE <> 1   \n" +
                        "    AND NOT SYSINDEXES.INDEXID IN (\n" +
                        "        SELECT SYSREFERENTIALCONSTRAINTS.CONSTRAINTINDEXID \n" +
                        "          FROM SYSREFERENTIALCONSTRAINTS) \n" +
                        " ORDER BY TABLE_NAME,INDEX_NAME,ORDINAL_POSITION";
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
                    JdbcTable tempJdbcTable = (JdbcTable)jdbcTableMap.get(
                            temptableName);

                    if (indexName != null && !indexName.equals(
                            tempJdbcTable.pkConstraintName)) {
                        if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                            JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                                    tableName);
                            JdbcIndex[] jdbcIndexes = new JdbcIndex[indexes.size()];
                            indexes.toArray(jdbcIndexes);
                            jdbcTable.indexes = jdbcIndexes;

                            tableName = temptableName;
                            indexes.clear();
                            indexNameMap.clear();

                        }
                        JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                                tableName);
                        if (indexNameMap.containsKey(indexName)) {
                            JdbcIndex index = null;
                            for (Iterator iter = indexes.iterator();
                                 iter.hasNext();) {
                                JdbcIndex jdbcIndex = (JdbcIndex)iter.next();
                                if (jdbcIndex.name.equals(indexName)) {
                                    index = jdbcIndex;
                                }
                            }

                            JdbcColumn[] tempIndexColumns = index.cols;
                            JdbcColumn[] indexColumns = new JdbcColumn[tempIndexColumns.length + 1];
                            System.arraycopy(tempIndexColumns, 0, indexColumns,
                                    0, tempIndexColumns.length);
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
                JdbcTable indexJdbcTable = (JdbcTable)jdbcTableMap.get(
                        tableName);
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
            // end of index ///////////////////////////////////////////////////////////////////////

            if (params.isCheckConstraint()) {

                // now we do forign keys

                String fkSql =
                        "select pt.TABLENAME as PKTABLE_NAME, \n" +
                        "       ft.TABLENAME as FKTABLE_NAME,\n" +
                        "       i.INDEXNAME as FK_NAME,\n" +
                        "       c.COLUMNNAME as FKCOLUMN_NAME,\n" +
                        "       ik.ORDINALPOSITION\n" +
                        "  from SYSREFERENTIALCONSTRAINTS as rc,\n" +
                        "       SYSTABLES as pt, \n" +
                        "       SYSTABLES as ft,\n" +
                        "       SYSINDEXES as i,\n" +
                        "       SYSINDEXKEYS as ik,\n" +
                        "       SYSCOLUMNS as c\n" +
                        " where rc.CONSTRAINTTABLEID = ft.TABLEID\n" +
                        "   and rc.REFERENCETABLEID = pt.TABLEID\n" +
                        "   and rc.CONSTRAINTINDEXID = i.INDEXID\n" +
                        "   and rc.CONSTRAINTINDEXID = ik.INDEXID\n" +
                        "   and ik.COLUMNID = c.COLUMNID\n" +
                        "   and c.TABLEID = ft.TABLEID\n" +
                        " ORDER BY 2,3,5";
                Statement statFK = con.createStatement();
                ResultSet rsFKs = statFK.executeQuery(fkSql);

                HashMap constraintNameMap = null;
                ArrayList constraints = null;
                while (rsFKs.next()) {
                    String temptableName = rsFKs.getString(2);
                    if (tableName == null) { // this is the first one
                        tableName = temptableName;
                        constraintNameMap = new HashMap();
                        constraints = new ArrayList();
                    }

                    if (!temptableName.equals(tableName)) { // now we set everyting up for prev table
                        JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                                tableName);
                        JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                        constraints.toArray(jdbcConstraints);
                        jdbcTable.constraints = jdbcConstraints;

                        tableName = temptableName;
                        constraintNameMap.clear();
                        constraints.clear();
                    }

                    String fkName = rsFKs.getString(3);
                    JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
                    if (constraintNameMap.containsKey(fkName)) {
                        JdbcConstraint constraint = null;
                        for (Iterator iter = constraints.iterator();
                             iter.hasNext();) {
                            JdbcConstraint jdbcConstraint = (JdbcConstraint)iter.next();
                            if (jdbcConstraint.name.equals(fkName)) {
                                constraint = jdbcConstraint;
                            }
                        }

                        JdbcColumn[] tempConstraintColumns = constraint.srcCols;
                        JdbcColumn[] constraintColumns = new JdbcColumn[tempConstraintColumns.length + 1];
                        System.arraycopy(tempConstraintColumns, 0,
                                constraintColumns, 0,
                                tempConstraintColumns.length);
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
                        constraint.dest = (JdbcTable)jdbcTableMap.get(
                                rsFKs.getString(1));
                        constraints.add(constraint);
                    }
                }
                JdbcTable constraintsjdbcTable = (JdbcTable)jdbcTableMap.get(
                        tableName);
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

            }
        }
        HashMap returnMap = new HashMap();
        Collection col = jdbcTableMap.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            JdbcTable table = (JdbcTable)iterator.next();
            returnMap.put(table.name.toLowerCase(), table);
        }
        fixAllNames(returnMap);
        return returnMap;
    }

    public boolean checkScale(JdbcColumn ourCol, JdbcColumn dbCol) {
        switch (ourCol.jdbcType) {
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                return true;
            default:
                return super.checkScale(ourCol, dbCol);
        }
    }

    public boolean checkLenght(JdbcColumn ourCol, JdbcColumn dbCol) {
        switch (ourCol.jdbcType) {
            case Types.BLOB:
            case Types.BINARY:
            case Types.CLOB:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return true;
            default:
                return super.checkLenght(ourCol, dbCol);
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
            appendCreateColumnNulls(t, c, s);
            if (!c.nulls) {
                s.append(" DEFAULT ");
                s.append(getDefaultForType(c));
                s.append(getRunCommand());

                s.append("UPDATE ");
                s.append(t.name);
                s.append(" SET ");
                s.append(c.name);
                s.append(" = ");
                s.append(getDefaultForType(c));
                s.append(getRunCommand());
            } else {
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

        if (length || scale || type || nulls) {
            s.append("\n");
            s.append(
                    comment(
                            "################################ WARNING ###################################\n"));

            if (length) {
                s.append(
                        comment(
                                "# Altering the lenght of a column for Pointbase, is not yet supported.     #\n"));
            }
            if (scale) {
                s.append(
                        comment(
                                "# Altering the scale of a column for Pointbase, is not yet supported.      #\n"));
            }
            if (type) {
                s.append(
                        comment(
                                "# Altering the data type of a column for Pointbase, is not yet supported.  #\n"));
            }
            if (nulls) {
                s.append(
                        comment(
                                "# Altering the null value of a column for Pointbase, is not yet supported. #\n"));
            }
            s.append(
                    comment(
                            "############################################################################"));
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
            s.append(" CASCADE");
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
        s.append(" CASCADE");
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
        s.append(t.name);
        s.append('.');
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
        s.append(" DROP CONSTRAINT ");
        s.append(t.pkConstraintName);
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

    protected void fixColumnsNonDirect(TableDiff tableDiff, PrintWriter out) {

        JdbcTable ourTable = tableDiff.getOurTable();
        String tempTableName = getTempTableName(ourTable, 31);
        CharBuf s = new CharBuf();

        s.append("CREATE TABLE ");
        s.append(tempTableName);
        s.append(" (\n");
        JdbcColumn[] cols = ourTable.getColsForCreateTable();
        int nc = cols.length;
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first) {
                first = false;
            } else {
                s.append("\n");
            }
            s.append("    ");
            appendCreateColumn(ourTable, cols[i], s, true);
        }
        s.append("\n    ");
        appendPrimaryKeyConstraint(ourTable, s);
        s.append("\n)");
        s.append(getRunCommand());


        // we have to make sure that the table does not have nulls when we export it to a not null column.
        for (int i = 0; i < nc; i++) {
            ColumnDiff diff = getColumnDiffForName(tableDiff, cols[i].name);
            if (diff != null && diff.isNullDiff()) {
                if (!diff.getOurCol().nulls) {
                    s.append("UPDATE ");
                    s.append(ourTable.name);
                    s.append("\n   SET ");
                    s.append(diff.getDbCol().name);
                    s.append(" = ");
                    s.append(getDefaultForType(diff.getDbCol()));
                    s.append("\n WHERE ");
                    s.append(diff.getDbCol().name);
                    s.append(" = NULL");
                    s.append(getRunCommand());
                }
            }
        }

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
                        s.append("NULL");
                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append(getDefaultForType(diff.getOurCol()));
                    }

                } else if (diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) {
                    if (i != 0) {
                        s.append("       ");
                    }
                    s.append("CAST(");
                    s.append(cols[i].name);
                    s.append(" AS ");
                    appendColumnType(cols[i], s);
                    s.append(")");

                } else if (diff.isNullDiff()) {
                    if (i != 0) {
                        s.append("       ");
                    }
                    s.append(cols[i].name);
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
        s.append("CREATE TABLE ");
        s.append(mainTempTableName);
        s.append(" (\n    ");
        // create identity column
        s.append(identityColumnName);
        s.append(" INTEGER IDENTITY,");
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
        s.append("\n GROUP BY ");
        s.append(indexColumn.name);
        s.append(',');
        for (int i = 0; i < nc; i++) {
            if (!isAddSequenceColumn(cols[i]) && !t.isInPrimaryKey(
                    cols[i].name)) {
                s.append(cols[i].name);
            }
        }

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
