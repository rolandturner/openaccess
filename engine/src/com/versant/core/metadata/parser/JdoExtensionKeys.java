
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
package com.versant.core.metadata.parser;

/**
 * <p>These are all the possible keys allowed for vendor extensions. The
 * text for each key is the constant name converted to lowercase with
 * underscores changed to hyphens. MetaDataParser uses reflection to
 * automatically derive the valid keys from this interface.</p>
 * <p/>
 * <p>The constant values are used to classify extensions. Constants 100 to 199
 * are jdbc extensions.</p>
 *
 * @see JdoExtension
 * @see JdoExtension#isJdbc
 * @see MetaDataParser
 */
public interface JdoExtensionKeys {

    // some values are copied into ConfigBuilder.cs - change at both locations!
    public static final int DATASTORE = 1;
    public static final int ORDERED = 2;
    public static final int READ_ONLY = 3;
    public static final int PROPERTY = 4;
    public static final int FIELD_NAME = 5;
    public static final int FETCH_GROUP = 6;
    public static final int NEXT_FETCH_GROUP = 7;
    public static final int NEXT_KEY_FETCH_GROUP = 8;
    public static final int DEPENDENT = 9;
    public static final int KEYS_DEPENDENT = 10;
    public static final int AUTOSET = 11;
    public static final int CACHE_STRATEGY = 12;
    public static final int OIDS_IN_DEFAULT_FETCH_GROUP = 13;
    public static final int CREATE_OID_AT_MAKE_PERSISTENT = 14;
    public static final int ORDERING = 15;
    public static final int DELETE_ORPHANS = 16;
    public static final int NULL_IF_NOT_FOUND = 17;
    public static final int VALID_CLASS = 18;
    public static final int INTERFACE = 19;
    public static final int PERSIST_AFTER = 20;
    public static final int CLASS = 21;
    public static final int MANAGED = 22;
    public static final int RANDOM_ACCESS = 23;
    public static final int COUNT_STAR_ON_SIZE = 24;
    public static final int MAX_ROWS = 25;
    public static final int FETCH_SIZE = 26;
    public static final int BOUNDED = 27;
    public static final int EVICTION_CLASSES = 28;
    public static final int OPTIMISTIC = 29;
    public static final int QUERY_PARAM_VALUES = 30;
    public static final int VALUE = 31;
    public static final int SCO_FACTORY = 32;
    public static final int EXTERNALIZER = 33;

    public static final int ALIAS = 34;
    public static final int CACHEABLE = 35;

    public static final int NULL_INDICATOR = 36;
    public static final int FIELD = 37;
    public static final int NULL_VALUE = 38;
    public static final int DEFAULT_FETCH_GROUP = 39;
    public static final int EMBEDDED = 40;
    public static final int COLLECTION = 41;
    public static final int MAP = 42;
    public static final int ARRAY = 43;
    public static final int KEY_TYPE = 44;
    public static final int VALUE_TYPE = 45;
    public static final int ELEMENT_TYPE = 46;
    public static final int EMBEDDED_ONLY = 47;

    public static final int JDBC_COLUMN = 104;
    public static final int JDBC_COLUMN_NAME = 105;
    public static final int JDBC_CONSTRAINT = 106;
    public static final int JDBC_PRIMARY_KEY = 108;
    public static final int JDBC_REF = 110;
    public static final int JDBC_INDEX = 111;
    public static final int JDBC_CONVERTER = 112;
    public static final int JDBC_JAVA_TYPE = 113;
    public static final int JDBC_KEY_GENERATOR = 115;
    public static final int JDBC_LENGTH = 117;
    public static final int JDBC_LINK_FOREIGN_KEY = 118;
    public static final int JDBC_LINK_TABLE = 119;
    public static final int JDBC_OPTIMISTIC_LOCKING = 120;
    public static final int JDBC_TABLE_NAME = 126;
    public static final int JDBC_TYPE = 127;
    public static final int JDBC_USE_JOIN = 128;
    public static final int JDBC_USE_SUBCLASS_JOIN = 129;
    public static final int JDBC_UNIQUE = 133;
    public static final int JDBC_CLUSTERED = 134;
    public static final int JDBC_CLASS_ID = 135;
    public static final int JDBC_SCALE = 136;
    public static final int JDBC_NULLS = 137;
    public static final int JDBC_USE_KEY_JOIN = 138;
    public static final int JDBC_INHERITANCE = 139;
    public static final int JDBC_PK_FK_CONSTRAINT_NAME = 140;
    public static final int JDBC_DO_NOT_CREATE_TABLE = 141;
    public static final int JDBC_SQL_TYPE = 142;
    public static final int JDBC_FIELD_NAME = 143;
    public static final int JDBC_DATABASE = 144;
    public static final int JDBC_OWNER_REF = 145;
    public static final int JDBC_SEQUENCE = 146;
    public static final int JDBC_KEY = 147;
    public static final int JDBC_VALUE = 148;
    public static final int INVERSE = 149;
    public static final int JDBC_SHARED = 150;
}
