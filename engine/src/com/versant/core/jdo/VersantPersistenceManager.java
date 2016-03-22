
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

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

/**
 * JDO Genie extension of the standard JDO PersistenceManager interface.
 */
public interface VersantPersistenceManager extends PersistenceManager {

    /**
     * Do not lock any objects in datastore tx.
     */
    public static final int LOCKING_NONE = 1;
    /**
     * Lock only the first object navigated or fetched in datastore tx.
     */
    public static final int LOCKING_FIRST = 2;
    /**
     * Lock all objects in datastore tx (not fully supported on all db's).
     */
    public static final int LOCKING_ALL = 3;

    public static final int EVENT_ERRORS = VersantPersistenceManagerFactory.EVENT_ERRORS;
    public static final int EVENT_NORMAL = VersantPersistenceManagerFactory.EVENT_NORMAL;
    public static final int EVENT_VERBOSE = VersantPersistenceManagerFactory.EVENT_VERBOSE;
    public static final int EVENT_ALL = VersantPersistenceManagerFactory.EVENT_ALL;

    /**
     * Use strong references to instances in the local PM cache. *
     */
    public final static int PM_CACHE_REF_TYPE_STRONG = 1;
    /**
     * Use soft references to instances in the local PM cache. *
     */
    public final static int PM_CACHE_REF_TYPE_SOFT = 2;
    /**
     * Use weak references to instances in the local PM cache. *
     */
    public final static int PM_CACHE_REF_TYPE_WEAK = 3;

    /**
     * If this flag is true then access to default fetch group fields loaded
     * into an instance is intercepted. See the "Cache Management" chapter of
     * the manual for more information on this flag.
     */
    public boolean isInterceptDfgFieldAccess();

