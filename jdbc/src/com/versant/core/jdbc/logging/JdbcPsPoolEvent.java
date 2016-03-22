
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

/**
 * Event logged for PreparedStatement pool operations.
 */
public class JdbcPsPoolEvent extends JdbcConnectionEvent {

    private int numActive;
    private int numIdle;
    private int statementID;

    public JdbcPsPoolEvent(long txId, Connection con, String descr, int type,
            int numActive, int numIdle) {
        super(txId, con, descr, type);
        this.numActive = numActive;
        this.numIdle = numIdle;
    }

    public int getNumActive() {
        return numActive;
    }

    public int getNumIdle() {
        return numIdle;
    }

    public int getStatementID() {
        return statementID;
    }

    public void setStatementID(int statementID) {
        this.statementID = statementID;
    }

    public String getDescription() {
        return numActive + "/" + numIdle + " " + super.getDescription();
    }

    public int getResourceID() {
        return statementID;
    }

}
