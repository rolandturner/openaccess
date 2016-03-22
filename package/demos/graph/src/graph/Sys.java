
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
package graph;

import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.server.perf.PerfEvent;
import com.versant.core.jdbc.perf.JdbcPerfEvent;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to manage the PMF and keep the PM for the current Thread
 * in a ThreadLocal. This is based on code by Bin Sun (sunbin@pconline.com.cn).
 */
public class Sys {

    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    private static PersistenceManagerFactory pmf;
    private static int lastEventId;

    private static final String PROJECT_FILE = "/versant.properties";

    /**
     * Get the PM for the current Thread.
     */
    public static PersistenceManager pm() {
        PersistenceManager pm = (PersistenceManager)THREAD_LOCAL.get();
        if (pm == null || pm.isClosed()) {
            THREAD_LOCAL.set(pm = pmf().getPersistenceManager());
        }
        return pm;
    }

    /**
     * Close the PM for this thread if any. Does a rollback if a tx is
     * active.
     */
    public static void cleanup() {
        PersistenceManager pm = (PersistenceManager)THREAD_LOCAL.get();
        if (pm != null) {
            if (!pm.isClosed()) {
                if (pm.currentTransaction().isActive()) {
                    pm.currentTransaction().rollback();
                }
                pm.close();
            }
            THREAD_LOCAL.set(null);
        }
    }

    /**
     * Get the PMF. This will create one using JDOHelper if needed.
     */
    public static synchronized PersistenceManagerFactory pmf() {
        if (pmf == null) {
            pmf = JDOHelper.getPersistenceManagerFactory(loadProperties());
        }
        return pmf;
    }

    /**
     * Close the PMF (NOP if none).
     */
    public static synchronized void shutdown() {
        if (pmf != null) {
            pmf.close();
            pmf = null;
        }
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        InputStream is = Sys.class.getResourceAsStream(PROJECT_FILE);
        if (is == null) {
            throw new RuntimeException("Resource not found: " + PROJECT_FILE);
        }
        try {
            p.load(is);
            is.close();
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    /**
     * Empty the level 2 cache.
     */
    public static void clearLevel2Cache() {
        // empty level 2 cache
        ((VersantPersistenceManagerFactory)pmf()).evictAll();
    }

    /**
     * Do getObjectById but fetch the named fetch group instead of the
     * default fetch group.
     */
    public static Object getObjectById(Object oid, String fetchGroup) {
        VersantPersistenceManager pm = (VersantPersistenceManager)pm();
        Object ans = pm.getObjectById(oid, false);
        pm.loadFetchGroup((PersistenceCapable)ans, fetchGroup);
        return ans;
    }

    /**
     * Return the number of SELECT queries in the JDO Genie event log since
     * the last call to this method. Event logging must be set to 'normal',
     * 'verbose' or 'all' for this to work.
     */
    public static int getSelectCount() {
        VersantPersistenceManagerFactory pmf =
                (VersantPersistenceManagerFactory)pmf();
        PerfEvent[] a = pmf.getNewPerfEvents(lastEventId);
        int count = 0;
        if (a != null) {
            for (int i = 0; i < a.length; i++) {
                PerfEvent e = a[i];
                if (e instanceof JdbcPerfEvent
                        && ((JdbcPerfEvent)e).getType() == JdbcPerfEvent.STAT_EXEC_QUERY) {
                    count++;
                }
            }
            lastEventId = a[a.length - 1].getId();
        }
        return count;
    }

}
