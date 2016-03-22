
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
package com.versant.core.vds;

import com.versant.core.storagemanager.*;
import com.versant.core.logging.LogEventStore;
import com.versant.core.common.*;
import com.versant.core.metadata.*;
import com.versant.core.server.CompiledQuery;
import com.versant.core.server.CachedQueryResult;
import com.versant.core.server.CompiledQueryCache;
import com.versant.core.jdo.query.ParamNode;
import com.versant.core.jdo.query.QueryParser;
import com.versant.core.jdo.query.MethodNode;
import com.versant.core.jdo.ServerLogEvent;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.jdo.VersantQueryPlan;
import com.versant.core.vds.util.Loid;
import com.versant.core.vds.logging.VdsLockEvent;
import com.versant.core.vds.logging.VdsGroupWriteEvent;
import com.versant.odbms.*;
import com.versant.odbms.query.DatastoreQuery;
import com.versant.odbms.query.Expression;
import com.versant.odbms.query.OrderByExpression;
import com.versant.odbms.model.DatastoreObject;
import com.versant.odbms.model.UserSchemaClass;
import java.util.*;

/**
 * StorageManager implementation for Versant ODBMS.
 */
public class VdsStorageManager implements StorageManager {

    private final ModelMetaData jmd;
    private final StorageCache cache;
    private final CompiledQueryCache compiledQueryCache;
    private final LogEventStore pes;
    private final VdsConnectionPool pool;
    private final VdsConfig vdsConfig;
    private final NamingPolicy namingPolicy;

    private int lockPolicy;
    private int conPolicy;

    private VdsConnection conx;
    private Object cacheTx;

    private boolean txActive;
    private boolean optimistic;
    private boolean pinned;     // do not release conx until end of tx
    private boolean flushed;    // updates done without commit on conx so do
                                // not use the cache even if optimistic

    private VdsQueryResult queryResultHead;
    private VdsQueryResult queryResultTail;

    private boolean[] changedClassesFlag;
    private ClassMetaData[] changedClasses;
    private int changedClassCount;

    public static final String STATUS_OPEN_QUERY_RESULT_COUNT =
            "openQueryResultCount";

