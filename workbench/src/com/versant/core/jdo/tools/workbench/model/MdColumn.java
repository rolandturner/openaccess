
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

import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcTypes;
import com.versant.core.jdbc.metadata.JdbcTable;

import java.util.List;

/**
 * This wraps an extension for a jdbc-column element.
 *
 * @keep-all
 */
public final class MdColumn implements JdoExtensionKeys, GraphColumn {

    private MdElement element; // the jdbc-column extension
    private MdColumnOwner owner;
    private Defaults def;

    private JdbcColumn jdbcColumn;
    private GraphTable table;
    private String help;
    private boolean primaryKey;

    public MdColumn(MdElement element) {
        this.element = element;
    }

    public MdColumn() {
        this(XmlUtils.createExtension(JDBC_COLUMN, null));
    }

    public MdColumn(MdColumn c) {
        this((MdElement)c.getElement().clone());
    }

    public void init(MdDataStore store, MdElement container, JdbcColumn jdbcColumn,
            GraphTable table) {
        setElement(XmlUtils.findOrCreateColElement(container,
                store.getSelectedDBImp()));
        setJdbcColumn(jdbcColumn);
        setTable(table);
    }

    public MdElement getElement() {
        return element;
    }

    public void setElement(MdElement element) {
        this.element = element;
    }

    public MdColumnOwner getOwner() {
        return owner;
    }

    public void setOwner(MdColumnOwner owner) {
        this.owner = owner;
    }

    public JdbcColumn getJdbcColumn() {
        return jdbcColumn;
    }

    public void setJdbcColumn(JdbcColumn jdbcColumn) {
        this.jdbcColumn = jdbcColumn;
    }

    public Defaults getDef() {
        return def;
    }

    public void setDef(Defaults def) {
        this.def = def;
    }

    public String getDefName() {
        return jdbcColumn == null ? null : jdbcColumn.name;
    }

    public String getJdbcConverterStr() {
        return XmlUtils.getExtension(element, JDBC_CONVERTER);
    }

    public MdValue getJdbcConverter() {
        MdValue v = new MdClassNameValue(getJdbcConverterStr());
        v.setPickList(PickLists.JDBC_CONVERTER);
        v.setOnlyFromPickList(false);
        v.setDefText(def == null ? null : def.getConverter());
        return v;
    }

    public void setJdbcConverter(MdValue v) {
        XmlUtils.setExtension(element, JDBC_CONVERTER, v.getText());
    }

    public String getDb() {
        return element.getAttributeValue("value");
    }

    public void setDb(String db) {
        if (db == null) {
            element.removeAttribute("value");
        } else {
            element.setAttribute("value", db);
        }
        XmlUtils.makeDirty(element);
    }

    public String getNameStr() {
        return XmlUtils.getExtension(element, JDBC_COLUMN_NAME);
    }

    public MdValue getName() {
        MdValue v = new MdValue(getNameStr());
        v.setOnlyFromPickList(false);
        if(jdbcColumn != null){
            JdbcTable table = jdbcColumn.table;
            if(table != null){
                String tableName = table.name;
                DatabaseMetaData databaseMetaData = MdClass.getDatabaseMetaData();
                if (tableName != null && databaseMetaData != null) {
                    List allColumnNames = databaseMetaData.getAllColumnNames(tableName);
                    if(allColumnNames != null){
                        v.setCaseSensitive(false);
                        v.setWarningOnError(true);
                        v.setOnlyFromPickList(true);
                        v.setPickList(allColumnNames);
                    }
                }
            }
        }
        v.setDefText(getDefName());
        return v;
    }

    public void setName(MdValue v) {
        XmlUtils.setExtension(element, JDBC_COLUMN_NAME, v.getText());
    }

    public String getTypeStr() {
        return XmlUtils.getExtension(element, JDBC_TYPE);
    }

    public MdValue getType() {
        MdValue v = new MdValue(getTypeStr());
        v.setPickList(PickLists.JDBC_TYPE);
        v.setDefText(
                jdbcColumn == null ? null : JdbcTypes.toString(
                        jdbcColumn.jdbcType));
//        v.setDefText(def == null ? null : def.getType());
        return v;
    }

    public void setType(MdValue v) {
        XmlUtils.setExtension(element, JDBC_TYPE, v.getText());
        if (owner != null) owner.columnTypeChanged(this);
    }

    public String getSqlTypeStr() {
        return XmlUtils.getExtension(element, JDBC_SQL_TYPE);
    }

    public MdValue getSqlType() {
        MdValue v = new MdValue(getSqlTypeStr());
        v.setDefText(jdbcColumn == null ? null : jdbcColumn.sqlType);
//        v.setDefText(def == null ? null : def.getSqlType());
        return v;
    }

    public void setSqlType(MdValue v) {
        XmlUtils.setExtension(element, JDBC_SQL_TYPE, v.getText());
    }

    /**
     * Get the SQL DDL for this column.
     */
    public String getSqlDDL() {
        String s = getSqlTypeStr();
        if (s == null) {
            if (jdbcColumn == null) {
                s = def == null ? "?" : def.getSqlType();
            } else {
                s = jdbcColumn.sqlType;
            }
        }
        String len = getLengthStr();
        if (len == null) {
            if (jdbcColumn == null) {
                len = def == null ? null : def.getLength();
            } else {
                len = Integer.toString(jdbcColumn.length);
            }
        }
        String scale = getScaleStr();
        if (scale == null) {
            if (jdbcColumn == null) {
                scale = def == null ? null : def.getScale();
            } else {
                scale = Integer.toString(jdbcColumn.scale);
            }
        }
        StringBuffer ans = new StringBuffer();
        ans.append(s);
        if (len != null && !len.equals("0")) {
            ans.append('(');
            ans.append(len);
            if (scale != null && !scale.equals("0")) {
                ans.append(',');
                ans.append(scale);
            }
            ans.append(')');
        }
        return ans.toString();
    }

