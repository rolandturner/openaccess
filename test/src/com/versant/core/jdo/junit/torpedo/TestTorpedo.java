
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
package com.versant.core.jdo.junit.torpedo;

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.torpedo.model.*;
import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.VersantQuery;
import com.versant.core.logging.LogEvent;
import com.versant.core.logging.LogEvent;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests similar to the Torpedo benchmark to make sure we get optimal
 * results. The setup method parses the SQL insert script used by the
 * benchmark and creates the same data using JDO calls. Each testXXX
 * method corresponds to a test from the benchmark.
 */
public class TestTorpedo extends VersantTestCase {

    public TestTorpedo(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testListAuction",
            "testListAuctionTwiceWithTransaction",
            "testListAuctionTwiceNoTransaction",
            "testListPartialAuction",
            "testFindAllAuctions",
            "testFindHighBids",
            "testPlaceBid",
            "testPlaceBidOpt",
            "testPlace2Bids",
            "testPlace2BidsOpt",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestTorpedo(a[i]));
        }
        return s;
    }


    public void testListAuction() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        listAuctionImp(pm);
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 1);
        pm.close();
    }

    public void testListAuctionTwiceWithTransaction() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        listAuctionImp(pm);
        listAuctionImp(pm);
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 1);
        pm.close();
    }

    public void testListAuctionTwiceNoTransaction() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        listAuctionImp(pm);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        listAuctionImp(pm);
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 1);
        pm.close();
    }

    private void listAuctionImp(PersistenceManager pm) {
        Object oid = pm.newObjectIdInstance(JDOAuction.class, "3");
        JDOAuction o = (JDOAuction)pm.getObjectById(oid, false);
        new AuctionInfo(o, false);
    }

    private void checkHitsAndSql(int hits, int sqlCount) {
        LogEvent[] ea = getNewPerfEvents();
        StringBuffer out = new StringBuffer();
        int actualHits = countHits(ea, out);
        String[] a = findAllExecQuerySQL(ea);
        System.out.println("\n=== TORPEDO txt OUTPUT ===");
        System.out.println(out);
        assertEquals(hits, actualHits);
        assertEquals(sqlCount, a.length);
    }

    public void testListPartialAuction() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        Object oid = pm.newObjectIdInstance(JDOAuction.class, "3");
        JDOAuction o = (JDOAuction)pm.getObjectById(oid, false);
        ((VersantPersistenceManager)pm).loadFetchGroup((PersistenceCapable)o, "partial");
        new AuctionInfo(o, true);
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 1);
        pm.close();
    }

    public void testFindAllAuctions() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        Query q = pm.newQuery (JDOAuction.class);
        ((VersantQuery)q).setBounded(true);
        Collection auctions = (Collection) q.execute();
        for (Iterator i = auctions.iterator(); i.hasNext(); ) {
            JDOAuction au = (JDOAuction)i.next();
            new AuctionInfo(au, false);
        }
        q.closeAll();
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 1);
        pm.close();
    }

    public void testFindHighBids() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        Object oid = pm.newObjectIdInstance(JDOAuction.class, "3");
        JDOAuction theAuction = (JDOAuction)pm.getObjectById(oid, false);
        Collection allBids = theAuction.getBids();
        Collection highBids = new ArrayList();
        Iterator i = allBids.iterator();
        float highAmount = 0;
        while (i.hasNext()) {
            float bidAmount = ((JDOBid)i.next()).getAmount().floatValue();
            if (bidAmount > highAmount) highAmount = bidAmount;
        }
        // Have high amount -- now add any bids that match to collection
        i = allBids.iterator();
        while (i.hasNext()) {
            JDOBid nextBid = (JDOBid)i.next();
            float bidAmount = nextBid.getAmount().floatValue();
            if (bidAmount == highAmount) highBids.add(nextBid);
        }
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 1);
        pm.close();
    }

    public void testPlaceBid() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        JDOUser user = (JDOUser)pm.getObjectById(
                pm.newObjectIdInstance(JDOUser.class, "Bruce Martin"), false);
        JDOAuction auction = (JDOAuction)pm.getObjectById(
                pm.newObjectIdInstance(JDOAuction.class, "3"), false);
        JDOBid newBid = new JDOBid("25", auction, user, new Float(2500.0),
                new Float(9500.0));
        pm.makePersistent(newBid);
        pm.currentTransaction().commit();
        checkHitsAndSql(4, 2);
        pm.close();
    }

    /**
     * This does not complete the one-to-many collections on JDOUser and
     * JDOAuction when creating a new bid. This is the same trick WebLogic
     * uses to get top spot. The buyer and auction are never read!
     */
    public void testPlaceBidOpt() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        JDOUser user = (JDOUser)pm.getObjectById(
                pm.newObjectIdInstance(JDOUser.class, "Bruce Martin"), false);
        JDOAuction auction = (JDOAuction)pm.getObjectById(
                pm.newObjectIdInstance(JDOAuction.class, "3"), false);
        JDOBid newBid = new JDOBid();
        newBid.id = "25";
        newBid.setAuction(auction);
        newBid.setBuyer(user);
        newBid.setAmount(new Float(2500.0));
        newBid.setMaxAmount(new Float(9500.0));
        pm.makePersistent(newBid);
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 0);
        pm.close();
    }

    public void testPlace2Bids() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        JDOUser user = (JDOUser)pm.getObjectById(
                pm.newObjectIdInstance(JDOUser.class, "Mark Smith"), false);
        JDOAuction auction = (JDOAuction)pm.getObjectById(
                pm.newObjectIdInstance(JDOAuction.class, "1"), false);
        JDOBid newBid = new JDOBid("26", auction, user,
                new Float(650000.0),
                new Float(750000.0));
        pm.makePersistent(newBid);
        JDOAuction auction2 = (JDOAuction)pm.getObjectById(
                pm.newObjectIdInstance(JDOAuction.class, "2"), false);
        JDOBid newBid2 = new JDOBid("27", auction2, user,
                new Float(500.1),
                new Float(650.75));
        pm.makePersistent(newBid2);
        pm.currentTransaction().commit();
        checkHitsAndSql(5, 3);
        pm.close();
    }

    /**
     * This does not complete the one-to-many collections on JDOUser and
     * JDOAuction when creating a new bid. This is the same trick WebLogic
     * uses to get top spot. The buyer and auction are never read!
     */
    public void testPlace2BidsOpt() {
        if (!isJdbc()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        findExecQuerySQL();
        pm.currentTransaction().begin();
        JDOUser user = (JDOUser)pm.getObjectById(
                pm.newObjectIdInstance(JDOUser.class, "Mark Smith"), false);
        JDOAuction auction = (JDOAuction)pm.getObjectById(
                pm.newObjectIdInstance(JDOAuction.class, "1"), false);
        JDOBid newBid = new JDOBid();
        newBid.id = "26";
        newBid.setAuction(auction);
        newBid.setBuyer(user);
        newBid.setAmount(new Float(650000.0));
        newBid.setMaxAmount(new Float(750000.0));
        pm.makePersistent(newBid);
        JDOAuction auction2 = (JDOAuction)pm.getObjectById(
                pm.newObjectIdInstance(JDOAuction.class, "2"), false);
        JDOBid newBid2 = new JDOBid();
        newBid2.id = "27";
        newBid2.setAuction(auction2);
        newBid2.setBuyer(user);
        newBid2.setAmount(new Float(500.1));
        newBid2.setMaxAmount(new Float(650.75));
        pm.makePersistent(newBid2);
        pm.currentTransaction().commit();
        checkHitsAndSql(2, 0);
        pm.close();
    }

    private static final String INSERT_INTO = "INSERT INTO ";

    /**
     * Create the model shared by all tests. This is the same as what is
     * included in the script that comes with the benchmark for Hypersonic.
     * This is done by parsing the script and persisting matching instances.
     */
    protected void setUp() throws Exception {
        super.setUp();
        InputStreamReader ins = new InputStreamReader(
                getClass().getResourceAsStream("test.initial.script"));
        BufferedReader in = new BufferedReader(ins);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        HashMap users = new HashMap();
        HashMap items = new HashMap();
        HashMap auctions = new HashMap();
        for (;;) {
            String line = in.readLine();
            if (line == null) break;
            if (!line.startsWith(INSERT_INTO)) continue;
            line = line.substring(INSERT_INTO.length());
            int i = line.indexOf(' ');
            String table = line.substring(0, i);
            String[] a = parse(line.substring(i + "VALUES(".length() + 1,
                    line.length() - 1));
            if ("AUCTION_USER".equals(table)) {
                JDOUser u = new JDOUser();
                u.id = a[1];
                pm.makePersistent(u);
                users.put(u.id, u);
            } else if ("ITEM".equals(table)) {
                JDOItem it = new JDOItem();
                it.id = a[1];
                it.setItemName(a[2]);
                it.setGraphicFilename(a[3]);
                it.setDescription(a[4]);
                pm.makePersistent(it);
                items.put(it.id, it);
            } else if ("AUCTION".equals(table)) {
                JDOAuction au = new JDOAuction();
                au.setSeller((JDOUser)users.get(a[1]));
                au.setItem((JDOItem)items.get(a[2]));
                au.setLowPrice(new Float(a[3]));
                au.setId(a[4]);
                pm.makePersistent(au);
                auctions.put(au.getId(), au);
            } else if ("BID".equals(table)) {
                JDOBid bid = new JDOBid();
                bid.id = a[1];
                bid.setAuction((JDOAuction)auctions.get(a[2]));
                bid.setBuyer((JDOUser)users.get(a[3]));
                bid.setAmount(new Float(a[4]));
                bid.setMaxAmount(new Float(a[5]));
                pm.makePersistent(bid);
            } else {
                fail("Unknown table: " + table);
            }
        }
        in.close();
        pm.currentTransaction().commit();
        pmf().evictAll();
        pmf().logEvent(VersantPersistenceManagerFactory.EVENT_NORMAL,
                "=== Finished inserting model data", 0);
    }

    private String[] parse(String s) {
        ArrayList a = new ArrayList();
        int i = 0;
        for (;;) {
            int j = s.indexOf(',', i);
            String w = j >= 0 ? s.substring(i, j) : s.substring(i);
            if (w.charAt(0) == '\'' && w.charAt(w.length() - 1) == '\'') {
                w = w.substring(1, w.length() - 1);
            }
            a.add(w);
            if (j < 0) break;
            i = j + 1;
        }
        String[] ans = new String[a.size()];
        a.toArray(ans);
        return ans;
    }

}

