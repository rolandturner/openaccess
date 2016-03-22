
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

import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.metadata.JdbcColumn;

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws clause
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * <p>This is a base class for converters that convert some type to/from String
 * and store it in a column compatible with getString/setString. Subclasses
 * must implement:</p>
 *
 * <ul>
 * <li>{@link #fromString(String) }
 * <li>{@link #getValueType() }
 * </ul>
 *
 * <p>Subclasses may also implement {@link #toString(Object) } if the
 * toString method of the value type itself is not suitable. A
 * {@link JdbcConverterFactory} must also be written.</p>
 *
 * @keep-all
 */
public abstract class TypeAsStringConverterBase extends JdbcConverterBase {

    /**
     * Get the value of col from rs at position index.
     * @exception SQLException on SQL errors
     * @exception JDOFatalDataStoreException if the ResultSet value is invalid
     */
    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        String s = rs.getString(index);
        if (s == null) return null;
        return fromString(s);
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
            ps.setString(index, toString(value));
        }
    }

    /**
     * Create an instance of our type from a String.
     * @param s String to use (never null)
     */
    protected abstract Object fromString(String s);

    /**
     * Convert an instance of our type to a String.
     * @param value Value to convert (never null)
     */
    protected String toString(Object value) {
        return value.toString();
    }

}

