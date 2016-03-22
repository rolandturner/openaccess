
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
/*
* ====================================================================
*
* TORPEDO
* A Testbed of Object Relational Products for Enterprise Distributed Objects
* Copyright (c) 2004 The Middleware Company All Rights Reserved
* @author Bruce Martin
*
* ====================================================================
*/

package com.versant.core.jdo.junit.torpedo.model;

import java.util.Collection;

public class JDOBid {

    public String id=null;
    private JDOAuction auction=null;
    private JDOUser buyer=null;
    private Float amount=null;
    private Float maxAmount=null;

    public JDOBid() {

    }
    public JDOBid(String id, JDOAuction auction, JDOUser buyer, Float amount, Float maxAmount) {
        this.id=id;
        this.auction=(JDOAuction)auction;
        this.buyer=(JDOUser)buyer;
        this.amount=amount;
        this.maxAmount=maxAmount;

        Collection auctionsbids = auction.getBids();
        auctionsbids.add(this);
        ((JDOAuction)auction).setBids(auctionsbids);

        Collection buyersbids = buyer.getBids();
        buyersbids.add(this);
        ((JDOUser)buyer).setBids(buyersbids);
    }
    public String getId(){
        return this.id;
    }
    public void setAuction(JDOAuction auction){
        this.auction=(JDOAuction)auction;
    }
    public JDOAuction getAuction(){
        return this.auction;
    }
    public void setBuyer(JDOUser buyer){
        this.buyer=(JDOUser)buyer;
    }
    public JDOUser getBuyer(){
        return this.buyer;
    }
    public void setAmount(Float amount){
        this.amount=amount;
    }
    public Float getAmount(){
        return this.amount;
    }
    public void setMaxAmount(Float maxAmount){
        this.maxAmount=maxAmount;
    }
    public Float getMaxAmount(){
        return this.maxAmount;
    }
    public boolean equals (Object other) {
        String otherID;
        if ((!(other instanceof JDOBid)) || (other==null)) otherID="invalid";
        else otherID=((JDOBid)other).getId();
        System.err.println("JDOBid.equals() "+this.id+" equals "+otherID);
        if (other == this) return true;
        if (!(other instanceof JDOBid)) return false;
        return id == null
                  ? ((JDOBid)other).getId() == null
        : id.equals (((JDOBid)other).getId());
    }
    public int hashCode () {
        return id == null ? 0 : id.hashCode ();
    }
}
