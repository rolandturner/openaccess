
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
package com.versant.core.jdo.junit;

import junit.framework.Assert;
import junit.framework.TestCase;
import com.versant.core.jdo.PMProxy;
import com.versant.core.metric.Metric;
import com.versant.core.metric.MetricSnapshotPacket;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.config.PropertyConverter;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.metadata.FieldMetaData;
import com.versant.core.common.Utils;
import com.versant.core.common.BindingSupportImpl;
import com.versant.core.jdo.VersantPersistenceManager;
import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.jdo.*;
import com.versant.core.server.*;
import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.JdbcStorageManager;
import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.jdbc.conn.JDBCConnectionPool;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.logging.JdbcLogEvent;
import com.versant.core.vds.logging.VdsLockEvent;
import com.versant.core.storagemanager.StorageCache;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.LRUStorageCache;
import com.versant.core.storagemanager.StorageManager;
import com.versant.core.storagemanager.logging.SmFetchEventBase;
import com.versant.core.logging.LogEventStore;
import com.versant.core.logging.LogEvent;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Base class for JDO Genie test cases. This uses build/compile/test.jdogenie
 * to create the PMF. Setup code in build.xml must have created this and
 * the database tables. This makes it easy to run individual tests from
 * an IDE.
 * <p/>
 * The JDO Genie runtime keeps track of all of the OIDs for all of the
 * instances in the store when Debug.DEBUG is true and the system property
 * versant.tests is "true". The teardown method writes all of these OIDs
 * to a file. The setup method reads this file and deletes all of the
 * OIDs. This gives each test a clean database but data created by a failed
 * test is left in the database for inspection.
 * 
 */
public class VersantTestCase extends TestCase {

    static {
        System.setProperty("versant.tests", "true");
    }

    /**
     * All OIDs that must be deleted before the next test is run are
     * written to this file in teardown. This makes it possible to repeatedly
     * run tests without regenerating the database schema.
     */
    public static final String OIDS_TO_DELETE = "oids-to-delete.txt";

    private boolean unsupported;
    private boolean broken;

    private static VersantPersistenceManagerFactory pmf;
    private static Properties props;
    private static OIDCollector oidCollector = new OIDCollector();

    private int lastEQEventId;
    private int lastEventId;
    private HashMap metricMap;
    private int lastSnapshotId;

    // constants used by some of the older tests
    public static final long TIME1 = 1046071155758L;
    public static final long TIME2 = TIME1 + 1000L;
    public static final long TIME3 = TIME2 + 1000L;
    public static final float ACC_FLOAT = 0.00001f;
    public static final double ACC_DOUBLE = 0.00001;
    public static final long ACC_DATE_MS = 10L;
    public static final long ACC_DATE_MS_ORACLE = 1000L;
    public static final long ACC_DATE_MS_MCKOI = 1000L;
    public static final long ACC_DATE_INTERBASE = 1000L;
    public static final long ACC_DATE_FIREBIRD = 1000L;
    public static final long ACC_DATE_MYSQL = 1000L;
    public static final long ACC_DATE_CACHE = 1000L;

    private DataStoreInfo defaultSubStoreInfo;

    private static final String PROPERTIES_FILE = System.getProperty("config",
            "test.jdogenie");

    public VersantTestCase(String name) {
        super(name);
    }

    public VersantTestCase() {
    }

    private void logNuke(String s) {
        logNuke(s, null);
    }

    private void logNuke(String s, Throwable t) {
        try {
            FileWriter w = new FileWriter("nuke.log", true);
            PrintWriter pw = new PrintWriter(w, false);
            pw.println(s);
            if (t != null) {
                t.printStackTrace(pw);
            }
            pw.flush();
            w.close();
        } catch (IOException e) {
            // ignore
        }
    }

    protected void setUp() throws Exception {
        logNuke("=== " + getName());

        // delete any OIDs left in the database by the last test run
        nukeOidsToDelete();

        // empty level 2 cache
        pmf().evictAll();

        if (!isRemote()) {
            ((LRUStorageCache)getLevel2Cache()).dump(System.out);
        }

        // log an event so we can easily see the test in the event log
        pmf().logEvent(VersantPersistenceManagerFactory.EVENT_NORMAL,
                "=== Start " + getName(), 0);
    }

