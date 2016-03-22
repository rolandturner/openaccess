
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
package aggregates;

import aggregates.model.Contact;
import aggregates.model.Country;

import java.util.Collection;
import java.util.Iterator;

import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.VersantQuery;

/**
 * Various queries using JDO 2 preview features.
 */
public class Main {

    public static void main(String[] args) {
        try {
            VersantPersistenceManager pm = (VersantPersistenceManager)Sys.pm();
            VersantQuery q;
            Collection ans;
            Number num;

            // see how many Contacts we have
            q = (VersantQuery)pm.newQuery(Contact.class);
            q.setResult("count(this)");
            num = (Number)q.execute();
            System.out.println("There are " + num + " Contact's");
            q.closeAll();
            System.out.println();

            // see how many Contacts we have for each name
            q = (VersantQuery)pm.newQuery(Contact.class);
            q.setResult("name, count(this)");
            q.setGrouping("name");
            q.setOrdering("name ascending");
            ans = (Collection)q.execute();
            System.out.println("name, count(this)");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Object[] row = (Object[])i.next();
                String name = (String)row[0];
                int count = ((Number)row[1]).intValue();
                System.out.println(name + ", " + count);
            }
            System.out.println("---");
            q.closeAll();
            System.out.println();

            // see how many Contacts we have for each Country and their
            // average ages
            q = (VersantQuery)pm.newQuery(Contact.class);
            q.setResult("address.country, count(this), avg(age)");
            q.setGrouping("address.country");
            q.setOrdering("address.country.name ascending");
            ans = (Collection)q.execute();
            System.out.println("address.country, count(this), avg(age)");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Object[] row = (Object[])i.next();
                Country country = (Country)row[0];
                int count = ((Number)row[1]).intValue();
                float age = ((Number)row[2]).floatValue();
                System.out.println(country + ", " + count + ", " + age);
            }
            System.out.println("---");
            q.closeAll();
            System.out.println();

            // list Country's with at least 3 Contact's
            q = (VersantQuery)pm.newQuery(Contact.class);
            q.setResult("address.country, count(this)");
            q.declareParameters("int n");
            q.setGrouping("address.country having count(this) >= n");
            q.setOrdering("address.country.name ascending");
            ans = (Collection)q.execute(new Integer(3));
            System.out.println(
                    "address.country, count(this) having count(this) >= 3");
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Object[] row = (Object[])i.next();
                Country country = (Country)row[0];
                int count = ((Number)row[1]).intValue();
                System.out.println(country + ", " + count);
            }
            System.out.println("---");
            q.closeAll();
            System.out.println();

            // list Country's with at least 3 Contact's using a named query
            // in the meta data
            q = (VersantQuery)pm.versantNewNamedQuery(Contact.class,
                    "countryContactCounts");
            ans = (Collection)q.execute(new Integer(3));
            for (Iterator i = ans.iterator(); i.hasNext();) {
                Object[] row = (Object[])i.next();
                Country country = (Country)row[0];
                int count = ((Number)row[1]).intValue();
                System.out.println(country + ", " + count);
            }
            System.out.println("---");
            q.closeAll();
            System.out.println();

            Sys.cleanup();
            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

}

