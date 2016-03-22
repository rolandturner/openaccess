
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

import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageManagerFactoryBuilder;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 */
public class SchemaMigrationBean {
    private ConfigInfo config;
    private Properties properties;
    private String propertiesResourceName = "versant.properties";
    private File propertiesFile;

    private StorageManagerFactory smf;

    private ControlParams params = null;
    private boolean checkLength = true;
    private boolean checkType = true;
    private boolean checkScale = true;
    private boolean checkNulls = true;
    private boolean checkPK = true;
    private boolean checkIndex = true;
    private boolean checkConstraint = true;
    private boolean checkExtraColumns = true;
    private File outputDir = null;
    private boolean direct = false;
    private String datastoreName;
    private boolean logEventsToSysOut = true;
    private PrintStream out = System.out;

    /**
     * Returns the output PrintStream
     */
    public PrintStream getOut() {
        return out;
    }
    /**
     * Set the logging PrintStream (System.out by default)
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }

    public boolean isLogEventsToSysOut() {
        return logEventsToSysOut;
    }

    /**
     * Must every thing be logged to System.out (true by default)
     */
    public void setLogEventsToSysOut(boolean logEventsToSysOut) {
        this.logEventsToSysOut = logEventsToSysOut;
    }

    /**
     * Is the constraints of the tables checked.
     */
    public boolean isCheckConstraint() {
        return checkConstraint;
    }

    /**
     * Must the constraints of the tables be checked.
     */
    public void setCheckConstraint(boolean checkConstraint) {
        this.checkConstraint = checkConstraint;
    }

    /**
     * Is the extra columns of the table checked.
     */
    public boolean isCheckExtraColumns() {
        return checkExtraColumns;
    }

    /**
     * Must the extra columns of the table be checked.
     */
    public void setCheckExtraColumns(boolean checkExtraColumns) {
        this.checkExtraColumns = checkExtraColumns;
    }

    /**
     * Is the indexes of the tables checked.
     */
    public boolean isCheckIndex() {
        return checkIndex;
    }

    /**
     * Must the indexes of the tables be checked.
     */
    public void setCheckIndex(boolean checkIndex) {
        this.checkIndex = checkIndex;
    }

    /**
     * Is the lenght of the columns checked.
     */
    public boolean isCheckLength() {
        return checkLength;
    }

    /**
     * Must the lenght of the columns be checked.
     */
    public void setCheckLength(boolean checkLength) {
        this.checkLength = checkLength;
    }

    /**
     * Is the null values of the columns checked.
     */
    public boolean isCheckNulls() {
        return checkNulls;
    }

    /**
     * Must the null values of the columns be checked.
     */
    public void setCheckNulls(boolean checkNulls) {
        this.checkNulls = checkNulls;
    }

    /**
     * Is the primary keys of the tables checked.
     */
    public boolean isCheckPK() {
        return checkPK;
    }

    /**
     * Must the primary keys of the tables be checked.
     */
    public void setCheckPK(boolean checkPK) {
        this.checkPK = checkPK;
    }

    /**
     * Is the scale of the columns checked.
     */
    public boolean isCheckScale() {
        return checkScale;
    }

    /**
     * Must the scale of the columns be checked.
     */
    public void setCheckScale(boolean checkScale) {
        this.checkScale = checkScale;
    }

    /**
     * Is the types of the columns checked.
     */
    public boolean isCheckType() {
        return checkType;
    }

    /**
     * Must the types of the columns be checked.
     */
    public void setCheckType(boolean checkType) {
        this.checkType = checkType;
    }

    /**
     * Gets the datastore name.
     */
    public String getDatastoreName() {
        return datastoreName;
    }

    /**
     * Sets the datastore name i.e. store0,
     * if this property is not set, then the first datastore will be used.
     */
    public void setDatastoreName(String datastoreName) {
        this.datastoreName = datastoreName;
    }

    /**
     * Will the script be executed directly.
     */
    public boolean isDirect() {
        return direct;
    }
    /**
     * Must the script be generated and executed directly?
     */
    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public File getOutputDir() {
        return outputDir;
    }
    /**
     * Where must the script be generated to.
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Get the jdogenie properties.
     */

    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the jdogenie properties, these are the same properies that are in
     * the *.jdogenie properties file.
     */
    public void setProperties(Properties properties) {
        if (config != null){
            // throw exe
        }
        this.properties = properties;
        ConfigParser parser = new ConfigParser();
        config = parser.parse(properties);
        config.validate();
    }

    /**
     * Get the *.jdogenie property file
     */
    public File getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * Sets the *.jdogenie property as a file.
     */
    public void setPropertiesFile(File propertiesFile) {
        if (config != null) {                       
            // throw exe
        }
        this.propertiesFile = propertiesFile;
        ConfigParser parser = new ConfigParser();
        config = parser.parseResource(propertiesFile);
        config.validate();
    }

    /**
     * Gets the *.jdogenie property file resource name
     */
    public String getPropertiesResourceName() {
        return propertiesResourceName;
    }

