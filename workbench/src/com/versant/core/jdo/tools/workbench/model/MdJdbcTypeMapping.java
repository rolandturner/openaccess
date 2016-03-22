
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
package com.versant.core.jdo.tools.workbench.model;

import com.versant.core.util.StringListParser;
import com.versant.core.util.StringList;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.metadata.JdbcTypes;
import com.versant.core.jdbc.metadata.JdbcTypeMapping;

/**
 * Wrapper around a JdbcTypeMapping. The properties are resolved using the
 * following order of precedence:<p>
 *
 * If the value in this class is not null then return that.<br>
 * If this is a database specific rule (i.e. database is not null) then
 * delegate to the matching all databases rule.<br>
 * Else delegate to the mapping returned by the SqlDriver in use.<p>
 *
 * @see JdbcTypeMapping
 * @keep-all
 */
public class MdJdbcTypeMapping implements Comparable {

    private MdDataStore ds;

    private String database;
    private int jdbcType;
    private String sqlType;
    private String length;
    private String scale;
    private String nulls;
    private String equalityTest;
    private String converter;

    private boolean empty;

    public MdJdbcTypeMapping(MdDataStore ds) {
        this.ds = ds;
        empty = true;
    }

    private void changed() {
        empty = sqlType == null && length == null && scale == null
            && nulls == null && equalityTest == null && converter == null;
    }

    private MdValue createMdValue(String s) {
        return new MdValue(s);
    }

    private MdValue createMdClassNameValue(String s) {
        return new MdClassNameValue(s);
    }

    private MdValueInt createMdValueInt(String s) {
        return new MdValueInt(s);
    }

    public boolean isEmpty() {
        return empty;
    }

    /**
     * Get the driver mapping rule for our JDBC type.
     */
    private JdbcTypeMapping getDriverMapping() {
        return ds.lookupDriverTypeMapping(jdbcType);
    }

