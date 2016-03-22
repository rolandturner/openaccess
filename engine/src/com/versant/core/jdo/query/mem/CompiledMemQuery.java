
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
package com.versant.core.jdo.query.mem;

import com.versant.core.jdo.QueryDetails;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.jdo.query.*;
import com.versant.core.server.CompiledQuery;

import com.versant.core.common.BindingSupportImpl;

/**
 * Holds query information after parsing filters, params, variables and orders.
 */
public class CompiledMemQuery implements CompiledQuery {

    private int id;
    public ParamNode[] params = null;
    public VarNode[] vars = null;
    public Node filter = null;
    public QueryDetails queryParams = null;
    public OrderNode[] orders = null;
    private QueryParser qParser;
    private ModelMetaData jmd;

    public CompiledMemQuery(ModelMetaData jmd) {
        this.jmd = jmd;
    }

    public int[] getClassIndexes() {
        return new int[0];
    }

    /**
     * The typeCode of each column.
     *
     * @see com.versant.core.metadata.MDStatics
     */
    public int[] getResultTypeCodes() {
        return new int[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * If this is a query with a single/unique result.
     */
    public boolean isUnique() {
        if (true) throw BindingSupportImpl.getInstance().notImplemented("");
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRandomAccess() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getQueryResultBatchSize() {
        return 0;
    }

    public int getFetchGroupIndex() {
        return 0;
    }

    public QueryDetails getQueryDetails() {
        return queryParams;
    }

    /**
     * Is this a non default projection query.
     * This will return false for the default projection.
     */
    public boolean isProjectionQuery() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * If this query returns default results.
     */
    public boolean isDefaultResult() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The index of the first occurance of 'this' in the projection.
     */
    public int getFirstThisIndex() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * If this is a result/projection that only contains 'this' and no other
     * fields in the projection or if no projection was specified.
     */
    public boolean isContainsThisOnly() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * If the results of the query should be copied for caching.
     * This should only happen for non-default type projection queries that
     * contains references.
     * If this is a projection that only specifies 'this' then this should
     * also return false.
     */
    public boolean isCopyResultsForCache() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Array containing the index pos of ref fields of the projection.
     */
    public int[] getRefIndexArray() {
        return new int[]{};  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void compile(QueryDetails qParams) throws ParseException {
        qParser = new QueryParser(jmd);

        try {
            qParser.parse(qParams);
            params = qParser.getParams();
            filter = qParser.getFilter();
            orders = qParser.getOrders();
            vars = qParser.getVars();
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
        } catch (TokenMgrError e) {
            throw BindingSupportImpl.getInstance().invalidOperation(e.getMessage(), e);
        }
    }

    public ClassMetaData[] getQueryClasses() {
        return null;
    }

    public boolean isCacheble() {
        return false;
    }

    public void setCacheable(boolean on) {
        // ignore
    }

    public int getMaxRows() {
        return 0;
    }

    public int[] getEvictionClassBits() {
        return new int[0];
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getParamIdentifiers() {
        return null;
    }

}
