
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
import com.versant.core.server.CompiledQuery;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.jdo.VersantQueryPlan;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.FetchGroup;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.OID;
import com.versant.core.common.OIDArray;
import com.versant.core.common.*;

import java.util.Map;

/**
 * This keeps track of server side transaction and other information for a
 * application level session (e.g. a PersistenceManager for JDO) and provides
 * persistence services for {@link State} instances.
 *
 * These methods are designed to be used from an application API layer
 * (e.g. JDO PersistenceManager) that may be in a different tier.
 *
 * All calls from the application API layer go through this interface. For
 * example there are methods to return results from queries that could have
 * been implemented via a separate "server side query result" interface.
 *
 * The query related methods need a lot more refactoring but one thing at
 * a time ...
 */
public interface StorageManager {

    /** Release the datastore connection as quickly as possible. */
    public static final int CON_POLICY_RELEASE = 1;
    /** Pin the datastore connection for the duration of the tx. */
    public static final int CON_POLICY_PIN_FOR_TX = 2;
    /** Pin the datastore connection indefinitely (even between tx's). */
    public static final int CON_POLICY_PIN = 3;

    /** Do not lock any objects in datastore tx. */
    public static final int LOCK_POLICY_NONE = 1;
    /** Lock only the first object navigated or fetched in datastore tx. */
    public static final int LOCK_POLICY_FIRST = 2;
    /** Lock all objects in datastore tx. */
    public static final int LOCK_POLICY_ALL = 3;

    /** Send changes to the datastore and do not commit. */
    public static final int STORE_OPTION_FLUSH = 1;
    /** Call prepareForCommit after store. */
    public static final int STORE_OPTION_PREPARE = 2;
    /** Call commit after store. */
    public static final int STORE_OPTION_COMMIT = 3;

    public static final int EVENT_ERRORS = 1;
    public static final int EVENT_NORMAL = 2;
    public static final int EVENT_VERBOSE = 3;
    public static final int EVENT_ALL = 4;    

    /**
     * Begin a tx using the optimistic or datastore transation model.
     */
    public void begin(boolean optimistic);

    /**
     * Commit the tx. Note that non-XA transactions can call {@link #store}
     * with {@link #STORE_OPTION_COMMIT} to commit instead of having to
     * also call this method. XA transactions should use the
     * {@link #STORE_OPTION_PREPARE} and call this method later. Normal
     * transactions might still call this method if they have no changes or
     * have already used {@link #STORE_OPTION_FLUSH} to flush changes.
     */
    public void commit();

    /**
     * Rollback a tx.
     */
    public void rollback();

    /**
     * Control pinning of datastore connections. Note that the datastore
     * may ignore these options.
     *
     * @see #CON_POLICY_RELEASE
     * @see #CON_POLICY_PIN_FOR_TX
     * @see #CON_POLICY_PIN
     */
    public void setConnectionPolicy(int policy);

    /**
     * Control locking of objects in datastore transactions. Note that the
     * datastore may ignore this options.
     *
     * @see #LOCK_POLICY_NONE
     * @see #LOCK_POLICY_FIRST
     * @see #LOCK_POLICY_ALL
     */
    public void setLockingPolicy(int policy);

    /**
     * Get the locking policy.
     */
    public int getLockingPolicy();

    /**
     * Get the State for an OID.
     *
     * @param triggerField The field that triggered the fetch or null if
     */
    public StatesReturned fetch(ApplicationContext context, OID oid, State current,
            FetchGroup fetchGroup, FieldMetaData triggerField);

    /**
     * Get the States for a batch of OIDs. The default fetch group is fetched
     * for each OID.
     *
     * @param triggerField The field that triggered the fetch or null if
     */
    public StatesReturned fetch(ApplicationContext context, OIDArray oids,
            FieldMetaData triggerField);

