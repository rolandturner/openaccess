
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

import com.versant.core.common.GenericOID;
import com.versant.core.jdo.junit.*;
import com.versant.core.jdo.junit.test1.model.Simple;
import com.versant.core.vds.util.Loid;
import javax.jdo.*;
import junit.framework.Assert;

/**
 * @author ppoddar
 *
 */
public class TestLock extends VersantTestCase {
    Object _oid;
    public void setUp() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(false);
        pm.currentTransaction().begin();
        Simple simple = new Simple();
        simple.setAge(20);
        pm.makePersistent(simple);
        pm.currentTransaction().commit();
        
        _oid = JDOHelper.getObjectId(simple);
    }
    
    public void testPessimisticLockContention() {
        PersistenceManager pm1 = pmf().getPersistenceManager();
        PersistenceManager pm2 = pmf().getPersistenceManager();
        
        Assert.assertTrue(pm1!=pm2);
        
        Transaction t1 = pm1.currentTransaction();
        Transaction t2 = pm2.currentTransaction();
        t1.setOptimistic(false);
        t2.setOptimistic(false);
        
        t1.begin();
        t2.begin();
        Simple pc1 = (Simple)pm1.getObjectById(_oid, true);
        Simple pc2 = (Simple)pm2.getObjectById(_oid, true);
        try {
            pc1.setAge(21);
            Assert.assertFalse("Expected lock conflict", true);
        } catch (Exception ex) {
            System.err.println("Expected exception " + ex.toString());
            Assert.assertTrue("Expected lock conflict", true);
        } finally {
            t1.rollback();
            t2.rollback();
        }
    }
}
