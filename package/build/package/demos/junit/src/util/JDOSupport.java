
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

import com.versant.core.jdo.VersantPersistenceManagerFactory;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOUserException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Utility methods to create the PMF and get PMs.
 *
 */
public class JDOSupport {

    private static PersistenceManagerFactory pmf;

    /**
     * Start the JDO Genie server. This is a NOP if already done.
     */
    public static void init() {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = JDOSupport.class.getResourceAsStream("/versant.properties");
            if (in == null) {
                throw new IOException("versant.properties not on classpath");
            }
            p.load(in);
            in.close();
        } catch (IOException e) {
            throw new JDOUserException(e.toString(), e);
        }
        pmf = JDOHelper.getPersistenceManagerFactory(p);
    }

    /**
     * Shutdown the JDO Genie server if running. This will close all database
     * connections and so on.
     */
    public static void shutdown() {
        if (pmf != null) ((VersantPersistenceManagerFactory)pmf).shutdown();
    }

    /**
     * Get a PersistenceManager.
     */
    public static PersistenceManager getPM() {
        return pmf.getPersistenceManager();
    }

}

