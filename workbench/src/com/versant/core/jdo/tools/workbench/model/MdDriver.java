
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

import za.co.hemtech.config.Config;

/**
 * A JDBC (or other) driver class and sample URL.
 * @keep-all
 */
public class MdDriver {

    private String driverClass;
    private String sampleURL;
    private String database;
    private String properties;

    public MdDriver() {
    }

    public MdDriver(String driverClass, String sampleURL, String database,
            String properties) {
        this.driverClass = driverClass;
        this.sampleURL = sampleURL;
        this.database = database;
        this.properties = properties;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getSampleURL() {
        return sampleURL;
    }

    public void setSampleURL(String sampleURL) {
        this.sampleURL = sampleURL;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * Save to a config (.ini file).
     */
    public void save(Config c) {
        c.setString("class", driverClass);
        c.setString("sampleURL", sampleURL);
        c.setString("database", database);
        c.setString("properties", properties);
    }

    /**
     * Load from a config (.ini file).
     */
    public void load(Config c) {
        driverClass = c.getString("class");
        sampleURL = c.getString("sampleURL");
        database = c.getString("database");
        properties = c.getString("properties");
    }

}

