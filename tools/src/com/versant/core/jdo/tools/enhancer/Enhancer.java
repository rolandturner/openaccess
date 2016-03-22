
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
package com.versant.core.jdo.tools.enhancer;

import com.versant.core.common.config.ConfigInfo;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.jdo.tools.enhancer.info.ClassInfo;
import com.versant.core.jdo.tools.enhancer.info.FieldInfo;
import com.versant.core.jdo.tools.enhancer.utils.MetaDataToInfoMapper;
import com.versant.core.jdo.tools.enhancer.utils.SwapFieldHelper;
import com.versant.core.metadata.ClassMetaData;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.storagemanager.StorageManagerFactory;
import com.versant.core.storagemanager.StorageManagerFactoryBuilder;
import com.versant.core.util.BeanUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The Enhancer class is used for enhancing .class files,
 * to implement the Persistence Capable interfase.
 * It can also be used from the command line.
 */
public class Enhancer {

    private ClassEnhancer classEnhancer;
    private FieldRefEnhancer fieldRefEnhancer;
    private List classes = new ArrayList();
    private HashMap classResourceMap = new HashMap(); // class name -> res name
    private MetaDataToInfoMapper classInfoUtil;
    private HashMap classMetaDataMap = new HashMap();
    private ModelMetaData metaData = null;
    protected Config cfg = new Config();

    private Set scopeFiles = new HashSet();
    private Set queriesFiles = new HashSet();
    private File outputDirectory;
    private ClassLoader classLoader;
    private String propertiesResourceName;
    private File propertiesFile;
    private boolean genHyper = false;
    private boolean makeFieldsPrivate = false;
    private boolean detached = true;
    private File hyperdriveDir;
    private File srcOutDir;
    private boolean genSrc;

    public Enhancer() {}

    public static class Config {

        public String outputDir;
        public String inputDir;
        public boolean h = false;
        public String hyperDir;
        public boolean genSrc = false;
        public String srcOutputDir;
        public boolean d = true;


        public static final String HELP_outputDir =
                "The output directory where the enhanced classes must be writen to " +
                "(If not set then the classes will be written where they are found).";
        public static final String HELP_hyperDir =
                "The output directory where the hyperdrive classes must be writen to.";
        public static final String HELP_inputDir =
                "The Persistence Aware files. " +
                "These are normal classes (i.e. NOT Persistence Capable classes) that " +
                "reference the persistence capable classes fields directly.";
        public static final String HELP_h =
                "generate hyperdrive classes at compile time.";
        public static final String HELP_srcOutputDir =
                "The place where the hyperdrive src files must be written to.";
        public static final String HELP_d =
                "enhanced for detach (true by default).";
        public static final String HELP_genSrc =
                "The directory where the hyperdrive src files must be written to.";


    }

