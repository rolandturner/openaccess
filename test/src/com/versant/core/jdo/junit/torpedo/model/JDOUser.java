
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

public class JDOUser {
    public String id=null;
    private Collection bids=null;
    private Collection auctions=null;

    public String getId(){
        return this.id;
    }
    public void setBids(Collection bids){
        this.bids=bids;
    }
    public Collection getBids(){
        return this.bids;
    }
    public void setAuctions(Collection auctions){
        this.auctions=auctions;
    }
    public Collection getAuctions(){
        return this.auctions;
    }
    public boolean equals (Object other) {
        String otherID;
        if ((!(other instanceof JDOUser)) || (other==null)) otherID="invalid";
        else otherID=((JDOUser)other).getId();
        System.err.println("JDOUser.equals() "+this.id+" equals "+otherID);
        if (other == this) return true;
        if (!(other instanceof JDOUser)) return false;
        return id == null
                  ? ((JDOUser)other).getId() == null
        : id.equals (((JDOUser)other).getId());
    }
    public int hashCode () {
        return id == null ? 0 : id.hashCode ();
    }
}
