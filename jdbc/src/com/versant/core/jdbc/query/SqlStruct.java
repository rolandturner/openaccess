
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
package com.versant.core.jdbc.query;

import com.versant.core.util.CharBuf;
import com.versant.core.common.Debug;
import com.versant.core.common.OID;
import com.versant.core.common.Utils;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.JdbcUtils;
import com.versant.core.jdbc.JdbcOID;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import com.versant.core.common.BindingSupportImpl;

/**
 * A struct representing sql for a query.
 */
public class SqlStruct {

    private static final char[] COUNT_STAR_PRE = "COUNT(".toCharArray();
    private static final char[] COUNT_STAR_POST = ")".toCharArray();
    private static final char[] COUNT_STAR_PRE_DISTINCT = "COUNT(DISTINCT(".toCharArray();
    private static final char[] COUNT_STAR_POST_DISTINCT = "))".toCharArray();
    private static final char[] IS_NULL = "is null".toCharArray();
    private static final char[] IS_NOT_NULL = "is not null".toCharArray();

    public String jdoqlFilter;

    /**
     * The SQL to execute the query. It is kept in a CharBuf as
     * it may need to be modified before each execution of the query depending
     * on which parameters are null.
     */
    private CharBuf sqlbuf;
    /**
     * Is the query a 'select distinct'?
     */
    private boolean distinct;
    /**
     * Index of the first character in the select list.
     */
    private int selectListStartIndex;
    /**
     * Number of characters in the select list.
     */
    private int selectListLength;
    /**
     * Index of the first column in the select list.
     */
    private int selectFirstColStart;
    /**
     * Index of the first column in the select list.
     */
    private int selectFirstColLength;
    /**
     * Index of the start of the 'order by ..' clause or 0 if none.
     */
    private int orderByStartIndex;
    /**
     * Number of characters in the 'order by ..' clause.
     */
    private int orderByLength;
    /**
     * The first table or alias in the from clause for the query. This is
     * required for some databases if the query needs to be converted into a
     * 'select for update' query (e.g. postgres).
     */
    private String firstTableOrAlias;
    /**
     * The sql is cached here in string form.
     */
    private transient String sql;
    /**
     * Is the current sqlbuf 'select for update'?
     */
    private boolean sqlForUpdate;
    /**
     * What was the original size of the sqlbuf before any modifications for
     * 'select for update'?
     */
    private int sqlbufNotForUpdateSize;
    /**
     * Is the current sqlbuf 'select count(*)'?
     */
    private boolean sqlForCount;
    /**
     * Store for the select list when query is converted to count(*).
     */
    private char[] originalSelectList;
    /**
     * Store for the first column of the select list when query is converted to
     * count(*).
     */
    private char[] originalSelectListFirstColumn;
    /**
     * Store for the order by clause when query is converted to count(*).
     */
    private char[] originalOrderByClause;
    /**
     * Params in the order that they appear in the SQL string. Each declared
     * parameter may have several entries in this array if it was used more
     * than once in the original query.
     */
    private Param paramList;
    /**
     * Characters used to convert a param into 'is null'.
     */
    private char[] isNullChars;
    /**
     * Characters used to convert a param into 'is not null'.
     */
    private char[] isNotNullChars;
    /**
     * Is this an aggregate query.
     */
    private boolean aggregate;

    public SqlStruct() {
        sqlbuf = new CharBuf(256);
    }

    public synchronized SqlStruct getClone() {
        SqlStruct clone = new SqlStruct();
        clone.jdoqlFilter = jdoqlFilter;
        clone.sqlbuf = new CharBuf(sqlbuf);
        clone.distinct = distinct;
        clone.selectListStartIndex = selectListStartIndex;
        clone.selectListLength = selectListLength;
        clone.orderByStartIndex = orderByStartIndex;
        clone.orderByLength = orderByLength;
        clone.firstTableOrAlias = firstTableOrAlias;
        clone.sql = sql;
        clone.sqlForUpdate = sqlForUpdate;
        clone.sqlbufNotForUpdateSize = sqlbufNotForUpdateSize;
        clone.sqlForCount = sqlForCount;
        clone.originalSelectList = originalSelectList;
        clone.originalOrderByClause = originalOrderByClause;
        clone.paramList = (paramList == null ? null : paramList.getClone());

        if (isNullChars != null) {
            clone.isNullChars = new char[isNullChars.length];
            for (int i = 0; i < isNullChars.length; i++) {
                clone.isNullChars[i] = isNullChars[i];
            }
        }

        if (isNotNullChars != null) {
            clone.isNotNullChars = new char[isNotNullChars.length];
            for (int i = 0; i < isNotNullChars.length; i++) {
                clone.isNotNullChars[i] = isNotNullChars[i];
            }
        }
        return clone;
    }

