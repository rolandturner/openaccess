
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

import model.Resturant;
import model.Address;
import model.Supplier;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Collection;
import java.util.Iterator;

/**
 * This creates some Supplier and Resturant instances with Address'es
 * and runs a query through the interface reference on Address.
 *
 */
public class Main {

    public static void main(String[] args) {
        try {
            PersistenceManagerFactory pmf =
                    JDOHelper.getPersistenceManagerFactory(loadProperties());
            PersistenceManager pm = pmf.getPersistenceManager();

            // Make some Resturant's and Supplier's.
            pm.currentTransaction().begin();
            pm.makePersistent(new Supplier("Chickens Are Us",
                    new Address("1 Java Way", "Beanville")));
            pm.makePersistent(new Supplier("Whats the Beef?",
                    new Address("32 Generic Lane", "Templatecity")));
            pm.makePersistent(new Resturant("Dodgy Chicken",
                    new Address("10 Flu Street", "Hungryville")));
            pm.makePersistent(new Resturant("The Mad Cow",
                    new Address("22 Spongyform Lane", "Porkville")));
            pm.currentTransaction().commit();

            // Bring back some Address'es using a cast. This query will be
            // executed purely in SQL.
            pm.currentTransaction().begin();
            Query q = pm.newQuery(Address.class);
            q.setFilter("((Supplier)owner).name.startsWith(p)");
            q.declareParameters("String p");
            Collection ans = (Collection)q.execute("Chic");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Address a = (Address)i.next();
                System.out.println(a + " owner " + a.getOwner());
            }
            q.closeAll();
            pm.currentTransaction().commit();

            pm.close();
            pmf.close();

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

    private static Properties loadProperties() throws IOException {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = Main.class.getResourceAsStream("/versant.properties");
            if (in == null) {
                throw new IOException("versant.properties not on classpath");
            }
            p.load(in);
        } finally {
            if (in != null) in.close();
        }
        return p;
    }

}

