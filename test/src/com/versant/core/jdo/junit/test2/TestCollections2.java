
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
package com.versant.core.jdo.junit.test2;

import com.versant.core.jdo.junit.test2.model.*;
import com.versant.core.jdo.junit.test2.model.bair.*;
import com.versant.core.jdo.junit.VersantTestCase;

import javax.jdo.*;

import junit.framework.Assert;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;

/**
 * More tests for Collection's and Map's.
 */
public class TestCollections2 extends VersantTestCase {

    public void testNullValueInMap() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        ClassWithMap cwm = new ClassWithMap(name);
        cwm.getMap().put("key-0-" + name, null);
        cwm.getMap().put("key-1-" + name, "value-1-" + name);
        pm.makePersistent(cwm);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(2, cwm.getMap().size());
        assertEquals(null, cwm.getMap().get("key-0-" + name));
        assertEquals("value-1-" + name, cwm.getMap().get("key-1-" + name));
        cwm.getMap().put("value-2-" + name, null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(3, cwm.getMap().size());
        assertEquals(null, cwm.getMap().get("key-0-" + name));
        assertEquals("value-1-" + name, cwm.getMap().get("key-1-" + name));
        assertEquals(null, cwm.getMap().get("key-2-" + name));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(3, cwm.getMap().size());
        cwm.getMap().remove("key-0-" + name);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(2, cwm.getMap().size());
        assertEquals("value-1-" + name, cwm.getMap().get("key-1-" + name));
        assertEquals(null, cwm.getMap().get("key-2-" + name));
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testNullValueInMap2() {
    	if (!isExtraJavaTypeSupported()) { // java.util.Property
    		return;
    	}
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        ArticleCategory cwm = new ArticleCategory();
        cwm.getLnkProperties().put("key-0-" + name, null);
        cwm.getLnkProperties().put("key-1-" + name, new Property("prop1-" + name));
        pm.makePersistent(cwm);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        assertEquals(2, cwm.getLnkProperties().size());
        System.out.println("cwm.getLnkProperties() = " + cwm.getLnkProperties());
        assertEquals(null, cwm.getLnkProperties().get("key-0-" + name));
        assertEquals("prop1-" + name, ((Property)cwm.getLnkProperties().get("key-1-" + name)).getName());

        cwm.getLnkProperties().put("key-2-" + name, null);
        pm.currentTransaction().commit();



        pm.currentTransaction().begin();
        assertEquals(3, cwm.getLnkProperties().size());
        System.out.println("\n\n\n\n\n\n\n\n\n**************************** cwm.getLnkProperties() = " + cwm.getLnkProperties());

        assertTrue(cwm.getLnkProperties().containsKey("key-0-" + name));
        assertEquals(null, cwm.getLnkProperties().get("key-0-" + name));

        assertTrue(cwm.getLnkProperties().containsKey("key-1-" + name));
        assertEquals("prop1-" + name, ((Property)cwm.getLnkProperties().get("key-1-" + name)).getName());

        assertTrue(cwm.getLnkProperties().containsKey("key-2-" + name));
        assertEquals(null, cwm.getLnkProperties().get("key-2-" + name));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwm.getLnkProperties().put("key-1-" + name, null);
        pm.currentTransaction().commit();


        pm.currentTransaction().begin();
        assertEquals(3, cwm.getLnkProperties().size());
        System.out.println("\n\n\n\n\n\n\n\n\n**************************** cwm.getLnkProperties() = " + cwm.getLnkProperties());

        assertTrue(cwm.getLnkProperties().containsKey("key-0-" + name));
        assertEquals(null, cwm.getLnkProperties().get("key-0-" + name));

        assertTrue(cwm.getLnkProperties().containsKey("key-1-" + name));
        assertEquals(null, (Property)cwm.getLnkProperties().get("key-1-" + name));

        assertTrue(cwm.getLnkProperties().containsKey("key-2-" + name));
        assertEquals(null, cwm.getLnkProperties().get("key-2-" + name));
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testNullElementInList() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        ClassWithList cwl = new ClassWithList(name);
        pm.makePersistent(cwl);
        cwl.getList().add(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getList().size());
        Assert.assertNull(cwl.getList().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getList().set(0, "bla");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getList().size());
        Assert.assertEquals("bla", cwl.getList().get(0));
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testNullElementInArrayList() {
    	if (!isQueryNullElementInCollectionSupported())
    		return;
    	
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        ClassWithList cwl = new ClassWithList(name);
        pm.makePersistent(cwl);
        cwl.getArrayList().add(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getArrayList().size());
        Assert.assertNull(cwl.getArrayList().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getArrayList().set(0, name + "-0");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getArrayList().size());
        Assert.assertEquals(name + "-0", cwl.getArrayList().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getArrayList().add(null);
        cwl.getArrayList().add(name + "-2");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, cwl.getArrayList().size());
        Assert.assertEquals(name + "-0", cwl.getArrayList().get(0));
        Assert.assertNull(cwl.getArrayList().get(1));
        Assert.assertEquals(name + "-2", cwl.getArrayList().get(2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ClassWithList.class);
        q.setFilter("arrayList.contains(param)");
        q.declareParameters("String param");
        List results = (List) q.execute(name + "-0");
        Assert.assertEquals(1, results.size());
        q.closeAll();

        results = (List)q.execute(null);
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testNullElementInLinkedList() {
    	if (!isQueryNullElementInCollectionSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        ClassWithList cwl = new ClassWithList(name);
        pm.makePersistent(cwl);
        cwl.getLinkedList().add(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getLinkedList().size());
        Assert.assertNull(cwl.getLinkedList().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getLinkedList().set(0, name + "-0");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getLinkedList().size());
        Assert.assertEquals(name + "-0", cwl.getLinkedList().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getLinkedList().add(name + "-1");
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, cwl.getLinkedList().size());
        Assert.assertEquals(name + "-0", cwl.getLinkedList().get(0));
        Assert.assertEquals(name + "-1", cwl.getLinkedList().get(1));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getLinkedList().add(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, cwl.getLinkedList().size());
        Assert.assertEquals(name + "-0", cwl.getLinkedList().get(0));
        Assert.assertEquals(name + "-1", cwl.getLinkedList().get(1));
        Assert.assertNull(cwl.getLinkedList().get(2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ClassWithList.class);
        q.setFilter("linkedList.contains(param)");
        q.declareParameters("String param");
        List results = (List) q.execute(name + "-0");
        Assert.assertEquals(1, results.size());
        q.closeAll();

        results = (List)q.execute(null);
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    public void testNullElementInPcLinkedList() {
    	if (!isQueryNullElementInCollectionSupported())
    		return;

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String name = "" + System.currentTimeMillis();
        ClassWithList cwl = new ClassWithList(name);
        pm.makePersistent(cwl);
        cwl.getPcLinkedList().add(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getPcLinkedList().size());
        Assert.assertNull(cwl.getPcLinkedList().get(0));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getPcLinkedList().set(0, new Country2(name + "-code-0", name + "-name-0"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(1, cwl.getPcLinkedList().size());
        Assert.assertEquals(name + "-code-0", ((Country2)cwl.getPcLinkedList().get(0)).getCode());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getPcLinkedList().add(new Country2(name + "-code-1", name + "-name-1"));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(2, cwl.getPcLinkedList().size());
        Assert.assertEquals(name + "-code-0", ((Country2)cwl.getPcLinkedList().get(0)).getCode());
        Assert.assertEquals(name + "-code-1", ((Country2)cwl.getPcLinkedList().get(1)).getCode());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        cwl.getPcLinkedList().add(null);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(3, cwl.getPcLinkedList().size());
        Assert.assertEquals(name + "-code-0", ((Country2)cwl.getPcLinkedList().get(0)).getCode());
        Assert.assertEquals(name + "-code-1", ((Country2)cwl.getPcLinkedList().get(1)).getCode());
        Assert.assertNull(cwl.getPcLinkedList().get(2));
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(ClassWithList.class);
        q.setFilter("pcLinkedList.contains(variable) && variable.code == param");
        q.declareVariables("Country2 variable");
        q.declareParameters("String param");
        List results = (List) q.execute(name + "-code-0");
        Assert.assertEquals(1, results.size());
        q.closeAll();

        Query q2 = pm.newQuery(ClassWithList.class);
        q2.setFilter("pcLinkedList.contains(param)");
        q2.declareParameters("Country2 param");
        results = (List)q2.execute(null);
        Assert.assertEquals(1, results.size());
        q.closeAll();

        pm.close();
    }

    /**
     * Test that fetching a one-to-many collection with a query will put the
     * elements into the level 2 cache with non tx reads.
     */
    public void testOneToManyQueryFetchCacheNonTx() throws Exception {
        testOneToManyQueryFetchCacheImp(false);
    }

    /**
     * Test that fetching a one-to-many collection will put the elements
     * into the level 2 cache with non tx reads.
     */
    public void testOneToManyFetchCacheNonTx() throws Exception {
        testOneToManyFetchCacheImp(false);
    }

    /**
     * Test that fetching a one-to-many collection will put the elements
     * into the level 2 cache in an optimistic tx.
     */
    public void testOneToManyFetchCacheOptTx() throws Exception {
        testOneToManyFetchCacheImp(true);
    }

    private void testOneToManyQueryFetchCacheImp(boolean tx) {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create stuff to query for
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        OneSide one = new OneSide("one");
        ManySide many0 = new ManySide("many0");
        ManySide many1 = new ManySide("many1");
        one.getList().add(many0);
        one.getList().add(many1);
        pm.makePersistent(one);
        Object oidOne = pm.getObjectId(one);
        Object oidMany0 = pm.getObjectId(many0);
        Object oidMany1 = pm.getObjectId(many1);
        pm.currentTransaction().commit();

        // new pm to avoid local cache
        pm.close();
        pm = pmf().getPersistenceManager();

        System.out.println("\n*** get it back " + (tx ? "opt-tx" : "non-tx") +
            "  and make sure instances go into level 2 cache");
        if (tx) {
            pm.currentTransaction().setOptimistic(true);
            pm.currentTransaction().begin();
        } else {
            pm.currentTransaction().setNontransactionalRead(true);
        }
        one = fetchOneSideWithQuery(pm);
        System.out.println("*** one.getName() = " + one.getName());
        List list = one.getList();
        System.out.println("*** one.getList() = " + list);
        Assert.assertTrue(pmf().isInCache(oidOne));
        Assert.assertTrue(pmf().isInCache(oidMany0));
        Assert.assertTrue(pmf().isInCache(oidMany1));
        if (tx) pm.currentTransaction().commit();

        // new pm to avoid local cache
        pm.close();
        pm = pmf().getPersistenceManager();

        System.out.println("\n*** get it back again and make sure no SQL is " +
            "executed as everything should come from the level 2 cache");
        if (tx) {
            pm.currentTransaction().setOptimistic(true);
            pm.currentTransaction().begin();
        } else {
            pm.currentTransaction().setNontransactionalRead(true);
        }
        countExecQueryEvents(); // clear counts
        one = fetchOneSideWithQuery(pm);
        System.out.println("*** one.getName() = " + one.getName());
        Assert.assertEquals(0, countExecQueryEvents());
        list = one.getList();
        System.out.println("*** one.getList() = " + list);
        Assert.assertEquals(0, countExecQueryEvents());
        if (tx) pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.deletePersistentAll(one.getList());
        pm.deletePersistent(one);
        pm.currentTransaction().commit();

        pm.close();
    }

    private OneSide fetchOneSideWithQuery(PersistenceManager pm) {
        Query q = pm.newQuery(OneSide.class, "name.startsWith(\"one\")");
        Collection ans = (Collection)q.execute();
        OneSide one = (OneSide)ans.iterator().next();
        q.closeAll();
        return one;
    }

    private void testOneToManyFetchCacheImp(boolean tx) {
        PersistenceManager pm = pmf().getPersistenceManager();

        // create stuff to query for
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        OneSide one = new OneSide("one");
        ManySide many0 = new ManySide("many0");
        ManySide many1 = new ManySide("many1");
        one.getList().add(many0);
        one.getList().add(many1);
        pm.makePersistent(one);
        Object oidOne = pm.getObjectId(one);
        Object oidMany0 = pm.getObjectId(many0);
        Object oidMany1 = pm.getObjectId(many1);
        pm.currentTransaction().commit();

        // new pm to avoid local cache
        pm.close();
        pm = pmf().getPersistenceManager();

        System.out.println("\n*** get it back " + (tx ? "opt-tx" : "non-tx") +
            "  and make sure instances go into level 2 cache");
        if (tx) {
            pm.currentTransaction().setOptimistic(true);
            pm.currentTransaction().begin();
        } else {
            pm.currentTransaction().setNontransactionalRead(true);
        }
        one = (OneSide)pm.getObjectById(oidOne, true);
        System.out.println("*** one.getName() = " + one.getName());
        List list = one.getList();
        System.out.println("*** one.getList() = " + list);
        Assert.assertTrue(pmf().isInCache(oidOne));
        Assert.assertTrue(pmf().isInCache(oidMany0));
        Assert.assertTrue(pmf().isInCache(oidMany1));
        if (tx) pm.currentTransaction().commit();

        // new pm to avoid local cache
        pm.close();
        pm = pmf().getPersistenceManager();

        System.out.println("\n*** get it back again and make sure no SQL is " +
            "executed as everything should come from the level 2 cache");
        if (tx) {
            pm.currentTransaction().setOptimistic(true);
            pm.currentTransaction().begin();
        } else {
            pm.currentTransaction().setNontransactionalRead(true);
        }
        countExecQueryEvents(); // clear counts
        one = (OneSide)pm.getObjectById(oidOne, true);
        System.out.println("*** one.getName() = " + one.getName());
        Assert.assertEquals(0, countExecQueryEvents());
        list = one.getList();
        System.out.println("*** one.getList() = " + list);
        Assert.assertEquals(0, countExecQueryEvents());
        if (tx) pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        pm.deletePersistentAll(one.getList());
        pm.deletePersistent(one);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test persisting a deep graph of collections. This is a test to
     * attempt to reproduce forums 536. So far we have not got it to fail.
     */
    public void testPersistDeepCollectionGraph() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // persist a deep collection graph
        pm.currentTransaction().begin();
        LaborItem laborItem = new LaborItem("laborItem");
        Part part = new PartRequest("partRequest");
        laborItem.getParts().add(part);
        Authorization auth = new Authorization("auth");
        auth.getLaborItems().add(laborItem);
        RepairOrder ro = new CurrentRepairOrder("ro");
        ro.getAuthorizations().add(auth);
        pm.makePersistent(ro);
        pm.currentTransaction().commit();

//        // check graph persisted ok
//        pm.currentTransaction().begin();
//        pm.currentTransaction().commit();
//
        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(laborItem);
        pm.deletePersistent(part);
        pm.deletePersistent(auth);
        pm.deletePersistent(ro);
        pm.currentTransaction().commit();

	pm.close();
    }

    /**
     * Test removing a key from a map and deleting it in the same tx (forums 512
     */
    public void testRemoveAndDeleteMapKey() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make some containers with maps
        pm.currentTransaction().begin();
        int n = 5;
        Address task = new Address("key1");
        pm.makePersistent(task);
        Object taskOID = pm.getObjectId(task);
        Object[] containerOID = new Object[n];
        for (int i = 0; i < n; i++) {
            Container c = new Container("oink");
            c.getMap().put(task, "val1");
            c.getMap().put(new Address("key2"), "val2");
            pm.makePersistent(c);
            containerOID[i] = pm.getObjectId(c);
        }
        pm.currentTransaction().commit();

        pm.close();
        pm = pmf().getPersistenceManager();

        // remove task from the maps that contain it and delete it
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Container.class, "map.containsKey(p)");
        q.declareParameters("Address p");
        task = (Address)pm.getObjectById(taskOID, true);
        Collection ans = (Collection)q.execute(task);
        for (Iterator i = ans.iterator(); i.hasNext(); ) {
            Container c = (Container)i.next();
            c.getMap().remove(task);
        }
        pm.deletePersistent(task);
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        for (int i = 0; i < n; i++) {
            pm.deletePersistent(pm.getObjectById(containerOID[i], true));
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Test adding to the end of a list by index.
     */
    public void testAddEndOfListByIndex() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // test add(int,object)
        pm.currentTransaction().begin();
        ClassWithList o = new ClassWithList("oink");
        pm.makePersistent(o);   // create SCOs
        testAddEndOfListByIndexImp(o.getList());
        testAddEndOfListByIndexImp(o.getArrayList());
        pm.currentTransaction().commit();

        // check lists are still ok
        pm.currentTransaction().begin();
        Assert.assertEquals("abcd", tos(o.getList()));
        Assert.assertEquals("abcd", tos(o.getArrayList()));
        pm.currentTransaction().commit();

        // test addAll(int,Collection)
        pm.currentTransaction().begin();
        o.getList().clear();
        o.getArrayList().clear();
        testAddAllEndOfListByIndexImp(o.getList());
        testAddAllEndOfListByIndexImp(o.getArrayList());
        pm.currentTransaction().commit();

        // check lists are still ok
        pm.currentTransaction().begin();
        Assert.assertEquals("abcde", tos(o.getList()));
        Assert.assertEquals("abcde", tos(o.getArrayList()));
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    private void testAddEndOfListByIndexImp(List list) {
        list.add(0, "b");
        list.add(1, "c");
        list.add(2, "d");
        list.add(0, "a");
        Assert.assertEquals("abcd", tos(list));
    }

    private void testAddAllEndOfListByIndexImp(List list) {
        list.addAll(0, Arrays.asList(new String[]{"a", "b", "c", "d", "e"}));
        Assert.assertEquals("abcde", tos(list));
    }

    private static String tos(Collection c) {
        StringBuffer s = new StringBuffer();
        for (Iterator i = c.iterator(); i.hasNext(); ) s.append(i.next());
        return s.toString();
    }

    /**
     * Make sure a TreeMap maintains its ordering properly.
     */
    public void testTreeMap() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithTreeMap o = new ClassWithTreeMap("oink");
        o.add("e", "1");
        o.add("b", "2");
        o.add("c", "3");
        o.add("d", "4");
        o.add("a", "5");
        Assert.assertEquals("abcde", o.getMapString());
        pm.makePersistent(o);
        Assert.assertEquals("abcde", o.getMapString());
        pm.currentTransaction().commit();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        Assert.assertEquals("abcde", o.getMapString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a TreeMap maintains its ordering properly when used with
     * a user defined Comparator.
     */
    public void testTreeMapComparator() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithTreeMap o = new ClassWithTreeMap("oink");
        o.addRev("e", "1");
        o.addRev("b", "2");
        o.addRev("a", "3");
        o.addRev("d", "4");
        o.addRev("c", "5");
        Assert.assertEquals("edcba", o.getRevMapString());
        pm.makePersistent(o);
        Assert.assertEquals("edcba", o.getRevMapString());
        pm.currentTransaction().commit();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        Assert.assertEquals("edcba", o.getRevMapString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a SortedMap maintains its ordering properly.
     */
    public void testSortedMap() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithSortedMap o = new ClassWithSortedMap("oink");
        o.add("e", "1");
        o.add("b", "2");
        o.add("c", "3");
        o.add("d", "4");
        o.add("a", "5");
        Assert.assertEquals("abcde", o.getMapString());
        pm.makePersistent(o);
        Assert.assertEquals("abcde", o.getMapString());
        pm.currentTransaction().commit();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        Assert.assertEquals("abcde", o.getMapString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a SortedMap maintains its ordering properly when used with
     * a user defined Comparator.
     */
    public void testSortedMapComparator() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithSortedMap o = new ClassWithSortedMap("oink");
        o.addRev("e", "1");
        o.addRev("b", "2");
        o.addRev("a", "3");
        o.addRev("d", "4");
        o.addRev("c", "5");
        Assert.assertEquals("edcba", o.getRevMapString());
        pm.makePersistent(o);
        Assert.assertEquals("edcba", o.getRevMapString());
        pm.currentTransaction().commit();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        Assert.assertEquals("edcba", o.getRevMapString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a TreeSet maintains its ordering properly.
     */
    public void testTreeSet() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithTreeSet o = new ClassWithTreeSet("oink");
        o.add("e");
        o.add("b");
        o.add("c");
        o.add("d");
        o.add("a");
        Assert.assertEquals("abcde", o.getSetString());
        pm.makePersistent(o);
        Assert.assertEquals("abcde", o.getSetString());
        pm.currentTransaction().commit();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        Assert.assertEquals("abcde", o.getSetString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a TreeSet maintains its ordering properly when used with
     * a user defined Comparator.
     */
    public void testTreeSetComparator() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithTreeSet o = new ClassWithTreeSet("oink");
        o.addRev("e");
        o.addRev("b");
        o.addRev("c");
        o.addRev("d");
        o.addRev("a");
        Assert.assertEquals("edcba", o.getRevSetString());
        pm.makePersistent(o);
        Assert.assertEquals("edcba", o.getRevSetString());
        pm.currentTransaction().commit();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        Assert.assertEquals("edcba", o.getRevSetString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a SortedSet maintains its ordering properly.
     */
    public void testSortedSet() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithSortedSet o = new ClassWithSortedSet("oink");
        o.add("e");
        o.add("b");
        o.add("c");
        o.add("d");
        o.add("a");
        Assert.assertEquals("abcde", o.getSetString());
        pm.makePersistent(o);
        Assert.assertEquals("abcde", o.getSetString());
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(o);

        pm.close();
        pm = pmf().getPersistenceManager();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        o = (ClassWithSortedSet)pm.getObjectById(oid, true);
        Assert.assertEquals("abcde", o.getSetString());
        pm.currentTransaction().commit();

        pm.close();
    }

    /**
     * Make sure a SortedSet maintains its ordering properly when used with
     * a user defined Comparator.
     */
    public void testSortedSetComparator() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        // make sure order is correct before and after makePersistent
        pm.currentTransaction().begin();
        ClassWithSortedSet o = new ClassWithSortedSet("oink");
        o.addRev("e");
        o.addRev("b");
        o.addRev("c");
        o.addRev("d");
        o.addRev("a");
        Assert.assertEquals("edcba", o.getRevSetString());
        pm.makePersistent(o);
        Assert.assertEquals("edcba", o.getRevSetString());
        pm.currentTransaction().commit();
        Object oid = pm.getObjectId(o);

        pm.close();
        pm = pmf().getPersistenceManager();

        // make sure the order is correct after retrieval
        pm.currentTransaction().begin();
        o = (ClassWithSortedSet)pm.getObjectById(oid, true);
        Assert.assertEquals("edcba", o.getRevSetString());
        pm.currentTransaction().commit();

        // cleanup
        pm.currentTransaction().begin();
        pm.deletePersistent(o);
        pm.currentTransaction().commit();

        pm.close();
    }

}
