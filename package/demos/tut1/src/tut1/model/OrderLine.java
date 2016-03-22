
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
package tut1.model;

import tut1.model.Item;
import tut1.model.Order;

public class OrderLine {

    private Order order; // set automatically when line is added to Order
    private Item item;
    private int qty;

    public OrderLine() {
    }

    public OrderLine(Item item, int qty) {
        this.item = item;
        this.qty = qty;
    }

    public Order getOrder() {
        return order;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(item == null ? "<ITEM NOT SET>" : item.toString());
        sb.append(" - Qty: ").append(qty);
        return sb.toString();
    }
}
