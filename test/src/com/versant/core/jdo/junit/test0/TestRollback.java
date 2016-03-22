
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
import com.versant.core.jdo.junit.test0.model.Address;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.test0.model.NonMutableJavaTypes;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.Date;

/**
 * Assorted rollback tests.
 */
public class TestRollback extends VersantTestCase {

    public TestRollback(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("Rollback");
        String[] a = new String[]{
            "testRollback1",
            "testRollbackPNew",
            "testRollbackPDirty",
            "testRollbackPDirty2",
            "testRollbackPDirty3",
            "testRollbackPDirty2_1",
            "testRollbackPDirty2Int",
            "testRollbackPDirty2Int_1",
            "testRollbackPDirty2Integer",
            "testRollbackPDeleted",
            "testRollbackPNewDeleted",
            "testBugNullFieldsAfterRollback",
            "testRollbackPClean",
            "testRollbackPClean1",
            "testRollbackSCO",
            "testRollbackConcurrent",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestRollback(a[i]));
        }
        return s;
    }

    public void testRollback1() {
        NonMutableJavaTypes pc = new NonMutableJavaTypes();
        pc.setIntValue(1);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.makeTransactional(pc);

        pc.setIntValue(2);
        pm.currentTransaction().commit();

        System.out.println("pc.getIntField() "  + pc.getIntValue());

        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().begin();
        pc.setIntValue(3);
        pm.currentTransaction().rollback();

        System.out.println("pc.getIntField() "  + pc.getIntValue());
        System.out.println("expected :  "+ 2);
        Assert.assertEquals(2, pc.getIntValue());
        pm.close();
    }

    /**
     * Rolling back a pclean instance with restoreValues true must return a
     * p-nontx instance.
     */
    public void testRollbackPClean() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address("street");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().begin();
        Assert.assertEquals("street", a.getStreet());
        Utils.isPClean(a);
        pm.currentTransaction().rollback();
        Utils.isPNonTx(a);
        pm.close();
    }

    public void testRollbackPClean2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address("street");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().begin();
        Assert.assertEquals("street", a.getStreet());
        Utils.isPClean(a);
        pm.currentTransaction().rollback();
        Utils.isPNonTx(a);
        pm.close();
    }

    /**
     * Rolling back a pclean instance with restoreValues='false' must return a
     * hollow instance.
     */
    public void testRollbackPClean1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address("street");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().begin();
        Assert.assertEquals("street", a.getStreet());
        Utils.isPClean(a);
        pm.currentTransaction().rollback();
        Utils.isHollow(a);
        pm.close();
    }

    /**
     * Test an rollback for a non mutable field
     */
    public void testRollbackPNew() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int sets = 2;
        int tests = 4;
        boolean[] bools = new boolean[]{
            false, true,
            false, false,
            true, true,
            true, false,
        };

        for (int i = 0; i < sets * tests; i = i + sets) {
            pm.currentTransaction().setOptimistic(bools[i]);
            pm.currentTransaction().setRestoreValues(bools[i + 1]);
            pm.currentTransaction().setRetainValues(bools[i + 1]);

            long now = TIME1;
            long then = TIME1 - 100;

            pm.currentTransaction().begin();
            Person friend = new Person("friend");

            Person p = new Person("name");
            p.setBirthDate(new Date(now));
            p.addPersonToList(friend);
            p.addPersonToSet(friend);
            pm.makePersistent(p);
            Utils.assertEquals(1, p.getPersonsList().size()); // extra
            Utils.isPNew(p);
            p.setName("name2");
            p.getBirthDate().setTime(then);
            p.addPersonToList(new Person("bla"));
            Utils.assertEquals(2, p.getPersonsList().size()); // extra
            p.addPersonToSet(new Person("bla"));
            pm.currentTransaction().rollback();

            if (pm.currentTransaction().getRestoreValues()) {
                Utils.assertEquals("name", p.getName());
                Utils.assertEquals(now, p.getBirthDate().getTime());
                Utils.assertEquals(1, p.getPersonsList().size());
            } else {
                Utils.assertEquals("name2", p.getName());
                Utils.assertEquals(then, p.getBirthDate().getTime());
                Utils.assertEquals(2, p.getPersonsList().size());
            }
        }
        pm.close();
    }

    public void testRollbackPDirty() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().begin();
        Person p2 = (Person)pm.getObjectById(id, false);
        Utils.assertEquals("name1", p2.getName());
        p2.setName("nameChanged");
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isPNonTx(p2));

        pm.currentTransaction().begin();
        Utils.assertEquals("name1", p2.getName());
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testRollbackPDirty3() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);

        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        Person p2 = (Person)pm.getObjectById(id, false);
        Utils.assertEquals("name1", p2.getName());
        p2.setName("nameChanged");
        countExecQueryEvents();
        pm.currentTransaction().rollback();
        Assert.assertEquals(0, countExecQueryEvents());
        pmf().evictAll();
        Utils.assertTrue(Utils.isPNonTx(p2));
        Assert.assertEquals(0, countExecQueryEvents());
        Utils.assertEquals("name1", p2.getName());

        pm.currentTransaction().begin();
        Utils.assertEquals("name1", p2.getName());
        Assert.assertEquals(0, countExecQueryEvents());
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testRollbackConcurrent() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Person p = new Person("concur");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        PersistenceManager pm2 = pmf().getPersistenceManager();
        pm2.currentTransaction().setOptimistic(true);
        pm2.currentTransaction().setRestoreValues(false);
        pm2.currentTransaction().begin();
        Person p2 = (Person)pm2.getObjectById(id, false);
        Assert.assertEquals("concur", p2.getName());
        p2.setName("updated2");
