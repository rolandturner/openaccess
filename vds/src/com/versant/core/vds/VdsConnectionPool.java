
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
package com.versant.core.vds;

import com.versant.core.common.Debug;
import com.versant.core.metric.*;

import javax.jdo.*;
import com.versant.core.vds.util.ConnectionURL;
import com.versant.core.logging.LogEventStore;
import com.versant.odbms.ConnectionInfo;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.DatastoreManagerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

/**
 * Pool of connection to VDS Server.
 *
 * @see VdsConnection
 */
public final class VdsConnectionPool implements Runnable {

    private final String name;
//    private final Properties props;
    private final String url;
    private final LogEventStore pes;
    private int maxActive;
    private int maxIdle;
    private int minIdle;
    private int reserved;
    private int nRealConnection;
    private final boolean testOnAlloc;
    private boolean testWhenIdle;
    private final int retryIntervalMs;
    private final int retryCount;
    private boolean closed;
    private int conTimeout;
    private int testInterval;
    private boolean blockWhenFull;
    private int maxConAge;

    private VdsConnection idleHead;  // next con to be given out
    private VdsConnection idleTail;  // last con returned
    private int idleCount;

    private VdsConnection activeHead;    // most recently allocated con
    private VdsConnection activeTail;    // least recently allocated con
    private int activeCount;

    private Thread cleanupThread;
    private long timeLastTest = System.currentTimeMillis();

    private final DatastoreManagerFactory factory;

    private BaseMetric metricActive;
    private BaseMetric metricIdle;
    private BaseMetric metricMaxActive;
    private BaseMetric metricCreated;
    private BaseMetric metricClosed;
    private BaseMetric metricAllocated;
    private BaseMetric metricValidated;
    private BaseMetric metricBad;
    private BaseMetric metricTimedOut;
    private BaseMetric metricExpired;
    private BaseMetric metricWait;
    private BaseMetric metricFull;

    private int createdCount;
    private int closedCount;
    private int allocatedCount;
    private int validatedCount;
    private int badCount;
    private int timedOutCount;
    private int waitCount;
    private int fullCount;
    private int expiredCount;

    private VdsConfig config;
    
    private LinkedList  _requestQueue = new LinkedList();

    private static final String CAT_POOL = "Con Pool";
    static boolean LOG = System.getProperty("pool.logging")!=null;
    static Logger _log = Logger.getLogger("com.versant.core.vds.ConnectionPool");

    /**
     * Create the pool. Note that changes to vdsConfig have no effect on the
     * pool after construction i.e. fields in vdsConfig are copied not
     * referenced.
     */
    public VdsConnectionPool(VdsConfig vdsConfig, LogEventStore pes)  {
        this.pes = pes;
        config = vdsConfig;
        name = config.name;


        url   = config.url;

        maxActive           = vdsConfig.maxActive;
        maxIdle             = vdsConfig.maxIdle;
        minIdle             = vdsConfig.minIdle;
        reserved            = vdsConfig.reserved;
        testOnAlloc         = vdsConfig.testOnAlloc;
        testWhenIdle        = vdsConfig.testWhenIdle;
        retryIntervalMs     = vdsConfig.retryIntervalMs;
        retryCount          = vdsConfig.retryCount;
        conTimeout          = vdsConfig.conTimeout;
        testInterval        = vdsConfig.testInterval;
        blockWhenFull       = vdsConfig.blockWhenFull;
        maxConAge           = vdsConfig.maxConAge;


        ConnectionURL connectionURL = new ConnectionURL(config.url);
        String user = config.user;
        if (user != null && user.length() == 0) user = null;
        String password = config.password;
        if (password != null && password.length() == 0) password = null;

        factory = new DatastoreManagerFactory(new ConnectionInfo(connectionURL.getName(),
                connectionURL.getHost(), connectionURL.getPort(), user, password,
                config.oidBatchSize), null);
    }

    /**
     * Start the pools cleanup thread if not already done.
     */
    public void startCleanupThread() {
        if (cleanupThread != null) return;
        cleanupThread = new Thread(this, "VOA Pool " + url);
        cleanupThread.setDaemon(true);
        cleanupThread.setPriority(Thread.MIN_PRIORITY);
        cleanupThread.start();
    }

    /**
     * Check that we can connect to the database 
     */
    public void check() throws Exception {
        // just be confident
    }

