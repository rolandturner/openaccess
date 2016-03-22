
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
 * Created on Sep 13, 2004
 *
 * Copyright Versant Corportaion.
 * All rights reserved 2004-05
 */
package com.versant.core.jdo.junit.test1;

import java.util.Random;
import com.versant.core.jdo.VersantPersistenceManager;

/** A runnable that reads an object, waits randomly and then dies.
 * Maintains static varaibles to gather number of threads that
 * succedded, failed, active.
 * @author ppoddar
 *
 */
public class ReaderThread implements Runnable {
        private static final Random rng = new Random(System.currentTimeMillis());
        VersantPersistenceManager _pm;
        boolean _isOptimistic;
        Object _oid;
        long   _waitInMs;
        public static int ACTIVE;
        public static int TOTAL;
        public static int FAILED;
        public static int PASSED;
/** Initialize this receiver with a PersistenceManager, object id.
 * In its run() method, it will read the object.
 * @param pm
 * @param oid
 * @param txnID
 */        
        ReaderThread(VersantPersistenceManager pm, 
                     Object oid, 
                     boolean isOptimistic,
                     long waitInMs) {
            _pm  = pm;
            _oid = oid;
            _isOptimistic = isOptimistic;
            _waitInMs = waitInMs;
        }
        public void run() {
            try {
                synchronized(this) {ACTIVE++;}
                _pm.currentTransaction().setOptimistic(_isOptimistic);
                _pm.currentTransaction().begin();
                _pm.getObjectById(_oid,true);
                waitRandomly(Math.abs(rng.nextLong()%_waitInMs));
                _pm.currentTransaction().commit();
                synchronized(this) {PASSED++;}
            } catch (Throwable t){
                System.err.println(t.getMessage());
                t.printStackTrace();
                synchronized(this) {FAILED++;}
                if (_pm.currentTransaction().isActive())
                _pm.currentTransaction().rollback();
            } finally { 
                synchronized(this) {
                    ACTIVE--;
                    TOTAL--;
                }
            }
                
            System.err.println(Thread.currentThread().getName() + ":"+this);
        }
//     waits randomly less than <code>time</code>.    
        void waitRandomly(long time){
            try {
                Thread.sleep(time);
            } catch (Exception ex){
                //ignore
            }
        }
        
        public String toString() {
            return "No. of transactions left " + TOTAL + " PASSED " + PASSED + " FAILED " + FAILED + " ACTIVE " + ACTIVE;
        }
    }