    public String getLengthStr() {
        return XmlUtils.getExtension(element, JDBC_LENGTH);
    }

    public MdValue getLength() {
        MdValue v = new MdValueInt(getLengthStr());
        v.setDefText(
                jdbcColumn == null ? null : Integer.toString(jdbcColumn.length));
//        v.setDefText(def == null ? null : def.getLength());
        return v;
    }

    public void setLength(MdValue v) {
        XmlUtils.setExtension(element, JDBC_LENGTH, v.getText());
    }

    public String getScaleStr() {
        return XmlUtils.getExtension(element, JDBC_SCALE);
    }

    public MdValue getScale() {
        MdValue v = new MdValueInt(getScaleStr());
        v.setDefText(
                jdbcColumn == null ? null : Integer.toString(jdbcColumn.scale));
//        v.setDefText(def == null ? null : def.getScale());
        return v;
    }

    public void setScale(MdValue v) {
        XmlUtils.setExtension(element, JDBC_SCALE, v.getText());
    }

    public String getNullsStr() {
        return XmlUtils.getExtension(element, JDBC_NULLS);
    }

    public MdValue getNulls() {
        MdValue v = new MdValue(getNullsStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText(
                jdbcColumn == null ? null : (jdbcColumn.nulls ? "true" : "false"));
//        v.setDefText(def == null ? null : def.getNulls());
        return v;
    }

    public void setNulls(MdValue v) {
        XmlUtils.setExtension(element, JDBC_NULLS, v.getText());
    }

    public String getSharedStr() {
        return XmlUtils.getExtension(element, JDBC_SHARED);
    }

    public MdValue getShared() {
        MdValue v = new MdValue(getSharedStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText(def == null ? null : def.getShared());
        return v;
    }

    public void setShared(MdValue v) {
        XmlUtils.setExtension(element, JDBC_SHARED, v.getText());
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        appendNotNull(s, XmlUtils.getExtension(element, JDBC_COLUMN_NAME));
        appendNotNull(s, XmlUtils.getExtension(element, JDBC_TYPE));
        String len = XmlUtils.getExtension(element, JDBC_LENGTH);
        String scale = XmlUtils.getExtension(element, JDBC_SCALE);
        if (len != null || scale != null) {
            s.append('(');
            s.append(len);
            if (scale != null) {
                s.append(',');
                s.append(scale);
            }
            s.append(')');
        }
        String nulls = XmlUtils.getExtension(element, JDBC_NULLS);
        if (nulls != null) {
            if (nulls.equals("true")) {
                s.append(" null");
            } else {
                s.append(" not null");
            }
        }
        return s.toString();
    }

    private void appendNotNull(StringBuffer s, String v) {
        if (v == null) return;
        if (s.length() > 0) s.append(' ');
        s.append(v);
    }

    /**
     * Write the name of this column to the meta data.
     */
    public void writeNameToMetaData() {
        if (getNameStr() == null) {
            setName(new MdValue(getDefName()));
        }
    }

    public String getColumnName() {
        String s = getNameStr();
        return s == null ? getDefName() : s;
    }

    public GraphTable getTable() {
        return table;
    }

    public void setTable(GraphTable table) {
        this.table = table;
    }

    public String getComment() {
        if (help != null) return help;
        return jdbcColumn == null ? null : jdbcColumn.comment;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Default values for various column properties.
     */
    public abstract static class Defaults {

        public abstract String getType();

        public abstract String getSqlType();

        public abstract String getLength();

        public abstract String getScale();

        public abstract String getNulls();

        public abstract String getShared();

        public abstract String getConverter();
    }

    /**
     * Defaults set using simple fields.
     */
    public static class SimpleDefaults extends Defaults {

        private String defType;
        private String defSqlType;
        private String defLength;
        private String defScale;
        private String defNulls;
        private String defConverter;

        public SimpleDefaults(String defType, String defSqlType, String defLength,
                String defScale, String defNulls, String defConverter) {
            this.defType = defType;
            this.defSqlType = defSqlType;
            this.defLength = defLength;
            this.defScale = defScale;
            this.defNulls = defNulls;
            this.defConverter = defConverter;
        }

        public String getType() {
            return defType;
        }

        public String getSqlType() {
            return defSqlType;
        }

        public String getLength() {
            return defLength;
        }

        public String getScale() {
            return defScale;
        }

        public String getNulls() {
            return defNulls;
        }

        public String getShared() {
            return null;
        }

        public String getConverter() {
            return defConverter;
        }
    }

    /**
     * Defaults obtained from another column.
     */
    public static class ColDefaults extends MdColumn.Defaults {

        private MdColumn c;

        public ColDefaults(MdColumn c) {
            this.c = c;
        }

        public String getType() {
            return c.getType().toString();
        }

        public String getSqlType() {
            return c.getSqlType().toString();
        }

        public String getLength() {
            return c.getLength().toString();
        }

        public String getScale() {
            return c.getScale().toString();
        }

        public String getNulls() {
            return c.getNulls().toString();
        }

        public String getShared() {
            return c.getShared().toString();
        }

        public String getConverter() {
            return c.getJdbcConverter().toString();
        }

    }

}

