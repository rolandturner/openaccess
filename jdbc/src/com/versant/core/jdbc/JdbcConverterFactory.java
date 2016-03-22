
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

import com.versant.core.jdbc.metadata.JdbcColumn;

/**
 * This is a factory for JdbcConverters.
 * @see JdbcConverter
 * @keep-all
 */
public interface JdbcConverterFactory {

    /**
     * Create a javabean to hold args for a createJdbcConverter call or null
     * if the converter does not accept any arguments.
     */
    public Object createArgsBean();

    /**
     * Create a converter for col using args as parameters. Return null if
     * no converter is required.
     * @param col The column the converter is for
     * @param args The args bean or null if none
     * @param jdbcTypeRegistry The JDBC type registry for looking up factories
     *        for JDBC types (for nested converters)
     * @exception IllegalArgumentException if any params are invalid
     */
    public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
            JdbcTypeRegistry jdbcTypeRegistry) throws IllegalArgumentException;

}

