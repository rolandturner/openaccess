
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
package com.versant.core.storagemanager.logging;

import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.metric.HasMetrics;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.server.DataStoreInfo;
import com.versant.core.server.CompiledQueryCache;
import com.versant.core.metric.BaseMetric;
import com.versant.core.metric.Metric;
import com.versant.core.metric.HasMetrics;
import com.versant.core.logging.LogEventStore;

import java.util.List;
import java.util.Set;

/**
 * Decorates another SMF to create SMs that log events and maintain
 * performance counters.
 */
public final class LoggingStorageManagerFactory
        implements StorageManagerFactory, HasMetrics {

    private final StorageManagerFactory smf;
    private final LogEventStore pes;

    public int txCount;
    public int txCommitCount;
    public int txCommitErrorCount;
    public int txRollbackCount;
    public int txFlushCount;
    public int txFlushErrorCount;
    public int fetchCount;
    public int fetchErrorCount;
    public int queryCount;
    public int queryErrorCount;

    private static final String CAT_MEM = "Memory";

    private final BaseMetric metricMemoryFree =
            new BaseMetric("MemFreeKB", "Mem Free KB", CAT_MEM,
                    "Available heap memory in KB", 0, Metric.CALC_AVERAGE);
    private final BaseMetric metricMemoryTotal =
            new BaseMetric("MemTotalKB", "Mem Total KB", CAT_MEM,
                    "Total heap memory in KB", 0, Metric.CALC_AVERAGE);

    private static final String CAT_TX = "Transactions";

    private final BaseMetric metricTx =
            new BaseMetric("Tx", "Tx", CAT_TX,
                    "Transactions started", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricTxCommit =
            new BaseMetric("TxCommit", "Tx Commit", CAT_TX,
                    "Transactions committed", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricTxCommitError =
            new BaseMetric("TxCommitError", "Tx Commit Error", CAT_TX,
                    "Transactions that failed with an error during commit", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricTxRollback =
            new BaseMetric("TxRollback", "Tx Rollback", CAT_TX,
                    "Transactions rolled back", 3,
                    Metric.CALC_DELTA);
    private final BaseMetric metricTxFlush =
            new BaseMetric("TxFlush", "Tx Flush", CAT_TX,
                    "Flushes prior to transaction commit", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricTxFlushError =
            new BaseMetric("TxFlushError", "Tx Flush Error", CAT_TX,
                    "Flushes that failed with an error", 3,
                    Metric.CALC_DELTA_PER_SECOND);

    private static final String CAT_FETCH = "Fetches";

    private final BaseMetric metricFetch =
            new BaseMetric("Fetch", "Fetch", CAT_FETCH,
                    "Number of calls to fetch data", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricFetchError =
            new BaseMetric("FetchError", "Fetch Error", CAT_FETCH,
                    "Number of calls to fetch data that failed with an error", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricQuery =
            new BaseMetric("Query", "Query", CAT_FETCH,
                    "Number of queries executed", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricQueryError =
            new BaseMetric("QueryError", "Query Error", CAT_FETCH,
                    "Number of queries executed that failed with an error", 3,
                    Metric.CALC_DELTA_PER_SECOND);

    public LoggingStorageManagerFactory(StorageManagerFactory smf,
            LogEventStore pes) {
        this.smf = smf;
        this.pes = pes;
    }

    public LogEventStore getLogEventStore() {
        return pes;
    }

    public void init(boolean full, ClassLoader loader) {
        smf.init(full, loader);
    }

    public void destroy() {
        smf.destroy();
    }

    public StorageManager getStorageManager() {
        StorageManager sm = smf.getStorageManager();
        return new LoggingStorageManager(this, sm);
    }

    public void returnStorageManager(StorageManager sm) {
        smf.returnStorageManager(sm);
    }

    public ModelMetaData getModelMetaData() {
        return smf.getModelMetaData();
    }

    public Object getDatastoreConnection() {
        return smf.getDatastoreConnection();
    }

    public void closeIdleDatastoreConnections() {
        smf.closeIdleDatastoreConnections();
    }

    public DataStoreInfo getDataStoreInfo() {
        return smf.getDataStoreInfo();
    }

    public StorageManagerFactory getInnerStorageManagerFactory() {
        return smf;
    }

    public void addMetrics(List list) {
        list.add(metricMemoryFree);
        list.add(metricMemoryTotal);
        list.add(metricTx);
        list.add(metricTxCommit);
        list.add(metricTxCommitError);
        list.add(metricTxRollback);
        list.add(metricTxFlush);
        list.add(metricTxFlushError);
        list.add(metricFetch);
        list.add(metricFetchError);
        list.add(metricQuery);
        list.add(metricQueryError);
        if (smf instanceof HasMetrics) {
            ((HasMetrics)smf).addMetrics(list);
        }
    }

    public void sampleMetrics(int[][] buf, int pos) {
        Runtime r = Runtime.getRuntime();
        buf[metricMemoryFree.getIndex()][pos] = (int)(r.freeMemory() >> 10);
        buf[metricMemoryTotal.getIndex()][pos] = (int)(r.totalMemory() >> 10);
        buf[metricTx.getIndex()][pos] = txCount;
        buf[metricTxCommit.getIndex()][pos] = txCommitCount;
        buf[metricTxCommitError.getIndex()][pos] = txCommitErrorCount;
        buf[metricTxRollback.getIndex()][pos] = txRollbackCount;
        buf[metricTxFlush.getIndex()][pos] = txFlushCount;
        buf[metricTxFlushError.getIndex()][pos] = txFlushErrorCount;
        buf[metricFetch.getIndex()][pos] = fetchCount;
        buf[metricFetchError.getIndex()][pos] = fetchErrorCount;
        buf[metricQuery.getIndex()][pos] = queryCount;
        buf[metricQueryError.getIndex()][pos] = queryErrorCount;
        if (smf instanceof HasMetrics) {
            ((HasMetrics)smf).sampleMetrics(buf, pos);
        }
    }

    public void supportedOptions(Set options) {
        smf.supportedOptions(options);
    }

    public CompiledQueryCache getCompiledQueryCache() {
        return smf.getCompiledQueryCache();
    }

}