    private void nukeOidsToDelete() {
        try {
            FileReader fr = new FileReader(OIDS_TO_DELETE);
            BufferedReader br = new BufferedReader(fr);
            HashSet set = new HashSet();
            for (; ;) {
                String s = br.readLine();
                if (s == null) break;
                set.add(s);
            }
            br.close();
            if (!set.isEmpty()) {   // nuke em, ignoring all errors
                nuke(set);
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (Exception e) {
            logNuke("FAILED: " + e, e);
        }
    }

    private void nuke(HashSet set) {
        PersistenceManager pm = null;
        try {
            pm = pmf().getPersistenceManager();
            ArrayList found = new ArrayList();
            pm.currentTransaction().setNontransactionalRead(true);
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                Object o = lookup(pm, (String)i.next());
                if (o != null) {
                    found.add(o);
                }
            }
            try {
                pm.currentTransaction().begin();
                pm.deletePersistentAll(found);
                pm.currentTransaction().commit();
            } catch (Exception e) {
                logNuke("FAILED: " + e, e);
            }
        } finally {
            if (pm != null) {
                try {
                    pm.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private Object lookup(PersistenceManager pm, String s) {
        try {
            int i = s.indexOf(' ');
            Class cls = getJdoMetaData().getClassMetaData(s.substring(0, i)).cls;
            Object oid = pm.newObjectIdInstance(cls, s.substring(i + 1));
            return pm.getObjectById(oid, true);
        } catch (Exception e) {
            // ignore
            logNuke("Lookup failed " + s + ": " + e, e);
            return null;
        }
    }

    /**
     * Catch tests that leave JDBC connections open. These sort of bugs in
     * tests make the whole test environment unstable.
     */
    protected void runTest() throws Throwable {
        super.runTest();
        checkForOpenConnections();
    }

    public void checkForOpenConnections() {
        if (!isDataSource() && isJdbc() && !isRemote()) {
            JDBCConnectionPool p = getJdbcConnectionPool();
            assertTrue("Test left JDBC connections open",
                    0 == p.getActiveCount());
        }
    }

    protected void tearDown() throws Exception {
        // log event so we can see where test ended in the event log
        pmf().logEvent(VersantPersistenceManagerFactory.EVENT_NORMAL,
                "=== Finish " + getName(), 0);

        // save all OIDs of objects left in the db by the test in a file so
        // they can be deleted by setup of the next test
        FileWriter fw = new FileWriter(OIDS_TO_DELETE);
        PrintWriter pw = new PrintWriter(fw, false);
        for (Iterator i = oidCollector.oids.iterator(); i.hasNext();) {
            pw.println((String)i.next());
        }
        pw.close();
        oidCollector.oids.clear();

        // close any PMs left open by the test
        List list = pmf().getPersistenceManagers();
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            PersistenceManager pm = (PersistenceManager)i.next();
            if (!pm.isClosed()) {
                try {
                    if (pm.currentTransaction().isActive()) {
                        pm.currentTransaction().rollback();
                    }
                } catch (Exception e) {
                    // ignore
                }
                try {
                    pm.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        if (!isRemote()) {
            try {
                ((LRUStorageCache)getLevel2Cache()).dump(System.out);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Get the shared PMF. This will be created if not already done.
     */
    public VersantPersistenceManagerFactory pmf() {
        if (pmf == null) {
            pmf = (VersantPersistenceManagerFactory)JDOHelper.getPersistenceManagerFactory(
                    getProperties());
            if (isRemote()) {
                setLogEventsToSysOut(false);
            }
            pmf.addInstanceLifecycleListener(oidCollector, null);
        }
        return pmf;
    }

    /**
     * Get the shared server properties.
     * First tries to load the <code>PROPERTIES_FILE</code> as a file name, if it exists.
     * Otherwise, load it as a resource using current classloader.
     */
    public Properties getProperties() {
        if (props == null) {
            props = new Properties();
            InputStream is = null;
            try {
                is = new FileInputStream(PROPERTIES_FILE);
            } catch (FileNotFoundException ex){
                is = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(PROPERTIES_FILE);
            }
            if (is == null) {
                throw new RuntimeException(
                        "not found " + PROPERTIES_FILE
                        + " for " + Thread.currentThread().getContextClassLoader());
            }
            try {
                props.load(is);
                is.close();
                PropertyConverter.convert(props);
                String host = System.getProperty("versant.host");
                if (host != null && host.length() > 0) {
                    props.setProperty("versant.host", host);
                }
                props.setProperty(ConfigParser.HYPERDRIVE,
                        System.getProperty("hyperdrive", "false"));
                props.setProperty(ConfigParser.STORE_TEST_WHEN_IDLE, "false");
                return props;
            } catch (IOException e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
        return props;
    }

    /**
     * Are we running against a JDBC database?
     */
    public boolean isJdbc() {
        return getSubStoreInfo().isJdbc();
    }

    /**
     * Are we running with a JDBC DataSource?
     */
    public boolean isDataSource() {
        return getSubStoreInfo().isDataSource();
    }

    /**
     * Are we running against a VDS database?
     */
    public boolean isVds() {
        return Utils.isVersantDatabaseType(getSubStoreInfo().getDataStoreType());
    }

    /**
     * Is embedded PC field supported?
     */
	public boolean isEmbeddedPCFieldSupported() {
		return !isVds();
	}

	public boolean isQueryNullElementInCollectionSupported() {
		return !isVds();
	}

	public boolean isSetResultSupported() {
		return !isVds();
	}

	public boolean isUnboundVariableSupported() {
		return !isVds();
	}
	
	public boolean isQueryOnBigNumberSupported() {
		return !isVds();
	}

	public boolean isManagedRelationshipSupported() {
		return !isVds();
	}

	public boolean isSQLSupported() {
		return !isVds();
	}

	public boolean isOderingOnThisSupported() {
		return !isVds();
	}

	public boolean isExtraJavaTypeSupported() {
		return !isVds();
	}

	/**
     * Get the name of the database we are running against (oracle, mysql, vds
     * etc.).
     */
    public String getDbName() {
        return getSubStoreInfo().getDataStoreType();
    }

    public DataStoreInfo getSubStoreInfo() {
        if (defaultSubStoreInfo == null) {
            defaultSubStoreInfo = pmf().getDataStoreInfo(null);
        }
        return defaultSubStoreInfo;
    }

    public DataStoreInfo getSubStoreInfo(String name) {
        if (name == null) return getSubStoreInfo();
        return pmf().getDataStoreInfo(name);
    }

    /**
     * Get the level 2 cache. Throws an exception if using a remote PMF.
     */
    public StorageCache getLevel2Cache() {
        Assert.assertTrue("local PMF",
                pmf instanceof PersistenceManagerFactoryImp);
        return ((PersistenceManagerFactoryImp)pmf).getStorageCache();
    }

    /**
     * Get the innermost SMF. Throws an exception if using a remote PMF.
     */
    public StorageManagerFactory getInnerSmf() {
        Assert.assertTrue("local PMF",
                pmf instanceof PersistenceManagerFactoryImp);
        StorageManagerFactory i =
                ((PersistenceManagerFactoryImp)pmf).getStorageManagerFactory();
        for (;;) {
            StorageManagerFactory next = i.getInnerStorageManagerFactory();
            if (next == null) return i;
            i = next;
        }
    }

    /**
     * Get the JDBCConnectionPool for the default datastore.
     */
    public JDBCConnectionPool getJdbcConnectionPool() {
        return (JDBCConnectionPool)getJdbcSmf().getConnectionSource();
    }

    /**
     * Get the ConnectionSource for the default datastore.
     */
    public JdbcConnectionSource getJdbcConnectionSource() {
        return getJdbcSmf().getConnectionSource();
    }

    /**
     * Get the JdbcStorageManagerFactory or throw an exception if not using
     * JDBC or if the pmf is remote.
     */
    public JdbcStorageManagerFactory getJdbcSmf() {
        assertTrue("JDBC", isJdbc());
        return (JdbcStorageManagerFactory)getInnerSmf();
    }

    /**
     * Get the event ring buffer.  Throws an exception if using a remote PMF.
     */
    public LogEventStore getPerfEventStore() {
        VersantPersistenceManagerFactory pmf = pmf();
        Assert.assertTrue("local PMF",
                pmf instanceof PersistenceManagerFactoryImp);
        return ((PersistenceManagerFactoryImp)pmf).getLogEventStore();
    }

    /**
     * Get new events and count the number of execQuery events since the last
     * batch.
     */
    public int countExecQueryEvents() {
        LogEvent[] ea = pmf().getNewPerfEvents(lastEQEventId);
        if (ea == null) return 0;
        lastEQEventId = ea[ea.length - 1].getId();
        return countExecQueryEvents(ea);
    }

    /**
     * Count the number of execQuery events in the array.
     */
    public int countExecQueryEvents(LogEvent[] a) {
        if (a == null) return 0;
        int c = 0;
        for (int i = 0; i < a.length; i++) {
            LogEvent pe = a[i];
            if (pe instanceof JdbcLogEvent) {
                JdbcLogEvent e = (JdbcLogEvent)pe;
                if (e.getType() == JdbcLogEvent.STAT_EXEC_QUERY) c++;
            }
        }
        return c;
    }

    /**
     * Get new performance events since the last call.
     */
    public LogEvent[] getEvents() {
        LogEvent[] ea = pmf.getNewPerfEvents(lastEQEventId);
        if (ea != null) {
            lastEQEventId = ea[ea.length - 1].getId();
        }
        return ea;
    }

    /**
     * Count the number of events of type in the array.
     */
    public int countEventsOfType(LogEvent[] events, int type) {
        if (events == null) return 0;
        int count = 0;
        for (int i = 0; i < events.length; i++) {
            LogEvent event = events[i];
            if (event.getType() == type) count++;
        }
        return count;
    }

    /**
     * Return the number of events of each type in an array since the last
     * call.
     */
    public int[] countJdbcEvents(int[] type) {
        int[] ans = new int[type.length];
        LogEvent[] ea = pmf.getNewPerfEvents(lastEventId);
        if (ea != null) {
            lastEventId = ea[ea.length - 1].getId();
            countJdbcEvents(ea, type, ans);
        }
        return ans;
    }

    /**
     * Return the number of events of each type in an array since the last
     * call. Return the result counts in the corresponding positions in ans.
     */
    public void countJdbcEvents(LogEvent[] a, int[] type, int[] ans) {
        for (int i = 0; i < a.length; i++) {
            LogEvent pe = a[i];
            if (pe instanceof JdbcLogEvent) {
                int t = ((JdbcLogEvent)pe).getType();
                for (int j = type.length - 1; j >= 0; j--) {
                    if (type[j] == t) {
                        ans[j]++;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the SQL for the first exec query event found since the last call
     * or null if none.
     */
    public String findExecQuerySQL() {
        LogEvent[] ea = pmf.getNewPerfEvents(lastEQEventId);
        if (ea == null) return null;
        lastEQEventId = ea[ea.length - 1].getId();
        for (int i = 0; i < ea.length; i++) {
            LogEvent pe = ea[i];
            if (pe instanceof JdbcLogEvent) {
                JdbcLogEvent e = (JdbcLogEvent)pe;
                if (e.getType() == JdbcLogEvent.STAT_EXEC_QUERY) {
                    return e.getDescr();
                }
            }
        }
        return null;
    }

    /**
     * Get the SQL for all exec query events found since the last call.
     * Returns String[0] if none.
     */
    public String[] findAllExecQuerySQL() {
        return findAllExecQuerySQL(getNewPerfEvents());
    }

    public LogEvent[] getNewPerfEvents() {
        LogEvent[] a = pmf.getNewPerfEvents(lastEQEventId);
        if (a != null) {
            lastEQEventId = a[a.length - 1].getId();
        }
        return a;
    }

    public String[] findAllExecQuerySQL(LogEvent[] ea) {
        ArrayList ans = new ArrayList();
        if (ea != null) {
            for (int i = 0; i < ea.length; i++) {
                LogEvent pe = ea[i];
                if (pe instanceof JdbcLogEvent) {
                    JdbcLogEvent e = (JdbcLogEvent)pe;
                    if (e.getType() == JdbcLogEvent.STAT_EXEC_QUERY) {
                        ans.add(e.getDescr());
                    }
                }
            }
        }
        String[] a = new String[ans.size()];
        ans.toArray(a);
        return a;
    }

    /**
     * Count the number of Torpedo style hits in the array.
     */
    public int countHits(LogEvent[] ea, StringBuffer buf) {
        if (ea == null) return 0;
        int hits = 0;
        for (int i = 0; i < ea.length; i++) {
            LogEvent pe = ea[i];
            if (pe instanceof JdbcLogEvent) {
                JdbcLogEvent e = (JdbcLogEvent)pe;
                switch (e.getType()) {
                    case JdbcLogEvent.STAT_EXEC_QUERY:
                    case JdbcLogEvent.CON_COMMIT:
                    case JdbcLogEvent.STAT_EXEC_BATCH:
                    case JdbcLogEvent.STAT_EXEC:
                    case JdbcLogEvent.STAT_EXEC_UPDATE:
                        ++hits;
                        if (e.getDescription() == null) {
                            buf.append(e.getName());
                        } else {
                            buf.append(e.getDescription());
                        }
                        buf.append('\n');
                }
            }
        }
        return hits;
    }

    /**
     * Get all the get.state and get.state.multi events since the last call.
     * Returns GetStateEventBase[0] if none.
     */
    public SmFetchEventBase[] findFetchEvents() {
        ArrayList ans = new ArrayList();
        LogEvent[] ea = pmf.getNewPerfEvents(lastEQEventId);
        if (ea != null) {
            lastEQEventId = ea[ea.length - 1].getId();
            for (int i = 0; i < ea.length; i++) {
                LogEvent pe = ea[i];
                if (pe instanceof SmFetchEventBase) {
                    ans.add(pe);
                }
            }
        }
        SmFetchEventBase[] a = new SmFetchEventBase[ans.size()];
        ans.toArray(a);
        return a;
    }

    /**
     * Is there a locking statement (dummy update or 'select for update') in
     * the event log?
     */
    public boolean foundLockSQL() {
        LogEvent[] ea = pmf.getNewPerfEvents(lastEQEventId);
        if (ea == null) return false;
        lastEQEventId = ea[ea.length - 1].getId();
        String s = null;
        if (getSubStoreInfo().getSelectForUpdate() != null) {
            s = new String(getSubStoreInfo().getSelectForUpdate());
        }
        for (int i = 0; i < ea.length; i++) {
            LogEvent pe = ea[i];
            if (pe instanceof JdbcLogEvent) {
                JdbcLogEvent e = (JdbcLogEvent)pe;
                if (s == null) {
                    if (e.getType() == JdbcLogEvent.STAT_EXEC_UPDATE) {
                        return true;
                    }
                } else {
                    if (e.getType() == JdbcLogEvent.STAT_EXEC_QUERY) {
                        return e.getDescr().indexOf(s) >= 0;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lookup a metric by name.
     */
    public Metric lookupMetric(String name) {
        if (metricMap == null) {
            metricMap = new HashMap();
            Metric[] a = pmf().getMetrics();
            for (int i = 0; i < a.length; i++) {
                Metric metric = a[i];
                metricMap.put(metric.getName(), metric);
            }
        }
        Metric ans = (Metric)metricMap.get(name);
        if (ans == null) {
            throw new IllegalArgumentException(
                    "Invalid metric name '" + name + "'");
        }
        return ans;
    }

    /**
     * Get the most recent metric snapshot packet. This will wait until new
     * data is available if necessary.
     */
    public MetricSnapshotPacket getMostRecentMetricSnapshot() {
        for (; ;) {
            MetricSnapshotPacket p = pmf().getMostRecentMetricSnapshot(
                    lastSnapshotId);
            if (p != null) {
                lastSnapshotId = p.getMostRecentID();
                return p;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * Get the number of instances in the level 2 cache.
     */
    public int getLevel2CacheSize() {
        VersantPersistenceManagerFactory pmf = pmf();
        if (pmf instanceof PersistenceManagerFactoryImp) {
            // fast cheat method for a local PMF
            return ((LRUStorageCache)getLevel2Cache()).getObjectCount();
        } else {
            // slower metrics method for remote PMF
            MetricSnapshotPacket p = getMostRecentMetricSnapshot();
            return (int)p.getMostRecentValue(lookupMetric("CacheSize"));
        }
    }

    public boolean setLogEventsToSysOut(boolean on) {
//        String[] path = new String[]{"perfEventStore", "logEventsToSysOut"};
//        Boolean oldVal = (Boolean)((PersistenceManagerFactoryBase)pmf()).getServerProperty(
//                path);
//        pmf().setServerProperty(path, Boolean.toString(on));
//        return oldVal.booleanValue();
        return true;
    }

    /**
     * Should tests requiring autoinc column support be run?
     */
    public boolean doAutoIncTests() {
        return getSubStoreInfo().isAutoIncSupported()
                && !getSubStoreInfo().getDataStoreType().equals("pointbase");
    }

    /**
     * Are we testing using a remote PMF?
     */
    public boolean isRemote() {
        return pmf().getClass().getName().toLowerCase().indexOf("remote") >= 0;
    }

    /**
     * Are we testing using Hyperdrive classes?
     */
    public boolean isHyperdrive() {
        Properties p = getProperties();
        String s = p.getProperty(ConfigParser.HYPERDRIVE);
        return s == null || "true".equals(s);
    }

    /**
     * Get ClassMetaData for a class.
     */
    public ClassMetaData getCmd(Class cls) {
        return getJdoMetaData().getClassMetaData(cls);
    }

    /**
     * Get FieldMetaData for a field.
     */
    public FieldMetaData getFieldMetaData(Class cls, String field) {
        return getJdoMetaData().getClassMetaData(cls).getFieldMetaData(field);
    }

    /**
     * Get JdbcClass for a class or throw an exception if not using JDBC or
     * if class does not exist.
     */
    public JdbcClass getJdbcClass(Class cls) {
        assertTrue(isJdbc());
        JdbcClass ans = (JdbcClass)getCmd(cls).storeClass;
        assertNotNull(ans);
        return ans;
    }

    /**
     * Get the runtime meta data.
     */
    public ModelMetaData getJdoMetaData() {
        return ((VersantPMFInternal)pmf).getJDOMetaData();
    }

    /**
     * Nuke all rows in the table for cls.
     */
    public void nuke(Class cls) throws SQLException {
        // no longer required - left over data is automatically deleted when
        // the next test starts
    	if (isVds()) {
	        String db = dequalifiedName(pmf().getConnectionURL(),":");
	        String dbClass = dequalifiedName(cls.getName(), ".");
	        String command = "dropinst -d " + db + " -y " + dbClass;
	        try {
	            System.out.println("$ " + command);
	            Process proc = Runtime.getRuntime().exec(command);
	            proc.waitFor();
	        } catch (Exception ex) {
	            System.err.println(
	                    "Failed to delete instances of " + cls + " with [" + command + "]");
	        }
    	}
    }

    String dequalifiedName(String s, String delimit) {
        int index = s.lastIndexOf(delimit);
        if (index == -1) return s;
        return s.substring(index + 1);
    }

    /**
     * Nuke all rows in the table for classes in a.
     */
    protected void nuke(Class[] a) throws SQLException {
        // no longer required - left over data is automatically deleted after
        // each test
        for (int i = 0; i < a.length; i++) nuke(a[i]);
    }

    /**
     * Get basic information about the test configuration in a String.
     */
    public String getConfigurationSummary() {
        StringBuffer s = new StringBuffer();
        String c = System.getProperty("tests.cfg");
        if (c != null) {
            s.append(c);
            s.append(' ');
        }
        s.append(getDbName());
        if (isHyperdrive()) s.append(" hyperdrive");
        if (isRemote()) s.append(" remote");
        s.append(' ');
        s.append(pmf().getConnectionURL());
        return s.toString();
    }

    /**
     * Tests that are not run for the current configuration must call this
     * method and then return.
     */
    public void unsupported() {
        unsupported = true;
    }

    /**
     * Tests that have been disabled must call this method and then return.
     */
    public void broken() {
        broken = true;
    }

    public boolean isUnsupported() {
        return unsupported;
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean isMySQL3() {
        DataStoreInfo info = getSubStoreInfo();
        return info.getDataStoreType().equals("mysql")
                && info.getMajorVersion() < 4;
    }

    /**
     * Set the datastoreTxLocking on pm to LOCKING_NONE.
     */
    public void lockNone(PersistenceManager pm) {
        ((VersantPersistenceManager)pm).setDatastoreTxLocking(
                VersantPersistenceManager.LOCKING_NONE);
    }

    /**
     * Get the number of pcleanToDirtyNotification's on the VdsDataStore
     * since the last call to this method. Throws an exception if not using
     * VDS.
     */
    public int getVdsPCleanToDirtyNotificationCount() {
        final LogEvent[] a = getEvents();
        if (a == null) {
            return 0;
        }
        int c = 0;
        for (int i = a.length - 1; i >= 0; i--) {
            if (a[i] instanceof VdsLockEvent) {
                ++c;
            }
        }
        return c;
    }

    /**
     * Is the pm using weak references?
     */
    public boolean isWeakRefs(PersistenceManager pm) {
        if (pm instanceof VersantPersistenceManagerImp) {
            return ((VersantPersistenceManagerImp)pm).getCache().getCurrentRefType()
                    == VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK;
        } else {
            return ((PMProxy)pm).getRealPM().getCache().getCurrentRefType()
                    == VersantPersistenceManager.PM_CACHE_REF_TYPE_WEAK;
        }
    }

    /**
     * Does the default datastore support application identity?
     */
    public boolean isApplicationIdentitySupported() {
        // change this when VDS has app id support
        boolean result = !isVds();
        if (!result) logFilter("AppID");
        return result;
    }

    /**
     * Is the connection pinned to the PM in an opt tx?
     */
    public boolean isConnectionPinnedInOptTx() {
        return isVds();
    }

    /**
     * Is datastore tx locking mode LOCKING_NONE supported?
     */
    public boolean isDatastoreTxLockingNoneSupported() {
        return !isVds();
    }

    /**
     * Are collections of Object supported (i.e. any PC class)?
     */
    public boolean isCollectionOfObjectSupported() {
        return isVds();
    }

    /**
     * Are null and empty collections distinguished?
     */
    public boolean isNullCollectionSupported() {
        return isVds();
    }

    /**
     * Get the StorageManager for the PM.
     */
    public StorageManager getStorageManager(PersistenceManager pm) {
        VersantPersistenceManagerImp rpm;
        if (pm instanceof UnsynchronizedPMProxy) {
            rpm = ((UnsynchronizedPMProxy)pm).getRealPM();
        } else {
            rpm = ((SynchronizedPMProxy)pm).getRealPM();
        }
        return rpm.getStorageManager();
    }

    /**
     * Is the PM currently holding a datastore connection?
     */
    public boolean hasDatastoreConnection(PersistenceManager pm) {
        return getStorageManager(pm).hasDatastoreConnection();
    }

    /**
     * How many open query results does the PM have?
     */
    public int getOpenQueryResultCount(PersistenceManager pm) {
        Map m = getStorageManager(pm).getStatus();
        try {
            return ((Integer)m.get(JdbcStorageManager.STATUS_OPEN_QUERY_RESULT_COUNT)).intValue();
        } catch (Exception e) {
            throw BindingSupportImpl.getInstance().internal(e.toString(), e);
        }
    }

    static PrintWriter filterFile = null;
    static int filterCount = 0;

    static {
        try {
            filterFile = new PrintWriter(
                    new FileOutputStream("filter.out", false));
        } catch (IOException ex) {

        }
    }

    protected void logFilter(String reason) {
        filterCount++;
        filterFile.println(filterCount + ". " +
                this.getName() +
                " -- " + reason);
        filterFile.flush();
    }

    /**
     * Collects all OIDs stored and not deleted. These are written to a file
     * after each test. Before each test all the OIDs in the file are deleted.
     * The OIDs are stored in their String form prefixed by the name of the
     * class of the object and a space.
     */
    private static class OIDCollector
            implements StoreLifecycleListener, DeleteLifecycleListener {

        public HashSet oids = new HashSet();

        private String oidToString(Object o) {
            return o.getClass().getName() + " " + JDOHelper.getObjectId(o);
        }

        public void preStore(InstanceLifecycleEvent ev) {
            //Do nothing, we just want postStore
        }

        public void postStore(InstanceLifecycleEvent ev) {
            String s = oidToString(ev.getSource());
            oids.add(s);
        }

        public void preDelete(InstanceLifecycleEvent ev) {
            //oids.remove(oidToString(ev.getSource()));
        }

        public void postDelete(InstanceLifecycleEvent ev) {
            //oids.remove(oidToString(ev.getSource()));
        }
    }

    /**
     * Are we running under JDK 1.4 or newer?
     */
    public static boolean isJDK14orNewer() {
        String v = System.getProperty("java.version");
        return v == null || !v.startsWith("1.3"); // we can ignore 1.2 and older
    }

}

