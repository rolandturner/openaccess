
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
import com.versant.core.jdo.junit.test0.model.*;

import javax.jdo.PersistenceManager;
import java.util.*;

/**
 * @keep-all
 */
public class TestLists extends VersantTestCase {

    public TestLists(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "testDeleteInstanceInCollection",
            "testDeleteInstanceInUnorderedCollection",
            "testLinkedList",
            "testLinkedListRetrieve",
            "testLinkedListUpdate",
            "testLinkedListAddNull",
            "testLinkedListRemove",
            "testLinkedListListIterAdd",
            "testLinkedListListIterRemove",
            "testVector",
            "testReplaceUnorderedCollection",
            "testOrdering",
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestLists(a[i]));
        }
        return s;
    }

    /**
     * Test the ordering extension.
     */
    public void testOrdering() {
    	if (isVds())
    		return;

    	PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        Ordering o = new Ordering(10);
        o.getEntryList().add(new Entry("a", 1));
        o.getEntryList().add(new Entry("b", 1));
        o.getEntryList().add(new Entry("c", 1));
        o.getEntryList().add(new Entry("d", 2));
        o.getEntryList().add(new Entry("e", 2));

        o.getFkEntryList().add(new FkEntry(o, "a", 1));
        o.getFkEntryList().add(new FkEntry(o, "b", 1));
        o.getFkEntryList().add(new FkEntry(o, "c", 1));
        o.getFkEntryList().add(new FkEntry(o, "d", 2));
        o.getFkEntryList().add(new FkEntry(o, "e", 2));

        o.getStringList().add("a");
        o.getStringList().add("b");
        o.getStringList().add("c");
        o.getStringList().add("d");
        o.getStringList().add("e");
        o.getEntryAddrList().add(new Entry("a", 1, new Address("1")));
        o.getEntryAddrList().add(new Entry("b", 1, new Address("2")));
        o.getEntryAddrList().add(new Entry("c", 1, new Address("3")));
        o.getEntryAddrList().add(new Entry("d", 2, new Address("4")));
        o.getEntryAddrList().add(new Entry("e", 2, new Address("5")));
        o.getEntryDetailList().add(new EntryDetail("E"));
        o.getEntryDetailList().add(new EntryDetail("D"));
        o.getEntryDetailList().add(new EntryDetail("C"));
        o.getEntryDetailList().add(new EntryDetail("B"));
        o.getEntryDetailList().add(new EntryDetail("A"));
        pm.makePersistent(o);
        Object oid = pm.getObjectId(o);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o = (Ordering)pm.getObjectById(oid, true);
        Assert.assertEquals("c b a e d", o.getEntryListString());
        Assert.assertEquals("c b a e d", o.getFkEntryListString());
        Assert.assertEquals("e d c b a", o.getStringListString());
        Assert.assertEquals("1 2 3 4 5", o.getEntryAddrListString());
        Assert.assertEquals("A B C D E", o.getEntryDetailListString());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        o = (Ordering)pm.getObjectById(oid, true);
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test replacing an unordered collection in a persistent instance
     * with a new collection.
     */
    public void testReplaceUnorderedCollection() {

        // persist list with entry 1 and 2
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ListModel lModel = new ListModel();
        lModel.getUnOrderedList().add(new PCCollectionEntry(1));
        lModel.getUnOrderedList().add(new PCCollectionEntry(2));
        pm.makePersistent(lModel);
        Object oid = pm.getObjectId(lModel);
        pm.currentTransaction().commit();
        pm.close();

        // replace list with new list with entries 3 and 4
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        lModel = (ListModel)pm.getObjectById(oid, true);
        lModel.setUnOrderedList(new ArrayList());
        lModel.getUnOrderedList().add(new PCCollectionEntry(3));
        lModel.getUnOrderedList().add(new PCCollectionEntry(4));
        pm.currentTransaction().commit();
        pm.close();

        // check list contains only 3 and 4 after commit
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        lModel = (ListModel)pm.getObjectById(oid, true);
        List l = lModel.getUnOrderedList();
        Assert.assertEquals(l.size(), 2);
        int a = ((PCCollectionEntry)l.get(0)).getUnique();
        int b = ((PCCollectionEntry)l.get(1)).getUnique();
        Assert.assertTrue((a == 3 && b == 4) || (a == 4 && b == 3));
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testDeleteInstanceInCollection() {
        PersistenceManager pm = pmf().getPersistenceManager();
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        lModel.getOrderedList().add(new PCCollectionEntry(1));
        lModel.getOrderedList().add(new PCCollectionEntry(2));
        lModel.getOrderedList().add(new PCCollectionEntry(3));
        lModel.getOrderedList().add(new PCCollectionEntry(4));
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object toDel = lModel.getOrderedList().get(0);
        lModel.getOrderedList().remove(toDel);
        pm.deletePersistent(toDel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, lModel.getOrderedList().size());
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testDeleteInstanceInUnorderedCollection() {
        PersistenceManager pm = pmf().getPersistenceManager();
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        lModel.getUnOrderedList().add(new PCCollectionEntry(1));
        lModel.getUnOrderedList().add(new PCCollectionEntry(2));
        lModel.getUnOrderedList().add(new PCCollectionEntry(3));
        lModel.getUnOrderedList().add(new PCCollectionEntry(4));
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Object toDel = lModel.getUnOrderedList().get(0);
        lModel.getUnOrderedList().remove(toDel);
        pm.deletePersistent(toDel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, lModel.getUnOrderedList().size());
        pm.currentTransaction().commit();
        pm.close();

    }

    public void testVector() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        Vector v = new Vector();
        v.add("bla1");
        lModel.setVector(v);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("bla1", lModel.getVector().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        lModel.getVector().add("bla2");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, lModel.getVector().size());
        Assert.assertEquals("bla1", lModel.getVector().get(0));
        Assert.assertEquals("bla2", lModel.getVector().get(1));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        lModel.getVector().add("bla3");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, lModel.getVector().size());
        Assert.assertEquals("bla1", lModel.getVector().get(0));
        Assert.assertEquals("bla2", lModel.getVector().get(1));
        Assert.assertEquals("bla3", lModel.getVector().get(2));
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testLinkedList() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        testLinkedListImp(pm);
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        testLinkedListImp(pm);
    }

    private void testLinkedListImp(PersistenceManager pm) {
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        LinkedList lList = new LinkedList();
        lList.add("bla");
        lModel.setlList(lList);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testLinkedListRetrieve() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        testLinkedListRetrieveImp(pm);
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        testLinkedListRetrieveImp(pm);
    }

    private void testLinkedListRetrieveImp(PersistenceManager pm) {
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        LinkedList lList = new LinkedList();
        lList.add("bla");
        lModel.setlList(lList);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        LinkedList l = lModel.getlList();
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("bla", l.getFirst());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testLinkedListUpdate() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        testLinkedListUpdateImp(pm);
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        testLinkedListUpdateImp(pm);
    }

    private void testLinkedListUpdateImp(PersistenceManager pm) {
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        LinkedList lList = new LinkedList();
        lList.add("first");
        lModel.setlList(lList);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        LinkedList l = lModel.getlList();
        l.addLast("last");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("first", l.getFirst());
        Assert.assertEquals("last", l.getLast());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        l = lModel.getlList();
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("first", l.getFirst());
        Assert.assertEquals("last", l.getLast());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        l = lModel.getlList();
        l.add(1, "middle");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("first", l.getFirst());
        Assert.assertEquals("last", l.getLast());
        Assert.assertEquals("middle", l.get(1));
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testLinkedListAddNull() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        testLinkedListAddNullImp(pm);
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        testLinkedListAddNullImp(pm);
    }

    private void testLinkedListAddNullImp(PersistenceManager pm) {
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        LinkedList lList = new LinkedList();
        lList.add(null);
        lModel.setlList(lList);
//        try {
            pm.makePersistent(lModel);
//            throw new TestFailedException(
//                    "expected VersantNullElementException");
//        } catch (VersantNullElementException e) {
//            // good
//        }
//
//        // this test cannot continue as we do not allow nulls in collections
//        if (true) {
//            pm.close();
//            return;
//        }

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        LinkedList l = lModel.getlList();
        l.addLast("last");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals(null, l.getFirst());
        Assert.assertEquals("last", l.getLast());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testLinkedListRemove() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        testLinkedListRemoveImp(pm);
        pm = pmf().getPersistenceManager();
        pm.currentTransaction().setRetainValues(true);
        testLinkedListRemoveImp(pm);
    }

    private void testLinkedListRemoveImp(PersistenceManager pm) {
        ListModel lModel = new ListModel();
        pm.currentTransaction().begin();
        LinkedList lList = new LinkedList();
        lList.add("bla");
        lModel.setlList(lList);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        LinkedList l = lModel.getlList();
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("bla", l.getFirst());
        l.removeFirst();
        Assert.assertTrue(l.isEmpty());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        l = lModel.getlList();
        Assert.assertTrue(l.isEmpty());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testLinkedListListIterAdd() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ListModel lModel = new ListModel();
        LinkedList lList = new LinkedList();
        lList.add("bla");
        lModel.setlList(lList);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ListIterator lIter = lModel.getlList().listIterator();
        lIter.next();
        lIter.set("set");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals("set", lModel.getlList().getFirst());
        Assert.assertEquals(1, lModel.getlList().size());
        pm.close();

    }

    public void testLinkedListListIterRemove() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ListModel lModel = new ListModel();
        LinkedList lList = new LinkedList();
        lList.add("bla");
        lModel.setlList(lList);
        pm.makePersistent(lModel);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        ListIterator lIter = lModel.getlList().listIterator();
        lIter.next();
        lIter.remove();
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(0, lModel.getlList().size());
        pm.close();

    }
}

