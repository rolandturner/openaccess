
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
package com.versant.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides JDBC Connections.
 */
public interface JdbcConnectionSource {

    /**
     * Get a Connection.
     *
     * @param highPriority If this is true then reserved high priority
     *      connections may be returned (e.g. for key generation)
     * @param autoCommit Must the connection have autoCommit set?
     */
    public Connection getConnection(boolean highPriority,
            boolean autoCommit) throws SQLException;

    /**
     * Return a Connection.
     */
    public void returnConnection(Connection con) throws SQLException;

    /**
     * Get the URL or "" if it is not known. This is for error messages and so
     * on.
     */
    public String getURL();

    /**
     * Get the name of the JDBC driver (typically its class name) or
     * "" if it is not known. This is for error messages and so
     * on.
     */
    public String getDriverName();

    /**
     * Perform any initialization that requires connecting to the database or
     * starting threads and so on. This should not be done in the constructor.
     */
    public void init();

    /**
     * Free any resources held. None of the methods should be called after
     * this but the implementation is not required to throw exceptions if
     * this happens.
     */
    public void destroy();

    /**
     * Close any idle connections if this source pools connections. This is
     * a NOP if it does not.
     */
    public void closeIdleConnections();

}

