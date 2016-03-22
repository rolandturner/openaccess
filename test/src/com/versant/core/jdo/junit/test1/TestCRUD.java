
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
package com.versant.core.jdo.junit.test1;

import junit.framework.Assert;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test1.model.*;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOObjectNotFoundException;

/**
 * First simple VDS test cases.
 */
public class TestCRUD extends VersantTestCase {

    /**
     * Test create retrieve update delete for a Simple instance.
     */
    public void testSimple() {
        PersistenceManager pm = pmf().getPersistenceManager();

        // store a Simple
        pm.currentTransaction().begin();
        Simple s1 = new Simple(10);
        pm.makePersistent(s1);
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(s1);
        System.out.println("oid = " + oid);

        pm.close();
        pm = pmf().getPersistenceManager();

        // get it back and update it
        pm.currentTransaction().begin();
        s1 = (Simple)pm.getObjectById(oid, true);
        Assert.assertEquals(10, s1.getAge());
        s1.setAge(20);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check the update worked and delete it
        pm.currentTransaction().begin();
        s1 = (Simple)pm.getObjectById(oid, true);
        Assert.assertEquals(20, s1.getAge());
        pm.deletePersistent(s1);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // make sure it is gone
        pm.currentTransaction().begin();
        try {
            pm.getObjectById(oid, true);
            Assert.assertFalse(true);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test create retrieve update delete for a Friend instance.
     */
    public void testUnicardinalityField() {
        PersistenceManager pm = pmf().getPersistenceManager();

        // store a Friend with a reference to another
        pm.currentTransaction().begin();
        Friend david  = new Friend("David", null);
        Friend pinaki = new Friend("Pinaki", david);
        david.setFriend(pinaki);
        pm.makePersistent(pinaki);
        pm.currentTransaction().commit();
        Object pinakiId = pm.getObjectId(pinaki);
        Object davidId  = pm.getObjectId(david);

        pm.evictAll();
        // get it back and update it
        pm.currentTransaction().begin();
        pinaki = (Friend)pm.getObjectById(pinakiId, true);
        david  = (Friend)pm.getObjectById(davidId, true);
        Assert.assertEquals("Pinaki", pinaki.getName());
        Assert.assertEquals("David",  david.getName());
        Assert.assertEquals("David",  pinaki.getFriend().getName());
        Assert.assertEquals("Pinaki", david.getFriend().getName());
        
        Friend keiron = new Friend("Keiron", pinaki);
        pinaki.setFriend(keiron);
        pm.currentTransaction().commit();

        pm.evictAll();

        // check the update worked and delete everything
        pm.currentTransaction().begin();
        pinaki = (Friend)pm.getObjectById(pinakiId, true);
        Assert.assertEquals("Keiron", pinaki.getFriend().getName());
        Assert.assertEquals("Pinaki", keiron.getFriend().getName());
        Assert.assertEquals("Keiron", david.getFriend().getFriend().getName());
        pm.deletePersistent(pinaki);
        pm.deletePersistent(david);
        pm.deletePersistent(keiron);
        pm.currentTransaction().commit();

        // make sure it is gone
        pm.currentTransaction().begin();
        try {
            pm.getObjectById(pinakiId, true);
            Assert.assertFalse(true);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test create retrieve update delete for a Set of Strings.
     */
    public void testSetOfString() {
        PersistenceManager pm = pmf().getPersistenceManager();

        // store a Node
        pm.currentTransaction().begin();
        StringSetContainer sc = new StringSetContainer(10);
        sc.getStrings().add("a");
        sc.getStrings().add("b");
        sc.getStrings().add("c");
        pm.makePersistent(sc);
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(sc);
        System.out.println("oid = " + oid);

        pm.close();
        pm = pmf().getPersistenceManager();

        // get it back and remove one element
        pm.currentTransaction().begin();
        sc = (StringSetContainer)pm.getObjectById(oid, true);
        Assert.assertEquals(10, sc.getAge());
        Assert.assertEquals("[a, b, c]", sc.getStringsStr());
        sc.getStrings().remove("b");
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // get it back and add two elements
        pm.currentTransaction().begin();
        sc = (StringSetContainer)pm.getObjectById(oid, true);
        Assert.assertEquals("[a, c]", sc.getStringsStr());
        sc.getStrings().add("d");
        sc.getStrings().add("e");
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // check that the update worked and nuke it
        pm.currentTransaction().begin();
        sc = (StringSetContainer)pm.getObjectById(oid, true);
        Assert.assertEquals("[a, c, d, e]", sc.getStringsStr());
        pm.deletePersistent(sc);
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // make sure it is gone
        pm.currentTransaction().begin();
        try {
            pm.getObjectById(oid, true);
            Assert.assertFalse(true);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        pm.currentTransaction().commit();

        pm.close();
    }
    public void testIndexedListOfPC() {
        PersistenceManager pm = pmf().getPersistenceManager();

        // store a Node
        pm.currentTransaction().begin();
        IndexedNode n1 = new IndexedNode("n1");
        IndexedNode n2 = new IndexedNode("n2");
        IndexedNode n3 = new IndexedNode("n3");
        IndexedNode n4 = new IndexedNode("n4");
        n1.getChildren().add(n2);
        n1.getChildren().add(n3);
        n1.getChildren().add(n4);
        pm.makePersistent(n1);
        pm.currentTransaction().commit();
        pm.evictAll();
        Object oid = pm.getObjectId(n1);
        System.out.println("oid = " + oid);

        // get it back and remove entries
        pm.currentTransaction().begin();
        Assert.assertEquals("[n2, n3, n4]", n1.getChildrenStr());
        n1.getChildren().remove(n2);
        n1.getChildren().remove(n3);
        pm.currentTransaction().commit();
        pm.evictAll();

        // get it back and add entries
        pm.currentTransaction().begin();
        Assert.assertEquals("[n4]", n1.getChildrenStr());
        n1.getChildren().add(n2);
        pm.currentTransaction().commit();
        pm.evictAll();

        // get it back and nuke it
        pm.currentTransaction().begin();
        Assert.assertEquals("[n2, n4]", n1.getChildrenStr());
        pm.deletePersistentAll(new Object[]{n1, n2, n3, n4});
        pm.currentTransaction().commit();
        pm.evictAll();


        // make sure it is gone
        pm.currentTransaction().begin();
        try {
            pm.getObjectById(oid, true);
            Assert.assertFalse(true);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        pm.currentTransaction().commit();

        pm.close();
    }


    /**
     * Test create retrieve update delete for a Set of PC instance.
     */
    public void testSetOfPC() {
        PersistenceManager pm = pmf().getPersistenceManager();

        // store a Node
        pm.currentTransaction().begin();
        Node n1 = new Node("n1");
        Node n2 = new Node("n2");
        Node n3 = new Node("n3");
        Node n4 = new Node("n4");
        n1.getChildren().add(n2);
        n1.getChildren().add(n3);
        n1.getChildren().add(n4);
        pm.makePersistent(n1);
        pm.currentTransaction().commit();
        pm.evictAll();
        Object oid = pm.getObjectId(n1);
        System.out.println("oid = " + oid);

        // get it back and remove entries
        pm.currentTransaction().begin();
        Assert.assertEquals("[n2, n3, n4]", n1.getChildrenStr());
        n1.getChildren().remove(n2);
        n1.getChildren().remove(n3);
        pm.currentTransaction().commit();
        pm.evictAll();

        // get it back and add entries
        pm.currentTransaction().begin();
        Assert.assertEquals("[n4]", n1.getChildrenStr());
        n1.getChildren().add(n2);
        pm.currentTransaction().commit();
        pm.evictAll();

        // get it back and nuke it
        pm.currentTransaction().begin();
        Assert.assertEquals("[n2, n4]", n1.getChildrenStr());
        pm.deletePersistentAll(new Object[]{n1, n2, n3, n4});
        pm.currentTransaction().commit();
        pm.evictAll();


        // make sure it is gone
        pm.currentTransaction().begin();
        try {
            pm.getObjectById(oid, true);
            Assert.assertFalse(true);
        } catch (JDOObjectNotFoundException e) {
            // good
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the p-clean -> dirty change notification works.
     */
    public void testPCleanToDirtyNotification() throws Exception {
        if (!isVds() || isRemote()) {
            unsupported();
            return;
        }

        // clear counter
        getVdsPCleanToDirtyNotificationCount();

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(false);

        // make sure no notification for new instance
        pm.currentTransaction().begin();
        Simple s1 = new Simple(1);
        pm.makePersistent(s1);
        assertEquals(0, getVdsPCleanToDirtyNotificationCount());
        s1.setAge(5);
        pm.currentTransaction().commit();
        assertEquals(0, getVdsPCleanToDirtyNotificationCount());

        // make sure only one notification for more than one change
        pm.currentTransaction().begin();
        s1.setAge(2);
        assertEquals(1, getVdsPCleanToDirtyNotificationCount());
        s1.setAge(3);
        assertEquals(0, getVdsPCleanToDirtyNotificationCount());
        pm.currentTransaction().commit();
        assertEquals(0, getVdsPCleanToDirtyNotificationCount());

        // make sure there is notification on delete as well
        pm.currentTransaction().begin();
        pm.deletePersistent(s1);
        assertEquals(1, getVdsPCleanToDirtyNotificationCount());
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure the connection is pinned to the PM for the life of the
     * transaction for VDS even in opt tx.
     */
    public void checkConnectionPinnedForVDS() throws Exception {
        if (!isVds() || isRemote()) {
            unsupported();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);

        // create an instance to lookup
        pm.currentTransaction().begin();
        Simple s = new Simple(123);
        pm.makePersistent(s);
        pm.currentTransaction().commit();

        // touch it and make sure connection is pinned after that
        pm.currentTransaction().begin();
    }


}

