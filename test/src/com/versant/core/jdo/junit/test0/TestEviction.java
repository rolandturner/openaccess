
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
import com.versant.core.jdo.PMProxy;
import com.versant.core.common.State;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.Utils;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.PMProxy;
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

/**
 * @keep-all
 */
public class TestEviction extends VersantTestCase {

    public TestEviction(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testEvictPNew",
            "testEvictPClean",
            "testEvictPDirty",
            "testEvictHollow",
            "testEvictPNonTX",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestEviction(a[i]));
        }
        return s;
    }

    public void testEvictPNew() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.evict(p);
        pm.currentTransaction().commit();

        State state = ((PMProxy)pm).getRealPM().getInternaleState(
                (PersistenceCapable)p);
        if (state != null && !state.isEmpty()) {
            Assert.fail("The state is supposed to be empty");
        }

        pm.currentTransaction().begin();
        Assert.assertEquals("name", p.getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testEvictPClean() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("name", p.getName());
        Assert.assertTrue("The instance must be P-clean.", Utils.isPClean(p));
        pm.evict(p);
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        State state = ((PMProxy)pm).getRealPM().getInternaleState(
                (PersistenceCapable)p);
        if (!state.isEmpty()) {
            Assert.fail("The state is supposed to be empty");
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testEvictPDirty() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("name", p.getName());
        p.setName("name1");
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        pm.evict(p);
        Assert.assertTrue("The instance must be P-Dirty.", Utils.isPDirty(p));
        State state = ((PMProxy)pm).getRealPM().getInternaleState(
                (PersistenceCapable)p);
        if (!state.isDirty()) {
            Assert.fail("The state is supposed to be dirty");
        }
        state = ((PMProxy)pm).getRealPM().getInternaleState(
                (PersistenceCapable)p);
        pm.currentTransaction().commit();
        if (!state.isEmpty()) {
            Assert.fail("The state is supposed to be empty");
        }
        pm.close();
    }

    public void testEvictHollow() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        pm.evict(p);
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testEvictPNonTX() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        p.getName();
        Assert.assertTrue("The instance must be PNonTX.", Utils.isPNonTx(p));
        pm.evict(p);
        Assert.assertTrue("The instance must be Hollow.", Utils.isHollow(p));
        State state = ((PMProxy)pm).getRealPM().getInternaleState(
                (PersistenceCapable)p);
        if (!state.isEmpty()) {
            Assert.fail("The state is supposed to be empty");
        }
        pm.currentTransaction().commit();
        pm.close();
    }
}
