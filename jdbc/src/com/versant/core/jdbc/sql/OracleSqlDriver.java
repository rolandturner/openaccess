
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
import com.versant.core.jdbc.sql.diff.ColumnDiff;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.diff.TableDiff;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.util.CharBuf;
import com.versant.core.common.BindingSupportImpl;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * A driver for Oracle.
 */
public class OracleSqlDriver extends SqlDriver {

    protected CharacterStreamConverter.Factory characterStreamConverterFactory
            = new CharacterStreamConverter.Factory();
    protected InputStreamConverter.Factory inputStreamConverterFactory
            = new InputStreamConverter.Factory();
	protected OracleBlobConverter.Factory blobConverterFactory
		= new OracleBlobConverter.Factory();
	
	protected OracleClobConverter.Factory clobConverterFactory
			= new OracleClobConverter.Factory();
	

		
    private HashMap typeDiffMap = null;

    /**
     * Get the name of this driver.
     */
    public String getName() {
        return "oracle";
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
                return new JdbcTypeMapping("NUMBER",
                    19, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);

            case Types.DECIMAL:
            
            case Types.NUMERIC:
                return new JdbcTypeMapping("NUMBER",
                    20, 10, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return new JdbcTypeMapping("DATE",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.DOUBLE:
                return new JdbcTypeMapping("DOUBLE PRECISION",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE, null);
            case Types.CLOB:
                return new JdbcTypeMapping("CLOB",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                    clobConverterFactory);
            case Types.LONGVARCHAR:
                return new JdbcTypeMapping("LONG",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                    characterStreamConverterFactory);
            case Types.VARCHAR:
                return new JdbcTypeMapping("VARCHAR2",
                    255, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.LONGVARBINARY:
                return new JdbcTypeMapping("LONG RAW",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                    inputStreamConverterFactory);

            case Types.VARBINARY:
            case Types.BLOB:
                return new JdbcTypeMapping("BLOB",
                    0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                    blobConverterFactory);

                    
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



        return ans;
    }



    /**
     * Does the JDBC driver support statement batching?
     */
    public boolean isInsertBatchingSupported() {
        return true;
    }

    /**
     * Can batching be used if the statement contains a column with the
     * given JDBC type?
     */
    public boolean isBatchingSupportedForJdbcType(int jdbcType) {
        return jdbcType != Types.LONGVARCHAR
            && jdbcType != Types.LONGVARBINARY;
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
     * Should indexes be used for columns in the order by list that are
     * also in the select list? This is used for databases that will not
     * order by a column that is duplicated in the select list (e.g. Oracle).
     */
    public boolean isUseIndexesForOrderCols() {
        return true;
    }

    public boolean isPutOrderColsInSelect() {
        return true;
    }

    /**
     * Is null a valid value for a column with a foreign key constraint?
     */
    public boolean isNullForeignKeyOk() {
        return true;
    }

    /**
     * How many PreparedStatement's should the pool be limited to by default
     * (0 for unlimited) ?
     */
    public int getDefaultPsCacheMax() {
        return 30;
    }

    /**
     * Can 'SELECT FOR UPDATE' be used with a DISTINCT?
     */
    public boolean isSelectForUpdateWithDistinctOk() {
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
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table, Statement stat) throws SQLException {
        stat.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS");
    }

    /**
     * Append the allow nulls part of the definition for a column in a
     * create table statement.
     */
    protected void appendCreateColumnNulls(JdbcTable t, JdbcColumn c,
            CharBuf s) {
        if (!c.nulls) {
            s.append(" NOT NULL");
        } else {
            s.append(" NULL");
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
     * Append an 'drop constraint' statement for c.
     */
    protected void dropRefConstraint(CharBuf s, JdbcConstraint c) {
        s.append("ALTER TABLE ");
        s.append(c.src.name);
        s.append(" DROP CONSTRAINT ");
        s.append(c.name);
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
        return "SELECT sysdate FROM dual";
    }

    /**
     * Gets the current user's schema
     */
    protected String getSchema(Connection con) {
        String schema = null;
        String sql = "SELECT sys_context('USERENV','CURRENT_SCHEMA') FROM dual";
        try {
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()){
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
            createPlan(con);
        } catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return "EXPLAIN PLAN SET statement_id = 'JDO_PLAN' INTO jdo_plan_table FOR "+sql;
    }

    private void createPlan(Connection con) throws SQLException{
        String exists ="SELECT TABLE_NAME FROM SYS.ALL_TABLES WHERE table_name = 'JDO_PLAN_TABLE'";
        Statement existsStat = con.createStatement();
        ResultSet rs = existsStat.executeQuery(exists);
        if (rs.next()){
            try{
                rs.close();
                existsStat.close();
            } catch (SQLException e){ }

        } else {
            Statement statement = con.createStatement();
            String create =
                    "CREATE TABLE jdo_plan_table (" +
                    "   STATEMENT_ID                    VARCHAR2(30)," +
                    "   TIMESTAMP                       DATE," +
                    "   REMARKS                         VARCHAR2(80)," +
                    "   OPERATION                       VARCHAR2(30)," +
                    "   OPTIONS                         VARCHAR2(30)," +
                    "   OBJECT_NODE                     VARCHAR2(128)," +
                    "   OBJECT_OWNER                    VARCHAR2(30)," +
                    "   OBJECT_NAME                     VARCHAR2(30)," +
                    "   OBJECT_INSTANCE                 NUMBER(38)," +
                    "   OBJECT_TYPE                     VARCHAR2(30)," +
                    "   OPTIMIZER                       VARCHAR2(255)," +
                    "   SEARCH_COLUMNS                  NUMBER," +
                    "   ID                              NUMBER(38)," +
                    "   PARENT_ID                       NUMBER(38)," +
                    "   POSITION                        NUMBER(38)," +
                    "   COST                            NUMBER(38)," +
                    "   CARDINALITY                     NUMBER(38)," +
                    "   BYTES                           NUMBER(38)," +
                    "   OTHER_TAG                       VARCHAR2(255)," +
                    "   PARTITION_START                 VARCHAR2(255)," +
                    "   PARTITION_STOP                  VARCHAR2(255)," +
                    "   PARTITION_ID                    NUMBER(38)," +
                    "   OTHER                           LONG," +
                    "   DISTRIBUTION                    VARCHAR2(30) )";

            statement.execute(create);

            try{
                rs.close();
                existsStat.close();
                statement.close();
            } catch (SQLException e){ }

        }
    }

    /**
     * Get the query plan for ps and cleanup anything done in
     * prepareForGetQueryPlan. Return null if this is not supported.
     * @see #prepareForGetQueryPlan
     * @see #cleanupForGetQueryPlan
     */
    public String getQueryPlan(Connection con, PreparedStatement ps) {
        StringBuffer buff = new StringBuffer();
        Statement stat = null;
        ResultSet rs = null;
        try{

            ps.execute();

            stat = con.createStatement();


            String select =
                    "select lpad(' ',4*(level-1))||operation||' ('||options||') '|| object_name||' '  "+
                    "       ||decode(object_type,'','','('||object_type||') ')	 "+
                    "       ||decode(object_node,'','','['||object_node||'] ') "+
                    "       ||decode(OPTIMIZER,'','','['||OPTIMIZER||'] ')    "+
                    "       ||decode(id,0,'Cost='||position,                  "+
                    "                     decode(COST,'','',' Cost='||COST||' '  "+
                    "                            ||decode(id,0,'','Card='||CARDINALITY||' ')  "+
                    "                            ||decode(id,0,'','Bytes='||BYTES)  "+
                    "                           )               ) query "+
                    "from jdo_plan_table "+
                    " where statement_id = 'JDO_PLAN' "+
                    "start with id = 0 "+
                    "connect by prior id = parent_id "+
                    "ORDER BY id";
            rs = stat.executeQuery(select);

            int count = -1;
            while (rs != null && rs.next()) {
                if (count == -1){
                    count = 0;
                } else {
                    buff.append('\n');
                }
                buff.append(rs.getString(1));
            }
        } catch (Exception sqle){
            sqle.printStackTrace();
        } finally {
            try{
                rs.close();
                stat.close();
            } catch (Exception e){}
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
            Statement statement2 = con.createStatement();
            statement2.execute("DROP TABLE jdo_plan_table");
            try{
                statement2.close();
            } catch (SQLException e){}
        } catch (SQLException sqle){
            sqle.printStackTrace();
        }
    }

    /**
     * Get the JdbcTables from the database for the given database con.
     * @param con
     * @return HashMap of tablename.toLowerCase() as key and JdbcTable as value
     * @throws SQLException on DB errors
     */
    public HashMap getDBSchema(Connection con, ControlParams params) throws SQLException {
        HashMap jdbcTableMap = new HashMap(); // main map of jdbc tables

        HashMap synonymMap = new HashMap();
        try {
            String synonymSql =
                    "SELECT TABLE_NAME,\n" +
                    "       SYNONYM_NAME \n" +
                    "  FROM ALL_SYNONYMS \n" +
                    " WHERE SYNONYM_NAME NOT LIKE TABLE_NAME ";
            Statement statSynonym = con.createStatement();
            ResultSet rsSynonym = statSynonym.executeQuery(synonymSql);
            while (rsSynonym.next()) {
                synonymMap.put(rsSynonym.getString(1).toLowerCase(), rsSynonym.getString(2).toLowerCase());
            }
            // clean up
            if (rsSynonym != null) {
                try {
                    rsSynonym.close();
                } catch (SQLException e) { }
            }
            if (statSynonym != null) {
                try {
                    statSynonym.close();
                } catch (SQLException e) { }
            }
        } catch (SQLException e) {
            // hide all
        }

        // now we do columns
        String tableName = null;

        String columnSql =
                "SELECT DISTINCT t.table_name AS table_name,\n" +
                "       t.column_name AS column_name,\n" +
                "       DECODE (t.data_type, 'CHAR', 1,'NCHAR', 1,'NVARCHAR2', 12, 'VARCHAR2', 12,\n" +
                "           'NUMBER', 3, 'LONG', -1, 'DATE', 91, 'RAW', -3, 'LONG RAW', -4,\n" +
                "           'FLOAT', DECODE (DECODE (t.data_precision, null, t.data_length, t.data_precision),126,8,6),\n" +
                "           'BLOB', 2004,'BFILE', 2004,'CLOB',2005,'NCLOB', 2005, 'TIMESTAMP(6)', 93,'TIMESTAMP', 93, \n" +
                "           'TIMESTAMP WITH LOCAL TIME ZONE' , 93 ,'TIMESTAMP WITH TIME ZONE',12,\n" +
                "           'XMLTYPE',2005, 1111) AS data_type, \n" +
                "       t.data_type AS type_name,\n" +
                "       decode(t.data_type, 'CLOB', 2147483647, 'NCLOB', 2147483647, 'LONG', 2147483647,\n" +
                "           'BLOB', 2147483647, 'LONG RAW', 2147483647, 'BFILE', 2147483647, 'DATE', 19,\n" +
                "           'ROWID', 18, DECODE (t.data_precision, null, t.data_length, t.data_precision)) as column_size,\n" +
                "       t.data_scale AS decimal_digits,\n" +
                "       DECODE (t.nullable, 'N', 0, 1) AS nullable,\n" +
                "       t.column_id AS ordinal_position\n" +
                "  FROM user_tab_columns t,\n" +
                "       user_tables u\n" +
                " WHERE u.table_name = t.table_name\n" +
                "   AND u.table_name NOT LIKE('AQ$_%')\n" +
                "   AND u.table_name NOT LIKE('DEF$_%')\n" +
                "   AND u.table_name NOT LIKE('LOGMNR_%')\n" +
                "   AND u.table_name NOT LIKE('LOGSTDBY$%')\n" +
                "   AND u.table_name NOT LIKE('MVIEW$_%')\n" +
                "   AND u.table_name NOT LIKE('REPCAT$_%')\n" +
                "   AND u.table_name NOT LIKE('SQLPLUS_PRODUCT_PROFILE')\n" +
                "   AND u.table_name NOT LIKE('HELP')\n" +
                " ORDER BY table_name, ordinal_position ";
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

            col.name = rsColumn.getString(2);
            col.sqlType = rsColumn.getString(4);
            col.jdbcType = rsColumn.getInt(3);
            col.length = rsColumn.getInt(5);
            col.scale = rsColumn.getInt(6);
//            if (col.sqlType.equals("NUMBER") &&
//                    col.jdbcType == 3
//                    && col.scale == 0){
//                col.jdbcType = java.sql.Types.INTEGER;
//            }
            col.nulls = rsColumn.getBoolean(7);

            switch (col.jdbcType) {
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
                        "SELECT c.table_name,\n" +
                        "       c.column_name,\n" +
                        "       c.position ,\n" +
                        "       c.constraint_name \n" +
                        "FROM user_cons_columns c,\n" +
                        "     user_constraints k\n" +
                        "WHERE k.constraint_type = 'P'\n" +
                        "  AND k.constraint_name = c.constraint_name\n" +
                        "  AND k.table_name = c.table_name \n" +
                        "  AND k.table_name NOT LIKE('AQ$_%')\n" +
                        "  AND k.table_name NOT LIKE('DEF$_%')\n" +
                        "  AND k.table_name NOT LIKE('LOGMNR_%')\n" +
                        "  AND k.table_name NOT LIKE('LOGSTDBY$%')\n" +
                        "  AND k.table_name NOT LIKE('MVIEW$_%')\n" +
                        "  AND k.table_name NOT LIKE('REPCAT$_%')\n" +
                        "  AND k.table_name NOT LIKE('SQLPLUS_PRODUCT_PROFILE')\n" +
                        "  AND k.table_name NOT LIKE('HELP')\n" +
                        "  AND k.owner = c.owner \n" +
                        "ORDER BY c.table_name,c.constraint_name,c.position";

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
                String indexSql =
                        "select  i.table_name,\n" +
                        "        c.column_name,\n" +
                        "        i.index_name,\n" +
                        "        decode (i.uniqueness, 'UNIQUE', 0, 1) as NON_UNIQUE,\n" +
                        "        1 as type,\n" +
                        "        c.column_position as ordinal_position\n" +
                        " from   user_indexes i,\n" +
                        "        user_ind_columns c  \n" +
                        " where  i.index_name = c.index_name\n" +
                        "   AND  i.table_name = c.table_name\n" +
                        "   AND  i.table_name NOT LIKE('AQ$_%')\n" +
                        "   AND  i.table_name NOT LIKE('DEF$_%')\n" +
                        "   AND  i.table_name NOT LIKE('LOGMNR_%')\n" +
                        "   AND  i.table_name NOT LIKE('LOGSTDBY$%')\n" +
                        "   AND  i.table_name NOT LIKE('MVIEW$_%')\n" +
                        "   AND  i.table_name NOT LIKE('REPCAT$_%')\n" +
                        "   AND  i.table_name NOT LIKE('SQLPLUS_PRODUCT_PROFILE')\n" +
                        "   AND  i.table_name NOT LIKE('HELP')" +
                        " order  by i.table_name,index_name, ordinal_position";
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

                    if (tempJdbcTable == null){
                        continue;
                    }

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
                            boolean foundCol = false;
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equalsIgnoreCase(jdbcColumn.name)) {
                                    indexColumns[tempIndexColumns.length] = jdbcColumn;
                                    jdbcColumn.partOfIndex = true;
                                    foundCol = true;
                                }
                            }

                            if (foundCol){
                                index.setCols(indexColumns);
                            }
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
                            boolean foundCol = false;
                            for (int i = 0; i < jdbcTable.cols.length; i++) {
                                JdbcColumn jdbcColumn = jdbcTable.cols[i];
                                if (colName.equalsIgnoreCase(jdbcColumn.name)) {
                                    indexColumns[0] = jdbcColumn;
                                    jdbcColumn.partOfIndex = true;
                                    foundCol = true;
                                }
                            }
                            if (foundCol) {
                                index.setCols(indexColumns);
                                indexes.add(index);
                            }
                        }
                    }
                }
                JdbcTable indexJdbcTable = (JdbcTable) jdbcTableMap.get(tableName);
                if (indexJdbcTable != null) {
                    if (indexJdbcTable != null){
                        JdbcIndex[] jdbcIndexes = new JdbcIndex[indexes.size()];
                        indexes.toArray(jdbcIndexes);
                        indexJdbcTable.indexes = jdbcIndexes;
                    }
                    indexes.clear();
                    indexNameMap.clear();
                }
                tableName = null;
                // clean up
                if (rsIndex != null){
                    try {
                        rsIndex.close();
                    } catch (SQLException e) { }
                }
                if (statIndex != null) {
                    try {
                        statIndex.close();
                    } catch (SQLException e) { }
                }
            }
            if (params.isCheckConstraint()) {
                // now we do forign keys

                String fkSql =
                        "SELECT p.table_name as pktable_name,\n" +
                        "       pc.column_name as pkcolumn_name,\n" +
                        "       f.table_name as fktable_name,\n" +
                        "       fc.column_name as fkcolumn_name,\n" +
                        "       fc.position as key_seq,\n" +
                        "       f.constraint_name as fk_name,\n" +
                        "       p.constraint_name as pk_name\n" +
                        "FROM   user_cons_columns pc, \n" +
                        "       user_constraints p,\n" +
                        "       user_cons_columns fc,\n" +
                        "       user_constraints f\n" +
                        "WHERE  f.constraint_type = 'R'\n" +
                        "  AND  p.owner = f.r_owner \n" +
                        "  AND  p.constraint_name = f.r_constraint_name\n" +
                        "  AND  p.constraint_type = 'P'\n" +
                        "  AND  pc.owner = p.owner\n" +
                        "  AND  pc.constraint_name = p.constraint_name\n" +
                        "  AND  pc.table_name = p.table_name\n" +
                        "  AND  fc.owner = f.owner\n" +
                        "  AND  fc.constraint_name = f.constraint_name\n" +
                        "  AND  fc.table_name = f.table_name\n" +
                        "  AND  f.table_name NOT LIKE('AQ$_%')\n" +
                        "  AND  f.table_name NOT LIKE('DEF$_%')\n" +
                        "  AND  f.table_name NOT LIKE('LOGMNR_%')\n" +
                        "  AND  f.table_name NOT LIKE('LOGSTDBY$%')\n" +
                        "  AND  f.table_name NOT LIKE('MVIEW$_%')\n" +
                        "  AND  f.table_name NOT LIKE('REPCAT$_%')\n" +
                        "  AND  f.table_name NOT LIKE('SQLPLUS_PRODUCT_PROFILE')\n" +
                        "  AND  f.table_name NOT LIKE('HELP')\n" +
                        "  AND  fc.position = pc.position\n" +
                        "ORDER  BY fktable_name, fk_name,key_seq ";
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
                if(constraintsjdbcTable != null) {
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
        String name = null;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            JdbcTable table = (JdbcTable) iterator.next();
            name = table.name.toLowerCase();
            returnMap.put(name, table);
            if (synonymMap.containsKey(name)){
                returnMap.put(synonymMap.get(name), table);
            }
        }
        fixAllNames(returnMap);
        return returnMap;
    }

    public boolean checkType(JdbcColumn ourCol, JdbcColumn dbCol) {
        String ourSqlType = ourCol.sqlType.toUpperCase();
        String dbSqlType = dbCol.sqlType.toUpperCase();
        if (ourCol.jdbcType == dbCol.jdbcType) {
            return true;
        } else if (ourSqlType.startsWith(dbSqlType)) {
            return true;
        } else {
            switch (ourCol.jdbcType) {
                case Types.SMALLINT:
                case Types.BIT:
                case Types.TINYINT:
                    switch (dbCol.jdbcType) {
                        case Types.BIT:
                        case Types.TINYINT:
                        case Types.DECIMAL:
                            return true;
                        default:
                            return false;
                    }
                case Types.INTEGER:
                    switch (dbCol.jdbcType) {
                        case Types.NUMERIC:
                        case Types.DECIMAL:
                            return true;
                        default:
                            return false;
                    }

                default:
                    return super.checkType(ourCol, dbCol);
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
            s.append(" ADD (");
            s.append(c.name);
            s.append(' ');
            appendColumnType(c, s);
            s.append(" NULL)");
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
                s.append(" MODIFY ");
                s.append(c.name);
                s.append(' ');
                appendColumnType(c, s);
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
                s.append(" IS NULL");

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
     * Must this column be recreated?
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

    public boolean isOracleStoreProcs() {
        return true;
    }

    boolean isDirectTypeColumnChangesSupported(JdbcColumn toCol, JdbcColumn fromCol) {
        switch (fromCol.jdbcType) {
            case Types.BIT:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.TINYINT:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.SMALLINT:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.INTEGER:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.BIGINT:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.FLOAT:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.REAL:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.DOUBLE:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.NUMERIC:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.DECIMAL:
                switch (toCol.jdbcType) {
                    case Types.BIT:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        return true;
                    default:
                        return false;
                }
            case Types.CHAR:
                switch (toCol.jdbcType) {
                    case Types.VARCHAR:
                    case Types.CHAR:
                        return true;
                    default:
                        return false;
                }
            case Types.VARCHAR:
                switch (toCol.jdbcType) {
                    case Types.VARCHAR:
                    case Types.CHAR:
                        return true;
                    default:
                        return false;
                }
            case Types.DATE:
                switch (toCol.jdbcType) {
                    case Types.TIMESTAMP:
                    case Types.DATE:
                    case Types.TIME:
                        return true;
                    default:
                        return false;
                }
            case Types.TIME:
                switch (toCol.jdbcType) {
                    case Types.TIMESTAMP:
                    case Types.DATE:
                    case Types.TIME:
                        return true;
                    default:
                        return false;
                }
            case Types.TIMESTAMP:
                switch (toCol.jdbcType) {
                    case Types.TIMESTAMP:
                    case Types.DATE:
                    case Types.TIME:
                        return true;
                    default:
                        return false;
                }
            case Types.CLOB:
            case Types.LONGVARCHAR:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
                /*
                todo we need to throw a exception if this happens, because we cannot support this changes.
                */



        }
        return false;
    }


    boolean isDirectScaleColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        if (dbCol.scale < ourCol.scale) {
            return true;
        }
        return false;
    }

    boolean isDirectLenghtColumnChangesSupported(JdbcColumn ourCol, JdbcColumn dbCol) {
        if (dbCol.length < ourCol.length) {
            return true;
        }
        return false;
    }

    /**
     * Append an 'drop constraint' statement for c.
     */
    protected void appendRefDropConstraint(CharBuf s, JdbcConstraint c, boolean comments) {
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

    protected void fixColumnsNonDirect(TableDiff tableDiff, PrintWriter out) {

        JdbcTable ourTable = tableDiff.getOurTable();
        String tempTableName = getTempTableName(ourTable,30);


        CharBuf s = new CharBuf();

        JdbcTable dbTable = tableDiff.getDbTable();
        if (dbTable.pkConstraintName != null) {
            if (tableDiff.getPkDiffs().isEmpty()) {
                dropPrimaryKeyConstraint(tableDiff.getDbTable(), s);
                s.append(getRunCommand());
            }
        }

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


        s.append("INSERT INTO ");
        s.append(tempTableName);  //
        s.append(" (");
        for (int i = 0; i < nc; i++) {
            s.append(cols[i].name);
            if ((i + 1) != nc) {
                s.append(", ");
            }
        }
        s.append(") ");

        s.append("\n");

        s.append("SELECT ");
        for (int i = 0; i < nc; i++) {
            JdbcColumn ourCol = cols[i];
            ColumnDiff diff = getColumnDiffForName(tableDiff, ourCol.name);
            if (diff == null) {
                if (i != 0) {
                    s.append("       ");
                }
                s.append(ourCol.name);
            } else {
                JdbcColumn dbCol = diff.getDbCol();
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

                    if (ourCol.nulls) {
                        if (i != 0) {
                            s.append("       ");
                        }
                        appendCast(ourCol, dbCol, s, false);
                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append("CASE ");
                        s.append("\n");//new line
                        s.append("            WHEN ");
                        s.append(ourCol.name);
                        s.append(" IS NOT NULL THEN ");
                        appendCast(ourCol, dbCol, s, false);
                        s.append("\n");//new line
                        s.append("            ELSE ");
                        appendCast(ourCol, dbCol, s, true);
                        s.append("\n");//new line
                        s.append("       END");
                    }

                } else if ((diff.isLenghtDiff() || diff.isScaleDiff() || diff.isTypeDiff()) && !diff.isNullDiff()) {
                    if (i != 0) {
                        s.append("       ");
                    }
                    appendCast(ourCol, dbCol, s, true);
                } else if (diff.isNullDiff()) {
                    if (ourCol.nulls) {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append(ourCol.name);
                    } else {
                        if (i != 0) {
                            s.append("       ");
                        }
                        s.append("CASE ");
                        s.append("\n");//new line
                        s.append("            WHEN ");
                        s.append(ourCol.name);
                        s.append(" IS NOT NULL THEN ");
                        s.append(ourCol.name);
                        s.append("\n");//new line
                        s.append("            ELSE ");
                        s.append(getDefaultForType(ourCol));
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
        s.append(" CASCADE CONSTRAINTS");
        s.append(getRunCommand());

        s.append("ALTER TABLE ");
        s.append(tempTableName);
        s.append(" RENAME TO ");
        s.append(ourTable.name);
        s.append(getRunCommand());

        out.println(s.toString());


    }

    private void appendCast(JdbcColumn ourCol, JdbcColumn dbCol, CharBuf s, boolean defaultValue) {
        String ourType = ourCol.sqlType.toUpperCase().trim();
        String dbType = dbCol.sqlType.toUpperCase().trim();

        if ((ourType.startsWith("VARCHAR2") || ourType.startsWith("CHAR")) && (
                dbType.startsWith("VARCHAR2") ||
                dbType.startsWith("CHAR") ||
                dbType.startsWith("NCHAR"))) {
            s.append("CAST(TRANSLATE(");
            if (defaultValue) {
                s.append(getDefaultForType(ourCol));
            } else {
                s.append(ourCol.name);
            }
            s.append(" USING CHAR_CS) AS ");
            appendColumnType(ourCol, s);
            s.append(")");
        } else if (ourType.startsWith("NCHAR") && (
                dbType.startsWith("VARCHAR2") ||
                dbType.startsWith("CHAR") ||
                dbType.startsWith("NCHAR"))) {

            s.append("CAST(TRANSLATE(");
            if (defaultValue) {
                s.append(getDefaultForType(ourCol));
            } else {
                s.append(ourCol.name);
            }
            s.append(" USING NCHAR_CS)  AS ");
            appendColumnType(ourCol, s);
            s.append(")");
        } else if ((ourType.startsWith("CLOB") || (ourType.startsWith("BLOB"))) && (
                dbType.startsWith("LONG"))) {
            if (defaultValue) {
                s.append(getDefaultForType(ourCol));
            } else {
                s.append("TO_LOB(");
                s.append(ourCol.name);
                s.append(")");
            }
        } else {
            s.append("CAST(");
            if (defaultValue) {
                s.append(getDefaultForType(ourCol));
            } else {
                s.append(ourCol.name);
            }
            s.append(" AS ");
            appendColumnType(ourCol, s);
            s.append(")");
        }
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
        s.append("ALTER TABLE ");
        s.append(tempTableName);
        s.append(" RENAME TO ");
        s.append(t.name);

    }


    /**
     * Add a Sequence column to implement a List
     */
    protected void addSequenceColumn(JdbcTable t, JdbcColumn c, CharBuf s, boolean comments) {
        String tempTableName = getTempTableName(t, 30);
        String minTempTableName = getTempTableName(t, 30);

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


        s.append(comment("create a temp table to store old table values."));
        s.append("\n");
        s.append("CREATE TABLE ");
        s.append(tempTableName);
        s.append(" (\n");
        boolean first = true;
        for (int i = 0; i < nc; i++) {
            if (first) {
                first = false;
            } else {
                s.append("\n");
            }
            s.append("    ");
            appendCreateColumn(t, cols[i], s, true);
        }
        int lastIndex = s.toString().lastIndexOf(',');
        s.replace(lastIndex, lastIndex + 1, ' ');// we take the last ',' out.
        s.append("\n)");
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


        s.append(comment("insert a sequence, and copy the rest of the old table into the temp table."));
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


        s.append(comment("store the minimum id."));
        s.append("\n");
        s.append("UPDATE ");
        s.append(tempTableName);
        s.append("\n   SET ");
        s.append(c.name);
        s.append(" = ROWNUM");
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
        s.append("MIN(" +
                c.name +   //"ROWNUM" +
                ")\n");
        s.append("  FROM ");
        s.append(tempTableName);
        s.append("\n");
        s.append(" GROUP BY ");
        s.append(indexColumn.name);


        s.append(getRunCommand());



        s.append(comment("update the sequence column."));
        s.append("\n");
        s.append("UPDATE ");
        s.append(tempTableName);
        s.append(" a\n   SET ");
        s.append(c.name);
        s.append(" = (SELECT a.");
        s.append(c.name);
        s.append(" - b.min_id\n");
        s.append(pad(13+ c.name.length()));
        s.append("FROM ");
        s.append(minTempTableName);
        s.append(" b\n");
        s.append(pad(12 + c.name.length()));
        s.append("WHERE a.");
        s.append(indexColumn.name);
        s.append(" = b.");
        s.append(indexColumn.name);
        s.append(')');


        s.append(getRunCommand());

        s.append(comment("drop temp table."));
        s.append("\n");
        s.append("DROP TABLE ");
        s.append(minTempTableName);
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
        s.append(getRunCommand());

        s.append(comment("Add the primary key back."));
        s.append("\n");
        addPrimaryKeyConstraint(t,s);
        s.append(getRunCommand());


    }

}
