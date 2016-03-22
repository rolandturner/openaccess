
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
package com.versant.core.vds;

import com.versant.core.common.config.ConfigParser;

import java.util.Properties;
import java.util.HashMap;

/**
 * Parsers VDS spefic stuff from Properties.
 */
public class VdsConfigParser {

    public VdsConfigParser() {
    }

    public VdsConfig parse(Properties p) {
        VdsConfig vc = new VdsConfig();
        vc.name = "main";
        vc.url = ConfigParser.getReq(p, ConfigParser.STD_CON_URL, null);
        vc.user = p.getProperty(ConfigParser.STD_CON_USER_NAME, VdsConfig.DEFAULT_USER);
        vc.password = p.getProperty(ConfigParser.STD_CON_PASSWORD,
                VdsConfig.DEFAULT_PASSWORD);
        vc.properties = p.getProperty(ConfigParser.STORE_PROPERTIES);
        vc.maxActive = ConfigParser.getInt(p, ConfigParser.STORE_MAX_ACTIVE,
                ConfigParser.DEFAULT_STORE_MAX_ACTIVE);
        vc.maxIdle = ConfigParser.getInt(p, ConfigParser.STORE_MAX_IDLE, ConfigParser.DEFAULT_STORE_MAX_IDLE);
        vc.minIdle = ConfigParser.getInt(p, ConfigParser.STORE_MIN_IDLE, ConfigParser.DEFAULT_STORE_MIN_IDLE);
        vc.reserved = ConfigParser.getInt(p, ConfigParser.STORE_RESERVED, ConfigParser.DEFAULT_STORE_RESERVED);
        vc.conTimeout = ConfigParser.getInt(p, ConfigParser.STORE_CON_TIMEOUT,
                VdsConfig.DEFAULT_CONNECTION_TIMEOUT);
        vc.testInterval = ConfigParser.getInt(p, ConfigParser.STORE_TEST_INTERVAL, 120);
        vc.waitForConOnStartup = ConfigParser.getBoolean(p,
                ConfigParser.STORE_WAIT_FOR_CON_ON_STARTUP,
                ConfigParser.DEFAULT_STORE_WAIT_FOR_CON_ON_STARTUP);
        vc.testOnAlloc = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_ON_ALLOC,
                ConfigParser.DEFAULT_STORE_TEST_ON_ALLOC);
        vc.testOnRelease = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_ON_RELEASE,
                ConfigParser.DEFAULT_STORE_TEST_ON_RELEASE);
        vc.testOnException = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_ON_EXCEPTION,
                ConfigParser.DEFAULT_STORE_TEST_ON_EXCEPTION);
        vc.testWhenIdle = ConfigParser.getBoolean(p,
                ConfigParser.STORE_TEST_WHEN_IDLE, true);
        vc.retryIntervalMs = ConfigParser.getInt(p,
                ConfigParser.STORE_RETRY_INTERVAL_MS,
                ConfigParser.DEFAULT_STORE_RETRY_INTERVAL_MS);
        vc.retryCount = ConfigParser.getInt(p,
                ConfigParser.STORE_RETRY_COUNT,
                ConfigParser.DEFAULT_STORE_RETRY_COUNT);
        vc.oidBatchSize = ConfigParser.getInt(p, ConfigParser.VDS_OID_BATCH_SIZE,
                VdsConfig.DEFAULT_OID_BATCH_SIZE);
        vc.isDynamicSchemaDefinition = ConfigParser.getBoolean(p,
                ConfigParser.VDS_SCHEMA_DEFINITION,
                VdsConfig.DEFAULT_SCHEMA_DEFINITION);
        vc.isDynamicSchemaEvolution = ConfigParser.getBoolean(p, ConfigParser.VDS_SCHEMA_EVOLUTION,
                VdsConfig.DEFAULT_SCHEMA_EVOLUTION);
        vc.validateMappingOnStartup = ConfigParser.getBoolean(p,
                ConfigParser.STORE_VALIDATE_MAPPING_ON_STARTUP, true);
        vc.maxConAge = ConfigParser.getInt(p, ConfigParser.STORE_MAX_CON_AGE, ConfigParser.DEFAULT_MAX_CON_AGE);
        vc.blockWhenFull = ConfigParser.getBoolean(p, ConfigParser.STORE_BLOCK_WHEN_FULL,
                VdsConfig.DEFAULT_BLOCK_ON_EMPTY_POOL);
        vc.namingPolicy = ConfigParser.getClassAndProps(p, ConfigParser.VDS_NAMING_POLICY,
                vc.namingPolicyProps = new HashMap());
        vc.validate();
        return vc;
    }

}

