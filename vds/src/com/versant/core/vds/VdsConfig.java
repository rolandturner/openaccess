
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

import java.util.*;

/**
 * Configuration parameters for a VDS datastore.
 * All members of this class is public to keep up with open access
 * spirit of Genie.
 * <p/>
 * None of the variables are initialized to any default value.
 * Initialization of these values are carried out in
 * {@link com.versant.core.common.config.ConfigParser#parse parse}
 * method of <code>ConfigParser</code>
 * <p/>
 * However, default values are specified statically here.
 */
public class VdsConfig {

    public static final String DEFAULT_USER = System.getProperty("user.name");
    public static final String DEFAULT_PASSWORD = null;
    public static final int DEFAULT_OID_BATCH_SIZE = 1024;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 120;
    public static final int DEFAULT_MAX_CONNECTION_AGE = 10;
    public static final boolean DEFAULT_BLOCK_ON_EMPTY_POOL = true;
    public static final boolean DEFAULT_SCHEMA_DEFINITION = true;
    public static final boolean DEFAULT_SCHEMA_EVOLUTION = false;

    public String name;
    public String url;
    public String user;
    public String password;
    public String properties;

    public int maxActive;
    public int maxIdle;
    public int minIdle;
    public int reserved;
    public int oidBatchSize;
    public boolean waitForConOnStartup;
    public boolean testOnAlloc;
    public boolean testOnRelease;
    public boolean testOnException;
    public boolean testWhenIdle;
    public int retryIntervalMs;
    public int retryCount;
    public int conTimeout;
    public int testInterval;
    public boolean blockWhenFull;
    public int maxConAge;
    public boolean validateMappingOnStartup;
    public boolean isDynamicSchemaDefinition;
    public boolean isDynamicSchemaEvolution;
    public String namingPolicy;
    public Map namingPolicyProps;

    /**
     * Validates the field values. Warns and sets some meaningful
     * default if wrong values are specified.
     */
    public void validate() {
        maxActive = rangeCheck("MaxActive", maxActive, 1000, 1);
        maxIdle = rangeCheck("MaxIdle", maxIdle, maxActive, 0);
        minIdle = rangeCheck("MinIdle", minIdle, maxIdle, 0);
//            maxIdle   = rangeCheck("reserved",  reserved,  4,0);
        retryCount = rangeCheck("retry count", retryCount, 1000, 1);
        retryIntervalMs = rangeCheck("retry interval", retryIntervalMs, 100000,
                10);
        oidBatchSize = rangeCheck("oid batch size", oidBatchSize, 1024 * 1024, 1);
    }

    int rangeCheck(String name, int original, int max, int min) {
        int result = original;
        if (original > max) {
            System.err.println(
                    "WARN: " + name + " can not be [" + original + "]. Maximum permissible value is " + max);
            System.err.println("WARN: " + name + " is set to [" + max + "]");
            result = max;
        }
        if (original < min) {
            System.err.println(
                    "WARN: " + name + " can not be [" + original + "]. Minimum permissible value is " + min);
            System.err.println("WARN: " + name + " is set to [" + min + "]");
            result = min;
        }
        return result;
    }

    public Properties toProperties() {
        Properties result = new Properties();
        result.put("Maximum Active Connections ", "" + maxActive);
        return result;
    }

    /**
     * Prints a readable version
     */
    public String toString() {
        StringBuffer tmp = new StringBuffer(
                "Versant configuration properties\r\n");
        tmp.append("Naming Policy              ").append(namingPolicy).append(
                "\r\n")
                .append("Maximum Active Connections ").append("" + maxActive).append(
                        "\r\n")
                .append("Maximum Idle Connections   ").append("" + maxIdle).append(
                        "\r\n")
                .append("Minimum Idle Connections   ").append("" + minIdle).append(
                        "\r\n")
                .append("Reserved Connections       ").append("" + reserved).append(
                        "\r\n")
                .append("Connection Retry Interval  ").append(
                        "" + retryIntervalMs).append("(ms)").append("\r\n")
                .append("Connection Retry Attempt   ").append("" + retryCount).append(
                        "\r\n")
                .append("Connection TimeOut         ").append("" + conTimeout).append(
                        "\r\n")
                .append("Blocks when full           ").append("" + blockWhenFull).append(
                        "\r\n")
                .append("Dynamically define schema  ").append(
                        "" + isDynamicSchemaDefinition).append("\r\n")
                .append("Dynamically evolve schema  ").append(
                        "" + isDynamicSchemaEvolution).append("\r\n");

        return tmp.toString();

    }
}

