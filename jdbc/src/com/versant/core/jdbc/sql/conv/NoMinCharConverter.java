
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
import com.versant.core.metadata.MDStatics;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws clause
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * This is a converter for databases that do not handle \u0000
 * (Character.MIN_VALUE) propertly (e.g. Postgres and Informix). A char with
 * value \u0000 must set as an empty String.
 *
 * @keep-all
 */
public class NoMinCharConverter extends JdbcConverterBase {
    private char defaultChar;
    public static class Factory extends NoArgJdbcConverterFactory {

        private NoMinCharConverter converter;

        /**
         * Create a converter for col using props as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new NoMinCharConverter();
            return converter;
        }

    }

    /**
     * Get the value of col from rs at position index.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        String s = rs.getString(index);
        if (s == null || s.length() == 0) {
            if (rs.wasNull()) {
                if (col.javaTypeCode == MDStatics.CHAR) {
                    return new Character(defaultChar);
                } else {
                    return null;
                }
            } else {
                return new Character(defaultChar);
            }
        }
        return new Character(s.charAt(0));
    }

    /**
     * Set parameter index on ps to value (for col).
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        if (value == null) {
            ps.setNull(index, col.jdbcType);
        } else {
            Character c = (Character)value;
            char cv = c.charValue();
            String s = cv == Character.MIN_VALUE ? "" : new String(new char[]{cv});
            ps.setString(index, s);
        }
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/Character.class/*RIGHTPAR*/;
    }

}

