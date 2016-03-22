
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
 * Created on Sep 10, 2004
 *
 * Copyright Versant Corportaion.
 * All rights reserved 2004-05
 */
package com.versant.core.jdo.junit.test1;

import com.versant.core.vds.VdsConfig;
import com.versant.core.vds.VdsConnection;
import com.versant.core.vds.VdsConnectionPool;
import com.versant.core.vds.VdsConfig;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.odbms.DatastoreManager;
import junit.framework.Assert;
import junit.framework.TestCase;
import java.util.*;

/** Tests the behaviour of VdsConnectionPool.
 */
public class TestConnectionPool extends VersantTestCase {
    private static final int MAX_CONNECTION = 10;
    private static final int MAX_USER       = 100;
/**
 * The pool is initailized with N maximum number of allowable connections,
 * while M concurrent user threads request a connection from the pool.
 * After obtaining the connections, it is held by each thread for a random
 * duration and then released to the pool.
 * <p>
 * The test confirms the following
 * <LI>1. Each thread must get a connection i.e. there must be 0 FAILUREs.
 * <LI>2. At some point of time, total number of active threads must exceed
 *  maximum number of connections.
 * <LI>3. The connections obtained from the pool, must not be already allocated
 * to other threads.
 */    
    public void testPool(){
        if (!isVds()) {
            unsupported();
            return;
        }

        VdsConfig config = new VdsConfig();
        config.maxActive = MAX_CONNECTION;
        config.oidBatchSize = 1;
        config.maxIdle      = MAX_CONNECTION;
        config.retryCount   = 10;
        config.blockWhenFull = true;
        config.retryIntervalMs = 100;
        config.url          = "genie";
        System.out.println("Testing Connection Pool with " + MAX_USER + " concurrent user");
        System.out.println(config);
        VdsConnectionPool pool = new VdsConnectionPool(config,null);
        ConnectionUser.TOTAL = MAX_USER;
        for (int i=0; i<MAX_USER; i++){
            ConnectionUser user = new ConnectionUser(pool);
            new Thread(user).start();
            delay(System.currentTimeMillis()%2);
        }
        while (ConnectionUser.TOTAL>0) {
            Thread.yield();
        }
        Assert.assertTrue("Never exceeded limit " + config.maxActive 
                        + " Max.Concurrent Request "+ConnectionUser.MAX_ACTIVE, 
                        config.maxActive<ConnectionUser.MAX_ACTIVE);
        Assert.assertTrue("FAILED " + ConnectionUser.FAILURE, ConnectionUser.FAILURE==0);
    }
    void delay(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex){
            //ignore
        }
        
    }
}

class ConnectionUser implements Runnable {
    public static Collection _allConnections = 
        Collections.synchronizedCollection(new ArrayList());
    final VdsConnectionPool _pool;
    volatile VdsConnection con;
    public static int SUCCESS = 0;
    public static int FAILURE = 0;
    public static int TOTAL   = 0;
    public static int ACTIVE  = 0;
    public static int MAX_ACTIVE = 0;
    private static long AVG_RESPONSE_TIME = 0;
    public ConnectionUser(VdsConnectionPool pool){
        _pool = pool;
    }
    public void run() {
        
        try {
             synchronized (this) {
                ACTIVE++;
                long start = System.currentTimeMillis();
                con = _pool.getConnection(false);
                long elapsedTime = System.currentTimeMillis()-start;
                AVG_RESPONSE_TIME += elapsedTime;
                if (ACTIVE>MAX_ACTIVE) MAX_ACTIVE = ACTIVE;
                SUCCESS++;
                Assert.assertFalse("Active Connection is reallocated", 
                                   _allConnections.contains(con));
                _allConnections.add(con);
                DatastoreManager dsi = con.getCon();
            
                Assert.assertFalse("Connection obtained is idle", con.idle);
                Thread.sleep(System.currentTimeMillis()%100);
            
                _pool.returnConnection(con);
                Assert.assertTrue(_allConnections.remove(con));
                Assert.assertTrue("Connection after returned to pool is not idle or destroyed " + con, con.idle || con.isDestroyed());
             }
        } catch (Exception ex){
            System.err.println(ex);
            synchronized (this) {FAILURE++;}
        } finally {
            synchronized (this) {
                --ACTIVE;
                --TOTAL;
            }
            System.out.println("TOTAL " + TOTAL + " ACTIVE " + ACTIVE + " SUCCESS " + SUCCESS + " FAILURE " + FAILURE);
        }
        
    }
}
