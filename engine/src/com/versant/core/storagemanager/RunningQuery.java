
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

import com.versant.core.jdo.QueryDetails;

/**
 * This identifies the "server side" of a query executed by a StorageManager.
 * Methods on StorageManager that return data from a query use one of these
 * to identify the query.
 *
 * @see ExecuteQueryReturn
 * @see StorageManager#fetchNextQueryResult 
 */
public interface RunningQuery {

    /**
     * Return the QueryDetails this query was compiled from. This method
     * may return null if this information is not available.
     */
    public QueryDetails getQueryDetails();

}

