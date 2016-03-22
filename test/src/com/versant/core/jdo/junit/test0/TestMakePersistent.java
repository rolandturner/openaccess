
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
public class TestMakePersistent extends VersantTestCase {

    public TestMakePersistent(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("MakePersistent");
        String[] a = new String[]{
            "testMP_Transient",
            "testMP_PNew",
            "testMP_PClean",
            "testMP_PDirty",
            "testMP_Hollow",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestMakePersistent(a[i]));
        }
        return s;
    }

    public void testMP_Transient() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isPNew(p));
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testMP_PNew() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isPNew(p));
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isPNew(p));
        pm.currentTransaction().rollback();
        pm.close();
    }

    public void testMP_PClean() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isPNew(p));
        pm.currentTransaction().commit();
        Utils.assertTrue(Utils.isHollow(p));

        pm.currentTransaction().begin();
        p.getName();
        Utils.assertTrue(Utils.isPClean(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMP_PDirty() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isPNew(p));
        pm.currentTransaction().commit();
        Utils.assertTrue(Utils.isHollow(p));

        pm.currentTransaction().begin();
        p.setName("name2");
        Utils.assertTrue(Utils.isPDirty(p));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMP_Hollow() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isPNew(p));
        pm.currentTransaction().commit();
        Utils.assertTrue(Utils.isHollow(p));

        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Utils.assertTrue(Utils.isHollow(p));
        pm.currentTransaction().commit();
        pm.close();
    }
}
