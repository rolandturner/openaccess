
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

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdbc.JdbcStorageManagerFactory;

/**
 * This task will load a sql script file or/and execute a sql
 * command in the body of the xml file.
 */
public class ExecuteScriptTask extends JdoTaskBase {

    private String scriptFile;
    private String sqlCommand = "";

    /**
     * Set an inline SQL command to execute.
     * NB: Properties are not expanded in this text.
     */
    public void addText(String sql) {
        this.sqlCommand += sql ;
    }

    public void setDatastore(String datastore) {
        // ignore - property kept for compatibility with old versions
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public void execute() throws BuildException {
        super.execute();
        File file = null;

        if (isSqlCommandEmpty() && scriptFile == null) {
            throw BindingSupportImpl.getInstance().illegalArgument("scriptFile property or sql statement must be set!");
        }


        if (scriptFile != null){
            file = new File(scriptFile);
            if (!file.exists()){
                throw BindingSupportImpl.getInstance().illegalArgument("File "+file.toString() +" does not exist");
            }
        }

        try {
            if (!isSqlCommandEmpty()) {
                runScript(sqlCommand);
            }

            if (file != null){
                runScript(getText(file));
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public void runScript(String script) throws Exception {
        Connection con = null;
        JdbcStorageManagerFactory smf = (JdbcStorageManagerFactory)getSmf();
        try {
            con = smf.getConnectionSource().getConnection(false, true);
            SQLScriptParser shredder = new SQLScriptParser();
            ArrayList list = shredder.parse(script, true);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                SQLScriptParser.SQLScriptPart scriptPart = (SQLScriptParser.SQLScriptPart) iter.next();
                log("Executing: "+scriptPart.getSql());
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
        } finally {
            if (con != null) {
                try {
                    smf.getConnectionSource().returnConnection(con);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    private String getText(File file) throws IOException {
        int size = (int) file.length();
        int chars_read = 0;
        FileReader in = new FileReader(file);
        char[] data = new char[size];
        while (in.ready()) {
            chars_read += in.read(data, chars_read, size - chars_read);
        }
        in.close();
        return new String(data, 0, chars_read);
    }

    private boolean isSqlCommandEmpty(){
        return sqlCommand.trim().equals("");
    }
}
