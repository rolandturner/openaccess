
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

import com.versant.core.logging.LogEvent;
import com.versant.core.metric.Metric;
import com.versant.core.metric.MetricSnapshotPacket;
import com.versant.core.server.DataStoreInfo;

import javax.jdo.PersistenceManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * JDO Genie extension to the PersistenceManagerFactory interface. This adds
 * methods for monitoring a JDO Genie server, controlling event logging,
 * changing the cache size etc. To use this just cast the PMF to this and
 * call the methods.
 */
public interface VersantPersistenceManagerFactory
        extends PersistenceManagerFactory {

    public static final String PROP_DRIVER_NAME = "javax.jdo.option.ConnectionDriverName";
    public static final String PROP_USER_NAME = "javax.jdo.option.ConnectionUserName";
    public static final String PROP_PASSWORD = "javax.jdo.option.ConnectionPassword";
    public static final String PROP_URL = "javax.jdo.option.ConnectionURL";
    public static final String PROP_FACTORY_NAME = "javax.jdo.option.ConnectionFactoryName";
    public static final String PROP_FACTORY2_NAME = "javax.jdo.option.ConnectionFactory2Name";

    public static final int EVENT_ERRORS = 1;
    public static final int EVENT_NORMAL = 2;
    public static final int EVENT_VERBOSE = 3;
    public static final int EVENT_ALL = 4;

    /**
     * Obtain a JDBC connection from the pool for the datastore. If the
     * datastore name is null then the default datastore is used. You must
     * close the connection at some point to return it to the pool. The
     * connection has no transaction association and autoCommit will be set
     * to false. If JDBC event logging is on then operations on the connection
     * will be logged. This method is not available to remote clients and a
     * JDOUserException is thrown if it is called by a remote client or if
     * the datastore does not exist.
     */
    public Connection getJdbcConnection(String datastore)
            throws SQLException;

    /**
     * Close all connections in the pool for the datastore. Connections
     * currently in use are not closed. If datastore is null then the
     * connections for the default datastore are closed. Any errors
     * encountered closing the connections are silently discarded. If you
     * know backups happen at 3am and the database is restarted then you
     * can schedule a call to this method to get rid of the stale
     * connections in the pool.
     */
    public void clearConnectionPool(String datastore);

    /**
     * Associate a user object with this PMF. Note that if the PMF is local
     * then there is one object for the whole server. If the PMF is remote
     * then there is one object per remote connection (i.e. client). For
     * remote PMFs the object must be Serializable. This is typically used
     * to identify remote clients in some way (e.g. the logged on user or a
     * remote stub to communicate with the client).
     *
     * @see #getUserObject()
     */
    public void setUserObject(Object o);

    /**
     * Get the user object.
     *
     * @see #setUserObject
     */
    public Object getUserObject();

    /**
     * Is access to loaded default fetch group fields always intercepted?
     * See the "Cache Management" chapter of the manual for more information
     * on this flag.
     */
    public boolean isInterceptDfgFieldAccess();

    /**
     * Control interception of loaded default fetch group fields.
     * See the "Cache Management" chapter of the manual for more information
     * on this flag.
     */
    public void setInterceptDfgFieldAccess(boolean interceptDfgFieldAccess);

    /**
     * Is closing a PersistenceManager with a active tx is allowed? The
     * default is false.
     *
     * @see #setAllowPmCloseWithTxOpen(boolean)
     */
    public boolean isAllowPmCloseWithTxOpen();

    /**
     * Allow the closing of a PersistenceManager with active tx. The default
     * is to not allow it.
     */
    public void setAllowPmCloseWithTxOpen(boolean allowed);

    /**
     * Are bidirectional relationships (one-to-many, many-to-many) checked for
     * consistency on commit for new PersistenceManagers? The default is false.
     *
     * @see #setCheckModelConsistencyOnCommit(boolean)
     * @see com.versant.core.jdo.VersantPersistenceManager#isCheckModelConsistencyOnCommit()
     */
    public boolean isCheckModelConsistencyOnCommit();

    /**
     * Enable or disable commit time consistency checking for bidirectional
     * relationships (one-to-many, many-to-many) in newly created
     * PersistenceManagers. When this flag is enabled commiting with an
     * incorrectly completed bidirectional relationship will trigger a
     * JDOUserException. This check is expensive and should only be enabled
     * during development.
     * @see com.versant.core.jdo.VersantPersistenceManager#setCheckModelConsistencyOnCommit(boolean)
     */
    public void setCheckModelConsistencyOnCommit(boolean on);

    /**
     * Associate a user object with the JDO Genie server. Note that if the PMF
     * is local then this method just calls setUserObject. This method makes
     * it possible for a remote client to change the user object associated
     * with the local PMF on the server.
     *
     * @see #getServerUserObject()
     * @see #setUserObject(Object)
     */
    public void setServerUserObject(Object o);

    /**
     * Get the user object associated with the JDO Genie server. Note that if
     * the PMF is local then this is the same object as returned by
     * getUserObject. If the PMF is remote then this is the user object
     * associated with the local PMF on the server. For the remote case
     * the user object must be serializable. Typically it will be a remote
     * object providing services to remote clients. Making it available
     * through this method avoids having to register it with the RMI registry
     * or some other naming service.
     *
     * @see #setServerUserObject(Object)
     */
    public Object getServerUserObject();

    /**
     * Shutdown the JDO Genie server.
     */
    public void shutdown();

    /**
     * Get all performance events newer than lastId or all events if lastId is
     * 0. If no new events are available null is returned.
     */
    public LogEvent[] getNewPerfEvents(int lastId);

    /**
     * Get server status information bean. For much more detailed information
     * on the state of the server use {@link #getNewMetricSnapshots(int) }.
     */
    public PmfStatus getPmfStatus();

    /**
     * Get all the performance metrics configured on the server.
     *
     * @see #getNewMetricSnapshots(int)
     */
    public Metric[] getMetrics();

    /**
     * Get all performance metric snapshots newer that lastId or all
     * available data if lastId is 0. If no new data is available then null
     * is be returned.
     *
     * @see #getMetrics()
     * @see #getMostRecentMetricSnapshot(int)
     */
    public MetricSnapshotPacket getNewMetricSnapshots(int lastId);

    /**
     * Get the most recent performance metric snapshot since the one with ID
     * of lastId. If no new data is available then null is be returned. If
     * lastId is 0 then the most recent snapshot is returned.
     *
     * @see #getMetrics()
     * @see #getNewMetricSnapshots(int)
     */
    public MetricSnapshotPacket getMostRecentMetricSnapshot(int lastId);

    /**
     * Set the value of the named user-defined metric.
     *
     * @see #incUserMetric
     */
    public void setUserMetric(String name, int value);

    /**
     * Add delta to the value of the named user-defined metric.
     *
     * @see #setUserMetric
     */
    public void incUserMetric(String name, int delta);

    /**
     * Get the value of the named user-defined metric. Note that the values for
     * all user-defined metrics are returned with each set of samples in
     * {@link #getNewMetricSnapshots(int) }.
     */
    public int getUserMetric(String name);

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
     * Call System.gc() in the VM that the JDO Genie server associated with
     * this PMF is running in.
     */
    public void doSystemGC();

    /**
     * Get a tree of all the configurable properties of the server and their
     * current values.
     */
    public PropertyInfo getServerConfiguration();

    /**
     * Change a property on a component of the server (e.g. the cache). If
     * the return value is null then the change was sucessful otherwise it
     * is an error message.
     *
     * @param beanPath The path to the bean property to change (build this
     *                 using the name of each bean in the path and the name of the
     *                 property)
     * @param value    The new value for the property
     */
    public String setServerProperty(String[] beanPath, String value);

    /**
     * Get status information on all active remote clients.
     */
    public RemoteClientStatus[] getRemoteClients();

    /**
     * Get status information on all active PersistenceManager's.
     */
    public List getPersistenceManagers();

    /**
     * Evict all information for an OID from the PMF wide cache. This is a NOP
     * if there is no information in the cache for the OID.
     *
     * @param oid OID of the JDO instance to be evicted
     */
    public void evict(Object oid);

    /**
     * Evict all information for an array of OIDs from the PMF wide cache.
     *
     * @param oids OIDs of the JDO instances to be evicted
     */
    public void evictAll(Object[] oids);

    /**
     * Evict all information for a collection of OIDs from the PMF wide cache.
     *
     * @param oids Collection of OIDs of the JDO instances to be evicted
     */
    public void evictAll(Collection oids);

    /**
     * Evict all information for all JDO instances of a Class from the PMF wide
     * cache.
     *
     * @param cls               Class of JDO instances to be evicted
     * @param includeSubclasses If true then instances of subclasses are also
     *                          evicted
     */
    public void evictAll(Class cls, boolean includeSubclasses);

    /**
     * Evict all JDO instances from the PMF wide cache.
     */
    public void evictAll();

    /**
     * Is the OID in the PMF wide cache? Note that it may already be gone
     * even if this call returns true. This is for our unit tests.
     */
    public boolean isInCache(Object oid);

    /**
     * Get the classid for the class. This is a positive int generated from
     * a hash of the class name. It is used as part of the OID string for
     * datastore identity classes and in jdo_class columns in inheritance
     * heirachies. A JDOUserException is thrown if cls is not persistent.
     *
     * @see #getClassForID
     * @see #getJdbcClassID
     * @see #getClassIndex
     */
    public int getClassID(Class cls);

    /**
     * Get the Class for the classid. A JDOUserException is thrown if the
     * classid is invalid.
     *
     * @see #getClassID
     * @see #getClassForJdbcID
     * @see #getClassForIndex
     */
    public Class getClassForID(int classid);

    /**
     * Get the JDBC classid for the class. If the class is part of an
     * inheritance hierarchy then this is the value of the jdbc-class column
     * that identifies instances of the class. The default value is the
     * classid for the class but this can be changed using the jdbc-class-id
     * extension in the meta data.
     *
     * @see #getClassForJdbcID
     * @see #getClassID
     * @see #getClassIndex
     */
    public Object getJdbcClassID(Class cls);

    /**
     * Get the Class for the jdbc-class-id for a class in the hierarchy
     * starting at baseClass. A JDOUserException is thrown if the jdbc-class-id
     * is invalid.
     *
     * @see #getJdbcClassID
     * @see #getClassForID
     * @see #getClassForIndex
     */
    public Class getClassForJdbcID(Class baseClass, Object jdbcClassid);

    /**
     * Get the class index for the class. This is an int between 0 and the
     * number of perstent classes less 1. It is appropriate for short term
     * representation of a class. It will change as new persistent classes
     * are added to the model. A JDOUserException is thrown if cls is not
     * persistent.
     *
     * @see #getClassForIndex
     * @see #getClassID
     * @see #getJdbcClassID
     */
    public int getClassIndex(Class cls);

    /**
     * Get the Class for a class index. An JDOUserException exception
     * is thrown if the index is invalid.
     *
     * @see #getClassIndex
     * @see #getClassForID
     * @see #getClassForJdbcID
     */
    public Class getClassForIndex(int index);

    /**
     * Convert an array of Class'es into their class indexes. If
     * includeSubclasses is true then this will recursively get the indexes
     * for all the subclasses in each hierarchy.
     */
    public int[] getClassIndexes(Class[] classes, boolean includeSubclasses);

    /**
     * Configure the encoder with PersistenceDelegate's for JDO Genie SCO
     * instances. The java.beans package was only added in JDK 1.4.
     *
     * @see java.beans.Encoder
     */
    public void registerSCOPersistenceDelegates(Object encoder);

    /**
     * Get the type of reference used to reference instances in the local
     * PM cache by PMs returned by this factory. 
     *
     * @see com.versant.core.jdo.VersantPersistenceManager#PM_CACHE_REF_TYPE_WEAK
     * @see com.versant.core.jdo.VersantPersistenceManager#PM_CACHE_REF_TYPE_SOFT
     * @see com.versant.core.jdo.VersantPersistenceManager#PM_CACHE_REF_TYPE_STRONG
     */
    public int getPmCacheRefType();

    /**
     * Set the type of reference used to reference instances in the local
     * PM cache by PMs returned by this factory. This can also be changed
     * for each PM using
     * {@link com.versant.core.jdo.VersantPersistenceManager#setPmCacheRefType(int)}.
     *
     * @see com.versant.core.jdo.VersantPersistenceManager#PM_CACHE_REF_TYPE_WEAK
     * @see com.versant.core.jdo.VersantPersistenceManager#PM_CACHE_REF_TYPE_SOFT
     * @see com.versant.core.jdo.VersantPersistenceManager#PM_CACHE_REF_TYPE_STRONG
     */
    public void setPmCacheRefType(int pmCacheRefType);

    /**
     * This method is for internal testing. Get information about the
     * datastore. If the datastore parameter is null the information about the
     * default datastore is returned.
     */
    public DataStoreInfo getDataStoreInfo(String datastore);

    /**
     * Create a ejb3 EntityManagerFactory.
     */
    public Object getEntityManagerFactory();

}
