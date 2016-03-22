
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
package com.versant.core.jdbc.conn;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import com.versant.core.common.Debug;
import com.versant.core.common.BindingSupportImpl;

/** 
 * Count limited LRU PreparedStatement pool for LoggingConnection.
 */
public final class PreparedStatementPool {

    private final LoggingConnection con;
    private final HashMap keys = new HashMap(); // maps Key -> Key
    private Key head;    // most recently accessed Key
    private Key tail;    // least recently accessed Key
    private int max;
    private int numActive;
    private int numIdle;

    public PreparedStatementPool(LoggingConnection con, int max) {
        this.con = con;
        this.max = max;
    }

    public LoggingConnection getCon() {
        return con;
    }

    public int getMax() {
        return max;
    }

    /**
     * Get the total number of PS currently idle in the pool.
     */
    public int getNumIdle() {
        return numIdle;
    }

    /**
     * Get the number of active PS outside of the pool.
     */
    public int getNumActive() {
        return numActive;
    }

    /**
     * Get the total number of PS (numActive + numIdle).
     */
    public int getSize() {
        return numIdle + numActive;
    }

    /**
     * Borrow a PS from the pool. This will create one if none are available.
     * If this pushes the total number of PSes over max then idle PS'es are
     * closed as needed.
     */
    public PooledPreparedStatement borrowPS(Key pkey) throws SQLException {
        Key key = (Key)keys.get(pkey);
        if (key == null) {
            keys.put(key = pkey, pkey);
        } else {
            removeFromLRUList(key);
        }
        addToHeadOfLRUList(key);
        PooledPreparedStatement ps = key.removePsFromList();
        if (ps == null) {
            ps = con.prepareStatementImp(key.getSql(), key.getResultSetType(),
                    key.getResultSetConcurrency(), key);
        } else {
            --numIdle;
        }
        ++key.activeCount;
        ++numActive;
        if (max > 0) {
            closeExcessStatements();
        }
        if (Debug.DEBUG) {
            check();
        }
        return ps;
    }

    /**
     * Return a PS to the pool.
     */
    public void returnPS(PooledPreparedStatement ps) {
        Key key = ps.getKey();
        key.addPsToList(ps);
        --key.activeCount;
        ++numIdle;
        --numActive;
        if (max > 0) {
            closeExcessStatements();
        }
        if (Debug.DEBUG) {
            check();
        }
    }

    private void closeExcessStatements() {
        int toClose = (numActive + numIdle) - max;
        if (toClose > numIdle) {
            toClose = numIdle;
        }
        for (Key key = tail; toClose > 0; ) {
            for (;;) {
                PooledPreparedStatement ps = key.removePsFromList();
                if (ps == null) {
                    break;
                }
                try {
                    ps.closeRealStatement();
                } catch (SQLException e) {
                    // ignore
                }
                --numIdle;
                if (--toClose == 0) {
                    break;
                }
            }
            if (key.psList == null) {
                if (key.activeCount == 0) {
                    Key next = key.next;
                    removeFromLRUList(key);
                    keys.remove(key);
                    key = next;
                } else {
                    key = key.next;
                }
            }
        }
        if (Debug.DEBUG) {
            check();
        }
    }

    private void check() {
        try {
            // count the number of idle PS'es
            int c = 0;
            for (Iterator i = keys.keySet().iterator(); i.hasNext(); ) {
                Key key = (Key)i.next();
                for (PooledPreparedStatement ps = key.psList; ps != null; ++c) {
                    if (ps == ps.next) {
                        throw BindingSupportImpl.getInstance().internal(
                                "ps == ps.next for key " + key);
                    }
                    ps = ps.next;
                }
            }
            if (numIdle != c) {
                throw BindingSupportImpl.getInstance().internal("numIdle is " +
                        numIdle + " but there are " + c + " idle PS'es in map");
            }
            // walk the LRU list checking the links, detected dups and making
            // sure all entries are in the map and that the map has no extra
            // entries
            c = 0;
            int totActive = 0;
            Map dupMap = new HashMap();
            for (Key key = tail; key != null; key = key.next, ++c) {
                if (key.next != null) {
                    if (key.next.prev != key) {
                        throw BindingSupportImpl.getInstance().internal(
                                "key.next.prev != key");
                    }
                }
                Key pkey = (Key)dupMap.get(key);
                if (pkey != null) {
                    throw BindingSupportImpl.getInstance().internal(
                            "Dup key in LRU list: " + pkey);
                }
                dupMap.put(key, key);
                if (!keys.containsKey(key)) {
                    throw BindingSupportImpl.getInstance().internal(
                            "Key in LRU list not in map: " + key);
                }
                totActive += key.activeCount;
            }
            if (c != keys.size()) {
                throw BindingSupportImpl.getInstance().internal(
                        "There are " + c + " keys in the LRU list and " +
                        keys.size() + " in the keys map");
            }
            if (numActive != totActive) {
                throw BindingSupportImpl.getInstance().internal("numActive is " +
                        numIdle + " but there are " + c + " active PS'es in keys");
            }
        } catch (RuntimeException e) {
            System.out.println("PreparedStatementPool check failed: " + e);
            dump();
            e.printStackTrace(System.out);
            throw e;
        }
    }

