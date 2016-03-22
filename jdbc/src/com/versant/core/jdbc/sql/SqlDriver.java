
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
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.sql.conv.*;
import com.versant.core.jdbc.sql.diff.*;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.JdbcUtils;
import com.versant.core.jdbc.conn.StatementWithLastSQL;
import com.versant.core.util.CharBuf;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.jdo.query.AggregateNode;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.metadata.ClassMetaData;

import java.util.*;
import java.util.Date;
import java.sql.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.PrintWriter;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;

/**
 * This is the base class for the classes responsible for generating SQL
 * for different databases and interfacing to JDBC drivers. There is normally
 * one shared instance per Store. This class is also responsible for creating
 * columns for fields and so on during meta data generation.<p>
 */
public abstract class SqlDriver {

    public static final char[] DEFAULT_PARAM_CHARS = new char[]{'?'};

    /**
     * These are all the JDBC type codes that we care about. All SqlDriver
     * subclasses must provide a mapping for each of these.
     */
    public static final int[] JDBC_TYPES = new int[]{
        Types.BIT, Types.TINYINT, Types.SMALLINT, Types.INTEGER,
        Types.BIGINT, Types.FLOAT, Types.REAL, Types.DOUBLE, Types.NUMERIC,
        Types.DECIMAL, Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR,
        Types.DATE, Types.TIME, Types.TIMESTAMP, Types.BINARY,
        Types.VARBINARY, Types.LONGVARBINARY,
        Types.BLOB, Types.CLOB, 
    };

    protected BytesConverter.Factory bytesConverterFactory
            = new BytesConverter.Factory();
    protected NullBytesAsBinaryConverter.Factory nullBytesAsBinaryConverterFactory
            = new NullBytesAsBinaryConverter.Factory();

    protected static final int COMMENT_COL = 40;

    private static char[] FOR_UPDATE = " FOR UPDATE".toCharArray();

    private ArrayList allTableList = null;

    protected static DecimalFormat doubleFormat;

    public SqlDriver() {
        doubleFormat = new DecimalFormat("#.#",
                new DecimalFormatSymbols(Locale.US));
    }

    /**
     * Get the name of this driver.
     */
    public abstract String getName();

    public int getMajorVersion() {
        return -1;
    }

    public int getMinorVersion() {
        return -1;
    }

    public String getMinorVersionPatchLevel() {
        return "NOT_SET";
    }

    public String getVersion() {
        return "NOT_SET";
    }

    /**
     * Create a SqlDriver instance by name.
     *
     * @param jdbcDriver Optional JDBC driver for more accurate feature use
     * @throws javax.jdo.JDOFatalUserException
     *          if name is invalid
     * @see #customizeForServer
     */
    public static SqlDriver createSqlDriver(String name, Driver jdbcDriver) {
        SqlDriver ans = null;

        if (name.equals("informix")) {
            ans = new InformixSqlDriver();
        } else if (name.equals("informixse")) {
            ans = new InformixSESqlDriver();
        } else if (name.equals("sybase")) {
            ans = new SybaseSqlDriver();
        } else if (name.equals("db2")) {
            ans = new DB2SqlDriver();
        } else  if (name.equals("mssql")) {
            ans = new MsSqlDriver();

        } else if (name.equals("postgres")) {
            ans = new PostgresSqlDriver(); 
        } else if (name.equals("oracle")) {
            ans = new OracleSqlDriver();

        } else if (name.equals("hypersonic")) {
            ans = new HypersonicSqlDriver();
        } else if (name.equals("instantdb")) {
            ans = new InstantDbSqlDriver();
        } else if (name.equals("mckoi")) {
            ans = new MckoiSqlDriver();
        } else if (name.equals("sapdb")) {
            ans = new SapDbSqlDriver();
        } else if (name.equals("interbase")) {
            ans = new InterbaseSqlDriver();
        } else if (name.equals("pointbase")) {
            ans = new PointbaseSqlDriver();
        } else if (name.equals("firebird")) {
            ans = new FirebirdSqlDriver();
        } else if (name.equals("mysql")) {
            ans = new MySqlSqlDriver();
        } else if (name.equals("daffodil")) {
            ans = new DaffodilSqlDriver();
        } else if (name.equals("cache")) {
            ans = new CacheSqlDriver();

        } else {
            throw BindingSupportImpl.getInstance().runtime(
                    "Unknown db: " + name);
        }
        if (jdbcDriver != null) {
            ans.customizeForDriver(jdbcDriver);
        }
        return ans;
    }

    /**
     * Try and guess an appropriate SqlDriver name from a JDBC URL. Returns
     * null if no match.
     */
    public static String getNameFromURL(String url) {
        url = url.toLowerCase();
        int i = url.indexOf(':');
        if (i < 0) return null;
        int j = ++i + 1;
        int n = url.length();
        for (; j < n; j++) {
            char c = url.charAt(j);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
        }
        String key = url.substring(i, j);
        String[] a = new String[]{
            "cache", "cache",
            "db2", "db2",
            "firebirdsql", "firebird",
            "hsqldb", "hypersonic",
            "informix", "informix",
            "interbase", "interbase",
            "microsoft", "mssql",
            "mysql", "mysql",
            "oracle", "oracle",
            "pointbase", "pointbase",
            "postgresql", "postgres",
            "sapdb", "sapdb",
            "sybase", "sybase",
        };
        for (i = 0; i < a.length; i += 2) {
            if (a[i].equals(key)) return a[i + 1];
        }
        return null;
    }

    /**
     * Try and guess an appropriate SqlDriver name from a JDBC URL. Returns
     * null if no match.
     */
    public static String getDriverFromURL(String url) {
        if (url == null) return null;
        url = url.toLowerCase();
        int i = url.indexOf(':');
        if (i < 0) return null;
        int j = ++i + 1;
        int n = url.length();
        for (; j < n; j++) {
            char c = url.charAt(j);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
        }
        String key = url.substring(i, j);
        String[] a = new String[]{
            "cache", "com.intersys.jdbc.CacheDriver",
            "db2", "com.ibm.db2.jcc.DB2Driver",
            "firebirdsql", "org.firebirdsql.jdbc.FBDriver",
            "hsqldb", "org.hsqldb.jdbcDriver",
            "informix", "com.informix.jdbc.IfxDriver",
            "interbase", "interbase.interclient.Driver",
            "microsoft", "com.microsoft.jdbc.sqlserver.SQLServerDriver",
            "mysql", "com.mysql.jdbc.Driver",
            "oracle", "oracle.jdbc.driver.OracleDriver",
            "pointbase", "com.pointbase.jdbc.jdbcUniversalDriver",
            "postgresql", "org.postgresql.Driver",
            "sapdb", "com.sap.dbtech.jdbc.DriverSapDB",
            "sybase", "com.sybase.jdbc2.jdbc.SybDriver",
        };
        for (i = 0; i < a.length; i += 2) {
            if (a[i].equals(key)) return a[i + 1];
        }
        return null;
    }

