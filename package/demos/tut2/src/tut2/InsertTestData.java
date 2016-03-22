
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
package tut2;

import tut2.model.Address;
import tut2.model.Country;
import tut2.model.Person;

import javax.jdo.PersistenceManager;

/**
 * Simple class to insert some test data for the demo. Using a class like
 * this as part of your build process makes it quick and easy to change
 * the model during development without having to migrate any data.
 */
public class InsertTestData {

    public static void main(String[] args) {
        try {
            InsertTestData m = new InsertTestData();
            m.go();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

    private PersistenceManager pm;
    private Country[] countries;

    public InsertTestData() {
        pm = JDOSupport.getInstance().getPMF().getPersistenceManager();
    }

    public void go() {
        pm.currentTransaction().begin();
        createCountries();
        createPersons();
        pm.currentTransaction().commit();
    }

    private void createCountries() {
        String[] a = new String[]{
            "za", "South Africa",
            "us", "United States",
            "uk", "United Kingdom",
            "fr", "France",
            "de", "Germany",
            "ru", "Russia",
        };
        countries = new Country[a.length / 2];
        for (int i = 0; i < a.length; i += 2) {
            countries[i / 2] = new Country(a[i], a[i + 1]);
        }
        pm.makePersistentAll(countries);
    }

    private void createPersons() {
        String[] a = new String[]{
            "Jaco", "Uys",
            "Carl", "Cronje",
            "David", "Tinker",
            "Michael", "Netshipse",
            "Alex", "Harin",
            "Marco", "Theart",
            "Corne", "Dreyer",
        };
        int cc = 0;
        int pc = 0;
        for (int i = 0; i < a.length; i += 2, pc++) {
            Person p = new Person(a[i], a[i + 1]);
            for (int j = 0; j < pc % 4; j++) {
                Address addr = new Address();
                addr.setStreet((j + 1) + " Bean Street");
                addr.setCity("Javaville");
                addr.setCode("" + (j + 1) * 10000);
                addr.setCountry(countries[cc]);
                cc = (cc + 1) % countries.length;
                p.setAddress(addr);
            }
            pm.makePersistent(p);
        }
    }

}

