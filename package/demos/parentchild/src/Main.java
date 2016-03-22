
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

import model.*;

import javax.jdo.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * This creates some instances, queries them back and deletes them.
 *
 */
public class Main {

    public static void main(String[] args) {
        try {
            Properties props = loadProperties();
            PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
                    props);

            PersistenceManager pm = pmf.getPersistenceManager();

            // Create p1 and p2 with 3 and 2 children.
            pm.currentTransaction().begin();
            ParentA p1 = new ParentA(1, 1, "p1");
            p1.newChild(1, "p1-c1");
            p1.newChild(2, "p1-c2");
            p1.newChild(3, "p1-c3");
            pm.makePersistent(p1);
            ParentB p2 = new ParentB(1, 2, "p2");
            p2.newChild(1, "p2-c1");
            p2.newChild(2, "p2-c2");
            pm.makePersistent(p2);
            pm.currentTransaction().commit();

            // Query back and display to sysout.
            pm.currentTransaction().begin();

            Query q = pm.newQuery(ParentA.class);
            Collection ans = (Collection)q.execute();
            for (Iterator i = ans.iterator(); i.hasNext();) {
                printParent((Parent)i.next());
            }
            q.closeAll();

            q = pm.newQuery(ParentB.class);
            ans = (Collection)q.execute();
            for (Iterator i = ans.iterator(); i.hasNext();) {
                printParent((Parent)i.next());
            }
            q.closeAll();

            pm.currentTransaction().commit();

            // Delete p1 and p2 using the original references. The children
            // lists have dependent=true so they are automatically deleted.
            pm.currentTransaction().begin();
            pm.deletePersistent(p1);
            pm.deletePersistent(p2);
            pm.currentTransaction().commit();

            pm.close();

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void printParent(Parent p) {
        System.out.println("p.name = " + p.getName());
        for (Iterator j = p.getChildren().iterator(); j.hasNext();) {
            Child c = (Child)j.next();
            System.out.println("  c.name = " + c.getName());
        }
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


