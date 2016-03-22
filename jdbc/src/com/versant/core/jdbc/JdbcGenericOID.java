
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

import com.versant.core.common.GenericOID;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Adds JDBC specific methods to GenericOID.
 */
public class JdbcGenericOID extends GenericOID implements JdbcOID {

    public JdbcGenericOID() {
    }

    public JdbcGenericOID(ClassMetaData cmd, boolean resolved) {
        super(cmd, resolved);
    }

    /**
     * Populate this OID from the given ResultSet. The firstCol parameter
     * specifies the column index of the first column to read from rs. If
     * the primary key consists of multiple columns then they will be
     * present in the same order as defined in the meta data.
     *
     * @return False if OID is 'null', true otherwise
     */
    public boolean copyKeyFields(ResultSet rs, int firstCol)
            throws SQLException {
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            JdbcColumn c = pkc[i];
            if (c.converter != null) {
                pk[i] = c.converter.get(rs, firstCol++, c);
            } else {
                pk[i] = JdbcUtils.get(rs, firstCol++, c.javaTypeCode, c.scale);
            }
            if (rs.wasNull()) return false;
        }
        return true;
    }

    /**
     * This is for appId
     */
    public boolean copyKeyFields(ResultSet rs, JdbcField[] pks, int[] pkFieldIndexs)
            throws SQLException {
        for (int j = 0; j < pkFieldIndexs.length; j++) {
            JdbcColumn c = pks[pkFieldIndexs[j]].mainTableCols[0];
            if (c.converter != null) {
                pk[j] = c.converter.get(rs, pkFieldIndexs[j] + 1, c);
            } else {
                pk[j] = JdbcUtils.get(rs, pkFieldIndexs[j] + 1, c.javaTypeCode, c.scale);
            }
            if (rs.wasNull()) return false;
        }
        return true;
    }

    /**
     * Return false if oid is null.
     */
    public boolean validateKeyFields(ResultSet rs, int firstCol) throws SQLException {
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            JdbcColumn c = pkc[i];
            if (c.converter != null) {
                c.converter.get(rs, firstCol++, c);
            } else {
                JdbcUtils.get(rs, firstCol++, c.javaTypeCode, c.scale);
            }
            if (rs.wasNull()) return false;
        }
        return true;
    }

    /**
     * Set parameters on a PrepareStatement from this OID. The firstParam
     * parameter specifies the column index of the first parameter to set.
     * If the primary key consists of multiple parameters then they must
     * all be set in the same order as defined in the meta data. The new
     * firstParam value must be returned i.e. if firstParam started as 3 and
     * our pk consists of 2 columns then 5 must be returned.
     */
    public int setParams(PreparedStatement ps, int firstParam)
            throws SQLException {
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            JdbcColumn c = pkc[i];
            if (c.converter != null) {
                c.converter.set(ps, firstParam++, c, pk[i]);
            } else {
                JdbcUtils.set(ps, firstParam++, pk[i], c.javaTypeCode,
                        c.jdbcType);
            }
        }
        return firstParam;
    }

    /**
     * Set parameters on a PrepareStatement from this OID. Columns in pkc
     * that return false from isForUpdate() should be ignored.
     */
    public int setParams(PreparedStatement ps, int firstParam,
            JdbcColumn[] pkc)
            throws SQLException {
        for (int i = 0; i < pkc.length; i++) {
            JdbcColumn c = pkc[i];
            if (c.isForUpdate()) {
                if (c.converter != null) {
                    c.converter.set(ps, firstParam++, c, pk[i]);
                } else {
                    JdbcUtils.set(ps, firstParam++, pk[i], c.javaTypeCode,
                            c.jdbcType);
                }
            }
        }
        return firstParam;
    }

    /**
     * Util method to 'setNull' for oid param.
     */
    public static int setNullParams(PreparedStatement ps,
            int firstParam, ClassMetaData cmd) throws SQLException {
        JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
        for (int i = 0; i < pkc.length; i++) {
            ps.setNull(firstParam++, pkc[i].jdbcType);
        }
        return firstParam;
    }

    protected GenericOID newInstance() {
        return new JdbcGenericOID();
    }

    public String toSString() {
        StringBuffer s = new StringBuffer();
        s.append("GenericOID@");
        s.append(Integer.toHexString(System.identityHashCode(this)));
        s.append(' ');
        if (cmd == null) {
            s.append("classIndex ");
            s.append(cmd.index);
        } else {
            String n = cmd.qname;
            int i = n.lastIndexOf('.');
            if (i >= 0) n = n.substring(i + 1);
            s.append(n);
            s.append(' ');
            JdbcColumn[] pkc = ((JdbcClass)cmd.storeClass).table.pk;
            for (i = 0; i < pkc.length; i++) {
                JdbcColumn c = pkc[i];
                s.append(c.name);
                s.append('=');
                s.append(pk[i]);
            }
        }
        if (!isResolved()) s.append(" NOTRES ");
        return s.toString();
    }

}

