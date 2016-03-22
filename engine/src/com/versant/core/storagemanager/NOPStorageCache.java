
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

import com.versant.core.common.State;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.common.OID;
import com.versant.core.common.*;
import com.versant.core.server.CachedQueryResult;
import com.versant.core.server.CompiledQuery;

/**
 * Dummy StorageCache implementation.
 */
public final class NOPStorageCache implements StorageCache {

    public NOPStorageCache() {
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean isQueryCacheEnabled() {
        return false;
    }

    public Object beginTx() {
        return "NOPStorageCache";
    }

    public void endTx(Object tx) {
    }

    public State getState(OID oid, FetchGroup fetchGroup) {
        return null;
    }

    public boolean contains(OID oid) {
        return false;
    }

    public CachedQueryResult getQueryResult(CompiledQuery cq, Object[] params) {
        return null;
    }

    public int getQueryResultCount(CompiledQuery cq, Object[] params) {
        return 0;
    }

    public void add(Object tx, StatesReturned container) {
    }

    public void add(Object tx,
            CompiledQuery cq, Object[] params, CachedQueryResult queryData) {
    }

    public void add(Object tx, CompiledQuery cq, Object[] params, int count) {
    }

    public void evict(Object tx, OID[] oids, int offset, int length,
            int expected) {
    }

    public void evict(Object tx, ClassMetaData[] classes, int classCount) {
    }

    public void evictAll(Object tx) {
    }

    public void evict(Object tx, CompiledQuery cq, Object[] params) {
    }

    public void setJDOMetaData(ModelMetaData jmd) {
    }

}

