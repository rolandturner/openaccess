
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
package tut1;

import javax.jdo.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * MainServer class for tut1. This creates a JDO Genie server.
 * <p/>
 * To test remote operation start a server in one console (ant run-server) and
 * then run the demo in remote mode from another console (ant run-remote).
 *
 */
public class MainServer {

    public static void main(String[] args) {
        try {
            // load versant.properties project file as properties to connect
            Properties p = loadProperties();
            // start a JDO Genie server and wait for connections
            JDOHelper.getPersistenceManagerFactory(p);
            System.out.println("Started remote server .. do 'ant run-remote' from another " +
                    "console (Ctrl-C to exit)");
            for (;;) {
                Thread.sleep(60000);
	    }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = MainServer.class.getResourceAsStream("/versant.properties");
            if (in == null) {
                throw new IOException("versant.properties not on classpath");
            }
            p.load(in);
        } finally {
            if (in != null) in.close();
        }
        return p;
    }

    private PersistenceManager pm;

    public MainServer(PersistenceManager pm) {
        this.pm = pm;
    }
}
