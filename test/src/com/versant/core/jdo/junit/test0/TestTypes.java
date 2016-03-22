
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
import com.versant.core.jdo.junit.test0.model.NonMutableJavaTypes;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import java.util.Locale;

/**
 * @keep-all
 */
public class TestTypes extends VersantTestCase {

    public TestTypes(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testNonMutableJavaTypes",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestTypes(a[i]));
        }
        return s;
    }

    /**
     * Test funtionality for the basic java immutable types.
     *
     * @throws Exception
     */
    public void testNonMutableJavaTypes() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
//        tx.setOptimistic(true);
//        tx.setRetainValues(false);
//        tx.setNontransactionalRead(true);
//        tx.setNontransactionalWrite(true);

        pm.currentTransaction().begin();
        NonMutableJavaTypes n = new NonMutableJavaTypes();

        byte b = 2;
        n.setByteValue(b);

        char c = 'a';
        n.setCharValue(c);

        double d = 2d;
        n.setDoubleValue(d);

        float f = 2f;
        n.setFloatValue(f);

        int i = 2;
        n.setIntValue(i);

        long l = 2l;
        n.setLongValue(l);

        short s = 2;
        n.setShortValue(s);

        String string = "string";
        n.setStringValue(string);

        boolean bool = false;
        n.setBooleanValue(bool);

        Locale locale = new Locale("fr", "FR");
        n.setLocale(locale);

        pm.makePersistent(n);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(b, n.getByteValue());
        Assert.assertEquals(c, n.getCharValue());
        Assert.assertEquals(d, n.getDoubleValue(), 0d);
        Assert.assertEquals(f, n.getFloatValue(), 0f);
        Assert.assertEquals(i, n.getIntValue());
        Assert.assertEquals(s, n.getShortValue());
        Assert.assertEquals(string, n.getStringValue());
        Assert.assertEquals(bool, n.isBooleanValue());
        Assert.assertEquals(locale, n.getLocale());
        pm.currentTransaction().commit();
        pm.close();
    }
}
