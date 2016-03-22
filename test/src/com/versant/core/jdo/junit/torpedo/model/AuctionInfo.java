
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
package com.versant.core.jdo.junit.torpedo.model;

import java.util.Collection;
import java.util.ArrayList;

public class AuctionInfo {
    protected String description;
    protected Float lowPrice;
    protected Collection bids;
    protected String sellerName;
    protected String itemName;
    protected String itemGraphicFilename;
    protected String itemID;
    protected String auctionID;
    private String sql;
    public AuctionInfo(JDOAuction auction, boolean partial) {
        lowPrice = auction.getLowPrice();
        auctionID = auction.getId();
        JDOItem i = auction.getItem();
        if (i!=null) itemName=i.getItemName();
        if (partial) {
            // not used
            bids = new ArrayList();
            itemGraphicFilename="not needed";
            sellerName = "not needed";
            itemID = "not needed";
            description = "not needed";
        } else {
            bids = BidInfo.bids(auction.getBids());
            JDOUser u = auction.getSeller();
            if (u!=null) {
                sellerName = u.getId();
            }
            if (i!=null) {
                description = i.getDescription();
                itemID = i.getId();
                itemGraphicFilename = i.getGraphicFilename();
            }
        }
    }
    public String getDescription() {
        return description;
    }
    public Float getLowPrice() {
        return lowPrice;
    }
    public java.util.Collection getBids() {
        return bids;
    }
    public String getSellerName() {
        return sellerName;
    }
    public String getItemName() {
        return itemName;
    }
    public String getItemID() {
        return itemID;
    }
    public String getItemGraphicFilename() {
        return itemGraphicFilename;
    }
    public String getAuctionID() {
        return auctionID;
    }
    public String getSQL() {
        return sql;
    }
    public void setSQL(String sqlResult) {
        sql = sqlResult;
    }
}

