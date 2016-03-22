
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
package com.versant.core.jdo.junit.test3.model;

/**
 * For testing SQL generated to fetch link table collections.
 */
public class OrderLine3 {

    private Order3 order;
    private String item;

    public OrderLine3(String item) {
        this.item = item;
    }

    public Order3 getOrder() {
        return order;
    }

    void setOrder(Order3 order) {
        this.order = order;
    }

    public String toString() {
        return item;
    }

}

