
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
import java.text.SimpleDateFormat;

public class Order {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm");

    private Customer customer;
    private Date orderDate;
    private List lines = new ArrayList(); // inverse OrderLine.order

    public Order() {
    }

    public Order(Customer customer) {
        this.customer = customer;
        orderDate = new Date();
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
        return Collections.unmodifiableList(lines);
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        line.setOrder(this);
    }

    public void removeLine(OrderLine line) {
        if(lines.remove(line)){
            if (line.getOrder() == this) line.setOrder(null);
        }
    }

    /**
     * @return <OUR ORDER DATE dd MM yyyy> - Customer: <OUR CUSTOMER>
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(orderDate == null ? "<DATE NOT SET>" : sdf.format(orderDate));
        sb.append(" - Customer: ").append(customer);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;

        final Order order = (Order) o;

        if (customer != null ? !customer.equals(order.customer) : order.customer != null) return false;
        if (orderDate != null ? !orderDate.equals(order.orderDate) : order.orderDate != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (customer != null ? customer.hashCode() : 0);
        result = 29 * result + (orderDate != null ? orderDate.hashCode() : 0);
        return result;
    }
}
