
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

/**
 * Event logged for connection pool operations.
 */
public class JdbcPoolEvent extends JdbcLogEvent {

    private int connectionID;
    private int maxActive;
    private int active;
    private int maxIdle;
    private int idle;
    private boolean highPriority;
    private boolean autoCommit;

    public JdbcPoolEvent(long txId, int type, boolean highPriority) {
        super(txId, type, null);
        this.highPriority = highPriority;
    }

    public void update(int maxActive, int active, int maxIdle, int idle) {
        updateTotalMs();
        this.maxActive = maxActive;
        this.active = active;
        this.maxIdle = maxIdle;
        this.idle = idle;
    }

    public String getDescription() {
        if (maxActive == 0) return "Busy ...";
        if (descr == null) {
            descr = "active " + active + "/" + maxActive +
                    " idle " + idle + "/" + maxIdle + (autoCommit ? " AC" : "");
        }
        return descr;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public int getResourceID() {
        return connectionID;
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public int getActive() {
        return active;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public int getIdle() {
        return idle;
    }

}
