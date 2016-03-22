
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
package com.versant.core.ejb.junit.ejbtest0;

import com.versant.core.ejb.junit.ejbtest0.model.*;
import com.versant.core.ejb.junit.VersantEjbTestCase;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 */
public class TestGeneral extends VersantEjbTestCase {
    public TestGeneral(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
                "testPersist",
                "testPersistWithRef",
                "testPersist2",
                "testMerge",
                "testMerge2",
                "testPersistCascade",
                "testPersistCascade2",
                "testMergeCascade",
                "testMergeCascade2",
                "testFlush",
                "testCompId1",
                "testCompId2",
                "testCompId3",
                "testQuery",
                "testQuery2",
                "testNoChange",
                "testNoChange2",
                "testReachable",
                "testMergeWithConflict",    // currently failing
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestGeneral(a[i]));
        }
        return s;
    }

    /**
     * Test insert with no keygen.
     */
    public void testPersist() {
        EntityManager em = emf().getEntityManager();

        Employee emp = new Employee();
        emp.setEmpNo(55);
        em.getTransaction().begin();
        em.persist(emp);
        em.getTransaction().commit();

        em.getTransaction().begin();
        Employee w = em.find(Employee.class, emp.getEmpNo());
        w.setSal(444);
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.remove(em.find(Employee.class, emp.getEmpNo()));
        em.getTransaction().commit();
    }

    /**
     * Test insert with no keygen, and add a address.
     */
    public void testPersistWithRef() {
        EntityManager em = emf().getEntityManager();
        Employee emp = new Employee();

        Address address = new Address();
        address.setStreet("wargrave");
        emp.setCurrentAddress(address);

        emp.setAddress(new ArrayList());
        emp.getAddress().add(address);

        int idVal = (int) System.currentTimeMillis();
        emp.setEmpNo(idVal);
        em.getTransaction().begin();

        System.out.println("\n\n\n\n\nBefore persist");
        em.persist(emp);
        em.getTransaction().commit();

        em.getTransaction().begin();
        Employee w = em.find(Employee.class, emp.getEmpNo());
        System.out.println("\n\n\n\nBEFORE ADDRESS NAV");
        Address ad2 = (Address) w.getAddress().get(0);
        assertEquals("wargrave", ad2.getStreet());
        w.setSal(444);
        em.getTransaction().commit();

        em.getTransaction().begin();
        em.remove(em.find(Employee.class, emp.getEmpNo()));
        em.getTransaction().commit();
    }

    /**
     * Test insert with HIGHLOW keygen.
     */
    public void testPersist2() {
        EntityManager em =  emf().getEntityManager();

        EmployeeWithKeyGen emp = new EmployeeWithKeyGen();
        em.getTransaction().begin();
        em.persist(emp);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();

        em.getTransaction().begin();
        EmployeeWithKeyGen w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());
        w.setSal(444);
        em.getTransaction().commit();

        assertEquals(444.0, w.getSal());

        em.getTransaction().begin();
        w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());
        em.remove(w);
        em.getTransaction().commit();

        em.getTransaction().begin();
        try {
            w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());
            fail("Expected finder exception");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        em.getTransaction().commit();
    }

    public void testMergeWithConflict() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithKeyGen emp = new EmployeeWithKeyGen();
        em.getTransaction().begin();
        em.persist(emp);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();

        em.getTransaction().begin();
        EmployeeWithKeyGen w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());
        w.setSal(444);
        em.getTransaction().commit();

        assertEquals(444.0, w.getSal());

        em.getTransaction().begin();
        w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());
        try {
            em.merge(emp);
            fail("Expected concurrect update");
        } catch (Exception e) {
            //expected
            e.printStackTrace(System.out);
        }
        em.getTransaction().commit();
    }

    public void testMerge() {
        EntityManager em =  emf().getEntityManager();

        EmployeeWithKeyGen emp = new EmployeeWithKeyGen();
        em.getTransaction().begin();
        em.persist(emp);
        emp.setSal(333.0);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();

        emp.setSal(444.0);

        em.getTransaction().begin();
        EmployeeWithKeyGen w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());
        em.merge(emp);
        em.getTransaction().commit();

        assertEquals(444.0, w.getSal());

        em.getTransaction().begin();
        w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());

        em.getTransaction().commit();
    }

    public void testMerge2() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithKeyGen emp = new EmployeeWithKeyGen();
        em.getTransaction().begin();
        em.persist(emp);
        emp.setSal(333.0);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();

        emp.setSal(444.0);

        em.getTransaction().begin();
        EmployeeWithKeyGen w = em.merge(emp);
        em.getTransaction().commit();

        assertEquals(444.0, w.getSal());

        em.getTransaction().begin();
        w = em.find(EmployeeWithKeyGen.class, emp.getEmpNo());

        em.getTransaction().commit();
    }

    public void testPersistCascade() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithReference emp = new EmployeeWithReference();
        em.getTransaction().begin();
        em.persist(emp);
        emp.setSal(333.0);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();



        em.getTransaction().begin();
        EmployeeWithReference w = em.find(EmployeeWithReference.class, emp.getEmpNo());

        Address a = new Address();
        a.setStreet("s");
        w.setAddress(a);

        em.persist(w);
        em.getTransaction().commit();

        assertEquals("s", w.getAddress().getStreet());

        em.getTransaction().begin();
        w = em.find(EmployeeWithReference.class, emp.getEmpNo());
        w.getAddress().setStreet("ss");
        em.getTransaction().commit();

        assertEquals("ss", w.getAddress().getStreet());

        em.getTransaction().begin();
        w = em.find(EmployeeWithReference.class, emp.getEmpNo());
        em.remove(w);
        em.getTransaction().commit();
    }

    public void testPersistCascade2() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithReference emp = new EmployeeWithReference();
        em.getTransaction().begin();
        em.persist(emp);
        emp.setSal(333.0);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();

        Address a = new Address();
        a.setStreet("s");
        emp.setAddress(a);

        em.getTransaction().begin();
        EmployeeWithReference w = em.merge(emp);
        em.getTransaction().commit();

        assertEquals("s", w.getAddress().getStreet());

        em.getTransaction().begin();
        w = em.find(EmployeeWithReference.class, emp.getEmpNo());
        em.remove(w);
        em.getTransaction().commit();
    }

    public void testMergeCascade() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithReference emp = new EmployeeWithReference();
        em.getTransaction().begin();
        em.persist(emp);
        emp.setSal(333.0);
        System.out.println("emp.getEmpNo() = " + emp.getEmpNo());
        em.getTransaction().commit();

        Address a = new Address();
        a.setStreet("s");
        emp.setAddress(a);
        EmployeeWithReference emp2 = new EmployeeWithReference();
        emp2.setEname("emp2");
        emp.setRef(emp2);
        emp2.setRef(emp);

        em.getTransaction().begin();
        EmployeeWithReference w = em.merge(emp);

        assertEquals(w, w.getRef().getRef());
        em.getTransaction().commit();

        assertEquals("s", w.getAddress().getStreet());

        em.getTransaction().begin();
        w = em.find(EmployeeWithReference.class, emp.getEmpNo());
        em.remove(w);
        em.getTransaction().commit();
    }

    public void testMergeCascade2() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithReference emp1 = new EmployeeWithReference();
        emp1.setEname("emp1");
        EmployeeWithReference emp2 = new EmployeeWithReference();
        emp2.setEname("emp2");

        em.getTransaction().begin();
        em.persist(emp1);
        em.persist(emp2);
        em.getTransaction().commit();

        Address a = new Address();
        a.setStreet("s");
        emp1.setRef(emp2);
        emp2.setRef(emp1);

        em.getTransaction().begin();
        EmployeeWithReference w = em.merge(emp1);

        assertEquals(w, w.getRef().getRef());
        em.getTransaction().commit();

        em.getTransaction().begin();
        w = em.find(EmployeeWithReference.class, emp1.getEmpNo());
        em.remove(w);
        em.getTransaction().commit();
    }

    public void testFlush() {
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Address address = new Address();
        address.setStreet("street");
        em.persist(address);
        em.flush();

        address.setStreet("street2");
        em.getTransaction().commit();
        assertEquals("street2", address.getStreet());
    }

    public void testCompId1() {
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        ClassWithCompPK cpk = new ClassWithCompPK();
        String idVal = "" + System.identityHashCode(cpk);
        cpk.setId1(idVal + "_1");
        cpk.setId2(idVal + "_2");
        em.persist(cpk);
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    public void testCompId2() {
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        ClassWithCompPK cpk = new ClassWithCompPK();
        String idVal = "" + System.currentTimeMillis();
        cpk.setId1(idVal + "_1");
        cpk.setId2(idVal + "_2");
        cpk.setVal(idVal);
        em.persist(cpk);
        em.flush();
        em.getTransaction().commit();

        ClassWithCompPK.PK pk = new ClassWithCompPK.PK(idVal + "_1-" + idVal + "_2");
        ClassWithCompPK cpk2 = em.find(ClassWithCompPK.class, pk);
        assertEquals(idVal, cpk2.getVal());

        em.close();
    }

    public void testCompId3() {
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        ClassWithCompPK cpk = new ClassWithCompPK();
        String idVal = "" + System.currentTimeMillis();
        cpk.setId1(idVal + "_1");
        cpk.setId2(idVal + "_2");
        cpk.setVal(idVal);
        em.persist(cpk);
        em.flush();
        em.getTransaction().commit();

        em.getTransaction().begin();
        ClassWithCompPK.PK pk = new ClassWithCompPK.PK(idVal + "_1-" + idVal + "_2");
        ClassWithCompPK cpk2 = em.find(ClassWithCompPK.class, pk);
        em.getTransaction().commit();

        em.close();
    }

    public void testQuery() {
        EntityManager em = emf().getEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("SELECT o FROM Address o");
        List result = q.getResultList();
        System.out.println("result.size() = " + result.size());
        System.out.println("bla");
        System.out.println("result = " + result);
        em.getTransaction().commit();
    }

    public void testQuery2() {
        EntityManager em = emf().getEntityManager();

        em.getTransaction().begin();
        for (int i = 0; i < 20; i++) {
            Address address = new Address();
            address.setStreet("bla");
            em.persist(address);
        }
        em.getTransaction().commit();

        em.getTransaction().begin();
        Query q = em.createQuery("SELECT o FROM Address o where o.street = ?1");
        q.setHint("fetchSize", 3);
        q.setParameter(0, "bla");
        List result = q.getResultList();
        int count = 0;
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            count++;
            Address o = (Address) iterator.next();
            System.out.println("o = " + o);
        }
//        assertEquals(20, count);
        System.out.println("result = " + result);
        em.getTransaction().commit();
    }

    public void testNoChange() {
        EntityManager em = emf().getEntityManager();


        em.getTransaction().begin();
        EmployeeWithReference emp = new EmployeeWithReference();
        EmployeeWithReference emp2 = new EmployeeWithReference();
        em.persist(emp);
        em.persist(emp2);
        emp.setSal(333.0);
        emp.setRef(emp2);
        em.getTransaction().commit();

        int emp1No = emp.getEmpNo();

        em.getTransaction().begin();
        EmployeeWithReference newEmp1 = em.find(EmployeeWithReference.class, emp1No);
//        System.out.println(newEmp1);
        em.getTransaction().commit();

    }


    public void testNoChange2() {
        EntityManager em = emf().getEntityManager();


        em.getTransaction().begin();
        EmployeeWithReference emp = new EmployeeWithReference();
        EmployeeWithReference emp2 = new EmployeeWithReference();
        EmployeeWithReference man = new EmployeeWithReference();
        EmployeeWithReference man2 = new EmployeeWithReference();
        emp.setRef(man);
        emp2.setRef(man2);
        emp.setSal(333.0);
        emp2.setSal(333.0);

        em.persist(emp);
        em.persist(emp2);
        em.getTransaction().commit();


        int emp1No = emp.getEmpNo();
        int emp2No = emp2.getEmpNo();



        em.getTransaction().begin();
        // When there is one em.find() then it works
        EmployeeWithReference manager = em.find(EmployeeWithReference.class, emp1No);
        EmployeeWithReference employee = em.find(EmployeeWithReference.class, emp2No);
        em.getTransaction().commit();
    }


    public void testReachable() {
        EntityManager em = emf().getEntityManager();

        EmployeeWithReference emp = new EmployeeWithReference();
        EmployeeWithReference emp2 = new EmployeeWithReference();
        em.getTransaction().begin();
        em.persist(emp);
        emp.setSal(333.0);
        emp.setRef(emp2);
        em.getTransaction().commit();
    }



}
