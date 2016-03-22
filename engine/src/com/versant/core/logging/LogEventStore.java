
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

import com.versant.core.common.BindingSupportImpl;

/**
 * This stores a configurable number of events in a ring buffer and provides
 * API's to access the buffer.
 */
public final class LogEventStore {

    private int size = 1000;
    private LogEvent[] buf = new LogEvent[size];
    private int pos;        // where the next event is stored in buf
    private int count;
    private int eventsLogged;

    public static final String LOG_EVENTS_NONE = "none";
    public static final String LOG_EVENTS_ERRORS = "errors";
    public static final String LOG_EVENTS_NORMAL = "normal";
    public static final String LOG_EVENTS_VERBOSE = "verbose";
    public static final String LOG_EVENTS_ALL = "all";

    private String logEvents;

    private boolean severe;
    private boolean warning;
    private boolean info;
    private boolean config;
    private boolean fine;
    private boolean finer;
    private boolean finest;

    private boolean logEventsToSysOut = true;

    public LogEventStore() {
        setLogEvents(LOG_EVENTS_NORMAL);
    }

    public synchronized void log(LogEvent ev) {
        synchronized (this) {
            buf[pos] = ev;
            pos = (pos + 1) % size;
            if (count < size) count++;
        }
        eventsLogged++;
        if (logEventsToSysOut) System.out.println(ev);
    }

    public int getMaxEvents() {
        return size;
    }

    /**
     * Get the number of events logged so far.
     */
    public int getEventsLogged() {
        return eventsLogged;
    }

    /**
     * Set the maximum number of events to store in the buffer.
     */
    public synchronized void setMaxEvents(int max) {
        LogEvent[] old = copyEvents(0);
        buf = new LogEvent[size = max];
        if (old != null) {
            int n = old.length;
            if (n > max) n = max;
            System.arraycopy(old, old.length - n, buf, 0, n);
            count = n;
            pos = n % size;
        } else {
            count = 0;
            pos = 0;
        }
    }

    public synchronized LogEvent[] copyEvents(int id) {
        if (count < size) {
            int first;
            for (first = pos - 1; first >= 0; first--) {
                if (buf[first].getId() == id) break;
            }
            first++;
            int n = pos - first;
            if (n == 0) return null;
            LogEvent[] ans = new LogEvent[n];
            System.arraycopy(buf, first, ans, 0, n);
            return ans;
        } else {
            if (buf[(pos + size - 1) % size].getId() == id) return null;
            int first = pos;
            int c = size;
            for (; c > 0; first = (first + 1) % size, c--) {
                if (buf[first].getId() == id) break;
            }
            first = (first + 1) % size;
            if (first >= pos) {
                int h1 = size - first;
                int h2 = pos;
                LogEvent[] ans = new LogEvent[h1 + h2];
                System.arraycopy(buf, first, ans, 0, h1);
                System.arraycopy(buf, 0, ans, h1, h2);
                return ans;
            } else {
                int n = pos - first;
                LogEvent[] ans = new LogEvent[n];
                System.arraycopy(buf, first, ans, 0, n);
                return ans;
            }
        }
    }

    public boolean isSevere() {
        return severe;
    }

    public boolean isWarning() {
        return warning;
    }

    public boolean isInfo() {
        return info;
    }

    public boolean isConfig() {
        return config;
    }

    public boolean isFine() {
        return fine;
    }

    public boolean isFiner() {
        return finer;
    }

    public boolean isFinest() {
        return finest;
    }

    /**
     * Return a Serializable Javabean that provides status information on
     * this component. This may be null if not supported.
     */
    public Object getStatusBean() {
        return null;
    }

    public String getLogEvents() {
        return logEvents;
    }

    public void setLogEvents(String logEvents) {
        if (logEvents.equals(LOG_EVENTS_NONE)) {
            severe = false;
            warning = false;
            info = false;
            config = false;
            fine = false;
            finer = false;
            finest = false;
        } else if (logEvents.equals(LOG_EVENTS_ERRORS)) {
            severe = true;
            warning = false;
            info = false;
            config = false;
            fine = false;
            finer = false;
            finest = false;
        } else if (logEvents.equals(LOG_EVENTS_NORMAL)) {
            severe = true;
            warning = true;
            info = true;
            config = false;
            fine = false;
            finer = false;
            finest = false;
        } else if (logEvents.equals(LOG_EVENTS_VERBOSE)) {
            severe = true;
            warning = true;
            info = true;
            config = true;
            fine = true;
            finer = false;
            finest = false;
        } else if (logEvents.equals(LOG_EVENTS_ALL)) {
            severe = true;
            warning = true;
            info = true;
            config = true;
            fine = true;
            finer = true;
            finest = true;
        } else {
            throw BindingSupportImpl.getInstance().illegalArgument(
                    "Invalid group: '" + logEvents + "'");
        }
        this.logEvents = logEvents;
    }

    public boolean isLogEventsToSysOut() {
        return logEventsToSysOut;
    }

    public void setLogEventsToSysOut(boolean logEventsToSysOut) {
        this.logEventsToSysOut = logEventsToSysOut;
    }

}
