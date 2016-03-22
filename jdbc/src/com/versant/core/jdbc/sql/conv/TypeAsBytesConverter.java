
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
import com.versant.core.common.Utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws-clause

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Utils;

/**
 * This converter can convert any type to/from byte[]. The type must have
 * a constructor that accepts a byte[] and a method called toBytes that
 * will provide a byte[].
 */
public class TypeAsBytesConverter implements JdbcConverter {

    private Class type;
    private Constructor constructor;
    private Method toBytes;
    private JdbcConverter bytesConverter;

    /**
     * This creates converters for different types.
     */
    public static class Factory extends NoArgJdbcConverterFactory {

        /**
         * Create a converter for col using props as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            Class type = col.javaType;
            JdbcConverterFactory f = jdbcTypeRegistry.getJdbcConverterFactory(col.jdbcType);
            JdbcConverter bytesConverter = f.createJdbcConverter(col, args, jdbcTypeRegistry);
            if (bytesConverter.getValueType() != /*CHFC*/byte[].class/*RIGHTPAR*/) {
                throw BindingSupportImpl.getInstance().illegalArgument("Invalid JDBC type: " +
                    JdbcTypes.toString(col.jdbcType));
            }
            return new TypeAsBytesConverter(type, bytesConverter);
        }

    }

    public TypeAsBytesConverter(Class type, JdbcConverter stringConverter) {
        this.type = type;
        this.bytesConverter = stringConverter;
        try {
            constructor = type.getConstructor(new Class[]{/*CHFC*/byte[].class/*RIGHTPAR*/});
        } catch (NoSuchMethodException e) {
            throw BindingSupportImpl.getInstance().runtime(type + " does not have a " +
                "constructor that accepts a byte[]", e);
        }
        try {
            toBytes = type.getMethod("toBytes", null);
        } catch (NoSuchMethodException e) {
            throw BindingSupportImpl.getInstance().runtime(type + " does not have a " +
                "public toBytes() method", e);
        }
        if (toBytes.getReturnType() != /*CHFC*/byte[].class/*RIGHTPAR*/) {
            throw BindingSupportImpl.getInstance().runtime(type + ".toBytes() does not " +
                "return byte[]");
        }
    }

    /**
     * Get the value of col from rs at position index.
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        byte[] a = (byte[])bytesConverter.get(rs, index, col);
        if (a == null) return null;
        try {
            return constructor.newInstance(new Object[]{a});
        } catch (Throwable x) {
            throw BindingSupportImpl.getInstance().fatalDatastore("Unable to create instance of " +
                type.getName() + " from " + Utils.toString(a) + ": " + x, x);
        }
    }





    private byte[] toBytes(Object value) {
        byte[] a;
        if (value == null) {
            a = null;
        } else {
            try {
                a = (byte[])toBytes.invoke(value, null);
            } catch (Throwable x) {
                throw BindingSupportImpl.getInstance().fatalDatastore("Unable to convert instance of " +
                    type.getName() + " '" + Utils.toString(value) + "' to byte[]: " + x, x);
            }
        }
        return a;
    }

    /**
     * Set parameter index on ps to value (for col).
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col,
            Object value) throws SQLException, JDOFatalDataStoreException {
        bytesConverter.set(ps, index, col, toBytes(value));
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return type;
    }

    /**
     * Is this converter for an Oracle style LOB column? Oracle LOBs require
     * a hard coded a value into the insert/update statement instead of using
     * a replaceable parameter and then select the value (if not null) and
     * modify it.
     */
    public boolean isOracleStyleLOB() {
        return bytesConverter.isOracleStyleLOB();
    }

    /**
     * This is only called if isOracleStyleLOB returns true. Get the String
     * to be embedded in an SQL insert/update statement when the value for
     * this column is not null (e.g. "empty_clob()");
     */
    public String getOracleStyleLOBNotNullString() {
        return bytesConverter.getOracleStyleLOBNotNullString();
    }

    /**
     * Set parameter index on ps to value (for col). This special form is used
     * when the value to be set is available as an int to avoid creating
     * an wrapper instance.
     *
     * @throws java.sql.SQLException on SQL errors
     * @throws javax.jdo.JDOFatalDataStoreException
     *                               if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, int value)
            throws SQLException, JDOFatalDataStoreException {
        throw BindingSupportImpl.getInstance().fatalDatastore("set(..int) called");
    }

    /**
     * This method is only called for converters that return true for
     * isOracleStyleLOB. The LOB to be updated is at index in rs.
     *
     * @throws java.sql.SQLException on SQL errors
     * @throws javax.jdo.JDOFatalDataStoreException
     *                               if value is invalid
     * @see #isOracleStyleLOB
     */
    public void set(ResultSet rs, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        bytesConverter.set(rs, index, col, toBytes(value));
    }

}
