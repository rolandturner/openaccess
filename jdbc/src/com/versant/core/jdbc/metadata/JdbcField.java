
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

import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.parser.JdoField;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.ColumnExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.query.JdbcJDOQLCompiler;
import com.versant.core.jdbc.fetch.FetchSpec;
import com.versant.core.jdbc.fetch.FetchOptions;
import com.versant.core.jdbc.fetch.FetchOpData;
import com.versant.core.jdbc.fetch.FetchFieldPath;
import com.versant.core.server.PersistGraph;
import com.versant.core.util.CharBuf;
import com.versant.core.jdo.query.Node;
import com.versant.core.common.State;

import java.io.Serializable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

import com.versant.core.common.*;

/**
 * Extra meta data for a field stored in JDBC. There are different subclasses
 * for different types of fields (e.g. simple, persistent class reference,
 * mem etc.).
 */
public abstract class JdbcField implements Serializable {

    /**
     * Do not join to resolve this field.
     */
    public static final int USE_JOIN_NO = 1;
    /**
     * Use an outer join to resolve this field.
     */
    public static final int USE_JOIN_OUTER = 2;
    /**
     * Use an inner join to resolve this field.
     */
    public static final int USE_JOIN_INNER = 3;

    /**
     * Our JDO field.
     */
    public FieldMetaData fmd;
    /**
     * Our state fieldNo.
     */
    public int stateFieldNo;
    /**
     * Our main table columns.
     */
    public JdbcColumn[] mainTableCols;
    /**
     * Our main table columns that need to be updated and inserted.
     */
    public JdbcColumn[] mainTableColsForUpdate;
    /**
     * Our main table.
     */
    public JdbcTable mainTable;
    /**
     * Is this a fake field created to store some extra data (e.g. row version
     * column values).
     */
    public boolean fake;
    /**
     * Should a join be done to pick up the fields for referenced classes
     * when this field is read? This only makes sense for fields that
     * reference other PC classes in some way.
     */
    public int useJoin;
    /**
     * Should this field be included in the where clause when using changed
     * optimistic locking? This default to false for fields mapped to columns
     * that have equalityTest false.
     */
    public boolean includeForChangedLocking;

    /**
     * Make sure all of this fields main table columns have names.
     */
    public void nameColumns(String tableName, JdbcNameGenerator nameGen) {
    }

    /**
     * Add all of this fields main table constraints to cons.
     */
    public void addConstraints(ArrayList cons) {
    }

    /**
     * Init the mainTableCols field to all our main table columns.
     */
    public void initMainTableCols() {
    }

    public SelectExp addParColJoin(SelectExp joinTo, boolean keyJoin) {
        return joinTo;
    }

    /**
     * Init the mainTableColsForUpdate field to all our main table columns
     * that are for update.
     */
    public void initMainTableColsForUpdate() {
        if (mainTableCols == null) return;

        // extract all the columns that should be updated and inserted into
        // mainTableColsForUpdate
        mainTableColsForUpdate = new JdbcColumn[mainTableCols.length];
        int c = 0;
        for (int i = 0; i < mainTableCols.length; i++) {
            JdbcColumn col = mainTableCols[i];
            if (col.isForUpdate()) mainTableColsForUpdate[c++] = col;
        }
        if (c == 0) {
            mainTableColsForUpdate = null;
        } else if (c < mainTableCols.length) {
            JdbcColumn[] a = new JdbcColumn[c];
            System.arraycopy(mainTableColsForUpdate, 0, a, 0, c);
            mainTableColsForUpdate = a;
        }
    }

    /**
     * Flatten all of this fields main table columns to a.
     */
    public void addMainTableCols(ArrayList a) {
    }

    /**
     * Set the table field on all our main table columns.
     */
    public void setMainTable(JdbcTable table) {
        mainTable = table;
    }

    public String toString() {
        String n = getClass().getName();
        n = n.substring(n.lastIndexOf('.') + 1);
        return n + " " + fmd.type.getName() + " " + fmd.classMetaData.getShortName() +
                "." + fmd.name + (fake ? " FAKE" : "");
    }

    /**
     * Convert a useJoin field value to a String.
     */
    public static String toUseJoinString(int useJoin) {
        switch (useJoin) {
            case USE_JOIN_NO:
                return "NO";
            case USE_JOIN_INNER:
                return "INNER";
            case USE_JOIN_OUTER:
                return "OUTER";
        }
        return "unknown(" + useJoin + ")";
    }

    /**
     * Get context information for this field from its .jdo meta data or
     * the .jdo meta data of its class.
     */
    public String getContext() {
        JdoField jf = fmd.jdoField;
        if (jf != null) return jf.getContext();
        return fmd.classMetaData.jdoClass.getContext();
    }

    /**
     * Add all tables that belong to this field to the set.
     */
    public void getTables(HashSet tables) {
        // nothing to do
    }

    /**
     * Get the useKeyJoin value for this field. This is only valid for maps.
     */
    public int getUseKeyJoin() {
        return 0;
    }

    public void dump() {
        dump(Debug.OUT, "");
    }

    public void dump(PrintStream out, String indent) {
        out.println(indent + this);
        String is = indent + "  ";
        out.println(is + "useJoin " + toUseJoinString(useJoin));
        out.println(is + "stateFieldNo " + stateFieldNo);
        if (mainTableCols != null) {
            out.println(is + mainTableCols.length + " mainTableCols(s)");
            for (int i = 0; i < mainTableCols.length; i++) {
                out.println(is + "[" + i + "] " + mainTableCols[i]);
            }
        }
        if (mainTableColsForUpdate != null) {
            out.println(
                    is + mainTableColsForUpdate.length + " mainTableColsForUpdate(s)");
            for (int i = 0; i < mainTableColsForUpdate.length; i++) {
                out.println(is + "[" + i + "] " + mainTableColsForUpdate[i]);
            }
        }
    }

