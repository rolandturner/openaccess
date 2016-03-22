
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

import com.versant.core.common.PCList;
import com.versant.core.common.QueryResultContainer;
import com.versant.core.server.CompiledQuery;
import com.versant.core.server.QueryResultWrapper;

import java.io.Serializable;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;

/**
 * Forward only access to the results of a Query. This will rerieve all results
 * if any method requiring access to all results is called (e.g. size).
 * Otherwise it retrieves elements in batches.
 *
 * @see QueryResult
 */
public final class ForwardQueryResult extends QueryResultBase
        implements Serializable {

    /**
     * Nothing has happened as such and hence anything is allowed at this stage.
     */
    private static final int STATUS_NON_INITIALISED = 0;
    /**
     * Implies that the results have been resolved fully and get's and iterators is allowed.
     */
    private static final int STATUS_RESOLVED = 1;
    /**
     * Implies that a get was called and hence no iterators may be created
     */
    private static final int STATUS_SPARSE = 2;
    /**
     * Implies that the query has been closed.
     */
    private static final int STATUS_CLOSED = 3;
    /**
     * The current status.
     */
    private int status;

    /**
     * The size if it has been computed. This depends on the state.
     */
    private int size;
    /**
     * If this is a countOnSize query? We only do a count once and then clear
     * the flag so more size() calls will resolve the results.
     */
    private boolean countOnSize;
    private PCList backingArray;

    private final PMProxy pm;
    /**
     * The params that was passed for query execution.
     */
    private Object[] params;
    private final List openWrapperIters = new ArrayList();

    private final QueryDetails queryDetails;
    private final CompiledQuery compiledQuery;

    /**
     * Fields used to implement a window in the resultset
     */
    private Object[] window;
    private int windowSize;
//    private int windowIndex;
    private boolean noMoreDataInResultSet;

    private int index;
    private int maxAvailableIndex = -1;
    private QueryResultWrapper qrw;

    public ForwardQueryResult(PMProxy pmProxy, QueryDetails queryDetails,
            CompiledQuery compiledQuery, Object[] params) {
        this.pm = pmProxy;
        this.queryDetails = queryDetails;
        this.compiledQuery = compiledQuery;
        this.setParams(params);
        countOnSize = queryDetails.isCountOnSize();
    }

    /**
     * Is our compiledQuery the same as the one for qc?
     */
    public boolean isCompiledQueryEqual(ForwardQueryResult qc) {
        return compiledQuery.equals(qc.compiledQuery);
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    /**
     * Close all the open iterator's.
     */
    public void close() {
        if (status == STATUS_CLOSED) return;

        if (qrw != null) {
            pm.closeQuery(qrw);
            qrw = null;
        }
        window = null;

        backingArray = null;
        for (int i = 0; i < openWrapperIters.size(); i++) {
            ((JDOListIterator)openWrapperIters.get(i)).close();
        }
        openWrapperIters.clear();
        status = STATUS_CLOSED;
    }

    /**
     * All the results will be resolved and the size returned. If this is already been called the the size will just
     * be returned.
     */
    public int size() {
        if (status != STATUS_RESOLVED) {
            if (countOnSize) {
                countOnSize = false;
                return countRows();
            } else {
                resolve();
            }
        }
        return size;
    }

    public boolean isEmpty() {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.isEmpty();
    }

    public boolean contains(Object o) {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.contains(o);
    }

    public Iterator iterator() {
        checkClosed();
        Iterator result = null;
        if (status == STATUS_RESOLVED) {
            result = getLocalIter();
        } else {
            result = createInternalIter();
        }
        return result;
    }

    /**
     * This is an iterator over the already fully resolved list.
     *
     * @see #backingArray
     */
    private ListIterator getLocalIter() {
        ListIterator lIter = backingArray.listIterator();
        openWrapperIters.add(lIter);
        return lIter;
    }

    /**
     * This executes a new server side query.
     *
     * @see QueryResultIterator
     */
    private ListIterator createInternalIter() {
        return createInternalIterImp(false);
    }

    private ListIterator createInternalIterImp(boolean doNotFlush) {
        checkClosed();
        QueryIterator queryIterator = new QueryIterator(pm, compiledQuery, params, doNotFlush);
        openWrapperIters.add(queryIterator);
        return queryIterator;
    }

    public Iterator createInternalIterNoFlush() {
        return createInternalIterImp(true);
    }

    public ListIterator listIterator() {
        return (ListIterator)iterator();
    }

    public ListIterator listIterator(int index) {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }

    /**
     * Resolves all the data and add it to array.
     */
    public Object[] toArray() {
        toArrayImp();
        return backingArray.toArray();
    }

    public Object[] toArray(Object[] a) {
        toArrayImp();
        return backingArray.toArray(a);
    }

    private void toArrayImp() {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
    }

    private void resolve() {
        if (status == STATUS_RESOLVED) return;
        checkClosed();
        if (status == STATUS_SPARSE) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Any operation that will fully resolve" +
                    " the query may not be called once a 'get' operation was performed");
        }

        if (!queryDetails.isIgnoreCache()) {
            pm.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }

        QueryResultContainer qContainer = pm.getAllQueryResults(
                compiledQuery, params);
        pm.addToCache(qContainer.container);

        backingArray = new PCList(qContainer.toResolvedObject(pm), 0,
                qContainer.size());
        size = backingArray.size();
        status = STATUS_RESOLVED;
        qContainer.reset();
        pm.processLocalCacheReferenceQueue();
    }

    private void checkClosed() {
        if (status == STATUS_CLOSED) {
            throw BindingSupportImpl.getInstance().invalidOperation("Query result has been closed");
        }
    }

    /**
     * Execute the query in count(*) mode (countOnSize option).
     */
    private int countRows() {
        checkClosed();
        if (!queryDetails.isIgnoreCache()) {
            pm.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }
        return pm.getQueryRowCount(compiledQuery, params);
    }

    /**
     * If the backingArray exist then the get should operate on it.
     */
    public Object get(int index) {
        if (index < 0) {
            throw BindingSupportImpl.getInstance().illegalArgument(
                    "Index smaller than zero is not allowed");
        }
        Object result = null;
        switch (status) {
            case STATUS_RESOLVED:
                result = backingArray.get(index);
                break;
            case STATUS_SPARSE:
                result = internalGet(index, maxAvailableIndex);
                break;
            case STATUS_NON_INITIALISED:
                if (queryDetails.prefetchAll()) {
                    resolve();
                    result = backingArray.get(index);
                } else {
                    if (!queryDetails.isIgnoreCache()) {
                        pm.flushIfDepOn(compiledQuery.getEvictionClassBits());
                    }

                    QueryResultWrapper qrsIF =
                            this.pm.executeQuery(compiledQuery, params);
                    QueryResultContainer qContainer =
                            this.pm.getNextQueryResult(qrsIF, index);


                    if (qContainer.allResults) {
                        pm.addToCache(qContainer.container);

                        status = STATUS_RESOLVED;
                        backingArray = new PCList(qContainer.toResolvedObject(pm), 0,
                                qContainer.size());
                        size = backingArray.size();
                        status = STATUS_RESOLVED;

                        qContainer.reset();
                        result = backingArray.get(index);
                        pm.processLocalCacheReferenceQueue();
                    } else {
                        status = STATUS_SPARSE;
                        qrw = qrsIF;
                        addNewData(qContainer, index);
                        result = internalGet(index, maxAvailableIndex);
                    }
                }
                break;
            default:
                checkClosed();
                throw BindingSupportImpl.getInstance().internal(
                        "Status does not exist. Status = '" + status + "'");
        }
        return result;
    }

    private Object internalGet(int requestedIndex, final int maxAvailableIndex) {
        if (requestedIndex > maxAvailableIndex && !noMoreDataInResultSet) {
            getMoreData(requestedIndex);
            return getNextData(0);
        } else {
            return getNextData(windowSize - ((maxAvailableIndex - requestedIndex) + 1));
        }

    }

    private Object getNextData(int windowIndex) {
        if (windowIndex == windowSize)
            throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds("index '"
                    + index + "' is too big");

        if (windowIndex < 0) {
            throw BindingSupportImpl.getInstance().unsupported(
                    "May only request index greater than the previously requested index." +
                    "\nIf this is required then use VersantQuery.setRandomAccess.");
        }

        Object result = QueryResultBase.resolveRow(window[windowIndex], pm);
        window[windowIndex] = null;
        return result;
    }

    private void getMoreData(int requestedIndex) {
        //get data from server
        addNewData(pm.getNextQueryResult(qrw, requestedIndex - maxAvailableIndex - 1), requestedIndex);
    }

    private void addNewData(QueryResultContainer container, int requestedIndex) {
        pm.addToCache(container.container);

        window = container.getDataArray();
        windowSize = container.size();
        noMoreDataInResultSet = container.isqFinished();

        index = requestedIndex;
        maxAvailableIndex = index + windowSize - 1;
        container.reset();
    }

    public int indexOf(Object o) {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.lastIndexOf(o);
    }

    public List subList(int fromIndex, int toIndex) {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.subList(fromIndex, toIndex);
    }

    public boolean containsAll(Collection c) {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.containsAll(c);
    }

    public boolean equals(Object obj) {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.equals(obj);
    }

    public String toString() {
        if (status != STATUS_RESOLVED) {
            resolve();
        }
        return backingArray.toString();
    }

    /**
     * Serialize out an ArrayList instead of ourselves.
     */
    public Object writeReplace() {
        return new ArrayList(this);
    }

}
