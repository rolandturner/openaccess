
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

import tut1.model.*;

import javax.ejb.*;

/**
 * Remote interface for the Tut1 session bean.
 */
public interface Tut1 extends EJBObject {
    /**
     * Create the product catalog.
     */
    public void createCatalog() throws RemoteException;

    /**
     * Return all items in the catalog in description order.
     */
    public List listItems() throws RemoteException;

    /**
     * Lookup an item by code or return null if no item found.
     */
    public Item lookupItem(String code) throws RemoteException;

    /**
     * Create an order for a new customer.
     */
    public String createOrder(Order o) throws RemoteException;

    /**
     * Lookup an Order. Returns null if not found.
     */
    public Order lookupOrder(String orderNo) throws RemoteException;

}

