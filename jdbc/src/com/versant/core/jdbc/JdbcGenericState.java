
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
package com.versant.core.jdbc;

import com.versant.core.common.*;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.FetchGroupField;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStaticUtils;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.server.PersistGraph;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * Adds JDBC specific methods to GenericState.
 */
public class JdbcGenericState extends GenericState implements JdbcState {

    public JdbcGenericState() {
    }

    public JdbcGenericState(ClassMetaData cmd) {
        super(cmd);
    }

    /**
     * Return 0 if state has the same field numbers as us, less than 0 we are
     * less than it or greater than 0 if we are greater than it. The definition
     * of less than and greater than is up to the state implementation but
     * must be detirministic. For fields that are stored using Oracle style
     * LOBs then the nullness of the value must also be considered in the
     * comparison i.e. states with field x null and not null respectively
     * are different.
     *
     * @param state State to compare to (will be for same class)
     */
    public int compareToPass1(State state) {
        JdbcGenericState s = (JdbcGenericState)state;
        checkCmd();
        boolean[] sf = s.filled;
        for (int i = 0; i < filled.length; i++) {
            if (!cmd.stateFields[i].primaryField) continue;
            boolean a = filled[i];
            boolean b = sf[i];
            if (a && !b) return -1;
            if (!a && b) return +1;
            if (a) {
                // both fields are filled so if the field is a LOB consider
                // value nullness
                JdbcField jdbcField = (JdbcField)cmd.stateFields[i].storeField;
                if (jdbcField.isOracleStyleLOB()) {
                    a = isNull(i);
                    b = s.isNull(i);
                    if (a && !b) return -1;
                    if (!a && b) return +1;
                }
            }
        }
        return 0;
    }

    /**
     * Populate this State from the given ResultSet. The firstCol parameter
     * specifies the column index of the first column to read from rs. All
     * persistent fields in the fetch group must be read in order.
     */
    public void copyPass1Fields(ResultSet rs, FetchGroup fetchGroup,
            int firstCol) {
        FetchGroupField[] a = fetchGroup.fields;
        for (int i = 0; i < a.length; i++) {
            try {
                FieldMetaData fmd = a[i].fmd;
                if (!fmd.primaryField) continue;
                JdbcField f = (JdbcField)fmd.storeField;
                int fieldNo = f.stateFieldNo;
                firstCol = getFieldData(f, rs, firstCol, fieldNo);
                filled[fieldNo] = true;
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().datastore("Error reading field " + a[i].fmd.getQName() +
                        " from ResultSet: " + e,
                        e);
            }
        }
    }

    public void copyPass1Fields(ResultSet rs, JdbcField[] fields) {
        JdbcField f = null;
        for (int i = 0; i < fields.length; i++) {
            try {
                f = fields[i];
                if (f == null) continue;
                //ignore fields that are not in the statefields
                if (f.stateFieldNo < cmd.stateFields.length
                        && cmd.stateFields[f.stateFieldNo] == f.fmd) {
                    getFieldData(f, rs, i + 1, f.stateFieldNo);
                    filled[f.stateFieldNo] = true;
                }
            } catch (SQLException e) {
                throw BindingSupportImpl.getInstance().datastore("Error reading field "
                        + f == null ? "" : f.fmd.getQName() +
                        " from ResultSet: " + e, e);
            }
        }
    }

