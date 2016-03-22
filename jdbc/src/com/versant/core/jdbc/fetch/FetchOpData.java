
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

import com.versant.core.common.State;
import com.versant.core.common.OID;
import com.versant.core.metadata.ClassMetaData;

import java.sql.ResultSet;

/**
 * <p>This is used for communication between FetchOp's when one needs to
 * provide information to another. It knows how to get the information
 * required from a FetchData instance. This decouples the receiving
 * FetchOp from the one providing the data making FetchOp's more reusable.</p>
 *
 * <p>Note that these must not store any per-fetch instance data. This must go
 * in the fetchData slot for the FetchOp.</p>
 *
 * Remember to add to keep {@link FetchOpDataProxy} in sync with this class.
 */
public class FetchOpData {

    public OID getOID(FetchResult fetchResult) {
        return null;
    }

    public State getState(FetchResult fetchResult) {
        return null;
    }

    public ResultSet getResultSet(FetchResult fetchResult) {
        return null;
    }

    /**
     * Return a description of this data for the fetch plan. This should
     * include a leading space if it is not blank.
     */
    public String getDescription() {
        return "";
    }

    public ClassMetaData getType(FetchResult fetchResult) {
        return null;
    }

    public Object[] getProjectionResultArray(FetchResult fetchResult) {
        return null;
    }
}
