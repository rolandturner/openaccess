
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
package com.versant.core.vds;

import com.versant.core.server.CompiledQuery;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.MDStatics;
import com.versant.core.jdo.query.*;
import com.versant.core.jdo.QueryDetails;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Debug;
import com.versant.odbms.query.*;

public final class VdsCompiledQuery implements CompiledQuery {
    private int id;
    private final QueryDetails qp;

    public QueryParser queryParser;

    private ParamNode[] params;

    /**
     * The class index of the candidate class of the query.
     */
    private int classIndex = -1;
    /**
     * The cls index's of all the filter classes.
     */
    private ClassMetaData[] filterCmds;
    /**
     * Bitmapped array of the class indexes that will cause the results
     * of this query to be evicted when their instances are modified. Each
     * class index has one bit in this array.
     */
    private int[] evictionClassBits;
    /**
     * If the results of the query is cacheble.
     */
    private boolean cacheble;
    /**
     * Must subclasses be included in the result?
     */
    private boolean includeSubclasses;
    /**
     * The root fetch group for the query. This will be from cmd. This
     * will be the default fetch group unless the user specified a
     * different group.
     */
    private int fetchGroupIndex;
    /**
     * Must this query return results suitable for random access?
     */
    private boolean randomAccess;
    /**
     * The max amount of rows to return for this query.
     */
    private int maxRows;
    /**
     * This is the amount of data that will be prefetched per each round trip
     * to the server.
     */
    private int queryResultBatchSize;


    private boolean parColFetchEnabled;
    private int unique;
    private int selectColumnCount;

    private Expression expr;
    private OrderByExpression[] orderby;

    public VdsCompiledQuery() {
      this.qp = null;
      this.evictionClassBits = new int[0];
    }

    public VdsCompiledQuery(ClassMetaData cmd, QueryDetails queryParams) {
        this.classIndex = cmd.index;
        this.includeSubclasses = queryParams.includeSubClasses() && cmd.isInHierarchy();
        this.fetchGroupIndex = queryParams.getFetchGroupIndex();
        this.randomAccess = queryParams.isRandomAccess();
        this.maxRows = queryParams.getMaxResultCount();
        this.queryResultBatchSize = queryParams.getResultBatchSize();
        this.qp = queryParams;

        parColFetchEnabled = QueryDetails.enableParallelCollectionFetch(qp, cmd.fetchGroups[qp.getFetchGroupIndex()]);
    }

    public int[] getClassIndexes() {
        // TODO: implement getClassIndexes
        return new int[0];
    }

    public void setParams(ParamNode[] params) {
      this.params = params;
    }

    public void setExpr(Object versantform) {
        if (Debug.DEBUG) {
            Debug.assertInternal(versantform instanceof Expression ||
                    versantform instanceof Predicate,
                    "versantform is not a instanceof Expression or Predicate");
        }
        if (versantform instanceof Predicate)
            this.expr = new Expression((Predicate)versantform);
        else
            this.expr = (Expression)versantform;
    }

    public void setOrderBy(OrderByExpression[] orders) {
        if (Debug.DEBUG) {
        	for (int i = 0; i < orders.length; i++) {
        		Debug.assertInternal(orders[i] instanceof OrderByExpression,
                    "orders are not instanceof OrderByExpression");
        	}
        }
        orderby = (OrderByExpression[])orders;
    }

    public OrderByExpression[] getOrderBy() {
    	return orderby;
    }

    public Expression getExpr() {
      return expr;
    }

    public ParamNode[] getParams() {
      return params;
    }
    public int getSelectColumnCount() {
        return selectColumnCount;
    }

    public void setSelectColumnCount(int selectColumnCount) {
        this.selectColumnCount = selectColumnCount;
    }


    /**
     * If this is a query with a single/unique result.
     */
    public boolean isUnique() {
      if (qp == null)
        return false;
        if (qp.getUnique() == MDStatics.TRUE) {
            unique = MDStatics.TRUE;
        } else {
            unique = MDStatics.FALSE;
        }

        if (unique == MDStatics.NOT_SET) {
            throw BindingSupportImpl.getInstance().internal("The 'unique' value has not been processed.");
        }
        boolean res = false;
        if (unique == MDStatics.TRUE) res = true;
        return res;
    }

    public boolean isProjectionQuery() {
        return false;
    }

    public int getFirstThisIndex() {
        return -1;
    }

    /**
     * If the results of the query should be copied for caching.
     * This should only happen for non-default type projection queries that
     * contains references.
     *
     * If this is a projection that only specifies 'this' then this should
     * also return false.
     */
    public boolean isCopyResultsForCache() {
        return false;
    }

    /**
     * If this query returns default results.
     */
    public boolean isDefaultResult() {
        return true;
    }

    public int[] getResultTypeCodes() {
        return null;
    }

    public boolean isParColFetchEnabled() {
        return parColFetchEnabled;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public int getQueryResultBatchSize() {
        return queryResultBatchSize;
    }

    public ClassMetaData[] getQueryClasses() {
        return filterCmds;
    }

    public void setFilterClsIndexs(ClassMetaData[] filterClsIndexs) {
        this.filterCmds = filterClsIndexs;
    }

    public int[] getEvictionClassBits() {
//      assert evictionClassBits != null;
        if (Debug.DEBUG) {
            Debug.assertInternal(evictionClassBits != null,
                    "evictionClassBits is null");
        }
        return evictionClassBits;
    }

    public QueryDetails getQueryDetails() {
        return qp;
    }

    public void setEvictionClassBits(int[] evictionClassBits) {
        this.evictionClassBits = evictionClassBits;
    }

    public boolean isCacheble() {
        return cacheble;
    }

    public void setCacheable(boolean cacheble) {
        this.cacheble = cacheble;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public boolean isIncludeSubclasses() {
        return includeSubclasses;
    }

    public int getFetchGroupIndex() {
        return fetchGroupIndex;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof VdsCompiledQuery) {
            return qp.equals(((VdsCompiledQuery) obj).qp);
        }
        return false;
    }

    public int hashCode() {
        return qp.hashCode();
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
