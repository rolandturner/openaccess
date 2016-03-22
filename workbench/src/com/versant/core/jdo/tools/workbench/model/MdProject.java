
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
package com.versant.core.jdo.tools.workbench.model;

import org.jdom.output.SAXOutputter;
import com.versant.core.common.Debug;
import com.versant.core.common.Utils;
import com.versant.core.jdo.VersantPersistenceManagerFactory;
import com.versant.core.jdo.LogDownloader;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.common.config.PropertyConverter;
import com.versant.core.metadata.*;
import com.versant.core.metadata.parser.JdoRoot;
import com.versant.core.metadata.parser.MetaDataParser;
import com.versant.core.logging.LogEventStore;

//import com.versant.core.cluster.VersantClusterTransport;

import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.JdbcConnectionSource;
import com.versant.core.jdbc.sql.SqlDriver;

import com.versant.core.jdo.tools.workbench.ant.AntRunner;

import com.versant.core.jdo.BootstrapPMF;
import com.versant.core.jdo.VersantBackgroundTask;
import com.versant.core.jdo.*;
import com.versant.core.util.BeanUtils;
import com.versant.core.util.StringListParser;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageManagerFactoryBuilder;

import javax.jdo.JDOFatalUserException;
import java.io.*;

import java.lang.reflect.Field;


import java.lang.reflect.InvocationTargetException;

import java.awt.*;

import java.util.*;
import java.util.List;
import java.sql.Connection;

/**
 * Project settings stored in the .jdogenie properties file. This also
 * maintains a ClassLoader for the project and a 2 tier JDO server.
 */
