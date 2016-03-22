
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

import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Utils;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.MetaDataEnums;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcMetaDataEnums;

import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.sql.Connection;

/**
 * Parses JDBC specific info from Properties.
 */
public class JdbcConfigParser {

    private MetaDataEnums MDE = new MetaDataEnums();
    private JdbcMetaDataEnums jdbcMDE = new JdbcMetaDataEnums();
    public Map JDBC_CLASS_ID_ENUM = new HashMap();

    public JdbcConfigParser() {
        JDBC_CLASS_ID_ENUM.put(JdoExtension.HASH_VALUE,
                new Integer(JdbcConfig.DEFAULT_CLASS_ID_HASH));
        JDBC_CLASS_ID_ENUM.put(JdoExtension.NO_VALUE,
                new Integer(JdbcConfig.DEFAULT_CLASS_ID_NO));
        JDBC_CLASS_ID_ENUM.put(JdoExtension.NAME_VALUE,
                new Integer(JdbcConfig.DEFAULT_CLASS_ID_NAME));
        JDBC_CLASS_ID_ENUM.put(JdoExtension.FULLNAME_VALUE,
                new Integer(JdbcConfig.DEFAULT_CLASS_ID_FULLNAME));
    }

    public JdbcConfig parse(Properties p) {
        JdbcConfig jc = new JdbcConfig();
        jc.name = "main";

        /*The url can be null, if we are using a DataSource*/
        jc.url = p.getProperty(ConfigParser.STD_CON_URL);
        jc.db = p.getProperty(ConfigParser.STORE_DB);
        if (jc.db != null) {
            jc.db = jc.db.trim();
        }
        if (jc.db == null || jc.db.length() == 0) {
            jc.db = SqlDriver.getNameFromURL(jc.url);
            if (jc.db == null) {
                throw BindingSupportImpl.getInstance().runtime("Unable to guess " +
                        "database type from URL '" + jc.url + "', " +
                        "use the " + ConfigParser.STORE_DB +
                        " property to set the database type");
            }
        }
        jc.driver = p.getProperty(ConfigParser.STD_CON_DRIVER_NAME);
        if (jc.driver != null && jc.driver.length() == 0) {
            jc.driver = null;
        }
        if (jc.driver == null) {
            jc.driver = SqlDriver.getDriverFromURL(jc.url);
        }
        if (jc.driver == null 
                && Utils.isStringEmpty(p.getProperty(ConfigParser.STD_CON_FACTORY_NAME))) {
            jc.driver = ConfigParser.getReq(p, ConfigParser.STD_CON_DRIVER_NAME);
        }
        jc.user = p.getProperty(ConfigParser.STD_CON_USER_NAME);
        jc.password = p.getProperty(ConfigParser.STD_CON_PASSWORD);
        jc.properties = p.getProperty(ConfigParser.STORE_PROPERTIES);
        jc.conFactory = p.getProperty(ConfigParser.STD_CON_FACTORY_NAME);
        jc.enlistedConnections = ConfigParser.getBoolean(p,
                ConfigParser.VERSANT_DATASOURCE_ENLISTED, false);
        // connection 2 stuff
        jc.conFactory2 = p.getProperty(ConfigParser.STD_CON2_FACTORY_NAME);
        jc.driver2 = p.getProperty(ConfigParser.CON2_DRIVER_NAME);
        jc.properties2 = p.getProperty(ConfigParser.CON2_PROPERTIES);
        jc.url2 = p.getProperty(ConfigParser.CON2_URL);
        jc.user2 = p.getProperty(ConfigParser.CON2_USER_NAME);
        jc.password2 = p.getProperty(ConfigParser.CON2_PASSWORD);

        jc.maxActive = ConfigParser.getInt(p, ConfigParser.STORE_MAX_ACTIVE,
                ConfigParser.DEFAULT_STORE_MAX_ACTIVE);
        jc.maxIdle = ConfigParser.getInt(p, ConfigParser.STORE_MAX_IDLE, ConfigParser.DEFAULT_STORE_MAX_IDLE);
        jc.minIdle = ConfigParser.getInt(p, ConfigParser.STORE_MIN_IDLE, ConfigParser.DEFAULT_STORE_MIN_IDLE);
        jc.reserved = ConfigParser.getInt(p, ConfigParser.STORE_RESERVED, ConfigParser.DEFAULT_STORE_RESERVED);
        jc.conTimeout = ConfigParser.getInt(p, ConfigParser.STORE_CON_TIMEOUT, 120);
        jc.testInterval = ConfigParser.getInt(p, ConfigParser.STORE_TEST_INTERVAL, 120);
        jc.waitForConOnStartup = ConfigParser.getBoolean(p,
                ConfigParser.STORE_WAIT_FOR_CON_ON_STARTUP,
                ConfigParser.DEFAULT_STORE_WAIT_FOR_CON_ON_STARTUP);
        jc.testOnAlloc = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_ON_ALLOC,
                ConfigParser.DEFAULT_STORE_TEST_ON_ALLOC);
        jc.testOnRelease = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_ON_RELEASE,
                ConfigParser.DEFAULT_STORE_TEST_ON_RELEASE);
        jc.testOnException = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_ON_EXCEPTION,
                ConfigParser.DEFAULT_STORE_TEST_ON_EXCEPTION);
        jc.testWhenIdle = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_WHEN_IDLE,
                true);
        jc.retryIntervalMs = ConfigParser.getInt(p,
                ConfigParser.STORE_RETRY_INTERVAL_MS,
                ConfigParser.DEFAULT_STORE_RETRY_INTERVAL_MS);
        jc.retryCount = ConfigParser.getInt(p,
                ConfigParser.STORE_RETRY_COUNT,
                ConfigParser.DEFAULT_STORE_RETRY_COUNT);
        jc.validateMappingOnStartup = ConfigParser.getBoolean(p,
                ConfigParser.STORE_VALIDATE_MAPPING_ON_STARTUP, false);
        jc.validateSQL = ConfigParser.trim(p.getProperty(
                ConfigParser.STORE_VALIDATE_SQL));
        jc.initSQL = ConfigParser.trim(p.getProperty(
                ConfigParser.STORE_INIT_SQL));
        jc.maxConAge = ConfigParser.getInt(p, ConfigParser.STORE_MAX_CON_AGE,
                ConfigParser.DEFAULT_MAX_CON_AGE);

        jc.blockWhenFull =
                ConfigParser.getBoolean(p, ConfigParser.STORE_BLOCK_WHEN_FULL, true);

        jc.jdbcDisableStatementBatching =
                ConfigParser.getBoolean(p, ConfigParser.STORE_DISABLE_BATCHING, false);
        jc.jdbcDisablePsCache =
                ConfigParser.getBoolean(p, ConfigParser.STORE_DISABLE_PS_CACHE, false);
        jc.psCacheMax = ConfigParser.getInt(p, ConfigParser.STORE_PS_CACHE_MAX, 0);

        String s = p.getProperty(ConfigParser.STORE_ISOLATION_LEVEL);
        if (s == null) {
            jc.isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
        } else {
            if (s.equals(ConfigParser.ISOLATION_LEVEL_READ_COMMITTED)) {
                jc.isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
            } else if (s.equals(ConfigParser.ISOLATION_LEVEL_READ_UNCOMMITTED)) {
                jc.isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
            } else if (s.equals(ConfigParser.ISOLATION_LEVEL_REPEATABLE_READ)) {
                jc.isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
            } else if (s.equals(ConfigParser.ISOLATION_LEVEL_SERIALIZABLE)) {
                jc.isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
            } else {
                throw BindingSupportImpl.getInstance().runtime("Invalid '" +
                        ConfigParser.STORE_ISOLATION_LEVEL + "', expected " +
                        ConfigParser.ISOLATION_LEVEL_READ_COMMITTED + ", " +
                        ConfigParser.ISOLATION_LEVEL_REPEATABLE_READ + ", " +
                        ConfigParser.ISOLATION_LEVEL_SERIALIZABLE + " or " +
                        ConfigParser.ISOLATION_LEVEL_READ_UNCOMMITTED);
            }
        }

        jc.jdbcNameGenerator = ConfigParser.getClassAndProps(p,
                ConfigParser.STORE_NAMEGEN,
                jc.jdbcNameGeneratorProps = new HashMap());
        ConfigParser.getClassAndProps(p, ConfigParser.STORE_MIGRATION_CONTROLS,
                jc.jdbcMigrationControlProps = new HashMap());

        String be = ConfigParser.STORE_EXT;
        jc.jdbcOptimisticLocking = ConfigParser.getExtEnum(p, be,
                JdoExtensionKeys.JDBC_OPTIMISTIC_LOCKING,
                jdbcMDE.OPTIMISTIC_LOCKING_ENUM,
                JdbcClass.OPTIMISTIC_LOCKING_VERSION);
        jc.readOnly = ConfigParser.getExtBoolean(p, be,
                JdoExtensionKeys.READ_ONLY, false);
        jc.cacheStrategy = ConfigParser.getExtEnum(p, be,
                JdoExtensionKeys.CACHE_STRATEGY,
                MDE.CACHE_ENUM,
                MDStatics.CACHE_STRATEGY_YES);
        jc.inheritance = ConfigParser.getExtEnum(p, be,
                JdoExtensionKeys.JDBC_INHERITANCE,
                jdbcMDE.INHERITANCE_ENUM,
                JdbcClass.INHERITANCE_FLAT);
        s = p.getProperty(be + ConfigParser.JDBC_INHERITANCE_NO_CLASSID);
        if ("true".equals(s)) {
            jc.defaultClassId = JdbcConfig.DEFAULT_CLASS_ID_NO;
        } else {
            jc.defaultClassId = ConfigParser.getExtEnum(p, be,
                    JdoExtensionKeys.JDBC_CLASS_ID,
                    JDBC_CLASS_ID_ENUM,
                    JdbcConfig.DEFAULT_CLASS_ID_HASH);
        }
        jc.jdbcDoNotCreateTable = ConfigParser.getExtBoolean(p, be,
                JdoExtensionKeys.JDBC_DO_NOT_CREATE_TABLE, false);
        jc.oidsInDefaultFetchGroup = ConfigParser.getExtBoolean(p, be,
                JdoExtensionKeys.OIDS_IN_DEFAULT_FETCH_GROUP, true);

        jc.managedOneToMany = ConfigParser.getBoolean(p,
                ConfigParser.STORE_MANAGED_ONE_TO_MANY,
                ConfigParser.DEFAULT_STORE_MANAGED_ONE_TO_MANY);
        jc.managedManyToMany = ConfigParser.getBoolean(p,
                ConfigParser.STORE_MANAGED_MANY_TO_MANY,
                ConfigParser.DEFAULT_STORE_MANAGED_MANY_TO_MANY);

        jc.jdbcKeyGenerator = ConfigParser.getClassAndProps(p, be + JdoExtension.
                toKeyString(JdoExtensionKeys.JDBC_KEY_GENERATOR),
                jc.jdbcKeyGeneratorProps = new HashMap());

        jc.typeMappings = readTypeMappings(p);
        jc.javaTypeMappings = readJavaTypeMappings(p);

        return jc;
    }

    private ArrayList readTypeMappings(Properties p) {
        int n = ConfigParser.MAX_STORE_TYPE_MAPPING_COUNT;
        String s = null;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            s = p.getProperty(ConfigParser.STORE_TYPE_MAPPING + i);
            if (s != null) {
                a.add(ConfigParser.getReq(p, ConfigParser.STORE_TYPE_MAPPING + i));
            }
        }
        return a;
    }

    private ArrayList readJavaTypeMappings(Properties p) {
        int n = ConfigParser.MAX_STORE_JAVATYPE_MAPPING_COUNT;
        String s = null;
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            s = p.getProperty(ConfigParser.STORE_JAVATYPE_MAPPING + i);
            if (s != null) {
                a.add(ConfigParser.getReq(p, ConfigParser.STORE_JAVATYPE_MAPPING + i));
            }
        }
        return a;
    }

}

