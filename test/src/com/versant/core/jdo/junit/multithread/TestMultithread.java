
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
package com.versant.core.jdo.junit.multithread;

import com.versant.core.jdo.*;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.multithread.model.Message;
import com.versant.core.jdo.junit.multithread.model.Topic;
import com.versant.core.metadata.ClassMetaData;

import javax.jdo.*;
import javax.jdo.spi.StateManager;
import java.lang.reflect.Field;
import java.util.*;

import junit.framework.AssertionFailedError;

/**
 * Tests for multithreaded PM access.
 */
public class TestMultithread extends VersantTestCase {

    /**
     * Use reflection to get the StateManager from a managed instance.
     */
    private StateManager getStateManager(Object pc) throws Exception {
        ClassMetaData cmd = getJdoMetaData().getClassMetaData(pc.getClass());
        assertNotNull(cmd);
        Field[] a = cmd.top.cls.getDeclaredFields();
        Field f = null;
        for (int i = 0; i < a.length; i++) {
            f = a[i];
            if ("jdoStateManager".equals(f.getName())) break;
        }
        assertNotNull(f);
        f.setAccessible(true);
        return (StateManager)f.get(pc);
    }           
    

    /**
     * Make sure config of multithreaded access works and that the correct
     * proxy classes are being used.
     */
    public void testConfig() throws Exception {
        assertTrue(pmf().getMultithreaded());
        try {
            // check correct setting and synch proxy
            PersistenceManager pm = pmf().getPersistenceManager();
            assertTrue(pm.getMultithreaded());
            assertTrue(pm instanceof SynchronizedPMProxy);

            // make sure true -> false is ignored
            pm.setMultithreaded(false);
            assertTrue(pm.getMultithreaded());

            // manage an instance and make sure correct StateManager is
            // installed
            pm.currentTransaction().begin();
            Message msg = new Message("blah");
            pm.makePersistent(msg);
            assertTrue(getStateManager(msg) instanceof SynchronizedStateManagerProxy);
            pm.currentTransaction().commit();

            // get new PM to clear local cache
            pm.close();
            pm = pmf().getPersistenceManager();

            // make sure the correct proxy is used for a query and the correct
            // StateManager is installed for the instances returned by the query
            pm.currentTransaction().begin();
            VersantQueryImp q = (VersantQueryImp)pm.newQuery(Message.class);
            Collection ans = (Collection)q.execute();
            assertTrue(ans instanceof SynchronizedQueryResult);
            msg = (Message)ans.iterator().next();
            assertTrue(getStateManager(msg) instanceof SynchronizedStateManagerProxy);
            q.closeAll();
            pm.currentTransaction().commit();

            pm.close();

            // now check the reverse
            pmf().setMultithreaded(false);
            assertTrue(!pmf().getMultithreaded());

            // check correct setting and unsynch proxy
            pm = pmf().getPersistenceManager();
            assertTrue(!pm.getMultithreaded());
            assertTrue(pm instanceof UnsynchronizedPMProxy);

            // make sure false -> true generates an exception
            try {
                pm.setMultithreaded(true);
                fail("Expected JDOUserException");
            } catch (JDOUserException e) {
                System.out.println("Good: " + e);
            }

            // manage an instance and make sure correct StateManager is
            // installed
            pm.currentTransaction().begin();
            msg = new Message("blahblah");
            pm.makePersistent(msg);
            assertTrue(getStateManager(msg) instanceof PCStateMan);
            pm.currentTransaction().commit();

            // get new PM to clear local cache
            pm.close();
            pm = pmf().getPersistenceManager();

            // make sure the correct proxy is used for a query and the correct
            // StateManager is installed for the instances returned by the query
            pm.currentTransaction().begin();
            q = (VersantQueryImp)pm.newQuery(Message.class);
            ans = (Collection)q.execute();
            assertFalse(ans instanceof SynchronizedQueryResult);
            msg = (Message)ans.iterator().next();
            assertTrue(getStateManager(msg) instanceof PCStateMan);
            q.closeAll();
            pm.currentTransaction().commit();

            pm.close();
        } finally {
            pmf().setMultithreaded(true);
        }
    }

