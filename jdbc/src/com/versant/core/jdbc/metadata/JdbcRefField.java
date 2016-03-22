
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

import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.common.OID;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.ColumnExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.Join;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.jdbc.fetch.*;
import com.versant.core.util.CharBuf;
import com.versant.core.common.State;
import com.versant.core.common.Debug;

import java.util.ArrayList;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.versant.core.common.BindingSupportImpl;

/**
 * A field that is a reference to another PC class.
 */
public class JdbcRefField extends JdbcField {

    /**
     * The columns used to store the primary key of the referenced (target)
     * table. These must be in the same order as the pk array of the target
     * table.
     */
    public JdbcColumn[] cols;
    /**
     * This is the class that we reference. Note that this may be for a
     * different Store (e.g. LDAP).
     */
    public ClassMetaData targetClass;
    /**
     * The foreign key constraint for this reference (null if none).
     */
    public JdbcConstraint constraint;
    /**
     * If this reference is being used to complete a collection mapped using
     * a foreign key reference then this is the collection field.
     */
    public JdbcFKCollectionField masterCollectionField;
    public String constraintName;
    public boolean createConstraint;

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        switch (useJoin) {
            case USE_JOIN_INNER:
                s.append(" INNER");
            case USE_JOIN_OUTER:
                s.append(" OUTER");
        }
        return s.toString();
    }

    /**
     * Set the table field on all our main table columns.
     */
    public void setMainTable(JdbcTable table) {
        super.setMainTable(table);
        for (int i = 0; i < cols.length; i++) {
            cols[i].setTable(table);
        }
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
        // check if there are other fields referencing our targetClass
        int refs = 0;
        FieldMetaData[] fields = fmd.classMetaData.fields;
        for (int i = fields.length - 1; i >= 0; i--) {
            JdbcField f = (JdbcField)fields[i].storeField;
            if (f instanceof JdbcRefField) {
                JdbcRefField rf = (JdbcRefField)f;
                if (rf.targetClass == targetClass) {
                    if (++refs == 2) break;
                }
            }
        }
        boolean otherRefs = refs >= 2;

        // extract the current names of the columns and the
        // matching referenced column names
        String[] names = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            names[i] = cols[i].name;
        }
        String[] refPkNames = null;
        JdbcClass tjc = (JdbcClass)targetClass.storeClass;
        if (tjc != null) {
            refPkNames = new String[cols.length];
            for (int i = 0; i < cols.length; i++) {
                refPkNames[i] = tjc.table.pk[i].name;
            }
        }

        // generate the names for the unnamed columns
        if (tjc == null) {
            nameGen.generateRefFieldColumnNames(tableName, fmd.name, names,
                    null, null, otherRefs);
        } else {
            nameGen.generateRefFieldColumnNames(tableName, fmd.name, names,
                    tjc.tableName, refPkNames, otherRefs);
        }

        // set our simple column names
        for (int i = 0; i < cols.length; i++) cols[i].name = names[i];
    }

    /**
     * Make sure all of this fields main table constraints have names and
     * add them to cons.
     */
    public void addConstraints(ArrayList cons) {
        if (constraint != null) cons.add(constraint);
    }

    public void dump(PrintStream out, String indent) {
        super.dump(out, indent);
        String is = indent + "  ";
        out.println(is + "targetClass = " + targetClass);
        out.println(is + "constraint = " + constraint);
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

    public SelectExp addParColJoin(SelectExp joinTo, boolean keyJoin) {
        SelectExp elementExp = new SelectExp();
        elementExp.table = ((JdbcClass)targetClass.storeClass).table;
        joinTo.addJoin(cols, elementExp.table.pk, elementExp);
        return elementExp;
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

    /**
     * Check to see if the supplied table is a subclass table of the supplied
     * ClassMetaData.
     */
    public static boolean isSubTableOf(JdbcTable table, ClassMetaData cmd) {
        if (table == ((JdbcClass)cmd.storeClass).table) return true;
        ClassMetaData[] pcSubs = cmd.pcSubclasses;
        for (int i = 0; i < pcSubs.length; i++) {
            ClassMetaData pcSub = pcSubs[i];
            if (((JdbcClass)pcSub.storeClass).table == table) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert this field into a list of ColumnExp's or null if this is
     * not possible.
     */
    public ColumnExp toColumnExp(SelectExp se, boolean joinToSuper) {
        if (Debug.DEBUG) {
            if (!isSubTableOf(se.table, fmd.classMetaData)) {
                throw BindingSupportImpl.getInstance().internal("The table '" + se.table.name
                        + "'of the suplied selectExp is not equal or a subClass " +
                        "table of table '"
                        + ((JdbcClass)fmd.classMetaData.storeClass).table.name + "'");
            }
        }
        if (joinToSuper) se = SelectExp.createJoinToSuperTable(se, this);
        return createOwningTableColumnExpList(se);
    }

    public ColumnExp createOwningTableColumnExpList(SelectExp se) {
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
     *
     * @return Index of the parameter after the last one we set in ps
     */
    public int setQueryParam(PreparedStatement ps, int firstParam,
            Object value)
            throws SQLException {
        OID oid = (OID)value;
        if (oid != null) {
            firstParam = ((JdbcOID)oid).setParams(ps, firstParam);
        } else {
            int nc = cols.length;
            for (int i = 0; i < nc; i++) {
                ps.setNull(firstParam++, cols[i].jdbcType);
            }
        }
        return firstParam;
    }

    public void prepareFetch(FetchSpec spec, FetchOptions options, SelectExp se,
                             int refLevel, FetchOpData src, FetchGroupField fgField, FetchFieldPath ffPath) {
        /**
         * - If the previous join is a outer then return as we do not follow
         * outer joins with outer joins.
         *
         * - If the refLevel is greater that 2 then stop.
         *
         */
        if (se.outer || refLevel >= 2) {
            //ignore
            return;
        }
        SelectExp refSe = new SelectExp();
        refSe.table = ((JdbcClass)targetClass.storeClass).table;
        refSe.outer = true;
        Join j = se.addJoin(cols, refSe.table.pk, refSe);
        j.selectExp.jdbcField = this;

        FopGetRefOID fopGetOid = new FopGetRefOID(spec,
                FetchOpDataMainRS.INSTANCE, this, refSe);
        FopGetState fopGetState = new FopGetState(spec,
                fopGetOid.getOutputData(), fgField.nextFetchGroup,
                true, refSe, refLevel, targetClass, ffPath.getCopy().add(this));

        spec.addFetchOp(fopGetOid, false);
        spec.addFetchOp(fopGetState, false);
    }
}


