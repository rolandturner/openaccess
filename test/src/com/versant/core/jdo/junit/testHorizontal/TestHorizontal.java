
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
package com.versant.core.jdo.junit.testHorizontal;

import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.testHorizontal.model.horizontal.*;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Date;

/**
 */
public class TestHorizontal extends VersantTestCase {

    /**
     * This is a scenario where a name clash between a user named field and the
     * auto named datastore pk key.
     */
    public void testInsertOfProtein() {
        if (true) {
            broken();
            return;
        }
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Protein p = new Protein();
        pm.makePersistent(p);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInheritancebeModel() {
		PersistenceManager pm = pmf().getPersistenceManager();
		pm.currentTransaction().begin();

		Customer customer = new Customer();
		long millis = (new Date()).getTime();
		customer.setAge(24);
		customer.setAmountSpent(25);
		customer.setName("dave");
		customer.setNumberOfPurchases(26);
		customer.setPersonID(millis);
        pm.makePersistent(customer);
        pm.currentTransaction().commit();
        pm.close();


		// select a.1, a.2, ..., b.1, b.2, ... from table1 a, table2 b where a.id1 = b.id1 and a.id2 = b.id2 and a.oid= X

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        System.out.println("\n\n\n\nbefore ");
		Customer.ID id = new Customer.ID("" + millis);
		customer = (Customer) pm.getObjectById(id, true);
		System.out.println("Customer="+customer);

        System.out.println("\n\n\nBEFORE COMMIT");
		pm.currentTransaction().commit();
        System.out.println("After commit\n\n\n\n");
		pm.close();
	}

    /**
     * Currently the horizontally mapped class may not be persisted or queried.
     */
    public void testErrorMessageOnQuery() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ConcreteSub1 c = new ConcreteSub1();
        c.setConcreteSub1("b1");
        c.setBaseString("baseString");
        pm.makePersistent(c);

        ConcreteSub2 c2 = new ConcreteSub2();
        c2.setConcreteSub2("b2");
        pm.makePersistent(c2);

        ConcreteSub3 c3 = new ConcreteSub3();
        c3.setConcreteSub3("b3");
        pm.makePersistent(c3);
        pm.currentTransaction().commit();
        pm.close();
        pm = pmf().getPersistenceManager();


        pm.currentTransaction().begin();
        Query q = null;
        q = pm.newQuery(AbsBaseClass.class);
        q.declareParameters("String param");
        q.setFilter("baseString == param");
        Collection res = (Collection) q.execute(null);
        for (Iterator iterator = res.iterator(); iterator.hasNext();) {
            AbsBaseClass absBaseClass = (AbsBaseClass) iterator.next();
            System.out.println("absBaseClass = " + absBaseClass);
        }
        pm.close();
    }

    public void testErrorMessageOnMakePersistent() {
        if (true) {
            broken();
            return;
        }
        fail("Should check makePersistent");
    }