public class MdProject extends MdBase implements MdChangeListener,
        MdDsTypeChangedListener, MdProjectProvider
 {

    private boolean dirty;
    private Logger logger;

    private File file;
    private List classPathList = new ArrayList();   // of File
    private File srcDir;

    private String description = "";
    private String serverName = "versant";
    private String remoteHost;
    private boolean allowRemotePMs = ConfigParser.DEFAULT_ALLOW_REMOTE_PMS;
    private String remoteUsername;
    private String remotePassword;
    private String rmiRegistryPort;
    private String serverPort;
    private String defaultFlushThreshold;
    private boolean allowPmCloseWithOpenTx;
    private boolean precompileNamedQueries = ConfigParser.DEFAULT_PRECOMPILE_NAMED_QUERIES;
    private boolean checkModelConsistencyOnCommit;
    private String testing;

    private boolean pmpoolEnabled = true;
    private String pmpoolMaxIdleT;
    private boolean remotePmpoolEnabled = true;
    private String remotePmpoolMaxIdleT;
    private String remotePmpoolMaxActiveT;

    private String remoteProtocols;

    private boolean hyperdrive = true;
    private List jdoFileList = new ArrayList();     // of MdJdoFile
    private MdDataStore dataStore = null;
    private List allClasses = new ArrayList(); // of MdClass
    private List allClassNames = new ArrayList();   // of String
    private List allInterfaces = new ArrayList(); // of MdInterface
    private List allInterfaceNames = new ArrayList();   // of String
    private List allElementNames = new ArrayList(); // of String
    private HashMap classTypeMap = new HashMap();   // Class -> MdClass/MdInterface
    private List externalizerList = new ArrayList(); // of MdExternalizer
    private List remoteList = new ArrayList(); // of MdRemoteProtocol

    private String pmfClassName = BootstrapPMF.class.getName();
    private boolean optimistic = true;
    private boolean retainValues = false;
    private boolean restoreValues = false;
    private boolean ignoreCache = false;
    private boolean nontransactionalRead = ConfigParser.DEFAULT_OPTION_NON_TRANSACTIONAL_READ;
    private boolean nontransactionalWrite = false;
    private boolean multithreaded = false;
    private String datastoreTxLocking;
    private String pmCacheRefType;

    private MdPropertySet perfProps;

    private boolean useCache = true;
    private String cacheMaxObjectsT = "10000";
    private String cacheListenerClass;
    private MdPropertySet cacheListenerProps;
    private boolean queryCacheEnabled = true;
    private String maxQueriesToCacheT = Integer.toString(
            ConfigParser.DEFAULT_CACHE_MAX_QUERIES);
    private String logDownloaderClass;
    private MdPropertySet logDownloaderProps;

    private String metricStoreCapacity;
    private String metricSnapshotIntervalMs;
    private List userBaseMetrics = new ArrayList(); // of MdUserBaseMetric

    private File lastClasspathDir;
    private File lastJdoFileDir;
    private ClassLoader projectClassLoader;




    private boolean antDisabled;
    private String antRunTarget;
    private AntRunner antRunner = new AntRunner();
    private String antCompile = "";
    private boolean antShowAllTargets;


    // the server running in the Workbench
    private VersantPMFInternal pmf;

    // these fields are filled when the meta data is parsed or the server starts
    private ModelMetaData modelMetaData;
    private StorageManagerFactory smf;
    private boolean metaDataUpToDate;
    private Exception metaDataError;
    private boolean metaDataCompileBusy;

    private final Object parseDatabaseLock = new Object();
    private DatabaseMetaData databaseMetaData;
    private boolean databaseUpToDate;
    private Exception databaseMetaDataError;

    private Properties tokenProps;
    private File tokenPropsFile;

    private MetaDataUtils mdutils = new MetaDataUtils();

    private List classDiagramList = new ArrayList();     // of ClassDiagram

    private static final List ELEMENT_TYPE_NAMES = Arrays.asList(new String[]{
        "java.lang.Boolean",
        "java.lang.Byte",
        "java.lang.Character",
        "java.lang.Double",
        "java.lang.Float",
        "java.lang.Long",
        "java.lang.Integer",
        "java.lang.Short",
        "java.lang.String",
        "java.math.BigDecimal",
        "java.math.BigInteger",
        "java.util.Date",
        "java.util.Locale",
    });

    private boolean disableHyperdriveInWorkbench = true;
    public File currentQueryDir;
    private boolean upgraded = false;
    private int propFillMode = PropertySaver.PROP_FILL_MODE_KEEP;
    private boolean splitPropFile = true;
    private Properties loadedProperties;

    private static final String REMOTE_PROTOCOLS = "workbench.remoteProtocols";

    public MdProject(Logger logger, File file) throws Exception {
        this.logger = logger;
        this.file = file;

        if (file != null) {
            lastClasspathDir = lastJdoFileDir = file.getParentFile();
        }

        perfProps = new MdPropertySet(this, true);
        perfProps.setBean(new LogEventStore());
        cacheListenerProps = new MdPropertySet(this, true);
        logDownloaderProps = new MdPropertySet(this, true);
    }

    public void initNewProject() throws Exception {
        createCacheListener();
        createLogDownloader();
    }


    public AntRunner getAntRunner() {
        return antRunner;
    }


    public boolean isAllowPmCloseWithOpenTx() {
        return allowPmCloseWithOpenTx;
    }

    public void setAllowPmCloseWithOpenTx(boolean allowPmCloseWithOpenTx) {
        this.allowPmCloseWithOpenTx = allowPmCloseWithOpenTx;
    }

    public boolean isPrecompileNamedQueries() {
        return precompileNamedQueries;
    }

    public void setPrecompileNamedQueries(boolean precompileNamedQueries) {
        this.precompileNamedQueries = precompileNamedQueries;
    }

    public boolean isCheckModelConsistencyOnCommit() {
        return checkModelConsistencyOnCommit;
    }

    public void setCheckModelConsistencyOnCommit(boolean on) {
        checkModelConsistencyOnCommit = on;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    public boolean isAntDisabled() {
        return antDisabled;
    }

    public void setAntDisabled(boolean antDisabled) {
        this.antDisabled = antDisabled;
    }

    public void setAntRunTarget(String antRunTarget) {
        this.antRunTarget = antRunTarget;
        setDirty(true);
    }

    public String getAntRunTarget() {
        return antRunTarget;
    }

    public File getBuildFile() {
        return antRunner.getBuildFile();
    }

    public void setBuildFile(File buildFile) {
        antRunner.setBuildFile(buildFile);
    }

    public boolean isAntShowAllTargets() {
        return antShowAllTargets;
    }

    public void setAntShowAllTargets(boolean antShowAllTargets) {
        this.antShowAllTargets = antShowAllTargets;
    }

    public String getAntCompileStr() {
        return antCompile;
    }

    public MdValue getAntCompile() {
        if (antCompile == null || antCompile.equals("")) {
            antCompile = antRunner.getPossibleEnhanceTarget();
            if (antCompile == null) antCompile = "";
        }
        MdValue v = new MdValue(antCompile);
        v.setPickList(antRunner.getTargetsList());
        return v;
    }

    public void setAntCompile(MdValue v) {
        antCompile = v.getText();
    }



    public String getTitle() {
        if (file == null) {
            return "(new project)";
        } else {
            return file.toString();
        }
    }

    public boolean isDirty() {
        if (dirty) return true;
        if (dataStore.isDirty()) return true;
        for (int i = jdoFileList.size() - 1; i >= 0; i--) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            if (f.isDirty()) return true;
        }
        for (int i = classDiagramList.size() - 1; i >= 0; i--) {
            ClassDiagram d = (ClassDiagram)classDiagramList.get(i);
            if (d.isDirty()) return true;
        }
        try {
            if(!loadedProperties.equals(toProperties(false))){
                dirty = true;
                return true;
            }
        } catch (IOException e) {
            // Do Nothing
        }
        return false;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            fireProjectEvent(MdProjectEvent.ID_DIRTY_FLAG);
        }
        if(!dirty){
            try {
                loadedProperties = toProperties(false);
            } catch (IOException e) {
                // Do Nothing
            }
        }
    }

    /**
     * An MdDataStore we have has changed. Pass on the event.
     */
    public void metaDataChanged(MdChangeEvent e) {
        fireProjectEvent(MdProjectEvent.ID_DATA_STORE_CHANGED);
    }

    /**
     * Get the properties used to replace Ant filter tokens in datastore
     * settings.
     */
    public Properties getTokenProps() {
        return tokenProps;
    }

    public File getTokenPropsFile() {
        return tokenPropsFile;
    }

    public void setTokenPropsFile(File tokenPropsFile) throws Exception {
        this.tokenPropsFile = tokenPropsFile;
        tokenProps = null;
        if (tokenPropsFile == null) return;
        tokenProps = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(tokenPropsFile);
            tokenProps.load(in);
        } finally {
            if (in != null) in.close();
            if (dataStore != null) {
                dataStore.tokenPropsChanged();
            }
        }
    }

    public List getJdoFileList() {
        return jdoFileList;
    }

    public void addJdoFile(MdJdoFile f) {
        jdoFileList.add(f);
        try {
            f.getDocument();
        } catch (Exception e) {
            getLogger().error(e);
        }
        syncAllClassesAndInterfaces();
        reloadProjectClasses();
        setDirty(true);
    }

    public void removeJdoFile(MdJdoFile f) {
        jdoFileList.remove(f);
        syncAllClassesAndInterfaces();
        reloadProjectClasses();
        setDirty(true);
    }

    /**
     * Find package(s) with name or null if none.
     */
    public List findPackages(String name) {
        ArrayList a = new ArrayList();
        int len = jdoFileList.size();
        for (int i = 0; i < len; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            MdPackage p = f.findPackage(name);
            if (p != null) a.add(p);
        }
        return a;
    }

    public List getAllClasses() {
        return allClasses;
    }

    /**
     * Get the names of all PC classes.
     */
    public List getAllClassNames() {
        return allClassNames;
    }

    public List getAllInterfaces() {
        return allInterfaces;
    }

    /**
     * Get the names of all persistent interfaces.
     */
    public List getAllInterfaceNames() {
        return allInterfaceNames;
    }

    /**
     * Get the names of all PC classes and the names of classes that can be
     * stored in a collection or map (e.g. String, Integer et al.).
     */
    public List getAllElementNames() {
        return allElementNames;
    }

    /**
     * Get all the MdPackage's sorted by name.
     */
    public List getPackages() {
        ArrayList a = new ArrayList();
        int n = jdoFileList.size();
        for (int i = 0; i < n; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            a.addAll(f.getPackageList());
        }
        Collections.sort(a);
        return a;
    }

    /**
     * Get all the MdQuery's sorted by name.
     */
    public List getQueries(String search) {
        ArrayList a = new ArrayList();
        for (Iterator iter = allClasses.iterator(); iter.hasNext();) {
            MdClass mdClass = (MdClass)iter.next();
            a.addAll(mdClass.getQueries(search));
        }
        Collections.sort(a);
        return a;
    }

    /**
     * Find the class with fully qualified name.
     */
    public MdClass findClass(String qname) {
        int n = allClasses.size();
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)allClasses.get(i);
            if (c.getQName().equals(qname)) return c;
        }
        return null;
    }

    /**
     * Find the class with fully qualified name.
     */
    public MdClassOrInterface findClassOrInterface(String qname) {
        int n = allClasses.size();
        for (int i = 0; i < n; i++) {
            MdClassOrInterface c = (MdClassOrInterface)allClasses.get(i);
            if (c.getQName().equals(qname)) return c;
        }
        n = allInterfaces.size();
        for (int i = 0; i < n; i++) {
            MdClassOrInterface c = (MdClassOrInterface)allInterfaces.get(i);
            if (c.getQName().equals(qname)) return c;
        }
        return null;
    }

    /**
     * Reload and reanalyze all project classes.
     */
    public void reloadProjectClasses() {
        projectClassLoader = null;
        classTypeMap.clear();
        loadAllClassOrInterfaces(allClasses);
        loadAllClassOrInterfaces(allInterfaces);
        syncAllClassesAndInterfaces();
        stopEngine();
        clearMetaData();
        fireMdChangeEvent(this, null, MdChangeEvent.FLAG_CLASSES_CHANGED);
    }

    /**
     * Load all the MdClassOrInterface's in all and put the Class instances
     * into classTypeMap.
     */
    private void loadAllClassOrInterfaces(List all) {
        int n;
        n = all.size();
        for (int i = 0; i < n; i++) {
            MdClassOrInterface c = (MdClassOrInterface)all.get(i);
            String qname = c.getQName();
            try {
                classTypeMap.put(loadClassImp(qname), c);
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    private void analyzeAllClasses() {
        int n = allClasses.size();
        for (int i = 0; i < n; i++) {
            ((MdClass)allClasses.get(i)).analyze(false);
        }
        for (int i = 0; i < n; i++) {
            ((MdClass)allClasses.get(i)).analyzePass2();
        }
        dataStore.syncFields();
    }

    public void syncAllClassesAndInterfaces() {

        allClasses.clear();
        classTypeMap.clear();
        mdutils.clear();
        allInterfaces.clear();

        // register externalized types so fields of those types will be
        // considered persistent
        int n = externalizerList.size();
        for (int i = 0; i < n; i++) {
            MdExternalizer e = (MdExternalizer)externalizerList.get(i);
            Class cls = loadClass(e.getTypeName());
            mdutils.registerExternalizedType(cls);
        }

        // Register any extended types with mdutils so fields of those types
        // will be considered persistent.
        dataStore.registerExtendedTypes(mdutils);

        n = jdoFileList.size();
        for (int i = 0; i < n; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            f.getAllClasses(allClasses);
            f.getAllInterfaces(allInterfaces);
        }

        // sort all the classes and interfaces by fully qualified name
        Comparator comp = new Comparator() {
            public int compare(Object aa, Object bb) {
                MdClassOrInterface a = (MdClassOrInterface)aa;
                MdClassOrInterface b = (MdClassOrInterface)bb;
                return a.getQName().compareTo(b.getQName());
            }
        };
        Collections.sort(allClasses, comp);
        Collections.sort(allInterfaces, comp);

        allClassNames.clear();
        n = allClasses.size();
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)allClasses.get(i);
            String qname = c.getQName();
            allClassNames.add(qname);
            try {
                classTypeMap.put(loadClassImp(qname), c);
            } catch (Throwable e) {
                // ignore
            }

            c.setDefJdbcClassId(
                    Integer.toString(mdutils.generateClassId(qname)));
            c.setMdDataStore(dataStore);
        }

        // create the subclasses list on each class
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)allClasses.get(i);
            c.getSubclasses().clear();
        }
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)allClasses.get(i);
            String sup = c.getPcSuperclassStr();
            if (sup != null) {
                MdClass q = c.getMdPackage().findClass(sup);
                if (q != null) q.getSubclasses().add(c);
            }
        }

        allInterfaceNames.clear();
        n = allInterfaces.size();
        for (int i = 0; i < n; i++) {
            MdInterface c = (MdInterface)allInterfaces.get(i);
            String qname = c.getQName();
            allInterfaceNames.add(qname);
            try {
                classTypeMap.put(loadClassImp(qname), c);
            } catch (Throwable e) {
                // ignore
            }

            c.setMdDataStore(dataStore);
        }

        // now that we have all the classes in a map they can find their
        // fields
        analyzeAllClasses();

        Collections.sort(allClassNames);
        allElementNames.clear();
        allElementNames.addAll(allClassNames);
        if (isVds()) {
            allElementNames.addAll(allInterfaceNames);
            Collections.sort(allElementNames);
        }
        allElementNames.addAll(0, ELEMENT_TYPE_NAMES);
        fireMdChangeEvent(this, null, MdChangeEvent.FLAG_CLASSES_CHANGED);

    }

    /**
     * Notification from one of our classes that its datastore has changed.
     */
    public void classDataStoreChanged() {
        syncAllClassesAndInterfaces();
    }

    /**
     * Should f be considered persistent by default?
     */
    public boolean isDefaultPersistentField(String storeName, Field f) {
        return mdutils.isDefaultPersistentField(f, classTypeMap);
    }

    /**
     * Can f be persistented?
     */
    public boolean isPersistableField(String storeName, Field f) {
        return mdutils.isPersistableField(f, classTypeMap);
    }

    /**
     * Can f be persistented?
     */
    public boolean isPersistableOnlyUsingExternalization(String storeName,
            Class type) {
        return mdutils.isPersistableOnlyUsingExternalization(type,
                classTypeMap);
    }

    /**
     * What is the category of the field with persistence-modifier pm and
     * type?
     */
    public int getFieldCategory(int pm, Class type) {
        return mdutils.getFieldCategory(pm, type, classTypeMap);
    }

    /**
     * Is a field of type embedded by default?
     */
    public boolean isEmbeddedField(Class type) {
        return mdutils.isEmbeddedType(type);
    }

    public void setDataStore(MdDataStore dataStore) {
        if(this.dataStore == dataStore) return;
        if(this.dataStore != null){
            this.dataStore.removeMdChangeListener(this);
            this.dataStore.removeMdDsTypeChangedListener(this);
            this.dataStore.nuke();
        }
        this.dataStore = dataStore;
        dataStore.addMdChangeListener(this);
        dataStore.addMdDsTypeChangedListener(this);
        syncAllClassesAndInterfaces();
        setDirty(true);
    }

    public MdDataStore getDataStore() {
        return dataStore;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        setDirty(true);
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
        setDirty(true);
    }

    public List getClassPathList() {
        return classPathList;
    }

    public void addClassPathFile(File f) {
        classPathList.add(f);
        projectClassLoader = null;
        setDirty(true);
    }

    public void removeClassPathFile(File f) {
        classPathList.remove(f);
        projectClassLoader = null;
        setDirty(true);
    }

    /**
     * Convert a file to a resource name by searching along the classpath.
     * This only looks at directories. If there is more than one match the
     * one with the shortest resource name is used.
     *
     * @return resource name or null if not found
     */
    public String toResourceName(File file) {
        String fs = file.toString();
        int n = classPathList.size();
        String match = null;
        for (int i = 0; i < n; i++) {
            File f = (File)classPathList.get(i);
            String s = f.toString();
            if (fs.startsWith(s)) {
                String a = fs.substring(s.length());
                if (a.startsWith(File.separator)) a = a.substring(1);
                if (File.separatorChar == '\\') {
                    a = a.replace('\\', '/');
                }
                if (match == null) {
                    match = a;
                } else if (a.length() < match.length()) match = a;
            }
        }
        return match;
    }

    public String toString() {
        if (serverName == null) {
            return "(unnamed)";
        } else {
            return serverName;
        }
    }

    public File getLastClasspathDir() {
        return lastClasspathDir;
    }

    public void setLastClasspathDir(File lastClasspathDir) {
        this.lastClasspathDir = lastClasspathDir;
    }

    public File getLastJdoFileDir() {
        return lastJdoFileDir;
    }

    public void setLastJdoFileDir(File lastJdoFileDir) {
        this.lastJdoFileDir = lastJdoFileDir;
    }

    public void setProjectClassLoader(ClassLoader projectClassLoader) {
        this.projectClassLoader = projectClassLoader;
    }

    /**
     * Get a ClassLoader that loads classes from our classpath.
     */

    public MdClassLoader getProjectClassLoader() throws Exception {
        if (projectClassLoader == null) {
            if (Debug.DEBUG) {
                if (classPathList.isEmpty()) {
                    new RuntimeException("getProjectClassLoader called " +
                            "with empty classPathList - unless the project " +
                            "really has an empty classpath this a bug").printStackTrace(
                                    System.out);
                }
            }
            projectClassLoader = new MdClassLoader(
                    MdUtils.toURLArray(classPathList));
        }
        if(!(projectClassLoader instanceof MdClassLoader)){
            projectClassLoader = new MdClassLoader(
                    MdUtils.toURLArray(classPathList), projectClassLoader);
        }
        return (MdClassLoader)projectClassLoader;
    }