    /**
     * Persist a graph of State's (insert, update and delete). For some states
     * persisting them will change some of their fields (e.g. autoset fields)
     * and the new values of these fields must be returned if
     * returnFieldsUpdatedBySM is true.
     *
     * @param returnFieldsUpdatedBySM Return States containing any fields
     *      updated by us (i.e. instead of using the value in the incoming
     *      State, if any) in returned.
     * @param storeOption Option to prepare or commit the tx after the store
     *      operation ({@link #STORE_OPTION_PREPARE},
     *      {@link #STORE_OPTION_COMMIT}, {@link #STORE_OPTION_FLUSH}).
     * @param evictClasses If this is true then classes with instances
     *      modified in the transaction are evicted instead of the instances
     */
    public StatesReturned store(StatesToStore toStore, DeletePacket toDelete,
            boolean returnFieldsUpdatedBySM, int storeOption,
            boolean evictClasses);

    /**
     * Create a real OID for a new instance. This is called prior to commit
     * when the real OID is required. If it is not possible to create the
     * OID (e.g. an IDENTITY column in JDBC) then an exception must be
     * thrown. For these classes a flush must be done instead.
     */
    public OID createOID(ClassMetaData cmd);

    /**
     * Compile a query into a form for the datastore (e.g. generate the SQL
     * for JDBC).
     */
    public CompiledQuery compileQuery(QueryDetails query);

    /**
     * Execute a query. If compiledQuery is not null then query is ignored
     * and may be null. If compiledQuery is null then query is used to compile
     * the query. The compiledQuery is accessable through the returned
     * ExecuteQueryReturn instance for reuse in future calls.
     */
    public ExecuteQueryReturn executeQuery(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params);

    /**
     * Prepare and execute the query, returning all results in the supplied
     * container.
     */
    public QueryResultContainer executeQueryAll(ApplicationContext context,
            QueryDetails query, CompiledQuery compiledQuery, Object[] params);

    /**
     * Prepare and execute the query and return the number of results
     * using count(*) or something similar.
     */
    public int executeQueryCount(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params);

    /**
     * Get the datastore specific query plan for the query (e.g. the SQL
     * and database plan for JDBC).
     */
    public VersantQueryPlan getQueryPlan(QueryDetails query,
            CompiledQuery compiledQuery, Object[] params);

    /**
     * Return the next batch of results for the query.
     */
    public QueryResultContainer fetchNextQueryResult(ApplicationContext context,
            RunningQuery runningQuery, int skipAmount);

    /**
     * This is used by random access queries to return absolute results.
     */
    public QueryResultContainer fetchRandomAccessQueryResult(
            ApplicationContext context, RunningQuery runningQuery, int index,
            int fetchAmount);

    /**
     * This is used by random access queries to return the number of results.
     */
    public int getRandomAccessQueryCount(ApplicationContext context,
            RunningQuery runningQuery);

    /**
     * Close the query.
     */
    public void closeQuery(RunningQuery runningQuery);

    /**
     * Get the underlying datastore connection. If no connection is associated
     * with this StorageManager then one is allocated and pinned. The returned
     * object must be a proxy for the real connection that is returned when
     * its "close" method is called.
     */
    public Object getDatastoreConnection();

    /**
     * Does the datastore require notification before an object becomes dirty
     * or deleted for the first time in the tx?
     *
     * @see #notifyDirty(com.versant.core.common.OID)
     */
    public boolean isNotifyDirty();

    /**
     * The object for the oid is about to become dirty or deleted.
     *
     * @see #isNotifyDirty()
     */
    public void notifyDirty(OID oid);

    /**
     * Restore to an initial inactive state.
     */
    public void reset();

    /**
     * Destroy cleaning up all resources. No methods should be invoked on a
     * destroyed StorageManager but the StorageManager is not required to
     * enforce this.
     */
    public void destroy();

    /**
     * Log an event to our event log.
     */
    public void logEvent(int level, String description, int ms);

    /**
     * If we are decorating another SM then return it. Otherwise return null.
     */
    public StorageManager getInnerStorageManager();

    /**
     * Are we holding a datastore connection?
     */
    public boolean hasDatastoreConnection();

    /**
     * Get internal status information in the form of property -> value pairs.
     */
    public Map getStatus();

    /**
     * Set a userObject on this SM.
     */
    public void setUserObject(Object o);
}


