
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
package com.versant.core.jdbc.conn;

import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.logging.LogEventStore;
import com.versant.core.common.Debug;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;

/**
 * Gets connections from one or more java.sql.DataSource's and/or a
 * JDBCConnectionPool. If there are 2 DataSource's then the second is used
 * for highPriority (keygen) connections. If there is only one DataSource
 * and a pool then the pool is used for highPriority connections. Otherwise
 * the first DataSource is used.
 * <p/>
 * Connections obtained from the DataSource(s) may optionally have
 * PreparedStatement pooling enabled for them. Operations on these connections
 * will also be logged depending on the logging level.
 */
public class ExternalJdbcConnectionSource implements JdbcConnectionSource {

    private DataSource ds1;
    private DataSource ds2;
    private JDBCConnectionPool pool;
    private LogEventStore pes;
    private String url;
    private String driverName;

    private boolean usePsPool;
    private int psCacheMax;
    private boolean clearBatch;
    private final boolean ds1Managed;

    public ExternalJdbcConnectionSource(DataSource ds1, boolean enlisted, DataSource ds2,
            JDBCConnectionPool pool, LogEventStore pes) {
        this.ds1 = ds1;
        this.ds2 = ds2;
        this.pool = pool;
        this.pes = pes;
        ds1Managed = enlisted;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public boolean isUsePsPool() {
        return usePsPool;
    }

    /**
     * Use PreparedStatement pooling on connections obtained from DataSource's
     * or not.
     */
    public void setUsePsPool(boolean usePsPool) {
        this.usePsPool = usePsPool;
    }

    public int getPsCacheMax() {
        return psCacheMax;
    }

    /**
     * Limit the number of cached PreparedStatement's per connection to this
     * if PreparedStatement pooling is enabled.
     */
    public void setPsCacheMax(int psCacheMax) {
        this.psCacheMax = psCacheMax;
    }

    public boolean isClearBatch() {
        return clearBatch;
    }

    /**
     * Invoke clearBatch on PreparedStatements returned to the pool
     * if PreparedStatement pooling is enabled.
     */
    public void setClearBatch(boolean clearBatch) {
        this.clearBatch = clearBatch;
    }

    public Connection getConnection(boolean highPriority, boolean autoCommit)
            throws SQLException {
        if (highPriority) {
            if (pool != null) {
                return pool.getConnection(false, autoCommit);
            } else if (ds2 != null) {
                return getConnection(ds2, false);
            }
        }
        return getConnection(ds1, ds1Managed);
    }

    /**
     * Get a Connection from ds and wrap it.
     */
    protected Connection getConnection(DataSource ds, boolean managedDs) throws SQLException {
        if (managedDs) {
            return new WrappedManagedConnection(ds.getConnection());
        } else {
            return ds.getConnection();
        }
    }

    public void returnConnection(Connection con) throws SQLException {
        if (con instanceof PooledConnection) {
            pool.returnConnection(con);
        } else {
            con.close();
        }
    }

    public String getURL() {
        return url;
    }

    public String getDriverName() {
        return driverName;
    }

    public void init() {
        if (pool != null) {
            pool.init();
        }
    }

    public void destroy() {
        ds1 = ds2 = null;
        if (pool != null) {
            pool.destroy();
            pool = null;
        }
    }

    public void closeIdleConnections() {
        if (pool != null) {
            pool.closeIdleConnections();
        }
    }

    /**
     * A Wrapper around a jdbc connection that is managed by a transaction manager.
     * We are not allowed to commit/rollback these as this is done by the 'TM'
     */
    private class WrappedManagedConnection implements Connection {
        private Connection con;

        public WrappedManagedConnection(Connection con) {
            this.con = con;
        }

        public void clearWarnings() throws SQLException {
            con.clearWarnings();
        }

        public void close() throws SQLException {
            con.close();
        }

        public void commit() throws SQLException {
            if (Debug.DEBUG) {
                System.out.println("ExternalJdbcConnectionSource$WrappedManagedConnection.commit");
            }
        }

        public void rollback() throws SQLException {
            if (Debug.DEBUG) {
                System.out.println("ExternalJdbcConnectionSource$WrappedManagedConnection.rollback");
            }
        }

        public Statement createStatement() throws SQLException {
            return con.createStatement();
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return con.createStatement(resultSetType, resultSetConcurrency);
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public boolean getAutoCommit() throws SQLException {
            return con.getAutoCommit();
        }

        public String getCatalog() throws SQLException {
            return con.getCatalog();
        }

        public int getHoldability() throws SQLException {
            return con.getHoldability();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            return con.getMetaData();
        }

        public int getTransactionIsolation() throws SQLException {
            return con.getTransactionIsolation();
        }

        public Map getTypeMap() throws SQLException {
            return con.getTypeMap();
        }

        public SQLWarning getWarnings() throws SQLException {
            return con.getWarnings();
        }

        public boolean isClosed() throws SQLException {
            return con.isClosed();
        }

        public boolean isReadOnly() throws SQLException {
            return con.isReadOnly();
        }

        public String nativeSQL(String sql) throws SQLException {
            return con.nativeSQL(sql);
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            return con.prepareCall(sql);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return con.prepareStatement(sql);
        }

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return con.prepareStatement(sql, autoGeneratedKeys);
        }

        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return con.prepareStatement(sql, columnIndexes);
        }

        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return con.prepareStatement(sql, columnNames);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            con.releaseSavepoint(savepoint);
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            con.rollback(savepoint);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            con.setAutoCommit(autoCommit);
        }

        public void setCatalog(String catalog) throws SQLException {
            con.setCatalog(catalog);
        }

        public void setHoldability(int holdability) throws SQLException {
            con.setHoldability(holdability);
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
            con.setReadOnly(readOnly);
        }

        public Savepoint setSavepoint() throws SQLException {
            return con.setSavepoint();
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            return con.setSavepoint(name);
        }

        public void setTransactionIsolation(int level) throws SQLException {
            con.setTransactionIsolation(level);
        }

        public void setTypeMap(Map map) throws SQLException {
            con.setTypeMap(map);
        }
    }

}

