
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

import com.versant.core.common.QueryResultContainer;
import com.versant.core.server.CompiledQuery;
import com.versant.core.server.QueryResultWrapper;



import java.util.*;
import java.lang.reflect.Array;

import com.versant.core.common.BindingSupportImpl;

/**
 * This is used for the results of queries executed with randomAccess=true.
 * It provides fully random access to the List of results using a scrollable
 * ResultSet on the server side.
 */
public final class RandomAccessQueryResult extends QueryResultBase {

    private QueryResultWrapper qrsIF;
    private PMProxy pmProxy;
    private final CompiledQuery compiledQuery;
    private Object[] params;
    private List openQIters = new ArrayList();
    private int size = -1;
    private int fetchAmount;


    private Object[] window;
    private int absoluteIndexStart = -1;
    private int windowSize;

    public RandomAccessQueryResult(PMProxy pm,
            CompiledQuery compiledQuery, Object[] params) {
        this.pmProxy = pm;
        this.compiledQuery = compiledQuery;
        qrsIF = pmProxy.executeQuery(compiledQuery, params);
        if (!compiledQuery.getQueryDetails().isIgnoreCache()) {
            pmProxy.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }
        this.params = params;
        fetchAmount = compiledQuery.getQueryDetails().getResultBatchSize();
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    private void checkClosed() {
        if (qrsIF == null) {
            throw BindingSupportImpl.getInstance().invalidOperation("Query result has been closed");
        }
    }

    public void setBatchFetchSize(int amount) {
        if (amount < 1) {
            throw BindingSupportImpl.getInstance().invalidOperation("'FetchSize of '" + amount + " is invalid");
        }
        fetchAmount = amount;
    }

    public Object get(int index) {
        checkClosed();
        if (index < 0) {
            throw BindingSupportImpl.getInstance().invalidOperation("Index must be greater or equal to '0'");
        }


        if (index < absoluteIndexStart) {
            //before current window

            QueryResultContainer container = null;
            synchronized(pmProxy) {
                container = pmProxy.getAbsolute(qrsIF, index, fetchAmount);
                pmProxy.addToCache(container.container);
            }

            window = container.getDataArray();
            absoluteIndexStart = index;
            windowSize = container.size();

            container.reset();
        } else if (index > absoluteIndexStart) {
            //might be in window
            int absoluteIndexEnd = ((absoluteIndexStart + windowSize) - 1);
            if (index > absoluteIndexEnd) {
                //after window
                QueryResultContainer container = pmProxy.getAbsolute(qrsIF, index, fetchAmount);
                pmProxy.addToCache(container.container);

                window = container.getDataArray();
                absoluteIndexStart = index;
                windowSize = container.size();

                container.reset();
            }
        }

        if (windowSize == 0) {
            throw BindingSupportImpl.getInstance().arrayIndexOutOfBounds("index '"
                    + index + "' is past the end of the result.");
        }
        return resolveRow(window[index - absoluteIndexStart], pmProxy);
    }

    /**
     * @return
     */
    public int size() {
        if (size == -1) {
            checkClosed();
            size = pmProxy.getResultCount(qrsIF);
        }
        return size;
    }

    public boolean isEmpty() {
        return size() > 0;
    }

    private RuntimeException createUseNonRandomAccessException() {
        return BindingSupportImpl.getInstance().invalidOperation(
                "Method not available for a randomAccess=true query");
    }

    public boolean contains(Object o) {
        throw createUseNonRandomAccessException();
    }

    public Object[] toArray() {
        if (!compiledQuery.getQueryDetails().isIgnoreCache()) {
            pmProxy.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }

        QueryResultContainer qContainer = pmProxy.getAllQueryResults(compiledQuery, params);
        pmProxy.addToCache(qContainer.container);

        Object[] resolvedData = qContainer.toResolvedObject(pmProxy);
        int n = qContainer.size();
        Object[] a = new Object[n];
        for (int i = 0; i < n; i++) {
            a[i] = resolvedData[i];
        }
        return a;
    }

    public Object[] toArray(Object a[]) {
        if (a == null) throw new NullPointerException("The supplied array is null");

        if (!compiledQuery.getQueryDetails().isIgnoreCache()) {
            pmProxy.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }

        QueryResultContainer qContainer = pmProxy.getAllQueryResults(compiledQuery, params);
        pmProxy.addToCache(qContainer.container);

        Object[] resolvedData = qContainer.toResolvedObject(pmProxy);
        int n = qContainer.size();
        if (n > a.length) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(), n);
        }

        for (int i = 0; i < n; i++) {
            a[i] = resolvedData[i];
        }
        return a;
    }

    public boolean containsAll(Collection c) {
        throw createUseNonRandomAccessException();
    }

    public int indexOf(Object o) {
        throw createUseNonRandomAccessException();
    }

    public int lastIndexOf(Object o) {
        throw createUseNonRandomAccessException();
    }

    public Iterator iterator() {
        return createInternalIter();
    }

    public ListIterator listIterator() {
        return createInternalIter();
    }

    public ListIterator listIterator(int index) {
        throw BindingSupportImpl.getInstance().unsupported(null);
    }

    public List subList(int fromIndex, int toIndex) {
        checkClosed();
        QueryResultContainer container = null;
        final int toAdd = toIndex - fromIndex;
        if (toAdd <= 0) {
            return new ArrayList();
        }
        final List list = new ArrayList(toAdd);
        container = pmProxy.getAbsolute(qrsIF, fromIndex, toAdd);

        //provide it for instanceCache
        pmProxy.addToCache(container.container);

        container.resolveAndAddTo(list, pmProxy);
        container.reset();
        return list;
    }

    private ListIterator createInternalIter() {
        return createInternalIterImp(false);
    }

    private ListIterator createInternalIterImp(boolean doNotFlush) {
        checkClosed();
        QueryIterator queryIterator = new QueryIterator(pmProxy, compiledQuery, params, doNotFlush);
        openQIters.add(queryIterator);
        return queryIterator;
    }

    public Iterator createInternalIterNoFlush() {
        return createInternalIterImp(true);
    }

    public void close() {
        for (int i = 0; i < openQIters.size(); i++) {
            ((JDOListIterator)openQIters.get(i)).close();
        }
        openQIters.clear();

        if (qrsIF != null) {
            pmProxy.closeQuery(qrsIF);
            qrsIF = null;
        }

        window = null;
    }

}
