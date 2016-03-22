
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
package com.versant.core.jdbc.sql.conv;

import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.JdbcConverter;
import com.versant.core.jdbc.JdbcTypeRegistry;
import com.versant.core.jdbc.metadata.JdbcColumn;

/** 
 * Base class for JdbcConverterFactory's that do not use an args bean.
 */
public abstract class NoArgJdbcConverterFactory implements JdbcConverterFactory {

    /**
     * Create a javabean to hold args for a createJdbcConverter call or null
     * if the converter does not accept any arguments.
     */
    public Object createArgsBean() {
        return null;  //To change body of implemented methods use Options | File Templates.
    }

    /**
     * Create a converter for col using args as parameters. Return null if
     * no converter is required.
     * @param col The column the converter is for
     * @param args The args bean or null if none
     * @param jdbcTypeRegistry The JDBC type registry for looking up factories
     *        for JDBC types (for nested converters)
     * @exception IllegalArgumentException if any params are invalid
     */
    public abstract JdbcConverter createJdbcConverter(JdbcColumn col,
            Object args, JdbcTypeRegistry jdbcTypeRegistry)
            throws IllegalArgumentException;

}