    private int getFieldData(JdbcField f, ResultSet rs, int firstCol,
            int fieldNo) throws SQLException {
        if (f instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField)f).col;
            if (Debug.DEBUG) {
                if (!c.name.toUpperCase().equals(
                        rs.getMetaData().getColumnName(firstCol).toUpperCase())) {
                    throw BindingSupportImpl.getInstance().internal(
                            "Reading the wrong column: " + firstCol + " \nrs field = "
                            + rs.getMetaData().getColumnName(firstCol) + "\nmetaData field = " + c.name);
                }
            }
            if (c.converter != null) {
                data[fieldNo] = c.converter.get(rs, firstCol++, c);
            } else {
                data[fieldNo] = JdbcUtils.get(rs, firstCol++, c.javaTypeCode,
                        c.scale);
                if (rs.wasNull()) {
                    data[fieldNo] = null;
                }
            }
        } else if (f instanceof JdbcRefField) {
            JdbcRefField rf = (JdbcRefField)f;
            JdbcOID oid = (JdbcOID)rf.targetClass.createOID(false);
            if (oid.copyKeyFields(rs, firstCol)) {
                data[fieldNo] = oid;
            } else {
                data[fieldNo] = null;
            }
            firstCol += rf.cols.length;
        } else if (f instanceof JdbcPolyRefField) {
            data[fieldNo] = getPolyRefOID(f, rs, firstCol);
            firstCol += ((JdbcPolyRefField)f).cols.length;
        } else {
            throw BindingSupportImpl.getInstance().internal("not implemented");
        }
        return firstCol;
    }

    /**
     * Set parameters on a PrepareStatement from this State. The firstParam
     * parameter specifies the column index of the first parameter to set.
     * Entries in fieldNos that are less than 0 should be skipped.
     *
     * @param firstFieldNo The index of the first state field to set
     * @param lastFieldNo  The index of the last state field to set + 1
     * @param tableNo      Set fields with table == jdbcClass.allTables[tableNo]
     * @return the index of the last param set + 1
     */
    public int setParams(PreparedStatement ps, int[] stateFieldNos,
            int firstFieldNo, int lastFieldNo, int firstParam,
            PersistGraph pGraph, int tableNo) throws SQLException {
        JdbcTable table = ((JdbcClass)cmd.storeClass).allTables[tableNo];
        for (int i = firstFieldNo; i < lastFieldNo; i++) {
            int fieldNo = stateFieldNos[i];
            if (fieldNo >= 0) {
                JdbcField field = (JdbcField)cmd.stateFields[fieldNo].storeField;
                if (field.mainTable == table) {
                    firstParam = setFieldData(field, ps, firstParam, fieldNo);
                }
            }
        }
        return firstParam;
    }

    /**
     * Set parameters on a PrepareStatement from this State for fields that
     * are not null. The firstParam parameter specifies the column index of
     * the first parameter. This will not be called for classes that are not
     * stored by the JdbcDataStore or that do not use changed optimistic
     * locking. Entries in fieldNos that are less than 0 should be skipped.
     *
     * @param firstFieldNo The index of the first field to set
     * @param lastFieldNo  The index of the last field to set + 1
     * @param tableNo
     * @return the index of the last param set + 1
     */
    public int setParamsChangedAndNotNull(PreparedStatement ps, int[] fieldNos,
            int firstFieldNo, int lastFieldNo, int firstParam,
            PersistGraph pGraph, int tableNo) throws SQLException {
        checkCmd();
        JdbcTable table = ((JdbcClass)cmd.storeClass).allTables[tableNo];
        for (int i = firstFieldNo; i < lastFieldNo; i++) {
            int fieldNo = fieldNos[i];
            if (fieldNo < 0 || data[fieldNo] == null) continue;
            JdbcField field = (JdbcField)cmd.stateFields[fieldNo].storeField;
            if (field.includeForChangedLocking && field.mainTable == table) {
                firstParam = setFieldData(field, ps, firstParam, fieldNo);
            }
        }
        return firstParam;
    }

    /**
     * Set parameters on a PrepareStatement from the optimistic locking field
     * for the class for this State. The firstParam parameter specifies the
     * column index of the first parameter to set.
     *
     * @return the index of the last param set + 1
     * @throws javax.jdo.JDOFatalInternalException
     *          if there is no such field
     * @see JdbcClass#optimisticLockingField
     */
    public int setOptimisticLockingParams(PreparedStatement ps,
            int firstParam) throws SQLException {
        checkCmd();
        JdbcSimpleField f = ((JdbcClass)cmd.storeClass).optimisticLockingField;
        if (f == null) {
            throw BindingSupportImpl.getInstance().internal("setOptimisticLockingParams " +
                    "called for non-optimistic locking class: " + cmd);
        }
        int fieldNo = f.stateFieldNo;
        // carl do not put this test into generated State class
        if (!containsField(fieldNo)) {
            throw BindingSupportImpl.getInstance().internal("setOptimisticLockingParams " +
                    "called for state not containing optimistic locking field: " +
                    cmd.qname + " " + f);
        }
        JdbcColumn c = f.col;
        if (c.converter != null) {
            c.converter.set(ps, firstParam++, c, data[fieldNo]);
        } else {
            JdbcUtils.set(ps, firstParam++, data[fieldNo], c.javaTypeCode,
                    c.jdbcType);
        }
        return firstParam;
    }

    private int setFieldData(JdbcField f, PreparedStatement ps, int firstParam,
            int fieldNo) throws SQLException {
        if (f instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField)f).col;
            if (c.isForUpdate()) {
                if (c.converter != null) {
                    c.converter.set(ps, firstParam++, c, data[fieldNo]);
                } else {
                    JdbcUtils.set(ps, firstParam++, data[fieldNo], c.javaTypeCode,
                            c.jdbcType);
                }
            }
        } else if (f instanceof JdbcRefField) {
            OID oid = (OID)data[fieldNo];
            if (oid == null || (oid = oid.getRealOID()) == null) {
                firstParam = setRefFieldToNull(f.mainTableCols, ps, firstParam);
            } else {
                firstParam = ((JdbcOID)oid).setParams(ps, firstParam, f.mainTableCols);
            }
        } else if (f instanceof JdbcPolyRefField) {
            firstParam = setPolyRefData(f, (OID)data[fieldNo], cmd, ps,
                    firstParam);
        } else {
            throw BindingSupportImpl.getInstance().internal("not implemented");
        }
        return firstParam;
    }

    /**
     * Call the set(rs,...) method on each of the converters for the first
     * numFieldNos entries in stateFieldNos. This is used to handle Oracle
     * style LOB columns.
     *
     * @param firstCol The first column in rs to use
     * @see com.versant.core.jdbc.JdbcConverter#set
     */
    public void setOracleStyleLOBs(ResultSet rs, int[] stateFieldNos,
            int numFieldNos, int firstCol) throws SQLException {
        for (int i = 0; i < numFieldNos; i++) {
            int fieldNo = stateFieldNos[i];
            JdbcField jdbcField = (JdbcField)cmd.stateFields[fieldNo].storeField;
            JdbcColumn c = ((JdbcSimpleField)jdbcField).col;
            c.converter.set(rs, firstCol++, c, data[fieldNo]);
        }
    }

    private com.versant.core.common.OID getPolyRefOID(
            com.versant.core.metadata.FieldMetaData fmd,
            java.sql.ResultSet rs,
            int firstCol) throws java.sql.SQLException {
        return getPolyRefOID((JdbcField)fmd.storeField, rs, firstCol);
    }

    public static com.versant.core.common.OID getPolyRefOID(
            com.versant.core.jdbc.metadata.JdbcField f,
            java.sql.ResultSet rs,
            int firstCol)
            throws java.sql.SQLException {
        com.versant.core.jdbc.metadata.JdbcPolyRefField pf =
                (com.versant.core.jdbc.metadata.JdbcPolyRefField)f;
        return pf.getData(rs, firstCol);
    }

    private int setPolyRefData(
            com.versant.core.metadata.FieldMetaData fmd,
            com.versant.core.common.OID oid,
            com.versant.core.metadata.ClassMetaData cmd,
            java.sql.PreparedStatement ps,
            int firstParam) throws java.sql.SQLException {
        return setPolyRefData((JdbcField)fmd.storeField, oid, cmd, ps, firstParam);
    }

    public static int setPolyRefData(
            com.versant.core.jdbc.metadata.JdbcField f,
            com.versant.core.common.OID oid,
            com.versant.core.metadata.ClassMetaData cmd,
            java.sql.PreparedStatement ps,
            int firstParam) throws java.sql.SQLException {
        com.versant.core.jdbc.metadata.JdbcPolyRefField pf =
                (com.versant.core.jdbc.metadata.JdbcPolyRefField)f;
        return pf.setData(ps, firstParam, oid);
    }

    public static int setRefFieldToNull(JdbcColumn[] cols,
            PreparedStatement ps, int firstParam) throws SQLException {
        int nc = cols.length;
        for (int i = 0; i < nc; i++) {
            JdbcColumn col = cols[i];
            if (col.isForUpdate()) {
                if (col.converter != null) {
                    col.converter.set(ps, firstParam++, col, null);       
                } else {
                    ps.setNull(firstParam++, col.jdbcType);
                }
            }
        }
        return firstParam;
    }

    public boolean hasSameNullFields(State state, State mask) {
        checkCmd();
        JdbcClass jc = (JdbcClass)cmd.storeClass;
        if (jc.optimisticLocking != JdbcClass.OPTIMISTIC_LOCKING_CHANGED) {
            return true;
        }
        JdbcGenericState s = (JdbcGenericState)state;
        JdbcGenericState ms = (JdbcGenericState)mask;
        int n = filled.length;
        for (int i = 0; i < n; i++) {
            if (ms.filled[i]) {
                if ((data[i] == null) != (s.data[i] == null)) return false;
            }
        }
        return true;
    }

}

