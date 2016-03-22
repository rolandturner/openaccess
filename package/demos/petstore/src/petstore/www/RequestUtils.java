
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
package petstore.www;

import petstore.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.jdo.PersistenceManager;
import java.util.ArrayList;

import org.apache.struts.util.LabelValueBean;

/**
 */
public class RequestUtils {

    public static void setCreditCards(HttpServletRequest request) {
        ArrayList l = new ArrayList();
        l.add(new LabelValueBean("--Select Card--", "0"));
        l.add(new LabelValueBean("Duke Express", "1"));
        l.add(new LabelValueBean("Java (TM) Card", "2"));
        l.add(new LabelValueBean("Meow Card", "3"));
        request.setAttribute("creditCards", l);
    }


    public static void setCountries(HttpServletRequest request) {
        ArrayList l = new ArrayList();
        l.add(new LabelValueBean("--Select Country--", "0"));
        l.add(new LabelValueBean("South Africa", "1"));
        l.add(new LabelValueBean("Russia", "2"));
        l.add(new LabelValueBean("USA", "3"));
        request.setAttribute("countries", l);
    }

    /**
     * Get the PM stored in the request. If there is none one is created
     * and cached and a tx is started.
     */
    public static PersistenceManager getPersistenceManager(HttpServletRequest request) {
        PersistenceManager pm = (PersistenceManager) request.getAttribute(Constants.PM_KEY);
        if (pm == null) {
            pm = JDOSupport.getInstance().getPMFactory().getPersistenceManager();
            pm.currentTransaction().begin();
            request.setAttribute(Constants.PM_KEY, pm);
            pm.setUserObject(request.getRemoteHost());
        }
        return pm;
    }

    /**
     * Close the PM stored in the request. This gets called from the end of
     * main_template.jsp to make sure the PM is closed. If the PM is not closed
     * JDO Genie will close it when it is garbaged collected but this may not
     * work with other JDO implementations.
     */
    public static void closePersistenceManager(HttpServletRequest request) {
        PersistenceManager pm = (PersistenceManager) request.getAttribute(Constants.PM_KEY);
        if (pm == null) return;
        pm.close();
        request.removeAttribute(Constants.PM_KEY);
    }

    public static CatalogDelegate getCatalog(HttpServletRequest request) {
        CatalogDelegate catalog = new CatalogDelegate(getPersistenceManager(request));
        return catalog;
    }



    public static CartDelegate getCart(HttpServletRequest request) {
       HttpSession session = request.getSession();
        CartDelegate cart = (CartDelegate) session.getAttribute(Constants.CART_KEY);
        if (cart == null) {
            cart = new CartDelegate(getCatalog(request));
            session.setAttribute(Constants.CART_KEY, cart);
        } else {
            cart.setCatalogDelegate(getCatalog(request));
        }
        return cart;
    }


    public static CustomerDelegate getCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession();
        CustomerDelegate customer = (CustomerDelegate) session.getAttribute(Constants.CUSTOMER_KEY);
        if (customer == null) {
            customer = new CustomerDelegate(getPersistenceManager(request));
            session.setAttribute(Constants.CUSTOMER_KEY, customer);
        } else {
            customer.setPersistenceManager(getPersistenceManager(request));
        }
        return customer;
    }


    public static OrderDelegate getOrderDelegate(HttpServletRequest request) {
        OrderDelegate ordDel = new OrderDelegate(getPersistenceManager(request));
        return ordDel;
    }

}
