
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
package com.versant.core.jdo;

/**
 *
 */
public class DriverUtils {

    private static String[] drivers = new String[]{
        "com.intersys.jdbc.CacheDriver",
        "jdbc:Cache://host:port/database_name",
        "cache",
        null,
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://host:port/database_name",
        "db2",
        "driverType=4",
        "org.firebirdsql.jdbc.FBDriver",
        "jdbc:firebirdsql://host/path/to/database.gdb",
        "firebird",
        null,
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:hsql://host/database_name",
        "hypersonic",
        null,
        "com.informix.jdbc.IfxDriver",
        "jdbc:informix-sqli:host:port/database_name",
        "informix",
        "INFORMIXSERVER=ifmxserver_tcp",
        "com.informix.jdbc.IfxDriver",
        "jdbc:informix-sqli:host:port/database_path",
        "informixse",
        "INFORMIXSERVER=server_name",
        "interbase.interclient.Driver",
        "jdbc:interbase://host/path/to/database.gdb",
        "interbase",
        null,
        "com.microsoft.jdbc.sqlserver.SQLServerDriver",
        "jdbc:microsoft:sqlserver://host:port",
        "mssql",
        "DatabaseName=database_name;SelectMethod=cursor",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://host:port/database_name",
        "mysql",
        null,
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@host:port:database_name",
        "oracle",
        null,
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase:server://host/database_name",
        "pointbase",
        null,
        "org.postgresql.Driver",
        "jdbc:postgresql://host/database_name",
        "postgres",
        null,
        "com.sap.dbtech.jdbc.DriverSapDB",
        "jdbc:sapdb://host/database_name",
        "sapdb",
        null,
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:host:port/database_name",
        "sybase",
        null,
        "[no driver required]",
        "versant:database[@hostname][:portno]",
        "versant",
        null,
    };

    public static void addDriver(String database, String jdoDriver, String driverClass,
            String urlPrefix, String sampleURL, String properties) {
        int length = drivers.length;
        String[] newList = new String[length + 5];
        System.arraycopy(drivers, 0, newList, 5, length);
        drivers[0] = database;
        drivers[1] = database;
        drivers[2] = database;
        drivers[3] = database;
        drivers[4] = database;
        drivers[5] = database;
    }
}
