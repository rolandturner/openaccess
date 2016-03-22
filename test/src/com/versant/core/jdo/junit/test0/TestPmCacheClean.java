
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
import com.versant.core.jdo.junit.test0.model.Stuff;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.VersantPersistenceManagerImp;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOHelper;

public class TestPmCacheClean extends VersantTestCase {

    public TestPmCacheClean(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testPmCacheClean",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestPmCacheClean(a[i]));
        }
        return s;
    }

    /**
     * This makes sure that PMs retrieved from the PM Pool have a clean
     * local cache.
     */
    public void testPmCacheClean() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Stuff stuff = new Stuff("myStuff");
        pm.makePersistent(stuff);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        Assert.assertEquals("myStuff", stuff.getStuffName());
        Object objectId = JDOHelper.getObjectId(stuff);
        VersantPersistenceManagerImp realPM = ((PMProxy)pm).getRealPM();
        VersantPersistenceManagerImp newRealPM;
        do {
            pm.close();
            pm = (VersantPersistenceManager)pmf().getPersistenceManager();
            newRealPM = ((PMProxy)pm).getRealPM();
            Object newStuff = newRealPM.getObjectByIdFromCache(objectId);
            Assert.assertNull(newStuff);
        } while (realPM != newRealPM);
        pm.close();
    }
}
