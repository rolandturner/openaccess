
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

import com.versant.core.util.*;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.JdbcConverterFactoryRegistry;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is a rule mapping a field name, java type and database name to
 * column properties. These rules can be set at class level (on a per
 * field basis) and at datastore level.
 *
 * @see JdbcMappingResolver
 */
public class JdbcJavaTypeMapping {

    public static final int NOT_SET = 0;
    public static final int FALSE = 1;
    public static final int TRUE = 2;

    private Class javaType;
    private String database;
    private String columnName;
    private int jdbcType;
    private String sqlType;
    private int length = -1;
    private int scale = -1;
    private int nulls = NOT_SET;
    private int equalityTest = NOT_SET;
    private JdbcConverterFactory converterFactory;
    private int enabled = NOT_SET;
    private int shared = NOT_SET;

    public JdbcJavaTypeMapping() {
    }

    public JdbcJavaTypeMapping(Class javaType, int jdbcType, int length,
            int scale, int nulls, JdbcConverterFactory converterFactory) {
        this.javaType = javaType;
        this.jdbcType = jdbcType;
        this.length = length;
        this.scale = scale;
        this.nulls = nulls;
        this.equalityTest = TRUE;
        this.converterFactory = converterFactory;
        enabled = TRUE;
    }

    public JdbcJavaTypeMapping(Class javaType, int jdbcType, int length,
            int scale, int nulls, JdbcConverterFactory converterFactory,
            boolean enabled) {
        this(javaType, jdbcType, length, scale, nulls, converterFactory);
        this.enabled = enabled ? TRUE : FALSE;
    }

    public JdbcJavaTypeMapping(Class javaType, int jdbcType, boolean nulls,
            boolean equalityTest) {
        this(javaType, jdbcType, nulls);
        this.equalityTest = equalityTest ? TRUE : FALSE;
        enabled = TRUE;
    }

    public JdbcJavaTypeMapping(Class javaType, int jdbcType, boolean nulls) {
        this.javaType = javaType;
        this.jdbcType = jdbcType;
        this.nulls = nulls ? TRUE : FALSE;
        enabled = TRUE;
    }

    public JdbcJavaTypeMapping(Class javaType, int jdbcType,
            JdbcConverterFactory converterFactory) {
        this.javaType = javaType;
        this.jdbcType = jdbcType;
        this.nulls = TRUE;
        this.equalityTest = FALSE;
        this.converterFactory = converterFactory;
        enabled = TRUE;
    }

    public JdbcJavaTypeMapping(Class javaType, int jdbcType,
            JdbcConverterFactory converterFactory, boolean enabled) {
        this(javaType, jdbcType, converterFactory);
        this.enabled = enabled ? TRUE : FALSE;
    }

    public Class getJavaType() {
        return javaType;
    }