    public boolean isAggregate() {
        return aggregate;
    }

    public void setAggregate(boolean aggregate) {
        this.aggregate = aggregate;
    }

    /**
     * Set the range of characters in our buffer that contain all the columns
     * in the select list.
     * @param start Index of the first character in the select list (after
     *          'SELECT ' or 'SELECT DISTINCT ')
     * @param firstColEnd Index of the last character in the first column + 1
     * @param end Index of the last character in the list + 1
     */
    public void setSelectListRange(boolean distinct, int start, int firstColEnd,
            int end) {
        this.distinct = distinct;
        if (distinct) {
            selectListStartIndex = start - 9; // "DISTINCT ".length()
        } else {
            selectListStartIndex = start;
        }
        selectFirstColStart = start;
        selectListLength = end - selectListStartIndex;
        selectFirstColLength = firstColEnd - selectFirstColStart;
    }

    /**
     * Set the range of characters in our buffer that contain order by clause
     * including the 'order by' keywords. The end index is exclusive.
     */
    public void setOrderByRange(int start, int end) {
        orderByStartIndex = start;
        orderByLength = end - start;
    }

    public CharBuf getSqlbuf() {
        return sqlbuf;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public String getFirstTableOrAlias() {
        return firstTableOrAlias;
    }

    public void setFirstTableOrAlias(String firstTableOrAlias) {
        this.firstTableOrAlias = firstTableOrAlias;
    }

    public String getSql() {
        if (sql == null) sql = sqlbuf.toString();
        return sql;
    }

    public boolean isSqlForUpdate() {
        return sqlForUpdate;
    }

    public int getSqlbufNotForUpdateSize() {
        return sqlbufNotForUpdateSize;
    }

    public boolean isSqlForCount() {
        return sqlForCount;
    }

    public char[] getOriginalSelectList() {
        return originalSelectList;
    }

    public char[] getOriginalOrderByClause() {
        return originalOrderByClause;
    }

    public Param getParamList() {
        return paramList;
    }

    public void setParamList(Param paramList) {
        this.paramList = paramList;
        if (paramList != null) analyzeCharSpans();
    }

    private void analyzeCharSpans() {
        int max = IS_NOT_NULL.length;
        for (Param p = paramList; p != null; p = p.next) {
            for (CharSpan s = p.charSpanList; s != null; s = s.next) {
                if (s.type == CharSpan.TYPE_REMOVE) continue;
                int len = s.lastCharIndex - s.firstCharIndex;
                if (len > max) max = len;
            }
        }
        if (max > 0) {
            isNullChars = new char[max];
            copyAndPad(IS_NULL, isNullChars, max);
            isNotNullChars = new char[max];
            copyAndPad(IS_NOT_NULL, isNotNullChars, max);
        }
    }

    /**
     * This must create space in the charBuf at the index.
     */
    public void createSpace(CharBuf charBuf, int index, int amount) {
        sql = null;
        for (SqlStruct.Param p = paramList; p != null; p = p.next) {
            if (p.firstCharIndex > index) {
                //must update
                p.firstCharIndex += amount;
                for (CharSpan cs = p.charSpanList; cs != null; cs = cs.next) {
                    cs.firstCharIndex += amount;
                    cs.lastCharIndex += amount;
                }
            }
        }

        if (orderByStartIndex > index) {
            orderByStartIndex += amount;
        }
    }

    /**
     * This must create space in the charBuf at the index.
     */
    public void removeSpace(CharBuf charBuf, int index, int amount) {
        sql = null;
        for (SqlStruct.Param p = paramList; p != null; p = p.next) {
            if (p.firstCharIndex > index) {
                //must update
                p.firstCharIndex -= amount;
                for (CharSpan cs = p.charSpanList; cs != null; cs = cs.next) {
                    cs.firstCharIndex -= amount;
                    cs.lastCharIndex -= amount;
                }
            }
        }

        if (orderByStartIndex > index) {
            orderByStartIndex -= amount;
        }
    }

    /**
     * Update all our Param's for the null/not null state of their parameters
     * and for 'select for update' or not. This may change the SQL query
     * string.
     */
    public synchronized void updateSql(SqlDriver driver, Object[] params, boolean forUpdate,
                          boolean forCount) {
        boolean changed = false;
        if (params != null) {
            int paramIndex = 0;
            for (SqlStruct.Param p = paramList; p != null; p = p.next, paramIndex++) {
                if (params[p.declaredParamIndex] instanceof Collection) {
                    Collection col = (Collection) params[p.declaredParamIndex];
                    int n = col.size();
                    if (n == 0) {
                        throw BindingSupportImpl.getInstance().invalidOperation(
                                "The supplied collection param at index "
                                + paramIndex + " may not be empty");
                    }
                    if (p.inListParamCount == 0) {
                        //this is a readOnly char[]. it may not be modified
                        final char[] charsToInsert = driver.getSqlParamStringChars(p.jdbcType);
                        int toInsert = (n == 1 ? charsToInsert.length : (n * charsToInsert.length) + (n - 1));
                        char[] chars = new char[toInsert];

                        int offset = 0;
                        for (int i = 0; i < n; i++) {
                            if (offset > 0) {
                                chars[offset++] = ',';
                            }
                            for (int j = 0; j < charsToInsert.length; j++) {
                                chars[j + offset] = charsToInsert[j];
                            }
                            offset += charsToInsert.length;
                        }
                        createSpace(sqlbuf, p.firstCharIndex, toInsert);
                        sqlbuf.insert(p.firstCharIndex, chars);
                        p.charLength = chars.length;
                        p.inListParamCount = n;
                        changed = true;
                    } else if (p.inListParamCount < n) {
                        //must insert more param's
                        int insertPoint = p.charLength + p.firstCharIndex;
                        //the diff between the required number and what is already there
                        int paramsToInsert = n - p.inListParamCount;
                        final char[] charStamp = driver.getSqlParamStringChars(p.jdbcType);
                        int charsToInsert = (charStamp.length * paramsToInsert) + paramsToInsert;
                        char[] chars = new char[charsToInsert];
                        int offset = 0;
                        for (int i = 0; i < paramsToInsert; i++) {
                            chars[offset++] = ',';
                            for (int j = 0; j < charStamp.length; j++) {
                                chars[offset++] = charStamp[j];
                            }
                        }
                        if (Debug.DEBUG) {
                            if (offset != chars.length) {
                                throw BindingSupportImpl.getInstance().internal("");
                            }
                        }
                        createSpace(sqlbuf, insertPoint, charsToInsert);
                        sqlbuf.insert(insertPoint, chars);
                        p.charLength += chars.length;
                        p.inListParamCount = n;
                        changed = true;
                    } else if (p.inListParamCount > n) {
                        //must remove some
                        changed = true;
                        int removeStart = p.firstCharIndex;
                        int paramToRemove = p.inListParamCount - n;

                        int charsToRemove = driver.getSqlParamStringChars(p.jdbcType).length * paramToRemove + paramToRemove;
                        int removeTo = charsToRemove + removeStart;
                        sqlbuf.remove(removeStart, removeTo);
                        removeSpace(sqlbuf, p.firstCharIndex, removeTo - removeStart);

                        p.charLength -= (removeTo - removeStart);
                        p.inListParamCount = n;
                        changed = true;
                    }

                }
            }
        }

        if (params != null) {
            for (SqlStruct.Param p = paramList; p != null; p = p.next) {
                if (p.update(this, params[p.declaredParamIndex] == null)) {
                    changed = true;
                }
            }
        }
        if (forUpdate != sqlForUpdate
                && (!distinct || driver.isSelectForUpdateWithDistinctOk())
                && (!aggregate || driver.isSelectForUpdateWithAggregateOk())) {
            char[] a = driver.getSelectForUpdate();
            if (a != null) {
                if (forUpdate) {
                    sqlbufNotForUpdateSize = sqlbuf.size();
                    sqlbuf.append(a);
                    if (driver.isSelectForUpdateAppendTable()) {
                        sqlbuf.append(firstTableOrAlias);
                    }
                } else {
                    sqlbuf.setSize(sqlbufNotForUpdateSize);
                }
                sqlForUpdate = forUpdate;
                changed = true;
            }
        }
        if (forCount != sqlForCount) {
            if (forCount) {
                if (originalSelectList == null) {
                    originalSelectList = sqlbuf.toArray(selectListStartIndex,
                            selectListLength);
                    originalSelectListFirstColumn = sqlbuf.toArray(
                            selectFirstColStart, selectFirstColLength);
                }
                int start;
                if (distinct) {
                    sqlbuf.replace(selectListStartIndex, COUNT_STAR_PRE_DISTINCT);
                    start = selectListStartIndex + COUNT_STAR_PRE_DISTINCT.length;
                } else {
                    sqlbuf.replace(selectListStartIndex, COUNT_STAR_PRE);
                    start = selectListStartIndex + COUNT_STAR_PRE.length;
                }
                sqlbuf.replace(start, originalSelectListFirstColumn);
                start += originalSelectListFirstColumn.length;
                if (distinct) {
                    sqlbuf.replace(start, COUNT_STAR_POST_DISTINCT);
                    start += COUNT_STAR_POST_DISTINCT.length;
                } else {
                    sqlbuf.replace(start, COUNT_STAR_POST);
                    start += COUNT_STAR_POST.length;
                }
                int n = (selectListStartIndex + selectListLength) - start;
                if (n > 0) sqlbuf.replace(start, start + n, ' ');
                if (orderByStartIndex > 0) {
                    if (originalOrderByClause == null) {
                        originalOrderByClause = sqlbuf.toArray(orderByStartIndex,
                                orderByLength);
                    }
                    sqlbuf.replace(orderByStartIndex,
                            orderByStartIndex + orderByLength, ' ');
                }
            } else {
                sqlbuf.replace(selectListStartIndex, originalSelectList);
                if (orderByStartIndex > 0) {
                    sqlbuf.replace(orderByStartIndex, originalOrderByClause);
                }
            }
            sqlForCount = forCount;
            changed = true;
        }

        if (changed) sql = null;
    }

    /**
     * Set all the parameters for this query on ps. This is a NOP if params
     * is null.
     */
    public synchronized void setParamsOnPS(ModelMetaData jmd, SqlDriver driver,
            PreparedStatement ps, Object[] params, String sql) throws SQLException {
        if (params == null) return;
        int pos = 1;
        SqlStruct.Param p = paramList;
        Object value = null;
        try {
            for (; p != null; p = p.next) {
                value = params[p.declaredParamIndex];
                switch (p.mod) {
                    case Param.MOD_NONE:
                        break;
                    case Param.MOD_APPEND_PERCENT:
                        if (value != null) value = value + "%";
                        break;
                    case Param.MOD_PREPEND_PERCENT:
                        if (value != null) value = "%" + value;
                        break;
                    default:
                        throw BindingSupportImpl.getInstance().internal("Invalid mod: "
                                + p.mod);
                }
                if ((value == null) && p.requiresUpdate()) continue;
                int pci = p.classIndex;
                if (pci >= 0) {
                    ClassMetaData pcmd = jmd.classes[pci];
                    int pfno = p.fieldNo;
                    if (pfno >= 0) {
                        JdbcField f = ((JdbcClass)pcmd.storeClass).stateFields[pfno];
                        if (value instanceof Collection) {
                            Collection col = (Collection) value;
                            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                                Object o = iterator.next();
                                if (o instanceof OID) {
                                    pos = ((JdbcOID)o).setParams(ps, pos);
                                } else {
                                    pos = f.setQueryParam(ps, pos, o);
                                }
                            }
                        } else {
                            pos = f.setQueryParam(ps, pos, value);
                        }
                    } else { // this is an OID param for a link table
                        if (value != null) {
                            pos = ((JdbcOID)value).setParams(ps, pos);
                        } else {
                            JdbcColumn[] pkcols = ((JdbcClass)pcmd.storeClass).table.pkSimpleCols;
                            int nc = pkcols.length;
                            for (int i = 0; i < nc; i++) {
                                ps.setNull(pos++, pkcols[i].jdbcType);
                            }
                        }
                    }
                } else {
                    if (p.col != null) {
                        p.col.set(ps, pos++, value);
                    } else {
                        int javaTypeCode = p.javaTypeCode;
                        if (javaTypeCode == 0 && value != null) {
                            javaTypeCode = MDStaticUtils.toTypeCode(/*CHFC*/value.getClass()/*RIGHTPAR*/);
                        }
                        JdbcUtils.set(ps, pos++, value, javaTypeCode, p.jdbcType);
                    }
                }
            }
        } catch (Exception e) {
            throw driver.mapException(e, "Error setting query parameter " +
                p.getIdentifier() + " = '" + Utils.toString(value) +
                "' at PreparedStatement index " + pos + " in\n" +
                JdbcUtils.getPreparedStatementInfo(sql, ps) + "\n" +
               JdbcUtils.toString(e), false);
        }
    }

