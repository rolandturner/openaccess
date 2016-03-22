
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a JDBC store.
 */
public class JdbcConfig {

    public String name;
    public String db;
    public String poolname;
    public String url;
    public String driver;
    public String user;
    public String password;
    public String properties;
    public String conFactory;   // JNDI lookup name1

    // connection 2 stuff
    public String driver2;
    public String url2;
    public String user2;
    public String password2;
    public String properties2;
    public String conFactory2;   // JNDI lookup name2

    public int maxActive;
    public int maxIdle;
    public int minIdle;
    public int reserved;

    public int jdbcOptimisticLocking;
    public boolean readOnly;
    public int cacheStrategy;
    public int inheritance;
    public int defaultClassId;
    public boolean jdbcDoNotCreateTable;
    public boolean oidsInDefaultFetchGroup;
    public boolean jdbcDisableStatementBatching;
    public boolean jdbcDisablePsCache;
    public boolean validateMappingOnStartup;
    public boolean managedOneToMany;
    public boolean managedManyToMany;

    public int isolationLevel;
    public String initSQL;
    public String validateSQL;
    public boolean waitForConOnStartup;
    public boolean testOnAlloc;
    public boolean testOnRelease;
    public boolean testOnException = true;
    public boolean testWhenIdle = true;
    public boolean enlistedConnections;
    public int retryIntervalMs = 100;
    public int retryCount;
    public int psCacheMax;
    public int conTimeout;
    public int testInterval;
    public boolean blockWhenFull;
    public int maxConAge;

    public String jdbcKeyGenerator;
    public Map jdbcKeyGeneratorProps;

    public String jdbcNameGenerator;
    public Map jdbcNameGeneratorProps;
    public Map jdbcMigrationControlProps;

    public ArrayList typeMappings;
    public ArrayList javaTypeMappings;

    /**
     * Use hash of the fully qualified name of the class as descriminator.
     */
    public static final int DEFAULT_CLASS_ID_HASH = 1;
    /**
     * Dot use a descriminator column in heirachies.
     */
    public static final int DEFAULT_CLASS_ID_NO = 2;
    /**
     * Use the name of the class without package as descriminator.
     */
    public static final int DEFAULT_CLASS_ID_NAME = 3;
    /**
     * Use the fully qualified name of the class as descriminator.
     */
    public static final int DEFAULT_CLASS_ID_FULLNAME = 4;
}

