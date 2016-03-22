
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

import com.versant.core.common.config.ConfigInfo;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.util.BeanUtils;
import com.versant.core.util.StringListParser;
import com.versant.core.util.PropertiesLoader;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.Utils;
import com.versant.core.metric.*;
import com.versant.core.metric.HasMetrics;
import com.versant.core.logging.LogEvent;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageManagerFactoryBuilder;

import java.util.*;
import java.io.IOException;

/**
 * PMF implementation.
 */
public class PersistenceManagerFactoryImp
        extends PersistenceManagerFactoryBase implements HasMetrics {

    protected Map hyperdriveBytecode;
    protected int hyperdriveBytecodeMaxSize;
    protected VersantBackgroundTask logDownloader;
    protected Thread logDownloaderThread;
    protected PMFServer[] pmfServers;
    protected boolean releaseHyperdriveBytecode;

    protected MetricSnapshotStore metricSnapshotStore;
    protected HashMap userMetricIndexMap;
    protected BaseMetric[] userMetrics;
    protected int[] userMetricValues;

    public static final String PMF_SERVER = "pmf.server";
    public static final String RELEASE_HYPERDRIVE_BYTECODE =
            "versant.releaseHyperdriveBytecode";

    private static final String CAT_GENERAL = "General";

    private final BaseMetric metricEvents =
            new BaseMetric("Events", "Events", CAT_GENERAL,
                    "Number of events logged", 3, Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricLastEventId =
            new BaseMetric("LastEventId", "Last Event ID", CAT_GENERAL,
                    "Approximate ID of the last event logged", 0,
                    Metric.CALC_RAW);

    private static final String CAT_PM = "PM";

    private final BaseMetric metricPMCreated =
            new BaseMetric("PMCreated", "PM Created", CAT_PM,
                    "Number of local PMs created", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricPMClosed =
            new BaseMetric("PMClosed", "PM Closed", CAT_PM,
                    "Number of PMs closed (local and remote)", 3,
                    Metric.CALC_DELTA_PER_SECOND);
    private final BaseMetric metricPMClosedAuto =
            new BaseMetric("PMClosedAuto", "PM Closed Auto", CAT_PM,
                    "Number of PMs closed automatically (local and remote)", 0,
                    Metric.CALC_RAW);
    private final BaseMetric metricPMClosedAutoTx =
            new BaseMetric("PMClosedAutoTx", "PM Closed Auto Tx", CAT_PM,
                    "Number of PMs closed automatically with active datastore transaction (BAD)",
                    0, Metric.CALC_RAW);
    private final BaseMetric metricPMCount =
            new BaseMetric("PMCount", "PM Count", CAT_PM,
                    "Number of open PMs", 0,
                    Metric.CALC_AVERAGE);

    /**
     * This is called by JDOHelper to construct a PM factory from a properties
     * instance.
     */
    public static javax.jdo.PersistenceManagerFactory 
    				getPersistenceManagerFactory(Properties props) {
        return new PersistenceManagerFactoryImp(props,
                PersistenceManagerFactoryImp.class.getClassLoader());
    }
    
    public PersistenceManagerFactoryImp(Properties props,
            ClassLoader loader) {
        super(props, loader);
        boolean ok = false;
        try {
            initLogDownloader(config, loader);
            initMetrics(config);
            if (pmfServers != null) {
                for (int i = 0; i < pmfServers.length; i++) {
                    PMFServer s = pmfServers[i];
                    s.init(this);
                    if (s instanceof HasMetrics) {
                        metricSnapshotStore.addSource((HasMetrics)s);
                    }
                }
            }
            startLogDownloader(config);
            metricSnapshotStore.start(Utils.removePassword(config.url));
            if (pmfServers != null) {
                for (int i = 0; i < pmfServers.length; i++) {
                    try {
                        pmfServers[i].start();
                    } catch (Exception e) {
                        throw handleException(e);
                    }
                }
            }
            ok = true;
        } finally {
            if (!ok) {
                try {
                    close();
                } catch (Throwable e) {
                    // ignore - already busy with an exception
                }
            }
        }
    }

    /**
     * Create our StorageManagerFactory.
     */
    protected StorageManagerFactory createStorageManagerFactory() {
        createPMFServers();
        releaseHyperdriveBytecode = pmfServers == null
                && "true".equals(props.getProperty(RELEASE_HYPERDRIVE_BYTECODE));
        StorageManagerFactoryBuilder b = getStorageManagerFactoryBuilder(); 
        b.setLogEventStore(pes);
        b.setConfig(config);
        b.setLoader(loader);
        b.setCache(cache);
        b.setKeepHyperdriveBytecode(!releaseHyperdriveBytecode);
        b.setIgnoreConFactoryProperties(Boolean.getBoolean(props.getProperty(
                ConfigParser.VERSANT_IGNORE_CON_FACT_PROPS, "false")));
        StorageManagerFactory ans = b.createStorageManagerFactory();
        hyperdriveBytecode = b.getHyperdriveBytecode();
        hyperdriveBytecodeMaxSize = b.getHyperdriveBytecodeMaxSize();
        return ans;
    }
    /**
     * Create a new StorageManagerFactoryBuilder.
     */
    protected StorageManagerFactoryBuilder getStorageManagerFactoryBuilder() {
        return new StorageManagerFactoryBuilder();
    }

    public void addPMFServer(PMFServer pmfServer) {
        if (releaseHyperdriveBytecode && config.hyperdrive) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Unable to add PMFServer as versant.releaseHyperdriveBytecode " +
                    "property is true");
        }
        addPMFServerImp(pmfServer);
        if (pmfServer instanceof HasMetrics) {
            metricSnapshotStore.addSource((HasMetrics)pmfServer);
        }
    }

    protected void addPMFServerImp(PMFServer pmfServer) {
        if (pmfServers == null) {
            pmfServers = new PMFServer[]{pmfServer};
        } else {
            int n = pmfServers.length;
            PMFServer[] a = new PMFServer[n + 1];
            System.arraycopy(pmfServers, 0, a, 0, n);
            a[n] = pmfServer;
            pmfServers = a;
        }
    }

    /**
     * Export us using whatever remote access protocols have been specified.
     */
    protected void createPMFServers() {
        String ra = config.remoteAccess;
        if (ra == null || "true".equals(ra)) {
            ra = "socket";
        } else if ("false".equals(ra)) {
            return;
        }
        boolean defaultProtocol = "socket".equals(ra);
        for (StringListParser lp = new StringListParser(ra);
                lp.hasNext(); ) {
            String protocol = lp.nextString();
            PMFServer pmfServer = createPMFServer(protocol, defaultProtocol);
            if (pmfServer != null) {
                addPMFServerImp(pmfServer);
            }
        }
    }

    /**
     * Create a PMFServer instance to handle the protocol. If defaultProtocol
     * is true and the properties resource for the protocol cannot be loaded
     * then null is returned. Otherwise an exception is thrown. This handles
     * the case where remote access is on by default but the remote access
     * module is not available.
     */
    private PMFServer createPMFServer(String protocol,
            boolean defaultProtocol) {
        Properties p;
        try {
            p = PropertiesLoader.loadProperties(loader,
                    "openaccess-remote", protocol);
        } catch (IOException e) {
            if (defaultProtocol) {
                return null;
            }
            throw BindingSupportImpl.getInstance().invalidOperation(
                    e.toString(), e);
        }
        String clsName = p.getProperty(PMF_SERVER);
        if (clsName == null) {
            throw BindingSupportImpl.getInstance().internal(
                    PMF_SERVER + " not found in resource " +
                    p.getProperty(PropertiesLoader.RES_NAME_PROP));
        }
        try {
            Class cls = ClassHelper.get().classForName(clsName, true, loader);
            return (PMFServer)cls.newInstance();
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal(e.toString(), e);
        }
    }

    protected void initMetrics(ConfigInfo config) {
        int n = config.userBaseMetrics.size();
        if (n > 0) {
            userMetricValues = new int[n];
            userMetricIndexMap = new HashMap(n * 2);
            userMetrics = new BaseMetric[n];
            for (int i = 0; i < n; i++) {
                ConfigInfo.UserBaseMetric u =
                        (ConfigInfo.UserBaseMetric)config.userBaseMetrics.get(i);
                BaseMetric m = new BaseMetric(u.name, u.displayName, u.category,
                        u.description, u.decimals, u.defaultCalc);
                userMetricIndexMap.put(m.getName(), new Integer(i));
                userMetrics[i] = m;
            }
        }
        metricSnapshotStore = new MetricSnapshotStore(
                config.metricStoreCapacity, config.metricSnapshotIntervalMs);
        metricSnapshotStore.addSource(this);
    }

    protected void startLogDownloader(ConfigInfo config) {
        logDownloaderThread = new Thread(logDownloader,
                "VOA Log Downloader " + Utils.removePassword(config.url));
        logDownloaderThread.setDaemon(true);
        logDownloaderThread.start();
    }

    protected void initLogDownloader(ConfigInfo config, ClassLoader loader) {
        try {
            if (config.logDownloaderClass == null) {
                logDownloader = new LogDownloader();
            } else {
                logDownloader = (VersantBackgroundTask)BeanUtils.newInstance(
                        config.logDownloaderClass, loader,
                        /*CHFC*/VersantBackgroundTask.class/*RIGHTPAR*/);
            }
            BeanUtils.setProperties(logDownloader, config.logDownloaderProps);
            if (logDownloader instanceof LogDownloader) {
                ((LogDownloader)logDownloader).setQuiet(true);
            }
            logDownloader.setPmf(this);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    public synchronized void close() {
        super.close();
        if (pmfServers != null) {
            for (int i = 0; i < pmfServers.length; i++) {
                try {
                    pmfServers[i].close();
                } catch (Throwable e) {
                    // ignore
                }
            }
        }
        if (logDownloaderThread != null) {
            logDownloader.shutdown();
            logDownloaderThread.interrupt();
            logDownloaderThread = null;
        }
        if (metricSnapshotStore != null) {
            metricSnapshotStore.shutdown();
            metricSnapshotStore = null;
        }
    }

    public void setMapping(String s) {
        //todo jdo2, implement this method
    }

    public String getMapping() {
        return null;  //todo jdo2, implement this method
    }

    public Metric[] getMetrics() {
        return metricSnapshotStore.getMetrics();
    }

    public MetricSnapshotPacket getNewMetricSnapshots(int lastId) {
        return metricSnapshotStore.getNewSnapshots(lastId);
    }

    public MetricSnapshotPacket getMostRecentMetricSnapshot(int lastId) {
        return metricSnapshotStore.getMostRecentSnapshot(lastId);
    }

    protected int findUserMetricIndex(String name) {
        Integer ans = (Integer)userMetricIndexMap.get(name);
        if (ans == null) {
            throw BindingSupportImpl.getInstance().invalidOperation(
                    "Unknown user-defined Metric: '" + name + "'");
        }
        return ans.intValue();
    }

    public void setUserMetric(String name, int value) {
        userMetricValues[findUserMetricIndex(name)] = value;
    }

    public synchronized void incUserMetric(String name, int delta) {
        userMetricValues[findUserMetricIndex(name)] += delta;
    }

    public int getUserMetric(String name) {
        return userMetricValues[findUserMetricIndex(name)];
    }

    public void addMetrics(List list) {
        list.add(metricEvents);
        list.add(metricLastEventId);
        list.add(metricPMCreated);
        list.add(metricPMClosed);
        list.add(metricPMClosedAuto);
        list.add(metricPMClosedAutoTx);
        list.add(metricPMCount);
        if (pmPool != null) {
            pmPool.addMetrics(list);
        }
        if (smf instanceof HasMetrics) {
            ((HasMetrics)smf).addMetrics(list);
        }
        if (userMetrics != null) {
            list.addAll(Arrays.asList(userMetrics));
        }
    }

    public void sampleMetrics(int[][] buf, int pos) {
        buf[metricEvents.getIndex()][pos] = pes.getEventsLogged();
        buf[metricLastEventId.getIndex()][pos] = LogEvent.getLastId();
        buf[metricPMCreated.getIndex()][pos] = pmCreatedCount;
        buf[metricPMClosed.getIndex()][pos] = pmClosedCount;
        buf[metricPMClosedAuto.getIndex()][pos] = pmClosedAutoCount;
        buf[metricPMClosedAutoTx.getIndex()][pos] = pmClosedAutoTxCount;
        buf[metricPMCount.getIndex()][pos] = activePMs.size();
        if (pmPool != null) {
            pmPool.sampleMetrics(buf, pos);
        }
        if (smf instanceof HasMetrics) {
            ((HasMetrics)smf).sampleMetrics(buf, pos);
        }
        if (userMetrics != null) {
            for (int i = userMetrics.length - 1; i >= 0; i--) {
                buf[userMetrics[i].getIndex()][pos] = userMetricValues[i];
            }
        }
    }

    public Map getHyperdriveBytecode() {
        return hyperdriveBytecode;
    }

    public int getHyperdriveBytecodeMaxSize() {
        return hyperdriveBytecodeMaxSize;
    }

    public boolean isLocal() {
        return true;
    }

	public boolean getDetachAllOnCommit() {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}

	public void setDetachAllOnCommit(boolean arg0) {
		// TODO JDO2
		throw BindingSupportImpl.getInstance().invalidOperation("Not implememted");
	}

}

