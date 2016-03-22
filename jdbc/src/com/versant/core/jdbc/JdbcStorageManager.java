
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

import com.versant.core.common.State;
import com.versant.core.common.*;
import com.versant.core.util.CharBuf;
import com.versant.core.util.IntArray;
import com.versant.core.metadata.*;
import com.versant.core.server.*;
import com.versant.core.logging.LogEventStore;
import com.versant.core.jdo.ServerLogEvent;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.jdo.VersantQueryPlan;
import com.versant.core.storagemanager.*;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.jdbc.sql.exp.SelectExp;
import com.versant.core.jdbc.conn.PooledPreparedStatement;
import com.versant.core.jdbc.query.JdbcCompiledQuery;
import com.versant.core.jdbc.query.JdbcJDOQLCompiler;
import com.versant.core.jdbc.fetch.*;

import com.versant.core.jdbc.ejbql.JdbcEJBQLCompiler;
import com.versant.core.jdbc.ejbql.JdbcQueryResultEJBQL;


import java.sql.*;
import java.util.*;

/**
 * StorageManager implementation for JDBC.
 */
public final class JdbcStorageManager implements StorageManager {

    private final ModelMetaData jmd;
    private final StorageCache cache;
    private final JdbcConnectionSource conSrc;
    private final SqlDriver sqlDriver;
    private final CompiledQueryCache compiledQueryCache;
    private final LogEventStore pes;

    private int lockPolicy;
    private int conPolicy;

    private Connection conx;
    private Object cacheTx;
    private VersantClientJDBCConnection clientCon;

    private boolean txActive;
    private boolean optimistic;
    private boolean pinned;     // do not release conx until end of tx
    private boolean flushed;    // updates done without commit on conx so do
                                // not use the cache even if optimistic
    private boolean forUpdateField;

    private JdbcQueryResult queryResultHead;
    private JdbcQueryResult queryResultTail;

    // this is a reference to persistGraphFullSort or persistGraphPartialSort
    private PersistGraph activePersistGraph;
    private PersistGraphFullSort persistGraphFullSort;
    private PersistGraph persistGraphPartialSort;

    private final boolean useBatchInsert;
    private final boolean useBatchUpdate;

    private boolean[] changedClassesFlag;
    private ClassMetaData[] changedClasses;
    private int changedClassCount;

    public static final String STATUS_OPEN_QUERY_RESULT_COUNT =
            "openQueryResultCount";

    public JdbcStorageManager(ModelMetaData jmd, JdbcConnectionSource conSrc,
            SqlDriver sqlDriver, StorageCache cache,
            CompiledQueryCache compiledQueryCache, LogEventStore pes,
            JdbcConfig c) {
        this.jmd = jmd;
        this.conSrc = conSrc;
        this.sqlDriver = sqlDriver;
        this.cache = cache;
        this.compiledQueryCache = compiledQueryCache;
        this.pes = pes;
        useBatchInsert = !c.jdbcDisableStatementBatching
            && sqlDriver.isInsertBatchingSupported();
        useBatchUpdate = !c.jdbcDisableStatementBatching
            && sqlDriver.isUpdateBatchingSupported();
        conPolicy = CON_POLICY_RELEASE;
        lockPolicy = LOCK_POLICY_NONE;
    }

    public boolean isForUpdate() {
        return forUpdateField;
    }

    public boolean isOptimistic() {
        return optimistic;
    }

    public boolean isActive() {
        return txActive;
    }

    public void begin(boolean optimistic) {
        if (txActive) {
            throw BindingSupportImpl.getInstance().internal("tx already active");
        }
        this.optimistic = optimistic;
        txActive = true;
        setFlagsForLockPolicy();
    }

    public void commit() {
        checkActiveTx();
        commitAndReleaseCon(flushed || !optimistic
                || conPolicy != CON_POLICY_RELEASE);
        if (cacheTx != null) {
            cache.endTx(cacheTx);
            cacheTx = null;
        }
        flushed = pinned = false;
        txActive = false;
    }

    public void rollback() {
        checkActiveTx();
        rollbackImp(false);
    }

    private void rollbackImp(boolean reset) {
        try {
            if (conx != null) {
                closeAllQueries();
                try {
                    conx.rollback();
                    if (conPolicy != CON_POLICY_PIN || reset) {
                        if (clientCon != null) {
                            clientCon.close();
                        }
                        conSrc.returnConnection(conx);
                        conx = null;
                    }
                } catch (SQLException e) {
                    throw handleException(e);
                }
            }
        } finally {
            if (cacheTx != null) {
                cache.endTx(cacheTx);
                cacheTx = null;
            }
            flushed = pinned = false;
            txActive = false;
        }
    }

    public void setConnectionPolicy(int policy) {
        conPolicy = policy;
    }

    public int getConnectionPolicy() {
        return conPolicy;
    }

    public void setLockingPolicy(int policy) {
        lockPolicy = policy;
        setFlagsForLockPolicy();
    }

    public int getLockingPolicy() {
        return lockPolicy;
    }

    public void logEvent(int level, String description, int ms) {
        // todo this must move to the event logging and error handling proxy
        switch (level) {
            case EVENT_ERRORS:
                if (!pes.isSevere()) return;
                break;
            case EVENT_NORMAL:
                if (!pes.isFine()) return;
                break;
            case EVENT_VERBOSE:
                if (!pes.isFiner()) return;
                break;
            case EVENT_ALL:
                if (!pes.isFinest()) return;
                break;
        }
        ServerLogEvent ev = new ServerLogEvent(ServerLogEvent.USER,
                description);
        ev.setTotalMs(ms);
        pes.log(ev);
    }

    public StorageManager getInnerStorageManager() {
        return null;
    }

    /**
     * Can data be retrieved from or stored in the cache given our current
     * state? This is true if no flush has been done and the current tx is
     * optimistic or there is no active tx.
     */
    private boolean canUseCache() {
        return optimistic && !flushed || !txActive;
    }

