
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
package com.versant.core.jdo;

import com.versant.core.common.NewObjectOID;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.server.CompiledQuery;
import com.versant.core.util.BeanUtils;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.FetchPlan;
import javax.jdo.spi.PersistenceCapable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is the implementation of a Query.
 */
public final class VersantQueryImp implements VersantQuery, Externalizable {

    private final QueryDetails queryDetails = new QueryDetails();
    private QueryResult resultList;
    private PMProxy pmProxy;
    private CompiledQuery compiledQuery;

    /**
     * For Serialization.
     */
    public VersantQueryImp() {
    }

    /**
     * Create a new JDOQL query for pmProxy. The ignoreCache setting is taken
     * from the curremt setting of pmProxy.
     */
    public VersantQueryImp(PMProxy pmProxy) {
        this(pmProxy, QueryDetails.LANGUAGE_JDOQL);
    }

    /**
     * Create a new query for pmProxy. The ignoreCache setting is taken
     * from the curremt setting of pmProxy.
     *
     * @param language Query language
     * @see QueryDetails#LANGUAGE_JDOQL
     * @see QueryDetails#LANGUAGE_SQL
     */
    public VersantQueryImp(PMProxy pmProxy, int language) {
        this.pmProxy = pmProxy;
        queryDetails.setLanguage(language);
        setIgnoreCache(pmProxy.getIgnoreCache());
    }

    /**
     * Create a new query for pmProxy using all the settings of other.
     */
    public VersantQueryImp(PMProxy pmProxy, VersantQueryImp other) {
        this(pmProxy, other.queryDetails);
    }

    /**
     * Create a new query for pmProxy using all the settings of params.
     * This is used to create Query's from named queries in the meta data.
     */
    public VersantQueryImp(PMProxy pmProxy, QueryDetails params) {
        this.pmProxy = pmProxy;
        queryDetails.fillFrom(params);
        if (params.isUseIgnoreCacheFromPM()) {
            setIgnoreCache(pmProxy.getIgnoreCache());
        }
    }

    /**
     * This is a util method that invalidates the preCompiled query because of
     * change made by the client.
     */
    private void changed() {
        compiledQuery = null;
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

    private VersantPersistenceManagerImp getRealPM() {
        if (pmProxy == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Query is not associated with a PersistenceManager");
        }
        return pmProxy.getRealPM();
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
            compiledQuery = pmProxy.getRealPM().getStorageManager().compileQuery(
                    queryDetails);
        }
    }


    private


