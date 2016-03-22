
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
import java.io.*;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws-clause

import com.versant.core.common.BindingSupportImpl;

/**
 * This converter converts Strings stored in TEXT or LONGVARCHAR columns to
 * and from SQL using rs.getCharacterStream and ps.setCharacterStream.
 * @keep-all
 */
public class CharacterStreamConverter extends JdbcConverterBase {

    public static class Factory extends NoArgJdbcConverterFactory {

        private CharacterStreamConverter converter;

        /**
         * Create a converter for col using args as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new CharacterStreamConverter();
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
        try {
            Reader in = rs.getCharacterStream(index);
            if (in == null) return null;
            return StreamUtils.readAll(in);
        } catch (IOException x) {
            throw BindingSupportImpl.getInstance().fatalDatastore(
                "Error reading " + col + ": " +
                x.getClass().getName() + ": " + x.getMessage(), x);
        }
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
            return;
        }
        String s = (String)value;
        ps.setCharacterStream(index, new StringReader(s), s.length());
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/String.class/*RIGHTPAR*/;
    }

}

