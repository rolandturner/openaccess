
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

import com.versant.core.jdbc.metadata.JdbcTable;

/**
 * These generate JdbcKeyGenerator instances for persistent classes.
 */
public interface JdbcKeyGeneratorFactory {

    /**
     * Create a javabean to hold args for a createJdbcKeyGenerator call or null
     * if the key generator does not accept any arguments.
     */
    public Object createArgsBean();

    /**
     * Create a JdbcKeyGenerator for class using args as parameters. The
     * instance returned may be new or may be a shared instance.
     *
     * @throws IllegalArgumentException if anything is invalid
     */
    public JdbcKeyGenerator createJdbcKeyGenerator(String className,
            JdbcTable classTable, Object args)
            throws IllegalArgumentException;

}
