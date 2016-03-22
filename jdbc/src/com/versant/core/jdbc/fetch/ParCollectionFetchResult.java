
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

import com.versant.core.jdbc.metadata.JdbcCollectionField;
import com.versant.core.common.BindingSupportImpl;

/**
 * This is a container that holds the relevant info for a parCol fetch that
 * is being processes.
 */
public class ParCollectionFetchResult {
    private final FetchResult fetchResult;
    private final JdbcCollectionField colField;

    public ParCollectionFetchResult(JdbcCollectionField colFied, FetchResult subFResult) {
        this.colField = colFied;
        this.fetchResult = subFResult;
    }

    public FetchResult getFetchResult() {
        return fetchResult;
    }
}
