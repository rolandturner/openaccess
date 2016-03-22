
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
package tut1;

import tut1.model.*;

import javax.jdo.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * InsertData class for tut1. This creates a simple product catalog, creates a
 * customer and order and amends the order.
 *
 */
public class InsertData {

    public static void main(String[] args) {
        try {
            // load versant.properties project file as properties to connect
            Properties p = loadProperties();

            // run against local or remote JDO Genie server
            PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
                    p);
            InsertData main = new InsertData(pmf.getPersistenceManager());
            try {
                main.go();
            } finally {
                main.pm.close();
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = InsertData.class.getResourceAsStream("/versant.properties");
            if (in == null) {
                throw new IOException("versant.properties not on classpath");
            }
            p.load(in);
        } finally {
            if (in != null) in.close();
        }
        return p;
    }

    private PersistenceManager pm;

    public InsertData(PersistenceManager pm) {
        this.pm = pm;
    }

    public void go() throws Exception {
        createCatalog();
        showCatalog();
        String orderNo = createOrder();
        amendOrder(orderNo);
        complexQuery();
    }

    /**
     * Create the product catalog.
     */
    private void createCatalog() {
        pm.currentTransaction().begin();
        Category dogs = new Category("Dogs");
        Category cats = new Category("Cats");
        pm.makePersistent(new Item("D001", "Wiener Dog", dogs));
        pm.makePersistent(new Item("D002", "Snauzer", dogs));
        pm.makePersistent(new Item("C001", "Persian", cats));
        // dogs and cats are persisted as they are reachable from the items
        pm.currentTransaction().commit();
    }

    /**
     * List all items in the catalog in description order.
     */
    private void showCatalog() {
        Query q = pm.newQuery(Item.class);
        q.setOrdering("description ascending");
        Collection ans = null;
        try {
            ans = (Collection)q.execute();
            System.out.println("Catalog items in description order:");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Item item = (Item)i.next();
                System.out.println(item.getCode() + ", " + item.getDescription() +
                        ", " + item.getCategory().getName());
            }
            System.out.println("---");
        } finally {
            if (ans != null) q.close(ans);
        }
    }

    /**
     * Find the item with code.
     */
    private Item lookupItem(String code) {
        Query q = pm.newQuery(Item.class);
        q.declareParameters("String c");
        q.setFilter("code == c");
        Collection ans = null;
        try {
            ans = (Collection)q.execute(code);
            Iterator i = ans.iterator();
            if (i.hasNext()) return (Item)i.next();
            throw new IllegalArgumentException(
                    "No item with code: '" + code + "'");
        } finally {
            if (ans != null) q.close(ans);
        }
    }

    /**
     * Create an order for a new customer and return the order number.
     */
    private String createOrder() {
        pm.currentTransaction().begin();
        Order o = new Order(new Customer("DOE001", "John Doe"));
        o.addLine(new OrderLine(lookupItem("D001"), 3));
        o.addLine(new OrderLine(lookupItem("C001"), 1));
        pm.makePersistent(o);
        o = new Order(new Customer("DOE002", "Tom Smith"));
        o.addLine(new OrderLine(lookupItem("D002"), 5));
        o.addLine(new OrderLine(lookupItem("C001"), 2));
        pm.makePersistent(o);
        pm.currentTransaction().commit();
        return pm.getObjectId(o).toString();
    }

    /**
     * Lookup the order and adjust all quantities to 1.
     */
    private void amendOrder(String orderNo) {
        pm.currentTransaction().begin();
        Object oid = pm.newObjectIdInstance(Order.class, orderNo);
        Order o = (Order)pm.getObjectById(oid, false);
        for (Iterator i = o.getLines().iterator(); i.hasNext();) {
            OrderLine ol = (OrderLine)i.next();
            ol.setQty(1);
        }
        pm.currentTransaction().commit();
    }

    /**
     * More complex query example.
     */
    private void complexQuery() {
        Query q = pm.newQuery(Order.class);
        q.declareParameters("String c, int n, java.util.Date d");
        q.declareVariables("OrderLine v");
        q.setFilter(
                "orderDate > d && lines.contains(v) && v.item.code == c && v.qty > n");
        q.setOrdering("orderDate descending");
        Calendar weekAgo = new GregorianCalendar();
        weekAgo.add(Calendar.DAY_OF_YEAR, -7);
        Collection ans = null;
        try {
            ans = (Collection)q.execute("D001", new Integer(5),
                    weekAgo.getTime());
            System.out.println(
                    "Orders for Item D001 with qty > 5 placed in the last week:");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Order order = (Order)i.next();
                System.out.println(order);
            }
            System.out.println("---");
        } finally {
            if (ans != null) q.close(ans);
        }
    }

}
