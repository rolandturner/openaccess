
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
package com.versant.core.jdo.junit.ejbql.model;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;

/**
 * Model for EJB 3 spec examples.
 */
public class Order implements Comparable {

    private String customer;
    private Date orderDate = new Date();
    private Set lineItems = new HashSet();
    private ShippingAddress shippingAddress;
    private BillingAddress billingAddress;

    public Order() {
    }

    public Order(String customer) {
        this.customer = customer;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Set getLineItems() {
        return lineItems;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    /**
     * Order by customer.
     */
    public int compareTo(Object o) {
        return customer.compareTo(((Order)o).customer);
    }

}

