
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
package util;

import model.Registration;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

import com.versant.core.jdo.VersantQuery;
import com.versant.core.jdo.VersantPersistenceManager;

/**
 * Update dateRegistered on all Registration's to the current date. This
 * class contains methods that illustrate different ways of doing this.
 */
public class BatchProcess {

    public static void main(String[] args) {
        try {
            int arg = Integer.parseInt(args[0]);
            switch (arg) {
                case 1:
                    queryForBatchesOfObjects();
                    break;
                case 2:
                    flushAtIntervals();
                    break;
                default:
                    System.out.println("Invalid arg");
            }
            Sys.cleanup();
            Sys.shutdown();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Repeatedly query for batches of 100 objects to update inside the loop.
     * Update each batch in a separate transaction. This is a good approach
     * if you can figure out a query to give you a batch of objects to
     * process. Note that our maxRows option is used to limit the number
     * of rows returned by the query. We could just process up to N results
     * and not use the maxRows option. However some databases (e.g. MySQL)
     * always return all of the rows for a query to the client immediately
     * and dont send them back as they are requested. This will run the client
     * out of memory if there are lots of rows even if only a few are used.
     */
    private static void queryForBatchesOfObjects() {
        System.out.println("=== queryForBatchesOfObjects ===");
        PersistenceManager pm = Sys.pm();

        Query q = pm.newQuery(Registration.class, "dateRegistered < p");
        q.declareParameters("java.util.Date p");
        int batchSize = 100;
        ((VersantQuery)q).setMaxRows(batchSize);

        Date now = new Date();
        int tot = 0;
        for (;;) {
            pm.currentTransaction().begin();
            Collection ans = (Collection)q.execute(now);
            if (ans.size() == 0) break;
            int c = 0;
            for (Iterator i = ans.iterator(); i.hasNext() && c < batchSize; c++) {
                Registration r = (Registration)i.next();
                r.setDateRegistered(now);
            }
            q.closeAll();
            pm.currentTransaction().commit();
            tot += c;
            log("Done " + tot);
        }
    }

    /**
     * Process all of the objects in one big transaction using flush calls
     * to release memory used by dirty instances. Some databases (e.g. MySQL)
     * always return all of the rows for a query to the client immediately
     * and dont send them back as they are requested. This option may not
     * be suitable if you are using a database and JDBC driver that does
     * this.
     */
    private static void flushAtIntervals() {
        System.out.println("=== flushAtIntervals ===");
        PersistenceManager pm = Sys.pm();

        Query q = pm.newQuery(Registration.class, "dateRegistered < p");
        q.declareParameters("java.util.Date p");
        int batchSize = 100;

        pm.currentTransaction().begin();
        Date now = new Date();
        Collection ans = (Collection)q.execute(now);
        int tot = 0;
        for (Iterator i = ans.iterator(); i.hasNext(); ) {
            Registration r = (Registration)i.next();
            r.setDateRegistered(now);
            if (++tot % batchSize == 0) {
                ((VersantPersistenceManager)pm).flush();
                log("Flushed " + tot);
            }
        }
        q.closeAll();
        pm.currentTransaction().commit();
    }

    private static void log(String msg) {
        System.gc();
        int free = (int)(Runtime.getRuntime().freeMemory() >> 10);
        int tot = (int)(Runtime.getRuntime().totalMemory() >> 10);
        System.out.println(msg + " Memory " + free + "/" + tot + " KB");
    }

}

