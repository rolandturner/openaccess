
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
import com.versant.core.common.Debug;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.TestUtils;
import com.versant.core.jdo.junit.test0.model.Person;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

/**
 * Tests for collections.
 */
public class JDOCollectionTests extends VersantTestCase {

    public JDOCollectionTests(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite("CollectionTests");
        String[] a = new String[]{
            "testUnsupportedTypesLinkedList",
////            "testListWithNulls",
            "testHashSetSimplesTypes1",
            "testReplaceHashSet",
//            "testSetExistingRefToNull",
            "testUnorderedListSimpleTypes",
            "testOrderedStringList",
            "testOrderedStringListAddFront",
            "testOrderedStringListAddBack",
            "testOrderedStringListAddMiddle",
            "testOrderedStringListRemoveMiddle",
            "testOrderedRefList",
            "testOrderedRefListAddFront",
            "testOrderedRefListAddMiddle",
            "testOrderedRefListRemoveMiddle",
            "testOrderedRefListGenUpdates",
            "testRefCollection",
            "testSetCollectionNull",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new JDOCollectionTests(a[i]));
        }
        return s;
    }

    public void testUnsupportedTypesLinkedList() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p");
        LinkedList lList = new LinkedList();
        lList.add(new Person("p2"));
        p.setPersonsList(lList);
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, p.getPersonsList().size());
        Assert.assertEquals("p2",
                ((Person)p.getPersonsList().get(0)).getName());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getPersonsList().add(new Person("p3"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, p.getPersonsList().size());
        Assert.assertEquals("p2",
                ((Person)p.getPersonsList().get(0)).getName());
        Assert.assertEquals("p3",
                ((Person)p.getPersonsList().get(1)).getName());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testListWithNulls() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("p");
        p.getPersonsList().add(null);
        pm.makePersistent(p);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, p.getPersonsList().size());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test the workings of a hashSet filled with strings.
     * This test a makePersistent.
     *
     * @throws Exception
     */
    public void testHashSetSimplesTypes1() throws Exception {
        boolean[] bools = new boolean[]{
            true,
            false,
        };

        for (int i = 0; i < 2; i++) {
            if (Debug.DEBUG) {
                Debug.OUT.println("############## RUN = " + i);
            }

            Set set = new HashSet();
            Set oSet = new HashSet();
            for (int j = 0; j < 6; j++) {
                set.add("String" + j);
                oSet.add("String" + j);
            }
            PersistenceManager pm = pmf().getPersistenceManager();
            pm.currentTransaction().setRetainValues(bools[i]);
            pm.currentTransaction().begin();
            Person p = new Person("name1");
            p.setStringSet(oSet);
            pm.makePersistent(p);
            pm.currentTransaction().commit();


//            pm = pmf().getPersistenceManager();
//            pm.currentTransaction().setRetainValues(bools[i]);
//            p = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
            pm.currentTransaction().begin();
            TestUtils.assertEquals(set, p.getStringSet());
            pm.currentTransaction().commit();

//            pm = pmf().getPersistenceManager();
//            pm.currentTransaction().setRetainValues(bools[i]);
//            p = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
            pm.currentTransaction().begin();
            p.getStringSet().add("added");
            set.add("added");
            pm.currentTransaction().commit();

//            pm = pmf().getPersistenceManager();
//            pm.currentTransaction().setRetainValues(bools[i]);
//            p = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
            pm.currentTransaction().begin();
            TestUtils.assertEquals(set, p.getStringSet());
            pm.currentTransaction().commit();

//            pm = pmf().getPersistenceManager();
//            pm.currentTransaction().setRetainValues(bools[i]);
//            p = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
            pm.currentTransaction().begin();
            TestUtils.assertTrue(p.getStringSet().remove("String5"));
            TestUtils.assertTrue(set.remove("String5"));
            TestUtils.assertEquals(set, p.getStringSet());
            pm.currentTransaction().commit();

//            pm = pmf().getPersistenceManager();
//            pm.currentTransaction().setRetainValues(bools[i]);
//            p = (Person)pm.getObjectById(JDOHelper.getObjectId(p), false);
            pm.currentTransaction().begin();
            TestUtils.assertEquals(set, p.getStringSet());
            pm.currentTransaction().commit();
            Debug.OUT.println(
                    "############################# after commit ####################");
            pm.close();

            pm = pmf().getPersistenceManager();
            Debug.OUT.println(
                    "############################# after after getPM ####################");
            pm.currentTransaction().begin();
            Person p2 = (Person)pm.getObjectById(JDOHelper.getObjectId(p),
                    false);
            TestUtils.assertEquals(set, p2.getStringSet());
            pm.currentTransaction().commit();
            pm.close();
        }

    }

    public void testReplaceHashSet() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Set set = new HashSet();
        for (int i = 0; i < 10; i++) {
            set.add("string" + i);
        }
        p.setStringSet(set);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(set, p.getStringSet());
        Set set2 = new HashSet();
        for (int i = 0; i < 5; i++) {
            set2.add("newString" + i);
        }
        p.setStringSet(set2);
        Assert.assertEquals(set2, p.getStringSet());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(set2, p.getStringSet());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testSetExistingRefToNull() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        Person p = new Person("p");
        pm.currentTransaction().begin();
        pm.makePersistent(p);
        Set set = new HashSet();
        for (int i = 0; i < 10; i++) {
            set.add("string" + i);
        }
        p.setStringSet(set);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(set, p.getStringSet());
        p.setStringSet(null);
        Assert.assertNull(p.getStringSet());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertNull(p.getStringSet());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testUnorderedListSimpleTypes() throws Exception {
        nuke(Person.class);
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        List stringList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            stringList.add("StringElement" + i);
        }
        for (int i = 0; i < 5; i++) {
            Person newP = new Person("name");
            newP.getStringList().addAll(stringList);
            pm.makePersistent(newP);
        }
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
//        q.setCandidates(new DummyExtent(Person.class, false));
//        q.setClass(Person.class);
        q.setFilter("name == \"name\"");
        Collection col = (Collection)q.execute();
        List list = new ArrayList();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person person = (Person)iterator.next();
            if (Debug.DEBUG) {
                Debug.OUT.println("name = " + person.getName());
            }
            list.add(person);
        }
        Person p = (Person)list.get(0);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedStringList() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedStringList().add("String0");
        p.getOrderedStringList().add("String1");
        p.getOrderedStringList().add("String2");
        p.getOrderedStringList().add("String3");
        p.getOrderedStringList().add("String4");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedStringList();
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals("String" + i, list.get(i));
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedStringListAddFront() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedStringList().add("String0");
        p.getOrderedStringList().add("String1");
        p.getOrderedStringList().add("String2");
        p.getOrderedStringList().add("String3");
        p.getOrderedStringList().add("String4");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedStringList().add(0, "String5");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedStringList();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                Assert.assertEquals("String5", list.get(0));
            }
            Assert.assertEquals("String" + i, list.get(++i));
        }
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedStringListAddBack() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedStringList().add("String0");
        p.getOrderedStringList().add("String1");
        p.getOrderedStringList().add("String2");
        p.getOrderedStringList().add("String3");
        p.getOrderedStringList().add("String4");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedStringList().add("String5");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedStringList();
        int i = 0;
        for (; i < list.size(); i++) {
            Assert.assertEquals("String" + i, list.get(i));
        }
        pm.currentTransaction().commit();
        Assert.assertEquals(6, i);
        pm.close();
    }

    public void testOrderedStringListAddMiddle() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedStringList().add("String0");
        p.getOrderedStringList().add("String1");
        p.getOrderedStringList().add("String2");
        p.getOrderedStringList().add("String3");
        p.getOrderedStringList().add("String4");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedStringList().add(3, "String5");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedStringList();

        Assert.assertEquals(6, list.size());
        Assert.assertEquals("String0", list.get(0));
        Assert.assertEquals("String1", list.get(1));
        Assert.assertEquals("String2", list.get(2));
        Assert.assertEquals("String5", list.get(3));
        Assert.assertEquals("String3", list.get(4));
        Assert.assertEquals("String4", list.get(5));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedStringListRemoveMiddle() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedStringList().add("String0");
        p.getOrderedStringList().add("String1");
        p.getOrderedStringList().add("String2");
        p.getOrderedStringList().add("String3");
        p.getOrderedStringList().add("String4");
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedStringList().remove("String3");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedStringList();

        Assert.assertEquals(4, list.size());
        Assert.assertEquals("String0", list.get(0));
        Assert.assertEquals("String1", list.get(1));
        Assert.assertEquals("String2", list.get(2));
        Assert.assertEquals("String4", list.get(3));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedRefList() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedRefList().add(new Person("name0"));
        p.getOrderedRefList().add(new Person("name1"));
        p.getOrderedRefList().add(new Person("name2"));
        p.getOrderedRefList().add(new Person("name3"));
        p.getOrderedRefList().add(new Person("name4"));

        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedRefList();

        Assert.assertEquals(5, list.size());
        Assert.assertEquals("name0", ((Person)list.get(0)).getName());
        Assert.assertEquals("name1", ((Person)list.get(1)).getName());
        Assert.assertEquals("name2", ((Person)list.get(2)).getName());
        Assert.assertEquals("name3", ((Person)list.get(3)).getName());
        Assert.assertEquals("name4", ((Person)list.get(4)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedRefListAddFront() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedRefList().add(new Person("name0"));
        p.getOrderedRefList().add(new Person("name1"));
        p.getOrderedRefList().add(new Person("name2"));
        p.getOrderedRefList().add(new Person("name3"));
        p.getOrderedRefList().add(new Person("name4"));

        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedRefList().add(0, new Person("name0"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedRefList();

        Assert.assertEquals(6, list.size());
        Assert.assertEquals("name0", ((Person)list.get(0)).getName());
        Assert.assertEquals("name0", ((Person)list.get(1)).getName());
        Assert.assertEquals("name1", ((Person)list.get(2)).getName());
        Assert.assertEquals("name2", ((Person)list.get(3)).getName());
        Assert.assertEquals("name3", ((Person)list.get(4)).getName());
        Assert.assertEquals("name4", ((Person)list.get(5)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedRefListRemoveFront() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedRefList().add(new Person("name0"));
        p.getOrderedRefList().add(new Person("name1"));
        p.getOrderedRefList().add(new Person("name2"));
        p.getOrderedRefList().add(new Person("name3"));
        p.getOrderedRefList().add(new Person("name4"));

        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedRefList().remove(0);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedRefList();

        Assert.assertEquals(4, list.size());
        Assert.assertEquals("name1", ((Person)list.get(1)).getName());
        Assert.assertEquals("name2", ((Person)list.get(2)).getName());
        Assert.assertEquals("name3", ((Person)list.get(3)).getName());
        Assert.assertEquals("name4", ((Person)list.get(4)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedRefListAddMiddle() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedRefList().add(new Person("name0"));
        p.getOrderedRefList().add(new Person("name1"));
        p.getOrderedRefList().add(new Person("name2"));
        p.getOrderedRefList().add(new Person("name3"));
        p.getOrderedRefList().add(new Person("name4"));

        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedRefList().add(3, new Person("name3"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedRefList();

        Assert.assertEquals(6, list.size());
        Assert.assertEquals("name0", ((Person)list.get(0)).getName());
        Assert.assertEquals("name1", ((Person)list.get(1)).getName());
        Assert.assertEquals("name2", ((Person)list.get(2)).getName());
        Assert.assertEquals("name3", ((Person)list.get(3)).getName());
        Assert.assertEquals("name3", ((Person)list.get(4)).getName());
        Assert.assertEquals("name4", ((Person)list.get(5)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedRefListRemoveMiddle() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedRefList().add(new Person("name0"));
        p.getOrderedRefList().add(new Person("name1"));
        p.getOrderedRefList().add(new Person("name2"));
        p.getOrderedRefList().add(new Person("name3"));
        p.getOrderedRefList().add(new Person("name4"));

        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getOrderedRefList().remove(3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedRefList();

        Assert.assertEquals(4, list.size());
        Assert.assertEquals("name0", ((Person)list.get(0)).getName());
        Assert.assertEquals("name1", ((Person)list.get(1)).getName());
        Assert.assertEquals("name2", ((Person)list.get(2)).getName());
        Assert.assertEquals("name4", ((Person)list.get(3)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testOrderedRefListGenUpdates() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person();
        p.getOrderedRefList().add(new Person("name0"));
        p.getOrderedRefList().add(new Person("name1"));
        p.getOrderedRefList().add(new Person("name2"));
        p.getOrderedRefList().add(new Person("name3"));
        p.getOrderedRefList().add(new Person("name4"));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ((Person)p.getOrderedRefList().get(3)).setName("name10");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        List list = p.getOrderedRefList();

        Assert.assertEquals(5, list.size());
        Assert.assertEquals("name0", ((Person)list.get(0)).getName());
        Assert.assertEquals("name1", ((Person)list.get(1)).getName());
        Assert.assertEquals("name2", ((Person)list.get(2)).getName());
        Assert.assertEquals("name10", ((Person)list.get(3)).getName());
        Assert.assertEquals("name4", ((Person)list.get(4)).getName());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ((Person)p.getOrderedRefList().get(3)).setName("name13");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        list = p.getOrderedRefList();

        Assert.assertEquals(5, list.size());
        Assert.assertEquals("name0", ((Person)list.get(0)).getName());
        Assert.assertEquals("name1", ((Person)list.get(1)).getName());
        Assert.assertEquals("name2", ((Person)list.get(2)).getName());
        Assert.assertEquals("name13", ((Person)list.get(3)).getName());
        Assert.assertEquals("name4", ((Person)list.get(4)).getName());
        pm.currentTransaction().commit();
        pm.close();
    }

    /**
     * Test to ensure that the ref values in collection is updated
     *
     * @throws Exception
     */
    public void testRefCollection() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.getRefCol().add(new Person("p1"));
        p.getRefCol().add(new Person("p2"));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(p.getRefCol().size() == 2);
        Collection col = p.getRefCol();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            Person person = (Person)iterator.next();
            Debug.OUT.println("name = " + person.getName());
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().setRetainValues(true);
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        p.getRefCol().add(new Person("p3"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        p.getRefCol().clear();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(p.getRefCol().size() == 0);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test to ensure that the ref values in collection is updated
     *
     * @throws Exception
     */
    public void testSetCollectionNull() throws Exception {
        if (true) {
            broken();
            return;
        }
        if (!isNullCollectionSupported()) {
            unsupported();
            return;
        }

        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Person p = new Person("name");
        p.getRefCol().add(new Person("p1"));
        p.getRefCol().add(new Person("p2"));
        pm.makePersistent(p);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertTrue(p.getRefCol().size() == 2);
        pm.currentTransaction().commit();

        // set null to the collection
        pm.currentTransaction().begin();
        p.setRefCol(null);
        pm.currentTransaction().commit();

        // get the null back
        pm.currentTransaction().begin();
        Assert.assertTrue(p.getRefCol() == null);
        pm.currentTransaction().commit();

        pm.close();
    }

}
