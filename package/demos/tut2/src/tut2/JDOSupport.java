
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
package tut2;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton to create the PersistenceManagerFactory.
 *
 */
public class JDOSupport {

    private static JDOSupport instance = new JDOSupport();
    private PersistenceManagerFactory pmf;

    /**
     * Get the one and only instance.
     */
    public static JDOSupport getInstance() {
        return instance;
    }

    private JDOSupport() {
    }

    public synchronized PersistenceManagerFactory getPMF() {
        if (pmf == null) {
            try {
                Properties p = loadProperties();
                pmf = JDOHelper.getPersistenceManagerFactory(p);
            } catch (IOException e) {
                throw new JDOException(e.getMessage(), e);
            }
        }
        return pmf;
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

