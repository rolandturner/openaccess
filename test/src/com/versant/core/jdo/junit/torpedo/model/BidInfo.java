
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
import java.util.Iterator;

public class BidInfo {

    protected Float bidAmount;
    protected Float maxBidAmount;
    protected String buyer;
    public BidInfo(JDOBid b) {
        if (b!=null) {
            bidAmount = b.getAmount();
            maxBidAmount = b.getMaxAmount();
            JDOUser u = b.getBuyer();
            if (u!=null) {
                buyer = u.getId();
            }
        }
    }
    static Collection bids(Collection bids) {
        ArrayList bidInfos = new ArrayList();
        if (bids!=null) {
            Iterator b = bids.iterator();
            while (b.hasNext()) bidInfos.add(new BidInfo((JDOBid)b.next()));
        }
        return bidInfos;
    }
    public Float getBidAmount() {
        return bidAmount;
    }
    public String getBuyer() {
        return buyer;
    }

}

