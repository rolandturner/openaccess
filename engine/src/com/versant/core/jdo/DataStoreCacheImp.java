
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
package com.versant.core.jdo;

import javax.jdo.datastore.DataStoreCache;
import java.util.Collection;

/**
 * This class is a wrapper for our level 2 Cache
 */
public class DataStoreCacheImp implements DataStoreCache {
    private VersantPersistenceManagerFactory pmf;

    public DataStoreCacheImp(VersantPersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    public void evict(Object o) {
        pmf.evict(o);
    }

    public void evictAll() {
        pmf.evictAll();
    }

    public void evictAll(Object[] objects) {
        pmf.evictAll(objects);
    }

    public void evictAll(Collection collection) {
        pmf.evictAll(collection);
    }

    public void evictAll(Class aClass, boolean b) {
        pmf.evictAll(aClass, b);
    }

    public void pin(Object o) {
        //We do not support pinning
    }

    public void pinAll(Collection collection) {
        //We do not support pinning
    }

    public void pinAll(Object[] objects) {
        //We do not support pinning
    }

    public void pinAll(Class aClass, boolean b) {
        //We do not support pinning
    }

    public void unpin(Object o) {
        //We do not support unpinning
    }

    public void unpinAll(Collection collection) {
        //We do not support unpinning
    }

    public void unpinAll(Object[] objects) {
        //We do not support unpinning
    }

    public void unpinAll(Class aClass, boolean b) {
        //We do not support unpinning
    }
}

