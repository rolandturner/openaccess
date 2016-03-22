
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
package com.versant.core.jdo.junit.ejbql;

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.ejbql.model.*;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

/**
 * EJBQL tests using JDO API.
 */
public class TestEJBQL extends VersantTestCase {

    /**
     * Persist a simple model based on the spec examples. This is used by many
     * of the tests. If you change this make sure that all the tests still
     * work.
     */
    private void createSimpleModel() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Product[] products = new Product[]{
            new Product("TypeA", "A1"),
            new Product("TypeA", "A2"),
            new Product("TypeB", "B1"),
        };

        Order o1 = new Order("o1");
        o1.setShippingAddress(new ShippingAddress("CA", "Fremont", "Dumnbarton"));
        o1.setBillingAddress(new BillingAddress("Cape", "Cape Town", "Medway"));
        o1.getLineItems().add(new LineItem(o1, products[0], 03));
        o1.getLineItems().add(new LineItem(o1, products[1], 13));

        Order o2 = new Order("o2");
        o2.setShippingAddress(new ShippingAddress("Gauteng", "Somecity", "Somestreet"));
        o2.setBillingAddress(new BillingAddress("Cape", "Somerset West", "Middle of nowhere"));
        o2.getLineItems().add(new LineItem(o2, products[2], 25));

        Order o3 = new Order("o3");
        o3.setShippingAddress(new ShippingAddress("Cape", "Gordens Bay", "Middle of nowhere"));
        o3.setBillingAddress(new BillingAddress("Cape", "Somerset West", "Middle of nowhere"));
        o3.getLineItems().add(new LineItem(o3, products[0], 35));

        pm.makePersistent(o1);
        pm.makePersistent(o2);
        pm.makePersistent(o3);

        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * SELECT p FROM Product p.
     */
    public void testSimple1() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        // get the results all at once
        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL", "SELECT p FROM Product p");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertProductNames(a, new String[]{"A1", "A2", "B1"});
        pm.currentTransaction().commit();

        // get the results one by one
        pm.currentTransaction().begin();
        a = new ArrayList();
        for (Iterator i = ((Collection)q.execute()).iterator(); i.hasNext(); ) {
            a.add(i.next());
        }
        q.closeAll();
        assertProductNames(a, new String[]{"A1", "A2", "B1"});
        pm.currentTransaction().commit();

        pm.close();
    }

    private void assertProductNames(List list, String[] expected) {
        assertEquals(expected.length, list.size());
        Collections.sort(list);
        for (int i = 0; i < expected.length; i++) {
            Product p = (Product)list.get(i);
            assertEquals(expected[i], p.getName());
        }
    }

    /**
     * SELECT p FROM Product p WHERE p.name = 'A1'.
     */
    public void testSimple2() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL", "SELECT p FROM Product p WHERE p.name = 'A1'");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertProductNames(a, new String[]{"A1"});
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT p FROM Product p WHERE p.name = :name'.
     */
    public void testParam1() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL", "SELECT p FROM Product p WHERE p.name = :name");
        ArrayList a = new ArrayList((Collection)q.execute("A1"));
        q.closeAll();
        assertProductNames(a, new String[]{"A1"});
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT o FROM Order o WHERE o.shippingAddress.state = 'CA1'
     */
    public void testSimpleNav1() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = 'CA'");
        ArrayList a = new ArrayList((Collection)q.execute());
        assertOrderCustomers(a, new String[]{"o1"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    private void assertOrderCustomers(List list, String[] expected) {
        assertEquals(expected.length, list.size());
        Collections.sort(list);
        for (int i = 0; i < expected.length; i++) {
            Order o = (Order)list.get(i);
            assertEquals(expected[i], o.getCustomer());
        }
    }

    /**
     * SELECT o FROM Order o
     * WHERE o.shippingAddress.state = 'CA1'
     *   AND o.shippingAddress.city = 'Fremont'
     */
    public void testAndNav2() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = 'CA' " +
                "  AND o.shippingAddress.city = 'Fremont'");
        ArrayList a = new ArrayList((Collection)q.execute());
        assertOrderCustomers(a, new String[]{"o1"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT o FROM Order o
     * WHERE o.shippingAddress.state = :1
     *   AND o.shippingAddress.city = :2
     */
    public void testAndNav2WithParams() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = ?1 " +
                "  AND o.shippingAddress.city = ?2");
        ArrayList a = new ArrayList((Collection)q.execute("CA", "Fremont"));
        assertOrderCustomers(a, new String[]{"o1"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = :state " +
                "  AND o.shippingAddress.city = :city");
        a = new ArrayList((Collection)q.execute("CA", "Fremont"));
        assertOrderCustomers(a, new String[]{"o1"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT o FROM Order o
     * WHERE o.shippingAddress.state = :1
     *    OR o.shippingAddress.city = :2
     */
    public void testOrNav2WithParams() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = ?1 " +
                "   OR o.shippingAddress.city = ?2");
        ArrayList a = new ArrayList((Collection)q.execute("CA", "Somecity"));
        assertOrderCustomers(a, new String[]{"o1", "o2"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = :state " +
                "   OR o.shippingAddress.city = :city");
        a = new ArrayList((Collection)q.execute("CA", "Somecity"));
        assertOrderCustomers(a, new String[]{"o1", "o2"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT o FROM Order o
     * WHERE o.shippingAddress.state = 'C' + 'A'
     */
    public void testStringConcat1() {
        if (true) {
            broken();
            return;
        }

        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT o FROM Order o " +
                "WHERE o.shippingAddress.state = 'C' + 'A'");
        ArrayList a = new ArrayList((Collection)q.execute());
        assertOrderCustomers(a, new String[]{"o1"});
        q.closeAll();
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT p FROM Product p WHERE p.name LIKE 'A%'.
     */
    public void testLike1() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL", "SELECT p FROM Product p WHERE p.name LIKE 'A%'");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertProductNames(a, new String[]{"A1", "A2"});
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT p FROM Product p WHERE p.name BETWEEN 'A0' AND 'A9'.
     */
    public void testBetween1() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT p FROM Product p WHERE p.name BETWEEN 'A0' AND 'A9'");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertProductNames(a, new String[]{"A1", "A2"});
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * SELECT OBJECT(p) FROM Product p WHERE p.name = 'A1'.
     */
    public void testObject1() {
        createSimpleModel();

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Query q = pm.newQuery("EJBQL",
                "SELECT OBJECT(p) FROM Product p WHERE p.name = 'A1'");
        ArrayList a = new ArrayList((Collection)q.execute());
        q.closeAll();
        assertProductNames(a, new String[]{"A1"});
        pm.currentTransaction().commit();

        pm.close();
    }

}