    private static void copyAndPad(char[] src, char[] dest, int len) {
        int n = src.length;
        System.arraycopy(src, 0, dest, 0, n);
        for (; n < len;) dest[n++] = ' ';
    }

    public char[] getNullChars() {
        return isNullChars;
    }

    public void setNullChars(char[] nullChars) {
        isNullChars = nullChars;
    }

    public char[] getNotNullChars() {
        return isNotNullChars;
    }

    public void setNotNullChars(char[] notNullChars) {
        isNotNullChars = notNullChars;
    }

    /**
     * A parameter. This tells us the indexes of the first and last characters
     * of this parameter and its index within the original list of declared
     * parameters.
     */
    public final static class Param implements Serializable {

        public static final int MOD_NONE = 0;
        public static final int MOD_PREPEND_PERCENT = 1;
        public static final int MOD_APPEND_PERCENT = 2;

        /**
         * An identifier for this parameter for error messages.
         */
        private String identifier;
        /**
         * The next Param in the list.
         */
        public Param next;
        /**
         * The index of this parameter in the original list of declared
         * parameters for the query.
         */
        public int declaredParamIndex;
        /**
         * The index of the first character in sql for this Param.
         * If charSpanList is not null then this is the same as there. It
         * is used for sorting.
         */
        public transient int firstCharIndex;
        /**
         * The current amount of chars that the param occupies when used a in list
         * for collection params
         */
        public transient int charLength;
        /**
         * The current amount params that this param can take is used as a collection
         * param.
         */
        public transient int inListParamCount;
        /**
         * List of character ranges used by this Param. This info is used
         * to turn '= ?' into 'is null' and so on. It is null if there is
         * no need for any replacement (e.g. using Sybase).
         */
        public CharSpan charSpanList;
        /**
         * The classIndex of the class associated with this parameter or
         * -1 if there is none (e.g. a parameter used in an expression).
         * @see #fieldNo
         */
        public int classIndex;
        /**
         * The field number associated with this parameter or -1 if none.
         * This is used with cls to locate its column(s). It is not used if
         * the classIndex is -1.
         * @see #classIndex
         */
        public int fieldNo;
        /**
         * The java type code of this parameter. This is only set if
         * classIndex is -1 i.e. this parameter is not for a field.
         */
        public int javaTypeCode;
        /**
         * The JDBC type (from java.sql.Types) for this parameter. This is
         * only set if classIndex is -1.
         * @see java.sql.Types
         */
        public int jdbcType;
        /**
         * How must the parameter value be modified before being set? This is
         * used for startsWith and endsWith for databases that do not allow
         * expressions on the right hand side of a LIKE (e.g. Informix).
         * @see #MOD_APPEND_PERCENT
         */
        public int mod;

