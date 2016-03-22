
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

import petstore.model.CartItem;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.text.NumberFormat;

/**
 */
public class CartVO implements Serializable {

    private Collection items;

    public CartVO(Collection items) {
        this.items = items;
    }

    public int getSize() {
        if (items != null) return items.size();
        else return 0;
    }


    /** @return an collection of all the CartItems. */
    public Collection getCart() {
        return items;
    }

    /** @return an iterator over all the CartItems. */
    public Iterator getItems() {
        return items.iterator();
    }

    public double getTotalCost() {
        double total = 0;
        for (Iterator li = getItems(); li.hasNext(); ) {
            CartItem item = (CartItem) li.next();
            total += item.getTotalCost();
        }
        return total;
    }

    public String getTotalCostString() {

        NumberFormat frm = NumberFormat.getCurrencyInstance();
        return frm.format(getTotalCost());
    }

    /**
     * copies over the data from the specified shopping cart. Note
     * that it is a shallow copy.
     */

    public void copy(CartVO src) {
        this.items = src.items;
    }

}
