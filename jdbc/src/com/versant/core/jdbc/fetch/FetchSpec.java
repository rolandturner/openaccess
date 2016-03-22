
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

import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.sql.exp.SqlExp;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdbc.JdbcOID;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.server.StateContainer;

import java.io.PrintStream;
import java.sql.*;

/**
 * This specifies how each row from a SQL query is processed and helps to
 * generate the query. A FetchSpec contains a list of FetchOp's, each of
 * which fetch something from the ResultSet. FetchOp's may provide data
 * to other subsequent FetchOp's so complex operations can be broken down
 * into simple operations. A FetchOp may have a nested FetchSpec of its own
 * to be executed once for each row or in parallel with the 'parent'
 * FetchSpec.
 */
public class FetchSpec {
    public static int POS_DEFAULT = 0;
    public static int POS_FIRST = 1;
    public static int POS_LAST = 2;

    private FetchOptions options = new FetchOptions();
    private FetchOp[] ops = new FetchOp[2];
    private int opCount;
    private FetchOp[] resultOps = new FetchOp[2];

    private int resultOpCount;
    private boolean singleObjectRow;

    private SqlDriver sqlDriver;
    private final SelectExp root;
    private SqlExp pos;
    private int totColumnCount;
    private boolean inAddFetchOp;

    private SqlBuffer sqlBuffer;
    private boolean finished;
    /**
     * If there is a oneToMany fetch. This info is required to skip over row's in the
     * resultset.
     */
    private boolean oneToMany;

    /**
     * A factory that can create the original filter for the query. This will
     * only be present on the root FetchSpec.
     */
    private FilterExpFactory filterExpFactory;

    /**
     * If this is a sub FetchSpec then this is the owner reference. It is used to
     * get to the original fetchspec.
     */
    private FetchSpec parent;
    /**
     * The cmd type of the root FetchSpec. This is used for parallel collection
     * fetching.
     */
    private ClassMetaData rootCmdType;
    private FetchOp lastOp;
    private FetchOp firstOp;
    /**
     * If this is set then this fetchSpec is for the fetch of a instance ant therefore
     * a single non-null oid param will be used as the param for the query.
     */
    private boolean instanceFetch;
    private FetchOpDiscriminator fopDiscr;
    private boolean sqlGenerated;

    public FetchSpec(SelectExp root, SqlDriver sqlDriver) {
//        if (root == null) {
//            throw BindingSupportImpl.getInstance().internal("");
//        }
        this.root = root;
        this.sqlDriver = sqlDriver;
    }

    public FetchSpec(SelectExp root, SqlDriver sqlDriver, boolean instanceFetch) {
        this(root, sqlDriver);
        this.instanceFetch = instanceFetch;
    }

    public boolean isInstanceFetch() {
        return instanceFetch;
    }

    /**
     * If this fetchSpec is a child FetchSpec.
     *
     * @param parent
     */
    public void setParentFetchSpec(FetchSpec parent) {
        this.parent = parent;
        instanceFetch = parent.instanceFetch;
        this.getOptions().setUseParallelQueries(parent.getOptions().isUseParallelQueries());
    }

    public SelectExp getJoinPathExp() {
        if (parent == null) return root;
        return null;
    }

    /**
     * Get the topmost SELECT for this spec.
     */
    public SelectExp getRoot() {
        return root;
    }

    public void prepend(FetchOp op) {
        addFetchOp(op, false);
        //move to the front
    }

    public int getProjectionIndex(FetchOp op) {
        return op.getFirstColIndex();
    }

    public void addFetchOpLast(FetchOp op) {
        if (lastOp != null) {
            throw BindingSupportImpl.getInstance().internal("There is " +
                    "already a FetchOp set to occopy the last pos");
        }
        lastOp = op;
        addFetchOpImp(op, false, false);
    }