    /**
     * Remove key from the double linked LRU list.
     */
    private void removeFromLRUList(Key key) {
        if (key.prev != null) {
            key.prev.next = key.next;
        } else {
            tail = key.next;
        }
        if (key.next != null) {
            key.next.prev = key.prev;
        } else {
            head = key.prev;
        }
    }

    /**
     * Add key to the head of the double linked LRU list. This will make it
     * the most recently accessed object.
     */
    private void addToHeadOfLRUList(Key key) {
        key.next = null;
        key.prev = head;
        if (head != null) head.next = key;
        head = key;
        if (tail == null) tail = key;
    }

    /**
     * Dump the contents of the pool.
     */
    public void dump() {
        if (Debug.DEBUG) {
            System.out.println("=== keys ===");
            for (Iterator i = keys.keySet().iterator(); i.hasNext(); ) {
                Key key = (Key)i.next();
                System.out.println(key.toDumpString());
            }
            System.out.println("=== LRU list ===");
            for (Key key = tail; key != null; key = key.next) {
                System.out.println(key);
            }
            System.out.println("---");
        }
    }

    /**
     * This is the key for this pool. These are linked together when in the
     * pool to form a double linked list used for LRU evictions. Each
     * maintains a single linked list of PooledPreparedStatement's.
     */
    public static final class Key {

        private final String sql;
        private final int resultSetType;
        private final int resultSetConcurrency;

        private PooledPreparedStatement psList;
        private int activeCount;
        private Key prev;
        private Key next;

        public Key(String sql, int resultSetType, int resultSetConcurrency) {
            if (sql == null) {
                throw new NullPointerException("sql is null");
            }
            this.sql = sql;
            this.resultSetType = resultSetType;
            this.resultSetConcurrency = resultSetConcurrency;
        }

        public Key(String sql) {
            if (sql == null) {
                throw new NullPointerException("sql is null");
            }
            this.sql = sql;
            resultSetType = 0;
            resultSetConcurrency = 0;
        }

        public String getSql() {
            return sql;
        }

        public int getResultSetType() {
            return resultSetType;
        }

        public int getResultSetConcurrency() {
            return resultSetConcurrency;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Key)) return false;
            final Key psKey = (Key)o;
            return resultSetConcurrency == psKey.resultSetConcurrency
                && resultSetType == psKey.resultSetType
                && sql.equals(psKey.sql);
        }

        public int hashCode() {
            return sql.hashCode() + resultSetType + resultSetConcurrency;
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append(Integer.toHexString(sql.hashCode()));
            for (int i = 9 - s.length(); i > 0; i--) {
                s.append(' ');
            }
            if (sql.length() > 40) {
                s.append(sql.substring(0, 37));
                s.append("...");
            } else {
                s.append(sql);
            }
            return s.toString();
        }

        public String toDumpString() {
            StringBuffer s = new StringBuffer();
            s.append(toString());
            for (PooledPreparedStatement ps = psList; ps != null; ) {
                s.append(" -> ");
                s.append(ps);
                if (ps == ps.next) {
                    s.append("*** LOOP ps == ps.next ***");
                    break;
                }
                ps = ps.next;
            }
            return s.toString();
        }

        /**
         * Do we have no PS'es?
         */
        public boolean isPsListEmpty() {
            return psList == null;
        }

        /**
         * Remove the first PS from our list and return it or null if none
         * are available.
         */
        public PooledPreparedStatement removePsFromList() {
            if (psList == null) {
                return null;
            }
            PooledPreparedStatement ps = psList;
            psList = ps.next;
            ps.next = null;
            return ps;
        }

        /**
         * Add ps to our list.
         */
        public void addPsToList(PooledPreparedStatement ps) {
            ps.next = psList;
            psList = ps;
        }
    }

 }
