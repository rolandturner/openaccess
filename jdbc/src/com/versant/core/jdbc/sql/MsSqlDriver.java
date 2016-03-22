
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
import com.versant.core.jdbc.sql.exp.BinaryOpExp;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.util.CharBuf;
import com.versant.core.common.BindingSupportImpl;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.io.PrintWriter;

/**
 * A driver for Microsoft SQL server using their JDBC driver.
 */
public class MsSqlDriver extends SqlDriver {

    private ClobStringConverter.Factory clobStringConverterFactory
            = new ClobStringConverter.Factory();

    /**
     * If we ever need to support SQL Server 6.5 this must be detirmined
     * from the dataserver version (also isOptimizeExistsUnderOrToOuterJoin).
     */
    private static final boolean ansiJoinSyntax = true;

    private static final String IDENTITY_FETCH = "\nselect @@identity";
    private static final String IDENTITY_FETCH_2000 = "\nselect scope_identity()";

    private String identityFetch = IDENTITY_FETCH_2000;

    private boolean usingJtds;

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
                return new JdbcTypeMapping("TINYINT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.BIGINT:
                return new JdbcTypeMapping("NUMERIC",
                        19, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("DATETIME",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.CLOB:
                // the converter is only required here as CLOB is not the
                // correct JDBC type when setting null on ps (must be
                // LONGVARCHAR)
                return new JdbcTypeMapping("TEXT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        clobStringConverterFactory);
            case Types.LONGVARCHAR:
                // unlike CLOB this does not need the clobStringConverterFactory
                // as LONGVARCHAR is the correct JDBC type for Sybase when
                // setting null on ps
                return new JdbcTypeMapping("TEXT",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.VARBINARY:
                return new JdbcTypeMapping("VARBINARY",
                        255, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        nullBytesAsBinaryConverterFactory);
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("IMAGE",
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        nullBytesAsBinaryConverterFactory);
        }
        return super.getTypeMapping(jdbcType);
    }

    public boolean isPutOrderColsInSelect() {
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
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Boolean.TYPE/*RIGHTPAR*/)).setConverterFactory(bcf);
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Boolean.class/*RIGHTPAR*/)).setConverterFactory(bcf);

        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Byte.TYPE/*RIGHTPAR*/)).setJdbcType(Types.SMALLINT);
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Byte.class/*RIGHTPAR*/)).setJdbcType(Types.SMALLINT);

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(/*CHFC*/Date.class/*RIGHTPAR*/)).setConverterFactory(dtcf);
        

        return ans;
    }

    public boolean isNullForeignKeyOk() {
		// dirkt: after consultation with david disabled again, because this 
		// change might break many 
		// customer apps, therefore it should be configurable. 
		
        return false;
    }

    /**
     * Create a default name generator instance for JdbcStore's using this
     * driver.
     */
    public JdbcNameGenerator createJdbcNameGenerator() {
        DefaultJdbcNameGenerator n = createDefaultJdbcNameGenerator();
        n.setMaxColumnNameLength(30);
        n.setMaxTableNameLength(30);
        n.setMaxConstraintNameLength(30);
        n.setMaxIndexNameLength(30);
        return n;
    }

    /**
     * Figure out if we are using jtds.
     */
    public void customizeForDriver(Driver jdbcDriver) {



        try {         
            usingJtds = jdbcDriver.acceptsURL(
                    "jdbc:jtds:sqlserver://localhost:1433");
        } catch (SQLException e) {
            // ignore
        }
 
    }

    public boolean isCustomizeForServerRequired() {
        return true;
    }

    /**
     * Perform any specific configuration appropriate for the database server
     * in use. If any SQL is done on con call con.commit() before returning.
     */
    public void customizeForServer(Connection con) throws SQLException {
        identityFetch = IDENTITY_FETCH_2000;
        try {
            String ver = getMsSqlVersion(con);
            int i = ver.indexOf('-') + 1;
            for (; ver.charAt(i) == ' '; i++) ;
            int j = ver.indexOf('.', i);
            int major = Integer.parseInt(ver.substring(i, j));
            if (major >= 8) {
                identityFetch = IDENTITY_FETCH_2000;
            } else {
                identityFetch = IDENTITY_FETCH;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (NumberFormatException e) {
        }
    }

    /**
     * Get the version of MS SQL on con.
     */
    private String getMsSqlVersion(Connection con) throws SQLException {
        String ver;
        Statement stat = null;
        ResultSet rs = null;
        try {
            stat = con.createStatement();
            rs = stat.executeQuery("select @@version");
            rs.next();
            ver = rs.getString(1);
            con.commit();
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
        return ver;
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
        out.println(sql);
        out.println("go");
        out.println();
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
        if (ansiJoinSyntax) {
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
        } else {
            s.append(',');
            s.append(' ');
            s.append(table.name);
            if (alias != null) {
                s.append(' ');
                s.append(alias);
            }
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
        if (outer && !ansiJoinSyntax) s.append('*');
        s.append('=');
        s.append(' ');
        s.append(rightAlias);
        s.append('.');
        s.append(right.name);
    }

    /**
     * Get the string form of a binary operator.
     *
     * @see com.versant.core.jdbc.sql.exp.BinaryOpExp
     */
    public String getSqlBinaryOp(int op) {
        switch (op) {
            case BinaryOpExp.CONCAT:
                return "+";
        }
        return super.getSqlBinaryOp(op);
    }

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "mssql";
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
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        ResultSet rs = null;
        try {
            stat = con.createStatement();        
            stat.execute("sp_helpconstraint " + table);     
            skipResultSet(stat);        // skip object name result set
            skipResultSet(stat);        // skip constraints on table itself
            rs = stat.getResultSet();   // foreign key constraints
            if (rs != null) {
                // each row has a String like 'ortest.ortest.grp_item: fk4'
                ArrayList a = new ArrayList();
                try {
                    for (; rs.next();) {
                        String s = rs.getString(1);
                        int i = s.indexOf(':');
                        String tn = s.substring(0, i);
                        String cname = s.substring(i + 2);
                        a.add(
                                "ALTER TABLE " + tn + " DROP CONSTRAINT " + cname);
                    }
                    rs.close();
                } catch (SQLException e) {
                    if (!usingJtds) throw e;
                    // normal for jtds to whine here - if there are no fk
                    // constraints then stat.getResultSet returns the previous
                    // already closed ResultSet
                }
                for (Iterator i = a.iterator(); i.hasNext();) {
                    String sql = (String)i.next();
                    stat.execute(sql);
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

    private void skipResultSet(Statement stat) throws SQLException {
        ResultSet rs = stat.getResultSet();
        if (rs != null) {
            for (; rs.next();) ;
            rs.close();
        }
        stat.getMoreResults();
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
        return "SELECT db_name()";
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
        s.append(" IDENTITY");
    }

    /**
     * Get extra SQL to be appended to the insert statement for retrieving
     * the value of an autoinc column after insert. Return null if none
     * is required or a separate query is run.
     *
     * @see #getAutoIncColumnValue(JdbcTable, Connection, Statement)
     */
    public String getAutoIncPostInsertSQLSuffix(JdbcTable classTable) {
        return identityFetch;
    }

    /**
     * Retrieve the value of the autoinc or serial column for a row just
     * inserted using stat on con.
     *
     * @see #getAutoIncPostInsertSQLSuffix(JdbcTable)
     */
    public Object getAutoIncColumnValue(JdbcTable classTable,
            Connection con, Statement stat) throws SQLException {
        stat.getMoreResults(); // skip the count
        ResultSet rs = stat.getResultSet();
        try {
            rs.next();
            return classTable.pk[0].get(rs, 1);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Enable or disable identity insert for the given table if this is
     * required to insert a value into an identity column.
     */
    public void enableIdentityInsert(Connection con, String table, boolean on)
            throws SQLException {
        Statement stat = con.createStatement();
        try {
            stat.execute(
                    "SET identity_insert " + table + (on ? " ON" : " OFF"));
        } finally {
            try {
                stat.close();
            } catch (SQLException e) {
                // ignore
            }
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

    /**
     * Get the JdbcTables from the database for the given database con.
     *
     * @param con
     * @return HashMap of tablename.toLowerCase() as key and JdbcTable as value
     * @throws SQLException on DB errors
     */
    public HashMap getDBSchema(Connection con, ControlParams params)
            throws SQLException {
        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables
        // now we do columns
        String tableName = null;
        ResultSet rs = null;
        HashMap tableNameMap = new HashMap();
        try {
            rs = con.getMetaData().getTables(null, getSchema(con), null, null);
            for (; rs.next();) {
                if (rs.getString(4).trim().equals("TABLE")) {
                    String name = rs.getString(3).trim();
                    tableNameMap.put(name, name);
                }
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException x) {
                    // ignore
                }
            }
        }
        String dbName = getDBName(con);
        //username
        String columnSql = "sp_columns null, null, '" + dbName + "', null, @ODBCVer = 3";

        Statement statCol = con.createStatement();
        ResultSet rsColumn = statCol.executeQuery(columnSql);

        ArrayList columns = null;

        while (rsColumn.next()) {

            String temptableName = rsColumn.getString(3).trim();

            if (!tableNameMap.containsKey(temptableName)) {
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
                JdbcTable jdbcTable0 = (JdbcTable)jdbcTableMap.get(tableName);
                jdbcTable0.cols = jdbcColumns;

                tableName = temptableName;
                columns.clear();
                JdbcTable jdbcTable1 = new JdbcTable();
                jdbcTable1.name = tableName;
                jdbcTableMap.put(tableName, jdbcTable1);
            }

            JdbcColumn col = new JdbcColumn();

            col.name = rsColumn.getString(4);
            String sqlType = rsColumn.getString(6).trim();
            if (sqlType.indexOf(' ') != -1){
                col.sqlType = sqlType.substring(0, sqlType.indexOf(' '));
                if (sqlType.endsWith("identity")){
                    col.autoinc = true;
                }
            } else {
                col.sqlType = sqlType;
            }

            int jdbcType = rsColumn.getInt(5);
            int lenght = rsColumn.getInt(8);
            col.jdbcType = jdbcType; // ms fucks up numeric types by 2
            if (java.sql.Types.NUMERIC == jdbcType) {
                col.length = lenght - 2;
            } else if (jdbcType == -8) { // NCHAR 0721068159
                col.jdbcType = 1;
                col.length = lenght / 2;
            } else if (jdbcType == -9) { // NVARCHAR
                col.jdbcType = 12;
                col.length = lenght / 2;
            } else if (jdbcType == -10) { // NTEXT
                col.jdbcType = -1;
                col.length = 0;
            } else if (col.sqlType.equalsIgnoreCase("text")) {
                col.length = 0;
            } else {
                col.length = lenght;
            }
            col.scale = rsColumn.getInt(9);
            col.nulls = rsColumn.getBoolean(11);

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
        if (columns != null) {
            JdbcColumn[] jdbcColumns = new JdbcColumn[columns.size()];
            if (jdbcColumns != null) {
                columns.toArray(jdbcColumns);
                JdbcTable colJdbcTable = (JdbcTable)jdbcTableMap.get(tableName);
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

                String pkSql =
                        "select TABLE_NAME = o.name,\n" +
                        "       COLUMN_NAME = c.name, \n" +
                        "       KEY_SEQ =\n" +
                        "           case\n" +
                        "               when c.name = index_col(o.name, i.indid,  1) then convert (smallint,1)\n" +
                        "               when c.name = index_col(o.name, i.indid,  2) then convert (smallint,2) \n" +
                        "               when c.name = index_col(o.name, i.indid,  3) then convert (smallint,3) \n" +
                        "               when c.name = index_col(o.name, i.indid,  4) then convert (smallint,4)\n" +
                        "               when c.name = index_col(o.name, i.indid,  5) then convert (smallint,5) \n" +
                        "               when c.name = index_col(o.name, i.indid,  6) then convert (smallint,6)\n" +
                        "               when c.name = index_col(o.name, i.indid,  7) then convert (smallint,7)\n" +
                        "               when c.name = index_col(o.name, i.indid,  8) then convert (smallint,8) \n" +
                        "               when c.name = index_col(o.name, i.indid,  9) then convert (smallint,9) \n" +
                        "               when c.name = index_col(o.name, i.indid, 10) then convert (smallint,10)\n" +
                        "               when c.name = index_col(o.name, i.indid, 11) then convert (smallint,11)\n" +
                        "               when c.name = index_col(o.name, i.indid, 12) then convert (smallint,12) \n" +
                        "               when c.name = index_col(o.name, i.indid, 13) then convert (smallint,13)\n" +
                        "               when c.name = index_col(o.name, i.indid, 14) then convert (smallint,14) \n" +
                        "               when c.name = index_col(o.name, i.indid, 15) then convert (smallint,15) \n" +
                        "               when c.name = index_col(o.name, i.indid, 16) then convert (smallint,16) \n" +
                        "           end, \n" +
                        "       PK_NAME = convert(sysname,i.name) \n" +
                        "  from sysindexes i, syscolumns c, sysobjects o\n" +
                        " where o.id = c.id \n" +
                        "   and o.id = i.id \n" +
                        "  -- and i.status2 & 2 = 2\n" +
                        "   and i.status & 2048 = 2048\n" +
                        "   and (c.name = index_col (o.name, i.indid,  1) or \n" +
                        "           c.name = index_col (o.name, i.indid,  2) or \n" +
                        "           c.name = index_col (o.name, i.indid,  3) or \n" +
                        "           c.name = index_col (o.name, i.indid,  4) or \n" +
                        "           c.name = index_col (o.name, i.indid,  5) or \n" +
                        "           c.name = index_col (o.name, i.indid,  6) or \n" +
                        "           c.name = index_col (o.name, i.indid,  7) or \n" +
                        "           c.name = index_col (o.name, i.indid,  8) or \n" +
                        "           c.name = index_col (o.name, i.indid,  9) or \n" +
                        "           c.name = index_col (o.name, i.indid, 10) or \n" +
                        "           c.name = index_col (o.name, i.indid, 11) or \n" +
                        "           c.name = index_col (o.name, i.indid, 12) or \n" +
                        "           c.name = index_col (o.name, i.indid, 13) or \n" +
                        "           c.name = index_col (o.name, i.indid, 14) or \n" +
                        "           c.name = index_col (o.name, i.indid, 15) or \n" +
                        "           c.name = index_col (o.name, i.indid, 16) \n" +
                        "       )  \n" +
                        " ORDER BY 1, 3";

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
                if (tableName != null) {
                    JdbcColumn[] pkColumns = new JdbcColumn[pkCount];
                    int indexOfPKCount = 0;
                    JdbcTable pkJdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
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
                }
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
                        "select 'TABLE_NAME' = o.name, \n" +
                        "       'COLUMN_NAME' = INDEX_COL(o.name,indid,colid),\n" +
                        "       'INDEX_NAME' = x.name,\n" +
                        "       'NON_UNIQUE' =\n" +
                        "         case\n" +
                        "           when x.status & 2 != 2 then convert (smallint,1)\n" +
                        "           else convert (smallint,0)\n" +
                        "         end,\n" +
                        "       'TYPE' = \n" +
                        "         case\n" +
                        "           when x.indid > 1 then convert (smallint,3)\n" +
                        "           else convert (smallint,1)\n" +
                        "         end,\n" +
                        "       'ORDINAL_POSITION' = colid \n" +
                        "  from sysindexes x, syscolumns c, sysobjects o \n" +
                        " where x.id = object_id(o.name) \n" +
                        "   and x.id = o.id \n" +
                        "   and o.type = 'U' \n" +
                        "   and x.indid != 1\n" +
                        "   AND (x.status & 32) = 0\n" +
                        "   and x.id = c.id\n" +
                        "   and c.colid < keycnt+(x.status&18)/18\n" +
                        "   and INDEX_COL(o.name,indid,colid) <> ''\n" +
                        " ORDER BY TABLE_NAME, INDEX_NAME,ORDINAL_POSITION";

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
                if (tableName != null) {
                    JdbcTable indexJdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
                    if (indexJdbcTable != null) {
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
                String fkSql1 =
                        " create table #fkeysall(  \n" +
                        "    rkeyid int NOT NULL,  \n" +
                        "    rkey1 int NOT NULL,  \n" +
                        "    rkey2 int NOT NULL,  \n" +
                        "    rkey3 int NOT NULL,  \n" +
                        "    rkey4 int NOT NULL,  \n" +
                        "    rkey5 int NOT NULL,  \n" +
                        "    rkey6 int NOT NULL,  \n" +
                        "    rkey7 int NOT NULL,  \n" +
                        "    rkey8 int NOT NULL,  \n" +
                        "    rkey9 int NOT NULL,  \n" +
                        "    rkey10 int NOT NULL,  \n" +
                        "    rkey11 int NOT NULL,  \n" +
                        "    rkey12 int NOT NULL,  \n" +
                        "    rkey13 int NOT NULL,  \n" +
                        "    rkey14 int NOT NULL,  \n" +
                        "    rkey15 int NOT NULL,  \n" +
                        "    rkey16 int NOT NULL,  \n" +
                        "    fkeyid int NOT NULL,  \n" +
                        "    fkey1 int NOT NULL,  \n" +
                        "    fkey2 int NOT NULL,  \n" +
                        "    fkey3 int NOT NULL,  \n" +
                        "    fkey4 int NOT NULL,  \n" +
                        "    fkey5 int NOT NULL,  \n" +
                        "    fkey6 int NOT NULL,  \n" +
                        "    fkey7 int NOT NULL,  \n" +
                        "    fkey8 int NOT NULL,  \n" +
                        "    fkey9 int NOT NULL,  \n" +
                        "    fkey10 int NOT NULL,  \n" +
                        "    fkey11 int NOT NULL,  \n" +
                        "    fkey12 int NOT NULL,  \n" +
                        "    fkey13 int NOT NULL,  \n" +
                        "    fkey14 int NOT NULL,  \n" +
                        "    fkey15 int NOT NULL,  \n" +
                        "    fkey16 int NOT NULL,  \n" +
                        "    constid int NOT NULL,  \n" +
                        "    name sysname collate database_default NOT NULL)  ";
                Statement statFK1 = con.createStatement();
                statFK1.execute(fkSql1);
                statFK1.close();
                String fkSql2 =
                        "create table #fkeys(  \n" +
                        "   pktable_id  int NOT NULL,  \n" +
                        "   pkcolid     int NOT NULL,  \n" +
                        "   fktable_id  int NOT NULL,  \n" +
                        "   fkcolid     int NOT NULL,  \n" +
                        "   KEY_SEQ     smallint NOT NULL,  \n" +
                        "   fk_id       int NOT NULL,  \n" +
                        "   PK_NAME     sysname collate database_default NOT NULL)  ";
                Statement statFK2 = con.createStatement();
                statFK2.execute(fkSql2);
                statFK2.close();
                String fkSql3 =
                        " create table #fkeysout(  \n" +
                        "   PKTABLE_QUALIFIER sysname collate database_default NULL,  \n" +
                        "   PKTABLE_OWNER sysname collate database_default NULL,  \n" +
                        "   PKTABLE_NAME sysname collate database_default NOT NULL,  \n" +
                        "   PKCOLUMN_NAME sysname collate database_default NOT NULL,  \n" +
                        "   FKTABLE_QUALIFIER sysname collate database_default NULL,  \n" +
                        "   FKTABLE_OWNER sysname collate database_default NULL,\n" +
                        "   FKTABLE_NAME sysname collate database_default NOT NULL,\n" +
                        "   FKCOLUMN_NAME sysname collate database_default NOT NULL,\n" +
                        "   KEY_SEQ smallint NOT NULL,\n" +
                        "   UPDATE_RULE smallint NULL,\n" +
                        "   DELETE_RULE smallint NULL,\n" +
                        "   FK_NAME sysname collate database_default NULL,\n" +
                        "   PK_NAME sysname collate database_default NULL,\n" +
                        "   DEFERRABILITY smallint null)";
                Statement statFK3 = con.createStatement();
                statFK3.execute(fkSql3);
                statFK3.close();
                String fkSql4 =
                        "insert into #fkeysall\n" +
                        "       select\n" +
                        "       r.rkeyid,\n" +
                        "       r.rkey1, r.rkey2, r.rkey3, r.rkey4,\n" +
                        "       r.rkey5, r.rkey6, r.rkey7, r.rkey8,\n" +
                        "       r.rkey9, r.rkey10, r.rkey11, r.rkey12,\n" +
                        "       r.rkey13, r.rkey14, r.rkey15, r.rkey16,\n" +
                        "       r.fkeyid,\n" +
                        "       r.fkey1, r.fkey2, r.fkey3, r.fkey4,\n" +
                        "       r.fkey5, r.fkey6, r.fkey7, r.fkey8,\n" +
                        "       r.fkey9, r.fkey10, r.fkey11, r.fkey12,\n" +
                        "       r.fkey13, r.fkey14, r.fkey15, r.fkey16,\n" +
                        "       r.constid,\n" +
                        "       i.name\n" +
                        "  from sysreferences r, sysobjects o, sysindexes i\n" +
                        " where r.constid = o.id\n" +
                        "   AND o.xtype = 'F'\n" +
                        "   AND r.rkeyindid = i.indid\n" +
                        "   AND r.rkeyid = i.id\n";
                Statement statFK4 = con.createStatement();
                statFK4.execute(fkSql4);
                statFK4.close();
                String fkSql5 =
                        "insert into #fkeys\n" +
                        "   select rkeyid, rkey1, fkeyid, fkey1, 1, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey2, fkeyid, fkey2, 2, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey3, fkeyid, fkey3, 3, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey4, fkeyid, fkey4, 4, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey5, fkeyid, fkey5, 5, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey6, fkeyid, fkey6, 6, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey7, fkeyid, fkey7, 7, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey8, fkeyid, fkey8, 8, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey9, fkeyid, fkey9, 9, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey10, fkeyid, fkey10, 10, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey11, fkeyid, fkey11, 11, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey12, fkeyid, fkey12, 12, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey13, fkeyid, fkey13, 13, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey14, fkeyid, fkey14, 14, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey15, fkeyid, fkey15, 15, constid, name\n" +
                        "   from #fkeysall\n" +
                        "  union all\n" +
                        "   select rkeyid, rkey16, fkeyid, fkey16, 16, constid, name\n" +
                        "   from #fkeysall";
                Statement statFK5 = con.createStatement();
                statFK5.execute(fkSql5);
                statFK5.close();
                String fkSql6 =
                        "insert into #fkeysout\n" +
                        "  select   PKTABLE_QUALIFIER = convert(sysname,db_name()),\n" +
                        "           PKTABLE_OWNER = convert(sysname,USER_NAME(o1.uid)),\n" +
                        "           PKTABLE_NAME = convert(sysname,o1.name),\n" +
                        "           PKCOLUMN_NAME = convert(sysname,c1.name),\n" +
                        "           FKTABLE_QUALIFIER = convert(sysname,db_name()),\n" +
                        "           FKTABLE_OWNER = convert(sysname,USER_NAME(o2.uid)),\n" +
                        "           FKTABLE_NAME = convert(sysname,o2.name),\n" +
                        "           FKCOLUMN_NAME = convert(sysname,c2.name),\n" +
                        "           KEY_SEQ,\n" +
                        "           UPDATE_RULE = CASE WHEN (ObjectProperty(fk_id, 'CnstIsUpdateCascade')=1)\n" +
                        "THEN   convert(smallint,0) ELSE convert(smallint,1) END,\n" +
                        "       DELETE_RULE = CASE WHEN (ObjectProperty(fk_id, 'CnstIsDeleteCascade')=1)\n" +
                        "THEN   convert(smallint,0) ELSE convert(smallint,1) END,\n" +
                        "       FK_NAME = convert(sysname,OBJECT_NAME(fk_id)),\n" +
                        "       PK_NAME,\n" +
                        "       DEFERRABILITY = 7 /* SQL_NOT_DEFERRABLE */\n" +
                        "  from #fkeys f,\n" +
                        "       sysobjects o1, sysobjects o2,\n" +
                        "       syscolumns c1, syscolumns c2\n" +
                        " where o1.id = f.pktable_id\n" +
                        "   AND o2.id = f.fktable_id\n" +
                        "   AND c1.id = f.pktable_id\n" +
                        "   AND c2.id = f.fktable_id\n" +
                        "   AND c1.colid = f.pkcolid\n" +
                        "   AND c2.colid = f.fkcolid";
                Statement statFK6 = con.createStatement();
                statFK6.execute(fkSql6);
                statFK6.close();

                String fkSql =
                        "select PKTABLE_NAME, PKCOLUMN_NAME,\n" +
                        "       FKTABLE_NAME, FKCOLUMN_NAME,\n" +
                        "       KEY_SEQ, FK_NAME, PK_NAME\n" +
                        "  from #fkeysout\n" +
                        " ORDER BY 3,6,5";
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
                        JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                                tableName);
                        JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                        constraints.toArray(jdbcConstraints);
                        jdbcTable.constraints = jdbcConstraints;

                        tableName = temptableName;
                        constraintNameMap.clear();
                        constraints.clear();
                    }

                    String fkName = rsFKs.getString(6);
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
                if (tableName != null && constraints != null) {
                    JdbcTable constraintsjdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
                    JdbcConstraint[] jdbcConstraints = new JdbcConstraint[constraints.size()];
                    if (jdbcConstraints != null) {
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

                Statement statCleanUp = con.createStatement();
                statCleanUp.execute("DROP TABLE #fkeysall, #fkeys, #fkeysout");
                if (statCleanUp != null) {
                    try {
                        statCleanUp.close();
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

    protected String getDBName(Connection con) throws SQLException {
        String catalog = null;
        Statement stat = null;
        ResultSet rs = null;

        try {
            stat = con.createStatement();
            rs = stat.executeQuery("select db_name()");
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
    }

//    public boolean checkLenght(JdbcColumn ourCol, JdbcColumn dbCol) {
//        if (dbCol.jdbcType == Types.NUMERIC){     // ms messes up NUMERIC by 2
//            if (ourCol.length != dbCol.length) {
//                if (ourCol.length != 0) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        return true;
//    }



    protected String getDefaultForType(JdbcColumn ourCol) {
        switch (ourCol.jdbcType) {
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "getdate()";
            
            default:
                return super.getDefaultForType(ourCol);
        }
    }

    public String getRunCommand() {
        return "\ngo\n";
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
                s.append(" ALTER COLUMN ");
                s.append(c.name);
                s.append(' ');
                appendColumnType(c, s);
                if (c.autoinc) {
                    appendCreateColumnAutoInc(t, c, s);
                }
                appendCreateColumnNulls(t, c, s);

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

        if (comments && isCommentSupported() && ourCol.comment != null) {
            s.append(comment("modify column for field " + ourCol.comment));
        }
        if (comments && isCommentSupported() && ourCol.comment == null) {
            s.append(comment("modify column " + ourCol.name));
        }
//        boolean weHavePkStuff = false;
//        if (tableDiff.getPkDiffs().isEmpty()){
//            if (t.isInPrimaryKey(ourCol.name)){
//                weHavePkStuff = true;
//            }
//        }

        s.append("\n");

        if (mustRecreate(diff)) {
            String tempcolumn = getTempColumnName(t);
            s.append("sp_rename '");
            s.append(t.name);
            s.append('.');
            s.append(ourCol.name);
            s.append("', ");
            s.append(tempcolumn);

            s.append(getRunCommand());

            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" ADD ");
            s.append(ourCol.name);
            s.append(' ');
            appendColumnType(ourCol, s);
            s.append(" NULL");// we always add it as null

            s.append(getRunCommand());

            s.append("UPDATE ");
            s.append(t.name);
            s.append("\n");//new line
            s.append("   SET ");
            s.append(ourCol.name);
            s.append(" = ");

            String pad = pad(10 + ourCol.name.length());

            if (diff.isNullDiff() && !ourCol.nulls) {
                s.append("CASE ");
                s.append("\n");//new line
                s.append(pad);
                s.append("  WHEN ");
                s.append(tempcolumn);
                s.append(" IS NOT NULL");
                s.append("\n");//new line
                s.append(pad);
                s.append("  THEN CONVERT(");
                appendColumnType(ourCol, s);
                s.append(", ");
                s.append(tempcolumn);
                s.append(")");
                s.append("\n");//new line
                s.append(pad);
                s.append("  ELSE ");
                s.append(getDefaultForType(ourCol));
                s.append(
                        comment(
                                "Add your own default value here, for when " + ourCol.name + " is null."));
                s.append("\n");//new line
                s.append(pad);
                s.append("END");

            } else {
                s.append("CONVERT(");
                appendColumnType(ourCol, s);
                s.append(", ");
                s.append(tempcolumn);
                s.append(")");
            }

            s.append(getRunCommand());

            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" DROP COLUMN ");
            s.append(tempcolumn);

            if (!ourCol.nulls) {
                s.append(getRunCommand());
                s.append("ALTER TABLE ");
                s.append(t.name);
                s.append(" ALTER COLUMN ");
                s.append(ourCol.name);
                s.append(' ');
                appendColumnType(ourCol, s);
                appendCreateColumnNulls(t, ourCol, s);
            }

        } else {

            if (diff.isNullDiff()) {
                if (!ourCol.nulls) {
                    s.append("UPDATE ");
                    s.append(t.name);
                    s.append("\n");
                    s.append("   SET ");
                    s.append(ourCol.name);
                    s.append(" = ");
                    s.append(getDefaultForType(ourCol));
                    s.append(' ');
                    s.append(
                            comment(
                                    "Add your own default value here, for when " + ourCol.name + " is null."));
                    s.append("\n");
                    s.append(" WHERE ");
                    s.append(ourCol.name);
                    s.append(" IS NULL");

                    s.append(getRunCommand());

                }

            }

            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" ALTER COLUMN ");
            s.append(ourCol.name);
            s.append(' ');
            appendColumnType(ourCol, s);
            appendCreateColumnNulls(t, ourCol, s);
        }

    }

    /**
     * Must this column be recreated?
     *
     * @param diff
     * @return
     */
    private boolean mustRecreate(ColumnDiff diff) {
        JdbcColumn ourCol = diff.getOurCol();
        JdbcColumn dbCol = diff.getDbCol();
        boolean recreateColumn = false;
        if (diff.isLenghtDiff()) {
            if (dbCol.length > ourCol.length) {
                recreateColumn = true;
            }
        }
        if (diff.isScaleDiff()) {
            if (dbCol.scale > ourCol.scale) {
                recreateColumn = true;
            }
        }
        return recreateColumn;
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
        // DROP INDEX authors.au_id_ind todo check what to do with uniqe indexes
//        if (idx.unique){
//            idx.
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

    /**
     * Drop a Sequence column to implement a Set
     */
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {
        String tempTableName = getTempTableName(t, 30);

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
        s.append("sp_rename ");
        s.append(tempTableName);
        s.append(", ");
        s.append(t.name);

    }

    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s,
            boolean comments) {

        String mainTempTableName = getTempTableName(t, 30);
        String minTempTableName = getTempTableName(t, 30);
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
        s.append(" NUMERIC(6) IDENTITY,");
        for (int i = 0; i < nc; i++) {
            s.append("\n    ");
            appendCreateColumn(t, cols[i], s, comments);
        }
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

        s.append(comment("update the temp table's sequence column."));
        s.append("\n");
        s.append("UPDATE ");
        s.append(mainTempTableName);
        s.append("\n   SET ");
        s.append(sequenceColumn.name);
        s.append(" = (");
        s.append(identityColumnName);
        s.append(" - ");
        s.append("b.");
        s.append("min_id");
        s.append(")\n");
        s.append("  FROM ");
        s.append(mainTempTableName);
        s.append(" a,\n");
        s.append("       ");
        s.append(minTempTableName);
        s.append(" b\n");
        s.append(" WHERE a.");
        s.append(indexColumn.name);
        s.append(" = b.");
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

	public RuntimeException mapException(Throwable cause, String message, 
                                         boolean isFatal)
	{
		if (isLockTimeout(cause)) {
			if (com.versant.core.common.Debug.DEBUG) 
				cause.printStackTrace(com.versant.core.common.Debug.OUT);
			Throwable[] nested = {cause};
			return BindingSupportImpl.getInstance().lockNotGranted
			    (message==null?com.versant.core.jdbc.JdbcUtils.toString(cause):
                 message, nested, null); 
		} else if (isDuplicateKey(cause)) {
			if (com.versant.core.common.Debug.DEBUG) 
				cause.printStackTrace(com.versant.core.common.Debug.OUT);
			return BindingSupportImpl.getInstance().duplicateKey
				(message==null?com.versant.core.jdbc.JdbcUtils.toString(cause):
				 message, cause, null);
		}

		return super.mapException(cause, message, isFatal);
	}

	public boolean isHandleLockTimeout() {
		return true;
	}

	public boolean isHandleDuplicateKey() {
		return true;
	}

    public boolean isLockTimeout(Throwable cause) {
		if (cause instanceof SQLException) {
			
		    SQLException sqlexc = (SQLException)cause;
            if (sqlexc.getErrorCode() == 1222) {
				return true;
			}
		}
		return false;
    }

    public boolean isDuplicateKey(Throwable cause) {
		if (cause instanceof SQLException) {
			
		    SQLException sqlexc = (SQLException)cause;
            if (sqlexc.getErrorCode() == 2627 || 
				sqlexc.getErrorCode() == 2601) {
				return true;
			}
		}
		return false;
    }
}
