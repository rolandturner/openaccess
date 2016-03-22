
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
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test2.model.*;
import com.versant.core.jdo.junit.test2.model.refpk.*;
import com.versant.core.jdo.junit.test2.model.refpk.Customer;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Tests for application identity.
 */
public class TestApplicationPK2 extends VersantTestCase {

    public void testAppIdWithDate() {
        if (!getDbName().equals("postgres")) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance
        pm.currentTransaction().begin();
        Date now = new Date();
        AppIdDateClass o = new AppIdDateClass(now, "now");
        pm.makePersistent(o);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AppIdDateClass data = (AppIdDateClass)pm.getObjectById(
                new AppIdDateClassPK(String.valueOf(now.getTime())), true);
        Assert.assertEquals("now", data.getName());
        pm.deletePersistent(data);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test deleting an existing app identity instance and persisting a new
     * instance in the same tx. This test currently fails.
     */
    public void testDeleteInsertSameInstance() throws Exception {
        if (true) {
            broken();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        // persist instance
        pm.currentTransaction().begin();
        AppIdClass o = new AppIdClass(200, "o");
        pm.makePersistent(o);
        pm.currentTransaction().commit();

        // nuke it and repersist new instance with same pk
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        AppIdClass o2 = new AppIdClass(200, "o2");
        pm.makePersistent(o2);
        pm.currentTransaction().commit();

        // check it is ok, then cleanup
        pm.currentTransaction().begin();
        Assert.assertEquals(200, o2.getPk());
        Assert.assertEquals("o2", o2.getName());
        pm.deletePersistent(o2);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test an application identity class with objectid-class that extends
     * a base class.
     */
    public void testObjectIdClassExtendsBaseClass() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        // persist one with given pk and one with generated pk
        pm.currentTransaction().begin();
        AppIdClass a = new AppIdClass(100, "a");
        AppIdClass b = new AppIdClass("b"); // autogen pk
        pm.makePersistent(a);
        pm.makePersistent(b);
        Object oidA = pm.getObjectId(a);
        Object oidB = pm.getObjectId(b);
        pm.currentTransaction().commit();

        // check instances are ok
        pm.currentTransaction().begin();
        Assert.assertEquals("a", a.getName());
        Assert.assertEquals("b", b.getName());
        pm.currentTransaction().commit();

        // check ID lookup works
        pm.currentTransaction().begin();
        AppIdClass a2 = (AppIdClass)pm.getObjectById(oidA, true);
        Assert.assertTrue(a == a2);
        AppIdClass b2 = (AppIdClass)pm.getObjectById(oidB, true);
        Assert.assertTrue(b == b2);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(a);
        pm.deletePersistent(b);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test shared column handling in a hierarchy.
     */
    public void testSharedColumnsInHierarchy() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create an instance of each class in the hierarchy
        pm.currentTransaction().begin();
        ShareBase shareBase = new ShareBase("shareBase");
        ShareA shareA = new ShareA("shareA", "phoneA");
        ShareB shareB = new ShareB("shareB", "phoneB");
        ShareAA shareAA = new ShareAA("shareAA", "phoneAA");
        pm.makePersistent(shareBase);
        pm.makePersistent(shareA);
        pm.makePersistent(shareB);
        pm.makePersistent(shareAA);
        pm.currentTransaction().commit();

        // get each one back and check it then change it
        pm.currentTransaction().begin();
        Assert.assertEquals("shareBase", shareBase.getName());
        Assert.assertEquals("shareA", shareA.getName());
        Assert.assertEquals("shareA", shareA.getNameA());
        Assert.assertEquals("phoneA", shareA.getPhone());
        Assert.assertEquals("shareB", shareB.getName());
        Assert.assertEquals("shareB", shareB.getNameB());
        Assert.assertEquals("phoneB", shareB.getPhone());
        Assert.assertEquals("shareAA", shareAA.getName());
        Assert.assertEquals("shareAA", shareAA.getNameA());
        Assert.assertEquals("shareAA", shareAA.getNameAA());
        Assert.assertEquals("phoneAA", shareAA.getPhone());
        Assert.assertEquals("phoneAA", shareAA.getPhoneAA());
        shareBase.setName("shareBase1");
        shareA.setNameA("shareA1");
        shareA.setPhone("phoneA1");
        shareB.setNameB("shareB1");
        shareB.setPhone("phoneB1");
        shareAA.setNameAA("shareAA1");
        shareAA.setPhoneAA("phoneAA1");
        pm.currentTransaction().commit();

        // get each one back and check it
        pm.currentTransaction().begin();
        Assert.assertEquals("shareBase1", shareBase.getName());
        Assert.assertEquals("shareA1", shareA.getName());
        Assert.assertEquals("shareA1", shareA.getNameA());
        Assert.assertEquals("phoneA1", shareA.getPhone());
        Assert.assertEquals("shareB1", shareB.getName());
        Assert.assertEquals("shareB1", shareB.getNameB());
        Assert.assertEquals("phoneB1", shareB.getPhone());
        Assert.assertEquals("shareAA1", shareAA.getName());
        Assert.assertEquals("shareAA1", shareAA.getNameA());
        Assert.assertEquals("shareAA1", shareAA.getNameAA());
        Assert.assertEquals("phoneAA1", shareAA.getPhone());
        Assert.assertEquals("phoneAA1", shareAA.getPhoneAA());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(shareBase);
        pm.deletePersistent(shareA);
        pm.deletePersistent(shareB);
        pm.deletePersistent(shareAA);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test insert, update, query, delete for classes that use application
     * identity with reference fields sharing columns.
     */
    public void testCompositePkRefQuerySimple() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        // create graph
        pm.currentTransaction().begin();
        CompRef ref = new CompRef(3, 4, "34", null);
        CompRef root = new CompRef(1, 2, "12", ref);
        pm.makePersistent(root);
        Assert.assertEquals("1/2/12/3/4/34/null", root.toString());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(CompRef.class, "ref == r");
        q.declareParameters("CompRef r");
        Collection ans = (Collection)q.execute(ref);
        Assert.assertEquals(1, ans.size());
        Assert.assertEquals(root, ans.iterator().next());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        pm.deletePersistent(root);
        pm.deletePersistent(ref);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with no columns shared with pk.
     */
    public void testCompositePkRefQueryNormal() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 5, 6, "hit");
        TriRef miss = new TriRef(7, 8, 9, "miss");
        root.setRef(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class, "ref == p");
        q.declareParameters("TriRef p");
        q.setOrdering("name ascending");
        Collection ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query for a miss
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(miss);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query with null
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals("hit miss", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match - this checks
        // that the SQL is correctly restored
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with middle column shared with pk.
     */
    public void testCompositePkRefQueryMiddle() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 2, 6, "hit");
        TriRef miss = new TriRef(7, 2, 9, "miss");
        root.setRefMiddle(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class, "refMiddle == p");
        q.declareParameters("TriRef p");
        q.setOrdering("name ascending");
        Collection ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query for a miss
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(miss);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query with null
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals("hit miss", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match - this checks
        // that the SQL is correctly restored
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with last column shared with pk.
     */
    public void testCompositePkRefQueryLast() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 5, 3, "hit");
        TriRef miss = new TriRef(7, 8, 3, "miss");
        root.setRefLast(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class, "refLast == p");
        q.declareParameters("TriRef p");
        q.setOrdering("name ascending");
        Collection ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query for a miss
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(miss);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query with null
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals("hit miss", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match - this checks
        // that the SQL is correctly restored
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with middle and last column shared
     * with pk.
     */
    public void testCompositePkRefQueryMiddleLast() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 2, 3, "hit");
        TriRef miss = new TriRef(7, 2, 3, "miss");
        root.setRefMiddleLast(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class, "refMiddleLast == p");
        q.declareParameters("TriRef p");
        q.setOrdering("name ascending");
        Collection ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query for a miss
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(miss);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query with null
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals("hit miss", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match - this checks
        // that the SQL is correctly restored
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with first and middle columns shared
     * with pk.
     */
    public void testCompositePkRefQueryFirstMiddle() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(1, 2, 6, "hit");
        TriRef miss = new TriRef(1, 2, 9, "miss");
        root.setRefFirstMiddle(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // Touch the null reference on hit to make sure it is fetched as null.
        // This tests for a bug where the non-null shared column is read first
        // and the reference is assumed to be not null as a result even though
        // the non-shared column is null.
        pm.currentTransaction().begin();
        Assert.assertNull(hit.getRefFirstMiddle());
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class, "refFirstMiddle == p");
        q.declareParameters("TriRef p");
        q.setOrdering("name ascending");
        Collection ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query for a miss
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(miss);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query with null
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals("hit miss", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match - this checks
        // that the SQL is correctly restored
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with all columns shared with
     * another reference.
     */
    public void testCompositePkRefQueryAllShared() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 5, 6, "hit");
        TriRef miss = new TriRef(7, 8, 9, "miss");
        root.setRefAllShared(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class, "refAllShared == p");
        q.declareParameters("TriRef p");
        q.setOrdering("name ascending");
        Collection ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query for a miss
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(miss);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat query with null
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals("hit miss", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // query for root based on ref and expect a match - this checks
        // that the SQL is correctly restored
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(hit);
        Assert.assertEquals("root", flatten(ans));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    private String flatten(Collection con) {
        StringBuffer s = new StringBuffer();
        Iterator i = con.iterator();
        if (i.hasNext()) {
            s.append(i.next());
            for (; i.hasNext();) {
                s.append(' ');
                s.append(i.next());
            }
        }
        return s.toString();
    }

    /**
     * Test 3 column composite pk reference with middle column shared.
     */
    public void testAppIdWithReferenceField() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query delQuery = pm.newQuery(OrderLine.class);
        pm.deletePersistentAll((Collection)delQuery.execute());

        delQuery = pm.newQuery(Order.class);
        pm.deletePersistentAll((Collection)delQuery.execute());

        delQuery = pm.newQuery(Item.class);
        pm.deletePersistentAll((Collection)delQuery.execute());

        delQuery = pm.newQuery(Customer.class);
        pm.deletePersistentAll((Collection)delQuery.execute());

        delQuery = pm.newQuery(Branch.class);
        pm.deletePersistentAll((Collection)delQuery.execute());
        pm.currentTransaction().commit();

        // create graph
        pm.currentTransaction().begin();
        Branch b1 = new Branch(1, "CT");
        Customer c1 = new Customer(b1, 100, "Woolies");
        Item i1 = new Item(b1, "WID001", "RedWidget");
        Item i2 = new Item(b1, "WID002", "GreenWidget");
        Order o = new Order(b1, 2000);
        o.setCustomer(c1);
        o.addOrderLine(i1, 10);
        o.addOrderLine(i2, 20);
        pm.makePersistent(o);
        String expected =
                "1/1/CT/2000 customer 1/1/CT/100/Woolies lines [" +
                "1/1/CT/2000/1 item 1/1/CT/WID001/RedWidget x 10, " +
                "1/1/CT/2000/2 item 1/1/CT/WID002/GreenWidget x 20" +
                "]";
        Assert.assertEquals(expected, o.toString());
        Object oidOrder = pm.getObjectId(o);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();

        // check that it is ok
        pm.currentTransaction().begin();
        o = (Order)pm.getObjectById(oidOrder, true);
        Assert.assertEquals(expected, o.toString());
        pm.currentTransaction().commit();

        // make an update to a ref with shared columns
        pm.currentTransaction().begin();
        Customer c2 = new Customer(o.getBranch(), 101, "Pnp");
        pm.deletePersistent(o.getCustomer());
        o.setCustomer(c2);
        expected =
                "1/1/CT/2000 customer 1/1/CT/101/Pnp lines [" +
                "1/1/CT/2000/1 item 1/1/CT/WID001/RedWidget x 10, " +
                "1/1/CT/2000/2 item 1/1/CT/WID002/GreenWidget x 20" +
                "]";
        Assert.assertEquals(expected, o.toString());
        pm.currentTransaction().commit();

        // check that it is ok
        pm.currentTransaction().begin();
        Assert.assertEquals(expected, o.toString());
        pm.currentTransaction().commit();

        // check that queries against fields with shared columns work
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class, "customer == c");
        q.declareParameters("Customer c");
        Collection ans = (Collection)q.execute(c2);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.iterator().next() == o);
        q.closeAll();
        o.setCustomer(null);
        pm.currentTransaction().commit();

        // repeat the query with null parameter
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(null);
        Assert.assertEquals(1, ans.size());
        Assert.assertTrue(ans.iterator().next() == o);
        q.closeAll();
        pm.currentTransaction().commit();

        // repeat the query with a non-null parameter but no results expected
        pm.currentTransaction().begin();
        ans = (Collection)q.execute(c2);
        Assert.assertEquals(0, ans.size());
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(c2);
        for (Iterator i = o.getLines().iterator(); i.hasNext();) {
            OrderLine line = (OrderLine)i.next();
            pm.deletePersistent(line.getItem());
            pm.deletePersistent(line);
        }
        pm.deletePersistent(o.getBranch());
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test a simple field sharing a column from a reference field.
     */
    public void testSharedColumnRefSimple() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Country za = new Country("ZA", "South Africa");
        Person p = new Person("David", za);
        pm.makePersistent(p);
        Country ru = new Country("RU", "Russia");
        pm.makePersistent(ru);
        pm.currentTransaction().commit();

        System.out.println(
                "\n*** check that the countryCode field is ok w/o fetching country");
        pm.currentTransaction().begin();
        Assert.assertEquals("ZA", p.getCountryCode());
        Assert.assertEquals(za, p.getCountry());
        pm.currentTransaction().commit();

        System.out.println("\n*** change the country");
        pm.currentTransaction().begin();
        p.setCountry(ru);
        pm.currentTransaction().commit();

        System.out.println(
                "\n*** check that the countryCode field is ok w/o fetching country");
        pm.currentTransaction().begin();
        Assert.assertEquals("RU", p.getCountryCode());
        Assert.assertEquals(ru, p.getCountry());
        pm.currentTransaction().commit();

        System.out.println("\n*** change the country to null");
        pm.currentTransaction().begin();
        p.setCountry(null);
        pm.currentTransaction().commit();

        System.out.println(
                "\n*** check that the countryCode field is ok w/o fetching country");
        pm.currentTransaction().begin();
        Assert.assertNull(p.getCountryCode());
        Assert.assertNull(p.getCountry());
        pm.currentTransaction().commit();

        System.out.println("\n*** cleanup");
        pm.currentTransaction().begin();
        pm.deletePersistent(za);
        pm.deletePersistent(ru);
        pm.deletePersistent(p);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with no columns shared with pk
     * compared to a null literal.
     */
    public void testCompositePkRefQueryNormalNullLiteral() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 5, 6, "hit");
        TriRef miss = new TriRef(7, 8, 9, "miss");
        root.setRef(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // check that we get the correct results for != null (i.e. root)
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class);
        q.setFilter("ref != null");
        q.setOrdering("name ascending");
        Assert.assertEquals("root", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // check that we get the correct results for == null (i.e. hit, miss)
        pm.currentTransaction().begin();
        q.setFilter("ref == null");
        Assert.assertEquals("hit miss", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with middle column shared with pk
     * compared to a null literal.
     */
    public void testCompositePkRefQueryMiddleNullLiteral() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 2, 6, "hit");
        TriRef miss = new TriRef(7, 2, 9, "miss");
        root.setRefMiddle(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // check that we get the correct results for != null (i.e. root)
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class);
        q.setFilter("refMiddle != null");
        q.setOrdering("name ascending");
        Assert.assertEquals("root", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // check that we get the correct results for == null (i.e. hit, miss)
        pm.currentTransaction().begin();
        q.setFilter("refMiddle == null");
        Assert.assertEquals("hit miss", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with last column shared with pk
     * compared to a null literal.
     */
    public void testCompositePkRefQueryLastNullLiteral() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 5, 3, "hit");
        TriRef miss = new TriRef(7, 8, 3, "miss");
        root.setRefLast(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // check that we get the correct results for != null (i.e. root)
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class);
        q.setFilter("refLast != null");
        q.setOrdering("name ascending");
        Assert.assertEquals("root", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // check that we get the correct results for == null (i.e. hit, miss)
        pm.currentTransaction().begin();
        q.setFilter("refLast == null");
        Assert.assertEquals("hit miss", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with middle and last column shared
     * with pk compared to a null literal.
     */
    public void testCompositePkRefQueryMiddleLastNullLiteral()
            throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 2, 3, "hit");
        TriRef miss = new TriRef(7, 2, 3, "miss");
        root.setRefMiddleLast(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // check that we get the correct results for != null (i.e. root)
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class);
        q.setFilter("refMiddleLast != null");
        q.setOrdering("name ascending");
        Assert.assertEquals("root", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // check that we get the correct results for == null (i.e. hit, miss)
        pm.currentTransaction().begin();
        q.setFilter("refMiddleLast == null");
        Assert.assertEquals("hit miss", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with first and middle columns shared
     * with pk compared to a null literal.
     */
    public void testCompositePkRefQueryFirstMiddleNullLiteral()
            throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(1, 2, 6, "hit");
        TriRef miss = new TriRef(1, 2, 9, "miss");
        root.setRefFirstMiddle(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // check that we get the correct results for != null (i.e. root)
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class);
        q.setFilter("refFirstMiddle != null");
        q.setOrdering("name ascending");
        Assert.assertEquals("root", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // check that we get the correct results for == null (i.e. hit, miss)
        pm.currentTransaction().begin();
        q.setFilter("refFirstMiddle == null");
        Assert.assertEquals("hit miss", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test 3 column composite pk reference with all columns shared with
     * another reference compared to a null literal.
     */
    public void testCompositePkRefQueryAllSharedNullLiteral() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TriRef root = new TriRef(1, 2, 3, "root");
        TriRef hit = new TriRef(4, 5, 6, "hit");
        TriRef miss = new TriRef(7, 8, 9, "miss");
        root.setRefAllShared(hit);
        pm.makePersistent(root);
        pm.makePersistent(miss);
        pm.currentTransaction().commit();

        // check that we get the correct results for != null (i.e. root)
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TriRef.class);
        q.setFilter("refAllShared != null");
        q.setOrdering("name ascending");
        Assert.assertEquals("root", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // check that we get the correct results for == null (i.e. hit, miss)
        pm.currentTransaction().begin();
        q.setFilter("refAllShared == null");
        Assert.assertEquals("hit miss", flatten((Collection)q.execute()));
        q.closeAll();
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(hit);
        pm.deletePersistent(miss);
        pm.deletePersistent(root);
        pm.currentTransaction().commit();

        pm.close();
    }

}
