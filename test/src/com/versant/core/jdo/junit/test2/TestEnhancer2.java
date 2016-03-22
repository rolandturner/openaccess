
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
package com.versant.core.jdo.junit.test2;

import com.versant.core.jdo.junit.test2.model.SalesProcess;
import com.versant.core.jdo.junit.test2.model.SalesProcessAppId;
import com.versant.core.jdo.junit.VersantTestCase;

import javax.jdo.PersistenceManager;

import junit.framework.Assert;

/**
 * Extra tests for the enhancer.
 */
public class TestEnhancer2 extends VersantTestCase {

    public void testConstructorForDatastore() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();


        SalesProcess sp = new SalesProcess("10","name");
        Assert.assertEquals("10", sp.getId());
        pm.currentTransaction().begin();
        pm.makePersistent(sp);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SalesProcess sp2 = new SalesProcess("20", "name");
        pm.makePersistent(sp2);
        Assert.assertEquals("20", sp2.getId());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testConstructorForApplication() throws Exception {
    	if (!isApplicationIdentitySupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();

        SalesProcessAppId sp1 = new SalesProcessAppId("10", "name");
        Assert.assertEquals("10", sp1.getId());
        pm.currentTransaction().begin();
        pm.makePersistent(sp1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SalesProcessAppId sp2 = new SalesProcessAppId("20", "name");
        pm.makePersistent(sp2);
        Assert.assertEquals("20", sp2.getId());
        pm.currentTransaction().commit();
        pm.close();
    }

}