    /**
     * Load a JDBC driver class and create an instance of it. The driver
     * class is unregistered if possible.
     */
    public static Driver createJdbcDriver(String name, ClassLoader cl) {
        Class cls = null;
        try {
            cls = ClassHelper.get().classForName(name, true, cl);
        } catch (ClassNotFoundException e) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "JDBC Driver class '" +
                    name + "' is not available in the classpath");
        }
        Driver driver = null;
        try {
			java.lang.Object drv = cls.newInstance();
            driver = (Driver)drv;
        } catch (Exception e) {
            BindingSupportImpl.getInstance().runtime("Unable to create " +
                    "instance of JDBC Driver class '" + name + "': " + e, e);
        }
        try {
            DriverManager.deregisterDriver(driver);
        } catch (Exception x) {
            // ignore
        }
        return driver;
    }

    /**
     * Get the default type mappings for this driver. These map JDBC type
     * codes from java.sql.Types to column properties.
     *
     * @see #getTypeMapping
     */
    public final JdbcTypeMapping[] getTypeMappings() {
        int n = JDBC_TYPES.length;
        JdbcTypeMapping[] a = new JdbcTypeMapping[n];
        String database = getName();
        for (int i = 0; i < n; i++) {
            int jdbcType = JDBC_TYPES[i];
            JdbcTypeMapping m = getTypeMapping(jdbcType);
            if (m != null) {
                m.setDatabase(database);
                m.setJdbcType(jdbcType);
            }
            a[i] = m;
        }
        return a;
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
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return new JdbcTypeMapping(JdbcTypes.toString(jdbcType),
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE, null);
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.BLOB:
            case Types.CLOB:
            case Types.LONGVARCHAR:
            case Types.LONGVARBINARY:
                return new JdbcTypeMapping(JdbcTypes.toString(jdbcType),
                        0, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.FALSE,
                        null);
            case Types.DECIMAL:

            case Types.NUMERIC:
                return new JdbcTypeMapping(JdbcTypes.toString(jdbcType),
                        20, 10, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        null);
            case Types.CHAR:
            case Types.VARCHAR:
                return new JdbcTypeMapping(JdbcTypes.toString(jdbcType),
                        255, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        null);
            case Types.BINARY:
            case Types.VARBINARY:
                return new JdbcTypeMapping(JdbcTypes.toString(jdbcType),
                        255, 0, JdbcTypeMapping.TRUE, JdbcTypeMapping.TRUE,
                        bytesConverterFactory);
        }
        throw BindingSupportImpl.getInstance().internal(
                "Invalid type in getTypeMapping: " + jdbcType);
    }

    /**
     * Get the default field mappings for this driver. These map java classes
     * to column properties. Subclasses should override this, call super() and
     * replace mappings as needed.
     */
    public HashMap getJavaTypeMappings() {
        HashMap ans = new HashMap(61);

        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Boolean.TYPE/*RIGHTPAR*/, Types.BIT, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Boolean.class/*RIGHTPAR*/, Types.BIT, true));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Byte.TYPE/*RIGHTPAR*/, Types.TINYINT, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Byte.class/*RIGHTPAR*/, Types.TINYINT, true));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Short.TYPE/*RIGHTPAR*/, Types.SMALLINT, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Short.class/*RIGHTPAR*/, Types.SMALLINT, true));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Integer.TYPE/*RIGHTPAR*/, Types.INTEGER, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Integer.class/*RIGHTPAR*/, Types.INTEGER, true));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Character.TYPE/*RIGHTPAR*/, Types.CHAR, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Character.class/*RIGHTPAR*/, Types.CHAR, true));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Long.TYPE/*RIGHTPAR*/, Types.BIGINT, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Long.class/*RIGHTPAR*/, Types.BIGINT, true));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Float.TYPE/*RIGHTPAR*/, Types.REAL, false, false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Float.class/*RIGHTPAR*/, Types.REAL, true, false));
        add(ans,
                new JdbcJavaTypeMapping(/*CHFC*/Double.TYPE/*RIGHTPAR*/, Types.DOUBLE, false,
                        false));
        add(ans,
                new JdbcJavaTypeMapping(/*CHFC*/Double.class/*RIGHTPAR*/, Types.DOUBLE, true,
                        false));

        add(ans, new JdbcJavaTypeMapping(/*CHFC*/String.class/*RIGHTPAR*/, Types.VARCHAR, true));
        add(ans,
                new JdbcJavaTypeMapping(/*CHFC*/Date.class/*RIGHTPAR*/, Types.TIMESTAMP, true,
                        false));
        add(ans,
                new JdbcJavaTypeMapping(/*CHFC*/BigDecimal.class/*RIGHTPAR*/, Types.NUMERIC, true));

        add(ans, new JdbcJavaTypeMapping(/*CHFC*/BigInteger.class/*RIGHTPAR*/, Types.NUMERIC, 20, 0,
                JdbcJavaTypeMapping.TRUE, null));




        JdbcConverterFactory f = new LocaleConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Locale.class/*RIGHTPAR*/, Types.CHAR, 6, 0,
                JdbcJavaTypeMapping.TRUE, f));

        f = new CharConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Character.class/*RIGHTPAR*/, Types.CHAR, 1, 0,
                JdbcJavaTypeMapping.TRUE, f));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Character.TYPE/*RIGHTPAR*/, Types.CHAR, 1, 0,
                JdbcJavaTypeMapping.FALSE, f));


        // primitive array[] mappings
        f = new ByteArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/byte[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new ShortArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/short[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new IntArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/int[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new LongArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/long[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new BooleanArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/boolean[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new CharArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/char[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new FloatArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/float[].class/*RIGHTPAR*/, Types.BLOB, f));
        f = new DoubleArrayConverter.Factory();
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/double[].class/*RIGHTPAR*/, Types.BLOB, f));




        // extended types
        add(ans,
                new JdbcJavaTypeMapping(/*CHFC*/File.class/*RIGHTPAR*/, Types.VARCHAR, -1, 0,
                        JdbcJavaTypeMapping.NOT_SET,
                        new FileConverter.Factory(), false));
        add(ans,
                new JdbcJavaTypeMapping(/*CHFC*/URL.class/*RIGHTPAR*/, Types.VARCHAR, -1, 0,
                        JdbcJavaTypeMapping.NOT_SET,
                        new URLConverter.Factory(), false));
        add(ans, new JdbcJavaTypeMapping(/*CHFC*/Timestamp.class/*RIGHTPAR*/, Types.TIMESTAMP,
                new TimestampConverter.Factory(), false));

        return ans;
    }

    /**
     * Add m to ans using its javaType as the key.
     */
    protected void add(HashMap ans, JdbcJavaTypeMapping m) {
        m.setDatabase(getName());
        ans.put(m.getJavaType(), m);
    }



    /**
     * Perform any driver specific customization. This can be used to control
     * functionality depending on the version of JDBC driver in use etc.
     */
    public void customizeForDriver(Driver jdbcDriver) {
    }

    /**
     * Does this store do anything in {@link #customizeForServer(java.sql.Connection)}.
     * This avoids allocating a connection if it is not required which sorts
     * out a problem we have with Torpedo.
     */
    public boolean isCustomizeForServerRequired() {
        return false;
    }

    /**
     * Perform any specific configuration appropriate for the database server
     * in use. If any SQL is done on con call con.commit() before returning.
     */
    public void customizeForServer(Connection con) throws SQLException {
    }

    /**
     * Create a default name generator instance for JdbcStore's using this
     * driver.
     */
    public JdbcNameGenerator createJdbcNameGenerator() {
        return createDefaultJdbcNameGenerator();
    }

    protected DefaultJdbcNameGenerator createDefaultJdbcNameGenerator() {
        DefaultJdbcNameGenerator ans = new DefaultJdbcNameGenerator();
        ans.setDatabaseType(getName());
        return ans;
    }

    /**
     * Some drivers require a call to clear the batches.
     *
     * @return
     */
    public boolean isClearBatchRequired() {
        return false;
    }

    /**
     * Does the JDBC driver support statement batching for inserts?
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
     * Can batching be used if the statement contains a column with the
     * given JDBC type?
     */
    public boolean isBatchingSupportedForJdbcType(int jdbcType) {
        return true;
    }

    /**
     * Does the JDBC driver support scrollable result sets?
     */
    public boolean isScrollableResultSetSupported() {
        return false;
    }

    /**
     * Does the JDBC driver support Statement.setFetchSize()?
     */
    public boolean isFetchSizeSupported() {
        return true;
    }

    /**
     * Should PreparedStatement batching be used for this database and
     * JDBC driver?
     */
    public boolean isPreparedStatementPoolingOK() {
        return true;
    }

    /**
     * How many PreparedStatement's should the pool be limited to by default
     * (0 for unlimited) ?
     */
    public int getDefaultPsCacheMax() {
        return 0;
    }

    /**
     * Does this driver use the ANSI join syntax (i.e. the join clauses appear
     * in the from list e.g. postgres)?
     */
    public boolean isAnsiJoinSyntax() {
        return false;
    }

    /**
     * May the ON clauses for joins in a subquery reference columns from the
     * enclosing query? DB2 does not allow this.
     */
    public boolean isSubQueryJoinMayUseOuterQueryCols() {
        return true;
    }

    /**
     * Is null a valid value for a column with a foreign key constraint?
     */
    public boolean isNullForeignKeyOk() {
        return false;
    }

    /**
     * Must columns used in an order by statement appear in the select list?
     */
    public boolean isPutOrderColsInSelect() {
        return false;
    }

    /**
     * Should indexes be used for columns in the order by list that are
     * also in the select list? This is used for databases that will not
     * order by a column that is duplicated in the select list (e.g. Oracle).
     */
    public boolean isUseIndexesForOrderCols() {
        return false;
    }

    public String getAliasPrepend() {
        return " ";
    }

    /**
     * Does the LIKE operator only support literal string and column
     * arguments (e.g. Informix)?
     */
    public boolean isLikeStupid() {
        return false;
    }

    /**
     * What is the maximum number of parameters allowed for the IN (?, .. ?)
     * operator?
     */
    public int getMaxInOperands() {
        return 10;
    }

    /**
     * Is it ok to convert simple 'exists (select ...)' clauses under an
     * 'or' into outer joins?
     */
    public boolean isOptimizeExistsUnderOrToOuterJoin() {
        return true;
    }

    /**
     * Must 'exists (select ...)' clauses be converted into a join and
     * distinct be added to the select (e.g. MySQL) ?
     */
    public boolean isConvertExistsToDistinctJoin() {
        return false;
    }

    public boolean isConvertExistsToJoins(int type) {
        switch (type) {
            case SqlExp.NO:
                return false;
            case SqlExp.YES:
                return true;
            case SqlExp.YES_DISTINCT:
            case SqlExp.YES_DISTINCT_NOT:
                return false;
            default:
                throw BindingSupportImpl.getInstance().internal(
                        "Unknown type '" + type + "'");
        }
    }

    /**
     * Must some expressions (+, -, string concat) be wrapped in brackets?
     */
    public boolean isExtraParens() {
        return false;
    }

    /**
     * Can the tx isolation level be set on this database?
     */
    public boolean isSetTransactionIsolationLevelSupported() {
        return true;
    }

    /**
     * Does this database support autoincrement or serial columns?
     */
    public boolean isAutoIncSupported() {
        return false;
    }

    /**
     * Does this database support comments embedded in SQL?
     */
    public boolean isCommentSupported() {
        return true;
    }

    /**
     * Generate SQL to create the database schema for the supplied tables.
     * If con is not null then it must have autoCommit true.
     */
    public void generateDDL(ArrayList tables, Connection con, PrintWriter out,
            boolean comments) {
        StatementWithLastSQL stat = null;
        try {
            if (con != null) {
                stat = new StatementWithLastSQL(con.createStatement());
            }
            // generate the 'create table' statements
            int n = tables.size();
            for (int i = 0; i < n; i++) {
                JdbcTable t = (JdbcTable)tables.get(i);
                generateCreateTable(t, stat, out, comments);
            }
            // generate the 'create index' statements
            for (int i = 0; i < n; i++) {
                JdbcTable t = (JdbcTable)tables.get(i);
                generateCreateIndexes(t, stat, out, comments);
            }
            // generate the 'add constraint' statements
            for (int i = 0; i < n; i++) {
                JdbcTable t = (JdbcTable)tables.get(i);
                generateConstraints(t, stat, out, comments);
            }
        } catch (SQLException x) {
            String msg;
            if (stat == null) {
                msg = x.toString();
            } else {
                msg = x + "\nMost recent SQL:\n" + stat.getLastSQL();
            }
            throw mapException(x, msg, false);
        } finally {
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
        appendIndexesInCreateTable(t, s);
        s.append("\n)");
        appendTableType(t, s);
        String sql = s.toString();
        if (out != null) {
            print(out, sql);
        }
        if (stat != null) {
            stat.execute(sql);
        }
    }

    /**
     * Format a comment.
     */
    public String comment(String msg) {
        return "-- " + msg;
    }

    /**
     * Hook for drivers that have to append a table type to the create table
     * statement (e.g. MySQL).
     */
    protected void appendTableType(JdbcTable t, CharBuf s) {
    }

    /**
     * Hook for drivers that must create indexes in the create table
     * statement (e.g. MySQL).
     */
    protected void appendIndexesInCreateTable(JdbcTable t, CharBuf s) {
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
     * Append the part of a create table statement for a column.
     */
    protected void appendCreateColumn(JdbcTable t, JdbcColumn c,
            CharBuf s, boolean comments) {
        int si = s.size();
        s.append(c.name);
        s.append(' ');
        appendColumnType(c, s);
        if (c.autoinc) appendCreateColumnAutoInc(t, c, s);
        appendCreateColumnNulls(t, c, s);
        s.append(',');
        if (comments && isCommentSupported() && c.comment != null) {
            s.append(' ');
            si += COMMENT_COL;
            for (; s.size() < si; s.append(' ')) ;
            s.append(comment(c.comment));
        }
    }

    /**
     * Append the column type part of a create table statement for a column.
     */
    protected void appendColumnType(JdbcColumn c, CharBuf s) {
        appendColumnType(c, s, useZeroScale(c));
    }

    protected boolean useZeroScale(JdbcColumn c) {
        return false;
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
        if (c.length != 0 || c.scale != 0) {
            s.append('(');
            s.append(c.length);
            if (c.scale != 0 || useZeroScale) {
                s.append(',');
                s.append(c.scale);
            }
            s.append(')');
        }
    }

    /**
     * Get the database specific name for the jdbcType.
     *
     * @see Types
     */
    protected String getTypeName(int jdbcType) {
        return JdbcTypes.toString(jdbcType);
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
     * Append the column auto increment part of a create table statement for a
     * column.
     */
    protected void appendCreateColumnAutoInc(JdbcTable t, JdbcColumn c,
            CharBuf s) {
    }

    /**
     * Add the primary key constraint part of a create table statement to s.
     */
    protected void appendPrimaryKeyConstraint(JdbcTable t, CharBuf s) {
        s.append("PRIMARY KEY (");
        appendColumnNameList(t.pk, s);
        s.append(") CONSTRAINT ");
        s.append(t.pkConstraintName);
    }

    /**
     * Append a comma separated list of column names to s.
     */
    protected void appendColumnNameList(JdbcColumn[] cols, CharBuf s) {
        int colslen = cols.length;
        for (int i = 0; i < colslen; i++) {
            if (i > 0) s.append(", ");
            s.append(cols[i].name);
        }
    }

    /**
     * Generate the 'create index' statements for t.
     */
    public void generateCreateIndexes(JdbcTable t, Statement stat,
            PrintWriter out, boolean comments) throws SQLException {
        JdbcIndex[] a = t.indexes;
        if (a == null) return;
        CharBuf s = new CharBuf();
        for (int i = 0; i < a.length; i++) {
            JdbcIndex idx = a[i];
            s.clear();
            appendCreateIndex(s, t, idx, comments);
            if (s.size() > 0) {
                String sql = s.toString();
                if (out != null) print(out, sql);
                if (stat != null) stat.execute(sql);
            }
        }
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
        s.append("CREATE ");
        if (idx.unique) s.append("UNIQUE ");
        //if (idx.clustered) s.append("clustered ");
        s.append("INDEX ");
        s.append(idx.name);
        s.append(" ON ");
        s.append(t.name);
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
        JdbcConstraint[] cons = t.constraints;
        if (cons == null) return;
        CharBuf s = new CharBuf();
        for (int i = 0; i < cons.length; i++) {
            JdbcConstraint c = cons[i];
            s.clear();
            appendRefConstraint(s, c);
            String sql = s.toString();
            if (!sql.equals("")) {
                if (out != null) print(out, sql);
                if (stat != null) stat.execute(sql);
            }
        }
    }

    /**
     * Append an 'add constraint' statement for c.
     */
    protected void appendRefConstraint(CharBuf s, JdbcConstraint c) {
        s.append("ALTER TABLE ");
        s.append(c.src.name);
        s.append(" ADD CONSTRAINT (FOREIGN KEY (");
        appendColumnNameList(c.srcCols, s);
        s.append(") REFERENCES ");
        s.append(c.dest.name);
        s.append('(');
        appendColumnNameList(c.dest.pk, s);
        s.append(") CONSTRAINT ");
        s.append(c.name);
        s.append(')');
    }

    /**
     * Get the names of all tables in the database con is connected to.
     */
    public ArrayList getTableNames(Connection con) throws SQLException {
        ResultSet rs = null;
        try {
            rs = con.getMetaData().getTables(null, getSchema(con), null, null);
            ArrayList a = new ArrayList();
            for (; rs.next();) {
                if (rs.getString(4).trim().equals("TABLE")) {
                    a.add(rs.getString(3).trim());
                }
            }
            return a;
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


//    /**
//     * Get the names of all tables in the database con is connected to.
//     */
//    public ArrayList getTableNames(Connection con) throws SQLException {
//        ResultSet rs = null;
//        try {
////            getSchema(con);
//
//
//            rs = con.getMetaData().getTables(null, getSchema(con), null, null);
//            ArrayList a = new ArrayList();
//
//            String name = null;
//            String type = null;
//            String schema = null;
//            for (; rs.next();) {
//                type = rs.getString(4).trim();
//                name = rs.getString(3).trim();
//                schema = rs.getString(2).trim();
//                if (name.lastIndexOf('/') == -1 && name.lastIndexOf('$') == -1){
//
//                    System.out.println("SELECT * FROM "+
//                            (schema == null ? "": schema+".")+
//                            name +"; --"+ type);
//                }
//                if (type.equals("TABLE")) {
//                    a.add(name);
//                }
//            }
//            return a;
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException x) {
//                    // ignore
//                }
//            }
//        }
//    }

    /**
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    public void dropTable(Connection con, String table)
            throws SQLException {
        StatementWithLastSQL stat = null;
        try {
            stat = new StatementWithLastSQL(con.createStatement());
            dropTable(con, table, stat);
        } catch (SQLException x) {
            String msg;
            if (stat == null) {
                msg = x.toString();
            } else {
                msg = x + "\nMost recent SQL:\n" + stat.getLastSQL();
            }
            throw mapException(x, msg, false);
        } finally {
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
     * Drop the table and all its constraints etc. This must remove
     * constraints to this table from other tables so it can be dropped.
     */
    protected void dropTable(Connection con, String table, Statement stat)
            throws SQLException {
        stat.execute("DROP TABLE " + table);
    }

    /**
     * Append a replacable parameter part of a where clause for the column.
     * This gives the driver a chance to embed type conversions and so on
     * for types not handled well by the JDBC driver (e.g. BigDecimals and
     * the postgres JDBC driver).
     */
    public void appendWhereParam(CharBuf s, JdbcColumn c) {
        s.append("?");
    }

    /**
     * Get the string form of a binary operator.
     *
     * @see BinaryOpExp
     */
    public String getSqlBinaryOp(int op) {
        switch (op) {
            case BinaryOpExp.EQUAL:
                return "=";
            case BinaryOpExp.NOT_EQUAL:
                return "<>";
            case BinaryOpExp.GT:
                return ">";
            case BinaryOpExp.LT:
                return "<";
            case BinaryOpExp.GE:
                return ">=";
            case BinaryOpExp.LE:
                return "<=";
            case BinaryOpExp.LIKE:
                return "LIKE";
            case BinaryOpExp.CONCAT:
                return "||";
            case BinaryOpExp.PLUS:
                return "+";
            case BinaryOpExp.MINUS:
                return "-";
            case BinaryOpExp.TIMES:
                return "*";
            case BinaryOpExp.DIVIDE:
                return "/";
        }
        throw BindingSupportImpl.getInstance().internal("Unknown op: " + op);
    }

    /**
     * Append the value of the literal to s.
     *
     * @see LiteralExp
     */
    public void appendSqlLiteral(int type, String value, CharBuf s) {
        if (type == LiteralExp.TYPE_STRING) {
            s.append('\'');
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char c = value.charAt(i);
                if (c == '\'') s.append('\'');
                s.append(c);
            }
            s.append('\'');
        } else {
            s.append(value);
        }
    }

    /**
     * Append the name of the column to s. If col is null then append '*'.
     *
     * @see ColumnExp
     */
    public void appendSqlColumn(JdbcColumn col, String alias, CharBuf s) {
        if (alias != null) {
            s.append(alias);
            s.append('.');
        }
        if (col == null) {
            s.append('*');
        } else {
            s.append(col.name);
        }
    }

    /**
     * Append the from list entry for a table.
     */
    public void appendSqlFrom(JdbcTable table, String alias,
            CharBuf s) {
        s.append(table.name);
        if (alias != null) {
            s.append(' ');
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
        s.append(',');
        s.append(' ');
        if (outer) s.append("OUTER ");
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
    }

    /**
     * Get a String for a replacable parameter. This gives the driver a
     * chance to embed type conversions and so on for types not handled well
     * by the JDBC driver (e.g. BigDecimals and the postgres JDBC driver).
     * <p/>
     * If you override this method then you must also override {@link #getSqlParamStringChars(int)}.
     */
    public String getSqlParamString(int jdbcType) {
        return "?";
    }

    /**
     * Return a shared char[] of the sqlParamString. This array may not be modified.
     * It is used as a stamp when creating a in list.
     *
     * @param jdbcType
     * @return
     */
    public char[] getSqlParamStringChars(int jdbcType) {
        return DEFAULT_PARAM_CHARS;
    }

    /**
     * Get the name of a function that accepts one argument.
     *
     * @see UnaryFunctionExp
     */
    public String getSqlUnaryFunctionName(int func) {
        switch (func) {
            case UnaryFunctionExp.FUNC_TO_LOWER_CASE:
                return "lower";
        }
        throw BindingSupportImpl.getInstance().internal(
                "Unknown func: " + func);
    }

    /**
     * Get default SQL to test a connection or null if none available. This
     * must be a query that returns at least one row.
     */
    public String getConnectionValidateSQL() {
        return null;
    }

    /**
     * Get default SQL used to init a connection or null if none required.
     */
    public String getConnectionInitSQL() {
        return null;
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
        return null;
    }

    /**
     * Cleanup anything done in prepareForGetQueryPlan. Example: On Sybase this
     * will do a 'set showplan 0' and 'set noexec 0'.
     *
     * @see #prepareForGetQueryPlan
     * @see #getQueryPlan
     */
    public void cleanupForGetQueryPlan(Connection con) {
    }

    /**
     * Get extra SQL to be appended to the insert statement for retrieving
     * the value of an autoinc column after insert. Return null if none
     * is required or a separate query is run.
     *
     * @see #getAutoIncColumnValue(JdbcTable, Connection, Statement)
     */
    public String getAutoIncPostInsertSQLSuffix(JdbcTable classTable) {
        return null;
    }

    /**
     * Retrieve the value of the autoinc or serial column for a row just
     * inserted using stat on con.
     *
     * @see #getAutoIncPostInsertSQLSuffix(JdbcTable)
     */
    public Object getAutoIncColumnValue(JdbcTable classTable,
            Connection con, Statement stat) throws SQLException {
        throw BindingSupportImpl.getInstance().internal(
                "autoincrement or identity columns " +
                "not supported for '" + getName() + "' table '" + classTable.name + "'");
    }

    /**
     * Enable or disable identity insert for the given table if this is
     * required to insert a value into an identity column.
     */
    public void enableIdentityInsert(Connection con, String table, boolean on)
            throws SQLException {
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
        return false;
    }

    /**
     * Can 'SELECT FOR UPDATE' be used with a DISTINCT?
     */
    public boolean isSelectForUpdateWithDistinctOk() {
        return true;
    }

    public boolean isSelectForUpdateWithAggregateOk() {
        return false;
    }

    /*########################################*/
    /*# These are the schema migration stuff #*/
    /*########################################*/

    /**
     * Append a column that needs to be added.
     */
    protected void appendAddNewColumn(JdbcTable t, JdbcColumn c,
            CharBuf s, boolean comments) {
        if (comments && isCommentSupported() && c.comment != null) {
            s.append(comment("add column for field " + c.comment));
        }

        s.append("\n");
        s.append("ALTER TABLE ");
        s.append(t.name);
        s.append(" ADD ");
        s.append(c.name);
        s.append(' ');
        appendColumnType(c, s);
        if (c.autoinc) {
            appendCreateColumnAutoInc(t, c, s);
        }
        if (c.nulls) {
            appendCreateColumnNulls(t, c, s);
            s.append(";\n");
        } else {
            s.append(";\n");
            s.append("UPDATE ");
            s.append(t.name);
            s.append(" SET ");
            s.append(c.name);
            s.append(" = ");
            s.append(getDefaultForType(c));
            s.append(";\n");

            s.append("ALTER TABLE ");
            s.append(t.name);
            s.append(" MODIFY ");
            s.append(c.name);
            s.append(' ');
            appendColumnType(c, s);
            if (c.autoinc) {
                appendCreateColumnAutoInc(t, c, s);
            }
            appendCreateColumnNulls(t, c, s);
            s.append(";\n");
        }
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
        s.append(" MODIFY ");
        s.append(c.name);
        s.append(' ');
        appendColumnType(c, s);
        if (c.autoinc) {
            appendCreateColumnAutoInc(t, c, s);
        }
        appendCreateColumnNulls(t, c, s);
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
        s.append("ALTER TABLE ");
        s.append(tableDiff.getOurTable().name);
        s.append(" DROP COLUMN ");
        s.append(c.name);

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

    public String getRunCommand() {
        return ";\n";
    }

    /**
     * Make sure the database tables and columns exist. This is used as a
     * quick check when the server starts.
     */
    public boolean checkDDLForStartup(ArrayList tables, Connection con,
            PrintWriter out, PrintWriter fix, ControlParams params)
            throws SQLException {
        Statement stat = null;
        boolean allIsWell = true;
        try {
            con.rollback();
            con.setAutoCommit(true);
            stat = con.createStatement();
            int n = tables.size();
            for (int i = 0; i < n; i++) {
                JdbcTable t = (JdbcTable)tables.get(i);
                if (!checkTable(t, stat, out)) allIsWell = false;
            }
        } finally {
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            con.setAutoCommit(false);
        }
        if (!allIsWell) {
            fix.println(comment("NOT IMPLEMENTED"));
        }
        return allIsWell;
    }

    /**
     * Check that the columns of t match those in the database schema.
     */
    protected boolean checkTable(JdbcTable t, Statement stat, PrintWriter out) {
        CharBuf s = new CharBuf();
        s.append("select ");
        int nc = t.cols.length;
        for (int i = 0; i < nc; i++) {
            s.append(t.cols[i].name);
            if (i != (nc - 1)) {
                s.append(", ");
            } else {
                s.append(" from ");
            }
        }
        s.append(t.name);
        s.append(" where 1 = 2");
        String sql = s.toString();

        try {
            stat.executeQuery(sql);
            return true;
        } catch (SQLException x) {
            printError(out, t.name);
            // we have a error, now we check if the table exist
            boolean tableExist = false;
            try {
                s.clear();
                s.append("select * from ");
                s.append(t.name);
                s.append(" where 1 = 2");
                stat.executeQuery(s.toString());
                tableExist = true;
            } catch (SQLException tablex) {
                printErrorMsg(out, "Table '" + t.name + "' does not exist.");
            }
            if (tableExist) {
                // the table does exist, now we find the what column does not exist
                s.clear();
                s.append(" from ");
                s.append(t.name);
                s.append(" where 1 = 2");
                String from = s.toString();
                String column = null;
                for (int i = 0; i < nc; i++) {
                    column = t.cols[i].name;
                    s.clear();
                    s.append("select ");
                    s.append(column);
                    s.append(from);
                    try {
                        stat.executeQuery(s.toString());
                    } catch (SQLException columnx) {
                        printErrorMsg(out,
                                "Column '" + column + "' does not exist.");
                    }
                }
            }
            return false;
        }
    }

    private static void printError(PrintWriter out, String tableName) {
        out.print("\nTable ");
        out.print(tableName);
        out.println(" : FAIL");
    }

    private static void printErrorMsg(PrintWriter out, String error) {
        out.print("    ");
        out.println(error);
    }

    /**
     * Get all the database tables and columns that is not system tables
     * and that is filled with what field it belongs to.
     */
    public HashMap getDatabaseMetaData(ArrayList tables, Connection con)
            throws SQLException {
        HashMap dbMap;
        ControlParams params = new ControlParams();
        params.setColumnsOnly(true);
        try {
            customizeForServer(con);
            con.rollback();
            con.setAutoCommit(true);
            dbMap = getDBSchema(con, params);
            setAllTableAndViewNames(con);
        } finally {
            con.setAutoCommit(false);
        }

        fillDatabaseMetaData(tables, dbMap);
        return dbMap;
    }

    /**
     * Fill the db classes with mapping info
     */
    public void fillDatabaseMetaData(ArrayList tables, HashMap dbMap) {
        int n = tables.size();
        for (int m = 0; m < n; m++) {
            JdbcTable ourTable = (JdbcTable)tables.get(m);
            JdbcTable dbTable = (JdbcTable)dbMap.get(
                    ourTable.name.toLowerCase());
            if (dbTable != null) {
                dbTable.comment = ourTable.comment;
                if (ourTable.cols != null) {
                    for (int i = 0; i < ourTable.cols.length; i++) {
                        JdbcColumn ourCol = ourTable.cols[i];
                        // check if our column is in there
                        JdbcColumn dbCol = null;
                        if (dbTable.cols != null) {
                            for (int j = 0; j < dbTable.cols.length; j++) {
                                JdbcColumn col = dbTable.cols[j];
                                if (ourCol.name.equalsIgnoreCase(col.name)) {
                                    dbCol = col;
                                    dbCol.comment = ourCol.comment;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean checkDDL(ArrayList tables, Connection con,
            PrintWriter errors, PrintWriter fix, ControlParams params)
            throws SQLException {
        HashMap dbMap;
        try {
            customizeForServer(con);
            con.rollback();
            con.setAutoCommit(true);
            dbMap = getDBSchema(con, params);
            setAllTableAndViewNames(con);
        } finally {
            con.setAutoCommit(false);
        }
        HashMap nameMap = new HashMap();
        ArrayList colChangeList = new ArrayList();
        try {
            HashMap dupMap = new HashMap();

            for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
                JdbcTable ourTable = (JdbcTable)iterator.next();
                JdbcTable dbTable = (JdbcTable)dbMap.get(
                        ourTable.name.toLowerCase());

                if (dbTable != null) {
                    nameMap.put(ourTable.name, ourTable);
                    ourTable.name = dbTable.name;
                }
                dupMap.clear();
                List colList = ourTable.getColumnList();
                for (Iterator iter = colList.iterator(); iter.hasNext();) {
                    JdbcColumn col = (JdbcColumn) iter.next();
                    String name = col.name.toLowerCase();
                    if (dupMap.put(name, name) != null) {
                        col.setShared(true);
                        colChangeList.add(col);
                    }
                }
            }
            dupMap = null;
            ArrayList diffList = checkAllTables(tables, dbMap, params);

            if (diffList.isEmpty()) {
                allTableList = null;
                return true;
            } else {
                DiffUtil.reportErrors(diffList, errors);
                reportFixes(diffList, fix);
                allTableList = null;
                return false;
            }
        } finally {
            Set set = nameMap.keySet();
            for (Iterator iter = set.iterator(); iter.hasNext();) {
                String name = (String)iter.next();
                JdbcTable table = (JdbcTable)nameMap.get(name);
                table.name = name;
            }
            for (Iterator iter = colChangeList.iterator(); iter.hasNext();) {
                JdbcColumn jdbcColumn = (JdbcColumn) iter.next();
                jdbcColumn.setShared(false);
            }
        }
    }

    protected String getCatalog(Connection con) throws SQLException {
        return null;
    }

    protected String getSchema(Connection con) {
        return null;
    }

    protected boolean isValidSchemaTable(String tableName) {
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
            col.nulls = "YES".equals(rsColumn.getString("IS_NULLABLE"));

//            if (tableName.equalsIgnoreCase("custom_types")){  //sqlType longtext
//                System.out.println(col.name);
//                System.out.println(col.sqlType);
//                System.out.println(col.jdbcType);
//                System.out.println(col.length);
//                System.out.println(col.scale);
//                System.out.println("---------------------");
//            }

            if (col.jdbcType == java.sql.Types.OTHER &&
                    col.sqlType.equals("longtext")) {
                col.jdbcType = java.sql.Types.CLOB;
            }

            if (col.jdbcType == 16) {
                col.jdbcType = java.sql.Types.BIT;
            }

            switch (col.jdbcType) {
                case java.sql.Types.BIT:
                case java.sql.Types.TINYINT:
                case java.sql.Types.SMALLINT:
                case java.sql.Types.INTEGER:
                case java.sql.Types.BIGINT:
                case java.sql.Types.LONGVARBINARY:
                case java.sql.Types.BLOB:
                case java.sql.Types.LONGVARCHAR:
                case java.sql.Types.CLOB:
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
                JdbcTable colJdbcTable = (JdbcTable)jdbcTableMap.get(tableName);
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
                        pkMap.put(rsPKs.getString("COLUMN_NAME"), null);
                        String pkName = rsPKs.getString("PK_NAME");
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

                        String indexName = rsIndex.getString("INDEX_NAME");
                        if (indexName != null
                                && !indexName.equals(
                                        jdbcTable.pkConstraintName)
                                && !indexName.startsWith("SYS_IDX_")) {
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
                                            "COLUMN_NAME");
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
                                        "COLUMN_NAME");
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
                        } catch (SQLException e) { }
                    }
                }

                // end of index ///////////////////////////////////////////////////////////////////////
            }
            if (params.isCheckConstraint()) {
                // now we do forign keys /////////////////////////////////////////////////////////////
                for (Iterator iterator = mainTableNames.iterator();
                     iterator.hasNext();) {
                    tableName = (String)iterator.next();
                    JdbcTable jdbcTable = (JdbcTable)jdbcTableMap.get(
                            tableName);
                    ResultSet rsFKs = null;
                    try {
                        rsFKs = meta.getImportedKeys(catalog, schema,
                                tableName);
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
                            constraint.dest = (JdbcTable)jdbcTableMap.get(
                                    rsFKs.getString("PKTABLE_NAME"));
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
            JdbcTable table = (JdbcTable)iterator.next();
            returnMap.put(table.name.toLowerCase(), table);
        }
        fixAllNames(returnMap);
        return returnMap;
    }

    boolean isDirectDropColumnSupported() {
        return true;
    }

    boolean isDirectAddColumnSupported(JdbcColumn ourCol) {
        return true;
    }

    boolean isDirectNullColumnChangesSupported() {
        return true;
    }

    boolean isDirectScaleColumnChangesSupported(JdbcColumn ourCol,
            JdbcColumn dbCol) {
        return true;
    }

    boolean isDirectLenghtColumnChangesSupported(JdbcColumn ourCol,
            JdbcColumn dbCol) {
        return true;
    }

    boolean isDirectTypeColumnChangesSupported(JdbcColumn ourCol,
            JdbcColumn dbCol) {
        return true;
    }

    boolean isDropConstraintsForDropTableSupported() {
        return true;
    }

    boolean isDropPrimaryKeySupported() {
        return true;
    }

    /**
     * Is this a sequence column from a List implementation that we are dropping to create a Set?
     */
    boolean isDropSequenceColumn(TableDiff tableDiff, JdbcColumn dropColumn) {
        JdbcTable ourTable = tableDiff.getOurTable();
        JdbcTable dbTable = tableDiff.getDbTable();
        // we have a seq if the old col was in the old pk
        // and the new pk includes another column
        if (ourTable.getColsForCreateTable().length == 2 && ourTable.getPkNames().length == 2) {
            try {
                if (dbTable.findPkColumn(dropColumn.name) != null) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * @param addColumn
     * @return
     */
    boolean isAddSequenceColumn(JdbcColumn addColumn) {
        if (addColumn.comment != null && addColumn.comment.equals(
                JdbcMetaDataBuilder.SEQUENCE_FIELDNAME)) {
            return true;
        } else {
            return false;
        }
    }

    private ArrayList checkAllTables(ArrayList tables, HashMap dbMap,
            ControlParams params) {
        ArrayList diffList = new ArrayList();
        int n = tables.size();
        for (int i = 0; i < n; i++) {
            JdbcTable ourTable = (JdbcTable)tables.get(i);
            TableDiff diff = DiffUtil.checkTable(this, ourTable,
                    (JdbcTable)dbMap.get(ourTable.name.toLowerCase()), params);
            if (diff != null) {
                diffList.add(diff);
            }
        }

        // if we have pks that we drop then we have to check if there are constraints that we
        // have to drop first that refrences this pk.
        ArrayList dropConsList = new ArrayList();
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            JdbcTable destTable = tableDiff.getDbTable();
            for (Iterator iterIndex = tableDiff.getPkDiffs().iterator();
                 iterIndex.hasNext();) {
                PKDiff pkDiff = (PKDiff)iterIndex.next();
                if (!pkDiff.isMissingPK()) {
                    for (Iterator mainTables = tables.iterator();
                         mainTables.hasNext();) {
                        JdbcTable ourJdbcTable = (JdbcTable)mainTables.next();
                        JdbcTable dbJdbcTable = (JdbcTable)dbMap.get(
                                ourJdbcTable.name.toLowerCase());
                        if (dbJdbcTable != null && dbJdbcTable.constraints != null) {
                            for (int i = 0;
                                 i < dbJdbcTable.constraints.length; i++) {
                                JdbcConstraint dbConstraint = dbJdbcTable.constraints[i];
                                if (dbConstraint.dest != null && destTable != null) {
                                    if (dbConstraint.dest.name.equalsIgnoreCase(
                                            destTable.name)) { // we found the constraint that we need to drop
                                        if (!dropConsList.contains(
                                                dbConstraint)) {
                                            pkDiff.setDropConstraintsRefs(
                                                    dbConstraint);
                                            dropConsList.add(dbConstraint);
                                            for (int j = 0;
                                                 j < ourJdbcTable.constraints.length;
                                                 j++) {
                                                JdbcConstraint ourConstraint = ourJdbcTable.constraints[j];
                                                if (ourConstraint.name.equalsIgnoreCase(
                                                        dbConstraint.name)) {
                                                    if (null == DiffUtil.checkConstraint(
                                                            ourConstraint,
                                                            dbConstraint,
                                                            params)) {
                                                        // we found the constraint in our schema that did not have a problem,
                                                        // so we add it again
                                                        pkDiff.setAddConstraintsRefs(
                                                                ourConstraint);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            for (Iterator iterator = tableDiff.getConstraintDiffs().iterator();
                 iterator.hasNext();) {
                ConstraintDiff diff = (ConstraintDiff)iterator.next();
                if (dropConsList.contains(diff.getDbConstraint())) {
                    diff.setDrop(false);  // don't drop this, it has already been dropped
                }
            }
        }

        // if we have tables that we are going to drop then we have to check if there are constraints that we
        // have to recreate that refrences this table.
        ArrayList dropTableList = new ArrayList();
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            JdbcTable ourTable = tableDiff.getOurTable();
            boolean direct = true;
            for (Iterator iterCol = tableDiff.getColDiffs().iterator();
                 iterCol.hasNext();) {
                ColumnDiff diff = (ColumnDiff)iterCol.next();
                if (diff.isExtraCol()) {
                    if (isDropSequenceColumn(tableDiff, diff.getDbCol())) {
                        direct = false;
                    } else if (!isDirectDropColumnSupported()) {
                        direct = false;
                    }
                }
                if (diff.isMissingCol()) {
                    if (isAddSequenceColumn(diff.getOurCol())) {
                        direct = false;
                    } else if (!isDirectAddColumnSupported(diff.getOurCol())) {
                        direct = false;
                    }
                }

                if (diff.isLenghtDiff()) {
                    if (!isDirectLenghtColumnChangesSupported(diff.getOurCol(),
                            diff.getDbCol())) {
                        direct = false;
                    }
                }
                if (diff.isNullDiff()) {
                    if (!isDirectNullColumnChangesSupported()) {
                        direct = false;
                    }
                }
                if (diff.isScaleDiff()) {
                    if (!isDirectScaleColumnChangesSupported(diff.getOurCol(),
                            diff.getDbCol())) {
                        direct = false;
                    }
                }
                if (diff.isTypeDiff()) {
                    if (!isDirectTypeColumnChangesSupported(diff.getOurCol(),
                            diff.getDbCol())) {
                        direct = false;
                    }
                }
            }
            if (!direct) {
                dropTableList.add(ourTable);
            }
        }

        for (Iterator iter = dropTableList.iterator(); iter.hasNext();) {
            JdbcTable jdbcTable = (JdbcTable)iter.next(); // this table will be droped
            // we must find all constraints that references this table and put them back
            for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
                JdbcTable table = (JdbcTable)iterator.next();
                if (!dropTableList.contains(table)) {
                    if (table.constraints != null) {
                        for (int i = 0; i < table.constraints.length; i++) {
                            JdbcConstraint constraint = table.constraints[i];
                            if (constraint.dest == jdbcTable) {// this constraint will be dropped

                                TableDiff tableDiff = null;
                                for (Iterator iterDiff = diffList.iterator();
                                     iterDiff.hasNext();) {
                                    TableDiff tempTableDiff = (TableDiff)iterDiff.next();
                                    if (tempTableDiff.getOurTable() == constraint.src) {
                                        tableDiff = tempTableDiff;
                                    }
                                }
                                if (tableDiff == null) {
                                    tableDiff = new TableDiff(constraint.src,
                                            null);
                                    ConstraintDiff diff = new ConstraintDiff(
                                            constraint, null);

                                    diff.setRecreate(true);
                                    diff.setDrop(false);
                                    tableDiff.getConstraintDiffs().add(diff);
                                    // there are no real errors, so we set it
                                    tableDiff.setHasRealErrors(false);
                                    diffList.add(tableDiff);
                                } else {
                                    ConstraintDiff ourConstDiff = null;
                                    for (Iterator iterConstraint = tableDiff.getConstraintDiffs().iterator();
                                         iterConstraint.hasNext();) {
                                        ConstraintDiff diff = (ConstraintDiff)iterConstraint.next();
                                        if (diff.getOurConstraint() == constraint) {
                                            diff.setRecreate(true);
                                            diff.setDrop(false);
                                            ourConstDiff = diff;
                                        }
                                    }

                                    if (ourConstDiff == null) {// we need to add it
                                        ourConstDiff = new ConstraintDiff(
                                                constraint, null);
                                        ourConstDiff.setRecreate(true);
                                        ourConstDiff.setDrop(false);
                                        tableDiff.getConstraintDiffs().add(
                                                ourConstDiff);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!isDropConstraintsForDropTableSupported()) {

            for (Iterator iter = dropTableList.iterator(); iter.hasNext();) {
                JdbcTable jdbcTable = (JdbcTable)iter.next(); // this table will be droped
                // we must find all constraints that references this table and drop them
                for (Iterator iterator = dbMap.keySet().iterator();
                     iterator.hasNext();) {
                    JdbcTable table = (JdbcTable)dbMap.get(
                            ((String)iterator.next()).toLowerCase());
                    boolean isGoingToBeDroped = false;
                    if (table != null) {
                        for (Iterator myiter = dropTableList.iterator();
                             myiter.hasNext();) {
                            JdbcTable tempJdbcTable = (JdbcTable)myiter.next();
                            if (tempJdbcTable.name.equalsIgnoreCase(table.name)) {
                                isGoingToBeDroped = true;
                            }
                        }
                    }

                    if (!isGoingToBeDroped) {
                        if (table.constraints != null) {
                            for (int i = 0; i < table.constraints.length; i++) {
                                JdbcConstraint constraint = table.constraints[i];
                                if (constraint.dest.name.equalsIgnoreCase(
                                        jdbcTable.name)) {// this constraint must be dropped
                                    TableDiff tableDiff = null;
                                    for (Iterator iterDiff = diffList.iterator();
                                         iterDiff.hasNext();) {
                                        TableDiff tempTableDiff = (TableDiff)iterDiff.next();
                                        if (tempTableDiff.getOurTable() == constraint.src) {
                                            tableDiff = tempTableDiff;
                                        }
                                    }
                                    if (tableDiff == null) {
                                        tableDiff = new TableDiff(null,
                                                constraint.src);
                                        ConstraintDiff diff = new ConstraintDiff(
                                                null, constraint);
                                        diff.setDrop(true);
                                        tableDiff.getConstraintDiffs().add(
                                                diff);
                                        // there are no real errors, so we set it
                                        tableDiff.setHasRealErrors(false);
                                        diffList.add(tableDiff);
                                    } else {
                                        ConstraintDiff dbConstDiff = null;
                                        for (Iterator iterConstraint = tableDiff.getConstraintDiffs().iterator();
                                             iterConstraint.hasNext();) {
                                            ConstraintDiff diff = (ConstraintDiff)iterConstraint.next();
                                            if (diff.getDbConstraint() == constraint) {
                                                diff.setDrop(true);
                                                dbConstDiff = diff;
                                            }
                                        }

                                        if (dbConstDiff == null) {// we need to add it
                                            dbConstDiff = new ConstraintDiff(
                                                    null, constraint);
                                            dbConstDiff.setDrop(true);
                                            tableDiff.getConstraintDiffs().add(
                                                    dbConstDiff);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return diffList;
    }

    protected void fixCoulumns(TableDiff tableDiff, PrintWriter out) {
        CharBuf buff = new CharBuf();
        ArrayList colList = tableDiff.getColDiffs();
        ArrayList pkList = tableDiff.getPkDiffs();
        boolean isMissingPK = false;  // flag, else we get multiple pk creations
        boolean otherPKProblems = false;
        for (Iterator iterator = pkList.iterator(); iterator.hasNext();) {
            PKDiff diff = (PKDiff)iterator.next();
            if (diff.isMissingPK()) {
                isMissingPK = true;
            } else if (diff.isMissingPKCol() || diff.isExtraPKCol()) {
                otherPKProblems = true;
                if (!diff.getDropConstraintsRefs().isEmpty()) {
                    for (Iterator iter = diff.getDropConstraintsRefs().iterator();
                         iter.hasNext();) {
                        JdbcConstraint constraint = (JdbcConstraint)iter.next();
                        buff.clear();
                        appendRefDropConstraint(buff, constraint, true);
                        String sql = buff.toString();
                        print(out, sql);
                    }
                }
            }
        }

        if (otherPKProblems && isDropPrimaryKeySupported()) {
            buff.clear();
            dropPrimaryKeyConstraint(tableDiff.getDbTable(), buff);
            print(out, buff.toString());
        }
        boolean direct = true; // check if we can do all the column changes direct
        boolean sequence = false;
        for (Iterator iterator = colList.iterator(); iterator.hasNext();) {
            ColumnDiff diff = (ColumnDiff)iterator.next();
            if (diff.isExtraCol()) {
                if (isDropSequenceColumn(tableDiff, diff.getDbCol())) {
                    sequence = true;
                } else if (!isDirectDropColumnSupported()) {
                    direct = false;
                }

            }
            if (diff.isMissingCol()) {
                if (isAddSequenceColumn(diff.getOurCol())) {
                    sequence = true;
                } else if (!isDirectAddColumnSupported(diff.getOurCol())) {
                    direct = false;
                }
            }
            if (diff.isLenghtDiff()) {
                if (!isDirectLenghtColumnChangesSupported(diff.getOurCol(),
                        diff.getDbCol())) {
                    direct = false;
                }
            }
            if (diff.isNullDiff()) {
                if (!isDirectNullColumnChangesSupported()) {
                    direct = false;
                }
            }
            if (diff.isScaleDiff()) {
                if (!isDirectScaleColumnChangesSupported(diff.getOurCol(),
                        diff.getDbCol())) {
                    direct = false;
                }
            }
            if (diff.isTypeDiff()) {
                if (!isDirectTypeColumnChangesSupported(diff.getOurCol(),
                        diff.getDbCol())) {
                    direct = false;
                }
            }

        }
        if (sequence && direct) {
            if (!isDropConstraintsForDropTableSupported()) {
                fixConstraintsForNonDirectColumns(tableDiff, out, true);
                fixIndexForNonDirectColumns(tableDiff, out, true);
            }
            for (Iterator iterator = colList.iterator(); iterator.hasNext();) {
                ColumnDiff diff = (ColumnDiff)iterator.next();
                if (diff.isExtraCol()) {
                    buff.clear();
                    appendDropColumn(tableDiff, diff.getDbCol(), buff, true);
                    print(out, buff.toString());
                }
                if (diff.isMissingCol()) {
                    buff.clear();
                    appendAddNewColumn(tableDiff.getOurTable(),
                            diff.getOurCol(), buff, true);
                    out.println(buff.toString());
                } else if (diff.isLenghtDiff() || diff.isNullDiff() || diff.isScaleDiff() || diff.isTypeDiff()) {
                    buff.clear();
                    appendModifyColumn(tableDiff, diff, buff, true);
                    print(out, buff.toString());
                }

            }
            fixIndexForNonDirectColumns(tableDiff, out, false);
            fixConstraintsForNonDirectColumns(tableDiff, out, false);

        } else if (direct) {
            for (Iterator iterator = colList.iterator(); iterator.hasNext();) {
                ColumnDiff diff = (ColumnDiff)iterator.next();
                if (diff.isExtraCol()) {
                    buff.clear();
                    appendDropColumn(tableDiff, diff.getDbCol(), buff, true);
                    print(out, buff.toString());
                }
                if (diff.isMissingCol()) {
                    buff.clear();
                    appendAddNewColumn(tableDiff.getOurTable(),
                            diff.getOurCol(), buff, true);
                    out.println(buff.toString());
                } else if (diff.isLenghtDiff() || diff.isNullDiff() || diff.isScaleDiff() || diff.isTypeDiff()) {
                    buff.clear();
                    appendModifyColumn(tableDiff, diff, buff, true);
                    print(out, buff.toString());
                }

            }

            if (isMissingPK || otherPKProblems) {
                buff.clear();
                addPrimaryKeyConstraint(tableDiff.getOurTable(), buff);
                print(out, buff.toString());
            }

        } else {
            if (!isDropConstraintsForDropTableSupported()) {
                fixConstraintsForNonDirectColumns(tableDiff, out, true);
                fixIndexForNonDirectColumns(tableDiff, out, true);
            }
            fixColumnsNonDirect(tableDiff, out);
            fixIndexForNonDirectColumns(tableDiff, out, false);
            fixConstraintsForNonDirectColumns(tableDiff, out, false);
        }
    }

    protected void fixConstraintsForNonDirectColumns(TableDiff tableDiff,
            PrintWriter out, boolean drop) {
        // we need to create all the constraints that was droped during drop table,
        // but only ones that did not have problems
        JdbcConstraint[] constraints = null;
        if (drop) {
            if (tableDiff.getDbTable() != null) {
                constraints = tableDiff.getDbTable().constraints;
            }
        } else {
            constraints = tableDiff.getOurTable().constraints;
        }

        if (constraints != null) {
            for (int i = 0; i < constraints.length; i++) {
                JdbcConstraint constraint = constraints[i];
                ConstraintDiff diff = getConstraintDiffForName(tableDiff,
                        constraint.name, drop);
                if (diff == null) {
                    CharBuf buff = new CharBuf();
                    if (drop) {
                        appendRefDropConstraint(buff, constraint, false);
                    } else {
                        appendRefConstraint(buff, constraint);
                    }
                    String sql = buff.toString();
                    print(out, sql);
                }
            }
        }
    }

    /**
     * Fixes all the names with spaces in
     *
     * @param nameMap
     */
    public void fixAllNames(HashMap nameMap) {
        Collection col = nameMap.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            JdbcTable table = (JdbcTable)iterator.next();
            String temptableName = table.name;
            if (temptableName.indexOf(' ') != -1) {
                table.name = "\"" + temptableName + "\"";
            }
            JdbcColumn[] cols = table.cols;
            if (cols != null) {
                for (int i = 0; i < cols.length; i++) {
                    JdbcColumn jdbcColumn = cols[i];
                    String tempColName = jdbcColumn.name;
                    if (tempColName.indexOf(' ') != -1) {
                        jdbcColumn.name = "\"" + tempColName + "\"";
                    }
                }
            }
            JdbcIndex[] indexes = table.indexes;
            if (indexes != null) {
                for (int i = 0; i < indexes.length; i++) {
                    JdbcIndex index = indexes[i];
                    String tempIndexName = index.name;
                    if (tempIndexName.indexOf(' ') != -1) {
                        index.name = "\"" + tempIndexName + "\"";
                    }
                }
            }
            JdbcConstraint[] constraints = table.constraints;
            if (constraints != null) {
                for (int i = 0; i < constraints.length; i++) {
                    JdbcConstraint constraint = constraints[i];
                    String tempConstraintName = constraint.name;
                    if (tempConstraintName.indexOf(' ') != -1) {
                        constraint.name = "\"" + tempConstraintName + "\"";
                    }

                }
            }
            String tempPkConstraintName = table.pkConstraintName;
            if (tempPkConstraintName != null) {
                if (tempPkConstraintName.indexOf(' ') != -1) {
                    table.pkConstraintName = "\"" + tempPkConstraintName + "\"";
                }
            }
        }

    }

    protected void fixIndexForNonDirectColumns(TableDiff tableDiff,
            PrintWriter out, boolean drop) {
        // we need to create all the indexes that was droped during drop table,
        // but only ones that did not have problems
        JdbcIndex[] indexes = null;
        if (drop) {
            if (tableDiff.getDbTable() != null) {
                indexes = tableDiff.getDbTable().indexes;
            }
        } else {
            indexes = tableDiff.getOurTable().indexes;
        }
        if (indexes != null) {
            for (int i = 0; i < indexes.length; i++) {
                JdbcIndex index = indexes[i];
                IndexDiff diff = getIndexDiffForName(tableDiff, index.name,
                        drop);
                if (diff == null) {
                    CharBuf buff = new CharBuf();
                    if (drop) {
                        appendDropIndex(buff, tableDiff.getDbTable(), index,
                                false);
                    } else {
                        appendCreateIndex(buff, tableDiff.getOurTable(), index,
                                false);
                    }
                    String sql = buff.toString();
                    print(out, sql);
                }
            }
        }
    }

    protected void fixColumnsNonDirect(TableDiff tableDiff, PrintWriter out) {

    }

    protected void reportFixes(ArrayList diffList, PrintWriter out)
            throws SQLException {
        // do all drop constraints
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            ArrayList constraintList = tableDiff.getConstraintDiffs();
            for (Iterator iterator = constraintList.iterator();
                 iterator.hasNext();) {
                ConstraintDiff diff = (ConstraintDiff)iterator.next();
                if (diff.drop() && !diff.isMissingConstraint()) {
                    JdbcConstraint constraint = diff.getDbConstraint();
                    if (constraint == null) {
                        constraint = diff.getOurConstraint();
                    }

                    CharBuf buff = new CharBuf();
                    appendRefDropConstraint(buff, constraint, true);
                    String sql = buff.toString();
                    print(out, sql);
                }
            }
        }

        // do all drop index
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            JdbcTable dbTable = tableDiff.getDbTable();
            ArrayList indexList = tableDiff.getIndexDiffs();
            for (Iterator iterator = indexList.iterator();
                 iterator.hasNext();) {
                IndexDiff diff = (IndexDiff)iterator.next();
                if (diff.isExtraIndex() || diff.isExtraCol() || diff.isMissingCol() || diff.isUniqueness()) {
                    JdbcIndex idx = diff.getDbIndex();
                    CharBuf buff = new CharBuf();
                    if (idx != null) {
                        appendDropIndex(buff, dbTable, idx, true);
                        String sql = buff.toString();
                        print(out, sql);
                    }
                }
            }
        }

        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            if (tableDiff.isMissingTable()) {
                generateCreateTable(tableDiff.getOurTable(), null, out, true);
            } else {
                fixCoulumns(tableDiff, out);
            }

        }

        // do all create index
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            JdbcTable ourTable = tableDiff.getOurTable();
            ArrayList indexList = tableDiff.getIndexDiffs();
            for (Iterator iterator = indexList.iterator();
                 iterator.hasNext();) {
                IndexDiff diff = (IndexDiff)iterator.next();
                if (diff.isMissingIndex() || diff.isExtraCol() || diff.isMissingCol() || diff.isUniqueness()) {
                    JdbcIndex idx = diff.getOurIndex();
                    CharBuf buff = new CharBuf();
                    appendCreateIndex(buff, ourTable, idx, false);
                    String sql = buff.toString();
                    print(out, sql);
                }
            }
        }

        // do all create constraints
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            ArrayList constraintList = tableDiff.getConstraintDiffs();
            for (Iterator iterator = constraintList.iterator();
                 iterator.hasNext();) {
                ConstraintDiff diff = (ConstraintDiff)iterator.next();
                if (diff.isMissingConstraint() || diff.isExtraCol() || diff.isMissingCol() || diff.isRecreate()) {
                    JdbcConstraint constraint = diff.getOurConstraint();
                    CharBuf buff = new CharBuf();
                    appendRefConstraint(buff, constraint);
                    String sql = buff.toString();
                    print(out, sql);
                }
            }
        }

        // do all create constraints that we had to drop to fix column's
        for (Iterator iter = diffList.iterator(); iter.hasNext();) {
            TableDiff tableDiff = (TableDiff)iter.next();
            ArrayList pkList = tableDiff.getPkDiffs();
            for (Iterator iterator = pkList.iterator(); iterator.hasNext();) {
                PKDiff diff = (PKDiff)iterator.next();
                for (Iterator iterPk = diff.getAddConstraintsRefs().iterator();
                     iterPk.hasNext();) {
                    JdbcConstraint constraint = (JdbcConstraint)iterPk.next();
                    CharBuf buff = new CharBuf();
                    appendRefConstraint(buff, constraint);
                    String sql = buff.toString();
                    print(out, sql);
                }
            }
        }
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
                case Types.BIT:
                    switch (dbCol.jdbcType) {
                        case Types.TINYINT:
                        case Types.SMALLINT:
                            return true;
                        default:
                            return false;
                    }
                case Types.TINYINT:
                    switch (dbCol.jdbcType) {
                        case Types.BIT:
                        case Types.SMALLINT:
                            return true;
                        default:
                            return false;
                    }
                case Types.SMALLINT:
                    switch (dbCol.jdbcType) {
                        case Types.BIT:
                        case Types.TINYINT:
                            return true;
                        default:
                            return false;
                    }
                case Types.INTEGER:
                    switch (dbCol.jdbcType) {
                        case Types.NUMERIC:
                            return true;
                        default:
                            return false;
                    }
                case Types.BIGINT:
                    switch (dbCol.jdbcType) {
                        case Types.NUMERIC:
                        case Types.DECIMAL:
                            return true;
                        default:
                            return false;
                    }
                case Types.FLOAT:
                    switch (dbCol.jdbcType) {
                        case Types.DOUBLE:
                        case Types.REAL:
                            return true;
                        default:
                            return false;
                    }
                case Types.REAL:
                    switch (dbCol.jdbcType) {
                        case Types.DOUBLE:
                        case Types.FLOAT:
                            return true;
                        default:
                            return false;
                    }
                case Types.DOUBLE:
                    switch (dbCol.jdbcType) {
                        case Types.FLOAT:
                        case Types.REAL:
                            return true;
                        default:
                            return false;
                    }
                case Types.NUMERIC:
                    switch (dbCol.jdbcType) {
                        case Types.DECIMAL:
                        case Types.BIGINT:
                            return true;
                        default:
                            return false;
                    }
                case Types.DECIMAL:
                    switch (dbCol.jdbcType) {
                        case Types.NUMERIC:
                            return true;
                        default:
                            return false;
                    }
                case Types.CHAR:
                    switch (dbCol.jdbcType) {
                        case Types.VARCHAR:
                            return true;
                        default:
                            return false;
                    }
                case Types.VARCHAR:
                    switch (dbCol.jdbcType) {
                        case Types.CHAR:
                            return true;
                        default:
                            return false;
                    }
                case Types.LONGVARCHAR:
                    switch (dbCol.jdbcType) {
                        case Types.CLOB:
                            return true;
                        default:
                            return false;
                    }
                case Types.DATE:
                    switch (dbCol.jdbcType) {
                        case Types.TIMESTAMP:
                        case Types.TIME:
                            return true;
                        default:
                            return false;
                    }
                case Types.TIME:
                    switch (dbCol.jdbcType) {
                        case Types.TIMESTAMP:
                        case Types.DATE:
                            return true;
                        default:
                            return false;
                    }
                case Types.TIMESTAMP:
                    switch (dbCol.jdbcType) {
                        case Types.DATE:
                        case Types.TIME:
                            return true;
                        default:
                            return false;
                    }
                case Types.BINARY:
                    switch (dbCol.jdbcType) {
                        case Types.BINARY:
                            return true;
                        default:
                            return false;
                    }
                case Types.VARBINARY:
                    switch (dbCol.jdbcType) {
                        case Types.VARBINARY:
                            return true;
                        default:
                            return false;
                    }
                case Types.LONGVARBINARY:
                    switch (dbCol.jdbcType) {
                        case Types.BLOB:
                            return true;
                        default:
                            return false;
                    }
                case Types.BLOB:
                    switch (dbCol.jdbcType) {
                        case Types.LONGVARBINARY:
                            return true;
                        default:
                            return false;
                    }
                case Types.CLOB:
                    switch (dbCol.jdbcType) {
                        case Types.LONGVARCHAR:
                            return true;
                        default:
                            return false;
                    }
                case Types.NULL:
                    switch (dbCol.jdbcType) {
                        case Types.NULL:
                            return true;
                        default:
                            return false;
                    }
                case Types.OTHER:
                    switch (dbCol.jdbcType) {
                        case Types.OTHER:
                            return true;
                        default:
                            return false;
                    }
                case Types.DISTINCT:
                    switch (dbCol.jdbcType) {
                        case Types.DISTINCT:
                            return true;
                        default:
                            return false;
                    }
                case Types.STRUCT:
                    switch (dbCol.jdbcType) {
                        case Types.STRUCT:
                            return true;
                        default:
                            return false;
                    }
                case Types.REF:
                    switch (dbCol.jdbcType) {
                        case Types.REF:
                            return true;
                        default:
                            return false;
                    }
                case Types.JAVA_OBJECT:
                    switch (dbCol.jdbcType) {
                        case Types.JAVA_OBJECT:
                            return true;
                        default:
                            return false;
                    }
                default:
                    return false;
            }
        }
    }

    public boolean checkScale(JdbcColumn ourCol, JdbcColumn dbCol) {
        switch (ourCol.jdbcType) {
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return true;
            default:
                if (ourCol.scale != dbCol.scale) {
                    return false;
                } else {
                    return true;
                }
        }
    }

    public boolean checkNulls(JdbcColumn ourCol, JdbcColumn dbCol) {
        if (ourCol.nulls != dbCol.nulls) {
            return false;
        }
        return true;
    }

    public boolean checkLenght(JdbcColumn ourCol, JdbcColumn dbCol) {
        if (ourCol.length != dbCol.length) {
            if (ourCol.length != 0) {
                return false;
            }
        }
        return true;
    }

    protected String getDefaultValueComment() {
        return "  " + comment("Please enter your own default value here.");
    }

    protected String getDefaultForType(JdbcColumn ourCol) {
        switch (ourCol.jdbcType) {
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
                return "0";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                return "' '";
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "' '";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return "' '";
            case Types.NULL:
            case Types.OTHER:
            case Types.DISTINCT:
            case Types.STRUCT:
            case Types.REF:
            case Types.JAVA_OBJECT:
                return "' '";
            default:
                return "' '";
        }

    }

    /**
     * Gets the Column diff for the column name, else returns null
     */
    protected ColumnDiff getColumnDiffForName(TableDiff tableDiff, String name) {
        for (Iterator iter = tableDiff.getColDiffs().iterator();
             iter.hasNext();) {
            ColumnDiff diff = (ColumnDiff)iter.next();
            JdbcColumn our = diff.getOurCol();
            if (our != null) {
                if (our.name.equalsIgnoreCase(name)) {// we have the right column
                    return diff;
                }
            }
        }
        return null;

    }

    /**
     * Gets the Index diff for the index name, else returns null
     */
    protected IndexDiff getIndexDiffForName(TableDiff tableDiff, String name,
            boolean db) {
        for (Iterator iter = tableDiff.getIndexDiffs().iterator();
             iter.hasNext();) {
            IndexDiff diff = (IndexDiff)iter.next();
            JdbcIndex index = null;
            if (db) {
                index = diff.getDbIndex();
            } else {
                index = diff.getOurIndex();
            }
            if (index != null) {
                if (index.name.equalsIgnoreCase(name)) {// we have the right index
                    return diff;
                }
            }
        }
        return null;

    }

    /**
     * Gets the Constraint diff for the constraint name, else returns null
     */
    protected ConstraintDiff getConstraintDiffForName(TableDiff tableDiff,
            String name, boolean db) {
        for (Iterator iter = tableDiff.getConstraintDiffs().iterator();
             iter.hasNext();) {
            ConstraintDiff diff = (ConstraintDiff)iter.next();

            JdbcConstraint cons = null;
            if (db) {
                cons = diff.getDbConstraint();
            } else {
                cons = diff.getOurConstraint();
            }
            if (cons != null) {
                if (cons.name.equalsIgnoreCase(name)) {// we have the right index
                    return diff;
                }
            }
        }
        return null;
    }

    /**
     * Get the names of all tables in the database con is connected to.
     */
    public void setAllTableAndViewNames(Connection con) throws SQLException {
        ResultSet rs = null;
        try {
            rs = con.getMetaData().getTables(null, null, null, null);
            allTableList = new ArrayList();
            for (; rs.next();) {
                allTableList.add(rs.getString(3).trim().toUpperCase());
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
    }

    protected String getTempColumnName(JdbcTable table) {
        char j = 'a';
        CharBuf tempColName = new CharBuf("temp_column_" + j);

        for (int i = 0; i < table.cols.length; i++) {
            JdbcColumn col = table.cols[i];
            if (col.name.equalsIgnoreCase(tempColName.toString())) {
                int lastIndex = tempColName.toString().lastIndexOf(j);
                tempColName.replace(lastIndex, lastIndex + 1, ++j);
                i = 0;
            }
        }
        return tempColName.toString();
    }

    protected String getTempTableName(JdbcTable table, int lenght) {
        String temp = "temp_" + table.name;
        if (lenght < temp.length()) {
            temp = shrinkName(temp, lenght);
        }
        char i = 'a';

        while (allTableList.contains(temp.toUpperCase())) {
            if (i == 'a') {
                temp = temp + '_' + i;
                i++;
            } else {
                int lastIndex = temp.lastIndexOf('_');
                CharBuf buff = new CharBuf(temp);
                buff.replace(lastIndex + 1, lastIndex + 2, ++i);
                temp = buff.toString();
            }
            if (lenght < temp.length()) {
                temp = shrinkName(temp, lenght);
            }

        }
        allTableList.add(temp.toUpperCase());
        return temp;
    }

    /**
     * Shrink the supplied name to maxlen chars if it is longer than maxlen.
     * This implementation removes vowels first and then truncates if it has
     * to.
     */
    protected String shrinkName(String name, int maxlen) {
        int len = name.length();
        if (len <= maxlen) return name;
        int todo = len - maxlen;
        StringBuffer s = new StringBuffer();
        s.append(name.charAt(0));
        int i;
        for (i = 1; todo > 0 && i < len;) {
            char c = name.charAt(i++);
            if (c == 'e' || c == 'a' || c == 'i' || c == 'o' || c == 'u') {
                --todo;
            } else {
                s.append(c);
            }
        }
        if (todo == 0) {
            s.append(name.substring(i));
        }
        if (s.length() > maxlen) s.setLength(maxlen);
        return s.toString();
    }

    /**
     * Get a string back with lengh i
     */
    protected String pad(int i) {
        char[] spaces = new char[i];
        for (int j = 0; j < spaces.length; j++) {
            spaces[j] = ' ';
        }
        return String.valueOf(spaces);
    }

    /**
     * Use the index of the column in the 'group by' expression.
     */
    public boolean useColumnIndexForGroupBy() {
        return false;
    }

    /**
     * Calculate the typeCode for a aggregate expression.
     *
     * @param aggType         The aggregate type.
     * @param currentTypeCode The currenct calculated typeCode.
     * @see MDStatics
     */
    public int getAggregateTypeCode(int aggType, int currentTypeCode) {
        switch (aggType) {
            case AggregateNode.TYPE_AVG:
                if (MDStaticUtils.isIntegerType(currentTypeCode)) {

                    return MDStatics.BIGDECIMAL;


                } else if (currentTypeCode == MDStatics.FLOATW
                        || currentTypeCode == MDStatics.DOUBLEW) {

                    return MDStatics.DOUBLEW;


                } else {
                    return currentTypeCode;
                }
            case AggregateNode.TYPE_COUNT:
                return MDStatics.LONGW;
            case AggregateNode.TYPE_MAX:
                return currentTypeCode;
            case AggregateNode.TYPE_MIN:
                return currentTypeCode;
            case AggregateNode.TYPE_SUM:
                if (MDStaticUtils.isSignedIntegerType(currentTypeCode)) {

                    return MDStatics.LONGW;



                } else {
                    return currentTypeCode;
                }
            default:
                throw BindingSupportImpl.getInstance().internal("Aggregate type '"
                        + aggType + "' is not supported.");
        }
    }

    /**
     * Should columns be added to groupBy in appear in orderby and not in groupBy.
     */
    public boolean putOrderColsInGroupBy() {
        return true;
    }

    /**
     * Provide an oportunity for the driver to update the column's
     * used for post insert keygen.
     */
    public void updateClassForPostInsertKeyGen(ClassMetaData cmd,
            JdbcMappingResolver mappingResolver) {
    }

    /**
     * Should we use the col alias for columns that was added to the select
     * list because they are in the orderby and not in the selectList.
     *
     * @return
     */
    public boolean useColAliasForAddedCols() {
        return false;
    }

    public boolean isOracleStoreProcs() {
        return false;
    }

    /**
     * Maps a backend exception to a specific JDO exception
     *
     * @param cause   the backend exception
     * @param message error message. if null, the error message is
     *                taken from the backend exception
     * @param isFatal is the error fatal or not
     */
    public RuntimeException mapException(Throwable cause, String message,
            boolean isFatal) {
        return defaultMapException(cause, message, isFatal);
    }

    /**
     * Maps a backend exception to a specific JDO exception.
     * This is the default implementation which is used if no SqlDriver
     * instance is available.
     *
     * @param cause   the backend exception
     * @param message error message. if null, the error message is
     *                taken from the backend exception
     * @param isFatal is the error fatal or not
     */
    public static RuntimeException defaultMapException(Throwable cause,
            String message,
            boolean isFatal) {
        BindingSupportImpl
         bsi
        	= BindingSupportImpl.getInstance();

        if (SQLException.class.isAssignableFrom(cause.getClass())) {
            SQLException sqlException = (SQLException) cause;
            displaySQLException(sqlException);
//            sqlException.getNextException().printStackTrace(System.out);
        }
        if (bsi.isOwnException(cause)) {
            return (RuntimeException)cause;
        } else {
            if (Debug.DEBUG) {
                cause.printStackTrace(System.out);
            }
            if (bsi.isError(cause)) {
                if (bsi.isOutOfMemoryError(cause)) {
                    return bsi.exception(cause.toString(), cause);
                }
                throw (Error)cause;
            }
            if (isFatal) {
                return bsi.fatalDatastore
                        (message == null ? JdbcUtils.toString(cause) : message,
                                cause);
            } else {
                return bsi.datastore
                        (message == null ? JdbcUtils.toString(cause) : message,
                                cause);
            }
        }
    }

    private static void displaySQLException(SQLException ex) {
        if (Debug.DEBUG) {
            System.out.println ("\n\n\nSQLException");
            while (ex != null) {
                System.out.println ("SQLState: " + ex.getSQLState ());
                System.out.println ("Message:  " + ex.getMessage ());
                System.out.println ("Vendor:   " + ex.getErrorCode ());
                ex = ex.getNextException ();
            }
        }
    }

    /**
     * Convenience method, which gets the SqlDriver instance from the
     * store and calls its mapException() method. isFatal defaults to true.
     * If no SqlDriver is set, SqlDriver.defaultMapException() is called.
     *
     * @param sqlDriver
     * @param cause   the backend exception
     * @param message error message. if null, the error message is
     */
    public static RuntimeException mapException(SqlDriver sqlDriver, Throwable cause,
            String message) {
        return SqlDriver.mapException(sqlDriver, cause, message, true);
    }

    /**
     * Convenience method, which gets the SqlDriver instance from the
     * store and calls its mapException() method.
     * If no SqlDriver is set, SqlDriver.defaultMapException() is called.
     *
     * @param sqlDriver
     * @param cause   the backend exception
     * @param message error message. if null, the error message is
     *                taken from the backend exception
     * @param isFatal is the error fatal or not
     */
    public static RuntimeException mapException(SqlDriver sqlDriver, Throwable cause,
            String message,
            boolean isFatal) {
        if (sqlDriver != null) {
            return sqlDriver.mapException(cause, message, isFatal);
        } else {
            return SqlDriver.defaultMapException(cause, message, isFatal);
        }
    }

    /**
     * Does the driver detect and handle exceptions caused by
     * lock timeouts?
     */
    public boolean isHandleLockTimeout() {
        return false;
    }

    /**
     * Does the driver detect and handle exceptions caused by
     * duplicate primary keys?
     */
    public boolean isHandleDuplicateKey() {
        return false;
    }

    /**
     * Is this a lock timeout exception?
     */
    public boolean isLockTimeout(Throwable e) {
        return false;
    }

    /**
     * Is this a duplicate key exception?
     */
    public boolean isDuplicateKey(Throwable e) {
        return false;
    }

    /**
     * Convert d to a String suitable for embedding as a literal in an SQL
     * statement.
     */
    public String toSqlLiteral(double d) {
        return doubleFormat.format(d);
    }

    /**
     * Convert l to a String suitable for embedding as a literal in an SQL
     * statement.
     */
    public String toSqlLiteral(long l) {
        return Long.toString(l);
    }

    /**
     * Convert s to a String suitable for embedding as a literal in an SQL
     * statement.
     */
    public String toSqlLiteral(String s) {
        return '\'' + s + '\'';
    }

    /**
     * Convert s to a String suitable for embedding as a literal in an SQL
     * statement.
     */
    public String toSqlLiteral(boolean b) {
        return b ? "TRUE" : "FALSE";
    }
}
