
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
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.test0.model.SimpleAP_KeyGen;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @keep-all This tests are for events that can take place on a Persistent-New instance.
 */
public class TestPNew extends VersantTestCase {

    public TestPNew(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testMakePersistent",
            "testDeletePersistent",
            "testMakeTransactional",
            "testMakeNontransactional",
            "testMakeTransient",
            "testCommitNonRetainValues",
            "testCommitRetainValues",
            "testRollback",
            "testRollbackRestoreValues",
            "testRefreshDS",
            "testRefreshDSOptimistic",
            "testEvict",
            "testReadFieldOptimistic",
            "testReadFieldOptimistic",
            "testReadFieldDS",
            "testWriteField",
            "testRetrieveOptimistic",
            "testRetrieveDS",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestPNew(a[i]));
        }
        return s;
    }

    /**
     * This is a simple makePersistent call. This should be ignored by the
     * instance and no state change should occur.
     */
    public void testMakePersistent() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.makePersistent(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * This is a simple deletePersistent call. This should change the state of
     * the instance to P-New-Deleted.
     */
    public void testDeletePersistent() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.deletePersistent(p);
        Assert.assertTrue(Utils.isPNewDeleted(p));
        pm.close();
    }

    /**
     * This is a simple deletePersistent call. This should be ignored by the
     * instance and no state change should occur.
     */
    public void testMakeTransactional() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.makeTransactional(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * This is a simple makeNonTransactional call. This must throw
     * an JDOUserException.
     */
    public void testMakeNontransactional() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        try {
            pm.makeNontransactional(p);
            Utils.fail("This should fail");
        } catch (TestFailedException e) {
            throw e;
        } catch (JDOUserException e) {
            e.printStackTrace(Debug.OUT);
            //ignore
        } catch (Exception e) {
            throw new TestFailedException(e);
        }
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * This is a simple makeTransient call. This must throw
     * an JDOUserException.
     */
    public void testMakeTransient() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        try {
            pm.makeTransient(p);
            Utils.fail("This should fail");
        } catch (TestFailedException e) {
            throw e;
        } catch (JDOUserException e) {
            e.printStackTrace(Debug.OUT);
            //ignore
        } catch (Exception e) {
            throw new TestFailedException(e);
        }
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * This tests a simple commit with <code>retainValue</code> to
     * <code>false</code>.
     * This instance must transition to hollow.
     */
    public void testCommitNonRetainValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isHollow(p));
        pm.close();
    }

    /**
     * This tests a simple commit with <code>retainValue</code> to
     * <code>true</code>.
     * This instance must transition to P-NonTx.
     */
    public void testCommitRetainValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isPNonTx(p));
        pm.close();
    }

    /**
     * This tests a simple rollback call with <code>restoreValue</code> to
     * <code>false</code>.
     */
    public void testRollback() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.currentTransaction().rollback();
        Assert.assertTrue(Utils.isTransient(p));
        pm.close();
    }

    /**
     * This tests rollback with <code>restoreValue</code> to
     * <code>true</code>. This implies that the state of the instance must be
     * restored as it was at the beginning of the tx.
     */
    public void testRollbackRestoreValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("name");
        Date now = new Date(TIME1);
        p.setBirthDate(now);
        SimpleAP_KeyGen simpleAP_keyGen1 = new SimpleAP_KeyGen();
        p.setSimpleAP_keyGen(simpleAP_keyGen1);
        List pList = new ArrayList();
        pList.add(new Person("p1"));
        pList.add(new Person("p2"));
        p.setPersonsList(pList);
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Assert.assertTrue(Utils.isPNew(p));
        p.setName("name2");
        p.setBirthDate(new Date(TIME2));
        p.setSimpleAP_keyGen(new SimpleAP_KeyGen());
        p.setPersonsList(new ArrayList());

        pm.currentTransaction().rollback();
        Assert.assertTrue(Utils.isTransient(p));
        /**
         * Test for String field.
         */
        Assert.assertEquals("name", p.getName());
        /**
         * Test for Date field.
         */
        Assert.assertEquals(now, p.getBirthDate());
        /**
         * Test for Ref field.
         */
        Assert.assertTrue(simpleAP_keyGen1 == p.getSimpleAP_keyGen());
        /**
         * Test for a collection field.
         */
        Assert.assertTrue(pList.equals(p.getPersonsList()));
        pm.close();
    }

    /**
     * Test refresh with active dataStore tx. This method should not do anything
     * because their is no state in the store to refresh from. The state should
     * stay as is.
     */
    public void testRefreshDS() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.refresh(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Test refresh with active optimistic tx. This method should not do anything
     * because their is no state in the store to refresh from. The state should
     * stay as is.
     */
    public void testRefreshDSOptimistic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.refresh(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Call evict on P-New instance. This implies that the state must be evicted
     * at commit even if retainValues is <code>true</code>.
     */
    public void testEvict() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.evict(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Read field with active optimistic tx. This should not change the state.
     */
    public void testReadFieldOptimistic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        p.getName();
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Read field with active dataStore tx. This should not change the state.
     */
    public void testReadFieldDS() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        p.getName();
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Read field with active tx. This should not change the state.
     */
    public void testWriteField() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        p.setName("name2");
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Test retrieve with active optimistic tx. No state change.
     */
    public void testRetrieveOptimistic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.retrieve(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    /**
     * Test retrieve with active ds tx. No state change.
     */
    public void testRetrieveDS() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = createPNew(pm);

        pm.retrieve(p);
        Assert.assertTrue(Utils.isPNew(p));
        pm.close();
    }

    private final Person createPNew(PersistenceManager pm) {
        Person p = new Person("name");
        pm.makePersistent(p);
        Assert.assertTrue(Utils.isPNew(p));
        return p;
    }
}

