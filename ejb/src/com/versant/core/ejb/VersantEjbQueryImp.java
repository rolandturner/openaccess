
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
package com.versant.core.ejb;

import com.versant.core.common.NewObjectOID;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.server.CompiledQuery;

import javax.jdo.Extent;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.FlushModeType;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.*;

/**
 * This is the implementation of Query.
 */
public final class VersantEjbQueryImp implements Externalizable, Query, VersantQuery {
    private Object[] params;

    private final QueryDetails queryDetails = new QueryDetails();
    private QueryResult resultList;
    private EMProxy pmProxy;
    private CompiledQuery compiledQuery;
    private int paramEndIndex = -1;
    private HashMap namedParamMap;

    /**
     * For Serialization.
     */
    public VersantEjbQueryImp() {
    }

    public VersantEjbQueryImp(EMProxy emProxy, String filter) {
        this(emProxy, QueryDetails.LANGUAGE_EJBQL);
        queryDetails.setFilter(filter);
    }

    /**
     * Create a new query for pmProxy. The ignoreCache setting is taken
     * from the curremt setting of pmProxy.
     *
     * @param language Query language
     * @see com.versant.core.jdo.QueryDetails#LANGUAGE_JDOQL
     * @see com.versant.core.jdo.QueryDetails#LANGUAGE_SQL
     * @see com.versant.core.jdo.QueryDetails#LANGUAGE_EJBQL
     */
    public VersantEjbQueryImp(EMProxy pmProxy, int language) {
        this.pmProxy = pmProxy;
        queryDetails.setLanguage(language);
    }

    /**
     * Create a new query for pmProxy using all the settings of params.
     * This is used to create Query's from named queries in the meta data.
     */
    public VersantEjbQueryImp(EMProxy pmProxy, QueryDetails params) {
        this.pmProxy = pmProxy;
        queryDetails.fillFrom(params);
    }

    /**
     * This is a util method that invalidates the preCompiled query because of
     * change made by the client.
     */
    private void changed() {
        compiledQuery = null;
    }

    public List getResultList() {
        if (params != null) {
            if (paramEndIndex >= 0) {
                if (params.length > paramEndIndex + 1) {
                    Object[] tmp = new Object[paramEndIndex + 1];
                    System.arraycopy(params, 0, tmp, 0, tmp.length);
                    params = tmp;
                }
                return (List) executeWithArray(params);
            }
        } else if (namedParamMap != null) {
            compile();
            return (List) executeWithArray(getParamForMapExecution());
        }
        return (List) execute();
    }

    private Object[] getParamForMapExecution() {
        String[] paramIds = compiledQuery.getParamIdentifiers();
        Object[] params = new Object[paramIds.length];
        for (int i = 0; i < paramIds.length; i++) {
            params[i] = namedParamMap.get(paramIds[i]);
        }
        return params;
    }

    public Object getSingleResult() {
        throw new RuntimeException("Not Implemented");
    }

    public int executeUpdate() {
        throw new RuntimeException("Not Implemented");
    }

    public Query setMaxResults(int maxResult) {
        changed();
        queryDetails.setMaxResultCount(maxResult);
        return this;
    }

    public Query setFirstResult(int startPosition) {
        if (startPosition == 0) return this;
        throw new RuntimeException("Not Implemented");
    }

    public Query setHint(String hintName, Object value) {
        //todo do this with reflection
        if ("fetchSize".equals(hintName)) {
            queryDetails.setResultBatchSize(getAsIntValue(hintName, value));
        }
        return this;
    }

    public Query setParameter(String name, Object value) {
        checkCreateNamedMap();
        namedParamMap.put(name, value);
        return this;
    }

    private void checkCreateNamedMap() {
        if (params != null) {
            params = null;
            paramEndIndex = -1;
        }

        if (namedParamMap == null) namedParamMap = new HashMap();
    }

