
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

import com.versant.core.common.Debug;
import com.versant.core.common.NotImplementedException;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.jdbc.JdbcConverter;
import com.versant.core.jdbc.JdbcConverterFactory;
import com.versant.core.jdbc.JdbcTypeRegistry;
import com.versant.core.jdbc.JdbcUtils;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.util.CharBuf;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.versant.core.common.BindingSupportImpl;

/**
 * A column in a JDBC table.
 */
public final class JdbcColumn implements Serializable {

    /**
     * The table this column belongs to.
     */
    public transient JdbcTable table;
    /**
     * The java type of this column. This is used to select converters
     * etc and when the state of this column needs to be stored. For a column
     * for a persistent field this will just be the type of the field.
     */
    public transient Class javaType;
    /**
     * The java type code of this column from MDStatics.
     *
     * @see com.versant.core.metadata.MDStatics
     */
    public int javaTypeCode;
    /**
     * Is this column part of the primary key for its table?
     */
    public boolean pk;
    /**
     * Is this column part of a foreign key reference to another table?
     */
    public boolean foreignKey;
    /**
     * Is this column part of an index?
     */
    public boolean partOfIndex;
    /**
     * Is this an autoincrement column?
     */
    public boolean autoinc;
    /**
     * The JDBC name of this column.
     */
    public String name;
    /**
     * The JDBC type of this column from java.sql.Types.
     */
    public int jdbcType;
    /**
     * The actual SQL type of this column for create scripts etc.
     */
    public String sqlType;
    
    
    /**
     * The length (or precision) of this column.
     */
    public int length;
    /**
     * The scale of this column (number of digits after the decimal point).
     */
    public int scale;
    /**
     * Does this column allow nulls?
     */
    public boolean nulls;
    /**
     * Can values from this column be compared for equality with Java values?
     * Non-exact data types should have false here (e.g. float and double).
     */
    public boolean equalityTest;
    /**
     * Should this column not be created when generating the schema? This
     * is used when columns are shared between one or more fields.
     */
    public boolean shared;
    /**
     * This is responsible for getting a suitable java value for this column
     * from a ResultSet and setting the java value of this column on a
     * PreparedStatement. It may be null in which case the ResultSet should
     * be accessed directly using one of the getXXX or setXXX methods.
     */
    public transient JdbcConverter converter;
    /**
     * The name of the field this column references. This is used for columns
     * that reference composite primary key classes to identify which
     * primary key field the column is for.
     */
    public transient JdbcSimpleField refField;
    /**
     * Comment info for the SQL script (e.g. what field this column is for).
     */
    public String comment;

    public JdbcColumn() {
    }

    public JdbcColumn(JdbcJavaTypeMapping m, JdbcTypeRegistry jdbcTypeRegistry) {
        sqlType = m.getSqlType();
        if (sqlType == null) {
            throw BindingSupportImpl.getInstance().internal(
                    "sqlType is null: " + m);
        }
        
        setJavaType(m.getJavaType());
        jdbcType = m.getJdbcType();
        length = m.getLength();
        scale = m.getScale();
        nulls = m.getNulls() != JdbcJavaTypeMapping.FALSE;
        equalityTest = m.getEqualityTest() != JdbcJavaTypeMapping.FALSE;
        setShared(m.getShared() == JdbcJavaTypeMapping.TRUE);
        JdbcConverterFactory cf = m.getConverterFactory();
        if (cf != null) {
            converter = cf.createJdbcConverter(this, null, jdbcTypeRegistry);
            if (converter != null && converter.isOracleStyleLOB()) equalityTest = false;
        }
    }

