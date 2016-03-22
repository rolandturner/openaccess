
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
package com.versant.core.jdbc;

import com.versant.core.jdbc.metadata.JdbcTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

/**
 * These generate primary keys for new rows in tables. Instances must be
 * thread safe.
 */
public interface JdbcKeyGenerator {

    /**
     * Add any JdbcTable instances that this key generator requires to the
     * supplied set. This method is called once per key generator during meta
     * data generation. Any tables returned will be added to the meta data and
     * will get into SQL scripts and so on. If the same key generator
     * instance is returned more than once by a factory then this method
     * will still only be called once on the instance.
     */
    public void addKeyGenTables(HashSet set, JdbcMetaDataBuilder mdb);

    /**
     * If the new key can only be detirmined after the new row has been
     * inserted (e.g. if using a database autoincrement column) then this
     * should return true.
     */
    public boolean isPostInsertGenerator();

    /**
     * Initialize this key generator. This is called when the JDO Genie
     * server initializes before any keys are generated. Key
     * generators should use this to avoid popular race conditions and
     * deadlock opportunities (e.g. multiple 'select max(id) from table'
     * statements executing at the same time). If the same key generator
     * instance is used on more than one class this will be called once
     * for each class.
     *
     * @param className  The name of the class
     * @param classTable The table for the class
     * @param con        Connection to the DataSource for the class
     */
    public void init(String className, JdbcTable classTable,
            Connection con) throws SQLException;

    /**
     * Does this key generator require its own connection? If it does then
     * one will be obtained to generate the key and committed after the
     * key has been generated. This is called prior to every key generation
     * call.
     *
     * NB! This answer should be a 'static' value. If not then race conditions may occur
     * between the time when this is evaluated and when we pass the connection.
     */
    public boolean isRequiresOwnConnection();

    /**
     * Generate a new primary key value for a new instance of the supplied
     * class prior to the row being inserted. The values generated will be used
     * to populate a new OID and then set on a PreparedStatement for the
     * insert. This is called if isPostInsertGenerator returns false.<p>
     * <p/>
     * The newObjectCount parameter indicates the number of new objects that
     * will be inserted (including this one) in the same transaction using
     * this key generator. This may be used to optimize the behavior of the
     * key generator or be ignored. The highlow key generator uses this value
     * instead of its grabSize to avoid executing redundant updates and
     * selects.<p>
     *
     * @param className      The name of the class
     * @param classTable     The table for the class
     * @param newObjectCount The number of new objects being created
     * @param data           The array to store the key values in.
     * @param con            Connection to the DataSource for the class.
     * @throws SQLException on errors
     */
    public void generatePrimaryKeyPre(String className,
            JdbcTable classTable, int newObjectCount, Object[] data,
            Connection con) throws SQLException;

    /**
     * Get extra SQL to be appended to the insert statement. This is only
     * called for post insert key generators. Return null if no extra SQL
     * is required. Key generators can use this as an alternative to running
     * a separate query to get the primary key for the just inserted row.
     */
    public String getPostInsertSQLSuffix(JdbcTable classTable);

    /**
     * Generate a new primary key value for a new instance of the supplied
     * class after the row has been inserted. The values generated will be used
     * to populate a new OID and then set on a PreparedStatement for the
     * insert.  This is called if isPostInsertGenerator returns true.
     *
     * @param className  The name of the class
     * @param classTable The table for the class
     * @param data       The array to store the key values in.
     * @param con        Connection to the DataSource for the class.
     * @param stat       Statement created from con. Do not close it. This will have
     *                   just been used to insert the new row.
     * @throws SQLException on errors
     */
    public void generatePrimaryKeyPost(String className,
            JdbcTable classTable, Object[] data,
            Connection con, Statement stat) throws SQLException;

}
