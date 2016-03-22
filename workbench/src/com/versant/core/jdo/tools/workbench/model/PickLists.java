
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
package com.versant.core.jdo.tools.workbench.model;

import com.versant.core.common.config.ConfigParser;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.MetaDataEnums;
import com.versant.core.logging.LogEventStore;
import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.JdbcConverterFactoryRegistry;
import com.versant.core.jdbc.metadata.JdbcMetaDataEnums;
import com.versant.core.jdo.externalizer.SerializedExternalizer;
import com.versant.core.jdo.externalizer.TypeAsBytesExternalizer;
import com.versant.core.jdo.externalizer.TypeAsStringExternalizer;

import java.util.*;

/**
 * @keep-all Pick lists for various things in the model.
 */
public class PickLists {

    private PickLists() {
    }

    public static final List EMPTY = Collections.EMPTY_LIST;

    public static final List BOOLEAN = Arrays.asList(new String[]{
        "false", "true"
    });

    public static final List FALSE = Arrays.asList(new String[]{
        "false"
    });

    public static final List TRUE = Arrays.asList(new String[]{
        "true"
    });

    public static final List CACHE_STRATEGY = Arrays.asList(new String[]{
        "no", "yes", "all"
    });

    public static final List JDBC_OPTIMISTIC_LOCKING = Arrays.asList(new String[]{
        MetaDataEnums.OPTIMISTIC_LOCKING_NONE, MetaDataEnums.OPTIMISTIC_LOCKING_VERSION,
        MetaDataEnums.OPTIMISTIC_LOCKING_TIMESTAMP, MetaDataEnums.OPTIMISTIC_LOCKING_CHANGED
    });

    public static final List JDBC_KEY_GENERATOR = Arrays.asList(new String[]{
        JdbcMetaDataBuilder.KEYGEN_HIGHLOW,
        JdbcMetaDataBuilder.KEYGEN_AUTOINC,
    });

    public static final List IDENTITY_TYPE = Arrays.asList(new String[]{
        "datastore", "application"
    });

    public static final List PERSISTENCE_MODIFIER = Arrays.asList(new String[]{
        "none", "persistent", "transactional",
    });

    public static final List NULL_VALUE = Arrays.asList(new String[]{
        "default", "exception",
    });

    public static final List JDBC_USE_JOIN = Arrays.asList(new String[]{
        "no", "outer", "inner",
    });

    public static final List JDBC_TYPE = Arrays.asList(new String[]{
        "ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "CHAR", "CLOB",
        "DATE", "DECIMAL", "DOUBLE", "FLOAT", "INTEGER", "LONGVARBINARY",
        "LONGVARCHAR", "NUMERIC", "REAL", "SMALLINT", "STRUCT", "TIME",
        "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR"
    });

    public static final List AUTOSET = Arrays.asList(new String[]{
        "no", "created", "modified", "both"
    });

    public static final List NO = Arrays.asList(new String[]{
        JdoExtension.NO_VALUE,
    });

    public static final List DB = Arrays.asList(new String[]{
        "cache", "db2", "firebird", "hypersonic", "informix", "informixse",
        "interbase", "mssql", "mysql", "oracle", "pointbase", "postgres",
        "sapdb", "sybase", "versant"
    });

    public static final List TRI_STATE = Arrays.asList(new String[]{
        "not set", "false", "true"
    });

    public static final List JDBC_CONVERTER = Arrays.asList(new String[]{
        JdbcConverterFactoryRegistry.NULL_CONVERTER_NAME,
        JdbcConverterFactoryRegistry.STRING_CONVERTER_NAME,
        JdbcConverterFactoryRegistry.BYTES_CONVERTER_NAME,
    });

    public static final List LOG_EVENTS = Arrays.asList(new String[]{
        LogEventStore.LOG_EVENTS_NONE,
        LogEventStore.LOG_EVENTS_ERRORS,
        LogEventStore.LOG_EVENTS_NORMAL,
        LogEventStore.LOG_EVENTS_VERBOSE,
        LogEventStore.LOG_EVENTS_ALL,
    });

    public static final List DATASTORE_TX_LOCKING = Arrays.asList(new String[]{
        ConfigParser.DATASTORE_TX_LOCKING_NONE,
        ConfigParser.DATASTORE_TX_LOCKING_FIRST,
        ConfigParser.DATASTORE_TX_LOCKING_ALL,
    });

    public static final List ISOLATION_LEVEL = Arrays.asList(new String[]{
        ConfigParser.ISOLATION_LEVEL_READ_COMMITTED,
        ConfigParser.ISOLATION_LEVEL_REPEATABLE_READ,
        ConfigParser.ISOLATION_LEVEL_SERIALIZABLE,
    });

    public static final List JDBC_INHERITANCE = Arrays.asList(new String[]{
        JdbcMetaDataEnums.INHERITANCE_FLAT,
        JdbcMetaDataEnums.INHERITANCE_VERTICAL,
    });

    public static final List JDBC_CLASS_ID = Arrays.asList(new String[]{
        JdoExtension.HASH_VALUE,
        JdoExtension.NAME_VALUE,
        JdoExtension.FULLNAME_VALUE,
        JdoExtension.NO_VALUE,
    });

    public static final List CLUSTER_TRANSPORT = Arrays.asList(new String[]{
        ConfigParser.CLUSTER_JGROUPS,
        ConfigParser.CLUSTER_TANGOSOL_COHERENCE,
    });

    public static final List PM_CACHE_REF_TYPE = Arrays.asList(new String[]{
        ConfigParser.PM_CACHE_REF_TYPE_SOFT,
        ConfigParser.PM_CACHE_REF_TYPE_WEAK,
        ConfigParser.PM_CACHE_REF_TYPE_STRONG,
    });

    public static final List EXTERNALIZER = Arrays.asList(new String[]{
        SerializedExternalizer.SHORT_NAME,
        TypeAsBytesExternalizer.SHORT_NAME,
        TypeAsStringExternalizer.SHORT_NAME,
    });

    public static final List REMOTE_ACCESS = Arrays.asList(new String[]{
        "false",
        "socket",
    });

}
