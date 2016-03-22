
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

import petstore.model.CatalogDelegate;
import petstore.model.CartDelegate;
import petstore.model.CustomerDelegate;
import petstore.model.OrderDelegate;
import petstore.db.PurchaseOrder;

import javax.jdo.PersistenceManager;

/**
 */
public interface Constants {

    public static final String CATALOG_KEY = CatalogDelegate.class.getName();
    public static final String CART_KEY = CartDelegate.class.getName();
    public static final String CUSTOMER_KEY = CustomerDelegate.class.getName();
    public static final String ORDER_KEY = OrderDelegate.class.getName();
    public static final String PM_KEY = PersistenceManager.class.getName();
    public static final String PURCHASE_ID = PurchaseOrder.class.getName();

    public static final String ITEM_PRM = "item";

}
