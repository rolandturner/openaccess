
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
package com.versant.core.jdo.junit.test3.model.attachdetach.externalizer;

public class CreditCard {
    private String cardHolderName;
    private CardTypes cardType = CardTypes.AmericanExpress;
    private String cardNumber;
    private String authorisationDigits;
    private int expiryYear;
    private int expiryMonth = 6;//Months.Six;

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public CardTypes getCardType() {
        return cardType;
    }

    public void setCardType(CardTypes cardType) {
        this.cardType = cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getAuthorisationDigits() {
        return authorisationDigits;
    }

    public void setAuthorisationDigits(String authorisationDigits) {
        this.authorisationDigits = authorisationDigits;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
}
