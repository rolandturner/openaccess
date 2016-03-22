
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

import com.versant.core.common.NewObjectOID;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.jdbc.metadata.JdbcField;
import com.versant.core.jdbc.metadata.JdbcColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Adds JDBC specific methods that pass through to the real OID. This saves
 * having to call getRealOID() before casting to JdbcOID in a lot of the JDBC
 * code.
 */
public final class JdbcNewObjectOID extends NewObjectOID implements JdbcOID {

    public JdbcNewObjectOID() {
    }

    public JdbcNewObjectOID(ClassMetaData cmd) {
        super(cmd);
    }

    public NewObjectOID newInstance(ClassMetaData cmd) {
        return new JdbcNewObjectOID(cmd);
    }

    public boolean copyKeyFields(ResultSet rs, int firstCol)
            throws SQLException {
        return ((JdbcOID)realOID).copyKeyFields(rs, firstCol);
    }

    public boolean copyKeyFields(ResultSet rs, JdbcField[] pks,
            int[] pkFieldIndexs) throws SQLException {
        return ((JdbcOID)realOID).copyKeyFields(rs, pks, pkFieldIndexs);
    }

    public boolean validateKeyFields(ResultSet rs, int firstCol)
            throws SQLException {
        return ((JdbcOID)realOID).validateKeyFields(rs, firstCol);
    }

    public int setParams(PreparedStatement ps, int firstParam)
            throws SQLException {
        return ((JdbcOID)realOID).setParams(ps, firstParam);
    }

    public int setParams(PreparedStatement ps, int firstParam,
            JdbcColumn[] pkc) throws SQLException {
        return ((JdbcOID)realOID).setParams(ps, firstParam, pkc);
    }

}

