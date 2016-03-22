
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
package tut1;

import tut1.model.*;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Client class for the Tut1 session bean. This looks up the bean and
 * invokes methods to demonstrate using JDO Genie.
 */
public class Main {

    public static void main(String[] args) {
        try {
            // lookup the Tut1 session bean
            InitialContext context = new InitialContext();
            Object obj = null;
            try {
                obj = context.lookup("Tut1");  // JBoss and WebLogic
            } catch (NameNotFoundException x) {
                obj = context.lookup("java:comp/env/ejb/Tut1"); // WebSphere
            }   
            Tut1Home home = (Tut1Home)PortableRemoteObject.narrow(obj, Tut1Home.class);
            Tut1 tut1 = home.create();

            // create the catalog
            tut1.createCatalog();

            // display the catalog
            for (Iterator i = tut1.listItems().iterator(); i.hasNext(); ) {
                Item item = (Item)i.next();
                System.out.println(item.getCode() + ", " + item.getDescription() +
                    ", " + item.getCategory().getName());
            }

            // create an order
            Order o = new Order(new Customer("DOE001", "John Doe"));
            o.addLine(new OrderLine(tut1.lookupItem("D001"), 3));
            o.addLine(new OrderLine(tut1.lookupItem("C001"), 1));
            String orderNo = tut1.createOrder(o);
            System.out.println("orderNo = " + orderNo);

            // retrieve the order and list it
            o = tut1.lookupOrder(orderNo);
            SimpleDateFormat df = new SimpleDateFormat();
            System.out.println("Customer: " + o.getCustomer() + " " +
                    df.format(o.getOrderDate()));
            for (Iterator i = o.getLines().iterator(); i.hasNext(); ) {
                OrderLine line = (OrderLine)i.next();
                System.out.println(line.getItem().getCode() + " x " + line.getQty());
            }
            System.out.println("---");

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

}

