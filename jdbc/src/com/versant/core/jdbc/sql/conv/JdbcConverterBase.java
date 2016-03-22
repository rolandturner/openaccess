
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
package com.versant.core.jdbc.sql.conv;

import com.versant.core.jdbc.JdbcConverter;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.metadata.JdbcColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws-clause

import com.versant.core.common.BindingSupportImpl;

/**
 * <p>Base class for converters. This implements most methods in JdbcConverter
 * assuming that this is not a Oracle style LOB converter. Subclasses
 * only have to provide the following methods:</p>
 *
 * <ul>
 * <li>{@link #get(ResultSet, int, JdbcColumn) }
 * <li>{@link #set(PreparedStatement, int, JdbcColumn, Object) }
 * <li>{@link #getValueType() }
 * </ul>
 *
 * <p>A {@link JdbcConverterFactory} must also be written.</p>
 *
 * @keep-all
 */
public abstract class JdbcConverterBase implements JdbcConverter {

    /**
     * Is this converter for an Oracle style LOB column? Oracle LOBs require
     * a hard coded a value into the insert/update statement instead of using
     * a replaceable parameter and then select the value (if not null) and
     * modify it.
     */
    public boolean isOracleStyleLOB() {
        return false;
    }

    /**
     * This is only called if isOracleStyleLOB returns true. Get the String
     * to be embedded in an SQL insert/update statement when the value for
     * this column is not null (e.g. "empty_clob()");
     */
    public String getOracleStyleLOBNotNullString() {
        return null;
    }

    /**
     * Get the value of col from rs at position index.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public abstract Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException;





    /**
     * Set parameter index on ps to value (for col).
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public abstract void set(PreparedStatement ps, int index, JdbcColumn col,
            Object value) throws SQLException, JDOFatalDataStoreException;

    /**
     * Set parameter index on ps to value (for col). This special form is used
     * when the value to be set is available as an int to avoid creating
     * an wrapper instance.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, int value)
            throws SQLException, JDOFatalDataStoreException {
        throw BindingSupportImpl.getInstance().fatalDatastore("set(..int) called");
    }

    /**
     * This method is called for converters that return true for
     * isOracleStyleLOB. The value at index in rs will contain the LOB to
     * be updated.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(ResultSet rs, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        throw BindingSupportImpl.getInstance().fatalDatastore("set(rs..) called");
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public abstract Class getValueType();

}