    public static void main(String[] args) {
        Enhancer server = new Enhancer();
        BeanUtils.CmdLineResult res = null;
        try {
            res = BeanUtils.processCmdLine(args,
                    server.getClass().getClassLoader(), server.cfg, null,
                    "Enhancer", "The JDO class enhancer",
                    true);
        } catch (IllegalArgumentException e) {
            // error message has already been printed
            System.exit(1);
        }
        try {

            ArrayList scopeFiles = new ArrayList();
            if (server.cfg.inputDir != null) {
                StringTokenizer tokenizer = new StringTokenizer(server.cfg.inputDir,",",false);
                while(tokenizer.hasMoreTokens()){
                    scopeFiles.add(new File(tokenizer.nextToken()));
                }
            }

            if (server.cfg.srcOutputDir != null){
                server.cfg.genSrc = true;
            }

            server.rumCommandLine(res.properties, server.cfg.outputDir, scopeFiles,
                    null, server.cfg.h, false, server.cfg.d, server.cfg.hyperDir,
                    server.cfg.genSrc, server.cfg.srcOutputDir);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Gets the classloader
     *
     * @return
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets a classloader for loading all the files.
     *
     * @param classLoader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Is the persistent capable classes enhanced for detached behavior.
     *
     * @return
     */
    public boolean isDetached() {
        return detached;
    }

    /**
     * Must the persistent capable classes be enhanced for detached behavior.
     *
     * @param detached
     */
    public void setDetached(boolean detached) {
        this.detached = detached;
    }

    /**
     * Is Hypedrive classes generated at enhancement time.
     *
     * @return
     */
    public boolean isGenHyper() {
        return genHyper;
    }

    /**
     * Must Hypedrive classes be generated at enhancement time.
     *
     * @param genHyper
     */
    public void setGenHyper(boolean genHyper) {
        this.genHyper = genHyper;
    }
    /**
     * Is Hypedrive src generated at enhancement time.
     *
     * @return
     */
    public boolean isGenSrc() {
        return genSrc;
    }
    /**
     * Must Hypedrive src be generated at enhancement time.
     *
     * @param genSrc
     */
    public void setGenSrc(boolean genSrc) {
        this.genSrc = genSrc;
    }

    public File getSrcOutDir() {
        return srcOutDir;
    }

    public void setSrcOutDir(File srcOutDir) {
        this.srcOutDir = srcOutDir;
    }

    /**
     * Is (public/protected/pakage) fields made private
     * during enhancement.
     *
     * @return
     */
    public boolean isMakeFieldsPrivate() {
        return makeFieldsPrivate;
    }

    /**
     * Must (public/protected/pakage) fields be made private
     * during enhancement.
     *
     * @param makeFieldsPrivate
     */
    public void setMakeFieldsPrivate(boolean makeFieldsPrivate) {
        this.makeFieldsPrivate = makeFieldsPrivate;
    }

    /**
     * Get the output directory where the enhanced classes are
     * written to.
     *
     * @return
     */
    public File getOutputDir() {
        return outputDirectory;
    }

    /**
     * Set the output directory where the enhanced classes are
     * written to.
     *
     * @param outputDir
     */
    public void setOutputDir(File outputDir) {
        this.outputDirectory = outputDir;
    }

    /**
     * Get the .jdogenie property file
     *
     * @return
     */
    public File getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * Sets the *.jdogenie property as a file.
     *
     * @param propertiesFile
     */
    public void setPropertiesFile(File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    /**
     * Gets the *.jdogenie property file resource name
     *
     * @return
     */
    public String getPropertiesResourceName() {
        return propertiesResourceName;
    }

    /**
     * Sets the *.jdogenie property filename as a resource.
     *
     * @param propertiesResourceName
     */
    public void setPropertiesResourceName(String propertiesResourceName) {
        this.propertiesResourceName = propertiesResourceName;
    }

//    public Set getQueriesFiles() {
//        return queriesFiles;
//    }
//
//    public void setQueriesFiles(Set queriesFiles) {
//        this.queriesFiles = queriesFiles;
//    }

    /**
     * Persistant Aware file set as Strings
     *
     * @return
     */
    public Set getPCAwareFiles() {
        return scopeFiles;
    }

    /**
     * Set of resource class file names as Strings i.e.
     * "com/bla/model/Person.class" these classes are
     * persistent aware file, i.e. these files are NOT
     * Persistant Capable files, they are classes that
     * use PC classes that have public fields.
     *
     * @param scopeFiles
     */
    public void setPCAwareFiles(Set scopeFiles) {
        this.scopeFiles = scopeFiles;
    }

    public void setHyperdriveDir(File hyperdriveDir){
        this.hyperdriveDir = hyperdriveDir;
    }

    /**
     * Enhance the files and write them to the output
     * directory
     *
     * @throws Exception
     */
    public int enhance() throws Exception {
        if (propertiesFile == null && propertiesResourceName == null) {
            throw new Exception(
                    "Either the propertiesFile or propertiesResourceName is required.");
        }

        setClassLoader(getPrivateClassLoader());
//        if (outputDirectory == null) {
//            throw new Exception("Output directory is required.");
//        }

        if (propertiesFile != null) {
            return start(scopeFiles, queriesFiles, outputDirectory, classLoader,
                    propertiesFile, genHyper, makeFieldsPrivate, detached,
                    genSrc, srcOutDir);
        } else {
            return start(scopeFiles, queriesFiles, outputDirectory, classLoader,
                    propertiesResourceName, genHyper, makeFieldsPrivate,
                    detached, genSrc, srcOutDir);
        }

    }

    private void rumCommandLine(Properties props, String outputDir,
            ArrayList scopeFiles, ArrayList queries, boolean genHyper,
            boolean makeFieldsPrivate, boolean detached, String hyperdriveDir,
            boolean genSrc, String hyperSrcDir) throws Exception {
        ClassLoader callingClassLoader = Enhancer.class.getClassLoader();
        GrepFile grepFile = null;

        try {
            grepFile = new GrepFile(scopeFiles);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        if (outputDir != null){
            this.outputDirectory = new File(outputDir);
        }

        if (hyperdriveDir != null) {
            this.hyperdriveDir = new File(hyperdriveDir);
        }

        if (hyperSrcDir != null) {
            this.srcOutDir = new File(hyperSrcDir);
        }
        this.genSrc = genSrc;

        start(grepFile.getJdoFiles(), null, outputDirectory,
                callingClassLoader, props, genHyper,
                makeFieldsPrivate, detached, genSrc, srcOutDir);

    }

    private int start(Set scopeFiles, Set queriesFiles, File outputDir,
            ClassLoader callingClassLoader,
            File configFile, boolean genHyper, boolean makeFieldsPrivate,
            boolean detached, boolean genSrc, File srcOutDir) throws Exception {
        this.genHyper = genHyper;
        metaData = getMetaData(configFile, callingClassLoader);
        return startImp(scopeFiles, queriesFiles, outputDir, callingClassLoader,
                genHyper, makeFieldsPrivate, detached, genSrc, srcOutDir);
    }

    private void start(Set scopeFiles, Set queriesFiles, File outputDir,
                       ClassLoader callingClassLoader,
                       Properties props, boolean genHyper, boolean makeFieldsPrivate,
                       boolean detached, boolean genSrc, File srcOutDir) throws Exception {
        this.genHyper = genHyper;
        metaData = getMetaData(props, callingClassLoader);
        startImp(scopeFiles, queriesFiles, outputDir, callingClassLoader,
                genHyper, makeFieldsPrivate, detached, genSrc, srcOutDir);
    }

    private int start(Set scopeFiles, Set queriesFiles, File outputDir,
            ClassLoader callingClassLoader,
            String configFilename, boolean genHyper, boolean makeFieldsPrivate,
            boolean detached, boolean genSrc, File srcOutDir) throws Exception {
        this.genHyper = genHyper;
        metaData = getMetaData(configFilename, callingClassLoader);
        return startImp(scopeFiles, queriesFiles, outputDir, callingClassLoader,
                genHyper, makeFieldsPrivate, detached, genSrc, srcOutDir);

    }

    private int startImp(Set scopeFiles, Set queriesFiles, File outputDir,
            ClassLoader callingClassLoader, boolean genHyper,
            boolean makeFieldsPrivate, boolean detached, boolean genSrc, File srcOutDir)
            throws Exception {
        classEnhancer = new ClassEnhancer(outputDir, callingClassLoader);
        fieldRefEnhancer = new FieldRefEnhancer(outputDir, callingClassLoader);
        classInfoUtil = new MetaDataToInfoMapper(classResourceMap,
                callingClassLoader);
        for (int k = 0; k < metaData.classes.length; k++) {
            ClassMetaData classMetaData = metaData.classes[k];
            classMetaDataMap.put(classMetaData.qname, classMetaData);
            classInfoUtil.setClassInfo(classMetaData.jdoClass, classMetaData);
        }

        classes = classInfoUtil.getClassInfoList();
        long start = System.currentTimeMillis();
        classEnhancer.setGetAndSettersMap(getMapOfGetAndSetters(classes));
        ListIterator classIter = classes.listIterator();
        int classCount = 0;
        while (classIter.hasNext()) {
            ClassInfo classInfo = (ClassInfo)classIter.next();
            fieldRefEnhancer.setPersistentCapable(classInfo.getClassName());
//            javaToJdoqlParser.setPersistentCapable(classInfo.getClassName());
            ClassMetaData classMeta = (ClassMetaData)classMetaDataMap.get(
                    classInfo.getClassName());
            if (classEnhancer.enhance(classInfo, classMeta, makeFieldsPrivate,
                    detached)) {
                classCount++;
            }
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println(
                "[Enhanced (" + classCount + ")] time = " + time + " ms");

        start = System.currentTimeMillis();

        classCount = classes.size();
        fieldRefEnhancer.setFieldsToEnhanceMap(
                getNonPrivateMapOfGetAndSetters(classes));
        fieldRefEnhancer.enhance(scopeFiles);

        end = System.currentTimeMillis();
        time = end - start;
        System.out.println(
                "[Looked through " + scopeFiles.size() + " class files and found " + fieldRefEnhancer.getAwareNum() + " Persistence Aware Classes] time = " + time + " ms");

//        start = System.currentTimeMillis();
//        classCount = classes.size();
//        JavaToJdoqlParser javaToJdoqlParser = new JavaToJdoqlParser(outputDir, callingClassLoader);
//        javaToJdoqlParser.setMetaData(metaData);
//        javaToJdoqlParser.enhance(queriesFiles);
//        end = System.currentTimeMillis();
//        time = end - start;
//        System.out.println("[Looked through " + queriesFiles.size() + " class files and found " + javaToJdoqlParser.getQueryNum() + " JavaQuery Classes] time = " + time + " ms");
        return classCount;
    }

    private ClassLoader getPrivateClassLoader() {
        ClassLoader classLoader = getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        ClassLoader taskClassLoader = getClass().getClassLoader();
        if (taskClassLoader == null) {
            taskClassLoader = ClassLoader.getSystemClassLoader();
        }
        return taskClassLoader;
    }

    ArrayList orderList = new ArrayList();

    private void getOrder(ClassMetaData meta) {
        orderList.add(meta.qname);
        ClassMetaData[] mySub = meta.pcSubclasses;
        if (mySub == null) {
            return;
        } else {
            for (int j = 0; j < mySub.length; j++) {
                ClassMetaData data = mySub[j];
                getOrder(data);
            }
        }
    }

    ArrayList hierList = new ArrayList();

    private void getHier(ClassMetaData meta) {
        hierList.add(meta.qname);
        ClassMetaData[] mySub = meta.pcSubclasses;
        if (mySub == null) {
            return;
        } else {
            for (int j = 0; j < mySub.length; j++) {
                ClassMetaData data = mySub[j];
                getHier(data);
            }
        }
    }

    private HashMap getMapOfGetAndSetters(List classes) {
        HashMap map = new HashMap();

        ClassMetaData[] allMetas = metaData.classes;
        for (int i = 0; i < allMetas.length; i++) {
            ClassMetaData meta = allMetas[i];
            if (!meta.isInHierarchy()) {
                orderList.add(meta.qname);
            } else if (meta.top.equals(meta)) {
                getOrder(meta);
            }
        }

        for (Iterator iterator = orderList.iterator(); iterator.hasNext();) {
            String s = (String)iterator.next();
            Iterator classIter = classes.iterator();
            while (classIter.hasNext()) {
                ClassInfo classInfo = (ClassInfo)classIter.next();
                String className = classInfo.getClassName();
                if (className.equals(s)) {
                    Iterator iter = classInfo.getFieldList().iterator();
                    while (iter.hasNext()) {
                        FieldInfo fieldInfo = (FieldInfo)iter.next();
                        String fieldName = fieldInfo.getFieldName();
                        SwapFieldHelper helper = new SwapFieldHelper();
                        helper.className = className;
                        helper.fieldName = fieldName;
                        helper.jdoGetName = fieldInfo.getJdoGetName();
                        helper.jdoSetName = fieldInfo.getJdoSetName();
                        helper.type = fieldInfo.getType();
                        map.put(className + "|" + fieldName, helper);

                        if (!fieldInfo.isPrivate()) {
                            ClassMetaData clMeta = metaData.getClassMetaData(
                                    className);
                            hierList = new ArrayList();
                            getHier(clMeta);
                            for (Iterator iterator1 = hierList.iterator();
                                 iterator1.hasNext();) {
                                String clName = (String)iterator1.next();
                                map.put(clName + "|" + fieldName, helper);
                            }
                        }
                    }

                }
            }

        }
        return map;
    }

    private HashMap getNonPrivateMapOfGetAndSetters(List classes) {
        HashMap map = new HashMap();

        for (Iterator iterator = orderList.iterator(); iterator.hasNext();) {
            String s = (String)iterator.next();
            Iterator classIter = classes.iterator();
            while (classIter.hasNext()) {
                ClassInfo classInfo = (ClassInfo)classIter.next();
                String className = classInfo.getClassName();
                if (className.equals(s)) {
                    Iterator iter = classInfo.getFieldList().iterator();
                    while (iter.hasNext()) {
                        FieldInfo fieldInfo = (FieldInfo)iter.next();
                        if (fieldInfo.isPrivate()) continue;
                        String fieldName = fieldInfo.getFieldName();
                        SwapFieldHelper helper = new SwapFieldHelper();
                        helper.className = className;
                        helper.fieldName = fieldName;
                        helper.jdoGetName = fieldInfo.getJdoGetName();
                        helper.jdoSetName = fieldInfo.getJdoSetName();
                        helper.type = fieldInfo.getType();
                        map.put(className + "|" + fieldName, helper);

                        ClassMetaData clMeta = metaData.getClassMetaData(
                                className);
                        hierList = new ArrayList();
                        getHier(clMeta);
                        for (Iterator iterator1 = hierList.iterator();
                             iterator1.hasNext();) {
                            String clName = (String)iterator1.next();
                            map.put(clName + "|" + fieldName, helper);
                        }
                    }
                }
            }
        }
        return map;
    }

    private ModelMetaData getMetaData(File configFile, ClassLoader loader)
            throws Exception {
        // parse the config
        ConfigInfo config = getConfigParser().parseResource(configFile);
        config.validate();
        return getMetaDataImp(config, loader);
    }

    private ModelMetaData getMetaData(String configFilename, ClassLoader loader)
            throws Exception {
        // parse the config
    	ConfigParser parser = getConfigParser();
    	ConfigInfo config = null;
    	try {
        	config = parser.parseResource(configFilename, loader);
    	}
    	catch (Exception e) {
    		Properties props = new Properties();
    		config = parser.parse(props);
    		String entityClassNames = 
    					props.getProperty("entity.class.names");
    		System.out.println("Entity classes: " +
    										entityClassNames);
    	}
        return getMetaDataImp(config, loader);
    }

    private ModelMetaData getMetaData(Properties props, ClassLoader loader)
            throws Exception {
        // parse the properties
        ConfigInfo config = getConfigParser().parse(props);
        config.validate();
        return getMetaDataImp(config, loader);
    }

    private ConfigParser getConfigParser() {
        ConfigParser p = null;
        try {
        	Class c = Class.forName("com.versant.core.ejb.ConfigParser");
        	p = (ConfigParser)c.newInstance();
        } catch (Throwable e) {
        	p = new ConfigParser();
        }
        return p;
    }

    private ModelMetaData getMetaDataImp(ConfigInfo config, ClassLoader loader)
            throws Exception {

        if (srcOutDir != null) {
            config.hyperdriveSrcDir = srcOutDir.toString();
        }
        if (hyperdriveDir != null) {
            config.hyperdriveClassDir = hyperdriveDir.toString();
        } else if (genHyper) {
            config.hyperdriveClassDir = outputDirectory.toString();
        }
        config.hyperdrive = srcOutDir != null || hyperdriveDir != null;
        if (this.genHyper && !config.hyperdrive){
            config.hyperdrive = true;
        }

        StorageManagerFactoryBuilder b = null;
        try {
        	Class c = Class.forName("com.versant.core.ejb.StorageManagerFactoryBuilder");
        	b = (StorageManagerFactoryBuilder)c.newInstance();
        } catch (Throwable e) {
         	b = new StorageManagerFactoryBuilder();
        }
        
        b.setConfig(config);
        b.setLoader(loader);
        b.setOnlyMetaData(true);
        b.setIgnoreConFactoryProperties(true);
        
    	StorageManagerFactory smf = b.createStorageManagerFactory();
    	metaData = smf.getModelMetaData();

        Thread.currentThread().setContextClassLoader(loader);
		return metaData;
	}

}
