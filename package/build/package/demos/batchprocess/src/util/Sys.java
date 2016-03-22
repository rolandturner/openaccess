
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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.PersistenceManager;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to manage the PMF and keep the PM for the current Thread
 * in a ThreadLocal. This is based on code by Bin Sun (sunbin@pconline.com.cn).
 *
 */
public class Sys {

    public static final boolean DEBUG = true;

    private static class State {
        public PersistenceManager pm;
    }

    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal() {
        protected Object initialValue() {
            return new State();
        }
    };

    private static PersistenceManagerFactory pmf;
    private static final String PROJECT_FILE = "/versant.properties";

    private static State getState() {
        return (State)THREAD_LOCAL.get();
    }

    /**
     * Get the PM for the current Thread.
     */
    public static PersistenceManager pm() {
        State state = getState();
        PersistenceManager pm = state.pm;
        if (pm == null || pm.isClosed()) {
            pm = state.pm = pmf().getPersistenceManager();
        }
        return pm;
    }

    /**
     * Close the PM for this thread if any. Does a rollback if a tx is
     * active.
     */
    public static void cleanup() {
        State state = getState();
        PersistenceManager pm = state.pm;
        if (pm != null) {
            if (!pm.isClosed()) {
                if (pm.currentTransaction().isActive()) {
                    pm.currentTransaction().rollback();
                }
                pm.close();
            }
            state.pm = null;
        }
    }

    /**
     * Get the PMF. Starts a new JDO Genie server if not already done.
     */
    public static synchronized PersistenceManagerFactory pmf() {
        if (pmf == null) {
            pmf = JDOHelper.getPersistenceManagerFactory(loadProperties());
        }
        return pmf;
    }

    /**
     * Close the PMF.
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
}