    void addFetchOpImp(FetchOp op, boolean includeInResult, boolean prepend) {
        if (opCount == ops.length) {
            FetchOp[] a = new FetchOp[opCount * 2];
            System.arraycopy(ops, 0, a, 0, opCount);
            ops = a;
        }

        if (prepend) {
            if (firstOp != null) {
                throw BindingSupportImpl.getInstance().internal("Only one " +
                        "fetchOp may be prepended in a FetchOp");
            }
            firstOp = op;
        }

        op.setIndex(opCount);
        ops[opCount++] = op;
        if (includeInResult) {
            if (resultOpCount == resultOps.length) {
                FetchOp[] a = new FetchOp[resultOpCount * 2];
                System.arraycopy(resultOps, 0, a, 0, resultOpCount);
                resultOps = a;
            }
            resultOps[resultOpCount++] = op;
        }


        // Process newly added ops in a loop protected by a flag so that
        // recursively added ops are processed in add order. This ensures
        // that the ResultSet columns will be read in ascending order.
        if (!inAddFetchOp) {
            try {
                inAddFetchOp = true;
                for (int i = opCount - 1; i < opCount; i++) {
                    ops[i].seExp = ops[i].init(root);
                }
            } finally {
                inAddFetchOp = false;
            }
        }
    }

    /**
     * Do the last init stuff. No more fetchOp can be added at this stage.
     */
    public void finish(int offset) {
        if (finished) return;
        if (root != null) {
            root.normalize(sqlDriver, null, sqlDriver.isConvertExistsToDistinctJoin());
        }


        moveLastOp();
        moveFirstOp();
        for (int i = 0; i < ops.length; i++) {
            FetchOp op = ops[i];
            if (op == null) break;
            SqlExp e = op.seExp;
            op.setFirstColIndex(totColumnCount + offset);
            if (e != null) {
                if (pos == null) {
                    pos = root.selectList = e;
                    ++totColumnCount;
                } else {
                    pos.setNext(e);
                }

                for (; pos.getNext() != null; pos = pos.getNext()) {
                    ++totColumnCount;
                }
            }
        }
        finished = true;
        if (parent != null) {
            if (parent.getParamList() != null) setParamList(parent.getParamList().getClone());
        }
    }

    public void offSetColIndex(int offset) {
        for (int i = 0; i < opCount; i++) {
            FetchOp op = ops[i];
            op.offsetFirstColIndex(offset);    
        }
    }

    private void moveFirstOp() {
        if (firstOp != null) {
            //already first
            if (firstOp.getIndex() == 0) return;

            System.arraycopy(ops, 0, ops, 1, firstOp.getIndex());
            ops[0] = firstOp;
            for (int i = 0; i < opCount; i++) {
                ops[i].setIndex(i);
            }
        }
    }

    private void moveLastOp() {
        if (lastOp != null) {
            //if not already last
            if (lastOp.getIndex() == (opCount - 1)) {
                return;
            }
            System.arraycopy(ops, lastOp.getIndex() + 1, ops, lastOp.getIndex(),
                    (opCount - lastOp.getIndex()) - 1);
            ops[opCount - 1] = lastOp;
            for (int i = 0; i < opCount; i++) {
                if (ops[i] == null) break;
                ops[i].setIndex(i);
            }
        }
    }

    /**
     * Add a new FetchOp to this plan. If includeInResult is true the the
     * result of the op is included in the projection returned by the
     * FetchSpec.
     */
    public void addFetchOp(FetchOp op, boolean includeInResult) {
        op.addToFSpec(includeInResult, false);
    }

    public void addFetchOp(FetchOp op, boolean includeInResult, boolean prepend) {
        if (includeInResult) {
            throw BindingSupportImpl.getInstance().internal("A FetchOp that is " +
                    "prepended may not be included in the FetchResult");
        }
        op.addToFSpec(includeInResult, prepend);
    }

    /**
     * The discrimininator fetchop must be the first op in the spec. It also needs
     * to be added to 'includeInResult'
     *
     * @param op
     */
    public void addDiscriminator(FetchOpDiscriminator op, boolean includeInResult) {
        if (fopDiscr != null) {
            throw BindingSupportImpl.getInstance().internal("There is already an 'Discriminator' for this fetchspec");
        }
        fopDiscr = op;
        op.addToFSpec(includeInResult, true);
    }

    public FetchOpDiscriminator getDiscriminator() {
        return fopDiscr;
    }

    /**
     * Get the number of FetchOp's in this spec.
     */
    public int getFetchOpCount() {
        return opCount;
    }

    /**
     * Get the types of the objects in our projection.
     */
    public int[] getProjectionTypes() {
        int[] a = new int[resultOpCount];
        for (int i = 0; i < resultOpCount; i++) {
            a[i] = resultOps[i].getResultType();
        }
        return a;
    }

