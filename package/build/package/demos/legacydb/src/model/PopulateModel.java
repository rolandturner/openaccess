
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
package model;

import util.JDOSupport;

import javax.jdo.PersistenceManager;

/** 
 * Populates the database with data for testing. Using a class like this
 * speeds up development. You can drop and recreate the tables when you make
 * model changes and easily refill them with test data.
 */
public class PopulateModel {

    public static void main(String[] args) {
        try {
            JDOSupport.init(null);
            new PopulateModel().go();
            JDOSupport.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

    public void go() {
        PersistenceManager pm = JDOSupport.getPM();

        // create countries
        Country za = new Country(Country.ZA.code, "South Africa");
        pm.makePersistent(za);
        Country na = new Country(Country.NA.code, "Namibia");
        pm.makePersistent(na);
        pm.makePersistent(new Country(Country.UK.code, "United Kingdom"));
        pm.makePersistent(new Country(Country.US.code, "United States"));

        // create branches
        Branch capeTown = new Branch(1, "Cape Town");
        capeTown.setCountry(za);
        pm.makePersistent(capeTown);
        Branch windhoek = new Branch(2, "Windhoek");
        windhoek.setCountry(na);
        pm.makePersistent(windhoek);

        // create suppliers
        Object[] a = new Object[]{
            capeTown, "100", "Tiger Oats",
            capeTown, "101", "National Brands",
            capeTown, "102", "Premier Milling",
            windhoek, "100", "National Brands",
            windhoek, "101", "Rolled Oats SA",
        };
        for (int i = 0; i < a.length; ) {
            Supplier supplier = new Supplier((Branch)a[i++],
                    Integer.parseInt((String)a[i++]), (String)a[i++]);
            pm.makePersistent(supplier);
        }

        // create items
        a = new Object[]{
            capeTown, "OAT001", "Oats So Easy",
            capeTown, "OAT002", "Instant Oats",
            capeTown, "PRN001", "Pro Nutro",
            capeTown, "PRN002", "Pro Nutro (chocolate)",
            windhoek, "PRN001", "Pro Nutro (honey)",
            windhoek, "OAT001", "Instant Oats",
            windhoek, "OAT002", "Horsey Oats",
        };
        for (int i = 0; i < a.length; ) {
            Item item = new Item((Branch)a[i++], (String)a[i++], (String)a[i++]);
            pm.makePersistent(item);
        }

        // create orders
        a = new Object[]{
            capeTown, "5000", "100", new String[]{"OAT001", "10", "OAT002", "5"},
            capeTown, "5001", "102", new String[]{"PRN001", "7"},
            capeTown, "5002", "100", new String[]{"OAT002", "12"},
            windhoek, "5000", "101", new String[]{"OAT002", "5"},
        };
        for (int i = 0; i < a.length; ) {
            Order o = new Order((Branch)a[i++], Integer.parseInt((String)a[i++]));
            Supplier.ID sid = new Supplier.ID(o.getBranch(), (String)a[i++]);
            o.setSupplier((Supplier)pm.getObjectById(sid, false));
            String[] b = (String[])a[i++];
            for (int j = 0; j < b.length; ) {
                Item.ID iid = new Item.ID(o.getBranch(), b[j++]);
                o.addOrderLine(
                        (Item)pm.getObjectById(iid, false),
                        Integer.parseInt(b[j++]));
            }
            pm.makePersistent(o);
        }

        JDOSupport.commit();
    }

}

