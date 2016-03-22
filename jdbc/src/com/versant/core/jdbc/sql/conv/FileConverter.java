
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

import javax.jdo.JDOFatalDataStoreException;	//todo: appears only in throws clause
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.io.File;

/**
 * This converter converts java.io.File objects to and from SQL. It assumes
 * that the File is stored in a column compatible with ResultSet.getString and
 * PreparedStatement.setString.
 * @keep-all
 */
public class FileConverter extends TypeAsStringConverterBase {

    public static class Factory extends NoArgJdbcConverterFactory {

        private FileConverter converter;

        /**
         * Create a converter for col using args as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new FileConverter();
            return converter;
        }

    }

    /**
     * Create an instance of our type from a String.
     * @param s String to use (never null)
     */
    protected Object fromString(String s) {
        return new File(s);
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/File.class/*RIGHTPAR*/;
    }

}

