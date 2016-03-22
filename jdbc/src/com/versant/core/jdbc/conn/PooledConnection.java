
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.core.logging.LogEventStore;

import java.sql.*;

/**
 * A JDBC connection belonging to a JDBCConnectionPool.
 *
 * @see JDBCConnectionPool
 */
public final class PooledConnection extends LoggingConnection {

    private final JDBCConnectionPool pool;
    private boolean destroyed; // connection has been timedout
    public boolean idle;   // is connection idle (i.e. in pool)?
    public PooledConnection prev, next; // linked list for JDBConnectionPool
    public int age; // number of times con has been returned to the pool
    private long lastActivityTime;
    // Time when something last happened on this connection. This is used
    // to cleanup active connections that are stuck.

    private int cachedTransationIsolation = -1;
    private boolean cachedAutoCommit;

    public PooledConnection(JDBCConnectionPool pool, Connection con,
            LogEventStore pes, boolean usePsPool, int psCacheMax) {
        super(con, pes, usePsPool, psCacheMax, pool.isClearBatch());
        this.pool = pool;
    }

    public JDBCConnectionPool getPool() {
        return pool;
    }

    public synchronized long getLastActivityTime() {
        return lastActivityTime;
    }

    public synchronized void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Return ps to the pool.
     * @see PooledPreparedStatement#close
     */
    public void returnPreparedStatement(PooledPreparedStatement ps)
            throws SQLException {
        updateLastActivityTime();
        super.returnPreparedStatement(ps);
    }

    /**
     * This is just going to return the connection to the pool.
     */
    public void close() {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        pool.returnConnection(this);
    }

    public void setHoldability(int holdability) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
            throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[])
            throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareStatement(sql, columnNames);
    }

    public String nativeSQL(String sql) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.nativeSQL(sql);
    }

    public boolean getAutoCommit() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return cachedAutoCommit = super.getAutoCommit();
    }

    public boolean getCachedAutoCommit() {
        return cachedAutoCommit;
    }

    public boolean isClosed() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.setTransactionIsolation(level);
        cachedTransationIsolation = level;
    }



    public int getTransactionIsolation() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return cachedTransationIsolation = super.getTransactionIsolation();
    }

    public int getCachedTransactionIsolation() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        if (cachedTransationIsolation >= 0) return cachedTransationIsolation;
        return cachedTransationIsolation = super.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.createStatement(resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                 int resultSetConcurrency) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public java.util.Map getTypeMap() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.getTypeMap();
    }

    public void setTypeMap(java.util.Map map) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.setTypeMap(map);
    }

    public Statement createStatement() throws SQLException {
        updateLastActivityTime();
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.createStatement();
    }

    public PreparedStatement prepareStatement(String sql)
            throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareStatement(sql);
    }


    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareStatement(sql,resultSetType, resultSetConcurrency);
    }


    public CallableStatement prepareCall(String sql) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        return super.prepareCall(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.setAutoCommit(autoCommit);
        cachedAutoCommit = autoCommit;
    }

    public void commit() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.commit();
    }

    public void rollback() throws SQLException {
        if (Debug.DEBUG) {
            if (idle) throw BindingSupportImpl.getInstance().internal("con in pool");
        }
        super.rollback();
    }


    /**
     * Has this connection been destroyed?
     * @see #destroy()
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Forceably close the real connection and set a flag to make sure this
     * connection will not go back in the pool. Any exceptions on close
     * are silently discarded. This is a NOP if the connection has already
     * been destroyed.
     * @see #isDestroyed()
     */
    public void destroy() {
        if (destroyed) return;
        this.destroyed = true;
        try {
            super.close();
        } catch (SQLException e) {
            // ignore
        }
    }

}

