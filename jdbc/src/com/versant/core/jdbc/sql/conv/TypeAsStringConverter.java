
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws-clause

import com.versant.core.common.BindingSupportImpl;

/**
 * <p>This converter can convert any type to/from String. The type must have
 * a constructor that accepts a String. If it has a method 'String
 * toExternalString()' then this is used to convert it to a String. Otherwise
 * its toString() method is used.</p>
 */
public class TypeAsStringConverter implements JdbcConverter {

    private Class type;
    private Constructor constructor;
    private Method toExternalString;
    private JdbcConverter stringConverter;

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
            JdbcConverter stringConverter;
            try {
                JdbcConverterFactory f = jdbcTypeRegistry.getJdbcConverterFactory(col.jdbcType);
                stringConverter = f.createJdbcConverter(col, args, jdbcTypeRegistry);
            } catch (IllegalArgumentException e) {
                // no converter available
                stringConverter = DummyStringConverter.INSTANCE;
            }
            if (stringConverter.getValueType() != /*CHFC*/String.class/*RIGHTPAR*/) {
                throw BindingSupportImpl.getInstance().illegalArgument("Invalid JDBC type: " +
                    JdbcTypes.toString(col.jdbcType));
            }
            return new TypeAsStringConverter(type, stringConverter);
        }

    }

    public TypeAsStringConverter(Class type, JdbcConverter stringConverter) {
        this.type = type;
        this.stringConverter = stringConverter;
        try {
            constructor = type.getConstructor(new Class[]{/*CHFC*/String.class/*RIGHTPAR*/});
        } catch (NoSuchMethodException e) {
            throw BindingSupportImpl.getInstance().runtime(type + " does not have a " +
                "constructor that accepts a String", e);
        }
        try {
            toExternalString = type.getMethod("toExternalString", null);
            if (toExternalString.getReturnType() != /*CHFC*/String.class/*RIGHTPAR*/) {
                throw BindingSupportImpl.getInstance().runtime(type + ".toExternalString() does not " +
                    "return String");
            }
        } catch (NoSuchMethodException e) {
            // no problem - we will use toString
        }
    }

    /**
     * Get the value of col from rs at position index.
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        String s = (String)stringConverter.get(rs, index, col);
        if (s == null) return null;
        try {
            return constructor.newInstance(new Object[]{s});
        } catch (Throwable x) {
            throw BindingSupportImpl.getInstance().fatalDatastore("Unable to create instance of " +
                type.getName() + " from '" + s + "': " + x, x);
        }
    }






    /**
     * Set parameter index on ps to value (for col).
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col,
            Object value) throws SQLException, JDOFatalDataStoreException {
        stringConverter.set(ps, index, col, toString(value));
    }

    private String toString(Object value) {
        String s;
        if (value == null) {
            s = null;
        } else {
            try {
                s = toExternalString == null
                        ? value.toString()
                        : (String)toExternalString.invoke(value, null);
            } catch (Throwable x) {
                throw BindingSupportImpl.getInstance().fatalDatastore("Unable to convert instance of " +
                    type.getName() + " using " +
                    (toExternalString == null ? "toString()" : "toExternalString()") +
                    ": " + x, x);
            }
        }
        return s;
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
        return stringConverter.isOracleStyleLOB();
    }

    /**
     * This is only called if isOracleStyleLOB returns true. Get the String
     * to be embedded in an SQL insert/update statement when the value for
     * this column is not null (e.g. "empty_clob()");
     */
    public String getOracleStyleLOBNotNullString() {
        return stringConverter.getOracleStyleLOBNotNullString();
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
        stringConverter.set(rs, index, col, toString(value));
    }

}