    /**
     * Sets the *.jdogenie property filename as a resource.
     */
    public void setPropertiesResourceName(String propertiesResourceName) {
        if (config != null) {
            // throw exe
        }
        this.propertiesResourceName = propertiesResourceName;
        ConfigParser parser = new ConfigParser();
        config = parser.parseResource(propertiesResourceName);
        config.validate();
    }

    private void init() {


        Thread.currentThread().setContextClassLoader(getClassLoader());




        StorageManagerFactoryBuilder b = new StorageManagerFactoryBuilder();
        b.setConfig(config);
        b.setLoader(getClassLoader());
        b.setFullInit(false);
        b.setIgnoreConFactoryProperties(true);
        smf = b.createStorageManagerFactory();

        params = new ControlParams();
        params.setCheckConstraint(checkConstraint);
        params.setCheckExtraColumns(checkExtraColumns);
        params.setCheckIndex(checkIndex);
        params.setCheckLength(checkLength);
        params.setCheckNulls(checkNulls);
        params.setCheckPK(checkPK);
        params.setCheckScale(checkScale);
        params.setCheckType(checkType);
    }

    private  ClassLoader getClassLoader() {
        ClassLoader taskClassLoader = getClass().getClassLoader();
        if (taskClassLoader == null) {
            taskClassLoader = ClassLoader.getSystemClassLoader();
        }
        return taskClassLoader;
    }

    /**
     * Migrate the given database, if direct is 'false' then just a script is
     * generated.
     * if direct is true, the the script is executed against the given datastore.
     */
    public void migrateDatabase() throws Exception {
        String script = generateScript();
        if (direct) {
            JdbcConnectionSource conSrc =
                    ((JdbcStorageManagerFactory)smf).getConnectionSource();
            Connection con = null;
            try {
                runScript(con, script);
            } finally {
                if (con != null) {
                    try {
                        conSrc.returnConnection(con);
                    } catch (SQLException e) {
                        // ignore
                    }
                }
            }
        }
    }

    private String getFileName(SqlDriver sqlDriver) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_H-m");
        return "Schema_Migration_" + sqlDriver.getName() + "_" + formatter.format(new Date());
    }

    /**
     * Generate the schema migration script, if there is no changes then an empty
     * String is returned.
     *
     */
    public String generateScript() throws Exception {
        init();
        JdbcStorageManagerFactory jsmf = (JdbcStorageManagerFactory)smf;
        JdbcConnectionSource conSrc = jsmf.getConnectionSource();
        Connection con = null;
        FileOutputStream fout = null;
        PrintWriter out1 = null;
        try {
            if (logEventsToSysOut) {
                out.println("Checking schema on " + conSrc.getURL());
            }
            StringWriter error = new StringWriter(10240);
            PrintWriter perror = new PrintWriter(error, false);

            StringWriter fix = new StringWriter(10240);
            PrintWriter pfix = new PrintWriter(fix, false);


            con = conSrc.getConnection(false, false);
            boolean valid = jsmf.getSqlDriver().checkDDL(
                    jsmf.getJdbcMetaData().getTables(), con, perror,pfix, params);
            perror.close();
            pfix.close();
            if (valid) {
                if (logEventsToSysOut) {
                    out.println("Schema is valid.");
                }
                return "";
            } else {
                if (logEventsToSysOut) {
                    out.println("Schema has errors.");
                    out.println(error.toString());
                }
                error.close();
                if (outputDir != null) {
                    String fileName = getFileName(jsmf.getSqlDriver()) + ".sql";
                    if (logEventsToSysOut) {
                        out.println("Writing file ("+ fileName +") to directory "+ outputDir);
                    }
                    File f = new File(outputDir, fileName);
                    fout = new FileOutputStream(f);
                    out1 = new PrintWriter(fout);
                    out1.write(fix.toString());
                    out1.flush();
                    out1.close();
                }
                return fix.toString();
            }
        } finally {
            if (con != null) {
                try {
                    conSrc.returnConnection(con);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }


    private void runScript(Connection con, String script) throws Exception {
            if (!con.getAutoCommit()) {
                con.rollback();
                con.setAutoCommit(true);
            }

            SQLScriptParser shredder = new SQLScriptParser();
            ArrayList list = shredder.parse(script, true);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                SQLScriptParser.SQLScriptPart scriptPart = (SQLScriptParser.SQLScriptPart) iter.next();
                if (logEventsToSysOut) {
                    out.println("Executing: \n" + scriptPart.getSql());
                }
                Statement stat = null;
                try {
                    stat = con.createStatement();
                    stat.execute(scriptPart.getSql());
                } finally {
                    try {
                        stat.close();
                    } catch (SQLException e) {
                        //hide
                    }
                }
            }
    }

    /**
     * Execute the given script agains the datastore
     */
    public void executeScript(String script) throws Exception {
        init();
        JdbcConnectionSource conSrc =
                ((JdbcStorageManagerFactory)smf).getConnectionSource();
        Connection con = null;
        try {
            runScript(con, script);
        } finally {
            if (con != null) {
                try {
                    conSrc.returnConnection(con);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }



}
