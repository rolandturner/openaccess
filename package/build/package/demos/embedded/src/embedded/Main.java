
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
package embedded;

import embedded.model.*;

import javax.jdo.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * InsertData class for tut1. This creates a simple product catalog, creates a
 * customer and order and amends the order.
 *
 */
public class Main {
    private PersistenceManagerFactory pmf;

    public Main(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    public static void main(String[] args) {
        try {
            // load versant.properties project file as properties to connect
            Properties p = loadProperties();

            // run against local or remote JDO Genie server
            PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
                    p);
            Main main = new Main(pmf);
            main.go();

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private void go() {
        createPerson();
        queryForPerson();

        PersistenceManager pm = pmf.getPersistenceManager();
        pm.currentTransaction().begin();

        EmbeddedSelfRef ref4 = new EmbeddedSelfRef("ref4");
        EmbeddedSelfRef ref3 = new EmbeddedSelfRef("ref3", ref4);
        EmbeddedSelfRef ref2 = new EmbeddedSelfRef("ref2", ref3);
        EmbeddedSelfRef ref1 = new EmbeddedSelfRef("ref1", ref2);
        EmbeddedSelfRef root = new EmbeddedSelfRef("root", ref1);

        pm.makePersistent(root);
        pm.currentTransaction().commit();
        pm.close();

        pm = pmf.getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(EmbeddedSelfRef.class, "val == \"root\"");
        Collection result = (Collection) q.execute();
        EmbeddedSelfRef er = (EmbeddedSelfRef) result.iterator().next();
        for (;er != null; er = er.getNext()) {
            System.out.println("er.getVal() = " + er.getVal());
        }
        pm.currentTransaction().commit();
    }

    private void queryForPerson() {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.currentTransaction().begin();
        Query q = pm.newQuery(Person.class);
        q.setFilter("address.city.code == param");
        q.declareParameters("String param");
        Collection result = (Collection) q.execute("cityCode");
        System.out.println("result = " + result);
        pm.currentTransaction().commit();
        pm.close();
    }

    private void createPerson() {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.currentTransaction().begin();
        City city = new City();
        city.setCode("cityCode");
        city.setName("cityName");

        Address address = new Address();
        address.setStreet("StreetName");
        address.setCity(city);

        Person person = new Person();
        person.setName("personName");
        person.setSurname("personSurname");
        person.setAddress(address);
        pm.makePersistent(person);
        pm.currentTransaction().commit();
        pm.close();
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
