
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
package com.versant.core.jdbc.fetch;

/**
 * Flags etc to control fetching.
 */
public class FetchOptions {

    private boolean useParallelQueries;
    private boolean useOneToManyJoin = false;

    public FetchOptions() {
    }

    public FetchOptions(boolean useOneToManyJoin, boolean useParallelQueries) {
        this.useOneToManyJoin = useOneToManyJoin;
        this.useParallelQueries = useParallelQueries;
    }

    /**
     * Set if the plan may consist of multiple separate SQL queries processed
     * in parallel i.e. several ResultSet's will be open at once. Each query
     * will have essentially the same where clause.
     */
    public void setUseParallelQueries(boolean useParallelQueries) {
        this.useParallelQueries = useParallelQueries;
    }

    public boolean isUseParallelQueries() {
        return useParallelQueries;
    }

    /**
     * Set if the plan may prefetch one to many relationships by joining
     * them to the main query to fetch the data. The main query will return
     * n * m rows but a separate query will not need to be done to fetch
     * the relationships.
     */
    public void setUseOneToManyJoin(boolean useOneToManyJoin) {
        this.useOneToManyJoin = useOneToManyJoin;
    }

    public boolean isUseOneToManyJoin() {
        return useOneToManyJoin;
    }
}

