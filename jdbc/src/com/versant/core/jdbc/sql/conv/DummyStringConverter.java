
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

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws clause
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.io.File;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This a dummy converter that just uses ResultSet.getString and
 * PreparedStatement.setString to read and write Strings.
 *
 * @keep-all
 */
public class DummyStringConverter extends JdbcConverterBase {

    public static final DummyStringConverter INSTANCE = new DummyStringConverter(); 

    public Object get(ResultSet rs, int index, JdbcColumn col)
            throws SQLException, JDOFatalDataStoreException {
        return rs.getString(index);
    }

    public void set(PreparedStatement ps, int index, JdbcColumn col,
            Object value) throws SQLException, JDOFatalDataStoreException {
        ps.setString(index, (String)value);
    }

    public Class getValueType() {
        return /*CHFC*/String.class/*RIGHTPAR*/;
    }

}

