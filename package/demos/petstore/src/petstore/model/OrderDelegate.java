
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

import petstore.db.*;

import javax.jdo.PersistenceManager;
import java.util.Iterator;
import java.util.ArrayList;


public class OrderDelegate {

    private PersistenceManager pm;

    public OrderDelegate(PersistenceManager pm) {
        this.pm = pm;
    }

    public String createOrder(User user,
                            ContactInfo billingDetails,
                            ContactInfo shippingDetails,
                            CreditCard creditCard,
                            CartVO cart) {

        try {
            PurchaseOrder po = new PurchaseOrder(user);

            po.setPoValue(cart.getTotalCost());

            ArrayList lineItems = new ArrayList();
            Iterator itr = cart.getItems();
            int i=0;
            while(itr.hasNext()) {
                CartItem cartItem = (CartItem)itr.next();
                LineItem lineItem = new LineItem();
                lineItem.setLineNumber(i++);
                lineItem.setItem(cartItem.getItem());
                lineItem.setQuantity(cartItem.getQuantity());
                lineItems.add(lineItem);
            }

            po.setLineItems(lineItems);

            po.setBillingDetails(billingDetails);
            po.setShippingDetails(shippingDetails);

            po.setPoCard(creditCard);
            po.setPoDate(new java.util.Date());

            pm.makePersistent(po);
            pm.currentTransaction().commit();
            return pm.getObjectId(po).toString();
        } catch (Exception e) {
            try {pm.currentTransaction().rollback();} catch (Exception e1) {}
            throw ExceptionUtil.createSystemException(e);
        } finally {
            try {pm.currentTransaction().begin();} catch (Exception e1) {}
        }

    }

}