        public transient JdbcColumn col;

        public Param(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        /**
         * Update all our CharSpan's for the null/not null state of the
         * parameter. This is a NOP if the parameter does not require update.
         * @return True if changes were made else false
         * @see #requiresUpdate
         */
        public boolean update(SqlStruct q, boolean newParamIsNull) {
            boolean ans = false;
            for (CharSpan cs = charSpanList; cs != null; cs = cs.next) {
                if (cs.update(q, newParamIsNull)) ans = true;
            }
            return ans;
        }

        /**
         * Does this need to be updated for null/not null parameter values?
         * @see #update
         */
        public boolean requiresUpdate() {
            return charSpanList != null;
        }

        public Param getClone() {
            Param clone = new Param(identifier);
            clone.next = (next == null ? null : next.getClone());
            clone.declaredParamIndex = declaredParamIndex;
            clone.firstCharIndex = firstCharIndex;
            clone.charSpanList = (charSpanList == null ? null : charSpanList.getClone());
            clone.classIndex = classIndex;
            clone.fieldNo = fieldNo;
            clone.javaTypeCode = javaTypeCode;
            clone.jdbcType = jdbcType;
            clone.mod = mod;
            clone.col = col;

            return clone;
        }
    }

    /**
     * This specifies a range of characters for a Param in our sql buffer. The
     * lastCharIndex is the index of the character after the last character
     * in the range.
     */
    public final static class CharSpan implements Serializable {

