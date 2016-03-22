
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

/**
 * This converter converts byte[] to and from SQL. It delegates to a nested
 * converter based on the JDBC type of the column.
 * @keep-all
 */
public class UByteArrayConverter extends TypeAsBytesConverterBase {

    public static class Factory extends TypeAsBytesConverterBase.Factory {

        protected JdbcConverter createConverter(JdbcConverter nested) {
            return new UByteArrayConverter(nested);
        }

    }

    public UByteArrayConverter(JdbcConverter nested) {
        super(nested);
    }

    /**
     * Convert a byte[] into an instance of our value class.
     */
    protected Object fromByteArray(byte[] buf) {

		return null;
    }

    /**
     * Convert an instance of our value class into a byte[].
     */
    protected byte[] toByteArray(Object value) {

		return null;
    }

    /**
     * Get the type of our expected value objects (e.g. java.util.Locale
     * for a converter for Locale's).
     */
    public Class getValueType() {

		return null;
    }

}

