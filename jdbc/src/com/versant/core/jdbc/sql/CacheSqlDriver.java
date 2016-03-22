
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
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.util.CharBuf;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.io.PrintWriter;

/**
 * A driver for Cache object database.
 */
public class CacheSqlDriver extends SqlDriver {

    private InputStreamConverter.Factory blobConverterFactory
            = new InputStreamConverter.Factory();
    private CharacterStreamConverter.Factory clobConverterFactory
            = new CharacterStreamConverter.Factory();
    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "cache";
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
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.BIGINT:
                return new JdbcTypeMapping("NUMERIC",
                        18, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,null);
            case Types.BLOB:
            case Types.LONGVARBINARY:
                return new JdbcTypeMapping("LONGVARBINARY",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        blobConverterFactory);
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONGVARCHAR",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        clobConverterFactory);
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
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet rsFKs = metaData.getExportedKeys(null, null, table);

        ArrayList a = new ArrayList();
        while (rsFKs.next()) {
            String tableName = rsFKs.getString("FKTABLE_NAME");
            String conName = rsFKs.getString("FK_NAME");
            a.add("ALTER TABLE " + tableName + " DROP CONSTRAINT " + conName);
        }
        try {
            rsFKs.close();
        } catch (SQLException e1) {
            // hide
        }
        for (Iterator i = a.iterator(); i.hasNext();) {
            String sql = (String) i.next();
            try {
                stat.execute(sql);
            } catch (SQLException x) {
                /*some times it's a bit slow to update it's system tables and we get a exeption*/
            }
        }
        stat.execute("DROP TABLE " + table + " CASCADE");
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
        s.append("CREATE TABLE ");
        s.append(t.name);
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
        return true;
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
        n.setMaxColumnNameLength(40);
        n.setMaxTableNameLength(40);
        n.setMaxConstraintNameLength(40);
        n.setMaxIndexNameLength(40);
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

