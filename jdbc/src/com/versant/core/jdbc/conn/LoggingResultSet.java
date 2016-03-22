
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
import com.versant.core.jdbc.logging.JdbcResultSetEvent;
import com.versant.core.jdbc.logging.JdbcLogEvent;

import java.math.BigDecimal;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Calendar;
import java.sql.*;
import java.net.URL;

import com.versant.core.common.BindingSupportImpl;

/**
 * A JDBC ResultSet wrapped for logging.
 */
public final class LoggingResultSet implements ResultSet {
//    public static final Map openResults = new HashMap();

    private LoggingStatement proxyStatement;
    private String sql;
    private ResultSet resultSet;
    private LogEventStore pes;
    private Object[] row;
    private int rowSize;

    public LoggingResultSet(LoggingStatement proxyStatement, String sql,
            ResultSet resultSet, LogEventStore pes) {
        this.proxyStatement = proxyStatement;
        this.sql = sql;
        this.resultSet = resultSet;
        this.pes = pes;
        try {
            row = new Object[resultSet.getMetaData().getColumnCount()];
        } catch (SQLException e) {
            //ignore
            row = new Object[8];
        }


//        if (Debug.DEBUG) {
//            openResults.put(this, new Object[] {sql, new Exception()});
//        }
    }

    /**
     * Add data for the current row to ev and reset the data store.
     */
    private void addRowToEvent(JdbcResultSetEvent ev) {
        if (rowSize > 0) {
            ev.setRow(getRowData());
            rowSize = 0;
        }
    }

    /**
     * Get whatever data we have for the current row. This may be an empty
     * array.
     */
    public Object[] getRowData() {
        Object[] a = new Object[rowSize];
        System.arraycopy(row, 0, a, 0, rowSize);
        return a;
    }

    /**
     * Get whatever data we have for the current row in a String.
     */
    public String getRowDataString() {
        Object[] a = getRowData();
        StringBuffer s = new StringBuffer();
        s.append('[');
        for (int i = 0; i < a.length; i++) {
            if (i > 0) s.append(", ");
            try {
                s.append(a[i]);
            } catch (Exception e) {
                s.append("<toString failed: ");
                s.append(e.toString());
                s.append('>');
            }
        }
        s.append(']');
        return s.toString();
    }

    /**
     * Get the original SQL statement for this ResultSet.
     */
    public String getSql() {
        return sql;
    }

    private void setRow(int columnIndex, Object x) {
        if (columnIndex > rowSize) rowSize = columnIndex;
        int i = columnIndex - 1;
        int n = row.length;
        if (i >= n) {
            Object[] a = new Object[i * 2];
            System.arraycopy(row, 0, a, 0, n);
            row = a;
        }
        row[i] = x;
    }

//    public URL getURL(int columnIndex) throws SQLException {
//        return resultSet.getURL(columnIndex);
//    }
//
//    public URL getURL(String columnName) throws SQLException {
//        return resultSet.getURL(columnName);
//    }
//
//    public void updateRef(int columnIndex, Ref x) throws SQLException {
//        resultSet.updateRef(columnIndex, x);
//    }
//
//    public void updateRef(String columnName, Ref x) throws SQLException {
//        resultSet.updateRef(columnName, x);
//    }
//
//    public void updateBlob(int columnIndex, Blob x) throws SQLException {
//        resultSet.updateBlob(columnIndex, x);
//    }
//
//    public void updateBlob(String columnName, Blob x) throws SQLException {
//        resultSet.updateBlob(columnName, x);
//    }
//
//    public void updateClob(int columnIndex, Clob x) throws SQLException {
//        resultSet.updateClob(columnIndex, x);
//    }
//
//    public void updateClob(String columnName, Clob x) throws SQLException {
//        resultSet.updateClob(columnName, x);
//    }
//
//    public void updateArray(int columnIndex, Array x) throws SQLException {
//        resultSet.updateArray(columnIndex, x);
//    }
//
//    public void updateArray(String columnName, Array x) throws SQLException {
//        resultSet.updateArray(columnName, x);
//    }

