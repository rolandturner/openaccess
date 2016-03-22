
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
import com.versant.core.jdbc.metadata.JdbcTypes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws-clause

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a base class for converters that convert a type to/from byte[] and
 * use a nested converter to do the actual JDBC work with the byte[].
 * @keep-all
 */
public abstract class TypeAsBytesConverterBase implements JdbcConverter {

    /**
     * Subclasses must extend this and provide their class to the superclass
     * constructor.
     */
    public static abstract class Factory extends NoArgJdbcConverterFactory {

        private HashMap instanceMap = new HashMap(17);

        /**
         * Create a converter for col using props as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            JdbcConverterFactory f = jdbcTypeRegistry.getJdbcConverterFactory(col.jdbcType);
            JdbcConverter c = f.createJdbcConverter(col, args, jdbcTypeRegistry);
            if (c.getValueType() != /*CHFC*/byte[].class/*RIGHTPAR*/) {
                throw BindingSupportImpl.getInstance().illegalArgument("Invalid JDBC type: " +
                    JdbcTypes.toString(col.jdbcType));
            }
            JdbcConverter ans = (JdbcConverter)instanceMap.get(c);
            if (ans == null) {
                instanceMap.put(c, ans = createConverter(c));
            }
            return ans;
        }

        /**
         * Create an instance of our converter.
         */
        protected abstract JdbcConverter createConverter(JdbcConverter nested);

    }

    protected final JdbcConverter nested;

    public TypeAsBytesConverterBase(JdbcConverter nested) {
        this.nested = nested;
    }

    /**
     * Convert a byte[] into an instance of our value class.
     */
    protected abstract Object fromByteArray(byte[] buf);

    /**
     * Convert an instance of our value class into a byte[].
     */
    protected abstract byte[] toByteArray(Object value);

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public abstract Class getValueType();

    /**
     * Is this converter for an Oracle style LOB column? Oracle LOBs require
     * a hard coded a value into the insert/update statement instead of using
     * a replaceable parameter and then select the value (if not null) and
     * modify it.
     */
    public boolean isOracleStyleLOB() {
        return nested.isOracleStyleLOB();
    }

    /**
     * This is only called if isOracleStyleLOB returns true. Get the String
     * to be embedded in an SQL insert/update statement when the value for
     * this column is not null (e.g. "empty_clob()");
     */
    public String getOracleStyleLOBNotNullString() {
        return nested.getOracleStyleLOBNotNullString();
    }

    /**
     * Get the value of col from rs at position index.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        byte[] buf = (byte[])nested.get(rs, index, col);
        if (buf == null) return null;
        return fromByteArray(buf);
    }




    /**
     * Set parameter index on ps to value (for col).
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        nested.set(ps, index, col, toByteArray(value));
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
        nested.set(rs, index, col, toByteArray(value));
    }

}

