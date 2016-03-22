
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

import com.versant.core.server.CompiledQuery;
import com.versant.core.server.QueryResultWrapper;
import com.versant.core.common.QueryResultContainer;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.JDOListIterator;
import com.versant.core.jdo.QueryResultBase;

import java.util.NoSuchElementException;

/**
 * A ListIterator implemenation that supports forward iteration over the results.
 */
public class EJBQueryIterator implements JDOListIterator {
    private EMProxy pm;

    private Object[] data;
    private int actualSize;
    private int indexInData;
    private QueryResultWrapper qrw;
    private boolean noMoreDataInResultSet;
    private boolean closed;
    private int nextIndex;

    public EJBQueryIterator(EMProxy pm, CompiledQuery compiledQuery, Object[] params,
            boolean doNotFlush) {
        this.pm = pm;
        if (!doNotFlush && !compiledQuery.getQueryDetails().isIgnoreCache()) {
            pm.flushIfDepOn(compiledQuery.getEvictionClassBits());
        }
        qrw = pm.executeQuery(compiledQuery, params);
    }

    public void remove() {
        throw BindingSupportImpl.getInstance().unsupported("Not allowed to modify");
    }

    public boolean hasNext() {
        if (closed) return false;

        if (!noMoreDataInResultSet && (data == null || indexInData == actualSize)) {
            getMoreData();
        }
        return !(data == null || indexInData == actualSize);
    }

    /**
     * If we have not started yet then execute the query and return the first result.
     * If we have already started and there is still data available then return the
     * next data. If we are at the end of the last fetched data then get more
     * and return the first data.
     */
    public Object next() {
        if (closed) {
            throw new NoSuchElementException();
        }

        if (!noMoreDataInResultSet && (data == null || indexInData == actualSize)) {
            getMoreData();
        }
        return getNextData();
    }

    private void getMoreData() {
        //get data from server
        QueryResultContainer container = pm.getNextQueryResult(qrw, 0);
        if (container == null) {
            data = null;
            actualSize = 0;
            noMoreDataInResultSet = true;
        } else {
            pm.addToCache(container.container);
            data = container.getDataArray();
            actualSize = container.size();
            noMoreDataInResultSet = container.isqFinished();
        }
        indexInData = 0;
    }

    private Object getNextData() {
        if (indexInData == actualSize) throw new NoSuchElementException();
        nextIndex++;
        return QueryResultBase.resolveRow(data[indexInData++], pm);
    }

    /**
     * Cleanup all resources.
     */
    public void close() {
        if (closed) return;

        if (qrw != null) {
            pm.closeQuery(qrw);
            qrw = null;
        }
        
        pm = null;
        data = null;
        qrw = null;

        closed = true;
    }

    public int nextIndex() {
        return nextIndex;
    }

    public int previousIndex() {
        return nextIndex - 1;
    }

    public boolean hasPrevious() {
        return nextIndex != 0;
    }

    public Object previous() {
        throw BindingSupportImpl.getInstance().unsupportedOperation(null);
    }

    public void add(Object o) {
        throw BindingSupportImpl.getInstance().unsupported("Not allowed to modify");
    }

    public void set(Object o) {
        throw BindingSupportImpl.getInstance().unsupported("Not allowed to modify");
    }
}

/* CVS change log - do not edit
 *
 * $Log: EJBQueryIterator.java,v $
 * Revision 1.1  2005/04/19 09:43:44  jaco
 * updated for ejb3 support
 *
 * Revision 1.1.1.1  2005/03/08 08:36:03  david
 * Source from jdo2 project
 *
 * Revision 1.2  2005/03/07 10:07:07  jaco
 * refactored query stuff
 *
 * Revision 1.1  2005/03/02 16:33:14  jaco
 * updated for horizontal inheritance
 *
 */
