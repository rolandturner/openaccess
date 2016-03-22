
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

import java.util.*;
import java.io.Serializable;

public class Order implements Serializable {

    private Customer customer;
    private Date orderDate;

    /**
     * @associates <{tut1.model.OrderLine}>
     */
    private List lines = new ArrayList();

    public Order() {
    }

    public Order(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public List getLines() {
        return lines;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        line.setOrder(this);
    }

    public void removeLine(OrderLine line) {
        lines.remove(line);
        if (line.getOrder() == this) line.setOrder(null);
    }

    public void removeLines(){
        while(lines.size() > 0){
            OrderLine line = (OrderLine)lines.remove(0);
            line.setOrder(null);
        }
    }

}
