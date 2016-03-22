
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
/*
 * Created on Sep 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.versant.core.jdo.junit.test1;

import java.util.*;
import junit.framework.Assert;

import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test1.model.*;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOHelper;


/**
 * @author hzhao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestArrayCollection extends VersantTestCase {
	
	VersantPersistenceManagerFactory pmf2;
	
	public VersantPersistenceManagerFactory pmf2() {
        if (pmf2 == null) {
            pmf2 = (VersantPersistenceManagerFactory)JDOHelper.getPersistenceManagerFactory(
                    getProperties());
            if (isRemote()) {
                setLogEventsToSysOut(false);
            }
        }
        return pmf2;
    }

	private void testNullElement(boolean embedded) {
		PersistenceManager pm = pmf().getPersistenceManager();

		pm.currentTransaction().begin();
		IndexedNode in1 = new IndexedNode("in1");
		IndexedNode in2 = new IndexedNode("in2");
		pm.makePersistent(in1);
		in1.getChildren().add(in2);
		in1.getChildren().add(null);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		IndexedNode in3 = new IndexedNode("in3");
		in1.getChildren().add(1, in3);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		Assert.assertEquals("in3", ((IndexedNode)in1.getChildren().get(1)).getName());
		Assert.assertEquals(null, in1.getChildren().get(2));
		Assert.assertEquals("[<null>, in2, in3]", in1.getChildrenStr());
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		in1.getChildren().set(1, null);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		Assert.assertEquals(null, in1.getChildren().get(1));
		Assert.assertEquals(null, in1.getChildren().get(2));
		Assert.assertEquals("[<null>, <null>, in2]", in1.getChildrenStr());
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		in1.getChildren().set(1, in3);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		Assert.assertEquals("in3", ((IndexedNode)in1.getChildren().get(1)).getName());
		Assert.assertEquals(null, in1.getChildren().get(2));
		Assert.assertEquals("[<null>, in2, in3]", in1.getChildrenStr());
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		ArrayList list = new ArrayList();
		list.add(in1);
		list.add(null);
		list.add(in2);
		list.add(null);
		in1.setChildren(list);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		Assert.assertEquals("in1", ((IndexedNode)in1.getChildren().get(0)).getName());
		Assert.assertEquals(null, in1.getChildren().get(1));
		Assert.assertEquals("in2", ((IndexedNode)in1.getChildren().get(2)).getName());
		Assert.assertEquals("[<null>, <null>, in1, in2]", in1.getChildrenStr());
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		list = null;
		in1.setChildren(list);
		pm.currentTransaction().commit();

		pm.currentTransaction().begin();
		Assert.assertEquals(null, in1.getChildren());
		pm.currentTransaction().commit();

		pm.close();
	}

	private void testMap(boolean embedded) {
		PersistenceManager pm = pmf().getPersistenceManager();

		// store a Node
		pm.currentTransaction().begin();
		ArrayCollection em = new ArrayCollection("em1");
		pm.makePersistent(em);
		pm.currentTransaction().commit();
		pm.evictAll();
		Object oid = pm.getObjectId(em);
		System.out.println("oid = " + oid);

		pm.currentTransaction().begin();
		HashMap map = new HashMap();
		em.setMap(map, embedded);
		pm.currentTransaction().commit();
		pm.evictAll();

		pm.currentTransaction().begin();
		Node n1 = new Node("n1");
		Node n2 = new Node("n2");
		Node n3 = new Node("n3");
		Node n4 = new Node("n4");

		em.setBirthday(new Date());
		em.setAge(new Integer(20));
//		em.getMap(embedded).put("n1", n1);
//		em.getMap(embedded).put("n2", n2);
//		em.getMap(embedded).put("n3", n3);
//		em.getMap(embedded).put("n4", n4);
		em.getMap(embedded).put(n1, n1);
		em.getMap(embedded).put(n2, n2);
		em.getMap(embedded).put(n3, n3);
		em.getMap(embedded).put(n4, n4);
		em.getSet(embedded).add(n4);

		pm.currentTransaction().commit();
		pm.evictAll();

		// pm.close();
		// pm = pmf2().getPersistenceManager();
		
		// get it back and remove entries
		pm.currentTransaction().begin();

		em = (ArrayCollection)pm.getObjectById(oid, true); 

		Assert.assertEquals("[n1, n2, n3, n4]", em.getMapStr(embedded));
		em.getMap(embedded).remove(n1);
		em.getMap(embedded).remove(n2);
		em.getMap(embedded).put(n1, n1);
		em.getMap(embedded).put(n2, n2);
		pm.currentTransaction().commit();
		pm.evictAll();

		// get it back and remove entries
		pm.currentTransaction().begin();
		Assert.assertEquals("[n1, n2, n3, n4]", em.getMapStr(embedded));
		em.getMap(embedded).remove(n1);
		em.getMap(embedded).remove(n2);
		em.getArraylist(embedded).add(n1);
		em.setBirthday(null);
		em.setAge(null);
		pm.currentTransaction().commit();
		pm.evictAll();

		// get it back and add entries
		pm.currentTransaction().begin();
		Assert.assertEquals("[n3, n4]", em.getMapStr(embedded));
		em.getMap(embedded).put(n2, n2);
		pm.currentTransaction().commit();
		pm.evictAll();

		// set hashmap to null
		pm.currentTransaction().begin();
		Assert.assertEquals("[n2, n3, n4]", em.getMapStr(embedded));
//		em.setMap(null, embedded);
		pm.currentTransaction().commit();
		pm.evictAll();

		// get it back and nuke it
		pm.currentTransaction().begin();
//		Assert.assertEquals(em.getMap(), null);
		pm.deletePersistentAll(new Object[] { em, n1, n2, n3, n4 });
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

	private void testArray(boolean embedded) {
		PersistenceManager pm = pmf().getPersistenceManager();

		// store a Node
		pm.currentTransaction().begin();
		ArrayCollection em = new ArrayCollection("em1");
		Node n1 = new Node("n1");
		Node n2 = new Node("n2");
		Node n3 = new Node("n3");
		Node n4 = new Node("n4");
		pm.makePersistent(n1);
		pm.makePersistent(n2);
		pm.makePersistent(n3);
		pm.makePersistent(n4);
		Node[] arr = new Node[5];
		arr[0] = n1;
		arr[1] = n2;
		arr[2] = null;
		arr[3] = n3;
		arr[4] = n4;
		em.setArrayNode(arr, embedded);
		pm.makePersistent(em);
		pm.currentTransaction().commit();
		pm.evictAll();
		Object oid = pm.getObjectId(em);
		System.out.println("oid = " + oid);

		pm.currentTransaction().begin();
		int[] intArray = new int[5];
		Long[] longArrayW = new Long[5];
		intArray[0] = 0;
		intArray[1] = 1;
		intArray[2] = 2;
		intArray[3] = 3;
		intArray[4] = 4;
		longArrayW[0] = new Long(10);
		longArrayW[1] = new Long(20);
		longArrayW[2] = new Long(30);
		longArrayW[3] = new Long(40);
		longArrayW[4] = new Long(50);
		em.setArrayInt(intArray, embedded);
		em.setArrayLongW(longArrayW, embedded);
		pm.currentTransaction().commit();
		pm.evictAll();

		// get it back and remove entries
		pm.currentTransaction().begin();

		em = (ArrayCollection)pm.getObjectById(oid, true); 

		Assert.assertEquals(null, em.getArrayNode(embedded)[2]);
		Assert.assertEquals("[0, 1, 2, 3, 4]", em.getArrayIntStr(embedded));
		em.setArrayInt(null, embedded);
		pm.currentTransaction().commit();
  		pm.evictAll();

		// get it back and nuke it
		pm.currentTransaction().begin();
		pm.deletePersistentAll(new Object[] { em, n1, n2, n3, n4 });
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

	public void testEmbeddedMap() {
		testMap(true);
	}

	public void testNotEmbeddedMap() {
		testMap(false);
	}

	public void testEmbeddedArray() {
		testArray(true);
	}

	public void testNotEmbeddedArray() {
		testArray(false);
	}

	public void testNotEmbeddedNullElement() {
		testNullElement(false);
	}
}
