
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
package com.versant.core.jdo.junit.testsco;

import junit.framework.Assert;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.sco.*;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.testsco.model.SCOFields;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.JDOHelper;
import java.io.IOException;
import java.util.*;

public class TestPluggableSCO extends VersantTestCase {

    public TestPluggableSCO(String name) {
        super(name);
    }

    public void testCollections() throws IOException, ClassNotFoundException {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SCOFields b = new SCOFields();
        String s1 = "key1";
        String s2 = "key2";
        b.getArrayList().add(s1);
        b.getArrayList().add(s2);
        b.getHashSet().add(s1);
        b.getHashSet().add(s2);
        Assert.assertEquals(2, b.getHashSet().size());
        b.getLinkedList().add(s1);
        b.getLinkedList().add(s2);
        b.getTreeSet().add(s1);
        b.getTreeSet().add(s2);
        b.getVector().add(s1);
        b.getVector().add(s2);
        b.getHashMap().put("1", s1);
        b.getHashMap().put("2", s2);
        b.getHashtable().put("1", s1);
        b.getHashtable().put("2", s2);
        b.getTreeMap().put("1", s1);
        b.getTreeMap().put("2", s2);

        Assert.assertTrue(b.getHashSet().getClass().equals(HashSet.class));
        Assert.assertTrue(b.getTreeSet().getClass().equals(TreeSet.class));
        Assert.assertTrue(b.getList().getClass().equals(ArrayList.class));
        Assert.assertTrue(b.getArrayList().getClass().equals(ArrayList.class));
        Assert.assertTrue(
                b.getLinkedList().getClass().equals(LinkedList.class));
        Assert.assertTrue(b.getVector().getClass().equals(Vector.class));
        Assert.assertTrue(b.getHashMap().getClass().equals(HashMap.class));
        Assert.assertTrue(b.getMap().getClass().equals(HashMap.class));
        Assert.assertTrue(b.getHashtable().getClass().equals(Hashtable.class));
        Assert.assertTrue(b.getTreeMap().getClass().equals(TreeMap.class));
        Assert.assertTrue(b.getSet().getClass().equals(HashSet.class));
        Assert.assertTrue(b.getDate().getClass().equals(java.util.Date.class));
        Assert.assertTrue(b.getCollection().getClass().equals(TreeSet.class));
        pm.makePersistent(b);
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        b = (SCOFields)pm.getObjectById(JDOHelper.getObjectId(b), false);
        Assert.assertTrue(b.getHashSet().getClass().equals(SCOHashSet.class));
        Assert.assertTrue(b.getTreeSet().getClass().equals(SCOTreeSet.class));
        Assert.assertTrue(b.getList().getClass().equals(SCOVector.class));
        Assert.assertTrue(
                b.getArrayList().getClass().equals(SCOArrayList.class));
        Assert.assertTrue(
                b.getLinkedList().getClass().equals(SCOLinkedList.class));
        Assert.assertTrue(b.getVector().getClass().equals(SCOVector.class));
        Assert.assertTrue(b.getHashMap().getClass().equals(SCOHashMap.class));
        Assert.assertTrue(b.getMap().getClass().equals(SCOHashtable.class));
        Assert.assertTrue(
                b.getHashtable().getClass().equals(SCOHashtable.class));
        Assert.assertTrue(b.getTreeMap().getClass().equals(SCOTreeMap.class));
        Assert.assertTrue(b.getSet().getClass().equals(SCOTreeSet.class));
        Assert.assertTrue(
                b.getDate().getClass().equals(
                        com.versant.core.jdo.sco.Date.class));
        Assert.assertTrue(
                b.getCollection().getClass().equals(SCOTreeSet.class));
        pm.currentTransaction().rollback();
        pm.close();
    }

}