	void checkParamCount(int n) {
        int tc = queryDetails.getTotalParamCount();
        if (tc >= 0 && n != tc) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Expected " +
                    queryDetails.getTotalParamCount() + " parameters, have " + n);
        }
    }

    public Object execute() {
        checkParamCount(0);
        queryDetails.updateCounts();
        return executeWithArrayImp(null);
    }

    public Object execute(Object p1) {
        checkParamCount(1);
        queryDetails.updateCounts();
        if (queryDetails.hasJdoGenieOptions()) {
            processJdoGenieOptions(p1);
            return executeWithArrayImp(null);
        } else {
            return executeWithArrayImp(new Object[]{p1});
        }
    }

    public Object execute(Object p1, Object p2) {
        checkParamCount(2);
        queryDetails.updateCounts();
        switch (queryDetails.getOptionsParamIndex()) {
            case 0:
                processJdoGenieOptions(p1);
                return executeWithArrayImp(new Object[]{p2});
            case 1:
                processJdoGenieOptions(p2);
                return executeWithArrayImp(new Object[]{p1});
        }
        return executeWithArrayImp(new Object[]{p1, p2});
    }

    public Object execute(Object p1, Object p2, Object p3) {
        checkParamCount(3);
        queryDetails.updateCounts();
        switch (queryDetails.getOptionsParamIndex()) {
            case 0:
                processJdoGenieOptions(p1);
                return executeWithArrayImp(new Object[]{p2, p3});
            case 1:
                processJdoGenieOptions(p2);
                return executeWithArrayImp(new Object[]{p1, p3});
            case 2:
                processJdoGenieOptions(p2);
                return executeWithArrayImp(new Object[]{p1, p2});
        }
        return executeWithArrayImp(new Object[]{p1, p2, p3});
    }

    public final Object executeWithArray(Object[] parameters) {
        queryDetails.updateCounts();
        int n = parameters == null ? 0 : parameters.length;
        checkParamCount(n);
        int oi = queryDetails.getOptionsParamIndex();
        if (oi >= 0) {
            processJdoGenieOptions(parameters[oi]);
            if (n == 1) {
                return executeWithArrayImp(null);
            }
            Object[] a = new Object[n - 1];
            if (oi > 0) System.arraycopy(parameters, 0, a, 0, oi);
            if (oi < n) System.arraycopy(parameters, oi + 1, a, oi, n - oi - 1);
            return executeWithArrayImp(a);
        } else {
            return executeWithArrayImp(copyParams(parameters));
        }
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
        int oi = queryDetails.getOptionsParamIndex();
        if (oi >= 0) {
            processJdoGenieOptions(parameters[oi]);
            if (n == 1) {
                return getPlanImp(null);
            }
            Object[] a = new Object[n - 1];
            if (oi > 0) System.arraycopy(parameters, 0, a, 0, oi);
            if (oi < n) System.arraycopy(parameters, oi, a, oi - 1, n - oi - 1);
            return getPlanImp(a);
        } else {
            return getPlanImp(parameters);
        }
    }

    public Object executeWithMap(Map parameters) {
        queryDetails.updateCounts();
        int tp = queryDetails.getTotalParamCount();
        if (parameters.size() != tp) {
            throw BindingSupportImpl.getInstance().runtime(
                    "The number of entries in the map (" + parameters.size() + ") " +
                    "differs from the number of declared parameters (" + tp + ")");
        }
        if (tp == 0) return executeWithArrayImp(null);

        // extract the normal parameters from the map in declaration order
        Object[] pa;
        int np = queryDetails.getParamCount();
        if (np > 0) {
            pa = new Object[np];
            String[] names = queryDetails.getParamNames();
            for (int i = 0; i < np; i++) {
                String name = names[i];
                if (!parameters.containsKey(name)) {
                    throw BindingSupportImpl.getInstance().runtime(
                            "Parameter '" + name + "' not found in map");
                }
                pa[i] = parameters.get(name);
            }
        } else {
            pa = null;
        }

        // process the jdoGenieOptions parameter if required
        if (queryDetails.hasJdoGenieOptions()) {
            Object o = parameters.get(VERSANT_OPTIONS);
            if (o == null) o = parameters.get(JDO_GENIE_OPTIONS);
            processJdoGenieOptions(o);
        }

        // exec
        return executeWithArrayImp(pa);
    }

    private void processJdoGenieOptions(Object o) {
        // restore default values first
        setFetchGroup(null);
        setRandomAccess(false);

        // now set properties
        if (o == null) return;
        if (!(o instanceof String)) {
            throw BindingSupportImpl.getInstance().runtime("Invalid " +
                    VERSANT_OPTIONS + ": Expected String value: " +
                    o.getClass());
        }
        String props = (String)o;
        if (props.length() == 0) return;
        try {
            BeanUtils.parseProperties(props, this);
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Invalid " + VERSANT_OPTIONS + ": " + e.getMessage(), e);
        }
    }

    /**
     * The parameters array must NOT contain the jdoGenieOptions.
     */
    private final Object executeWithArrayImp(Object[] parameters) {
        final PMProxy pmProxy = this.pmProxy;
        pmProxy.getRealPM().convertPcParamsToOID(parameters);
        if (!pmProxy.isActive() && !pmProxy.getRealPM().isNontransactionalRead()) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Must set nonTransactionalRead to true");
        }

        //Check if this should be a multi-part query
        Class cls = queryDetails.getCandidateClass();
        Class[] candidates = null;
        if (cls != null) {
            candidates = pmProxy.getRealPM().modelMetaData.getQueryCandidatesFor(cls);
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

                CompiledQuery cq = this.pmProxy.getRealPM().getStorageManager().compileQuery(qd);
                QueryResult qr = getQueryResult(parameters, qd, cq, this.pmProxy);

                qResults.add(qr);
            }
            return new MultiPartQueryResult(qResults);
        } else {
            compile();

            //is this a unique query
            if (compiledQuery.isUnique()) {
                if (!queryDetails.isIgnoreCache()) {
                    pmProxy.flushIfDepOn(compiledQuery.getEvictionClassBits());
                }
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
            for (int i = 0; i < params.length; i++) {
                params[i] = parameters[i];
            }
        }
        return params;
    }

    private QueryResult getQueryResult(Object[] params,
            QueryDetails queryDetails, CompiledQuery compiledQuery, PMProxy pmProxy) {
        QueryResult res;
        boolean collectionQuery = queryDetails.getCol() != null;
        boolean containsNewOID = containsNewOID(params);

        if (collectionQuery) {
            // query agains a collection in memory
            res = new MemoryQueryResult(pmProxy,
                    queryDetails, createSMList(queryDetails.getCol(), pmProxy), params);
        } else if (containsNewOID && queryDetails.isIgnoreCache()) {
            // query agains a collection in memory with some new instances
            res = new MemoryQueryResult();
        } else if (queryDetails.isRandomAccess()) {
            // random access query against database
            res = new RandomAccessQueryResult(pmProxy,
                    compiledQuery, params);
        } else {
            // normal query against database
            res = new ForwardQueryResult(pmProxy, queryDetails,
                    compiledQuery, params);
        }
        if (pmProxy.getMultithreaded()) {
            res = new SynchronizedQueryResult(pmProxy, res);
        }

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
        VersantPersistenceManagerImp realPM = pmProxy.getRealPM();
        realPM.convertPcParamsToOID(parameters);
        compile();
        return realPM.getStorageManager().getQueryPlan(queryDetails,
                compiledQuery, parameters);
    }

    private static List createSMList(Collection col, PMProxy pmProxy) {
        List tmpList = new ArrayList();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            PersistenceCapable persistenceCapable = (PersistenceCapable)iterator.next();
            tmpList.add(pmProxy.getInternalSM(persistenceCapable));
        }
        return tmpList;
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

    public PersistenceManager getPersistenceManager() {
        return pmProxy;
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

    public void initialiseFrom(VersantQueryImp clientQuery) {
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

    public void addExtension(String s, Object o) {
        //todo jdo2, implement this method
    }

    public long deletePersistentAll() {
        // todo jdo2 we need to handle deletePersistentAll()
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 deletePersistentAll() not implemented");
    }

    public long deletePersistentAll(Map map) {
        // todo jdo2 we need to handle deletePersistentAll(Map)
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 deletePersistentAll(Map) not implemented");
    }

    public long deletePersistentAll(Object[] objects) {
        // todo jdo2 we need to handle deletePersistentAll(Object[])
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 deletePersistentAll(Object[]) not implemented");
    }

    public FetchPlan getFetchPlan() {
        // todo jdo2 we need to handle getFetchPlan()
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 getFetchPlan() not implemented");
    }

    public boolean isUnmodifiable() {
        return false;  //todo jdo2, implement this method
    }

    public void setExtensions(Map map) {
        //todo jdo2, implement this method
    }

    public void setRange(long first, long last) {
        if (first == 0){
            setMaxRows((int)last);
        } else {
            throw BindingSupportImpl.getInstance().notImplemented(
                    "JDO 2 setRange(long first, long last) not implemented where first != 0");
        }
        //todo jdo2, implement this method
    }

    public void setResultClass(Class aClass) {
        throw BindingSupportImpl.getInstance().notImplemented(
                "JDO 2 setResultClass(Class) not implemented");
        //todo jdo2, implement this method
    }

    public void setUnmodifiable() {
        //todo jdo2, implement this method
    }
}
