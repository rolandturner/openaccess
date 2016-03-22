
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
package com.versant.core.jdbc.metadata;

import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcRefField;
import com.versant.core.metadata.MDStatics;

import java.util.Map;
import java.util.HashMap;

/**
 * This contains maps that convert Strings to int codes for JDBC meta data
 * options. They are not static so this class can be completly discarded
 * when the meta data has been read.
 */
public class JdbcMetaDataEnums {

    public static final String OPTIMISTIC_LOCKING_NONE = "none";
    public static final String OPTIMISTIC_LOCKING_VERSION = "version";
    public static final String OPTIMISTIC_LOCKING_TIMESTAMP = "timestamp";
    public static final String OPTIMISTIC_LOCKING_CHANGED = "changed";

    public final Map USE_JOIN_ENUM;
    public final Map OPTIMISTIC_LOCKING_ENUM;
    public final Map INHERITANCE_ENUM;

    public static final String INHERITANCE_FLAT = "flat";
    public static final String INHERITANCE_VERTICAL = "vertical";
    public static final String INHERITANCE_HORIZONTAL = "horizontal";

    public JdbcMetaDataEnums() {
        USE_JOIN_ENUM = new HashMap();
        USE_JOIN_ENUM.put("no", new Integer(JdbcRefField.USE_JOIN_NO));
        USE_JOIN_ENUM.put("inner", new Integer(JdbcRefField.USE_JOIN_INNER));
        USE_JOIN_ENUM.put("outer", new Integer(JdbcRefField.USE_JOIN_OUTER));

        OPTIMISTIC_LOCKING_ENUM = new HashMap();
        OPTIMISTIC_LOCKING_ENUM.put(OPTIMISTIC_LOCKING_NONE, new Integer(JdbcClass.OPTIMISTIC_LOCKING_NONE));
        OPTIMISTIC_LOCKING_ENUM.put(OPTIMISTIC_LOCKING_VERSION, new Integer(JdbcClass.OPTIMISTIC_LOCKING_VERSION));
        OPTIMISTIC_LOCKING_ENUM.put(OPTIMISTIC_LOCKING_TIMESTAMP, new Integer(JdbcClass.OPTIMISTIC_LOCKING_TIMESTAMP));
        OPTIMISTIC_LOCKING_ENUM.put(OPTIMISTIC_LOCKING_CHANGED, new Integer(JdbcClass.OPTIMISTIC_LOCKING_CHANGED));

        INHERITANCE_ENUM = new HashMap();
        INHERITANCE_ENUM.put(INHERITANCE_FLAT, new Integer(JdbcClass.INHERITANCE_FLAT));
        INHERITANCE_ENUM.put(INHERITANCE_VERTICAL, new Integer(JdbcClass.INHERITANCE_VERTICAL));
        INHERITANCE_ENUM.put(INHERITANCE_HORIZONTAL, new Integer(JdbcClass.INHERITANCE_HORIZONTAL));
    }
}
