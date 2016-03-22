
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
import com.versant.core.jdbc.sql.conv.CharacterStreamConverter;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.util.CharBuf;
import com.versant.core.jdo.query.OrNode;

import java.sql.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.Date;

/**
 * A driver for SAPDB.
 */
public class SapDbSqlDriver extends SqlDriver {

    private CharacterStreamConverter.Factory characterStreamConverterFactory
            = new CharacterStreamConverter.Factory();

    private static char[] FOR_UPDATE = " WITH LOCK EXCLUSIVE".toCharArray();

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "sapdb";
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
                return new JdbcTypeMapping("BOOLEAN",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.TINYINT:
                return new JdbcTypeMapping("SMALLINT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.BIGINT:
                return new JdbcTypeMapping("NUMERIC",
                        19, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("TIMESTAMP",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONG",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        characterStreamConverterFactory);
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("LONG BYTE",
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
        n.setMaxColumnNameLength(32);
        n.setMaxTableNameLength(32);
        n.setMaxConstraintNameLength(32);
        n.setMaxIndexNameLength(32);
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
        return false;
    }

    /**
     * Does the JDBC driver support statement batching for updates?
     */
    public boolean isUpdateBatchingSupported() {
        return false;
    }

    /**
     * Does the JDBC driver support scrollable result sets?
     */
    public boolean isScrollableResultSetSupported() {
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
     * Is it ok to convert simple 'exists (select ...)' clauses under an
     * 'or' into outer joins?
     */
    public boolean isOptimizeExistsUnderOrToOuterJoin() {
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
     * Does the JDBC driver support Statement.setFetchSize()?
     */
    public boolean isFetchSizeSupported() {
        return false;
    }

    /**
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        stat.execute("DROP TABLE " + table + " CASCADE");
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
     * Write an SQL statement to a script with appropriate separator.
     */
    protected void print(PrintWriter out, String sql) {
        out.print(sql);
        out.println(";");
        out.println();
    }

    /**
     * Append the from list entry for a table that is the right hand table
     * in a join i.e. it is being joined to.
     * @param exp This is the expression that joins the tables
     * @param outer If true then this is an outer join
     */
    public void appendSqlFromJoin(JdbcTable table, String alias, SqlExp exp,
                                  boolean outer, CharBuf s) {
        s.append(',');
        s.append(' ');
        s.append(table.name);
        if (alias != null) {
            s.append(' ');
            s.append(alias);
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
        if (outer) {
            s.append(' ');
            s.append('(');
            s.append('+');
            s.append(')');
        }
    }

    /**
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return "select KERNEL from VERSIONS";
    }

    /**
     * Append the column auto increment part of a create table statement for a
     * column.
     */
    protected void appendCreateColumnAutoInc(JdbcTable t, JdbcColumn c, CharBuf s) {
        s.append(" DEFAULT SERIAL");
    }

    /**
     * Does this database support autoincrement or serial columns?
     * Autoinc does not work on SAP DB yet.
     */
    public boolean isAutoIncSupported() {
        return false;
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
     * Must columns used in an order by statement appear in the select list?
     */
    public boolean isPutOrderColsInSelect() {
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
        String tableName = null;

        String columnSqlWithoutOracle =
                " SELECT tablename TABLE_NAME,\n" +
                "        columnname COLUMN_NAME,\n" +
                "        decode ((ASCII(decode (datatype,'CHAR','CHAR()','VARCHAR','VARCHAR()','LONG','LONG','LONG RAW','LONG',datatype))\n" +
                "            || (' ' || ASCII(codetype))), 'CHAR', 1, 'CHAR() ASCII', 1, 'CHAR() EBCDIC', 1, 'CHAR() UNICODE', 1, \n" +
                "               'CHAR() BYTE', -2, 'VARCHAR', 12, 'VARCHAR() ASCII', 12, 'VARCHAR() EBCDIC', 12, 'VARCHAR() UNICODE', 12, \n" +
                "               'VARCHAR() BYTE', -3, 'LONG', -1, 'LONG ASCII', -1, 'LONG EBCDIC', -1, 'LONG UNICODE', -1, 'LONG BYTE', -4, \n" +
                "               'LONG RAW', -4, 'FIXED', 3, 'DECIMAL', 3, 'REAL', 7, 'FLOAT', 6, 'DOUBLE PRECISION', 8, 'SMALLINT', 5, \n" +
                "               'INTEGER', 4, 'BOOLEAN', -7, 'TIME', 92, 'DATE', 91, 'TIMESTAMP', 93, 'NUMBER', 2, 1111) DATA_TYPE,\n" +
                "        ASCII(decode(datatype,'CHAR','CHAR()','VARCHAR','VARCHAR()','LONG','LONG','LONG RAW','LONG',datatype))\n" +
                "            || (' ' || ASCII(codetype)) TYPE_NAME,\n" +
                "        len COLUMN_SIZE,\n" +
                "        dec DECIMAL_DIGITS,\n" +
                "        decode(mode, 'OPT', 1, 0) NULLABLE,\n" +
                "        ROWNO ORDINAL_POSITION\n" +
                "   FROM domain.columns\n" +
                "  WHERE not owner in('DOMAIN','DBA')"+
                "  ORDER BY TABLE_NAME, ORDINAL_POSITION";
        String columnSqlWithOracle =
                " SELECT tablename TABLE_NAME,\n" +
                "        columnname COLUMN_NAME,\n" +
                "        decode ((ASCII(decode (datatype,'CHAR','CHAR()','VARCHAR','VARCHAR()','LONG','LONG','LONG RAW','LONG',datatype))\n" +
                "            || (' ' || ASCII(codetype))), 'CHAR', 1, 'CHAR() ASCII', 1, 'CHAR() EBCDIC', 1, 'CHAR() UNICODE', 1, \n" +
                "               'CHAR() BYTE', -2, 'VARCHAR', 12, 'VARCHAR() ASCII', 12, 'VARCHAR() EBCDIC', 12, 'VARCHAR() UNICODE', 12, \n" +
                "               'VARCHAR() BYTE', -3, 'LONG', -1, 'LONG ASCII', -1, 'LONG EBCDIC', -1, 'LONG UNICODE', -1, 'LONG BYTE', -4, \n" +
                "               'LONG RAW', -4, 'FIXED', 3, 'DECIMAL', 3, 'REAL', 7, 'FLOAT', 6, 'DOUBLE PRECISION', 8, 'SMALLINT', 5, \n" +
                "               'INTEGER', 4, 'BOOLEAN', -7, 'TIME', 92, 'DATE', 91, 'TIMESTAMP', 93, 'NUMBER', 2, 1111) DATA_TYPE,\n" +
                "        ASCII(decode(datatype,'CHAR','CHAR()','VARCHAR','VARCHAR()','LONG','LONG','LONG RAW','LONG',datatype))\n" +
                "            || (' ' || ASCII(codetype)) TYPE_NAME,\n" +
                "        len COLUMN_SIZE,\n" +
                "        dec DECIMAL_DIGITS,\n" +
                "        decode(mode, 'OPT', 1, 0) NULLABLE,\n" +
                "        ROWNUM ORDINAL_POSITION\n" +
                "   FROM domain.columns\n" +
                "  WHERE not owner in('DOMAIN','DBA')"+
                "  ORDER BY TABLE_NAME, ORDINAL_POSITION";

        Statement statCol = con.createStatement();
        ResultSet rsColumn = null;
        try {
            rsColumn = statCol.executeQuery(columnSqlWithoutOracle);
        } catch (SQLException e) {
            rsColumn = statCol.executeQuery(columnSqlWithOracle);
        }
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

            col.name = rsColumn.getString(2);
            col.sqlType = rsColumn.getString(4);
            col.jdbcType = rsColumn.getInt(3);
            col.length = rsColumn.getInt(5);
            col.scale = rsColumn.getInt(6);
            col.nulls = rsColumn.getBoolean(7);

            switch (col.jdbcType) {
                case java.sql.Types.BIT:
                case java.sql.Types.TINYINT:
                case java.sql.Types.SMALLINT:
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

                String pkSql =
                        " SELECT tablename TABLE_NAME,\n" +
                        "        columnname COLUMN_NAME,\n" +
                        "        keypos KEY_SEQ\n" +
                        "   FROM domain.columns \n" +
                        "  WHERE keypos is not null\n" +
                        "  ORDER BY TABLE_NAME,KEY_SEQ";

                Statement statPK = con.createStatement();
                ResultSet rsPKs = statPK.executeQuery(pkSql);
                int pkCount = 0;
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


                        tableName = temptableName;
                        pkMap.clear();
                        pkCount = 0;
                    }
                    pkCount++;
                    pkMap.put(rsPKs.getString(2), null);
                }
                JdbcColumn[] pkColumns = new JdbcColumn[pkCount];
                int indexOfPKCount = 0;
                JdbcTable pkJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                if (pkJdbcTable != null){
                    for (int i = 0; i < pkJdbcTable.cols.length; i++) {
                        JdbcColumn jdbcColumn = pkJdbcTable.cols[i];
                        if (pkMap.containsKey(jdbcColumn.name)) {
                            pkColumns[indexOfPKCount] = jdbcColumn;
                            jdbcColumn.pk = true;
                            indexOfPKCount++;
                        }
                    }
                    pkJdbcTable.pk = pkColumns;
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
                String indexSql =
                        "SELECT  TABLE_NAME,\n" +
                        "        COLUMN_NAME,\n" +
                        "        INDEX_NAME,\n" +
                        "        decode (non_unique, 1, 'true', 'false') NON_UNIQUE,\n" +
                        "        TYPE,\n" +
                        "        seq_in_index ORDINAL_POSITION\n" +
                        "   FROM sysodbcindexes\n" +
                        "  WHERE INDEX_NAME <> 'SYSPRIMARYKEYINDEX'\n" +
                        "  ORDER BY TABLE_NAME,INDEX_NAME,ORDINAL_POSITION";
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
                if (tableName != null){
                    JdbcTable indexJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                    if (indexJdbcTable != null){
                        JdbcIndex[] jdbcIndexes = new JdbcIndex[indexes.size()];
                        indexes.toArray(jdbcIndexes);
                        indexJdbcTable.indexes = jdbcIndexes;
                        indexes.clear();
                        indexNameMap.clear();
                    }

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

                String fkSql =
                        " SELECT PKTABLE_NAME,\n" +
                        "        PKCOLUMN_NAME,\n" +
                        "        FKTABLE_NAME,\n" +
                        "        FKCOLUMN_NAME,\n" +
                        "        KEY_SEQ,\n" +
                        "        FK_NAME,\n" +
                        "        PK_NAME\n" +
                        "   FROM sysodbcforeignkeys\n" +
                        "  ORDER BY FKTABLE_NAME, FK_NAME, KEY_SEQ";

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
                if (tableName != null){
                    JdbcTable constraintsjdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                    if (constraintsjdbcTable != null){
                        JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                        constraints.toArray(jdbcConstraints);
                        constraintsjdbcTable.constraints = jdbcConstraints;
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

        s.append("\n");
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" MODIFY ");
        s.append(c.name);
        s.append(' ');
        appendColumnType(c, s);

        if (!c.nulls){
            s.append(" NOT NULL");
        } else {
            s.append(" NULL");
        }
//        if (c.autoinc) {
//            appendCreateColumnAutoInc(t, c, s);
//        }

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
            s.append(" DROP ");
            s.append(c.name);
            s.append(" RELEASE SPACE");
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
//        if (c.autoinc) {
//            appendCreateColumnAutoInc(t, c, s);
//        }
            if (c.nulls) {
                s.append(" NULL");
                s.append(getRunCommand());
            } else {
                s.append(getRunCommand());
                s.append("UPDATE ");
                s.append(t.name);
                s.append(" SET ");
                s.append(c.name);
                s.append(" = ");
                s.append(getDefaultForType(c));
                s.append(getRunCommand());

                s.append("ALTER TABLE ");
                s.append(t.name);
                s.append(" MODIFY ");
                s.append(c.name);
                s.append(" NOT NULL");
                s.append(getRunCommand());
            }
        }
    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {
        String tempTableName = getTempTableName(t, 32);

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
        s.append(" CASCADE");
        s.append(getRunCommand());

        s.append(comment("rename temp table to main table."));
        s.append("\n");
        s.append("RENAME TABLE ");
        s.append(tempTableName);
        s.append(" TO ");
        s.append(t.name);

    }


    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {

        String mainTempTableName = getTempTableName(t, 32);
        String minTempTableName = getTempTableName(t, 32);
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
        s.append(" INTEGER DEFAULT SERIAL,");
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
        s.append("\n GROUP BY ");
        s.append(indexColumn.name);
        s.append(',');
        for (int i = 0; i < nc; i++) {
            if (!isAddSequenceColumn(cols[i]) && !t.isInPrimaryKey(cols[i].name)) {
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
        s.append(" CASCADE");

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
        s.append(" CASCADE");
        s.append(getRunCommand());



        s.append("DROP TABLE ");
        s.append(minTempTableName);
        s.append(" CASCADE");
        s.append(getRunCommand());
    }



}
