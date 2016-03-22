
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
package com.versant.core.jdo.tools.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import com.versant.core.common.BindingSupportImpl;

/** 
 * Ant wrapper for the CopyDatabaseBean.
 * @see CopyDatabaseBean
 * @keep-all
 */
public class CopyDatabaseTask extends Task {

    private String config = "versant.properties";
    private CopyDatabaseBean cbean = new CopyDatabaseBean();

    public void execute() throws BuildException {
        if (config == null) {
            throw new BuildException("project attribute is required " +
                "(name of .jdogenie project on the classpath)");
        }
        try {
            cbean.setSrcProps(loadProperties(config));
            cbean.copyDatabase();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            throw new BuildException(t);
        }
    }

    private Properties loadProperties(String filename) throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        InputStream in = null;
        try {
            if (filename.startsWith("/")) filename = filename.substring(1);
            in = cl.getResourceAsStream(filename);
            if (in == null) {
                throw BindingSupportImpl.getInstance().runtime("Resource not found: " + filename);
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            if (in != null) in.close();
        }
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void setProject(String config) {
        setConfig(config);
    }

    public void setDatastore(String datastore) {
        cbean.setDatastore(datastore);
    }

    public void setDb(String db) {
        cbean.setDb(db);
    }

    public void setUrl(String url) {
        cbean.setUrl(url);
    }

    public void setDriver(String driver) {
        cbean.setDriver(driver);
    }

    public void setUser(String user) {
        cbean.setUser(user);
    }

    public void setPassword(String password) {
        cbean.setPassword(password);
    }

    public void setProperties(String properties) {
        cbean.setProperties(properties);
    }

    public void setDropTables(boolean dropTables) {
        cbean.setDropTables(dropTables);
    }

    public void setCreateTables(boolean createTables) {
        cbean.setCreateTables(createTables);
    }

    public void setRowsPerTransaction(int rowsPerTransaction) {
        cbean.setRowsPerTransaction(rowsPerTransaction);
    }

    public void setLogEvents(String logEvents) {
        cbean.setLogEvents(logEvents);
    }

}