//        pm2.currentTransaction().commit();


        PersistenceManager pm3 = pmf().getPersistenceManager();
        pm3.currentTransaction().setOptimistic(true);
        pm3.currentTransaction().setRestoreValues(false);
        pm3.currentTransaction().begin();
        Person p3 = (Person)pm3.getObjectById(id, false);
        Assert.assertEquals("concur", p3.getName());
        p3.setName("updated3");

        pm2.currentTransaction().commit();
        pm3.currentTransaction().rollback();

        pm2.currentTransaction().begin();
        Assert.assertEquals("updated2", p2.getName());

        pm3.currentTransaction().begin();
        Assert.assertEquals("updated2", p3.getName());

        pm2.close();
        pm3.close();
    }

    /**
     * This is to test a optimitic tx with a pdirty instance and restoreValues=true
     * At rollback the instance must change to pNonTx and non-mutable fields must be
     * restored to their values as of the begining of the tx.
     */
    public void testRollbackPDirty2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        Person p2 = (Person)pm.getObjectById(id, false);
        p2.setName("nameChanged");
        p2.setName("nameChangedAgain");
        p2.setName("nameChangedAgain1");
        p2.setName("nameChangedFinal");
        pm.currentTransaction().begin();
        p2.setName("nameChanged2");
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isPNonTx(p2));
        Assert.assertEquals("nameChangedFinal", p2.getName());
        pm.close();
    }

    public void testRollbackPDirty2_1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        Person p2 = (Person)pm.getObjectById(id, false);
        p2.setName("nameChanged");
        p2.setName("nameChangedAgain");
        p2.setName("nameChangedAgain1");
        p2.setName("nameChangedFinal");
        pm.currentTransaction().begin();
        p2.setName("nameChanged2");
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isHollow(p2));
        Assert.assertEquals("name1", p2.getName());
        pm.close();
    }

    public void testRollbackPDirty2Int() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);
        Person p2 = (Person)pm.getObjectById(id, false);
        p2.setIntField(1);
        p2.setIntField(2);
        p2.setIntField(3);
        pm.currentTransaction().begin();
        p2.setIntField(4);
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isPNonTx(p2));
        Assert.assertEquals(3, p2.getIntField());
        pm.close();
    }

    public void testRollbackPDirty2Int_1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);
        Person p2 = (Person)pm.getObjectById(id, false);
        p2.setIntField(1);
        p2.setIntField(2);
        p2.setIntField(3);
        pm.currentTransaction().begin();
        p2.setIntField(4);
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isHollow(p2));
        Assert.assertEquals(0, p2.getIntField());
        pm.close();
    }

    public void testRollbackPDirty2Integer() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        p.setIntegerField(new Integer(44));
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(p);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRestoreValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setNontransactionalWrite(true);
        pm.currentTransaction().setNontransactionalRead(true);
        Person p2 = (Person)pm.getObjectById(id, false);
        p2.setIntegerField(new Integer(1));
        p2.setIntegerField(new Integer(2));
        p2.setIntegerField(new Integer(3));

        pm.currentTransaction().begin();
        p2.setIntegerField(new Integer(4));
        pm.currentTransaction().rollback();

        Utils.assertTrue(Utils.isPNonTx(p2));
        System.out.println("%%% Assert.assertEquals(new Integer(44), p2.getIntegerField())");
        Assert.assertEquals(new Integer(44), p2.getIntegerField());

        pm.close();
    }

    public void testRollbackPDeleted() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p2 = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
        p2.setName("nameChanged");
        pm.deletePersistent(p2);
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isHollow(p2));
        pm.close();

        /**
         * retainValues to true
         */
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        p2 = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
        p2.setName("nameChanged");
        pm.deletePersistent(p2);
        pm.currentTransaction().rollback();
        Utils.assertTrue(Utils.isHollow(p2));
        pm.close();
    }

    public void testRollbackPNewDeleted() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().rollback();
        Utils.assertTrue(JDOHelper.getPersistenceManager(p) == null);
        pm.close();
    }

    public void testRollback() throws Exception {
        if (true) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setNontransactionalWrite(true);

        pm.currentTransaction().begin();
        Address addressCopy = new Address();
        addressCopy.setStreet("street1");

        Address address = new Address();
        address.setStreet("street1");

        Person person = new Person("name1");
        Date bDay = new Date(TIME1);
        Date bDayCopy = new Date(bDay.getTime());
        person.setBirthDate(bDay);
        person.setAddress(address);
        pm.makePersistent(person);
        address.setStreet("street2");
        person.setName("name2");
        person.getBirthDate().setTime(TIME2);
//        Date bDay2 = new Date();
//        person.setBirthDate(bDay2);
        Utils.isPNew(person);
        pm.currentTransaction().rollback();

        Utils.assertEquals("name1", person.getName());
        Utils.assertEquals(bDayCopy.getTime(), person.getBirthDate().getTime());
        Utils.assertEquals(bDayCopy, person.getBirthDate());

        Utils.assertEquals(addressCopy.getStreet(),
                person.getAddress().getStreet());

        pm.close();
    }

    public void testRollbacks1() throws Exception {
//        pmf = getPMF();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setNontransactionalWrite(true);

        pm.currentTransaction().begin();
        Person p = new Person("name1");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.setName("name2");
        pm.currentTransaction().rollback();

        Utils.assertEquals("name1", p.getName());
    }

