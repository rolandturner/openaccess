
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
package com.versant.core.jdo.junit.test0.model;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 
 */
public class LItem {
    private int orderNumber; //pk
    private int itemNumber;  //pk
    private Od order;
    private String description;
    private BigDecimal price;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public Od getOrder() {
        return order;
    }

    public void setOrder(Od order) {
        this.order = order;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public static class ID implements Serializable {
        public int orderNumber;
        public int itemNumber;

        public ID(String id) {
            int index = id.indexOf("-");
            orderNumber = Integer.parseInt(id.substring(0, index));
            itemNumber = Integer.parseInt(id.substring(index + 1));
        }

        public ID() {
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID) o;

            if (itemNumber != id.itemNumber) return false;
            if (orderNumber != id.orderNumber) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = orderNumber;
            result = 29 * result + itemNumber;
            return result;
        }

        public String toString() {
            return "" + orderNumber + "-" + itemNumber;
        }
    }
}
