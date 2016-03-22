
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
package com.versant.core.common.config;

import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.metadata.parser.JdoRoot;

/**
 * This wraps a properties instance to provide access to the keys in a sorted
 * order.
 */
public class ConfigInfo {

    public Properties props;
    public String url;
    public String db;
    public String serverName;
    public boolean allowPmCloseWithOpenTx;
    public boolean precompileNamedQueries;
    public boolean checkModelConsistencyOnCommit;
    public boolean interceptDfgFieldAccess;
    public boolean hyperdrive;
    public String hyperdriveSrcDir;
    public String hyperdriveClassDir;
    public boolean keepHyperdriveBytecode;
    public int pmCacheRefType;
    public boolean testing;

    public String remoteAccess;

    public boolean retainValues;
    public boolean restoreValues;
    public boolean optimistic;
    public boolean nontransactionalRead;
    public boolean nontransactionalWrite;
    public boolean ignoreCache;
    public boolean multithreaded;
    public String connectionFactoryName;
    public String connectionFactory2Name;
    public boolean enlistedConnections;

    public boolean pmpoolEnabled;
    public int pmpoolMaxIdle;

    public boolean remotePmpoolEnabled;
    public int remotePmpoolMaxIdle;
    public int remotePmpoolMaxActive;

    /**
     * If query results to be cached.
     */
    public boolean queryCacheEnabled;
    public int maxQueriesToCache;
    public int compiledQueryCacheSize;

    /**
     * The names of .jdo and .jdoql resources.
     */
    public ArrayList jdoResources = new ArrayList();
    public JdoRoot[] jdoMetaData;

    public Map perfProps;

    public boolean useCache;
    public int cacheMaxObjects;
    public String cacheListenerClass;
    public Map cacheListenerProps;

    public int flushThreshold;
    public int datastoreTxLocking;
    public int retainConnectionInOptTx; // MdStatics.NOT_SET, FALSE, TRUE

    public int metricStoreCapacity;
    public int metricSnapshotIntervalMs;
    public ArrayList userBaseMetrics = new ArrayList(); // of UserBaseMetric

    public String logDownloaderClass;
    public Map logDownloaderProps;

    public List externalizers = new ArrayList();   // of ExternalizerInfo
    public Map scoFactoryRegistryMappings = new HashMap();
    public String metaDataPreProcessor;

    /**
     * Info on a user-defined base metric.
     */
    public static class UserBaseMetric {
        public String name;
        public String displayName;
        public String category;
        public String description;
        public int defaultCalc;
        public int decimals;
    }

    /**
     * Info on an externalizer.
     */
    public static class ExternalizerInfo {
        public String typeName;
        public boolean enabled;
        public String externalizerName;
        public Map args = new HashMap(17);
    }

    /**
     * Perform basic validation. There must be at least one store and jdo
     * file.
     */
    public void validate() {
        if (jdoResources.isEmpty()) {
            /**throw BindingSupportImpl.getInstance().runtime(
                    "At least one jdoNNN property is required");*/
        }
    }

    /**
     * Get the names of .jdo and .jdoql resources.
     */
    public String[] getJdoResources() {
        String[] a = new String[jdoResources.size()];
        jdoResources.toArray(a);
        return a;
    }
}
