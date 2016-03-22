
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
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test0.model.Person;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;

/**
 * These tests look for bugs with objects in the local PM cache.
 *
 * @keep-all
 * @see PersistenceManager
 */
public class TestLocalCache extends VersantTestCase {

    public TestLocalCache(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testBugNullFieldsInTxAfterRollback",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestLocalCache(a[i]));
        }
        return s;
    }

    /**
     * Bug: Start tx, lookup instance by OID, modify fields, rollback tx.
     * Start new tx, lookup instance by OID, some fields are null which
     * should not be null.
     *
     * @throws Exception
     */
    public void testBugNullFieldsInTxAfterRollback() throws Exception {
        VersantPersistenceManager pm =
                (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().setRetainValues(true);

        System.out.println("\n*** create Person, commit, rollback");
        String name = "testBugNullFieldsInTxAfterRollback";
        Person p = new Person(name);
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        System.out.println("\n*** read person with a , rollback");
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class, "name == n");
        q.declareParameters("String n");
        Collection ans = (Collection)q.execute(name);
        p = (Person)ans.iterator().next();
        q.closeAll();
        Assert.assertEquals(name, p.getName());
        String id = pm.getObjectId(p).toString();
        pm.currentTransaction().rollback();

        System.out.println("\n*** modify name and do rollback");
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectByIDString(id, true);
        Assert.assertEquals(name, p.getName());
        p.setName(name + "-xxx");
        pm.currentTransaction().rollback();

        System.out.println("\n*** check name is unchanged");
        pm.currentTransaction().begin();
        p = (Person)pm.getObjectByIDString(id, true);
        Assert.assertEquals(name, p.getName());

        pm.close();
    }

}
