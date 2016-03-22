
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

import java.net.URL;
import java.net.MalformedURLException;

import com.versant.core.common.BindingSupportImpl;

/**
 * This converter converts java.net.URL objects to and from SQL. It assumes
 * that the File is stored in a column compatible with ResultSet.getString and
 * PreparedStatement.setString.
 * @keep-all
 */
public class URLConverterTrim extends TypeAsTrimStringConverterBase {

    public static class Factory extends NoArgJdbcConverterFactory {

        private URLConverterTrim converter;

        /**
         * Create a converter for col using args as parameters. Return null if
         * no converter is required.
         */
        public JdbcConverter createJdbcConverter(JdbcColumn col, Object args,
                JdbcTypeRegistry jdbcTypeRegistry) {
            if (converter == null) converter = new URLConverterTrim();
            return converter;
        }

    }

    /**
     * Create an instance of our type from a String.
     * @param s String to use (never null)
     */
    protected Object fromString(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw BindingSupportImpl.getInstance().fatalDatastore(e.toString(), e);
        }
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {
        return /*CHFC*/URL.class/*RIGHTPAR*/;
    }

}

