
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

import com.versant.core.metadata.ModelMetaData;
import com.versant.core.server.DataStoreInfo;
import com.versant.core.server.CompiledQueryCache;

import java.util.Set;

/**
 * Factory for StorageManager instances. There is a separate return method
 * so that a StorageManager pool does not have to create proxies for them.
 * Implementations must have a public constructor that accepts a
 * StorageManagerFactoryBuilder argument.
 */
public interface StorageManagerFactory {

    /**
     * Perform factory initialization that requires connecting to the
     * datastore. If the full flag is set then the factory will be fully
     * initialized e.g. keygen tables populated for the JDBC store. If this
     * flag is not set then the factory may connect to the datastore (e.g. to
     * check the server version) but will not do anything that depends on the
     * existence of tables and so on. The full == false option is useful
     * for tools that define the schema or do migrations etc.
     *
     * All dynamically generated classes will have been compiled at this
     * point. They can be loaded using loader.
     */
    public void init(boolean full, ClassLoader loader);

    /**
     * Get a StorageManager instance.
     */
    public StorageManager getStorageManager();

    /**
     * Return a StorageManager instance. The caller should hold no references
     * to the returned StorageManager.
     */
    public void returnStorageManager(StorageManager sm);

    /**
     * Free any resources held by this factory. None of the factory methods
     * should be called after this but the factory is not required to throw
     * exceptions if this happens.
     */
    public void destroy();

    /**
     * Get a native connection to the datastore. This must be wrapped so it
     * is returned when "closed" by the caller. If native connection access
     * is not supported then a user exception must be thrown.
     */
    public Object getDatastoreConnection();

    /**
     * If this factory maintains a connection pool for its StorageManager's
     * then close all idle connections in the pool. If the factory does
     * not maintain a pool then this is a NOP.
     */
    public void closeIdleDatastoreConnections();

    /**
     * Get the meta data being used by the factory.
     */
    public ModelMetaData getModelMetaData();

    /**
     * Get information about the factories datastore.
     */
    public DataStoreInfo getDataStoreInfo();

    /**
     * If we are decorating another SMF then return it. Otherwise return null.
     */
    public StorageManagerFactory getInnerStorageManagerFactory();

    /**
     * Make sure the options match our capabilities.
     */
    public void supportedOptions(Set options);

    /**
     * Get our CompiledQueryCache.
     */
    public CompiledQueryCache getCompiledQueryCache();
}

