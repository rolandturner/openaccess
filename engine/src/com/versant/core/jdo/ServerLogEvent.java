
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

import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEvent;

/**
 * Server side performance events.
 */
public class ServerLogEvent extends LogEvent {

    protected int type;
    protected String description;

    public static final int JDOQL_COMPILE = 1;
    public static final int JDOQL_EXEC = 2;
    public static final int JDOQL_EXEC_COUNT = 33;
    public static final int SET_PROPERTY = 3;
    public static final int REMOTE_PMF_CONNECT = 4;
    public static final int REMOTE_PMF_DISCONNECT = 5;
    public static final int REMOTE_PMF_BAD_AUTH = 6;
    public static final int REMOTE_PMF_DISABLED = 7;
    public static final int REMOTE_PMF_BAD_VERSION = 8;
    public static final int REMOTE_PM_DISABLED = 12;
    public static final int PM_CREATED = 13;
    public static final int PM_CLOSED = 14;
    public static final int PM_CLOSED_AUTO = 15;
    public static final int PM_CLOSED_AUTO_TX = 16;
    public static final int TX_BEGIN = 17;
    public static final int TX_BEGIN_DATASTORE = 18;
    public static final int TX_COMMIT = 19;
    public static final int TX_ROLLBACK = 20;
    public static final int TX_FLUSH_AUTO = 21;
    public static final int TX_FLUSH = 22;
    public static final int PMF_EVICT = 23;
    public static final int PMF_EVICT_ALL = 24;
    public static final int PMF_EVICT_CLASS = 25;
    public static final int PM_ALLOC = 26;
    public static final int PM_RELEASE = 27;
    public static final int LOCK_CLEANUP = 28;
    public static final int JDOQL_CACHE_HIT = 29;
    public static final int JDOQL_CACHE_EVICT = 30;
    public static final int JDOQL_CACHE_FAIL = 31;
    public static final int SET_DATASTORE_TX_LOCKING = 32;
    public static final int GET_STATE = 34;
    public static final int GET_STATE_MULTI = 35;
    public static final int GET_QUERY_BATCH = 36;
    public static final int GET_QUERY_ALL = 37;
    public static final int USER = 38;
    public static final int SET_RETAIN_CONNECTION_IN_OPT_TX = 39;

    public ServerLogEvent(int type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * Get a short descriptive name for this event.
     */
    public String getName() {
        switch (type) {
            case JDOQL_COMPILE:             return "jdoql.compile";
            case JDOQL_EXEC:                return "jdoql.exec";
            case JDOQL_EXEC_COUNT:          return "jdoql.exec.count";
            case JDOQL_CACHE_HIT:           return "jdoql.cache.hit";
            case JDOQL_CACHE_EVICT:         return "jdoql.cache.evict";
            case JDOQL_CACHE_FAIL:          return "jdoql.cache.fail";
            case GET_STATE:                 return "get.state";
            case GET_STATE_MULTI:           return "get.state.multi";
            case GET_QUERY_BATCH:           return "get.query.batch";
            case GET_QUERY_ALL:             return "get.query.all";
            case PMF_EVICT:                 return "pmf.evict";
            case PMF_EVICT_ALL:             return "pmf.evict.all";
            case PMF_EVICT_CLASS:           return "pmf.evict.class";
            case REMOTE_PMF_CONNECT:        return "remote.pmf.connect";
            case REMOTE_PMF_DISCONNECT:     return "remote.pmf.disconnect";
            case REMOTE_PMF_BAD_AUTH:       return "remote.pmf.badauth";
            case REMOTE_PMF_DISABLED:       return "remote.pmf.disabled";
            case REMOTE_PMF_BAD_VERSION:    return "remote.pmf.bad.version";
            case REMOTE_PM_DISABLED:        return "remote.pm.disabled";
            case PM_CREATED:                return "pm.created";
            case PM_CLOSED:                 return "pm.closed";
            case PM_CLOSED_AUTO:            return "pm.closed.auto";
            case PM_CLOSED_AUTO_TX:         return "pm.closed.auto.tx";
            case PM_ALLOC:                  return "pm.alloc";
            case PM_RELEASE:                return "pm.release";
            case SET_DATASTORE_TX_LOCKING:  return "set.ds.tx.locking";
            case SET_RETAIN_CONNECTION_IN_OPT_TX:  return "set.retain.con.in.opt.tx";
            case TX_BEGIN:                  return "tx.begin";
            case TX_BEGIN_DATASTORE:        return "tx.begin.datastore";
            case TX_COMMIT:                 return "tx.commit";
            case TX_ROLLBACK:               return "tx.rollback";
            case TX_FLUSH_AUTO:             return "tx.flush.auto";
            case TX_FLUSH:                  return "tx.flush";
            case LOCK_CLEANUP:              return "lock.cleanup";
            case SET_PROPERTY:              return "set.property";
            case USER:                      return "user";
        }
        return "UNKNOWN(" + type + ")";
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
