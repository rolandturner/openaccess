
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
 * This is a read only value object to report the status of an jdo client
 * connection i.e. a PersistenceManager (local or remote).
 */
public final class PersistenceManagerStatus implements Serializable {

    private final int id;
    private final long timeCreated;
    private final boolean txActive;
    private final long lastTxStarted;
    private final long timeLastTxEnded;
    private final Object userObject;
    private final String remoteClient;
    private final boolean optimistic;
    private final boolean open;

    public PersistenceManagerStatus(int id, long timeCreated, boolean txActive,
            long lastTxStarted, long timeLastTxEnded,
            Object userObject, String remoteClient,
            boolean optimistic, boolean open) {
        this.id = id;
        this.timeCreated = timeCreated;
        this.txActive = txActive;
        this.lastTxStarted = lastTxStarted;
        this.timeLastTxEnded = timeLastTxEnded;
        this.userObject = userObject;
        this.remoteClient = remoteClient;
        this.optimistic = optimistic;
        this.open = open;
    }

    public int getId() {
        return id;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public boolean getTxActive() {
        return txActive;
    }

    public long getLastTxStarted() {
        return lastTxStarted;
    }

    public long getTimeLastTxEnded() {
        return timeLastTxEnded;
    }

    public Object getUserObject() {
        return userObject;
    }

    public String getRemoteClient() {
        return remoteClient;
    }

    public boolean getOptimistic() {
        return optimistic;
    }

    public boolean isRemote() {
        return remoteClient != null;
    }

    public boolean isOpen() {
        return open;
    }

}