    /**
     * Set our properties based on the info in the mapping. This is useful
     * when a column is based on another column instead of being created
     * by resolving the mapping (e.g. reference fields).
     */
    public void updateFrom(JdbcJavaTypeMapping m,
            JdbcTypeRegistry jdbcTypeRegistry) {
        if (m.getSqlType() != null) sqlType = m.getSqlType();
        if (m.getJavaType() != null) setJavaType(m.getJavaType());
        if (m.getJdbcType() != 0) jdbcType = m.getJdbcType();
        if (m.getLength() != -1) length = m.getLength();
        if (m.getScale() != -1) scale = m.getScale();
        if (m.getNulls() != JdbcJavaTypeMapping.NOT_SET) {
            nulls = m.getNulls() != JdbcJavaTypeMapping.FALSE;
        }
        if (m.getEqualityTest() != JdbcJavaTypeMapping.NOT_SET) {
            equalityTest = m.getEqualityTest() != JdbcJavaTypeMapping.FALSE;
        }
        if (m.getShared() != JdbcJavaTypeMapping.NOT_SET) {
            setShared(m.getShared() != JdbcJavaTypeMapping.FALSE);
        }
        JdbcConverterFactory cf = m.getConverterFactory();
        if (cf != null) {
            converter = cf.createJdbcConverter(this, null, jdbcTypeRegistry);
            if (converter != null && converter.isOracleStyleLOB()) equalityTest = false;
        }
    }

    /**
     * Set the javaType and the javaTypeCode.
     */
    public void setJavaType(Class javaType) {
        this.javaType = javaType;
        javaTypeCode = MDStaticUtils.toTypeCode(javaType);
    }

    public Class getJavaType() {
        return javaType;
    }

    /**
     * If the column or its component columns have names then add them to
     * nameGen.
     *
     * @throws IllegalArgumentException if any names are invalid
     */
    public void addColumnNames(String tableName, JdbcNameGenerator nameGen)
            throws IllegalArgumentException {
        if (name != null) nameGen.addColumnName(tableName, name);
    }

    /**
     * Get the names of all our component columns.
     */
    public String[] getColumnNames() {
        return new String[]{name};
    }

    /**
     * Get the names of all our component columns into the array.
     */
    public void getColumnNames(String[] names) {
        names[0] = name;
    }

    /**
     * Set the names of all our component columns from the array.
     */
    public void setColumnNames(String[] names) {
        name = names[0];
    }

    /**
     * Set our table field and recursively all our columns.
     */
    public void setTable(JdbcTable t) {
        table = t;
    }

    /**
     * Duplicate this column but leave the name, table and refField fields of
     * the duplicates null.
     */
    public JdbcColumn copy() {
        JdbcColumn d = new JdbcColumn();
        d.javaType = javaType;
        d.pk = pk;
        d.javaTypeCode = javaTypeCode;
        d.jdbcType = jdbcType;
        d.sqlType = sqlType;
        d.length = length;
        d.scale = scale;
        d.nulls = nulls;
        d.equalityTest = equalityTest;
        d.setShared(shared);
        d.converter = converter;
        return d;
    }

    /**
     * Combine two arrays of JdbcColumn's into one. If b is null then a is
     * returned as is.
     */
    public static JdbcColumn[] concat(JdbcColumn[] a, JdbcColumn[] b) {
        if (b == null) return a;
        if (a == null) return b;
        int na = a.length;
        int nb = b.length;
        JdbcColumn[] ans = new JdbcColumn[na + nb];
        System.arraycopy(a, 0, ans, 0, na);
        System.arraycopy(b, 0, ans, na, nb);
        return ans;
    }

    /**
     * Combine an arrays of JdbcColumn's and a single column into one. If
     * the single column is null then the array is returned as is.
     */
    public static JdbcColumn[] concat(JdbcColumn[] a, JdbcColumn b) {
        if (b == null) return a;
        int na = a.length;
        JdbcColumn[] ans = new JdbcColumn[na + 1];
        System.arraycopy(a, 0, ans, 0, na);
        ans[na] = b;
        return ans;
    }

    /**
     * Flatten cols into a list of expressions to select all the cols.
     */
    public static SqlExp toSqlExp(JdbcColumn[] cols, SelectExp se) {
        SqlExp list = new ColumnExp(cols[0], se, null);
        SqlExp e = list;
        int nc = cols.length;
        for (int i = 1; i < nc; i++) {
            e = e.setNext(new ColumnExp(cols[i], se, null));
        }
        return list;
    }

