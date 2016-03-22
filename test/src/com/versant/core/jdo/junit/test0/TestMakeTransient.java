
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
import com.versant.core.jdo.junit.Utils;

import javax.jdo.PersistenceManager;

/**
 * @keep-all
 */
public class TestMakeTransient extends VersantTestCase {

    public TestMakeTransient(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("MakeTransient");
        String[] a = new String[]{
            "testCheckFg",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestMakeTransient(a[i]));
        }
        return s;
    }

    public void testCheckFg() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.setIntField(3);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Utils.isHollow(p);
        p.getName();
        p.getIntField();

        pm.makeTransient(p);
        Assert.assertEquals("name", p.getName());
        Assert.assertEquals(3, p.getIntField());
        pm.close();
    }

}