    public void setJavaType(Class javaType) {
        this.javaType = javaType;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
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

    public void setNulls(boolean nulls) {
        this.nulls = nulls ? TRUE : FALSE;
    }

    public int getEqualityTest() {
        return equalityTest;
    }

    public void setEqualityTest(int equalityTest) {
        this.equalityTest = equalityTest;
    }

    public void setEqualityTest(boolean equalityTest) {
        this.equalityTest = equalityTest ? TRUE : FALSE;
    }

    public JdbcConverterFactory getConverterFactory() {
        return converterFactory;
    }

    public void setConverterFactory(JdbcConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled ? TRUE : FALSE;
    }

    public int getShared() {
        return shared;
    }

    public void setShared(int shared) {
        this.shared = shared;
    }

    public void setShared(boolean on) {
        this.shared = on ? TRUE : FALSE;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        if (javaType != null) s.append("javaType: " + javaType + " ");
        if (database != null) s.append("database: " + database + " ");
        if (jdbcType != 0) s.append("jdbcType: " + JdbcTypes.toString(jdbcType) + " ");
        if (sqlType != null) s.append("sqlType: " + sqlType + " ");
        if (length >= 0) s.append("length: " + length + " ");
        if (scale >= 0) s.append("scale: " + scale + " ");
        if (nulls != NOT_SET) s.append("nulls: " + (nulls == TRUE) + " ");
        if (equalityTest != NOT_SET) s.append("equalityTest: " + (equalityTest == TRUE) + " ");
        if (converterFactory != null) s.append("converterFactory: " + converterFactory);
        if (enabled != NOT_SET) s.append("enabled: " + (enabled == TRUE) + " ");
        if (shared != NOT_SET) s.append("shared: " + (shared == TRUE) + " ");
        return s.toString();
    }

    /**
     * Do the parameters match this rule?
     * @param database Database name
     * @param fieldName Field name (may be null)
     * @param javaType Java type name
     */
    public boolean match(String database, String fieldName, Class javaType) {
        return (this.database == null || this.database.equals(database))
            && (this.javaType == null || this.javaType == javaType);
    }

    /**
     * Copy in fields from another rule. Only fields not set in this rule
     * are changed. If r is a composite rule then the extra mappings are
     * duplicated and hooked up to this rule.
     */
    public void copyFrom(JdbcJavaTypeMapping r) {
        if (javaType == null) javaType = r.javaType;
        if (database == null) database = r.database;
        if (columnName == null) columnName = r.columnName;
        if (jdbcType == 0) jdbcType = r.jdbcType;
        if (sqlType == null) sqlType = r.sqlType;
        if (length == -1) length = r.length;
        if (scale == -1) scale = r.scale;
        if (nulls == NOT_SET) nulls = r.nulls;
        if (equalityTest == NOT_SET) equalityTest = r.equalityTest;
        if (converterFactory == null) converterFactory = r.converterFactory;
        if (enabled == NOT_SET) enabled = r.enabled;
        if (shared == NOT_SET) shared = r.shared;
    }

    /**
     * Copy in fields from a type rule. Only fields not set in this rule
     * are changed.
     */
    public void copyFrom(JdbcTypeMapping r) {
        if (sqlType == null) sqlType = r.getSqlType();
        if (length == -1) length = r.getLength();
        if (scale == -1) scale = r.getScale();
        if (nulls == NOT_SET) nulls = r.getNulls();
        if (equalityTest == NOT_SET) equalityTest = r.getEqualityTest();
        if (converterFactory == null) converterFactory = r.getConverterFactory();
    }

    private int nextTriState(StringListParser p, String name) {
        String s = p.nextQuotedString();
        if (s == null) return NOT_SET;
        else if (s.equals("true")) return TRUE;
        else if (s.equals("false")) return FALSE;
        else throw BindingSupportImpl.getInstance().runtime("Invalid " + name + " setting: '" + s + "'");
    }

    /**
     * Parse from p.
     */
    public void parse(StringListParser p, JdbcConverterFactoryRegistry jcfreg) {
        try {
            javaType = p.nextClass(jcfreg.getLoader());
        } catch (ClassNotFoundException e) {
            throw BindingSupportImpl.getInstance().runtime(
                "Unable to load class: " + e.getMessage() , e);
        }
        database = p.nextQuotedString();
        String s = p.nextQuotedString();
        jdbcType = s == null ? 0 : JdbcTypes.parse(s);
        sqlType = p.nextQuotedString();
        length = getInt(p);
        scale = getInt(p);
        nulls = nextTriState(p, "nulls");
        equalityTest = nextTriState(p, "equalityTest");
        s = p.nextQuotedString();
        if (s != null) converterFactory = jcfreg.getFactory(s);
        if (p.hasNext()) enabled = nextTriState(p, "enabled");
    }

    private int getInt(StringListParser p) {
        String s = p.nextQuotedString();
        if (s == null) return -1;
        return Integer.parseInt(s);
    }

}