    /**
     * Flatten cols into a list of expressions to select all the cols. Add the sqlExp
     * to the end of the created list.
     */
    public static SqlExp toSqlExp(JdbcColumn[] cols, SelectExp se,
            SqlExp sList) {
        SqlExp list = new ColumnExp(cols[0], se, null);
        SqlExp e = list;
        int nc = cols.length;
        for (int i = 1; i < nc; i++) {
            e = e.setNext(new ColumnExp(cols[i], se, null));
        }
        e.setNext(sList);
        return list;
    }

    /**
     * Get a list of expressions to select all our simple cols.
     */
    public SqlExp toSqlExp(SelectExp se) {
        return new ColumnExp(this, SelectExp.createJoinToSuperTable(se, table),
                null);
    }

    /**
     * If the column or its component columns have names then add them to
     * nameGen.
     *
     * @throws IllegalArgumentException if any names are invalid
     */
    public static void addColumnNames(String tableName, JdbcColumn[] cols,
            JdbcNameGenerator namegen) throws IllegalArgumentException {
        for (int i = 0; i < cols.length; i++) {
            cols[i].addColumnNames(tableName, namegen);
        }
    }

    /**
     * Get the names of all the simple columns in an array of columns.
     */
    public static String[] getColumnNames(JdbcColumn[] cols) {
        if (cols == null) {
            return new String[0];
        }
        int n = cols.length;
        String[] ans = new String[n];
        for (int i = 0; i < n; i++) {
            ans[i] = cols[i].name;
        }
        return ans;
    }

    /**
     * Set the names of all the simple columns in an array of columns.
     */
    public static void setColumnNames(JdbcColumn[] cols, String[] names) {
        if (cols == null) {
            return;
        }
        int n = cols.length;
        for (int i = 0; i < n; i++) {
            cols[i].name = names[i];
        }
    }

