
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
package com.versant.core.jdo.junit.test2;

import junit.framework.Assert;

import com.versant.core.metric.BaseMetric;
import com.versant.core.metric.MetricSnapshotPacket;
import com.versant.core.common.Utils;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test2.model.*;
import com.versant.core.jdo.junit.test2.model.Person;
import com.versant.core.jdo.junit.test2.model.Address;
import com.versant.core.jdo.junit.test2.model.Country;
import com.versant.core.jdo.junit.test2.model.dfgRefs.A;
import com.versant.core.jdo.junit.test2.model.dfgRefs.B;
import com.versant.core.jdo.junit.test2.model.dfgRefs.C;
import com.versant.core.jdo.junit.test2.model.dfgRefs.D;
import com.versant.core.jdo.junit.test2.model.knowhow.Resource;
import com.versant.core.jdo.junit.test2.model.knowhow.ResourceData;
import com.versant.core.jdo.junit.test2.model.knowhow.ResourceLog;
import com.versant.core.jdo.junit.test2.model.knowhow.User;
import com.versant.core.jdo.junit.test2.model.refInt.Reference;
import com.versant.core.jdo.junit.test2.model.refInt.Referent;
import com.versant.core.jdo.junit.test2.model.tom.AbstractListable;
import com.versant.core.jdo.junit.test2.model.tom.AnotherListableElement;
import com.versant.core.jdo.junit.test2.model.tom.ListableElement;
import com.versant.core.jdo.junit.test2.model.tom.TomContainer;
import com.versant.core.jdo.*;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.logging.LogEvent;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdbc.VersantClientJDBCConnection;

import javax.jdo.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.util.*;

/**
 * General tests.
 */
public class TestGeneral2 extends VersantTestCase {

    private int p1AppId = 1;

