
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

import javax.jdo.JDOFatalDataStoreException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;

/**
 * This converter converts byte[] to and from SQL. It assumes the data is
 * accessable using ResultSet.getBytes and PreparedStatement.setBytes.
 * This is special Converter for mssql that handles null blobs as Binary. 
 */
public class NullBytesAsBinaryConverter extends JdbcConverterBase {

    public static class Factory extends NoArgJdbcConverterFactory {

        private NullBytesAsBinaryConverter asBinaryConverter;

        /**
         * Create a converter for col using props as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (asBinaryConverter == null) asBinaryConverter = new NullBytesAsBinaryConverter();
            return asBinaryConverter;
        }

    }

    /**
     * Get the value of col from rs at position index.
     * @exception java.sql.SQLException on SQL errors
     * @exception javax.jdo.JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        return rs.getBytes(index);
    }

    /**
     * Set parameter index on ps to value (for col).
     * @exception java.sql.SQLException on SQL errors
     * @exception javax.jdo.JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        if (value == null) {
            if (col.jdbcType == Types.BLOB) {
                ps.setNull(index, Types.BINARY);
            } else {
                ps.setNull(index, col.jdbcType);
            }
        } else {
            ps.setBytes(index, (byte[])value);
        }
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/byte[].class/*RIGHTPAR*/;
    }

}

