
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
import com.versant.core.jdbc.sql.conv.BooleanConverter;
import com.versant.core.jdbc.sql.conv.ClobStringConverter;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.BinaryOpExp;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.util.CharBuf;
import com.versant.core.metadata.ClassMetaData;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.io.PrintWriter;

import com.versant.core.common.BindingSupportImpl;

/**
 * A driver for Sybase using jconnect JDBC driver.
 */
public class SybaseSqlDriver extends SqlDriver {

    private ClobStringConverter.Factory clobStringConverterFactory
            = new ClobStringConverter.Factory();

    private boolean batchingSupported = false;
    private boolean scrollableResultSetSupported = false;
    private boolean optimizeExistsUnderOrToOuterJoin = false;
    private boolean ansiJoinSyntax = false;
    private boolean supportDirectColumnChange = false;
    private HashMap sysMap = null;

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "sybase";
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
                return new JdbcTypeMapping("TINYINT",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.BIGINT:
                return new JdbcTypeMapping("NUMERIC",
                    19, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("DATETIME",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
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
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
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

        ((JdbcJavaTypeMapping)ans.get(Byte.TYPE)).setJdbcType(Types.SMALLINT);
        ((JdbcJavaTypeMapping)ans.get(Byte.class)).setJdbcType(Types.SMALLINT);

        DateTimestampConverter.Factory dtcf = new DateTimestampConverter.Factory();
        ((JdbcJavaTypeMapping)ans.get(Date.class)).setConverterFactory(dtcf);

        return ans;
    }

    /**
     * Perform any driver specific customization. This can be used to control
     * funcionality depending on the version of JDBC driver in use etc.
     */
    public void customizeForDriver(Driver jdbcDriver) {
        String n = jdbcDriver.getClass().getName();
        if (n.startsWith("com.sybase.jdbc.")) {
            // very old JDBC driver
            batchingSupported = false;
            scrollableResultSetSupported = false;
        } else {
            // the newer ones are com.sybase.jdbc2 or jdbc3 etc
            batchingSupported = true;
            scrollableResultSetSupported = true;            
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
        // get Sybase major and minor version numbers
        int major = 0;
        //int minor = 0;
        try {
            String ver = getSybaseVersion(con);
            int i = ver.indexOf('/') + 1;
            String no = ver.substring(i, ver.indexOf('/', i));
            i = no.indexOf('.');
            major = Integer.parseInt(no.substring(0, i));
            //int j = no.indexOf('.', i + 1);
            //if (j >= 0) minor = Integer.parseInt(no.substring(i + 1, j));
            //else minor = Integer.parseInt(no.substring(i + 1));
        } catch (NumberFormatException e) {
            // ignore - must be old Sybase
        }
        // optimize settings to match
        if (major >= 12) {
            ansiJoinSyntax = true;
            optimizeExistsUnderOrToOuterJoin = true;
            supportDirectColumnChange = true;
        }
    }

    /**
     * Get the version of Sybase on con.
     */
    private String getSybaseVersion(Connection con) throws SQLException {
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
     * Does the JDBC driver support statement batching?
     */
    public boolean isInsertBatchingSupported() {
        return batchingSupported;
    }

    /**
     * Does the JDBC driver support statement batching for updates?
     */
    public boolean isUpdateBatchingSupported() {
        return batchingSupported;
    }

    /**
     * Does the JDBC driver support scrollable result sets?
     */
    public boolean isScrollableResultSetSupported() {
        return scrollableResultSetSupported;
    }

    /**
     * Is it ok to convert simple 'exists (select ...)' clauses under an
     * 'or' into outer joins?
     */
    public boolean isOptimizeExistsUnderOrToOuterJoin() {
        return optimizeExistsUnderOrToOuterJoin;
    }

    /**
     * Does this driver use the ANSI join syntax (i.e. the join clauses appear
     * in the from list e.g. postgres)?
     */
    public boolean isAnsiJoinSyntax() {
        return ansiJoinSyntax;
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
     * Append the allow nulls part of the definition for a column in a
     * create table statement.
     */
    protected void appendCreateColumnNulls(JdbcTable t, JdbcColumn c,
            CharBuf s) {
        if (c.nulls) s.append(" NULL");
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
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        ResultSet rs = null;
        try {
            stat = con.createStatement();
            try {
                rs = stat.executeQuery("sp_helpconstraint " + table);
                ArrayList a = new ArrayList();
                for (; rs.next(); ) {
                    String cname = rs.getString(1);
                    String info = rs.getString(2);
                    if (info.indexOf("FOREIGN KEY") >= 0) {
                        String tn = info.substring(0, info.indexOf(' '));
                        a.add("ALTER TABLE " + tn + " DROP CONSTRAINT " + cname);
                    }
                }
                rs.close();
                for (Iterator i = a.iterator(); i.hasNext(); ) {
                    String sql = (String)i.next();
                    stat.execute(sql);
                }
            } catch (SQLException e) {
                // ignore
                // when there are no constraints, sybase 12 throws an exception ?
            }
            String sql = "DROP TABLE " + table;
            System.out.println(sql);
            stat.execute(sql);
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

    public void updateClassForPostInsertKeyGen(ClassMetaData cmd, JdbcMappingResolver mappingResolver) {
        JdbcTypeMapping tm = mappingResolver.getTypeMapping(Types.NUMERIC);
        JdbcTable table = ((JdbcClass)cmd.storeClass).table;
        table.pk[0].jdbcType = tm.getJdbcType();
        table.pk[0].sqlType = tm.getSqlType();
        updateSubClassPkCols(cmd, tm);
    }

    private void updateSubClassPkCols(ClassMetaData cmd, JdbcTypeMapping tm) {
        ClassMetaData[] subs = cmd.pcSubclasses;
        if (subs != null) {
            for (int j = 0; j < subs.length; j++) {
                ClassMetaData sub = subs[j];
                JdbcClass jdbcClass = (JdbcClass)sub.storeClass;
                if (jdbcClass.inheritance == JdbcClass.INHERITANCE_VERTICAL) {
                    jdbcClass.table.pk[0].jdbcType = tm.getJdbcType();
                    jdbcClass.table.pk[0].sqlType = tm.getSqlType();
                }
                updateSubClassPkCols(sub, tm);
            }
        }
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
     * @param exp This is the expression that joins the tables
     * @param outer If true then this is an outer join
     */
    public void appendSqlFromJoin(JdbcTable table, String alias, SqlExp exp,
            boolean outer, CharBuf s) {
        if (ansiJoinSyntax) {
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
     * @see BinaryOpExp
     */
    public String getSqlBinaryOp(int op) {
        switch (op) {
            case BinaryOpExp.CONCAT:    return "+";
        }
        return super.getSqlBinaryOp(op);
    }

    /**
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return "SELECT db_name()";
    }

    /**
     * Get con ready for a getQueryPlan call. Example: On Sybase this will
     * do a 'set showplan 1' and 'set noexec 1'. Also make whatever changes
     * are necessary to sql to prepare it for a getQueryPlan call. Example:
     * On Oracle this will prepend 'explain '. The cleanupForGetQueryPlan
     * method must be called in a finally block if this method is called.
     * @see #cleanupForGetQueryPlan
     * @see #getQueryPlan
     */
    public String prepareForGetQueryPlan(Connection con, String sql) {
        try{
            Statement statement = con.createStatement();
            statement.execute("SET showplan ON");
            statement.execute("SET noexec ON");
        } catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return sql;
    }

    /**
     * Get the query plan for ps and cleanup anything done in
     * prepareForGetQueryPlan. Return null if this is not supported.
     * @see #prepareForGetQueryPlan
     * @see #cleanupForGetQueryPlan
     */
    public String getQueryPlan(Connection con, PreparedStatement ps) {
        StringBuffer buff = new StringBuffer();
        try{
            ps.execute();
            SQLWarning warning = ps.getWarnings();
            while (warning != null) {
                buff.append(warning.getLocalizedMessage());
                warning = warning.getNextWarning();
            }

        } catch (Exception sqle){
            sqle.printStackTrace();
        }
        return buff.toString();
    }


    /**
     * Cleanup anything done in prepareForGetQueryPlan. Example: On Sybase this
     * will do a 'set showplan 0' and 'set noexec 0'.
     * @see #prepareForGetQueryPlan
     * @see #getQueryPlan
     */
    public void cleanupForGetQueryPlan(Connection con) {
        try{
            Statement statement = con.createStatement();
            statement.execute("SET noexec OFF");
            statement.execute("SET showplan OFF");

        } catch (SQLException sqle){
            sqle.printStackTrace();
        }
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
            s.append(" NUMERIC");
            if (c.length != 0) {
                s.append('(');
                s.append(c.length);
                s.append(')');
            }
            s.append(" IDENTITY,");
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
     * Retrieve the value of the autoinc or serial column for a row just
     * inserted using stat on con.
     */
    public Object getAutoIncColumnValue(JdbcTable classTable,
            Connection con, Statement stat) throws SQLException {
        Statement es = null;
        try {
            es = con.createStatement();
            ResultSet rs = es.executeQuery("SELECT @@identity");
            if (!rs.next()) {
                throw BindingSupportImpl.getInstance().datastore("Unable to get identity column " +
                    "value - 'select @@identity' returned no row");
            }
            return classTable.pk[0].get(rs, 1);
        } finally {
            if (es != null) {
                try {
                    es.close();
                } catch (SQLException e) {
                    // ignore
                }
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
            stat.execute("SET identity_insert " + table + (on ? " ON" : " OFF"));
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


    protected String getCatalog(Connection con) throws SQLException {
        String catalog = null;
        Statement stat = null;
        ResultSet rs = null;

        try {
            stat = con.createStatement();
            rs = stat.executeQuery("SELECT db_name()");
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


    protected boolean isValidSchemaTable(String tableName) {
        if (sysMap == null){
            sysMap = new HashMap();
            String[] sysNames = new String[]{"sysalternates",
                                             "sysattributes",
                                             "syscolumns",
                                             "syscomments",
                                             "sysconstraints",
                                             "sysdepends",
                                             "sysindexes",
                                             "syskeys",
                                             "syslogs",
                                             "sysobjects",
                                             "syspartitions",
                                             "sysprocedures",
                                             "sysprotects",
                                             "sysreferences",
                                             "sysroles",
                                             "syssegments",
                                             "sysstatistics",
                                             "systabstats",
                                             "systhresholds",
                                             "systypes",
                                             "sysusermessages",
                                             "sysusers",
                                             "sysobjects",
                                             "sysindexes",
                                             "syscolumns",
                                             "systypes",
                                             "sysprocedures",
                                             "syscomments",
                                             "syssegments",
                                             "syslogs",
                                             "sysprotects",
                                             "sysusers",
                                             "sysalternates",
                                             "sysdepends",
                                             "syskeys",
                                             "sysgams",
                                             "sysusermessages",
                                             "sysreferences",
                                             "sysconstraints",
                                             "systhresholds",
                                             "sysroles",
                                             "sysattributes",
                                             "syspartitions",
                                             "systabstats",
                                             "sysstatistics",
                                             "sysxtypes",
                                             "sysjars",
                                             "sysqueryplans",
                                             "sysdatabases",
                                             "sysusages",
                                             "sysprocesses",
                                             "syslogins",
                                             "syslocks",
                                             "sysdevices",
                                             "sysmessages",
                                             "sysconfigures",
                                             "syscurconfigs",
                                             "syssecmechs",
                                             "sysservers",
                                             "sysremotelogins",
                                             "sysmonitors",
                                             "sysengines",
                                             "syslanguages",
                                             "syscharsets",
                                             "systestlog",
                                             "syslisteners",
                                             "syssrvroles",
                                             "sysloginroles",
                                             "syslogshold",
                                             "systimeranges",
                                             "sysresourcelimits",
                                             "systransactions",
                                             "syssessions",
                                             "syscertificates",
                                             "spt_values",
                                             "spt_monitor",
                                             "spt_limit_types",
                                             "syblicenseslog",
                                             "spt_ijdbc_table_types",
                                             "spt_ijdbc_mda",
                                             "spt_ijdbc_conversion",
                                             "ijdbc_function_escapes",
                                             "sp_procxmode",
                                             "sp_validlang",
                                             "sp_getmessage",
                                             "sp_aux_getsize",
                                             "sp_configure",
                                             "sp_dboption",
                                             "sp_dropdevice",
                                             "sp_dbupgrade",
                                             "sp_loaddbupgrade",
                                             "sp_prtsybsysmsgs",
                                             "spt_jdbc_table_types",
                                             "spt_mda",
                                             "spt_jtext",
                                             "spt_jdbc_conversion",
                                             "jdbc_function_escapes"};
            for (int i = 0; i < sysNames.length; i++) {
                String sysName = sysNames[i];
                sysMap.put(sysName,null);
            }
        }

        if (sysMap.containsKey(tableName)){
            return false;
        }
        return true;
    }

    /**
     * Get the JdbcTable from the database for the given database connection and table name.
     */
    public HashMap getDBSchema(Connection con, ControlParams params) throws SQLException {

        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables

        String catalog = getCatalog(con);

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
        String colSql = "sp_columns null,null,'"+ catalog +"',null";
        Statement statCol = con.createStatement();
        ResultSet rsColumn = statCol.executeQuery(colSql);
        ArrayList currentColumns = null;

        while (rsColumn.next()) {

            String temptableName = rsColumn.getString("TABLE_NAME");

            if (!isValidSchemaTable(temptableName)) {
                continue;
            }
            if (!tableNameMap.containsKey(temptableName)) {
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
            col.length = rsColumn.getInt("PRECISION");
            col.scale = rsColumn.getInt("SCALE");
            col.nulls = rsColumn.getBoolean("NULLABLE");


            if (col.jdbcType == 11){
                col.jdbcType = java.sql.Types.TIMESTAMP;
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
        JdbcColumn[] lastJdbcColumns = new JdbcColumn[currentColumns.size()];
        currentColumns.toArray(lastJdbcColumns);
        JdbcTable colJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
        colJdbcTable.cols = lastJdbcColumns;
        tableName = null;
        currentColumns.clear();

        rsColumn.close();
        statCol.close();

        if (!params.checkColumnsOnly()) {
            Set mainTableNames = jdbcTableMap.keySet();
            if (params.isCheckPK()) {
                // now we do primaryKeys
                HashMap pkMap = null;

                String pkSql =
                        "select TABLE_NAME = o.name, \n" +
                        "       COLUMN_NAME = c.name, \n" +
                        "       KEY_SEQ = \n" +
                        "           case \n" +
                        "               when c.name = index_col(o.name, i.indid,  1) then convert (smallint,1) \n" +
                        "               when c.name = index_col(o.name, i.indid,  2) then convert (smallint,2) \n" +
                        "               when c.name = index_col(o.name, i.indid,  3) then convert (smallint,3) \n" +
                        "               when c.name = index_col(o.name, i.indid,  4) then convert (smallint,4) \n" +
                        "               when c.name = index_col(o.name, i.indid,  5) then convert (smallint,5)  \n" +
                        "               when c.name = index_col(o.name, i.indid,  6) then convert (smallint,6) \n" +
                        "               when c.name = index_col(o.name, i.indid,  7) then convert (smallint,7) \n" +
                        "               when c.name = index_col(o.name, i.indid,  8) then convert (smallint,8) \n" +
                        "               when c.name = index_col(o.name, i.indid,  9) then convert (smallint,9) \n" +
                        "               when c.name = index_col(o.name, i.indid, 10) then convert (smallint,10) \n" +
                        "               when c.name = index_col(o.name, i.indid, 11) then convert (smallint,11) \n" +
                        "               when c.name = index_col(o.name, i.indid, 12) then convert (smallint,12) \n" +
                        "               when c.name = index_col(o.name, i.indid, 13) then convert (smallint,13) \n" +
                        "               when c.name = index_col(o.name, i.indid, 14) then convert (smallint,14) \n" +
                        "               when c.name = index_col(o.name, i.indid, 15) then convert (smallint,15) \n" +
                        "               when c.name = index_col(o.name, i.indid, 16) then convert (smallint,16) \n" +
                        "           end, \n" +
                        "       PK_NAME = convert(sysname,i.name) \n" +
                        "  from sysindexes i, syscolumns c, sysobjects o\n" +
                        " where o.id = c.id \n" +
                        "   and o.id = i.id \n" +
                        "   and i.status2 & 2 = 2\n" +
                        "   and i.status & 2048 = 2048\n" +
                        "   and (c.name = index_col (o.name, i.indid,  1) or \n" +
                        "           c.name = index_col (o.name, i.indid,  2) or \n" +
                        "           c.name = index_col (o.name, i.indid,  3) or \n" +
                        "           c.name = index_col (o.name, i.indid,  4) or \n" +
                        "           c.name = index_col (o.name, i.indid,  5) or \n" +
                        "           c.name = index_col (o.name, i.indid,  6) or \n" +
                        "           c.name = index_col (o.name, i.indid,  7) or  \n" +
                        "           c.name = index_col (o.name, i.indid,  8) or \n" +
                        "           c.name = index_col (o.name, i.indid,  9) or \n" +
                        "           c.name = index_col (o.name, i.indid, 10) or \n" +
                        "           c.name = index_col (o.name, i.indid, 11) or  \n" +
                        "           c.name = index_col (o.name, i.indid, 12) or  \n" +
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
                        "select 'TABLE_NAME' = o.name, \n" +
                        "       'COLUMN_NAME' = INDEX_COL(o.name,indid,colid), \n" +
                        "       'INDEX_NAME' = x.name, \n" +
                        "       'NON_UNIQUE' =\n" +
                        "         case\n" +
                        "           when x.status & 2 != 2 then convert (smallint,1)\n" +
                        "           else convert (smallint,0)\n" +
                        "         end,\n" +
                        "       'TYPE' = \n" +
                        "         case\n" +
                        "           when x.indid > 1 then convert (smallint,3)\n" +
                        "           when x.status2 & 512 = 512 then convert (smallint,1)\n" +
                        "           else convert (smallint,1)\n" +
                        "         end,\n" +
                        "       'ORDINAL_POSITION' = colid \n" +
                        "  from sysindexes x, syscolumns c, sysobjects o \n" +
                        " where x.id = object_id(o.name) \n" +
                        "   and x.id = o.id \n" +
                        "   and o.type = 'U' \n" +
                        "   and x.status = 0 \n" +
                        "   and x.id = c.id \n" +
                        "   and c.colid < keycnt + (x.status & 16) / 16\n" +
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
                JdbcTable indexJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                if (indexJdbcTable != null){
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
                String fkSql1 =
                        " create table #fkeysall( \n" +
                        "    rkeyid int NOT NULL, \n" +
                        "    rkey1 int NOT NULL,\n" +
                        "    rkey2 int NOT NULL, \n" +
                        "    rkey3 int NOT NULL, \n" +
                        "    rkey4 int NOT NULL, \n" +
                        "    rkey5 int NOT NULL, \n" +
                        "    rkey6 int NOT NULL, \n" +
                        "    rkey7 int NOT NULL, \n" +
                        "    rkey8 int NOT NULL, \n" +
                        "    rkey9 int NOT NULL, \n" +
                        "    rkey10 int NOT NULL, \n" +
                        "    rkey11 int NOT NULL, \n" +
                        "    rkey12 int NOT NULL, \n" +
                        "    rkey13 int NOT NULL, \n" +
                        "    rkey14 int NOT NULL, \n" +
                        "    rkey15 int NOT NULL, \n" +
                        "    rkey16 int NOT NULL, \n" +
                        "    fkeyid int NOT NULL, \n" +
                        "    fkey1 int NOT NULL, \n" +
                        "    fkey2 int NOT NULL, \n" +
                        "    fkey3 int NOT NULL, \n" +
                        "    fkey4 int NOT NULL, \n" +
                        "    fkey5 int NOT NULL, \n" +
                        "    fkey6 int NOT NULL, \n" +
                        "    fkey7 int NOT NULL, \n" +
                        "    fkey8 int NOT NULL, \n" +
                        "    fkey9 int NOT NULL, \n" +
                        "    fkey10 int NOT NULL, \n" +
                        "    fkey11 int NOT NULL, \n" +
                        "    fkey12 int NOT NULL, \n" +
                        "    fkey13 int NOT NULL, \n" +
                        "    fkey14 int NOT NULL, \n" +
                        "    fkey15 int NOT NULL, \n" +
                        "    fkey16 int NOT NULL, \n" +
                        "    constid int NOT NULL, \n" +
                        "    name varchar(32) NOT NULL) ";
                Statement statFK1 = con.createStatement();
                statFK1.execute(fkSql1);
                statFK1.close();
                String fkSql2 =
                        "insert into #fkeysall\n" +
                        "       select\n" +
                        "       r.reftabid,\n" +
                        "       r.refkey1, r.refkey2, r.refkey3, r.refkey4,\n" +
                        "       r.refkey5, r.refkey6, r.refkey7, r.refkey8,\n" +
                        "       r.refkey9, r.refkey10, r.refkey11, r.refkey12,\n" +
                        "       r.refkey13, r.refkey14, r.refkey15, r.refkey16,\n" +
                        "       r.tableid,\n" +
                        "       r.fokey1, r.fokey2, r.fokey3, r.fokey4,\n" +
                        "       r.fokey5, r.fokey6, r.fokey7, r.fokey8,\n" +
                        "       r.fokey9, r.fokey10, r.fokey11, r.fokey12,\n" +
                        "       r.fokey13, r.fokey14, r.fokey15, r.fokey16,\n" +
                        "       r.constrid,\n" +
                        "       i.name\n" +
                        "  from sysreferences r, sysobjects o, sysindexes i\n" +
                        " where r.constrid = o.id\n" +
                        "   AND o.type = 'RI'\n" +
                        "   AND r.indexid = i.indid\n" +
                        "   AND r.reftabid = i.id";
                Statement statFK2 = con.createStatement();
                statFK2.execute(fkSql2);
                statFK2.close();
                String fkSql3 =
                        "create table #fkeys( \n" +
                        "   pktable_id  int NOT NULL, \n" +
                        "   pkcolid     int NOT NULL, \n" +
                        "   fktable_id  int NOT NULL, \n" +
                        "   fkcolid     int NOT NULL, \n" +
                        "   KEY_SEQ     smallint NOT NULL, \n" +
                        "   fk_id       int NOT NULL, \n" +
                        "   PK_NAME     varchar(32) NOT NULL) ";
                Statement statFK3 = con.createStatement();
                statFK3.execute(fkSql3);
                statFK3.close();
                String fkSql4 =
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
                Statement statFK4 = con.createStatement();
                statFK4.execute(fkSql4);
                statFK4.close();


                String fkSql =
                        "select PKTABLE_NAME = convert(sysname,o1.name),\n" +
                        "       PKCOLUMN_NAME = convert(sysname,c1.name),\n" +
                        "       FKTABLE_NAME = convert(sysname,o2.name),\n" +
                        "       FKCOLUMN_NAME = convert(sysname,c2.name),\n" +
                        "       KEY_SEQ,\n" +
                        "       FK_NAME = convert(sysname,OBJECT_NAME(fk_id)),\n" +
                        "       PK_NAME\n" +
                        "  from #fkeys f,\n" +
                        "       sysobjects o1, sysobjects o2,\n" +
                        "       syscolumns c1, syscolumns c2\n" +
                        " where o1.id = f.pktable_id\n" +
                        "   AND o2.id = f.fktable_id\n" +
                        "   AND c1.id = f.pktable_id\n" +
                        "   AND c2.id = f.fktable_id\n" +
                        "   AND c1.colid = f.pkcolid\n" +
                        "   AND c2.colid = f.fkcolid\n" +
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
                JdbcTable constraintsjdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                if (constraintsjdbcTable != null){
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

                Statement statCleanUp = con.createStatement();
                statCleanUp.execute("DROP TABLE #fkeysall, #fkeys");
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
            if (c.autoinc) {
                appendCreateColumnAutoInc(t, c, s);
                s.append(getRunCommand());
            } else if (c.nulls) {
                appendCreateColumnNulls(t, c, s);
                s.append(getRunCommand());
            } else {
                s.append(" NULL");
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
                s.append(' ');
                appendColumnType(c, s);
                s.append(" NOT NULL");
                s.append(getRunCommand());
            }
        }
    }
    /**
     * Add a Sequence column to implement a list
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {

        String mainTempTableName = getTempTableName(t, 30);
        String minTempTableName = getTempTableName(t,30);
        String identityColumnName = getTempColumnName(t);


        JdbcColumn indexColumn = null;
        JdbcColumn sequenceColumn = null;
        JdbcColumn[] cols = t.getColsForCreateTable();
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            if (isAddSequenceColumn(cols[i])) {
                sequenceColumn = cols[i];
            } else if (t.isInPrimaryKey(cols[i].name)){
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
        s.append(" NUMERIC(6) IDENTITY,");
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


        s.append(comment("recreate table "+ t.name+"."));
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


        s.append(getRunCommand());


        s.append(comment("populate table "+ t.name +" with the new sequence column."));
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
    protected void appendModifyColumn(TableDiff tableDiff, ColumnDiff diff,
                                      CharBuf s, boolean comments) {
        JdbcTable t = tableDiff.getOurTable();
        JdbcColumn c = diff.getOurCol();
        boolean nulls = diff.isNullDiff();
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
        if (nulls){
            if (c.nulls){
                s.append(" NULL");
            } else {
                s.append(" NOT NULL");
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
            s.append(" DROP ");
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
    protected void dropSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {
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
        s.append(getRunCommand());

        s.append(comment("rename temp table to main table."));
        s.append("\n");
        s.append("sp_rename ");
        s.append(tempTableName);
        s.append(", ");
        s.append(t.name);

    }

    protected void fixColumnsNonDirect(TableDiff tableDiff, PrintWriter out) {

        JdbcTable ourTable = tableDiff.getOurTable();
        String tempTableName = getTempTableName(ourTable, 30);
        CharBuf s = new CharBuf();
        s.append("CREATE TABLE ");
        s.append(tempTableName);
        s.append(" (\n");
        JdbcColumn[] cols = ourTable.getColsForCreateTable();
        int nc = cols.length;
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first){
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

                } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && diff.isNullDiff()) {
                    if (cols[i].nulls) {
                        if (i != 0) {
                            s.append("       ");
                        }
                        //select convert(char(25),col1) from test1a
                        s.append("CONVERT(");
                        appendColumnType(cols[i], s);
                        s.append(", ");
                        s.append(cols[i].name);
                        s.append(")");
                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append("CASE ");
                        s.append("\n");//new line
                        s.append("         WHEN ");
                        s.append(cols[i].name);
                        s.append(" IS NOT NULL THEN CONVERT(");
                        appendColumnType(cols[i], s);
                        s.append(", ");
                        s.append(cols[i].name);
                        s.append(")");
                        s.append("\n");//new line
                        s.append("         ELSE ");
                        s.append(getDefaultForType(diff.getOurCol()));
                        s.append("\n");//new line
                        s.append("       END");
                    }

                } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && !diff.isNullDiff()) {
                    if (i != 0) {
                        s.append("       ");
                    }
                    s.append("CONVERT(");
                    appendColumnType(cols[i], s);
                    s.append(", ");
                    s.append(cols[i].name);
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
                        s.append("         WHEN ");
                        s.append(cols[i].name);
                        s.append(" IS NOT NULL THEN ");
                        s.append(cols[i].name);
                        s.append("\n");//new line
                        s.append("         ELSE ");
                        s.append(getDefaultForType(diff.getOurCol()));
                        s.append("\n");//new line
                        s.append("       END");
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


        s.append("sp_rename ");
        s.append(tempTableName);
        s.append(", ");
        s.append(ourTable.name);


        s.append(getRunCommand());
        out.println(s.toString());


    }

    boolean isDirectDropColumnSupported() {
        return supportDirectColumnChange;
    }

    boolean isDirectAddColumnSupported(JdbcColumn ourCol) {
        return supportDirectColumnChange;
    }

    boolean isDirectNullColumnChangesSupported() {
        return supportDirectColumnChange;
    }

    boolean isDirectScaleColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        return supportDirectColumnChange;
    }

    boolean isDirectLenghtColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        return supportDirectColumnChange;
    }

    boolean isDirectTypeColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        return supportDirectColumnChange;
    }

    boolean isDropConstraintsForDropTableSupported() {
        return false;
    }

}
