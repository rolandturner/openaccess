
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

import com.versant.core.server.CompiledQuery;
import com.versant.core.metadata.*;
import com.versant.core.common.*;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.jdo.VersantQueryPlan;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.storagemanager.ApplicationContext;
import com.versant.core.storagemanager.ExecuteQueryReturn;
import com.versant.core.storagemanager.RunningQuery;
import com.versant.core.logging.LogEventStore;
import com.versant.core.util.IntArray;

import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

/**
 * Decorates another storage manager to add event logging.
 */
public final class LoggingStorageManager implements StorageManager {

    private final LoggingStorageManagerFactory smf;
    private final StorageManager sm;
    private final LogEventStore pes;
    private final ModelMetaData jmd;
    private final int id;

    private static int lastId;

    public LoggingStorageManager(LoggingStorageManagerFactory smf,
            StorageManager sm) {
        this.smf = smf;
        this.sm = sm;
        this.pes = smf.getLogEventStore();
        this.jmd = smf.getModelMetaData();
        id = ++lastId;
    }

    public StorageManager getStorageManager() {
        return sm;
    }

    public LogEventStore getPerfEventStore() {
        return pes;
    }

    private RuntimeException handleException(Throwable cause,
            StorageManagerEvent ev) {
        BindingSupportImpl
         bsi
        	= BindingSupportImpl.getInstance();
        if (ev != null) {
            ev.setErrorMsg(cause);
        }
        if (bsi.isOwnException(cause)) {
            return (RuntimeException)cause;
        } else {
            if (Debug.DEBUG) {
                cause.printStackTrace(System.out);
            }
            if (bsi.isError(cause)) {
                if (bsi.isOutOfMemoryError(cause)) {
                    return bsi.exception(cause.toString(), cause);
                }
                throw (Error)cause;
            }
            return bsi.internal(cause.toString(), cause);
        }
    }

