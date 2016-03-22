
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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to manage the PMF. This also contains a main method to
 * start a standalone JDO Genie server to connect to when running the
 * app in remote mode.
 */
public class Sys {

    private static final String PROJECT_FILE = "/versant.properties";

    private static PersistenceManagerFactory pmf;
    private static String jdoHost;

    /**
     * Get the PMF. This will connect to a remote JDO Genie server if the
     * system property jdo.host has been set.
     */
    public static PersistenceManagerFactory pmf() {
        if (pmf == null) {
            Properties p = loadProperties();
            jdoHost = System.getProperty("jdo.host");
            if (jdoHost != null) p.put("host", jdoHost);
            pmf = JDOHelper.getPersistenceManagerFactory(p);
        }
        return pmf;
    }

    /**
     * Close the PMF.
     */
    public static void shutdown() {
        if (pmf != null) {
            if (jdoHost == null) pmf.close(); // dont shutdown server!
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
     * Start a standalone JDO Genie server.
     */
    public static void main(String[] args) {
        try {
            Sys.pmf();
            System.out.println("Press Ctrl-C to close server");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
