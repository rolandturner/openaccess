
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
package com.versant.core.jdo.junit;

import com.versant.core.jdo.VersantPersistenceManagerImp;
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.common.OID;
import com.versant.core.jdo.junit.TestFailedException;
import com.versant.core.jdo.junit.test0.CacheListenerForTests;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 */
public class Utils {

    public static void checkQCacheSize(int size, PersistenceManager pm) {
        if (pm instanceof PMProxy) {
            VersantPersistenceManagerImp rPm = ((PMProxy)pm).getRealPM();
//            if (rPm.realJdoConnection instanceof JDOConnectionImp) {
//                Assert.assertEquals(size, ((JDOConnectionImp)rPm.realJdoConnection).queryCacheSize());
//            }
        } else {
            fail("Instance not PMProxy");
        }
    }

    public static VersantPersistenceManagerImp getJDOPMImp(PersistenceManager pm) {
        if (pm instanceof PMProxy) {
            return ((PMProxy)pm).getRealPM();
        } else {
            fail("Instance not PMProxy");
        }
        return null;
    }

    public static boolean cacheEnabled() {
        return CacheListenerForTests.instance != null;
    }

    public static void checkQCacheSize(int size, VersantPersistenceManagerImp pm) {
//        if (pm.realJdoConnection instanceof JDOConnectionImp) {
//            Assert.assertEquals(size, ((JDOConnectionImp)pm.realJdoConnection).queryCacheSize());
//        }
    }

    public static boolean isHollow(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isHollow(pc);
    }

    public static boolean isPNonTx(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isPNonTx(pc);
    }

    public static boolean isPersistentClean(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isPClean(pc);
    }

    public static boolean isPDirty(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isPDirty(pc);
    }

    public static boolean isPClean(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isPClean(pc);
    }

    public static boolean isPNew(Object pc) {
        return JDOHelper.isNew(pc);
    }

    public static boolean isPNewDeleted(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isPNewDeleted(pc);
    }

    public static boolean isPDeleted(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isPDeleted(pc);
    }

    public static boolean isTClean(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isTClean(pc);
    }

    public static boolean isTDirty(Object pc) {
        return (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).isTDirty(pc);
    }

    public static boolean isTransient(Object pc) {
        return (!JDOHelper.isDeleted(pc)
            && !JDOHelper.isDirty(pc)
            && !JDOHelper.isNew(pc)
            && !JDOHelper.isPersistent(pc)
            && !JDOHelper.isTransactional(pc)
            && (JDOHelper.getPersistenceManager(pc) == null));
    }



    public static void dump(Object pc) {
        (((PMProxy)JDOHelper.getPersistenceManager(pc)).getRealPM()).dump((OID)JDOHelper.getObjectId(pc));
    }

    static public void assertEquals(Object expected, Object actual) {
	    assertEquals(null, expected, actual);
	}

    static public void assertEquals(String message, Object expected, Object actual) {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		failNotEquals(message, expected, actual);
	}

    static public void assertEquals(long expected, long actual) {
	    assertEquals(null, expected, actual);
	}

    static public void assertEquals(String message, long expected, long actual) {
	    assertEquals(message, new Long(expected), new Long(actual));
	}

    static public void assertTrue(boolean condition) {
		assertTrue(null, condition);
	}

    static public void assertTrue(String message, boolean condition) {
		if (!condition)
			fail(message);
	}

    static private void failNotEquals(String message, Object expected, Object actual) {
		String formatted= "";
		if (message != null)
			formatted= message+" ";
		fail(formatted+"expected:<"+expected+"> but was:<"+actual+">");
	}

    static public void fail(String message) {
		throw new TestFailedException(message);
	}

}
