
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
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.Address;
import com.versant.core.jdo.junit.test0.model.ClassA;
import com.versant.core.jdo.junit.test0.model.Employee;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import java.lang.reflect.Constructor;

/**
 * @keep-all This is tests for the getObejctById method on PersistenceManager.
 * @see javax.jdo.PersistenceManager
 */
public class TestGetObjectById extends VersantTestCase {

    public TestGetObjectById(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testTxInstanceInCacheValidateTrue",
            "testNonTxInstanceInCacheValidateTrue",
            "testNonTxInstanceInCacheValidateTrueOptimistic",
            "testGetNonAppIdObjectIdClass",
            "testGetObjectByIDString",
            "testGetNonAppIdObjectIdClassInHier",
            "testGetObjectByIdFromPreCommit",
            "testGetObjectByIdStringFromPreCommit",
            "testGetObjectByIdFromPreCommitSubClass",
            "testGetObjectByIdForListElement",
            "testNewObjectIdInstanceNullClass",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestGetObjectById(a[i]));
        }
        return s;
    }

    /**
     * Test already tx instance in cache.
     *
     * @throws Exception
     */
    public void testTxInstanceInCacheValidateTrue() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("p", p.getName());
        Assert.assertTrue(Utils.isPClean(p));
        Assert.assertTrue(JDOHelper.isTransactional(p));
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertTrue(p2 == p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test non tx instance in cache than does exist. This is done with a dataStore tx and
     * hence instance must be persistent clean.
     *
     * @throws Exception
     */
    public void testNonTxInstanceInCacheValidateTrue() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(Utils.isHollow(p));
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertTrue(p2 == p);
        Assert.assertTrue(Utils.isPClean(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test non tx instance in cache than does exist. This is done with a optimistic tx and
     * hence instance must be persistent clean.
     *
     * @throws Exception
     */
    public void testNonTxInstanceInCacheValidateTrueOptimistic()
            throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Assert.assertTrue(Utils.isHollow(p));
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertTrue(p2 == p);
        Assert.assertTrue(Utils.isPNonTx(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGetNonAppIdObjectIdClass() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        Person p = new Person("p");
        p.setVal("val1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        Class c = pm.getObjectIdClass(Person.class);
        Constructor constr = c.getConstructor(new Class[]{String.class});
        String str = pm.getObjectId(p).toString();
        System.out.println("*** str = '" + str + "'");
        Object id = constr.newInstance(new Object[]{str});
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertTrue(p2 == p);
        pm.close();
    }

    public void testGetNonAppIdObjectIdClassInHier() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        ClassA p = new ClassA();
        p.setStringA("val1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        Class c = pm.getObjectIdClass(ClassA.class);
        Constructor constr = c.getConstructor(new Class[]{String.class});
        Object id = constr.newInstance(
                new Object[]{pm.getObjectId(p).toString()});
        ClassA p2 = (ClassA)pm.getObjectById(id, true);
        Assert.assertTrue(p2 == p);
        pm.close();
    }

    public void testGetObjectByIDString() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        Person p = new Person("p");
        p.setVal("val1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        String idString = pm.getObjectId(p).toString();
        System.out.println("idString = " + idString);
        Person p2 = (Person)((VersantPersistenceManager)pm).getObjectByIDString(
                idString, true);
        Assert.assertTrue(p2 == p);
        pm.close();
    }

    public void testGetObjectByIdFromPreCommit() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p3");
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Person p2 = (Person)pm.getObjectById(id, true);
        Assert.assertTrue(p2 == p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testGetObjectByIdStringFromPreCommit() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p3");
        pm.makePersistent(p);
        Object id = pm.getObjectId(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        String idString = id.toString();
        try {
            ((PMProxy)pm).getRealPM().getObjectByIDString(idString, true);
        } catch (JDOUserException e) {
            //expected
        }
        pm.close();
    }

    /**
     * Check that the OID for an instance of a subclass obtained prior to
     * commit returns the correct instance for getObjectById(oid) after the
     * commit.
     */
    public void testGetObjectByIdFromPreCommitSubClass() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Employee emp = new Employee("testGetObjectByIdFromPreCommitSubClass",
                "123");
        pm.makePersistent(emp);
        Object oid = pm.getObjectId(emp);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Employee emp2 = (Employee)pm.getObjectById(oid, true);
        Assert.assertTrue(emp == emp2);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Check that getObjectId(pc) works (i.e. does not return null) when pc
     * is an instance from a navigated list on another instance [118].
     */
    public void testGetObjectByIdForListElement() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Employee emp = new Employee("testGetObjectByIdForListElement", "123");
        int n = 3;
        for (int i = 0; i < n; i++) {
            emp.getPersons().add(new Person("dork" + i));
        }
        pm.makePersistent(emp);
        Object oid = pm.getObjectId(emp);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        emp = (Employee)pm.getObjectById(oid, true);
        for (int i = 0; i < n; i++) {
            Person p = (Person)emp.getPersons().get(i);
            Assert.assertNotNull(pm.getObjectId(p));
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that newObjectIdInstance works with a null class for datastore
     * identity.
     */
    public void testNewObjectIdInstanceNullClass() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a1 = new Address("somewhere");
        pm.makePersistent(a1);
        String oidStr = pm.getObjectId(a1).toString();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object oid = pm.newObjectIdInstance(null, oidStr);
        Address a2 = (Address)pm.getObjectById(oid, true);
        Assert.assertTrue(a1 == a2);
        pm.deletePersistent(a1);
        pm.currentTransaction().commit();

        pm.close();
    }

}

