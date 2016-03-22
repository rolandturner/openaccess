
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
import com.versant.core.jdbc.metadata.JdbcTypes;
import com.versant.core.jdbc.metadata.JdbcTypeMapping;
import com.versant.core.jdbc.metadata.JdbcJavaTypeMapping;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.util.StringList;

/**
 * Wrapper around a JdbcJavaTypeMapping. The properties are resolved
 * as follows:<p>
 * <p/>
 * <code>
 * If the value in this class is not null then return that.
 * If this is the jdbcType property:
 * If this is a database specific rule (i.e. database is not null) then
 * delegate to the matching all databases rule.
 * Else delegate to the mapping returned by the SqlDriver in use.
 * Else (sqlType, length etc):
 * If a jdbcType has been specified then delegate to the matching
 * MdJdbcTypeMapping from the datastore.
 * Else
 * If this is a database specific rule (i.e. database is not null) then
 * delegate to the matching all databases rule.
 * Else if the mapping returned by the SqlDriver has a value return it
 * Else delegate to the datastore type mapping for the sqlDriver mapping
 * jdbcType
 * </code>
 *
 * @keep-all
 * @see JdbcJavaTypeMapping
 * @see JdbcTypeMapping
 */
public class MdJdbcJavaTypeMapping implements Comparable {

    private MdDataStore ds;

    private String javaType;
    private String database;
    private String jdbcType;
    private int jdbcTypeCode;
    private String sqlType;
    private String length;
    private String scale;
    private String nulls;
    private String equalityTest;
    private String converter;
    private String enabled;

    private String javaTypePackageLast;
    private boolean empty;
    private boolean custom;

    public MdJdbcJavaTypeMapping(MdDataStore ds) {
        this.ds = ds;
        empty = true;
    }

    private void changed() {
        empty = sqlType == null && length == null && scale == null
                && nulls == null && equalityTest == null && converter == null
                && enabled == null && jdbcType == null;
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

    public String getJavaType() {
        return javaType;
    }

    public String getJavaTypePackageLast() {
        return javaTypePackageLast;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
        if (javaType != null) {
            javaTypePackageLast = MdUtils.putClassNameFirst(javaType);
        } else {
            javaType = null;
        }
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Get the driver mapping rule for our java type.
     */
    private JdbcJavaTypeMapping getDriverMapping() {
        return ds.lookupDriverJavaTypeMapping(javaType);
    }

    /**
     * Get the all databases rule for our java type.
     */
    private MdJdbcJavaTypeMapping getAllMapping() {
        return ds.getJavaTypeMapping(javaType, null);
    }

    /**
     * Get the rule for our JDBC type or null if none i.e. no or invalid
     * jdbcType specified.
     */
    private MdJdbcTypeMapping getCustomTypeMapping() {
        if (jdbcTypeCode == 0) return null;
        return ds.getTypeMapping(jdbcTypeCode, database);
    }

    public String getJdbcTypeStr() {
        if (jdbcType == null) return getDefJdbcType();
        return jdbcType;
    }

    private String getDefJdbcType() {
        if (database != null) return getAllMapping().getJdbcTypeStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) return JdbcTypes.toString(dm.getJdbcType());
        return null;
    }

    public MdValue getJdbcType() {
        MdValue v = createMdValue(jdbcType);
        v.setDefText(getDefJdbcType());
        v.setPickList(PickLists.JDBC_TYPE);
        return v;
    }

    public void setJdbcType(MdValue v) {
        setJdbcType(v.getText());
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
        if (jdbcType == null) {
            jdbcTypeCode = 0;
        } else {
            try {
                jdbcTypeCode = JdbcTypes.parse(jdbcType);
            } catch (IllegalArgumentException e) {
                jdbcTypeCode = 0;
                ds.getProject().getLogger().error(e);
            }
        }
        changed();
    }

    public int getJdbcTypeCode() {
        return jdbcTypeCode;
    }

    public String getSqlTypeStr() {
        if (sqlType == null) return getDefSqlType();
        return sqlType;
    }

    private String getDefSqlType() {
        MdJdbcTypeMapping tm = getCustomTypeMapping();
        if (tm != null) return tm.getSqlTypeStr();
        if (database != null) return getAllMapping().getSqlTypeStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            String s = dm.getSqlType();
            if (s != null) return s;
            tm = ds.getTypeMapping(dm.getJdbcType(), ds.getSelectedDBImp());
            return tm.getSqlTypeStr();
        }
        return null;
    }

