
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
package com.versant.core.ejb;

import com.versant.core.jdo.*;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageCache;
import com.versant.core.metric.Metric;
import com.versant.core.metric.MetricSnapshotPacket;
import com.versant.core.server.DataStoreInfo;
import com.versant.core.logging.LogEventStore;
import com.versant.core.logging.LogEvent;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.ClassMetaData;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.jdo.listener.InstanceLifecycleListener;
import java.util.*;

/**
 * Factory class responsible for supplying EntityManger instances.
 */
public class EntityManagerFactoryImp implements
						javax.persistence.EntityManagerFactory {

    private com.versant.core.jdo.VersantPMFInternal pmf;
    private EntityLifecycleManager entityListener;
    private PostPersistCallback postPersistCallback;

    public EntityManagerFactoryImp(VersantPMFInternal pmf) {
        this.pmf = pmf;
        setupEntityListener();
    }

    /**
     * Create a new EntityManager of PersistenceContextType.TRANSACTION
     * <p/>
     * The isOpen method will return true on the returned instance.
     * <p/>
     * This method returns a new EntityManagerFactory instance (with a new
     * persistence context) every time it is invoked.
     */
    public static javax.persistence.EntityManagerFactory getEntityManagerFactory(
            Properties props) throws javax.persistence.PersistenceException {
        props.setProperty(ConfigParser.META_DATA_PRE_PROCESSOR,
                props.getProperty(ConfigParser.META_DATA_PRE_PROCESSOR,
                        MDStatics.EJB_JDBC_PRE_PROCESSOR));
	    ClassLoader loader =
	    	javax.jdo.PersistenceManagerFactory.class.getClassLoader();
	    com.versant.core.jdo.PersistenceManagerFactoryImp jdopmfi =
	    	new	com.versant.core.ejb.PersistenceManagerFactoryImp(
	    											props, loader);
	    EntityManagerFactoryImp emfi =
	    						new EntityManagerFactoryImp(jdopmfi);
	    return emfi;
    }

    /**
     * Create a new EntityManager of PersistenceContextType.TRANSACTION
     * <p/>
     * The isOpen method will return true on the returned instance.
     * <p/>
     * This method returns a new EntityManager instance (with a new
     * persistence context) every time it is invoked.
     */
    public EntityManager createEntityManager() {
        return createEntityManager(PersistenceContextType.TRANSACTION);
    }

    /**
     * Create a new EntityManager of the specified
     * PersistenceContextType.
     * The isOpen method will return true on the returned instance.
     * This method returns a new EntityManager instance (with a new
     * persistence context) every time it is invoked.
     */
	public EntityManager createEntityManager(
								PersistenceContextType type) {
        return new EntityManagerImp(
                pmf.getStorageManagerFactory().getStorageManager(),
                pmf.getJDOMetaData(), entityListener, type, postPersistCallback);
    }

    /**
     * Close this factory, releasing any resources that might be
     * held by this factory. After invoking this method, all methods
     * on the EntityManagerFactory instance will throw an
     * IllegalStateException, except for isOpen, which will return
     * false.
     */
    public void close() {
		pmf.close();
    }

    /**
     * Indicates whether or not this factory is open. Returns true
     * until a call to close has been made.
     */
    public boolean isOpen() {
		return !pmf.isClosed();
    }

    /**
     * Get the container-managed EntityManager bound to the
     * current JTA transaction.
     * If there is no persistence context bound to the current
     * JTA transaction, a new persistence context is created and
     * associated with the transaction.
     * If there is an existing persistence context bound to
     * the current JTA transaction, it is returned.
     * If no JTA transaction is in progress, an EntityManager
     * instance is created that will be bound to subsequent
     * JTA transactions.
     * Throws IllegalStateException if called on an
     * EntityManagerFactory that does not provide JTA EntityManagers.
     */
    public EntityManager getEntityManager()
                                        throws IllegalStateException {
        return new EntityManagerImp(
                pmf.getStorageManagerFactory().getStorageManager(),
                pmf.getJDOMetaData(), entityListener,
                PersistenceContextType.TRANSACTION, postPersistCallback);
    }

    /**
     * Setup the EntityLifecycleManager for callbacks and listeners
     */
    private void setupEntityListener() {
        if (entityListener != null) return;
        ModelMetaData jmd = pmf.getJDOMetaData();
        List lifecycleEvents = jmd.getEntityLifecycleEvent();
        if (lifecycleEvents == null) {
            entityListener = new EntityLifecycleManager(null);
            return;
        }
        EntityClassLifecycleManager[] listeners =
                new EntityClassLifecycleManager[jmd.classes.length + 1];

        HashMap weightMap = new HashMap();
        HashMap classToCmd = new HashMap();
        ClassMetaData[] cmds = jmd.classes;
        for (int i = 0; i < cmds.length; i++) {
            ClassMetaData cmd = cmds[i];
            weightMap.put(cmd.cls, new Integer(cmd.pcHierarchy.length));
            classToCmd.put(cmd.cls, cmd);
        }

        for (Iterator iter = lifecycleEvents.iterator(); iter.hasNext();) {
            EntityLifecycleEvent event = (EntityLifecycleEvent) iter.next();
            Integer i = (Integer) weightMap.get(event.getForClass());
            event.setWeight(i.intValue());
        }
        Collections.sort(lifecycleEvents);

        int allClassIndex = jmd.classes.length;
        for (Iterator iter = lifecycleEvents.iterator(); iter.hasNext();) {
            EntityLifecycleEvent event = (EntityLifecycleEvent) iter.next();
            int[] indexs = jmd.convertToClassIndexes(
                    new Class[]{event.getForClass()}, true);

            for (int i = 0; i < indexs.length; i++) {
                int index = indexs[i];
                if (listeners[index] == null) {
                    listeners[index] = new EntityClassLifecycleManager();
                }
                listeners[index].add(event);

                // setup all listener
                if (listeners[allClassIndex] == null) {
                    listeners[allClassIndex] = new EntityClassLifecycleManager();
                }
                listeners[allClassIndex].add(event);
            }
        }
        entityListener = new EntityLifecycleManager(listeners);
        entityListener.setCassToCmd(classToCmd);
    }

    public ModelMetaData getModelMetaData() {
        return pmf.getJDOMetaData();
    }

    public void setPostPersistListener(PostPersistCallback cb) {
       this.postPersistCallback = cb;
    }

    protected StorageManagerFactory createStorageManagerFactory() {
        return null;
    }

    public Metric[] getMetrics() {
        return pmf.getMetrics();
    }

    public MetricSnapshotPacket getNewMetricSnapshots(int lastId) {
        return pmf.getNewMetricSnapshots(lastId);
    }

    public MetricSnapshotPacket getMostRecentMetricSnapshot(int lastId) {
        return pmf.getMostRecentMetricSnapshot(lastId);
    }

    public void setUserMetric(String name, int value) {
        pmf.setUserMetric(name, value);
    }

    public void incUserMetric(String name, int delta) {
        pmf.incUserMetric(name, delta);
    }

    public int getUserMetric(String name) {
        return pmf.getUserMetric(name);
    }

    public Map getHyperdriveBytecode() {
        return pmf.getHyperdriveBytecode();
    }

    public int getHyperdriveBytecodeMaxSize() {
        return pmf.getHyperdriveBytecodeMaxSize();
    }

    public DataStoreInfo getDataStoreInfo(String datastore) {
        return pmf.getStorageManagerFactory().getDataStoreInfo();
    }

    public void evictAll() {
        pmf.evictAll();
    }

    public void logEvent(int eventNormal, String s, int i) {
        pmf.logEvent(eventNormal, s, i);
    }

    public void addInstanceLifecycleListener(InstanceLifecycleListener listener,
                                             Class[] classes) {
        pmf.addInstanceLifecycleListener(listener, classes);
    }

    public void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        pmf.removeInstanceLifecycleListener(listener);
    }

    public boolean isLocal() {
        return pmf.isLocal();
    }

    public LogEventStore getLogEventStore() {
        return pmf.getLogEventStore();
    }

    public LogEvent[] getNewPerfEvents(int lastEQEventId) {
        return pmf.getNewPerfEvents(lastEQEventId);
    }

    public String getConnectionURL() {
        return pmf.getConnectionURL();
    }

    public StorageCache getStorageCache() {
        return pmf.getStorageCache();
    }

    public StorageManagerFactory getStorageManagerFactory() {
        return pmf.getStorageManagerFactory();
    }
}
