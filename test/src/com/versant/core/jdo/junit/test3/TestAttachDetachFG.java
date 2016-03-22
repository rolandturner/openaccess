
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
package com.versant.core.jdo.junit.test3;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.junit.VersantTestCase;
import com.versant.core.jdo.junit.test3.model.attachdetach.fgtest.*;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.FieldMetaData;

import java.io.*;
import java.util.*;

public class TestAttachDetachFG extends VersantTestCase {

    public TestAttachDetachFG(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite s = new TestSuite();
        String[] a = new String[]{
            "test1111Ref",
            "test1111Ref2",
            "testCol1",
            "test9Fg"
        };
        for (int i = 0; i < a.length; i++) {
            s.addTest(new TestAttachDetachFG(a[i]));
        }
        return s;
    }

    public void test1111Ref() throws IOException, ClassNotFoundException {
        ClassMetaData cmd = getCmd(Order.class);
        printFG(0, cmd.getFetchGroup("test1111Ref"), true);
        test1111Ref("test1111Ref");
    }

    public void test1111Ref2() throws IOException, ClassNotFoundException {
        ClassMetaData cmd = getCmd(Order.class);
        printFG(0, cmd.getFetchGroup("test1111Ref2"), true);
        test1111Ref("test1111Ref2");
    }

    private void printFG(int i, FetchGroup fg, boolean printName) {
        if (fg == null) return;
        ClassMetaData cmd = fg.classMetaData;
        if (printName) {
            printLine(i);
            System.out.println(fg.name + " on " + cmd.cls.getName());
        }
        i++;
        FetchGroupField[] fields = fg.fields;
        boolean defaultFG = fg == cmd.fetchGroups[0];
        for (int j = 0; j < fields.length; j++) {
            FetchGroupField field = fields[j];
            FieldMetaData fmd = field.fmd;
            if (fmd.fake) continue;
            if (defaultFG && !fmd.isJDODefaultFetchGroup()) continue;
            printLine(i);
            System.out.println(field.fmd.name);
            FetchGroup fg2 = field.nextFetchGroup;
            printFG(i, fg2, true);
        }
        printFG(--i, fg.superFetchGroup, false);
    }

    private void printLine(int i) {
        for (int x = i; x > 0; x--) {
            System.out.print("---");
        }
    }

    public void test1111Ref(String fg) throws IOException,
            ClassNotFoundException {
        Order order = new Order(new Customer(5, new Address("protea",
                new City("CT"), 7735),
                "Dirk"));
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        Order detOrder = (Order)pm.versantDetachCopy(
                Collections.singletonList(order), fg).iterator().next();
        check1111Ref(detOrder, fg);
        pm.currentTransaction().commit();
        pm.close();
        check1111Ref(detOrder, fg);
    }

    private void check1111Ref(Order detOrder, String fg) {
        Assert.assertNotNull("Detach failed", detOrder);
        Assert.assertNotNull("Customer not loaded but in detach FG " + fg,
                detOrder.customer);
        Assert.assertNotNull("Address not loaded but in detach FG " + fg,
                detOrder.customer.address);
        Assert.assertNotNull("City not loaded but in detach FG " + fg,
                detOrder.customer.address.city);
        Assert.assertNotNull("City name not loaded but in detach FG " + fg,
                detOrder.customer.address.city.name);
        try {
            System.out.println("\n\n\n" + detOrder.customer.address.street);
            Assert.fail("Address.street is loaded but not in detach FG " + fg);
        } catch (Exception e) {}
        try {
            System.out.println(detOrder.customer.address.code);
            Assert.fail("Address.code is loaded but not in detach FG " + fg);
        } catch (Exception e) {}
        try {
            System.out.println(detOrder.customer.customerNo);
            Assert.fail(
                    "Customer.customerNo is loaded but not in detach FG " + fg);
        } catch (Exception e) {}
        try {
            System.out.println(detOrder.lines);
            Assert.fail("Order.lines is loaded but not in detach FG " + fg);
        } catch (Exception e) {}
    }

