
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

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.query.JdbcCompiledQuery;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdo.QueryDetails;

import java.sql.*;

/**
 *
 */
public class FetchFactorySqlQuery extends FetchResultFactory {
    private final SqlDriver sqlDriver;
    private final boolean scrollable;
    private final FetchSpec fetchSpec;
    private final JdbcCompiledQuery cq;
    private final JdbcStorageManager sm;
    private final Object[] params;
    private ResultSet rs;
    private final int fetchSize;
    private final int maxRows;

    public FetchFactorySqlQuery(SqlDriver sqlDriver, boolean scrollable,
                                FetchSpec fetchSpec, JdbcCompiledQuery cq,
                                JdbcStorageManager sm, Object[] params,
                                int fetchSize, int maxRows) {
        this.sqlDriver = sqlDriver;
        this.scrollable = scrollable;
        this.fetchSpec = fetchSpec;
        this.cq = cq;
        this.sm = sm;
        this.params = params;
        this.fetchSize = fetchSize;
        this.maxRows = maxRows;
    }


    public FetchResult createFetchResult(Connection con) {
        boolean error = true;
        PreparedStatement ps = null;
        try {
            if (scrollable && !sqlDriver.isScrollableResultSetSupported()) {
                throw BindingSupportImpl.getInstance().datastore(
                        "Scrollable ResultSet's not supported for " +
                        sqlDriver.getName() + " using JDBC driver " +
                        sm.getJdbcConnectionSource().getDriverName());
            }
            final int resultSetType =
                    scrollable ? ResultSet.TYPE_SCROLL_INSENSITIVE: ResultSet.TYPE_FORWARD_ONLY;

            if (cq.isStoredProc()) {
                CallableStatement cs = con.prepareCall(
                        cq.getQueryDetails().getFilter(), resultSetType,
                        ResultSet.CONCUR_READ_ONLY);
                ps = cs;
                int[] sqlTypes = cq.getSqlTypes();
                int[] paramDir = cq.getParamDirection();

                final int count = sqlTypes.length;
                for (int i = 0; i < count; i++) {
                    if (paramDir[i] == JdbcCompiledQuery.PARAM_IN) {
                        cs.setObject(i + 1, params[i], sqlTypes[i]);
                    } else if (paramDir[i] == JdbcCompiledQuery.PARAM_OUT) {
                        cs.registerOutParameter(i + 1, sqlTypes[i]);
                    } else if (paramDir[i] == JdbcCompiledQuery.PARAM_OUT_CURSOR) {
                        cs.registerOutParameter(i + 1, -10);
                    }
                }

            } else if (cq.isDirectSql()) {
                ps = con.prepareStatement(cq.getQueryDetails().getFilter(),
                        resultSetType, ResultSet.CONCUR_READ_ONLY);
                int[] sqlTypes = cq.getSqlTypes();
                final int count = sqlTypes.length;
                for (int i = 0; i < count; i++) {
                    ps.setObject(i + 1, params[i], sqlTypes[i]);
                }
            }

            if (maxRows > 0) {
                try {
                    ps.setMaxRows(maxRows);
                } catch (SQLException e) {
                    throw sqlDriver.mapException(e, e.toString(), true);
                }
            }
            if (fetchSize > 0) {
                int sz = fetchSize;
                if (maxRows > 0 && sz > maxRows) {
                    sz = maxRows;
                }
                try {
                    ps.setFetchSize(sz);
                } catch (Exception e) {
                    throw sqlDriver.mapException(e, e.toString(), true);
                }
            }

            if (cq.isStoredProc() && sm.getSqlDriver().isOracleStoreProcs()) {
                ps.executeUpdate();
                int[] paramDir = cq.getParamDirection();
                boolean cursorSet = false;
                QueryDetails queryDetails = cq.getQueryDetails();
                final int count = queryDetails.getParamCount();
                for (int i = 0; i < count; i++) {
                    if (paramDir[i] == JdbcCompiledQuery.PARAM_OUT_CURSOR) {
                        if (cursorSet) {
                            throw BindingSupportImpl.getInstance().invalidOperation(
                                    "Query may have only one OUT parameter");
                        }
                        rs = (ResultSet)((CallableStatement)
                                ps).getObject(i + 1);
                        cq.getMappingInfo(rs);
                        cursorSet = true;
                    }
                }
                if (!cursorSet) {
                    rs = (ResultSet)((CallableStatement)
                            ps).getObject(count + 1);
                    cq.getMappingInfo(rs);
                }
            } else {
                rs = ps.executeQuery();
                cq.getMappingInfo(rs);
            }

            /**
             * Check for a valid pk mapping
             */
            if (!cq.getMappingInfo(rs).isPkValid()
                    && cq.getQueryDetails().getCandidateClass() != null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Candidate class '"
                        + cq.getQueryDetails().getCandidateClass().getName()
                        + "' was specified, "
                        + "but the ResultSet does not contain any/all of the pk columns.");
            }

            FetchResult ans = new FetchResultImp(fetchSpec, ps, rs,
                    cq.getQueryDetails().getFilter(), scrollable,
                    sm, con, false, false, params);
            error = false;
            return ans;
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        } finally {
            if (error && ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
        return null;
    }
}
