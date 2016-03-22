
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
package com.versant.core.jdo;

import java.io.Serializable;

/**
 * Info on the status of a connection pool for a datastore.
 * @keep-all
 */
public class PoolStatus implements Serializable {

    private String datastore;
    private int active;
    private int maxActive;
    private int idle;
    private int maxIdle;

    public PoolStatus() {
    }

    public PoolStatus(String datastore) {
        this.datastore = datastore;
    }

    public void fill(int maxActive, int active, int maxIdle, int idle) {
        this.maxActive = maxActive;
        this.active = active;
        this.maxIdle = maxIdle;
        this.idle = idle;
    }

    public String getDatastore() {
        return datastore;
    }

    public int getActive() {
        return active;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public String getActiveStr() {
        return active + "/" + maxActive;
    }

    public int getIdle() {
        return idle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public String getIdleStr() {
        return idle + "/" + maxIdle;
    }

    public String toString() {
        return datastore +  " active " + active + "/" + maxActive +
            " idle " + idle + "/" + maxIdle;
    }
}

