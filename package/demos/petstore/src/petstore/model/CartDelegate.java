
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

import petstore.model.CatalogDelegate;
import petstore.db.Item;

import java.util.*;


/**
 */
public class CartDelegate {

    private HashMap cart;
    private transient CatalogDelegate catalog;

    public CartDelegate(CatalogDelegate catalog) {
        cart = new HashMap();
        this.catalog = catalog;
    }

    public void setCatalogDelegate(CatalogDelegate catalog) {
        this.catalog = catalog;
    }

    public Collection getItems() {

        ArrayList items = new ArrayList();
        Iterator it = cart.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Integer value = (Integer) cart.get(key);
            Item item = null;

            item = catalog.getItem(key);
            // convert catalog item to cart item
            CartItem ci = new CartItem(getStringObjectID(item), item, value.intValue());

            items.add(ci);

        }
        return items;
    }

    public String getStringObjectID(Object object) {
        return catalog.getStringObjectID(object);
    }

    public CartVO getCart() {
        return new CartVO(getItems());
    }

    public void addItem(String itemCode, int qty) {
        cart.put(itemCode, new Integer(qty));
    }

    public void deleteItem(String itemID) {
        cart.remove(itemID);
    }

    public void updateItemQuantity(String itemID, int newQty) {
        cart.remove(itemID);
        // remove item if it is less than or equal to 0
        if (newQty > 0) cart.put(itemID, new Integer(newQty));
    }

    public void empty() {
        cart.clear();
    }

}
