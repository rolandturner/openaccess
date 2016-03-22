
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
import com.versant.core.jdbc.logging.JdbcStatementEvent;
import com.versant.core.logging.LogEventStore;

import java.math.BigDecimal;
import java.io.InputStream;
import java.io.Reader;
import java.util.Calendar;
import java.sql.*;
import java.net.URL;

/**
 * A JDBC PreparedStatement wrapped for event logging that can be pooled by
 * PooledConnection.
 */
public class PooledPreparedStatement extends LoggingStatement implements PreparedStatement {

    protected final PreparedStatement statement;
    protected final String sql;
    private final PreparedStatementPool.Key key;

    public PooledPreparedStatement next; // linked list for PsPool

    public PooledPreparedStatement(LoggingConnection con,
            PreparedStatement statement, LogEventStore pes, String sql,
            PreparedStatementPool.Key key) {
        super(con, statement, pes);
        this.statement = statement;
        this.sql = sql;
        this.key = key;
    }

    /**
     * Close the real statement.
     */
    public void closeRealStatement() throws SQLException {
        super.close();
    }

    public PreparedStatementPool.Key getKey() {
        return key;
    }

    /**
     * If this is a pooled ps then return it to the pool otherwise close
     * it normally.
     */
    public void close() throws SQLException {
        if (key != null) con.returnPreparedStatement(this);
        else super.close();
    }

    protected String getSql() {
        return sql;
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        statement.setURL(parameterIndex, x);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return statement.getParameterMetaData();
    }

    public ResultSet executeQuery() throws SQLException {
        if (!pes.isFine()) return statement.executeQuery();
        JdbcStatementEvent ev = new JdbcStatementEvent(0, this,
            sql, JdbcStatementEvent.STAT_EXEC_QUERY);
        pes.log(ev);
        try {
            ResultSet rs = statement.executeQuery();
            ev.updateResultSetID(rs);
            if (pes.isFiner()) rs = new LoggingResultSet(this, sql, rs, pes);
            return rs;
        } catch (SQLException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public int executeUpdate() throws SQLException {
        if (!pes.isFine()) return statement.executeUpdate();
        JdbcStatementEvent ev = new JdbcStatementEvent(0, this,
            sql, JdbcStatementEvent.STAT_EXEC_UPDATE);
        pes.log(ev);
        try {
            int c = statement.executeUpdate();
            ev.setUpdateCount(c);
            return c;
        } catch (SQLException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        statement.setNull(parameterIndex,sqlType);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        statement.setBoolean(parameterIndex,x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        statement.setByte(parameterIndex,x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        statement.setShort(parameterIndex,x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        statement.setInt(parameterIndex,x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        statement.setLong(parameterIndex,x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        statement.setFloat(parameterIndex,x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        statement.setDouble(parameterIndex,x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        statement.setBigDecimal(parameterIndex,x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        statement.setString(parameterIndex,x);
    }



	public void setBytes(int parameterIndex, byte x[]) throws SQLException 
	{
        statement.setBytes(parameterIndex,x);
    }

    public void setDate(int parameterIndex, Date x)
            throws SQLException {
        statement.setDate(parameterIndex,x);
    }

    public void setTime(int parameterIndex, Time x)
            throws SQLException {
        statement.setTime(parameterIndex,x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        statement.setTimestamp(parameterIndex,x);
    }
    
    

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        statement.setAsciiStream(parameterIndex,x,length);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x,
                                 int length) throws SQLException {
        statement.setUnicodeStream(parameterIndex,x,length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x,
                                int length) throws SQLException {
        statement.setBinaryStream(parameterIndex,x,length);
    }

    public void clearParameters() throws SQLException {
        statement.clearParameters();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
            throws SQLException {
        statement.setObject(parameterIndex,x,targetSqlType,scale);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        statement.setObject(parameterIndex,x,targetSqlType);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        statement.setObject(parameterIndex,x);
    }

    public boolean execute() throws SQLException {
        if (!pes.isFine()) return statement.execute();
        JdbcStatementEvent ev = new JdbcStatementEvent(0, this, sql,
            JdbcStatementEvent.STAT_EXEC);
        pes.log(ev);
        try {
            boolean ans = statement.execute();
            ev.setHasResultSet(ans);
            return ans;
        } catch (SQLException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public void addBatch() throws SQLException {
        if (!pes.isFiner()) {
            statement.addBatch();
            return;
        }
        JdbcStatementEvent ev = new JdbcStatementEvent(0, this, null,
            JdbcStatementEvent.STAT_ADD_BATCH);
        pes.log(ev);
        try {
            statement.addBatch();
        } catch (SQLException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            con.setNeedsValidation(true);
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public void setCharacterStream(int parameterIndex,
                                   Reader reader,
                                   int length) throws SQLException {
        statement.setCharacterStream(parameterIndex,reader,length);
    }

    public void setRef(int i, Ref x) throws SQLException {
        statement.setRef(i,x);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        statement.setBlob(i,x);
    }

    public void setClob(int i, Clob x) throws SQLException {
        statement.setClob(i,x);
    }

    public void setArray(int i, Array x) throws SQLException {
        statement.setArray(i,x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return statement.getMetaData();
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        statement.setDate(parameterIndex,x,cal);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        statement.setTime(parameterIndex,x,cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        statement.setTimestamp(parameterIndex,x,cal);
    }

    public void setNull(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        statement.setNull(paramIndex,sqlType,typeName);
    }

}

