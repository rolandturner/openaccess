
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
package com.versant.core.jdo.junit.testfg;

import junit.framework.Assert;
import com.versant.core.jdo.VersantQuery;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.TestUtils;
import com.versant.core.jdo.junit.testfg.model.*;
import com.versant.core.jdo.VersantPersistenceManager;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;
import java.util.*;

/**
 * 
 */
public class TestFG extends VersantTestCase {

    public TestFG(String name) {
        super(name);
    }

    public String[] getTestMethods() {
        return new String[]{
            "testFetchFirstFkWithCrossJoin",
            "testMapFetchSize1DS",
            "testMapFetchSize1OPT",
            "testMapFetchSize2DS",
            "testMapFetchSize2OPT",
            "testMapFetchSize3DS",
            "testMapFetchSize3OPT",
            "testMapFetchSize4DS",
            "testMapFetchSize4OPT",
            "testBla",
            "testFGWith2ndLevelOuterJoins",
            "testInheritanceWithRefs1",
            "testInheritanceWithRefs2",
            "testInheritanceWithRefs3",
            "test3LevelDeepListWithFilter",
            "test3LevelDeepListWithFilter2",
            "test3LevelDeepListWithFilter3",
            "test3LevelDeepListWithFilter4",
            "test3LevelDeepListWithFilterFK",
            "test3LevelDeepListWithFilter2FK",
            "test3LevelDeepListWithFilterFK4",
            "test3LevelDeepListWithFilterFK5",
            "testMapWithFilter1",
            "testMapWithFilter2",
            "testMapWithFilter3",
            "testRecursive1",
            "testRecursive11",
            "testRecursive2",
            "testRecursive23",
            "testRecursive22",
            "testInheritance1",
            "testInheritance2",
            "testRefs1",
            "testCol1_N_PM",
            "testCol1_OLD_PM",
            "testCol2",
            "testCol3",
        };
    }

