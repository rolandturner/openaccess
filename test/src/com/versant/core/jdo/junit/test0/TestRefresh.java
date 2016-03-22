
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
import com.versant.core.jdo.junit.test0.model.Address;
import com.versant.core.jdo.junit.test0.model.Person;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @keep-all
 */
public class TestRefresh extends VersantTestCase {

    public TestRefresh(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testRefreshByQuery",
            "testRefreshOnOptimisticStaleInstance",
            "testRefresh",
            "testRefreshDsTx",
            "testRefreshNonTxInstance",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestRefresh(a[i]));
        }
        return s;
    }

    public void testRefreshByQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String prefix = "" + System.currentTimeMillis();
        Person p = new Person(prefix);
        Address a = new Address("bla");
        p.setAddress(a);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("changed");
        Query q = pm.newQuery(Person.class);
        q.setFilter("name.startsWith(nParam)");
        q.declareParameters("String nParam");
        Collection result = (Collection)q.execute(prefix);
        pm.refreshAll(result);
        countExecQueryEvents();
        Assert.assertEquals(prefix, p.getName());
        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void testRefresh() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = new Person("main");
        p.getPersonsList().add(new Person("p1"));
        p.getPersonsList().add(new Person("p2"));
        p.getPersonsList().add(new Person("p3"));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        PersistenceManager pm1 = pmf().getPersistenceManager();
        pm1.currentTransaction().setOptimistic(true);
        pm1.currentTransaction().setRetainValues(true);
        pm1.currentTransaction().begin();
        Person p1 = (Person)pm1.getObjectById(JDOHelper.getObjectId(p), false);
        Assert.assertEquals(3, p1.getPersonsList().size());
        p1.getPersonsList().add(new Person("p4"));
        pm1.currentTransaction().commit();

        p.getName();
        pm.refresh(p);
        Assert.assertEquals(4, p.getPersonsList().size());
        pm.currentTransaction().commit();

        pm.close();
        pm1.close();
    }

    public void testRefreshDsTx() throws Exception {
        // TODO figure out why this fails on Interbase - tx levels?
        // this fails in MySQL as MySQL ensures consistent reads i.e. you
        // always see rows as they were at the start of your tx
        String dname = getSubStoreInfo().getDataStoreType();
        if (dname.equals("interbase") || dname.equals("mysql")) return;

        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.setName("p1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        changeName(pm.getObjectId(p), "p3");
        p.setName("p2");
        pm.refresh(p);
        Assert.assertEquals("p3", p.getName());
        pm.close();
    }

    public void changeName(Object id, String name) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = (Person)pm.getObjectById(id, true);
        p.setName(name);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRefreshOnOptimisticStaleInstance() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("name1", p.getName());
        changeName(pm.getObjectId(p), "name2");
        Assert.assertEquals("name1", p.getName());
        pm.refresh(p);
        Assert.assertEquals("name2", p.getName());
        pm.close();
    }

    /**
     * Make sure that refreshing a non-tx instance in a datastore tx goes
     * to the database immediately.
     */
    public void testRefreshNonTxInstance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somewhere");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        // touch a field outside a tx to make instance non-transactional
        pm.currentTransaction().setNontransactionalRead(true);
        System.out.println("a.getStreet() = " + a.getStreet());

        // now refresh it inside a datastore tx and make sure there is a select
        pm.currentTransaction().begin();
        findExecQuerySQL();
        pm.refresh(a);
        if (isJdbc()) {
            String sql = findExecQuerySQL();
            Assert.assertTrue(sql.indexOf("SELECT") >= 0);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();

        pm.close();
    }

}
