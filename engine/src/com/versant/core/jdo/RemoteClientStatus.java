
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
 * Information on a remote PMF connection to the VOA server.
 */
public class RemoteClientStatus implements Serializable {

    private int id;
    private long timeCreated;
    private long timeLastActive;
    private long timeLastPMCreated;
    private String remoteClient;
    private Object userObject;

    public RemoteClientStatus() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public void setTimeLastActive(long timeLastActive) {
        this.timeLastActive = timeLastActive;
    }

    public void setTimeLastPMCreated(long timeLastPMCreated) {
        this.timeLastPMCreated = timeLastPMCreated;
    }

    public void setRemoteClient(String remoteClient) {
        this.remoteClient = remoteClient;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    public int getId() {
        return id;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public long getTimeLastPMCreated() {
        return timeLastPMCreated;
    }

    public long getTimeLastActive() {
        return timeLastActive;
    }

    public String getRemoteClient() {
        return remoteClient;
    }

    public Object getUserObject() {
        return userObject;
    }

}