    /**
     * Control the interception of access to default fetch group fields loaded
     * into an instance. See the "Cache Management" chapter of the manual
     * for more information on this flag.
     */
    public void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess);

    /**
     * This will cancel the last exectuted query.
     */
    public void cancelQueryExecution();

    /**
     * Does the current transaction contain any dirty instances?
     */
    public boolean isDirty();

    /**
     * Utility method to return an datastore identity instance by the String
     * value that was returned from the id of the instance. This avoids having
     * to obtain the identity class for an Object and then via reflection
     * creating an instance of the id and asking the pm for the pc instance
     * by the id instance.
     */
    public Object getObjectByIDString(String value, boolean toValidate);

    /**
     * <p>This method returns an OID instance for the given class and String.
     * If the String is for a datastore identity class then the pcClass
     * parameter may be null. Calling this method with resolved true is
     * equivalent to calling {@link #newObjectIdInstance(Class, Object)}.</p>
     * <p/>
     * <p>If resolved is false then the String may be for a superclass of the
     * actual class. For datastore identity classes this is only possible if
     * you have constructed the String yourself from the class ID of the base
     * class and the primary key of the instance.</p>
     *
     * @see #getObjectByIDString(String, boolean)
     */
    public Object newObjectIdInstance(Class pcClass, String str,
            boolean resolved);

    /**
     * <p>This method returns an instance of any datastore identity class
     * from the toString of its OID. Calling this method with resolved true is
     * equivalent to calling {@link #getObjectByIDString(String, boolean) }.</p>
     * <p/>
     * <p>If resolved is false then the String may be for a superclass of the
     * actual class. This is only possible if you have constructed the String
     * yourself from the class ID of the base class and the primary key of the
     * instance.</p>
     *
     * @see #newObjectIdInstance(Class, String, boolean)
     */
    public Object getObjectByIDString(String value, boolean toValidate,
            boolean resolved);

    /**
     * Loads the fetch group fields for the already managed instance.
     *
     * @param pc   The PersistenceCapable instance for which to load the fields
     * @param name The name of the fetch group
     */
    public void loadFetchGroup(Object pc, String name);

    /**
     * This is typically used in operations where there is not enough memory
     * available on the client to managed the transaction's dirty instances.
     * <p/>
     * Flush all dirty and new instances to the database and evict all
     * instances from the local cache. This allows unreferenced instances to
     * be garbage collected making it easier to write loops that update
     * millions of instances in a single transaction.<p>
     *
     * @see #flush(boolean)
     * @see #evictAll()
     */
    public void flush();

    /**
     * Flush all dirty and new instances to the database.<p>
     * <p/>
     * If retainValues is false then refer to {@link #flush()}.
     * <p/>
     * If the PM is not currently associated with any JDBC connection
     * (i.e. it is using optimistic transactions) then the connection used
     * to do the flush is pinned to the PM and used for all subsequent
     * operations. This is very similar to a PM using datastore transactions.<p>
     * <p/>
     * This is used typically when you want to see the changes made in the pm when
     * direct sql is used.
     *
     * @see #getJdbcConnection(java.lang.String)
     * @see #flush()
     */
    public void flush(boolean retainValues);

    /**
     * Obtain the JDBC connection associated with the PM for the datastore. If
     * the datastore name is null then the connection for the default datastore
     * is returned. If the PM is not currently associated with any connection
     * (i.e. it is using optimistic transactions) then the connection returned
     * is pinned to the PM and used for all subsequent operations. This is very
     * similar to a PM using datastore transactions.<p>
     * <p/>
     * An exception is thrown if any transaction related methods are called on
     * the connection (commit, rollback, setAutoCommit). The JDO API must be
     * used for transaction control (pm.currentTransation().commit() etc.).
     * The returned connection has autoCommit set to false and is a proxy
     * for the real connection. This method may only be called inside a JDO
     * transaction.<p>
     * <p/>
     * The connection is pinned to the PM until commit or rollback of the
     * current JDO transaction. Once commit or rollback has been done it will
     * be returned to the pool and the proxy is automatically closed. You
     * can call close on the proxy but this does not close the underlying
     * JDBC connection and it remains pinned to the PM.<p>
     * <p/>
     * If JDBC event logging is on then operations on the connection will be
     * logged. This method is not available to remote clients and a
     * JDOUserException is thrown if it is called by a remote client or if
     * the datastore does not exist.<p>
     *
     * @see VersantPersistenceManagerFactory#getJdbcConnection
     */
    public Connection getJdbcConnection(String datastore);

    /**
     * Obtain the URL for the datastore. If the datastore name is null then
     * the URL for the default datastore is returned.
     */
    public String getConnectionURL(String dataStore);

    /**
     * Obtain the driver name for the datastore. If the datastore name is null
     * then the driver name for the default datastore is returned.
     */
    public String getConnectionDriverName(String dataStore);

    /**
     * This will recursively make all pc fields that is loaded transient.
     */
    public void makeTransientRecursive(Object pc);

    /**
     * This will return all the dirty instances in the current transaction. If
     * there are no dirty instance then an empty list is returned.
     */
    public List versantAllDirtyInstances();

    /**
     * Set the locking mode for datastore transactions. You can set the
     * default value for this property using the Workbench or edit your
     * properties file directly (versant.datastoreTxLocking property). This
     * method may be called at any time. If called inside a transaction it
     * changes the mode for future SQL. The default setting is LOCKING_FIRST.
     *
     * @see #LOCKING_NONE
     * @see #LOCKING_FIRST
     * @see #LOCKING_ALL
     */
    public void setDatastoreTxLocking(int mode);

    /**
     * Get the locking mode for datastore transactions.
     */
    public int getDatastoreTxLocking();

    /**
     * Return the instance for oid if it is present in the local PM cache
     * otherwise return null. Note that the instance might still be hollow
     * and touching its fields will cause a fetch from the level 2 cache
     * or database.
     *
     * @see #isHollow(Object)
     */
    public Object getObjectByIdFromCache(Object oid);

    /**
     * Is the instance hollow? Hollow instances are managed but their fields
     * have not been loaded from the level 2 cache or database.
     *
     * @see #getObjectByIdFromCache(Object)
     */
    public boolean isHollow(Object pc);

    /**
     * Does the instance have an identity? New instances are only assigned
     * an identity on commit or flush or when the application executes an
     * operation that requires the identity.
     */
    public boolean hasIdentity(Object pc);

    /**
     * Log a user defined event. If the event logging level does not match
     * the level parameter then the event is ignored. For remote PMs this
     * check is done on the server so a network call is required even if the
     * event is not logged.
     *
     * @see #EVENT_ERRORS
     * @see #EVENT_NORMAL
     * @see #EVENT_VERBOSE
     * @see #EVENT_ALL
     */
    public void logEvent(int level, String description, int ms);

    /**
     * This util method is used by collection types to preload their pc
     * entries. It tests to determine if the states refered to by the oids is
     * in the managed cache. If not they must be bulk loaded from server.
     * The scenario in which this is likely to happen is when the collection
     * is not in the default fetch group and the state is in cache with the
     * collection filled in. If this collection field is read then the
     * pcstateman will determine that the stateField is filled and hence not
     * ask the server for it.
     */
    public int getObjectsById(Object[] oids, int length, Object[] data,
            int stateFieldNo, int classMetaDataIndex);

    /**
     * Construct a new query instance with the given candidate class from a
     * named query. The query name given must be the name of a query defined
     * in metadata. The metadata is searched for the specified name.
     * This is a JDO 2 preview feature.
     */
    public Query versantNewNamedQuery(Class cls, String queryName);

    /**
     * This method makes detached copies of the parameter instances and returns
     * the copies as the result of the method. The order of instances in the
     * parameter Collection's iteration corresponds to the order of corresponding
     * instances in the returned Collection's iteration.
     * <p/>
     * The Collection of instances is first made persistent, and the reachability
     * algorithm is run on the instances. This ensures that the closure of all
     * of the instances in the the parameter Collection is persistent.
     * <p/>
     * For each instance in the parameter Collection, a corresponding detached
     * copy is created. Each field in the persistent instance is handled based on
     * its type and whether the field is contained in the fetch group for the
     * persistence-capable class. If there are duplicates in the parameter
     * Collection, the corresponding detached copy is used for each such duplicate.
     */
    public Collection versantDetachCopy(Collection pcs, String fetchGroup);

    /**
     * Are instances in the local PM cache checked for consistency on commit?
     * The default is false.
     *
     * @see #checkModelConsistency()
     * @see #setCheckModelConsistencyOnCommit(boolean)
     * @see VersantPersistenceManagerFactory#isCheckModelConsistencyOnCommit()
     */
    public boolean isCheckModelConsistencyOnCommit();

    /**
     * Enable or disable commit time consistency checking. When this flag is
     * enabled all instances in the local PM cache checked for consistency on
     * commit. This check is expensive and should only be enabled during
     * development.
     *
     * @see #checkModelConsistency()
     * @see VersantPersistenceManagerFactory#setCheckModelConsistencyOnCommit(boolean)
     */
    public void setCheckModelConsistencyOnCommit(boolean on);

    /**
     * Check the consistency of all instances in the local cache. Currently
     * this makes sure that all birectional relationships have been completed
     * properly (both sides in sync) but other checks may will be added in
     * future. This method is very slow and should only be used for debugging
     * during development.
     *
     * @see #setCheckModelConsistencyOnCommit(boolean)
     */
    public void checkModelConsistency();

    /**
     * This method applies the changes contained in the collection of detached
     * instances to the corresponding persistent instances in the cache and
     * returns a collection of persistent instances that exactly corresponds to
     * the parameter instances. The order of instances in the parameter
     * Collection's iteration corresponds to the order of corresponding
     * instances in the returned Collection's iteration.
     * <p/>
     * Changes made to instances while detached are applied to the corresponding
     * persistent instances in the cache. New instances associated with the
     * detached instances are added to the persistent instances in the
     * corresponding place.
     */
    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional);

    /**
     * This method applies the changes contained in the collection of detached
     * instances to the corresponding persistent instances in the cache and
     * returns a collection of persistent instances that exactly corresponds to
     * the parameter instances. The order of instances in the parameter
     * Collection's iteration corresponds to the order of corresponding
     * instances in the returned Collection's iteration.
     * <p/>
     * Changes made to instances while detached are applied to the corresponding
     * persistent instances in the cache. New instances associated with the
     * detached instances are added to the persistent instances in the
     * corresponding place.
     *
     * @param detached VersantDetachable objects to attach in the current
     *                 transaction
     * @param shallow  attach only the objects in 'detached' Collection and not
     *                 reachable objects if true.
     */
    public Collection versantAttachCopy(Collection detached,
            boolean makeTransactional, boolean shallow);

    /**
     * Change the type of reference to an instance in the local PM cache
     * i.e. an instance managed by this PM.
     *
     * @see #PM_CACHE_REF_TYPE_WEAK
     * @see #PM_CACHE_REF_TYPE_SOFT
     * @see #PM_CACHE_REF_TYPE_STRONG
     */
    public void setPmCacheRefType(Object pc, int type);

    /**
     * Change the type of reference to an array of instances in the local PM
     * cache i.e. an array of instances managed by this PM.
     *
     * @see #PM_CACHE_REF_TYPE_WEAK
     * @see #PM_CACHE_REF_TYPE_SOFT
     * @see #PM_CACHE_REF_TYPE_STRONG
     */
    public void setPmCacheRefType(Object[] pcs, int type);

    /**
     * Change the type of reference to a collection of instances in the local
     * PM cache i.e. an array of instances managed by this PM.
     *
     * @see #PM_CACHE_REF_TYPE_WEAK
     * @see #PM_CACHE_REF_TYPE_SOFT
     * @see #PM_CACHE_REF_TYPE_STRONG
     */
    public void setPmCacheRefType(Collection col, int type);

    /**
     * Set the type of reference used to reference new instances added to
     * the local PM cache.
     *
     * @see #PM_CACHE_REF_TYPE_WEAK
     * @see #PM_CACHE_REF_TYPE_SOFT
     * @see #PM_CACHE_REF_TYPE_STRONG
     */
    public void setPmCacheRefType(int type);

    /**
     * Get the type of reference used to reference new instances added to
     * the local PM cache.
     *
     * @see #PM_CACHE_REF_TYPE_WEAK
     * @see #PM_CACHE_REF_TYPE_SOFT
     * @see #PM_CACHE_REF_TYPE_STRONG
     */
    public int getPmCacheRefType();

    /**
     * If this is true then the datastore connection is retained throughout
     * an optimistic tx even for JDBC databases. Note that the datastore
     * may ignore this flag (e.g. Versant which always retains the
     * connection). 
     */
    public void setRetainConnectionInOptTx(boolean on);

    /**
     * Evict all information for an OID or persistent instance from the
     * level 2 cache if the current transaction commits. If there is no
     * active transaction the oid will be evicted after the next transaction
     * that commits changes to the database. This is a NOP
     * if there is no information in the cache for the OID or object or
     * if the object is transient.
     *
     * @see VersantPersistenceManagerFactory#evict(java.lang.Object)
     */
    public void evictFromL2CacheAfterCommit(Object o);

    /**
     * Do {@link #evictFromL2CacheAfterCommit(java.lang.Object)} for each
     * entry in the array.
     *
     * @see VersantPersistenceManagerFactory#evictAll(java.lang.Object[])
     */
    public void evictAllFromL2CacheAfterCommit(Object[] a);

    /**
     * Do {@link #evictFromL2CacheAfterCommit(java.lang.Object)} for each
     * entry in the collection.
     *
     * @see VersantPersistenceManagerFactory#evictAll(java.util.Collection)
     */
    public void evictAllFromL2CacheAfterCommit(Collection c);

    /**
     * Evict all information for all instances of a Class from the level
     * 2 cache if the current transaction commits. If there is no
     * active transaction the class(es) will be evicted after the next
     * transaction that commits changes to the database.
     *
     * @param cls Class to be evicted
     * @param includeSubclasses If true then instances of subclasses are also
     *              evicted
     *
     * @see VersantPersistenceManagerFactory#evictAll(java.lang.Class, boolean)
     */
    public void evictAllFromL2CacheAfterCommit(Class cls, boolean includeSubclasses);

    /**
     * Evict all JDO instances from the level 2 cache if the current
     * transaction commits. If there is no active transaction the cache is
     * emptied immediately.
     */
    public void evictAllFromL2CacheAfterCommit();

    /**
     * Return the Optimistic locking field value.
     * If the instance is persistent-new or if changedChecking is used then
     * 'null' will be returned.
     */
    public Object getOptimisticLockingValue(Object o);

}

