
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
package com.versant.core.metadata;

/**
 * Constants used in the meta data.
 */
public interface MDStatics {

	public static final int CASCADE_PERSIST = 1;
    public static final int CASCADE_MERGE = 2;
    public static final int CASCADE_REMOVE = 4;
    public static final int CASCADE_REFRESH = 8;
    public static final int CASCADE_ALL = CASCADE_PERSIST + CASCADE_MERGE + CASCADE_REMOVE + CASCADE_REFRESH;

    /**
     * Boolean value not set in a .jdo file.
     */
    public static final int NOT_SET = 0;
    /**
     * Boolean value set as false in a .jdo file.
     */
    public static final int FALSE = 1;
    /**
     * Boolean value set as true in a .jdo file.
     */
    public static final int TRUE = 2;

    public static final int IDENTITY_TYPE_APPLICATION = 1;
    public static final int IDENTITY_TYPE_DATASTORE = 2;
    public static final int IDENTITY_TYPE_NONDURABLE = 3;

    public static final int PERSISTENCE_MODIFIER_PERSISTENT = 1;
    public static final int PERSISTENCE_MODIFIER_TRANSACTIONAL = 2;
    public static final int PERSISTENCE_MODIFIER_NONE = 3;

    public static final int NULL_VALUE_EXCEPTION = 1;
    public static final int NULL_VALUE_DEFAULT = 2;
    public static final int NULL_VALUE_NONE = 3;

    /**
     * A datastore identity primary key 'field' (used by workbench).
     */
    public static final int CATEGORY_DATASTORE_PK = 1;
    /**
     * A class-id 'field' (used by workbench).
     */
    public static final int CATEGORY_CLASS_ID = 2;
    /**
     * A version or timestamp 'field' (used by workbench).
     */
    public static final int CATEGORY_OPT_LOCKING = 3;

    /**
     * A normal field (e.g. int, String etc).
     */
    public static final int CATEGORY_SIMPLE = 4;
    /**
     * A reference to a PC class.
     */
    public static final int CATEGORY_REF = 5;
    /**
     * A reference to any PC class.
     */
    public static final int CATEGORY_POLYREF = 6;
    /**
     * A Collection.
     */
    public static final int CATEGORY_COLLECTION = 7;
    /**
     * An array.
     */
    public static final int CATEGORY_ARRAY = 8;
    /**
     * A Map.
     */
    public static final int CATEGORY_MAP = 9;
    /**
     * A transactional non-persistent field.
     */
    public static final int CATEGORY_TRANSACTIONAL = 10;
    /**
     * A non-persistent field.
     */
    public static final int CATEGORY_NONE = 11;
    /**
     * An externalized field
     */
    public static final int CATEGORY_EXTERNALIZED = 12;

    public static final int AUTOSET_NO = 0;
    public static final int AUTOSET_CREATED = 1;
    public static final int AUTOSET_MODIFIED = 2;
    public static final int AUTOSET_BOTH = 3;

    /**
     * Do not cache instances of this class.
     */
    public static final int CACHE_STRATEGY_NO = 1;
    /**
     * Cache instances of this class.
     */
    public static final int CACHE_STRATEGY_YES = 2;
    /**
     * Cache all instances of this class as soon as an instance is requested.
     * All rows from its table will be read and cached whenever an instance
     * is requested but not found in cache. This may work well for small
     * static tables when using a big cache.
     */
    public static final int CACHE_STRATEGY_ALL = 3;

    // type codes for all the simple JDO types

    public static final int BOOLEAN = 1;
    public static final int BYTE = 2;
    public static final int SHORT = 3;
    public static final int INT = 4;
    public static final int LONG = 5;
    public static final int FLOAT = 6;
    public static final int DOUBLE = 7;
    public static final int CHAR = 8;

    // W => Wrapper
    public static final int BOOLEANW = 9;
    public static final int BYTEW = 10;
    public static final int SHORTW = 11;
    public static final int INTW = 12;
    public static final int LONGW = 13;
    public static final int FLOATW = 14;
    public static final int DOUBLEW = 15;
    public static final int CHARW = 16;

    public static final int STRING = 17;
    public static final int BIGDECIMAL = 18;
    public static final int BIGINTEGER = 19;
    public static final int DATE = 20;
    public static final int LOCALE = 21;

    public static final int LIST = 22;
    public static final int ARRAYLIST = 23;
    public static final int LINKEDLIST = 24;
    public static final int VECTOR = 25;

    public static final int SET = 26;
    public static final int HASHSET = 27;
    public static final int TREESET = 28;
    public static final int SORTEDSET = 50;

    public static final int MAP = 29;
    public static final int HASHMAP = 30;
    public static final int TREEMAP = 31;
    public static final int SORTEDMAP = 51;
    public static final int HASHTABLE = 32;

    public static final int COLLECTION = 33;

    public static final int NULL = 33;

    public static final int OID = 34;


    public static final String GEN_START = "HYPERDRIVE_";
    // all generated class names must start with this string
    public static final String GEN_OID_START = "HYPERDRIVE_OID_";
    public static final String GEN_STATE_START = "HYPERDRIVE_STATE_";
    public static final String OID_STRING_SEPERATOR = "-";
    public static final char OID_CHAR_SEPERATOR = '-';

    public static final String STATE_METHOD_STRING = "getStringField";
    public static final String STATE_METHOD_OBJECT = "getInternalObjectField";
    public static final String STATE_METHOD_BOOLEAN = "getBooleanField";
    public static final String STATE_METHOD_BYTE = "getByteField";
    public static final String STATE_METHOD_SHORT = "getShortField";
    public static final String STATE_METHOD_INT = "getIntField";
    public static final String STATE_METHOD_LONG = "getLongField";
    public static final String STATE_METHOD_FLOAT = "getFloatField";
    public static final String STATE_METHOD_DOUBLE = "getDoubleField";
    public static final String STATE_METHOD_CHAR = "getCharField";

    public static final String EJB_JDBC_PRE_PROCESSOR = 
        "com.versant.core.ejb.metadata.EJBAnnotationProcessor";

}
