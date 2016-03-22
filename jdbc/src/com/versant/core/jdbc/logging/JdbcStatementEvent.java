
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

import java.sql.Statement;
import java.sql.ResultSet;

/**
 * A JDBC Statement, PreparedStatement or CallableStatement related event.
 * @keep-all
 */
public class JdbcStatementEvent extends JdbcLogEvent {

    private int statementID;
    private int resultSetID;
    private int updateCount;
    private boolean hasResultSet;
    private int[] updateCounts;

    public JdbcStatementEvent(long txId, Statement stat, String descr, int type) {
        super(txId, type, descr);
        this.statementID = System.identityHashCode(stat);
    }

    public int getStatementID() {
        return statementID;
    }

    public int getResultSetID() {
        return resultSetID;
    }

    public void updateResultSetID(ResultSet rs) {
        resultSetID = System.identityHashCode(rs);
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public boolean isHasResultSet() {
        return hasResultSet;
    }

    public void setHasResultSet(boolean hasResultSet) {
        this.hasResultSet = hasResultSet;
    }

    public int[] getUpdateCounts() {
        return updateCounts;
    }

    public void setUpdateCounts(int[] updateCounts) {
        this.updateCounts = updateCounts;
    }

    public int getResourceID() {
        return statementID;
    }
}

