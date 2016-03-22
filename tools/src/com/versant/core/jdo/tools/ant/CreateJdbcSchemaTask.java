
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

import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.util.BeanUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.versant.core.logging.LogEventStore;

/**
 * This ant task will create the JDBC schema for a set of .jdo files and
 * classes. The classes do not have to be enhanced first. The schema may
 * be written to one or more sql script files or may be generated directly.
 * This can also be used outside of Ant by creating an instance and setting
 * properties or by using the main method and command line args.
 * <p/>
 * This also supports Versant ODBMS.
 */
public class CreateJdbcSchemaTask extends JdoTaskBase {

    public static void main(String[] args) {
        try {
            CreateJdbcSchemaTask t = new CreateJdbcSchemaTask();
            BeanUtils.setCommandLineArgs(t, args, new String[]{
                "config", "project", "outputdir", "destdir", "direct",
                "droptables", "createtables", "validate", "comments",
                "out"
            });
            t.execute();
        } catch (IllegalArgumentException x) {
            System.err.println(x.getMessage());
            System.err.println(HELP);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private static final String HELP =
            "Usage: java com.versant.core.jdo.tools.ant.CreateJdbcSchemaTask\n" +
            "       [-out <name of file for SQL script, default versant.sql>]\n" +
            "       [-droptables <true | false>]\n" +
            "       [-createtables <true | false>]\n\n" +
            "       [-validate <true | false>]\n\n" +
            "       [-comments <true | false>]\n\n" +
            "       [-project <name of project file, default versant.properties>]\n" +
            "WARNING: The droptables option will drop all tables with the same\n" +
            "         names (case insensitive) as tables in the generated schema!\n\n" +
            "This Class can also be used as an Ant task. The command line arguments\n" +
            "have the same names and functions as the task attributes.\n";



    protected String outName = "versant.sql";
    
    protected boolean direct;
    protected boolean droptables;
    protected boolean validate;
    protected boolean comments = true;
    private String logEvents = LogEventStore.LOG_EVENTS_ERRORS;

    public void setOut(String out) {
        this.outName = out;
    }

    public void setOutputdir(String dir) {
        setDestdir(dir);
    }

    
    public void setDestdir(String destdir) {
        this.outName = destdir + "/versant.sql";
    }

  

    public void setCreatetables(String direct) {
        setDirect(direct);
    }

    public void setDirect(String s) {
        direct = isTrue(s);
    }

    private static boolean isTrue(String s) {
        return "*".equals(s) || "true".equals(s);
    }

    public void setDroptables(String s) {
        droptables = isTrue(s);
    }

    public void setValidate(String s) {
        validate = isTrue(s);
    }

    public void setLogEvents(String logEvents) {
        this.logEvents = logEvents;
    }

    /**
     * Include comments in the output?
     */
    public void setComments(boolean comments) {
        this.comments = comments;
    }

    public void execute() {
        super.execute();
        if (!(innermostSmf instanceof JdbcStorageManagerFactory)) {
            return;
        }
        pes.setLogEvents(logEvents);
        JdbcStorageManager sm = (JdbcStorageManager)innermostSmf.getStorageManager();
        if (droptables) dropAllTables(sm);
        generateDatabase(sm, direct);
        if (validate) validateDatabase(sm);
        innermostSmf.returnStorageManager(sm);
    }

    private void dropAllTables(JdbcStorageManager sm) {
        JdbcConnectionSource conSrc = sm.getJdbcConnectionSource();
        Connection con = null;
        try {
            log("Dropping tables in schema on " + conSrc.getURL());
            con = conSrc.getConnection(false, true);
            HashMap dbTableNames = sm.getDatabaseTableNames(con);
            ArrayList a = sm.getJdbcMetaData().getTables();
            for (int i = 0; i < a.size(); i++) {
                JdbcTable t = (JdbcTable)a.get(i);
                String name = (String)dbTableNames.get(t.name.toLowerCase());
                if (name != null) {
                    sm.getSqlDriver().dropTable(con, name);
                }
            }
        } catch (SQLException x) {
            throwBuildException(x.getClass().getName() + ": " +
                    x.getMessage(), x);
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

    private void generateDatabase(JdbcStorageManager sm, boolean direct) {
        JdbcConnectionSource conSrc = sm.getJdbcConnectionSource();
        Connection con = null;
        FileOutputStream fout = null;
        PrintWriter out = null;
        try {
            File f = new File(outName);
            log("Creating " + f);
            fout = new FileOutputStream(f);
            out = new PrintWriter(fout);
            if (direct) {
                log("Creating schema on " + conSrc.getURL());
                con = conSrc.getConnection(false, true);
            }
            sm.getSqlDriver().generateDDL(sm.getJdbcMetaData().getTables(),
                    con, out, comments);
            if (out != null) {
                out.flush();
            }
        } catch (Exception x) {
            throwBuildException(x.getClass().getName() + ": " +
                    x.getMessage(), x);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (con != null) {
                try {
                    conSrc.returnConnection(con);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    private void validateDatabase(JdbcStorageManager sm) {
        JdbcConnectionSource conSrc = sm.getJdbcConnectionSource();
        Connection con = null;
        try {
            log("Validating mapping for " + conSrc.getURL());
            con = conSrc.getConnection(false, false);
            StringWriter error = new StringWriter();
            PrintWriter perror = new PrintWriter(error, false);
            StringWriter fix = new StringWriter();
            PrintWriter pfix = new PrintWriter(fix, false);
            if (!sm.getSqlDriver().checkDDL(
                    sm.getJdbcMetaData().getTables(true), con, perror, pfix,
                    sm.getJdbcMetaData().getMigrationParams())) {
                log(error.toString());
                throwBuildException("Mapping for " +
                        conSrc.getURL() + " has errors");
            }
        } catch (Exception x) {
            throwBuildException(x.getClass().getName() + ": " +
                    x.getMessage(), x);
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
