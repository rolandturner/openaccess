
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
package customtypes;

import customtypes.model.Contact;
import customtypes.model.PhoneNumber;
import customtypes.model.PngImage;

import javax.jdo.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Insert test data for this demo.
 * $Id: InsertData.java,v 1.1 2005/03/08 08:31:44 david Exp $
 */
public class InsertData {

    public static void main(String[] args) {
        try {
            PersistenceManager pm = Sys.pmf().getPersistenceManager();

            pm.currentTransaction().begin();
            Object[] a = new Object[]{
                "David Tinker", "david@versant.com", "david.gif",
                new PhoneNumber("27", "21", "555-1234"),
                "Jaco Uys", "jaco@versant.com", "jaco.jpg",
                new PhoneNumber("44", "11", "555-1235"),
                "Carl Cronje", "carl@versant.com", "carl.jpg",
                new PhoneNumber("1", "31", "555-1236"),
                "Dirk Le Roux", "dirk@versant.com", "dirk.jpg",
                new PhoneNumber("43", "24", "555-1237"),
            };
            for (int i = 0; i < a.length; i += 4) {
                Contact c = new Contact();
                c.setName((String)a[i]);
                c.setEmail((String)a[i + 1]);
                c.setPngImage(createPngImage((String)a[i + 2]));
                c.setPhone((PhoneNumber)a[i + 3]);
                pm.makePersistent(c);
            }
            pm.currentTransaction().commit();

            pm.close();

            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

    private static PngImage createPngImage(String name) throws IOException {
        URL url = InsertData.class.getResource(name);
        if (url == null) return null;
        return new PngImage(url);
    }

}

