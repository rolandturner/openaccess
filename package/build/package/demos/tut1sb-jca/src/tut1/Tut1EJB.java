
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

import java.rmi.*;
import java.util.*;
import java.io.*;
import javax.ejb.*;
import javax.naming.*;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import tut1.model.*;

/**
 * The Tut1 session bean. This provides a few simple services that are
 * invoked by the client.
 * @see Main
 * @see Tut1
 * @see Tut1Home
 */
public class Tut1EJB implements SessionBean {

    private SessionContext ctx;
    private PersistenceManagerFactory pmf;

    private void log(String msg) {
        System.out.println("Tut1EJB: " + msg);
    }

    public void setSessionContext(SessionContext ctx) throws EJBException {
        this.ctx = ctx;
        try {
            Context ic = new InitialContext();
            pmf = (PersistenceManagerFactory)ic.lookup("java:comp/env/jdo/jdo_tut1sb_jca");
            log("Created PMF pmf = " + pmf);
        } catch (NamingException e) {
            log("Error looking up PMF: tut1sb");
            throw new EJBException(e);
        }
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    public void ejbCreate() throws RemoteException, CreateException {
    }

    public void ejbRemove() throws EJBException {
    }

    /**
     * Create the product catalog.
     */
    public void createCatalog() {
        log("createCatalog()");
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Category dogs = new Category("Dogs");
            Category cats = new Category("Cats");
            pm.makePersistent(new Item("D001", "Wiener Dog", dogs));
            pm.makePersistent(new Item("D002", "Snauzer", dogs));
            pm.makePersistent(new Item("C001", "Persian", cats));
        } catch (RuntimeException x) {
            x.printStackTrace();
            throw x;
        } finally {
            pm.close();
        }
    }

    /**
     * Return all items in the catalog in description order.
     */
    public List listItems() {
        log("listItems()");
        Query q = null;
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            q = pm.newQuery(Item.class);
            q.setOrdering("description ascending");
            List ans = new ArrayList((List) q.execute());
            pm.retrieveAll(ans);        // make sure all fields are filled in
            //call makeTransient on all the categories
            for (int i = 0; i < ans.size(); i++) {
                Category cat = ((Item) ans.get(i)).getCategory();
                pm.retrieve(cat);
                pm.makeTransient(cat);
            }
            pm.makeTransientAll(ans);   // must be done prior to serialization
            return ans;
        } catch (RuntimeException x) {
            x.printStackTrace();
            throw x;
        } finally {
            if (q != null) q.closeAll();
            pm.close();
        }
    }

    /**
     * Lookup an item by code or return null if no item found.
     */
    public Item lookupItem(String code) throws RemoteException {
        log("lookUpItem('" + code + "')");
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Item item = lookupItem(pm, code);
            if (item != null) {
                pm.retrieve(item);      // make sure all fields are filled in
                pm.retrieve(item.getCategory());
                pm.makeTransient(item.getCategory());
                pm.makeTransient(item); // must be done prior to serialization
            }
            return item;
        } catch (RuntimeException x) {
            x.printStackTrace();
            throw x;
        } finally {
            pm.close();
        }
    }

    private Item lookupItem(PersistenceManager pm, String code) {
        Query q = pm.newQuery(Item.class);
        try {
            q.declareParameters("String c");
            q.setFilter("code == c");
            Iterator i = ((Collection) q.execute(code)).iterator();
            if (i.hasNext()) return (Item) i.next();
            return null;
        } finally {
            q.closeAll();
        }
    }

    /**
     * Create an order for a new customer.
     *
     * Also note that the transient items on the order are replaced by
     * requeried JDO instances. This avoids creating a new item for each
     * line on the order.<p>
     */
    public String createOrder(Order o) throws RemoteException {
        log("createOrder()");
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            // relookup and replace each item to avoid creating new items
            for (Iterator i = o.getLines().iterator(); i.hasNext();) {
                OrderLine line = (OrderLine) i.next();
                String code = line.getItem().getCode();
                Item item = lookupItem(pm, code);
                if (item == null) {
                    throw new IllegalArgumentException("Invalid item code '" +
                            code + "'");
                }
                line.setItem(item);
            }
            o.setOrderDate(new Date());
            pm.makePersistent(o);
            return pm.getObjectId(o).toString();
        } catch (RuntimeException x) {
            x.printStackTrace();
            throw x;
        } finally {
            pm.close();
        }
    }

    /**
     * Lookup an Order. Returns null if not found. This returns the Order
     * as a graph of value objects. This is done by touching each field to
     * fetch it and making JDO instances transient with pm.makeTransient.
     * Another way to do this is to Serialize and de-Serialize the graph b
     * this will always fetch everything.
     * @see #fetchAll
     */
    public Order lookupOrder(String orderNo) throws RemoteException {
        log("lookupOrder('" + orderNo + "')");
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Object oid = pm.newObjectIdInstance(Order.class, orderNo);
            Order o = (Order)pm.getObjectById(oid, true);
            for (Iterator i = o.getLines().iterator(); i.hasNext(); ) {
                OrderLine line = (OrderLine)i.next();
                pm.makeTransient(line.getItem().getCategory());
                pm.makeTransient(line.getItem());
            }
            pm.makeTransientAll(o.getLines());
            pm.makeTransient(o.getCustomer());
            pm.makeTransient(o);
            return o;
        } catch (RuntimeException x) {
            x.printStackTrace();
            throw x;
        } finally {
            pm.close();
        }
    }

    /**
     * Fetch the whole object graph starting at o. This is an alternate way
     * to create a value object graph from a JDO instance graph.
     * @see #lookupOrder(String)
     */
    private Serializable fetchAll(Serializable o) {
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(buf);
            os.writeObject(o);
            os.close();
            ByteArrayInputStream in = new ByteArrayInputStream(buf.toByteArray());
            ObjectInputStream is = new ObjectInputStream(in);
            return (Serializable)is.readObject();
        } catch (Exception e) {
            // should not be possible
            throw new RuntimeException(e.toString());
        }
    }

}

