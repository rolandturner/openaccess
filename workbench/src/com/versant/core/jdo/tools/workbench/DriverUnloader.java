
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
package com.versant.core.jdo.tools.workbench;

import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.SQLException;

/**
 * This will unload a JDBC driver from the DriverManager. Unfortunately this
 * can only be done by a class loaded by the same classloader that loaded
 * the driver. If this class is a loaded by the class that loaded the
 * driver it can unload it.
 * @see com.versant.core.jdo.tools.workbench.model.MdDataStore#loadDriver
 * @see com.versant.core.jdo.tools.workbench.model.MdClassLoader#DRIVER_UNLOADER_NAME
 * @keep-all
 */
public class DriverUnloader {

    public static void unload(Driver d) throws SQLException {
        DriverManager.deregisterDriver(d);
    }

}

 
