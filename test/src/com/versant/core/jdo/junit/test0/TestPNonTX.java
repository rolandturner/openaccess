
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
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;

/**
 * @keep-all
 */
public class TestPNonTX extends VersantTestCase {

    public TestPNonTX(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testReadWithActiveDsTx",
            "testWriteWithActiveDsTx",
            "testRollbackWithActiveDsTx",
            "testRollbackWithActiveDsTxRetainValues",
            "testRollbackFromWriteWithActiceOptimisticTx",
            "testDSCommit",
//            "testRollbackFromWriteWithActiceOptimisticTx2",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestPNonTX(a[i]));
        }
        return s;
    }

    public void testReadWithActiveDsTx() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.getName();
        Assert.assertTrue("The instance must be P-Clean.", Utils.isPClean(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * For a read the state in memory is not preserved and hence after a rollback it will
     * be as in the db.
     *
     * @throws Exception
     */
    public void testReadWithActiveDsTxRollback() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.getName();
        Assert.assertTrue("The instance must be P-Clean.", Utils.isPClean(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be P-Clean.", Utils.isHollow(p));
        pm.close();
    }

    public void testWriteWithActiveDsTx() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.setName("name2");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * This is a write on a p-non tx instance. The instance in memory must be
     * kept for rollback.
     */
    public void testRollbackWithActiveDsTxRetainValues() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

//        List list = new ArrayList();
//        list.add("list1");
        pm.currentTransaction().begin();
        Person p = new Person("name1");
//        p.getStringList().addAll(list);

        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.setName("name");
        p.getStringList().add("list2");
//        list.add("list2");


        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.setName("name2");
        p.getStringList().add("list3");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        Assert.assertEquals("name", p.getName());
        Assert.assertEquals(0, p.getStringList().size());

        ((VersantPersistenceManager)pm).setInterceptDfgFieldAccess(true);
        pm.currentTransaction().begin();
        Assert.assertEquals("name1", p.getName());
        Assert.assertTrue("The instance must be P-Clean.", Utils.isPClean(p));
        pm.currentTransaction().rollback();

        pm.currentTransaction().begin();
        Assert.assertEquals("name1", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRollbackWithActiveDsTx() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.setName("name");

        pm.currentTransaction().setRetainValues(false);
        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.setName("name2");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        //this must be the value from the db that was last commited.
        Assert.assertEquals("name1", p.getName());
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));

        ((VersantPersistenceManager)pm).setInterceptDfgFieldAccess(true);
        pm.currentTransaction().begin();
        Assert.assertEquals("name1", p.getName());
        Assert.assertTrue("The instance must be P-Clean.", Utils.isPClean(p));
        pm.currentTransaction().rollback();

        pm.currentTransaction().begin();
        Assert.assertEquals("name1", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * If retainValues is set to false then the instance will always be hollow after a commit
     * or a rollback and therefore it overrides restoreValues.
     *
     * @throws Exception
     */
    public void testDSCommit() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));

        pm.currentTransaction().begin();
        p.setName("name2");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        Assert.assertEquals("name1", p.getName());
        pm.close();
    }

    public void testRollbackFromWriteWithActiceOptimisticTx() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        p.setName("name");

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        //here the non-tx state must be kept for rollback and the state must change to PDirty
        p.setName("name2");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        Assert.assertEquals("name", p.getName());

        pm.currentTransaction().begin();
        Assert.assertEquals("name", p.getName());
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        pm.currentTransaction().rollback();
//        Assert.assertEquals("name", p.getName());
//        pm.currentTransaction().begin();
//        Assert.assertEquals("name", p.getName());
//        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRollbackFromWriteWithActiceOptimisticTx2()
            throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(false);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        p.setName("name");

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        //here the non-tx state must be kept for rollback and the state must change to PDirty
        p.setName("name2");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        Assert.assertEquals("name1", p.getName());

        pm.currentTransaction().begin();
        Assert.assertEquals("name1", p.getName());
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        pm.currentTransaction().rollback();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        Assert.assertEquals("name1", p.getName());

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        Assert.assertEquals("name1", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMakeTxWithActiveDsTx() throws Exception {
    }

}
