
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

import aggregates.model.Address;
import aggregates.model.Contact;
import aggregates.model.Country;

import javax.jdo.PersistenceManager;
import java.util.Random;

/**
 * Insert test data for the demo.
 */
public class InsertData {

    public static void main(String[] args) {
        try {
            PersistenceManager pm = Sys.pm();

            Random rnd = new Random(10);
            pm.currentTransaction().begin();

            String a[] = new String[]{
                "ZA", "South Africa",
                "US", "United States",
                "UK", "United Kingdom",
                "CN", "China",
                "IN", "India",
                "DE", "Germany",
                "FR", "France",
                "BE", "Belgium",
            };
            Country[] countries = new Country[a.length / 2];
            for (int i = 0; i < a.length; i += 2) {
                countries[i / 2] = new Country(a[i], a[i + 1]);
            }
            pm.makePersistentAll(countries);

            String[] names = new String[]{
                "david", "jaco", "dirk", "carl", "lisa", "pickle", "michelle"
            };
            for (int i = 0; i < names.length; i++) {
                for (int j = 0; j < countries.length; j++) {
                    if (rnd.nextBoolean()) {
                        Address addr = new Address(
                                j + " " + names[i] + " drive",
                                names[i] + "ville", countries[j]);
                        Contact c = new Contact(names[i],
                                Integer.toString(rnd.nextInt(10000000)),
                                names[i] + j + "@somewhere.com", addr,
                                rnd.nextInt(40) + 10);
                        pm.makePersistent(c);
                    }
                }
            }

            pm.currentTransaction().commit();

            Sys.cleanup();
            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
