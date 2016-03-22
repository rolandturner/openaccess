
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
package com.versant.core.jdo.junit.test2;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdbc.VersantClientJDBCConnection;
import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.metric.BaseMetric;
import com.versant.core.common.VersantConnectionPoolFullException;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.logging.LogEvent;
import com.versant.core.jdbc.conn.*;
import com.versant.core.jdbc.logging.JdbcLogEvent;
import com.versant.core.jdbc.logging.JdbcPoolEvent;
import com.versant.core.jdo.junit.TestFailedException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.lang.reflect.Field;

/**
 * Tests for per connection PreparedStatement cache and for the pool itself.
 */
public class TestPsCache extends VersantTestCase {

    private static Field pcLastActivityTime;

    public TestPsCache(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testPoolStress",
            "testPoolBasic",
            "testPoolConTimeout",
            "testMultiplePSForKey",
            "testPoolBlockWhenFull",
            "testConLifespan",
            "testPoolMaxActiveStress",
            "testAutoCommit",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestPsCache(a[i]));
        }
        return s;
    }

    /**
     * Make sure the autoCommit option to getConnection works.
     */
    public void testAutoCommit() throws Exception {
        if (isRemote() || !isJdbc()) {
            unsupported();
            return;
        }
        JdbcConnectionSource pool = getJdbcConnectionSource();
        Connection pc;
        pc = pool.getConnection(false, false);
        assertTrue(!pc.getAutoCommit());
        pc.close();
        pc = pool.getConnection(false, true);
        assertTrue(pc.getAutoCommit());
        pc.close();
    }

    /**
     * See if we can get the pool to exceed the maxActive setting with
     * some Threads rapidly allocating and releasing connections.
     */
    public void testPoolMaxActiveStress() throws Exception {
        if (isRemote() || !isJdbc() || isDataSource()) {
            unsupported();
            return;
        }

        // get a connection to get the pool reference
        PooledConnection pc = (PooledConnection)pmf().getJdbcConnection(null);
        JDBCConnectionPool pool = pc.getPool();
        pc.close();

        int reserved = pool.getReserved();
        boolean blockWhenFull = pool.isBlockWhenFull();
        pool.setBlockWhenFull(false);
        pool.setReserved(0);

        // create n threads and then start them
        int n = 5;
        PoolMaxThread t[] = new PoolMaxThread[n];
        for (int i = 0; i < n; i++) t[i] = new PoolMaxThread(pool, i);
        for (int i = 0; i < n; i++) t[i].start();

        // look for failures in the event log for secs
        int secs = 10;
        int max = pool.getMaxActive() - pool.getReserved();
        long start = System.currentTimeMillis();
        LogEvent[] a = pmf().getNewPerfEvents(0);
        int lastId = a[a.length - 1].getId();
        JdbcPoolEvent badEvent = null;
        for (; badEvent == null
                && (System.currentTimeMillis() - start) < secs * 1000L;) {
            a = pmf().getNewPerfEvents(lastId);
            if (a != null) {
                lastId = a[a.length - 1].getId();
                for (int i = 0; i < a.length; i++) {
                    LogEvent e = a[i];
                    if (!(e instanceof JdbcPoolEvent)) continue;
                    JdbcPoolEvent pe = (JdbcPoolEvent)e;
                    if (pe.getType() == JdbcLogEvent.POOL_ALLOC
                            && pe.getActive() > max) {
                        badEvent = pe;
                        break;
                    }
                }
            }
            Thread.sleep(100);
        }

        // shut them down
        for (int i = 0; i < n; i++) t[i].finish();
        for (int i = 0; i < n; i++) t[i].join(5000);
        for (int i = 0; i < n; i++) assertFalse(t[i].isAlive());

        // throw exception if there was a problem
        if (badEvent != null) {
            throw new TestFailedException(
                    "Pool (maxActive - reserved) exceeded: " + badEvent);
        }

        pool.setReserved(reserved);
        pool.setBlockWhenFull(blockWhenFull);
    }

    /**
     * This will get a connection from the pool and then sleep for 5 seconds.
     */
    private static class PoolMaxThread extends Thread {

        private JDBCConnectionPool pool;
        private boolean stopFlag;

        public PoolMaxThread(JDBCConnectionPool pool, int i) {
            super("PoolMaxThread" + i);
            this.pool = pool;
            setDaemon(true);
        }

        public void run() {
            try {
                for (; !stopFlag;) {
                    ArrayList a = new ArrayList();
                    try {
                        for (; !stopFlag;) {
                            a.add(pool.getConnection(false, false));
                        }
                    } catch (VersantConnectionPoolFullException e) {
                        // ignore
                    }
                    for (Iterator i = a.iterator(); i.hasNext();) {
                        PooledConnection con = (PooledConnection)i.next();
                        con.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        public void finish() {
            stopFlag = true;
        }
    }

    /**
     * Make sure the pool expires connections that reach maxConAge.
     */
    public void testConLifespan() throws Exception {
        if (isRemote() || isDataSource() || !isJdbc()) {
            unsupported();
            return;
        }

        // get a connection to get the pool reference
        JDBCConnectionPool pool = getJdbcConnectionPool();

        // make sure there is only one connection in the pool so we will get
        // the same one back every time
        int maxActive = pool.getMaxActive();
        int maxIdle = pool.getMaxIdle();
        int reserved = pool.getReserved();
        pool.setMaxActive(1);
        pool.setMaxIdle(1);
        pool.setReserved(0);
        pool.closeIdleConnections();
        System.out.println("XXXX " + pool.toString());

        // set the lifespan
        int conLifespan = 3;
        pool.setMaxConAge(conLifespan);

        BaseMetric expiredMetric = (BaseMetric)lookupMetric("JDBCConExpired");
        int v = getMostRecentMetricSnapshot().getMostRecentSample(
                expiredMetric);
        System.out.println("*** v = " + v);

        // use up the connection lifespan
        PooledConnection first = (PooledConnection)pool.getConnection(false, false);
        first.close();
        for (int i = 1; i < conLifespan; i++) {
            PooledConnection c = (PooledConnection)pool.getConnection(false, false);
            assertTrue(c == first);
            c.close();
            assertEquals(i + 1, c.age);
        }

        // make sure the expired counter is up - do this twice to avoid timing
        // issues with databases that take a long time to connect
        getMostRecentMetricSnapshot();
        int v2 = getMostRecentMetricSnapshot().getMostRecentSample(
                expiredMetric);
        System.out.println("*** v2 = " + v2);
        assertEquals(v + 1, v2);

        // make sure we are given a new connection
        PooledConnection c = (PooledConnection)pool.getConnection(false, false);
        assertTrue(c != first);
        c.close();

        pool.setMaxConAge(1000);
        pool.setMaxActive(maxActive);
        pool.setMaxIdle(maxIdle);
        pool.setReserved(reserved);
    }

    /**
     * Make sure the pool handles the blockWhenFull option propertly.
     */
    public void testPoolBlockWhenFull() throws Exception {
        if (isRemote() || !isJdbc() || isDataSource()) {
            unsupported();
            return;
        }

        JDBCConnectionPool pool = getJdbcConnectionPool();
        try {
            pool.setBlockWhenFull(true);

            // make sure the pool is full
            int n = pool.getMaxActive() - pool.getReserved();
            PooledConnection[] a = new PooledConnection[n];
            for (int i = 0; i < n; i++) {
                a[i] = (PooledConnection)pool.getConnection(false, false);
                System.out.println("a[" + i + "] = " + a[i]);
            }

            // now create some threads waiting for connections
            PoolBlockThread[] ta = new PoolBlockThread[3];
            for (int i = 0; i < ta.length; i++) {
                ta[i] = new PoolBlockThread(pool, i);
                ta[i].start();
            }
            Thread.sleep(100); // let them start

            // make sure they are all alive and do not have cons
            for (int i = 0; i < ta.length; i++) {
                assertTrue(ta[i].isAlive());
                assertTrue(!ta[i].hasCon());
            }

            // free up one connection for each Thread
            for (int i = 0; i < ta.length; i++) a[i].close();
            Thread.sleep(100); // let them run

            // make sure they are all alive and have their cons
            for (int i = 0; i < ta.length; i++) {
                assertTrue(ta[i].isAlive());
                assertTrue(ta[i].hasCon());
            }

            // wait for them to finish
            for (int i = 0; i < ta.length; i++) ta[i].join(5000);
            for (int i = 0; i < ta.length; i++) assertFalse(ta[i].isAlive());

            // free remaining connections
            for (int i = ta.length; i < n; i++) a[i].close();
        } finally {
            pool.setBlockWhenFull(false);
        }
    }

    /**
     * This will get a connection from the pool and then sleep for 5 seconds.
     */
    private static class PoolBlockThread extends Thread {

        private JDBCConnectionPool pool;
        private PooledConnection con;

        public PoolBlockThread(JDBCConnectionPool pool, int i) {
            super("PoolBlockThread" + i);
            this.pool = pool;
            setDaemon(true);
        }

        public void run() {
            try {
                con = (PooledConnection)pool.getConnection(false, false);
                Thread.sleep(2000);
                con.close();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        public boolean hasCon() {
            return con != null;
        }
    }

    /**
     * Do a stress test of the pool.
     */
    public void testPoolStress() throws Exception {
        if (isRemote() || !isJdbc() || isDataSource()) {
            unsupported();
            return;
        }

        // get a connection to get the pool reference
        PooledConnection pc = (PooledConnection)pmf().getJdbcConnection(null);
        JDBCConnectionPool pool = pc.getPool();
        pc.close();
        pool.closeIdleConnections();

        int oldMaxActive = pool.getMaxActive();
        pool.setMaxActive(21);

        PoolStressThread[] pa = new PoolStressThread[5];
        for (int i = 0; i < pa.length; i++) {
            pa[i] = new PoolStressThread(pool, i, 25);
            pa[i].start();
        }
        for (int i = 0; i < pa.length; i++) pa[i].join(30000);
        for (int i = 0; i < pa.length; i++) assertFalse(pa[i].isAlive());
        for (int i = 0; i < pa.length; i++) {
            if (pa[i].error != null) throw pa[i].error;
        }

        pool.setMaxActive(oldMaxActive);

        pool.closeIdleConnections();
    }

    private static class PoolStressThread extends Thread {

        private JDBCConnectionPool pool;
        private int n;
        private Random rnd;
        private Exception error;

        public PoolStressThread(JDBCConnectionPool pool, int i, int n) {
            super("Piglet" + i);
            this.pool = pool;
            this.n = n;
            rnd = new Random(i);
        }

        public void run() {
            LinkedList list = new LinkedList();
            try {
                for (int i = 0; i < n; i++) {
                    alloc(rnd.nextInt(5), list, pool);
                    Thread.sleep(rnd.nextInt(100) + 1);
                    free(rnd.nextInt(5), list);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
                error = e;
            }
            try {
                free(list.size(), list);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                error = e;
            }
        }
    }

    private static void alloc(int n, LinkedList list, JDBCConnectionPool pool) {
        for (int i = 0; i < n; i++) {
            try {
                list.add(pool.getConnection(false, false));
            } catch (TestFailedException x) {
                throw x;
            } catch (Exception e) {
                System.out.println("TestPsCache.alloc " + e);
            }
        }
    }

    private static void free(int n, LinkedList list) {
        for (int i = 0; i < n && !list.isEmpty(); i++) {
            PooledConnection pc = (PooledConnection)list.removeFirst();
            pc.close();
        }
    }


    private void hackLastActivityTime(PooledConnection pc, long ms)
            throws IllegalAccessException {
        if (pcLastActivityTime == null) {
            Field[] a = PooledConnection.class.getDeclaredFields();
            for (int i = 0; i < a.length; i++) {
                if (a[i].getName().equals("lastActivityTime")) {
                    pcLastActivityTime = a[i];
                    break;
                }
            }
            if (pcLastActivityTime == null) {
                fail("lastActivityTime field not found on PooledConnection");
            }
            pcLastActivityTime.setAccessible(true);
        }
        pcLastActivityTime.set(pc, new Long(ms));
    }

    /**
     * Test the pool connection timeout feature.
     */
    public void testPoolConTimeout() throws Exception {
        if (isRemote() || !isJdbc() || isDataSource() ) {
            unsupported();
            return;
        }

        // get connections
        PooledConnection[] pc = new PooledConnection[3];
        for (int i = 0; i < pc.length; i++) {
            pc[i] = (PooledConnection)pmf().getJdbcConnection(null);
        }
        JDBCConnectionPool pool = pc[0].getPool();

        // check that the pool is configured correctly
        assertEquals(120, pool.getConTimeout());
        assertEquals(10, pool.getTestInterval());

        // check pool count
        assertEquals(3, pool.getActiveCount());

        // fudge connections 0 and 1 so they are old and should be nuked
        long old = System.currentTimeMillis() - 119000;
        hackLastActivityTime(pc[0], old);
        hackLastActivityTime(pc[1], old);

        // wait for them to timeout and get closed
        for (int i = 0; i < 15; i++) {
            Thread.sleep(1000);
            if (pool.getActiveCount() == 1) break;
        }

        // try and use them
        for (int i = 0; i < 2; i++) {
            try {
                Statement stat = pc[i].createStatement();
                stat.executeQuery("select * from jdo_keygen");
                throw new TestFailedException("connection still ok");
            } catch (SQLException e) {
                System.out.println("*** [" + i + "] " + e);
                // good
            }
        }

        // check pool count
        assertEquals(1, pool.getActiveCount());

        // make sure 2 still works
        Statement stat = pc[2].createStatement();
        ResultSet rs = stat.executeQuery("select * from jdo_keygen");
        rs.close();
        stat.close();
        pc[2].close();
    }

    /**
     * Test all the basic functions of the JDBC connection pool itself.
     */
    public void testPoolBasic() throws Exception {
        if ("mssql".equals(getDbName()) || "oracle".equals(getDbName()) ||
                "postgres".equals(getDbName())) {
            broken();
            return;
        }
        
        if (isRemote() || !isJdbc() || isDataSource()) {
            unsupported();
            return;
        }

        // enable idle connection testing
        PooledConnection pc = (PooledConnection)pmf().getJdbcConnection(null);
        JDBCConnectionPool pool = pc.getPool();
        pc.close();
        boolean testWhenIdle = pool.isTestWhenIdle();
        pool.setTestWhenIdle(true);
        int minIdle = pool.getMinIdle();
        pool.setMinIdle(2);

        // get a connection just so we can get a reference to the pool
        pc = (PooledConnection)pmf().getJdbcConnection(null);
        pool = pc.getPool();
        pc.close();

        // check that the pool is configured correctly
        assertEquals(2, pool.getMinIdle());
        assertEquals(3, pool.getMaxIdle());
        assertEquals(6, pool.getMaxActive());
        assertEquals(1, pool.getReserved());
        assertEquals(1, pool.getReserved());
        assertFalse(pool.isBlockWhenFull());

        // empty the pool and check that the minIdle connections are created
        System.out.println("\n*** pmf().clearConnectionPool");
        pmf().clearConnectionPool(null);
        waitForIdle(pool);
        assertEquals(pool.getMinIdle(), pool.getIdleCount());

        // grab 2 connections and check that minIdle connections are there
        // and that the activeCount is correct
        System.out.println("\n*** grabbing 2 connections");
        Connection[] con = new Connection[6];
        con[0] = pmf().getJdbcConnection(null);
        con[1] = pmf().getJdbcConnection(null);
        System.out.println("\n*** got 2 connections");
        waitForIdle(pool);
        assertEquals(2, pool.getActiveCount());
        assertEquals(pool.getMinIdle(), pool.getIdleCount());

        // get two more and make sure pool is not overfilled
        System.out.println("\n*** grabbing 2 more connections");
        con[2] = pmf().getJdbcConnection(null);
        con[3] = pmf().getJdbcConnection(null);
        waitForIdle(pool);
        assertEquals(4, pool.getActiveCount());
        assertEquals(1, pool.getIdleCount());

        // get one more and make sure pool is not overfilled
        System.out.println("\n*** grabbing 1 more connection");
        con[4] = pmf().getJdbcConnection(null);
        waitForIdle(pool);
        assertEquals(5, pool.getActiveCount());
        assertEquals(0, pool.getIdleCount());

        // get one more and make sure we get exception
        System.out.println("\n*** grabbing 1 more connection for error");
        try {
            pmf().getJdbcConnection(null);
            throw new TestFailedException("pool limit not checked");
        } catch (VersantConnectionPoolFullException x) {
            System.out.println("*** " + x);
            // good
        }

        // make sure we can get one more highPriority connection
        System.out.println("\n*** grabbing 1 more highPriority connection");
        con[5] = pool.getConnection(true, false);

        // release 4 connections and check counts (release order is mixed up
        // to better test linked list code)
        System.out.println("\n*** releasing 3 connections");
        con[1].close();
        con[2].close();
        con[0].close();
        assertEquals(3, pool.getActiveCount());
        assertEquals(3, pool.getIdleCount());

        // release three more and make sure maxIdle nukes two
        System.out.println("\n*** releasing 3 connections");
        con[3].close();
        con[4].close();
        con[5].close();
        assertEquals(0, pool.getActiveCount());
        for (int i = 0; i < 6; i++) {
            Thread.sleep(1000);
            if (pool.getIdleCount() == pool.getMinIdle()) break;
        }
        assertEquals(3, pool.getIdleCount());

        Thread.sleep(1000);

        pool.setMinIdle(minIdle);
        pool.setTestWhenIdle(testWhenIdle);

        System.out.println("\n*** finished TestPsCache.testPool\n\n");
    }

    private void waitForIdle(JDBCConnectionPool pool)
            throws InterruptedException {
        for (int i = 0; i < 7; i++) {
            Thread.sleep(1000);
            if (pool.getIdleCount() >= pool.getMinIdle()) break;
        }
        Thread.sleep(1000);
    }

    /**
     * Make sure everything functions properly when there are multiple
     * PSes for a given key. This is not well tested by all the other tests.
     */
    public void testMultiplePSForKey() throws Exception {
        if (isRemote() || !isJdbc()) {
            unsupported();
            return;
        }

        // make sure we get a fresh connection
        pmf().clearConnectionPool(null);

        VersantPersistenceManager pm
                = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantClientJDBCConnection clientCon
                = (VersantClientJDBCConnection)pm.getJdbcConnection(null);
        LoggingConnection con = (LoggingConnection)clientCon.getRealConnection();
        PreparedStatementPool psPool = con.getPsPool();
        if (psPool == null) { // pooling off so skip test
            clientCon.close();
            pm.currentTransaction().rollback();
            pm.close();
            return;
        }

        // way overfill the pool and make sure none of the active ps get
        // killed off
        int n = psPool.getMax();
        int np = 3;
        PooledPreparedStatement[][] ps = new PooledPreparedStatement[n][];
        String[] sql = new String[n];
        for (int i = 0; i < n; i++) {
            sql[i] = "select * from country where 0 = " + (i + 1);
            ps[i] = new PooledPreparedStatement[np];
            for (int j = 0; j < np; j++) {
                ps[i][j] = (PooledPreparedStatement)con.prepareStatement(
                        sql[i]);
            }
        }
        assertEquals(n * np, psPool.getSize());

        System.out.println("\n*** after over-filling pool");
        psPool.dump();

        // check that all the statements still work (i.e. not closed) and
        // put the first ones back
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < np; j++) {
                ResultSet rs = ps[i][j].executeQuery();
                rs.close();
                if (j == 0) ps[i][j].close();
            }
        }

        // the pool should be back down to n * 2
        assertEquals(n * 2, psPool.getSize());

        // check and close half of the remaining ones
        int n2 = n / 2;
        for (int i = 0; i < n2; i++) {
            for (int j = 1; j < np; j++) {
                ResultSet rs = ps[i][j].executeQuery();
                rs.close();
                ps[i][j].close();
            }
        }

        // the pool should be back down to n
        assertEquals(n, psPool.getSize());

        int[] types = new int[]{
            JdbcLogEvent.CON_PREPARE_STATEMENT,
            JdbcLogEvent.STAT_CLOSE
        };
        countJdbcEvents(types); // clear counters

        // close the remaining PS so they go idle
        System.out.println(
                "\n*** closing remaining PS to put them in idle pool");
        for (int i = n2; i < n; i++) {
            for (int j = 1; j < np; j++) ps[i][j].close();
        }

        // now churn on whats left and make sure no ps get made or closed
        System.out.println("\n*** churning on whats left");
        psPool.dump();
        for (int i = n2; i < n; i++) {
            for (int j = 1; j < np; j++) {
                PreparedStatement p = con.prepareStatement(sql[i]);
                ResultSet rs = p.executeQuery();
                rs.close();
                p.close();
            }
        }

        // check that nothing was created or closed
        int[] a = countJdbcEvents(types);
        assertEquals(0, a[0]);
        assertEquals(0, a[1]);

        // the pool should still be n
        assertEquals(n, psPool.getSize());

        clientCon.close();
        pm.close();
    }

}
