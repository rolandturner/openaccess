
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
package com.versant.core.logging;

import java.io.Serializable;

/**
 * The base class for events. These are used for logging query execution
 * times etc. for performance tuning and debugging. Every event created in
 * a VM is assigned a unique id.
 */
public abstract class LogEvent implements Serializable {

    protected int id;
    protected String remoteClient;
    protected String userString;
    protected long datastoreTxId;
    protected long start;
    protected int totalMs;
    protected String errorMsg;

    private static int lastId;

    private static ThreadLocal contextStore = new ThreadLocal();


	

    /**
     * Information associated with the current thread that is added to all
     * events logged.
     */
    public static class Context {
        public String remoteClient;
        public String userString;
    }

    public LogEvent() {
        id = ++lastId;
        Context c = getContext();
        remoteClient = c.remoteClient;
        userString = c.userString;
        totalMs = -1;
        start = System.currentTimeMillis();
    }

    /**
     * Get the event context for the calling thread.
     */
    public static Context getContext() {

        Context c = (Context)contextStore.get();
        if (c == null) contextStore.set(c = new Context());
        return c;


    }

    /**
     * Get the ID of the last event logged.
     */
    public static int getLastId() {
        return lastId;
    }

    public long getStart() {
        return start;
    }

    public final int getId() {
        return id;
    }

    public long getDatastoreTxId() {
        return datastoreTxId;
    }

    public void setDatastoreTxId(long datastoreTxId) {
        this.datastoreTxId = datastoreTxId;
    }

    public int getTotalMs() {
        return totalMs;
    }

    public void setTotalMs(int totalMs) {
        this.totalMs = totalMs;
    }

    public void updateTotalMs() {
        totalMs = (int)(System.currentTimeMillis() - start);
    }

    public void zeroTotalMs() {
        totalMs = 0;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setErrorMsg(Throwable t) {
        errorMsg = t.getClass().getName() + ": " + t.getMessage();
    }

    public boolean getOk() {
        return errorMsg == null;
    }

    public String getRemoteClient() {
        if (remoteClient == null) {
            return userString;
        } else if (userString == null) {
            return remoteClient;
        } else {
            return remoteClient + " - " + userString;
        }
    }

    /**
     * Get a short descriptive name for this event.
     */
    public abstract String getName();

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public abstract String getDescription();

    /**
     * Dummy set method so the Workbench will allow 'editing' for cut and
     * paste.
     */
    public void setDescription(String s) {
    }

    public String toString() {
        String d = getDescription();
        if (d == null) return getName();
        else return getName() + " " + d;
    }

    /**
     * Should this event be sorted onto its own tab in the perf monitoring
     * form? Major events e.g. executing a query should return true here.
     */
    public boolean isOwnTab() {
        return false;
    }

    /**
     * If this event has an int type then it is returned.
     */
    public int getType() {
        return 0;
    }

}
