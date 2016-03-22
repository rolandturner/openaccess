
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

import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.server.StateContainer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * This provides access to the results of a FetchSpec query.
 */
public interface FetchResult {

    /**
     * Execute the query for these results.
     */
    public void execute();

    /**
     * If this query is busy executing then call cancel on its
     * PreparedStatement. Otherwise do nothing.
     */
    public void cancel();

    /**
     * Close these results, discarding any exceptions. It is a NOP to call
     * close on an already closed result.
     */
    public void close();

    /**
     * Has this result been closed?
     */
    public boolean isClosed();

    /**
     * Are there more results? This will execute the query if not already
     * done. It returns false if the results have been closed. The results
     * are automatically closed if there are no more results.
     */
    public boolean hasNext();

    public void setHaveRow(boolean haveRow);

    /**
     * Return an Object[] or a Object depending on the spec. Throws a
     * JDOFatalInternalException if there are no more rows or the results
     * have been closed.
     */
    public Object next(StateContainer stateContainer);

    public void fetchPass2(StateContainer stateContainer);

    /**
     * Is this a scrollable result? If true then get(index) can be used to
     * access the rows.
     */
    public boolean isScrollable();

    /**
     * Return the number of results. This is only valid for a scrollable
     * result. This will execute the query if not already done.
     */
    public int size();

    /**
     * Return the row at index (zero based). This is only valid for a
     * scrollable result. This will execute the query if not already done.
     */
    public Object get(int index, StateContainer stateContainer);

    /**
     * Get the FetchSpec we were created from. This provides state information
     * e.g. the type of each entry in the projection.
     */
    public FetchSpec getFetchSpec();

    /**
     * Todo remove this hack for EJBQL when we refactor query stuff
     */
    public PreparedStatement getPreparedStatement();

    /**
     * Return the underlying resultset as used by the FetchResult
     */
    public ResultSet getResultSet();

    /**
     * This is used by the fetchop's to store data while fetching/processing the row.
     */
    public void setData(FetchOp fopGetColumn, Object val);

    public Object getData(FetchOp fopGetColumn);

    /**
     * This is used by pass2 FetchOp's. These fetchop's fetch data is given a change
     * to fetch their data after the required amount of rows of the resultset is fetched.
     */
    public void setPass2Data(FetchOp op, Object val);

    public Object getPass2Data(FetchOp op);

    public JdbcStorageManager getStorageManager();

    public Connection getConnection();

    public boolean isForUpdate();

    public boolean isForCount();

    public Object[] getParams();

    public boolean skip(int skip);

    public boolean absolute(int row);

    public int getAbsoluteCount();

    public Object getDiscriminator(StateContainer stateContainer);

    void fetchCachedDiscr(FetchOpDiscriminator fetchOpDiscriminator,
            StateContainer stateContainer);

    SqlBuffer.Param getParamList();
}

