
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
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;

/**
 * @keep-all
 */
public class TestMakeTransactional extends VersantTestCase {

    public TestMakeTransactional(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("MakeTransactional");
        String[] a = new String[]{
            "testMakeTransactionOfTransientInstance",
            "testMakeTransactionOfTransientInstanceUseDSTx",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestMakeTransactional(a[i]));
        }
        return s;
    }

    public void testMakeTransactionOfTransientInstance() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person();
        pm.makeTransactional(p);
        Utils.isTClean(p);
        pm.close();
    }

    public void testMakeTransactionOfTransientInstanceUseDSTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("bla1");
        pm.makeTransactional(p);
        Utils.isTClean(p);

        pm.currentTransaction().begin();
        Assert.assertEquals("bla1", p.getName());
        Utils.isTClean(p);
        p.setName("bla2");
        Utils.isTDirty(p);
        Assert.assertEquals("bla2", p.getName());
        Utils.isTDirty(p);
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testMakeTransactionOfTransientInstanceUseOpTx() {
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("bla1");
        pm.makeTransactional(p);
        Utils.isTClean(p);

        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Assert.assertEquals("bla1", p.getName());
        Utils.isTClean(p);
        p.setName("bla2");
        Utils.isTDirty(p);
        Assert.assertEquals("bla2", p.getName());
        Utils.isTDirty(p);
        pm.currentTransaction().commit();
        pm.close();

    }

}

