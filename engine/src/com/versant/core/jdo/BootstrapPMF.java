
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
package com.versant.core.jdo;

import java.util.Map;
import java.util.Properties;

/**
 * Bootstrap class for JDOHelper.getPersistenceManagerFactory. This checks
 * the 'versant.host' property to see if remote access is required and delegates to
 * the appropriate PMF class.
 */
public class BootstrapPMF {

    /**
     * This is called be JDOHelper to construct a PM factory from a properties
     * file.
     */
    public static javax.jdo.PersistenceManagerFactory 
    				getPersistenceManagerFactory(Properties props) {
        String host = props.getProperty("versant.host");
        if (host == null) {
            host = props.getProperty("host");
            if (host != null) {
                props.setProperty("versant.host", host);
            }
        }
        if (host != null && host.trim().length() > 0) {
            props.setProperty(
                    "javax.jdo.PersistenceManagerFactoryClass",
                    "com.versant.core.jdo.remote.RemotePersistenceManagerFactory");
            return javax.jdo.JDOHelper.getPersistenceManagerFactory(props,
                    BootstrapPMF.class.getClassLoader());
        }
        return PersistenceManagerFactoryImp.getPersistenceManagerFactory(props);
    }

    public static javax.jdo.PersistenceManagerFactory 
	getPersistenceManagerFactory(Map mprops) {
    	Properties props = new Properties();
    	props.putAll(mprops);
    	return getPersistenceManagerFactory(props);
}
    
}


