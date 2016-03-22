
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
import com.versant.core.jdo.junit.test0.model.Person;

import javax.jdo.JDODataStoreException;
import javax.jdo.PersistenceManager;

/**
 * @keep-all
 */
public class TestExceptionHandling extends VersantTestCase {

    public TestExceptionHandling(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testGetObjectByIdForNonExistingOID",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestExceptionHandling(a[i]));
        }
        return s;
    }

    /**
     * This test should throw a JDOUserException and not rollback the tx.
     *
     * @throws Exception
     */
    public void testGetObjectByIdForNonExistingOID() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person main = new Person("main");
        pm.makePersistent(main);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(main);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        pm.deletePersistent(pm.getObjectById(id, false));
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = null;
        try {
            p = (Person)pm.getObjectById(id, true);
        } catch (JDODataStoreException e) {
            //expected
        } catch (Exception e) {
            Assert.fail("A JDOUserException must be thrown");
        }
        Assert.assertTrue("The tx must still be active",
                pm.currentTransaction().isActive());
        pm.currentTransaction().commit();
        pm.close();
    }
}