    /**
     * Test concurrent insert, query, update and select operations.
     */
    public void testConcurrentOps() {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create topics to add messages to
        pm.currentTransaction().begin();
        Topic[] topics = new Topic[10];
        for (int i = 0; i < topics.length; i++) {
            topics[i] = new Topic("Topic" + i);
        }
        pm.makePersistentAll(topics);
        pm.currentTransaction().commit();

        // now add a bunch of messages using concurrent threads
        pm.currentTransaction().begin();
        OpRunner[] opr = new OpRunner[10];
        for (int i = 0; i < opr.length; i++) {
            opr[i] = new OpRunner(i + 1, "OpRunner" + i, topics, pm, 100);
        }
        runOps(OpRunner.INSERT, opr);
        pm.currentTransaction().commit();

        // make sure that all of the topics have the expected number of msgs
        pm.currentTransaction().begin();
        for (int i = 0; i < topics.length; i++) {
            Topic t = topics[i];
            assertEquals(t.getMessageCount(), t.getMessageList().size());
        }
        pm.currentTransaction().commit();

        // run a bunch of queries at once
        pm.currentTransaction().begin();
        runOps(OpRunner.QUERY, opr);
        pm.currentTransaction().commit();

        pm.close();
    }

    private void runOps(int op, OpRunner[] opr) {
        for (int i = 0; i < opr.length; i++) {
            opr[i].createThread();
        }
        for (int i = 0; i < opr.length; i++) {
            opr[i].start(op);
        }
        for (int i = 0; i < opr.length; i++) {
            opr[i].join();
        }
        for (int i = 0; i < opr.length; i++) {
            Throwable t = opr[i].getThrowable();
            if (t != null) {
                t.printStackTrace(System.out);
                fail(t.toString());
            }
        }
    }

    /**
     * Run all the operations single threaded. This is for validating the
     * tests.
     */
    private void runOpsSync(int op, OpRunner[] opr) {
        for (int i = 0; i < opr.length; i++) {
            opr[i].createThread();
            opr[i].start(op);
            opr[i].join();
            Throwable t = opr[i].getThrowable();
            if (t != null) {
                t.printStackTrace(System.out);
                fail(t.toString());
            }
        }
    }

    /**
     * Creates new messages and topics.
     */
    private static class OpRunner implements Runnable {

        public static final int INSERT = 1;
        public static final int QUERY = 2;

        private int op;
        private Random rnd;
        private String name;
        private Topic[] topics;
        private PersistenceManager pm;
        private int count;
        private Throwable throwable;
        private Thread thread;

        public OpRunner(int seed, String name, Topic[] topics,
                PersistenceManager pm, int count) {
            this.rnd = new Random(seed);
            this.name = name;
            this.topics = topics;
            this.pm = pm;
            this.count = count;
        }

        public void createThread() {
            thread = new Thread(this, name);
            thread.setDaemon(true);
        }

        public void start(int op) {
            this.op = op;
            thread.start();
        }

        public void join() {
            for (;;) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        public String getName() {
            return name;
        }

        public void run() {
            try {
                switch (op) {
                    case INSERT:
                        insert();
                        break;
                    case QUERY:
                        query();
                        break;
                    default:
                        throw new IllegalStateException("invalid op: " + op);
                }

            } catch (Throwable e) {
                throwable = e;
            }
        }

        private void insert() {
            for (int i = 0; i < count; i++) {
                Topic t = topics[rnd.nextInt(topics.length)];
                Message msg = new Message(t.getName() + name + i);
                pm.makePersistent(msg);
                t.addMessage(msg);
                System.out.println(msg.getText());
            }
        }

        private void query() {
            for (int k = 0; k < 20; k++) {
                Topic t = topics[rnd.nextInt(topics.length)];
                Query q = pm.newQuery(Message.class,
                        "text.startsWith(p)");
                q.declareParameters("String p");
                String p = t.getName();
                Iterator i = ((Collection)q.execute(p)).iterator();
                int count = 0;
                for (; i.hasNext(); count++) {
                    Message o = (Message)i.next();
                    assertTrue(o.getText().startsWith(p));
                }
                assertEquals(t.getMessageCount(), count);
            }
        }

        public Throwable getThrowable() {
            return throwable;
        }

    }

}


