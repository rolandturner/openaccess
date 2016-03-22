
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

import com.versant.core.common.Debug;
import com.versant.core.common.OID;
import com.versant.core.common.QueryResultContainer;
import com.versant.core.common.State;
import com.versant.core.metadata.*;
import com.versant.core.server.*;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.query.JdbcCompiledQuery;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.fetch.*;



import java.sql.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.storagemanager.*;
import com.versant.core.jdo.QueryDetails;

/**
 * The results of a JDBC JDOQL or SQL query.
 */
public class JdbcQueryResult
        implements RunningQuery, ExecuteQueryReturn {

    private Connection con;
    protected final JdbcStorageManager sm;
    private boolean scrollable;

    public String sql;

    /**
     * The compiled query for this result.
     */
    protected JdbcCompiledQuery cq;

    private Object[] params;
    private boolean forUpdate;
    private int nextCount;

//    private final Object[] singleResult = new Object[1];

    /**
     * No iteration has been done on the query.
     */
    private static final int STATUS_NOT_STARTED = 0;
    /**
     * This query is being iterated over.
     */
    private static final int STATUS_BUSY = 1;
    /**
     * This query has been iterated over to the end.
     */
    private static final int STATUS_FINISHED = 2;

    /**
     * Container used for caching the results
     */
    CachedQueryResult qRCache;
    /**
     * The state of the result.
     *
     * @see #STATUS_NOT_STARTED
     * @see #STATUS_BUSY
     * @see #STATUS_FINISHED
     */
    private int status;
    /**
     * This is a flag to indicate if the results might be cached or may be retrieved from cache.
     * A true might change to false but a false may not become a true.
     */
    private boolean isCacheble;

    public JdbcQueryResult next;
    public JdbcQueryResult prev;

    private FetchResult fResult;

    public JdbcQueryResult(JdbcStorageManager sm, JdbcCompiledQuery cq,
            Object[] params, boolean cachable) {
        this.sm = sm;
        this.cq = cq;
        this.params = params;
        isCacheble = cachable && cq.isCacheble();
    }

    public void init(ClassMetaData cmd, boolean scrollable) {
        this.scrollable = scrollable;
    }

    public RunningQuery getRunningQuery() {
        return this;
    }

    public int getRelativeResultCount() {
        return nextCount;
    }

    public void resetRelativeResultCount() {
        nextCount = 0;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void cancel() {
        //todo not implemented
//        try {
//            ps.cancel();
//        } catch (NullPointerException e) {
//            //ignore
//        } catch (SQLException e) {
//            throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
//        }
    }

    public CompiledQuery getCompiledQuery() {
        return cq;
    }

    public JdbcCompiledQuery getJdbcCompiledQuery() {
        return cq;
    }

    public boolean isClosed() {
        if (fResult == null) return false;
        return fResult.isClosed();
//        return ps == null;
    }

    /**
     * Close these results.
     */
    public void close() {
        if (fResult != null) fResult.close();

        setNonCacheble();
        status = STATUS_FINISHED;

        qRCache = null;
        params = null;
    }

    /**
     * Are there more results? Calling this advances to the next result if
     * there is one and returns true otherwise it returns false.
     */
    public boolean next(int skip) {
        if (skip != 0) {
            return fResult.skip(skip);
        } else {
            return fResult.hasNext();
        }
    }

    public boolean isRandomAccess() {
        return scrollable;
    }

    public int getResultCount() {
        if (isNotStarted()) {
            updateCacheble();
            executeQueryImp();
            if (isCacheble()) {
                qRCache = new CachedQueryResult();
            }
        }
        return fResult.getAbsoluteCount();
    }

    /**
     * Get fetchAmount of data if possible.
     * @return Return true if the there is no more data in the result set.
     */
    private boolean fetchNextResultBatch(ApplicationContext context,
            QueryResultContainer results, int fetchAmount) {
        try {
            if (cq.isSqlQuery()) {
                if (fetchAmount == -1) {
                    //get all results
                    while (fResult.hasNext()) {
                        Object[] row = (Object[]) fResult.next(results.container);
                        results.addRow(row[0]);
                    }
                    fResult.fetchPass2(results.container);
                    fResult.close();
                    return true;
                } else {
                    for (int i = 0; i < fetchAmount && fResult.hasNext(); i++) {
                        Object[] row = (Object[]) fResult.next(results.container);
                        results.addRow(row[0]);
                    }
                    fResult.fetchPass2(results.container);
                    return closeFRAfterBatch();
                }
            }

            ProjectionQueryDecoder decoder = cq.getProjectionDecoder();
            /**
             * This is a normal query for a instance with no supplied projection
             * or aggregate info('result' is null or equal to default).
             *
             * In this scenario nothing is added to the 'result' as the
             * StatesReturned will already contain all the data.
             */
            if (decoder == null) {
                return fetchManagedTypes(context, fetchAmount, results);
            } else {
                if (fetchAmount == -1) {
                    //get all results
                    while (fResult.hasNext()) {
                        Object[] row = (Object[]) fResult.next(results.container);
                        results.addRow(row[0]);
                    }
                    fResult.fetchPass2(results.container);
                    fResult.close();
                    return true;
                } else {
                    for (int i = 0; i < fetchAmount && fResult.hasNext(); i++) {
                        Object[] row = (Object[]) fResult.next(results.container);
                        results.addRow(row[0]);
                    }
                    fResult.fetchPass2(results.container);
                    return closeFRAfterBatch();
                }
            }
        } catch (Exception e) {
            throw sm.handleException(e);
        }
    }

    /**
     * Fill the results with managed instances(oid-state).
     */
    private boolean fetchManagedTypes(ApplicationContext context,
            int fetchAmount, QueryResultContainer results) {
        if (fetchAmount == -1) {
            //get all results
            while (fResult.hasNext()) {
                Object[] row = (Object[]) fResult.next(results.container);
                OID oid = (OID) row[0];
                State state = (State) row[1];
                results.container.add(oid, state);
                results.addRow(oid);
            }
            fResult.fetchPass2(results.container);
            fResult.close();
            return true;
        } else {
            for (int i = 0; i < fetchAmount && fResult.hasNext(); i++) {
                Object[] row = (Object[]) fResult.next(results.container);
                OID oid = (OID) row[0];
                State state = (State) row[1];
                results.container.add(oid, state);
                results.addRow(oid);
            }
            fResult.fetchPass2(results.container);
            return closeFRAfterBatch();
        }
    }

    private boolean closeFRAfterBatch() {
        return !fResult.hasNext();
    }

    public int getFieldData(FieldMetaData fmd, ResultSet rs, int firstCol,
            Object[] dataRow, int dataIndex) throws SQLException {
        JdbcField f = (JdbcField)fmd.storeField;
        if (f instanceof JdbcSimpleField) {
            JdbcColumn c = ((JdbcSimpleField)f).col;
            if (Debug.DEBUG) {
                if (!cq.isProjectionQuery() && !c.name.toUpperCase().equals(
                        rs.getMetaData().getColumnName(firstCol).toUpperCase())) {
                    throw BindingSupportImpl.getInstance().internal(
                            "Reading the wrong column: \nrs field = "
                            + rs.getMetaData().getColumnName(firstCol) + "\nmetaData field = " + c.name);
                }
            }
            if (c.converter != null) {
                dataRow[dataIndex] = c.converter.get(rs, firstCol++, c );
            } else {
                dataRow[dataIndex] = JdbcUtils.get(rs, firstCol++, c.javaTypeCode,
                        c.scale );
                if (rs.wasNull()) {
                    dataRow[dataIndex] = null;
                }
            }
        } else if (f instanceof JdbcRefField) {
            JdbcRefField rf = (JdbcRefField)f;
            JdbcOID oid = (JdbcOID)rf.targetClass.createOID(false);
            if (oid.copyKeyFields(rs, firstCol)) {
                dataRow[dataIndex] = oid;
            } else {
                dataRow[dataIndex] = null;
            }
            firstCol += rf.cols.length;
        } else if (f instanceof JdbcPolyRefField) {
            dataRow[dataIndex] = JdbcGenericState.getPolyRefOID(f, rs, firstCol);
            firstCol += ((JdbcPolyRefField)f).cols.length;
        } else {
            throw BindingSupportImpl.getInstance().internal("not implemented");
        }
        return firstCol;
    }

    /**
     * If the cache can be checked for results represented by this query.
     */
    public boolean isCachedResultsOk() {
        return isCacheble && status == STATUS_NOT_STARTED;
    }

    private void setQResult(QueryResultContainer qContainer) {
        if (Debug.DEBUG) {
            if (status != STATUS_NOT_STARTED) {
                throw BindingSupportImpl.getInstance().internal(
                        "query already started");
            }
        }
        status = STATUS_BUSY;
        if (!fResult.hasNext()) {
            qContainer.qFinished = fResult.hasNext();
            status = STATUS_FINISHED;
        }

//        if (cq.isRandomAccess()) {
//            if (!absolute(0)) {
//                status = STATUS_FINISHED;
//                qContainer.qFinished = true;
//            }
//        } else {
//            if (!next(0)) {
//                status = STATUS_FINISHED;
//                qContainer.qFinished = true;
//            }
//        }
    }

    public boolean isNotStarted() {
        return status == STATUS_NOT_STARTED;
    }

    public void getAbsolute(ApplicationContext context,
            QueryResultContainer qContainer, int index, int fetchAmount) {
        if (isNotStarted()) {
            executeQueryImp();
            setQResult(qContainer);
        }

        resetRelativeResultCount();
        if (Debug.DEBUG) {
            if (!isRandomAccess()) {
                throw BindingSupportImpl.getInstance().internal("getAbsolute may only " +
                        "be called on randomAccess queries");
            }
        }

        if (fResult.absolute(index + 1)) {
            if (fetchNextResultBatch(context, qContainer, fetchAmount)) {
                status = STATUS_FINISHED;
            }
        }

        if (status == STATUS_FINISHED) {
            qContainer.qFinished = true;
        }
    }

    /**
     * Get the next batch of results. Returns true if there are no more
     * (i.e. this query should be closed).
     */
    public boolean nextBatch(ApplicationContext context, int skipAmount,
            QueryResultContainer qContainer) {
        prepare(qContainer);
        updateCacheble();


        //get the rs at the correct spot
        if (skipAmount != 0) {
            setNonCacheble();
            if (!next(skipAmount)) {
                status = STATUS_FINISHED;
            }
        }

        if (status != STATUS_FINISHED) {
            if (fetchNextResultBatch(context, qContainer, cq.getQueryResultBatchSize())) {
                status = STATUS_FINISHED;
                fResult.close();
            }

            if (qRCache != null) {
                qContainer.addResultsTo(qRCache,
                        cq.isCopyResultsForCache());
            }
        }
        if (status == STATUS_FINISHED) {
            qContainer.qFinished = true;
        }
        return status == STATUS_FINISHED;
    }

    private void prepare(QueryResultContainer qContainer) {
        if (isNotStarted()) {
            executeQueryImp();
            setQResult(qContainer);
            if (isCacheble()) {
                qRCache = new CachedQueryResult();
            }
        } else {
            resetRelativeResultCount();
        }
    }

    /**
     * This is called to recheck if this results is still cacheble.
     */
    public void updateCacheble() {
        if (isCacheble) {
            if (sm.isActive() && !sm.isOptimistic() || sm.isFlushed()
                    || !sm.getCache().isQueryCacheEnabled()) {
                setNonCacheble();
            }
        }
    }

    public boolean isFinished() {
        return status == STATUS_FINISHED;
    }

    public boolean isCacheble() {
        return isCacheble;
    }

    public QueryDetails getQueryDetails() {
        return cq.getQueryDetails();
    }

    private void executeQueryImp() {
        executePrepare(sm.isForUpdate(), sm.getSqlDriver());
        executeActual();
    }

    private void executePrepare(boolean forUpdate, SqlDriver sqlDriver) {
        this.forUpdate = forUpdate;
        JdbcCompiledQuery cq = getJdbcCompiledQuery();
        ClassMetaData cmd = cq.getCmd();
        boolean randomAccess = cq.isRandomAccess();
        if (randomAccess && !sqlDriver.isScrollableResultSetSupported()) {
            throw BindingSupportImpl.getInstance().datastore("Random access not supported for " +
                    sqlDriver.getName() + " using JDBC driver " +
                    sm.getJdbcConnectionSource().getDriverName());
        }
        init(cmd, randomAccess);
        boolean ok = false;
        try {
            con = sm.con();
            ok = true;
        } catch (Exception x) {
            throw sm.handleException(x);
        } finally {
            if (!ok) {
                try {
                    close();
                } catch (Exception x) {
                    // ignore as we are already in an exception
                }
            }
        }
    }

    private void executeActual() {
        JdbcCompiledQuery cq = getJdbcCompiledQuery();
        boolean ok = false;
        try {
            try {
                if (cq.isSqlQuery()) {
                    fResult = createFetchResult(con, sm.getSqlDriver(), scrollable,
                            cq.getFetchSpec(), cq, sm, params, cq.getQueryResultBatchSize(), cq.getMaxRows());
                } else {
                    fResult = cq.getFetchSpec().createFetchResult(sm, con, params,
                            forUpdate, false, 0, 0, cq.getQueryResultBatchSize(),
                            scrollable, cq.getMaxRows());
                }
            } catch (Exception e) {
                throw sm.handleException("Query failed: " + JdbcUtils.toString(
                        e) + "\n" +
                        JdbcUtils.getPreparedStatementInfo(sql, null),
                        e);
            }

            ok = true;
        } finally {
            if (!ok) {
                try {
                    close();
                } catch (Exception x) {
                    // ignore as we are already in an exception
                }
            }
        }
    }

    /**
     * If at any stage it is detected that the results may not be cached this
     * this method is called.
     */
    public void setNonCacheble() {
        isCacheble = false;
        qRCache = null;
    }

    /**
     * Todo get rid of this horrible hack when we refactor all the query stuff
     */
    public boolean isEJBQLHack() {
        return false;
    }

    public void getAllResults(ApplicationContext context,
            QueryResultContainer container, boolean forUpdate) {
        try {
            executeQueryImp();
            resetRelativeResultCount();

            if (next(0)) {
                fetchNextResultBatch(context, container, -1);
            }
        } finally {
            close();
        }
    }

    private static FetchResult createFetchResult(Connection con, SqlDriver sqlDriver,
            boolean scrollable, FetchSpec fetchSpec, JdbcCompiledQuery cq,
            JdbcStorageManager sm, Object[] params, int fetchSize, int maxRows) {
        ResultSet rs = null;
        boolean error = true;
        PreparedStatement ps = null;
        try {
            if (scrollable && !sqlDriver.isScrollableResultSetSupported()) {
                throw BindingSupportImpl.getInstance().datastore("Scrollable ResultSet's not supported for " +
                        sqlDriver.getName() + " using JDBC driver " +
                        sm.getJdbcConnectionSource().getDriverName());
            }
            final int resultSetType =
                    scrollable ? ResultSet.TYPE_SCROLL_INSENSITIVE : ResultSet.TYPE_FORWARD_ONLY;

            if (cq.isStoredProc()) {
                CallableStatement cs = con.prepareCall(cq.getQueryDetails().getFilter(), resultSetType,
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
                if (maxRows > 0 && fetchSize > maxRows) {
                    fetchSize = maxRows;
                }
                try {
                    ps.setFetchSize(fetchSize);
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
                            throw BindingSupportImpl.getInstance().invalidOperation("Query may have only one OUT parameter");
                        }
                        rs = (ResultSet) ((CallableStatement)
                                ps).getObject(i + 1);
                        cq.getMappingInfo(rs);
                        cursorSet = true;
                    }
                }
                if (!cursorSet) {
                    rs = (ResultSet) ((CallableStatement)
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
                throw BindingSupportImpl.getInstance().invalidOperation("Candidate class '"
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
            throw sm.getSqlDriver().mapException(e, e.toString(), true);
        } finally {
            if (error && ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }


}

