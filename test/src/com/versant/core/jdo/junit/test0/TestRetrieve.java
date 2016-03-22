
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
import com.versant.core.jdo.junit.test0.model.*;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

/**
 * Tests for pm.retrieveXXX calls.
 */
public class TestRetrieve extends VersantTestCase {

    public TestRetrieve(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testRetrieveGraph",
            "testRetrieveHollowNonTX",
            "testBasicRetrieve",
            "testBasicRetrieve2",
            "testCircularRetrieve",
            "testRetrieveAllOnQueryResult",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestRetrieve(a[i]));
        }
        return s;
    }

    public void testRetrieveGraph() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Employee1 emp1 = new Employee1();
        emp1.setName("emp1");

        Company comp = new Company();
        comp.setName("comp");
        comp.setVal("val");

        Manager man = new Manager();
        man.setName("man");
        man.setAge(21);
        emp1.setCompany(comp);
        comp.setManager(man);

        pm.makePersistent(emp1);
        Object id = pm.getObjectId(emp1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        emp1 = (Employee1)pm.getObjectById(id, false);
        pm.retrieve(emp1);

        pm.close();

    }

    public void testRetrieveHollowNonTX() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        pm.retrieve(p);
        Assert.assertTrue("The instance must be P-NonTx.", Utils.isPNonTx(p));
        pm.close();
    }

    /**
     * This is a test to ensure that retrieve does actually fill all the fields.
     */
    public void testBasicRetrieve() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p");
        Person child = new Person("child");
        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);
        p.getPersonsSet().add(child);
        p.getPersonsList().add(child);

        Set sSet = new HashSet();
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            sSet.add("string" + i);
            sList.add("sstring" + i);
        }

        p.setStringSet(sSet);
        p.setStringList(sList);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.retrieve(p);
        pm.makeTransient(p);
        pm.makeTransient(child);
        pm.makeTransient(address);
        pm.currentTransaction().commit();

        Assert.assertEquals(sSet, p.getStringSet());
        assertEqualsUnorderd(sList, p.getStringList());
        Assert.assertTrue(p.getPersonsSet().contains(child));
        Assert.assertEquals(child, p.getPersonsList().get(0));
        Assert.assertEquals(address, p.getAddress());
        pm.close();
    }

    /**
     * This is a test to ensure that retrieve does actually fill all the fields.
     */
    public void testBasicRetrieve2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p");
        Person child = new Person("child");
        Address address = new Address();
        address.setStreet("street");
        p.setAddress(address);

        Set sSet = new HashSet();
        List sList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            sSet.add("string" + i);
            sList.add("sstring" + i);
        }

        p.setStringSet(sSet);
        p.setStringList(sList);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getPersonsSet().add(child);
        p.getPersonsList().add(child);
        pm.makePersistent(p);
        pm.retrieve(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.retrieve(p);
        pm.makeTransient(p);
        pm.makeTransient(child);
        pm.makeTransient(address);
        pm.currentTransaction().commit();

        Assert.assertEquals(sSet, p.getStringSet());
        assertEqualsUnorderd(sList, p.getStringList());
        Assert.assertTrue(p.getPersonsSet().contains(child));
        Assert.assertEquals(child, p.getPersonsList().get(0));
        Assert.assertEquals(address, p.getAddress());
        pm.close();
    }

    private void assertEqualsUnorderd(Collection a, Collection b) {
        HashSet sa = new HashSet(a);
        HashSet sb = new HashSet(b);
        Assert.assertEquals(sa, sb);
    }

    public void testCircularRetrieve() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Person p1 = new Person("p1");
        p1.setVal("val1");
        Person p2 = new Person("p2");
        p2.setVal("val2");

        p1.addPersonToList(p2);
        p2.addPersonToList(p1);
        pm.makePersistent(p1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.retrieve(p1);
        pm.makeTransient(p1);
        pm.makeTransient(p2);
        pm.currentTransaction().commit();

        Assert.assertEquals("val1", p1.getVal());
        Assert.assertEquals("val2", p2.getVal());
        Assert.assertEquals(p1, p2.getPersonsList().get(0));
        Assert.assertEquals(p2, p1.getPersonsList().get(0));

        pm.close();
    }

    /**
     * Test retrieveAll applied directly to the result of a query.
     */
    public void testRetrieveAllOnQueryResult() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        nuke(Person.class);
        nuke(Address.class);

        pm.currentTransaction().begin();
        int n = 10;
        for (int i = 0; i < n; i++) {
            pm.makePersistent(new Address("street" + i));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Address.class);
        Collection ans = (Collection)q.execute();
        pm.retrieveAll(ans);
        pm.currentTransaction().commit();
        pm.close();

        nuke(Address.class);
    }

}