    public Query setParameter(String name, Date value, TemporalType temporalType) {
        checkCreateNamedMap();
        namedParamMap.put(name, value);
        return this;
    }

    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        checkCreateNamedMap();
        namedParamMap.put(name, value);
        return this;
    }

    public Query setParameter(int position, Object value) {
        if (namedParamMap != null) {
            namedParamMap = null;
        }

        if (position > paramEndIndex) {
            paramEndIndex = position;
        }
        if (params == null) {
            params = new Object[position + 5];
        } else if (params.length == position) {
            Object[] tmp = new Object[params.length + 1];
            System.arraycopy(params, 0, tmp, 0, params.length);
            params = tmp;
        }
        params[position] = value;
        return this;
    }

    public Query setParameter(int position, Date value, TemporalType temporalType) {
        setParameter(position, value);
        return this;
    }

    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        setParameter(position, value);
        return this;
    }

    public Query setFlushMode(FlushModeType flushMode) {
        setIgnoreCache(flushMode != FlushModeType.AUTO);
        return this;
    }


















    public void setBounded(boolean value) {
        queryDetails.setBounded(value);
    }

    public boolean isBounded() {
        return queryDetails.isBounded();
    }

    public void setClass(Class cls) {
        changed();
        queryDetails.setCandidateClass(cls);
    }

    public void setCandidates(Extent pcs) {
        changed();
        queryDetails.setExtent(pcs);
    }

    public void setCandidates(Collection pcs) {
        changed();
        queryDetails.setCol(pcs);
    }

    public void setFilter(String filter) {
        changed();
        queryDetails.setFilter(filter);
    }

    public String getFilter() {
        return queryDetails.getFilter();
    }

    public void declareImports(String imports) {
        changed();
        queryDetails.setImports(imports);
    }

    public void declareParameters(String params) {
        changed();
        queryDetails.declareParameters(params);
    }

    public void declareVariables(String variables) {
        changed();
        queryDetails.setVariables(variables);
    }

    public void setOrdering(String ordering) {
        changed();
        queryDetails.setOrdering(ordering);
    }

    public void setIgnoreCache(boolean ignoreCache) {
        changed();
        queryDetails.setIgnoreCache(ignoreCache);
    }

    private EntityManagerImp getRealPM() {
        if (pmProxy == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Query is not associated with a PersistenceManager");
        }
        return pmProxy.getEm();
    }

    public void setFetchGroup(String fgName) {
        if (fgName == null) {
            queryDetails.setFetchGroupIndex(0);
            changed();
            return;
        } else {
            if (queryDetails.getCandidateClass() == null) {
                throw BindingSupportImpl.getInstance().invalidOperation(
                        "Please first supply a candidate class");
            }
            FetchGroup fg = getRealPM().modelMetaData.getClassMetaData(
                    queryDetails.getCandidateClass()).getFetchGroup(fgName);
            if (fg == null) {
                throw BindingSupportImpl.getInstance().invalidOperation("No fetchGroup with name "
                        + fgName
                        + " for class "
                        + queryDetails.getCandidateClass().getName());
            }

            queryDetails.setFetchGroupIndex(fg.index);
            changed();
        }
    }

    public String getFetchGroup() {
        int i = queryDetails.getFetchGroupIndex();
        if (i == 0) return null;
        ClassMetaData cmd = getRealPM().modelMetaData.getClassMetaData(
                queryDetails.getCandidateClass());
        return cmd.fetchGroups[i].name;
    }

    public void setMaxRows(int amount) {
        changed();
        this.queryDetails.setMaxResultCount(amount);
    }

    public int getMaxRows() {
        return queryDetails.getMaxResultCount();
    }

    public void setFetchSize(int value) {
        changed();
        this.queryDetails.setResultBatchSize(value);
    }

    public int getFetchSize() {
        return queryDetails.getResultBatchSize();
    }

    public void setRandomAccess(boolean on) {
        changed();
        queryDetails.setRandomAccess(on);
    }

    public boolean isRandomAccess() {
        return queryDetails.isRandomAccess();
    }

    public void setCountStarOnSize(boolean on) {
        changed();
        queryDetails.setCountOnSize(on);
    }

    public boolean isCountStarOnSize() {
        return queryDetails.isCountOnSize();
    }

    public boolean getIgnoreCache() {
        return queryDetails.isIgnoreCache();
    }

    public void setEvictionClasses(Class[] classes, boolean includeSubclasses) {
        setEvictionClasses(getRealPM().modelMetaData.convertToClassIndexes(
                classes,
                includeSubclasses));
    }

    public void setEvictionClasses(int[] classIndexes) {
        changed();
        queryDetails.setExtraEvictClasses(classIndexes);
    }

    public Class[] getEvictionClasses() {
        int[] a = queryDetails.getExtraEvictClasses();
        return a == null ? null : getRealPM().modelMetaData.convertFromClassIndexes(
                a);
    }

    public void setResult(String result) {
        changed();
        queryDetails.setResult(result);
    }

    public void setGrouping(String grouping) {
        changed();
        queryDetails.setGrouping(grouping);
    }

    /**
     * Specify that there is a single result of the query.
     */
    public void setUnique(boolean unique) {
        changed();
        queryDetails.setUnique(unique);
    }

    public void compile() {
        if (compiledQuery == null) {
            queryDetails.updateCounts();
            compiledQuery = pmProxy.getEm().getStorageManager().compileQuery(
                    queryDetails);
        }
    }

    /**
     * TODO: move to base class
     * @param n
     */
    public void checkParamCount(int n) {
        if (n != queryDetails.getTotalParamCount()) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Expected " +
                    queryDetails.getTotalParamCount() + " parameters, have " + n);
        }
    }

    public Object execute() {
        queryDetails.updateCounts();
        return executeWithArrayImp(null);
    }

    private Object executeWithArray(Object[] parameters) {
        queryDetails.updateCounts();
        return executeWithArrayImp(copyParams(parameters));
    }

    /**
     * Get the query plan for this query. This will include the SQL and
     * possibly also a query plan for the SQL from the database itself.
     */
    public VersantQueryPlan getPlan(Object[] parameters) {
        queryDetails.updateCounts();
        if (queryDetails.getCol() != null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "getPlan is not supported for queries executed against a collection");
        }
        int n = parameters == null ? 0 : parameters.length;
        checkParamCount(n);
        return getPlanImp(parameters);
    }

    /**
     * The parameters array must NOT contain the jdoGenieOptions.
     */
    private Object executeWithArrayImp(Object[] parameters) {
        final EMProxy pmProxy = this.pmProxy;
        pmProxy.getEm().convertPcParamsToOID(parameters);

        //Check if this should be a multi-part query
        Class cls = queryDetails.getCandidateClass();
        Class[] candidates = null;
        if (cls != null) {
            candidates = pmProxy.getEm().modelMetaData.getQueryCandidatesFor(cls);
            if (candidates == null) {
                throw BindingSupportImpl.getInstance().unsupported("Queries for class '"
                        + queryDetails.getCandidateClass() + "' is not supported");
            }
        }

        if (candidates != null && candidates.length > 1) {
            //create subQueries for all the candidates and compile it
            queryDetails.updateCounts();
            Set qResults = new HashSet();
            for (int i = 0; i < candidates.length; i++) {
                Class candidate = candidates[i];

                QueryDetails qd = new QueryDetails(queryDetails);
                qd.setCandidateClass(candidate);

                CompiledQuery cq = this.pmProxy.getEm().getStorageManager().compileQuery(qd);
                QueryResult qr = getQueryResult(parameters, qd, cq, this.pmProxy);

                qResults.add(qr);
            }
            return new MultiPartQueryResult(qResults);
        } else {
            compile();

            //is this a unique query
            if (compiledQuery.isUnique()) {
                //must do immediately
                return QueryResultBase.resolveRow(pmProxy.getAllQueryResults(
                        compiledQuery,
                        parameters).getUnique(),
                        pmProxy);
            } else {
                return getQueryResult(parameters, queryDetails, compiledQuery, this.pmProxy);
            }
        }
    }

    private static Object[] copyParams(Object[] parameters) {
        Object[] params = null;
        if (parameters != null) {
            params = new Object[parameters.length];
            System.arraycopy(parameters, 0, params, 0, params.length);
        }
        return params;
    }

    private QueryResult getQueryResult(Object[] params,
            QueryDetails queryDetails, CompiledQuery compiledQuery, EMProxy emProxy) {
        QueryResult res = null;
//        boolean collectionQuery = queryDetails.getCol() != null;
//        boolean containsNewOID = containsNewOID(params);

//        if (collectionQuery) {
//            // query agains a collection in memory
////            res = new MemoryQueryResult(pmProxy,
////                    queryDetails, createSMList(queryDetails.getCol(), pmProxy), params);
//        } else if (containsNewOID && queryDetails.isIgnoreCache()) {
//            // query agains a collection in memory with some new instances
//            res = new MemoryQueryResult();
//        } else if (queryDetails.isRandomAccess()) {
//            // random access query against database
//            res = new RandomAccessQueryResult(pmProxy,
//                    compiledQuery, params);
//        } else {
            // normal query against database
            res = new ForwardEJBQueryResult(emProxy, queryDetails,
                    compiledQuery, params);
//        }

//        if (emProxy.getMultithreaded()) {
//            res = new SynchronizedQueryResult(emProxy, res);
//        }
        addResults(res);
        return res;
    }

    /**
     * Add a set of results to this query.
     */
    private void addResults(QueryResult q) {
        synchronized (pmProxy) {
            if (resultList == null) {
                resultList = q;
            } else {
                q.setNext(resultList);
                resultList.setPrev(q);
                resultList = q;
            }
        }
    }

    /**
     * Remove a set of results from this query.
     */
    private void removeResults(QueryResult q) {
        synchronized (pmProxy) {
            if (resultList == q) {  // at tail of list
                resultList = q.getNext();
                if (resultList != null) resultList.setPrev(null);
                q.setNext(null);
            } else {
                q.getPrev().setNext(q.getNext());
                if (q.getNext() != null) q.getNext().setPrev(q.getPrev());
                q.setNext(null);
                q.setPrev(null);
            }
        }
    }

    /**
     * TODO must check not to try and do convertPcParams twice
     * <p/>
     * The parameters array must NOT contain the jdoGenieOptions.
     */
    private VersantQueryPlan getPlanImp(Object[] parameters) {
        pmProxy.getEm().convertPcParamsToOID(parameters);
        compile();
        return pmProxy.getEm().getStorageManager().getQueryPlan(queryDetails,
                compiledQuery, parameters);
    }

    private static boolean containsNewOID(Object[] params) {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof NewObjectOID) {
                    return true;
                }
            }
        }
        return false;
    }

    public void close(Object queryResult) {
        QueryResult qr = (QueryResult)queryResult;
        qr.close();
        removeResults(qr);
    }

    public void closeAll() {
        synchronized (pmProxy) {
            if (resultList == null) return;
            resultList.close();
            for (QueryResult i = resultList.getNext(); i != null; i = i.getNext()) {
                i.close();
                i.getPrev().setNext(null);
                i.setPrev(null);
            }
            resultList = null;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        queryDetails.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        QueryDetails qp = new QueryDetails();
        qp.readExternal(in);
        this.queryDetails.fillFrom(qp);
    }

    public void initialiseFrom(VersantEjbQueryImp clientQuery) {
        this.queryDetails.fillFrom(clientQuery.queryDetails);
        this.queryDetails.clearExtentAndCol();
    }

    public void setCacheable(boolean on) {
        changed();
        queryDetails.setCacheable(on);
    }

    public String getImports() {
        return queryDetails.getImports();
    }

    public String getParameters() {
        return queryDetails.getParameters();
    }

    public String getVariables() {
        return queryDetails.getVariables();
    }

    public String getOrdering() {
        return queryDetails.getOrdering();
    }

    public String getGrouping() {
        return queryDetails.getGrouping();
    }

    public String getResult() {
        return queryDetails.getResult();
    }

    public boolean isUnique() {
        return queryDetails.getUnique() == QueryDetails.TRUE;
    }

    private int getAsIntValue(String hintName, Object value) {
        if (value instanceof Number) {
            return ((Number)value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else {
            throw BindingSupportImpl.getInstance().unsupported("The type '"
                    + value + "' is not supported for the hintName '" + hintName + "'");
        }
    }
}