    /**
     * Get the default FetchOptions for this spec.
     */
    public FetchOptions getOptions() {
        return options;
    }

    /**
     * Set the compiled parameter info.
     */
    public void setParamList(SqlBuffer.Param paramList) {
        if (sqlBuffer == null) {
            generateSQL();
        }
        sqlBuffer.setParamList(paramList);
    }

    public SqlBuffer.Param getParamList() {
        if (sqlBuffer == null) {
            return null;
        }
        return sqlBuffer.getParamList();
    }

    /**
     * Print a user understandable description of this operation.
     */
    public void printPlan(PrintStream p, String indent) {
        for (int i = 0; i < opCount; i++) {
            ops[i].printPlan(p, indent);
        }
    }

    /**
     * Finish creating this spec and generate the SQL buffer for our query.
     * This is a NOP if already done.
     */
    public synchronized void generateSQL() {
        checkFinished();
        if (sqlGenerated) {
            return;
        }

        if (sqlBuffer == null) {
            sqlBuffer = new SqlBuffer();
        }

        int aliasCount = root.createAlias(0);
        if (aliasCount == 1) {
            root.alias = null;
            sqlBuffer.setFirstTableOrAlias(root.table.name);
        } else {
            sqlBuffer.setFirstTableOrAlias(root.alias);
        }
        root.appendSQL(sqlDriver, sqlBuffer.getSqlbuf(), null);
        sqlBuffer.setSelectListRange(root.distinct, root.selectListStartIndex,
                root.selectListFirstColEndIndex, root.selectListEndIndex);
        sqlBuffer.setOrderByRange(root.orderByStartIndex, root.orderByEndIndex);
        // work around bug with replace in CharBuffer class
        sqlBuffer.getSqlbuf().append(' ');

        for (int i = 0; i < opCount; i++) {
            ops[i].generateSQL();
    }

        // clear fields we dont need any more now that we have the SQL
//        root = null;
        pos = null;
        sqlGenerated = true;
    }

    private void checkFinished() {
        if (!finished) {
            throw BindingSupportImpl.getInstance().internal("The FetchSpec is not finished");
        }
    }

