
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

import java.util.ArrayList;
import java.io.Serializable;

/**
 * 
 */
public class Od {
    private int orderNumber; //pk
    private ArrayList lineItems;

    public ArrayList getLineItems() {
        return lineItems;
    }

    public void setLineItems(ArrayList lineItems) {
        this.lineItems = lineItems;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public static class ID implements Serializable {
        public int orderNumber;

        public ID(String id) {
            this.orderNumber = Integer.parseInt(id);
        }

        public ID() {
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID) o;

            if (orderNumber != id.orderNumber) return false;

            return true;
        }

        public int hashCode() {
            return orderNumber;
        }

        public String toString() {
            return "" + orderNumber;
        }
    }
}
