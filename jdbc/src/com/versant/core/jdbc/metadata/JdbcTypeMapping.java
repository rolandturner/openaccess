
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
package com.versant.core.jdbc.metadata;

import com.versant.core.util.StringListParser;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.JdbcConverterFactoryRegistry;
import com.versant.core.jdbc.JdbcConverter;
import com.versant.core.jdbc.JdbcTypeRegistry;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a rule mapping a JDBC type code from java.sql.Types and optional
 * database type name to column properties. The SqlDriver class for the
 * datastore provides a list of these to do basic mapping. The datastore
 * may provide additional rules to override the driver supplied rules.
 *
 * @see JdbcMappingResolver
 */
public class JdbcTypeMapping implements Cloneable {

    public static final int NOT_SET = 0;
    public static final int FALSE = 1;
    public static final int TRUE = 2;

    private String database;
    private int jdbcType;
    private String sqlType;
    private int length = -1;
    private int scale = -1;
    private int nulls = NOT_SET;
    private int equalityTest = NOT_SET;
    private JdbcConverterFactory converterFactory;

    public JdbcTypeMapping() {
    }

    public JdbcTypeMapping(String sqlType, int length, int scale,
            int nulls, int equalityTest, JdbcConverterFactory converter) {
        this.sqlType = sqlType;
        this.length = length;
        this.scale = scale;
        this.nulls = nulls;
        this.equalityTest = equalityTest;
        this.converterFactory = converter;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getNulls() {
        return nulls;
    }

    public void setNulls(int nulls) {
        this.nulls = nulls;
    }

    public int getEqualityTest() {
        return equalityTest;
    }

    public void setEqualityTest(int equalityTest) {
        this.equalityTest = equalityTest;
    }

    public JdbcConverterFactory getConverterFactory() {
        return converterFactory;
    }

    public void setConverterFactory(JdbcConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        if (database != null) s.append("database: " + database + " ");
        if (jdbcType != 0) {
            s.append("jdbcType: " + JdbcTypes.toString(jdbcType) + " ");
        }
        if (sqlType != null) s.append("sqlType: " + sqlType + " ");
        if (length >= 0) s.append("length: " + length + " ");
        if (scale >= 0) s.append("scale: " + scale + " ");
        if (nulls != NOT_SET) s.append("nulls: " + (nulls == TRUE) + " ");
        if (equalityTest != NOT_SET) {
            s.append("equalityTest: " + (equalityTest == TRUE) + " ");
        }
        if (converterFactory != null) {
            s.append("converterFactory: " + converterFactory);
        }
        return s.toString();
    }

    private int nextTriState(StringListParser p, String name) {
        String s = p.nextQuotedString();
        if (s == null) {
            return NOT_SET;
        } else if (s.equals("true")) {
            return TRUE;
        } else if (s.equals("false")) {
            return FALSE;
        } else {
            throw BindingSupportImpl.getInstance().runtime(
                    "Invalid " + name + " setting: '" + s + "'");
        }
    }

    /**
     * Parse from p.
     */
    public void parse(StringListParser p, JdbcConverterFactoryRegistry jcfreg) {
        jdbcType = JdbcTypes.parse(p.nextString());
        database = p.nextQuotedString();
        sqlType = p.nextQuotedString();
        length = getInt(p);
        scale = getInt(p);
        nulls = nextTriState(p, "nulls");
        equalityTest = nextTriState(p, "equalityTest");
        String s = p.nextQuotedString();
        if (s != null) converterFactory = jcfreg.getFactory(s);
    }

    private int getInt(StringListParser p) {
        String s = p.nextQuotedString();
        if (s == null) return -1;
        return Integer.parseInt(s);
    }

    /**
     * Do the parameters match this rule?
     *
     * @param jdbcType JDBC type code from java.sql.Types
     * @param database Database name
     */
    public boolean match(int jdbcType, String database) {
        return this.jdbcType == jdbcType
                && (database == null
                || this.database == null
                || this.database.equals(database));
    }

    /**
     * Copy in fields from another rule. Only fields not set in this rule
     * are changed.
     */
    public void copyFrom(JdbcTypeMapping r) {
        if (database == null) database = r.database;
        if (jdbcType == 0) jdbcType = r.jdbcType;
        if (sqlType == null) sqlType = r.sqlType;
        if (length == -1) length = r.length;
        if (scale == -1) scale = r.scale;
        if (nulls == NOT_SET) nulls = r.nulls;
        if (equalityTest == NOT_SET) equalityTest = r.equalityTest;
        if (converterFactory == null) converterFactory = r.converterFactory;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("clone() failed?");
        }
    }

}
