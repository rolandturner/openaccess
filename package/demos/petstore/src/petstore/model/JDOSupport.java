
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
package petstore.model;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOHelper;
import java.util.Properties;

public class JDOSupport {

    private static JDOSupport instance;

    private PersistenceManagerFactory pmFactory;

    public static JDOSupport getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Not initialized");
        }
        return instance;
    }

    private JDOSupport() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        Properties props = new Properties();
        props.load(loader.getResourceAsStream("versant.properties"));
        pmFactory = JDOHelper.getPersistenceManagerFactory(props, loader);
    }

    public static void init() throws Exception {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new JDOSupport();
    }

    public PersistenceManagerFactory getPMFactory() {
        return pmFactory;
    }

}
