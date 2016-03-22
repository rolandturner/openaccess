
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
package petstore.model;

import petstore.db.Customer;
import petstore.db.User;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import java.util.Collection;
import java.util.Iterator;

/**
 */
public class CustomerDelegate {

    private transient PersistenceManager pm;

    private String login;
    private String password;

    public CustomerDelegate(PersistenceManager pm) {
        this.pm = pm;
    }

    public void setPersistenceManager(PersistenceManager pm) {
        this.pm = pm;
    }

    public User getUser() {
        User user = null;
        Query q = null;
        try {
            q = pm.newQuery();
            q.setClass(User.class);
            //TODO Replace with full user query.
            q.declareParameters("String id");
            q.setFilter("login == id");
            q.setIgnoreCache(true);
            Collection col = (Collection) q.execute(login);

            Iterator iter = col.iterator();
            while (iter.hasNext()) {
                User candidate = (User) iter.next();
                if (candidate.getPassword().equals(password)) {
                    user = candidate;
                    break;
                }
            }
        } finally {
            if (q!= null) try {q.closeAll();} catch (Exception e) {}
        }
        return user;
    }

    public Customer getCustomer() {
        Customer customer = null;
        Query q = null;
        try {
            q = pm.newQuery();
            q.setClass(Customer.class);
            q.declareParameters("Object usr");
            q.setFilter("user == usr");
            q.setIgnoreCache(true);
            Collection col = (Collection) q.execute(getUser());
            Iterator iter = col.iterator();

            if (iter.hasNext()) {
                customer = (Customer) iter.next();
            }
        } finally {
            if (q!= null) try {q.closeAll();} catch (Exception e) {}
        }
        return customer;
    }

    public void createUserCustomer(String login, String password) {
        try {
            this.login = login;
            this.password = password;
            User user = new User(login, password);
            pm.makePersistent(user);
            Customer customer = new Customer(user);
            pm.makePersistent(customer);
            pm.currentTransaction().commit();
        } catch (Exception e) {
            try {pm.currentTransaction().rollback();} catch (Exception e1) {}
            throw ExceptionUtil.createSystemException(e);
        } finally {
            try {pm.currentTransaction().begin();} catch (Exception e1) {}
        }
    }

    public void setUserCustomer(String login, String password) {

        this.login = login;
        this.password = password;

    }

    public void saveCustomer(Customer c) {
        try {
            pm.currentTransaction().commit();
        } catch (Exception e) {
            try {pm.currentTransaction().rollback();} catch (Exception e1) {}
            throw ExceptionUtil.createSystemException(e);
        } finally {
            try {pm.currentTransaction().begin();} catch (Exception e1) {}
        }
    }

}
