
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
package com.versant.core.jdbc.logging;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;

/**
 * An JDBC ResultSet related event.
 * @keep-all
 */
public class JdbcResultSetEvent extends JdbcLogEvent {

    private int statementID;
    private int resultSetID;
    private boolean next;
    private Object[] row;
    private int rows;

    public JdbcResultSetEvent(long txId, ResultSet rs, String descr, int type) {
        super(txId, type, descr);
        this.resultSetID = System.identityHashCode(rs);
        try {
            Statement s = rs.getStatement();
            if (s != null) statementID = System.identityHashCode(s);
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        StringBuffer s = new StringBuffer();
        if (descr != null) {
            s.append(descr);
            s.append(' ');
        }
        if (row != null) {
            s.append('[');
            int n = row.length;
            for (int i = 0; i < n; i++) {
                if (i > 0) s.append(", ");
                s.append(row[i]);
            }
            s.append(']');
        }
        return s.toString();
    }

    public int getStatementID() {
        return statementID;
    }

    public void setStatementID(int statementID) {
        this.statementID = statementID;
    }

    public int getResultSetID() {
        return resultSetID;
    }

    public void setResultSetID(int resultSetID) {
        this.resultSetID = resultSetID;
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public Object[] getRow() {
        return row;
    }

    public void setRow(Object[] row) {
        this.row = row;
    }

    public List getRowList() {
        return Arrays.asList(row);
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getResourceID() {
        return statementID;
    }
}

