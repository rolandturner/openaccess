
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

import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.common.OID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * JDBC specific methods for OID.
 */
public interface JdbcOID extends OID {

    /**
     * Populate this OID from the given ResultSet. The firstCol parameter
     * specifies the column index of the first column to read from rs. If
     * the primary key consists of multiple columns then they will be
     * present in the same order as defined in the meta data.
     *
     * @return False if OID is 'null', true otherwise
     */
    public boolean copyKeyFields(ResultSet rs, int firstCol)
            throws SQLException;

    public boolean copyKeyFields(ResultSet rs, JdbcField[] pks,
            int[] pkFieldIndexs) throws SQLException;

    public boolean validateKeyFields(ResultSet rs, int firstCol)
            throws SQLException;

    /**
     * Set parameters on a PrepareStatement from this OID. The firstParam
     * parameter specifies the column index of the first parameter to set.
     * If the primary key consists of multiple parameters then they must
     * all be set in the same order as defined in the meta data. The new
     * firstParam value must be returned i.e. if firstParam started as 3 and
     * our pk consists of 2 columns then 5 must be returned.
     */
    public int setParams(PreparedStatement ps, int firstParam)
            throws SQLException;

    /**
     * Set parameters on a PrepareStatement from this OID. Columns in pkc
     * that return false from isForUpdate() should be ignored.
     */
    public int setParams(PreparedStatement ps, int firstParam,
            JdbcColumn[] pkc) throws SQLException;

}

