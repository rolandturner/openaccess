
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

//import com.versant.core.cluster.ClusterInfoBean;
//import com.versant.core.cluster.VersantClusterMsgHandler;
//import com.versant.core.cluster.VersantClusterTransport;

import java.util.Arrays;

/**
 * This is for testing cache evictions. It records operations to a
 * StringBuffer log for easy checking.
 *
 * @see com.versant.core.jdo.junit.test0.TestLevel2Cache
 */
public class CacheListenerForTests /*implements VersantClusterTransport */{

    public static CacheListenerForTests instance;

    private boolean enabled;
//    private SynchCache cache;
    private StringBuffer log = new StringBuffer();

    public CacheListenerForTests() {
    }

    public void sendMessage(byte[] buffer, int offset, int length) {
//        cache.handleMessage(buffer, offset, length, this);
    }

//    public ClusterInfoBean getStatus() {
//        return null;  //To change body of implemented methods use Options | File Templates.
//    }

    public void close() {
        //doNothing
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

//    public SynchCache getCache() {
//        return cache;
//    }

    /**
     * Get and clear the callback log.
     */
    public String getLog() {
        String s = log.toString();
        clearLog();
        return s;
    }

    /**
     * Clear the callback log.
     */
    public void clearLog() {
        log.setLength(0);
    }

    /**
     * This is called after all props have been set when the cache listener is
     * first created. The cache listener can disable changes to certain
     * properties after this call.
     *
     * @param cache      Use this interface to control JDO Genies internal cache
     *                   (e.g. evicting instances in response to a communication from
     * @param serverPort
     */
//    public void init(VersantClusterMsgHandler cache, String serverName,
//            int serverPort) {
////        this.cache = (SynchCache)cache;
//        instance = this;
//    }

    /**
     * The OID has been evicted from the cache. This method must be
     * synchronized.
     */
    public synchronized void evictedOID(Object oid) {
        if (!enabled) return;
        System.out.println("CacheListenerForTests.evictedOID " + oid);
        if (log.length() > 0) log.append(' ');
        log.append("e ");
        log.append(oid);
    }

    /**
     * The first n entries in oids have been evicted from the cache. This
     * method must be synchronized.
     */
    public synchronized void evictedOIDs(Object[] oids, int n) {
        if (!enabled) return;
        System.out.println("CacheListenerForTests.evictedOIDs n = " + n);

        // convert oids to Strings and sort so test results are detirministic
        String[] a = new String[n];
        for (int i = 0; i < n; i++) a[i] = oids[i].toString();
        Arrays.sort(a);

        if (log.length() > 0) log.append(' ');
        log.append("en");
        for (int i = 0; i < n; i++) {
            log.append(' ');
            log.append(a[i]);
        }
    }

    /**
     * The class has been evicted from the cache. This method must be
     * synchronized.
     */
    public synchronized void evictedClass(int classIndex,
            boolean includeSubclasses) {
        if (!enabled) return;
        System.out.println("CacheListenerForTests.evictedClass " + classIndex +
                " " + includeSubclasses);
        if (log.length() > 0) log.append(' ');
        log.append("ec ");
        log.append(classIndex);
        log.append(' ');
        log.append(includeSubclasses);
    }

    /**
     * The classes with bits set in classIndexBits have been evicted from the
     * cache. This method must be synchronized.
     */
    public synchronized void evictedClasses(int[] classIndexBits) {
        if (!enabled) return;
        System.out.println("CacheListenerForTests.evictedClasses");
        if (log.length() > 0) log.append(' ');
        log.append("ecs");
        int n = classIndexBits.length;
        int classIndex = 0;
        for (int i = 0; i < n; i++) {
            int x = classIndexBits[i];
            for (int mask = 1; mask != 0; mask <<= 1, classIndex++) {
                if ((x & mask) != 0) {
                    log.append(' ');
                    log.append(classIndex);
                }
            }
        }
    }

    /**
     * All OIDs have been evicted from the cache. This method must be
     * synchronized.
     */
    public synchronized void evictedAll() {
        if (!enabled) return;
        if (log.length() > 0) log.append(' ');
        log.append("all");
    }

    /**
     * Return a Serializable Javabean that provides status information on
     * this component. This may be null if not supported.
     */
    public Object getStatusBean() {
        return null;
    }

}