//    public PersistenceManagerFactory getPMF() throws Exception {
//        if (pmf == null) {
//            JDOServerImp jdoServerImp = new JDOServerImp(System.getProperty("JDO.CONFIG.XML"),false);
////        jdoMetaData = jdoServerImp.getJdoMetaData();
//
//            pmf = jdoServerImp.getLocalPMF();
//        }
//        return pmf;
//    }
//
//    public PersistenceManagerFactory getNewPMF() throws Exception {
//        JDOServerImp jdoServerImp = new JDOServerImp(System.getProperty("JDO.CONFIG.XML"),false);
////        jdoMetaData = jdoServerImp.getJdoMetaData();
//
//        pmf = jdoServerImp.getLocalPMF();
//        return pmf;
//    }


    /**
     * Bug: Start tx, create instance, commit, get new pm, lookup instance with
     * query, modify field without read, rollback tx, Start new tx,
     * lookup instance by OID, field is null instead of old value.
     */
    public void testBugNullFieldsAfterRollback() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);

        System.out.println("\n*** create Person, commit, rollback");
        String name = "testBugNullFieldsAfterRollback";
        Person p = new Person(name);
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();

        pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);

        System.out.println("\n*** read person with a query, change, rollback");
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "name == n");
        q.declareParameters("String n");
        Collection ans = (Collection)q.execute(name);
        p = (Person)ans.iterator().next();
        q.closeAll();
        // the field must be written to without being read to get the bug
        p.setName("piggy");
        String id = pm.getObjectId(p).toString();
        pm.currentTransaction().rollback();

        System.out.println("\n*** check name is unchanged");
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectByIDString(id, true);
        Assert.assertEquals(name, p.getName());

        pm.close();
    }

    public void testRollbackSCO() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        pm.makePersistent(p);
        pm.currentTransaction().rollback();

        pm.currentTransaction().begin();
        pm.makePersistent(p);
        p.setName("bla");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

}
