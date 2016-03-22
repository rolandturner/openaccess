
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
package com.versant.core.server;

import com.versant.core.common.OIDArray;

import java.util.ArrayList;

/**
 * This is a datastruct to hold query results in the level2Cache.
 */
public class CachedQueryResult {
    /**
     * The actual results.
     */
    public ArrayList results;
    /**
     * All indirect oids.
     */
    public OIDArray indirectOIDs;

    public int getResultSize() {
        if (results == null) return 0;
        return results.size();
    }
}
