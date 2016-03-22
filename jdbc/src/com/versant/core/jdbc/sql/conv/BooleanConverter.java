
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
import com.versant.core.metadata.MDStatics;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws clause
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Types;

/**
 * This converter converts boolean's and Boolean's to and from SQL. It assumes
 * that the value is stored in a column compatible with ResultSet.getInt and
 * PreparedStatement.setInt. The value is stored 0 or 1.
 */
public class BooleanConverter extends JdbcConverterBase {

    public static class Factory extends NoArgJdbcConverterFactory {

        private BooleanConverter converter;

        /**
         * Create a converter for col using props as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new BooleanConverter();
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
        int i = rs.getInt(index);
        if (rs.wasNull()){
            if (col.javaTypeCode == MDStatics.BOOLEAN){
                return Boolean.FALSE;

            } else {
                return null;
            }
        }

        return i == 0 ? Boolean.FALSE : Boolean.TRUE;
    }




    /**
     * Set parameter index on ps to value (for col).
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if value is invalid
     */
    public void set(PreparedStatement ps, int index, JdbcColumn col, Object value)
            throws SQLException, JDOFatalDataStoreException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {

                boolean v = ((Boolean)value).booleanValue();
                ps.setInt(index, v ? 1 : 0);

        }
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/Boolean.class/*RIGHTPAR*/;
    }

}