    public StatesReturned fetch(ApplicationContext context, OID oid, State current,
            FetchGroup fetchGroup,
            FieldMetaData triggerField) {
        try {
            StatesReturned container = new StatesReturned(context);
            if (canUseCache()) {
                State s = cache.getState(oid, fetchGroup);
                if (s == null) {
                    ClassMetaData base = oid.getBaseClassMetaData();
                    if (base.cacheStrategy == MDStatics.CACHE_STRATEGY_ALL
                            && !base.cacheStrategyAllDone) {
                        base.cacheStrategyAllDone = true;
                        StatesReturned all = new StatesReturned(
                                DummyApplicationContext.INSTANCE);
                        getAllStates(context, base, fetchGroup, container.next = all);
                        s = all.get(oid);
                    }
                }
                // todo fetch related objects from cache if s came from cache
                if (s == null) {
                    getState(oid, fetchGroup, container);
                } else {
                    container.add(oid, s);
                }
            } else {
                getState(oid, fetchGroup, container);
            }
            finishRead(container);
            return container;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public StatesReturned fetch(ApplicationContext context, OIDArray oids,
            FieldMetaData triggerField) {
        try {
            StatesReturned container = new StatesReturned(context);
            int n = oids.size();
            if (canUseCache()) {
                StatesReturned all = null;
                for (int i = 0; i < n; i++) {
                    OID oid = oids.oids[i];
                    ClassMetaData cmd = oid.getAvailableClassMetaData();
                    FetchGroup fg = cmd.fetchGroups[0];
                    State s = cache.getState(oid, fg);
                    if (s == null) {
                        ClassMetaData base = oid.getBaseClassMetaData();
                        if (base.cacheStrategy == MDStatics.CACHE_STRATEGY_ALL
                                && !base.cacheStrategyAllDone) {
                            base.cacheStrategyAllDone = true;
                            if (all == null) {
                                container.next = all = new StatesReturned(
                                    DummyApplicationContext.INSTANCE);
                            }
                            getAllStates(context, base, base.fetchGroups[0], all);
                            s = all.get(oid);
                        }
                    }
                    // todo fetch related objects from cache if s came from cache
                    if (s == null) {
                        getState(oid, fg, container);
                    } else {
                        container.add(oid, s);
                    }
                }
            } else {
                for (int i = 0; i < n; i++) {
                    OID oid = oids.oids[i];
                    ClassMetaData cmd = oid.getAvailableClassMetaData();
                    getState(oid, cmd.fetchGroups[0], container);
                }
            }
            finishRead(container);
            return container;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public StatesReturned store(StatesToStore toStore, DeletePacket toDelete,
            boolean returnFieldsUpdatedBySM, int storeOption,
            boolean evictClasses) {
        checkActiveTx();
        if (storeOption == STORE_OPTION_FLUSH) {
            // make sure open queries will not be cached if this is a flush
            for (JdbcQueryResult qrw = queryResultHead; qrw != null; qrw = qrw.prev) {
                qrw.setNonCacheble();
            }
            flushed = pinned = true;
        }

        // persist changes to the database
        StatesReturned container = new StatesReturned(
                DummyApplicationContext.INSTANCE);
        boolean updates = toStore != null && !toStore.isEmpty();
        boolean deletes = toDelete != null && !toDelete.isEmpty();
        try {
            if (updates) {
                doUpdates(toStore, container, returnFieldsUpdatedBySM);
            }
            if (deletes) {
                doDeletes(toDelete);
            }
            clearNonAutoSetFields(container);
        } catch (Exception e) {
            throw handleException(e);
        } finally {
            if (activePersistGraph != null) {
                activePersistGraph.clear();
                activePersistGraph = null;
            }
        }

        boolean commit = storeOption == STORE_OPTION_COMMIT;
        switch (storeOption) {
            case STORE_OPTION_COMMIT:
                commit = true;
                commitAndReleaseCon(!optimistic || flushed || updates || deletes
                        || conPolicy != CON_POLICY_RELEASE);
                flushed = pinned = false;
                txActive = false;
                break;
            case STORE_OPTION_PREPARE:
                // make sure conx.commit() is done when commit() is called later
                flushed = flushed || updates || deletes;
                break;
        }

        // evict from cache
        cacheTx();
        if (toStore.epcAll) {
            cache.evictAll(cacheTx);
        } else if (evictClasses || changedClassCount > 0) {
            addChangedClasses(toStore, toDelete);
            cache.evict(cacheTx, changedClasses, changedClassCount);
            clearChangedClasses();
        } else {
            int expected = toStore.size() + toDelete.size() +
                    (toStore.epcOids == null ? 0 : toStore.epcOids.length);
            cache.evict(cacheTx, toStore.oids, 0, toStore.size(), expected);
            cache.evict(cacheTx, toDelete.oids, 0, toDelete.size(), expected);
            if (toStore.epcClasses != null) {
                int n = toStore.epcClasses.length;
                ClassMetaData[] a = new ClassMetaData[n];
                for (int i = 0; i < n; i++) {
                    a[i] = jmd.classes[toStore.epcClasses[i]];
                }
                cache.evict(cacheTx, a, n);
            }
            if (toStore.epcOids != null) {
                cache.evict(cacheTx, toStore.epcOids, 0, toStore.epcOids.length,
                        expected);
            }
        }

        // get rid of cache transaction if we have committed
        if (commit) {
            cache.endTx(cacheTx);
            cacheTx = null;
        }

        return container;
    }

    public OID createOID(ClassMetaData cmd) {
        checkActiveTx();
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        JdbcKeyGenerator keygen = jdbcClass.jdbcKeyGenerator;
        if (keygen == null) {
            throw BindingSupportImpl.getInstance().invalidOperation("Class " + cmd.qname +
                    " has no jdbc-key-generator");
        }
        if (keygen.isPostInsertGenerator()) {
            throw BindingSupportImpl.getInstance().invalidOperation("Class " + cmd.qname +
                    " is using a post insert jdbc-key-generator");
        }
        OID oid = cmd.createOID(true);
        Object[] oidData = new Object[((JdbcMetaData)jmd.jdbcMetaData).maxPkSimpleColumns];
        try {
            Connection kgcon = null;
            boolean rollback = true;
            try {
                boolean needKgcon = keygen.isRequiresOwnConnection();
                if (needKgcon) {
                    kgcon = conSrc.getConnection(true, false);
                }
                keygen.generatePrimaryKeyPre(cmd.qname,
                        jdbcClass.table, 1, oidData, needKgcon ? kgcon : con());
                oid.copyKeyFields(oidData);
                rollback = false;
            } finally {
                if (kgcon != null) {
                    if (rollback) {
                        kgcon.rollback();
                    } else {
                        kgcon.commit();
                    }
                    conSrc.returnConnection(kgcon);
                }
            }
            // make sure normal con is not released until end of tx
            if (kgcon == null && conx != null) {
                pinned = true;
            }
            return oid;
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    public CompiledQuery compileQuery(QueryDetails query) {
        JdbcCompiledQuery cq = (JdbcCompiledQuery)compiledQueryCache.get(query);
        if (cq == null) {
            cq = compile(query);
            // apply caching override, if any
            switch (query.getCacheable()) {
                case QueryDetails.FALSE:
                    cq.setCacheable(false);
                    break;
                case QueryDetails.TRUE:
                    cq.setCacheable(true);
                    break;
            }
            cq = (JdbcCompiledQuery)compiledQueryCache.add(cq);
        }

        return cq;
    }

    public ExecuteQueryReturn executeQuery(ApplicationContext context, QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        JdbcCompiledQuery cq;
        if (compiledQuery == null) {
            cq = (JdbcCompiledQuery)compileQuery(query);
        } else {
            cq = (JdbcCompiledQuery)compiledQuery;
        }
        JdbcQueryResult res = null;
        if (cq.isEJBQLHack()) {

            res = new JdbcQueryResultEJBQL(this, cq, params,
                    canUseCache());

        } else {
            res = new JdbcQueryResult(this, cq, params,
                    canUseCache());
        }
        addQueryResult(res);
        return res;
    }

    public QueryResultContainer executeQueryAll(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params) {
        JdbcCompiledQuery cq;
        if (compiledQuery == null) {
            cq = (JdbcCompiledQuery)compileQuery(query);
        } else {
            cq = (JdbcCompiledQuery)compiledQuery;
        }
        try {
            QueryResultContainer container = new QueryResultContainer(context, cq);
            if (cq.isCacheble() && cache.isQueryCacheEnabled()
                    && (!txActive || optimistic && !flushed)) {
                CachedQueryResult res = cache.getQueryResult(cq, params);
                if (res == null || !addToContainer(cq, params, res, container)) {
                    fillContainerWithAll(context, cq, params, container);
                    //try and add the results to the cache
                    res = new CachedQueryResult();
//                    container.container.addIndirectOIDs(res);
                    container.addResultsTo(res, cq.isCopyResultsForCache());
                    finishRead(container.container, cq, params, res, -1);
                }
            } else {
                fillContainerWithAll(context, cq, params, container);
                finishRead(container.container, cq, params, null, -1);
            }
            return container;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public int executeQueryCount(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        JdbcCompiledQuery cq;
        if (compiledQuery == null) {
            cq = (JdbcCompiledQuery)compileQuery(query);
        } else {
            cq = (JdbcCompiledQuery)compiledQuery;
        }
        try {
            int ans;
            if (cq.isCacheble() && cache.isQueryCacheEnabled()
                        && (!txActive || optimistic && !flushed)) {
                ans = cache.getQueryResultCount(cq, params);
                if (ans < 0) {
                    ans = executeCount(cq, params);
                    finishRead(null, cq, params, null, ans);
                }
            } else {
                ans = executeCount(cq, params);
            }
            return ans;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public VersantQueryPlan getQueryPlan(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        try {
            if (compiledQuery == null) {
                compiledQuery = compileQuery(query);
            }
            VersantQueryPlan qp = executePlan((JdbcCompiledQuery)compiledQuery,
                    params);
            finishRead();
            return qp;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public QueryResultContainer fetchNextQueryResult(ApplicationContext context,
            RunningQuery runningQuery, int skipAmount) {
        JdbcQueryResult res = (JdbcQueryResult)runningQuery;
        if (res == null || res.isFinished()) {
            return null;
        }
        try {
            QueryResultContainer container = new QueryResultContainer(context, 
                    res.getCompiledQuery());
            boolean cacheable = canUseCache() && res.isCachedResultsOk();
            if (cacheable && res.isNotStarted() 
                    && checkCacheForQuery(res.getJdbcCompiledQuery(),
                            res.getParams(), container)) {
                // we got data from cache
                container.qFinished = true;
                res.close();
                removeQueryResult(res);
            } else { // get from database
                CachedQueryResult queryData = null;
                Object[] params = null;
                res.updateCacheble();

                if (res.nextBatch(context, skipAmount, container)) {
                    // query has finished
                    if (cacheable && res.isCacheble()) {
                        queryData = res.qRCache;
                        params = res.getParams();
                    }
                    res.close();
                    removeQueryResult(res);
                }
                finishRead(container.container, res.getJdbcCompiledQuery(),
                        params, queryData, -1);
            }
            return container;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public QueryResultContainer fetchRandomAccessQueryResult(
            ApplicationContext context, RunningQuery runningQuery, int index,
            int fetchAmount) {
        try {
            JdbcQueryResult res = (JdbcQueryResult)runningQuery;
            QueryResultContainer qContainer = new QueryResultContainer(context, res.getCompiledQuery());

            res.getAbsolute(context, qContainer, index, fetchAmount);
            finishRead(qContainer.container, res.getJdbcCompiledQuery(),
                    res.getParams(), null, 0);
            return qContainer;
        } catch (Throwable t) {
            finishFailedRead();
            throw handleException(t);
        }
    }

    public int getRandomAccessQueryCount(ApplicationContext context,
            RunningQuery runningQuery) {
        return ((JdbcQueryResult)runningQuery).getResultCount();
    }

    public void closeQuery(RunningQuery runningQuery) {
        JdbcQueryResult res = (JdbcQueryResult)runningQuery;
        if (!res.isClosed()) {
            res.close();
            removeQueryResult(res);
            finishRead();
        }
    }

    public Object getDatastoreConnection() {
        if (clientCon == null) {
            clientCon = new VersantClientJDBCConnection(this, con());
            pinned = true;
        }
        return clientCon;
    }

    /**
     * This is called when a JDBC Connection previously given to a client
     * is closed.
     */
    public void clientConClosed() {
        clientCon = null;
    }

    public boolean isNotifyDirty() {
        return false;
    }

    public void notifyDirty(OID oid) {
        throw BindingSupportImpl.getInstance().internal("should not be called");
    }

    public void reset() {
        resetImp();
        forUpdateField = false;
        lockPolicy = LOCK_POLICY_NONE;
        conPolicy = CON_POLICY_RELEASE;
        clearChangedClasses();
    }

    public void destroy() {
        resetImp();
    }

    private void resetImp() {
        try {
            rollbackImp(true);
        } catch (Exception e) {
            // ignore
        }
        finishFailedRead();
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    public RuntimeException handleException(Throwable e) {
		return handleException(e.toString(), e, false, null);
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    public RuntimeException handleException(String msg, Throwable e) {
		return handleException(msg, e, false, null);
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    public RuntimeException handleException(String msg, Throwable e, 
											boolean convertLockTimeout, Object failed) {
		if (convertLockTimeout && isOptimistic() && 
			sqlDriver.isHandleLockTimeout() &&
			sqlDriver.isLockTimeout(e)) {
			
			throw BindingSupportImpl.getInstance().concurrentUpdate
				("Row is locked: " + msg, failed);
		}							
		return sqlDriver.mapException(e, msg, true);
    }

    /**
     * Get the names of all tables in the database converted to lower case.
     * The lower case name is mapped to the real case name.
     */
    public HashMap getDatabaseTableNames(Connection con) throws SQLException {
        ArrayList a = sqlDriver.getTableNames(con);
        int n = a.size();
        HashMap ans = new HashMap(n * 2);
        for (int i = 0; i < a.size(); i++) {
            String t = (String)a.get(i);
            ans.put(t.toLowerCase(), t);
        }
        return ans;
    }

    /**
     * Clear our list of changed classes.
     */
    private void clearChangedClasses() {
        changedClasses = null;
        changedClassesFlag = null;
        changedClassCount = 0;
    }

    /**
     * Add a class to the list of changed classes that we keep track of.
     */
    private void addChangedClass(ClassMetaData cmd) {
        if (changedClasses == null) {
            changedClasses = new ClassMetaData[jmd.classes.length];
            changedClassesFlag = new boolean[jmd.classes.length];
        }
        if (changedClassesFlag[cmd.index]) {
            return;
        }
        changedClasses[changedClassCount++] = cmd;
        changedClassesFlag[cmd.index] = true;
    }

    /**
     * Add all the classes referenced in toStore and toDelete to our list of
     * changed classes.
     */
    private void addChangedClasses(StatesToStore toStore,
            DeletePacket toDelete) {
        // add classes for all states.
        State[] states = toStore.states;
        int n = toStore.size();
        for (int i = 0; i < n; i++) {
            addChangedClass(states[i].getClassMetaData(jmd));
        }
        // add classes for all epc classes.
        if (toStore.epcClasses != null) {
            int[] a = toStore.epcClasses;
            for (int i = toStore.epcClassCount - 1; i >= 0; i--) {
                addChangedClass(jmd.classes[a[i]]);
            }
        }
        // Make sure the classes for all deleted OIDs are in
        OID[] oids = toDelete.oids;
        n = toDelete.size();
        for (int i = 0; i < n; i++) {
            OID oid = oids[i];
            addChangedClass(oid.getClassMetaData());
        }
        // Make sure the classes for all epc OIDs are in
        oids = toStore.epcOids;
        if (oids != null) {
            n = oids.length;
            for (int i = 0; i < n; i++) {
                addChangedClass(oids[i].getClassMetaData());
            }
        }
    }

    private void checkActiveTx() {
        if (!txActive) {
            throw BindingSupportImpl.getInstance().internal(
                    "no active transaction");
        }
    }

    /**
     * This method must be called at the end of all top level read operations
     * to maybe commit and maybe release the database connection (depending on
     * connection policy). It will add the data in container to the cache
     * and also add the query data. Either container or queryData or both
     * may be null.
     */
    private void finishRead(StatesReturned container, JdbcCompiledQuery cq,
            Object[] params, CachedQueryResult queryData, int queryResultCount) {
        if (conx == null) {
            // no connection so all data must have come
            // from the level 2 cache so we have nothing to do
            return;
        }
        boolean commit;
        boolean release;
        if (optimistic || !txActive) {
            if (pinned) {
                commit = release = false;
            } else {
                // if there are open queries then we cannot commit or release
                commit = release = queryResultHead == null
                        && conPolicy == CON_POLICY_RELEASE;
            }
        } else {
            commit = release = false;
        }
        boolean ok = false;
        try {
            if (commit) {
                conx.commit();
            }
            if (canUseCache()) {
                for (StatesReturned c = container; c != null; c = c.next) {
                    cache.add(cacheTx(), c);
                }
                if (cq != null && cq.isCacheble()) {
                    if (queryData != null) {
                        cache.add(cacheTx(), cq, params, queryData);
                    } else if (queryResultCount >= 0) {
                        cache.add(cacheTx(), cq, params, queryResultCount);
                    }
                }
            }
            if (commit && cacheTx != null) {
                cache.endTx(cacheTx);
                cacheTx = null;
            }
            if (release) {
                conSrc.returnConnection(conx);
                conx = null;
            }
            ok = true;
        } catch (SQLException e) {
            throw BindingSupportImpl.getInstance().datastore(e.toString(), e);
        } finally {
            if (!ok) {
                if (release && conx != null) {
                    try {
                        conx.rollback();
                    } catch (Exception e) {
                        // ignore
                    }
                    try {
                        conSrc.returnConnection(conx);
                    } catch (SQLException e) {
                        // ignore
                    }
                    conx = null;
                }
            }
        }
    }

    private void finishRead() {
        finishRead(null, null, null, null, -1);
    }

    private void finishRead(StatesReturned container) {
        finishRead(container, null, null, null, -1);
    }
        
    /**
     * This must be called for all top level read operations that fail.
     * It ensures that if the connection should be released it is released.
     * Any exceptions are discarded as they would likely hide the original
     * exception that caused the read to fail.
     */
    private void finishFailedRead() {
        try {
            finishRead(null, null, null, null, -1);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * If we have a connection optionally commit it and then release it if
     * the conPolicy allows. If the connection is released then any client
     * connection is closed. All open queries are closed.
     */
    private void commitAndReleaseCon(boolean commit) {
        if (conx == null) return;
        closeAllQueries();
//        if (!LoggingResultSet.openResults.isEmpty()) {
//            Set entrySet = LoggingResultSet.openResults.entrySet();
//            System.out.println("Dumping where open results was created");
//            for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
//                Map.Entry entry = (Map.Entry) iterator.next();
//                Object[] oa = (Object[]) entry.getValue();
//                System.out.println("entry.getKey() = " + entry.getKey());
//                System.out.println("sql = " + oa[0] + "\n");
//                ((Exception)oa[1]).printStackTrace(System.out);
//                System.out.println("\n\n\n ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//            }
//
//            throw BindingSupportImpl.getInstance().internal("There are resultset left open");
//        }
        try {
            if (commit) {
                conx.commit();
            }
            if (conPolicy != CON_POLICY_PIN) {
                if (clientCon != null) {
                    clientCon.close();
                }
                conSrc.returnConnection(conx);
                conx = null;
            }
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    /**
     * Set flags for lockPolicy as required. This must be called
     * when the policy changes or when a new tx starts.
     */
    private void setFlagsForLockPolicy() {
        forUpdateField = lockPolicy != LOCK_POLICY_NONE && !optimistic;

    }

    /**
     * Get our database connection. This will allocate one if we currently
     * have none. It will also start a cache transaction if there is none.
     */
    public Connection con() {
        if (conx == null) {
            cacheTx();
            try {
                conx = conSrc.getConnection(false, false);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }
        return conx;
    }

    /**
     * Get our cache transaction. This will begin one if there is none.
     */
    public Object cacheTx() {
        if (cacheTx == null) {
            cacheTx = cache.beginTx();
        }
        return cacheTx;
    }

    /**
     * Get the meta data.
     */
    public ModelMetaData getJmd() {
        return jmd;
    }

    /**
     * Return a state for the supplied oid containing at least the fetch
     * group specified. Additional states may be supplied to the container.
     * The must oid must be resolved by this call. The state returned is
     * added to the container.
     */
    public State getState(OID oid, FetchGroup fetchGroup,
            StateContainer container) {
        ClassMetaData cmd = oid.getBaseClassMetaData();
        boolean forUpdate = forUpdateField;
        if (forUpdate) {
            JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
            if (sqlDriver.getSelectForUpdate() == null) {
                // lock the instance with an update
                lock((JdbcOID)oid, jdbcClass);
                forUpdate = false;
            }
        }
        State s = getStateImp((JdbcOID)oid.getRealOID(),
                fetchGroup, forUpdate, container);
        if (forUpdateField && lockPolicy == LOCK_POLICY_FIRST) {
            forUpdateField = false;
        }
        container.add(oid, s);
        return s;
    }

//    /**
//     * Get a state and any prefeched states from queryResult.
//     */
//    public void getState(ApplicationContext context, OID oid,
//            FetchGroup fetchGroup, JdbcQueryResult queryResult,
//            StateContainer container) {
//        if (!context.isStateRequired(oid, fetchGroup)) {
//            // todo old code added state to container here
//            return;
//        }
//        State s;
//        if (canUseCache()) {
//            s = cache.getState(oid, fetchGroup);
//        } else {
//            s = null;
//        }
//        if (s == null) {
//            s = queryResult.getResultState(forUpdateField, container);
//        }
//        container.add(oid, s);
//    }

    /**
     * Lock the oid using an update statement.
     */
    private void lock(JdbcOID oid, JdbcClass jdbcClass) {
        PreparedStatement ps = null;
        try {
            ps = con().prepareStatement(jdbcClass.getLockRowSql());
            oid.setParams(ps, 1);
            if (ps.executeUpdate() == 0) {
                throw BindingSupportImpl.getInstance().objectNotFound(
                        oid.toSString());
            }
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            cleanup(ps);
        }
    }

    private void cleanup(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException x) {
                // ignore
            }
        }
    }

    private void cleanup(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException x) {
                // ignore
            }
        }
    }

    /**
     * Get the state for the supplied OID.
     */
    private State getStateImp(JdbcOID oid, FetchGroup fetchGroup,
            boolean forUpdate, StateContainer container) {

        fetchGroup = fetchGroup.resolve(oid, jmd);
        State state = null;
        boolean includeSubclasses = !oid.isResolved();

        //create a fetchspec for the operation.
        FetchSpec fSpec = getGetStateFetchSpec(fetchGroup, includeSubclasses, oid);

        //create the fetchResult from the fetchSpec
        FetchResult fetchResult = fSpec.createFetchResult(this, con(),
                new Object[] {oid}, forUpdate, false, 0, 0, -1, false,
                0);

        try {
            if (fetchResult.hasNext()) {
                Object[] oa = (Object[]) fetchResult.next(container);
                state = (State) oa[0];
                container.add(oid, state);
                fSpec.fetchPass2(fetchResult, new Object[] {oid},
                    container);
            } else {
                Utils.checkToThrowRowNotFound(oid, jmd);
                state = NULLState.NULL_STATE;
                container.add(oid, state);
            }
        } finally {
            fetchResult.close();
        }
        return state;
    }

    /**
     * Create the fetchspec to fetch the state.
     * TODO: This resulting fetchSpec could/should be cached in future.
     */
    private FetchSpec getGetStateFetchSpec(FetchGroup group, boolean includeSubclasses,
            final OID oid) {

        final JdbcClass jdbcClass = (JdbcClass)group.classMetaData.storeClass;
        // generate a join query to get the group
        SelectExp root = new SelectExp();
        root.table = jdbcClass.table;

        FetchSpec fetchSpec = new FetchSpec(root, jdbcClass.sqlDriver, true);
        fetchSpec.setFilterExpFactory(new FilterExpFactory() {
            public SelectExp createFilterExp() {
                SelectExp root = new SelectExp();
                root.table = jdbcClass.table;
                root.whereExp = root.table.createPkEqualsParamExp(root);
                return root;
            }
        });
        fetchSpec.setRootCmdType(group.classMetaData);

        boolean parFetch = true;


        fetchSpec.getOptions().setUseParallelQueries(parFetch);
        fetchSpec.getOptions().setUseOneToManyJoin(parFetch);

        FetchOpData fopData = new FetchOpDataProxy(FetchOpDataMainRS.INSTANCE) {
            public OID getOID(FetchResult fetchResult) {
                return oid;
            }

            public String getDescription() {
                return "Root OID " + oid.toStringImp();
            }
        };

        FopGetState fopGetState = new FopGetState(fetchSpec,
                fopData, group, includeSubclasses, root, 0,
                jdbcClass.cmd, new FetchFieldPath());

        fetchSpec.addFetchOp(fopGetState, true);

        fetchSpec.finish(1);
        if (Debug.DEBUG) {
            fetchSpec.printPlan(System.out, "    ");
        }
        root.appendToWhereExp(jdbcClass.table.createPkEqualsParamExp(root));

//        if (root.selectList == null) {
//            fgDs.setSql(sql = "", forUpdate);
//        } else {
//            root.forUpdate = forUpdate;
//            fgDs.setSql(sql = generateSql(root).toString(),
//                    forUpdate);
//        }

        return fetchSpec;
    }

    /**
     * Generate SQL text for the expression.
     */
    public CharBuf generateSql(SelectExp root) {
        int aliasCount = root.createAlias(0);
        if (aliasCount == 1) root.alias = null;
        CharBuf s = new CharBuf();
        root.appendSQL(sqlDriver, s, null);
        return s;
    }

    private void doUpdates(StatesToStore toStore, StateContainer container,
            boolean retainValues) {
        initPersistGraph(toStore.isFullSortRequired(), toStore.size());

        OID oid = null;
        int n = toStore.size();
        for (int i = 0; i < n; i++) {
            oid = toStore.oids[i];
            
            ClassMetaData cmd = toStore.states[i].getClassMetaData(jmd);
            
            // make sure no attempt is made to store embedded-only instances
            if (cmd.embeddedOnly) {
                throw BindingSupportImpl.getInstance().runtime("Instances of " +
                        cmd.qname + " may not be persisted as the class is " + 
                        "flagged as emdedded-only");
            }

            // check if the state needs to be returned to the client
            // i.e. it has autoset fields or is new
            if (retainValues || cmd.hasAutoSetFields) {
                container.add(oid, toStore.states[i]);
            } else if (oid.isNew()) {
                container.add(oid, null);
            }

            checkReqFieldsOnUpdate(oid, cmd, toStore, i);
            activePersistGraph.add(oid, toStore.origStates[i],
                    toStore.states[i]);
        }
        activePersistGraph.doAutoSets();
        activePersistGraph.sort();
        if (Debug.DEBUG) {
            activePersistGraph.dump();
        }
        persistPass1(activePersistGraph);
        persistPass2(activePersistGraph);
    }

    /**
     * Make sure required (e.g. jdoVersion) fields are filled in origstate.
     * Fetch the data from the database if needed.
     */
    private void checkReqFieldsOnUpdate(OID oid, ClassMetaData cmd,
            StatesToStore toStore, int i) {
        if (!oid.isNew()) {
            FetchGroup reqFetchGroup = cmd.reqFetchGroup;
            if (reqFetchGroup != null) {
                State old = toStore.origStates[i];
                if (!old.containsFetchGroup(reqFetchGroup)) {
                    State s = cache.getState(oid, reqFetchGroup);
                    if (s == null) {
                        s = getStateImp((JdbcOID)oid, reqFetchGroup, false,
                                DummyStateContainer.INSTANCE);
                    }
                    old.updateNonFilled(s);
                }
            }
        }
    }

    /**
     * Select the correct PersistGraph instance to use and make sure it is
     * big enough. It is cleared for use.
     */
    private void initPersistGraph(boolean fullSort, int minSize) {
        if (fullSort) {
            if (persistGraphFullSort == null
                    || minSize > persistGraphFullSort.size()) {
                persistGraphFullSort = new PersistGraphFullSort(jmd,
                        minSize * 2);
            } else {
                persistGraphFullSort.clear();
            }
            activePersistGraph = persistGraphFullSort;
        } else {
            if (persistGraphPartialSort == null
                    || minSize > persistGraphPartialSort.size()) {
                persistGraphPartialSort = new PersistGraph(jmd,
                        minSize * 2);
            } else {
                persistGraphPartialSort.clear();
            }
            activePersistGraph = persistGraphPartialSort;
        }
        activePersistGraph.optimistic = this.optimistic;
    }

    /**
     * Persist main table fields. All the OID's for new objects are replaced
     * with real OID's.
     *
     * @see #persistPass2
     */
    public void persistPass1(PersistGraph graph) {
        try {
            int[] fieldNos = new int[jmd.maxFieldsLength];
            Object[] oidData = new Object[((JdbcMetaData)jmd.jdbcMetaData).maxPkSimpleColumns];

            CharBuf s = new CharBuf();
            boolean haveNewObjects = false;
            int graphSize = graph.size();

            // generate primary keys for all new objects with preinsert keygens
            // that do not already have keys and all application identity
            // instances not using a post insert keygen
            for (int si = 0; si < graphSize; si++) {
                OID oid = graph.getOID(si);
                if (!oid.isNew() || ((NewObjectOID)oid).realOID != null) continue;
                haveNewObjects = true;
                ClassMetaData cmd = oid.getClassMetaData();
                JdbcKeyGenerator keygen = ((JdbcClass)cmd.storeClass).jdbcKeyGenerator;
                if (keygen == null) {
                    if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                        State ns = graph.getNewState(si);
                        if (Debug.DEBUG) {
                            if (!ns.containsApplicationIdentityFields()) {
                                throw BindingSupportImpl.getInstance().internal(
                                        "pk fields not filled for appid class\n" + ns);
                            }
                        }
                        NewObjectOID noid = (NewObjectOID)oid;
                        ns.copyKeyFields(noid.realOID = cmd.createOID(true));
                        continue;
                    } else {
                        throw BindingSupportImpl.getInstance().runtime("Class " + cmd.qname +
                                " has identity-type " +
                                MDStaticUtils.toIdentityTypeString(
                                        cmd.identityType) +
                                " but no jdbc-key-generator");
                    }
                } else {
                    //if it is app id and the state does contain the key field's
                    //then we do not use the keygen to do it.
                    if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
                        State ns = graph.getNewState(si);
                        if (ns.containsValidAppIdFields()) {
                            ns.copyKeyFields(((NewObjectOID)oid).realOID = cmd.createOID(
                                    true));
                            continue;
                        }
                    }
                }
                if (keygen.isPostInsertGenerator()) continue;

                // count how many keys we need
                int keyCount = 1;
                for (int i = si + 1; i < graphSize; i++) {
                    OID nextOid = graph.getOID(i);
                    if (!nextOid.isNew() || nextOid.getClassIndex() != cmd.index) break;
                    if (((NewObjectOID)nextOid).realOID == null) keyCount++;
                }

                Connection kgcon = null;
                boolean rollback = true;
                synchronized (keygen) {
                    try {
                        OID realOID;
                        for (; keyCount > 0;) {
                            NewObjectOID noid = (NewObjectOID)graph.getOID(si++);
                            if (noid.realOID != null) continue;
                            noid.realOID = realOID = cmd.createOID(true);
                            boolean needKgcon = keygen.isRequiresOwnConnection();
                            if (kgcon == null && needKgcon) {
                                kgcon = conSrc.getConnection(true, false);
                            }
                            keygen.generatePrimaryKeyPre(cmd.qname,
                                    ((JdbcClass)cmd.storeClass).table, keyCount--, oidData,
                                    needKgcon ? kgcon : con());
                            realOID.copyKeyFields(oidData);
                        }
                        rollback = false;
                    } finally {
                        if (kgcon != null) {
                            if (rollback) {
                                kgcon.rollback();
                            } else {
                                kgcon.commit();
                            }
                            conSrc.returnConnection(kgcon);
                        }
                    }
                }
                
                si--;
            }

            // generate the inserts and updates
            IntArray toUpdateIndexes = new IntArray();
            for (int i = 0; i < graphSize;) {
                OID oid = graph.getOID(i);
                ClassMetaData cmd = oid.getClassMetaData();
                if (oid.isNew()) {
                    i = generateInserts((NewObjectOID)oid, i, cmd, graph,
                            fieldNos, s, oidData, toUpdateIndexes);
                } else {
                    i = generateUpdates(oid, i, cmd, graph, fieldNos,
                            haveNewObjects, s);
                }
            }

            if (toUpdateIndexes.size() > 0) {
                int n = toUpdateIndexes.size();
                for (int updateStartIndex = 0; updateStartIndex < n;) {
                    updateStartIndex = generateUpdatesOfCircularReferences(
                            fieldNos, s, toUpdateIndexes, updateStartIndex,
                            graph);
                    if (updateStartIndex == toUpdateIndexes.size()) break;
                }
            }

        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    /**
     * Generate insert statement(s) for pass1 changes to one or more new
     * objects. The only objects without keys will be those using app identity
     * and no keygen and those using postInsert keygens.
     *
     * @return The index of the last object inserted + 1
     */
    private int generateInserts(NewObjectOID oid,
            int index, ClassMetaData cmd, PersistGraph graph, int[] fieldNos,
            CharBuf s, Object[] oidData, IntArray toUpdateIndexes) throws SQLException {

        int identityType = cmd.identityType;
        boolean appIdentity = identityType == MDStatics.IDENTITY_TYPE_APPLICATION;
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        int graphSize = graph.size();

        JdbcColumn classIdCol = jdbcClass.classIdCol;
        Object classId = classIdCol == null ? null : jdbcClass.jdbcClassId;

        JdbcKeyGenerator keygen = jdbcClass.jdbcKeyGenerator;

        // decide if we can use statement batching
        boolean batchPossible = (keygen == null || !keygen.isPostInsertGenerator())
                && useBatchInsert
                && !jdbcClass.noBatching;

        // count how many states we have with the same class that are new
        int sameClassCount = 1;
        for (int i = index + 1; i < graphSize; sameClassCount++, i++) {
            OID nextOid = graph.getOID(i);
            if (!nextOid.isNew() || nextOid.getClassIndex() != cmd.index) break;
        }

        Connection con = con();

        for (; sameClassCount > 0;) {

            State ns = graph.getNewState(index);

            // figure out what key generation needs to happen
            boolean useKeyGenPre = false;
            boolean useKeyGenPost = false;
            boolean fillFieldsFromOid = false;
            boolean clearAppIdFields = false;
            if (appIdentity) {
                if (keygen != null) {
                    useKeyGenPost = keygen.isPostInsertGenerator();
                    useKeyGenPre = !useKeyGenPost;
                    fillFieldsFromOid = true;
                    clearAppIdFields = true;
                    ns.clearApplicationIdentityFields();
                }
            } else {
                useKeyGenPost = keygen.isPostInsertGenerator();
                useKeyGenPre = !useKeyGenPost;
            }

            // count how many states we can insert with the same SQL
            int count = 1;
            for (int i = index + 1; count < sameClassCount; count++, i++) {
                // make sure the next state has the same field numbers
                State nextState = graph.getNewState(i);
                if (clearAppIdFields) nextState.clearApplicationIdentityFields();
                if (nextState.compareToPass1(ns) != 0) break;
            }

            boolean batch = batchPossible && count > 1;
            int numFieldNos = ns.getPass1FieldNos(fieldNos);

            // do a stripe of inserts for each table for the class
            int startIndex = index;
            int startSameClassCount = sameClassCount;
            int startCount = count;
            for (int tableNo = 0;
                 tableNo < jdbcClass.allTables.length; tableNo++) {
                JdbcTable table = jdbcClass.allTables[tableNo];
                if (tableNo > 0) {   // reset after first table
                    classIdCol = null;
                    useKeyGenPre = true;
                    useKeyGenPost = false;
                    fillFieldsFromOid = false;
                    ns = graph.getNewState(index = startIndex);
                    oid = (NewObjectOID)graph.getOID(index);
                    sameClassCount = startSameClassCount;
                    count = startCount;
                }

                // create PreparedStatement and do count inserts for each
                PreparedStatement ps = null;
                try {

                    // create ps for the insert
                    boolean lobColFound = createInsertSql(jdbcClass, table,
                            useKeyGenPre, classIdCol, fieldNos, numFieldNos, s,
                            ns);
                    if (useKeyGenPost) {
                        String suffix = keygen.getPostInsertSQLSuffix(table);
                        if (suffix != null) s.append(suffix);
                    }
                    String sql = s.toString();
                    ps = con.prepareStatement(sql);

                    for (; ;) {
                        boolean alreadyHaveRealOID = oid.realOID != null;
                        JdbcOID realOID;
                        if (alreadyHaveRealOID) {
                            realOID = (JdbcOID)oid.realOID;
                        } else {
                            realOID = (JdbcOID)(oid.realOID = cmd.createOID(true));
                        }

                        int pos = useKeyGenPre ? realOID.setParams(ps, 1) : 1;

                        if (classIdCol != null) {
                            classIdCol.set(ps, pos++, classId);
                        }

                        // do the insert
                        if (tableNo == 0) {
                            if (ns.replaceNewObjectOIDs(fieldNos, numFieldNos)) {
                                toUpdateIndexes.add(index);
                            }
                        }
                        try {
                            ((JdbcState)ns).setParams(ps, fieldNos, 0, numFieldNos, pos,
                                    graph, tableNo);
                        } catch (Exception e) {
                            throw handleException(
                                    "Error setting parameters on PreparedStatement " +
                                    "for insert of '" + Utils.toString(realOID) + "':\n" +
                                    JdbcUtils.toString(e) + "\n" +
                                    JdbcUtils.getPreparedStatementInfo(sql, ps),
                                    e);
                        }
                        if (batch) {
                            ps.addBatch();
                        } else {
                            try {
                                ps.execute();
                            } catch (Exception e) {
                                throw handleException("Insert of '" +
                                        Utils.toString(realOID) + "' failed: " +
                                        JdbcUtils.toString(e) + "\n" +
                                        JdbcUtils.getPreparedStatementInfo(sql,
                                                ps),
                                        e);
                            }
                        }

                        // do post insert key generation if required
                        if (useKeyGenPost) {
                            keygen.generatePrimaryKeyPost(cmd.qname, table,
                                    oidData, con,
                                    ((PooledPreparedStatement)ps).getStatement());
                            realOID.copyKeyFields(oidData);
                        }

                        if (fillFieldsFromOid) ns.copyFields(realOID);

                        ++index;
                        if (--sameClassCount == 0) break;
                        oid = (NewObjectOID)graph.getOID(index);
                        if (--count == 0) break;

                        ns = graph.getNewState(index);
                    }
                    if (batch) {
                        try {
                            ps.executeBatch();
                        } catch (Exception e) {
                            throw handleException(
                                    "Batch insert failed: " +
                                    JdbcUtils.toString(e) + "\n" +
                                    JdbcUtils.getPreparedStatementInfo(sql, ps),
                                    e);
                        }
                    }

                    // If there was at least one Oracle style LOB col we have to
                    // select all of the non null LOB cols back to give their
                    // converters a chance to set the data in the LOB.
                    int lobNumFieldNos;
                    if (lobColFound &&
                            (lobNumFieldNos = removeNullLOBFields(fieldNos,
                                    numFieldNos, ns)) > 0) {
                        selectAndUpdateOracleLOBCols(s, startIndex, index,
                                jdbcClass, table, fieldNos, lobNumFieldNos,
                                graph);
                    }

                } finally {
                    cleanup(ps);
                }
            }
        }

        return index;
    }

    /**
     * Look at all the negative entries in fieldNos, make any that are for
     * not null fields in state positive and copy them to the beginning of
     * fieldsNos. Return the number of not-null negative entries found and
     * converted. This effectively compresses the array of fieldNos for
     * faster LOB processing.
     */
    private int removeNullLOBFields(int[] fieldNos, int numFieldNos,
            State state) {
        int pos = 0;
        for (int i = 0; i < numFieldNos; i++) {
            int fieldNo = fieldNos[i];
            if (fieldNo < 0) {
                fieldNo = -(fieldNo + 1);
                if (!state.isNull(fieldNo)) fieldNos[pos++] = fieldNo;
            }
        }
        return pos;
    }

    /**
     * Generate SQL to insert a row into the table for a class hierarchy. Any
     * entries in fieldNos for fields that return true to appendInsertValueList
     * (i.e. they did not add a replacable param) are made negative. This is
     * used to handle Oracle LOB columns. Returns true if there was at least
     * one such column or false otherwise.
     */
    private boolean createInsertSql(JdbcClass jdbcClass, JdbcTable table,
            boolean useKeyGenPre, JdbcColumn classIdCol, int[] fieldNos,
            int numFieldNos, CharBuf s, State state) {
        JdbcField[] stateFields = jdbcClass.stateFields;
        s.clear();
        s.append("INSERT INTO ");
        s.append(table.name);
        s.append(" (");
        if (useKeyGenPre) table.appendInsertPKColumnList(s);
        boolean first = !useKeyGenPre;
        if (classIdCol != null) {
            if (first) {
                first = false;
            } else {
                s.append(',');
                s.append(' ');
            }
            classIdCol.appendNames(s);
        }
        for (int i = 0; i < numFieldNos; i++) {
            int fieldNo = fieldNos[i];
            JdbcField f = stateFields[fieldNo];
            if (f.mainTableColsForUpdate == null || f.mainTable != table) continue;
            if (first) {
                first = false;
            } else {
                s.append(',');
                s.append(' ');
            }
            f.appendInsertColumnList(s);
        }
        s.append(") VALUES (");
        if (useKeyGenPre) table.appendInsertPKValueList(s);
        first = !useKeyGenPre;
        if (classIdCol != null) {
            if (first) {
                first = false;
            } else {
                s.append(',');
                s.append(' ');
            }
            classIdCol.appendParams(s);
        }
        boolean lobColFound = false;
        for (int i = 0; i < numFieldNos; i++) {
            int fieldNo = fieldNos[i];
            JdbcField f = stateFields[fieldNo];
            if (f.mainTableColsForUpdate == null || f.mainTable != table) continue;
            if (first) {
                first = false;
            } else {
                s.append(',');
                s.append(' ');
            }
            if (f.appendInsertValueList(s, state)) {
                // no replaceable param so skip field when params are set
                fieldNos[i] = -(fieldNo + 1);
                lobColFound = true;
            }
        }
        s.append(")");
        return lobColFound;
    }

    /**
     * Select all the LOB cols for the OIDs in the graph and update them.
     * All the states will have the same LOB fields and only LOB fields
     * for table will be in the fieldNos array.
     */
    private void selectAndUpdateOracleLOBCols(CharBuf s, int startIndex,
            int index, JdbcClass jdbcClass, JdbcTable table, int[] fieldNos,
            int numFieldNos, PersistGraph graph)
            throws SQLException {
        Connection con = con();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            int oidCount = index - startIndex;
            int maxOIDsForIN = jdbcClass.getMaxOIDsForIN(sqlDriver);

            if (oidCount > 1 && maxOIDsForIN > 1) {

                // process OIDs in blocks using IN (?, .., ?)
                Map map = new HashMap(maxOIDsForIN * 2);
                JdbcOID key = (JdbcOID)jdbcClass.cmd.createOID(true);

                int fullBlocks = oidCount / maxOIDsForIN;
                if (fullBlocks > 0) {
                    s.clear();
                    createSelectLOBsSql(jdbcClass, table, fieldNos,
                            numFieldNos, s, maxOIDsForIN);
                    ps = con.prepareStatement(s.toString());
                    for (int i = 0; i < fullBlocks; i++) {
                        for (int j = 0; j < maxOIDsForIN;) {
                            OID oid = graph.getOID(startIndex);
                            if (oid instanceof NewObjectOID) {
                                oid = ((NewObjectOID)oid).realOID;
                            }
                            map.put(oid, graph.getNewState(startIndex++));
                            ((JdbcOID)oid).setParams(ps, ++j);
                        }
                        rs = ps.executeQuery();
                        for (int j = 0; j < maxOIDsForIN; j++) {
                            rs.next();
                            key.copyKeyFields(rs, 1);
                            State ns = (State)map.get(key);
                            ((JdbcState)ns).setOracleStyleLOBs(rs, fieldNos, numFieldNos, 2);
                        }
                        rs.close();
                    }
                    rs = null;
                    ps.close();
                    ps = null;
                }

                oidCount = oidCount % maxOIDsForIN;
                if (oidCount > 1) {
                    // process partial block
                    s.clear();
                    createSelectLOBsSql(jdbcClass, table, fieldNos,
                            numFieldNos, s, oidCount);
                    ps = con.prepareStatement(s.toString());
                    map.clear();
                    for (int j = 0; j < oidCount;) {
                        OID oid = graph.getOID(startIndex);
                        if (oid instanceof NewObjectOID) {
                            oid = ((NewObjectOID)oid).realOID;
                        }
                        map.put(oid, graph.getNewState(startIndex++));
                        ((JdbcOID)oid).setParams(ps, ++j);
                    }
                    rs = ps.executeQuery();
                    for (int j = 0; j < oidCount; j++) {
                        rs.next();
                        key.copyKeyFields(rs, 1);
                        State ns = (State)map.get(key);
                        ((JdbcState)ns).setOracleStyleLOBs(rs, fieldNos, numFieldNos, 2);
                    }
                    rs.close();
                    rs = null;
                    ps.close();
                    ps = null;
                    oidCount = 0;
                }
            }

            if (oidCount == 1 || maxOIDsForIN <= 1) {
                // process OIDs one at a time
                s.clear();
                createSelectLOBsSql(jdbcClass, table, fieldNos, numFieldNos, s,
                        1);
                ps = con.prepareStatement(s.toString());
                for (int i = startIndex; i < index; i++) {
                    OID oid = graph.getOID(i);
                    if (oid instanceof NewObjectOID) {
                        oid = ((NewObjectOID)oid).realOID;
                    }
                    ((JdbcOID)oid).setParams(ps, 1);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw BindingSupportImpl.getInstance().fatalDatastore(
                                "Row not found: " + oid.toSString());
                    }
                    State ns = graph.getNewState(i);
                    ((JdbcState)ns).setOracleStyleLOBs(rs, fieldNos, numFieldNos, 1);
                    rs.close();
                }
                rs = null;
                ps.close();
                ps = null;
            }
        } finally {
            cleanup(rs);
            cleanup(ps);
        }
    }

    /**
     * Generate SQL to select Oracle style LOB columns for a table for a
     * class hierarchy. The SQL must provide for blocksz OID parameters for
     * the query. If there is more than one then an IN must be used. The
     * blocksz will always be 1 if the table uses a composite primary key.
     *
     * @see #createInsertSql
     */
    private void createSelectLOBsSql(JdbcClass jdbcClass, JdbcTable table,
            int[] fieldNos, int numFieldNos, CharBuf s, int blocksz) {
        JdbcField[] stateFields = jdbcClass.stateFields;
        s.clear();
        s.append("SELECT ");
        if (blocksz > 1) {
            table.appendInsertPKColumnList(s);
            s.append(',');
            s.append(' ');
        }
        stateFields[fieldNos[0]].appendInsertColumnList(s);
        for (int i = 1; i < numFieldNos; i++) {
            s.append(',');
            s.append(' ');
            stateFields[fieldNos[i]].appendInsertColumnList(s);
        }
        s.append(" FROM ");
        s.append(table.name);
        s.append(" WHERE ");
        if (blocksz == 1) {
            table.appendWherePK(s);
        } else {
            s.append(table.pk[0].name); // will never be composite pk
            s.append(" IN (");
            s.append('?');
            for (int i = 1; i < blocksz; i++) {
                s.append(',');
                s.append('?');
            }
            s.append(')');
        }
        s.append(" FOR UPDATE");
    }

    /**
     * Generate update statement(s) for pass1 changes to one or more objects.
     *
     * @return The index of the last object updated + 1
     */
    private int generateUpdates(OID oid, int index,
            ClassMetaData cmd, PersistGraph graph, int[] fieldNos,
            boolean haveNewObjects, CharBuf s)
            throws SQLException {

        State ns = graph.getNewState(index);
        if (!ns.containsPass1Fields()) return ++index;

        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        State os = graph.getOldState(index);
        boolean usingChanged =
                jdbcClass.optimisticLocking == JdbcClass.OPTIMISTIC_LOCKING_CHANGED;
        JdbcSimpleField optimisticLockingField = jdbcClass.optimisticLockingField;
        boolean usingOLF = optimisticLockingField != null;

        Connection con = con();

        // count how many states we can update with the same SQL
        int graphSize = graph.size();
        // the amount of states with the same fields for update
        int count = 1;
        for (int i = index + 1; i < graphSize; count++, i++) {
            // make sure the next object is not new and has the same class
            OID nextOid = graph.getOID(i);
            if (Debug.DEBUG) {
                if (!nextOid.isNew() && !nextOid.isResolved()) {
                    throw BindingSupportImpl.getInstance().internal("OID is not resolved: "
                            + oid.toSString());
                }
            }
            if (nextOid.isNew() || nextOid.getClassIndex() != cmd.index) break;

            // make sure the next state has the same field numbers
            State nextState = graph.getNewState(i);
            if (nextState.compareToPass1(ns) != 0) break;

            // if we are using changed optimistic locking make sure the next
            // old state has the same null fields as the current old state
            if (usingChanged) {
                if (!((JdbcState)os).hasSameNullFields(graph.getOldState(i), ns)) break;
            }
        }

        // decide if we will use statement batching
        boolean batch = count > 1 && useBatchUpdate
                && !jdbcClass.noBatching;

        // check if the OID must be updated from the state after the update
        boolean updateOIDFromState = ns.containsApplicationIdentityFields();

        // deny update of app identity for inheritance heirachies
        if (updateOIDFromState && cmd.isInHierarchy()) {
            throw BindingSupportImpl.getInstance().runtime("Application identity change for inheritance heirachies " +
                    "is not supported: " + oid.toSString());
        }

        final int numFieldNos = ns.getPass1FieldNos(fieldNos);

        // do a stripe of inserts for each table for the class
        int startIndex = index;
        int startCount = count;
        for (int tableNo = 0; tableNo < jdbcClass.allTables.length; tableNo++) {
            JdbcTable table = jdbcClass.allTables[tableNo];
            if (tableNo > 0) {   // reset after first table
                ns = graph.getNewState(index = startIndex);
                os = graph.getOldState(index);
                oid = graph.getOID(index);
                count = startCount;
            }

            // create PreparedStatement(s) and do count updates for each
            PreparedStatement ps = null;
            try {
                // generate the SQL for the table
                boolean lobColFound = createUpdateSql(jdbcClass, table, numFieldNos,
                        fieldNos, (usingOLF && tableNo == 0), optimisticLockingField, usingChanged, os,
                        ns, s);
                String sql = s.toString();
                if (sql.length() == 0) {
                    index = index + startCount;
                    continue;
                }

                ps = con.prepareStatement(sql);

                for (; ;) {
                    if (haveNewObjects) {
                        ns.replaceNewObjectOIDs(fieldNos,
                                numFieldNos);
                    }

                    // set parameters on ps
                    try {
                        int pos = ((JdbcState)ns).setParams(ps, fieldNos, 0, numFieldNos, 1,
                                graph, tableNo);
                        pos = ((JdbcOID)oid).setParams(ps, pos);
                        if (usingOLF && tableNo == 0) {
                            //this is only needed on the base table
                            ((JdbcState)os).setOptimisticLockingParams(ps, pos);
                        } else if (usingChanged) {
                            ((JdbcState)os).setParamsChangedAndNotNull(ps, fieldNos, 0,
                                    numFieldNos, pos, graph, tableNo);
                        }
                    } catch (SQLException e) {
                        throw handleException("Error setting parameters on PreparedStatement for " +
                                "update of '" + Utils.toString(oid) + "':\n" +
                                JdbcUtils.toString(e) + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, ps), e);
                    }

                    // do the update
                    if (batch) {
                        ps.addBatch();
                    } else {
                        int uc;
                        try {
                            uc = ps.executeUpdate();
                        } catch (Exception e) {
                            throw handleException(
                                    "Update failed: " +
                                    JdbcUtils.toString(e) + "\n" +
                                    "Row: " + oid.toSString() + "\n" +
                                    JdbcUtils.getPreparedStatementInfo(sql, ps),
                                    e, true, oid);
                        }
                        if (uc == 0) {
                            throw BindingSupportImpl.getInstance().concurrentUpdate(
                                    "Row not found: " +
                                    oid.toSString() + "\n" +
                                    JdbcUtils.getPreparedStatementInfo(sql, ps), oid);
                        }
                    }
                    if (updateOIDFromState) ns.copyKeyFieldsUpdate(oid);

                    index = index + 1;
                    if (--count == 0) break;
                    oid = graph.getOID(index);
                    ns = graph.getNewState(index);
                    os = graph.getOldState(index);
                }

                // if batching then exec the batch and check all the update counts
                if (batch) {
                    int[] a;
                    try {
                        a = ps.executeBatch();
                    } catch (Exception e) {
                        throw handleException("Batch update failed: " + JdbcUtils.toString(
                                e) + "\n" +
                                "Row: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, ps), e, true, oid);
                    }
                    for (int j = 0; j < count; j++) {
                        int c = a[j];
                        if (c <= 0) {
                            String psi = JdbcUtils.getPreparedStatementInfo(
                                    sql, ps, j);
                            oid = graph.getOID(startIndex + j);
                            if (c == 0) {
                                throw BindingSupportImpl.getInstance().concurrentUpdate(
                                        "Row not found: " + oid.toSString() + "\n" + psi, oid);
                            }
                            throw BindingSupportImpl.getInstance().datastore(
                                    "Unexpected update count " +
                                    c + " for row: " + oid.toSString() + "\n" + psi);
                        }
                    }
                }

                // If there was at least one Oracle style LOB col we have to
                // select all of the non null LOB cols back to give their
                // converters a chance to set the data in the LOB.
                int lobNumFieldNos;
                if (lobColFound &&
                        (lobNumFieldNos = removeNullLOBFields(fieldNos,
                                numFieldNos, ns)) > 0) {
                    selectAndUpdateOracleLOBCols(s, startIndex, index,
                            jdbcClass, table, fieldNos, lobNumFieldNos,
                            graph);
                }
            } finally {
                cleanup(ps);
            }
        }
        return index;
    }

    /**
      * This method is used as part of the insert operation to update all newOid's with null
      * realOids at the time of the insert. This will only happen if there is a circular dep on the
      * creation of id's (e.g. two new Application id instances that uses autoinc pk columns).
      */
     private int generateUpdatesOfCircularReferences(int[] fieldNos, CharBuf s,
             IntArray toUpdateIndexes, int indexInIntArray, PersistGraph graph)
             throws SQLException {
        OID oid = graph.getOID(toUpdateIndexes.get(indexInIntArray));
        State ns = graph.getNewState(toUpdateIndexes.get(indexInIntArray));

        if (!ns.containsPass1Fields()) return ++indexInIntArray;
        ClassMetaData cmd = ns.getClassMetaData(jmd);

        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        State os = graph.getOldState(toUpdateIndexes.get(indexInIntArray));
        boolean usingChanged =
                jdbcClass.optimisticLocking == JdbcClass.OPTIMISTIC_LOCKING_CHANGED;
        JdbcSimpleField optimisticLockingField = jdbcClass.optimisticLockingField;
        boolean usingOLF = optimisticLockingField != null;

        // the amount of states with the same fields for update
        int count = 1;
        for (int i = indexInIntArray + 1; i < toUpdateIndexes.size(); count++, i++) {
            // make sure the next object is not new and has the same class
            OID nextOid = graph.getOID(toUpdateIndexes.get(i));
            if (Debug.DEBUG) {
                if (!nextOid.isNew() && !nextOid.isResolved()) {
                    throw BindingSupportImpl.getInstance().internal("OID is not resolved: "
                            + oid.toSString());
                }
            }
            if (nextOid.isNew() || nextOid.getClassIndex() != cmd.index) break;

            // make sure the next state has the same field numbers
            State nextState = graph.getNewState(toUpdateIndexes.get(i));
            if (nextState.compareToPass1(ns) != 0) break;

            // if we are using changed optimistic locking make sure the next
            // old state has the same null fields as the current old state
            if (usingChanged) {
                if (!((JdbcState)os).hasSameNullFields(graph.getOldState(toUpdateIndexes.get(i)), ns)) break;
            }
        }

        // decide if we will use statement batching
        boolean batch = count > 1 && useBatchUpdate && !jdbcClass.noBatching;
        final int numFieldNos = ns.getPass1FieldRefFieldNosWithNewOids(fieldNos);

        // do a stripe of inserts for each table for the class
        Connection con = con();
        int startIndex = indexInIntArray;
        int startCount = count;
        for (int tableNo = 0; tableNo < jdbcClass.allTables.length; tableNo++) {
            JdbcTable table = jdbcClass.allTables[tableNo];
            if (tableNo > 0) {   // reset after first table
                ns = graph.getNewState(toUpdateIndexes.get(indexInIntArray = startIndex));
                os = graph.getOldState(toUpdateIndexes.get(indexInIntArray));
                oid = graph.getOID(toUpdateIndexes.get(indexInIntArray));
                count = startCount;
            }

            // create PreparedStatement(s) and do count updates for each
            PreparedStatement ps = null;
            try {
                // generate the SQL for the table
                createUpdateSql(jdbcClass, table, numFieldNos,
                        fieldNos, false, optimisticLockingField, false, os,
                        ns, s);
                String sql = s.toString();
                if (sql.length() == 0) {
                    indexInIntArray = indexInIntArray + startCount;
                    continue;
                }

                ps = con.prepareStatement(sql);

                for (; ;) {
                    ns.replaceNewObjectOIDs(fieldNos,
                            numFieldNos);

                    // set parameters on ps
                    try {
                        int pos = ((JdbcState)ns).setParams(ps, fieldNos, 0, numFieldNos, 1,
                                graph, tableNo);
                        pos = ((JdbcOID)oid).setParams(ps, pos);
                        if (os != null&& usingOLF && tableNo == 0) {
                            //this is only needed on the base table
                            ((JdbcState)os).setOptimisticLockingParams(ps, pos);
                        } else if (usingChanged) {
                            ((JdbcState)os).setParamsChangedAndNotNull(ps, fieldNos, 0,
                                    numFieldNos, pos, graph, tableNo);
                        }
                    } catch (SQLException e) {
                        throw handleException(
                                "Error setting parameters on PreparedStatement for " +
                                "update of '" + Utils.toString(oid) + "':\n" +
                                JdbcUtils.toString(e) + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, ps), e);
                    }

                    // do the update
                    if (batch) {
                        ps.addBatch();
                    } else {
                        int uc;
                        try {
                            uc = ps.executeUpdate();
                        } catch (Exception e) {
                            throw handleException(
                                    "Update failed: " +
                                    JdbcUtils.toString(e) + "\n" +
                                    "Row: " + oid.toSString() + "\n" +
                                    JdbcUtils.getPreparedStatementInfo(sql, ps), e, true, oid);
                        }
                        if (uc == 0) {
                            throw BindingSupportImpl.getInstance().concurrentUpdate(
                                    "Row not found: " +
                                    oid.toSString() + "\n" +
                                    JdbcUtils.getPreparedStatementInfo(sql, ps), oid);
                        }
                    }

                    indexInIntArray = indexInIntArray + 1;
                    if (--count == 0) break;
                    oid = graph.getOID(toUpdateIndexes.get(indexInIntArray));
                    ns = graph.getNewState(toUpdateIndexes.get(indexInIntArray));
                    os = graph.getOldState(toUpdateIndexes.get(indexInIntArray));
                }

                // if batching then exec the batch and check all the update counts
                if (batch) {
                    int[] a;
                    try {
                        a = ps.executeBatch();
                    } catch (Exception e) {
                        throw handleException(
                                "Batch update failed: " + JdbcUtils.toString(
                                        e) + "\n" +
                                "Row: " + oid.toSString() + "\n" +
                                JdbcUtils.getPreparedStatementInfo(sql, ps), e, true, oid);
                    }
                    for (int j = 0; j < count; j++) {
                        int c = a[j];
                        if (c <= 0) {
                            String psi = JdbcUtils.getPreparedStatementInfo(
                                    sql, ps, j);
                            oid = graph.getOID(startIndex + j);
                            if (c == 0) {
                                throw BindingSupportImpl.getInstance().concurrentUpdate(
                                        "Row not found: " + oid.toSString() + "\n" + psi, oid);
                            }
                            throw BindingSupportImpl.getInstance().datastore(
                                    "Unexpected update count " +
                                    c + " for row: " + oid.toSString() + "\n" + psi);
                        }
                    }
                }
            } finally {
                cleanup(ps);
            }
        }
        return indexInIntArray;
    }

    /**
     * Generate SQL to update a row in the base table for a class hierarchy.
     * Any entries in fieldNos for fields that return true to appendUpdate
     * (i.e. they did not add a replacable param) are made negative. This is
     * used to handle Oracle LOB columns. Returns true if there was at least
     * one such column or false otherwise.
     */
    private boolean createUpdateSql(JdbcClass jdbcClass,
            JdbcTable table, int numFieldNos, int[] fieldNos, boolean usingOLF,
            JdbcSimpleField optimisticLockingField,
            boolean usingChanged, State os, State ns, CharBuf s) {
        JdbcField[] fields = jdbcClass.stateFields;
        boolean lobColFound = false;
        s.clear();
        s.append("UPDATE ");
        s.append(table.name);
        s.append(" SET ");
        boolean first = true;
        for (int i = 0; i < numFieldNos; i++) {
            int fieldNo = fieldNos[i];
            JdbcField f = fields[fieldNo];
            if (f.mainTableColsForUpdate == null || f.mainTable != table) continue;
            if (first) {
                first = false;
            } else {
                s.append(',');
                s.append(' ');
            }
            if (f.appendUpdate(s, ns)) {
                // no replaceable param so skip field when params are set
                fieldNos[i] = -(fieldNo + 1);
                lobColFound = true;
            }
        }
        if (first) {    // no columns to update
            s.clear();
        } else {
            s.append(" WHERE ");
            table.appendWherePK(s);
            if (usingOLF) {
                s.append(" AND ");
                optimisticLockingField.appendWhere(s, sqlDriver);
            } else if (usingChanged) {
                for (int i = 0; i < numFieldNos; i++) {
                    int fieldNo = fieldNos[i];
                    if (fieldNo < 0) continue;
                    JdbcField f = fields[fieldNo];
                    if (f.mainTableColsForUpdate == null
                            || !f.includeForChangedLocking
                            || f.mainTable != table) {
                        continue;
                    }
                    s.append(" AND ");
                    if (os.isNull(fieldNo)) {
                        f.appendWhereIsNull(s, sqlDriver);
                    } else {
                        f.appendWhere(s, sqlDriver);
                    }
                }
            }
        }
        return lobColFound;
    }

    /**
     * Perist fields stored in link tables and so on.
     *
     * @see #persistPass1
     */
    private void persistPass2(PersistGraph graph) {
        try {
            int[] fieldNos = new int[jmd.maxFieldsLength];
            CharBuf s = new CharBuf();
            int graphSize = graph.size();

            // process blocks of the same class together
            int startIndex = 0;
            for (; startIndex < graphSize;) {
                State ns = graph.getNewState(startIndex);
                if (!ns.containsPass2Fields()) {
                    startIndex++;
                    continue;
                }

                // count entries in the graph with the same class
                int classIndex = ns.getClassIndex();
                int blockEnd = startIndex;
                for (;
                     ++blockEnd < graphSize
                        && graph.getNewState(blockEnd).getClassIndex() == classIndex;) {
                    ;
                }

                // get info common to all the entries in the block
                ClassMetaData cmd = ns.getClassMetaData(jmd);
                Connection con = con();

                // find the fields we need to check for each block entry
                int[] fna;
                int nf;
                if (blockEnd - startIndex == 1) {
                    // only one entry so check only its pass 2 fields
                    nf = ns.getPass2FieldNos(fieldNos);
                    fna = fieldNos;
                } else {
                    // multiple entries so check all pass 2 fields
                    fna = cmd.pass2Fields;
                    nf = fna.length;
                }

                // process block for each fieldNo so we can make best use
                // of PreparedStatement's and batching
                for (int fpos = 0; fpos < nf; fpos++) {
                    int fieldNo = fna[fpos];
                    FieldMetaData fmd = cmd.stateFields[fieldNo];
                    ((JdbcField)fmd.storeField).persistPass2Block(graph, startIndex,
                            blockEnd, s, con, useBatchInsert, useBatchUpdate);
                }

                startIndex = blockEnd;
            }
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    private void doDeletes(DeletePacket toDelete) {
        if (!toDelete.isKeepStates()) {
            toDelete.sortOIDs(new OIDRefGraphIndexComparator());
        }
        deletePass1(toDelete);
        deletePass2(toDelete);
    }

    /**
     * Delete rows from link tables.
     *
     * @see #deletePass2
     */
    private void deletePass1(DeletePacket graph) {
        try {
            CharBuf s = new CharBuf();
            int graphSize = graph.size();

            for (int startIndex = 0; startIndex < graphSize;) {

                OID oid = graph.oids[startIndex];
                int classIndex = oid.getClassIndex();
                ClassMetaData cmd = jmd.classes[classIndex];

                // count entries in the graph with the same class
                int blockEnd = startIndex;
                for (;
                     ++blockEnd < graphSize
                        && graph.oids[blockEnd].getClassIndex() == classIndex;) {
                    ;
                }

                // get info common to all the entries in the block
                Connection con = con();

                // find the fields we need to check for each block entry
                int[] fna = cmd.pass2Fields;
                int nf = fna.length;

                // process block for each fieldNo so we can make best use
                // of PreparedStatement's and batching
                for (int fpos = 0; fpos < nf; fpos++) {
                    int fieldNo = fna[fpos];
                    FieldMetaData fmd = cmd.stateFields[fieldNo];
                    ((JdbcField)fmd.storeField).deletePass2Block(graph, startIndex,
                            blockEnd, s, con, useBatchUpdate);
                }

                startIndex = blockEnd;
            }
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    /**
     * Delete main table rows.
     *
     * @see #deletePass1
     */
    public void deletePass2(DeletePacket graph) {
        try {
            CharBuf s = new CharBuf();
            int graphSize = graph.size();

            int count;
            for (int startIndex = 0; startIndex < graphSize; startIndex += count) {
                OID oid = graph.oids[startIndex];
                int classIndex = oid.getClassIndex();
                ClassMetaData cmd = jmd.classes[classIndex];
                JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
                Connection con = con();

                boolean batch = useBatchUpdate;
                boolean useInList = jdbcClass.table.pkSimpleCols.length == 1;

                count = 1;
                for (int index = startIndex + 1; index < graphSize; count++, index++) {
                    if (graph.oids[startIndex + count].getClassMetaData() != cmd) break;
                }
                if (count == 1) {
                    useInList = false;
                }

                PreparedStatement ps = null;
                try {
                    if (!batch && !useInList) {
                        //delete heirarchies one-by-one
                        int n = jdbcClass.allTables.length;
                        for (int tableNo = n - 1; tableNo >= 0; tableNo--) {
                            //must create ps now
                            String sql = getDeleteRowSql(
                                    jdbcClass.allTables[tableNo], s);
                            ps = con.prepareStatement(sql);

                            for (int i = 0; i < count; i++) {
                                deleteRow(ps, (JdbcOID)graph.oids[startIndex + i], sql);
                            }
                        }
                    } else if (useInList) {
                        //use 'IN' list
                        final int maxInOps = sqlDriver.getMaxInOperands();
                        final char[] whereParam = sqlDriver.getSqlParamStringChars(
                                jdbcClass.table.pkSimpleCols[0].jdbcType);

                        if (count <= maxInOps) {
                            final char[] totalWhereParams = createInParamArray(
                                    whereParam, count);
                            int n = jdbcClass.allTables.length;
                            for (int tableNo = n - 1; tableNo >= 0; tableNo--) {
                                getDeleteRowSqlWithInList(jdbcClass.allTables[tableNo], s);
                                s.append(totalWhereParams);
                                String sql = s.toString();
                                ps = con.prepareStatement(sql);
                                for (int i = 0; i < count; i++) {
                                    ((JdbcOID)graph.oids[startIndex + i]).setParams(ps, (i + 1));
                                }
                                try {
                                    ps.executeUpdate();
                                } catch (Exception e) {
                                    throw handleException(
                                            "Delete with IN list failed: " + JdbcUtils.toString(e) + "\n" +
                                            JdbcUtils.getPreparedStatementInfo(sql, ps),
                                            e);
                                }
                            }
                        } else {
                            int n = jdbcClass.allTables.length;
                            int amountLeft = count % sqlDriver.getMaxInOperands();
                            int amountOfFullRuns = count/maxInOps;

                            char[] totalWhereParams1 = null;
                            if (amountLeft > 0) {
                                totalWhereParams1 = createInParamArray(whereParam,
                                        amountLeft);
                            }
                            char[] totalWhereParams2 = createInParamArray(whereParam,
                                    maxInOps);

                            for (int tableNo = n - 1; tableNo >= 0; tableNo--) {
                                String sql = null;
                                int pos = startIndex;
                                if (amountLeft > 0) {
                                    //do the smaller amount first and once
                                    getDeleteRowSqlWithInList(jdbcClass.allTables[tableNo], s);
                                    s.append(totalWhereParams1);

                                    sql = s.toString();
                                    ps = con.prepareStatement(sql);
                                    for (int i = 0; i < amountLeft; i++) {
                                        ((JdbcOID)graph.oids[pos++]).setParams(ps, (i + 1));
                                    }
                                    try {
                                        ps.executeUpdate();
                                    } catch (Exception e) {
                                        throw handleException(
                                                "Delete with IN list failed: " + JdbcUtils.toString(e) + "\n" +
                                                JdbcUtils.getPreparedStatementInfo(sql, ps),
                                                e);
                                    }
                                }

                                getDeleteRowSqlWithInList(jdbcClass.allTables[tableNo], s);
                                s.append(totalWhereParams2);
                                sql = s.toString();
                                ps = con.prepareStatement(sql);

                                //do the full runs
                                for (int i = 0; i < amountOfFullRuns; i++) {
                                    for (int j = 0; j < maxInOps; j++) {
                                        ((JdbcOID)graph.oids[pos++]).setParams(ps, (j + 1));
                                    }
                                    try {
                                        ps.executeUpdate();
                                    } catch (Exception e) {
                                        throw handleException(
                                                "Delete with IN list failed: " + JdbcUtils.toString(e) + "\n" +
                                                JdbcUtils.getPreparedStatementInfo(sql, ps),
                                                e);
                                    }
                                }
                            }
                        }
                    } else {
                        //use batching
                        int n = jdbcClass.allTables.length;
                        for (int tableNo = n - 1; tableNo >= 0; tableNo--) {
                            String sql = getDeleteRowSql(
                                    jdbcClass.allTables[tableNo], s);
                            ps = con.prepareStatement(sql);
                            for (int i = 0; i < count; i++) {
                                ((JdbcOID)graph.oids[startIndex + i]).setParams(ps, 1);
                                ps.addBatch();
                            }

                            int[] a;
                            try {
                                a = ps.executeBatch();
                            } catch (Exception e) {
                                throw handleException(
                                        "Batch delete failed: " + JdbcUtils.toString(e) + "\n" +
                                        "Row: " + graph.oids[startIndex].toSString() + "\n" +
                                        JdbcUtils.getPreparedStatementInfo(sql, ps),
                                        e, true, graph.oids[startIndex]);
                            }
                            for (int j = 0; j < count; j++) {
                                int c = a[j];
                                if (c <= 0) {
                                    String psi = JdbcUtils.getPreparedStatementInfo(
                                            sql, ps, j);
                                    if (c == 0) {
                                        throw BindingSupportImpl.getInstance().concurrentUpdate(
                                                "Row not found: " + graph.oids[startIndex + j].toSString() + "\n" + psi, graph.oids[startIndex + j]);
                                    }
                                    throw BindingSupportImpl.getInstance().datastore(
                                            "Unexpected update count " +
                                            c + " for row: " + graph.oids[startIndex + j].toSString() + "\n" + psi);
                                }
                            }
                        }
                    }
                } finally {
                    cleanup(ps);
                }
            }
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    private char[] createInParamArray(final char[] whereParam, int count) {
        int pos = 0;
        char[] totalWhereParams;
        if (count == 1) {
            totalWhereParams = new char[whereParam.length * count + 1];
        } else {
            totalWhereParams = new char[whereParam.length * count + (count - 1) + 1];
        }
        for (int i = 0; i < count; i++) {
            if (i != 0) totalWhereParams[pos++] = ',';
            for (int j = 0; j < whereParam.length; j++) {
                totalWhereParams[pos++] = whereParam[j];
            }
        }
        totalWhereParams[pos] = ')';
        return totalWhereParams;
    }

    private void deleteRow(PreparedStatement ps, JdbcOID oid, String sql) {
        int uc;
        try {
            oid.setParams(ps, 1);
            uc = ps.executeUpdate();
        } catch (Exception e) {
            throw handleException(
                    "Delete failed: " + JdbcUtils.toString(e) + "\n" +
                    "Row: " + oid.toSString() + "\n" +
                    JdbcUtils.getPreparedStatementInfo(sql, ps),
                    e, true, oid);
        }
        if (uc == 0) {
            throw BindingSupportImpl.getInstance().concurrentUpdate(
                    "Row not found: " + oid.toSString() + "\n" +
                    JdbcUtils.getPreparedStatementInfo(sql, ps), oid);
        }
    }

    private String getDeleteRowSql(JdbcTable table, CharBuf s) {
        String sql = table.deleteRowSql;
        if (sql != null) return sql;
        s.clear();
        s.append("DELETE FROM ");
        s.append(table.name);
        s.append(" WHERE ");
        table.appendWherePK(s);
        return table.deleteRowSql = s.toString();
    }

    private void getDeleteRowSqlWithInList(JdbcTable table, CharBuf s) {
        s.clear();
        s.append("DELETE FROM ");
        s.append(table.name);
        s.append(" WHERE ");
        s.append(table.pkSimpleCols[0].name);
        s.append(" IN (");
    }

    /**
     * Close all open queries.
     */
    private final void closeAllQueries() {
        for (JdbcQueryResult res = queryResultHead; res != null;) {
            JdbcQueryResult n = res.prev;
            res.close();
            res.next = null;
            res.prev = null;
            if (n == null) break;
            res = n;
        }
        queryResultHead = null;
        queryResultTail = null;
    }

    private JdbcCompiledQuery compile(QueryDetails q) {
        JdbcCompiledQuery cq = null;
        int language = q.getLanguage();
        if (language == QueryDetails.LANGUAGE_EJBQL) {

            cq = new JdbcEJBQLCompiler(this).compile(q);

        } else if (language == QueryDetails.LANGUAGE_SQL) {
            ClassMetaData cmd = null;
            if (q.getCandidateClass() != null) {
                cmd = jmd.getClassMetaData(q.getCandidateClass());
            }

            //unset everything not used for sql queries
            q.setOrdering(null);
            q.setGrouping(null);
            q.setVariables(null);
            q.setCol(null);
            q.setImports(null);
            q.setResult(null);

            cq = new JdbcCompiledQuery(cmd, q);
            cq.setCacheable(false); // SQL queries not cached by default

            CmdBitSet bits = new CmdBitSet(jmd);
            if (cmd != null) {
                bits.addPlus(cmd);
            }
            int[] a = q.getExtraEvictClasses();
            if (a != null) {
                for (int i = a.length - 1; i >= 0; i--) {
                    bits.add(jmd.classes[a[i]]);
                }
            }
            cq.setFilterClsIndexs(bits.toArray());
            cq.setEvictionClassBits(bits.getBits());
            cq.setEvictionClassIndexes(bits.getIndexes());


            FetchSpec fSpec = new FetchSpec(null, getSqlDriver());
            FopSqlQuery sqlFop = new FopSqlQuery(fSpec, cmd,
                    (cmd == null ? null: cq.getFetchGroup()),
                    cq);
            fSpec.addFetchOp(sqlFop, true);
            fSpec.finish(1);
            cq.setFetchSpec(fSpec);
        } else {
            cq = new JdbcJDOQLCompiler(this).compile(q);
        }
        return cq;
    }

    public LogEventStore getPerfEventStore() {
        return pes;
    }

    private void addQueryResult(JdbcQueryResult res) {
        if (res.next != null || res.prev != null) {
            throw BindingSupportImpl.getInstance().internal(
                    "Adding a duplicate queryResult to query linked list");
        }

        res.prev = queryResultHead;
        if (queryResultHead != null) queryResultHead.next = res;
        queryResultHead = res;
        if (queryResultTail == null) queryResultTail = res;
    }

    private void removeQueryResult(JdbcQueryResult res) {
        if (res.prev != null) {
            res.prev.next = res.next;
        } else {
            queryResultTail = res.next;
        }
        if (res.next != null) {
            res.next.prev = res.prev;
        } else {
            queryResultHead = res.prev;
        }
        res.next = null;
        res.prev = null;
    }

    private void fillContainerWithAll(ApplicationContext context,
            JdbcCompiledQuery cq, Object[] params,
            QueryResultContainer container) {
        JdbcQueryResult res = null;
        if (cq.isEJBQLHack()) {

            res = new JdbcQueryResultEJBQL(this, cq, params,
                    canUseCache());

        } else {
            res = new JdbcQueryResult(this, cq, params,
                canUseCache());
        }
        res.getAllResults(context, container, forUpdateField);
    }

    private int executeCount(JdbcCompiledQuery cq, Object[] params) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = con();

            // this sync block can be removed when compiledQuery is no longer shared
            String sql;
            synchronized (cq) {
//                cq.updateSql(sqlDriver, params, false, true);
                ps = con.prepareStatement(sql = cq.getSql(sqlDriver, params, false, true, 0, 0));
                cq.setParamsOnPS(jmd, sqlDriver, ps, params, sql);
            }
            try {
                rs = ps.executeQuery();
            } catch (Exception e) {
                throw sqlDriver.mapException(e, "Count(*) query failed: " + JdbcUtils.toString(
                        e) + "\n" + JdbcUtils.getPreparedStatementInfo(sql, ps), true);
            }
            if (!rs.next()) {
                throw BindingSupportImpl.getInstance().fatalDatastore(
                        "No row returned by count(*) query:\n" +
                        JdbcUtils.getPreparedStatementInfo(sql, ps));
            }
            return rs.getInt(1);
        } catch (SQLException x) {
            handleException(x);
            return 0; // keep compiler happy
        } finally {
            cleanup(rs);
            cleanup(ps);
        }
    }

    private VersantQueryPlan executePlan(JdbcCompiledQuery cq,
            Object[] params) {
        VersantQueryPlan qp = new VersantQueryPlan();
        // this sync block can go when compiledQuery is no longer shared
        synchronized (cq) {
//            cq.updateSql(sqlDriver, params, forUpdateField, false);
            qp.setDatastoreQuery(cq.getSql(sqlDriver, params, forUpdateField,
                    false, 0, 0));
            if (params == null) { // query plans can only be done when there is no params
                try {
                    Connection con = con();
                    PreparedStatement ps;
                    String sql;
                    try {
                        sql = sqlDriver.prepareForGetQueryPlan(con, qp.getDatastoreQuery());
                        ps = con.prepareStatement(sql);
                        qp.setDatastorePlan(sqlDriver.getQueryPlan(con, ps));
                    } finally {
                        sqlDriver.cleanupForGetQueryPlan(con);
                    }
                    cq.setParamsOnPS(jmd, sqlDriver, ps, params, sql);
                } catch (SQLException e) {
                    qp.setDatastorePlan(e.getMessage());
                }
            } else {
                qp.setDatastorePlan(
                        "Query plan can only be done when there are no parameters.");
            }
        }
        return qp;
    }

    /**
     * Look for cached query results and add them to the container if there
     * are some. Returns true if results were found and false otherwise.
     */
    private boolean checkCacheForQuery(JdbcCompiledQuery cq,
            Object[] params, QueryResultContainer qContainer) {
        CachedQueryResult data = cache.getQueryResult(cq, params);
        if (data == null) {
            return false;
        }
        StatesReturned container = qContainer.container;

        // add all the results to the qContainer cloning all mutable stuff
        if (data.results != null) {
            qContainer.fillFrom(data);
            // update the container with the oid-state pairs
            if (cq.isDefaultResult()) {
                // this is a query that only contains oids
                ArrayList res = data.results;
                int n = res.size();
                for (int i = 0; i < n; i++) {
                    OID oid = (OID)res.get(i);
                    if (oid == null) break;
                    State s = cache.getState(oid, null);
                    if (s == null) {
                        cache.evict(cacheTx(), cq, params);
                        qContainer.reset();
                        return false;
                    }
                    container.add(oid, s);
                }
            } else {
                final int firstThisCol = cq.getFirstThisIndex();
                if (firstThisCol >= 0 && data.results != null) {
                    // first put in all the primary query results in correct order
                    ArrayList res = data.results;
                    int n = res.size();
                    for (int i = 0; i < n; i++) {
                        Object[] row = (Object[])res.get(i);
                        OID oid = (OID)row[firstThisCol];
                        State s = cache.getState(oid, null);
                        if (s == null) {
                            cache.evict(cacheTx(), cq, params);
                            qContainer.reset();
                            return false;
                        }
                        container.add(oid, s);
                    }
                }
            }
        }

        // process indirect oids
        if (data.indirectOIDs != null) {
            OID[] indirectOIDs = data.indirectOIDs.oids;
            int n = indirectOIDs.length;
            for (int i = 0; i < n; i++) {
                OID oid = indirectOIDs[i];
                if (oid == null) break;
                State s = cache.getState(oid, null);
                if (s == null) {
                    cache.evict(cacheTx(), cq, params);
                    container.clear();
                    qContainer.reset();
                    return false;
                }
                container.addIndirect(oid, s);
            }
        }

        qContainer.allResults = true;
        return true;
    }

    public SqlDriver getSqlDriver() {
        return sqlDriver;
    }
    
//    /**
//     * Skip over states not needed from a select. This follows the same
//     * recursive algorithm as populateStateFromSelect but does not read
//     * anything. It is usefull if some states have to be skipped but
//     * others need to be read from other columns in the ResultSet.
//     */
//    public int skipState(int firstCol, FgDs fgds) {
//        return firstCol + fgds.columnSkipCount;
//    }

    public boolean isFlushed() {
        return flushed;
    }

    public StorageCache getCache() {
        return cache;
    }

    public boolean isUseBatchInsert() {
        return useBatchInsert;
    }

    public boolean isUseBatchUpdate() {
        return useBatchUpdate;
    }

    public JdbcMetaData getJdbcMetaData() {
        return (JdbcMetaData)jmd.jdbcMetaData;
    }

    /**
     * Get all of the states for the class and others pulled in by the fg.
     */
    private void getAllStates(ApplicationContext context, ClassMetaData cmd,
            FetchGroup fg, StatesReturned all) {
        fg = fg.resolve(cmd);
        QueryDetails qd = new QueryDetails();
        qd.setBounded(true);
        qd.setCandidateClass(cmd.cls);
        qd.setFilter(null);
        qd.setFetchGroupIndex(fg.index);
        qd.updateCounts();
        JdbcCompiledQuery cq = (JdbcCompiledQuery)compileQuery(qd);

        QueryResultContainer qrc = new QueryResultContainer(all);
        qrc.init(cq);

        JdbcQueryResult res = null;
        if (cq.isEJBQLHack()) {

            res = new JdbcQueryResultEJBQL(this, cq, new Object[0],
                    canUseCache());

        } else {
            res = new JdbcQueryResult(this, cq, null, false);
        }

        res.getAllResults(context, qrc, forUpdateField);
    }

    /**
     * Add all of the cached query results to the container. This returns
     * false if any of the states are no longer in the level 2 cache.
     */
    private boolean addToContainer(JdbcCompiledQuery cq, Object[] params,
            CachedQueryResult data, QueryResultContainer qContainer) {
        if (data.results != null) {
            qContainer.fillFrom(data);
            // this is to update the container with the oid-state pairs
            if (cq.isDefaultResult()) {
                //this is a query that only contains oids
                ArrayList res = data.results;
                int n = res.size();
                for (int i = 0; i < n; i++) {
                    OID oid = (OID)res.get(i);
                    if (oid == null) break;
                    State s = cache.getState(oid, null);
                    if (s == null) {
                        cache.evict(cacheTx, cq, params);
                        qContainer.reset();
                        return false;
                    }
                    qContainer.container.add(oid, s);
                }
            } else {
                final int firstThisCol = cq.getFirstThisIndex();
                if (firstThisCol >= 0 && data.results != null) {
                    // first put in all the primary query results in correct order
                    ArrayList res = data.results;
                    int n = res.size();
                    for (int i = 0; i < n; i++) {
                        Object[] row = (Object[])res.get(i);
                        OID oid = (OID)row[firstThisCol];
                        State s = cache.getState(oid, null);
                        if (s == null) {
                            cache.evict(cacheTx, cq, params);
                            qContainer.reset();
                            return false;
                        }
                        qContainer.container.add(oid, s);
                    }
                }
            }
        }

        // process indirect oids
        if (data.indirectOIDs != null) {
            OID[] indirectOIDs = data.indirectOIDs.oids;
            int n = indirectOIDs.length;
            for (int i = 0; i < n; i++) {
                OID oid = indirectOIDs[i];
                if (oid == null) break;
                State s = cache.getState(oid, null);
                if (s == null) {
                    cache.evict(cacheTx, cq, params);
                    qContainer.container.clear();
                    qContainer.reset();
                    return false;
                }
                qContainer.container.addIndirect(oid, s);
            }
        }

        qContainer.allResults = true;
        return true;
    }

    public JdbcConnectionSource getJdbcConnectionSource() {
        return conSrc;
    }

    /**
     * Count the number of open query results.
     */
    private int getOpenQueryResultCount() {
        int c = 0;
        for (JdbcQueryResult i = queryResultTail; i != null; i = i.next, ++c);
        return c;
    }

    /**
     * Do we have a JDBC Connection?
     */
    public boolean hasDatastoreConnection() {
        return conx != null;
    }

    public Map getStatus() {
        Map m = new HashMap();
        m.put(STATUS_OPEN_QUERY_RESULT_COUNT,
                new Integer(getOpenQueryResultCount()));
        return m;
    }

    /**
     * This compares OIDs based on the referenceGraphIndex'es of their base class
     * meta data.
     */
    public class OIDRefGraphIndexComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            ClassMetaData ca = ((OID)o1).getAvailableClassMetaData();
            ClassMetaData cb = ((OID)o2).getAvailableClassMetaData();
            int diff = ca.referenceGraphIndex - cb.referenceGraphIndex;
            if (diff != 0) return diff;
            return ca.index - cb.index;
        }

    }

    /**
     * This is used to clear all non-auto set field on states that returned to
     * the client. This is done to save network traffic.
     */
    private void clearNonAutoSetFields(StatesReturned container) {
        for (Iterator i = container.iterator(); i.hasNext(); ) {
            EntrySet.Entry e = (EntrySet.Entry)i.next();
            State state = (State)e.getValue();
            if (state != null) {
                state.clearNonAutoSetFields();
            }
        }
    }

    public void setUserObject(Object o) {
        // ignore
    }

}

