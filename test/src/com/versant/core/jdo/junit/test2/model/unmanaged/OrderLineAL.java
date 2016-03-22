
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
package com.versant.core.jdo.junit.test2.model.unmanaged;

/**
 * Many side of unmanaged one-to-many.
 */
public class OrderLineAL {

    private OrderAL order;
    private int qty;

    public OrderLineAL(int qty) {
        this.qty = qty;
    }

    public OrderAL getOrder() {
        return order;
    }

    public void setOrder(OrderAL order) {
        this.order = order;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String toString() {
        return Integer.toString(qty);
    }

}

