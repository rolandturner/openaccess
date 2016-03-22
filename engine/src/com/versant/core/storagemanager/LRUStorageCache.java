
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
import com.versant.core.metric.*;

import java.util.*;
import java.io.PrintStream;

/**
 * Count limited StorageCache implementation that uses an LRU algorithm to
 * limit the number of cached instances and query results.
 */
public final class LRUStorageCache implements StorageCache, HasMetrics {

    private final Map stateMap; // OID -> StateEntry
    private final Map queryMap; // QueryEntry -> QueryEntry

    private ModelMetaData jmd;
    private boolean enabled = true;
    private boolean queryCacheEnabled = true;
    private int maxObjects = 10000;
    private int maxQueries = 1000;
    private int objectCount;
    private int queryCount;

    private long now;
    private long evictAllTimestamp;

    // single linked active Tx list
    private Tx txTail, txHead;

    // double linked LRU State list
    private StateEntry stateTail;    // least recently accessed State
    private StateEntry stateHead;    // most recently accessed State

    // double linked list of StateEntry's for each class
    private StateEntry[] classStateHead;

    // time each class was last evicted
    private long[] classEvictionTimestamp;

    // double linked LRU query key list
    private QueryEntry queryHead;
    private QueryEntry queryTail;

    // double linked list for each class containing the queries that depend it
    private QueryEntryNode[] classQueryHead;

    private int hitCount;
    private int missCount;
    private int queryHitCount;
    private int queryMissCount;

    private static final Tx DISABLED_TX = new Tx(0);

    private static final String CAT_CACHE = "L2Cache";

    private final BaseMetric metricCacheSize =
            new BaseMetric("CacheSize", "Cache Size", CAT_CACHE,
                    "Number of objects in the level 2 cache", 0,
                    Metric.CALC_AVERAGE);
    private final BaseMetric metricCacheMaxSize =
            new BaseMetric("CacheMaxSize", "Cache Max Size", CAT_CACHE,
                    "Max number of objects to store in the level 2 cache", 0,
                    Metric.CALC_AVERAGE);
    private final BaseMetric metricCacheHit =
            new BaseMetric("CacheHit", "Cache Hit", CAT_CACHE,
                    "Number of times data was found in cache", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricCacheMiss =
            new BaseMetric("CacheMiss", "Cache Miss", CAT_CACHE,
                    "Number of times data was not found in cache", 3,
                    Metric.CALC_DELTA_PER_SECOND);

    private final BaseMetric metricQueryCacheSize =
            new BaseMetric("QueryCacheSize", "Query Cache Size", CAT_CACHE,
                    "Number of queries in the cache", 0, Metric.CALC_AVERAGE);
    private final BaseMetric metricQueryCacheMaxSize =
            new BaseMetric("QueryCacheMaxSize", "Query Cache Max Size",
                    CAT_CACHE,
                    "Max number of queries to store in the cache", 0,
                    Metric.CALC_AVERAGE);
    private final BaseMetric metricQueryCacheHit =
            new BaseMetric("QueryCacheHit", "Query Cache Hit", CAT_CACHE,
                    "Number of times query results were found in cache", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricQueryCacheMiss =
            new BaseMetric("QueryCacheMiss", "Query Cache Miss", CAT_CACHE,
                    "Number of times query results were not found in cache", 3,
                    Metric.CALC_DELTA_PER_SECOND);

    private final PercentageMetric metricCacheFullPercent =
            new PercentageMetric("CacheFullPercent", "Cache Full %", CAT_CACHE,
                    "Number of objects in the cache as a percentage of the max",
                    metricCacheSize, metricCacheMaxSize);
    private final PercentageSumMetric metricCacheHitPercent =
            new PercentageSumMetric("CacheHitPercent", "Cache Hit %", CAT_CACHE,
                    "Cache hit rate percentage",
                    metricCacheHit, metricCacheMiss);
    private final PercentageMetric metricQueryCacheFullPercent =
            new PercentageMetric("QueryCacheFullPercent", "Query Cache Full %", CAT_CACHE,
                    "Number of queries in the cache as a percentage of the max",
                    metricQueryCacheSize, metricQueryCacheMaxSize);
    private final PercentageSumMetric metricQueryCacheHitPercent =
            new PercentageSumMetric("QueryCacheHitPercent", "Query Cache Hit %", CAT_CACHE,
                    "Query Cache hit rate percentage",
                    metricQueryCacheHit, metricQueryCacheMiss);

    public LRUStorageCache() {
        stateMap = new HashMap();
        queryMap = new HashMap();
    }

    public void setJDOMetaData(ModelMetaData jmd) {
        this.jmd = jmd;
        int n = jmd.classes.length;
        classStateHead = new StateEntry[n];
        classEvictionTimestamp = new long[n];
        classQueryHead = new QueryEntryNode[n];
    }

    public boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            evictAll(null);
        }
    }