    public boolean next() throws SQLException {
        JdbcResultSetEvent ev = new JdbcResultSetEvent(
            0, this, null, JdbcStatementEvent.RS_NEXT);
        addRowToEvent(ev);
        pes.log(ev);
        try {
            boolean ans = resultSet.next();
            ev.setNext(ans);
            return ans;
        } catch (SQLException e) {
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public void close() throws SQLException {
        JdbcResultSetEvent ev = new JdbcResultSetEvent(
            0, this, null, JdbcStatementEvent.RS_CLOSE);
        addRowToEvent(ev);
        pes.log(ev);
        try {
//            if (Debug.DEBUG) {
//                openResults.remove(this);
//            }
            resultSet.close();
        } catch (SQLException e) {
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }

    public String getString(int columnIndex) throws SQLException {
        String s = resultSet.getString(columnIndex);
        setRow(columnIndex, s);
        return s;
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        boolean b = resultSet.getBoolean(columnIndex);
        setRow(columnIndex, b ? Boolean.TRUE : Boolean.FALSE);
        return b;
    }

    public byte getByte(int columnIndex) throws SQLException {
        byte b = resultSet.getByte(columnIndex);
        setRow(columnIndex, new Byte(b));
        return b;
    }

    public short getShort(int columnIndex) throws SQLException {
        short s = resultSet.getShort(columnIndex);
        setRow(columnIndex, new Short(s));
        return s;
    }

    public int getInt(int columnIndex) throws SQLException {
        int x = resultSet.getInt(columnIndex);
        setRow(columnIndex, new Integer(x));
        return x;
    }

    public long getLong(int columnIndex) throws SQLException {
        long x = resultSet.getLong(columnIndex);
        setRow(columnIndex, new Long(x));
        return x;
    }

    public float getFloat(int columnIndex) throws SQLException {
        float x = resultSet.getFloat(columnIndex);
        setRow(columnIndex, new Float(x));
        return x;
    }

    public double getDouble(int columnIndex) throws SQLException {
        double x = resultSet.getDouble(columnIndex);
        setRow(columnIndex, new Double(x));
        return x;
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        BigDecimal x = resultSet.getBigDecimal(columnIndex,scale);
        setRow(columnIndex, x);
        return x;
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        byte[] x = resultSet.getBytes(columnIndex);
        setRow(columnIndex, x);
        return x;
    }

    public Date getDate(int columnIndex) throws SQLException {
        Date x = resultSet.getDate(columnIndex);
        setRow(columnIndex, x);
        return x;
    }

    public Time getTime(int columnIndex) throws SQLException {
        Time x = resultSet.getTime(columnIndex);
        setRow(columnIndex, x);
        return x;
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        Timestamp x = resultSet.getTimestamp(columnIndex);
        setRow(columnIndex, x);
        return x;
    }



    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        BigDecimal x = resultSet.getBigDecimal(columnIndex);
        setRow(columnIndex, x);
        return x;
    }

    public Object getObject(int columnIndex) throws SQLException {
        Object x = resultSet.getObject(columnIndex);
        setRow(columnIndex, x);
        return x;
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return resultSet.getAsciiStream(columnIndex);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return resultSet.getUnicodeStream(columnIndex);
    }

    public InputStream getBinaryStream(int columnIndex)
            throws SQLException {
        return resultSet.getBinaryStream(columnIndex);
    }

    public String getString(String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return resultSet.getBoolean(columnName);
    }

    public byte getByte(String columnName) throws SQLException {
        return resultSet.getByte(columnName);
    }

    public short getShort(String columnName) throws SQLException {
        return resultSet.getShort(columnName);
    }

    public int getInt(String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }

    public long getLong(String columnName) throws SQLException {
        return resultSet.getLong(columnName);
    }

    public float getFloat(String columnName) throws SQLException {
        return resultSet.getFloat(columnName);
    }

    public double getDouble(String columnName) throws SQLException {
        return resultSet.getDouble(columnName);
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return resultSet.getBigDecimal(columnName,scale);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return resultSet.getBytes(columnName);
    }

    public Date getDate(String columnName) throws SQLException {
        return resultSet.getDate(columnName);
    }

    public Time getTime(String columnName) throws SQLException {
        return resultSet.getTime(columnName);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return resultSet.getTimestamp(columnName);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return resultSet.getAsciiStream(columnName);
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return resultSet.getUnicodeStream(columnName);
    }

    public InputStream getBinaryStream(String columnName)
            throws SQLException {
        return resultSet.getBinaryStream(columnName);
    }

    public SQLWarning getWarnings() throws SQLException {
        return resultSet.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        resultSet.clearWarnings();
    }

    public String getCursorName() throws SQLException {
        return resultSet.getCursorName();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return resultSet.getMetaData();
    }

    public Object getObject(String columnName) throws SQLException {
        return resultSet.getObject(columnName);
    }

    public int findColumn(String columnName) throws SQLException {
        return resultSet.findColumn(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return resultSet.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return resultSet.getCharacterStream(columnName);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return resultSet.getBigDecimal(columnName);
    }

    public boolean isBeforeFirst() throws SQLException {
        return resultSet.isBeforeFirst();
    }

    public boolean isAfterLast() throws SQLException {
        return resultSet.isAfterLast();
    }

    public boolean isFirst() throws SQLException {
        return resultSet.isFirst();
    }

    public boolean isLast() throws SQLException {
        return resultSet.isLast();
    }

    public void beforeFirst() throws SQLException {
        resultSet.beforeFirst();
    }

    public void afterLast() throws SQLException {
        resultSet.afterLast();
    }

    public boolean first() throws SQLException {
        return resultSet.first();
    }

    public boolean last() throws SQLException {
        JdbcResultSetEvent ev = new JdbcResultSetEvent(
            0, this, null, JdbcStatementEvent.RS_LAST);
        addRowToEvent(ev);
        pes.log(ev);
        try {
            boolean ans = resultSet.last();
            ev.setNext(ans);
            return ans;
        } catch (SQLException e) {
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public int getRow() throws SQLException {
        JdbcResultSetEvent ev = new JdbcResultSetEvent(
            0, this, null, JdbcStatementEvent.RS_GET_ROW);
        addRowToEvent(ev);
        pes.log(ev);
        try {
            int ans = resultSet.getRow();
            ev.setRows(ans);
            return ans;
        } catch (SQLException e) {
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public boolean absolute(int row) throws SQLException {
        JdbcResultSetEvent ev = new JdbcResultSetEvent(
            0, this, null, JdbcStatementEvent.RS_ABSOLUTE);
        ev.setRows(row);
        addRowToEvent(ev);
        pes.log(ev);
        try {
            boolean ans = resultSet.absolute(row);
            ev.setNext(ans);
            return ans;
        } catch (SQLException e) {
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public boolean relative(int rows) throws SQLException {
        JdbcResultSetEvent ev = new JdbcResultSetEvent(
            0, this, null, JdbcStatementEvent.RS_RELATIVE);
        ev.setRows(rows);
        addRowToEvent(ev);
        pes.log(ev);
        try {
            boolean ans = resultSet.relative(rows);
            ev.setNext(ans);
            return ans;
        } catch (SQLException e) {
            ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            ev.setErrorMsg(e);
            throw e;
        } finally {
            ev.updateTotalMs();
        }
    }

    public boolean previous() throws SQLException {
        return resultSet.previous();
    }

    public void setFetchDirection(int direction) throws SQLException {
        resultSet.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return resultSet.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        JdbcLogEvent ev = null;
        if (pes.isFiner()) {
            ev = new JdbcLogEvent(0,
                JdbcLogEvent.RS_FETCH_SIZE, Integer.toString(rows));
            pes.log(ev);
        }
        try {
            resultSet.setFetchSize(rows);
        } catch (SQLException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } catch (RuntimeException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public int getFetchSize() throws SQLException {
        return resultSet.getFetchSize();
    }

    public int getType() throws SQLException {
        return resultSet.getType();
    }

    public int getConcurrency() throws SQLException {
        return resultSet.getConcurrency();
    }

    public boolean rowUpdated() throws SQLException {
        return resultSet.rowUpdated();
    }

    public boolean rowInserted() throws SQLException {
        return resultSet.rowInserted();
    }

    public boolean rowDeleted() throws SQLException {
        return resultSet.rowDeleted();
    }

    public void updateNull(int columnIndex) throws SQLException {
        resultSet.updateNull(columnIndex);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        resultSet.updateBoolean(columnIndex,x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        resultSet.updateByte(columnIndex,x);
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        resultSet.updateShort(columnIndex,x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        resultSet.updateInt(columnIndex,x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        resultSet.updateLong(columnIndex,x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        resultSet.updateFloat(columnIndex,x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        resultSet.updateDouble(columnIndex,x);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        resultSet.updateBigDecimal(columnIndex,x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        resultSet.updateString(columnIndex,x);
    }

    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        resultSet.updateBytes(columnIndex,x);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        resultSet.updateDate(columnIndex,x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        resultSet.updateTime(columnIndex,x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException {
        resultSet.updateTimestamp(columnIndex,x);
    }

    public void updateAsciiStream(int columnIndex,
                                  InputStream x,
                                  int length) throws SQLException {
        resultSet.updateAsciiStream(columnIndex,x,length);
    }

    public void updateBinaryStream(int columnIndex,
                                   InputStream x,
                                   int length) throws SQLException {
        resultSet.updateBinaryStream(columnIndex,x,length);
    }

    public void updateCharacterStream(int columnIndex,
                                      Reader x,
                                      int length) throws SQLException {
        resultSet.updateCharacterStream(columnIndex,x,length);
    }

    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException {
        resultSet.updateObject(columnIndex,x,scale);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        resultSet.updateObject(columnIndex,x);
    }

    public void updateNull(String columnName) throws SQLException {
        resultSet.updateNull(columnName);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        resultSet.updateBoolean(columnName,x);
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        resultSet.updateByte(columnName,x);
    }

    public void updateShort(String columnName, short x) throws SQLException {
        resultSet.updateShort(columnName,x);
    }

    public void updateInt(String columnName, int x) throws SQLException {
        resultSet.updateInt(columnName,x);
    }

    public void updateLong(String columnName, long x) throws SQLException {
        resultSet.updateLong(columnName,x);
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        resultSet.updateFloat(columnName,x);
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        resultSet.updateDouble(columnName,x);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        resultSet.updateBigDecimal(columnName,x);
    }

    public void updateString(String columnName, String x) throws SQLException {
        resultSet.updateString(columnName,x);
    }

    public void updateBytes(String columnName, byte x[]) throws SQLException {
        resultSet.updateBytes(columnName,x);
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        resultSet.updateDate(columnName,x);
    }

    public void updateTime(String columnName, Time x) throws SQLException {
          resultSet.updateTime(columnName,x);
    }

    public void updateTimestamp(String columnName, Timestamp x)
            throws SQLException {
        resultSet.updateTimestamp(columnName,x);
    }

    public void updateAsciiStream(String columnName,
                                  InputStream x,
                                  int length) throws SQLException {
        resultSet.updateAsciiStream(columnName,x,length);
    }

    public void updateBinaryStream(String columnName,
                                   InputStream x,
                                   int length) throws SQLException {
        resultSet.updateBinaryStream(columnName,x,length);
    }

    public void updateCharacterStream(String columnName,
                                      Reader reader,
                                      int length) throws SQLException {
        resultSet.updateCharacterStream(columnName,reader,length);
    }

    public void updateObject(String columnName, Object x, int scale)
            throws SQLException {
        resultSet.updateObject(columnName,x,scale);
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        resultSet.updateObject(columnName,x);
    }

    public void insertRow() throws SQLException {
        resultSet.insertRow();
    }

    public void updateRow() throws SQLException {
        resultSet.updateRow();
    }

    public void deleteRow() throws SQLException {
        resultSet.deleteRow();
    }

    public void refreshRow() throws SQLException {
        resultSet.refreshRow();
    }

    public void cancelRowUpdates() throws SQLException {
        resultSet.cancelRowUpdates();
    }

    public void moveToInsertRow() throws SQLException {
        resultSet.moveToInsertRow();
    }

    public void moveToCurrentRow() throws SQLException {
        resultSet.moveToCurrentRow();
    }

    public Statement getStatement() throws SQLException {
        return proxyStatement;
    }

    public Object getObject(int i, Map map) throws SQLException {
        return resultSet.getObject(i,map);
    }

    public Ref getRef(int i) throws SQLException {
        return resultSet.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return resultSet.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return resultSet.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return resultSet.getArray(i);
    }

    public Object getObject(String colName, Map map) throws SQLException {
        return resultSet.getObject(colName,map);
    }

    public Ref getRef(String colName) throws SQLException {
        return resultSet.getRef(colName);
    }

    public Blob getBlob(String colName) throws SQLException {
        return resultSet.getBlob(colName);
    }

    public Clob getClob(String colName) throws SQLException {
        return resultSet.getClob(colName);
    }

    public Array getArray(String colName) throws SQLException {
        return resultSet.getArray(colName);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return resultSet.getDate(columnIndex,cal);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return resultSet.getDate(columnName,cal);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return resultSet.getTime(columnIndex,cal);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return resultSet.getTime(columnName,cal);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException {
        return resultSet.getTimestamp(columnIndex,cal);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException {
        return resultSet.getTimestamp(columnName,cal);
    }

    //#####################################################################//
    //#################### this stuff is for jdk1.4 #######################//
    //#####################################################################//
    public URL getURL(int columnIndex) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public URL getURL(String columnName) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateRef(String columnName, Ref x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateBlob(String columnName, Blob x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateClob(String columnName, Clob x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
    public void updateArray(String columnName, Array x) throws SQLException {
        throw BindingSupportImpl.getInstance().unsupported("not implemented.");
    }
}

