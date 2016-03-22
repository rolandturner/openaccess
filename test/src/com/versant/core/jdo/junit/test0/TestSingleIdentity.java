
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

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.singleID.*;
import com.versant.core.jdo.VersantPersistenceManagerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jdo.JDOHelper;
import javax.jdo.Extent;
import javax.jdo.identity.SingleFieldIdentity;
import java.util.*;

public class TestSingleIdentity extends VersantTestCase {
    protected static final int TEST_OBJECT_COUNT = 10;

    protected Object[] ids = new Object[TEST_OBJECT_COUNT];
    protected TestObject[] objs = new TestObject[TEST_OBJECT_COUNT];

    public TestSingleIdentity(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("SingleIdentity");
        String[] a = new String[]{
                "testStringIdentity",
                "testCharIdentity",
                "testCharObjectIdentity",
                "testCharObjectIdentity2",
                "testSubCharObjectIdentity2",
                "testLongIdentity",
                "testLongObjectIdentity",
                "testIntegerIdentity",
                "testIntegerObjectIdentity",
                "testShortIdentity",
                "testShortObjectIdentity",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestSingleIdentity(a[i]));
        }
        return s;
    }

    /**
     * Test for SingleField StringIdentity.
     *
     * @throws Exception
     */
    public void testStringIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityString.class);
    }

    /**
     * Test for SingleField CharIdentity.
     *
     * @throws Exception
     */
    public void testCharIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityChar.class);
    }

    /**
     * Test for SingleField CharIdentity (object form).
     *
     * @throws Exception
     */
    public void testCharObjectIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityCharObject.class);
    }

    /**
     * Test for SingleField CharIdentity (object form).
     *
     * @throws Exception
     */
    public void testCharObjectIdentity2()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }

        runStorageFor(SingleIdentityCharObject2.class);
    }

    /**
     * Test for SingleField CharIdentity (object form).
     *
     * @throws Exception
     */
    public void testSubCharObjectIdentity2()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentitySubCharObject2.class);
    }



    /**
     * Test for SingleField LongIdentity.
     *
     * @throws Exception
     */
    public void testLongIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityLong.class);
    }

    /**
     * Test for SingleField LongIdentity (object form).
     *
     * @throws Exception
     */
    public void testLongObjectIdentity()
            throws Exception {

        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityLongObject.class);
    }

    /**
     * Test for SingleField IntegerIdentity.
     *
     * @throws Exception
     */
    public void testIntegerIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityInteger.class);
    }

    /**
     * Test for SingleField IntegerIdentity (object form).
     *
     * @throws Exception
     */
    public void testIntegerObjectIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityIntegerObject.class);
    }

    /**
     * Test for SingleField ShortIdentity.
     *
     * @throws Exception
     */
    public void testShortIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityShort.class);
    }

    /**
     * Test for SingleField ShortIdentity (object form).
     *
     * @throws Exception
     */
    public void testShortObjectIdentity()
            throws Exception {
        if (isVds()) {
            unsupported();
            return;
        }
        runStorageFor(SingleIdentityShortObject.class);
    }

    protected void runStorageFor(Class c) throws Exception {
        insertObjects(c);
        validateObjects(c);
        updateObjects(c);
        validateObjects(c);
        iterateUsingExtent(c);
        validateTransactionalRefresh(c);
        removeObjects();
//        validateNewObjectRollback(c);
    }

    /**
     * Asserts that the persistent fields of two test objects are equal using
     * the <tt>compareTo()</tt> method.  The <tt>equals()</tt> method cannot be
     * used for this purpose because, for most persistence-capable objects
     * (including all our test widgets), it only compares JDO identity.
     *
     * @param expected An object having the expected field values.
     * @param actual   The object to compare fields against.
     */
    protected void assertFieldsEqual(TestObject expected, TestObject actual) {
        assertTrue("Incorrect field values in object, was " + actual + ", should be " + expected, actual.compareTo(expected));
    }


    protected void assertResultsEqual(Set expected, Collection results) {
        if (!expected.isEmpty() || !results.isEmpty()) {
            assertTrue("Query has no expected results (test is broken)", !expected.isEmpty());
            assertTrue("Query returned no rows", !results.isEmpty());

            HashSet actual = new HashSet(results);

            assertEquals("Query returned duplicate rows", results.size(), actual.size());
            assertEquals("Query did not return expected results", expected, actual);
        }
    }


    protected void insertObjects(Class c) throws Exception {
        /*
         * Insert TEST_OBJECT_COUNT random objects.
         */

        PersistenceManager pm = pmf().getPersistenceManager();

        try {
            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                pm.currentTransaction().setRetainValues(true);
                pm.currentTransaction().begin();

                TestObject obj = (TestObject) c.newInstance();
                obj.fillRandom();
                objs[i] = (TestObject) obj.clone();
                assertFieldsEqual(obj, objs[i]);
                pm.makePersistent(obj);
                ids[i] = JDOHelper.getObjectId(obj);
                pm.currentTransaction().commit();
            }
        } finally {
            if (pm.currentTransaction().isActive())
                pm.currentTransaction().rollback();

            pm.close();
        }
    }


    protected void validateObjects(Class c) throws Exception {
        /*
         * Read them back and verify that they contain what they should.
         */

        PersistenceManager pm = pmf().getPersistenceManager();

        try {
            TestObject[] loaded = new TestObject[TEST_OBJECT_COUNT];

            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                pm.currentTransaction().begin();
                TestObject obj = (TestObject) pm.getObjectById(ids[i], true);
                assertFieldsEqual(objs[i], obj);
                loaded[i] = obj;
                pm.currentTransaction().commit();
            }
        } finally {
            if (pm.currentTransaction().isActive())
                pm.currentTransaction().rollback();

            pm.close();
        }


        /*
         * Read some of them back and verify them using non-transactional reads.
         * Only some are done because non-transactional reads are much slower
         * unless connection pooling is used (eventually we should use pooling
         * when testing).
         */

        pm = pmf().getPersistenceManager();

        try {
            pm.currentTransaction().setNontransactionalRead(true);
            for (int i = 0; i < TEST_OBJECT_COUNT; i += 10) {
                TestObject obj = (TestObject) pm.getObjectById(ids[i], false);
                assertFieldsEqual(objs[i], obj);
            }
        } finally {
            pm.close();
        }

        /*
         * Read some of them back, verify them, then verify values get retained
         * after commit when retainValues mode is on.
         */

        pm = pmf().getPersistenceManager();

        try {
            pm.currentTransaction().setRetainValues(true);
            pm.currentTransaction().begin();
            TestObject[] loaded = new TestObject[TEST_OBJECT_COUNT];
            for (int i = 0; i < TEST_OBJECT_COUNT; i += 10) {
                TestObject obj = (TestObject) pm.getObjectById(ids[i], true);
                assertFieldsEqual(objs[i], obj);
                loaded[i] = obj;
            }
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();  // so we dont get javax.jdo.JDOUserException:
            // Must set nonTransactionalRead to true
            for (int i = 0; i < TEST_OBJECT_COUNT; i += 10) {
                assertFieldsEqual(objs[i], loaded[i]);
            }
            pm.currentTransaction().commit();
        } finally {
            if (pm.currentTransaction().isActive())
                pm.currentTransaction().rollback();

            pm.close();
        }
    }


    protected void updateObjects(Class c) throws Exception {
        /*
         * Update them all with new values.
         */

        PersistenceManager pm = pmf().getPersistenceManager();

        try {
            /*
             * Test basic update functionality by filling each object with new
             * random data.
             */
            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                pm.currentTransaction().setRetainValues(true);
                pm.currentTransaction().begin();

                TestObject obj = (TestObject) pm.getObjectById(ids[i], false);
                obj.fillUpdateRandom();
                objs[i] = (TestObject) obj.clone();
                assertFieldsEqual(obj, objs[i]);
                pm.currentTransaction().commit();
            }
        } finally {
            if (pm.currentTransaction().isActive()) {
                pm.currentTransaction().rollback();
            }
            pm.close();
        }
    }


    protected void iterateUsingExtent(Class c) throws Exception {
        /*
         * Iterate over them using an Extent and verify that they're all
         * returned.
         */

        PersistenceManager pm = ((VersantPersistenceManagerFactory)pmf()).getPersistenceManager();

        try {
            pm.currentTransaction().begin();

            Extent extent = pm.getExtent(c, true);
            Iterator ei = extent.iterator();
            try {
                HashSet returned = new HashSet();
                while (ei.hasNext()) {
                    TestObject obj = (TestObject) ei.next();
                    assertTrue("Object returned twice from Extent iterator: " + obj, returned.add(obj));
                }
                assertEquals(TEST_OBJECT_COUNT, returned.size());
                for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                    TestObject obj = (TestObject) pm.getObjectById(ids[i], true);
                    assertTrue("Object never returned from Extent iterator: " + obj, returned.remove(obj));
                }
            }
            finally {
                extent.close(ei);
            }
            pm.currentTransaction().commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (pm.currentTransaction().isActive()) {
                pm.currentTransaction().rollback();
            }
            pm.close();
        }
    }


    protected void validateTransactionalRefresh(Class c) throws Exception {
        /*
         * Validate that persistent non-transactional objects transition to
         * persistent, and refresh themselves, when accessed from within a
         * transaction.
         */

        PersistenceManager pm1 = pmf().getPersistenceManager();
        Transaction tx1 = pm1.currentTransaction();

        tx1.setRetainValues(true);

        try {
            PersistenceManager pm2 = pmf().getPersistenceManager();
            Transaction tx2 = pm2.currentTransaction();

            Random rnd = new Random(0);
            TestObject[] pobjs = new TestObject[TEST_OBJECT_COUNT];

            try {
                /* Load all of the objects using pm1. */
                tx1.begin();

                for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                    /* Half will be Hollow and half PersistentClean. */
                    boolean validate = rnd.nextBoolean();

                    pobjs[i] = (TestObject) pm1.getObjectById(ids[i], validate);

                    /* Half of the PersistentClean will be fully loaded. */
                    if (validate && rnd.nextBoolean())
                        assertFieldsEqual(objs[i], pobjs[i]);
                }

                tx1.commit();

                for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                    assertTrue("Object is not persistent: " + ids[i], JDOHelper.isPersistent(pobjs[i]));
                    assertTrue("Object is transactional: " + ids[i], !JDOHelper.isTransactional(pobjs[i]));
                }

                /* Modify them all using pm2. */
                tx2.begin();

                for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                    TestObject obj = (TestObject) pm2.getObjectById(ids[i], false);
                    obj.fillUpdateRandom();

                    objs[i] = (TestObject) obj.clone();

                    assertFieldsEqual(obj, objs[i]);
                }

                tx2.commit();
                if (!tx1.getOptimistic()) {
                    /* Access them all inside a transaction using pm1. */
                    tx1.begin();

                    for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                        assertTrue("Object is not persistent: " + ids[i], JDOHelper.isPersistent(pobjs[i]));
                        assertTrue("Object is transactional: " + ids[i], !JDOHelper.isTransactional(pobjs[i]));
                        assertFieldsEqual(objs[i], pobjs[i]);
                        assertTrue("Object is not persistent: " + ids[i], JDOHelper.isPersistent(pobjs[i]));
                        assertTrue("Object is not transactional: " + ids[i], JDOHelper.isTransactional(pobjs[i]));
                    }

                    tx1.commit();
                }
            } finally {
                if (tx2.isActive())
                    tx2.rollback();

                pm2.close();
            }
        } finally {
            if (tx1.isActive())
                tx1.rollback();

            pm1.close();
        }
    }


    protected void removeObjects() throws Exception {
        /*
         * Remove all of the objects.
         */

        PersistenceManager pm = pmf().getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        try {
            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                tx.begin();

                TestObject obj = (TestObject) pm.getObjectById(ids[i], false);

                pm.deletePersistent(obj);

                tx.commit();
            }
        } finally {
            if (tx.isActive())
                tx.rollback();

            pm.close();
        }
    }


    protected void validateNewObjectRollback(Class c) throws Exception {
        /*
         * Create TEST_OBJECT_COUNT random objects, update them, rollback the
         * transaction, and verify they return to being transient objects
         * having their former values.  Requires RestoreValues == true in order
         * to get the restoration on rollback.
         */

        PersistenceManager pm = pmf().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        tx.setRestoreValues(true);
        try {
            TestObject[] pobjs = new TestObject[TEST_OBJECT_COUNT];

            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                objs[i] = (TestObject) c.newInstance();
                objs[i].fillRandom();

                pobjs[i] = (TestObject) objs[i].clone();
            }

            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                tx.begin();
                pm.makePersistent(pobjs[i]);
                pobjs[i].fillRandom();
                tx.rollback();
            }

            for (int i = 0; i < TEST_OBJECT_COUNT; ++i) {
                assertNull(JDOHelper.getPersistenceManager(pobjs[i]));
                assertFieldsEqual(objs[i], pobjs[i]);
            }
        } finally {
            if (tx.isActive())
                tx.rollback();

            pm.close();
        }
    }

}

