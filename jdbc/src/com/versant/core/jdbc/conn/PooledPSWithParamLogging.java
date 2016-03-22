
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
import com.versant.core.jdbc.logging.JdbcStatementParamsEvent;
import com.versant.core.jdbc.logging.JdbcStatementEvent;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * This is a JDBC PreparedStatement that logs the values of all parameters
 * set.
 */
public final class PooledPSWithParamLogging extends PooledPreparedStatement {

    protected JdbcStatementParamsEvent.Row row;
    protected ArrayList rows;
    protected int nextRowSize = 8;

    private JdbcStatementParamsEvent.Row lastRow;
    private ArrayList lastRows;

    public PooledPSWithParamLogging(LoggingConnection con,
            PreparedStatement statement, LogEventStore pes, String sql,
            PreparedStatementPool.Key key) {
        super(con, statement, pes, sql, key);
    }

    /**
     * Get the number of batches added for the last execute.
     */
    public int getLastBatchCount() {
        return lastRows == null ? 0 : lastRows.size();
    }

    /**
     * Get the parameter data for the last execute in a String.
     */
    public String getLastExecParamsString() {
        if (lastRow == null) return "(no parameters set)";
        return lastRow.toString();
    }

    /**
     * Get parameter data for the last execute batchEntry in a String.
     */
    public String getLastExecParamsString(int batchEntry) {
        int n = lastRows == null ? 0 : lastRows.size();
        return "<batch " + batchEntry + "> " +
            (batchEntry < n
                ? lastRows.get(batchEntry).toString()
                : getLastExecParamsString());
    }

    /**
     * Cleanup our stores
     */
    public void close() throws SQLException {
        row = lastRow = null;
        rows = lastRows = null;
        super.close();
    }

    private void setParam(int index, Object value, int sqlType) {
        if (row == null) {
            row = new JdbcStatementParamsEvent.Row(nextRowSize);
        }
        row.set(index, value, sqlType);
    }

    private void setParam(int index, Object value) {
        setParam(index, value, 0);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setParam(parameterIndex, null, sqlType);
        statement.setNull(parameterIndex,sqlType);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setParam(parameterIndex, x ? Boolean.TRUE : Boolean.FALSE);
        statement.setBoolean(parameterIndex,x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        setParam(parameterIndex, new Byte(x));
        statement.setByte(parameterIndex,x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        setParam(parameterIndex, new Short(x));
        statement.setShort(parameterIndex,x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        setParam(parameterIndex, new Integer(x));
        statement.setInt(parameterIndex,x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        setParam(parameterIndex, new Long(x));
        statement.setLong(parameterIndex,x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        setParam(parameterIndex, new Float(x));
        statement.setFloat(parameterIndex,x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        setParam(parameterIndex, new Double(x));
        statement.setDouble(parameterIndex,x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setParam(parameterIndex, x);
        statement.setBigDecimal(parameterIndex,x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        setParam(parameterIndex, x);
        statement.setString(parameterIndex,x);
    }

    public void setBytes(int parameterIndex, byte x[]) throws SQLException {
        setParam(parameterIndex, x);
        statement.setBytes(parameterIndex,x);
    }

    public void setDate(int parameterIndex, Date x)
            throws SQLException {
        setParam(parameterIndex, x);
        statement.setDate(parameterIndex,x);
    }

    public void setTime(int parameterIndex, Time x)
            throws SQLException {
        setParam(parameterIndex, x);
        statement.setTime(parameterIndex,x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        setParam(parameterIndex, x);
        statement.setTimestamp(parameterIndex,x);
    }

    public void clearParameters() throws SQLException {
        row = null;
        rows = null;
        statement.clearParameters();
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        setParam(parameterIndex, x);
        statement.setObject(parameterIndex,x);
    }

    protected JdbcStatementEvent createAndLogEventForExec(int type, boolean logSql) {
        if (rows == null && row == null) {
            JdbcStatementEvent ev = new JdbcStatementEvent(0, this,
                logSql ? sql : null, type);
            pes.log(ev);
            lastRow = null;
            lastRows = null;
            return ev;
        } else {
            JdbcStatementParamsEvent ev = new JdbcStatementParamsEvent(
                0, this, logSql ? sql : null, type);
            if (rows == null) {
                ev.setParams(new JdbcStatementParamsEvent.Row[]{row});
            } else {
                if (row != null) rows.add(row);
                JdbcStatementParamsEvent.Row[] a =
                        new JdbcStatementParamsEvent.Row[rows.size()];
                rows.toArray(a);
                ev.setParams(a);
            }
            pes.log(ev);
            lastRows = rows;
            lastRow = row;
            rows = null;
            row = null;
            return ev;
        }
    }

    public ResultSet executeQuery() throws SQLException {
        JdbcStatementEvent ev = createAndLogEventForExec(
                JdbcStatementEvent.STAT_EXEC_QUERY, true);
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
        JdbcStatementEvent ev = createAndLogEventForExec(
                JdbcStatementEvent.STAT_EXEC_UPDATE, true);
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

    public boolean execute() throws SQLException {
        JdbcStatementEvent ev = createAndLogEventForExec(
                JdbcStatementEvent.STAT_EXEC, true);
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
        JdbcStatementParamsEvent ev = new JdbcStatementParamsEvent(
            0, this, null, JdbcStatementEvent.STAT_ADD_BATCH);
        if (row != null) {
            ev.setParams(new JdbcStatementParamsEvent.Row[]{row});
            if (rows == null) rows = new ArrayList();
            row.trim();
            rows.add(row);
            nextRowSize = row.size;
            row = null;
        }
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

    public int[] executeBatch() throws SQLException {
        JdbcStatementEvent ev = createAndLogEventForExec(
                JdbcStatementEvent.STAT_EXEC_BATCH, true);
        try {
            int[] a = statement.executeBatch();
            ev.setUpdateCounts(a);
            return a;
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
}

