
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
import com.versant.core.common.Debug;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.*;
import java.util.List;

/**
 * @keep-all
 */
public class TestConcurrentUsage extends VersantTestCase {

    public TestConcurrentUsage(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testUpdate",
            "testListAccess",
            "test1",
            "test1a",
            "test2",
            "test3",
            "testRollbackFromConcurrent1",
            "testRollbackFromConcurrent2",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestConcurrentUsage(a[i]));
        }
        return s;
    }

    public void testUpdate() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setOptimistic(true);
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);

        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(id, true);
        p2.getName();
        pm2.currentTransaction().commit();

        pm1.currentTransaction().begin();
        Person p3 = (Person)pm1.getObjectById(id, true);
        p3.setName("name3");
        pm1.currentTransaction().commit();

        pm2.currentTransaction().begin();
        p2 = (Person)pm2.getObjectById(id, true);
        p2.setName("name2");
        try {
            pm2.currentTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        pm2.currentTransaction().begin();
        p2 = (Person)pm2.getObjectById(id, true);
        p2.setName("name4");
        pm2.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    public void testListAccess() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        Person p = new Person("testCon");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        PersistenceManager pm1 = pmf().getPersistenceManager();
        PersistenceManager pm2 = pmf().getPersistenceManager();

        pm1.currentTransaction().setOptimistic(true);
        pm1.currentTransaction().begin();
        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().begin();
        Person p1 = (Person)pm1.getObjectById(id, true);
        List sList = p1.getOrderedStringList();
        sList.add("val1");
        Person p2 = (Person)pm2.getObjectById(id, true);
        pm1.currentTransaction().commit();
        sList = p2.getOrderedStringList();
        sList.add("val2");
        try {
            pm2.currentTransaction().commit();
        } catch (JDOFatalDataStoreException e) {
            e.printStackTrace(Debug.OUT);
            // interbase sees this as a deadlock and throws an SQLException
            if (!getSubStoreInfo().getDataStoreType().equals("interbase")) {
                Assert.assertTrue(
                        e instanceof JDOOptimisticVerificationException);
            }
//            pm2.currentTransaction().rollback();
        }

        Assert.assertTrue(!pm1.currentTransaction().isActive());
        Assert.assertTrue(!pm2.currentTransaction().isActive());

        pm2.currentTransaction().begin();
        p2.setVal("val3");
        pm2.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    /**
     * This test should not fail because the values is not retained and therefore it
     * will refetch the values.
     *
     * @throws Exception
     */
    public void test1() throws Exception {
        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setNontransactionalRead(true);
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setNontransactionalRead(true);

        pm1.currentTransaction().begin();
        Person p = new Person("name1");
        pm1.makePersistent(p);
        pm1.currentTransaction().commit();

        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(JDOHelper.getObjectId(p), false);
        p2.setName("name2");
        pm2.currentTransaction().commit();

        pm1.currentTransaction().begin();
        p.setName("name2");
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    /**
     * This test should not fail because the values is not retained and therefore it
     * will refetch the values.
     *
     * @throws Exception
     */
    public void test1a() throws Exception {
        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setNontransactionalRead(true);
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setNontransactionalRead(true);

        pm1.currentTransaction().begin();
        Person p = new Person("name1");
        pm1.makePersistent(p);
        pm1.currentTransaction().commit();

        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().begin();
        pm1.currentTransaction().setOptimistic(true);
        pm1.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(JDOHelper.getObjectId(p), false);
        p2.setName("name2");
        p.setName("name3");
        pm2.currentTransaction().commit();
        try {
            pm1.currentTransaction().commit();
            Utils.fail("A concurrent update should have happened");
        } catch (JDOException e) {
            //ignore
        }
        pm1.close();
        pm2.close();
    }

    public void test2() throws Exception {
        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setNontransactionalRead(true);
        pm1.currentTransaction().setRetainValues(true);
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setNontransactionalRead(true);
        pm2.currentTransaction().setRetainValues(true);

        pm1.currentTransaction().begin();
        Person p = new Person("name1");
        pm1.makePersistent(p);
        pm1.currentTransaction().commit();

        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(JDOHelper.getObjectId(p), false);
        p2.setName("name2");
        pm2.currentTransaction().commit();

        pm1.currentTransaction().begin();
        p.setName("name2");
        try {
            pm1.currentTransaction().commit();
            Utils.fail("A concurrent update should have happened");
        } catch (JDOException e) {
            //ignore
        }

        pm1.currentTransaction().begin();
        p.setName("name2");
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    /**
     * This test a concurrent update on different fields. This test is only valid if
     * concurrent updates is done on diff instead of rowversion
     *
     * @throws Exception
     */
    public void test3() throws Exception {
        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setNontransactionalRead(true);
        pm1.currentTransaction().setRetainValues(true);
        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setNontransactionalRead(true);
        pm2.currentTransaction().setRetainValues(true);

        pm1.currentTransaction().begin();
        Person p = new Person("name1");
        p.setVal("val1");
        pm1.makePersistent(p);
        pm1.currentTransaction().commit();

        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(JDOHelper.getObjectId(p), false);
        p2.setName("name2");
        pm2.currentTransaction().commit();

        pm1.currentTransaction().begin();
        p.setVal("val2");
        pm1.currentTransaction().commit();

        pm1.close();
        pm2.close();
    }

    public void testRollbackFromConcurrent1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p1");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        Object id = pm.getObjectId(p);
        pm.close();

        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setOptimistic(true);
        pm1.currentTransaction().begin();
        Person p1 = (Person)pm1.getObjectById(id, true);
        p1.setName("p2");

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(id, true);
        p2.setName("p3");
        pm2.currentTransaction().commit();

        try {
            pm1.currentTransaction().commit();
        } catch (JDOException e) {
            //ignore
//            pm1.currentTransaction().rollback();
        }

        pm1.currentTransaction().begin();
        p1 = (Person)pm1.getObjectById(id, true);
        Assert.assertEquals("p3", p1.getName());
        pm2.close();
        pm1.close();
    }

    public void testRollbackFromConcurrent2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p1");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        Object id = pm.getObjectId(p);
        pm.close();

        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setRetainValues(true);
        pm1.currentTransaction().setOptimistic(true);
        pm1.currentTransaction().begin();
        Person p1 = (Person)pm1.getObjectById(id, true);
        p1.setName("p2");

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(id, true);
        p2.setName("p3");
        pm2.currentTransaction().commit();

        try {
            pm1.currentTransaction().commit();
        } catch (JDOException e) {
            //ignore
//            pm1.currentTransaction().rollback();
        }

        pm1.currentTransaction().begin();
        Assert.assertEquals("p3", p1.getName());
        pm1.currentTransaction().commit();
        pm1.close();
        pm2.close();
    }

}
