
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
package com.versant.core.jdbc.sql;

/**
 * Generates names for SQL objects that are compatible with JSR220. 
 */
public class Ejb3JdbcNameGenerator extends DefaultJdbcNameGenerator {

    /**
     * Generate a JDBC name from a java class or field name. This just converts 
     * to upper case.
     */
    protected String getJdbcName(String name) {
        return name.toUpperCase();
    }    
    
    public String generateClassTableName(String className) {
        String name = className.substring(className.lastIndexOf('.')+1).toUpperCase();
        addTableName(name);
        return name;
    }

    public String generateLinkTableName(String tableName, String fieldName,
            String elementTableName) {
        String name = tableName + "_" + elementTableName;
        addTableName(name);
        return name;
    }

    public String generateFieldColumnName(String tableName, String fieldName,
            boolean primaryKey) {
        String name = fieldName.toUpperCase();
		String[] split = name.split("[./]");
		name = split[split.length - 1];
        addColumnNameImp(tableName, name);
        return name;
    }
    
    public void generateRefFieldColumnNames(String tableName, String fieldName,
            String[] columnNames, String refTableName, String[] refPkNames,
            boolean otherRefs) throws IllegalArgumentException {
        for (int i = 0; i < refPkNames.length; i++) {
            String name = columnNames[i];
            if (name == null) {
                name = fieldName.toUpperCase();
        		String[] split = name.split("[./]");
        		name = split[split.length - 1];
                name = name + "_" + refPkNames[i];
                columnNames[i] = name;
            }
            if (!isColumnInTable(tableName, name)) {
                addColumnNameImp(tableName, name);
            }
        }
    }

    public void generateLinkTableMainRefNames(String tableName,
            String[] mainTablePkNames, String[] linkMainRefNames) {
        for (int i = 0; i < linkMainRefNames.length; i++) {
            if (linkMainRefNames[i] == null){
                linkMainRefNames[i] = mainTablePkNames[i];
            }
            addColumnNameImp(tableName, linkMainRefNames[i]);
        }
    }

    public void generateLinkTableValueRefNames(String tableName,
            String[] valuePkNames, String valueClassName,
            String[] linkValueRefNames, boolean key) {
        for (int i = 0; i < linkValueRefNames.length; i++) {
            if (linkValueRefNames[i] == null){
                linkValueRefNames[i] = valuePkNames[i];
            }
            addColumnNameImp(tableName, linkValueRefNames[i]);
        }
    }
}
