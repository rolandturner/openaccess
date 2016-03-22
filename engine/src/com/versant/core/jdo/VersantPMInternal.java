
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

import com.versant.core.server.QueryResultWrapper;
import com.versant.core.server.CompiledQuery;
import com.versant.core.common.QueryResultContainer;
import com.versant.core.common.StatesReturned;

import javax.jdo.spi.PersistenceCapable;

/**
 * This interface is there to make it easier for instances like sco's or queryresult,
 * that are directly accessible to the client to invoke methods on the pm.
 * This also ensures that the method is run within a synchronized block if nec.
 *
 * @see UnsynchronizedPMProxy
 * @see SynchronizedPMProxy
 */
public interface VersantPMInternal {
    public QueryResultWrapper executeQuery(CompiledQuery cq, Object[] params);

    public QueryResultContainer getNextQueryResult(QueryResultWrapper aQrs,
            int skipAmount);

    public void closeQuery(QueryResultWrapper qrw);

    public void flushIfDepOn(int[] bits);

    public void processLocalCacheReferenceQueue();

    public void addToCache(StatesReturned container);

    public QueryResultContainer getAbsolute(QueryResultWrapper qrsIF,
            int index, int fetchAmount);

    public int getResultCount(QueryResultWrapper qrsIF);

    public int getQueryRowCount(CompiledQuery cq, Object[] params);

    public QueryResultContainer getAllQueryResults(CompiledQuery cq,
            Object[] params);

    public void setMasterOnDetail(PersistenceCapable detail, int managedFieldNo,
            PersistenceCapable master, boolean removeFromCurrentMaster);


    public Object getObjectField(PersistenceCapable pc,
            int fieldNo);

    public int getObjectsById(Object[] a, int count, Object[] data, int stateFieldNo,
            int index);
}
