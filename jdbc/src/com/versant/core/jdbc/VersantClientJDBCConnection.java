
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
package com.versant.core.jdbc;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdbc.conn.LoggingConnection;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.datastore.JDOConnection;
import java.sql.*;

/**
 * This is a JDBC Connection proxy for local PersistenceManager use. It is
 * wrapped by this proxy so that we can disconnect the client and the pooled
 * connection if he misbehaves.
 */
public final class VersantClientJDBCConnection implements Connection,
        JDOConnection {

    private JdbcStorageManager owner;
    private Connection realConnection;

    public VersantClientJDBCConnection(JdbcStorageManager owner,
            Connection realConnection) {
        this.owner = owner;
        this.realConnection = realConnection;
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    private void checkClosed() {
        if (realConnection == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "The connection has been closed");
        }
    }

    public Statement createStatement() throws SQLException {
        checkClosed();
        return realConnection.createStatement();
    }

    public PreparedStatement prepareStatement(String sql)
            throws SQLException {
        checkClosed();
        return realConnection.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        checkClosed();
        return realConnection.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        checkClosed();
        return realConnection.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) {
        checkClosed();
        throw BindingSupportImpl.getInstance().invalidOperation(
                "This is not allowed. This connection is managed by JDO.");
    }

    public boolean getAutoCommit() throws SQLException {
        checkClosed();
        return realConnection.getAutoCommit();
    }

    public void commit() {
        checkClosed();
        throw BindingSupportImpl.getInstance().invalidOperation(
                "This is not allowed. Commit must be done via PersistenceManager.");
    }

    public void rollback() {
        checkClosed();
        throw BindingSupportImpl.getInstance().invalidOperation(
                "This is not allowed. Rollback must be done via PersistenceManager.");
    }

    public void close(){
        
    	try {
			if (isClosed()) return;
		} catch (SQLException e) {
			new JDODataStoreException("SQLException",e);
		}
        
        closeImp();
    }

    public void closeImp() {
        realConnection = null;
        owner.clientConClosed();
    }

    public boolean isClosed() throws SQLException {
        return realConnection == null || realConnection.isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();
        return realConnection.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed();
        realConnection.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        checkClosed();
        return realConnection.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        checkClosed();
        realConnection.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        checkClosed();
        return realConnection.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();
        realConnection.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        checkClosed();
        return realConnection.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        checkClosed();
        return realConnection.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        checkClosed();
        realConnection.clearWarnings();
    }

    public Statement createStatement(int resultSetType,
            int resultSetConcurrency)
            throws SQLException {
        checkClosed();
        return realConnection.createStatement(resultSetType,
                resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency)
            throws SQLException {
        checkClosed();
        return realConnection.prepareStatement(sql, resultSetType,
                resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        checkClosed();
        return realConnection.prepareCall(sql, resultSetType,
                resultSetConcurrency);
    }

    public java.util.Map getTypeMap() throws SQLException {
        checkClosed();
        return realConnection.getTypeMap();
    }

    public void setTypeMap(java.util.Map map) throws SQLException {
        checkClosed();
        realConnection.setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException {
        checkClosed();
        realConnection.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        checkClosed();
        return realConnection.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        checkClosed();
        return realConnection.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        checkClosed();
        return realConnection.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        checkClosed();
        realConnection.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        checkClosed();
        realConnection.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        checkClosed();
        return realConnection.createStatement(resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        checkClosed();
        return realConnection.prepareStatement(sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        checkClosed();
        return realConnection.prepareCall(sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql,
            int autoGeneratedKeys)
            throws SQLException {
        checkClosed();
        return realConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
            throws SQLException {
        checkClosed();
        return realConnection.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[])
            throws SQLException {
        checkClosed();
        return realConnection.prepareStatement(sql, columnNames);
    }

    /**
     * Returns the native, datastore-specific connection that this connection
     * wraps.
     * In general, it is not recommended that this native connection be used
     * directly, since the JDO implementation has no way to intercept calls to
     * it, so it is quite possible to put the PersistenceManager's connection
     * into an invalid state.
     */
    public Object getNativeConnection() {
        if (realConnection instanceof LoggingConnection) {
            return ((LoggingConnection)realConnection).getCon();
        }
        return realConnection;
    }
}
