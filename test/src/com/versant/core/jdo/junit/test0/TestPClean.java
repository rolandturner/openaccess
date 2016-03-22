
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
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;
import java.util.Date;

/**
 * @keep-all This test the state changes from P-Clean.
 */
public class TestPClean extends VersantTestCase {

    public TestPClean(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testMakePersistent",
            "testDeletePersistent",
            "testMakeTransactional",
            "testMakeNonTransactional",
            "testMakeTransient",
            "testCommit",
            "testCommitRetainValues",
            "testRollback",
            "testRollbackRetainValues",
            "testRollbackRestoreValues",
            "testRefreshDS",
            "testRefreshOptimistic",
            "testEvict",
            "testReadOptimistic",
            "testReadDS",
            "testWrite",
            "testRetrieveOptimistic",
            "testRetrieveDS",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestPClean(a[i]));
        }
        return s;
    }

    public void testMakePersistent() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.makePersistent(p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testDeletePersistent() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.deletePersistent(p);
        Assert.assertTrue(Utils.isPDeleted(p));
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testMakeTransactional() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.makeTransactional(p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testMakeNonTransactional() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.makeNontransactional(p);
        Assert.assertTrue(Utils.isPNonTx(p));
        pm.close();
    }

    public void testMakeTransient() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.makeTransient(p);
        Assert.assertTrue(Utils.isTransient(p));
        pm.close();
    }

    public void testCommit() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isHollow(p));
        pm.close();
    }

    public void testCommitRetainValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.currentTransaction().commit();
        Assert.assertTrue(Utils.isPNonTx(p));
        pm.close();
    }

    public void testRollback() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.currentTransaction().rollback();
        Assert.assertTrue(Utils.isHollow(p));
        pm.close();
    }

    public void testRollbackRetainValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.currentTransaction().rollback();
        Assert.assertTrue(Utils.isHollow(p));
        pm.close();
    }

    public void testRollbackRestoreValues() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.currentTransaction().rollback();
        Assert.assertTrue(Utils.isPNonTx(p));
        pm.close();
    }

    /**
     *
     */
    public void testRefreshDS() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.refresh(p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testRefreshOptimistic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        p.getName();
        pm.makeTransactional(p);
        Assert.assertTrue(Utils.isPClean(p));

        pm.refresh(p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testEvict() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.evict(p);
        Assert.assertTrue(Utils.isHollow(p));
        pm.close();
    }

    public void testReadOptimistic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        p.getName();
        pm.makeTransactional(p);
        Assert.assertTrue(Utils.isPClean(p));

        p.getBirthDate();
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testReadDS() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        p.getBirthDate();
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testWrite() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        p.setBirthDate(new Date(TIME1));
        Assert.assertTrue(Utils.isPDirty(p));
        pm.close();
    }

    public void testRetrieveOptimistic() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        p.getName();
        pm.makeTransactional(p);
        Assert.assertTrue(Utils.isPClean(p));

        pm.retrieve(p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    public void testRetrieveDS() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = getPClean(pm);

        pm.retrieve(p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.close();
    }

    private Person getPClean(PersistenceManager pm) {
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        p.getName();
        Assert.assertTrue(Utils.isPClean(p));
        return p;
    }

}

