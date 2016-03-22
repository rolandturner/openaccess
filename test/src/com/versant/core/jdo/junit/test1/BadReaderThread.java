
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
 * The behaviour is bad because the thread does not always commits,
 * rolls back or closes the PM before exiting.
 * 
 * Maintains static varaibles to gather number of threads that
 * succedded, failed, active.
 * @author ppoddar
 *
 */
public class BadReaderThread implements Runnable {
        private static final Random rng = new Random(System.currentTimeMillis());
        VersantPersistenceManager _pm;
        boolean _isOptimistic;
        Object _oid;
        long   _waitInMs;
        float[]  _actionProbability   = new float[]{0.1F, 0.2F, 0.5F};
        
        public static int ACTIVE;
        public static int TOTAL;
        public static int FAILED;
        public static int PASSED;
        public static int BAD;
        
        private static final int COMMIT   = 0;
        private static final int ROLLBACK = 1;
        private static final int CLOSE    = 2;
        private static final int DO_NOTHING = 3;
        
/** Initialize this receiver with a PersistenceManager, object id.
 * In its run() method, it will read the object.
 * @param pm
 * @param oid
 * @param txnID
 */        
        BadReaderThread(VersantPersistenceManager pm, 
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
                int action = decideAction(_actionProbability);
                switch (action) {
                	case COMMIT:
                        _pm.currentTransaction().commit();
                	    break;
                	case ROLLBACK:
                        _pm.currentTransaction().rollback();
                	    break;
                	case CLOSE:
                        _pm.close();
                	    break;
                	case DO_NOTHING:
                	default:
                	    BAD++;
                	    // do nothing -- bad behaviour
                }
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
        static final Random _rng = new Random(System.currentTimeMillis());
        int decideAction(float[] pdf){
            float p = _rng.nextFloat();
            for (int i=0; i<pdf.length; i++) {
                if (p>pdf[i]) {
                    continue;
                } else {
                    return i;
                }
            }
            return DO_NOTHING;
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
            return "No. of transactions left " + TOTAL + " PASSED " + PASSED + " FAILED " + FAILED + " ACTIVE " + ACTIVE + " BAD " + BAD;
        }
    }