    public void testDirectSQL() {
        if (!isSQLSupported())
        	return;

    	PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL", "select max(address_id) from address");
        Collection result = (Collection) q.execute();
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            for (int i = 0; i < row.length; i++) {
                Object col = row[i];
                System.out.println("col = " + col);
            }
        }
        pm.close();
    }

    public void testDirectSQL1() {
        if (!isSQLSupported())
        	return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL", "select address_id, city from address");
        q.setClass(Address.class);
        List result = (List)q.execute();
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            Address dup = (Address)it.next();
            System.out.println("dup = " + dup);
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDirectSQL11() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL", "select address_id, city from address");
        ((VersantQuery)q).setCacheable(true);
        ((VersantQuery)q).setEvictionClasses(new int[3]);
        q.setClass(Address.class);

        List result = (List)q.execute();
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            Address dup = (Address)it.next();
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        result = (List)q.execute();
        System.out.println("result.size() = " + result.size());
        System.out.println("result = " + result);
        pm.close();
    }

    public void testDirectSQL111() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL", "select address_id, city from address");
        ((VersantQuery)q).setCacheable(true);
        ((VersantQuery)q).setEvictionClasses(new int[3]);

        List result = (List)q.execute();
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            System.out.println("it.next() = " + it.next());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDirectSQL1111() {
        if (!isSQLSupported())
        	return;

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(
                    new Person("street", new Country("c" + i, "name")));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select person_id, country_code, age from person");
        q.setClass(Person.class);
        ((VersantQuery)q).setCacheable(true);

        List result = (List)q.execute();
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            System.out.println("it.next() = " + it.next());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDirectSQL1112() {
        if (!isSQLSupported())
        	return;

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        SimpleColHolder sch = new SimpleColHolder();
        for (int i = 0; i < 3; i++) {
            sch.getDfgList().add(new SimpleColEntry(i));
        }
        pm.makePersistent(sch);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select smplColHldrId, jdo_version from simple_col_holder");
        q.setClass(SimpleColHolder.class);
        ((VersantQuery)q).setCacheable(true);

        List result = (List)q.execute();
        assertTrue("Must be at least 10", result.size() >= 1);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            System.out.println("it.next() = " + it.next());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testNamedSQL1() {
        if (!isSQLSupported())
        	return;

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.versantNewNamedQuery(Address.class, "testNamedSQL1");
        List result = (List)q.execute();
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            Address dup = (Address)it.next();
            System.out.println("dup = " + dup);
        }

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testDirectSQL2() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select address_id, city from address where street = ?");
        q.setClass(Address.class);
        q.declareParameters("Varchar p");
        List result = (List)q.execute("street");
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            Address dup = (Address)it.next();
            System.out.println("dup = " + dup);
        }
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testNamedSQL2() {
        if (!isSQLSupported())
            return;

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        for (int i = 0; i < 4; i++) {
            pm.makePersistent(new Address("testNamedSQL2"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.versantNewNamedQuery(Address.class, "testNamedSQL2");
        List result = (List)q.execute("testNamedSQL2");
        Iterator it = result.iterator();
        while (it.hasNext()) {
            Address dup = (Address)it.next();
            System.out.println("dup = " + dup);
        }
        assertEquals(4, result.size());

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testNamedSQL3() {
        if (!isSQLSupported())
        	return;

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        for (int i = 0; i < 4; i++) {
            pm.makePersistent(new Address("testNamedSQL2"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.versantNewNamedQuery(Address.class, "testNamedSQL3");
        List result = (List)q.execute("street");
        Iterator it = result.iterator();
        while (it.hasNext()) {
            Address dup = (Address)it.next();
            System.out.println("dup = " + dup);
        }
        System.out.println("$$$ calling result.size()");
        assertEquals(5, result.size());

        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testDirectSQL3() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL", "select address_id, city, street " +
                "from address where street = ?");
        q.declareParameters("Varchar p");
        List result = (List)q.execute("street");
        assertTrue("Must be at least 10", result.size() >= 10);

        Iterator it = result.iterator();
        while (it.hasNext()) {
            Object[] o = (Object[])it.next();
            assertEquals(3, o.length);
            for (int i = 0; i < o.length; i++) {
                Object o1 = o[i];
                System.out.println("o1 = " + o1);
            }
        }
        pm.currentTransaction().rollback();
        pm.close();
    }

    /**
     * Must throw a userException if a 'Candidate' class was specified but could
     * not find a pk cols.
     */
    public void testDirectSQL4() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select city from address where street = ?");
        q.setClass(Address.class);
        q.declareParameters("Varchar p");
        List result = (List)q.execute("street");
        try {
            assertTrue("Must be at least 10", result.size() >= 10);
            fail("This test should fail because a 'Candidate' class was specified but there is" +
                    "not enough info in the rs to map it");
        } catch (JDOUserException e) {
            //expected
        }
        pm.close();
    }

    public void testDirectSQL5() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new Address("street"));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL", "   ");
        q.setClass(Address.class);
        q.declareParameters("Varchar p");

        try {
            List result = (List)q.execute("street");
            fail("This test should fail because a invalid filter was specified");
        } catch (JDOUserException e) {
            //expected
        }
        pm.close();
    }

    public void testDirectSQL6() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new BaseClassFlat("bc-name" + i));
        }
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new SubClassFlat("sc-name" + i, i));
        }
        for (int i = 0; i < 10; i++) {
            pm.makePersistent(new SubClass2Flat("sc2-name" + i, i));
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select base_class_flat_id, jdo_class, nme from base_class_flat");
        q.setClass(BaseClassFlat.class);
        List result = (List)q.execute();
        int countb = 0;
        int counts = 0;
        int counts2 = 0;
        for (int i = 0; i < result.size(); i++) {
            BaseClassFlat baseClassFlat = (BaseClassFlat)result.get(i);
            System.out.println(
                    "baseClassFlat.getName() = " + baseClassFlat.getName());
            if (baseClassFlat.getName().startsWith("bc-")) {
                assertTrue(baseClassFlat.getClass().getName().equals(
                        BaseClassFlat.class.getName()));
                countb++;
            } else if (baseClassFlat.getName().startsWith("sc2-")) {
                assertTrue(baseClassFlat.getClass().getName().equals(
                        SubClass2Flat.class.getName()));
                counts2++;
            } else if (baseClassFlat.getName().startsWith("sc-")) {
                assertTrue(baseClassFlat.getClass().getName().equals(
                        SubClassFlat.class.getName()));
                counts++;
            } else {
                fail();
            }
        }
        assertEquals(10, counts);
        assertEquals(10, countb);
        assertEquals(10, counts2);
        pm.close();
    }

    public void testDirectSql7() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            CompoundPkClass c = new CompoundPkClass(i, i, "name" + i);
            pm.makePersistent(c);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select pk_a, pk_b, nme from compound_pk_class");
        q.setClass(CompoundPkClass.class);
        List results = (List)q.execute();
        for (int i = 0; i < results.size(); i++) {
            CompoundPkClass compoundPkClass = (CompoundPkClass)results.get(i);
            System.out.println(
                    "compoundPkClass = " + compoundPkClass.getName());
        }
        pm.close();
    }

    public void testDirectSql8() {
        if (!isSQLSupported())
        	return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        for (int i = 0; i < 10; i++) {
            CompoundPkClass c = new CompoundPkClass(i, i, "name" + i);
            pm.makePersistent(c);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("SQL",
                "select pk_a, nme, pk_b, nme, jdo_version from compound_pk_class");
        q.setClass(CompoundPkClass.class);
        List results = (List)q.execute();
        for (int i = 0; i < results.size(); i++) {
            CompoundPkClass compoundPkClass = (CompoundPkClass)results.get(i);
            System.out.println(
                    "compoundPkClass = " + compoundPkClass.getName());
        }
        pm.close();
    }

    public void testStrinArray1() {
        String dn = getDbName();
        if (!isJdbc() || dn.equals("informixse")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ArraysModel am = new ArraysModel();
        am.setStringArray(new String[4]);
        pm.makePersistent(am);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        System.out.println("\n\n\n\n\n\n");
        String[] sa = am.getStringArray();
        Assert.assertNotNull(sa);
        Assert.assertEquals(4, sa.length);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        am.getStringArray()[0] = "string1";
        JDOHelper.makeDirty(am, "stringArray");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("string1", am.getStringArray()[0]);
        Assert.assertEquals(4, am.getStringArray().length);

        pm.deletePersistent(am);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testPCArray1() {
        String dn = getDbName();
        if (!isJdbc() || dn.equals("informixse")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ArraysModel am = new ArraysModel();
        am.setAddresses(new Address[10]);
        pm.makePersistent(am);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Address[] aa = am.getAddresses();
        Assert.assertNotNull(aa);
        Assert.assertEquals(10, aa.length);
        for (int i = 0; i < aa.length; i++) {
            Assert.assertNull(aa[i]);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        am.getAddresses()[0] = new Address("street");
        JDOHelper.makeDirty(am, "addresses");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("street", am.getAddresses()[0].getStreet());
        aa = am.getAddresses();
        Assert.assertEquals(10, aa.length);
        for (int i = 1; i < aa.length; i++) {
            Assert.assertNull(aa[i]);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        System.out.println("\n\n\n\n\n\n\n 222");
        am.getAddresses()[0] = null;
        JDOHelper.makeDirty(am, "addresses");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        aa = am.getAddresses();
        Assert.assertNotNull(aa);
        for (int i = 0; i < aa.length; i++) {
            Assert.assertNull(aa[i]);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        am.getAddresses()[5] = new Address("street5");
        am.getAddresses()[9] = new Address("street9");
        Assert.assertEquals(10, am.getAddresses().length);
        JDOHelper.makeDirty(am, "addresses");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("street5", am.getAddresses()[5].getStreet());
        Assert.assertEquals("street9", am.getAddresses()[9].getStreet());

        pm.deletePersistent(am);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testPCArrayQ1() {
        String dn = getDbName();
        if (!isJdbc() || dn.equals("informixse")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ArraysModel am = new ArraysModel();
        am.setAddresses(new Address[5]);
        am.getAddresses()[3] = new Address("street1");
        pm.makePersistent(am);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ArraysModel.class,
                "addresses.contains(addParam)");
        q.declareParameters("Address addParam");
        List result = (List)q.execute(am.getAddresses()[3]);
        Assert.assertEquals(1, result.size());

        pm.deletePersistent(am);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Non-managed tests
     */
    public void testPCFKArray() {
    	if (isVds())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ArraysModel am = new ArraysModel();

        ArrayModelFkArrayEntry[] amfk = new ArrayModelFkArrayEntry[20];
        am.setFkArray1(amfk);

        for (int i = 5; i < 15; i++) {
            amfk[i] = new ArrayModelFkArrayEntry(am, i);
            amfk[i].setVal("" + i);
        }
        pm.makePersistent(am);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(am);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        am = (ArraysModel)pm.getObjectById(id, true);
        amfk = am.getFkArray1();
        Assert.assertEquals(10, amfk.length);
        for (int i = 0; i < 10; i++) {
            Assert.assertNotNull(amfk[i]);
        }

        //this will not replace the instance in the db because the old
        //instance still has a fk ref and therefore will be picked up
        //on a select. So this is the same as a add.
        amfk[0].setParent(null);
        amfk[0] = new ArrayModelFkArrayEntry(am);
        amfk[0].setVal("00");
        JDOHelper.makeDirty(am, "fkArray1");
        System.out.println("\n\n\n\n\n\n before commit");
        pm.currentTransaction().commit();
        System.out.println("after commit\n\n\n\n\n\n\n");
        pm.close();


        //*******************************************************
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        am = (ArraysModel)pm.getObjectById(id, true);

        amfk = am.getFkArray1();
        Assert.assertEquals(10, amfk.length);
        Assert.assertEquals("00", amfk[0].getVal());

        for (int i = 0; i < 10; i++) {
            Assert.assertNotNull(amfk[i]);
        }
        amfk[9].setParent(null);
        amfk[9] = null;

        JDOHelper.makeDirty(am, "fkArray1");
        pm.currentTransaction().commit();
        pm.close();


        //+++++++++++++++++++++++++++++++++++++++++++++++++++
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        am = (ArraysModel)pm.getObjectById(id, true);

        amfk = am.getFkArray1();
        Assert.assertEquals("00", amfk[0].getVal());
        Assert.assertEquals(9, amfk.length);
        for (int i = 0; i < 9; i++) {
            Assert.assertNotNull(amfk[i]);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testRefInt1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Referent refT = new Referent();
        Reference reference = new Reference();
        refT.setRef(reference);
        pm.makePersistent(refT);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(reference);
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testEqualsId() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Address a = new Address("st", "city");
        pm.makePersistent(a);
        Object id1 = JDOHelper.getObjectId(a);
        pm.currentTransaction().commit();
        Object id2 = JDOHelper.getObjectId(a);
        Assert.assertEquals(id1, id2);

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFkColFetch1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        FkColHolder1 holder = new FkColHolder1();
        holder.setFkList(new ArrayList());
        FkColEntry1 entry1 = new FkColEntry1();
        entry1.setOwner(holder);
        holder.getFkList().add(entry1);
        pm.makePersistent(holder);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(holder);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        System.out.println("\n\n\n\n\n********************* load holder");
        holder = (FkColHolder1)pm.getObjectById(id, true);
        System.out.println("\n\n\n\n\n********************* load fk coll");
        holder.getFkList();

        pm.deletePersistentAll(holder.getFkList());
        pm.deletePersistent(holder);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testFkColFetch2() {
        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        FkColHolder1 holder = new FkColHolder1();
        holder.setVal(val);

        holder.setFkList(new ArrayList());
        FkColEntry1 entry1 = new FkColEntry1();
        entry1.setOwner(holder);
        holder.getFkList().add(entry1);
        pm.makePersistent(holder);
        pm.currentTransaction().commit();
        pm.getObjectId(holder);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(FkColHolder1.class, "val == valParam");
        ((VersantQuery)q).setFetchGroup("fkList");
        q.declareParameters("String valParam");
        List result = (List)q.execute(val);
        System.out.println("\n\n\n\n\n********************* load holder");
        holder = (FkColHolder1)result.get(0);
        System.out.println("\n\n\n\n\n********************* load fk coll");
        holder.getFkList();

        pm.deletePersistentAll(holder.getFkList());
        pm.deletePersistent(holder);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDfgColRefs() {
        Collection col = new ArrayList();
        PersistenceManager pm = pmf().getPersistenceManager();
        String val = "" + System.currentTimeMillis();
        pm.currentTransaction().begin();

        A a = new A();
        a.setValA("A" + val);
        col.add(a);

        B b = new B();
        b.setValB("B" + val);
        col.add(b);

        B b2 = new B();
        b2.setValB("B2" + val);

        C c2 = new C();
        c2.setValC("C2" + val);
        b2.setRefToC(c2);

        a.getListOfA().add(b2);
        a.setRefFromAToB(b);

        C c = new C();
        c.setValC("C" + val);
        col.add(c);

        D d = new D();
        d.setRefToB(b);
        col.add(d);
        pm.makePersistent(d);

        pm.makePersistent(a);
        pm.currentTransaction().commit();
        Object ida = pm.getObjectId(a);
        Object idb = pm.getObjectId(b);
        Object idd = pm.getObjectId(d);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        b = (B)pm.getObjectById(idb, true);

        a = (A)pm.getObjectById(ida, true);
        b = a.getRefFromAToB();
        b.getRefToC();
        a.getListOfA();

        d = (D)pm.getObjectById(idd, true);
        d.getRefToB();
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Make sure our flush behaves as documented.
     */
    public void testFlushStateTransitions() throws Exception {
        if (true) {
            broken();
            return;
        }

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        // flush retainValues=false - instance must be hollow and have ID
        pm.currentTransaction().begin();
        Address a = new Address("somewhere");
        pm.makePersistent(a);
        Assert.assertTrue(JDOHelper.isNew(a));
        Assert.assertTrue(JDOHelper.isDirty(a));
        Assert.assertTrue(JDOHelper.isPersistent(a));
        Assert.assertTrue(!pm.isHollow(a));
        Assert.assertTrue(!pm.hasIdentity(a));
        pm.flush();
        System.out.println("new = " + JDOHelper.isNew(a));
        System.out.println("dirty = " + JDOHelper.isDirty(a));
        System.out.println("persistent = " + JDOHelper.isPersistent(a));
        System.out.println("hollow = " + pm.isHollow(a));
        System.out.println("hasIdentity = " + pm.hasIdentity(a));
        Assert.assertTrue(!JDOHelper.isNew(a));
        Assert.assertTrue(!JDOHelper.isDirty(a));
        Assert.assertTrue(JDOHelper.isPersistent(a));
        Assert.assertTrue(pm.isHollow(a));
        Assert.assertTrue(pm.hasIdentity(a));
        pm.currentTransaction().commit();

        // flush retainValues=true - instance must not be hollow
        pm.currentTransaction().begin();
        Address a2 = new Address("somewhere");
        pm.makePersistent(a2);
        pm.flush(true);
        System.out.println("new = " + JDOHelper.isNew(a2));
        System.out.println("dirty = " + JDOHelper.isDirty(a2));
        System.out.println("persistent = " + JDOHelper.isPersistent(a2));
        System.out.println("hollow = " + pm.isHollow(a2));
        System.out.println("hasIdentity = " + pm.hasIdentity(a2));
        Assert.assertTrue(!JDOHelper.isNew(a2));
        Assert.assertTrue(!JDOHelper.isDirty(a2));
        Assert.assertTrue(JDOHelper.isPersistent(a2));
        Assert.assertTrue(!pm.isHollow(a2));
        Assert.assertTrue(pm.hasIdentity(a2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(a2);
        pm.currentTransaction().commit();

        pm.close();
    }

    private int getP1Id() {
        return p1AppId++;
    }

    public void testBrokenRefs1() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        if (isRemote()) {
            unsupported();
            return;
        }
        ClassMetaData cmd = getCmd(Child1.class);
        cmd.returnNullForRowNotFound = ClassMetaData.NULL_NO_ROW_TRUE;
        System.out.println("\n\n -- cmd.qname = " + cmd.qname);
        System.out.println("cmd = " + System.identityHashCode(cmd));
        System.out.println(
                "cmd.returnNullForRowNotFound = " + cmd.returnNullForRowNotFound);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int id = getP1Id();
        Parent1 p = new Parent1();
        p.setId(id);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNull(p.getChild1());
        pm.deletePersistent(p);
        pm.close();
    }

    public void testBrokenRefs2() throws Exception {

        if (true) {
            broken();
            return;
        }

        if (isRemote()) {
            unsupported();
            return;
        }

        ClassMetaData cmd = getCmd(Child1.class);
        cmd.returnNullForRowNotFound = ClassMetaData.NULL_NO_ROW_FALSE;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int id = getP1Id();
        Parent1 p = new Parent1();
        p.setId(id);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        try {
            p.getChild1();
            fail("Expected VersantObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            //ignore
        }
        pm.close();
    }

    public void testBrokenRefs3() throws Exception {
        if (true) {
            broken();
            return; // this test is broken
        }

        if (isRemote()) {
            broken();
            return;
        }

        ClassMetaData cmd = getCmd(Child1.class);
        cmd.returnNullForRowNotFound = ClassMetaData.NULL_NO_ROW_TRUE;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int id = getP1Id();
        Parent1 p = new Parent1();
        p.setId(id);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNull(p.getChild1());
        Assert.assertNull(p.getChild1());
        Assert.assertNull(p.getChild1());
        Assert.assertNull(p.getChild1());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testBrokenRefs4() {
        if (true) {
            broken();
            return;
        }

        if (isRemote()) {
            unsupported();
            return;
        }

        ClassMetaData cmd = getCmd(Child1.class);
        cmd.returnNullForRowNotFound = ClassMetaData.NULL_NO_ROW_TRUE;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int id = getP1Id();
        Parent1 p = new Parent1();
        p.setId(id);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        p.setVal("bla");
        ((VersantPersistenceManager)pm).flush(true);
        Assert.assertNull(p.getChild1());
        pm.deletePersistent(p);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testSharedPk() {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SharedPkParent parent = new SharedPkParent(1, "parent");
        pm.makePersistent(parent);
        pm.currentTransaction().commit();
        Object pId = pm.getObjectId(parent);

        List result;
        VersantQuery q;

        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(SharedPkParent.class);
        result = (List)q.execute();
        for (int i = 0; i < result.size(); i++) {
            SharedPkParent sharedPkParent = (SharedPkParent)result.get(i);
            try {
                System.out.println("\n\n\n ----- \n\n\n");
                sharedPkParent.getChild();
                fail("Expected JDOObjectNotFoundException");
            } catch (JDOObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
        q.closeAll();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(SharedPkParent.class);
        q.setFetchGroup("child");
        result = (List)q.execute();

        try {
            result.get(0);
//            fail("Expected JDOObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
            e.printStackTrace();
        }
        q.closeAll();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(SharedPkParent.class);
        q.setFetchGroup("child");
        ((VersantQuery)q).setBounded(true);
        result = (List)q.execute();

        try {
            result.get(0);
//            fail("Expected JDOObjectNotFoundException");
        } catch (JDOObjectNotFoundException e) {
//            e.printStackTrace();
        }
        q.closeAll();

        pm.deletePersistent(pm.getObjectById(pId, false));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testSharedPk2() {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SharedPkParent parent = new SharedPkParent(1, "parent");
        SharedPkChild child = new SharedPkChild(1, "child");
        parent.setChild(child);
        pm.makePersistent(parent);
        pm.currentTransaction().commit();
        Object pId = pm.getObjectId(parent);
        Object cId = pm.getObjectId(child);

        List result;
        VersantQuery q;

        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(SharedPkParent.class);
        result = (List)q.execute();
        for (int i = 0; i < result.size(); i++) {
            SharedPkParent sharedPkParent = (SharedPkParent)result.get(i);
            SharedPkChild c = (SharedPkChild)sharedPkParent.getChild();
            Assert.assertEquals("child", c.getVal());
        }
        q.closeAll();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(SharedPkParent.class);
        q.setFetchGroup("child");
        result = (List)q.execute();

        SharedPkParent sharedPkParent = (SharedPkParent)result.get(0);
        SharedPkChild c = (SharedPkChild)sharedPkParent.getChild();
        Assert.assertEquals("child", c.getVal());
        q.closeAll();

//        q = (VersantQuery)pm.newQuery(SharedPkParent.class);
//        result = (List)q.execute();

        pm.deletePersistent(pm.getObjectById(pId, false));
        pm.deletePersistent(pm.getObjectById(cId, false));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testSharedPk3() {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SharedPkParent parent = new SharedPkParent(1, "parent");
        pm.makePersistent(parent);
        pm.currentTransaction().commit();
        Object pId = pm.getObjectId(parent);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        parent = (SharedPkParent)pm.getObjectById(pId, true);
        try {
            parent.getChild();
        } catch (JDOObjectNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        pm.deletePersistent(pm.getObjectById(pId, false));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testConCurUpdate() {
        if ("oracle".equals(getDbName())) {
            broken();
            return;
        }
        
        if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Role_JDO role = new Role_JDO();
        int roleId = 1;
        role.setRoleId(roleId);
        role.setName("bla");
        pm.makePersistent(role);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.setIgnoreCache(false);
        pm.currentTransaction().begin();
        Object oid = new Role_JDO.ID(Integer.toString(roleId));
        role = (Role_JDO)pm.getObjectById(oid, true);

        role.setDescription("myDescription");

        Query q = pm.newQuery(Role_JDO.class);
        q.declareParameters("String roleName");
        q.setFilter("name == roleName");

        Collection roles = (Collection)q.execute("bla1");

        if (roles.size() > 0) {
            throw new RuntimeException("Found other roles");
        }

        System.out.println(
                "\n\n******************************************************************************\n\n");

        role.setName("bla1");
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testPersistClassWithNoFields() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instances
        pm.currentTransaction().begin();
        NoFields o = new NoFields();
        pm.makePersistent(o);
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        // check we can get it back and nuke it
        pm.currentTransaction().begin();
        o = (NoFields)pm.getObjectById(oid, true);
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testRefreshOnDsTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        SimpleColHolder sh = new SimpleColHolder();
        sh.getDfgList().add(new SimpleColEntry(1));
        sh.getDfgList().add(new SimpleColEntry(2));
        sh.getDfgList().add(new SimpleColEntry(3));
        pm.makePersistent(sh);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, sh.getDfgList().size());
        pm.refreshAll();
        Assert.assertEquals(3, sh.getDfgList().size());
        pm.close();
    }

    /**
     * Test for "read from deleted instance" bug with prefetching into a
     * deleted instance (forums thread 708).
     */
    public void testPrefetchIntoDeletedInstance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "cape town");
        Holder h = new Holder("piggy", a);
        pm.makePersistent(h);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        // touching h will prefetch data into already deleted a
        System.out.println("h.getName() = " + h.getName());
        pm.deletePersistent(h);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that non-transactional writes are trapped (forums thread 551).
     */
    public void testNonTxWriteException() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().setNontransactionalRead(true);
        pm.currentTransaction().setNontransactionalWrite(false);
        pm.currentTransaction().begin();
        Address a = new Address("somestreet", "cape town");
        pm.makePersistent(a);
        pm.currentTransaction().commit();

        try {
            a.setCity("jhb");
            throw new TestFailedException("expected non-tx-write exception");
        } catch (JDOUserException x) {
            // good
        }

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test getting ab object by ID in the same tx that it was created in.
     */
    public void testGetNewObjectByIDInSameTx() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // datastore identity + HIGHLOW
        pm.currentTransaction().begin();
        Address a = new Address("somewhere");
        pm.makePersistent(a);
        Object oid = pm.getObjectId(a);
        Assert.assertTrue(a == pm.getObjectById(oid, true));
        pm.currentTransaction().commit();

        // datastore identity + AUTOINC
        AutoIncDatastore f = null;
        if (doAutoIncTests()) {
            pm.currentTransaction().begin();
            f = new AutoIncDatastore("dsai");
            pm.makePersistent(f);
            oid = pm.getObjectId(f);
            Assert.assertTrue(f == pm.getObjectById(oid, true));
            pm.currentTransaction().commit();
        }

        // application identity + no key generator
        pm.currentTransaction().begin();
        Country c = new Country("ZA", "South Africa");
        pm.makePersistent(c);
        oid = pm.getObjectId(c);
        Assert.assertTrue(c == pm.getObjectById(oid, true));
        pm.currentTransaction().commit();

        // application identity + HIGHLOW
        pm.currentTransaction().begin();
        AppIdClass d = new AppIdClass("hello");
        pm.makePersistent(d);
        oid = pm.getObjectId(d);
        Assert.assertTrue(d == pm.getObjectById(oid, true));
        pm.currentTransaction().commit();

        // application identity + AUTOINC
        AutoIncApp e = null;
        if (doAutoIncTests()) {
            pm.currentTransaction().begin();
            e = new AutoIncApp("world");
            pm.makePersistent(e);
            oid = pm.getObjectId(e);
            Assert.assertTrue(e == pm.getObjectById(oid, true));
            pm.currentTransaction().commit();
        }

        // application identity + AUTOINC + pk field touched
        AutoIncApp g = null;
        if (doAutoIncTests()) {
            pm.currentTransaction().begin();
            g = new AutoIncApp("world2");
            pm.makePersistent(g);
            oid = new AutoIncApp.ID(g.getAutoIncAppId());
            Assert.assertTrue(g == pm.getObjectById(oid, true));
            pm.currentTransaction().commit();
        }

        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(c);
        pm.deletePersistent(d);
        if (e != null) pm.deletePersistent(e);
        if (f != null) pm.deletePersistent(f);
        if (g != null) pm.deletePersistent(g);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a hierarchy with lots of fields.
     */
    public void testHierarchyWithLotsOfFields() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        BigSub sub = new BigSub(10, "x");
        pm.makePersistent(sub);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        sub.check("x");
        sub.set("y");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        sub.check("y");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(sub);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test logging a user event.
     */
    public void testUserEvent() throws Exception {

        // test event logged on PMF
        LogEvent[] a = pmf().getNewPerfEvents(0);
        pmf().logEvent(VersantPersistenceManagerFactory.EVENT_ALL, "Hello!",
                10);
        a = pmf().getNewPerfEvents(a[a.length - 1].getId());
        Assert.assertEquals("Hello!", a[a.length - 1].getDescription());

        // test event logged on PM
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.logEvent(VersantPersistenceManagerFactory.EVENT_ALL, "Hello2", 20);
        a = pmf().getNewPerfEvents(a[a.length - 1].getId());
        Assert.assertEquals("Hello2", a[a.length - 1].getDescription());
        pm.close();
    }

    /**
     * Test user defined performance metrics.
     */
    public void testUserMetrics() throws Exception {
        pmf().setUserMetric("User1", 10);
        pmf().setUserMetric("User2", 20);
        Assert.assertEquals(10, pmf().getUserMetric("User1"));
        Assert.assertEquals(20, pmf().getUserMetric("User2"));

        pmf().incUserMetric("User1", 20);
        pmf().incUserMetric("User2", 40);
        Assert.assertEquals(30, pmf().getUserMetric("User1"));
        Assert.assertEquals(60, pmf().getUserMetric("User2"));

        // make sure they end up in the snapshots
        getMostRecentMetricSnapshot(); // discard previous snapshots
        MetricSnapshotPacket p = getMostRecentMetricSnapshot();
        Assert.assertEquals(30,
                p.getMostRecentSample((BaseMetric)lookupMetric("User1")));
        Assert.assertEquals(60,
                p.getMostRecentSample((BaseMetric)lookupMetric("User2")));
    }

    /**
     * Test null-if-not-found extension.
     */
    public void testNullIfNotFound() throws Exception {
        if (true) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // create two instances - one with valid ref and one not
        pm.currentTransaction().begin();
        Address a1 = new Address("a1");
        NullIfNotFound n1 = new NullIfNotFound("n1", a1);
        Address a2 = new Address("a1");
        NullIfNotFound n2 = new NullIfNotFound("n1", a2);
        pm.makePersistent(n1);
        pm.makePersistent(n2);
        pm.currentTransaction().commit();

        // delete the address on one of the instances but keep it referenced
        pm.currentTransaction().begin();
        pm.deletePersistent(a1);
        pm.currentTransaction().commit();

        // now navigate the fields and check there is no exception
        pm.currentTransaction().begin();
        Assert.assertTrue(n1.getAddress() == null);
        Assert.assertTrue(n2.getAddress() == a2);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a2);
        pm.deletePersistent(n1);
        pm.deletePersistent(n2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * For testing Parent-Child zero ref problem (forums 609).
     */
    public void testParentChildAppIdentity() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Parent p = new Parent();
        Child c = new Child();
        p.setChild(c);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(p.getChild() == c);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(p);
        pm.deletePersistent(c);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * This is just for hacking around with error handling. It is not actually
     * a test.
     */
    public void testFetchWithError() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("someone", new Country("ZA", "South Africa"));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        Collection ans = (Collection)q.execute();
        p = (Person)ans.iterator().next();
        q.closeAll();
        System.out.println("p.getCountry() = " + p.getCountry());
        Assert.assertTrue(!pm.isClosed());
        pm.currentTransaction().commit();

//        pm.currentTransaction().begin();
//        pm.currentTransaction().commit();
//
//        pm.currentTransaction().begin();
//        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that equals works on OID instances returned by getObjectId and
     * that they serialize ok.
     */
    public void testEqualsOnOIDAndSerialization() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create an instance to look for
        pm.currentTransaction().begin();
        Address a = new Address("testEqualsOnOID");
        pm.makePersistent(a);
        Object oid1 = pm.getObjectId(a);
        Assert.assertTrue(oid1 instanceof VersantOid);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check the new OID is equal to old OID
        pm.currentTransaction().begin();
        Object oid2 = pm.newObjectIdInstance(null, oid1.toString());
        Assert.assertTrue(oid2 instanceof VersantOid);
        Assert.assertEquals(oid1, oid2);
        a = (Address)pm.getObjectById(oid2, true);
        Assert.assertEquals(oid2, pm.getObjectId(a));
        pm.currentTransaction().commit();

        // serialize and unserialize oid1 and oid2 and check equal
        oid1 = serialized(oid1);
        oid2 = serialized(oid2);
        Assert.assertEquals(oid1, oid2);

        pm.close();
        pm = pmf().getPersistenceManager();

        // check that everything still works with serialized OIDs
        pm.currentTransaction().begin();
        Object oid3 = pm.newObjectIdInstance(null, oid1.toString());
        Assert.assertEquals(oid1, oid3);    // ser compared to normal
        a = (Address)pm.getObjectById(oid1, true);
        Assert.assertEquals(oid3, pm.getObjectId(a));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Serialize and unserialize an object.
     */
    private Object serialized(Object o) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buf);
        out.writeObject(o);
        out.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(buf.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        return in.readObject();
    }

    /**
     * Check that looking up instances in a hierarchy with OIDs constructed
     * using the class ID for the base class works.
     */
    public void testFetchForUnresolvedOID() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        // create SubClass instance
        pm.currentTransaction().begin();
        SubClass sub = new SubClass("sub", 10);
        pm.makePersistent(sub);
        Object oidSub = pm.getObjectId(sub);
        String oidSubStr = oidSub.toString();
        String pk = oidSubStr.substring(oidSubStr.indexOf('-') + 1);
        pm.currentTransaction().commit();

        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        // check we can get it back with an OID string for the base class
        pm.currentTransaction().begin();
        String s = pmf().getClassID(BaseClass.class) + "-" + pk;
        Object oid = pm.newObjectIdInstance(null, s, false);
        sub = (SubClass)pm.getObjectById(oid, true);
        // check local cache lookup as well
        Assert.assertTrue(sub == pm.getObjectById(oid, true));
        pm.currentTransaction().commit();

        pm.close();
        pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        // check we can get it back with an OID string for the base class
        pm.currentTransaction().begin();
        sub = (SubClass)pm.getObjectByIDString(s, true, false);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(sub);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test that classes with cache-strategy=all work properly and are
     * correctly reloaded after evictAll or evict(Class).
     */
    public void testCacheAll() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // use opt tx so cache is used
        pm.currentTransaction().setOptimistic(true);

        // create some instances
        pm.currentTransaction().begin();
        CacheAll[] a = new CacheAll[4];
        for (int i = 0; i < a.length; i++) a[i] = new CacheAll("a" + i);
        pm.makePersistentAll(a);
        Object[] oid = new Object[a.length];
        for (int i = 0; i < a.length; i++) oid[i] = pm.getObjectId(a[i]);
        pm.currentTransaction().commit();

        // check that cache all works
        pm.currentTransaction().begin();
        testCacheAllImp(a, oid);
        pm.currentTransaction().commit();

        // do an evictAll and check cache all works
        pm.currentTransaction().begin();
        pmf().evictAll();
        assertNoneInCache(oid);
        testCacheAllImp(a, oid);
        pm.currentTransaction().commit();

        // evict just the CacheAll class and check that cache all works
        pm.currentTransaction().begin();
        pmf().evictAll(CacheAll.class, true);
        assertNoneInCache(oid);
        testCacheAllImp(a, oid);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistentAll(a);
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Touch the first instance in a and make sure all are put into the
     * level 2 cache. Also make sure that only one 'select' is done.
     */
    private void testCacheAllImp(CacheAll[] a, Object[] oid) {
    	if (!isSQLSupported()) // SQL
    		return;
 
        countExecQueryEvents();
        System.out.println("a[0].getName() = " + a[0].getName());
        Assert.assertEquals(1, countExecQueryEvents());
        for (int i = 1; i < oid.length; i++) {
            Assert.assertTrue(pmf().isInCache(oid[i]));
        }
    }

    private void assertNoneInCache(Object[] oid) {
        for (int i = 0; i < oid.length; i++) {
            Assert.assertTrue(!pmf().isInCache(oid[i]));
        }
    }

    /**
     * Test testGetObjectByIdFromCache method.
     */
    public void testGetObjectByIdFromCache() throws Exception {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        // create an object to lookup
        pm.currentTransaction().begin();
        Address a = new Address("testGetObjectByIdFromCache");
        pm.makePersistent(a);
        Object oid = pm.getObjectId(a);
        pm.currentTransaction().commit();

        // get it back and check that it is hollow
        pm.currentTransaction().begin();
        Address a2 = (Address)pm.getObjectByIdFromCache(oid);
        Assert.assertTrue(a == a2);
        Assert.assertTrue(pm.isHollow(a2));
        pm.currentTransaction().commit();

        // ask for an OID that does not exist and make sure we get null
        pm.currentTransaction().begin();
        Object bogusOID = pm.newObjectIdInstance(null,
                pmf().getClassID(Address.class) + "-12345678");
        Assert.assertNull(pm.getObjectByIdFromCache(bogusOID));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testNPEOnPolyListFetch() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = this.pmf().getPersistenceManager();

        System.out.println("\n*** Tester.go");

        //creating the container with two elements
        TomContainer newContainer = new TomContainer();
        ListableElement elem1 = new ListableElement("parentString1", "elem1");
        newContainer.addElement(elem1);
        AnotherListableElement elem3 = new AnotherListableElement(
                "parentString3", "elem3");
        newContainer.addElement(elem3);

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.makePersistent(newContainer);
        pm.currentTransaction().commit();
        pm.close();

        System.out.println("\n*** Tester.go: get container and elements");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        String criticalElementId = null;

        Extent extent = pm.getExtent(TomContainer.class, true);
        newContainer = (TomContainer)extent.iterator().next();
        for (Iterator iter = newContainer.getElements().iterator();
             iter.hasNext();) {
            AbstractListable currElement = (AbstractListable)iter.next();
            System.out.println(currElement.getParentString());
            if (currElement instanceof AnotherListableElement) {
                criticalElementId = currElement.getId();
            }
        }

        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println(
                "\n*** Tester.go: get critical element by id and edit it");
        AbstractListable.ObjectID oid = new AbstractListable.ObjectID(
                criticalElementId);
        AbstractListable criticalElem = (AbstractListable)pm.getObjectById(oid,
                true);

//        AbstractListable criticalElem = (AbstractListable) ((VersantPersistenceManager) pm).getObjectByIDString(
//                jdoGeniePmf.getClassID(AnotherListableElement.class) + "-" + criticalElementId, true);

        criticalElem.setParentString("newName");

        pm.currentTransaction().commit();
        pm.close();

        System.out.println("\n*** Tester.go: get container and elements 2");
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        extent = pm.getExtent(TomContainer.class, true);
        newContainer = (TomContainer)extent.iterator().next();
        for (Iterator iter = newContainer.getElements().iterator();
             iter.hasNext();) {
            AbstractListable currElement = (AbstractListable)iter.next();
            pm.retrieve(currElement);
        }

        pm.currentTransaction().rollback();

        // cleanup
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        extent = pm.getExtent(TomContainer.class, true);
        for (Iterator iter = extent.iterator(); iter.hasNext();) {
            pm.deletePersistent(iter.next());
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test that our long UTF8 methods work properly.
     */
    public void testLongUTF8() throws Exception {
        System.out.println("*** test all possible chars String");
        char[] a = new char[0xFFFF];
        for (int i = 0; i < a.length; i++) a[i] = (char)i;
        testLongUTF8(new String(a));

        System.out.println("*** test big random String");
        int n = 1000000;
        Random r = new Random(n);
        a = new char[n];
        for (int i = 0; i < n; i++) a[i] = (char)r.nextInt();
        testLongUTF8(new String(a));
    }

    private void testLongUTF8(String s) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(s.length() * 2);
        DataOutputStream out = new DataOutputStream(bo);
        Utils.writeLongUTF8(s, out);
        out.close();

        byte[] bytes = bo.toByteArray();
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bi);

        String t = Utils.readLongUTF8(in);

        int n = s.length();
        if (n != t.length()) {
            throw new TestFailedException(
                    "length error: " + n + " != " + t.length());
        }
        for (int i = 0; i < n; i++) {
            int a = s.charAt(i);
            int b = t.charAt(i);
            if (a != b) {
                throw new TestFailedException("error at " + i + ": " +
                        a + " != " + b);
            }
        }
    }

    /**
     * Check that the same connection is returned with every call. This test
     * will lockup or fail with a deadlock or locking exception if it breaks.
     */
    public void testPMGetJdbcConSameEachCall() throws Exception {
        if (isRemote() || !isJdbc()) {
            unsupported();
            return;
        }

        nuke(Contact.class);
        nuke(Address.class);

        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        VersantClientJDBCConnection con;
        Statement s;

        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();

//        pm.makePersistent(new Address("dreyer", "ct"));
//        pm.flush();

        // insert row into address
        con = (VersantClientJDBCConnection)pm.getJdbcConnection(null);
        Connection realCon = con.getRealConnection();
        s = con.createStatement();
        s.executeUpdate("insert into address(address_id, jdo_version, city, street) " +
                "values (1, 1, 'ct', 'dreyer')");
        s.close();
        con.close();

        // empty the pool to increase chances of this failing
        if (!isDataSource()){
            getJdbcConnectionPool().closeIdleConnections();
        }

        // check we can get it back with 'new' connection
        con = (VersantClientJDBCConnection)pm.getJdbcConnection(null);
        Assert.assertTrue(con.getRealConnection() == realCon);
        s = con.createStatement();
        ResultSet rs = s.executeQuery("select address_id from address");
        int c = 0;
        for (; rs.next(); c++) {
            System.out.println("rs.getInt(1) = " + rs.getInt(1));
        }
        rs.close();
        s.close();
        con.close();
        Assert.assertEquals(1, c);

        // nuke it with 'new' connection
        con = (VersantClientJDBCConnection)pm.getJdbcConnection(null);
        Assert.assertTrue(con.getRealConnection() == realCon);
        s = con.createStatement();
        c = s.executeUpdate("delete from address");
        s.close();
        con.close();
        Assert.assertEquals(1, c);

        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Check that multiple instances of a class with LONGVARBINARY field can
     * be inserted and updated at once (Oracle LONG RAW not allowed in batch
     * mode bug).
     */
    public void testLongFieldBatchMode() throws Exception {
        // informix SE does not support blobs
        if (!isJdbc() || getDbName().equals("informixse")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // insert
        pm.currentTransaction().begin();
        int n = 5;
        int m = 1000;
        LongField[] a = new LongField[n];
        for (int i = 0; i < n; i++) {
            byte[] data = new byte[m];
            for (int j = 0; j < m; j++) data[j] = (byte)i;
            a[i] = new LongField("lf" + i, data);
            pm.makePersistent(a[i]);
        }
        pm.currentTransaction().commit();

        // get back and check
        pm.currentTransaction().begin();
        for (int i = 0; i < n; i++) {
            Assert.assertEquals("lf" + i, a[i].getName());
            byte[] data = a[i].getData();
            Assert.assertNotNull(data);
            Assert.assertEquals(m, data.length);
            Assert.assertEquals(i, data[0]);
        }
        pm.currentTransaction().commit();

        // update
        pm.currentTransaction().begin();
        for (int i = 0; i < n; i++) {
            byte[] data = new byte[m];
            for (int j = 0; j < m; j++) data[j] = (byte)(i + 10);
            a[i].setData(data);
        }
        pm.currentTransaction().commit();

        // get back and check and nuke
        pm.currentTransaction().begin();
        for (int i = 0; i < n; i++) {
            Assert.assertEquals("lf" + i, a[i].getName());
            byte[] data = a[i].getData();
            Assert.assertNotNull(data);
            Assert.assertEquals(m, data.length);
            Assert.assertEquals(i + 10, data[0]);
            pm.deletePersistent(a[i]);
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test fetching a map field with joined references and subclasses
     * (Alans Knowhow bug).
     */
    public void testMapFetchWithJoinedRefsAndSubclasses() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        User u = new User("user", 10);
        Resource r = new Resource("resource",
                new ResourceData("resource data"));
        ResourceLog rl = new ResourceLog(u, 100, r);
        u.put(r, rl);
        pm.makePersistent(u);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Map m = u.getResourceLogMap();
        Assert.assertEquals(1, m.size());
        Assert.assertEquals(rl, m.get(r));
        Assert.assertEquals("resource", r.getName());
        Assert.assertEquals("resource data", r.getData().getDescription());
        Assert.assertEquals(u, rl.getUser());
        Assert.assertEquals(100, rl.getScore());
        Assert.assertEquals(r, rl.getResource());
        pm.deletePersistent(r.getData());
        pm.deletePersistent(r);
        pm.deletePersistent(rl);
        pm.deletePersistent(u);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test making an instance with a Map transient.
     */
    public void testTransientMap() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ClassWithMap o = new ClassWithMap("one");
        pm.makePersistent(o);
        o.setData("a", "aa");
        o.setData("b", "bb");
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o.getMap();
        pm.makeTransient(o);
        o.setData("c", "cc");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o = (ClassWithMap)pm.getObjectById(oid, true);
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test storing international characters in a CLOB.
     */
    public void testGetByOIDForPNewInstance() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Address o = new Address("somewhere");
        pm.makePersistent(o);
        System.out.println("\n*** before getObjectId() for new instance");
        Object oid = o.getOID();
        pm.getObjectById(oid, true);
        System.out.println("\n*** about to commit()");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test storing international characters in a CLOB.
     */
    public void testI18nClob() throws Exception {
        if (true) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        String sample = "I18n text \uC3A9\uC3A8\uC3A1";

        // create a instance
        pm.currentTransaction().begin();
        I18nClob o = new I18nClob(sample);
        pm.makePersistent(o);
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        // check it in new tx
        pm.currentTransaction().begin();
        Assert.assertEquals(sample, o.getI18nClob());
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check it in new pm and nuke it
        pm.currentTransaction().begin();
        o = (I18nClob)pm.getObjectById(oid, true);
        Assert.assertEquals(sample, o.getI18nClob());
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testAutoIncCircRef() {
        if (!doAutoIncTests()) return;
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AutoIncApp a1 = new AutoIncApp("n1");
        AutoIncApp a2 = new AutoIncApp("n2");
        a1.setOther(a2);
        a2.setOther(a1);
        pm.makePersistent(a1);
        pm.makePersistent(a2);
        AutoIncApp.ID id1 = (AutoIncApp.ID)pm.getObjectId(a1);
        AutoIncApp.ID id2 = (AutoIncApp.ID)pm.getObjectId(a2);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        System.out.println("a1.getOther() = " + a1.getOther());
        System.out.println("a2.getOther() = " + a2.getOther());
        assertTrue(a1.getOther() == a2);
        assertTrue(a2.getOther() == a1);
        pm.currentTransaction().commit();
        pm.close();

    }

    /**
     * Test using the autoincrement key generator for a datastore identity
     * class.
     */
    public void testAutoIncKeyGenDatastoreIdentity() throws Exception {
        if (!doAutoIncTests()) return;

        PersistenceManager pm = pmf().getPersistenceManager();

        // create 10 instances
        pm.currentTransaction().begin();
        int n = 10;
        Object oid[] = new Object[n];
        Object a[] = new Object[n];
        for (int i = 0; i < n; i++) {
            a[i] = new AutoIncDatastore("o" + i);
            pm.makePersistent(a[i]);
        }
        pm.currentTransaction().commit();
        for (int i = 0; i < n; i++) {
            oid[i] = pm.getObjectId(a[i]);
        }

        // close the PM so the local cache is empty
        pm.close();
        pm = pmf().getPersistenceManager();

        // make sure they all come back ok
        pm.currentTransaction().begin();
        for (int i = 0; i < n; i++) {
            AutoIncDatastore o = (AutoIncDatastore)pm.getObjectById(oid[i], true);
            Assert.assertEquals(o.getName(), "o" + i);
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test using the autoincrement key generator for a application identity
     * class.
     */
    public void testAutoIncKeyGenApplicationIdentity() throws Exception {
        if (!doAutoIncTests()) return;

        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();

        // create 10 instances
        pm.currentTransaction().begin();
        int n = 10;
        AutoIncApp[] oa = new AutoIncApp[n];
        for (int i = 0; i < n; i++) {
            pm.makePersistent(oa[i] = new AutoIncApp("o" + i));
        }

        // do a flush for the rest and check that the app id fields are filled
        pm.flush();
        for (int i = 0; i < n; i++) {
            System.out.println("oa[" + i + "].getAutoIncAppId() = " +
                    oa[i].getAutoIncAppId());
            Assert.assertTrue(oa[i].getAutoIncAppId() != 0);
        }

        pm.currentTransaction().commit();

        // make sure they all come back ok, then nuke em
        pm.currentTransaction().begin();
        Query q = pm.newQuery(AutoIncApp.class);
        q.setOrdering("name ascending");
        ArrayList a = new ArrayList((Collection)q.execute());
        Assert.assertEquals(n, a.size());
        for (int i = 0; i < n; i++) {
            AutoIncApp o = (AutoIncApp)a.get(i);
            Assert.assertEquals(o.getName(), "o" + i);
        }
//        pm.deletePersistentAll(a);
        pm.currentTransaction().commit();

        pm.close();
    }

}