    public void testSelectExpJoinOrdering() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        SubB sb = new SubB();
        sb.setSubB("sb");
        sb.setBase("bBase");
        final Base bBaseRef = new Base();
        bBaseRef.setBase("base");
        sb.setbBaseRef(bBaseRef);
        pm.makePersistent(sb);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(SubB.class);
        q.setFilter("base == \"bBase\"");
        ((VersantQuery)q).setFetchGroup("new2");
        List result = (List) q.execute();
        System.out.println("result = " + result);
        pm.currentTransaction().commit();
        pm.close();
    }


    public void testFetchFirstFkWithCrossJoin() {
        VersantPersistenceManager pm = (VersantPersistenceManager) pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        OrderFK o = new OrderFK();

        RefAB refAB = new RefAB();
        RefBC refBC = new RefBC();
        refBC.setOrder(1);
        refBC.setVal("" + System.currentTimeMillis());
        refAB.setRefBC(refBC);
        o.setRefAB(refAB);
        o.setRefBC(refBC);

        for (int i = 0; i < 10; i++) {
            OrderItemFK oi = new OrderItemFK();
            oi.setVal("oi" + i);
            oi.setOrdVal(i);
            o.getOrderItems().add(oi);
        }
        pm.makePersistent(o);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        countExecQueryEvents();
        pm.loadFetchGroup((PersistenceCapable) o, "all");
        Assert.assertEquals(10, o.getOrderItems().size());
        o.getLongVal();
        o.getOrdVal();
        Assert.assertEquals(1, countExecQueryEvents());
        pm.close();
    }

    public void testCol1_N_PM() {
        testCol1(true);
    }

    public void testCol1_OLD_PM() {
        testCol1(false);
    }

    private void testCol1(boolean newPm) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Order order = new Order();
        pm.makePersistent(order);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(order);

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(0, order.getOrderItems().size());

        //add orderItems
        for (int i = 0; i < 3; i++) {
            OrderItem oi = new OrderItem();
            order.getOrderItems().add(oi);
        }
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testCol2() {
        testCol2Imp(true);
        testCol2Imp(false);
    }

    private void testCol2Imp(boolean newPm) {
        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Order order = new Order();
        order.setVal(val);
        pm.makePersistent(order);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(order);

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(0, order.getOrderItems().size());

        //add orderItems
        for (int i = 0; i < 3; i++) {
            OrderItem oi = new OrderItem();
            order.getOrderItems().add(oi);
        }
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class);
        q.setFilter("val == vParam");
        q.declareParameters("String vParam");
        List results = (List)q.execute(val);
        Assert.assertEquals(1, results.size());
        order = (Order)results.get(0);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testCol3() {
        testCol3Imp(true);
        testCol3Imp(false);
    }

    private void testCol3Imp(boolean newPm) {
        String val = "" + System.currentTimeMillis();
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(true);
        pm.currentTransaction().begin();
        Order order = new Order();
        order.setVal(val);
        pm.makePersistent(order);
        pm.currentTransaction().commit();
        Object id = pm.getObjectId(order);

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(0, order.getOrderItems().size());

        //add orderItems
        for (int i = 0; i < 3; i++) {
            OrderItem oi = new OrderItem();
            order.getOrderItems().add(oi);
        }
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        order = (Order)pm.getObjectById(id, true);
        Assert.assertEquals(3, order.getOrderItems().size());
        pm.currentTransaction().commit();

        if (newPm) {
            pm.close();
            pm = pmf().getPersistenceManager();
        }

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class);
        q.setFilter("val == vParam");
        q.declareParameters("String vParam");
        ((VersantQuery)q).setFetchGroup("orderItems1");
        List results = (List)q.execute(val);
        Assert.assertEquals(1, results.size());
        countExecQueryEvents();
        order = (Order)results.get(0);
        Assert.assertEquals(3, order.getOrderItems().size());
        if (!isWeakRefs(pm) && !isRemote()) {
            Assert.assertEquals(0, countExecQueryEvents());
        }
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testMapFetchSize1DS() {
        testMapFetchSize1Imp(false);
    }

    public void testMapFetchSize1OPT() {
        testMapFetchSize1Imp(true);
    }

    /**
     * The following test is to assert that correct result with a fetchsize set
     * on query.
     */
    private void testMapFetchSize1Imp(boolean opt) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().setOptimistic(opt);
        pm.currentTransaction().begin();

        int noOfMM = 6;
        int noOfOrders = 3;
        int noOfOrderItems = 3;
        int noOfProductions = 3;
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setVal("mm" + k);
            mm.setOrdering(k);
            for (int i = 0; i < noOfOrders; i++) {
                Order order = new Order();
                order.setVal(mm.getVal() + "order" + i);
                OrderItem oi = null;
                for (int c = 0; c < noOfOrderItems; c++) {
                    oi = new OrderItem();
                    oi.setVal(order.getVal() + "oi" + c);
                    oi.setOrder(c);
                    for (int j = 0; j < noOfProductions; j++) {
                        Production prod = new Production(
                                oi.getVal() + "prod" + j);
                        prod.setOrder(j);
                        oi.getProductions().add(prod);
                    }
                    order.getOrderItems().add(oi);
                }
                mm.getStringOrderMap().put("" + i, order);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Assert.assertEquals(opt, pm.currentTransaction().getOptimistic());
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL3");
        List result = (List)q.execute();
        int i = 0;
        for (; i < noOfMM; i++) {
            MapModel mapModel = (MapModel)result.get(i);
            /**
             * The previous call should fetch all the data needed so count the
             * query exec counts.
             */
            countExecQueryEvents();
            Assert.assertEquals(i, mapModel.getOrdering());
            Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals("mm" + i, mapModel.getVal());
            Assert.assertEquals(0, countExecQueryEvents());
            Map soMap = mapModel.getStringOrderMap();
            if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals(noOfOrders, soMap.size());
            for (int j = 0; j < noOfOrders; j++) {
                Order order = (Order)soMap.get("" + j);
                if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                Assert.assertEquals("mm" + i + "order" + j, order.getVal());

                List ois = order.getOrderItems();
                Assert.assertEquals(noOfOrderItems, ois.size());
                boolean[] oisDone = new boolean[noOfOrderItems];
                for (int k = 0; k < ois.size(); k++) {
                    OrderItem orderItem = (OrderItem)ois.get(k);
                    if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                    String val = "mm" + i + "order" + j + "oi" + orderItem.getOrder();
                    if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                    Assert.assertEquals(val, orderItem.getVal());
                    /**
                     * The following is done to ensure that the items are different and
                     * that all the possiblities is checked
                     */
                    if (oisDone[orderItem.getOrder()]) {
                        TestUtils.fail("This must be false");
                    }
                    oisDone[orderItem.getOrder()] = true;

                    List prods = orderItem.getProductions();
                    if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                    Assert.assertEquals(noOfProductions, prods.size());
                    boolean[] prodsDone = new boolean[noOfProductions];
                    for (int l = 0; l < prods.size(); l++) {
                        Production prod = (Production)prods.get(l);
                        String prodVal = val + "prod" + prod.getOrder();
                        Assert.assertEquals(prodVal, prod.getVal());
                        if (prodsDone[prod.getOrder()]) {
                            TestUtils.fail("This must be false");
                        }
                        prodsDone[prod.getOrder()] = true;
                    }
                }
            }
            if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
        }

        result = (List)q.execute();
        i = 0;
        for (; i < result.size(); i++) {
            MapModel mapModel = (MapModel)result.get(i);
            Collection orders = mapModel.getStringOrderMap().values();
            for (Iterator iterator = orders.iterator(); iterator.hasNext();) {
                Order order = (Order)iterator.next();
                List ois = order.getOrderItems();
                for (int j = 0; j < ois.size(); j++) {
                    OrderItem orderItem = (OrderItem)ois.get(j);
                    pm.deletePersistentAll(orderItem.getProductions());
                }
                pm.deletePersistentAll(ois);
            }
            pm.deletePersistentAll(orders);
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMapFetchSize2DS() {
        testMapFetchSize2Imp(false);
    }

    public void testMapFetchSize2OPT() {
        testMapFetchSize2Imp(true);
    }

    public void testMapFetchSize2Imp(boolean opt) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        int noOfMM = 6;
        int noOfOrders = 3;
        int noOfOrderItems = 3;
        int noOfProductions = 3;
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setVal("mm" + k);
            mm.setOrdering(k);
            for (int i = 0; i < noOfOrders; i++) {
                Order order = new Order();
                order.setOrdering(i);
                order.setVal(mm.getVal() + "order" + i);
                OrderItem oi = null;
                for (int c = 0; c < noOfOrderItems; c++) {
                    oi = new OrderItem();
                    oi.setVal(order.getVal() + "oi" + c);
                    oi.setOrder(c);
                    for (int j = 0; j < noOfProductions; j++) {
                        Production prod = new Production(
                                oi.getVal() + "prod" + j);
                        prod.setOrder(j);
                        oi.getProductions().add(prod);
                    }
                    order.getOrderItems().add(oi);
                }
                mm.getOrderStringMap().put(order, "" + i);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL4");
        List result = (List)q.execute();
        int i = 0;
        for (; i < noOfMM; i++) {
            System.out.println("\n\n\n\n ---- ITERATION i = " + i);
            MapModel mapModel = (MapModel)result.get(i);
            /**
             * The previous call should fetch all the data needed so count the
             * query exec counts.
             */
            countExecQueryEvents();
            Assert.assertEquals(i, mapModel.getOrdering());
            Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals("mm" + i, mapModel.getVal());
            Assert.assertEquals(0, countExecQueryEvents());
            Map soMap = mapModel.getOrderStringMap();
            Set entrySet = soMap.entrySet();
            Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals(3, soMap.size());

            Order[] orders = new Order[noOfOrders];
            System.out.println("\n\n\n Creating order array");
            for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Order order = (Order)entry.getKey();
                String val = (String)entry.getValue();
                System.out.println("\n\nval = " + val);
                System.out.println("order.getVal() = " + order.getVal());
                int index = Integer.parseInt(val);
                System.out.println("index = " + index);
                orders[index] = order;
            }
            System.out.println("\n\n");

            for (int j = 0; j < noOfOrders; j++) {
                Order order = orders[j];
                if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                System.out.println("expected val = mm" + i + "order" + j);
                System.out.println("order.getVal() = " + order.getVal());
                Assert.assertEquals("mm" + i + "order" + j, order.getVal());

                List ois = order.getOrderItems();
                Assert.assertEquals(noOfOrderItems, ois.size());
                boolean[] oisDone = new boolean[noOfOrderItems];
                for (int k = 0; k < ois.size(); k++) {
                    OrderItem orderItem = (OrderItem)ois.get(k);
                    if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                    String val = "mm" + i + "order" + j + "oi" + orderItem.getOrder();
                    if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                    Assert.assertEquals(val, orderItem.getVal());
                    /**
                     * The following is done to ensure that the items are different and
                     * that all the possiblities is checked
                     */
                    if (oisDone[orderItem.getOrder()]) {
                        TestUtils.fail("This must be false");
                    }
                    oisDone[orderItem.getOrder()] = true;

                    List prods = orderItem.getProductions();
                    if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
                    Assert.assertEquals(noOfProductions, prods.size());
                    boolean[] prodsDone = new boolean[noOfProductions];
                    for (int l = 0; l < prods.size(); l++) {
                        Production prod = (Production)prods.get(l);
                        String prodVal = val + "prod" + prod.getOrder();
                        Assert.assertEquals(prodVal, prod.getVal());
                        if (prodsDone[prod.getOrder()]) {
                            TestUtils.fail("This must be false");
                        }
                        prodsDone[prod.getOrder()] = true;
                    }
                }
            }
            if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
        }

        result = (List)q.execute();
        i = 0;
        for (; i < result.size(); i++) {
            MapModel mapModel = (MapModel)result.get(i);
            Collection orders = mapModel.getOrderStringMap().keySet();
            for (Iterator iterator = orders.iterator(); iterator.hasNext();) {
                Order order = (Order)iterator.next();
                List ois = order.getOrderItems();
                for (int j = 0; j < ois.size(); j++) {
                    OrderItem orderItem = (OrderItem)ois.get(j);
                    pm.deletePersistentAll(orderItem.getProductions());
                }
                pm.deletePersistentAll(ois);
            }
            pm.deletePersistentAll(orders);
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMapFetchSize3DS() {
        testMapFetchSize3Imp(false);
    }

    public void testMapFetchSize3OPT() {
        testMapFetchSize3Imp(true);
    }

    public void testMapFetchSize3Imp(boolean opt) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        int noOfMM = 6;
        int noOfOrders = 3;
        int noOfOrderItems = 3;
        int noOfProductions = 3;
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setVal("mm" + k);
            mm.setOrdering(k);
            for (int i = 0; i < noOfOrders; i++) {
                Order order = new Order();
                order.setOrdering(i);
                order.setVal(mm.getVal() + "order" + i);
                OrderItem oi = null;
                for (int c = 0; c < noOfOrderItems; c++) {
                    oi = new OrderItem();
                    oi.setVal(order.getVal() + "oi" + c);
                    oi.setOrder(c);
                    for (int j = 0; j < noOfProductions; j++) {
                        Production prod = new Production(
                                oi.getVal() + "prod" + j);
                        prod.setOrder(j);
                        oi.getProductions().add(prod);
                    }
                    order.getOrderItems().add(oi);
                }
                mm.getOrderOrderMap().put(order, order);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL5");
        List result = (List)q.execute();
        int i = 0;
        for (; i < noOfMM; i++) {
            System.out.println("\n\n\n\n ---- ITERATION i = " + i);
            MapModel mapModel = (MapModel)result.get(i);
            /**
             * The previous call should fetch all the data needed so count the
             * query exec counts.
             */
            countExecQueryEvents();
            Assert.assertEquals(i, mapModel.getOrdering());
            Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals("mm" + i, mapModel.getVal());
            Assert.assertEquals(0, countExecQueryEvents());
            Map soMap = mapModel.getOrderOrderMap();
            if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals(3, soMap.size());

            Order[] orders1 = new Order[noOfOrders];
            System.out.println("\n\n\n Creating order array");
            for (Iterator iterator = soMap.values().iterator();
                 iterator.hasNext();) {
                Order order = (Order)iterator.next();
                int index = order.getOrdering();
                orders1[index] = order;
            }
            checkOrders(noOfOrders, orders1, i, noOfOrderItems,
                    noOfProductions);

            Order[] orders2 = new Order[noOfOrders];
            System.out.println("\n\n\n Creating order array");
            for (Iterator iterator = soMap.keySet().iterator();
                 iterator.hasNext();) {
                Order order = (Order)iterator.next();
                int index = order.getOrdering();
                orders2[index] = order;
            }
            checkOrders(noOfOrders, orders2, i, noOfOrderItems,
                    noOfProductions);

            for (int j = 0; j < orders2.length; j++) {
                Assert.assertTrue(orders1[j] == orders2[j]);
            }
        }

        result = (List)q.execute();
        i = 0;
        for (; i < result.size(); i++) {
            MapModel mapModel = (MapModel)result.get(i);
            Collection orders = mapModel.getOrderOrderMap().keySet();
            for (Iterator iterator = orders.iterator(); iterator.hasNext();) {
                Order order = (Order)iterator.next();
                List ois = order.getOrderItems();
                for (int j = 0; j < ois.size(); j++) {
                    OrderItem orderItem = (OrderItem)ois.get(j);
                    pm.deletePersistentAll(orderItem.getProductions());
                }
                pm.deletePersistentAll(ois);
            }
            pm.deletePersistentAll(orders);
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testMapFetchSize4DS() {
        testMapFetchSize4Imp(false);
    }

    public void testMapFetchSize4OPT() {
        testMapFetchSize4Imp(true);
    }

    public void testMapFetchSize4Imp(boolean opt) {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        int noOfMM = 6;
        int noOfOrders = 3;
        int noOfOrderItems = 3;
        int noOfProductions = 3;
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setVal("mm" + k);
            mm.setOrdering(k);
            for (int i = 0; i < noOfOrders; i++) {
                String s = mm.getVal() + "s" + i;
                mm.getStringStringMap().put(s, s);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL5");
        List result = (List)q.execute();
        int i = 0;
        for (; i < noOfMM; i++) {
            System.out.println("\n\n\n\n ---- ITERATION i = " + i);
            MapModel mapModel = (MapModel)result.get(i);
            /**
             * The previous call should fetch all the data needed so count the
             * query exec counts.
             */
            countExecQueryEvents();
            Assert.assertEquals(i, mapModel.getOrdering());
            Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals("mm" + i, mapModel.getVal());
            Assert.assertEquals(0, countExecQueryEvents());
            Map soMap = mapModel.getStringStringMap();
            Assert.assertEquals(0, countExecQueryEvents());
            Assert.assertEquals(3, soMap.size());

            Set eSet = soMap.entrySet();
            boolean[] checked = new boolean[noOfOrders];
            for (Iterator iterator = eSet.iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Assert.assertEquals(entry.getKey(), entry.getValue());
                Assert.assertTrue(
                        ((String)entry.getKey()).startsWith("mm" + i + "s"));
                String s = (String)entry.getKey();
                String is = s.substring(s.length() - 1);
                int index = Integer.parseInt(is);
                if (checked[index]) {
                    TestUtils.fail("This must be unchecked");
                }
                checked[index] = true;
            }

            for (int j = 0; j < checked.length; j++) {
                boolean b = checked[j];
                if (!b) {
                    TestUtils.fail("field " + j + " was skiped");
                }
            }
        }

        result = (List)q.execute();
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();
        pm.close();
    }

    private void checkOrders(int noOfOrders, Order[] orders, int i,
            int noOfOrderItems, int noOfProductions) {
        for (int j = 0; j < noOfOrders; j++) {
            Order order = orders[j];
            Assert.assertEquals(0, countExecQueryEvents());
            System.out.println("expected val = mm" + i + "order" + j);
            System.out.println("order.getVal() = " + order.getVal());
            Assert.assertEquals("mm" + i + "order" + j, order.getVal());

            List ois = order.getOrderItems();
            Assert.assertEquals(noOfOrderItems, ois.size());
            boolean[] oisDone = new boolean[noOfOrderItems];
            for (int k = 0; k < ois.size(); k++) {
                OrderItem orderItem = (OrderItem)ois.get(k);
                if (!isWeakRefs(((PersistenceCapable)orderItem).jdoGetPersistenceManager())) Assert.assertEquals(0, countExecQueryEvents());
                String val = "mm" + i + "order" + j + "oi" + orderItem.getOrder();
                Assert.assertEquals(0, countExecQueryEvents());
                Assert.assertEquals(val, orderItem.getVal());
                /**
                 * The following is done to ensure that the items are different and
                 * that all the possiblities is checked
                 */
                if (oisDone[orderItem.getOrder()]) {
                    TestUtils.fail("This must be false");
                }
                oisDone[orderItem.getOrder()] = true;

                List prods = orderItem.getProductions();
                if (!isWeakRefs(((PersistenceCapable)orderItem).jdoGetPersistenceManager())) Assert.assertEquals(0, countExecQueryEvents());
                Assert.assertEquals(noOfProductions, prods.size());
                boolean[] prodsDone = new boolean[noOfProductions];
                for (int l = 0; l < prods.size(); l++) {
                    Production prod = (Production)prods.get(l);
                    String prodVal = val + "prod" + prod.getOrder();
                    Assert.assertEquals(prodVal, prod.getVal());
                    if (prodsDone[prod.getOrder()]) {
                        TestUtils.fail("This must be false");
                    }
                    prodsDone[prod.getOrder()] = true;
                }
            }
        }
        Assert.assertEquals(0, countExecQueryEvents());
    }

    public void testBla() {
        PersistenceManager pm = pmf().getPersistenceManager();
        System.out.println(
                "pm.currentTransaction().getRetainValues() = " + pm.currentTransaction().getRetainValues());
        pm.currentTransaction().begin();
        TestA testA = new TestA();
        String val = "" + System.currentTimeMillis();
        testA.setVal(val);
        testA.setInt1(111);
        testA.setInt2(222);
        testA.setInt3(333);
        pm.makePersistent(testA);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(TestA.class);
        ((VersantQuery)q).setFetchGroup("fg1");
        q.setFilter("val == param");
        q.declareParameters("String param");
        List l = (List)q.execute(val);
        testA = (TestA)l.get(0);
        System.out.println("testA.getInt1() = " + testA.getInt1());
        System.out.println("testA.getInt2() = " + testA.getInt2());
        pm.close();
    }

    /**
     * This tests a fg with a potential 3 level deep join. A ref B ref C ref D.
     */
    public void testFGWith2ndLevelOuterJoins() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();

        pm.currentTransaction().begin();
        A a1 = new A("aString1",
                new B("bString1", new C("cString1", new D("dString1"))),
                new E("eString1"));
        A a2 = new A("aString2",
                new B("bString2", new C("cString2", new D("dString2"))),
                new E("eString2"));
        A a3 = new A("aString3",
                new B("bString3", new C("cString3", new D("dString3"))),
                new E("eString3"));
        pm.makePersistent(a1);
        pm.makePersistent(a2);
        pm.makePersistent(a3);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(A.class);
        ((VersantQuery)q).setFetchGroup("testFG_A1");
        q.setFilter("b.c.cString != null");
        List l = (List)q.execute();
        Assert.assertTrue(l.size() >= 2);
        System.out.println("l.get(0) = " + ((A)l.get(0)).getaString());
        System.out.println("l.get(1) = " + ((A)l.get(1)).getaString());
        pm.currentTransaction().commit();


        //delete all the added stuff
        pm.currentTransaction().begin();
        Collection toDelete = new ArrayList();
        q = pm.newQuery(A.class);
        l = (List)q.execute();
        for (int i = 0; i < l.size(); i++) {
            A a = (A)l.get(i);
            toDelete.add(a);
            toDelete.add(a.getB());
            toDelete.add(a.getE());
            toDelete.add(a.getB().getC());
            toDelete.add(a.getB().getC().getD());
        }
        pm.deletePersistentAll(toDelete);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testInheritanceWithRefs1() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Base base1 = new Base();
        base1.setBase("base1");
        pm.makePersistent(base1);

        SubA subA1 = new SubA();
        subA1.setBase("subabase1");
        subA1.setSubA("suba1");
        pm.makePersistent(subA1);

        SubB subB1 = new SubB();
        subB1.setBase("subbbase1");
        subB1.setSubB("subb1");
        pm.makePersistent(subB1);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Base.class);
        q.setOrdering("base ascending");
        List results = (List)q.execute();
        Assert.assertEquals(3, results.size());

        Assert.assertEquals("base1", ((Base)results.get(0)).getBase());
        Assert.assertEquals("subabase1", ((SubA)results.get(1)).getBase());
        Assert.assertEquals("subbbase1", ((SubB)results.get(2)).getBase());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(Base.class);
        results = (List)q.execute();
        pm.deletePersistentAll(results);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInheritanceWithRefs2() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Base baseRef1 = new Base();
        baseRef1.setBase("a_base");
        pm.makePersistent(baseRef1);
        Base baseRef2 = new Base();
        baseRef2.setBase("b_base");
        pm.makePersistent(baseRef2);

        Base base1 = new Base();
        base1.setBase("c_base");
        pm.makePersistent(base1);

        SubA subA1 = new SubA();
        subA1.setBase("d_suba");
        subA1.setSubA("suba1");
        subA1.setaBaseRef(baseRef1);
        pm.makePersistent(subA1);

        SubB subB1 = new SubB();
        subB1.setBase("e_subb");
        subB1.setSubB("subb1");
        subB1.setbBaseRef(baseRef2);
        pm.makePersistent(subB1);

        SubBB subBB = new SubBB();
        subBB.setBase("f_subBB");
        subBB.setSubBB("subbb");
        subBB.setSubB("f_subB");
        SubA subA2 = new SubA();
        subA2.setBase("g_suba_base");
        subA2.setSubA("g_suba");

        subBB.setbBaseRef(subA2);
        pm.makePersistent(subBB);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Base.class);
        q.setFetchGroup("testFG_A2");
        q.setOrdering("base ascending");
        List results = (List)q.execute();
        Assert.assertEquals(7, results.size());

        Assert.assertEquals("a_base", ((Base)results.get(0)).getBase());
        Assert.assertEquals("b_base", ((Base)results.get(1)).getBase());
        Assert.assertEquals("c_base", ((Base)results.get(2)).getBase());

        Assert.assertEquals("d_suba", ((SubA)results.get(3)).getBase());
        Assert.assertEquals("suba1", ((SubA)results.get(3)).getSubA());

        Assert.assertEquals("e_subb", ((SubB)results.get(4)).getBase());
        Assert.assertEquals("subb1", ((SubB)results.get(4)).getSubB());

        Assert.assertEquals("f_subBB", ((SubBB)results.get(5)).getBase());
        Assert.assertEquals("subbb", ((SubBB)results.get(5)).getSubBB());
        Assert.assertEquals("f_subB", ((SubBB)results.get(5)).getSubB());

        Assert.assertEquals("g_suba_base", ((SubA)results.get(6)).getBase());
        Assert.assertEquals("g_suba", ((SubA)results.get(6)).getSubA());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(Base.class);
        results = (List)q.execute();
        pm.deletePersistentAll(results);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void testInheritanceWithRefs3() {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Base baseRef1 = new Base();
        baseRef1.setBase("z1");
        pm.makePersistent(baseRef1);
        Base baseRef2 = new Base();
        baseRef2.setBase("z2");
        pm.makePersistent(baseRef2);

        Base base1 = new Base();
        base1.setBase("base1");
        pm.makePersistent(base1);

        SubA subA1 = new SubA();
        subA1.setBase("subabase1");
        subA1.setSubA("suba1");
        subA1.setaBaseRef(baseRef1);
        pm.makePersistent(subA1);

        SubB subB1 = new SubB();
        subB1.setBase("subbbase1");
        subB1.setSubB("subb1");
        subB1.setbBaseRef(baseRef2);
        pm.makePersistent(subB1);

        SubBB subBB = new SubBB();
        subBB.setBase("zzzzzzzz");
        subBB.setSubBB("subbb");
        SubA subA2 = new SubA();
        subA2.setBase("zzzzzzzzzzzzzz");

        subBB.setbBaseRef(subA2);
        pm.makePersistent(subBB);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(SubB.class);
        q.setFetchGroup("testFG_A2");
        q.setOrdering("base ascending");
        List results = (List)q.execute();
        Assert.assertEquals(2, results.size());

        Assert.assertEquals("subbbase1", ((SubB)results.get(0)).getBase());
        Assert.assertEquals("zzzzzzzz", ((SubBB)results.get(1)).getBase());
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        q = (VersantQuery)pm.newQuery(Base.class);
        results = (List)q.execute();
        pm.deletePersistentAll(results);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void test3LevelDeepListWithFilter() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 5;
        int noOfOrderItems = 5;
        int noOfOrders = 3;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            Order order = new Order();
            OrderItem oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                oi = new OrderItem();
                for (int j = 0; j < noOfProductions; j++) {
                    oi.getProductions().add(new Production("" + j));
                }
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class);
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL3");
        ((VersantQuery)q).setFetchSize(noOfOrders);
        List result = (List)q.execute();
        Assert.assertEquals(noOfOrders, result.size());
        countExecQueryEvents();

        Order order = (Order)result.get(0);
        OrderItem oi = (OrderItem)order.getOrderItems().get(0);
        Production p = (Production)oi.getProductions().get(0);
        String val = p.getVal();
//        ((Production)((OrderItem).getOrderItems().get(0)).getProductions().get(0));.getVal();
        Assert.assertEquals(0, countExecQueryEvents());

        deleteAllOrders(result, pm);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void test3LevelDeepListWithFilter3() throws Exception {
        Map ordSlMap = new HashMap();
        List localStringList = new ArrayList();

        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 2;
        int noOfOrderItems = 2;
        int noOfOrders = 5;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            Order order = new Order();
            order.setVal("ord" + i);
            OrderItem oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                oi = new OrderItem();
                oi.setVal(order.getVal() + ":oi" + c);
                localStringList = new ArrayList();
                ordSlMap.put(oi.getVal(), localStringList);
                for (int j = 0; j < noOfProductions; j++) {
                    String s = oi.getVal() + ":sl" + j;
                    System.out.println("s = " + s);
                    oi.getStringList().add(s);
                    localStringList.add(s);
                }
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class);
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("test1");
        ((VersantQuery)q).setFetchSize(noOfOrders);
        List result = (List)q.execute();
        Assert.assertEquals(noOfOrders, result.size());
        countExecQueryEvents();

        for (int i = 0; i < noOfOrders; i++) {
            Order order = (Order)result.get(i);
            Assert.assertEquals(noOfOrderItems, order.getOrderItems().size());
            OrderItem oi = (OrderItem)order.getOrderItems().get(0);
            List stringList = new ArrayList(oi.getStringList());
            localStringList = (List)ordSlMap.get(oi.getVal());
            Collections.sort(stringList);
            Collections.sort(localStringList);
            Assert.assertEquals(localStringList, stringList);
        }
        Assert.assertEquals(0, countExecQueryEvents());

        deleteAllOrders(result, pm);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void test3LevelDeepListWithFilter4() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 5;
        int noOfOrderItems = 2;
        int noOfOrders = 3;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            long orderLong = System.currentTimeMillis();
            Order order = new Order();
            order.setLongVal(orderLong);
            order.setVal("ord" + i);
            OrderItem oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                long oiLong = System.currentTimeMillis();
                oi = new OrderItem();
                oi.setOrder(c);
                oi.setLongVal(oiLong);
                oi.setParentLongVal(orderLong);
                oi.setVal(order.getVal() + ":oi" + c);
                for (int j = 0; j < noOfProductions; j++) {
                    String s = oi.getVal() + ":prod" + j;
                    oi.getProductions().add(new Production(s, "" + j, oiLong));
                }
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class);
        q.setOrdering("this ascending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("test2");
        ((VersantQuery)q).setFetchSize(noOfOrders);
        List result = (List)q.execute();
        Assert.assertEquals(noOfOrders, result.size());
        countExecQueryEvents();

        for (int i = 0; i < noOfOrders; i++) {
            Order order = (Order)result.get(i);
            Assert.assertEquals(noOfOrderItems, order.getOrderItems().size());
            List ois = order.getOrderItems();
            Assert.assertEquals(noOfOrderItems, ois.size());
            for (int k = 0; k < ois.size(); k++) {
                OrderItem oi = (OrderItem)ois.get(k);
                List prods = oi.getProductions();
                Assert.assertEquals(noOfProductions, prods.size());
                Assert.assertEquals(oi.getParentLongVal(), order.getLongVal());
                for (int j = 0; j < prods.size(); j++) {
                    Production production = (Production)prods.get(j);
                    Assert.assertEquals(production.getParentLongVal(),
                            oi.getLongVal());
                    String s = "ord" + i + ":oi" + k + ":prod" + j;
                    Assert.assertEquals(s, production.getVal());
                }
            }

        }

        Assert.assertEquals(0, countExecQueryEvents());

        deleteAllOrders(result, pm);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void test3LevelDeepListWithFilterFK4() throws Exception {
        Collection col = new HashSet();
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 5;
        int noOfOrderItems = 2;
        int noOfOrders = 3;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            long orderLong = System.currentTimeMillis();
            OrderFK order = new OrderFK();
            col.add(order);
            order.setLongVal(orderLong);
            order.setVal("ord" + i);
            order.setOrdVal(i);
            OrderItemFK oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                long oiLong = System.currentTimeMillis();
                oi = new OrderItemFK();
                col.add(oi);
                oi.setOrdVal(c);
                oi.setLongVal(oiLong);
                oi.setParentLongVal(orderLong);
                oi.setVal(order.getVal() + ":oi" + c);
                for (int j = 0; j < noOfProductions; j++) {
                    String s = oi.getVal() + ":prod" + j;
                    oi.getProductions().add(new ProductionFK(oi, s, j, oiLong));
                }
                col.addAll(oi.getProductions());
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(OrderFK.class);
        q.setOrdering("ordVal ascending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("test2");
        ((VersantQuery)q).setFetchSize(noOfOrders);
        List result = (List)q.execute();
        Assert.assertEquals(noOfOrders, result.size());
        countExecQueryEvents();

        for (int i = 0; i < noOfOrders; i++) {
            OrderFK order = (OrderFK)result.get(i);
            Assert.assertEquals(noOfOrderItems, order.getOrderItems().size());
            List ois = order.getOrderItems();
            Assert.assertEquals(noOfOrderItems, ois.size());
            for (int k = 0; k < ois.size(); k++) {
                OrderItemFK oi = (OrderItemFK)ois.get(k);
                List prods = oi.getProductions();
                Assert.assertEquals(noOfProductions, prods.size());
                Assert.assertEquals(oi.getParentLongVal(), order.getLongVal());
                for (int j = 0; j < prods.size(); j++) {
                    ProductionFK production = (ProductionFK)prods.get(j);
                    Assert.assertEquals(production.getParentLongVal(),
                            oi.getLongVal());
                    String s = "ord" + i + ":oi" + k + ":prod" + j;
                    Assert.assertEquals(s, production.getVal());
                }
            }

        }

        Assert.assertEquals(0, countExecQueryEvents());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void test3LevelDeepListWithFilterFK5() throws Exception {
        Collection col = new HashSet();
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 5;
        int noOfOrderItems = 2;
        int noOfOrders = 3;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            long orderLong = System.currentTimeMillis();
            OrderFK order = new OrderFK();
            col.add(order);
            order.setLongVal(orderLong);
            order.setVal("ord" + i);
            order.setOrdVal(i);
            OrderItemFK oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                long oiLong = System.currentTimeMillis();
                oi = new OrderItemFK();
                col.add(oi);
                oi.setOrdVal(c);
                oi.setLongVal(oiLong);
                oi.setParentLongVal(orderLong);
                oi.setVal(order.getVal() + ":oi" + c);
                for (int j = 0; j < noOfProductions; j++) {
                    String s = oi.getVal() + ":prod" + j;
                    oi.getProductions().add(new ProductionFK(oi, s, j, oiLong));
                }
                col.addAll(oi.getProductions());
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(OrderFK.class);
//        q.setOrdering("ordVal ascending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("test2");
        ((VersantQuery)q).setFetchSize(noOfOrders);
        List result = (List)q.execute();
        Assert.assertEquals(noOfOrders, result.size());
        countExecQueryEvents();

        for (int i = 0; i < noOfOrders; i++) {
            OrderFK order = (OrderFK)result.get(i);
            Assert.assertEquals(noOfOrderItems, order.getOrderItems().size());
            List ois = order.getOrderItems();
            Assert.assertEquals(noOfOrderItems, ois.size());
            for (int k = 0; k < ois.size(); k++) {
                OrderItemFK oi = (OrderItemFK)ois.get(k);
                List prods = oi.getProductions();
                Assert.assertEquals(noOfProductions, prods.size());
                Assert.assertEquals(oi.getParentLongVal(), order.getLongVal());
                for (int j = 0; j < prods.size(); j++) {
                    ProductionFK production = (ProductionFK)prods.get(j);
                    Assert.assertEquals(production.getParentLongVal(),
                            oi.getLongVal());
                }
            }

        }

        Assert.assertEquals(0, countExecQueryEvents());

        pm.deletePersistentAll(col);
        pm.currentTransaction().commit();
        pm.close();
    }

    public void test3LevelDeepListWithFilterFK() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 2;
        int noOfOrderItems = 2;
        int noOfOrders = 3;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            OrderFK order = new OrderFK();
            OrderItemFK oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                oi = new OrderItemFK();
                for (int j = 0; j < noOfProductions; j++) {
                    oi.getProductions().add(new ProductionFK("" + j));
                }
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(OrderFK.class);
        q.setOrdering("this descending");
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL3");
        ((VersantQuery)q).setFetchSize(noOfOrders);
        List result = (List)q.execute();
        Assert.assertEquals(noOfOrders, result.size());
        countExecQueryEvents();

        ((ProductionFK)((OrderItemFK)((OrderFK)result.get(0)).getOrderItems().get(
                0)).getProductions().get(0)).getVal();
        for (int i = 0; i < result.size(); i++) {
            OrderFK orderFK = (OrderFK)result.get(i);
            Assert.assertEquals(noOfOrderItems, orderFK.getOrderItems().size());
            for (int j = 0; j < orderFK.getOrderItems().size(); j++) {
                OrderItemFK orderItemFK = (OrderItemFK)orderFK.getOrderItems().get(
                        j);
                Assert.assertEquals(noOfProductions,
                        orderItemFK.getProductions().size());
            }
        }
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());

        deleteAllOrderFKs(result, pm);
        pm.currentTransaction().commit();

        pm.close();

    }

    public void test3LevelDeepListWithFilter2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 5;
        int noOfOrderItems = 5;
        int noOfOrders = 10;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            Order order = new Order();
            order.setVal("" + i);
            OrderItem oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                oi = new OrderItem();
                for (int j = 0; j < noOfProductions; j++) {
                    oi.getProductions().add(new Production("" + j));
                }
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(Order.class);
        q.setOrdering("val ascending");
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL3");
        List result = (List)q.execute();
//        Assert.assertEquals(noOfOrders, result.size());
        Assert.assertEquals("0", ((Order)result.get(0)).getVal());
        Assert.assertEquals("1", ((Order)result.get(1)).getVal());
        Assert.assertEquals("2", ((Order)result.get(2)).getVal());
        Assert.assertEquals("9", ((Order)result.get(9)).getVal());
        try {
            Assert.assertEquals("10", ((Order)result.get(10)).getVal());
            TestUtils.fail("Expect IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //ignore
        }

        result = (List)q.execute();
        deleteAllOrders(result, pm);
        pm.currentTransaction().commit();

        pm.close();

    }

    private void deleteAllOrders(List result, PersistenceManager pm) {
        for (int i = 0; i < result.size(); i++) {
            Order orderFK = (Order)result.get(i);
            List ois = orderFK.getOrderItems();
            for (int j = 0; j < ois.size(); j++) {
                OrderItem oi = (OrderItem)ois.get(j);
                pm.deletePersistentAll(oi.getProductions());
            }
            pm.deletePersistentAll(ois);
        }
        pm.deletePersistentAll(result);
    }

    public void test3LevelDeepListWithFilter2FK() throws Exception {
        nuke(ProductionFK.class);
        nuke(OrderItemFK.class);
        nuke(OrderFK.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfProductions = 5;
        int noOfOrderItems = 5;
        int noOfOrders = 10;
        pm.currentTransaction().begin();
        for (int i = 0; i < noOfOrders; i++) {
            OrderFK order = new OrderFK();
            order.setVal("" + i);
            OrderItemFK oi = null;
            for (int c = 0; c < noOfOrderItems; c++) {
                oi = new OrderItemFK();
                for (int j = 0; j < noOfProductions; j++) {
                    oi.getProductions().add(new ProductionFK("" + j));
                }
                order.getOrderItems().add(oi);
            }
            pm.makePersistent(order);
        }
        pm.currentTransaction().commit();

//        ((VersantPersistenceManager)pm).setDatastoreTxLocking(VersantPersistenceManager.LOCKING_NONE);
        pm.currentTransaction().begin();
        Query q = pm.newQuery(OrderFK.class);
//        q.setOrdering("this descending");
        q.setOrdering("val ascending");
        ((VersantQuery)q).setFetchSize(1);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL3");
        List result = (List)q.execute();
        Assert.assertEquals("0", ((OrderFK)result.get(0)).getVal());
        Assert.assertEquals("1", ((OrderFK)result.get(1)).getVal());
        Assert.assertEquals("2", ((OrderFK)result.get(2)).getVal());
        Assert.assertEquals("7", ((OrderFK)result.get(7)).getVal());
        Assert.assertEquals("9", ((OrderFK)result.get(9)).getVal());
        try {
            Assert.assertEquals("10", ((OrderFK)result.get(10)).getVal());
            TestUtils.fail("Expect IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //ignore
        }

        result = (List)q.execute();
        deleteAllOrderFKs(result, pm);
        pm.currentTransaction().commit();

        pm.close();
    }

    private void deleteAllOrderFKs(List result, PersistenceManager pm) {
        for (int i = 0; i < result.size(); i++) {
            OrderFK orderFK = (OrderFK)result.get(i);
            List ois = orderFK.getOrderItems();
            for (int j = 0; j < ois.size(); j++) {
                OrderItemFK oi = (OrderItemFK)ois.get(j);
                pm.deletePersistentAll(oi.getProductions());
            }
            pm.deletePersistentAll(ois);
        }
        pm.deletePersistentAll(result);
    }

    public void testMapWithFilter1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfMM = 3;
        int noOfProductions = 2;
        int noOfOrderItems = 2;
        int noOfOrders = 2;
        pm.currentTransaction().begin();
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setOrdering(k);
            for (int i = 0; i < noOfOrders; i++) {
                Order order = new Order();
                order.setVal("" + i);
                OrderItem oi = null;
                for (int c = 0; c < noOfOrderItems; c++) {
                    oi = new OrderItem();
                    for (int j = 0; j < noOfProductions; j++) {
                        oi.getProductions().add(new Production("" + j));
                    }
                    order.getOrderItems().add(oi);
                }
                mm.getStringOrderMap().put("" + i, order);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(noOfMM);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL3");
        List result = (List)q.execute();
        Assert.assertEquals(noOfMM, result.size());
        countExecQueryEvents();
        mm = (MapModel)result.get(0);
        Assert.assertEquals("" + 0,
                ((Order)mm.getStringOrderMap().get("" + 0)).getVal());
        Assert.assertEquals(0, mm.getOrdering());
        Assert.assertEquals(0, countExecQueryEvents());

        result = (List)q.execute();
        for (int i = 0; i < result.size(); i++) {
            MapModel mapModel = (MapModel)result.get(i);
            Collection orders = mapModel.getStringOrderMap().values();
            for (Iterator iterator = orders.iterator(); iterator.hasNext();) {
                Order order = (Order)iterator.next();
                List ois = order.getOrderItems();
                for (int j = 0; j < ois.size(); j++) {
                    OrderItem orderItem = (OrderItem)ois.get(j);
                    pm.deletePersistentAll(orderItem.getProductions());
                }
                pm.deletePersistentAll(ois);
            }
            pm.deletePersistentAll(orders);
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testMapWithFilter2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfMM = 2;
        int noOfEntries = 10;
        pm.currentTransaction().begin();
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setOrdering(k);
            for (int i = 0; i < noOfEntries; i++) {
                mm.getStringStringMap().put("key" + i, "val" + i);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(noOfMM);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFGL1");
        List result = (List)q.execute();
        Assert.assertEquals(noOfMM, result.size());
        countExecQueryEvents();
        mm = (MapModel)result.get(0);
        System.out.println("mm = " + mm);
        System.out.println(
                "mm.getStringStringMap() = " + mm.getStringStringMap());
        Assert.assertEquals("val" + 0, mm.getStringStringMap().get("key" + 0));
        Assert.assertEquals(0, mm.getOrdering());
        Assert.assertEquals(0, countExecQueryEvents());

        result = (List)q.execute();
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();

        pm.close();
    }

    public void testMapWithFilter3() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        int noOfMM = 3;
        int noOfProductions = 2;
        int noOfOrderItems = 2;
        int noOfOrders = 2;
        pm.currentTransaction().begin();
        MapModel mm = null;
        for (int k = 0; k < noOfMM; k++) {
            mm = new MapModel();
            mm.setOrdering(k);
            for (int i = 0; i < noOfOrders; i++) {
                Order order = createOrder(i, noOfOrderItems, noOfProductions);
                mm.getOrderOrderMap().put(order, order);
            }
            pm.makePersistent(mm);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        Query q = pm.newQuery(MapModel.class);
        q.setOrdering("ordering ascending");
        ((VersantQuery)q).setFetchSize(noOfMM);
        ((VersantQuery)q).setBounded(true);
        ((VersantQuery)q).setFetchGroup("testFG2");
        List result = (List)q.execute();
        Assert.assertEquals(noOfMM, result.size());
        countExecQueryEvents();
        mm = (MapModel)result.get(0);
        Assert.assertEquals(noOfOrders, mm.getOrderOrderMap().values().size());
        Assert.assertEquals(noOfOrders, mm.getOrderOrderMap().keySet().size());

        Assert.assertEquals(new ArrayList(mm.getOrderOrderMap().values()),
                new ArrayList(mm.getOrderOrderMap().keySet()));
        Assert.assertEquals(0, mm.getOrdering());
        Assert.assertEquals(0, countExecQueryEvents());

        result = (List)q.execute();
        for (int i = 0; i < result.size(); i++) {
            MapModel mapModel = (MapModel)result.get(i);
            Collection orders = mapModel.getStringOrderMap().values();
            for (Iterator iterator = orders.iterator(); iterator.hasNext();) {
                Order order = (Order)iterator.next();
                List ois = order.getOrderItems();
                for (int j = 0; j < ois.size(); j++) {
                    OrderItem orderItem = (OrderItem)ois.get(j);
                    pm.deletePersistentAll(orderItem.getProductions());
                }
                pm.deletePersistentAll(ois);
            }
            pm.deletePersistentAll(orders);
        }
        pm.deletePersistentAll(result);
        pm.currentTransaction().commit();

        pm.close();

    }

    private Order createOrder(int i, int noOfOrderItems, int noOfProductions) {
        Order order = new Order();
        order.setVal("" + i);
        OrderItem oi = null;
        for (int c = 0; c < noOfOrderItems; c++) {
            oi = new OrderItem();
            for (int j = 0; j < noOfProductions; j++) {
                oi.getProductions().add(new Production("" + j));
            }
            order.getOrderItems().add(oi);
        }
        return order;
    }

    public void testMapWithFilter4() throws Exception {

    }

    public void testRecursive1() throws Exception {
        nuke(Recursive.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Recursive root = new Recursive();
        root.setVal("root");
        Set set = new HashSet();
        set.add(root);
        addToRec(root, 5, 3, 0, set);
        int totalAmount = set.size();
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Recursive.class);
        q.setFilter("val == param");
        q.declareParameters("String param");
        q.setFetchGroup("test1");
        q.setBounded(true);
        List result = (List)q.execute("root");
        Assert.assertEquals(1, result.size());
        countExecQueryEvents();
        Set valSet = new HashSet();
        root = (Recursive)result.get(0);
        valSet.add(root);
        validate(root, 5, 3, 0, "root", valSet);
        Assert.assertEquals(totalAmount, valSet.size());
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());

        pm.close();
    }

    public void testRecursive11() throws Exception {
        nuke(Recursive.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Recursive root = new Recursive();
        root.setVal("root");
        Set set = new HashSet();
        set.add(root);
        addToRec11(root, 5, 3, 0, set);
        int totalAmount = set.size();
        pm.makePersistent(root);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Recursive.class);
        q.setFilter("val == param");
        q.declareParameters("String param");
        q.setFetchGroup("test11");
        q.setBounded(true);
        List result = (List)q.execute("root");
        Assert.assertEquals(1, result.size());
        countExecQueryEvents();
        Set valSet = new HashSet();
        root = (Recursive)result.get(0);
        valSet.add(root);
        validate11(root, 5, 3, 0, "root", valSet);
        Assert.assertEquals(totalAmount, valSet.size());
        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void addToRec11(Recursive rec, int amount, int levels,
            int currentLevel, Set holder) {
        String tStamp = "" + System.currentTimeMillis();
        rec.settStamp(tStamp);
        for (int i = 0; i < amount; i++) {
            Recursive newRec = new Recursive(
                    rec.getVal() + ":level" + currentLevel + ":n");
            newRec.setParentTStamp(rec.gettStamp());
            holder.add(newRec);
            if (levels > currentLevel) {
                addToRec11(newRec, amount, levels,
                        currentLevel + 1, holder);
            }
            rec.getRecSet().add(newRec);
        }
    }

    public void validate11(Recursive root, int amount, int levels,
            int currentLevel, String val, Set holder) {
        Set recList = root.getRecSet();
        if (levels >= currentLevel) {
            Assert.assertEquals(amount, recList.size());
            for (Iterator iterator = recList.iterator(); iterator.hasNext();) {
                Recursive child = (Recursive)iterator.next();
                holder.add(child);
                Assert.assertEquals(root.gettStamp(), child.getParentTStamp());

                String ss = val + ":level" + currentLevel + ":n";
                Assert.assertTrue(ss, child.getVal().startsWith(ss));
                validate11(child, amount, levels, currentLevel + 1,
                        val + ":level" + currentLevel + ":n", holder);
            }
        } else {
            Assert.assertEquals(0, recList.size());
        }
    }

    public void addToRec(Recursive rec, int amount, int levels,
            int currentLevel, Set holder) {
        for (int i = 0; i < amount; i++) {
            Recursive newRec = new Recursive(
                    rec.getVal() + ":level" + currentLevel + ":n" + i);
            holder.add(newRec);
            if (levels > currentLevel) {
                addToRec(newRec, amount, levels,
                        currentLevel + 1, holder);
            }
            rec.getRecList().add(newRec);
        }
    }

    public void validate(Recursive root, int amount, int levels,
            int currentLevel, String val, Set holder) {
        List recList = root.getRecList();
        if (levels >= currentLevel) {
            Assert.assertEquals(amount, recList.size());
            for (int i = 0; i < recList.size(); i++) {
                Recursive child = (Recursive)recList.get(i);
                holder.add(child);
                String ss = val + ":level" + currentLevel + ":n" + i;
                Assert.assertEquals(ss, child.getVal());
                validate(child, amount, levels, currentLevel + 1,
                        val + ":level" + currentLevel + ":n" + i, holder);
            }
        } else {
            Assert.assertEquals(0, recList.size());
        }
    }

    public void testRecursive2() throws Exception {
        nuke(Recursive.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Recursive root = new Recursive();
        root.setVal("rootFK");
        Set addToSet = new HashSet();
        addToSet.add(root);
        addToRec2(root, 5, 3, 0, addToSet);
        int totalAmount = addToSet.size();
        pm.makePersistent(root);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Recursive.class);
        q.setFilter("val == param");
        q.declareParameters("String param");
        q.setFetchGroup("test2");
        List result = (List)q.execute("rootFK");
        Assert.assertEquals(1, result.size());
        countExecQueryEvents();
        Set valSet = new HashSet();
        root = (Recursive)result.get(0);
        valSet.add(root);
        validate2(root, 5, 3, 0, "rootFK", valSet);
        Assert.assertEquals(totalAmount, valSet.size());
        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void testRecursive23() throws Exception {
        nuke(Recursive.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Recursive root = new Recursive();
        root.setVal("rootFK");
        Set addToSet = new HashSet();
        addToSet.add(root);
        addToRec2(root, 5, 3, 0, addToSet);
        int totalAmount = addToSet.size();
        pm.makePersistent(root);
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Recursive.class);
        q.setFilter("val == param");
        q.declareParameters("String param");
        q.setFetchGroup("test2");
        q.setBounded(true);
        List result = (List)q.execute("rootFK");
        Assert.assertEquals(1, result.size());
        countExecQueryEvents();
        Set valSet = new HashSet();
        root = (Recursive)result.get(0);
        valSet.add(root);
        validate2(root, 5, 3, 0, "rootFK", valSet);
        Assert.assertEquals(totalAmount, valSet.size());
        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void testRecursive22() throws Exception {
        nuke(Recursive.class);

        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Recursive root = new Recursive();
        root.setVal("rootFK");
        Set addToSet = new HashSet();
        addToSet.add(root);
        addToRec22(root, 5, 3, 0, addToSet);
        int totalAmount = addToSet.size();
        pm.makePersistent(root);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(Recursive.class);
        q.setFilter("val == param");
        q.declareParameters("String param");
        q.setFetchGroup("test22");
        List result = (List)q.execute("rootFK");
        Assert.assertEquals(1, result.size());
        countExecQueryEvents();
        Set valSet = new HashSet();
        root = (Recursive)result.get(0);
        valSet.add(root);
        validate22(root, 5, 3, 0, "rootFK", valSet);
        Assert.assertEquals(totalAmount, valSet.size());
        if (!isWeakRefs(pm)) Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void addToRec2(Recursive rec, int amount, int levels,
            int currentLevel, Set holder) {
        String tStamp = "" + System.currentTimeMillis();
        rec.settStamp(tStamp);
        for (int i = 0; i < amount; i++) {
            Recursive newRec = new Recursive(
                    rec.getVal() + ":level" + currentLevel + ":n" + i);

            holder.add(newRec);
            if (levels > currentLevel) {
                addToRec2(newRec, amount, levels,
                        currentLevel + 1, holder);
            }
            rec.getRecFkList().add(newRec);
        }
    }

    public void addToRec22(Recursive rec, int amount, int levels,
            int currentLevel, Set holder) {
        String tStamp = "" + System.currentTimeMillis();
        rec.settStamp(tStamp);
        for (int i = 0; i < amount; i++) {
            Recursive newRec = new Recursive(
                    rec.getVal() + ":level" + currentLevel + ":n");
            newRec.setParentTStamp(rec.gettStamp());
            holder.add(newRec);
            if (levels > currentLevel) {
                addToRec22(newRec, amount, levels,
                        currentLevel + 1, holder);
            }
            rec.getRecFkSet().add(newRec);
        }
    }

    public void validate2(Recursive root, int amount, int levels,
            int currentLevel, String val, Set holder) {
        List recList = root.getRecFkList();
        if (levels >= currentLevel) {
            Assert.assertEquals(amount, recList.size());
            for (int i = 0; i < recList.size(); i++) {
                Recursive child = (Recursive)recList.get(i);
                holder.add(child);
                String ss = val + ":level" + currentLevel + ":n" + i;
                Assert.assertEquals(ss, child.getVal());
                validate2(child, amount, levels, currentLevel + 1,
                        val + ":level" + currentLevel + ":n" + i, holder);
            }
        } else {
            Assert.assertEquals(0, recList.size());
        }
    }

    public void validate22(Recursive root, int amount, int levels,
            int currentLevel, String val, Set holder) {
        Set recList = root.getRecFkSet();
        if (levels >= currentLevel) {
            Assert.assertEquals(amount, recList.size());
            for (Iterator iterator = recList.iterator(); iterator.hasNext();) {
                Recursive child = (Recursive)iterator.next();
                holder.add(child);
                Assert.assertEquals(root.gettStamp(), child.getParentTStamp());
                String ss = val + ":level" + currentLevel + ":n";
                Assert.assertEquals(ss, child.getVal());
                validate22(child, amount, levels, currentLevel + 1,
                        val + ":level" + currentLevel + ":n", holder);
            }
        } else {
            Assert.assertEquals(0, recList.size());
        }
    }

    public void testInheritance1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        InherA inherA = new InherA();
        inherA.setOrder(0);
        inherA.getStringListA().add("inherA:string0");
        inherA.getStringListA().add("inherA:string1");
        inherA.getStringListA().add("inherA:string2");
        pm.makePersistent(inherA);

        InherB inherB = new InherB();
        inherB.setOrder(1);
        inherB.getStringListB().add("inherB:string0");
        inherB.getStringListB().add("inherB:string1");
        inherB.getStringListB().add("inherB:string2");
        pm.makePersistent(inherB);

        InherC inherC = new InherC();
        inherC.setOrder(2);
        inherC.getStringListC().add("inherC:string0");
        inherC.getStringListC().add("inherC:string1");
        inherC.getStringListC().add("inherC:string2");
        pm.makePersistent(inherC);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(InherA.class);
        q.setOrdering("order ascending");
        q.setFetchGroup("testInher1");
        List result = (List)q.execute();
        Assert.assertEquals(3, result.size());
        countExecQueryEvents();
        inherA = (InherA)result.get(0);
        Assert.assertEquals(3, inherA.getStringListA().size());

        inherB = (InherB)result.get(1);
        Assert.assertEquals(3, inherB.getStringListB().size());

        inherC = (InherC)result.get(2);
        Assert.assertEquals(3, inherC.getStringListC().size());

        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    public void testInheritance2() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        InherA inherA = new InherA();
        inherA.setOrder(0);
        inherA.setVal("rootA");
        for (int i = 0; i < 3; i++) {
            inherA.getStringListA().add(inherA.getVal() + ":string" + i);
        }
        pm.makePersistent(inherA);

        addColItems(inherA);

        InherB inherB = new InherB();
        inherB.setOrder(1);
        inherB.setVal("rootB");
        for (int i = 0; i < 3; i++) {
            inherB.getStringListB().add(inherB.getVal() + ":string" + i);
        }
        pm.makePersistent(inherB);

        InherC inherC = new InherC();
        inherC.setOrder(2);
        inherC.setVal("rootC");
        for (int i = 0; i < 3; i++) {
            inherC.getStringListC().add(inherC.getVal() + ":string" + i);
        }
        pm.makePersistent(inherC);

        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(InherA.class);
        q.setOrdering("order ascending");
        q.setFetchGroup("testInher2");
        q.setFilter("val.startsWith(param)");
        q.declareParameters("String param");
        List result = (List)q.execute("root");
        Assert.assertEquals(3, result.size());
        countExecQueryEvents();
        inherA = (InherA)result.get(0);
        Assert.assertEquals(3, inherA.getStringListA().size());

        List sList = inherA.getStringListA();
        for (int i = 0; i < sList.size(); i++) {
            Assert.assertEquals("rootA:string" + i, (String)sList.get(i));
        }

        //
        sList = inherA.getInherAList();
        Assert.assertEquals(3, sList.size());
        for (int i = 0; i < sList.size(); i++) {
            InherA a = (InherA)sList.get(i);
        }

        inherA = (InherA)sList.get(0);
        List list = inherA.getStringListA();
        for (int i = 0; i < list.size(); i++) {
            String s = (String)list.get(i);
            Assert.assertEquals("rootA:inherA:string" + i, s);
        }

        inherB = (InherB)sList.get(1);
        list = inherB.getStringListB();
        for (int i = 0; i < list.size(); i++) {
            String s = (String)list.get(i);
            Assert.assertEquals("rootA:inherB:string" + i, s);
        }

        inherC = (InherC)sList.get(2);
        list = inherC.getStringListC();
        for (int i = 0; i < list.size(); i++) {
            String s = (String)list.get(i);
            Assert.assertEquals("rootA:inherC:string" + i, s);
        }

        inherB = (InherB)result.get(1);
        Assert.assertEquals(3, inherB.getStringListB().size());
        sList = inherB.getStringListB();
        for (int i = 0; i < sList.size(); i++) {
            Assert.assertEquals("rootB:string" + i, (String)sList.get(i));
        }

        inherC = (InherC)result.get(2);
        Assert.assertEquals(3, inherC.getStringListC().size());
        sList = inherC.getStringListC();
        for (int i = 0; i < sList.size(); i++) {
            Assert.assertEquals("rootC:string" + i, (String)sList.get(i));
        }

        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

    private void addColItems(InherA root) {
        InherA inherA = new InherA();
        inherA.setOrder(0);
        inherA.getStringListA().add(root.getVal() + ":inherA:string0");
        inherA.getStringListA().add(root.getVal() + ":inherA:string1");
        inherA.getStringListA().add(root.getVal() + ":inherA:string2");
        root.getInherAList().add(inherA);

        InherB inherB = new InherB();
        inherB.setOrder(1);
        inherB.getStringListB().add(root.getVal() + ":inherB:string0");
        inherB.getStringListB().add(root.getVal() + ":inherB:string1");
        inherB.getStringListB().add(root.getVal() + ":inherB:string2");
        root.getInherAList().add(inherB);

        InherC inherC = new InherC();
        inherC.setOrder(2);
        inherC.getStringListC().add(root.getVal() + ":inherC:string0");
        inherC.getStringListC().add(root.getVal() + ":inherC:string1");
        inherC.getStringListC().add(root.getVal() + ":inherC:string2");
        root.getInherAList().add(inherC);
    }

    public void testRefs1() throws Exception {
        PersistenceManager pm = pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        int noOfRefA = 5;
        for (int i = 0; i < noOfRefA; i++) {
            RefA refA = new RefA();
            refA.setOrder(i);
            refA.setVal("refA" + i);
            RefAB refAB = new RefAB();
            refA.setRefAB(refAB);
            RefBC refBC = new RefBC();
            refAB.setRefBC(refBC);

            RefAD refAD = new RefAD();
            refAD.setVal(refA.getVal() + ":refAD" + i);

            refAD.getStringListAD().add("NEO0:" + refAD.getVal() + "NEO0");
            refAD.getStringListAD().add("NEO1:" + refAD.getVal() + "NEO1");
            refAD.getStringListAD().add("NEO2:" + refAD.getVal() + "NEO2");
            refAD.getStringListAD().add("NEO3:" + refAD.getVal() + "NEO3");

            refA.setRefAD(refAD);
            pm.makePersistent(refA);
        }
        pm.currentTransaction().commit();

        pm.currentTransaction().begin();
        VersantQuery q = (VersantQuery)pm.newQuery(RefA.class);
        q.setFetchGroup("testRef1");
        q.setOrdering("order ascending");
        List result = (List)q.execute();
        Assert.assertEquals(noOfRefA, result.size());
        countExecQueryEvents();

        for (int i = 0; i < result.size(); i++) {
            RefA refA = (RefA)result.get(i);
            List sList = refA.getRefAD().getStringListAD();
            Assert.assertEquals(4, sList.size());
            String s = refA.getRefAD().getVal();
            for (int j = 0; j < sList.size(); j++) {
                Assert.assertEquals("NEO" + j + ":" + s + "NEO" + j,
                        (String)sList.get(j));
            }
        }

        Assert.assertEquals(0, countExecQueryEvents());
        pm.close();
    }

}
