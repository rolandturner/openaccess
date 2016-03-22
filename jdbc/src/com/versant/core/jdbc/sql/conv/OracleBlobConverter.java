
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
import com.versant.core.jdbc.JdbcTypeRegistry;
import com.versant.core.jdbc.metadata.JdbcColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws-clause

import com.versant.core.common.BindingSupportImpl;


import oracle.sql.BLOB;



/**
 * This converter converts byte[] stored in Oracle BLOB columns to
 * and from SQL.
 * @keep-all
 */
public class OracleBlobConverter implements JdbcConverter 
{

    public static class Factory extends NoArgJdbcConverterFactory {

        private OracleBlobConverter converter;

        /**
         * Create a converter for col using args as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new OracleBlobConverter();
            return converter;
        }

    }

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Is this converter for an Oracle style LOB column? Oracle LOBs require
     * a hard coded a value into the insert/update statement instead of using
     * a replaceable parameter and then select the value (if not null) and
     * modify it.
     */
    public boolean isOracleStyleLOB() {
        return true;
    }

    /**
     * This is only called if isOracleStyleLOB returns true. Get the String
     * to be embedded in an SQL insert/update statement when the value for
     * this column is not null (e.g. "empty_clob()");
     */
    public String getOracleStyleLOBNotNullString() {
        return "empty_blob()";
    }

    /**
     * Get the value of col from rs at position index.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {

        BLOB blob = (BLOB)rs.getBlob(index);
        if (blob == null || blob.isEmptyLob()) return null;
        if (blob.length() == 0) return EMPTY_BYTE_ARRAY;
        return blob.getBytes(1, (int)blob.length());



// 
    }



    /**
     * Set parameter index on ps to value (for col).
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        throw BindingSupportImpl.getInstance().fatalDatastore("set(ps..) called");
    }

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

        BLOB blob = (BLOB)rs.getBlob(index);
        blob.putBytes(1, (byte[])value);


		// Calling trim leaks cursors - we make new CLOBs for every update to
        // avoid this problem
        // DO NOT DO - blob.trim( ((byte[])value).length );
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/byte[].class/*RIGHTPAR*/;
    }

}