/*END_JAVAVONLY*/


    private Class loadClassImp(String name) throws Exception {

        return BeanUtils.loadClass(name, false, getProjectClassLoader());


    }

    /**
     * Load a class using the project classloader and return null if this
     * fails. This can load primtivies (e.g. "int") and single dimensional
     * arrays.
     */
    public Class loadClass(String name) {
        try {
            return loadClassImp(name);
        } catch (ClassNotFoundException e) {
            getLogger().error(name + ": " + e);
        } catch (Throwable e) {
            getLogger().error(name, e);
        }
        return null;
    }

    /**
     * Load and create an instance of a class using the project classloader.
     * Return null if this fails.
     */

    public Object newInstance(String className) {
        Class c = loadClass(className);
        if (c == null) return null;
        try {
            return c.newInstance();
        } catch (Exception e) {
            Throwable t = e;
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException)e).getTargetException();
            }
            getLogger().error(t);
            return null;
        }
    }


    /**
     * Save this project. This will write to the project file and all modified
     * .jdo files.
     */

    public void save(File prFile) throws Exception {
        this.file = prFile;
        // save properties
        File wbFile = getWorkbenchFile(prFile);
        ByteArrayOutputStream prOut = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream wbOut = new ByteArrayOutputStream(1024);
        saveProps(prOut, wbOut);
        FileOutputStream pr = new FileOutputStream(prFile);
        pr.write(prOut.toByteArray());
        pr.close();
        FileOutputStream wb = new FileOutputStream(wbFile);
        wb.write(wbOut.toByteArray());
        wb.close();
        getLogger().info("Saved project " + this.file);
        int n = jdoFileList.size();
        for (int i = 0; i < n; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            f.save();
        }
        upgraded = false;
        setDirty(false);
    }


    private File getWorkbenchFile(File prFile) {
        String name = prFile.getName();
        int index = name.lastIndexOf('.');
        String wbName = null;
        if (index > 0) {
            wbName = name.substring(0, index) + ".workbench";
        } else {
            wbName = name + ".workbench";
        }
        return new File(prFile.getParent(), wbName);
    }

    private void saveProps(PropertySaver pr, PropertySaver wb,
            boolean forWorkbench) {

        boolean verbose = propFillMode == PropertySaver.PROP_FILL_MODE_VERBOSE;
        if (remoteHost != null && !forWorkbench) {
            pr.add("versant.host", remoteHost);
        }
        if (pmfClassName != null && pmfClassName.startsWith(
                "za.co.hemtech.jdo")) {
            pmfClassName = BootstrapPMF.class.getName();
        }
        pr.add(ConfigParser.PMF_CLASS, pmfClassName);
        pr.add(ConfigParser.OPTION_OPTIMISTIC, forWorkbench || optimistic,
                ConfigParser.DEFAULT_OPTION_OPTIMISTIC);
        pr.add(ConfigParser.OPTION_RETAINVALUES, retainValues,
                ConfigParser.DEFAULT_OPTION_RETAINVALUES);
        pr.add(ConfigParser.OPTION_RESTORE_VALUES, restoreValues,
                ConfigParser.DEFAULT_OPTION_RESTORE_VALUES);
        pr.add(ConfigParser.OPTION_IGNORE_CACHE, ignoreCache,
                ConfigParser.DEFAULT_OPTION_IGNORE_CACHE);
        pr.add(ConfigParser.OPTION_NON_TRANSACTIONAL_READ,
                forWorkbench || nontransactionalRead,
                ConfigParser.DEFAULT_OPTION_NON_TRANSACTIONAL_READ);
        pr.add(ConfigParser.OPTION_NON_TRANSACTIONAL_WRITE, nontransactionalWrite,
                ConfigParser.DEFAULT_OPTION_NON_TRANSACTIONAL_WRITE);
        pr.add(ConfigParser.OPTION_MULTITHREADED, multithreaded,
                ConfigParser.DEFAULT_OPTION_MULTITHREADED);

        // save stores (excluding mappings)
        dataStore.save(pr, forWorkbench, verbose);

        pr.add(ConfigParser.SERVER, serverName, "versant");
        pr.add(ConfigParser.REMOTE_ACCESS,
                forWorkbench ? "false" : getRemoteAccess(), "socket");
        pr.add(ConfigParser.ALLOW_REMOTE_PMS, allowRemotePMs && !forWorkbench,
                ConfigParser.DEFAULT_ALLOW_REMOTE_PMS);
        pr.add("versant.remoteUsername", remoteUsername);
        pr.add("versant.remotePassword", remotePassword);
        for (Iterator i = remoteList.iterator(); i.hasNext(); ) {
            MdRemoteProtocol r = (MdRemoteProtocol)i.next();
            r.saveProps(pr);
        }

        pr.add(ConfigParser.HYPERDRIVE, hyperdrive &&
                !(forWorkbench && isDisableHyperdriveInWorkbench()),
                ConfigParser.DEFAULT_HYPERDRIVE);
        pr.add(ConfigParser.FLUSH_THRESHOLD, defaultFlushThreshold,
                ConfigParser.DEFAULT_FLUSH_THRESHOLD);
        pr.add(ConfigParser.METRIC_STORE_CAPACITY, metricStoreCapacity,
                ConfigParser.DEFAULT_METRIC_STORE_CAPACITY);
        pr.add(ConfigParser.METRIC_SNAPSHOT_INTERVAL_MS,
                metricSnapshotIntervalMs,
                ConfigParser.DEFAULT_METRIC_SNAPSHOT_INTERVAL_MS);
        pr.add(ConfigParser.PMPOOL_ENABLED, pmpoolEnabled,
                ConfigParser.DEFAULT_PMPOOL_ENABLED);
        pr.add(ConfigParser.PMPOOL_MAX_IDLE,
                pmpoolMaxIdleT,
                ConfigParser.DEFAULT_PMPOOL_MAX_IDLE);
        pr.add(ConfigParser.REMOTE_PMPOOL_ENABLED,
                remotePmpoolEnabled,
                ConfigParser.DEFAULT_REMOTE_PMPOOL_ENABLED);
        pr.add(ConfigParser.REMOTE_PMPOOL_MAX_IDLE, remotePmpoolMaxIdleT,
                ConfigParser.DEFAULT_REMOTE_PMPOOL_MAX_IDLE);
        pr.add(ConfigParser.REMOTE_PMPOOL_MAX_ACTIVE,
                remotePmpoolMaxActiveT,
                ConfigParser.DEFAULT_REMOTE_PMPOOL_MAX_ACTIVE);
        pr.add(ConfigParser.DATASTORE_TX_LOCKING, datastoreTxLocking,
                VersantPersistenceManager.LOCKING_FIRST);
        pr.add(ConfigParser.PM_CACHE_REF_TYPE, pmCacheRefType,
                ConfigParser.PM_CACHE_REF_TYPE_SOFT);
        pr.add(ConfigParser.ALLOW_PM_CLOSE_WITH_OPEN_TX,
                forWorkbench || allowPmCloseWithOpenTx,
                ConfigParser.DEFAULT_ALLOW_PM_CLOSE_WITH_OPEN_TX);
        pr.add(ConfigParser.PRECOMPILE_NAMED_QUERIES,
                !forWorkbench && precompileNamedQueries,
                ConfigParser.DEFAULT_PRECOMPILE_NAMED_QUERIES);
        pr.add(ConfigParser.CHECK_MODEL_CONSISTENCY_ON_COMMIT,
                checkModelConsistencyOnCommit,
                ConfigParser.DEFAULT_CHECK_MODEL_CONSISTENCY_ON_COMMIT);
        pr.add(ConfigParser.TESTING, testing, false);

        pr.removePropertyWild(ConfigParser.JDO);
        int n = jdoFileList.size();
        for (int i = 0; i < n; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            pr.add(ConfigParser.JDO + i, f.getResourceName());
        }

        pr.removePropertyWild(ConfigParser.EXTERNALIZER);
        n = externalizerList.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                MdExternalizer e = (MdExternalizer)externalizerList.get(i);
                String base = ConfigParser.EXTERNALIZER + i;
                pr.add(base + ConfigParser.EXTERNALIZER_TYPE,
                        e.getTypeName());
                pr.add(base + ConfigParser.EXTERNALIZER_ENABLED,
                        (e.isEnabled() ? "true" : "false"));
                if (e.getExternalizerStr() != null) {
                    pr.add(base + ConfigParser.EXTERNALIZER_CLASS,
                            e.getExternalizer().getText());
                }
                if (!e.getArgs().isEmpty()) {
                    pr.add(base + ConfigParser.EXTERNALIZER_CLASS + ".", null,
                            e.getArgs(), null);
                }
            }
        }

        pr.add(ConfigParser.EVENT_LOGGING, null,
                perfProps.getValues(),
                perfProps.getDefaultValues());
        HashMap map = logDownloaderProps.getValues();
        HashMap defaults = logDownloaderProps.getDefaultValues();
        if (forWorkbench) {
            map = new HashMap(map);
            map.put("eventBinary", "false");
            map.put("eventText", "false");
            map.put("metricBinary", "false");
        }
        pr.add(ConfigParser.LOG_DOWNLOADER, logDownloaderClass, map, defaults);

        pr.removePropertyWild(ConfigParser.METRIC_USER);
        n = userBaseMetrics.size();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                MdUserBaseMetric u = (MdUserBaseMetric)userBaseMetrics.get(i);
                pr.add(ConfigParser.METRIC_USER + i, u.saveSettings());
            }
        }

        pr.add(ConfigParser.CACHE_ENABLED, useCache,
                ConfigParser.DEFAULT_CACHE_ENABLED);
        pr.add(ConfigParser.CACHE_MAX_OBJECTS,
                forWorkbench ? getCacheMaxObjects() : cacheMaxObjectsT,
                10000);
        if (!forWorkbench) {
            pr.add(ConfigParser.CACHE_CLUSTER_TRANSPORT, cacheListenerClass,
                    cacheListenerProps.getValues(),
                    cacheListenerProps.getDefaultValues());
        }
        pr.add(ConfigParser.QUERY_CACHE_ENABLED, queryCacheEnabled,
                ConfigParser.DEFAULT_QUERY_CACHE_ENABLED);
        pr.add(ConfigParser.QUERY_CACHE_MAX_QUERIES,
                forWorkbench ? getMaxQueriesToCache() : maxQueriesToCacheT,
                ConfigParser.DEFAULT_CACHE_MAX_QUERIES);

        // save store mappings
        if (dataStore != null) {
            dataStore.saveMappings(pr);
        }

        File projectDir = file.getParentFile();
        // Open Access version that last saved this project
        String version = MdUtils.getVersion();
        if (version != null) {
            wb.add("version", version);
        }
        if (!isSplitPropFile()) wb.println();
        wb.println("Workbench properties (Not used at run-time)");
        wb.add(ConfigParser.PROJECT_DESCRIPTION, description);
        wb.add(ConfigParser.PROPERTIES_FILE_MODE, getPropFillModeString(),
                "Preserve");
        if (remoteProtocols != null) {
            wb.add(REMOTE_PROTOCOLS, remoteProtocols);
        }

        // save classpath entries relative to the dir of newFile
        if (srcDir != null) {
            wb.add(ConfigParser.MDEDIT_SRC_PATH,
                    MdUtils.toRelativePath(srcDir, projectDir));
        }
        pr.removePropertyWild(ConfigParser.MDEDIT_CP);
        n = classPathList.size();
        for (int i = 0; i < n; i++) {
            File f = (File)classPathList.get(i);
            wb.add(ConfigParser.MDEDIT_CP + i,
                    MdUtils.toRelativePath(f, projectDir));
        }
        wb.add(ConfigParser.ANT_DISABLED, antDisabled,
                ConfigParser.DEFAULT_ANT_DISABLED);
        if (antRunner.getBuildFile() != null) {
            wb.add(ConfigParser.ANT_BUILDFILE,
                    MdUtils.toRelativePath(antRunner.getBuildFile(),
                            projectDir), "build.xml");
        }
        wb.add(ConfigParser.ANT_COMPILE, antCompile, "compile");
        if (antRunner.getAntArgs() != null) {
            wb.add(ConfigParser.ANT_ARGS, antRunner.getAntArgs());
        }
        wb.add(ConfigParser.ANT_SHOW_ALL_TARGETS, antShowAllTargets,
                ConfigParser.DEFAULT_ANT_SHOW_ALL_TARGETS);
        wb.add(ConfigParser.ANT_RUN_TARGET, antRunTarget);

        if (currentQueryDir != null) {
            wb.add(ConfigParser.SCRIPT_DIR,
                    MdUtils.toRelativePath(currentQueryDir, projectDir), ".");
        } else {
            wb.add(ConfigParser.SCRIPT_DIR,
                    ".", ".");
        }

        // save JDO Genie Class Diagrams
        wb.removePropertyWild(ConfigParser.DIAGRAM);
        n = classDiagramList.size();
        if (n > 0) {
            wb.println();
            wb.println("Open Access Class Diagrams");
            wb.add(ConfigParser.DIAGRAM + ConfigParser.DIAGRAM_COUNT, n);
            for (int i = 0; i < n; i++) {
                wb.println();
                ClassDiagram diagram = (ClassDiagram)classDiagramList.get(i);
                diagram.save(wb, ConfigParser.DIAGRAM + i);
                if (!forWorkbench) diagram.clearDirty();
            }
            if (n > 0) wb.println();
        }

    }

    private boolean isDisableHyperdriveInWorkbench() {
        return disableHyperdriveInWorkbench;
    }

    public void setDisableHyperdriveInWorkbench(
            boolean disableHyperdriveInWorkbench) {
        this.disableHyperdriveInWorkbench = disableHyperdriveInWorkbench;
    }



    /**
     * Load this project. This will load the project file and all .jdo files
     * we can find.
     */

    public void load() throws Exception {
        FileInputStream prIn = null;
        FileInputStream wbIn = null;
        try {
            prIn = new FileInputStream(file);
            Properties p = new Properties();
            p.load(prIn);
            File wbFile = getWorkbenchFile(file);
            if (wbFile != null && wbFile.exists()) {
                wbIn = new FileInputStream(wbFile);
                p.load(wbIn);
            }
            upgraded = PropertyConverter.convert(p);
            logger.info("Loading project " + file);
            String projectDir = file.getAbsoluteFile().getParentFile().toString();

            setPropFillModeString(
                    p.getProperty(ConfigParser.PROPERTIES_FILE_MODE));
            setSplitPropFileString(
                    p.getProperty(ConfigParser.PROPERTIES_SPLIT_FILE, "true"));
            description = p.getProperty(ConfigParser.PROJECT_DESCRIPTION);
            serverName = p.getProperty(ConfigParser.SERVER, "versant");
            remoteHost = p.getProperty("versant.host");
            remoteProtocols = p.getProperty(REMOTE_PROTOCOLS);

            allowRemotePMs = getBoolean(p, ConfigParser.ALLOW_REMOTE_PMS,
                    ConfigParser.DEFAULT_ALLOW_REMOTE_PMS);
            remoteUsername = p.getProperty("versant.remoteUsername");
            remotePassword = p.getProperty("versant.remotePassword");

            hyperdrive = getBoolean(p, ConfigParser.HYPERDRIVE,
                    ConfigParser.DEFAULT_HYPERDRIVE);
            defaultFlushThreshold = p.getProperty(ConfigParser.FLUSH_THRESHOLD);
            datastoreTxLocking = p.getProperty(
                    ConfigParser.DATASTORE_TX_LOCKING);
            pmCacheRefType = p.getProperty(ConfigParser.PM_CACHE_REF_TYPE);
            allowPmCloseWithOpenTx = getBoolean(p,
                    ConfigParser.ALLOW_PM_CLOSE_WITH_OPEN_TX,
                    ConfigParser.DEFAULT_ALLOW_PM_CLOSE_WITH_OPEN_TX);
            precompileNamedQueries = getBoolean(p,
                    ConfigParser.PRECOMPILE_NAMED_QUERIES,
                    ConfigParser.DEFAULT_PRECOMPILE_NAMED_QUERIES);
            checkModelConsistencyOnCommit = getBoolean(p,
                    ConfigParser.CHECK_MODEL_CONSISTENCY_ON_COMMIT,
                    ConfigParser.DEFAULT_CHECK_MODEL_CONSISTENCY_ON_COMMIT);
            testing = p.getProperty(ConfigParser.TESTING);

            metricStoreCapacity = p.getProperty(
                    ConfigParser.METRIC_STORE_CAPACITY);
            metricSnapshotIntervalMs = p.getProperty(
                    ConfigParser.METRIC_SNAPSHOT_INTERVAL_MS);
            userBaseMetrics.clear();
            for (int i = 0; i < ConfigParser.MAX_METRIC_USER_COUNT; i++) {
                String s = p.getProperty(ConfigParser.METRIC_USER + i);
                if (s == null) continue;
                userBaseMetrics.add(new MdUserBaseMetric(s));
            }

            pmpoolEnabled = getBoolean(p, ConfigParser.PMPOOL_ENABLED,
                    ConfigParser.DEFAULT_PMPOOL_ENABLED);
            pmpoolMaxIdleT = p.getProperty(ConfigParser.PMPOOL_MAX_IDLE);
            remotePmpoolEnabled = getBoolean(p,
                    ConfigParser.REMOTE_PMPOOL_ENABLED,
                    ConfigParser.DEFAULT_REMOTE_PMPOOL_ENABLED);
            remotePmpoolMaxIdleT = p.getProperty(
                    ConfigParser.REMOTE_PMPOOL_MAX_IDLE);
            remotePmpoolMaxActiveT = p.getProperty(
                    ConfigParser.REMOTE_PMPOOL_MAX_ACTIVE);

            antDisabled = getBoolean(p, ConfigParser.ANT_DISABLED,
                    ConfigParser.DEFAULT_ANT_DISABLED);
            antRunTarget = p.getProperty(ConfigParser.ANT_RUN_TARGET);
            String s = p.getProperty(ConfigParser.ANT_BUILDFILE);
            if (s == null) s = "build.xml";
            File buildFile = new File(MdUtils.toAbsolutePath(s, projectDir));
            try {
                antRunner.setBuildFile(buildFile);
            } catch (Exception e) {
                if (!antDisabled) logger.error(e);
            }
            antCompile = p.getProperty(ConfigParser.ANT_COMPILE,
                    antRunner.getPossibleEnhanceTarget());
            if (antCompile == null) antCompile = "compile";
            antShowAllTargets = getBoolean(p,
                    ConfigParser.ANT_SHOW_ALL_TARGETS,
                    ConfigParser.DEFAULT_ANT_SHOW_ALL_TARGETS);
            antRunner.setAntArgs(p.getProperty(ConfigParser.ANT_ARGS));

            String sname = "main";
            MdDataStore ds = new MdDataStore(this, sname);
            ds.load(p);
            ds.loadMappings(p);
            String db = p.getProperty(ConfigParser.STORE_DB);
            if (!MdUtils.isStringNotEmpty(db)) {
                String url = ds.getUrl();
                if (url != null) {
                    if (Utils.isVersantURL(url)) {
                        db = "versant";
                    } else {
                        db = SqlDriver.getNameFromURL(url);
                    }
                } else {
                    db = "mysql";
                }
            }
            setDataStore(ds);
            ds.setDbT(db);
            ds.setDirty(false);

            // find all .jdo files
            jdoFileList.clear();
            int n = ConfigParser.MAX_JDO_FILE_COUNT; //look for 1000 .jdo files
            for (int i = 0; i < n; i++) {
                String resName = p.getProperty(ConfigParser.JDO + i);
                if (resName == null) continue;
                MdJdoFile f = new MdJdoFile(this, resName);
                jdoFileList.add(f);
            }

            // Properties for JDOHelper.getPersistenceManagerFactory(...)
            pmfClassName = p.getProperty(ConfigParser.PMF_CLASS);
            optimistic = getBoolean(p, ConfigParser.OPTION_OPTIMISTIC,
                    ConfigParser.DEFAULT_OPTION_OPTIMISTIC);
            retainValues = getBoolean(p, ConfigParser.OPTION_RETAINVALUES,
                    ConfigParser.DEFAULT_OPTION_RETAINVALUES);
            restoreValues = getBoolean(p, ConfigParser.OPTION_RESTORE_VALUES,
                    ConfigParser.DEFAULT_OPTION_RESTORE_VALUES);
            ignoreCache = getBoolean(p, ConfigParser.OPTION_IGNORE_CACHE,
                    ConfigParser.DEFAULT_OPTION_IGNORE_CACHE);
            nontransactionalRead = getBoolean(p,
                    ConfigParser.OPTION_NON_TRANSACTIONAL_READ,
                    ConfigParser.DEFAULT_OPTION_NON_TRANSACTIONAL_READ);
            nontransactionalWrite = getBoolean(p,
                    ConfigParser.OPTION_NON_TRANSACTIONAL_WRITE,
                    ConfigParser.DEFAULT_OPTION_NON_TRANSACTIONAL_WRITE);
            multithreaded = getBoolean(p, ConfigParser.OPTION_MULTITHREADED,
                    ConfigParser.DEFAULT_OPTION_MULTITHREADED);

            // externalizers
            n = ConfigParser.MAX_EXTERNALIZER_COUNT;  //look for 100 externalizers
            for (int i = 0; i < n; i++) {
                s = p.getProperty(ConfigParser.EXTERNALIZER + i +
                        ConfigParser.EXTERNALIZER_TYPE);
                if (s == null) continue;
                MdExternalizer e = new MdExternalizer();
                loadExternalizer(p, ConfigParser.EXTERNALIZER + i, e);
                externalizerList.add(e);
            }

            // event logging
            getClassAndProps(p, ConfigParser.EVENT_LOGGING, perfProps);
            logDownloaderClass = getClassAndProps(p, ConfigParser.LOG_DOWNLOADER,
                    logDownloaderProps);

            // cache
            useCache = getBoolean(p, ConfigParser.CACHE_ENABLED,
                    ConfigParser.DEFAULT_CACHE_ENABLED);
            cacheMaxObjectsT = p.getProperty(ConfigParser.CACHE_MAX_OBJECTS,
                    "10000");
            // get the cache cluster transport twice so old key and new key
            // both work with new key having priority
            cacheListenerClass = getClassAndProps(p,
                    ConfigParser.CACHE_CLUSTER_TRANSPORT, cacheListenerProps);
            if (cacheListenerClass == null) {
                cacheListenerClass = getClassAndProps(p,
                        ConfigParser.CACHE_LISTENER, cacheListenerProps);
            }
            queryCacheEnabled = getBoolean(p, ConfigParser.QUERY_CACHE_ENABLED,
                    ConfigParser.DEFAULT_QUERY_CACHE_ENABLED);
            maxQueriesToCacheT = p.getProperty(
                    ConfigParser.QUERY_CACHE_MAX_QUERIES,
                    Integer.toString(ConfigParser.DEFAULT_CACHE_MAX_QUERIES));

            s = p.getProperty(ConfigParser.MDEDIT_SRC_PATH);
            if (s != null) {
                srcDir = new File(MdUtils.toAbsolutePath(s, projectDir));
            }
            // restore the classpath
            classPathList.clear();
            n = ConfigParser.MAX_MDEDIT_CP_COUNT; // look for 1000 classpaths
            for (int i = 0; i < n; i++) {
                s = p.getProperty(ConfigParser.MDEDIT_CP + i);
                if (s == null) continue;
                File cp = new File(MdUtils.toAbsolutePath(s, projectDir));
                if (!cp.canRead()) {
                    logger.error("Cannot read classpath entry: " + cp);
                }
                classPathList.add(cp);
            }

            // create pluggable beans now that we have a project classloader
            buildRemoteList(p, p.getProperty(ConfigParser.REMOTE_ACCESS));
            try {
                createCacheListener();
            } catch (Exception e) {
                logger.error(e);
            }
            try {
                createLogDownloader();
            } catch (Exception e) {
                logger.error(e);
            }

            // load all the meta data files
            n = jdoFileList.size();
            for (int i = 0; i < n; i++) {
                MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
                try {
                    f.getDocument();
                } catch (Exception e) {
                    getLogger().error(e);
                }
            }

            // load script directory
            String scriptDir = p.getProperty(ConfigParser.SCRIPT_DIR,
                    ".");
            currentQueryDir = new File(
                    MdUtils.toAbsolutePath(scriptDir, projectDir));

            // create Class Diagrams
            classDiagramList.clear();
            syncAllClassesAndInterfaces();
            n = jdoFileList.size();
            for (int i = 0; i < n; i++) {
                MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
                f.setDirty(false);
            }
            n = getInt(p, ConfigParser.DIAGRAM + ConfigParser.DIAGRAM_COUNT,
                    50);
            String base = null;
            for (int i = 0; i < n; i++) {
                base = ConfigParser.DIAGRAM + i;
                String name = p.getProperty(base + ConfigParser.DIAGRAM_NAME);
                if (name == null) continue;
                ClassDiagram d = new ClassDiagram(this);
                d.load(p, base);
                classDiagramList.add(d);
            }
            setDirty(false);
        } catch(Exception e) {
            try {
                syncAllClassesAndInterfaces();
            } catch (Exception x) {
                // ignore
            }
            throw e;
        } finally {
            if (prIn != null) prIn.close();
            if (wbIn != null) wbIn.close();
        }
    }

    private void buildRemoteList(Properties props, String ra) {
        TreeSet protcols = new TreeSet();
        protcols.add("socket");
        protcols.add("http");
        if (remoteProtocols != null) {
            for (StringListParser p = new StringListParser(remoteProtocols);
                    p.hasNext(); ) {
                String protocol = p.nextString();
                if (protocol.length() > 0) protcols.add(protocol);
            }
        }
        HashSet enabled = new HashSet();
        if (ra == null || "true".equals(ra)) {
            enabled.add("socket");
        } else if (!"false".equals(ra) && remoteProtocols != null) {
            for (StringListParser p = new StringListParser(remoteProtocols);
                    p.hasNext(); ) {
                String protocol = p.nextString();
                enabled.add(protocol);
            }
        }
        protcols.addAll(enabled);
        remoteList.clear();
        for (Iterator i = protcols.iterator(); i.hasNext(); ) {
            String protocol = (String)i.next();
            MdRemoteProtocol mdp = new MdRemoteProtocol(this, protocol, props);
            mdp.setExportOnStartup(enabled.contains(protocol));
            remoteList.add(mdp);
        }
    }


    private void loadExternalizer(Properties p, String ext, MdExternalizer e) {
        e.setTypeName(p.getProperty(ext + ConfigParser.EXTERNALIZER_TYPE));
        e.setEnabled(
                getBoolean(p, ext + ConfigParser.EXTERNALIZER_ENABLED, false));
        e.setExternalizerStr(
                p.getProperty(ext + ConfigParser.EXTERNALIZER_CLASS));
        Set all = p.keySet();
        String extProps = ext + ConfigParser.EXTERNALIZER_CLASS + ".";
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            String key = (String)iter.next();
            if (key.startsWith(extProps)) {
                String newKey = key.substring(extProps.length() + 1);
                e.getArgs().put(newKey, p.getProperty(key));
            }
        }
    }

    public void reload() throws Exception {
        try {
            // refrech all the meta data files
            // from memory
            int n = jdoFileList.size();
            for (int i = 0; i < n; i++) {
                MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
                try {
                    f.getDocument();
                    f.analyze();
                } catch (Exception e) {
                    getLogger().error(e);
                }
            }
        } finally {
            reloadProjectClasses();
        }
    }

    private String getClassAndProps(Properties p, String property,
            MdPropertySet props) {
        props.clear();
        String cname = p.getProperty(property);
        Set all = p.keySet();
        String nextKey = property + '.';
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            String s = (String)iter.next();
            if (s.startsWith(nextKey)) {
                String newKey = s.substring(nextKey.length());
                props.getValues().put(newKey, p.getProperty(s));
            }
        }
        return cname;
    }

    /**
     * Attempt to load any jdo files that are not already loaded.
     */
    public void retryJdoFiles() {
        int n = jdoFileList.size();
        for (int i = 0; i < n; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            try {
                f.getDocument();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        syncAllClassesAndInterfaces();
    }

    private int getInt(Properties p, String s, int def) {
        String v = p.getProperty(s);
        if (v == null) return def;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new JDOFatalUserException("Expected int: " + s);
        }
    }

    private boolean getBoolean(Properties p, String s, boolean def) {
        String v = p.getProperty(s);
        if (v == null) return def;
        if (v.equalsIgnoreCase("true")) return true;
        return false;
    }

    /**
     * Find the longest common prefix for all package names.
     */
    public int getCommonPackagePrefixLength() {
        ArrayList a = new ArrayList();
        for (int i = jdoFileList.size() - 1; i >= 0; i--) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            List pl = f.getPackageList();
            for (int j = pl.size() - 1; j >= 0; j--) {
                MdPackage p = (MdPackage)pl.get(j);
                a.add(p.getName());
            }
        }
        return MdUtils.getCommonPrefixLength(a, '.');
    }

    /**
     * Find the longest common prefix for all meta data resource names.
     */

    public int getCommonJdoFilePrefixLength() {
        ArrayList a = new ArrayList();
        for (int i = jdoFileList.size() - 1; i >= 0; i--) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            a.add(f.getResourceName());
        }
        return MdUtils.getCommonPrefixLength(a, '/');
    }


    public String getPmfClassName() {
        return pmfClassName;
    }

    public boolean getOptimistic() {
        return optimistic;
    }

    public void setOptimistic(boolean optimistic) {
        this.optimistic = optimistic;
        setDirty(true);
    }

    public boolean getRetainValues() {
        return retainValues;
    }

    public void setRetainValues(boolean retainValues) {
        this.retainValues = retainValues;
        setDirty(true);
    }

    public boolean getRestoreValues() {
        return restoreValues;
    }

    public void setRestoreValues(boolean restoreValues) {
        this.restoreValues = restoreValues;
        setDirty(true);
    }

    public boolean getIgnoreCache() {
        return ignoreCache;
    }

    public void setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
        setDirty(true);
    }

    public boolean getNontransactionalRead() {
        return nontransactionalRead;
    }

    public void setNontransactionalRead(boolean nontransactionalRead) {
        this.nontransactionalRead = nontransactionalRead;
        setDirty(true);
    }

    public boolean getNontransactionalWrite() {
        return nontransactionalWrite;
    }

    public void setNontransactionalWrite(boolean nontransactionalWrite) {
        this.nontransactionalWrite = nontransactionalWrite;
        setDirty(true);
    }

    public boolean getMultithreaded() {
        return multithreaded;
    }

    public void setMultithreaded(boolean multithreaded) {
        this.multithreaded = multithreaded;
        setDirty(true);
    }

    /**
     * Is the JDO engine running?
     */
    public boolean isEngineRunning() {
        return pmf != null;
    }

    /**
     * 'Save' this project to a Properties object. This will encode any
     * 'use global data source' properties instead of the project
     * properties.
     */
    public Properties toProperties() throws IOException {
        return toProperties(true);
    }
    /**
     * 'Save' this project to a Properties object. This will encode any
     * 'use global data source' properties instead of the project
     * properties.
     */
    public Properties toProperties(boolean forWorkbench) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        PropertySaver ps = new PropertySaver();
        saveProps(ps, ps, forWorkbench);
        ps.store(out);
        Properties p = new Properties();
        p.load(new ByteArrayInputStream(out.toByteArray()));
        return p;
    }

    /**
     * Parse all the in memory MdJdoFile's and return a JdoRoot instance for
     * each. This will use the root cache on each MdJdoFile if possible.
     */
    public JdoRoot[] parseJdoFiles() throws Exception {
        int n = jdoFileList.size();
        JdoRoot[] a = new JdoRoot[n];
        MetaDataParser parser = new MetaDataParser();
        SAXOutputter out = new SAXOutputter(parser);
        for (int i = 0; i < n; i++) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);

            parser.init(f.getResourceName());


            out.output(f.getDocument());
            a[i] = parser.getJdoRoot();
        }
        return a;
    }

    /**
     * Parse the current database meta data. This is a NOP if the meta data
     * is currently up to date and compiled.
     *
     * @see #isDatabaseParsed
     * @see #isDatabaseUpToDate
     */
    public void parseDatabase(boolean quiet, boolean toLowerCase)
            throws Exception {
        synchronized (parseDatabaseLock) {
            try {
                clearDatabaseMetaData();
                if (!quiet) logger.info("Parsing database ...");
                long start = System.currentTimeMillis();
                MdDataStore mdStore = getDataStore();
                if (mdStore != null) {
                    HashMap dbClasses = mdStore.getDatabaseMetaData();
                    if (dbClasses != null) {
                        databaseMetaData = new DatabaseMetaData(dbClasses,
                                toLowerCase);
                    }
                }
                int ms = (int)(System.currentTimeMillis() - start);
                if (!quiet) logger.info("Database parsed in " + ms + " ms");
            } catch (Exception e) {
                databaseMetaDataError = e;
                throw e;
            } finally {
                databaseMetaDataParseComplete();
            }
        }
    }

    /**
     * This is called after the database meta data has been parsed sucessfully or
     * not. It gives the runtime structures (DatabaseMetaData et al.) to the
     * MdClass and MdField instances.
     */
    private void databaseMetaDataParseComplete() {
        // make sure the event notification and updates to classes happen
        // on the event dispatch thread
        Runnable r = new Runnable() {
            public void run() {
                if (databaseMetaData != null) {
                    databaseUpToDate = true;
                }
                MdClass.setDatabaseMetaData(databaseMetaData);
                //allClasses.fireListUpdated();
                fireProjectEvent(MdProjectEvent.ID_PARSED_DATABASE);
            }
        };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }


    }

    /**
     * Parse (compile) the current meta data. This is a NOP if the meta data
     * is currently up to date and compiled.
     *
     * @see #isMetaDataCompiled
     * @see #isMetaDataUpToDate
     * @see #getMetaDataError
     */
    public synchronized void compileMetaData(boolean quiet, boolean force)
            throws Exception {
        if (!force && isMetaDataCompiled() && isMetaDataUpToDate()) return;
        try {
            metaDataCompileBusy = true;
            clearMetaData();

            if (!quiet) logger.info("Compiling meta data ...");
            long start = System.currentTimeMillis();

			
            Properties p = toProperties();
			

            ConfigInfo config = new ConfigParser().parse(p);
            config.jdoMetaData = parseJdoFiles();

            LogEventStore pes = new LogEventStore();
            pes.setLogEvents(LogEventStore.LOG_EVENTS_NONE);

            StorageManagerFactoryBuilder b = new StorageManagerFactoryBuilder();
            b.setLogEventStore(pes);
            b.setConfig(config);
            b.setLoader(getProjectClassLoader());
            b.setOnlyMetaData(true);
            b.setIgnoreConFactoryProperties(true);
            b.setContinueAfterMetaDataError(true);
            smf = b.createStorageManagerFactory();
            modelMetaData = smf.getModelMetaData();

            int ms = (int)(System.currentTimeMillis() - start);
            if (!quiet) logger.info("Meta data compiled in " + ms + " ms");

            // find an exception to throw if there were errors
            if (modelMetaData.hasErrors()) {
                throw modelMetaData.getFirstError();
            }
        } catch (Exception e) {
            metaDataError = e;
            throw e;
        } finally {
            metaDataCompileBusy = false;
            metaDataParseComplete();
        }
    }

    /**
     * This is called after the meta data has been compiled sucessfully or
     * not. It gives the runtime structures (ClassMetaData et al.) to the
     * MdClass and MdField instances.
     */
    private void metaDataParseComplete() {
        if (isMetaDataCompiled()) {
            MdDataStore mdStore = getDataStore();
            if (databaseMetaData != null && mdStore != null) {
                try {
                    databaseMetaData.remap(mdStore.getCurrentJdbcMetaData());
                } catch (Exception e) {
                }
            }
        } else {
            if (databaseMetaData != null) {
                databaseMetaData.unmap();
            }
        }

        // make sure the event notification and updates to classes happen
        // on the event dispatch thread
        Runnable r = new Runnable() {
            public void run() {
                metaDataUpToDate = true;
                List l = allClasses;
                int n = l.size();
                for (int i = 0; i < n; i++) {
                    MdClass c = (MdClass)l.get(i);
                    ClassMetaData cmd;
                    if (modelMetaData == null) {
                        cmd = null;
                    } else {
                        cmd = modelMetaData.getClassMetaData(c.getQName());
                    }
                    c.setClassMetaData(cmd, true);
                }
                //allClasses.fireListUpdated();
                fireProjectEvent(MdProjectEvent.ID_PARSED_META_DATA);
            }
        };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }


    }

    /**
     * Clear the parsed meta data. This will close any database connections
     * that were opened.
     */
    private void clearMetaData() {
        modelMetaData = null;
        if (smf != null) {
            smf.destroy();
            smf = null;
        }
        metaDataUpToDate = false;
        metaDataError = null;
    }

    /**
     * Has an attempt been made to compile the meta data since the last change
     * to the project?
     *
     * @see #isMetaDataCompiled
     * @see #getMetaDataError
     */
    public boolean isMetaDataUpToDate() {
        return metaDataUpToDate;
    }

    /**
     * Is the meta data busy being compiled?
     */
    public boolean isMetaDataCompileBusy() {
        return metaDataCompileBusy;
    }

    /**
     * Clear the parsed database meta data.
     */
    public void clearDatabaseMetaData() {
        databaseMetaData = null;
        databaseUpToDate = false;
        databaseMetaDataError = null;
    }

    /**
     * Has an attempt been made to parse the database since the last change
     * to the project?
     *
     * @see #isDatabaseParsed
     */
    public boolean isDatabaseUpToDate() {
        return databaseUpToDate;
    }

    /**
     * Have we parsed the database? If isDatabaseUpToDate returns true and
     * isDatabaseParsed returns false then there was an error.
     *
     * @see #isDatabaseUpToDate
     */
    public boolean isDatabaseParsed() {
        return databaseMetaData != null;
    }

    /**
     * Do we have compiled meta data? If isMetaDataUpToDate returns true and
     * isMetaDataCompiled returns false then there was an error.
     *
     * @see #isMetaDataUpToDate
     * @see #getMetaDataError
     */
    public boolean isMetaDataCompiled() {
        return modelMetaData != null && metaDataError == null;
    }

    /**
     * What error was encountered compiling the meta data (null if none)?
     *
     * @see #isMetaDataUpToDate
     * @see #isMetaDataCompiled
     */
    public Exception getMetaDataError() {
        return metaDataError;
    }

    /**
     * Are all of the classes enhanced and loaded?
     */

    public boolean isAllClassesEnhanced() {
        for (Iterator i = allClasses.iterator(); i.hasNext();) {
            MdClass c = (MdClass)i.next();
            if (!c.isEnhanced()) return false;
        }
        return true;
    }


    /**
     * Start the JDO engine.
     */
    public void startEngine() throws Exception {
        stopEngine();
        compileMetaData(true, false);
        logger.info("Engine starting ...");
        long start = System.currentTimeMillis();
        Properties p = toProperties();
        p.put(ConfigParser.VERSANT_JDO_META_DATA, parseJdoFiles());
        p.put(ConfigParser.VERSANT_IGNORE_CON_FACT_PROPS, "true");
        pmf = new PersistenceManagerFactoryImp(p, getProjectClassLoader());
        int ms = (int)(System.currentTimeMillis() - start);
        logger.info("Engine started in " + ms + " ms");
        fireProjectEvent(MdProjectEvent.ID_ENGINE_STARTED);
    }

    /**
     * Stop the JDO engine.
     */
    public void stopEngine() {
        if (pmf != null){
            pmf.close();
            logger.info("Engine stopped");
            pmf = null;
            fireProjectEvent(MdProjectEvent.ID_ENGINE_STOPPED);
        }
    }

    /**
     * Get the PM factory. This will be null if the engine has not been
     * started.
     */
    public VersantPersistenceManagerFactory getPmf() {
        return pmf;
    }

    /**
     * Get the meta data if available. This is only available if the meta
     * data has been parsed or the engine is running.
     */
    public ModelMetaData getJdoMetaData() {
        return modelMetaData;
    }

    /**
     * Get the database meta data if available. This is only available if the database meta
     * data has been parsed.
     */
    public DatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    /**
     * Get the JDBC SMF. This will parse the meta data as required.
     */
    public JdbcStorageManagerFactory getJdbcStorageManagerFactory()
            throws Exception {
        if (smf == null) {
            compileMetaData(false, false);
        }
        return (JdbcStorageManagerFactory)getInnerSmf();
    }

    private StorageManagerFactory getInnerSmf() {
        for (StorageManagerFactory i = smf; ; ) {
            StorageManagerFactory next = i.getInnerStorageManagerFactory();
            if (next == null) return i;
            i = next;
        }
    }

    /**
     * Get the JdbcConnectionSource from the SMF. This will parse the meta
     * data as required and will also ensure that a connection pool has been
     * created.
     */
    public JdbcConnectionSource getJdbcConnectionSource() throws Exception {
        JdbcStorageManagerFactory smf = getJdbcStorageManagerFactory();
        smf.createPool(getProjectClassLoader());
        return smf.getConnectionSource();
    }

    /**
     * Get a JDBC Connection.
     */
    public Connection getJdbcConnection(boolean highPriority, boolean autoCommit)
            throws Exception {
        return getJdbcConnectionSource().getConnection(highPriority, autoCommit);
    }

    /**
     * Return a JDBC Connection. This is a NOP if con is null.
     */
    public void returnJdbcConnection(Connection con) {
        if (con != null) {
            try {
                getJdbcConnectionSource().returnConnection(con);
            } catch (Exception e) {
                getLogger().error(e);
            }
        }
    }

    /**
     * One of our MdJdoFile's has changed.
     */
    public void jdoFileChanged() {
        fireProjectEvent(MdProjectEvent.ID_DIRTY_FLAG);
    }

    /**
     * One of our MdQueryFile's has changed.
     */
    public void jdoQueryFileChanged() {
        fireProjectEvent(MdProjectEvent.ID_DIRTY_FLAG);
    }

    //= Event notification ===============================================

    public void addMdProjectListener(MdProjectListener l) {
        listenerList.addListener(l);
    }

    public void removeMdProjectListener(MdProjectListener l) {
        listenerList.removeListener(l);
    }

    /**
     * Fire project event.
     */
    public void fireProjectEvent(int id) {
        fireProjectEvent(id, null);
    }

    /**
     * Fire project event.
     */
    public void fireProjectEvent(int id, Object arg) {
        switch (id) {
            case MdProjectEvent.ID_DATA_STORE_CHANGED:
                databaseUpToDate = false;
            case MdProjectEvent.ID_DIRTY_FLAG:
                metaDataUpToDate = false;
        }
        MdProjectEvent ev = new MdProjectEvent(this, id);
        ev.setArg(arg);
        Iterator listeners = listenerList.getListeners(/*CHFC*/MdProjectListener.class/*RIGHTPAR*/);
        while (listeners.hasNext()) {
            MdProjectListener listener = (MdProjectListener)listeners.next();
            listener.projectChanged(ev);
        }
    }

    public boolean getUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
        setDirty(true);
        if (!useCache) {
            setQueryCacheEnabled(false);
        }
    }

    public boolean isQueryCacheEnabled() {
        return queryCacheEnabled;
    }

    public void setQueryCacheEnabled(boolean queryCacheEnabled) {
        this.queryCacheEnabled = queryCacheEnabled;
        setDirty(true);
    }

    public String getMaxQueriesToCache() {
        return resolveToken(maxQueriesToCacheT);
    }

    public String getMaxQueriesToCacheT() {
        return maxQueriesToCacheT;
    }

    public void setMaxQueriesToCacheT(String value) {
        this.maxQueriesToCacheT = value;
        setDirty(true);
    }

    public String getCacheMaxObjects() {
        return resolveToken(cacheMaxObjectsT);
    }

    public String getCacheMaxObjectsT() {
        return cacheMaxObjectsT;
    }

    public void setCacheMaxObjectsT(String s) {
        cacheMaxObjectsT = s;
    }

    public MdValue getCacheListener() {
        MdClassNameValue v = new MdClassNameValue(cacheListenerClass);
        v.setPickList(PickLists.CLUSTER_TRANSPORT);
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setCacheListener(MdValue v) throws Exception {
        cacheListenerClass = v.getText();
        if (cacheListenerClass != null && cacheListenerClass.trim().length() == 0) {
            cacheListenerClass = null;
        }
        createCacheListener();
    }

    private void createCacheListener() throws Exception {

        Object cl = null;
        try {
            if (cacheListenerClass != null) {
//todo                String cname = null; //JDOServerImp.resolveClusterTransportClassName(
//todo                        cacheListenerClass);
//todo                cl = (VersantClusterTransport)BeanUtils.newInstance(cname, getProjectClassLoader(),
//todo                        VersantClusterTransport.class);
            }
        } finally {
            cacheListenerProps.setBean(cl);
        }

    }

    public List getPerfPropsList() {
        return perfProps.getPropertyList();
    }

    public List getCachePropsList() {
        return cacheListenerProps.getPropertyList();
    }

    public List getLogDownloaderPropsList() {
        return logDownloaderProps.getPropertyList();
    }

    public String getRemoteAccess() {
        StringBuffer s = new StringBuffer();
        int n = remoteList.size();
        boolean first = true;
        for (int i = 0; i < n; i++) {
            MdRemoteProtocol mdp = (MdRemoteProtocol)remoteList.get(i);
            if (mdp.isExportOnStartup()) {
                if (first) {
                    first = false;
                } else {
                    s.append(',');
                }
                s.append(mdp.getProtocolStr());
            }
        }
        return s.length() == 0 ? "false" : s.toString();
    }

    public boolean isAllowRemotePMs() {
        return allowRemotePMs;
    }

    public void setAllowRemotePMs(boolean allowRemotePMs) {
        this.allowRemotePMs = allowRemotePMs;
    }

    public String getRemoteUsername() {
        return remoteUsername;
    }

    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }

    public String getRemotePassword() {
        return remotePassword;
    }

    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }

    public MdValue getRmiRegistryPort() {
        MdValue v = new MdValueInt(rmiRegistryPort);
        v.setDefText(Integer.toString(ConfigParser.DEFAULT_RMI_REGISTRY_PORT));
        return v;
    }

    public void setRmiRegistryPort(MdValue v) {
        rmiRegistryPort = v.getText();
    }

    public MdValue getServerPort() {
        MdValue v = new MdValueInt(serverPort);
        v.setDefText("auto");
        return v;
    }

    public void setServerPort(MdValue v) {
        serverPort = v.getText();
    }

    public MdValue getDefaultFlushThreshold() {
        MdValue v = new MdValueInt(defaultFlushThreshold);
        v.setDefText(Integer.toString(ConfigParser.DEFAULT_FLUSH_THRESHOLD));
        return v;
    }

    public void setDefaultFlushThreshold(MdValue v) {
        defaultFlushThreshold = v.getText();
    }

    public String getRemoteHost() {
        return remoteHost == null ? "" : remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        if (remoteHost != null && remoteHost.trim().length() == 0) {
            this.remoteHost = null;
        } else {
            this.remoteHost = remoteHost;
        }
    }

    public boolean isHyperdrive() {
        return hyperdrive;
    }

    public void setHyperdrive(boolean hyperdrive) {
        this.hyperdrive = hyperdrive;
    }

    /**
     * Have any of the projects classes been sucessfully loaded?
     */
    public boolean hasLoadedClasses() {
        int n = allClasses.size();
        if (n == 0) return true;
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)allClasses.get(i);
            if (c.getCls() != null) return true;
        }
        return false;
    }

    public MetaDataUtils getMdutils() {
        return mdutils;
    }

    /**
     * Resolve an Ant token to a value against our tokenProps.
     */
    public String resolveToken(String token) {
        if (tokenProps == null || token == null) return token;
        int n = token.length();
        if (n < 3) return token;
        if (token.charAt(0) == '@' && token.charAt(n - 1) == '@') {
            String prop = token.substring(1, n - 1);
            String v = tokenProps.getProperty(prop);
            if (v != null) return v;
        }
        return token;
    }

    public boolean isPmpoolEnabled() {
        return pmpoolEnabled;
    }

    public void setPmpoolEnabled(boolean pmpoolEnabled) {
        this.pmpoolEnabled = pmpoolEnabled;
    }

    public String getPmpoolMaxIdle() {
        return resolveToken(pmpoolMaxIdleT);
    }

    public MdValue getPmpoolMaxIdleT() {
        MdValue v = new MdValue(pmpoolMaxIdleT);
        v.setDefText(Integer.toString(ConfigParser.DEFAULT_PMPOOL_MAX_IDLE));
        return v;
    }

    private static String trim(String s) {
        if (s == null) return s;
        s = s.trim();
        if (s.length() == 0) return null;
        return s;
    }

    public void setPmpoolMaxIdleT(MdValue v) {
        this.pmpoolMaxIdleT = trim(v.getText());
    }

    public boolean isRemotePmpoolEnabled() {
        return remotePmpoolEnabled;
    }

    public void setRemotePmpoolEnabled(boolean remotePmpoolEnabled) {
        this.remotePmpoolEnabled = remotePmpoolEnabled;
    }

    public String getRemotePmpoolMaxIdle() {
        return resolveToken(remotePmpoolMaxIdleT);
    }

    public MdValue getRemotePmpoolMaxIdleT() {
        MdValue v = new MdValue(remotePmpoolMaxIdleT);
        v.setDefText(
                Integer.toString(ConfigParser.DEFAULT_REMOTE_PMPOOL_MAX_IDLE));
        return v;
    }

    public void setRemotePmpoolMaxIdleT(MdValue v) {
        this.remotePmpoolMaxIdleT = trim(v.getText());
    }

    public String getRemotePmpoolMaxActive() {
        return resolveToken(remotePmpoolMaxActiveT);
    }

    public MdValue getRemotePmpoolMaxActiveT() {
        MdValue v = new MdValue(remotePmpoolMaxActiveT);
        v.setDefText(
                Integer.toString(ConfigParser.DEFAULT_REMOTE_PMPOOL_MAX_ACTIVE));
        return v;
    }

    public void setRemotePmpoolMaxActiveT(MdValue v) {
        this.remotePmpoolMaxActiveT = trim(v.getText());
    }

    public List getClassDiagramList() {
        return classDiagramList;
    }

    public void setClassDiagramList(List classDiagramList) {
        this.classDiagramList = classDiagramList;
    }

    public void addClassDiagram(ClassDiagram diagram) {
        classDiagramList.add(diagram);
    }

    public void removeClassDiagram(ClassDiagram diagram) {
        classDiagramList.remove(diagram);
    }


    public String getAntArgs() {
        return antRunner.getAntArgs();
    }

    public void setAntArgs(String s) {
        antRunner.setAntArgs(s);
    }


    public MdValue getDatastoreTxLocking() {
        MdValue v = new MdValue(datastoreTxLocking);
        v.setPickList(PickLists.DATASTORE_TX_LOCKING);
        v.setDefText(ConfigParser.DATASTORE_TX_LOCKING_FIRST);
        return v;
    }

    public void setDatastoreTxLocking(MdValue v) {
        this.datastoreTxLocking = v.getText();
    }

    public MdValue getLogDownloader() {
        MdClassNameValue v = new MdClassNameValue(logDownloaderClass);
        v.setDefText(LogDownloader.class.getName());
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setLogDownloader(MdValue v) throws Exception {
        logDownloaderClass = v.getText();
        if (logDownloaderClass != null && logDownloaderClass.trim().length() == 0) {
            logDownloaderClass = null;
        }
        createLogDownloader();
    }

    private void createLogDownloader() throws Exception {
        VersantBackgroundTask dl = null;
        try {
            String c = logDownloaderClass;
            if (c == null) {
                dl = new LogDownloader();
            } else {
                dl = (VersantBackgroundTask)BeanUtils.newInstance(c,
                        getProjectClassLoader(), /*CHFC*/VersantBackgroundTask.class/*RIGHTPAR*/);
            }
        } finally {
            logDownloaderProps.setBean(dl);
        }
    }

    public MdValue getMetricSnapshotIntervalMs() {
        MdValue v = new MdValue(metricSnapshotIntervalMs);
        v.setDefText(Integer.toString(
                ConfigParser.DEFAULT_METRIC_SNAPSHOT_INTERVAL_MS));
        return v;
    }

    public void setMetricSnapshotIntervalMs(MdValue v) {
        this.metricSnapshotIntervalMs = v.getText();
    }

    public MdValue getMetricStoreCapacity() {
        MdValue v = new MdValue(metricStoreCapacity);
        v.setDefText(
                Integer.toString(ConfigParser.DEFAULT_METRIC_STORE_CAPACITY));
        return v;
    }

    public void setMetricStoreCapacity(MdValue v) {
        this.metricStoreCapacity = v.getText();
    }

    public List getUserBaseMetrics() {
        return userBaseMetrics;
    }

    public File getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    /**
     * Get all Collection and Map fields that do not have the element-type (or key-type or value-type
     * for maps) set.
     */
    public List getCollectionAndMapFieldsInError() {
        List list = getAllClasses();
        ArrayList errorList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MdClass mdClass = (MdClass)iter.next();
            List fieldList = mdClass.getFieldList();
            for (Iterator iterator = fieldList.iterator();
                 iterator.hasNext();) {
                MdField mdField = (MdField)iterator.next();
                switch (mdField.getCategory()) {
                    case MDStatics.CATEGORY_COLLECTION:
                        if (mdField.getElementTypeStr() == null) {
                            errorList.add(mdField);
                        }
                        break;
                    case MDStatics.CATEGORY_MAP:
                        if (mdField.getElementTypeStr() == null || mdField.getKeyTypeStr() == null) {
                            errorList.add(mdField);
                        }
                        break;
                }
            }
        }
        return errorList;
    }

    public Exception getDatabaseMetaDataError() {
        return databaseMetaDataError;
    }


    public void reloadJDOFiles() {
        for (int i = jdoFileList.size() - 1; i >= 0; i--) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
            f.reload();
        }
    }

    public List checkJDOFilesUpToDate() {
        ArrayList l = new ArrayList(5);
        boolean changed = false;
        for (int i = jdoFileList.size() - 1; i >= 0; i--) {
            MdJdoFile f = (MdJdoFile)jdoFileList.get(i);
//        for (Iterator it = jdoFileList.iterator(); it.hasNext();) {
//            MdJdoFile f = (MdJdoFile) it.next();
            if(f.hasFileChanged()){
                changed = true;
                if(f.isDirty()){
                    l.add(f);
                }else{
                    f.reload();
                }
            }
        }
        if (changed) {
            return l;
        }else{
            return null;
        }
    }


    public File getCurrentQueryDir() {
        return currentQueryDir;
    }

    public void setCurrentQueryDir(File currentQueryDir) {
        this.currentQueryDir = currentQueryDir;
    }

    public MdValue getPmCacheRefType() {
        MdValue v = new MdValue(pmCacheRefType);
        v.setPickList(PickLists.PM_CACHE_REF_TYPE);
        v.setDefText(ConfigParser.PM_CACHE_REF_TYPE_SOFT);
        return v;
    }

    public void setPmCacheRefType(MdValue v) {
        this.pmCacheRefType = v.getText();
    }

    public void addMdDsTypeChangedListener(MdDsTypeChangedListener listener) {
        listenerList.addListener(listener);
    }

    public void removeMdDsTypeChangedListener(MdDsTypeChangedListener listener) {
        listenerList.removeListener(listener);
    }

    public void dataStoreTypeChanged(MdDsTypeChangedEvent event) {
        Iterator it = listenerList.getListeners(/*CHFC*/MdDsTypeChangedListener.class/*RIGHTPAR*/);
        while (it.hasNext() && !event.isConsumed()) {
            MdDsTypeChangedListener listener = (MdDsTypeChangedListener)it.next();
            listener.dataStoreTypeChanged(event);
        }
    }

    public List getExternalizerList() {
        return externalizerList;
    }

    public void addExternalizer(MdExternalizer e) {
        externalizerList.add(e);
        setDirty(true);
    }

    public void removeExternalizer(MdExternalizer e) {
        externalizerList.remove(e);
        setDirty(true);
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public MdValue getPropFillModeStr() {
        MdValue value = new MdValue();
        ArrayList list = new ArrayList(3);
        list.add("Minimal");
        list.add("Preserve");
        list.add("Verbose");
        value.setPickList(list);
        value.setOnlyFromPickList(true);
        value.setText(getPropFillModeString());
        return value;
    }

    private String getPropFillModeString() {
        switch (propFillMode) {
            case PropertySaver.PROP_FILL_MODE_MIN:
                return "Minimal";
            case PropertySaver.PROP_FILL_MODE_KEEP:
                return "Preserve";
            case PropertySaver.PROP_FILL_MODE_VERBOSE:
                return "Verbose";
        }
        return "Preserve";
    }

    public void setPropFillModeStr(MdValue propFillMode) {
        setDirty(true);
        String text = propFillMode.getText();
        setPropFillModeString(text);
    }

    private void setPropFillModeString(String text) {
        if (text != null) {
            if (text.equals("Minimal")) {
                this.propFillMode = PropertySaver.PROP_FILL_MODE_MIN;
                return;
            } else if (text.equals("Verbose")) {
                this.propFillMode = PropertySaver.PROP_FILL_MODE_VERBOSE;
                return;
            }
        }
        this.propFillMode = PropertySaver.PROP_FILL_MODE_KEEP;
    }

    public boolean isSplitPropFile() {
        return splitPropFile;
    }

    public void setSplitPropFile(boolean splitPropFile) {
        this.splitPropFile = splitPropFile;
        setDirty(true);
    }

    private void setSplitPropFileString(String split) {
        splitPropFile = !(split != null && split.equals("false"));
    }

    public int getPropFillMode() {
        return propFillMode;
    }

    public void setPropFillMode(int propFillMode) {
        this.propFillMode = propFillMode;
    }

    public List getFilePreviewText() throws Exception {
        ByteArrayOutputStream prOut = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream wbOut = new ByteArrayOutputStream(1024);
        saveProps(prOut, wbOut);
        String prText = new String(prOut.toByteArray());
        String wbText = new String(wbOut.toByteArray());
        ArrayList list = new ArrayList(2);
        list.add(prText);
        list.add(wbText);
        return list;
    }

    private void saveProps(OutputStream prOut, OutputStream wbOut)
            throws Exception {
        // save properties
        PropertySaver pr = new PropertySaver();
        pr.setMode(propFillMode);
        if (file != null && file.exists()) {
            pr.load(new FileInputStream(file));
        }
        PropertySaver wb = null;
        if (splitPropFile) {
            wb = new PropertySaver();
//            File wbFile = getWorkbenchFile(file);
//            if (wbFile != null && wbFile.exists()) {
//                wb.load(new FileInputStream(wbFile));
//            }
            wb.setMode(propFillMode);
            pr.removePropertyWild("versant.workbench");
            pr.removePropertyWild("# Workbench ");
            pr.removePropertyWild("# Open Access Class Diagrams");
        } else {
            wb = pr;
        }
        saveProps(pr, wb, false);
        pr.store(prOut);
        if (splitPropFile) {
            wb.store(wbOut);
        }
    }

    /**
     * Get the default externalizer for the type or null if none.
     */
    public MdExternalizer getDefaultExternalizer(String type) {
        int n = externalizerList.size();
        for (int i = 0; i < n; i++) {
            MdExternalizer e = (MdExternalizer)externalizerList.get(i);
            if (e.getTypeName().equals(type)) return e;
        }
        return null;
    }

    public boolean isVds() {
        if (dataStore != null) {
            return dataStore.isVds();
        }
        return false;
    }

    public MdProject getMdProject() {
        return this;
    }

    public List getRemoteList() {
        return remoteList;
    }

}
