
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

import com.versant.core.server.CompiledQuery;
import com.versant.core.server.QueryResultWrapper;

/**
 * This holds a CompiledQuery and RunningQuery instance returned by a
 * call to {@link StorageManager#executeQuery}. This will not need to extend
 * QueryResultWrapper when JDOConnection is finally gone.
 */
public interface ExecuteQueryReturn extends QueryResultWrapper {

    /**
     * Get the compiled form of the query. This can be cached and passed to
     * {@link StorageManager#executeQuery} to avoid recompilation. If a
     * compiledQuery was passed to executeQuery then this may return null.
     * This is used to optimize remote access.
     */
    public CompiledQuery getCompiledQuery();

    /**
     * This is passed to {@link StorageManager#fetchNextQueryResult}
     * or {@link StorageManager#fetchRandomAccessQueryResult} to retrieve
     * the results of the query.
     */
    public RunningQuery getRunningQuery();

}

