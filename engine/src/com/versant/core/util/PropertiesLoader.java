
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
package com.versant.core.util;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Utils;
import com.versant.core.common.config.ConfigParser;

import java.util.Properties;
import java.io.*;
import java.net.MalformedURLException;

/**
 * Utility methods to load Properties instances as resources from a ClassLoader
 * based on a name prefix and type String.
 */
public class PropertiesLoader {

    public static final String RES_NAME_PROP = "this.resource.name";

    /**
     * Look for a resorce named prefix-type.properties and load it. If
     * the resource contains a property named alias.for then replace type
     * with that String and attempt to load that resource instead.
     * If sucessful the name of the resource loaded is inserted into the
     * Properties instance using the key {@link #RES_NAME_PROP}.
     *
     * @exception FileNotFoundException if no resource could be loaded
     * @exception IOException on read errors
     */
    public static Properties loadPropertiesForURL(ClassLoader loader,
            String prefix, String url) throws IOException {
        int i = url.indexOf(':');
        if (i <= 0) {
            throw new MalformedURLException(url);
        }
        String type = url.substring(0, i);
        return loadProperties(loader, prefix, type);
    }

    public static Properties loadPropertiesForDB(ClassLoader loader,
            String prefix, String db) throws IOException {
        if (Utils.isStringEmpty(db)){
            throw BindingSupportImpl.getInstance().runtime("Unable to guess " +
                    "database type. use the " + ConfigParser.STORE_DB +
                    " property to set the database type");
        }

        if (Utils.isVersantDatabaseType(db)){
            return loadProperties(loader, prefix, "versant");
        } else {
            return loadProperties(loader, prefix, "jdbc");
        }
    }

    /**
     * Look for a resorce named prefix-type.properties and load it. If
     * the resource contains a property named alias.for then replace type
     * with that String and attempt to load that resource instead.
     * If sucessful the name of the resource loaded is inserted into the
     * Properties instance using the key {@link #RES_NAME_PROP}.
     *
     * @exception FileNotFoundException if no resource could be loaded
     * @exception IOException on read errors
     */
    public static Properties loadProperties(ClassLoader loader,
            String prefix, String type) throws IOException {
        for (;;) {
            String resourceName = prefix + "-" + type + ".properties";
            Properties p = loadProperties(loader, resourceName);
            if (p == null) {
                throw new FileNotFoundException("Resource not found: " +
                        resourceName);
            }
            type = p.getProperty("alias.for");
            if (type == null) {
                p.put(RES_NAME_PROP, resourceName);
                return p;
            }
        }
    }

    /**
     * Load Properties from resource name. If the first attempt fails then
     * '/' is prepended to the resource name and another try is made.
     * Returns null if not found.
     *
     */
    public static Properties loadProperties(ClassLoader loader,
            String resourceName) throws IOException {
        InputStream in = loader.getResourceAsStream(resourceName);
        if (in == null) {
            in = loader.getResourceAsStream("/" + resourceName);
        }
        if (in == null) {
            return null;
        }
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Load Properties from a file. Returns null if not found.
     */
    public static Properties loadProperties(String fileName) throws IOException {
        InputStream in;
        try {
            in = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            return null;
        }
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}

