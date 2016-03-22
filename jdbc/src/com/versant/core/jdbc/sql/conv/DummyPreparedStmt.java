
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
package com.versant.core.jdbc.sql.conv;

import java.sql.*;
import java.io.Reader;
import java.io.InputStream;
import java.util.Calendar;
import java.net.URL;
import java.math.BigDecimal;

/**
 * This PreparedStatement is used to capture the conversion done by a converter.
 */
public class DummyPreparedStmt implements PreparedStatement {

    public String value;
    public boolean toQuote = false;

    public DummyPreparedStmt() {
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setRef(int i, Ref x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        value = String.valueOf(x);
        toQuote = true;
    }



    public void setLong(int parameterIndex, long x) throws SQLException 
	{
        value = String.valueOf(x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        value = String.valueOf(x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        value = String.valueOf(x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setBlob(int i, Blob x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }
    
    

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setBytes(int parameterIndex, byte x[]) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setClob(int i, Clob x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setArray(int i, Array x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }











    public ResultSet executeQuery() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public int executeUpdate() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public void clearParameters() throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void addBatch() throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public boolean execute() throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setCursorName(String name) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public int getFetchSize() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int getUpdateCount() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public Connection getConnection() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public void cancel() throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setMaxFieldSize(int max) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void setMaxRows(int max) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public int getResultSetConcurrency() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int executeUpdate(String sql) throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int executeUpdate(String sql, int columnIndexes[]) throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int executeUpdate(String sql, String columnNames[]) throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int getFetchDirection() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public void setFetchDirection(int direction) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public int getQueryTimeout() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public int getResultSetType() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public void clearWarnings() throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void addBatch(String sql) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public void close() throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public int getMaxFieldSize() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public void clearBatch() throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public boolean getMoreResults() throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public boolean getMoreResults(int current) throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public int getMaxRows() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }

    public boolean execute(String sql) throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public boolean execute(String sql, int columnIndexes[]) throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public boolean execute(String sql, String columnNames[]) throws SQLException {
        return false;  //To change body of implemented methods use Options | File Templates.
    }

    public void setFetchSize(int rows) throws SQLException {
        //To change body of implemented methods use Options | File Templates.
    }

    public ResultSet getResultSet() throws SQLException {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    public int[] executeBatch() throws SQLException {
        return new int[0];  //To change body of implemented methods use Options | File Templates.
    }

    public int getResultSetHoldability() throws SQLException {
        return 0;  //To change body of implemented methods use Options | File Templates.
    }
}