        public static final int TYPE_NULL = 1;
        public static final int TYPE_NOT_NULL = 2;
        public static final int TYPE_REMOVE = 3;

        /**
         * What must be done to this span?
         */
        public int type;
        /**
         * The index of the first character in sql.
         */
        public int firstCharIndex;
        /**
         * The index of the character after the last character in the span.
         */
        public int lastCharIndex;
        /**
         * The next span in the list.
         */
        public CharSpan next;

        /**
         * The current state of the parameter in sql. If this is true then
         * the parameter has been replaced with an 'is null' or 'is not null'.
         */
        private boolean paramIsNull;
        /**
         * The original text from the sql query. This is filled the first
         * time the param is null and is used to restore the query when it
         * is not null in future.
         */
        private char[] originalSql;

        public CharSpan getClone() {
            CharSpan clone = new CharSpan();
            clone.type = type;
            clone.firstCharIndex = firstCharIndex;
            clone.lastCharIndex = lastCharIndex;
            clone.next = (next == null ? null : next.getClone());
            clone.paramIsNull = paramIsNull;
            if (originalSql != null) {
                clone.originalSql = new char[originalSql.length];
                for (int i = 0; i < originalSql.length; i++) {
                    clone.originalSql[i] = originalSql[i];
                }
            }
            return clone;
        }

