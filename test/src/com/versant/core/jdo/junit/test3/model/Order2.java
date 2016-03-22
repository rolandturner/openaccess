
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

import java.util.ArrayList;
import java.util.List;

/**
 * For testing SQL generated to fetch inverse fk collections.
 */
public class Order2 {

    private String name;
    private Customer customer;  // dfg=true
    private List lines = new ArrayList(); // inverse OrderLine2.order

    public Order2() {
    }

    public Order2(String name, Customer customer) {
        this.name = name;
        this.customer = customer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List getLines() {
        return lines;
    }

    public void add(OrderLine2 line) {
        lines.add(line);
        line.setOrder(this);
    }

    public String toString() {
        return name + " " + customer + " " + lines;
    }

}

