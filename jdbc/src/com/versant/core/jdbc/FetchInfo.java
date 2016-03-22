
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
package com.versant.core.jdbc;

import com.versant.core.common.OID;

/**
 * This is a data structure that is used to hold information when fetching data
 * from a queryResult
 */
public class FetchInfo {
    public static final int BREAK_STATUS_DEFAULT = 0;
    public static final int BREAK_STATUS_NULL = 1;
    public static final int BREAK_STATUS_VALID = 2;
    public static final int BREAK_STATUS_READ = 3;

    /**
     * Why did the iteration stop.
     */
    public int breakStatus;
    /**
     * Did we advance to a next row.
     */
    public boolean onNextRow;
    /**
     * Is this a valid row.
     */
    public boolean onValidRow;
    /**
     * If we advanced to the next row then this is the oid.
     */
    public OID nextOid;
    public boolean finished;

    public void reset() {
        nextOid = null;
        breakStatus = 0;
        onNextRow = false;
    }
}