    /**
     * Append part of an update statement for us to s (e.g col = ?). This
     * must return true if a replacable parameter was <b>not</b> added (e.g.
     * columns using Oracle LOBs which put in empty_clob() or whatever).
     */
    public boolean appendUpdate(CharBuf s, State state) {
        return false;
    }

    /**
     * Append part of a where clause for us to s (e.g cola = ? and colb = ?).
     * This is used for generating the where clause for changed locking.
     */
    public void appendWhere(CharBuf s, SqlDriver sqlDriver) {
    }

    /**
     * Append part of a is null where clause for us to s (e.g cola is null
     * and colb is null).
     * This is used for generating the where clause for changed locking.
     */
    public void appendWhereIsNull(CharBuf s, SqlDriver sqlDriver) {
    }

    /**
     * Append part of the insert list for us to s (e.g. cola, colb)).
     */
    public void appendInsertColumnList(CharBuf s) {
    }

    /**
     * Append part of the insert value list for us to s (e.g. ?, ?)). This
     * must return true if a replacable parameter was <b>not</b> added (e.g.
     * columns using Oracle LOBs which put in empty_clob() or whatever).
     */
    public boolean appendInsertValueList(CharBuf s, State state) {
        return false;
    }

    /**
     * Convert this field into a list of ColumnExp's or null if this is
     * not possible.
     */
    public ColumnExp toColumnExp(SelectExp se, boolean joinToSuper) {
        return null;
    }

    /**
     * Convert this field into a list of ColumnExp's to be compared to
     * a null literal. This should only include non-shared columns i.e.
     * columns that are updated. If all columns are shared then all should
     * be included.
     */
    public ColumnExp toColumnExpForNullLiteralCompare(SelectExp se) {
        return toColumnExp(se, true);
    }

    /**
     * Convert this field into an isEmpty expression.
     */
    public SqlExp toIsEmptySqlExp(JdbcJDOQLCompiler comp, SelectExp root) {
        throw BindingSupportImpl.getInstance().runtime("isEmpty() may not be called on " +
                fmd.getQName());
    }

    /**
     * Convert this field into an contains expression.
     */
    public SqlExp toContainsSqlExp(JdbcJDOQLCompiler comp, SelectExp root,
            Node args) {
        throw BindingSupportImpl.getInstance().runtime("contains(...) may not be called on " +
                fmd.getQName());
    }

    /**
     * Convert this field into an containsKey expression.
     */
    public SqlExp toContainsKeySqlExp(JdbcJDOQLCompiler comp, SelectExp root,
            Node args) {
        throw BindingSupportImpl.getInstance().runtime("containsKey(...) may not be called on " +
                fmd.getQName());
    }

    /**
     * Set this field on a PreparedStatement. This is used to set parameters
     * for queries.
     *
     * @return Index of the parameter after the last one we set in ps
     */
    public int setQueryParam(PreparedStatement ps, int firstParam,
            Object value)
            throws SQLException {
        throw BindingSupportImpl.getInstance().internal(
                "set called on " + this);
    }

    /**
     * Persist pass 2 field for a block of graph entries all with
     * the same class. The same ps'es can be used for all entries in the block.
     */
    public void persistPass2Block(PersistGraph graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batchInserts,
            boolean batchUpdates)
            throws SQLException {
        throw BindingSupportImpl.getInstance().internal(
                "persistPass2Block called on " + this);
    }

    /**
     * Delete a pass 2 field for a block of graph entries all with
     * the same class. The same ps'es can be used for all entries in the block.
     */
    public void deletePass2Block(DeletePacket graph, int blockStart,
            int blockEnd, CharBuf s, Connection con, boolean batch)
            throws SQLException {
        throw BindingSupportImpl.getInstance().internal(
                "deletePass2Block called on " + this);
    }

    /**
     * Does this field require the sucky Oracle LOB support on insert/update?
     */
    public boolean isOracleStyleLOB() {
        return false;
    }

    /**
     * Make sure all the indexes on our link tables (if any) have names,
     */
    public void nameLinkTableIndexes(JdbcNameGenerator namegen) {
    }

    /**
     * If there a columnName in our main table columns array then return it
     * else return null.
     */
    public JdbcColumn findMainTableColumn(String columnName) {
        if (mainTableCols == null) return null;
        for (int i = mainTableCols.length - 1; i >= 0; i--) {
            JdbcColumn c = mainTableCols[i];
            if (c.name.equals(columnName)) return c;
        }
        return null;
    }

    /**
     * Get the current SqlDriver.
     */
    public SqlDriver getSqlDriver() {
        return ((JdbcClass)fmd.classMetaData.storeClass).sqlDriver;
    }

    /**
     * Map an exception using the current SqlDriver.
     */
    public RuntimeException mapException(Throwable cause,
            String message) {
        return getSqlDriver().mapException(cause, message, true);
    }

    /**
     * Create a list of ColumnExp's for this field or null if it has no
     * columns stored in any of the tables for its owning class. 
     */
    public ColumnExp createOwningTableColumnExpList(SelectExp se) {
        return null;
    }

    /**
     * Adjust spec so this field will be fetched.
     */
    public void prepareFetch(FetchSpec spec, FetchOptions options, SelectExp se,
                             int refLevel, FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {
    }

}