    /**
     * Create a connection. If this fails then sleep for millis and try again.
     * If retryCount is 0 then retry is done forever, if < 0 then there are
     * no retries.
     */
    private DatastoreManager createRealConWithRetry(int retryCount, int millis) {
        for (int n = 0; ;) {
            try {
                return createRealCon();
            } catch (JDODataStoreException e) {
                if (retryCount < 0 || (retryCount > 0 && ++n > retryCount)) throw e;
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e1) {
                    // ignore the interruption
                }
            }
        }
    }

    public int getConTimeout() {
        return conTimeout;
    }

    public void setConTimeout(int conTimeout) {
        this.conTimeout = conTimeout;
    }

    public int getTestInterval() {
        return testInterval;
    }

    public void setTestInterval(int testInterval) {
        this.testInterval = testInterval;
    }

    public boolean isTestWhenIdle() {
        return testWhenIdle;
    }

    public void setTestWhenIdle(boolean on) {
        testWhenIdle = on;
    }

    public boolean isBlockWhenFull() {
        return blockWhenFull;
    }

    public void setBlockWhenFull(boolean blockWhenFull) {
        this.blockWhenFull = blockWhenFull;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
        if (cleanupThread != null) cleanupThread.interrupt();
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
        if (cleanupThread != null) cleanupThread.interrupt();
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
        if (cleanupThread != null) cleanupThread.interrupt();
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public int getIdleCount() {
        return idleCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public int getMaxConAge() {
        return maxConAge;
    }

    public void setMaxConAge(int maxConAge) {
        this.maxConAge = maxConAge;
    }

    /**
     * Get our URL.
     */
    public String getURL() {
        return url;
    }

    /**
     * Return the connection prop of the pool.
     */
    public Properties getConnectionProperties() {
        return config.toProperties();
    }


    /**
     * Allocate a PooledConnection from the pool.
     *
     * @param highPriority If this is true then reserved high priority
     *                     connections may be given out
     */
    public VdsConnection getConnection(boolean highPriority)
            throws Exception {
        // adjust local maxActive to maintain reserved connections if needed
        int maxActive = this.maxActive - (highPriority ? 0 : reserved);
        VdsConnection con;
        for (; ;) {
            synchronized (this) {
                for (; ;) {
                    if (closed) {
                        throw new JDOFatalException(this + " has been closed");
                    }
                    if (activeCount >= maxActive) {
                        if (LOG) _log.log(Level.WARNING, this + ": Active connection " + activeCount + " exceeded maximum " + maxActive);
                        if (blockWhenFull) {
                            try {
                                _requestQueue.addLast(Thread.currentThread());
                              if (LOG) _log.log(Level.WARNING, this + ": Waiting for connection to be freed. Active connection " + activeCount + " exceeded maximum " + maxActive + " No. of blocked request " + _requestQueue.size());
                                wait();
                            } catch (InterruptedException e) {
                                if (LOG) _log.log(Level.WARNING, this + ": Waiting ended for connection to be freed. Active connection " + activeCount + " maximum " + maxActive);
                                // ignore
                            }
                        } else {
                            throw new JDODataStoreException(this + " is full");
                        }
                    } else {
                        con = removeFromIdleHead();
                        ++activeCount;
                        break;
                    }
                }
            }
            if (con == null) {
                try {
                    waitCount++;
                    con = createPooledConnection(retryCount, retryIntervalMs);
                    
                } finally {
                    if (con == null) {
                        synchronized (this) {
                            --activeCount;
                        }
                    }
                }
                break;
            } else {
                if (testOnAlloc && !validateConnection(con)) {
                    destroy(con);
                    synchronized (this) {
                        --activeCount;
                    }
                } else {
                    break;
                }
            }
        }
        con.updateLastActivityTime();
        addToActiveHead(con);
        allocatedCount++;
        
        if (LOG) _log.log(Level.INFO, this + ": Allocated : " + con);
        return con;
    }

    /**
     * Test 1 idle connection. If it fails, close it and repeat.
     */
    public void testIdleConnections() {
        for (; ;) {
            VdsConnection con;
            synchronized (this) {
                // don't test if the pool is almost full - this will just
                // reduce throughput by making some thread wait for a con
                if (activeCount >= (maxActive - reserved - 1)) break;
                con = removeFromIdleHead();
                if (con == null) break;
                ++activeCount;
            }
            if (validateConnection(con)) {
                synchronized (this) {
                    addToIdleTail(con);
                    --activeCount;
                }
                break;
            } else {
                destroy(con);
                synchronized (this) {
                    --activeCount;
                }
            }
        }
    }

    /**
     * Check the integrity of the double linked with head and tail.
     */
    private void checkList(VdsConnection tail, VdsConnection head,
            int size) {
        if (tail == null) {
            testTrue(head == null);
            return;
        }
        if (head == null) {
            testTrue(tail == null);
            return;
        }
        checkList(tail, size);
        checkList(head, size);
        testTrue(tail.prev == null);
        testTrue(head.next == null);
    }

    /**
     * Check the integrity of the double linked list containing pc.
     */
    private void checkList(VdsConnection pc, int size) {
        if (pc == null) return;
        int c = -1;
        // check links to tail
        for (VdsConnection i = pc; i != null; i = i.prev) {
            if (i.prev != null) testTrue(i.prev.next == i);
            ++c;
        }
        // check links to head
        for (VdsConnection i = pc; i != null; i = i.next) {
            if (i.next != null) testTrue(i.next.prev == i);
            ++c;
        }
        if (size >= 0) {
            testEquals(size, c);
        }
    }

    private static void testEquals(int a, int b) {
        if (a != b) {
            throw new JDOFatalInternalException(
                    "assertion failed: expected " + a + " got " + b);
        }
    }

    private static void testTrue(boolean t) {
        if (!t) {
            throw new JDOFatalInternalException(
                    "assertion failed: expected true");
        }
    }

    /**
     * Return a PooledConnection to the pool. This is called by
     * PooledConnection when it is closed. This is a NOP if the connection
     * has been destroyed.
     *
     * @see VdsConnection#close
     */
    public void returnConnection(VdsConnection con) {
        if (con.isDestroyed()) return;
        if (LOG) _log.log(Level.INFO, this +": Before Returned :" + con);
        if (maxConAge > 0 && ++con.age >= maxConAge) {
            ++expiredCount;
            removeFromActiveList(con);
            destroy(con);
        } else {
            synchronized (this) {
                removeFromActiveList(con);
                addToIdleTail(con);
            }
        }

        if (LOG) _log.log(Level.INFO, this +": After Returned :" + con);
    }

    /**
     * Close all idle connections. This method closes idleCount connections.
     * If another thread is making new connections at the same time the
     * idle list will not be empty on return.
     */
    public void clear() {
        for (int i = idleCount; i > 0; i--) {
            VdsConnection con = removeFromIdleHead();
            if (con == null) break;
            destroy(con);
        }
        if (cleanupThread != null) cleanupThread.interrupt();
    }

    /**
     * Destroy a connection.
     */
    private void destroy(VdsConnection con) {
        if (LOG) _log.log(Level.INFO, this +": Destroyed : " + con);
        closedCount++;
        con.destroy();
    }

    /**
     * Close all connections and shutdown the pool.
     */
    public void shutdown() throws Exception {
        closed = true;
        if (cleanupThread != null) cleanupThread.interrupt();
        for (; ;) {
            VdsConnection con = removeFromIdleHead();
            if (con == null) break;
            destroy(con);
        }
        // this will cause any threads waiting for connections to get a
        // closed exception
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Close excess idle connections or create new idle connections if less
     * than minIdle (but do not exceed maxActive in total).
     */
    public void checkIdleConnections() throws Exception {
        // close excess idle connections
        for (; idleCount > maxIdle;) {
            VdsConnection con = removeFromIdleHead();
            if (con == null) break;
            destroy(con);
        }
        // Start creating a new connection if there is space in the pool for 2
        // more. Only add the connection to the pool once created if there is
        // space for 1 more.
        for (; ;) {
            if (!needMoreIdleConnections(1)) break;
            VdsConnection con = createPooledConnection(retryCount,
                    retryIntervalMs);
            synchronized (this) {
                if (needMoreIdleConnections(0)) {
                    addToIdleTail(con);
                    continue;
                }
            }
            destroy(con);
            break;
        }
    }

    private synchronized boolean needMoreIdleConnections(int space) {
        return idleCount < minIdle && idleCount + activeCount < (maxActive - space);
    }

    /**
     * Close active connections that have been out of the pool for too long.
     * This is a NOP if activeTimeout <= 0.
     */
    public void closeTimedOutConnections() {
        if (conTimeout <= 0) return;
        for (; ;) {
            VdsConnection con;
            synchronized (this) {
                if (activeTail == null) return;
                long t = activeTail.getLastActivityTime();
                if (t == 0) return;
                int s = (int)((System.currentTimeMillis() - t) / 1000);
                if (s < conTimeout) return;
                con = activeTail;
                removeFromActiveList(con);
            }
            timedOutCount++;
            destroy(con);
        }
    }

    /**
     * Return the con at the head of the idle list removing it from the list.
     * If there is no idle con then null is returned. The con has its idle
     * flag cleared.
     */
    private synchronized VdsConnection removeFromIdleHead() {
        VdsConnection con = idleHead;
        if (con == null) return null;
        idleHead = con.prev;
        con.prev = null;
        if (idleHead == null) {
            idleTail = null;
        } else {
            idleHead.next = null;
        }
        --idleCount;
        con.idle = false;
        if (Debug.DEBUG) checkList(idleTail, idleHead, idleCount);
        return con;
    }

    /**
     * Add con to the tail of the idle list. The con has its idle flag set.
     * This will notify any blocked threads so they can get the newly idle
     * connection.
     */
    private synchronized void addToIdleTail(VdsConnection con) {
        if (Debug.DEBUG) {
            if (con.prev != null || con.next != null) {
                throw new JDOFatalInternalException("con belongs to a list");
            }
        }
        con.idle     = true;
        if (idleTail == null) {
            idleHead = idleTail = con;
        } else {
            con.next = idleTail;
            idleTail.prev = con;
            idleTail = con;
        }
        ++idleCount;
        if (Debug.DEBUG) checkList(idleTail, idleHead, idleCount);
        
        if (!_requestQueue.isEmpty()){
            Thread waitingThread = (Thread)_requestQueue.removeFirst();
            if (LOG) _log.log(Level.INFO, "Waking up Waiting thread " + waitingThread);
           waitingThread.interrupt();
        }
//        notifyAll();
    }

    /**
     * Add con to the head of the active list. Note that this does not
     * bump up activeCount.
     */
    private synchronized void addToActiveHead(VdsConnection con) {
        if (Debug.DEBUG) {
            if (con.prev != null || con.next != null) {
                throw new JDOFatalInternalException("con belongs to a list");
            }
        }
        if (activeHead == null) {
            activeHead = activeTail = con;
        } else {
            con.prev = activeHead;
            activeHead.next = con;
            activeHead = con;
        }
        if (Debug.DEBUG) checkList(activeTail, activeHead, -1);
    }

    /**
     * Remove con from the active list.
     */
    private synchronized void removeFromActiveList(VdsConnection con) {
        if (con.prev != null) {
            con.prev.next = con.next;
        } else {
            activeTail = con.next;
        }
        if (con.next != null) {
            con.next.prev = con.prev;
        } else {
            activeHead = con.prev;
        }
        con.prev = con.next = null;
        --activeCount;
        if (Debug.DEBUG) checkList(activeTail, activeHead, -1);
    }

    private DatastoreManager createRealCon() {
        nRealConnection++;
        if (LOG) _log.log(Level.INFO, this + " Creating Datastore Connection " + nRealConnection + " maxActive " + maxActive);
        createdCount++;
        return factory.getDatastoreManager();
    }

    /**
     * Create an initialize a PooledConnection. 
     */
    private VdsConnection createPooledConnection(int retryCount,
            int retryIntervalMs) throws Exception {
        DatastoreManager realCon = createRealConWithRetry(retryCount,
                retryIntervalMs);
//        assert !realCon.isClosed();
        if (Debug.DEBUG) {
            Debug.assertInternal(!realCon.isClosed(),
                    "DatastoreManager is closed");
        }
        return new VdsConnection(this, realCon);
    }



    /**
     * Check that the connection is open and if so validate the 
     * connection. Returns true if the connection is ok.
     */
    private boolean validateConnection(VdsConnection con) {
        return true;
    }

    /**
     * Perform maintenance operations on the pool at periodic intervals.
     */
    public void run() {
        for (; ;) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
            }
            if (closed) break;

            if (duration(timeLastTest) >= testInterval) {
                timeLastTest = System.currentTimeMillis();
                if (testWhenIdle) testIdleConnections();
                if (conTimeout > 0) closeTimedOutConnections();
            }

            try {
                checkIdleConnections();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private long duration(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }

    /**
     * Return our name and status.
     */
    public String toString() {
        return "Active:"+activeCount + " of total " + maxActive + 
               " idle " + idleCount + " of total " + maxIdle    +
               " blocked " + _requestQueue.size();
    }

    /**
     * Add all BaseMetric's for this store to the List.
     */
    public void addBaseMetrics(int index, List list, boolean onlyOne) {
        String name = onlyOne ? "" : " " + this.name;
        list.add(metricActive = new BaseMetric("VDSPoolActive" + index,
                "Pool Active" + name, CAT_POOL, "Number of active VDS connections in pool",
                0, Metric.CALC_AVERAGE));
        list.add(metricMaxActive = new BaseMetric("VDSPoolMaxActive" + index,
                "Pool Max Active" + name, CAT_POOL, "Max number of VDS connections allowed in pool",
                0, Metric.CALC_AVERAGE));
        list.add(metricIdle = new BaseMetric("VDSPoolIdle" + index,
                "Pool Idle" + name, CAT_POOL, "Number of idle VDS connections in pool",
                0, Metric.CALC_AVERAGE));
        list.add(metricWait = new BaseMetric("VDSPoolWait" + index,
                "Pool Wait" + name, CAT_POOL, "Number of times that a caller had to wait for a connection",
                0, Metric.CALC_DELTA));
        list.add(metricFull = new BaseMetric("VDSPoolFull" + index,
                "Pool Full" + name, CAT_POOL, "Number of times that the pool was full and a connection was needed",
                0, Metric.CALC_DELTA));
        list.add(metricTimedOut = new BaseMetric("VDSConTimedOut" + index,
                "Con Timed Out" + name, CAT_POOL, "Number of active VDS connections timed out and closed",
                0, Metric.CALC_DELTA));
        list.add(metricExpired = new BaseMetric("VDSConExpired" + index,
                "Con Expired" + name, CAT_POOL, "Number of VDS connections closed due to their age reaching the maximum lifespan",
                0, Metric.CALC_DELTA));
        list.add(metricBad = new BaseMetric("VDSConBad" + index,
                "Con Bad" + name, CAT_POOL, "Number of VDS connections that failed validation test",
                0, Metric.CALC_DELTA));
        list.add(metricCreated = new BaseMetric("VDSConCreated" + index,
                "Con Created" + name, CAT_POOL, "Number of VDS connections created",
                0, Metric.CALC_DELTA));
        list.add(metricClosed = new BaseMetric("VDSConClosed" + index,
                "Con Closed" + name, CAT_POOL, "Number of VDS connections closed",
                0, Metric.CALC_DELTA));
        list.add(metricAllocated = new BaseMetric("VDSConAllocated" + index,
                "Con Allocated" + name, CAT_POOL, "Number of VDS connections given out by the pool",
                3, Metric.CALC_DELTA_PER_SECOND));
        list.add(metricValidated = new BaseMetric("VDSConValidated" + index,
                "Con Validated" + name, CAT_POOL, "Number of VDS connections tested by the pool",
                0, Metric.CALC_DELTA));
    }

    /**
     * Add all DerivedMetric's for this store to the List.
     */
    public void addDerivedMetrics(int index, List list, boolean onlyOne) {
        String name = onlyOne ? "" : " " + this.name;
        list.add(new PercentageMetric("JdbcPoolPercentFull" + index,
                "Pool % Full " + name, CAT_POOL,
                "Active connections as a percentage of the maximum",
                metricActive, metricMaxActive));
    }

    /**
     * Get values for our metrics.
     */
    public void sampleMetrics(int[][] buf, int pos) {
        buf[metricActive.getIndex()][pos] = activeCount;
        buf[metricMaxActive.getIndex()][pos] = maxActive;
        buf[metricIdle.getIndex()][pos] = idleCount;
        buf[metricWait.getIndex()][pos] = waitCount;
        buf[metricFull.getIndex()][pos] = fullCount;
        buf[metricTimedOut.getIndex()][pos] = timedOutCount;
        buf[metricExpired.getIndex()][pos] = expiredCount;
        buf[metricBad.getIndex()][pos] = badCount;
        buf[metricAllocated.getIndex()][pos] = allocatedCount;
        buf[metricValidated.getIndex()][pos] = validatedCount;
        buf[metricCreated.getIndex()][pos] = createdCount;
        buf[metricClosed.getIndex()][pos] = closedCount;
    }

    public LogEventStore getPerfEventStore() {
        return pes;
    }

}