    public MdValue getSqlType() {
        MdValue v = createMdValue(sqlType);
        v.setDefText(getDefSqlType());
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
        MdJdbcTypeMapping tm = getCustomTypeMapping();
        if (tm != null) return tm.getLengthStr();
        if (database != null) return getAllMapping().getLengthStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            int len = dm.getLength();
            if (len != -1) return Integer.toString(len);
            tm = ds.getTypeMapping(dm.getJdbcType(), ds.getSelectedDBImp());
            return tm.getLengthStr();
        }
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
        MdJdbcTypeMapping tm = getCustomTypeMapping();
        if (tm != null) return tm.getScaleStr();
        if (database != null) return getAllMapping().getScaleStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            int sc = dm.getScale();
            if (sc != -1) return Integer.toString(sc);
            tm = ds.getTypeMapping(dm.getJdbcType(), ds.getSelectedDBImp());
            return tm.getScaleStr();
        }
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

    public String getNullsStr() {
        if (nulls == null) return getDefNulls();
        return nulls;
    }

    private String getDefNulls() {
        MdJdbcTypeMapping tm = getCustomTypeMapping();
        if (tm != null) return tm.getNullsStr();
        if (database != null) return getAllMapping().getNullsStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            switch (dm.getNulls()) {
                case JdbcJavaTypeMapping.FALSE:
                    return "false";
                case JdbcJavaTypeMapping.TRUE:
                    return "true";
            }
            tm = ds.getTypeMapping(dm.getJdbcType(), ds.getSelectedDBImp());
            return tm.getNullsStr();
        }
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
        if (equalityTest == null) return getDefEqualityTest();
        return equalityTest;
    }

    private String getDefEqualityTest() {
        MdJdbcTypeMapping tm = getCustomTypeMapping();
        if (tm != null) return tm.getEqualityTestStr();
        if (database != null) return getAllMapping().getEqualityTestStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            switch (dm.getEqualityTest()) {
                case JdbcJavaTypeMapping.FALSE:
                    return "false";
                case JdbcJavaTypeMapping.TRUE:
                    return "true";
            }
            tm = ds.getTypeMapping(dm.getJdbcType(), ds.getSelectedDBImp());
            return tm.getEqualityTestStr();
        }
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

    public String getEnabledStr() {
        if (enabled == null) return getDefEnabled();
        return enabled;
    }

    private String getDefEnabled() {
        if (database != null) return getAllMapping().getEnabledStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            switch (dm.getEnabled()) {
                case JdbcJavaTypeMapping.FALSE:
                    return "false";
                case JdbcJavaTypeMapping.TRUE:
                    return "true";
            }
        }
        return null;
    }

    public MdValue getEnabled() {
        MdValue v = createMdValue(enabled);
        v.setDefText(getDefEnabled());
        v.setPickList(PickLists.BOOLEAN);
        return v;
    }

    public void setEnabled(MdValue v) {
        enabled = v.getText();
        changed();
    }

    public boolean getEnabledBool() {
        if (enabled != null) return enabled.equals("true");
        String def = getDefEnabled();
        return def == null || def.equals("true");
    }

    public String getConverterStr() {
        if (converter == null) return getDefConverter();
        return converter;
    }

    private String getDefConverter() {
        MdJdbcTypeMapping tm = getCustomTypeMapping();
        if (tm != null) return tm.getConverterStr();
        if (database != null) return getAllMapping().getConverterStr();
        JdbcJavaTypeMapping dm = getDriverMapping();
        if (dm != null) {
            JdbcConverterFactory c = dm.getConverterFactory();
            if (c != null) return c.getClass().getName();
            tm = ds.getTypeMapping(dm.getJdbcType(), ds.getSelectedDBImp());
            return tm.getConverterStr();
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
        String s = v.getText();
        if (s != null && s.length() == 0) s = null;
        converter = s;
        changed();
    }

    /**
     * Is this a mapping for a custom data type?
     */
    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    /**
     * Write to s.
     */
    public void write(StringList s) {
        s.append(javaType);
        s.appendQuoted(database);
        s.appendQuoted(jdbcType);
        s.appendQuoted(sqlType);
        s.appendQuoted(length);
        s.appendQuoted(scale);
        s.appendQuoted(nulls);
        s.appendQuoted(equalityTest);
        s.appendQuoted(converter);
        if (enabled != null) s.appendQuoted(enabled);
    }

    /**
     * Read from p.
     */
    public void read(StringListParser p) {
        setJavaType(p.nextString());
        database = p.nextQuotedString();
        setJdbcType(p.nextQuotedString());
        sqlType = p.nextQuotedString();
        length = p.nextQuotedString();
        scale = p.nextQuotedString();
        nulls = p.nextQuotedString();
        equalityTest = p.nextQuotedString();
        converter = p.nextQuotedString();
        if (p.hasNext()) enabled = p.nextQuotedString();
        changed();
    }

    /**
     * Order by javaType then database.
     */
    public int compareTo(Object o) {
        MdJdbcJavaTypeMapping m = (MdJdbcJavaTypeMapping)o;
        if (!m.custom && custom) return -1;
        if (m.custom && !custom) return +1;
        int c = javaTypePackageLast.compareTo(m.javaTypePackageLast);
        if (c != 0) return c;
        if (database == null) {
            if (m.database == null) return 0;
            return -1;
        } else {
            if (m.database == null) return 1;
            return database.compareTo(m.database);
        }
    }

    public Key getKey() {
        return new Key(javaType, database);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        if (javaType != null) s.append("javaType: " + javaType + " ");
        if (database != null) s.append("database: " + database + " ");
        if (jdbcType != null) s.append("jdbcType: " + jdbcType + " ");
        if (sqlType != null) s.append("sqlType: " + sqlType + " ");
        if (length != null) s.append("length: " + length + " ");
        if (scale != null) s.append("scale: " + scale + " ");
        if (nulls != null) s.append("nulls: " + nulls + " ");
        if (converter != null) s.append("converter: " + converter + " ");
        if (equalityTest != null) {
            s.append("equalityTest: " + equalityTest + " ");
        }
        if (equalityTest != null) s.append("custom: " + custom);
        return s.toString();
    }

    /**
     * This is a key class for looking these up by database and javaType
     * in HashMaps etc.
     */
    public static class Key {

        private String javaType;
        private String database;

        public Key(String javaType, String database) {
            this.javaType = javaType;
            this.database = database;
        }

        public int hashCode() {
            int hc = javaType.hashCode();
            if (database != null) hc += database.hashCode();
            return hc;
        }

        public boolean equals(Object o) {
            Key k = (Key)o;
            if (!javaType.equals(k.javaType)) return false;
            if (database == null) return k.database == null;
            return k.database != null && k.database.equals(database);
        }

        public String getJavaType() {
            return javaType;
        }

        public String getDatabase() {
            return database;
        }

        public String toString() {
            return javaType + " " + database;
        }

    }

}

