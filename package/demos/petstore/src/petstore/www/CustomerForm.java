
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
package petstore.www;

import org.apache.struts.action.ActionForm;
import petstore.www.ContactForm;

/**
 */
public class CustomerForm extends ActionForm {

    private ContactForm contact = new ContactForm();

    private String cardNumber;
    private String cardType;
    private String expiryMonth;
    private String expiryYear;

    public ContactForm getContact() {
        return contact;
    }

    public void setContact(ContactForm contact) {
        this.contact = contact;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardType() {
        return (cardType == null) ? "0":cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getExpiryMonth() {
        return (expiryMonth == null) ? "":expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return (expiryYear == null) ? "": expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }
}