    public void printFG() throws IOException, ClassNotFoundException {
        ClassMetaData cmd = getCmd(Order.class);
        printFG(0, cmd.getFetchGroup("test1111Ref"), true);
        printFG(0, cmd.getFetchGroup("test1111Ref2"), true);
        printFG(0, cmd.getFetchGroup("testCol1"), true);
    }

    public void testCol1() throws IOException, ClassNotFoundException {
        Order order = new Order(new Customer(5, new Address("protea",
                new City("CT"), 7735),
                "Dirk"));
        Supplier s = new Supplier(order.customer.address, "Dave", 8);
        order.lines.add(new OrderLine(new Product(s, "Stuff")));
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();
        String fg = "testCol1";
        Order detOrder = (Order)pm.versantDetachCopy(
                Collections.singletonList(order), fg).iterator().next();
        checkCol(detOrder, fg);
        pm.currentTransaction().commit();
        pm.close();
        checkCol(detOrder, fg);
    }

    private void checkCol(Order detOrder, String fg) {
        Assert.assertNotNull("Detach failed", detOrder);
        Assert.assertNotNull("Customer not loaded but in detach FG " + fg,
                detOrder.customer);
        Assert.assertNotNull(
                "Customer.Address not loaded but in detach FG " + fg,
                detOrder.customer.address);
        Assert.assertNotNull("City not loaded but in detach FG " + fg,
                detOrder.customer.address.city);
        Assert.assertNotNull("City name not loaded but in detach FG " + fg,
                detOrder.customer.address.city.name);
        Assert.assertNotNull("Order.lines not loaded but in detach FG " + fg,
                detOrder.lines);
        Assert.assertEquals("Order.lines empty in detach FG " + fg, 1,
                detOrder.lines.size());
        Object o = detOrder.lines.get(0);
        Assert.assertTrue(
                "Order.lines contains something other than OrderLine " + o.getClass().getName(),
                (o instanceof OrderLine));
        OrderLine ol = (OrderLine)o;
        Assert.assertNotNull("Product not loaded but in detach FG " + fg,
                ol.product);
        Assert.assertNotNull("Supplier not loaded but in detach FG " + fg,
                ol.product.supplier);
        Assert.assertNotNull(
                "Supplier.Address not loaded but in detach FG " + fg,
                ol.product.supplier.address);
        Assert.assertEquals(
                "Supplier.supplierNo not loaded but in detach FG " + fg, 8,
                ol.product.supplier.supplierNo);
        Assert.assertEquals("Address.code not loaded but in detach FG " + fg,
                7735, ol.product.supplier.address.code);
        try {
            System.out.println("\n\n\n" + detOrder.customer.address.street);
            Assert.fail("Address.street is loaded but not in detach FG " + fg);
        } catch (Exception e) {}
        try {
            System.out.println(detOrder.customer.customerNo);
            Assert.fail(
                    "Customer.customerNo is loaded but not in detach FG " + fg);
        } catch (Exception e) {}
    }

    public void test9Fg() throws IOException, ClassNotFoundException {
        VersantPersistenceManager pm = (VersantPersistenceManager)pmf().getPersistenceManager();
        pm.currentTransaction().begin();

        Address address = new Address("protea", new City("CT"), 7735);
        SupplierHolder holder = new SupplierHolder();
        pm.makePersistent(holder);
        holder.supplier1 = new Supplier(address, "Supplier 1", 1);
        holder.supplier2 = holder.supplier1;
        holder.supplier3 = holder.supplier1;
        holder.supplier4 = holder.supplier1;
        holder.supplier5 = holder.supplier1;
        holder.supplier6 = holder.supplier1;
        holder.supplier7 = holder.supplier1;
        holder.supplier8 = holder.supplier1;
        holder.supplier9 = holder.supplier1;
        pm.currentTransaction().commit();
        pm.currentTransaction().begin();
        pm.versantDetachCopy(Collections.singletonList(holder), "test9Fg");
        pm.currentTransaction().commit();
        pm.close();
    }
}
