
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


import org.apache.tools.ant.BuildException;


import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.jdbc.JdbcStorageManagerFactory;

/**
 */
public class SchemaMigrationTask extends JdoTaskBase {

    private ControlParams params = null;
    private boolean checkLength = true;
    private boolean checkType = true;
    private boolean checkScale = true;
    private boolean checkNulls = true;
    private boolean checkPK = true;
    private boolean checkIndex = true;
    private boolean checkConstraint = true;
    private boolean checkExtraColumns = true;
    private String outPutdir = null;
    private boolean direct = false;

    public void setOutputdir(String dir) {
        outPutdir = dir;
    }

    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public void setDatastore(String datastore) {
        // ignore - keep property for compatibility
    }

    public void setCheckConstraint(boolean checkConstraint) {
        this.checkConstraint = checkConstraint;
    }

    public void setCheckExtraColumns(boolean checkExtraColumns) {
        this.checkExtraColumns = checkExtraColumns;
    }

    public void setCheckIndex(boolean checkIndex) {
        this.checkIndex = checkIndex;
    }

    public void setCheckLength(boolean checkLength) {
        this.checkLength = checkLength;
    }

    public void setCheckNulls(boolean checkNulls) {
        this.checkNulls = checkNulls;
    }

    public void setCheckPK(boolean checkPK) {
        this.checkPK = checkPK;
    }

    public void setCheckScale(boolean checkScale) {
        this.checkScale = checkScale;
    }

    public void setCheckType(boolean checkType) {
        this.checkType = checkType;
    }

    public void setOutPutdir(String outPutdir) {
        this.outPutdir = outPutdir;
    }

    public void execute() 

    	throws BuildException 

    	{
        super.execute();

        params = new ControlParams();
        params.setCheckConstraint(checkConstraint);
        params.setCheckExtraColumns(checkExtraColumns);
        params.setCheckIndex(checkIndex);
        params.setCheckLength(checkLength);
        params.setCheckNulls(checkNulls);
        params.setCheckPK(checkPK);
        params.setCheckScale(checkScale);
        params.setCheckType(checkType);

        migrateDatabase();
    }

    private String getFileName(SqlDriver sqlDriver){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_H-m");
        return "Schema_Migration_" + sqlDriver.getName() + "_" + formatter.format(new Date());
    }

    private void migrateDatabase()

    	throws BuildException 

    	{
        JdbcStorageManagerFactory jsmf = (JdbcStorageManagerFactory)innermostSmf;
        JdbcConnectionSource conSrc = jsmf.getConnectionSource();
        Connection con = null;
        FileOutputStream fout = null;
        PrintWriter out = null;
        try {
            log("Checking schema on " + conSrc.getURL());
            StringWriter error = new StringWriter();
            PrintWriter perror = new PrintWriter(error, false);

            StringWriter fix = new StringWriter();
            PrintWriter pfix = new PrintWriter(fix, false);


            con = conSrc.getConnection(false, false);
            boolean valid = jsmf.getSqlDriver().checkDDL(
                    jsmf.getJdbcMetaData().getTables(), con, perror,pfix, params);
            perror.close();
            pfix.close();
            if (valid) {
                log("Schema is valid.");
            } else {
                log("Schema has errors.");
                log(error.toString());
                error.close();

                if (outPutdir != null) {
					File f;
                	
                   		f = new File(outPutdir, getFileName(jsmf.getSqlDriver()) + ".sql");
                    fout = new FileOutputStream(f);
                    out = new PrintWriter(fout);
                    out.write(fix.toString());
                    out.flush();
                    out.close();
                }

                if (direct){
                    runScript(con, fix.toString());
                }

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

    public void runScript(Connection con, String script) throws Exception {
        if (!con.getAutoCommit()) {
            con.rollback();
            con.setAutoCommit(true);
        }

        SQLScriptParser shredder = new SQLScriptParser();
        ArrayList list = shredder.parse(script, true);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            SQLScriptParser.SQLScriptPart scriptPart = (SQLScriptParser.SQLScriptPart) iter.next();
            log("Executing: " + scriptPart.getSql());
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
}
