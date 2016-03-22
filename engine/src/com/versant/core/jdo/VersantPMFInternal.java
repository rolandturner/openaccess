
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

import com.versant.core.metadata.ModelMetaData;
import com.versant.core.logging.LogEventStore;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageCache;

import java.util.Properties;
import java.util.Map;

/**
 * This adds methods to VersantPMF that are for internal use only.
 */
public interface VersantPMFInternal extends VersantPersistenceManagerFactory {

    /**
     * Create a new, unconfigured, PM. This is used by the pool to create
     * new PMs.
     */
    public VersantPersistenceManagerImp createVersantPersistenceManagerImp();

    /**
     * This is called by VersantPersistenceManagerImp when it is closed.
     *
     * @param fromFinalizer True if the PM was closed automatically by its
     *          finalizer
     */
    public void pmClosedNotification(VersantPersistenceManagerImp pm,
            boolean fromFinalizer, boolean txWasActive);

    /**
     * Get the meta data.
     */
    public ModelMetaData getJDOMetaData();

    /**
     * Get the event store.
     */
    public LogEventStore getLogEventStore();

    /**
     * Get the properties that we were created from.
     */
    public Properties getInitProperties();

    /**
     * Get our StorageManagerFactory.
     */
    public StorageManagerFactory getStorageManagerFactory();

    /**
     * Get the bytecode for the hypedrive classes or null if hyperdrive is
     * not in use. The map maps class name -> byte[] or null if the class was
     * not generated at runtime (i.e. loaded from our classloader). Each byte[]
     * is compressed with gzip to save memory.
     *
     * @see #getHyperdriveBytecodeMaxSize()
     */
    public Map getHyperdriveBytecode();

    /**
     * Get the maximum decompressed size of the bytecode for any of the
     * hyperdrive classes. This can be used to size a decompression buffer.
     */
    public int getHyperdriveBytecodeMaxSize();

    /**
     * Get the classloader we are using.
     */
    public ClassLoader getClassLoader();

    /**
     * Export this PMF to remote clients using pmfServer. Note that init
     * and start are not called on the added pmfServer. Only its close
     * method is called when the PMF is closed.
     */
    public void addPMFServer(PMFServer pmfServer);

    /**
     * If Local pmf.
     */
    boolean isLocal();

    /**
     * Return the level 2 cache.
     */
    StorageCache getStorageCache();

    /**
     * If the primary datasource returns already enlisted connections.
     */
    public boolean isEnlistedDataSource();    
}

