
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
package com.versant.core.jdo.junit.test0;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.InstanceCallbacksClass;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Test InstanceCallBacks and Synchronization.
 */
public class TestInstanceCallBacks extends VersantTestCase {

    public TestInstanceCallBacks(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testPostLoad",
            "testPostLoad1",
            "testPostLoadForSerialization",
            "testPostLoadFromQuery",
            "testInstanceCallbacks",
            "testInstanceCallbacks2",
            "testSynchronization",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestInstanceCallBacks(a[i]));
        }
        return s;
    }

    public void testPostLoad() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person p = new Person("pl");
        p.setIntField(10);
        p.setNonDFGString("nonDFG");
        col.add(p);

        pm.makePersistentAll(col);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();
        pm.close();

        System.out.println(
                "\n*** Check pm.getObjectById(id, false) calls jdoPostLoad when dfg field is touched");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        p.getName();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.close();

        System.out.println(
                "\n*** Check pm.getObjectById(id, true) calls jdoPostLoad");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.close();
    }

    public void testPostLoad1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person p = new Person("pl");
        p.setIntField(10);
        p.setNonDFGString("nonDFG");
        col.add(p);

        pm.makePersistentAll(col);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();
        pm.close();

        System.out.println(
                "\n*** Check pm.getObjectById(id, false) calls jdoPostLoad");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, false);
        p.getName();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.retrieve(p);
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.currentTransaction().commit();

        pm.close();

        System.out.println(
                "\n*** Check pm.getObjectById(id, true) calls jdoPostLoad");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectById(id, true);
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.close();
    }

    public void testPostLoadForSerialization() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Collection col = new ArrayList();

        Person p = new Person("pl");
        p.setIntField(10);
        p.setNonDFGString("nonDFG");
        col.add(p);

        pm.makePersistentAll(col);
        pm.currentTransaction().commit();

        System.out.println(
                "\n*** Check pm.getObjectById(id, false) calls jdoPostLoad");
        pm.currentTransaction().begin();
        Utils.isHollow(p);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bOut);
        out.writeObject(p);
        Utils.isPClean(p);
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        p.getNonDFGString();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        p.getName();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        p.jdoPostLoadCalledCounter = 0;
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.isHollow(p);
        Assert.assertEquals(0, p.jdoPostLoadCalledCounter);
        p.getName();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        Utils.isPClean(p);
        bOut = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bOut);
        out.writeObject(p);
        Utils.isPClean(p);
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        p.getNonDFGString();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        p.getName();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.close();

    }

    public void testPostLoadFromQuery() throws Exception {

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);

        pm.currentTransaction().begin();
        Person p = new Person("pl");
        p.setIntField(123);
        p.setNonDFGString("nonDFG");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "intField == 123");
        p = (Person)((Collection)q.execute()).iterator().next();
        p.getIntField();
        Assert.assertEquals(1, p.jdoPostLoadCalledCounter);
        pm.close();
    }

    /**
     * Make sure that the various instance callback methods get invoked
     * at the correct times.
     */
    public void testInstanceCallbacks() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        System.out.println("\n*** Persisting new instance");
        pm.currentTransaction().begin();
        InstanceCallbacksClass ic = new InstanceCallbacksClass(10);
        pm.makePersistent(ic);
        ic.check(0, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 1, 0);

        System.out.println("\n*** Updating existing instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        ic.setAge(ic.getAge() + 1);
        ic.check(1, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 1, 0);

        System.out.println("\n*** Deleting instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        pm.deletePersistent(ic);
        ic.check(0, 0, 0, 1);
        pm.currentTransaction().commit();
        ic.check(0, 0, 0, 0);

        // repeat tests with retainValues + optimistic and make sure
        // preClear is not called

        System.out.println(
                "\n*** Persisting new instance with retainValues + optimistic");
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        ic = new InstanceCallbacksClass(20);
        pm.makePersistent(ic);
        ic.check(0, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 0, 0);

        System.out.println("\n*** Updating existing instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        ic.setAge(ic.getAge() + 1);
        ic.check(1, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 0, 0);

        System.out.println("\n*** Deleting instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        pm.deletePersistent(ic);
        ic.check(0, 0, 0, 1);
        pm.currentTransaction().commit();
        ic.check(0, 0, 0, 0);

        pm.close();
    }

    /**
     * Make sure that the various instance callback methods get invoked
     * at the correct times.
     */
    public void testInstanceCallbacks2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        System.out.println("\n*** Persisting new instance");
        pm.currentTransaction().begin();
        InstanceCallbacksClass ic = new InstanceCallbacksClass(10);
        pm.makePersistent(ic);
        ic.check(0, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 1, 0);

        System.out.println("\n*** Updating existing instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        ic.setAge(ic.getAge() + 1);
        ic.check(1, 0, 0, 0);
        ((VersantPersistenceManager)pm).flush();
        pm.currentTransaction().commit();
        ic.check(0, 1, 1, 0);

        System.out.println("\n*** Deleting instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        pm.deletePersistent(ic);
        ic.check(0, 0, 0, 1);
        pm.currentTransaction().commit();
        ic.check(0, 0, 0, 0);

        // repeat tests with retainValues + optimistic and make sure
        // preClear is not called

        System.out.println(
                "\n*** Persisting new instance with retainValues + optimistic");
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        ic = new InstanceCallbacksClass(20);
        pm.makePersistent(ic);
        ic.check(0, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 0, 0);

        System.out.println("\n*** Updating existing instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        ic.setAge(ic.getAge() + 1);
        ic.check(1, 0, 0, 0);
        pm.currentTransaction().commit();
        ic.check(0, 1, 0, 0);

        System.out.println("\n*** Deleting instance");
        pm.currentTransaction().begin();
        ic.check(0, 0, 0, 0);
        pm.deletePersistent(ic);
        ic.check(0, 0, 0, 1);
        pm.currentTransaction().commit();
        ic.check(0, 0, 0, 0);

        pm.close();
    }

    /**
     * Make sure the Synchronization interface works.
     */
    public void testSynchronization() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Sync sync = new Sync();
        pm.currentTransaction().setSynchronization(sync);

        System.out.println("\n*** Commit");
        pm.currentTransaction().begin();
        sync.check("");
        pm.currentTransaction().commit();
        sync.check("before after " + Status.STATUS_COMMITTING +
                " after " + Status.STATUS_COMMITTED);

        System.out.println("\n*** Rollback");
        pm.currentTransaction().begin();
        sync.check("");
        pm.currentTransaction().rollback();
        sync.check("after " + Status.STATUS_ROLLING_BACK +
                " after " + Status.STATUS_ROLLEDBACK);

        pm.close();

    }

    private static class Sync implements Synchronization {

        StringBuffer buf = new StringBuffer();

        public void beforeCompletion() {
            System.out.println("Sync.beforeCompletion");
            if (buf.length() > 0) buf.append(' ');
            buf.append("before");
        }

        public void afterCompletion(int status) {
            System.out.println("Sync.afterCompletion");
            if (buf.length() > 0) buf.append(' ');
            buf.append("after ");
            buf.append(status);
        }

        public void resetCounters() {
            System.out.println("Sync.resetCounters");
            buf.setLength(0);
        }

        /**
         * Assert counter values and reset.
         */
        public void check(String s) {
            Assert.assertEquals(s, buf.toString());
            resetCounters();
        }

    }

}