    public VdsStorageManager(ModelMetaData jmd, StorageCache cache,
            CompiledQueryCache compiledQueryCache, LogEventStore pes,
            VdsConfig vdsConfig, VdsConnectionPool connectionPool,
            NamingPolicy namingPolicy) {
        this.jmd = jmd;
        this.cache = cache;
        this.compiledQueryCache = compiledQueryCache;
        this.pes = pes;
        this.pool = connectionPool;
        this.vdsConfig = vdsConfig;
        this.namingPolicy = namingPolicy;
        conPolicy = CON_POLICY_RELEASE;
        lockPolicy = LOCK_POLICY_NONE;
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
                        pool.returnConnection(conx);
                        conx = null;
                    }
                } catch (DatastoreException e) {
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
    }

    public int getLockingPolicy() {
        return lockPolicy;
    }

    public StatesReturned fetch(ApplicationContext context, OID oid,
            State current, FetchGroup fetchGroup, FieldMetaData triggerField) {
        try {
            StatesReturned container = new StatesReturned(context);
            if (canUseCache()) {
                State s = cache.getState(oid, fetchGroup);
                if (s == null) {
                    ClassMetaData base = oid.getBaseClassMetaData();
                    if (null != base && base.cacheStrategy == MDStatics.CACHE_STRATEGY_ALL
                            && !base.cacheStrategyAllDone) {
                        base.cacheStrategyAllDone = true;
                        StatesReturned all = new StatesReturned(
                                DummyApplicationContext.INSTANCE);
                        getAllStates(base, base.fetchGroups[0], container.next = all);
                        s = all.get(oid);
                        if (s != null && !s.containsFetchGroup(fetchGroup)) {
                            s = null;
                        }
                    }
                }
                // todo fetch related objects from cache if s came from cache
                if (s == null) {
                    getState(oid, current, fetchGroup, container);
                } else {
                    container.add(oid, s);
                }
            } else {
                getState(oid, current, fetchGroup, container);
            }
            finishRead(container);
            return container;
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    private void getState(OID oid, State current, FetchGroup fetchGroup,
            StatesReturned container) {
        StateFetchQueue sfq = new StateFetchQueue(jmd, container, con(),
                getReadLock(), getReadOption(), pes);
        try {
            State s = sfq.fetch(oid, current, fetchGroup);
            container.add(oid, s);
        } catch (DatastoreException ex) {
            throw BindingSupportImpl.getInstance().datastore("Failed to read object on " +
                    Loid.asString(oid.getLongPrimaryKey()), ex);
        }
    }

    /**
     * Get all of the states for the class and others pulled in by the fg.
     */
    private void getAllStates(ClassMetaData cmd, FetchGroup fg,
            StatesReturned all) {
        fg = fg.resolve(cmd);
        QueryDetails qd = new QueryDetails();
        qd.setBounded(true);
        qd.setCandidateClass(cmd.cls);
        qd.setFilter(null);
        qd.setFetchGroupIndex(fg.index);
        qd.updateCounts();
        VdsQueryResult res = (VdsQueryResult)executeQueryImp(qd, null, null);
        QueryResultContainer qrc = new QueryResultContainer(all);
        qrc.init(res.getCompiledQuery());
        if (res.next(0)) {
            res.addNextResult(0, qrc, -1);
        }
        finishRead(qrc.container);
    }

    public StatesReturned fetch(ApplicationContext context, OIDArray oids,
            FieldMetaData triggerField) {
        try {
            StatesReturned container = new StatesReturned(context);
            StateFetchQueue sfq = new StateFetchQueue(jmd, container, con(),
                    getReadLock(), getReadOption(), pes);
            int n = oids.size();
            if (canUseCache()) {
                for (int i = 0; i < n; i++) {
                    OID oid = oids.oids[i];
                    ClassMetaData cmd = oid.getAvailableClassMetaData();
                    FetchGroup fetchGroup = null;
					if ( cmd != null )
						fetchGroup = cmd.fetchGroups[0];
                    State s = cache.getState(oid, fetchGroup);
                    if (s == null) {
                        sfq.add(oid, fetchGroup);
                    } else {
                        container.add(oid, s);
                    }
                }
            } else {
                for (int i = 0; i < n; i++) {
                    OID oid = oids.oids[i];
                    ClassMetaData cmd = oid.getAvailableClassMetaData();
					if ( cmd != null )
						sfq.add(oid, cmd.fetchGroups[0]);
                }
            }
            try {
                sfq.fetch();
            } catch (DatastoreException ex) {
                throw BindingSupportImpl.getInstance().datastore(ex.toString(), ex);
            }
            finishRead(container);
            return container;
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    public StatesReturned store(StatesToStore toStore, DeletePacket toDelete,
            boolean returnFieldsUpdatedBySM, int storeOption,
            boolean evictClasses) {
        checkActiveTx();
        if (storeOption == STORE_OPTION_FLUSH) {
            // make sure open queries will not be cached if this is a flush
            for (VdsQueryResult qrw = queryResultHead; qrw != null; qrw = qrw.prev) {
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
        checkApplicationIdentitySupport(cmd);
        try {
            long loid = con().getNewLoid();
            OID oid = cmd.createOID(true);
            oid.setLongPrimaryKey(loid);
            finishRead();
            return oid;
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    public CompiledQuery compileQuery(QueryDetails query) {
        return compileQueryImp(query);
    }

    private VdsCompiledQuery compileQueryImp(QueryDetails query) {
        VdsCompiledQuery cq = (VdsCompiledQuery)compiledQueryCache.get(query);
        if (cq == null) {
            VersantQueryCompiler qcompiler = new VersantQueryCompiler(pes,
               vdsConfig, jmd);
            cq = qcompiler.compile(query);
            cq = (VdsCompiledQuery)compiledQueryCache.add(cq);
        }
        return cq;
    }

    public ExecuteQueryReturn executeQuery(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params) {
        try {
            ExecuteQueryReturn ans = executeQueryImp(query, compiledQuery, params);
            finishRead();
            return ans;
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    private ExecuteQueryReturn executeQueryImp(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        VdsCompiledQuery cq = (VdsCompiledQuery)compiledQuery;
        if (cq == null) {
            cq = compileQueryImp(query);
        }
        ClassMetaData cmd = jmd.classes[cq.getClassIndex()];
        String vdsClassName = namingPolicy.mapClassName(cmd);
        DatastoreQuery dq = new DatastoreQuery(vdsClassName);
        dq.setSubClasses(cq.getQueryDetails().includeSubClasses());
        Expression expr = ((VdsCompiledQuery)cq).getExpr();
        OrderByExpression[] orderExpr = ((VdsCompiledQuery)cq).getOrderBy();
        dq.setExpression(expr);
        dq.setOrderByExpression(orderExpr);
        if (expr != null) {
            if (cq.getParams() != null) {
                Map pvalues = getParameterValues(cq.getParams(), params,
                        cq.queryParser);
                dq.setParameters(pvalues);
            }
        }
        VdsQueryResult res = new VdsQueryResult(cq, cmd, this, dq, canUseCache());
        List result = con().executeQuery(dq, LockMode.IRLOCK, LockMode.RLOCK, 0);
        long[] loids = toLoidArray(result);
        res.setLoids(loids);
        addQueryResult(res);
        return res;
    }

    public QueryResultContainer executeQueryAll(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params) {
        try {
            VdsQueryResult res = (VdsQueryResult)executeQueryImp(query,
                    compiledQuery, params);
            if (compiledQuery == null) {
                compiledQuery = res.getCompiledQuery();
            }
            QueryResultContainer container = new QueryResultContainer(context,
                    compiledQuery);
            if (res.next(0)) {
                res.addNextResult(0, container, -1);
            }
            finishRead(container.container);
            return container;
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    public int executeQueryCount(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        try {
            VdsQueryResult res = (VdsQueryResult)executeQueryImp(
                    query, compiledQuery, params);
            finishRead();
            return res.getResultCount();
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    public VersantQueryPlan getQueryPlan(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        throw BindingSupportImpl.getInstance().notImplemented("");
    }

    public QueryResultContainer fetchNextQueryResult(
            ApplicationContext context, RunningQuery runningQuery,
            int skipAmount) {
        try {
            VdsQueryResult res = (VdsQueryResult)runningQuery;
            VdsCompiledQuery cq = (VdsCompiledQuery)res.getCompiledQuery();
            QueryResultContainer container = new QueryResultContainer(context,
                    cq);
            if (!res.addNextResult(skipAmount, container, cq.getQueryResultBatchSize())) {
                removeQueryResult(res);
            }
            finishRead(container.container);
            return container;
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    public QueryResultContainer fetchRandomAccessQueryResult(
            ApplicationContext context, RunningQuery runningQuery, int index,
            int fetchAmount) {
        try {
            VdsQueryResult res = (VdsQueryResult)runningQuery;
            VdsCompiledQuery cq = (VdsCompiledQuery)res.getCompiledQuery();
            QueryResultContainer container = new QueryResultContainer(context,
                    cq);
            if (!res.absolute(index)
                    || !res.addNextResult(0, container, fetchAmount)) {
                removeQueryResult(res);
            }
            finishRead(container.container);
            return container;
        } catch (Throwable e) {
            finishFailedRead();
            throw handleException(e);
        }
    }

    public int getRandomAccessQueryCount(ApplicationContext context,
            RunningQuery runningQuery) {
        VdsQueryResult res = (VdsQueryResult)runningQuery;
        return res.getResultCount();
    }

    public void closeQuery(RunningQuery runningQuery) {
        VdsQueryResult res = (VdsQueryResult)runningQuery;
        removeQueryResult(res);
    }

    public Object getDatastoreConnection() {
        return null;
    }

    public boolean isNotifyDirty() {
        return txActive && !optimistic;
    }

    public void notifyDirty(OID oid) {
        if (Debug.DEBUG) {
            System.out.println("%%% VdsStorageManager.notifyDirty " + oid);
        }
        long loid = oid.getLongPrimaryKey();
        VdsLockEvent ev;
        if (pes.isFine()) {
            ev = new VdsLockEvent(0, LockMode.WLOCK, loid);
            pes.log(ev);
        } else {
            ev = null;
        }
        try {
            DatastoreObject dso = new DatastoreObject(loid);
            con().lockObject(dso, LockMode.WLOCK, Options.NO_OPTIONS);
        } catch (DatastoreException ex) {
            if (ev != null) {
                ev.setErrorMsg(ex);
            }
            throw BindingSupportImpl.getInstance().datastore(
                    "Failed to obtain WRITE LOCK on " +
                    Loid.asString(oid.getLongPrimaryKey()) + " of " +
                    oid.getBaseClassMetaData().qname,
                    ex);
        } finally {
            if (ev != null) {
                ev.updateTotalMs();
            }
        }
    }

    public void reset() {
        resetImp();
        lockPolicy = LOCK_POLICY_NONE;
        conPolicy = CON_POLICY_PIN_FOR_TX;
        clearChangedClasses();
    }

    public void destroy() {
        resetImp();
    }

    private void resetImp() {
        conPolicy = CON_POLICY_RELEASE;
        try {
            rollbackImp(true);
        } catch (Exception e) {
            // ignore
        }
        finishFailedRead();
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
     * Get our database connection. This will allocate one if we currently
     * have none. It will also start a cache transaction if there is none.
     */
    public DatastoreManager con() {
        if (conx == null) {
            cacheTx();

            try {
                conx = pool.getConnection(false);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.toString(), e);
            }
        }
        return conx.getCon();
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
     * Gets read option based on current mode of transaction (PESSIMISTIC or
     * OPTIMISTIC)
     */
    public short getReadOption() {
        return (optimistic)
                ? Options.DOWNGRADE_LOCKS_OPTION
                : Options.NO_OPTIONS;
    }

    /**
     * Get the lock mode based on the current state of the transaction.
     */
    public LockMode getReadLock() {
        return LockMode.RLOCK;
    }

    /**
     * Get the flush option based on the current state of the transaction.
     */
    public short getFlushOption() {
        return (optimistic)
                ? Options.CHECK_TIMESTAMPS_OPTION
                : Options.NO_OPTIONS;
    }

    private void checkActiveTx() {
        if (!txActive) {
            throw BindingSupportImpl.getInstance().internal(
                    "no active transaction");
        }
    }

    /**
     * Close all open queries.
     */
    private final void closeAllQueries() {
        for (VdsQueryResult res = queryResultHead; res != null;) {
            VdsQueryResult n = res.prev;
            res.close();
            res.next = null;
            res.prev = null;
            if (n == null) break;
            res = n;
        }
        queryResultHead = null;
        queryResultTail = null;
    }

    private void addQueryResult(VdsQueryResult res) {
        if (res.next != null || res.prev != null) {
            throw BindingSupportImpl.getInstance().internal(
                    "Adding a duplicate queryResult to query linked list");
        }

        res.prev = queryResultHead;
        if (queryResultHead != null) queryResultHead.next = res;
        queryResultHead = res;
        if (queryResultTail == null) queryResultTail = res;
    }

    private void removeQueryResult(VdsQueryResult res) {
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

    private void doUpdates(StatesToStore toStore, StatesReturned container, boolean returnFieldsUpdatedBySM) {
        int n = toStore.size();
        OID[] oids = toStore.oids;
        State[] states = toStore.states;
        State[] origStates = toStore.origStates;
        DatastoreManager dsi = con();

        // make sure all new instances have LOIDs and autoset fields are
        // updated
        Date now = new Date();
        for (int i = 0; i < n; i++) {
            OID oid = oids[i];
            ClassMetaData cmd = oid.getClassMetaData();
            if (oid.isNew() && ((NewObjectOID)oid).realOID == null) {
                checkApplicationIdentitySupport(cmd);
                OID realOID = cmd.createOID(true);
                ((NewObjectOID)oid).realOID = realOID;
                realOID.setLongPrimaryKey(dsi.getNewLoid());
            }
            State ns = states[i];
            if (ns != null) {
                ns.setClassMetaData(oid.getClassMetaData());
                if (oid.isNew()) {
                    ns.updateAutoSetFieldsCreated(now);
                } else {
                    ns.updateAutoSetFieldsModified(now, origStates[i]);
                }
            }
        }

        // persist all of the secondary fields (collections etc.) so that
        // the fake fields holding LOIDs will be filled in correctly
        DSOList toPersistSCOs = new DSOList(n, false);
        for (int si = 0; si < n;) {
            ((VdsState)states[si++]).writeSecondaryFieldsToDSOList(dsi, toPersistSCOs);
        }

        // persist all of the primary fields
        DSOList toPersist = new DSOList(n, false);
        for (int si = 0; si < n; si++) {
            OID oid = oids[si];
            ClassMetaData cmd = oid.getClassMetaData();
            checkApplicationIdentitySupport(cmd);
            DatastoreObject dso = ((UserSchemaClass)cmd.storeClass).getInstance(dsi,
                    dsi.getDefaultDatastore(), oid.getLongPrimaryKey(),
                    oid.isNew());
            State state = states[si];
            ((VdsState)state).writePrimaryFieldsToDSO(dso, dsi);
            int ts = state.getIntField(
                    cmd.optimisticLockingField.stateFieldNo);
            dso.setTimestamp(ts);
            toPersist.add(dso);

            // increase timestamp (data[i]) in State so that the timestamp
            // in State is in synch with the timestamp in db.
            state.setInternalIntField(cmd.optimisticLockingField.stateFieldNo,
               (ts + 1) & 0x7FFFFFFF);

            // return fields updated by us to the client if required
            if (returnFieldsUpdatedBySM && cmd.hasAutoSetFields) {
                container.add(oid, state);
            } else if (oid.isNew()) {
                container.add(oid, null);
            }
        }

        groupWriteObjects(dsi, "PC Objects\n", toPersist.trim(),
                getFlushOption());

        // Persist the SCOs without opt lock checking. This is fine for
        // JDO Genie apps as the version of the owning instance is always
        // updated if only an SCO is modified but is not safe if other
        // apps are modifying the same SCOs.
        if (toPersistSCOs.size() > 0) {
            groupWriteObjects(dsi, "SCO Objects\n", toPersistSCOs.trim(),
                    Options.NO_OPTIONS);
        }
    }

    /**
     * Read a group of objects logging an event if required. The loids are
     * assumed to be at position pos in loids if an event is logged.
     */
    private GroupOperationResult groupWriteObjects(DatastoreManager dsi,
            String msg, DatastoreObject[] dsoBatch, short options) {
        VdsGroupWriteEvent ev;
        if (pes.isFine()) {
            final int n = dsoBatch.length;
            long[] loids = new long[n];
            for (int i = 0; i < n; i++) {
                loids[i] = dsoBatch[i].getLOID();
            }
            ev = new VdsGroupWriteEvent(loids, options);
            pes.log(ev);
        } else {
            ev = null;
        }
        try {
            GroupOperationResult res = dsi.groupWriteObjects(dsoBatch, options);
            if (ev != null && res != null) {
                ev.setNotFound(res.objectsNotFound);
                ev.setWriteFailed(res.objects);
            }
            processGroupWriteResult(msg, res);
            return res;
        } catch (RuntimeException e) {
            if (ev != null) ev.setErrorMsg(e);
            throw e;
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    private void checkApplicationIdentitySupport(ClassMetaData cmd) {
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            throw BindingSupportImpl.getInstance().unsupported(
                    cmd + " using unsupported Application Identity");
        }
    }

    private void processGroupWriteResult(String msg, GroupOperationResult res) {
        if (res == null) return;
        StringBuffer tmp = new StringBuffer(msg);
        if (res.objects !=null && res.objects.length > 0) {
        	tmp.append("Database write failed on:\n");
	        for (int i = 0; i < res.objects.length; i++) {
	            tmp.append("  " + Loid.asString(res.objects[i]) + "\n");
	        }
        }
        if (res.objectsNotFound!=null && res.objectsNotFound.length > 0) {
            tmp.append("Not found in database:\n");
	        for (int i = 0; i < res.objectsNotFound.length; i++) {
	            tmp.append("  " + Loid.asString(res.objectsNotFound[i]) + "\n");
	        }
        }
        if (optimistic) {
            throw BindingSupportImpl.getInstance().optimisticVerification(
                    tmp.toString(), null);
        } else {
            throw BindingSupportImpl.getInstance().datastore(tmp.toString());
        }
    }

    private void doDeletes(DeletePacket toDelete) {
        DatastoreManager con = con();

        ArrayList dList = new ArrayList(toDelete.size());

        OID[] oids = toDelete.oids;
        State[] states = toDelete.states;
        ArrayList oidList = new ArrayList();
        ArrayList stateList = new ArrayList();

        for (int i = 0; i < oids.length; i++) {
            if (oids[i] == null) continue;
            if (states[i] != null) {
            	stateList.add(states[i]);
            } else {
            	oidList.add(oids[i]);
            }
        }

        DatastoreObject[] primaryDSO = new DatastoreObject[oidList.size()];
        OID[] primaryOID = new OID[oidList.size()];
        for (int i = 0; i < oidList.size(); i++) {
        	OID oid = (OID)oidList.get(i);
        	primaryDSO[i] = new DatastoreObject(oid.getLongPrimaryKey());
        	primaryOID[i] = oid;
        }

        if (primaryDSO.length != 0) {
        	GroupOperationResult res =
                    con.groupReadObjects(primaryDSO, LockMode.WLOCK,
                    		Options.NO_OPTIONS);
        	if (res != null) {
        		// TODO
        	}
        }

        for (int i = 0; i < primaryDSO.length; i++) {
        	// TODO: add to delete list without creating state
        	VdsState state = (VdsState)primaryOID[i].getClassMetaData().createState();
        	state.readPrimaryFieldsFromDSO(primaryDSO[i]);
        	state.deleteSecondaryFields(dList);
        }

		for (int i = 0; i < stateList.size(); i++) {
			VdsState state = (VdsState)stateList.get(i);
			state.deleteSecondaryFields(dList);
		}

		for (int i = 0; i < oids.length; i++) {
            OID oid = oids[i];
            if (oid == null) continue;
            if (Debug.DEBUG) {
                System.out.println("%%% deletePass1 " + oid + " state " +
                        toDelete.states[i]);
            }
            DatastoreObject dso = new DatastoreObject(oid.getLongPrimaryKey());
            dso.setIsDeleted(true);
            dList.add(dso);
        }

        DatastoreObject[] a = new DatastoreObject[dList.size()];
        dList.toArray(a);
        con.groupDeleteObjects(a, Options.NO_OPTIONS);
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    public RuntimeException handleException(Throwable e) {
		return handleException(e.toString(), e);
    }

    /**
     * Wrap an exception appropriately and return one to be thrown.
     */
    public RuntimeException handleException(String msg, Throwable e) {
        BindingSupportImpl bsi = BindingSupportImpl.getInstance();
        if (bsi.isError(e) && !bsi.isOutOfMemoryError(e)) {
            throw (Error)e;
        }
        if (bsi.isOwnException(e)) {
            return (RuntimeException)e;
        }
        return bsi.datastore(msg, e);
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

    public LogEventStore getPerfEventStore() {
        return pes;
    }

    private Map getParameterValues(ParamNode[] params, Object[] values,
            QueryParser qparser) {
        Map pvalues = new HashMap();
        if (Debug.DEBUG) {
            Debug.assertIllegalArgument(params.length == values.length,
                    "params.length != values.length");
        }

        for (int i = 0; i < params.length; i++) {
            Class pcls = params[i].getCls();
            if (pcls == null) {
                pcls = qparser.resolveParamType(params[i].getType());
            }
            params[i].setCls(pcls);
            getParameterValue(pvalues, params[i], values[i]);
        }

        return pvalues;
    }

    private void getParameterValue(Map values, ParamNode param, Object value) {
        Class type = param.getCls();
        String name = param.getIdentifier();

        if (jmd.getClassMetaData(type) != null || value instanceof VdsGenericOID) {
            // For PC type
            if (value != null) {
                value = getLoidFromPC(value);
            }
        }
        else if (Collection.class.isAssignableFrom(type)) {
        	if ((param.parent instanceof MethodNode &&
        			((MethodNode)param.parent).getMethod() ==
        				MethodNode.IS_EMPTY)) {
        		name = param.getIdentifier() + "_isempty";
        		if (value == null) {
        			value = new Boolean(true);
        		} else {
        			value = new Boolean(((Collection)value).isEmpty());
        		}
        	}
        	else {
        		Collection col = (Collection)value; 
        		if (!col.isEmpty()) {
        			ArrayList col2 = new ArrayList(col.size());
        			Iterator iter = col.iterator();
        			Object elem = iter.next();
        			if (elem instanceof OID) {
        				DatastoreObject dso;
        				for ( ; ; ) {
        					dso = getLoidFromPC(elem);
        					col2.add(dso);
        					if (!iter.hasNext())
        						break;
        					elem = iter.next();
        				}
        				value = col2;
        			}
        		}
        	}
        }
        
        if (value == null) {
            // For primitive null value
            if (type.isPrimitive()) {
                if (type == boolean.class) {
                    value = new Boolean(false);
                } else if (type == char.class) {
                    value = new Character((char)0);
                } else if (type == byte.class) {
                    value = new Byte((byte)0);
                } else if (type == short.class) {
                    value = new Short((short)0);
                } else if (type == int.class) {
                    value = new Integer(0);
                } else if (type == long.class) {
                    value = new Long((long)0);
                } else if (type == float.class) {
                    value = new Float((float)0.0);
                } else if (type == double.class) {
                    value = new Double((double)0.0);
                } else {
                    if (Debug.DEBUG) {
                        Debug.assertInternal(false,
                                "Unknown type " + type);
                    }
                }
            }
        }

        values.put(name, value);
    }

    private DatastoreObject getLoidFromPC(Object value) {
        if (Debug.DEBUG) {
            Debug.assertInternal(value instanceof OID,
                    "value is not a instanceof OID");
        }
        long loid = ((OID)value).getLongPrimaryKey();
        return new DatastoreObject(loid);
    }

    private long[] toLoidArray(List/*<DatastoreObject>*/ result) {
        Object[] dataObjects = result.toArray();
        HashSet uniqueLoids = new HashSet();
        ArrayList orderedUniqueLoids = new ArrayList();
        for (int i = 0; i < dataObjects.length; i++) {
        	Long loid = new Long(((DatastoreObject)dataObjects[i]).getLOID());
        	if (uniqueLoids.add(loid)) {
        		orderedUniqueLoids.add(loid);
        	}
        }
        long[] loids = new long[orderedUniqueLoids.size()];
        for (int i = 0; i < loids.length; i++) {
        	loids[i] = ((Long)orderedUniqueLoids.get(i)).longValue();
        }
        return loids;
    }

    public StorageCache getCache() {
        return cache;
    }

    public boolean isFlushed() {
        return flushed;
    }

    public boolean isTxActive() {
        return txActive;
    }

    /**
     * This method must be called at the end of all top level read operations
     * to maybe commit and maybe release the database connection (depending on
     * connection policy). It will add the data in container to the cache
     * and also add the query data. Either container or queryData or both
     * may be null.
     */
    private void finishRead(StatesReturned container, VdsCompiledQuery cq,
            Object[] params, CachedQueryResult queryData, int queryResultCount) {
        if (conx == null) {
            // no connection so all data must have come
            // from the level 2 cache so we have nothing to do
            return;
        }
        if (Debug.DEBUG) {
//            if (container != null) {
//                System.out.println("%%% VdsStorageManager.finishRead");
//                container.dump();
//            }
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
                pool.returnConnection(conx);
                conx = null;
            }
            ok = true;
        } catch (DatastoreException e) {
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
                        pool.returnConnection(conx);
                    } catch (DatastoreException e) {
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
        try {
            if (commit) {
                conx.commit();
            }
            if (conPolicy != CON_POLICY_PIN) {
                pool.returnConnection(conx);
                conx = null;
            }
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().datastore(e.toString(), e);
        }
    }

    /**
     * Can data be retrieved from or stored in the cache given our current
     * state? This is true if no flush has been done and the current tx is
     * optimistic or there is no active tx.
     */
    private boolean canUseCache() {
        return optimistic && !flushed || !txActive;
    }

    /**
     * Count the number of open query results.
     */
    public int getOpenQueryResultCount() {
        int c = 0;
        for (VdsQueryResult i = queryResultTail; i != null; i = i.next, ++c);
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


