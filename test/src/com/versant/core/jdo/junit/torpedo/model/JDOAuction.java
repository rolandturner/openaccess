
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

public class JDOAuction {

    private Collection bids = null;
    private JDOUser seller = null;
    private JDOItem item = null;
    private Float lowPrice = null;
    private String id = null;

    public void setBids(Collection bids){
        this.bids=bids;
    }
    public Collection getBids(){
        return this.bids;
    }
    public void setSeller(JDOUser seller){
        this.seller= seller;
    }
    public JDOUser getSeller(){
        return this.seller;
    }
    public void setItem(JDOItem item){
        this.item=item;
    }
    public JDOItem getItem(){
        return this.item;
    }
    public void setLowPrice(Float lowPrice){
        this.lowPrice=lowPrice;
    }
    public Float getLowPrice(){
        return this.lowPrice;
    }
    public void setId(String id){
        this.id=id;
    }
    public String getId(){
        return this.id;
    }
    public boolean equals (Object other) {
        String otherID;
        if ((!(other instanceof JDOAuction)) || (other==null)) otherID="invalid";
        else otherID=((JDOAuction)other).getId();
        System.err.println("Auction.equals() "+this.id+" equals "+otherID);
        if (other == this) return true;
        if (!(other instanceof JDOAuction)) return false;
        return id == null
                      ? ((JDOAuction)other).getId() == null
        : id.equals (((JDOAuction)other).getId());
    }
    public int hashCode () {
        return id == null ? 0 : id.hashCode ();
    }

}
