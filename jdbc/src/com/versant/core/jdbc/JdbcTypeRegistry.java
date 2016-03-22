
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

/**
 * This supplies information about JDBC types (e.g. the JdbcConverterFactory
 * for a JDBC type).
 * @keep-all
 * @see JdbcConverterFactory
 */
public interface JdbcTypeRegistry {

    /**
     * Get the converter factory used for the supplied JDBC type or null
     * if none.
     * @param jdbcType JDBC type code from java.sql.Types
     * @see java.sql.Types
     * @exception IllegalArgumentException if jdbcType is invalid or there
     *            is no converter
     */
    public JdbcConverterFactory getJdbcConverterFactory(int jdbcType)
            throws IllegalArgumentException;

}

 
