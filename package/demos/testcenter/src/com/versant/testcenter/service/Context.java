
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
package com.versant.testcenter.service;

import com.versant.testcenter.model.SystemUser;
import com.versant.core.jdo.VersantPersistenceManagerFactory;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOHelper;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Context associated with current thread. Used to provide current thread's
 * {@link PersistenceManager instance} using static refence to application's
 * {@link PersistenceManagerFactory}. Also keeps track of the currently
 * logged on user.<p>
 * <p/>
 * This code is not web-application specific, but can be used in
 * any environment.<p>                           cat
 * <p/>
 * A main method is included so this class can be used to start a standalone
 * JDO Genie server. The gui client can connect to this server instead of
 * running a web app.<p>
 */
public class Context {

    public static void main(String[] args) {
        try {
            initialize(loadJDOProperties());
            System.out.println("Press Ctrl-C to exit");
            for (;;) {
                //keeping server alive as all server threads are deamon threads
                Thread.sleep(60000);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static PersistenceManagerFactory pmf;
    private static ThreadLocal contextHolder = new ThreadLocal();

    private PersistenceManager pm;
    private SystemUser currentUser;

    private Context() {
    }

    /**
     * Creates and configures {@link PersistenceManagerFactory}.
     *
     * @param props JDO configuration parameters
     */
    public static void initialize(Properties props) {
        pmf = JDOHelper.getPersistenceManagerFactory(props);
    }

    /**
     * Initialize from an existing PMF.
     */
    public static void initialize(PersistenceManagerFactory pmfParam) {
        pmf = pmfParam;
    }

    /**
     * Load the JDO configuration from the classpath.
     */
    public static Properties loadJDOProperties() throws IOException {
        Properties props = new Properties();
        InputStream is = Context.class.getResourceAsStream(
                "/versant.properties");
        if (is == null) {
            throw new IOException(
                    "Cannot find versant.properties on classpath");
        }
        try {
            props.load(is);
        } finally {
            is.close();
        }
        return props;
    }

    /**
     * Shut down JDO engine
     */
    public static void shutdown() {
        // non-portable code with JDO 1.01 this will be replaced by pmf.close()
        ((VersantPersistenceManagerFactory)pmf).shutdown();
    }

    /**
     * Obtain instance of the Context for current thread.
     *
     * @return Current thread's context
     */
    public static Context getContext() {
        Context ctx = (Context)contextHolder.get();
        if (ctx == null) contextHolder.set(ctx = new Context());
        return ctx;
    }

    /**
     * Obtain this context's JDO persistence manager.
     *
     * @return JDO persistence manager
     */
    public PersistenceManager getPersistenceManager() {
        if (pm == null) pm = pmf.getPersistenceManager();
        return pm;
    }

    /**
     * Close Context's instance. Closes its instance of {@link PersistenceManager}
     * and removes reference to this instance from current thread.
     */
    public void close() {
        contextHolder.set(null);
        currentUser = null;
        if (pm != null) {
            pm.close();
            pm = null;
        }
    }

    /**
     * Get the logged on user or null if none.
     */
    public SystemUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Change the logged on user.
     */
    public void setCurrentUser(SystemUser currentUser) {
        this.currentUser = currentUser;
    }

}


