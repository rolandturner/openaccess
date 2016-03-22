
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

import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ClassIdTranslator;
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.parser.JdoField;
import com.versant.core.common.OID;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.*;
import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.JdbcPolyRefMetaDataBuilder;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.jdbc.fetch.FetchSpec;
import com.versant.core.jdbc.fetch.FetchOptions;
import com.versant.core.jdbc.fetch.FetchOpData;
import com.versant.core.jdbc.fetch.FetchFieldPath;
import com.versant.core.util.CharBuf;
import com.versant.core.common.State;

import java.util.ArrayList;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * A field that is a polymorphic reference to any other PC class (e.g. an
 * Object or interface reference).
 */
public class JdbcPolyRefField extends JdbcField {

    /**
     * The column used to hold the classId value identifying the class (and
     * hence table) that is being referenced.
     * @see #cols
     */
    public JdbcColumn classIdCol;
    /**
     * The columns used to store the classId and the primary key of the
     * referenced (target) tables.
     * @see #cols
     */
    public JdbcColumn[] refCols;
    /**
     * The classIdCol at index 0 and pkCols.
     */
    public JdbcColumn[] cols;

    private ClassIdTranslator classIdTranslator;
    private int literalType;

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        switch (useJoin) {
            case USE_JOIN_INNER:    s.append(" INNER");
            case USE_JOIN_OUTER:    s.append(" OUTER");
        }
        return s.toString();
    }

    public void prepareFetch(FetchSpec spec, FetchOptions options, SelectExp se,
                             int refLevel, FetchOpData src, FetchGroupField fgField,
                             FetchFieldPath ffPath) {
    }

    /**
     * Complete our meta data except for column names.
     */
    public void processMetaData(JdoField context, JdbcMetaDataBuilder mdb) {

        JdbcPolyRefMetaDataBuilder rdb = new JdbcPolyRefMetaDataBuilder(mdb,
                context, fmd.name,
                fmd.jdoField == null ? null : fmd.jdoField.extensions, this);

        classIdCol = rdb.getClassIdCol();
        refCols = rdb.getPkCols();
        cols = rdb.getCols();

        int nc = cols.length;
        for (int i = 0; i < nc; i++) cols[i].comment = fmd.getCommentName();

        boolean nulls = fmd.nullValue != MDStatics.NULL_VALUE_EXCEPTION;
        for (int i = 0; i < nc; i++) cols[i].nulls = nulls;

        // include this in the where clause for changed locking only if all
        // columns support equalityTest
        includeForChangedLocking = true;
        for (int i = 0; i < nc; i++) {
            if (!cols[i].equalityTest) {
                includeForChangedLocking = false;
                break;
            }
        }

        useJoin = JdbcRefField.USE_JOIN_NO;

        literalType = JdbcTypes.getLiteralType(classIdCol.jdbcType);
        classIdTranslator = rdb.getClassIdTranslator();
    }

    /**
     * Set the table field on all our main table columns.
     */
    public void setMainTable(JdbcTable table) {
        super.setMainTable(table);
        for (int i = 0; i < cols.length; i++) cols[i].setTable(table);
    }

    /**
     * Init the mainTableCols field to all our main table columns.
     */
    public void initMainTableCols() {
        mainTableCols = cols;
        super.initMainTableCols();
    }

    /**
     * Flatten all of this fields main table columns to a.
     */
    public void addMainTableCols(ArrayList a) {
        int n = cols.length;
        for (int i = 0; i < n; i++) a.add(cols[i]);
    }

    /**
     * Make sure all of this fields main table columns have names.
     */
    public void nameColumns(String tableName, JdbcNameGenerator nameGen) {
        // extract the current names of the columns
        String[] names = new String[cols.length];
        for (int i = 0; i < cols.length; i++) names[i] = cols[i].name;

        // generate the names for the unnamed columns
        nameGen.generatePolyRefFieldColumnNames(tableName, fmd.name, names);

        // set our column names
        for (int i = 0; i < cols.length; i++) cols[i].name = names[i];
    }

    public void dump(PrintStream out, String indent) {
        super.dump(out, indent);
        String is = indent + "  ";
        if (cols != null) {
            out.println(is + cols.length + " cols(s)");
            for (int i = 0; i < cols.length; i++) {
                out.println(is + "[" + i + "] " + cols[i]);
            }
        }
    }

    /**
     * Append part of an update statement for us to s (e.g col = ?).
     */
    public boolean appendUpdate(CharBuf s, State state) {
        int nc = mainTableColsForUpdate.length;
        s.append(mainTableColsForUpdate[0].name);
        s.append("=?");
        for (int i = 1; i < nc; i++) {
            s.append(", ");
            s.append(mainTableColsForUpdate[i].name);
            s.append("=?");
        }
        return false;
    }

    /**
     * Append part of a where clause for us to s (e.g cola = ? and colb = ?).
     * This is used for generating the where clause for changed locking.
     */
    public void appendWhere(CharBuf s, SqlDriver sqlDriver) {
        int nc = mainTableColsForUpdate.length;
        JdbcColumn c = mainTableColsForUpdate[0];
        s.append(c.name);
        s.append('=');
        sqlDriver.appendWhereParam(s, c);
        for (int i = 1; i < nc; i++) {
            c = mainTableColsForUpdate[i];
            s.append(" and ");
            s.append(c.name);
            s.append('=');
            sqlDriver.appendWhereParam(s, c);
        }
    }

    /**
     * Append part of a is null where clause for us to s (e.g cola is null
     * and colb is null).
     * This is used for generating the where clause for changed locking.
     */
    public void appendWhereIsNull(CharBuf s, SqlDriver sqlDriver) {
        int nc = mainTableColsForUpdate.length;
        JdbcColumn c = mainTableColsForUpdate[0];
        s.append(c.name);
        s.append(" is null");
        for (int i = 1; i < nc; i++) {
            c = mainTableColsForUpdate[i];
            s.append(" and ");
            s.append(c.name);
            s.append(" is null");
        }
    }

    /**
     * Append part of the insert list for us to s (e.g. cola, colb)).
     */
    public void appendInsertColumnList(CharBuf s) {
        int nc = mainTableColsForUpdate.length;
        s.append(mainTableColsForUpdate[0].name);
        for (int i = 1; i < nc; i++) {
            s.append(", ");
            s.append(mainTableColsForUpdate[i].name);
        }
    }

    /**
     * Append part of the insert value list for us to s (e.g. ?, ?)). This
     * must return true if a replacable parameter was <b>not</b> added (e.g.
     * columns using Oracle LOBs which put in empty_clob() or whatever).
     */
    public boolean appendInsertValueList(CharBuf s, State state) {
        s.append('?');
        int nc = mainTableColsForUpdate.length;
        for (int i = 1; i < nc; i++) s.append(", ?");
        return false;
    }

    public ColumnExp createOwningTableColumnExpList(SelectExp se) {
        return toColumnExp(se, false);
    }

    /**
     * Convert this field into a list of ColumnExp's or null if this is
     * not possible.
     */
    public ColumnExp toColumnExp(SelectExp se, boolean joinToSuper) {
        if (joinToSuper) se = SelectExp.createJoinToSuperTable(se, this);

        ColumnExp ans = new ColumnExp(cols[0], se, this);
        SqlExp e = ans;
        int nc = cols.length;
        for (int i = 1; i < nc; i++) {
            e = e.setNext(new ColumnExp(cols[i], se, this));
        }
        return ans;
    }

    /**
     * Convert this field into a list of ColumnExp's to be compared to
     * a null literal. This should only include non-shared columns i.e.
     * columns that are updated. If all columns are shared then all should
     * be included.
     */
    public ColumnExp toColumnExpForNullLiteralCompare(SelectExp se) {
        se = SelectExp.createJoinToSuperTable(se, this);
        
        if (mainTableColsForUpdate == null) return toColumnExp(se, true);
        ColumnExp ans = new ColumnExp(mainTableColsForUpdate[0], se, this);
        SqlExp e = ans;
        int nc = mainTableColsForUpdate.length;
        for (int i = 1; i < nc; i++) {
            e = e.setNext(new ColumnExp(mainTableColsForUpdate[i], se, this));
        }
        return ans;
    }

    /**
     * Set this field on a PreparedStatement. This is used to set parameters
     * for queries.
     * @return Index of the parameter after the last one we set in ps
     */
    public int setQueryParam(PreparedStatement ps, int firstParam, Object value)
            throws SQLException {
        OID oid = (OID)value;
        if (oid != null) {
            ClassMetaData target = oid.getClassMetaData();
            if (classIdTranslator.isStringClassIds()) {
                String id = classIdTranslator.getStringClassIdForClass(target);
                classIdCol.set(ps, firstParam, id);
            } else {
                int id = classIdTranslator.getIntClassIdForClass(target);
                classIdCol.set(ps, firstParam, id);
            }
            firstParam = ((JdbcOID)oid).setParams(ps, ++firstParam);
        } else {
            int nc = cols.length;
            for (int i = 0; i < nc; i++) {
                ps.setNull(firstParam++, cols[i].jdbcType);
            }
        }
        return firstParam;
    }

    /**
     * Set this field on a PS for inserting or updating a row.
     * @param firstParam Index of first parameter to set
     * @return Index of next parameter to be set on ps
     */
    public int setData(PreparedStatement ps, int firstParam, OID oid)
            throws SQLException {
        if (oid != null) {
            if (classIdCol.isForUpdate()) {
                ClassMetaData target = oid.getClassMetaData();
                if (classIdTranslator.isStringClassIds()) {
                    String id = classIdTranslator.getStringClassIdForClass(target);
                    classIdCol.set(ps, firstParam++, id);
                } else {
                    int id = classIdTranslator.getIntClassIdForClass(target);
                    classIdCol.set(ps, firstParam++, id);
                }
            }
            firstParam = ((JdbcOID)oid).setParams(ps, firstParam, refCols);
        } else {
            JdbcColumn[] cols = mainTableCols;
            int nc = cols.length;
            for (int i = 0; i < nc; i++) {
                JdbcColumn col = cols[i];
                if (col.isForUpdate()) {
                    ps.setNull(firstParam++, col.jdbcType);
                }
            }
        }
        return firstParam;
    }

    /**
     * Get this field from a ResultSet starting at index firstCol.
     */
    public OID getData(ResultSet rs, int firstCol) throws SQLException {
        ClassMetaData target;
        if (classIdTranslator.isStringClassIds()) {
            String id = (String)classIdCol.get(rs, firstCol);
            if (id == null) return null;
            target = classIdTranslator.getClassForStringClassId(id);
        } else {
            int id = classIdCol.getInt(rs, firstCol);
            if (id == 0) return null;
            target = classIdTranslator.getClassForIntClassId(id);
        }
        JdbcOID oid = (JdbcOID)target.createOID(target.pcSubclasses == null);
        if (oid.copyKeyFields(rs, firstCol + 1)) return oid;
        else return null;
    }

    /**
     * Create an expression to compare our classIdCol to the correct class-id
     * value for target.
     */
    public SqlExp createClassIdMatchExp(SelectExp root, ClassMetaData target) {
        SqlExp left = classIdCol.toSqlExp(root);
        String id = classIdTranslator.isStringClassIds()
                ? classIdTranslator.getStringClassIdForClass(target)
                : Integer.toString(classIdTranslator.getIntClassIdForClass(target));
        LiteralExp right = new LiteralExp(literalType, id);
        return new BinaryOpExp(left, BinaryOpExp.EQUAL, right);
    }

}