    protected String getSchema(Connection con) {
        /*This is very messy, and if anybody knows how to do this better
        please tell me (carl)!!!!*/
        Statement stat = null;
        String schema = null;
        try {
            stat = con.createStatement();
            stat.execute("SELECT * FROM XXX_XXX_XXX_XXX_XXX");
        } catch (SQLException e) {
            try {
                String msg = e.getMessage();
                int start = msg.indexOf("SQL ERROR #30");
                int end = msg.indexOf(".XXX_XXX_XXX_XXX_XXX'");
                String iffy = msg.substring(start, end);
                start = iffy.indexOf('`');
                schema = iffy.substring(start+1);
            } catch (Exception x) {
                // now we try someting else
                ResultSet rs = null;
                try {
                    ArrayList list = new ArrayList();
                    DatabaseMetaData metaData = con.getMetaData();
                    rs = metaData.getSchemas();
                    while(rs.next()){
                        list.add(rs.getString(1));
                    }
                    if (list.contains("SQLUser")){
                        return "SQLUser";
                    }
                } catch (SQLException e1) {
                    return null;
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e2) {
                            //hide
                        }
                    }
                }
                return null;
            }
        } finally {
            if (stat != null){
                try {
                    stat.close();
                } catch (SQLException e) {
                    //hide
                }
            }
        }
        return schema;
    }

    /**
     * Get the JdbcTable from the database for the given database connection and table name.
     */
    public HashMap getDBSchema(Connection con, ControlParams params) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();

        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables

        String catalog = getCatalog(con);
        String schema = getSchema(con);

        // now we do columns
        String tableName = null;
        ResultSet rsColumn = meta.getColumns(catalog, schema, null, null);
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
                JdbcTable jdbcTable0 = (JdbcTable) jdbcTableMap.get(tableName);
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
            col.nulls = "YES".equals(rsColumn.getString("IS_NULLABLE"));
            if (col.jdbcType == 2 && col.scale == 0){
                col.length = col.length + 2;
            }
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
            currentColumns.add(col);
        }
        // we fin last table
        if (currentColumns != null) {
            JdbcColumn[] lastJdbcColumns = new JdbcColumn[currentColumns.size()];
            if (lastJdbcColumns != null) {
                currentColumns.toArray(lastJdbcColumns);
                JdbcTable colJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                colJdbcTable.cols = lastJdbcColumns;
                tableName = null;
                currentColumns.clear();
            }
        }

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
                for (Iterator iterator = mainTableNames.iterator(); iterator.hasNext();) {
                    tableName = (String) iterator.next();
                    JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                    HashMap pkMap = new HashMap();
                    HashMap pkNames = new HashMap();
                    ResultSet rsPKs = meta.getPrimaryKeys(catalog, schema, tableName);
                    int pkCount = 0;
                    while (rsPKs.next()) {
                        pkCount++;
                        pkMap.put(rsPKs.getString("COLUMN_NAME"), null);
                        String pkName = rsPKs.getString("PK_NAME");
                        jdbcTable.pkConstraintName = pkName;
                        pkNames.put(pkName, null);
                    }
                    rsPKs.close();
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

                // end of primaryKeys ///////////////////////////////////////////////////////////////////////
            }
            if (params.isCheckIndex()) {
                // now we do index  /////////////////////////////////////////////////////////////////////////
                for (Iterator iterator = mainTableNames.iterator(); iterator.hasNext();) {
                    tableName = (String) iterator.next();
                    JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                    ResultSet rsIndex = null;
                    try {
                        rsIndex = meta.getIndexInfo(catalog, schema, tableName, false, false);
                    } catch (SQLException e) {
                        iterator.remove();
                        continue;
                    }


                    HashMap indexNameMap = new HashMap();
                    ArrayList indexes = new ArrayList();
                    while (rsIndex.next()) {

                        String indexName = rsIndex.getString("INDEX_NAME");
                        if (indexName != null
                                && !indexName.equals(jdbcTable.pkConstraintName)
                                && !indexName.startsWith("SYS_IDX_")) {
                            if (indexNameMap.containsKey(indexName)) {
                                JdbcIndex index = null;
                                for (Iterator iter = indexes.iterator(); iter.hasNext();) {
                                    JdbcIndex jdbcIndex = (JdbcIndex) iter.next();
                                    if (jdbcIndex.name.equals(indexName)) {
                                        index = jdbcIndex;
                                    }
                                }
                                if (index != null) {
                                    JdbcColumn[] tempIndexColumns = index.cols;
                                    JdbcColumn[] indexColumns = new JdbcColumn[tempIndexColumns.length + 1];
                                    System.arraycopy(tempIndexColumns, 0, indexColumns, 0, tempIndexColumns.length);
                                    String colName = rsIndex.getString("COLUMN_NAME");
                                    for (int i = 0; i < jdbcTable.cols.length; i++) {
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
                                index.unique = !rsIndex.getBoolean("NON_UNIQUE");
                                short indexType = rsIndex.getShort("TYPE");
                                switch (indexType) {
                                    case DatabaseMetaData.tableIndexClustered:
                                        index.clustered = true;
                                        break;
                                }
                                String colName = rsIndex.getString("COLUMN_NAME");
                                JdbcColumn[] indexColumns = new JdbcColumn[1];
                                for (int i = 0; i < jdbcTable.cols.length; i++) {
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

                // end of index ///////////////////////////////////////////////////////////////////////
            }
            if (params.isCheckConstraint()) {
                // now we do forign keys /////////////////////////////////////////////////////////////
                for (Iterator iterator = mainTableNames.iterator(); iterator.hasNext();) {
                    tableName = (String) iterator.next();
                    JdbcTable jdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                    ResultSet rsFKs = null;
                    try {
                        rsFKs = meta.getImportedKeys(catalog, schema, tableName);
                    } catch (SQLException e) {
                        iterator.remove();
                        continue;
                    }
                    HashMap constraintNameMap = new HashMap();
                    ArrayList constraints = new ArrayList();
                    while (rsFKs.next()) {


                        String fkName = rsFKs.getString("FK_NAME");

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
                            String colName = rsFKs.getString("FKCOLUMN_NAME");
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
                            String colName = rsFKs.getString("FKCOLUMN_NAME");
                            JdbcColumn[] constraintColumns = new JdbcColumn[1];
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equals(jdbcColumn.name)) {
                                    constraintColumns[0] = jdbcColumn;
                                    jdbcColumn.foreignKey = true;
                                }
                            }
                            constraint.srcCols = constraintColumns;
                            constraint.dest = (JdbcTable) jdbcTableMap.get(rsFKs.getString("PKTABLE_NAME"));
                            constraints.add(constraint);
                        }

                    }
                    if (constraints != null) {
                        JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                        if (jdbcConstraints != null) {
                            constraints.toArray(jdbcConstraints);
                            jdbcTable.constraints = jdbcConstraints;
                        }
                    }
                    if (rsFKs != null) {
                        try {
                            rsFKs.close();
                        } catch (SQLException e) {
                        }
                    }
                }
                // end of forign keys /////////////////////////////////////////////////////////////
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
    protected void appendModifyColumn(TableDiff tableDiff, ColumnDiff diff,
                                      CharBuf s, boolean comments) {
        JdbcTable t = tableDiff.getOurTable();
        JdbcColumn ourCol = diff.getOurCol();
        boolean nulls = diff.isNullDiff();

        if (comments && isCommentSupported() && ourCol.comment != null) {
            s.append(comment("modify column for field " + ourCol.comment));
        }
        if (comments && isCommentSupported() && ourCol.comment == null) {
            s.append(comment("modify column " + ourCol.name));
        }

        s.append("\n");

        if (nulls) {
            if (!ourCol.nulls) {
                s.append("UPDATE ");
                s.append(t.name);
                s.append("\n");
                s.append("   SET ");
                s.append(ourCol.name);
                s.append(" = ");
                s.append(getDefaultForType(ourCol));
                s.append("\n");
                s.append(" WHERE ");
                s.append(ourCol.name);
                s.append(" = NULL");

                s.append(getRunCommand());

            }

        }

        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" MODIFY ");
        s.append(ourCol.name);
        s.append(' ');
        appendColumnType(ourCol, s);
        if (nulls) {
            appendCreateColumnNulls(t, ourCol, s);
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
//        s.append(t.pkConstraintName);
    }

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {
        String tempTableName = getTempTableName(t, 40);

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


        s.append(comment("create a the original table again."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(t.name);
        s.append(" (\n");
        first = true;
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

        s.append(comment("insert the list back into the main table."));
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
        s.append(tempTableName);
        s.append(getRunCommand());

        s.append(comment("drop temp table."));
        s.append("\n");
        s.append("DROP TABLE ");
        s.append(tempTableName);


    }

    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {

        String mainTempTableName = getTempTableName(t, 40);
        String minTempTableName = getTempTableName(t, 40);


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
        s.append(comment("create a temp table."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(mainTempTableName);
        s.append(" (");
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
        s.append("MIN(ID)\n");
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
                s.append("(a.ID - b.min_id)");
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
