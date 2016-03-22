
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
 * Created on Sep 15, 2004
 *
 * Copyright Versant Corportaion.
 * All rights reserved 2004-05
 */
package com.versant.core.jdo.junit.test1;

import java.util.Random;
import com.versant.core.jdo.junit.VersantTestCase;
import javax.jdo.*;
import junit.framework.Assert;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.test1.model.*;

/**
 * @author ppoddar
 *
 */
public class TestBadConnection extends VersantTestCase {
    Object 		_oid;
    int     N = 100; // No of independent threads;
    public void setUp() throws Exception {
        super.setUp();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        Simple simple = new Simple();
        simple.setAge(20);
        pm.makePersistent(simple);
        pm.currentTransaction().commit();
        
        _oid = JDOHelper.getObjectId(simple);
    }
    public void testPoorConcurrentAccess() {
        if (!isVds()) {
            unsupported();
            return;
        }
        BadReaderThread.TOTAL = N;
        for (int i=1; i<=N; i++){
            VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
            Runnable reader = new BadReaderThread(pm, _oid, false,1000L);
            Thread t = new Thread(reader,"BadReader-"+i);
            t.start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex){
                
            }
        }
        while (BadReaderThread.TOTAL>0);
        Assert.assertTrue((BadReaderThread.PASSED+BadReaderThread.FAILED)==N);
        Assert.assertTrue("TOTAL " + N + " FAILED " + ReaderThread.FAILED, 
                ReaderThread.FAILED==0);
    }}
