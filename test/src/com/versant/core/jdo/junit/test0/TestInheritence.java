
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
import com.versant.core.jdo.junit.test0.model.horizontal.HorizSubA;
import com.versant.core.jdo.junit.test0.model.horizontal.HorizSubB;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.List;

/**
 * @keep-all
 */
public class TestInheritence extends VersantTestCase {

    public TestInheritence(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("Inheritence");
        String[] a = new String[]{
            "testMakePM",
            "testAddRefs",
            "testCollectionOnSubClass",
            "testDSAbstract1",
            "testDSAbstract2",
            "testAppIdAbstract2",
            "testUpdateSubFromBaseOID",
            "testHorizontalBaseRef",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestInheritence(a[i]));
        }
        return s;
    }
    
    /**
     * Test horizontal inheritance with a reference in the base class.
     * This reproduces the NPE in the circular reference check bug.
     * This bug does NOT show up on MySQL as we do not generate constraints
     * for MySQL.
     */
    public void testHorizontalBaseRef() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address addr = new Address("somewhere");
        HorizSubA subA = new HorizSubA(10, addr, "suba");
        HorizSubB subB = new HorizSubB(20, addr, "subb");
        pm.currentTransaction().commit();
        
        pm.close();
    }

    public void testMakePM() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ClassA classA = new ClassAB();
        pm.makePersistent(classA);
        classA.setStringA("stringA");
        ((ClassAB)classA).setStringAB("stringAB");
        ((ClassAB)classA).setStringAA("stringAA");
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ClassA classA2 = (ClassA)pm.getObjectById(
                JDOHelper.getObjectId(classA), true);
        Utils.assertEquals("stringA", classA2.getStringA());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testAddRefs() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ClassAB classAB = new ClassAB();
        ClassAB ref = new ClassAB();
        ref.setStringA("stringA");

        classAB.setClassAB(ref);
        pm.makePersistent(classAB);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("stringA", classAB.getClassAB().getStringA());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testCollectionOnSubClass() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ClassAB classAB = new ClassAB();
        ClassAB lstItem1 = new ClassAB();
        lstItem1.setStringAA("stringAA1");
        lstItem1.setStringAB("stringAB1");

        ClassAB lstItem2 = new ClassAB();
        lstItem2.setStringAA("stringAA2");
        lstItem2.setStringAB("stringAB2");
        classAB.getListAB().add(lstItem1);
        classAB.getListAB().add(lstItem2);
        pm.makePersistent(classAB);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("The list must contain 2 items", 2,
                classAB.getListAB().size());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDSAbstract1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Concrete1 concrete1 = new Concrete1();
        concrete1.setAbs1("abs1");
        concrete1.setAbs2("abs2");
        concrete1.setConc1("conc1");
        pm.makePersistent(concrete1);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(concrete1);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        concrete1 = (Concrete1)pm.getObjectById(id, true);
        Assert.assertEquals("abs1", concrete1.getAbs1());
        Assert.assertEquals("abs2", concrete1.getAbs2());
        Assert.assertEquals("conc1", concrete1.getConc1());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDSAbstract2() throws Exception {
        nuke(Concrete1.class);
        nuke(Concrete2.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Concrete1 concrete1 = new Concrete1();
        concrete1.setAbs1("abs11");
        concrete1.setAbs2("abs21");
        concrete1.setConc1("conc11");

        Concrete2 concrete2 = new Concrete2();
        concrete2.setAbs1("abs12");
        concrete2.setAbs2("abs22");
        concrete2.setConc2("conc22");
        pm.makePersistent(concrete2);
        pm.makePersistent(concrete1);
        pm.currentTransaction().commit();
        Object id1 = pm.getObjectId(concrete1);
        Object id2 = pm.getObjectId(concrete2);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(pm.getExtent(Concrete1.class, false));
        List l = (List)q.execute();
        Assert.assertEquals(1, l.size());
        concrete1 = (Concrete1)l.get(0);
        Assert.assertEquals("abs11", concrete1.getAbs1());
        Assert.assertEquals("abs21", concrete1.getAbs2());
        Assert.assertEquals("conc11", concrete1.getConc1());

        pm.currentTransaction().commit();
        pm.close();

    }

    public void testAppIdAbstract2() throws Exception {
        if (!isApplicationIdentitySupported()) return;
        nuke(AppIDConcrete1.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        AppIDConcrete1 appIDConcrete1 = new AppIDConcrete1();
        appIDConcrete1.setAppIdConc1("appIdConc1");
        appIDConcrete1.setAppIdAbs1("appIdAbs1");
        appIDConcrete1.setAppIdConcKey(2);
        pm.makePersistent(appIDConcrete1);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test looking up an instance of a subclass using an OID constructed
     * using the base class and modifying the instance.
     */
    public void testUpdateSubFromBaseOID() throws Exception {

        // create an Employee (extends EmpSuper)
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        Employee emp = new Employee("rabbit", "stew");
        pm.makePersistent(emp);
        String oids = pm.getObjectId(emp).toString();
        pm.currentTransaction().commit();
        pm.close();

        // lookup and check it is ok outside of tran
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        Object oid = pm.newObjectIdInstance(EmpSuper.class, oids);
        EmpSuper es = (EmpSuper)pm.getObjectById(oid, true);
        Assert.assertEquals(es.getName(), "rabbit");
        if (pm.currentTransaction().isActive()) {
            pm.currentTransaction().rollback();
        }
        pm.close();

        // lookup emp by OID constructed from EmpSuper
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        oid = pm.newObjectIdInstance(EmpSuper.class, oids);
        es = (EmpSuper)pm.getObjectById(oid, true);
        es.setName("roger");
        pm.currentTransaction().commit();
        pm.close();

        // get it again and make sure the name is correct
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().begin();
        oid = pm.newObjectIdInstance(EmpSuper.class, oids);
        es = (EmpSuper)pm.getObjectById(oid, true);
        Assert.assertEquals(es.getName(), "roger");
        pm.currentTransaction().commit();
        pm.close();
    }

}
