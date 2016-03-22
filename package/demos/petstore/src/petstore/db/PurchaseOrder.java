
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
package petstore.db;

import petstore.db.CreditCard;
import petstore.db.ContactInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 */
public class PurchaseOrder implements Serializable {


    private User user;

    private Date poDate;
    private double poValue;
    private String poStatus;
    private CreditCard poCard;

    private List lineItems;

    private ContactInfo billingDetails;

    private ContactInfo shippingDetails;

    public PurchaseOrder() {
        super();
    }

    public PurchaseOrder(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Date getPoDate() {
        return poDate;
    }

    public void setPoDate(Date poDate) {
        this.poDate = poDate;
    }

    public double getPoValue() {
        return poValue;
    }

    public void setPoValue(double poValue) {
        this.poValue = poValue;
    }

    public String getPoStatus() {
        return poStatus;
    }

    public void setPoStatus(String poStatus) {
        this.poStatus = poStatus;
    }


    public CreditCard getPoCard() {
        return poCard;
    }

    public void setPoCard(CreditCard poCard) {
        this.poCard = poCard;
    }

    public List getLineItems() {
        return lineItems;
    }

    public void setLineItems(List lineItems) {
        this.lineItems = lineItems;
    }

    public ContactInfo getBillingDetails() {
        return billingDetails;
    }

    public void setBillingDetails(ContactInfo billingDetails) {
        this.billingDetails = billingDetails;
    }

    public ContactInfo getShippingDetails() {
        return shippingDetails;
    }

    public void setShippingDetails(ContactInfo shippingDetails) {
        this.shippingDetails = shippingDetails;
    }
}