    public void test1() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub1 concreteSub1 = new ConcreteSub1();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub1("sub-" + val);
        concreteSub1.getStringList().add(val);
        pm.makePersistent(concreteSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub1.class);
        List col = (List) q.execute();
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub1) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub1());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        pm.close();
    }

    public void test2() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub1 concreteSub1 = new ConcreteSub1();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub1("sub-" + val);
        concreteSub1.getStringList().add(val);
        pm.makePersistent(concreteSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub1.class);
        q.setFilter("baseString == param");
        q.declareParameters("String param");
        List col = (List) q.execute("base-" + val);
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub1) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub1());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        pm.close();
    }

    public void test3() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub1 concreteSub1 = new ConcreteSub1();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub1("sub-" + val);
        concreteSub1.getStringList().add(val);
        pm.makePersistent(concreteSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub1.class);
        q.declareVariables("String v");
        q.setFilter("stringList.contains(v) && v == param");
        q.declareParameters("String param");
        List col = (List) q.execute(val);
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub1) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub1());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        pm.close();
    }

    public void test4() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub3 concreteSub1 = new ConcreteSub3();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub3("sub-" + val);
        concreteSub1.getStringList().add(val);
        pm.makePersistent(concreteSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub3.class);
        q.declareVariables("String v");
        q.setFilter("stringList.contains(v) && v == param");
        q.declareParameters("String param");
        List col = (List) q.execute(val);
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub3) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub3());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        pm.close();
    }

    public void test5() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub2 concreteSub1 = new ConcreteSub2();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub2("sub-" + val);
        concreteSub1.getStringList().add(val);
        pm.makePersistent(concreteSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub2.class);
        q.declareVariables("String v");
        q.setFilter("stringList.contains(v) && v == param");
        q.declareParameters("String param");
        List col = (List) q.execute(val);
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub2) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub2());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        pm.close();
    }

    public void testPCCollection1() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub2 concreteSub1 = new ConcreteSub2();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub2("sub-" + val);

        concreteSub1.getRefClassList().add(new RefClass(val));
        pm.makePersistent(concreteSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub2.class);
        q.declareVariables("RefClass v");
        q.setFilter("refClassList.contains(v) && v.val == param");
        q.declareParameters("String param");
        List col = (List) q.execute(val);
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub2) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub2());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        assertEquals(val, ((RefClass)concreteSub1.getRefClassList().get(0)).getVal());
        pm.close();
    }

    public void testPCMap1() {
        if (true) {
            broken();
            return;
        }
        
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub2 concreteSub1 = new ConcreteSub2();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub2("sub-" + val);

        concreteSub1.getStringRefMap().put(val + "-1", new RefClass(val + "-1"));
        concreteSub1.getStringRefMap().put(val + "-2", new RefClass(val + "-2"));
        pm.makePersistent(concreteSub1);

        concreteSub1 = new ConcreteSub2();
        concreteSub1.getStringRefMap().put(val + "-3", new RefClass(val + "-4"));
        concreteSub1.getStringRefMap().put(val + "-4", new RefClass(val + "-4"));
        pm.makePersistent(concreteSub1);

        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub2.class);
        q.declareVariables("String v");
        q.setFilter("stringRefMap.containsKey(v) && v == param");
        q.declareParameters("String param");
        List col = (List) q.execute(val + "-2");
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub2) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub2());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        assertEquals(val + "-2", concreteSub1.getStringRefMap().get(val + "-2"));
        pm.close();
    }

    public void testPCMap2() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub2 concreteSub1 = new ConcreteSub2();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub2("sub-" + val);

        concreteSub1.getStringRefMap().put(val + "-1", new RefClass(val + "-1"));
        concreteSub1.getStringRefMap().put(val + "-2", new RefClass(val + "-2"));
        pm.makePersistent(concreteSub1);

        concreteSub1 = new ConcreteSub2();
        concreteSub1.getStringRefMap().put(val + "-3", new RefClass(val + "-3"));
        concreteSub1.getStringRefMap().put(val + "-4", new RefClass(val + "-4"));
        pm.makePersistent(concreteSub1);

        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(ConcreteSub2.class);
        q.declareVariables("RefClass v");
        q.setFilter("stringRefMap.contains(v) && v.val == param");
        q.declareParameters("String param");
        List col = (List) q.execute(val + "-2");
        assertEquals(1, col.size());
        concreteSub1 = (ConcreteSub2) col.get(0);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub2());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        assertEquals(val + "-2", ((RefClass)concreteSub1.getStringRefMap().get(val + "-2")).getVal());
        pm.close();
    }

    public void testTransactionalFields() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        ConcreteSub1 cs1 = new ConcreteSub1();
        cs1.setTxField("txFieldVal");
        cs1.setBaseString("baseString");
        pm.makePersistent(cs1);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(cs1);
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        cs1 = (ConcreteSub1) pm.getObjectById(id, false);
        System.out.println("cs1.getBaseString() = " + cs1.getBaseString());
        System.out.println("cs1.getTxField() = " + cs1.getTxField());
        pm.currentTransaction().commit();
        pm.close();
    }

    public void test6() {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        ConcreteSub3 concreteSub1 = new ConcreteSub3();
        String val = "" + System.identityHashCode(concreteSub1) + System.currentTimeMillis();
        concreteSub1.setBaseString("base-" + val);
        concreteSub1.setConcreteSub3("sub-" + val);
        concreteSub1.getStringList().add(val);
        pm.makePersistent(concreteSub1);
        Object id = pm.getObjectId(concreteSub1);
        System.out.println("id = " + id);
        pm.currentTransaction().commit();
        assertEquals(id, pm.getObjectId(concreteSub1));
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        concreteSub1 = (ConcreteSub3) pm.getObjectById(id, false);
        assertEquals("sub-" + val, concreteSub1.getConcreteSub3());
        assertEquals("base-" + val, concreteSub1.getBaseString());
        pm.close();
    }

    public void testAppIdBase1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        AppIdSub1 appIdSub1 = new AppIdSub1();
        String val = "" + System.currentTimeMillis();
        appIdSub1.setVal1(val + "-1");
        appIdSub1.setPk(3);
        pm.makePersistent(appIdSub1);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Object id = pm.newObjectIdInstance(AppIdSub1.class, "3");
        appIdSub1 = (AppIdSub1) pm.getObjectById(id, false);
        assertEquals(val + "-1", appIdSub1.getVal1());
        pm.currentTransaction().commit();
        pm.close();


    }

}