    public boolean isQueryCacheEnabled() {
        return queryCacheEnabled;
    }

    public void setQueryCacheEnabled(boolean queryCacheEnabled) {
        this.queryCacheEnabled = queryCacheEnabled;
    }

    public synchronized Object beginTx() {
        if (!enabled) {
            return DISABLED_TX;
        }
        Tx tx = new Tx(++now);
        if (txHead == null) {
            txTail = txHead = tx;
        } else {
            txHead.next = tx;
            txHead = tx;
        }
        return tx;
    }

    public synchronized void endTx(Object o) {
        if (o == DISABLED_TX) {
            return;
        }
        Tx tx = (Tx)o;
        tx.finished = true;
        // remove all eviction markers for each finished tx that is the oldest
        // tx i.e. there is no older tx that has not finished yet
        for (; txTail.finished; ) {
            for (int i = txTail.evictedCount - 1; i >= 0; i--) {
                OID oid = txTail.evicted[i];
                StateEntry e = (StateEntry)stateMap.get(oid);
                if (e != null && e.timestamp == tx.started) {
                    stateMap.remove(oid);
                }
            }
            if (txTail.next == null) {
                txTail = txHead = null;
                break;
            }
            txTail = txTail.next;
        }
    }

    public synchronized State getState(OID oid, FetchGroup fetchGroup) {
        if (!enabled) {
            return null;
        }
        StateEntry e = (StateEntry)stateMap.get(oid);
        if (e != null && e.state != null) {
            removeFromStateList(e);
            addToHeadOfStateList(e);
            if (fetchGroup == null || e.state.containsFetchGroup(fetchGroup)) {
                ++hitCount;
                return e.state.getCopy();
            }
        }
        ++missCount;
        return null;
    }

    public synchronized boolean contains(OID oid) {
        if (!enabled) {
            return false;
        }
        StateEntry e = (StateEntry)stateMap.get(oid);
        return e != null && e.state != null;
    }

    public synchronized CachedQueryResult getQueryResult(CompiledQuery cq,
            Object[] params) {
        if (!enabled || !queryCacheEnabled) {
            return null;
        }
        QueryEntry e = (QueryEntry)queryMap.get(new QueryEntry(cq, params));
        if (e != null && e.res != null) {
            ++queryHitCount;
            removeFromQueryList(e);
            addToHeadOfQueryList(e);
            return e.res;
        } else {
            ++queryMissCount;
            return null;
        }
    }

    public synchronized int getQueryResultCount(CompiledQuery cq,
            Object[] params) {
        if (!enabled || !queryCacheEnabled) {
            return -1;
        }
        QueryEntry e = (QueryEntry)queryMap.get(new QueryEntry(cq, params));
        if (e != null) {
            ++queryHitCount;
            removeFromQueryList(e);
            addToHeadOfQueryList(e);
            //HACK return e.res != null ? e.res.results.size() : e.resultCount;
            return e.resultCount;
        } else {
            ++queryMissCount;
            return -1;
        }
    }

    public synchronized void evict(Object tx, CompiledQuery cq,
            Object[] params) {
        if (!enabled) {
            return;
        }
        QueryEntry e = (QueryEntry)queryMap.get(new QueryEntry(cq, params));
        if (e != null) {
            removeFromQueryList(e);
            removeFromClassQueryLists(e);
            queryMap.remove(e);
        }
    }