    public void begin(boolean optimistic) {
        StorageManagerEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new SmTxEvent(id, SmTxEvent.BEGIN, optimistic));
        }
        try {
            smf.txCount++;
            sm.begin(optimistic);
        } catch (Throwable e) {
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void commit() {
        StorageManagerEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new SmTxEvent(id, SmTxEvent.COMMIT));
        }
        try {
            smf.txCommitCount++;
            sm.commit();
        } catch (Throwable e) {
            smf.txCommitErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void rollback() {
        StorageManagerEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new SmTxEvent(id, SmTxEvent.ROLLBACK));
        }
        try {
            smf.txRollbackCount++;
            sm.rollback();
        } catch (Throwable e) {
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void setConnectionPolicy(int policy) {
        sm.setConnectionPolicy(policy);
    }

    public void setLockingPolicy(int policy) {
        sm.setLockingPolicy(policy);
    }

    public int getLockingPolicy() {
        return sm.getLockingPolicy();
    }

    public void logEvent(int level, String description, int ms) {
        sm.logEvent(level, description, ms);
    }

    public StatesReturned fetch(ApplicationContext context, OID oid,
            State current, FetchGroup fetchGroup, FieldMetaData triggerField) {
        SmStatesReturnedEvent ev = null;
        if (pes.isFine()) {
            ev = createAndLogSmFetchEvent(oid, fetchGroup, triggerField);
        }
        try {
            smf.fetchCount++;
            StatesReturned ans = sm.fetch(context, oid, current, fetchGroup,
                    triggerField);
            if (ev != null) fillEventForPacket(ev, ans);
            return ans;
        } catch (Throwable e) {
            smf.fetchErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public StatesReturned fetch(ApplicationContext context, OIDArray oids,
            FieldMetaData triggerField) {
        SmStatesReturnedEvent ev = null;
        if (pes.isFine()) {
            ev = createAndLogSmFetchBulkEvent(oids, triggerField);
        }
        try {
            smf.fetchCount++;
            StatesReturned ans = sm.fetch(context, oids, triggerField);
            if (ev != null) fillEventForPacket(ev, ans);
            return ans;
        } catch (Throwable e) {
            smf.fetchErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public StatesReturned store(StatesToStore toStore, DeletePacket toDelete,
            boolean returnFieldsUpdatedBySM, int storeOption,
            boolean evictClasses) {
        SmStoreEvent ev = null;
        if (pes.isFine()) {
            ev = createAndLogSmStoreEvent(toStore, toDelete,
                    returnFieldsUpdatedBySM, storeOption, evictClasses);
        }
        try {
            switch (storeOption) {
                case STORE_OPTION_COMMIT:
                    smf.txCommitCount++;
                    break;
                case STORE_OPTION_FLUSH:
                    smf.txFlushCount++;
                    break;
            }
            StatesReturned ans = sm.store(toStore, toDelete,
                    returnFieldsUpdatedBySM, storeOption, evictClasses);
            if (ev != null) fillEventForPacket(ev, ans);
            return ans;
        } catch (Throwable e) {
            switch (storeOption) {
                case STORE_OPTION_COMMIT:
                    smf.txCommitErrorCount++;
                    break;
                case STORE_OPTION_FLUSH:
                    smf.txFlushErrorCount++;
                    break;
            }
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public OID createOID(ClassMetaData cmd) {
        return sm.createOID(cmd);
    }

    public CompiledQuery compileQuery(QueryDetails query) {
        SmQueryEvent ev = null;
        if (pes.isFiner()) {
            pes.log(ev = new SmQueryEvent(id, StorageManagerEvent.COMPILE,
                    query, null, jmd));
        }
        try {
            return sm.compileQuery(query);
        } catch (Throwable e) {
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public ExecuteQueryReturn executeQuery(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params) {
        SmQueryEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new SmQueryEvent(id, StorageManagerEvent.EXEC,
                    query, compiledQuery, jmd));
        }
        try {
            smf.queryCount++;
            return sm.executeQuery(context, query, compiledQuery, params);
        } catch (Throwable e) {
            smf.queryErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public QueryResultContainer executeQueryAll(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params) {
        SmQueryEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new SmQueryEvent(id, StorageManagerEvent.EXEC_ALL,
                    query, compiledQuery, jmd));
        }
        try {
            smf.queryCount++;
            QueryResultContainer ans = sm.executeQueryAll(context, query,
                    compiledQuery, params);
            if (ev != null) {
                fillEventForPacket(ev, ans.container);
            }
            return ans;
        } catch (Throwable e) {
            smf.queryErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public int executeQueryCount(QueryDetails query, CompiledQuery compiledQuery,
            Object[] params) {
        SmQueryEvent ev = null;
        if (pes.isFine()) {
            pes.log(ev = new SmQueryEvent(id, StorageManagerEvent.EXEC_COUNT,
                    query, compiledQuery, jmd));
        }
        try {
            smf.queryCount++;
            int ans = sm.executeQueryCount(query, compiledQuery, params);
            if (ev != null) {
                ev.setCount(ans);
            }
            return ans;
        } catch (Throwable e) {
            smf.queryErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public VersantQueryPlan getQueryPlan(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params) {
        return sm.getQueryPlan(query, compiledQuery, params);
    }

    public QueryResultContainer fetchNextQueryResult(ApplicationContext context,
            RunningQuery runningQuery, int skipAmount) {
        SmQueryEvent ev = null;
        if (pes.isFine()) {
            ev = new SmQueryEvent(id, StorageManagerEvent.FETCH_BATCH,
                    runningQuery.getQueryDetails(), null, jmd);
            ev.setSkipAmount(skipAmount);
            pes.log(ev);
        }
        try {
            smf.fetchCount++;
            QueryResultContainer ans = sm.fetchNextQueryResult(context,
                    runningQuery, skipAmount);
            if (ans != null && ev != null) {
                fillEventForPacket(ev, ans.container);
            }
            return ans;
        } catch (Throwable e) {
            smf.fetchErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public QueryResultContainer fetchRandomAccessQueryResult(
            ApplicationContext context, RunningQuery runningQuery, int index,
            int fetchAmount) {
        SmQueryEvent ev = null;
        if (pes.isFine()) {
            ev = new SmQueryEvent(id, StorageManagerEvent.FETCH_INDEX,
                    runningQuery.getQueryDetails(), null, jmd);
            ev.setIndex(index);
            ev.setFetchAmount(fetchAmount);
            pes.log(ev);
        }
        try {
            smf.fetchCount++;
            QueryResultContainer ans = sm.fetchRandomAccessQueryResult(context,
                    runningQuery, index, fetchAmount);
            if (ev != null) {
                fillEventForPacket(ev, ans.container);
            }
            return ans;
        } catch (Throwable e) {
            smf.fetchErrorCount++;
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public int getRandomAccessQueryCount(ApplicationContext context,
            RunningQuery runningQuery) {
        SmQueryEvent ev = null;
        if (pes.isFine()) {
            ev = new SmQueryEvent(id, StorageManagerEvent.FETCH_COUNT,
                    runningQuery.getQueryDetails(), null, jmd);
            pes.log(ev);
        }
        try {
            int ans = sm.getRandomAccessQueryCount(context, runningQuery);
            if (ev != null) {
                ev.setCount(ans);
            }
            return ans;
        } catch (Throwable e) {
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public void closeQuery(RunningQuery runningQuery) {
        SmQueryEvent ev = null;
        if (pes.isFiner()) {
            ev = new SmQueryEvent(id, StorageManagerEvent.QUERY_CLOSE,
                    runningQuery.getQueryDetails(), null, jmd);
            pes.log(ev);
        }
        try {
            sm.closeQuery(runningQuery);
        } catch (Throwable e) {
            throw handleException(e, ev);
        } finally {
            if (ev != null) ev.updateTotalMs();
        }
    }

    public Object getDatastoreConnection() {
        return sm.getDatastoreConnection();
    }

    public boolean isNotifyDirty() {
        return sm.isNotifyDirty();
    }

    public void notifyDirty(OID oid) {
        sm.notifyDirty(oid);
    }

    public void reset() {
        sm.reset();
    }

    public void destroy() {
        sm.destroy();
    }

    public StorageManager getInnerStorageManager() {
        return sm;
    }

    public boolean hasDatastoreConnection() {
        return sm.hasDatastoreConnection();
    }

    public Map getStatus() {
        return sm.getStatus();
    }

    public void setUserObject(Object o) {
        sm.setUserObject(o);
    }

    private SmFetchEvent createAndLogSmFetchEvent(OID oid,
            FetchGroup fetchGroup, FieldMetaData triggerField) {
        ClassMetaData cmd = oid.getAvailableClassMetaData();
        String fieldName;
        if (triggerField == null) {
            fieldName = null;
        } else {
            if (triggerField.classMetaData != cmd) {
                fieldName = triggerField.getQName();
            } else {
                fieldName = triggerField.name;
            }
        }
        SmFetchEvent ev = new SmFetchEvent(id, StorageManagerEvent.FETCH,
                cmd == null ? null : cmd.qname,
                oid.toPkString(),
                fetchGroup == null ? null : fetchGroup.name,
                fieldName);
        pes.log(ev);
        return ev;
    }

    private SmFetchBulkEvent createAndLogSmFetchBulkEvent(OIDArray oids,
            FieldMetaData triggerField) {
        int n = oids.size();
        SmFetchBulkEvent ev = new SmFetchBulkEvent(id,
                StorageManagerEvent.FETCH_BULK, n, FetchGroup.DFG_NAME,
                triggerField.getQName());
        if (pes.isFiner()) {
            OID[] a = oids.oids;
            String[] strings = new String[n];
            boolean found[] = new boolean[jmd.classes.length];
            IntArray classIds = new IntArray();
            ArrayList cnames = new ArrayList();
            for (int i = 0; i < n; i++) {
                OID oid = a[i];
                strings[i] = oid.toStringImp();
                int classIndex = oid.getClassIndex();
                if (classIndex >= 0 && !found[classIndex]) {
                    found[classIndex] = true;
                    ClassMetaData cmd = jmd.classes[classIndex];
                    classIds.add(cmd.classId);
                    cnames.add(cmd.qname);
                }
            }
            ev.setOids(strings);
            String[] sa = new String[cnames.size()];
            cnames.toArray(sa);
            ev.setLookupClasses(classIds.toArray(), sa);
        }
        pes.log(ev);
        return ev;
    }

    private void fillEventForPacket(SmStatesReturnedEvent ev,
            StatesReturned container) {
        int n = container.size();
        ev.setReturnedSize(n);
        if (pes.isFiner()) {
            ev.setReturnedOIDs(convertOidsToStrings(
                    container.iteratorForOIDs(), n, ev));
        }
    }

    private String[] convertOidsToStrings(Iterator iter, int size,
            SmStatesReturnedEvent ev) {
        String[] oids = new String[size];
        boolean found[] = new boolean[jmd.classes.length];
        IntArray classIds = new IntArray();
        ArrayList cnames = new ArrayList();
        if (ev.getLookupClassIDs() != null) {
            int[] a = ev.getLookupClassIDs();
            String[] sa = ev.getLookupClassNames();
            for (int i = 0; i < a.length; i++) {
                found[jmd.getClassMetaData(a[i]).index] = true;
                classIds.add(a[i]);
                cnames.add(sa[i]);
            }
        }
        int count = 0;
        for (; iter.hasNext(); ) {
            OID oid = (OID)iter.next();
            oids[count++] = oid.toStringImp();
            int classIndex = oid.getClassIndex();
            if (classIndex >= 0 && !found[classIndex]) {
                found[classIndex] = true;
                ClassMetaData cmd = jmd.classes[classIndex];
                classIds.add(cmd.classId);
                cnames.add(cmd.qname);
            }
        }
        String[] ca = new String[cnames.size()];
        cnames.toArray(ca);
        ev.setLookupClasses(classIds.toArray(), ca);
        return oids;
    }

    private SmStoreEvent createAndLogSmStoreEvent(
            StatesToStore toStore, DeletePacket toDelete,
            boolean returnFieldsUpdatedBySM, int storeOption,
            boolean evictClasses) {
        SmStoreEvent ev = new SmStoreEvent(id, returnFieldsUpdatedBySM,
                storeOption, evictClasses);
        int n = toStore == null ? 0 : toStore.size();
        ev.setToStoreSize(n);
        if (pes.isFiner() && toStore != null) {
            ev.setToStoreOIDs(convertOidsToStrings(toStore.iteratorForOIDs(), n, ev));
        }
        n = toDelete == null ? 0 : toDelete.size();
        ev.setToDeleteSize(n);
        if (pes.isFiner() && toDelete != null) {
            ev.setToDeleteOIDs(convertOidsToStrings(toDelete.iterator(), n, ev));
        }
        pes.log(ev);
        return ev;
    }

}
