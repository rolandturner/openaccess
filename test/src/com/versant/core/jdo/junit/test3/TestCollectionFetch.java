
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
package com.versant.core.jdo.junit.test3;

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.*;
import com.versant.core.jdo.VersantQuery;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;

/**
 * Test the SQL generated to fetch various types of collections. These tests
 * make sure that there are no extra joins and other insidious bugs that
 * cause things to work but slowly.
 */
public class TestCollectionFetch extends VersantTestCase {

    /**
     * Make sure the fetch of an unordered inverse FK one to many using
     * a JDOQL query is ok (does parallel fetch).
     */
    public void testFetchOneToManyJDOQL() throws Exception {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Order o1 = new Order("xo1");
        o1.add(new OrderLine("line1"));
        o1.add(new OrderLine("line2"));
        Order o2 = new Order("xo2");
        o2.add(new OrderLine("line1"));
        o2.add(new OrderLine("line2"));
        Order o3 = new Order("xo3");
        o3.add(new OrderLine("line1"));
        o3.add(new OrderLine("line2"));
        pm.makePersistent(o1);
        pm.makePersistent(o2);
        pm.makePersistent(o3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        findExecQuerySQL();
        VersantQuery q = (VersantQuery)pm.newQuery(Order.class,
                "name.startsWith('xo')");
        ((VersantQuery)q).setBounded(true);
        q.setFetchGroup("all");
        ((Collection)q.execute()).size();
        q.closeAll();
        String[] a = findAllExecQuerySQL();
        assertEquals(1, a.length);
        // if parallel fetch is not done there will be 4 queries

        pm.close();
    }

    /**
     * Make sure the fetch of an unordered inverse FK one to many is ok
     * (no joins, no order by).
     */
    public void testFetchOneToMany() throws Exception {
        if (true) {
            broken();
            return;
        }
        
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Order o1 = new Order("o1");
        o1.add(new OrderLine("line1"));
        o1.add(new OrderLine("line2"));
        pm.makePersistent(o1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        findExecQuerySQL();
        o1.getLines();

        String[] a = findAllExecQuerySQL();
        assertEquals(2, a.length);

        // make sure the query to fetch the lines does not join
        String sql = a[0].toUpperCase();
        System.out.println("sql = " + sql);
        assertTrue(sql.indexOf("ORDER_LINE") >= 0);
        assertFalse(sql.indexOf("JOIN") >= 0);
        assertFalse(sql.indexOf("ORDER BY") >= 0);

        // make sure the extra query is fetching the version number
        sql = a[1].toUpperCase();
        assertTrue(sql.indexOf("ORDR") >= 0);
        assertTrue(sql.indexOf("JDO_VERSION") >= 0);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the fetch of an unordered inverse FK one to many with
     * outer join fetched reference is ok (no joins, no order by).
     */
    public void testFetchOneToMany2() throws Exception {
        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Order2 o1 = new Order2("o1", new Customer("cust1"));
        o1.add(new OrderLine2(new Item("line1")));
        o1.add(new OrderLine2(new Item("line2")));
        pm.makePersistent(o1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        findExecQuerySQL();
        o1.getLines();

        String[] a = findAllExecQuerySQL();
        assertEquals(1, a.length);

        // make first query is joining to the customer
        String sql = a[0].toUpperCase();
        assertTrue(sql.indexOf("ORDER2") >= 0);
        assertTrue(sql.indexOf("CUSTOMER") >= 0);
        assertTrue(sql.indexOf("LASTNAME") >= 0);
        assertTrue(sql.indexOf("JDO_VERSION") >= 0);
        pm.currentTransaction().commit();

        // make sure the query to fetch the lines joins only to item
        sql = a[0].toUpperCase();
        assertTrue(sql.indexOf("ORDER_LINE2") >= 0);
        assertTrue(sql.indexOf("ITEM") >= 0);
        assertFalse(sql.indexOf("ORDER BY") >= 0);

        pm.close();
    }

    /**
     * Make sure the fetch of an unordered link table collection is ok
     * (no extra joins, no order by).
     */
    public void testFetchLinkTable() throws Exception {
        if (true) {
            broken();
            return;
        }

        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Order3 o1 = new Order3("o1");
        o1.add(new OrderLine3("line1"));
        o1.add(new OrderLine3("line2"));
        pm.makePersistent(o1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        findExecQuerySQL();
        o1.getLines();

        String[] a = findAllExecQuerySQL();
        assertEquals(2, a.length);

        // make sure the query to fetch the lines only joins to link table
        String sql = a[0].toUpperCase();
        assertTrue(sql.indexOf("ORDER_LINE3") >= 0);
        assertTrue(sql.indexOf("ORDER3_ORDER_LINE3") >= 0);
        assertFalse(sql.indexOf("ORDER BY") >= 0);

        // make sure the extra query is fetching the version number
        sql = a[1].toUpperCase();
        assertTrue(sql.indexOf("ORDER3") >= 0);
        assertTrue(sql.indexOf("JDO_VERSION") >= 0);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the fetch of an unordered link table collection with
     * outer join fetched reference is ok (no extra joins, no order by).
     */
    public void testFetchLinkTable2() throws Exception {
        if (true) {
            broken();
            return;
        }

        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Order4 o1 = new Order4("o1", new Customer("cust1"));
        o1.add(new OrderLine4(new Item("line1")));
        o1.add(new OrderLine4(new Item("line2")));
        pm.makePersistent(o1);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        findExecQuerySQL();
        o1.getLines();

        String[] a = findAllExecQuerySQL();
        assertEquals(2, a.length);

        // make first query is joining to the customer
        String sql = a[0].toUpperCase();
        assertTrue(sql.indexOf("ORDER4") >= 0);
        assertTrue(sql.indexOf("CUSTOMER") >= 0);
        assertTrue(sql.indexOf("LASTNAME") >= 0);
        assertTrue(sql.indexOf("JDO_VERSION") >= 0);
        pm.currentTransaction().commit();

        // make sure the query to fetch the lines joins only to item
        sql = a[1].toUpperCase();
        assertTrue(sql.indexOf("ORDER4_ORDER_LINE4") >= 0);
        assertTrue(sql.indexOf("ORDER_LINE4") >= 0);
        assertTrue(sql.indexOf("ITEM") >= 0);
        assertFalse(sql.indexOf("ORDER BY") >= 0);

        pm.close();
    }

    /**
     * Test fetching a tree with children in a link table.
     */
    public void testFetchLinkTableTree() throws Exception {
        if (true) {
            broken();
            return;
        }

        if (!isJdbc()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        TreeNode root = new TreeNode("root");
        TreeNode c1 = new TreeNode("c1");
        TreeNode c2 = new TreeNode("c2");
        root.add(c1);
        root.add(c2);
        TreeNode c1a = new TreeNode("c1a");
        TreeNode c1b = new TreeNode("c1b");
        c1.add(c1a);
        c1.add(c1b);
        TreeNode c2a = new TreeNode("c2a");
        TreeNode c2b = new TreeNode("c2b");
        c2.add(c2a);
        c2.add(c2b);
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(root);

        pm.currentTransaction().begin();
        findExecQuerySQL();
        pm.getObjectById(oid, true);
        String[] a = findAllExecQuerySQL();
        pm.currentTransaction().commit();

        pm.close();
    }
}

