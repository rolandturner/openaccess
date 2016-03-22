
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

import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEvent;

/**
 * JDBC related performance events.
 */
public class JdbcLogEvent extends LogEvent {

    protected int type;
    protected String descr;

    public static final int CON_OPEN = 0;
    public static final int CON_CLOSE = 1;
    public static final int CON_COMMIT = 2;
    public static final int CON_ROLLBACK = 3;
    public static final int CON_AUTOCOMMIT = 4;
    public static final int CON_ISOLATION = 30;
    public static final int CON_CREATE_STATEMENT = 5;
    public static final int CON_PREPARE_STATEMENT = 6;
    public static final int CON_PREPARE_CALL = 7;

    public static final int STAT_EXEC_QUERY = 8;
    public static final int STAT_EXEC_UPDATE = 9;
    public static final int STAT_EXEC = 10;
    public static final int STAT_CLOSE = 11;
    public static final int STAT_ADD_BATCH = 12;
    public static final int STAT_EXEC_BATCH = 13;
    public static final int STAT_MAX_ROWS = 28;

    public static final int RS_CLOSE = 14;
    public static final int RS_NEXT = 15;
    public static final int RS_RELATIVE = 16;
    public static final int RS_ABSOLUTE = 17;
    public static final int RS_LAST = 18;
    public static final int RS_GET_ROW = 19;
    public static final int RS_FETCH_SIZE = 20;

    public static final int QUERY_COMPILE = 21;

    public static final int POOL_ALLOC = 22;
    public static final int POOL_RELEASE = 23;
    public static final int POOL_BAD_CON = 24;
    public static final int POOL_CON_FAILED= 25;
    public static final int POOL_CON_TIMEOUT= 29;
    public static final int POOL_CON_EXPIRED = 32;
    public static final int POOL_FULL= 31;

    public static final int PSPOOL_ALLOC = 26;
    public static final int PSPOOL_RELEASE = 27;

    public JdbcLogEvent(long txId, int type, String descr) {
        this.datastoreTxId = txId;
        this.type = type;
        this.descr = descr;
    }

    /**
     * Get a short descriptive name for this event.
     */
    public String getName() {
        switch (type) {
            case CON_OPEN:              return "jdbc.con.connect";
            case CON_CLOSE:             return "jdbc.con.close";
            case CON_COMMIT:            return "jdbc.con.commit";
            case CON_ROLLBACK:          return "jdbc.con.rollback";
            case CON_AUTOCOMMIT:        return "jdbc.con.autoCommit";
            case CON_ISOLATION:         return "jdbc.con.isolation";
            case CON_CREATE_STATEMENT:  return "jdbc.con.createStat";
            case CON_PREPARE_STATEMENT: return "jdbc.con.prepareStat";
            case CON_PREPARE_CALL:      return "jdbc.con.prepareCall";

            case STAT_EXEC_QUERY:       return "jdbc.stat.execQuery";
            case STAT_EXEC_UPDATE:      return "jdbc.stat.execUpdate";
            case STAT_EXEC:             return "jdbc.stat.exec";
            case STAT_CLOSE:            return "jdbc.stat.close";
            case STAT_ADD_BATCH:        return "jdbc.stat.addBatch";
            case STAT_EXEC_BATCH:       return "jdbc.stat.execBatch";
            case STAT_MAX_ROWS:         return "jdbc.stat.maxrows";

            case RS_CLOSE:              return "jdbc.rs.close";
            case RS_NEXT:               return "jdbc.rs.next";
            case RS_RELATIVE:           return "jdbc.rs.relative";
            case RS_ABSOLUTE:           return "jdbc.rs.absolute";
            case RS_LAST:               return "jdbc.rs.last";
            case RS_GET_ROW:            return "jdbc.rs.getrow";
            case RS_FETCH_SIZE:         return "jdbc.rs.fetchsize";

            case QUERY_COMPILE:         return "jdbc.query.compile";

            case POOL_ALLOC:            return "jdbc.pool.alloc";
            case POOL_RELEASE:          return "jdbc.pool.release";
            case POOL_BAD_CON:          return "jdbc.pool.badcon";
            case POOL_CON_FAILED:       return "jdbc.pool.confailed";
            case POOL_CON_TIMEOUT:      return "jdbc.pool.contimeout";
            case POOL_CON_EXPIRED:      return "jdbc.pool.conexpired";
            case POOL_FULL:             return "jdbc.pool.full";

            case PSPOOL_ALLOC:          return "jdbc.pspool.alloc";
            case PSPOOL_RELEASE:        return "jdbc.pspool.release";
        }
        return "jdbc.UNKNOWN(" + type + ")";
    }

    public String toString() {
        String d = getDescription();
        if (d == null) {
            return getName();
        } else {
            StringBuffer s = new StringBuffer();
            s.append(getName());
            for (int n = 22 - s.length(); n > 0; n--) s.append(' ');
            s.append(d);
            return s.toString();
        }
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    /**
     * Get a long description for this event (e.g. the query text).
     */
    public String getDescription() {
        return descr;
    }

    public boolean isOwnTab() {
        switch (type) {
            case STAT_EXEC:
            case STAT_EXEC_BATCH:
            case STAT_EXEC_QUERY:
            case STAT_EXEC_UPDATE:
                return true;
        }
        return false;
    }

    /**
     * Get the type of this event (STAT_EXEC_QUERY etc).
     */
    public int getType() {
        return type;
    }
}
