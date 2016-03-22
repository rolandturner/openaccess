
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

import java.sql.Connection;
import java.sql.Statement;

/**
 * A JDBC Connection related event (open, close etc.).
 */
public class JdbcConnectionEvent extends JdbcLogEvent {

    private int connectionID;
    private int statementID;
    private int resultSetType;
    private int resultSetConcurrency;

    public JdbcConnectionEvent(long txId, Connection con, String descr, int type) {
        super(txId, type, descr);
        this.connectionID = System.identityHashCode(con);
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void updateConnectionID(Connection con) {
        connectionID = System.identityHashCode(con);
    }

    public int getStatementID() {
        return statementID;
    }

    public void updateStatementID(Statement stat) {
        statementID = System.identityHashCode(stat);
    }

    public int getResultSetType() {
        return resultSetType;
    }

    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    public int getResultSetConcurrency() {
        return resultSetConcurrency;
    }

    public void setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public boolean isScrollable() {
        return resultSetType != 0;
    }

}
