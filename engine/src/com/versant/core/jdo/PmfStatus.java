
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
import java.util.Date;

/**
 * Info on the status of a JdoGeniePersistenceManagerFactory.
 * @see com.versant.core.jdo.VersantPersistenceManagerFactory#getPmfStatus
 */
public class PmfStatus implements Serializable {

    private String server;
    private Date date;
    private int freeK;
    private int totalK;

    public PmfStatus() {
        date = new Date();
        freeK = (int)(Runtime.getRuntime().freeMemory() >> 10);
        totalK = (int)(Runtime.getRuntime().totalMemory() >> 10);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Date getDate() {
        return date;
    }

    public int getFreeK() {
        return freeK;
    }

    public int getTotalK() {
        return totalK;
    }

}
