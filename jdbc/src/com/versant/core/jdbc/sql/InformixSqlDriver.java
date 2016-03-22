
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
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.util.CharBuf;

import java.sql.*;
import java.util.*;
import java.util.Date;

import com.informix.jdbc.IfmxStatement;

/**
 * A driver for Informix using the ifxjdbc JDBC driver.
 */
public class InformixSqlDriver extends SqlDriver {

    protected AsciiStreamConverter.Factory asciiStreamConverterFactory
            = new AsciiStreamConverter.Factory();

    public InformixSqlDriver() {
    }

    public String getName() {
        return "informix";
    }

    public boolean isAnsiJoinSyntax() {
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
                return new JdbcTypeMapping("NUMERIC",
                        19, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("DATETIME YEAR TO FRACTION",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("TEXT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        asciiStreamConverterFactory);
            case Types.CHAR:
                return new JdbcTypeMapping("CHAR",
                        250, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        null);
            case Types.VARCHAR:
                return new JdbcTypeMapping("VARCHAR",
                        250, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        null);
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("BYTE",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        bytesConverterFactory);
        }
        return super.getTypeMapping(jdbcType);
    }

    public boolean isUseIndexesForOrderCols() {
        return true;
    }

    /**
     * Get the default field mappings for this driver. These map java classes
     * to column properties. Subclasses should override this, call super() and
     * replace mappings as needed.
     */
    public HashMap getJavaTypeMappings() {
        HashMap ans = super.getJavaTypeMappings();

        BooleanConverter.Factory bcf = new BooleanConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(Boolean.TYPE)).setConverterFactory(bcf);
        ((JdbcJavaTypeMapping)ans.get(Boolean.class)).setConverterFactory(bcf);

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(Date.class)).setConverterFactory(dtcf);

        NoMinCharConverter.Factory f = new NoMinCharConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(Character.class, Types.CHAR, 1, 0,
                JdbcJavaTypeMapping.TRUE, f));
        add(ans, new JdbcJavaTypeMapping(Character.TYPE, Types.CHAR, 1, 0,
                JdbcJavaTypeMapping.FALSE, f));

        return ans;
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
     * Must columns used in an order by statement appear in the select list?
     */
    public boolean isPutOrderColsInSelect() {
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
        return false;
    }

    /**
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return "select first 1 tabname from systables";
    }

    /**
     * Get default SQL used to init a connection or null if none required.
     */
    public String getConnectionInitSQL() {
        return "set lock mode to wait 30";
    }

    /**
     * Can the tx isolation level be set on this database?
     */
    public boolean isSetTransactionIsolationLevelSupported() {
        return false;
    }

    /**
     * Does this database support autoincrement or serial columns?
     */
    public boolean isAutoIncSupported() {
        return true;
    }

    /**
     * Append the part of a create table statement for a column.
     */
    protected void appendCreateColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {
        if (c.autoinc) {
            int si = s.size();
            s.append(c.name);
            s.append(' ');
            if (c.jdbcType == Types.BIGINT) {
                s.append("SERIAL8");
            } else {
                s.append("SERIAL");
            }
            appendCreateColumnNulls(t, c, s);
            s.append(',');
            if (comments && c.comment != null) {
                s.append(' ');
                si += COMMENT_COL;
                for (; s.size() < si; s.append(' ')) ;
                s.append(comment(c.comment));
            }
        } else {
            super.appendCreateColumn(t, c, s, comments);
        }
    }

    /**
     * Retrieve the value of the autoinc or serial column for a row just
     * inserted using stat on con.
     */
    public Object getAutoIncColumnValue(JdbcTable classTable,
            Connection con, Statement stat) throws SQLException {
        if (classTable.pk[0].jdbcType == Types.BIGINT) {
            return new Long(((IfmxStatement)stat).getSerial8());
        } else {
            return new Integer(((IfmxStatement)stat).getSerial());
        }
    }

    /**
     * Get whatever needs to be appended to a SELECT statement to lock the
     * rows if this makes sense for the database. This must have a leading
     * space if not empty.
     */
    public char[] getSelectForUpdate() {
        return null;
    }

