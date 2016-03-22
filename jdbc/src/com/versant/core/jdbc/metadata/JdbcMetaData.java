
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
package com.versant.core.jdbc.metadata;

import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.JdbcConfig;
import com.versant.core.util.BeanUtils;
import com.versant.core.jdbc.sql.diff.ControlParams;
import com.versant.core.jdbc.JdbcConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

/**
 * Extra JDBC specific meta data attached to JDOMetaData.
 */
public class JdbcMetaData {

    private final ModelMetaData jmd;
    private final ControlParams migrationParams;

    /**
     * These are all the tables required for the key generators.
     */
    public JdbcTable[] keyGenTables;
    /**
     * This is the max number of simple fields in the primary key of the
     * table for any class.
     */
    public int maxPkSimpleColumns;

    public JdbcMetaData(ModelMetaData jmd, JdbcConfig config) {
        this.jmd = jmd;
        migrationParams = new ControlParams();
        BeanUtils.setProperties(migrationParams,
                config.jdbcMigrationControlProps);
    }

    public ControlParams getMigrationParams() {
        return migrationParams;
    }

    /**
     * Get all tables for store sorted in name order. Tables for classes
     * flagged as doNotCreateTable are left out.
     */
    public ArrayList getTables() {
        return getTables(false);
    }

    /**
     * Get all tables for store sorted in name order. If all is false tables
     * for classes flagged as doNotCreateTable are left out.
     */
    public ArrayList getTables(boolean all) {
        HashSet tables = new HashSet();
        ClassMetaData[] classes = jmd.classes;
        for (int i = classes.length - 1; i >= 0; i--) {
            ClassMetaData cmd = jmd.classes[i];
            JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
            if (!all && jdbcClass.doNotCreateTable) {
                continue;
            }
            jdbcClass.getTables(tables);
        }
        ArrayList a = new ArrayList(tables);

        JdbcTable[] keyGenTables = ((JdbcMetaData)jmd.jdbcMetaData).keyGenTables;
        if (keyGenTables != null) {
            for (int i = 0; i < keyGenTables.length; i++) {
                JdbcTable keyGenTable = keyGenTables[i];
                if (keyGenTable != null) {
                    a.add(keyGenTable);
                }
            }
        }
        Collections.sort(a);
        return a;
    }

}
