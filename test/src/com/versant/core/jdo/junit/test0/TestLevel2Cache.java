
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
package com.versant.core.jdo.junit.test0;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.common.OID;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.Address;
import com.versant.core.jdo.junit.test0.model.Entry;

import javax.jdo.PersistenceManager;
import java.util.Arrays;

/**
 * Tests for the level 2 (PMF wide) cache and the cache listener.
 */
public class TestLevel2Cache extends VersantTestCase {

    public TestLevel2Cache(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testEvictOIDOptimistic",
            "testEvictOIDDatastore",
            "testEvictOIDsMixedOptimistic",
            "testEvictOIDsMixedDatastore",
            "testEvictClassesOptimistic",
            "testEvictClassesDatastore",
            "testEvictAll",
            "testExternalControl",
            "testEvictFromL2CacheAfterCommit",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestLevel2Cache(a[i]));
        }
        return s;
    }

    /**
     * Check that the post commit eviction methods on PM work.
     */
    public void testEvictFromL2CacheAfterCommit() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Address addr = new Address("piggy");
        pm.makePersistent(addr);
        Address addr2 = new Address("piggy2");
        pm.makePersistent(addr2);
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(addr);
        Object oid2 = pm.getObjectId(addr2);

        // evict object
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictFromL2CacheAfterCommit(addr);
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        // evict 2 objects separately
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        addr2.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        Assert.assertTrue(pmf().isInCache(oid2));
        pm.evictFromL2CacheAfterCommit(addr);
        pm.evictFromL2CacheAfterCommit(addr2);
        Assert.assertTrue(pmf().isInCache(oid));
        Assert.assertTrue(pmf().isInCache(oid2));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));
        Assert.assertTrue(!pmf().isInCache(oid2));

        // evict OID
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictFromL2CacheAfterCommit(oid);
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        // evict object[]
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictAllFromL2CacheAfterCommit(new Object[]{addr});
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        // evict OID[]
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictAllFromL2CacheAfterCommit(new Object[]{oid});
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        // evict Collection of object
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictAllFromL2CacheAfterCommit(Arrays.asList(new Object[]{addr}));
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        // evict Collection of OID
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictAllFromL2CacheAfterCommit(Arrays.asList(new Object[]{oid}));
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        // evictAll
        pm.currentTransaction().begin();
        addr.getStreet(); // load into L2 cache
        Assert.assertTrue(pmf().isInCache(oid));
        pm.evictAllFromL2CacheAfterCommit();
        Assert.assertTrue(pmf().isInCache(oid));
        pm.currentTransaction().commit();
        Assert.assertTrue(!pmf().isInCache(oid));

        pm.close();
    }

    /**
     * Check that notification of OID eviction happens both automatically
     * after commit and when manually evicted for an optimistic tx. Also
     * checks that the OID ends up in the PMF wide cache when it should.
     */
    public void testEvictOIDOptimistic() throws Exception {
        testEvictOIDImp(true);
    }

    /**
     * Check that notification of OID eviction happens both automatically
     * after commit and when manually evicted for a datastore tx. Also
     * checks that the OID ends up in the PMF wide cache when it should.
     */
    public void testEvictOIDDatastore() throws Exception {
        testEvictOIDImp(false);
    }

    private void testEvictOIDImp(boolean optimistic) {
        CacheListenerForTests ct = CacheListenerForTests.instance;
        if (ct == null) return; // remote

        PersistenceManager pm = pmf().getPersistenceManager();
        ct.setEnabled(true);

        // create new
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        Address addr = new Address("piggy");
        pm.makePersistent(addr);
        Object oid = pm.getObjectId(addr);
        pm.currentTransaction().commit();
        Assert.assertEquals("", ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid));

        // update
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        addr = (Address)pm.getObjectById(oid, true);
        addr.setStreet("oink");
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid));
        }
        pm.currentTransaction().commit();
        Assert.assertEquals("en " + oid, ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid));

        // manual eviction of something not in cache
        pmf().evict(oid);
        Assert.assertEquals("e " + oid, ct.getLog());

        // get instance into cache again (if optimistic)
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        addr = (Address)pm.getObjectById(oid, true);
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid));
        }

        // manual eviction of something in cache (if optimistic)
        pmf().evict(oid);
        Assert.assertEquals("e " + oid, ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid));

        // delete
        pm.deletePersistent(addr);
        pm.currentTransaction().commit();
        Assert.assertEquals("en " + oid, ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid));

        pm.close();
        ct.setEnabled(false);
    }

    /**
     * Test eviction of a mixed set of new, modified and deleted instances
     * with optimistic tx.
     */
    public void testEvictOIDsMixedOptimistic() {
        testEvictOIDsMixedImp(true);
    }

    /**
     * Test eviction of a mixed set of new, modified and deleted instances
     * with datastore tx.
     */
    public void testEvictOIDsMixedDatastore() {
        testEvictOIDsMixedImp(false);
    }

    private Object makeAddr(PersistenceManager pm, String street) {
        Address addr = new Address(street);
        pm.makePersistent(addr);
        return pm.getObjectId(addr);
    }

    private void updateAddr(PersistenceManager pm, Object oid, String street) {
        Address addr = (Address)pm.getObjectById(oid, true);
        addr.setStreet(street);
    }

    private void deleteAddr(PersistenceManager pm, Object oid) {
        Address addr = (Address)pm.getObjectById(oid, true);
        pm.deletePersistent(addr);
    }

    /**
     * Create a sorted String of the toStrings of oids space separated. This
     * matches the order used by CacheListenerForTests.
     */
    private String sort(Object[] oids) {
        int n = oids.length;
        String[] a = new String[n];
        for (int i = 0; i < n; i++) a[i] = oids[i].toString();
        Arrays.sort(a);
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(' ');
            s.append(a[i]);
        }
        return s.toString();
    }

    private String sort(Object oid1, Object oid2) {
        return sort(new Object[]{oid1, oid2});
    }

    private String sort(Object oid1, Object oid2, Object oid3) {
        return sort(new Object[]{oid1, oid2, oid3});
    }

    private void testEvictOIDsMixedImp(boolean optimistic) {
        CacheListenerForTests ct = CacheListenerForTests.instance;
        if (ct == null) return; // remote
        PersistenceManager pm = pmf().getPersistenceManager();
        ct.setEnabled(true);

        // create 2 new
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        Object oid1 = makeAddr(pm, "piggy1");
        Object oid2 = makeAddr(pm, "piggy2");
        pm.currentTransaction().commit();
        Assert.assertEquals("", ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));

        // create 1 new and update 2
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        updateAddr(pm, oid1, "oink1");
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid1));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid1));
        }
        updateAddr(pm, oid2, "oink2");
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid2));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid2));
        }
        Object oid3 = makeAddr(pm, "piggy3");
        pm.currentTransaction().commit();
        Assert.assertEquals("en " + sort(oid1, oid2), ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(!pmf().isInCache(oid3));

        // manual eviction of things not in cache
        pmf().evictAll(new Object[]{oid1, oid2});
        Assert.assertEquals("en " + sort(oid1, oid2), ct.getLog());

        // update 1 and delete 2
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        deleteAddr(pm, oid3);
        deleteAddr(pm, oid2);
        updateAddr(pm, oid1, "oinker");
        pm.currentTransaction().commit();
        Assert.assertEquals("en " + sort(oid1, oid3, oid2), ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(!pmf().isInCache(oid3));

        // delete the last one
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        pm.deletePersistent(pm.getObjectById(oid1, true));
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid1));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid1));
        }
        pm.currentTransaction().commit();
        Assert.assertEquals("en " + oid1, ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid1));

        pm.close();
        ct.setEnabled(false);
    }

    /**
     * Test eviction of classes using a manual flush and the PMF eviction
     * APIs with optimistic tx.
     */
    public void testEvictClassesOptimistic() {
        testEvictClassesImp(true);
    }

    /**
     * Test eviction of classes using a manual flush and the PMF eviction
     * APIs with datastore tx.
     */
    public void testEvictClassesDatastore() {
        testEvictClassesImp(false);
    }

    private Object makeEntry(PersistenceManager pm, String name) {
        Entry e = new Entry(name, 1);
        pm.makePersistent(e);
        return pm.getObjectId(e);
    }

    private void testEvictClassesImp(boolean optimistic) {
        CacheListenerForTests ct = CacheListenerForTests.instance;
        if (ct == null) return; // remote
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        ct.setEnabled(true);

        // create 2 new Address'es and 1 new Entry
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        Object oid1 = makeAddr(pm, "addr1");
        Object oid2 = makeAddr(pm, "addr2");
        Object oid3 = makeEntry(pm, "entry3");
        pm.currentTransaction().commit();
        Assert.assertEquals("", ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(!pmf().isInCache(oid3));

        // modify addr, do a manual flush and check that both are evicted
        // on commit
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        updateAddr(pm, oid1, "addr1x");
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid1));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid1));
        }
        pm.getObjectById(oid3, true); // put oid3 in cache
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid3));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid3));
        }

        pm.flush();
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid1));
            Assert.assertTrue(pmf().isInCache(oid3));
        }

        pm.getObjectById(oid2, true); // cannot go in in cache after flush
        Assert.assertTrue(!pmf().isInCache(oid2));
        ct.clearLog();
        pm.currentTransaction().commit();

        if (optimistic) {
            // all the addresses should have been evicted
            Assert.assertTrue(!pmf().isInCache(oid1));
            Assert.assertTrue(!pmf().isInCache(oid2));
            Assert.assertTrue(pmf().isInCache(oid3));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid1));
            Assert.assertTrue(!pmf().isInCache(oid2));
            Assert.assertTrue(!pmf().isInCache(oid3));
        }

        Assert.assertEquals("ecs " + getCmd(Address.class).index, ct.getLog());

        // If optimistic fill up the cache and then use the pmf evict to
        // evict all the addresses. Nothing should get in the cache for
        // datastore.
        pm.currentTransaction().setOptimistic(optimistic);
        pm.currentTransaction().begin();
        pm.getObjectById(oid1, true);
        pm.getObjectById(oid2, true);
        pm.getObjectById(oid3, true);
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid1));
            Assert.assertTrue(pmf().isInCache(oid2));
            Assert.assertTrue(pmf().isInCache(oid3));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid1));
            Assert.assertTrue(!pmf().isInCache(oid2));
            Assert.assertTrue(!pmf().isInCache(oid3));
        }
        pmf().evictAll(Address.class, true);
        Assert.assertEquals("ec " + getCmd(Address.class).index + " true",
                ct.getLog());
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        if (optimistic) {
            Assert.assertTrue(pmf().isInCache(oid3));
        } else {
            Assert.assertTrue(!pmf().isInCache(oid3));
        }
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(pm.getObjectById(oid1, true));
        pm.deletePersistent(pm.getObjectById(oid2, true));
        pm.deletePersistent(pm.getObjectById(oid3, true));
        pm.currentTransaction().commit();
        ct.clearLog();

        ct.setEnabled(false);
        pm.close();
    }

    /**
     * Check that pmf().evictAll works.
     */
    public void testEvictAll() {
        CacheListenerForTests ct = CacheListenerForTests.instance;
        if (ct == null) return; // remote
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        ct.setEnabled(true);

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Object oid = makeAddr(pm, "all");
        pm.currentTransaction().commit();

        // get it into cache
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.getObjectById(oid, true);
        pm.currentTransaction().commit();
        Assert.assertTrue(pmf().isInCache(oid));

        // evict all
        ct.clearLog();
        pmf().evictAll();
        Assert.assertTrue(!pmf().isInCache(oid));
        Assert.assertEquals("all", ct.getLog());

        // cleanup
        pm.currentTransaction().begin();
        deleteAddr(pm, oid);
        pm.currentTransaction().commit();

        ct.clearLog();
        ct.setEnabled(false);
        pm.close();
    }

    /**
     * Test control of the level 2 cache using the external interface given
     * to cache listeners.
     */
    public void testExternalControl() {
        CacheListenerForTests ct = CacheListenerForTests.instance;
        if (ct == null) return; // remote
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        ct.setEnabled(true);

        // create 2 new Address'es and 1 new Entry
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Object oid1 = makeAddr(pm, "addr1");
        Object oid2 = makeAddr(pm, "addr2");
        Object oid3 = makeEntry(pm, "entry3");
        OID coid1 = getJdoMetaData().convertToOID(oid1);
        OID coid2 = getJdoMetaData().convertToOID(oid2);
        OID coid3 = getJdoMetaData().convertToOID(oid3);
        pm.currentTransaction().commit();
        ct.clearLog();

        pm.currentTransaction().begin();
        pm.getObjectById(oid1, true);
        pm.getObjectById(oid2, true);
        pm.getObjectById(oid3, true);
        Assert.assertTrue(pmf().isInCache(oid1));
        Assert.assertTrue(pmf().isInCache(oid2));
        Assert.assertTrue(pmf().isInCache(oid3));
        ct.clearLog();

        // test evictOID
//        ct.getCache().evictOID(coid1);
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(pmf().isInCache(oid2));
        Assert.assertTrue(pmf().isInCache(oid3));
        Assert.assertEquals("", ct.getLog());

        // test evictOIDs
//        ct.getCache().evictOIDs(new OID[]{coid2, coid3}, 2);
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(!pmf().isInCache(oid3));
        Assert.assertEquals("", ct.getLog());

        // get a new connection and hence new TxId if the con is pinned
        // otherwise data will not go into the cache as the TxId is too old
        if (isConnectionPinnedInOptTx())  {
            pm.currentTransaction().commit();
            pm.currentTransaction().begin();
        }

        // repopulate the cache
        pm.getObjectById(oid1, true);
        pm.getObjectById(oid2, true);
        pm.getObjectById(oid3, true);
        ct.clearLog();

        // test evictClass
//        ct.getCache().evictClass(getCmd(Address.class).index, true);
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(pmf().isInCache(oid3));
        Assert.assertEquals("", ct.getLog());

        // repopulate the cache
        pm.getObjectById(oid1, true);
        pm.getObjectById(oid2, true);
        ct.clearLog();

        // test evictClasses
        int[] bits = new int[getJdoMetaData().classes.length / 32 + 1];
        int ci = getCmd(Address.class).index;
        bits[ci / 32] |= 1 << (ci % 32);
//        ct.getCache().evictClasses(bits);
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(pmf().isInCache(oid3));
        Assert.assertEquals("", ct.getLog());

        // repopulate the cache
        pm.getObjectById(oid1, true);
        pm.getObjectById(oid2, true);
        ct.clearLog();

        // test evictAll
//        ct.getCache().evictAll();
        Assert.assertTrue(!pmf().isInCache(oid1));
        Assert.assertTrue(!pmf().isInCache(oid2));
        Assert.assertTrue(!pmf().isInCache(oid3));
        Assert.assertEquals("", ct.getLog());

        // cleanup
        pm.deletePersistent(pm.getObjectById(oid1, true));
        pm.deletePersistent(pm.getObjectById(oid2, true));
        pm.deletePersistent(pm.getObjectById(oid3, true));
        pm.currentTransaction().commit();

        ct.clearLog();
        ct.setEnabled(false);
        pm.close();
    }

}