    /**
     * Create a FetchResult to execute our query. This will execute the
     * query as soon as the data is needed.
     *
     * @param forUpdate Generate SELECT FOR UPDATE type query
     * @param forCount Generate a COUNT(*) query to just count the rows
     * @param fromIncl Index of first row to return
     * @param toExcl Index of row after last row to return (-1 for all)
     * @param scrollable Use a scrollable ResultSet
     *
     * @param maxRows
     * @see FetchResultImp#execute()
     */
    public FetchResult createFetchResult(JdbcStorageManager sm, Connection con,
            Object[] params, boolean forUpdate, boolean forCount,
            long fromIncl, long toExcl, int fetchSize, boolean scrollable,
            int maxRows) {
        checkFinished();

        /**
         * If all the fields where pass2 fields then there will be no columns in
         * the selectList.
         */
        if (totColumnCount == 0) {
            return new EmptyProjectionFetchResultImp(this, scrollable,
                    sm, con, forUpdate, forCount, params);
        }

        if (scrollable && !sqlDriver.isScrollableResultSetSupported()) {
            throw BindingSupportImpl.getInstance().datastore(
                    "Scrollable ResultSet's not supported for " +
                    sqlDriver.getName() + " using JDBC driver " +
                    sm.getJdbcConnectionSource().getDriverName());
        }

        if (sqlBuffer == null) {
            generateSQL();
        }
        String sql = sqlBuffer.getSql(sqlDriver, params, forUpdate, forCount,
                fromIncl, toExcl);
        boolean error = true;
        PreparedStatement ps = null;
        try {
            try {
                if (scrollable) {
                    ps = con.prepareStatement(sql,
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
                } else {
                    ps = con.prepareStatement(sql);
                }
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().datastore(
                        "Error creating PreparedStatement: " + e + "\nSQL:\n" +
                        sql);
            }
            if (instanceFetch) {
                try {
                    ((JdbcOID) params[0]).setParams(ps, 1);
                } catch (SQLException e) {
                    throw getSqlDriver().mapException(e, e + "\nSQL:\n" + sql,
                            true);
                }
            } else {
                sqlBuffer.setParamsOnPS(sm.getJmd(), sqlDriver, ps, params, sql);
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

            FetchResult ans = new FetchResultImp(this, ps, sql, scrollable,
                    sm, con, forUpdate, forCount, params, oneToMany);
            error = false;
            return ans;
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

    /**
     * This is invoked by one of our results when it is closed. We call
     * fetchResultClosed on all of our ops so they have a chance to close
     * any nested results.
     */
    public void fetchResultClosed(FetchResult fetchResult) {
        for (int i = 0; i < opCount; i++) {
            ops[i].fetchResultClosed(fetchResult);
        }
    }

    public SqlDriver getSqlDriver() {
        return sqlDriver;
    }

    public boolean isSingleObjectRow() {
        return singleObjectRow;
    }

    /**
     * If singleObjectRow is true and the projection only has one Object
     * then this is returned as is and not in an Object[1].
     */
    public void setSingleObjectRow(boolean singleObjectRow) {
        this.singleObjectRow = singleObjectRow;
    }

    /**
     * Process the current row in fetchResult's ResultSet and return our
     * projection.
     */
    public Object createRow(FetchResult fetchResult, StateContainer stateContainer) {
        for (int i = 0; i < opCount; i++) {
            try {
                ops[i].fetch(fetchResult, stateContainer);
            } catch (Exception e) {
                throw sqlDriver.mapException(e, e.toString() + "\nProcessing " +
                        ops[i].getIndex() + ": " + ops[i].getDescription(),
                        true);
            }
        }

        if (singleObjectRow && resultOpCount == 1) {
            return resultOps[0].getResult(fetchResult);
        } else {
            Object[] a = new Object[resultOpCount];
            for (int i = 0; i < resultOpCount; i++) {
                a[i] = resultOps[i].getResult(fetchResult);
            }
            return a;
        }
    }

    public Object fetchDiscriminator(FetchResult fetchResult, StateContainer stateContainer) {
        if (fopDiscr == null) {
            throw BindingSupportImpl.getInstance().internal("There is no 'Discriminator' FetchOp set");
        }
        try {
            fopDiscr.fetch(fetchResult, stateContainer);
            return fetchResult.getData(fopDiscr);
        } catch (SQLException e) {
            throw sqlDriver.mapException(e, e.toString() + "\nProcessing " +
                        fopDiscr.getDescription(),
                        true);
        }
    }

    /**
     * Called when all the rows are processed.
     * @param fetchResult
     * @param params
     * @param stateContainer
     */
    public void fetchPass2(FetchResult fetchResult, Object[] params,
            StateContainer stateContainer) {
        if (fetchResult.getFetchSpec() != this) {
            throw BindingSupportImpl.getInstance().internal(
                    "The fetchResult does not belong to the fetchspec");
        }
        for (int i = 0; i < opCount; i++) {
            try {
                ops[i].fetchPass2(fetchResult, params, stateContainer);
            } catch (Exception e) {
                throw sqlDriver.mapException(e, e.toString() + "\nProcessing " +
                        ops[i].getIndex() + ": " + ops[i].getDescription(),
                        true);
            }
        }
    }

    public void setFilterExpFactory(FilterExpFactory filterExpFactory) {
        this.filterExpFactory = filterExpFactory;
    }

    public FilterExpFactory getFilterExpFactory() {
        return filterExpFactory;
    }

    public SelectExp createQueryFilter() {
        if (parent == null) {
            if (filterExpFactory == null) {
                throw BindingSupportImpl.getInstance().internal(
                        "This FetchSpec does not contain a filter factory");
            }
            return filterExpFactory.createFilterExp();
        }
        else return parent.createQueryFilter();
    }

    /**
     * Return the root cmd type of this fetchspec graph.
     */
    public ClassMetaData getRootType() {
        if (parent == null) return rootCmdType;
        else return parent.getRootType();
    }

    public void setRootCmdType(ClassMetaData rootCmdType) {
        this.rootCmdType = rootCmdType;
    }

    public FetchResult createFetchResult(FetchResult parentResult) {
        checkFinished();
        return new SubFetchResultImp(this, parentResult);
    }

    public void setOneToMany(boolean b) {
        this.oneToMany = true;
    }

    public void setSqlBuffer(SqlBuffer sqlBuffer) {
        this.sqlBuffer = sqlBuffer;
    }
}