    /**
     * Format an array of columns as a comma separated String of the names.
     */
    public static String toNameString(JdbcColumn[] cols) {
        StringBuffer s = new StringBuffer();
        int len = cols.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) s.append(", ");
            s.append(cols[i].name);
        }
        return s.toString();
    }

    /**
     * Set the nulls value for all of our columns.
     */
    public void setNulls(boolean nulls) {
        this.nulls = nulls;
    }

    /**
     * Get a 'cola = ? [and colb = ?]' expression for an array of simple
     * columns from se.
     */
    public static SqlExp createEqualsParamExp(JdbcColumn[] scols, SelectExp se) {
        int nc = scols.length;
        if (nc == 1) {
            return scols[0].createEqualsParamExp(se);
        } else {
            SqlExp list = scols[0].createEqualsParamExp(se);
            SqlExp pos = list;
            for (int i = 1; i < nc; i++) {
                pos = pos.setNext(scols[i].createEqualsParamExp(se));
            }
            return new AndExp(list);
        }
    }

    public static InExp createInParamExp(JdbcColumn[] scols, SelectExp se,
            int size) {
        if (scols.length > 1) {
            throw BindingSupportImpl.getInstance().notImplemented(
                    "'In' expressions is not support on multi-pk classes");
        }

        ColumnExp columnExp = new ColumnExp(scols[0], se, null);
        ParamExp rootParam = new ParamExp(scols[0].jdbcType, null);
        columnExp.setNext(rootParam);
        for (int i = 0; i < (size - 1); i++) {
            rootParam.setNext(new ParamExp(scols[0].jdbcType, null));
            rootParam = (ParamExp)rootParam.getNext();
        }
        return new InExp(columnExp);
    }

    /**
     * /**
     * Append a 'cola = ? [and colb = ?' string for an array of simple cols
     * to s.
     */
    public static void appendEqualsParam(CharBuf s, JdbcColumn[] cols,
            SqlDriver driver) {
        int nc = cols.length;
        JdbcColumn sc = cols[0];
        s.append(sc.name);
        s.append(' ');
        s.append('=');
        s.append(' ');
        driver.appendWhereParam(s, sc);
        for (int i = 1; i < nc; i++) {
            s.append(" AND ");
            sc = cols[i];
            s.append(sc.name);
            s.append(' ');
            s.append('=');
            s.append(' ');
            driver.appendWhereParam(s, sc);
        }
    }

    /**
     * Get the value of this column from a ResultSet.
     */
    public Object get(ResultSet rs, int index) throws SQLException {
        if (converter == null) {
            return JdbcUtils.get(rs, index, javaTypeCode, scale);
        } else {
            return converter.get(rs, index, this);
        }
    }



    /**
     * Set a value for this column on a PreparedStatement.
     */
    public void set(PreparedStatement ps, int index, Object value)
            throws SQLException {
        if (converter == null) {
            JdbcUtils.set(ps, index, value, javaTypeCode, jdbcType);
        } else {
            converter.set(ps, index, this, value);
        }
    }

    /**
     * Set a value for this column on a PreparedStatement.
     */
    public void set(PreparedStatement ps, int index, int value)
            throws SQLException {
        if (converter == null) {
            JdbcUtils.set(ps, index, value, javaTypeCode, jdbcType);
        } else {
            converter.set(ps, index, this, value);
        }
    }

    /**
     * Get the value of this column from the ResultSet as an int.
     */
    public int getInt(ResultSet rs, int index) throws SQLException {
        if (converter == null) {
            return rs.getInt(index);
        } else {
            return ((Integer)converter.get(rs, index, this)).intValue();
        }
    }

    /**
     * Append a comma list of our column name(s) to s.
     */
    public void appendNames(CharBuf s) {
        s.append(name);
    }

    /**
     * Append a comma param list to s (e.g. ?, ?).
     */
    public void appendParams(CharBuf s) {
        s.append('?');
    }

    /**
     * Create a col = ? expression.
     */
    public SqlExp createEqualsParamExp(SelectExp se) {
        return new BinaryOpExp(new ColumnExp(this, se, null),
                BinaryOpExp.EQUAL,
                new ParamExp(jdbcType, null));
    }

    /**
     * Must this column be included in update and insert statements?
     */
    public boolean isForUpdate() {
        return !shared;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(javaType == null ? "(null javaType)" : javaType.getName());
        s.append(' ');
        s.append(name);
        s.append(' ');
        s.append(sqlType);
        s.append('[');
        s.append(JdbcTypes.toString(jdbcType));
        s.append(']');
        if (length != 0 || scale != 0) {
            s.append('(');
            s.append(length);
            if (scale != 0) {
                s.append(',');
                s.append(scale);
            }
            s.append(')');
        }
        s.append(nulls ? " null" : " not null");
        if (refField != null) {
            s.append(" ref ");
            s.append(refField);
        }
        s.append(shared ? " shared" : "");
        s.append(autoinc ? " autoinc" : "");
        if (Debug.DEBUG) {
            s.append(" 0x");
            s.append(Integer.toHexString(System.identityHashCode(this)));
        }
        return s.toString();
    }

    public String toJoinString() {
        return table.name + "." + name;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getShortName() {
        StringBuffer buffer = new StringBuffer(name);
        buffer.append("  ");
        buffer.append(sqlType);
        if (length > 0) {
            buffer.append("(");
            buffer.append(length);
            buffer.append(")");
        }
        return buffer.toString();
    }

    public String getTypeString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sqlType);
        if (length > 0) {
            buffer.append("(");
            buffer.append(length);
            buffer.append(")");
        }
        return buffer.toString();
    }

    /**
     * Create a Literal Exp for a classIdColumn
     */
    public LiteralExp createClassIdLiteralExp(Object val) {
        return new LiteralExp(JdbcTypes.getLiteralType(javaTypeCode),
                val.toString());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JdbcColumn)) return false;

        final JdbcColumn jdbcColumn = (JdbcColumn)o;

        if (name != null ? !name.equals(jdbcColumn.name) : jdbcColumn.name != null) return false;
        if (table != null ? !table.equals(jdbcColumn.table) : jdbcColumn.table != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (table != null ? table.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
