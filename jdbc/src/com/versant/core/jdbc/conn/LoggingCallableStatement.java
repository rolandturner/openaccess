
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

import com.versant.core.logging.LogEventStore;
import com.versant.core.logging.LogEventStore;
import com.versant.core.logging.LogEventStore;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.net.URL;
import java.io.InputStream;
import java.io.Reader;

/**
 * A JDBC CallableStatement wrapped for logging.
 */
public final class LoggingCallableStatement extends PooledPreparedStatement
        implements CallableStatement {

    protected CallableStatement cstat = null;

    public LoggingCallableStatement(LoggingConnection con,
                                    CallableStatement statement, LogEventStore pes) {
        super(con, statement, pes, null, null);
        this.cstat = statement;
    }

    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        cstat.registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        cstat.registerOutParameter(parameterIndex, sqlType, scale);
    }

    public boolean wasNull() throws SQLException {
        return cstat.wasNull();
    }

    public String getString(int parameterIndex) throws SQLException {
        return cstat.getString(parameterIndex);
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return cstat.getBoolean(parameterIndex);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return cstat.getByte(parameterIndex);
    }

    public short getShort(int parameterIndex) throws SQLException {
        return cstat.getShort(parameterIndex);
    }

    public int getInt(int parameterIndex) throws SQLException {
        return cstat.getInt(parameterIndex);
    }

    public long getLong(int parameterIndex) throws SQLException {
        return cstat.getLong(parameterIndex);
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return cstat.getFloat(parameterIndex);
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return cstat.getDouble(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        return cstat.getBigDecimal(parameterIndex, scale);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return cstat.getBytes(parameterIndex);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return cstat.getDate(parameterIndex);
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return cstat.getTime(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex)
            throws SQLException {
        return cstat.getTimestamp(parameterIndex);
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return cstat.getObject(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return cstat.getBigDecimal(parameterIndex);
    }

    public Object getObject(int i, Map map) throws SQLException {
        return cstat.getObject(i, map);
    }

    public Ref getRef(int i) throws SQLException {
        return cstat.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return cstat.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return cstat.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return cstat.getArray(i);
    }

    public Date getDate(int parameterIndex, Calendar cal)
            throws SQLException {
        return cstat.getDate(parameterIndex, cal);
    }

    public Time getTime(int parameterIndex, Calendar cal)
            throws SQLException {
        return cstat.getTime(parameterIndex, cal);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException {
        return cstat.getTimestamp(parameterIndex, cal);
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        cstat.registerOutParameter(paramIndex, sqlType, typeName);
    }

    public void registerOutParameter(String parameterName, int sqlType)
            throws SQLException {
        cstat.registerOutParameter(parameterName, sqlType);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale)
            throws SQLException {
        cstat.registerOutParameter(parameterName, sqlType, scale);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName)
            throws SQLException {
        cstat.registerOutParameter(parameterName, sqlType, typeName);
    }

    public java.net.URL getURL(int parameterIndex) throws SQLException {
        return cstat.getURL(parameterIndex);
    }

    public void setURL(String parameterName, java.net.URL val) throws SQLException {
        cstat.setURL(parameterName, val);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        cstat.setNull(parameterName, sqlType);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        cstat.setBoolean(parameterName, x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        cstat.setByte(parameterName, x);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        cstat.setShort(parameterName, x);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        cstat.setInt(parameterName, x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        cstat.setLong(parameterName, x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        cstat.setFloat(parameterName, x);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        cstat.setDouble(parameterName, x);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        cstat.setBigDecimal(parameterName, x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        cstat.setString(parameterName, x);
    }



	public void setBytes(String parameterName, byte x[]) throws SQLException 
	{
        cstat.setBytes(parameterName, x);
    }

    public void setDate(String parameterName, Date x)
            throws SQLException {
        cstat.setDate(parameterName, x);
    }

    public void setTime(String parameterName, Time x)
            throws SQLException {
        cstat.setTime(parameterName, x);
    }

    public void setTimestamp(String parameterName, Timestamp x)
            throws SQLException {
        cstat.setTimestamp(parameterName, x);
    }  

    public void setAsciiStream(String parameterName, java.io.InputStream x, int length)
            throws SQLException {
        cstat.setAsciiStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, java.io.InputStream x,
                                int length) throws SQLException {
        cstat.setBinaryStream(parameterName, x, length);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
            throws SQLException {
        cstat.setObject(parameterName, x, targetSqlType, scale);
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
            throws SQLException {
        cstat.setObject(parameterName, x, targetSqlType);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        cstat.setObject(parameterName, x);
    }

    public void setCharacterStream(String parameterName,
                                   java.io.Reader reader,
                                   int length) throws SQLException {
        cstat.setCharacterStream(parameterName, reader, length);
    }

    public void setDate(String parameterName, Date x, Calendar cal)
            throws SQLException {
        cstat.setDate(parameterName, x, cal);
    }

    public void setTime(String parameterName, Time x, Calendar cal)
            throws SQLException {
        cstat.setTime(parameterName, x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
            throws SQLException {
        cstat.setTimestamp(parameterName, x, cal);
    }

    public void setNull(String parameterName, int sqlType, String typeName)
            throws SQLException {
        cstat.setNull(parameterName, sqlType, typeName);
    }

    public String getString(String parameterName) throws SQLException {
        return cstat.getString(parameterName);
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return cstat.getBoolean(parameterName);
    }

    public byte getByte(String parameterName) throws SQLException {
        return cstat.getByte(parameterName);
    }

    public short getShort(String parameterName) throws SQLException {
        return cstat.getShort(parameterName);
    }

    public int getInt(String parameterName) throws SQLException {
        return cstat.getInt(parameterName);
    }

    public long getLong(String parameterName) throws SQLException {
        return cstat.getLong(parameterName);
    }

    public float getFloat(String parameterName) throws SQLException {
        return cstat.getFloat(parameterName);
    }

    public double getDouble(String parameterName) throws SQLException {
        return cstat.getDouble(parameterName);
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return cstat.getBytes(parameterName);
    }

    public Date getDate(String parameterName) throws SQLException {
        return cstat.getDate(parameterName);
    }

    public Time getTime(String parameterName) throws SQLException {
        return cstat.getTime(parameterName);
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return cstat.getTimestamp(parameterName);
    }

    public Object getObject(String parameterName) throws SQLException {
        return cstat.getObject(parameterName);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return cstat.getBigDecimal(parameterName);
    }

    public Object getObject(String parameterName, Map map) throws SQLException {
        return cstat.getObject(parameterName, map);
    }

    public Ref getRef(String parameterName) throws SQLException {
        return cstat.getRef(parameterName);
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return cstat.getBlob(parameterName);
    }

    public Clob getClob(String parameterName) throws SQLException {
        return cstat.getClob(parameterName);
    }

    public Array getArray(String parameterName) throws SQLException {
        return cstat.getArray(parameterName);
    }

    public Date getDate(String parameterName, Calendar cal)
            throws SQLException {
        return cstat.getDate(parameterName, cal);
    }

    public Time getTime(String parameterName, Calendar cal)
            throws SQLException {
        return cstat.getTime(parameterName, cal);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal)
            throws SQLException {
        return cstat.getTimestamp(parameterName, cal);
    }

    public java.net.URL getURL(String parameterName) throws SQLException {
        return cstat.getURL(parameterName);
    }

    public ResultSet executeQuery() throws SQLException {
        return cstat.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return cstat.executeUpdate();
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        cstat.setNull(parameterIndex, sqlType);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        cstat.setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        cstat.setByte(parameterIndex, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        cstat.setShort(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        cstat.setInt(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        cstat.setLong(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        cstat.setFloat(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        cstat.setDouble(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        cstat.setBigDecimal(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        cstat.setString(parameterIndex, x);
    }
    
    

    public void setBytes(int parameterIndex, byte x[]) throws SQLException {
        cstat.setBytes(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x)
            throws SQLException {
        cstat.setDate(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x)
            throws SQLException {
        cstat.setTime(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        cstat.setTimestamp(parameterIndex, x);
    }
    
    

    public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length)
            throws SQLException {
        cstat.setAsciiStream(parameterIndex, x, length);
    }

    public void setUnicodeStream(int parameterIndex, java.io.InputStream x,
                                 int length) throws SQLException {
        cstat.setUnicodeStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, java.io.InputStream x,
                                int length) throws SQLException {
        cstat.setBinaryStream(parameterIndex, x, length);
    }

    public void clearParameters() throws SQLException {
        cstat.clearParameters();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
            throws SQLException {
        cstat.setObject(parameterIndex, x, targetSqlType, scale);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        cstat.setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        cstat.setObject(parameterIndex, x);
    }

    public boolean execute() throws SQLException {
        return cstat.execute();
    }

    public void addBatch() throws SQLException {
        cstat.addBatch();
    }

    public void setCharacterStream(int parameterIndex,
                                   java.io.Reader reader,
                                   int length) throws SQLException {
        cstat.setCharacterStream(parameterIndex, reader, length);
    }

    public void setRef(int i, Ref x) throws SQLException {
        cstat.setRef(i, x);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        cstat.setBlob(i, x);
    }

    public void setClob(int i, Clob x) throws SQLException {
        cstat.setClob(i, x);
    }

    public void setArray(int i, Array x) throws SQLException {
        cstat.setArray(i, x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return cstat.getMetaData();
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        cstat.setDate(parameterIndex, x, cal);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        cstat.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        cstat.setTimestamp(parameterIndex, x, cal);
    }

    public void setNull(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        cstat.setNull(paramIndex, sqlType, typeName);
    }

    public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
        cstat.setURL(parameterIndex, x);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return cstat.getParameterMetaData();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return cstat.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return cstat.executeUpdate(sql);
    }

    public void close() throws SQLException {
        cstat.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return cstat.getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        cstat.setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException {
        return cstat.getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException {
        cstat.setMaxRows(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        cstat.setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException {
        return cstat.getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        cstat.setQueryTimeout(seconds);
    }

    public void cancel() throws SQLException {
        cstat.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return cstat.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        cstat.clearWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        cstat.setCursorName(name);
    }

    public boolean execute(String sql) throws SQLException {
        return cstat.execute(sql);
    }

    public ResultSet getResultSet() throws SQLException {
        return cstat.getResultSet();
    }

    public int getUpdateCount() throws SQLException {
        return cstat.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return cstat.getMoreResults();
    }

    public void setFetchDirection(int direction) throws SQLException {
        cstat.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return cstat.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        cstat.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return cstat.getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return cstat.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return cstat.getResultSetType();
    }

    public void addBatch(String sql) throws SQLException {
        cstat.addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        cstat.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        return cstat.executeBatch();
    }

    public Connection getConnection() throws SQLException {
        return cstat.getConnection();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return cstat.getMoreResults(current);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return cstat.getGeneratedKeys();
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return cstat.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int columnIndexes[]) throws SQLException {
        return cstat.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String columnNames[]) throws SQLException {
        return cstat.executeUpdate(sql, columnNames);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return cstat.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int columnIndexes[]) throws SQLException {
        return cstat.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String columnNames[]) throws SQLException {
        return cstat.execute(sql, columnNames);
    }

    public int getResultSetHoldability() throws SQLException {
        return cstat.getResultSetHoldability();
    }



}

