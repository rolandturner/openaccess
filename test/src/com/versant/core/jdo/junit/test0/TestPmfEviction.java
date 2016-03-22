
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
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.Address;
import com.versant.core.jdo.junit.test0.model.EmpSuper;
import com.versant.core.jdo.junit.test0.model.Employee;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;
import java.util.Arrays;

/**
 * Test API to evict instances from the PMF wide cache.
 *
 * @keep-all
 */
public class TestPmfEviction extends VersantTestCase {

    private Object addressOID;
    private Object[] addressOIDs;
    private Object[] empSuperOIDs;

    public TestPmfEviction(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testEvictAll",
            "testEvict",
            "testEvictAllOIDsArray",
            "testEvictAllOIDsCollection",
            "testEvictAllClass",
            "testEvictAllClassIncludeSubclasses",
            "testEvictAllClassIncludeSubclassesFalse"
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestPmfEviction(a[i]));
        }
        return s;
    }

    private void insertTestData() throws Exception {
        if (addressOID != null) return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        // create Address'es to use for tests
        Address p = new Address("testEvictOIDOptimistic");
        pm.makePersistent(p);
        addressOID = pm.getObjectId(p);
        int n = 10;
        addressOIDs = new Object[n];
        for (int i = 0; i < n; i++) {
            pm.makePersistent(p = new Address("testEvictOIDOptimistic" + i));
            addressOIDs[i] = pm.getObjectId(p);
        }

        // create Employee's and EmpSuper's to use for test
        empSuperOIDs = new Object[n * 2];
        for (int i = 0; i < n; i++) {
            EmpSuper e;
            pm.makePersistent(e = new EmpSuper("testEvictOIDOptimistic" + i));
            empSuperOIDs[i] = pm.getObjectId(e);
            pm.makePersistent(
                    e = new Employee("testEvictOIDOptimistic" + i, "empNo" + i));
            empSuperOIDs[i + n] = pm.getObjectId(e);
        }

        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Check that the cache is emptied.
     */
    public void testEvictAll() throws Exception {
        if (!Utils.cacheEnabled()) return;
        insertTestData();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println("*** empty the cache");
        pmf().evictAll();
        Assert.assertEquals(0, getLevel2CacheSize());

        System.out.println(
                "*** put our Address into the cache by looking it up");
        pm.getObjectById(addressOID, true);

        System.out.println("*** check that it is in the cache");
        Assert.assertTrue(getLevel2CacheSize() > 0);

        System.out.println("*** empty the cache");
        pmf().evictAll();
        Assert.assertEquals(getLevel2CacheSize(), 0);

        pm.close();
    }

    /**
     * Check that an OID is evicted.
     */
    public void testEvict() throws Exception {
        if (!Utils.cacheEnabled()) return;
        insertTestData();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println("*** empty the cache");
        pmf().evictAll();
        Assert.assertEquals(0, getLevel2CacheSize());

        System.out.println(
                "*** put our Address into the cache by looking it up");
        pm.getObjectById(addressOID, true);

        System.out.println("*** check that it is in the cache");
        int c = getLevel2CacheSize();
        Assert.assertTrue(c > 0);

        System.out.println(
                "*** evict it and check that there is one less in cache");
        pmf().evict(addressOID);
        Assert.assertEquals(c - 1, getLevel2CacheSize());

        pm.close();
    }

    /**
     * Check that an array of OIDs is evicted OK.
     */
    public void testEvictAllOIDsArray() throws Exception {
        testEvictAllImp(true);
    }

    /**
     * Check that a collection of OIDs is evicted OK.
     */
    public void testEvictAllOIDsCollection() throws Exception {
        testEvictAllImp(false);
    }

    private void testEvictAllImp(boolean useArray) throws Exception {
        if (!Utils.cacheEnabled()) return;
        insertTestData();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println("*** empty the cache");
        pmf().evictAll();
        Assert.assertEquals(0, getLevel2CacheSize());

        System.out.println(
                "*** put our Address'es into the cache by looking them up");
        int n = addressOIDs.length;
        for (int i = 0; i < n; i++) pm.getObjectById(addressOIDs[i], true);

        System.out.println("*** check that they are in the cache");
        Assert.assertEquals(n, getLevel2CacheSize());

        Object[] a = new Object[n / 2];
        for (int i = 0; i < n / 2; i++) a[i] = addressOIDs[i * 2];

        System.out.println("*** remove half of them from the cache");
        if (useArray) {
            pmf().evictAll(a);
        } else {
            pmf().evictAll(Arrays.asList(a));
        }
        Assert.assertEquals(n - n / 2, getLevel2CacheSize());

        pm.close();
    }

    /**
     * Check that a class is evicted OK.
     */
    public void testEvictAllClass() throws Exception {
        if (!Utils.cacheEnabled()) return;
        insertTestData();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println("*** empty the cache");
        pmf().evictAll();
        Assert.assertEquals(0, getLevel2CacheSize());

        System.out.println(
                "*** put our Address'es into the cache by looking them up");
        int n = addressOIDs.length;
        for (int i = 0; i < n; i++) pm.getObjectById(addressOIDs[i], true);

        System.out.println("*** check that they are in the cache");
        Assert.assertEquals(n, getLevel2CacheSize());

        System.out.println("*** evict all by class");
        pmf().evictAll(Address.class, true);
        Assert.assertEquals(0, getLevel2CacheSize());

        pm.close();
    }

    /**
     * Check that a class is evicted OK with includeSubclasses true.
     */
    public void testEvictAllClassIncludeSubclasses() throws Exception {
        testEvictAllClassImp(true);
    }

    /**
     * Check that a class is evicted OK with includeSubclasses false.
     */
    public void testEvictAllClassIncludeSubclassesFalse() throws Exception {
        testEvictAllClassImp(false);
    }

    private void testEvictAllClassImp(boolean includeSubclasses)
            throws Exception {
        if (!Utils.cacheEnabled()) return;
        insertTestData();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();

        System.out.println("*** empty the cache");
        pmf().evictAll();
        Assert.assertEquals(0, getLevel2CacheSize());
        Utils.checkQCacheSize(0, pm);

        System.out.println(
                "*** put our EmpSuper'es into the cache by looking them up");
        int n = empSuperOIDs.length;
        for (int i = 0; i < n; i++) {
            System.out.println("*** [" + i + "] = " +
                    pm.getObjectById(empSuperOIDs[i], true));
        }

        System.out.println("*** check that they are in the cache");
        Assert.assertEquals(n, getLevel2CacheSize());

        System.out.println("*** evict all by class");
        pmf().evictAll(EmpSuper.class, includeSubclasses);
        Assert.assertEquals(includeSubclasses ? 0 : n / 2,
                getLevel2CacheSize());

        pm.close();
    }

}