    public synchronized void add(Object otx, StatesReturned container) {
        if (!enabled) {
            return;
        }
        Tx tx = (Tx)otx;
        if (tx.started <= evictAllTimestamp) {
            // cannot accept any data from tx as evict all was done after
            // it started
            return;
        }
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry me = (EntrySet.Entry)i.next();
            OID oid = (OID)me.getKey();
            State state = (State)me.getValue();
            if (!state.isCacheble()
                    || tx.started <= classEvictionTimestamp[state.getClassIndex()]) {
                continue; // class was evicted after tx started so dont add
            }
            StateEntry e = (StateEntry)stateMap.get(oid);
            if (e != null) {
                if (tx.started <= e.timestamp || e.state == null) {
                    continue; // data in cache is newer or evicted so dont add
                }
                e.state.updateFrom(state);
                e.timestamp = now;
            } else {
                e = new StateEntry(now, oid, state);
                stateMap.put(oid, e);
                addToHeadOfStateList(e);
                addToClassStateList(e);
                discardExcessStates();
            }
        }
    }

    public synchronized void add(Object tx, CompiledQuery cq, Object[] params,
            CachedQueryResult queryData) {
        if (!enabled) {
            return;
        }
        addImp((Tx)tx, cq, params, queryData,
                queryData.results == null ? 0 : queryData.results.size());
    }

    private void addImp(Tx tx, CompiledQuery cq, Object[] params,
            CachedQueryResult queryData, int resultCount) {
        if (tx.started <= evictAllTimestamp) {
            // cannot accept any data from tx as evict all was done after
            // it started
            return;
        }
        QueryEntry e = new QueryEntry(cq, params);
        QueryEntry existing = (QueryEntry)queryMap.get(e);
        if (existing != null) {
            if (tx.started <= existing.timestamp) {
                return; // data in cache is newer so dont add
            }
            e = existing;
        } else {
            int[] indexes = e.cq.getClassIndexes();
            // dont add to cache if any of the classes involved have been
            // evicted since we started as the query data may be stale
            for (int i = indexes.length - 1; i >= 0; i--) {
                if (classEvictionTimestamp[indexes[i]] >= tx.started) {
                    return;
                }
            }
            queryMap.put(e, e);
            addToHeadOfQueryList(e);
            discardExcessQueries();
            addToClassQueryLists(e);
        }
        e.res = queryData;
        e.resultCount = resultCount;
        e.timestamp = tx.started;
    }

    public synchronized void add(Object tx, CompiledQuery cq, Object[] params,
            int count) {
        if (!queryCacheEnabled) {
            return;
        }
        addImp((Tx)tx, cq, params, null, count);
    }

    public synchronized void evict(Object otx, OID[] oids, int offset,
            int length, int expected) {
        if (!enabled) {
            return;
        }
        Tx tx = (Tx)otx;
        OID[] a;
        int evictedCount = tx.evictedCount;
        if (evictedCount > 0) {
            if (tx.evicted.length - evictedCount >= length) {
                a = tx.evicted;
            } else {
                a = new OID[evictedCount + length];
                System.arraycopy(tx.evicted, 0, a, 0, evictedCount);
                tx.evicted = a;
            }
        } else {
            tx.evicted = a = new OID[expected < length ? length : expected];
        }
        System.arraycopy(oids, offset, a, evictedCount, length);
        long started = tx.started;
        for (int i = 0; i < length; i++) {
            OID oid = a[i + evictedCount];
            StateEntry e = (StateEntry)stateMap.get(oid);
            if (e == null) { // create eviction marker
                stateMap.put(oid, new StateEntry(started, oid, null));
            } else if (e.state != null) {
                removeFromStateList(e);
                removeFromClassStateList(e);
                e.state = null;
                e.timestamp = started;
            } else if (e.timestamp < started) {
                e.timestamp = started;
                continue;
            }
            int ci = oid.getClassIndex();
            classEvictionTimestamp[ci] = now;
            removeQueriesForClass(ci);
        }
        tx.evictedCount += length;
    }

    public synchronized void evict(Object tx, ClassMetaData[] classes,
            int classCount) {
        if (!enabled) {
            return;
        }
        for (int i = 0; i < classCount; i++) {
            int ci = classes[i].index;
            classEvictionTimestamp[ci] = now;
            // evict all states for class
            for (StateEntry e = classStateHead[ci]; e != null; ) {
                removeFromStateList(e);
                stateMap.remove(e.oid);
                e.state = null;
                e.oid = null;
                StateEntry prev = e.classPrev;
                e.classNext = null;
                e.classPrev = null;
                e = prev;
            }
            classStateHead[ci] = null;
            // evict all queries for the class
            removeQueriesForClass(ci);
            classes[i].cacheStrategyAllDone = false;
        }
    }

    public synchronized void evictAll(Object tx) {
        evictAllTimestamp = now;
        stateMap.clear();
        objectCount = 0;
        queryMap.clear();
        queryCount = 0;
        for (int i = classQueryHead.length - 1; i >= 0; i--) {
            classQueryHead[i] = null;
        }
        for (int i = jmd.classes.length - 1; i >= 0; i--) {
            jmd.classes[i].cacheStrategyAllDone = false;
        }
        stateHead = stateTail = null;
    }

    public int getObjectCount() {
        return objectCount;
    }

    public int getMaxObjects() {
        return maxObjects;
    }

    public synchronized void setMaxObjects(int maxObjects) {
        this.maxObjects = maxObjects;
        discardExcessStates();
    }

    public int getMaxQueries() {
        return maxQueries;
    }

    public synchronized void setMaxQueries(int maxQueries) {
        this.maxQueries = maxQueries;
        discardExcessQueries();
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getMissCount() {
        return missCount;
    }

    public int getQueryHitCount() {
        return queryHitCount;
    }

    public int getQueryMissCount() {
        return queryMissCount;
    }

    private void discardExcessStates() {
        for (; objectCount > maxObjects && stateTail != null; --objectCount) {
            StateEntry e = stateTail;
            stateMap.remove(e.oid);
            stateTail = e.lruNext;
            e.lruNext = null;
            if (stateTail != null) {
                stateTail.lruPrev = null;
            } else {
                stateHead = null;
            }
            removeFromClassStateList(e);
        }
    }

    private void discardExcessQueries() {
        for (; queryCount > maxQueries && queryTail != null; --queryCount) {
            QueryEntry e = queryTail;
            queryMap.remove(e);
            queryTail = e.next;
            e.next = null;
            if (queryTail != null) {
                queryTail.prev = null;
            } else {
                queryHead = null;
            }
            removeFromClassQueryLists(e);
        }
    }

    /**
     * Remove e from the double linked LRU list and dec objectCount.
     */
    private void removeFromStateList(StateEntry e) {
        if (e.lruPrev != null) {
            e.lruPrev.lruNext = e.lruNext;
        } else {
            stateTail = e.lruNext;
        }
        if (e.lruNext != null) {
            e.lruNext.lruPrev = e.lruPrev;
        } else {
            stateHead = e.lruPrev;
        }
        e.lruNext = e.lruPrev = null;
        --objectCount;
    }

    /**
     * Add ps to the head of the double linked LRU list and inc objectCount.
     * This will make it the most recently accessed object.
     */
    private void addToHeadOfStateList(StateEntry e) {
        e.lruNext = null;
        e.lruPrev = stateHead;
        if (stateHead != null) {
            stateHead.lruNext = e;
        }
        stateHead = e;
        if (stateTail == null) {
            stateTail = e;
        }
        ++objectCount;
    }

    /**
     * Add e to the double linked class state list for its class.
     */
    private void addToClassStateList(StateEntry e) {
        int i = e.state.getClassIndex();
        e.classPrev = classStateHead[i];
        if (classStateHead[i] != null) {
            classStateHead[i].classNext = e;
        }
        e.classNext = null;
        classStateHead[i] = e;
    }

    /**
     * Remove e from the double linked class state list for its class.
     */
    private void removeFromClassStateList(StateEntry e) {
        if (e.classPrev != null) {
            e.classPrev.classNext = e.classNext;
        } else {
            classStateHead[e.state.getClassIndex()] = e.classNext;
        }
        if (e.classNext != null) {
            e.classNext.classPrev = e.classPrev;
            e.classNext = null;
        }
        e.classPrev = null;
    }

    /**
     * Remove cps from the double linked LRU list.
     */
    private void removeFromQueryList(QueryEntry e) {
        if (e.prev != null) {
            e.prev.next = e.next;
        } else {
            queryTail = e.next;
        }
        if (e.next != null) {
            e.next.prev = e.prev;
        } else {
            queryHead = e.prev;
        }
        e.next = e.prev = null;
        --queryCount;
    }

    /**
     * Add ps to the head of the double linked LRU list. This will make it
     * the most recently accessed object.
     */
    private void addToHeadOfQueryList(QueryEntry e) {
        e.next = null;
        e.prev = queryHead;
        if (queryHead != null) {
            queryHead.next = e;
        }
        queryHead = e;
        if (queryTail == null) {
            queryTail = e;
        }
        ++queryCount;
    }

    /**
     * Add e to the class query lists of all of the classes that it depends
     * on.
     */
    private void addToClassQueryLists(QueryEntry e) {
        int[] indexes = e.cq.getClassIndexes();
        QueryEntryNode sibling = null;
        for (int i = 0; i < indexes.length; i++) {
            int classIndex = indexes[i];
            QueryEntryNode head = classQueryHead[classIndex];
            QueryEntryNode n = new QueryEntryNode(e);
            n.prev = head;
            if (head != null) {
                head.next = n;
            }
            if (sibling != null) {
                sibling.nextSibling = n;
            } else {
                e.queryNodeTail = n;
            }
            classQueryHead[classIndex] = sibling = n;
        }
    }

    /**
     * Remove e from the class query lists of all of the classes it depends
     * on.
     */
    private void removeFromClassQueryLists(QueryEntry e) {
        int i = 0;
        int[] indexes = e.cq.getClassIndexes();
        for (QueryEntryNode n = e.queryNodeTail; n != null; i++) {
            QueryEntryNode nextSibling = n.nextSibling;
            n.nextSibling = null;
            if (n.prev != null) {
                n.prev.next = n.next;
            }
            if (n.next != null) {
                n.next.prev = n.prev;
            } else { // n is current head of list
                classQueryHead[indexes[i]] = n.prev;
            }
            n.next = n.prev = null;
            n = nextSibling;
        }
    }

    /**
     * Remove all queries for the class from the LRU query list, the query
     * lists for all classes each query depends on and the queryMap itself.
     */
    private void removeQueriesForClass(int classIndex) {
        for (QueryEntryNode n = classQueryHead[classIndex]; n != null; ) {
            removeFromQueryList(n.e);
            QueryEntryNode prev = n.prev;
            removeFromClassQueryLists(n.e);
            queryMap.remove(n.e);
            n = prev;
        }
    }

    public void dump(PrintStream out) {
        out.println("stateMap.size() = " + stateMap.size());
        out.println("objectCount = " + objectCount +
                ", maxObjects = " + maxObjects);
        int c = 0;
        StateEntry p = null;
        for (StateEntry e = stateTail; e != null; p = e, e = e.lruNext, ++c) {
            asst(e.lruPrev == p);
        }
        out.println("LRU StateList length = " + c + " stateTail = " +
                stateTail + " stateHead = " + stateHead);
        HashSet oids = new HashSet();
        for (Tx e = txTail; e != null; e = e.next) {
            if (e.evicted != null) {
                oids.addAll(Arrays.asList(e.evicted));
            }
        }
        asst(stateHead == p);
        asst(objectCount <= maxObjects);
        asst(objectCount + oids.size() == stateMap.size());
        out.println("--- Tx list ---");
        c = 0;
        int totEvictedCount = 0;
        for (Tx e = txTail; e != null; e = e.next, ++c) {
            out.println(e + " started " + e.started + " finished " + e.finished +
                    " evictedCount " + e.evictedCount);
            totEvictedCount += e.evictedCount;
        }
        out.println("--- count " + c + " totEvictedCount " + totEvictedCount);
    }

    private void asst(boolean bool) {
        if (!bool) {
            throw BindingSupportImpl.getInstance().internal("assertion failed");
        }
    }

    public void addMetrics(List list) {
        list.add(metricCacheSize);
        list.add(metricCacheMaxSize);
        list.add(metricCacheHit);
        list.add(metricCacheMiss);
        list.add(metricQueryCacheSize);
        list.add(metricQueryCacheMaxSize);
        list.add(metricQueryCacheHit);
        list.add(metricQueryCacheMiss);
        list.add(metricCacheFullPercent);
        list.add(metricCacheHitPercent);
        list.add(metricQueryCacheFullPercent);
        list.add(metricQueryCacheHitPercent);
    }

    public void sampleMetrics(int[][] buf, int pos) {
        buf[metricCacheSize.getIndex()][pos] = objectCount;
        buf[metricCacheMaxSize.getIndex()][pos] = maxObjects;
        buf[metricCacheHit.getIndex()][pos] = hitCount;
        buf[metricCacheMiss.getIndex()][pos] = missCount;
        buf[metricQueryCacheSize.getIndex()][pos] = queryCount;
        buf[metricQueryCacheMaxSize.getIndex()][pos] = maxQueries;
        buf[metricQueryCacheHit.getIndex()][pos] = queryHitCount;
        buf[metricQueryCacheMiss.getIndex()][pos] = queryMissCount;
    }

    /**
     * Info about a cache transaction.
     */
    private static final class Tx {
        final long started;
        Tx next;
        boolean finished;
        OID[] evicted;
        int evictedCount;

        public Tx(long timestamp) {
            this.started = timestamp;
        }
    }

    /**
     * Stuff we associate with each State in stateMap. The state field is
     * null if this is an eviction marker.
     */
    private static final class StateEntry {
        long timestamp; // data read in a tx with started <= timestamp will
                        // not go in cache
        OID oid;
        State state;
        StateEntry lruPrev, lruNext;
        StateEntry classPrev, classNext;

        public StateEntry(long txId, OID oid, State state) {
            this.timestamp = txId;
            this.oid = oid;
            this.state = state;
        }
    }

    /**
     * This is the key and value for our query map. These form a double linked
     * LRU list of query results.
     */
    private static final class QueryEntry {
        final CompiledQuery cq;
        final Object[] params;
        final int hashCode;
        long timestamp; // data read in a tx with started <= timestamp will
                        // not go in cache
        CachedQueryResult res;
        int resultCount;
        QueryEntry prev, next;
        QueryEntryNode queryNodeTail;
            // single linked list of our nodes on QueryEntryNode.nextSibling

        public QueryEntry(CompiledQuery cq, Object[] params) {
            this.cq = cq;
            this.params = params;
            int hc = cq.hashCode();
            if (params != null) {
                hc = cq.hashCode();
                for (int i = params.length - 1; i >= 0; i--) {
                    Object o = params[i];
                    if (o != null) {
                        hc = hc * 29 + o.hashCode();
                    }
                }
            }
            hashCode = hc;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object o) {
            QueryEntry k = (QueryEntry)o;
            if (hashCode != k.hashCode) {
                return false;
            }
            if (!cq.equals(k.cq)) {
                return false;
            }
            if (params != null) {
                for (int i = params.length - 1; i >= 0; i--) {
                    Object a = params[i];
                    Object b = k.params[i];
                    if (a == null) {
                        if (b != null) return false;
                    } else if (b == null || !a.equals(b)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Node in a double linked list of QueryEntry's. All the nodes for a
     * single QueryEntry are also linked together starting from the
     * QueryEntry.queryNodeTail. This makes it easy to remove all of the
     * nodes for a QueryEntry.
     */
    private static final class QueryEntryNode {
        QueryEntry e;
        QueryEntryNode prev, next, nextSibling;

        public QueryEntryNode(QueryEntry e) {
            this.e = e;
        }

    }

}
