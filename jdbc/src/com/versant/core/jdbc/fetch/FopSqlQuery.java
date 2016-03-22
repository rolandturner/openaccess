
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
package com.versant.core.jdbc.fetch;

import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.query.JdbcCompiledQuery;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.jdbc.JdbcState;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.server.StateContainer;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;

import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * FetchOp to fetch sql queries.
 */
public class FopSqlQuery extends FetchOp {
    /**
     * If a candidate class was speficied for the query then this will be its cmd
     * else it will be null.
     */
    private final ClassMetaData cmd;
    private final FetchGroup fetchGroup;
    private final JdbcCompiledQuery cq;

    public FopSqlQuery(FetchSpec spec, ClassMetaData cmd, FetchGroup fg,
            JdbcCompiledQuery cq) {
        super(spec);
        this.cmd = cmd;
        this.fetchGroup = fg;
        this.cq = cq;
    }

    public SqlExp init(SelectExp root) {
        return null;
    }

    /**
     * This will fetch a row of a sql query. A sql query is either a query with user
     * supplied sql of a stored procedure that is executed.
     */
    public void fetch(FetchResult fetchResult, StateContainer stateContainer)
            throws SQLException {
        PreparedStatement ps = fetchResult.getPreparedStatement();
        ResultSet rs = fetchResult.getResultSet();

        /**
         * If this query contains 'out' params then we will return a Object[]
         * with length the same as the amount of 'out' and 'out_cursor' params.
         *
         * Should we return a single result.
         */
        if (cq.isUnique()) {
            int[] paramDir = cq.getParamDirection();
            Object[] outResult = new Object[cq.getOutParamCount()];
            int outCount = 0;
            for (int i = 0; i < paramDir.length; i++) {
                if (paramDir[i] == JdbcCompiledQuery.PARAM_OUT) {
                    outResult[outCount++] = ((CallableStatement)ps).getObject(i + 1);
                } else if (paramDir[i] == JdbcCompiledQuery.PARAM_OUT_CURSOR) {
                    //if there is mapping info available then map it to a class
                    //else create an object[] with all the cols read as getObject
                    JdbcCompiledQuery.MappingInfo mi = cq.getMappingInfo(rs);
                    if (mi.isPkValid()) {
                        //if this can be resolved to oid-state
                        throw BindingSupportImpl.getInstance().unsupported(
                                "Mapping of managed instances for storedprocedure " +
                                "OUT_CURSOR is not supported");
                    } else {
                        outResult[outCount++] = fetchAsObjects(mi, rs);
                    }
                }
            }
            fetchResult.setData(this, outResult);
        } else {
            final JdbcCompiledQuery.MappingInfo mi = cq.getMappingInfo(rs);
            if (mi.isPkValid()) {
                ClassMetaData specificCmd = cmd;
                //fetch this as if this was a normal query for a managed type.
                OID oid = specificCmd.createOID(false);
                if (specificCmd.pkFields == null) {
                    ((JdbcOID)oid).copyKeyFields(rs, mi.dsPkIndex);
                } else {
                    ((JdbcOID)oid).copyKeyFields(rs, mi.fields,
                            cq.getMappingInfo(rs).pkIndexInFieldsArray);
                }

                /**
                 * This will not work if the class-id is not after the oid and before the fields because
                 * this read will skip some fields which can not be read twice.
                 */
                if (mi.discrIndex != 0) {
                    Object classId = ((JdbcClass)specificCmd.storeClass).classIdCol.get(rs, mi.discrIndex);
                    if (rs.wasNull()) {
                        throw BindingSupportImpl.getInstance().objectNotFound("No row for " +
                                cmd.qname + " " + oid.toSString() + " OR "
                                + ((JdbcClass)cmd.storeClass).classIdCol.name +
                                " is null for row");
                    }
                    specificCmd = ((JdbcClass)cmd.storeClass).findClass(classId);
                    if (specificCmd == null) {
                        throw BindingSupportImpl.getInstance().fatalDatastore(
                                "Row for OID " + oid.toSString() +
                                        " is not in the heirachy starting at " +
                                        cmd.qname +
                                        " (" + ((JdbcClass)cmd.storeClass).classIdCol.name
                                        + " for row is " + classId + ")");
                    }
                }

                JdbcState state = (JdbcState) specificCmd.createState();
                state.copyPass1Fields(rs, mi.fields);
                oid.resolve((State) state);
                stateContainer.add(oid, (State) state);
//                fetchResult.getStorageManager().getState(oid, fetchGroup, stateContainer);
                fetchResult.setData(this, oid);
            } else {
                //just return it as a object[] as we could not determine the pk column etc.
                int colCount = mi.colCount;
                Object[] oa = new Object[colCount];
                for (int j = 0; j < colCount; j++) {
                    oa[j] = rs.getObject(j + 1);

                }
                fetchResult.setData(this, oa);
            }
        }
    }

    /**
     * Fetch the entire resultset and return as a list. The list contains object[]
     * that represent the rows.
     */
    private static List fetchAsObjects(JdbcCompiledQuery.MappingInfo mi, ResultSet rs)
            throws SQLException {
        int colCount = mi.colCount;
        ArrayList rowData = new ArrayList();
        do {
            Object[] oa = new Object[colCount];
            for (int j = 0; j < colCount; j++) {
                oa[j] = rs.getObject(j + 1);
            }
            rowData.add(oa);
        } while (rs.next());
        return rowData;
    }

    public Object getResult(FetchResult fetchResult) {
        return fetchResult.getData(this);
    }

    public FetchOpData getOutputData() {
        return null;
    }

    public String getDescription() {
        return null;
    }
}
