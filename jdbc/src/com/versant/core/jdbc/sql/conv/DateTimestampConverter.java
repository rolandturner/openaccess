
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
import com.versant.core.jdbc.JdbcTypeRegistry;
import com.versant.core.jdbc.metadata.JdbcColumn;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws clause
import java.sql.*;
import java.util.Date;

/**
 * This converts java.util.Date's to and from a column using setTimestamp
 * and createTimestamp.
 */
public class DateTimestampConverter extends JdbcConverterBase {

    private static final boolean useNanos;

    static {
    	
        String v = System.getProperty("java.version");
        useNanos = v.startsWith("1.3");

        
    }

    public static class Factory extends NoArgJdbcConverterFactory {

        private DateTimestampConverter converter;

        /**
         * Create a converter for col using props as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new DateTimestampConverter();
            return converter;
        }

    }

    /**
     * Get the value of col from rs at position index.
     *
     * @throws SQLException               on SQL errors
     * @throws JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        Timestamp t = rs.getTimestamp(index);
        if (t == null) return null;
        if (useNanos) {
            return new Date(t.getTime() + (t.getNanos() / 1000000));
        } else {
            return new Date(t.getTime());
        }
    }

    /**
     * Set parameter index on ps to value (for col).
     *
     * @throws SQLException               on SQL errors
     * @throws JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col,
            Object value)
            throws SQLException, JDOFatalDataStoreException {
        if (value == null) {
            ps.setNull(index, col.jdbcType);
        } else {
            Date d = (Date)value;
            if (col.jdbcType == Types.DATE) {
                ps.setDate(index, new java.sql.Date(d.getTime()));
            } else {
                ps.setTimestamp(index, new Timestamp(d.getTime()));
            }
        }
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/Date.class/*RIGHTPAR*/;
    }
}

