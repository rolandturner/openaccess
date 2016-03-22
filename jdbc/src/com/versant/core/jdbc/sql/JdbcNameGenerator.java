
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
 * This is responsible for generating and validating names for different
 * JDBC entities (tables, columns, indexes and constraints). The SqlDriver
 * for each store creates and configures a name generator instance for the
 * store. This may be replaced by a user configured generator.
 *
 * @keep-all
 *
 */
public interface JdbcNameGenerator {

    /**
     * This is called immediately after the name generator has been constructed
     * to indicate which database is in use.
     * @param db Database type (mssql, sybase, oracle, informix et al.).
     */
    public void setDatabaseType(String db);

    /**
     * Add a table name specified in jdo meta data.
     *
     * @exception IllegalArgumentException if the name is invalid
     *         (e.g. 'duplicate table name' or 'invalid character XXX in name'
     *         etc.)
     */
    public void addTableName(String name) throws IllegalArgumentException;

    /**
     * Remove all information about table.
     */
    public void removeTableName(String name);

    /**
     * Generate a table name for a persistent class. The name generator must
     * 'add' it.
     *
     * @see #addTableName
     */
    public String generateClassTableName(String className);

    /**
     * Generate a table name for a link table (normally used to hold the values
     * of a collection). The name generator must 'add' it.
     *
     * @param tableName The table on the 1 side of the the link
     * @param fieldName The field the link table is for
     * @param elementTableName The table on the n side of the link or null if
     *        none (e.g. a link table for a collection of String's)
     *
     * @see #addTableName
     */
    public String generateLinkTableName(String tableName, String fieldName,
        String elementTableName);

    /**
     * Add the primary key constaint name specified in jdo meta data.
     * @exception IllegalArgumentException if it is invalid
     */
    public void addPkConstraintName(String tableName, String pkConstraintName)
            throws IllegalArgumentException;

    /**
     * Generate a name for the primary key constaint for tableName. The name
     * generator must add it.
     *
     * @see #addPkConstraintName
     */
    public String generatePkConstraintName(String tableName);

    /**
     * Add the referential integrity constaint name specified in jdo meta data.
     * @exception IllegalArgumentException if it is invalid
     */
    public void addRefConstraintName(String tableName, String refConstraintName)
            throws IllegalArgumentException;

    /**
     * Generate a name for a referential integrity constaint for tableName.
     * The name generator must add it.
     *
     * @param tableName The table with the constraint
     * @param refTableName The table being referenced
     * @param fkNames The names of the foreign keys in tableName
     * @param refPkNames The names of the primary key of refTableName
     *
     * @see #addRefConstraintName
     */
    public String generateRefConstraintName(String tableName,
        String refTableName, String[] fkNames, String[] refPkNames);

    /**
     * Add a column name. The tableName will have already been added.
     *
     * @exception IllegalArgumentException if the name is invalid
     *         (e.g. 'duplicate column name' or 'invalid character XXX in name'
     *         etc.)
     */
    public void addColumnName(String tableName, String columnName)
            throws IllegalArgumentException;

    /**
     * Does the table contain the column?
     */
    public boolean isColumnInTable(String tableName, String columnName);

    /**
     * Generate and add a name for the primary key column for a PC class using
     * datastore identity.
     */
    public String generateDatastorePKName(String tableName);

    /**
     * Generate and add the name for a classId column.
     * @see #addColumnName
     */
    public String generateClassIdColumnName(String tableName);

    /**
     * Generate and add the name for a field column.
     */
    public String generateFieldColumnName(String tableName, String fieldName,
        boolean primaryKey);

    /**
     * Generate and add names for one or more columns for a field that is
     * a reference to another PC class. Some of the columns may already have
     * names.
     *
     * @param columnNames Store the column names here (some may already have
     *        names if specified in the .jdo meta data)
     * @param refTableName The table being referenced (null if not a JDBC class)
     * @param refPkNames The names of the primary key columns of refTableName
     * @param otherRefs Are there other field referencing the same class here?
     *
     * @exception IllegalArgumentException if any existing names are invalid
     */
    public void generateRefFieldColumnNames(String tableName,
            String fieldName, String[] columnNames, String refTableName,
            String[] refPkNames, boolean otherRefs)
        throws IllegalArgumentException;

    /**
     * Generate and add names for one or more columns for a field that is
     * a polymorphic reference to any other PC class. Some of the columns may
     * already have names.
     *
     * @param columnNames Store the column names here (some may already have
     *        names if specified in the .jdo meta data). The class-id column
     *        is at index 0.
     *
     * @exception IllegalArgumentException if any existing names are invalid
     */
    public void generatePolyRefFieldColumnNames(String tableName,
            String fieldName, String[] columnNames)
        throws IllegalArgumentException;

    /**
     * Generate and add names for the column(s) in a link table that reference
     * the primary key of the main table. Some of the columns may already
     * have names which must be kept (no need to add them).
     *
     * @param tableName The link table
     * @param mainTablePkNames The names of the main table primary key
     * @param linkMainRefNames The corresponding column names in the link table
     */
    public void generateLinkTableMainRefNames(String tableName,
            String[] mainTablePkNames, String[] linkMainRefNames);

    /**
     * Generate and add the name for a the column in a link table that stores
     * the element sequence number.
     */
    public String generateLinkTableSequenceName(String tableName);

    /**
     * Generate and add names for the column(s) in a link table that reference
     * the primary key of the value table. This is called for collections of
     * PC classes. Some of the columns may already have names which must be
     * kept (no need to add them).
     *
     * @param tableName The link table
     * @param valuePkNames The names of the value table primary key (may be
     *        null if the value class is not stored in JDBC)
     * @param valueClassName The name of the value class
     * @param linkValueRefNames The corresponding column names in the link table
     * @param key Is this a key in a link table for a map?
     */
    public void generateLinkTableValueRefNames(String tableName,
            String[] valuePkNames, String valueClassName,
            String[] linkValueRefNames, boolean key);

    /**
     * Generate and add the name for a the column in a link table that stores
     * the value where the value is not a PC class (int, String etc).
     *
     * @param tableName The link table
     * @param valueCls The value class
     * @param key Is this a key in a link table for a map?
     */
    public String generateLinkTableValueName(String tableName,
            Class valueCls, boolean key);

    /**
     * Add an index name. The tableName will have already been added.
     * @exception IllegalArgumentException if it is invalid
     */
    public void addIndexName(String tableName, String indexName)
            throws IllegalArgumentException;

    /**
     * Generate and add an index name.
     * @see #addIndexName
     */
    public String generateIndexName(String tableName, String[] columnNames);

}

