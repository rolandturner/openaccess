
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
package attachdetach;

import attachdetach.model.Country;
import attachdetach.model.Address;
import attachdetach.model.Contact;

import javax.jdo.PersistenceManager;

/**
 * Insert test data for the demo.
 */
public class InsertData {

    public static void main(String[] args) {
        try {
            PersistenceManager pm = Sys.pm();

            pm.currentTransaction().begin();

            Country za = new Country("ZA", "South Africa");
            Country us = new Country("US", "United States");
            Country uk = new Country("UK", "United Kingdom");
            pm.makePersistent(za);
            pm.makePersistent(us);
            pm.makePersistent(uk);

            Contact david = new Contact("david", "555-1234",
                    "david@versant.com",
                    new Address("123 SomeStreet", "Cape Town", za), 32);
            pm.makePersistent(david);

            pm.currentTransaction().commit();

            Sys.cleanup();
            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