    /**
     * Get the all databases rule for our JDBC type.
     */
    private MdJdbcTypeMapping getAllMapping() {
        return ds.getTypeMapping(jdbcType, null);
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

    public String getJdbcTypeStr() {
        return JdbcTypes.toString(jdbcType);
    }

    public String getSqlTypeStr() {
        if (sqlType == null) return getDefSqlType();
        return sqlType;
    }

    private String getDefSqlType() {
        if (database != null) return getAllMapping().getSqlTypeStr();
        JdbcTypeMapping dm = getDriverMapping();
        if (dm != null) return dm.getSqlType();
        return null;
    }

    public MdValue getSqlType() {
        MdValue v = createMdValue(sqlType);
        v.setDefText(getDefSqlType());
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setSqlType(MdValue v) {
        sqlType = v.getText();
        changed();
    }

    public String getLengthStr() {
        if (length == null) return getDefLength();
        return length;
    }

    private String getDefLength() {
        if (database != null) return getAllMapping().getLengthStr();
        JdbcTypeMapping dm = getDriverMapping();
        if (dm != null) return Integer.toString(dm.getLength());
        return null;
    }

    public MdValue getLength() {
        MdValueInt v = createMdValueInt(length);
        v.setDefText(getDefLength());
        return v;
    }

    public void setLength(MdValue v) {
        length = v.getText();
        changed();
    }

    public String getScaleStr() {
        if (scale == null) return getDefScale();
        return scale;
    }

    private String getDefScale() {
        if (database != null) return getAllMapping().getScaleStr();
        JdbcTypeMapping dm = getDriverMapping();
        if (dm != null) return Integer.toString(dm.getScale());
        return null;
    }

    public MdValue getScale() {
        MdValueInt v = createMdValueInt(scale);
        v.setDefText(getDefScale());
        return v;
    }

    public void setScale(MdValue v) {
        scale = v.getText();
        changed();
    }

    private String triStateToStr(int v) {
        switch (v) {
            case JdbcTypeMapping.FALSE:   return "false";
            case JdbcTypeMapping.TRUE:    return "true";
        }
        return "?";
    }

    public String getNullsStr() {
        if (nulls == null && getDriverMapping() != null) {
            return triStateToStr(getDriverMapping().getNulls());
        }
        return nulls;
    }

    private String getDefNulls() {
        if (database != null) return getAllMapping().getNullsStr();
        JdbcTypeMapping dm = getDriverMapping();
        if (dm != null) return triStateToStr(dm.getNulls());
        return null;
    }

    public MdValue getNulls() {
        MdValue v = createMdValue(nulls);
        v.setDefText(getDefNulls());
        v.setPickList(PickLists.BOOLEAN);
        return v;
    }

    public void setNulls(MdValue v) {
        nulls = v.getText();
        changed();
    }

    public String getEqualityTestStr() {
        if (equalityTest == null && getDriverMapping() != null) {
            return triStateToStr(getDriverMapping().getEqualityTest());
        }
        return equalityTest;
    }

    private String getDefEqualityTest() {
        if (database != null) return getAllMapping().getEqualityTestStr();
        JdbcTypeMapping dm = getDriverMapping();
        if (dm != null) return triStateToStr(dm.getEqualityTest());
        return null;
    }

    public MdValue getEqualityTest() {
        MdValue v = createMdValue(equalityTest);
        v.setDefText(getDefEqualityTest());
        v.setPickList(PickLists.BOOLEAN);
        return v;
    }

    public void setEqualityTest(MdValue v) {
        equalityTest = v.getText();
        changed();
    }

    public String getConverterStr() {
        if (converter == null && getDriverMapping() != null) {
            JdbcConverterFactory c = getDriverMapping().getConverterFactory();
            if (c != null) return c.getClass().getName();
        }
        return converter;
    }

    private String getDefConverter() {
        if (database != null) return getAllMapping().getConverterStr();
        JdbcTypeMapping dm = getDriverMapping();
        if (dm != null) {
            JdbcConverterFactory c = dm.getConverterFactory();
            if (c != null) return c.getClass().getName();
        }
        return null;
    }

    public MdValue getConverter() {
        MdValue v = createMdClassNameValue(converter);
        v.setPickList(PickLists.JDBC_CONVERTER);
        v.setOnlyFromPickList(false);
        String def = getDefConverter();
        v.setDefText(def == null ? "none" : def);
        return v;
    }

    public void setConverter(MdValue v) {
        converter = v.getText();
        changed();
    }

    /**
     * Write to s.
     */
    public void write(StringList s) {
        s.append(JdbcTypes.toString(jdbcType));
        s.appendQuoted(database);
        s.appendQuoted(sqlType);
        s.appendQuoted(length);
        s.appendQuoted(scale);
        s.appendQuoted(nulls);
        s.appendQuoted(equalityTest);
        s.appendQuoted(converter);
    }

    /**
     * Read from p.
     */
    public void read(StringListParser p) {
        jdbcType = JdbcTypes.parse(p.nextString());
        database = p.nextQuotedString();
        sqlType = p.nextQuotedString();
        length = p.nextQuotedString();
        scale = p.nextQuotedString();
        nulls = p.nextQuotedString();
        equalityTest = p.nextQuotedString();
        converter = p.nextQuotedString();
        changed();
    }

    /**
     * Order by jdbcType then database.
     */
    public int compareTo(Object o) {
        MdJdbcTypeMapping m = (MdJdbcTypeMapping)o;
        int diff = jdbcType - m.jdbcType;
        if (diff != 0) return diff;
        if (database == null) {
            if (m.database == null) return 0;
            return -1;
        } else {
            if (m.database == null) return 1;
            return database.compareTo(m.database);
        }
    }

    public Key getKey() {
        return new Key(jdbcType, database);
    }

    /**
     * This is a key class for looking these up by database and jdbcType
     * in HashMaps etc.
     */
    public static class Key {

        private int jdbcType;
        private String database;

        public Key(int jdbcType, String database) {
            this.jdbcType = jdbcType;
            this.database = database;
        }

        public int hashCode() {
            if (database == null) return jdbcType;
            else return database.hashCode() + jdbcType;
        }

        public boolean equals(Object o) {
            Key k = (Key)o;
            if (jdbcType != k.jdbcType) return false;
            if (database == null) return k.database == null;
            return k.database != null && k.database.equals(database);
        }

    }

}