        /**
         * Update the query and our state if the newParamIsNull value differs
         * from our paramIsNull field.
         * @return True if changes were made else false
         */
        public boolean update(SqlStruct q, boolean newParamIsNull) {
            if (newParamIsNull == paramIsNull) return false;
            CharBuf sql = q.sqlbuf;
            if (newParamIsNull) {
                if (originalSql == null) {
                    originalSql = sql.toArray(firstCharIndex,
                            lastCharIndex - firstCharIndex);
                }
                if (Debug.DEBUG) {
                    System.out.println("*** CharSpan.update replacing '" +
                            new String(originalSql) + "' " + originalSql.length +
                            " " + q.isNullChars.length);
                }
                switch (type) {
                    case TYPE_NULL:
                        sql.replace(firstCharIndex, q.isNullChars);
                        break;
                    case TYPE_NOT_NULL:
                        sql.replace(firstCharIndex, q.isNotNullChars);
                        break;
                    case TYPE_REMOVE:
                        sql.replace(firstCharIndex, lastCharIndex, ' ');
                        break;
                    default:
                        throw BindingSupportImpl.getInstance().internal(
                                "Unknown CharSpan type: " + type);
                }
            } else {
                sql.replace(firstCharIndex, originalSql);
            }
            paramIsNull = newParamIsNull;
            return true;
        }
    }

}
