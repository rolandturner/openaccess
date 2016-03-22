
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

import com.versant.core.common.OID;
import com.versant.core.common.State;
import com.versant.core.common.Debug;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.metadata.ClassMetaData;

import java.sql.ResultSet;

/**
 * This delegates all calls to a src FetchOpData. Subclasses can extend this
 * and override some of the methods to return different data.
 */
public class FetchOpDataProxy extends FetchOpData {

    private FetchOpData src;

    public FetchOpDataProxy(FetchOpData src) {
        if (Debug.DEBUG) {
            if (src == null) {
                throw BindingSupportImpl.getInstance().internal("src == null");
            }
        }
        this.src = src;
    }

    public OID getOID(FetchResult fetchResult) {
        return src.getOID(fetchResult);
    }

    public State getState(FetchResult fetchResult) {
        return src.getState(fetchResult);
    }

    public ResultSet getResultSet(FetchResult fetchResult) {
        return src.getResultSet(fetchResult);
    }

    public ClassMetaData getType(FetchResult fetchResult) {
        return src.getType(fetchResult);
    }

    public Object[] getProjectionResultArray(FetchResult fetchResult) {
        return src.getProjectionResultArray(fetchResult);
    }

}

