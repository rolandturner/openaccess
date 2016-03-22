
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

import com.versant.core.jdbc.metadata.JdbcColumn;

import javax.jdo.JDOFatalDataStoreException;	//todo: only referenced in "throws"-clause, discuss, whether to remove this

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * JdbcConverters are used to convert a column from a JDBC ResultSet into a
 * Java object and to set a parameter of a PreparedStatement from a Java
 * object. There are also extra methods used to support columns that need
 * special handling (e.g. Oracle LOBs).
 */
public interface JdbcConverter {

    /**
     * Is this converter for an Oracle style LOB column? Oracle LOBs require
     * a hard coded a value into the insert/update statement instead of using
     * a replaceable parameter and then select the value (if not null) and
     * modify it.
     */
    public boolean isOracleStyleLOB();

    /**
     * This is only called if isOracleStyleLOB returns true. Get the String
     * to be embedded in an SQL insert/update statement when the value for
     * this column is not null (e.g. "empty_clob()");
     */
    public String getOracleStyleLOBNotNullString();

    /**
     * Get the value of col from rs at position index.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException;




    /**
     * Set parameter index on ps to value (for col). This is not called for
     * converters that return true for isOracleStyleLOB.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     * @see #isOracleStyleLOB
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException;

    /**
     * Set parameter index on ps to value (for col). This special form is used
     * when the value to be set is available as an int to avoid creating
     * an wrapper instance.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, int value)
            throws SQLException, JDOFatalDataStoreException;

    /**
     * This method is only called for converters that return true for
     * isOracleStyleLOB. The LOB to be updated is at index in rs.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     * @see #isOracleStyleLOB
     */
    public void set(ResultSet rs, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException;

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's). If the converter works with a primitive
     * type e.g. Integer.TYPE then the corresponding wrapper class should be
     * returned i.e. Integer.class.
     */
    public Class getValueType();
}
