
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
package com.versant.core.storagemanager;

import com.versant.core.server.CachedQueryResult;
import com.versant.core.server.CompiledQuery;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.common.*;

/**
 * Cache of State's and query results shared by multiple StorageManager's.
 * A single class is used to cache both State's and query results so that
 * query results can be efficiently evicted when States of classes they
 * depend on are evicted. Only data read in a committed database transaction
 * may be added to the cache.
 */
public interface StorageCache {

    /**
     * This is invoked when the meta data is available.
     */
    public void setJDOMetaData(ModelMetaData jmd);

    /**
     * Is the cache enabled?
     */
    public boolean isEnabled();

    /**
     * Is query caching enabled?
     */
    public boolean isQueryCacheEnabled();

    /**
     * Begin a cache transaction and return an identifier for it. This
     * must be called before a new database transaction is started.
     */
    public Object beginTx();

    /**
     * End a cache transaction.
     */
    public void endTx(Object tx);

    /**
     * Get the State for an OID or null if it is not in cache or does not
     * contain the fields in the fetchGroup. This returns a copy of the
     * data in the cache. If fetchGroup is null then if any state is
     * present it is returned.
     */
    public State getState(OID oid, FetchGroup fetchGroup);

    /**
     * Does the cache contain any data for the oid? Note that the data could
     * be evicted at any time.
     */
    public boolean contains(OID oid);

    /**
     * Get cached query results or null if there are none.
     * @param cq
     * @param params
     */
    public CachedQueryResult getQueryResult(CompiledQuery cq, Object[] params);

    /**
     * Get result count or -1 if there are none.
     * @param cq
     * @param params
     */
    public int getQueryResultCount(CompiledQuery cq, Object[] params);

    /**
     * Add all the states in the container to the cache.
     */
    public void add(Object tx, StatesReturned container);

    /**
     * Add query results to the cache.
     */
    public void add(Object tx, CompiledQuery cq, Object[] params,
            CachedQueryResult queryData);

    /**
     * Add query result count to the cache.
     */
    public void add(Object tx, CompiledQuery cq, Object[] params, int count);

    /**
     * Evict length OIDs from oids starting at offset. Expected is the total
     * number of OIDs we expect to evict in the transaction or 0 if this is
     * not known. This may be used to optimize cache storage allocation.
     */
    public void evict(Object tx, OID[] oids, int offset, int length,
            int expected);

    /**
     * Evict all data for the classes.
     */
    public void evict(Object tx, ClassMetaData[] classes, int classCount);

    /**
     * Evict all data.
     */
    public void evictAll(Object tx);

    /**
     * Evict any cached information for the query.
     */
    public void evict(Object tx, CompiledQuery cq, Object[] params);
}