    protected boolean isValidSchemaTable(String tableName) {
        String[] sysNames = new String[]{
            "sysblobs",
            "syschecks",
            "syscolauth",
            "syscoldepend",
            "syscolumns",
            "sysconstraints",
            "sysdefaults",
            "sysdepend",
            "sysdistrib",
            "sysfragauth",
            "sysfragments",
            "sysindexes",
            "sysobjstate",
            "sysopclstr",
            "sysprocauth",
            "sysprocbody",
            "sysprocedures",
            "sysprocplan",
            "sysreferences",
            "sysroleauth",
            "syssynonyms",
            "syssyntable",
            "systabauth",
            "systables",
            "systrigbody",
            "systriggers",
            "sysusers",
            "sysviews",
            "sysviolations", // to here is in 7
            "sysaggregates", // from here is in 9
            "sysams",
            "sysattrtypes",
            "syscasts",
            "syscolattribs",
            "sysdomains",
            "syserrors",
            "sysindices",
            "sysinherits",
            "syslangauth",
            "syslogmap",
            "sysopclasses",
            "sysroutinelangs",
            "systabamdata",
            "systraceclasses",
            "systracemsgs",
            "sysxtddesc",
            "sysxtdtypeauth",
            "sysxtdtypes"};

        for (int i = 0; i < sysNames.length; i++) {
            if (sysNames[i].equals(tableName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the JdbcTable from the database for the given database connection and table name.
     */
    public HashMap getDBSchema(Connection con, ControlParams params)
            throws SQLException {
        DatabaseMetaData meta = con.getMetaData();

        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables

        String catalog = getCatalog(con);
        String schema = getSchema(con);

        // now we do columns
        String tableName = null;
        ResultSet rsColumn = meta.getColumns(catalog, schema, null, null);
        ArrayList currentColumns = null;

        while (rsColumn.next()) {

            String temptableName = rsColumn.getString("TABLE_NAME").trim();

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

            col.name = rsColumn.getString("COLUMN_NAME").trim();
            col.sqlType = rsColumn.getString("TYPE_NAME").trim();
            col.jdbcType = rsColumn.getInt("DATA_TYPE");
            if (col.sqlType.equals("int") ||
                    col.sqlType.equals("smallint") ||
                    col.sqlType.equals("tinyint") ||
                    col.sqlType.equals("float") ||
                    col.sqlType.equals("smallfloat") ||
                    col.sqlType.equals("serial8") ||
                    col.sqlType.equals("serial")) {
                col.scale = 0;
                col.length = 0;
            } else {
                col.length = rsColumn.getInt("COLUMN_SIZE");
                col.scale = rsColumn.getInt("DECIMAL_DIGITS");
            }

            if (col.sqlType.equals("decimal")) {
                if (col.scale == 255) {
                    col.scale = 0;
                }
            }
            col.nulls = "YES".equals(rsColumn.getString("IS_NULLABLE").trim());

            currentColumns.add(col);
        }
        // we fin last table
        if (currentColumns != null) {
            JdbcColumn[] lastJdbcColumns = new JdbcColumn[currentColumns.size()];
            if (lastJdbcColumns != null) {
                currentColumns.toArray(lastJdbcColumns);
                JdbcTable colJdbcTable = (JdbcTable)jdbcTableMap.get(tableName);
                colJdbcTable.cols = lastJdbcColumns;
                currentColumns.clear();
            }
        }
        tableName = null;
        if (rsColumn != null) {
            try {
                rsColumn.close();
            } catch (SQLException e) {
            }
        }

        if (!params.checkColumnsOnly()) {
            Set mainTableNames = jdbcTableMap.keySet();
            if (params.isCheckPK()) {
                // now we do primaryKeys ///////////////////////////////////////////////////////////////////////
                for (Iterator iterator = mainTableNames.iterator();
                     iterator.hasNext();) {
                    tableName = (String)iterator.next();
                    JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
                    HashMap pkMap = new HashMap();
                    HashMap pkNames = new HashMap();
                    ResultSet rsPKs = meta.getPrimaryKeys(catalog, schema,
                            tableName);
                    int pkCount = 0;
                    while (rsPKs.next()) {
                        pkCount++;

                        String colName = rsPKs.getString("COLUMN_NAME").trim();
                        pkMap.put(colName, null);
                        String pkName = rsPKs.getString("PK_NAME").trim();
                        jdbcTable.pkConstraintName = pkName;
                        pkNames.put(pkName, null);

                    }
                    rsPKs.close();
                    if (pkCount != 0) {
                        JdbcColumn[] pkColumns = new JdbcColumn[pkCount];
                        if (pkColumns != null) {
                            int indexOfPKCount = 0;
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (pkMap.containsKey(jdbcColumn.name)) {
                                    pkColumns[indexOfPKCount] = jdbcColumn;
                                    jdbcColumn.pk = true;
                                    indexOfPKCount++;
                                }
                            }
                            jdbcTable.pk = pkColumns;
                        }
                    }

                }
                tableName = null;
                // end of primaryKeys ///////////////////////////////////////////////////////////////////////
            }
            if (params.isCheckIndex()) {
                // now we do index  /////////////////////////////////////////////////////////////////////////
                for (Iterator iterator = mainTableNames.iterator();
                     iterator.hasNext();) {
                    tableName = (String)iterator.next();
                    JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
                    ResultSet rsIndex = null;
                    try {
                        rsIndex = meta.getIndexInfo(catalog, schema, tableName,
                                false, false);
                    } catch (SQLException e) {
                        iterator.remove();
                        continue;
                    }

                    HashMap indexNameMap = new HashMap();
                    ArrayList indexes = new ArrayList();
                    while (rsIndex.next()) {

                        String indexName = rsIndex.getString("INDEX_NAME").trim();
                        char[] chars = indexName.toCharArray();

                        if (chars.length > 5 &&
                                !Character.isLetter(chars[0]) &&
                                !Character.isLetter(chars[1]) &&
                                !Character.isLetter(chars[2]) &&
                                !Character.isLetter(chars[3])) {
                            continue;
                        }

                        if (indexName != null
                                && !indexName.equals(
                                        jdbcTable.pkConstraintName)) {
                            if (indexNameMap.containsKey(indexName)) {
                                JdbcIndex index = null;
                                for (Iterator iter = indexes.iterator();
                                     iter.hasNext();) {
                                    JdbcIndex jdbcIndex = (JdbcIndex)iter.next();
                                    if (jdbcIndex.name.equals(indexName)) {
                                        index = jdbcIndex;
                                    }
                                }
                                if (index != null) {
                                    JdbcColumn[] tempIndexColumns = index.cols;
                                    JdbcColumn[] indexColumns = new JdbcColumn[tempIndexColumns.length + 1];
                                    System.arraycopy(tempIndexColumns, 0,
                                            indexColumns, 0,
                                            tempIndexColumns.length);
                                    String colName = rsIndex.getString(
                                            "COLUMN_NAME").trim();
                                    for (int i = 0;
                                         i < jdbcTable.cols.length; i++) {
                                        JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                        if (colName.equals(jdbcColumn.name)) {
                                            indexColumns[tempIndexColumns.length] = jdbcColumn;
                                            jdbcColumn.partOfIndex = true;
                                        }
                                    }
                                    index.setCols(indexColumns);
                                }
                            } else {
                                indexNameMap.put(indexName, null);
                                JdbcIndex index = new JdbcIndex();
                                index.name = indexName;
                                index.unique = !rsIndex.getBoolean(
                                        "NON_UNIQUE");
                                short indexType = rsIndex.getShort("TYPE");
                                switch (indexType) {
                                    case DatabaseMetaData.tableIndexClustered:
                                        index.clustered = true;
                                        break;
                                }
                                String colName = rsIndex.getString(
                                        "COLUMN_NAME").trim();
                                JdbcColumn[] indexColumns = new JdbcColumn[1];
                                for (int i = 0;
                                     i < jdbcTable.cols.length; i++) {
                                    JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                    if (colName.equals(jdbcColumn.name)) {
                                        indexColumns[0] = jdbcColumn;
                                        jdbcColumn.partOfIndex = true;
                                    }
                                }
                                if (indexColumns[0] != null) {
                                    index.setCols(indexColumns);
                                    indexes.add(index);
                                }
                            }
                        }
                    }
                    if (indexes != null) {
                        JdbcIndex[] jdbcIndexes = new JdbcIndex[indexes.size()];
                        if (jdbcIndexes != null) {
                            indexes.toArray(jdbcIndexes);
                            jdbcTable.indexes = jdbcIndexes;
                        }
                    }
                    if (rsIndex != null) {
                        try {
                            rsIndex.close();
                        } catch (SQLException e) {
                        }
                    }
                }
                tableName = null;
                // end of index ///////////////////////////////////////////////////////////////////////
            }

            if (params.isCheckConstraint()) {
                // now we do forign keys

                String fkSql =
                        "SELECT pt.tabname,\n" +
                        "       pc.colname,\n" +
                        "       ft.tabname,\n" +
                        "       fc.colname,\n" +
                        "       fk.constrname,\n" +
                        "       pk.constrname\n" +
                        "  FROM informix.systables pt,\n" +
                        "       informix.syscolumns pc,\n" +
                        "       informix.sysindexes pii,\n" +
                        "       informix.sysconstraints pk,\n" +
                        "       informix.systables ft,\n" +
                        "       informix.syscolumns fc,\n" +
                        "       informix.sysindexes fi,\n" +
                        "       informix.sysconstraints fk,\n" +
                        "       informix.sysreferences r\n" +
                        " WHERE pt.tabid = pc.tabid\n" +
                        "   AND pc.tabid = pii.tabid\n" +
                        "   AND pt.tabid = pk.tabid\n" +
                        "   AND pk.constrid = r.PRIMARY \n" +
                        "   AND r.constrid = fk.constrid\n" +
                        "   AND pii.idxname = pk.idxname\n" +
                        "   AND fi.idxname = fk.idxname\n" +
                        "   AND ft.tabid = fc.tabid\n" +
                        "   AND fc.tabid = fi.tabid\n" +
                        "   AND ft.tabid = fk.tabid\n" +
                        "   AND (pc.colno = ABS (pii.part1)\n" +
                        "       AND fc.colno = ABS (fi.part1) OR pc.colno = ABS (pii.part2)\n" +
                        "       AND fc.colno = ABS (fi.part2) OR pc.colno = ABS (pii.part3)\n" +
                        "       AND fc.colno = ABS (fi.part3) OR pc.colno = ABS (pii.part4)\n" +
                        "       AND fc.colno = ABS (fi.part4) OR pc.colno = ABS (pii.part5)\n" +
                        "       AND fc.colno = ABS (fi.part5) OR pc.colno = ABS (pii.part6)\n" +
                        "       AND fc.colno = ABS (fi.part6) OR pc.colno = ABS (pii.part7)\n" +
                        "       AND fc.colno = ABS (fi.part7) OR pc.colno = ABS (pii.part8)\n" +
                        "       AND fc.colno = ABS (fi.part8) OR pc.colno = ABS (pii.part9)\n" +
                        "       AND fc.colno = ABS (fi.part9) OR pc.colno = ABS (pii.part10)\n" +
                        "       AND fc.colno = ABS (fi.part10) OR pc.colno = ABS (pii.part11)\n" +
                        "       AND fc.colno = ABS (fi.part11) OR pc.colno = ABS (pii.part12)\n" +
                        "       AND fc.colno = ABS (fi.part12) OR pc.colno = ABS (pii.part13)\n" +
                        "       AND fc.colno = ABS (fi.part13) OR pc.colno = ABS (pii.part14)\n" +
                        "       AND fc.colno = ABS (fi.part14) OR pc.colno = ABS (pii.part15)\n" +
                        "       AND fc.colno = ABS (fi.part15) OR pc.colno = ABS (pii.part16)\n" +
                        "       AND fc.colno = ABS (fi.part16))\n" +
                        " ORDER BY ft.tabname, fk.constrname";
                Statement statFK = con.createStatement();
                ResultSet rsFKs = statFK.executeQuery(fkSql);

                HashMap constraintNameMap = null;
                ArrayList constraints = null;
                while (rsFKs.next()) {
                    String temptableName = rsFKs.getString(3).trim();
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

                    String fkName = rsFKs.getString(5).trim();
                    JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);

                    if (jdbcTable == null) continue;

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
                        String colName = rsFKs.getString(4).trim();
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
                        String colName = rsFKs.getString(4).trim();
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
                                rsFKs.getString(1).trim());
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
            if (c.nulls) {
                appendCreateColumnNulls(t, c, s);
                s.append(getRunCommand());
            } else {
                s.append(";\n");
                s.append("UPDATE ");
                s.append(t.name);
                s.append(" SET ");
                s.append(c.name);
                s.append(" = ");
                s.append(getDefaultForType(c));
                s.append(getRunCommand());

                s.append("ALTER TABLE ");
                s.append(t.name);
                s.append(" MODIFY (");
                s.append(c.name);
                s.append(' ');
                appendColumnType(c, s);
                appendCreateColumnNulls(t, c, s);
                s.append(')');
                s.append(getRunCommand());
            }
        }
    }

    protected boolean useZeroScale(JdbcColumn c) {
        if ("NUMERIC".equals(c.sqlType)) {
            return true;
        }
        return false;
    }

    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {

        String mainTempTableName = getTempTableName(t, 18);
        String minTempTableName = getTempTableName(t, 18);
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
        s.append(comment("create a temp table with a extra serial column."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(mainTempTableName);
        s.append(" (\n    ");
        // create identity column
        s.append(identityColumnName);
        s.append(" SERIAL NOT NULL,");
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
        int lastIndex = s.toString().lastIndexOf(',');
        s.replace(lastIndex, lastIndex + 1, ' ');// we take the last ',' out.
        s.append("\n)");

        s.append(getRunCommand());
        s.append("\n");
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
        s.append("min_id INTEGER\n)");

        s.append(getRunCommand());

        s.append(comment("store the id's."));
        s.append("\n");
        s.append("INSERT INTO ");
        s.append(minTempTableName);
        s.append(" (");
        s.append(indexColumn.name);
        s.append(")\n");
        s.append("SELECT ");
        s.append(indexColumn.name);
        s.append("\n  FROM ");
        s.append(t.name);
        s.append("\n");
        s.append(" GROUP BY ");
        s.append(indexColumn.name);

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
                s.append("a.");
                s.append(cols[i].name);
            }
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append("\n  FROM ");
        s.append(t.name);
        s.append(" a,\n        ");
        s.append(minTempTableName);
        s.append(" b");
        s.append("\n WHERE a.");
        s.append(indexColumn.name);
        s.append(" = b.");
        s.append(indexColumn.name);

        s.append(getRunCommand());

        s.append(comment("store the minimum id."));
        s.append("\n");
        s.append("UPDATE ");
        s.append(minTempTableName);
        s.append("\n   SET min_id = ");
        s.append("\n       (SELECT MIN(");
        s.append(identityColumnName);
        s.append(")\n          FROM ");
        s.append(mainTempTableName);
        s.append("\n         WHERE ");
        s.append(indexColumn.name);
        s.append(" = ");
        s.append(minTempTableName);
        s.append(".");
        s.append(indexColumn.name);
        s.append(")");

        s.append(getRunCommand());

        s.append(comment("update the temp table's sequence column."));
        s.append("\n");
        s.append("UPDATE ");
        s.append(mainTempTableName);
        s.append("\n   SET ");
        s.append(sequenceColumn.name);
        s.append(" = ");
        s.append(identityColumnName);
        s.append(" - \n       (SELECT a.min_id\n          FROM ");
        s.append(minTempTableName);
        s.append(" a\n         WHERE ");
        s.append(mainTempTableName);
        s.append(".");
        s.append(indexColumn.name);
        s.append(" = a.");
        s.append(indexColumn.name);
        s.append(")");

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
            s.append(cols[i].name);

            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append("\n  FROM ");
        s.append(mainTempTableName);

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

    /**
     * Append a column that needs to be added.
     */
    protected void appendModifyColumn(TableDiff tableDiff, ColumnDiff diff, CharBuf s,
            boolean comments) {
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
        s.append(" MODIFY (");
        s.append(c.name);
        s.append(' ');
        appendColumnType(c, s);
        appendCreateColumnNulls(t, c, s);
        s.append(')');
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
            s.append(tableDiff.getDbTable().name);
            s.append(" DROP ");
            s.append(c.name);
        }

    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {
        String tempTableName = getTempTableName(t, 18);

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
        s.append("RENAME TABLE ");
        s.append(tempTableName);
        s.append(" TO ");
        s.append(t.name);

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
        s.append("DROP INDEX ");
        s.append(idx.name);
//        s.append("ALTER TABLE ");
//        s.append(t.name);
//        s.append(" DROP CONSTRAINT ");
//        s.append(idx.name);
    }

    /**
     * Add the primary key constraint in isolation.
     */
    protected void addPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" ADD CONSTRAINT ");
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

}
