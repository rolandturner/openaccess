
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

public class JDOItem {

    public String id=null;
    private JDOAuction auction=null;
    private String itemName=null;
    private String graphicFilename=null;
    private String description=null;

    public String getId(){
        return this.id;
    }
    public void setAuction(JDOAuction auction){
        this.auction=(JDOAuction)auction;
    }
    public JDOAuction getAuction(){
         return this.auction;
    }
    public void setItemName(String itemName){
        this.itemName=itemName;
    }
    public String getItemName(){
        return this.itemName;
    }
    public void setGraphicFilename(String graphicFilename){
        this.graphicFilename=graphicFilename;
    }
    public String getGraphicFilename(){
        return this.graphicFilename;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public String getDescription(){
        return this.description;
    }
    public boolean equals (Object other) {
        String otherID;
        if ((!(other instanceof JDOItem)) || (other==null)) otherID="invalid";
        else otherID=((JDOItem)other).getId();
        System.err.println("Item.equals() "+this.id+" equals "+otherID);
        if (other == this) return true;
        if (!(other instanceof JDOItem)) return false;
        return id == null
                  ? ((JDOItem)other).getId() == null
        : id.equals (((JDOItem)other).getId());
    }
    public int hashCode () {
        return id == null ? 0 : id.hashCode ();
    }
}
