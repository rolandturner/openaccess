
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

import com.versant.core.common.Debug;
import com.versant.core.common.QueryResultContainer;
import com.versant.core.common.Stack;
import com.versant.core.server.CompiledQuery;
import com.versant.core.server.QueryResultWrapper;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.storagemanager.ExecuteQueryReturn;

/**
 * Forward only Iterator for query results. Data is fetched lazily in batches
 * as it is required. Instances of this class are created when iterator()
 * is called on the results of a JDOQL query. This supports multithreaded
 * PM access by synchronizing some operations on the PM proxy.
 *
 * @see com.versant.core.jdo.ForwardQueryResult
 * @see RandomAccessQueryResult
 * @see QueryDetails#setResultBatchSize
 */
public final class QueryResultIterator implements JDOListIterator {

    private PMProxy pmProxy;
    /**
     * Indicates that the iterator is closed.
     */
    private boolean closed = false;
    /**
     * Indicates that all results from the jdbc query has been processed.
     */
    private boolean queryFinished = false;
    /**
     * This is used as a token to identify the serverside queryResult.
     */
    private QueryResultWrapper qrsIF = null;
    /**
     * A stack where all retrieved results is kept till it is iterated past.
     */
    public final Stack stack = new Stack();

    public int toSkip;
    private int nextIndex;

    public QueryResultIterator(PMProxy pmProxy, QueryDetails queryDetails,
            CompiledQuery compiledQuery, Object[] params, boolean doNotFlush) {
        this.pmProxy = pmProxy;
        // no need to synchronize on pmProxy here as we are created inside
        // synchronized block
        if (!doNotFlush && !queryDetails.isIgnoreCache()) {
            pmProxy.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }
        qrsIF = pmProxy.executeQuery(compiledQuery, params);
    }

    public QueryResultIterator(PMProxy pmProxy, QueryResultWrapper qrsw,
            QueryResultContainer container, int index) {
        this.pmProxy = pmProxy;
        qrsIF = qrsw;
        nextIndex = index;
        if (container != null) container.addToQueryStack(stack);
        if (container != null && container.qFinished) queryFinished = true;
    }

    public int nextIndex() {
        return nextIndex;
    }

    public boolean hasNext() {
        if (closed) return false;
        if (stack.size() > 0) return true;
        addToQueryStack();
        return (stack.size() > 0);
    }

    /**
     * Try and add more results to the query stack. This also triggers
     * processing of the ReferenceQuery for the PMs local cache. This stops
     * long running reporting type queries from leaking SoftReferences.
     */
    private void addToQueryStack() {
        final VersantPersistenceManagerImp realPM = pmProxy.getRealPM();
        synchronized (pmProxy) {
            realPM.processLocalCacheReferenceQueue();
            QueryResultContainer container = null;
            if (!queryFinished && stack.isEmpty()) {
                container = realPM.getStorageManager().fetchNextQueryResult(
                        realPM, ((ExecuteQueryReturn)qrsIF).getRunningQuery(),
                        toSkip);
                toSkip = 0;

                if (container == null) {
                    closed = true;
                    return;
                }

                if (container.qFinished) queryFinished = true;
                if (Debug.DEBUG) {
                    Debug.OUT.println("############ amount recieved = " + container.size()
                            + " for queryId = " + qrsIF + " (inner container size " +
                            container.container.size() + ")");
                }
                //add to managed cache.
                realPM.addToCache(container.container);
                //add the results to the stack.
                container.addToQueryStack(stack);
            }
            if (container != null && container.qFinished) queryFinished = true;
            if (container != null) container.reset();
        }
    }

    public Object next() {
        if (!hasNext()) throw BindingSupportImpl.getInstance().noSuchElement("");
        nextIndex++;
        return QueryResultBase.resolveRow(stack.pop(), pmProxy);
    }

    public final void close() {
        if (closed) return;
        try {
            stack.close();
            if (qrsIF != null) {
                synchronized (pmProxy) {
                    pmProxy.getRealPM().getStorageManager().closeQuery(
                            ((ExecuteQueryReturn)qrsIF).getRunningQuery());
                }
                qrsIF = null;
            }
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
        } finally {
            closed = true;
        }
    }

    public final void remove() {
        throw BindingSupportImpl.getInstance().unsupported("Not allowed to modify");
    }

    public boolean hasPrevious() {
        throw BindingSupportImpl.getInstance().unsupportedOperation(null);
    }

    public Object previous() {
        throw BindingSupportImpl.getInstance().unsupportedOperation(null);
    }

    public int previousIndex() {
        throw BindingSupportImpl.getInstance().unsupportedOperation(null);
    }

    public void set(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation(null);
    }

    public void add(Object o) {
        throw BindingSupportImpl.getInstance().unsupportedOperation(null);
    }
}
