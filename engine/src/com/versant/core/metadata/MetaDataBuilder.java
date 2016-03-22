
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
package com.versant.core.metadata;

import com.versant.core.common.Debug;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.jdo.EntityLifecycleEvent;
import com.versant.core.jdo.QueryDetails;
import com.versant.core.jdo.sco.VersantSCOFactoryRegistry;
import com.versant.core.metadata.parser.*;
import com.versant.core.jdo.query.ParseException;
import com.versant.core.jdo.query.QueryParser;
import com.versant.core.jdo.externalizer.Externalizer;
import com.versant.core.jdo.externalizer.SerializedExternalizer;
import com.versant.core.jdo.externalizer.TypeAsBytesExternalizer;
import com.versant.core.jdo.externalizer.TypeAsStringExternalizer;
import com.versant.core.util.classhelper.ClassHelper;
import com.versant.core.util.BeanUtils;
import com.versant.core.util.IntArray;

import java.io.Serializable;
import java.lang.reflect.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;

import javax.jdo.identity.*;

/**
 * This will parse one or more .jdo files and load and analyze the persistent
 * classes to produce basic meta data. There are different subclasses for
 * different StorageManager's.
 */
public class MetaDataBuilder implements MDStatics {

    protected final ConfigInfo config;
    protected final ClassLoader loader;
    protected final boolean quiet;
    protected final ModelMetaData jmd;

    private MetaDataPreProcessor metaDataPreProcessor;

    private final MetaDataEnums MDE = new MetaDataEnums();

    protected final MetaDataUtils mdutils = new MetaDataUtils();
    protected final VersantSCOFactoryRegistry scoFactoryRegistry;

    private Map appIdUniqueMap = new HashMap();
    private Map externalizerMap = new HashMap();

    private PackageMetaData[] packageMetaData;
    private HashMap classInfoMap = new HashMap();   //  Class -> ClassInfo
    private HashMap interfaceMap = new HashMap();   // Class -> InterfaceMetaData
    private HashMap classAndInterfaceMap = new HashMap();
    // Class -> ClassMetaData/InterfaceMetaData

    private final FetchGroupBuilder fetchGroupBuilder;
    private final QueryParser queryParser;
    private static final String ARRAY_SUFFIX = "[]";

    public HashMap getClassMap() {
        return jmd.classMap;
    }

    /**
     * Info extracted from package extensions. These form defaults that
     * apply to all classes in the package unless overridden at class level.
     */
    private static class PackageMetaData {

        public String nameWithDot;
        public JdoPackage jdoPackage;
    }

    /**
     * Info extracted from class extensions that is only needed during
     * meta data generation.
     */
    private static class ClassInfo {

        public boolean refsInDefaultFetchGroup;
        public JdoClass jdoClass;
        public ArrayList horizontalFields;
    }

    public MetaDataBuilder(ConfigInfo config, ClassLoader loader,
            boolean quiet) {
        this.config = config;
        this.loader = loader;
        this.quiet = quiet;
        jmd = new ModelMetaData();
        jmd.testing = config.testing;
        fetchGroupBuilder = createFetchGroupBuilder();
        queryParser = new QueryParser(jmd);
        scoFactoryRegistry = new VersantSCOFactoryRegistry(
                config.scoFactoryRegistryMappings, loader);


        if (config.metaDataPreProcessor == null)
            config.metaDataPreProcessor = EJB_JDBC_PRE_PROCESSOR;


        if (config.metaDataPreProcessor != null) {
            try {
                Class cls = loadClass(config.metaDataPreProcessor);
                metaDataPreProcessor =  (MetaDataPreProcessor) cls.getConstructor(
                        new Class[] {/*CHFC*/ClassLoader.class/*RIGHTPAR*/, /*CHFC*/MetaDataUtils.class/*RIGHTPAR*/}).
                        newInstance(new Object[] {loader, mdutils});
            } catch (Throwable e) {
                //ignore for now: The metadataPreProcessor is going to be removed soon

            }
        }
    }

    protected FetchGroupBuilder createFetchGroupBuilder() {
        return new FetchGroupBuilder(jmd,
                isSendCurrentForFGWithSecFields(), isReadObjectBeforeWrite());
    }

    public FetchGroupBuilder getFetchGroupBuilder() {
        return fetchGroupBuilder;
    }



    private void addCallbacks(JdoClass jdoClass, ModelMetaData jmd) {
    	if (jdoClass.entityListener == null && jdoClass.callbackMap == null)
    		return;
        Object listener = null;
        try {
        	if (jdoClass.entityListener != null)
        		listener = jdoClass.entityListener.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("EntityListener class '" +
            		jdoClass.entityListener.getName() +
                    "' must have a public no args constructor.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("EntityListener class '" +
            		jdoClass.entityListener.getName() +
                    "' must have a public no args constructor.");
        }
    	List lifecycleEvents = new ArrayList();
		addEvents(jdoClass.callbackMap, jdoClass, listener, lifecycleEvents);
		addEvents(jdoClass.listenerCallbackMap, jdoClass, listener, lifecycleEvents);
        jmd.addEntityLifecycleEvent(lifecycleEvents);
    }
    
    private void addEvents(Method[] callbackMap, JdoClass jdoClass, Object listener, 
    		List lifecycleEvents) {
    	if (callbackMap != null) {
    		for (int i = 0; i < callbackMap.length; i++) {
    			if (callbackMap[i] != null) {
    	        	Object o = new EntityLifecycleEvent(i + 1, jdoClass.javaClass,
    	                    listener, callbackMap[i]);
    	        	lifecycleEvents.add(o);
    			}
    		}
    	}    	
    }
    
    /**
     * Create meta data for already parsed .jdo meta data.
     */
    public ModelMetaData buildMetaData(JdoRoot[] roots) {

        // populate the externalizer map
        fillExternalizerMap();

        // combine all the meta data into a single structure
        JdoRoot jdoRoot = buildSingleJdoRoot(roots);

        // pull out some package level defaults
        int n = jdoRoot.packages.length;
        packageMetaData = new PackageMetaData[n];
        for (int i = 0; i < n; i++) {
            packageMetaData[i] = createPackageMetaData(jdoRoot.packages[i]);
        }

        // create initial meta data for all classes
        if (Debug.DEBUG) {
            System.out.println(
                    "MDB: Creating initial meta data for all classes and interfaces");
        }

        ClassMetaData[] classes = jmd.classes = new ClassMetaData[jmd.classResourceMap.size()];
        for (int i = 0, c = 0; i < packageMetaData.length; i++) {
            PackageMetaData pmd = packageMetaData[i];
            JdoClass[] ca = pmd.jdoPackage.classes;
            for (int j = 0; j < ca.length; j++) {
                if (metaDataPreProcessor != null) {
                	JdoClass jdoClass = ca[j];
                	if (!jdoClass.isXmlGenerated()) {
//                		metaDataPreProcessor.process(ca[j], jmd);
                	}
                	addCallbacks(jdoClass, jmd);
                }
                ClassMetaData cmd = createMetaData(pmd, ca[j], quiet);
                classes[c++] = cmd;
                getClassMap().put(cmd.cls, cmd);
            }
            JdoExtension[] extensions = pmd.jdoPackage.extensions;
            if (extensions != null) {
                for (int j = 0; j < extensions.length; j++) {
                    JdoExtension e = extensions[j];
                    if (e.key == JdoExtensionKeys.INTERFACE) {
                        InterfaceMetaData imd = createInterfaceMetaData(pmd, e);
                        interfaceMap.put(imd.cls, imd);
                    }
                }
            }
        }
        jmd.buildObjectIdClassMap();
        jmd.buildAbstractSchemaNameMap();
        classAndInterfaceMap.putAll(getClassMap());
        classAndInterfaceMap.putAll(interfaceMap);

        // sort the classes by name and generate classId's - the name sort
        // ensures that the classIds are allocated deterministicly
        if (Debug.DEBUG) {
            System.out.println(
                    "MDB: Sorting classes by name and generating classIds");
        }
        Arrays.sort(classes, new Comparator() {
            public int compare(Object aa, Object bb) {
                ClassMetaData a = (ClassMetaData)aa;
                ClassMetaData b = (ClassMetaData)bb;
                return a.qname.compareTo(b.qname);
            }
        });
        int clen = classes.length;
        for (int i = 0; i < clen; i++) {
            ClassMetaData c = classes[i];
            c.setClassId(mdutils.generateClassId(c.qname));
        }

        // fill in the pcClassMetaData field for all classes
        if (Debug.DEBUG) System.out.println("MDB: Filling pcClassMetaData");
        for (int i = 0; i < clen; i++) {
            ClassMetaData c = classes[i];
            Class pcs = c.pcSuperClass;
            if (pcs == null) continue;
            c.pcSuperMetaData = jmd.getClassMetaData(pcs);
            if (c.pcSuperMetaData == null) {
                RuntimeException x = BindingSupportImpl.getInstance().runtime("persistence-capable-superclass not declared in meta data: " +
                        pcs.getName() + "\n" +
                        c.jdoClass.getContext());
                c.addError(x, quiet);
            } else if (c.pcSuperMetaData.horizontal) {
                c.horizontalCMD = c.pcSuperMetaData;
                c.pcSuperMetaData = null;
            }
        }

        // build the pcSubclasses arrays
        if (Debug.DEBUG) {
            System.out.println("MDB: Building pcSubclasses arrays");
        }
        ArrayList a = new ArrayList();
        for (int i = 0; i < clen; i++) {
            ClassMetaData c = classes[i];
            a.clear();
            for (int j = 0; j < clen; j++) {
                ClassMetaData sc = classes[j];
                if (sc.pcSuperMetaData == c) a.add(sc);
            }
            int len = a.size();
            if (len > 0) {
                c.pcSubclasses = new ClassMetaData[len];
                a.toArray(c.pcSubclasses);
            }
        }

        // fill the pcHierarchy field for all classes and copy the identityType
        // down the hierrachy
        if (Debug.DEBUG) {
            System.out.println(
                    "MDB: Filling pcHierarchy field + copying identity type down");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null) {
                cmd.calcPcHierarchy();
                if (config.hyperdrive) {
                    cmd.oidClassName = MDStatics.GEN_OID_START +
                            cmd.qname.replace('.', '_').replace('$', '_');
                }
            }
        }

        // name the hyperdrive OID and State classes
        if (config.hyperdrive) {
            for (int i = 0; i < clen; i++) {
                ClassMetaData cmd = classes[i];
                if (cmd.pcSuperMetaData != null) {
                    cmd.oidClassName = cmd.top.oidClassName;
                }
                cmd.stateClassName = MDStatics.GEN_STATE_START +
                        cmd.qname.replace('.', '_').replace('$', '_');
            }
        }

        // create meta data for all fields of all classes
        if (Debug.DEBUG) System.out.println("MDB: Creating field meta data");
        ClassMetaData[] ca = jmd.classes;
        for (int j = 0; j < ca.length; j++) {
            ClassMetaData cmd = ca[j];
            try {
                createFieldMetaData(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        doHorizontal(ca);
        doEmbedded(ca);

        // extract all the primary key fields for all classes
        if (Debug.DEBUG) {
            System.out.println("MDB: Extracting primary key fields");
        }
        for (int i = 0; i < clen; i++) {
            extractPrimaryKeyFields(classes[i], quiet);
        }

        // sort the classes array by classId so we can do a binary search
        // and fill the index fields on all classes
        if (Debug.DEBUG) System.out.println("MDB: Sorting classes by classId");
        Arrays.sort(jmd.classes);
        for (int i = 0; i < clen; i++) classes[i].index = i;

        // call init1 on all our DataStore's - they may add additional store
        // specific meta data at this time
        if (Debug.DEBUG) {
            System.out.println("MDB: Calling preBuildFetchGroupsHook");
        }
        preBuildFetchGroupsHook();

        // create all the fetch groups
        if (Debug.DEBUG) System.out.println("MDB: Creating fetch groups");
        fetchGroupBuilder.buildFetchGroups(quiet);

        calcSuperCounts(classes);

        // resolve all ordering fields
        if (Debug.DEBUG) {
            System.out.println("MDB: Resolving collection field ordering");
        }
        for (int i = 0; i < clen; i++) resolveOrdering(classes[i], quiet);

        // give all DataStore's a chance to do extra stuff now that
        // superFieldCount and superFetchGroupCount are filled in
        if (Debug.DEBUG) {
            System.out.println("MDB: Calling postAllFieldsCreatedHook");
        }
        postAllFieldsCreatedHook();

        // fill in the maxFieldsLength value
        if (Debug.DEBUG) {
            System.out.println("MDB: Calculating maxFieldsLength");
        }
        int max = 0;
        for (int i = 0; i < clen; i++) {
            ClassMetaData c = classes[i];
            int len = c.stateFields == null ? 0 : c.stateFields.length;
            if (len > max) max = len;
        }
        jmd.maxFieldsLength = max;

        // collect all the pass 2 fields for each class
        if (Debug.DEBUG) {
            System.out.println("MDB: Creating pass2Fields[] for each class");
        }
        IntArray ia = new IntArray();
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            ia.clear();
            FieldMetaData[] fields = cmd.fields;
            if (fields == null) continue; // possible if previous errors
            int fc = fields.length;
            for (int j = 0; j < fc; j++) {
                FieldMetaData fmd = fields[j];
                if (fmd.secondaryField) ia.add(fmd.fieldNo);
            }
            cmd.pass2Fields = ia.toArray();
        }

        // Fill in the fieldNo arrays now that everything is cool.
        if (Debug.DEBUG) {
            System.out.println("MDB: Calling initMDFields() for each class");
        }
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null) cmd.initMDFields();
        }

        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null) cmd.initMDFields2();
        }

        /**
         * Set flags in the meta data for the client to correctly handle
         * bidirectional relationships.
         */
        setMasterDetailFlags(classes);

        /**
         * Update the totalSubClassCount
         */
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            cmd.totalNoOfSubClasses = getSubClassCount(cmd);
        }

        for (int i = 0; i < classes.length; i++) {
            ClassMetaData aClass = classes[i];
            if (aClass.objectIdClass != null) {
                try {
                    validateIDClass(aClass);
                } catch (RuntimeException e) {
                    aClass.addError(e, quiet);
                }
            }
        }

        // finish initialization of fetch groups
        if (Debug.DEBUG) {
            System.out.println("MDB: Finishing fetch group initialization");
        }
        for (int i = 0; i < clen; i++) classes[i].finishFetchGroups();
        for (int i = 0; i < clen; i++) classes[i].finishFetchGroups2();
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null) checkHierarchy(cmd);
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            FieldMetaData[] fmds = cmd.fields;
            if (fmds == null) continue; // possible if previous error
            for (int j = 0; j < fmds.length; j++) {
                FieldMetaData.setStateMethodName(fmds[j]);
            }
        }

        // This must be done after all fmd's have been finished.
        // This must set the inverseFieldNos
        // and null the inverseFieldMetaData
        for (int i = 0; i < classes.length; i++) {
            ClassMetaData cmd = classes[i];
            FieldMetaData[] fmds = cmd.stateFields;
            if (fmds == null) continue; // possible if previous error
            for (int j = 0; j < fmds.length; j++) {
                FieldMetaData fmd = fmds[j];
                if (fmd.inverseFieldMetaData != null) {
                    fmd.inverseFieldNo = fmd.inverseFieldMetaData.managedFieldNo;
                }
            }
        }

        // Update the cacheStrat of the subclasses to that of the base class.
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null) cmd.overRideCacheStrategy();
        }

        // create QueryParam's for all the named queries
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                createQueryParamsForNamedQueries(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        appIdUniqueMap = null;

        // cleanup data structures not required after meta data generation
        jmd.cleanupAfterMetaDataGeneration();

        // check the meta data for consistency
        jmd.validate();

        return jmd;
    }

    protected void doHorizontal(ClassMetaData[] ca) {
    }

    protected void doEmbedded(ClassMetaData[] ca) {
    }

    protected void createHorizontalFieldMetaData(ClassMetaData cmd, boolean quiet) {
        ClassMetaData superCmd = cmd.horizontalCMD;
        if (superCmd == null) return;
        if (superCmd.fields == null) return;

        Map fieldMap = new HashMap();
        createFmdWithReflection(superCmd, fieldMap, superCmd.getShortName() + ".");

        //take the fields from the original instance mapping and copy metadata,
        //if there is metadata in the subclass then merge it in
        updateMDFromHorizontalSuper(cmd);
        updateFmdFromMetadata(cmd.horizontalCMD, fieldMap, quiet, cmd.jdoClass.elements);
        List fields = updateFmd(fieldMap, cmd, quiet);

        for (int i = 0; i < fields.size(); i++) {
            FieldMetaData fieldMetaData = (FieldMetaData) fields.get(i);
            fieldMetaData.classMetaData = cmd;
            fieldMetaData.fake = true;
            fieldMetaData.origFmd = superCmd.fields[i];
            checkOrigFmd(fieldMetaData);
            fieldMetaData.horizontalFakeField = true;
        }

        cmd.horizontalFields = new FieldMetaData[fields.size()];
        fields.toArray(cmd.horizontalFields);

        fields.addAll(0, Arrays.asList(cmd.fields));
        cmd.fields = new FieldMetaData[fields.size()];
        fields.toArray(cmd.fields);

        for (int i = cmd.fields.length - 1; i >= 0; i--) {
            cmd.fields[i].fieldNo = i;
        }

        updateScoFields(cmd);
    }

    /**
     * Fill the superFieldCount and superFetchGroupCount values and
     * build the stateFields arrays.
     */
    protected void calcSuperCounts(ClassMetaData[] classes) {
        if (Debug.DEBUG) {
            System.out.println("MDB: Calcing superFieldCount and superFetchGroupCount " +
                    "filling stateFields");
        }
        int clen = classes.length;
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData == null) {
                cmd.calcSuperCounts();
            }
        }
    }

    /**
     * Set flags in the meta data for the client to correctly handle
     * bidirectional relationships.
     */
    protected void setMasterDetailFlags(ClassMetaData[] classes) {
        // nothing to do
    }

    /**
     * This is invoked before fetch groups are built. Subclasses can override
     * this to do additional processing.
     */
    protected void preBuildFetchGroupsHook() {
    }

    /**
     * Second hook called late in the build process after all fields have
     * been created and fetch groups have been built.
     */
    protected void postAllFieldsCreatedHook() {
    }

    private void createQueryParamsForNamedQueries(ClassMetaData cmd) {
        JdoClass jdoClass = getClassInfo(cmd).jdoClass;
        ArrayList queries = jdoClass.queries;
        if (queries == null || queries.isEmpty()) return;
        int n = queries.size();
        for (int i = 0; i < n; i++) {
            JdoQuery q = (JdoQuery)queries.get(i);
            if (cmd.getNamedQuery(q.name) != null) {
                throw BindingSupportImpl.getInstance().runtime("There " +
                        "is already a query called '" + q.name + "' on " +
                        cmd.qname + "\n" + q.getContext());
            }
            cmd.addNamedQuery(q.name, new QueryDetails(cmd, q));
        }
    }

    /**
     * What is the default cache strategy?
     */
    public int getCdCacheStrategy() {
        return MDStatics.CACHE_STRATEGY_YES;
    }

    /**
     * Are references placed in the default fetch group?
     */
    public boolean isCdRefsInDefaultFetchGroup() {
        return false;
    }

    /**
     * Recursive method to calculate the total subclass count.
     */
    private int getSubClassCount(ClassMetaData cmd) {
        if (cmd.pcSubclasses == null) return 0;
        int count = cmd.pcSubclasses.length;
        for (int i = 0; i < cmd.pcSubclasses.length; i++) {
            count += getSubClassCount(cmd.pcSubclasses[i]);
        }
        return count;
    }

    /**
     * Resolve the ordering nodes for all collection fields with ordering.
     */
    protected void resolveOrdering(ClassMetaData cmd, boolean quiet) {
        FieldMetaData[] fields = cmd.fields;
        if (fields == null) return; // possible if previous errors
        int flen = fields.length;
        for (int i = 0; i < flen; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.ordering != null) {
                int len = fmd.ordering.length;
                try {
                    for (int j = 0; j < len; j++) {
                        fmd.ordering[j].resolve(queryParser,
                                fmd.elementTypeMetaData, false);
                    }
                } catch (Exception e) {
                    if (BindingSupportImpl.getInstance().isOwnFatalUserException(
                            e)) {
                        fmd.addError((RuntimeException)e, quiet);
                    } else {
                        RuntimeException jfue = BindingSupportImpl.getInstance().runtime(
                                "Invalid ordering extension for field " + fmd.getQName(),
                                e);
                        fmd.addError(jfue, quiet);
                    }
                }
            }
        }
    }

    private void checkHierarchy(ClassMetaData cmd) {
        if (cmd.pcSubclasses == null) return;
        ClassMetaData[] pcSubs = cmd.pcSubclasses;
        FetchGroup[] superFG = cmd.fetchGroups;
        for (int i = 0; i < pcSubs.length; i++) {
            ClassMetaData pcSub = pcSubs[i];
            for (int j = 0; j < superFG.length; j++) {
                FetchGroup fetchGroup = superFG[j];
                FetchGroup subFG = pcSub.getFetchGroup(fetchGroup.name);
                if (subFG == null || subFG.superFetchGroup != fetchGroup) {
                    throw BindingSupportImpl.getInstance().internal(
                            "FG hierachy broken");
                }
            }
            checkHierarchy(pcSub);
        }
    }

    /**
     * Throw a JDOFatalUserException for an unexpected extension.
     */
    public static void throwUnexpectedExtension(JdoExtension e) {
        throw BindingSupportImpl.getInstance().runtime("Extension not allowed here: " +
                e + "\n" + e.getContext());
    }

    /**
     * Find all the primary key fields for cmd.
     */
    private void extractPrimaryKeyFields(ClassMetaData cmd, boolean quiet) {
        // collect pk fields in  meta data order
        ArrayList a = new ArrayList(4);
        JdoElement[] ea = cmd.jdoClass.elements;
        int n = ea.length;
        for (int i = 0; i < n; i++) {
            JdoElement o = ea[i];
            if (!(o instanceof JdoField)) continue;
            FieldMetaData fmd = cmd.getFieldMetaData(((JdoField)o).name);
            if (fmd != null && fmd.primaryKey) a.add(fmd);
        }

        boolean appid = cmd.identityType == IDENTITY_TYPE_APPLICATION;
        if (a.isEmpty()) {
            if (appid && cmd.pcSuperClass == null) {
                RuntimeException e = BindingSupportImpl.getInstance().runtime("No primary key fields found for class with identity-type " +
                        "application and no persistence-capable-superclass\n" +
                        cmd.jdoClass.getContext());
                cmd.addError(e, quiet);
                return;
            }
        } else {
            if (!appid && !cmd.horizontal) {
                RuntimeException e = BindingSupportImpl.getInstance().runtime("Only classes with identity-type application may have " +
                        "primary-key fields\n" +
                        cmd.jdoClass.getContext());
                cmd.addError(e, quiet);
            }
            cmd.pkFields = new FieldMetaData[a.size()];
            a.toArray(cmd.pkFields);
        }
    }



    /**
     * Create a single JdoRoot from all packages.
     */
    private JdoRoot buildSingleJdoRoot(JdoRoot[] roots) {

        // process all the JdoRoot's that are not query only resources
        HashMap classMap = new HashMap();
        ArrayList packageList = new ArrayList();
        int n = roots.length;
        for (int i = 0; i < n; i++) {
            JdoRoot root = roots[i];
            if (root.isQueryMetaData()) continue;
            for (int k = root.packages.length - 1; k >= 0; k--) {
                JdoPackage p = root.packages[k];
				boolean addPackage = true;
                for (int j = p.classes.length - 1; j >= 0; j--) {
                    JdoClass cls = p.classes[j];
                    String qname = cls.getQName();
                    String other = (String)jmd.classResourceMap.get(qname);
                    if (other != null) {


                        if (other.equals(root.name)) {
                            throw BindingSupportImpl.getInstance().runtime(
                                    "Class " + p.classes[j].name +
                                    " is defined more than once in " + root.name);
                        } else {
                            throw BindingSupportImpl.getInstance().runtime(
                                    "Class " + p.classes[j].name +
                                    " is defined in " + other + " and " + root.name);
                        }

                    }
                    jmd.classResourceMap.put(qname, root.name);
                    classMap.put(qname, cls);
                }
				if (addPackage == true)
	                packageList.add(p);
            }
        }

        // move all JdoQuery's to the meta data for the classes they are for
        for (int i = 0; i < n; i++) {
            JdoRoot root = roots[i];
            if (!root.isQueryMetaData()) continue;
            for (int k = root.packages.length - 1; k >= 0; k--) {
                JdoPackage p = root.packages[k];
                for (int j = p.classes.length - 1; j >= 0; j--) {
                    JdoClass cls = p.classes[j];
                    if (cls.queries == null) continue;
                    for (Iterator t = cls.queries.iterator(); t.hasNext();) {
                        JdoQuery q = (JdoQuery)t.next();
                        String qname = q.getCandidateClass();
                        JdoClass target = (JdoClass)classMap.get(qname);
                        if (target == null) {
                            throw BindingSupportImpl.getInstance().runtime("Candidate class for query is not " +
                                    "defined in the meta data: " + qname + "\n" +
                                    q.getContext());
                        }
                        target.addJdoQuery(q);
                    }
                }
            }
        }

        // make a new single JdoRoot
        JdoRoot root = new JdoRoot();
        root.packages = new JdoPackage[packageList.size()];
        root.name = "combined";
        packageList.toArray(root.packages);
        // Note that we have not changed the parent field on each package.
        // This still points at the original JdoRoot so that error messages
        // can display the correct filename.

        return root;
    }

    /**
     * Load class name using our loader.
     */
    private Class loadClass(String name) throws ClassNotFoundException {
        return ClassHelper.get().classForName(name, false, loader);
    }

    /**
     * Create tempory meta data for a package. This is used for defaults
     * for classes etc.
     */
    private PackageMetaData createPackageMetaData(JdoPackage p) {
        PackageMetaData pmd = new PackageMetaData();
        pmd.nameWithDot = p.name + ".";
        pmd.jdoPackage = p;
        return pmd;
    }

    /**
     * Create meta data for an interface in package pmd.
     */
    private InterfaceMetaData createInterfaceMetaData(PackageMetaData pmd,
            JdoExtension ext) {
        String qname = ext.getString();
        if (qname.indexOf('.') < 0) qname = pmd.nameWithDot + qname;
        Class cls;
        try {
            cls = loadClass(qname);
        } catch (ClassNotFoundException e) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Interface not found: " + qname + "\n" + ext.getContext(),
                    e);
        }
        if (!cls.isInterface()) {
            throw BindingSupportImpl.getInstance().runtime("Expected interface, found class: " + qname + "\n" +
                    ext.getContext());
        }
        return new InterfaceMetaData(cls);
    }

    /**
     * Create basic meta data for class cls in package pmd. This does not
     * pickup the fields. It loads all classes etc and finds some class
     * extensions.
     */
    private ClassMetaData createMetaData(PackageMetaData pmd, JdoClass jdoCls,
            boolean quiet) {
        ClassMetaData cmd = new ClassMetaData(jdoCls, jmd);
        ClassInfo classInfo = getClassInfo(cmd);
        classInfo.jdoClass = jdoCls;

        // init defaults before we do anything else
        cmd.packageNameWithDot = pmd.nameWithDot;
        classInfo.refsInDefaultFetchGroup =  isCdRefsInDefaultFetchGroup();
        cmd.cacheStrategy = getCdCacheStrategy();
        cmd.setObjectIdClasssRequired(jdoCls.objectIdClasssRequired);

        // load the PC class
        try {
            cmd.cls = loadClass(cmd.qname);
        } catch (ClassNotFoundException e) {
            RuntimeException x = BindingSupportImpl.getInstance().runtime("Class not found: " + cmd.qname + "\n" +
                    jdoCls.getContext(), e);
            cmd.addError(x, quiet);
            return cmd;
        }
        if (cmd.cls.isInterface()) {
            RuntimeException x = BindingSupportImpl.getInstance().runtime("Expected class, found interface: " + cmd.qname + "\n" +
                    jdoCls.getContext());
            cmd.addError(x, quiet);
            return cmd;
        }

        boolean isStr = false;

        if (isStr) {
            cmd.structType = cmd.embeddedOnly = true;
        }

        // load its persistence-capable-superclass (if any)
        String qname = jdoCls.getPCSuperClassQName();
        if (qname != null) {
            try {
                cmd.pcSuperClass = loadClass(qname);
            } catch (ClassNotFoundException e) {
                RuntimeException x = BindingSupportImpl.getInstance().runtime("persistence-capable-superclass not found: " +
                        qname + "\n" +
                        jdoCls.getContext(), e);
                cmd.addError(x, quiet);
                return cmd;
            }
        }

        checkForHorizontal(jdoCls, cmd);
        updateIdMetaData(cmd, jdoCls);

        // pickup some class extensions
        JdoElement[] elements = jdoCls.elements;
        int n = elements.length;
        for (int i = 0; i < n; i++) {
            JdoElement o = elements[i];
            if (o instanceof JdoExtension) {
                JdoExtension e = (JdoExtension)o;
                switch (e.key) {
                    case JdoExtensionKeys.READ_ONLY:
                        try {
                            cmd.readOnly = e.getBoolean();
                        } catch (RuntimeException x) {
                            cmd.addError(x, quiet);
                        }
                        break;
                    case JdoExtensionKeys.CACHE_STRATEGY:
                        try {
                            cmd.cacheStrategy = e.getEnum(MDE.CACHE_ENUM);
                        } catch (RuntimeException x) {
                            cmd.addError(x, quiet);
                        }
                        break;
                    case JdoExtensionKeys.DELETE_ORPHANS:
                        try {
                            cmd.deleteOrphans = e.getBoolean();
                        } catch (RuntimeException x) {
                            cmd.addError(x, quiet);
                        }
                        break;
                    case JdoExtensionKeys.OIDS_IN_DEFAULT_FETCH_GROUP:
                        try {
                            getClassInfo(cmd).refsInDefaultFetchGroup = e.getBoolean();
                        } catch (RuntimeException x) {
                            cmd.addError(x, quiet);
                        }
                        break;
                    case JdoExtensionKeys.CREATE_OID_AT_MAKE_PERSISTENT:
                        // ignore - no longer required
                        break;
                    case JdoExtensionKeys.EMBEDDED_ONLY:
                        if (e.value != null) {
                            cmd.embeddedOnly = e.getBoolean();
                        } else {
                            cmd.embeddedOnly = true;
                        }
                        break;
                }
            }
        }

        return cmd;
    }

    private void updateIdMetaData(ClassMetaData cmd, JdoClass jdoCls) {
        // load its objectid-class (if any)
        cmd.identityType = jdoCls.identityType;
        if (cmd.identityType == IDENTITY_TYPE_APPLICATION) {
            String qname = jdoCls.getObjectIdClassQName();
            if (qname == null) {
                if (cmd.isObjectIdClasssRequired()) {
                    if (!fillSingleIdentity(jdoCls, cmd)) {
                        RuntimeException x = BindingSupportImpl.getInstance().runtime(
                                "objectid-class is required for application identity\n" +
                                        jdoCls.getContext());
                        cmd.addError(x, quiet);
                    }
                }
                return;
            }

            if (!isSingleId(qname)) {
                String pcRootClsName = (String)appIdUniqueMap.get(qname);
                if (pcRootClsName == null) {
                    appIdUniqueMap.put(qname, jdoCls.getQName());
                } else {
                    RuntimeException x = BindingSupportImpl.getInstance().invalidOperation("The objectid-class for " +
                            jdoCls.getQName() + " has already been used for " +
                            pcRootClsName + ": " + qname + "\n" +
                            jdoCls.getContext());
                    cmd.addError(x, quiet);
                    return;
                }
            } else {
                cmd.isSingleIdentity = true;
            }

            try {
                cmd.objectIdClass = loadClass(qname);
            } catch (ClassNotFoundException e) {
                RuntimeException x = BindingSupportImpl.getInstance().runtime("objectid-class not found: " + qname + "\n" +
                        jdoCls.getContext(), e);
                cmd.addError(x, quiet);
                return;
            }
        } else if (cmd.identityType == IDENTITY_TYPE_NONDURABLE) {
            RuntimeException x = BindingSupportImpl.getInstance().runtime("nondurable identity-type is not supported\n" +
                    jdoCls.getContext());
            cmd.addError(x, quiet);
            return;
        } else {
            cmd.identityType = IDENTITY_TYPE_DATASTORE;
            if (jdoCls.objectIdClass != null) {
                RuntimeException x = BindingSupportImpl.getInstance().runtime("objectid-class is only allowed for application identity\n" +
                        jdoCls.getContext());
                cmd.addError(x, quiet);
                return;
            }
        }
//        } else {
//            if (jdoCls.objectIdClass != null) {
//                RuntimeException x = BindingSupportImpl.getInstance().runtime("objectid-class is not allowed as class " +
//                        "has a persistence-capable-superclass\n" +
//                        jdoCls.getContext());
//                cmd.addError(x, quiet);
//                return cmd;
//            }
//        }
    }

    private boolean isSingleId(String qname) {
        if (qname.equals(IntIdentity.class.getName()) ||
                qname.equals(LongIdentity.class.getName()) ||
                qname.equals(StringIdentity.class.getName()) ||
                qname.equals(ShortIdentity.class.getName()) ||
                qname.equals(CharIdentity.class.getName()) ||
                qname.equals(ByteIdentity.class.getName())) {
            return true;
        }
        return false;
    }

    private boolean fillSingleIdentity(JdoClass jdoCls, ClassMetaData cmd) {
        String name = jdoCls.getSinglePKField();
        Field field = null;
        Class type = null;
        if (name != null) {
            try {
                field = cmd.cls.getDeclaredField(name);
            } catch (NoSuchFieldException e) { /*hide*/}

            if (field != null)
            {
                type = field.getType();
                if (type != null) {
                    if (/*CHFC*/Byte.class/*RIGHTPAR*/.isAssignableFrom(type) ||
                        /*CHFC*/byte.class/*RIGHTPAR*/.isAssignableFrom(type)) {
                        cmd.objectIdClass = /*CHFC*/ByteIdentity.class/*RIGHTPAR*/;
                        cmd.isSingleIdentity = true;
                        return true;
                    } else if (/*CHFC*/Character.class/*RIGHTPAR*/.isAssignableFrom(type) ||
                               /*CHFC*/char.class/*RIGHTPAR*/.isAssignableFrom(type)) {
                        cmd.objectIdClass = /*CHFC*/CharIdentity.class/*RIGHTPAR*/;
                        cmd.isSingleIdentity = true;
                        return true;
                    } else if (/*CHFC*/Integer.class/*RIGHTPAR*/.isAssignableFrom(type) ||
                               /*CHFC*/int.class/*RIGHTPAR*/.isAssignableFrom(type)) {
                        cmd.objectIdClass = /*CHFC*/IntIdentity.class/*RIGHTPAR*/;
                        cmd.isSingleIdentity = true;
                        return true;
                    } else if (/*CHFC*/Long.class/*RIGHTPAR*/.isAssignableFrom(type) ||
                               /*CHFC*/long.class/*RIGHTPAR*/.isAssignableFrom(type)) {
                        cmd.objectIdClass = /*CHFC*/LongIdentity.class/*RIGHTPAR*/;
                        cmd.isSingleIdentity = true;
                        return true;
                    } else if (/*CHFC*/Short.class/*RIGHTPAR*/.isAssignableFrom(type) ||
                               /*CHFC*/short.class/*RIGHTPAR*/.isAssignableFrom(type)) {
                        cmd.objectIdClass = /*CHFC*/ShortIdentity.class/*RIGHTPAR*/;
                        cmd.isSingleIdentity = true;
                        return true;
                    } else if (/*CHFC*/String.class/*RIGHTPAR*/.isAssignableFrom(type)) {
                        cmd.objectIdClass = /*CHFC*/StringIdentity.class/*RIGHTPAR*/;
                        cmd.isSingleIdentity = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Calculate if this class is horizontally mapped.
     * @param jdoCls
     * @param cmd
     */
    protected void checkForHorizontal(JdoClass jdoCls, ClassMetaData cmd) {
    }

    /**
     * Validate the application id class. This also fills
     * {@link FieldMetaData#objectidClassField}.
     */
    private void validateIDClass(ClassMetaData cmd) {
        Class idClass = cmd.objectIdClass;

        if (!Modifier.isPublic(idClass.getModifiers())) {
            throw BindingSupportImpl.getInstance().runtime("Application id class '"
                    + idClass.getName()
                    + "' must be public");
        }

        if (idClass.isInterface()) {
            throw BindingSupportImpl.getInstance().runtime("Application id class '"
                    + idClass.getName()
                    + "' may not be an interface");
        }

        FieldMetaData[] pkFields = cmd.pkFields;
        Class singleFieldCls = /*CHFC*/SingleFieldIdentity.class/*RIGHTPAR*/;
        for (int i = 0; i < pkFields.length; i++) {
            Class theClass = cmd.objectIdClass;
            if (! singleFieldCls.isAssignableFrom(theClass)) {
                pkFields[i].getObjectidClassField();
            }
        }

        // Must implement java.io.Serializable
        
		Class serializableClass = /*CHFC*/Serializable.class/*RIGHTPAR*/;
		if (!serializableClass.isAssignableFrom(idClass))
		{
            throw BindingSupportImpl.getInstance().runtime("Application id class '"
                    + idClass.getName()
                    + "' does not implement java.io.Serializable");
        }
        

        checkForDefaultConstructor(idClass);
        Class theClass = cmd.objectIdClass;
        if (! singleFieldCls.isAssignableFrom(theClass)) {
            checkForPublicStringConstructor(idClass);
        }
        checkForToString(idClass);
    }

    private void checkForToString(Class idClass) {
        try {
            Method tos = null;
            Class objectClass = /*CHFC*/Object.class/*RIGHTPAR*/;
            for (Class c = idClass; c != objectClass; c = c.getSuperclass()) {
                try {
                    tos = c.getDeclaredMethod("toString", null);
                    break;
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
            if (tos == null) {
                throw BindingSupportImpl.getInstance().runtime("Application id class '"
                        + idClass.getName() + "' must override toString");
            }
        } catch (SecurityException e) {
            throw BindingSupportImpl.getInstance().exception(e.getMessage(), e);
        }
    }

    private void checkForDefaultConstructor(final Class idClass) {
        // Must have default constructor
        boolean foundDefaultCon = false;
        /**
         * Search through the heirarchy until we reach Object.class for a default constructor
         */
        Class objectClass = /*CHFC*/Object.class/*RIGHTPAR*/;
        for (Class ch = idClass; ch != objectClass; ch = ch.getSuperclass()) {
            try {
                ch.getConstructor(new Class[] {});
                foundDefaultCon = true;
                break;
            } catch (NoSuchMethodException e) {
                //ignore
            }
        }

        if (!foundDefaultCon) {
            throw BindingSupportImpl.getInstance().runtime("Application id class '"
                    + idClass.getName()
                    + "' does not have a public no-arg constructor");
        }
    }

    private void checkForPublicStringConstructor(final Class idClass) {
        boolean found = false;
        /**
         * Search through the heirarchy until we reach Object.class
         */
        Class objectClass = /*CHFC*/Object.class/*RIGHTPAR*/;
        for (Class ch = idClass; ch != objectClass; ch = ch.getSuperclass()) {
            try {
                ch.getConstructor(new Class[] {/*CHFC*/String.class/*RIGHTPAR*/});
                found = true;
                break;
            } catch (NoSuchMethodException e) {
                //ignore
            }
        }

        if (!found) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Application id class '"
                    + idClass.getName()
                    + "' does not have a public constructor that accepts a String parameter");
        }
    }

    /**
     * Create the FieldMetaData for class cmd. This finds all fields using
     * reflection and merges this with the JdoField information from the
     * .jdo files.
     */
    private void createFieldMetaData(ClassMetaData cmd, boolean quiet) {
        if (cmd.pcSuperMetaData != null) return;
        createFieldMetaDataImp(cmd, quiet);
    }

    protected void createEmbeddeFieldMetaData(ClassMetaData cmd, boolean quiet) {
        if (cmd.pcSuperMetaData != null) return;
        createEmbeddeFieldMetaDataImp(cmd, quiet);
    }

    /**
     * Update all the jdo metadata for horizontal fields from horizontal superclass.
     */
    private void updateMDFromHorizontalSuper(ClassMetaData cmd) {
        if (cmd.horizontalCMD == null) return;
        JdoElement[] ea = cmd.jdoClass.elements;
        Map nameToJdoFieldMap = new HashMap();
        JdoElement[] horElements = cmd.horizontalCMD.jdoClass.elements;
        final String prefix = cmd.horizontalCMD.getShortName() + ".";

        //iterate over the elements defined in the horizontal base class
        for (int i = 0; i < horElements.length; i++) {
            JdoElement o = horElements[i];
            if (!(o instanceof JdoField)) continue;
            JdoField horBaseJdoField = (JdoField)o;

            /**
             * If this field is descibed in subclass then merge it, else just copy
             * it to the subclass
             */
            JdoField extendedField = findJdoFieldIn(ea, prefix + horBaseJdoField.name);
            if (extendedField == null) {
                extendedField = horBaseJdoField.createCopy(cmd.jdoClass);
                extendedField.name = prefix + extendedField.name;
                nameToJdoFieldMap.put(extendedField.name, extendedField);
            } else {
                //if a field is described as persistent-none in the subclass then
                //change it to transactional
                if (extendedField.persistenceModifier == PERSISTENCE_MODIFIER_NONE) {
                    extendedField.persistenceModifier = PERSISTENCE_MODIFIER_TRANSACTIONAL;
                }
                extendedField.synchWith(horBaseJdoField, Collections.EMPTY_SET, false);
                nameToJdoFieldMap.put(extendedField.name, extendedField);
            }
        }

        /**
         * Find all the horizontal field metadata that is only in the subclass and add
         * it.
         */
        List newElements = new ArrayList();
        newElements.addAll(nameToJdoFieldMap.values());

        for (int i = 0; i < ea.length; i++) {
            JdoElement jdoElement = ea[i];
            if (!(jdoElement instanceof JdoField)) {
                newElements.add(jdoElement);
                continue;
            }

            JdoField extJdoField = (JdoField) jdoElement;
            JdoField alreadyMerged = (JdoField) nameToJdoFieldMap.get(extJdoField.name);
            if (alreadyMerged == null) {
                //if horizontal field metadata
                if (extJdoField.name.startsWith(prefix)) {
                    //check for illegal overrides in subclass
                    if (extJdoField.persistenceModifier == PERSISTENCE_MODIFIER_NONE) {
                        extJdoField.persistenceModifier = PERSISTENCE_MODIFIER_TRANSACTIONAL;
                    }
                }
                newElements.add(extJdoField);
            }
        }

        ea = new JdoElement[newElements.size()];
        newElements.toArray(ea);
        cmd.jdoClass.elements = ea;
    }


    private JdoField findJdoFieldIn(JdoElement[] elements, String name) {
        for (int i = 0; i < elements.length; i++) {
            JdoElement element = elements[i];
            if (!(element instanceof JdoField)) continue;
            JdoField jdoField = (JdoField) element;
            if (jdoField.name.equals(name)) {
                return jdoField;
            }
        }
        return null;
    }


    /**
     * Notes:
     *
     * An embedded field can not be polymorphic. This would require us to add all the fields
     * of the embedded type, its superclass and its subclasses to the owning table.
     *
     *
     * How does recursive embedded fields work?
     * Fields are added to the currentFields list. If any of these are again a 'embedded' field
     * then it will be recursively processed.
     *
     * Rules for fieldNo calculation.
     *
     * Follow the fields as sorted accordingly to the jdo spec.
     * if a embedded field is found then add its fields to the end of the fields
     * of the owner.
     *
     * Start with the fields as declared in the class. This fields list is iterated
     * over and for each embedded field found we add the fields of the embedded instance
     * to the bottom of the list. This process is continued until we reach the
     * end of the list.
     *
     * eg.
     * name
     * embeddedAddress1
     * embeddedAddress2
     *
     * ------------------------------
     *
     * name
     *> embeddedAddress1
     * embeddedAddress2
     *
     * {streetName
     *  postallCode
     *  embeddedCountry}
     *
     * ------------------------------
     *
     * name
     * embeddedAddress1
     *> embeddedAddress2
     *
     *  {streetName  4
     *  postallCode  5
     *  embeddedCountry} 6
     *
     *  {streetName 7
     *  postllCode  8
     *  embeddedCountry} 9
     *
     * ------------------------------
     *
     * name
     * emebddedAddress1
     * embeddedAddress2
     *      streetName
     *      postallCode
     * >    embeddedCountry
     *
     *      streetName
     *      postallCode
     *      embeddedCountry
     *
     *          countryName
     *          countryCode
     *
     * ------------------------
     *
     * name
     * emebddedAddress1
     * embeddedAddress2
     *      streetName
     *      postallCode
     *      embeddedCountry
     *
     *      streetName
     *      postallCode
     * >    embeddedCountry
     *
     *          countryName
     *          countryCode
     *
     *          countryName
     *          countryCode
     *
     * -------------------------
     *
     *
     *                                   relative field no.    ranges of relative field no.
     * aName                        0    0
     * emebddedAddressHome          1    1                     3-10
     * embeddedAddressHomeDefault   2    2
     * embeddedAddressOffice        3    3                     11-18 *
     *      aStreetName             1.1  4
     *      bPostallCode            1.2  5
     *      embeddedCountry         1.3  6                     6-10   *
     *
     *          aCountryName        5.1  7
     *          bCountryCurrency    5.2  8                     9-10 *
     *          countryCode         5.2  9
     *
     *              aAurrencyCode   7.1  10
     *              bCurrencyName   7.2  11
     *
     *      aStreetName              3.1  12
     *      bPostallCode             3.2  13
     *      cEmbeddedCountry         3.3  14                    14-18
     *
     *          aCountryName         13.1 15
     *          bCountryCurrency     13.2 16                    17-18
     *          cCountryCode         13.3 17
     *
     *              aCurrencyCode    15.1  18
     *              bCurrencyName    15.2  19
     *
     *
     * @param cmd
     * @param quiet
     */
    private void createEmbeddeFieldMetaDataImp(ClassMetaData cmd, boolean quiet) {
        List currentFields = new ArrayList(Arrays.asList(cmd.fields));

        HashMap parents = new HashMap(cmd.fields.length * 5);
        FieldMetaData fmd = null;
        for (int i = 0; i < currentFields.size(); i++) {
            fmd = (FieldMetaData) currentFields.get(i);

            if (fmd.isEmbeddedRef()) {
                ClassMetaData embeddedCmd = fmd.typeMetaData;

                Map fieldMap = new HashMap();
                createFmdWithReflection(embeddedCmd, fieldMap, fmd.name + "/");

                /**
                 * Make a copy of the jdoFields as found on the original class and
                 * merge info from the owners metadata if available
                 */
                Map copiedJdoFields = new HashMap();
                for (int j = 0; j < embeddedCmd.jdoClass.elements.length; j++) {
                    JdoElement element = embeddedCmd.jdoClass.elements[j];
                    if (!(element instanceof JdoField)) continue;
                    JdoField copy = ((JdoField) element).createCopy(cmd.jdoClass);
                    //map it to old name
                    copiedJdoFields.put(copy.name, copy);
                    copy.origName = copy.name;
                    copy.name = fmd.name + "/" + copy.name;

                    //look in the owners metadata for overriding extensions.
                    if (fmd.jdoField != null && fmd.jdoField.extensions != null) {
                        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.FIELD, copy.origName, fmd.jdoField.extensions);
                        if (ext != null) {
                            copy.applyEmbeddedExtensions(ext.nested);
                        } else {
                            copy.applyEmbeddedExtensions(null);
                        }
                    } else {
                        copy.applyEmbeddedExtensions(null);
                    }
                }

                //go through the owners metadata extensions that is only in the owners metadata
                if (fmd.jdoField != null && fmd.jdoField.extensions != null) {
                    JdoExtension exts[] = fmd.jdoField.extensions;
                    Iterator iter = fieldMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String name = (String) iter.next();
                        name = name.substring((fmd.name + "/").length());   //todo change this to fmd.origName

                        //if the jdofield is already in the map the ignore it as it's already merged
                        if (copiedJdoFields.containsKey(name)) continue;

                        JdoExtension ext = JdoExtension.find(JdoExtensionKeys.FIELD, name, exts);
                        if (ext != null) {
                            JdoField newJdoField = new JdoField(fmd.jdoField);
                            newJdoField.name = fmd.name + "/" + name;
                            newJdoField.applyEmbeddedExtensions(ext.nested);
                            copiedJdoFields.put(name, newJdoField);
                        }
                    }
                }

                JdoElement[] els = new JdoElement[copiedJdoFields.size()];
                copiedJdoFields.values().toArray(els);

                updateFmdFromMetadata(embeddedCmd, fieldMap, quiet, els);

                List fields = updateFmd(fieldMap, cmd, quiet);
                findNullIndicationFmd(fields, fmd);

                for (int j = 0; j < fields.size(); j++) {
                    FieldMetaData fieldMetaData = (FieldMetaData) fields.get(j);
                    fieldMetaData.classMetaData = cmd;
                    fieldMetaData.fake = true;
                    fieldMetaData.origFmd = embeddedCmd.fields[j];
                    checkOrigFmd(fieldMetaData);
                    fieldMetaData.embeddedFakeField = true;

                    //look for recursive embedded field
                    checkRecursiveEmbedded(fieldMetaData, fmd, parents);
                }

                //check for horizontal superclass for embedded class mapping
                if (embeddedCmd.horizontalCMD != null) {
                    List horFmds = doEmbeddedHorizontal(embeddedCmd, cmd, fmd.name + "/");
                    for (int j = 0; j < horFmds.size(); j++) {
                        FieldMetaData fieldMetaData = (FieldMetaData) horFmds.get(j);
                        fieldMetaData.classMetaData = cmd;
                        fieldMetaData.fake = true;
                        checkOrigFmd(fieldMetaData);
                        fieldMetaData.embeddedFakeField = true;
                        //look for recursive embedded field
                        checkRecursiveEmbedded(fieldMetaData, fmd, parents);
                    }

                    fields.addAll(0, horFmds);
                }

                fmd.embeddedFmds = new FieldMetaData[fields.size()];
                fields.toArray(fmd.embeddedFmds);

                /**
                 * Look for nullIndicator
                 * The nullIndicator might be a column or a field of the embedded class.
                 * If it is a column then we must check if there is a field with the same
                 * column name. If there is then it must be used as the null indicator.
                 * Else we must create a fake field with the supplied column name to act as
                 * the nullIndicator.
                 */
                if (fmd.jdoField.extensions != null) {
                    for (int j = 0; j < fmd.jdoField.extensions.length; j++) {
                        JdoExtension ext = fmd.jdoField.extensions[j];
                        if (ext.key == JdoExtensionKeys.NULL_INDICATOR) {
                            //must add fake field
                        }
                    }
                }
                currentFields.addAll(fields);
            }
        }

        currentFields = reOrderEmbeddedFields(cmd, currentFields);

        cmd.fields = new FieldMetaData[currentFields.size()];
        currentFields.toArray(cmd.fields);

        for (int j = cmd.fields.length - 1; j >= 0; j--) {
            cmd.fields[j].fieldNo = j;
        }

        updateScoFields(cmd);

        if (cmd.pcSubclasses != null) {
            for (int i = 0; i < cmd.pcSubclasses.length; i++) {
                createEmbeddeFieldMetaDataImp(cmd.pcSubclasses[i], quiet);
            }
        }
    }

    /**
     * Re order the embedded fields.
     */
    private List reOrderEmbeddedFields(ClassMetaData cmd, List currentFields) {
        List orderedEmbeddedList = walk(cmd);
        if (orderedEmbeddedList.size() != currentFields.size()) {
            throw new RuntimeException("Field list ordering mismatch");
        }
        return orderedEmbeddedList;
    }

    private List walk(ClassMetaData cmd) {
        List l = new ArrayList();
        FieldMetaData[] fmds = cmd.fields;
        l.addAll(Arrays.asList(fmds));

        for (int i = 0; i < fmds.length; i++) {
            FieldMetaData fmd = fmds[i];
            if (fmd.isEmbeddedRef()) {
                walkImp(fmd, l);
            }
        }
        return l;
    }

    private void walkImp(FieldMetaData fmd, List l) {
        FieldMetaData[] fmds = fmd.embeddedFmds;
        for (int i = 0; i < fmds.length; i++) {
            FieldMetaData aFmd = fmds[i];
            l.add(aFmd);
        }
        for (int i = 0; i < fmds.length; i++) {
            FieldMetaData aFmd = fmds[i];
            if (aFmd.isEmbeddedRef()) {
                walkImp(aFmd, l);
            }
        }
    }

    private List doEmbeddedHorizontal(ClassMetaData cmd, ClassMetaData referenceCmd, String prefix) {
        ClassMetaData superCmd = cmd.horizontalCMD;
        if (superCmd == null || superCmd.fields == null)
			return new java.util.ArrayList(0);

        Map fieldMap = new HashMap();
        createFmdWithReflection(superCmd, fieldMap, superCmd.getShortName() + ".");
        updateMDFromHorizontalSuper(cmd);

        updateFmdFromMetadata(cmd.horizontalCMD, fieldMap, quiet, cmd.jdoClass.elements);
        List fields = updateFmd(fieldMap, cmd, quiet);

        for (int i = 0; i < fields.size(); i++) {
            FieldMetaData fieldMetaData = (FieldMetaData) fields.get(i);
            fieldMetaData.classMetaData = referenceCmd;
            fieldMetaData.fake = true;
            fieldMetaData.origFmd = superCmd.fields[i];
            checkOrigFmd(fieldMetaData);
            fieldMetaData.name = prefix + fieldMetaData.name;
            fieldMetaData.horizontalFakeField = true;
        }
        return fields;
    }

    private void checkOrigFmd(FieldMetaData fieldMetaData) {
        if (!fieldMetaData.origFmd.origName.equals(fieldMetaData.origName)) {
            throw BindingSupportImpl.getInstance().internal("There is a FieldMeta mismatch");
        }
    }

    private void findNullIndicationFmd(List fields, FieldMetaData fmd) {
        int s = fields.size();
        for (int j = 0; j < s; j++) {
            FieldMetaData fieldMetaData = (FieldMetaData) fields.get(j);
            if (fieldMetaData.jdoField != null && fieldMetaData.jdoField.nullIndicator) {
                fmd.setNullIndicatorFmd(fieldMetaData);
                break;
            }
        }
    }

    private void checkRecursiveEmbedded(FieldMetaData fieldMetaData, FieldMetaData fmd, HashMap parents) {
        if (fieldMetaData.jdoField != null && fieldMetaData.isDirectRef()) {
            JdoExtension ext = null;
            if (fieldMetaData.jdoField.extensions != null) {
                ext = JdoExtension.find(JdoExtensionKeys.EMBEDDED, fieldMetaData.jdoField.extensions);
            }
            if (ext != null) {
                if (ext.getBoolean()) {
                    fieldMetaData.embedded = true;
                } else {
                    fieldMetaData.embedded = false;
                }
            } else {
                if (fieldMetaData.embedded) {
                    HashSet parentTypes = new HashSet(5);
                    FieldMetaData field = fmd;
                    while(field != null){
                        ClassMetaData refClass = field.typeMetaData;
                        while(refClass != null){
                            parentTypes.add(refClass);
                            refClass = refClass.pcSuperMetaData;
                        }
                        FieldMetaData tempParent = (FieldMetaData)parents.get(field);
                        if(tempParent == null){
                            refClass = field.classMetaData;
                            while(refClass != null){
                                parentTypes.add(refClass);
                                refClass = refClass.pcSuperMetaData;
                            }
                        }
                        if(parentTypes.contains(fieldMetaData.typeMetaData)){
                            fieldMetaData.embedded = false;
                            break;
                        }
                        field = tempParent;
                    }
                }
            }
        }
    }

    /**
     * Create the FieldMetaData for class cmd and recursively all of its
     * subclasses.
     */
    private void createFieldMetaDataImp(ClassMetaData cmd, boolean quiet) {
        HashMap fieldMap = new HashMap(); // name -> FieldMetaData
        createFmdWithReflection(cmd, fieldMap, "");
        updateFmdFromMetadata(cmd, fieldMap, quiet, cmd.jdoClass.elements);
        List fields = updateFmd(fieldMap, cmd, quiet);
        finalizeFmds(fields, cmd, quiet);
    }

    /**
     * extract all the persistent and transactional fields and fill in more info.
     * Return a list of managed fields.
     */
    private List updateFmd(Map fieldMap, ClassMetaData cmd, boolean quiet) {
        ArrayList fields = new ArrayList();
        FieldMetaData fmd = null;
        for (Iterator i = fieldMap.values().iterator(); i.hasNext();) {
            fmd = (FieldMetaData)i.next();
            if (fmd.persistenceModifier == PERSISTENCE_MODIFIER_NONE) continue;
            JdoField jdoField = fmd.jdoField;
            if (Modifier.isTransient(fmd.modifiers)) {
                if (jdoField == null) continue;
            }
            fields.add(fmd);
            Class tt = fmd.componentType;
            if (tt == null) tt = fmd.type;
            fmd.typeMetaData = jmd.getClassMetaData(tt);
            if (fmd.category == 0) {
                fmd.category = mdutils.getFieldCategory(
                        fmd.persistenceModifier,
                        fmd.type, classAndInterfaceMap);
            }
            if (fmd.category == MDStatics.CATEGORY_COLLECTION
                    || fmd.category == MDStatics.CATEGORY_ARRAY
                    || fmd.category == MDStatics.CATEGORY_MAP) {
                fmd.collectionDiffType = true;
            }
            if ((!fmd.embedded && fmd.category == CATEGORY_REF || fmd.category == CATEGORY_POLYREF)
                    && (jdoField == null || jdoField.defaultFetchGroup == NOT_SET)) {
                fmd.fetchOIDInDfg = getClassInfo(cmd).refsInDefaultFetchGroup;
            }
            if (fmd.category == MDStatics.CATEGORY_TRANSACTIONAL && fmd.scoField == true) {
                fmd.scoField = false;
            }
            if (fmd.category == MDStatics.CATEGORY_EXTERNALIZED) {
                if (fmd.externalizer == null) {
                    fmd.externalizer = getExternalizerForType(fmd.type);
                }
                fmd.scoField = false;
            }
            if (fmd.category != MDStatics.CATEGORY_TRANSACTIONAL) {
                try {
                    fillCollectionMetaData(fmd);
                } catch (RuntimeException e) {
                    fmd.addError(e, quiet);
                }
            }
        }
        // sort by field name i.e. into fieldNo order
        Collections.sort(fields);
        return fields;
    }

    /**
     * Look at all the meta data from .jdo files and merge it in.
     */
    private void updateFmdFromMetadata(ClassMetaData cmd, Map fieldMap,
            boolean quiet, JdoElement[] ea) {
        int n = ea.length;
        for (int i = 0; i < n; i++) {
            JdoElement o = ea[i];
            if (!(o instanceof JdoField)) continue;
            JdoField jdoField = (JdoField)o;
            if (jdoField.persistenceModifier == PERSISTENCE_MODIFIER_NONE) {
                fieldMap.remove(jdoField.name);
                continue;
            }

            FieldMetaData fmd = (FieldMetaData)fieldMap.get(jdoField.name);
            /**
             * The fmd might be null
             */
            if (fmd == null) {
                final String prefix = cmd.getShortName() + ".";
                if (jdoField.name.startsWith(prefix)) {
                    String name = jdoField.name.substring(prefix.length());
                    fmd = (FieldMetaData)fieldMap.get(name);
                }
            }
            if (fmd == null) {
                int cIndex = jdoField.name.lastIndexOf("/");
                if (cIndex != -1) {
                    String name = jdoField.name.substring(cIndex);
                    fmd = (FieldMetaData)fieldMap.get(name);
                }
            }
            try {
                if (fmd == null) {
                    if (cmd.horizontal ||
                            (cmd.horizontalCMD != null && (jdoField.name.indexOf(cmd.horizontalCMD.getShortName() + ".")!= -1))) {
                        //this is a metadata for a horizontal field so ignore it for now
                        continue;
                    }
                    throw BindingSupportImpl.getInstance().runtime("Field " +
                            jdoField.name + " not found on " + cmd.qname + "\n" +
                            jdoField.getContext());
                }
                if (fmd.jdoField != null) {
                    throw BindingSupportImpl.getInstance().runtime("Duplicate meta data for field " + jdoField.name + "\n" +
                            jdoField.getContext());
                }
                fmd.jdoField = jdoField;
                fmd.cascadeType = jdoField.cascadeType;

                if (jdoField.persistenceModifier != NOT_SET) {
                    fmd.persistenceModifier = jdoField.persistenceModifier;
                }

                // do not allow static or final fields
                if (!mdutils.isPersistentModifiers(fmd.modifiers)) {
                    throw BindingSupportImpl.getInstance().runtime("Field " + jdoField.name + " is static and/or final\n" +
                            jdoField.getContext());
                }

                // fill in more basic info
                fmd.primaryKey = jdoField.primaryKey;
                fmd.nullValue = jdoField.nullValue;
                if (fetchGroupBuilder.findFetchGroupExt(jdoField.extensions) != null) {
                    fmd.defaultFetchGroup = false;
                } else if (jdoField.defaultFetchGroup != NOT_SET) {
                    fmd.defaultFetchGroup = jdoField.defaultFetchGroup == TRUE;
                }
                if (jdoField.embedded != NOT_SET) {
                    fmd.embedded = jdoField.embedded == TRUE;
                }
                fmd.jdoCollection = jdoField.collection;
                fmd.jdoArray = jdoField.array;
                fmd.jdoMap = jdoField.map;

                // process extensions
                processFieldExtensions(fmd, quiet);

                // make sure transactional fields do not end up in the default
                // fetch group
                if (fmd.persistenceModifier == PERSISTENCE_MODIFIER_TRANSACTIONAL) {
                    fmd.defaultFetchGroup = false;
                }
            } catch (RuntimeException e) {
                if (quiet) {
                    if (fmd != null) fmd.addError(e, quiet);
                    else cmd.addError(e, quiet);
                    continue;
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Create metadata using reflection.
     * @param cmd
     * @param fieldMap
     */
    private void createFmdWithReflection(ClassMetaData cmd, Map fieldMap, String prefix) {
        ClassMetaData.FieldInfo[] fa = cmd.getDeclaredFields();
        boolean embIntern = cmd.structType;
        for (int i = fa.length - 1; i >= 0; i--) {
            ClassMetaData.FieldInfo f = fa[i];
            String fname = f.getName();

            if (mdutils.isEnhancerAddedField(fname))
                continue;

            FieldMetaData fmd = new FieldMetaData();
            fmd.classMetaData = cmd;
            fmd.name = prefix + fname;
            fmd.origName = fname;
            fmd.setType(f.getType());
            fmd.setComponentType(fmd.type.getComponentType());
            fmd.modifiers = f.getModifiers();
            fmd.scoField = mdutils.isMutableType(fmd.type);
            if (mdutils.isDefaultPersistentField(f,
                    classAndInterfaceMap)) {
                fmd.persistenceModifier = PERSISTENCE_MODIFIER_PERSISTENT;
            } else {
                fmd.persistenceModifier = PERSISTENCE_MODIFIER_NONE;
            }
            fmd.nullValue = NULL_VALUE_NONE;
            fmd.defaultFetchGroupDefault = mdutils.isDefaultFetchGroupType(fmd.type);
            fmd.defaultFetchGroup = fmd.defaultFetchGroupDefault;
            fmd.embedded = mdutils.isEmbeddedType(fmd,jmd);
            fmd.embeddedInternally = embIntern;
            fieldMap.put(fmd.name, fmd);
        }
    }

    /**
     * The supplied 'fields' list must be sorted on the natural ordering of fmd's.
     */
    private void finalizeFmds(List fields, ClassMetaData cmd, boolean quiet) {
        // create our fields array
        FieldMetaData[] fmda = cmd.fields = new FieldMetaData[fields.size()];
        fields.toArray(fmda);
        cmd.realFieldCount = fmda.length;

        // set the fieldNo for each field
        for (int i = fmda.length - 1; i >= 0; i--) {
            fmda[i].fieldNo = i;
        }

        // process all subclasses
        ClassMetaData[] pcSubclasses = cmd.pcSubclasses;
        if (pcSubclasses != null) {
            int len = pcSubclasses.length;
            for (int i = 0; i < len; i++) {
                createFieldMetaDataImp(pcSubclasses[i], quiet);
            }
        }
        updateScoFields(cmd);
    }

    private void updateScoFields(ClassMetaData cmd) {
        FieldMetaData fmd;
        for (int i = 0; i < cmd.fields.length; i++) {
            fmd = cmd.fields[i];
            if (fmd.category == MDStatics.CATEGORY_ARRAY
                    || fmd.category == MDStatics.CATEGORY_COLLECTION
                    || fmd.category == MDStatics.CATEGORY_MAP

                    || fmd.typeCode == MDStatics.DATE) {
                fmd.setScoField(true);
                setSCOFactory(fmd);
            }
        }
    }

    /**
     * Process the extensions for fmd (if any).
     */
    private void processFieldExtensions(FieldMetaData fmd, boolean quite) {
        JdoExtension[] a = fmd.jdoField.extensions;
        if (a == null) return;
        for (int i = 0; i < a.length; i++) {
            JdoExtension e = a[i];
            switch (e.key) {
                case JdoExtensionKeys.NULL_INDICATOR:
                    fmd.embedded = true;
                    break;
                case JdoExtensionKeys.FIELD:
                    fmd.embedded = true;
                    break;
                case JdoExtensionKeys.EXTERNALIZER:
                    fmd.category = MDStatics.CATEGORY_EXTERNALIZED;
                    try {
                        fmd.externalizer = createExternalizer(fmd.type, e);
                    } catch (RuntimeException x) {
                        fmd.addError(x, quiet);
                    }
                    break;
                case JdoExtensionKeys.NULL_IF_NOT_FOUND:
                    fmd.nullIfNotFound = e.getBoolean();
                    break;
                case JdoExtensionKeys.DEPENDENT:
                    fmd.dependentValues = e.getBoolean();
                    break;
                case JdoExtensionKeys.KEYS_DEPENDENT:
                    fmd.dependentKeys = e.getBoolean();
                    break;
                case JdoExtensionKeys.AUTOSET:
                    int v = e.getEnum(MDE.AUTOSET_ENUM);
                    int tc = fmd.typeCode;
                    if (v != AUTOSET_NO && tc != DATE && tc != INT
                            && tc != SHORT && tc != BYTE ) {
                        RuntimeException ex = BindingSupportImpl.getInstance().runtime("The autoset extension " +
                                "may only be used on java.util.Date, int, short and byte fields\n" +
                                e.getContext());
                        fmd.addError(ex, quite);
                    }
                    fmd.setAutoSet(v);
                    break;
                case JdoExtensionKeys.NULL_VALUE:
                case JdoExtensionKeys.FETCH_GROUP:
                case JdoExtensionKeys.VALID_CLASS:
                    // these are handled later
                    break;
                case JdoExtensionKeys.SCO_FACTORY:
                    try {
                        Class factoryClass = ClassHelper.get().classForName
                                (e.value, false, loader);
                        Object factory = factoryClass.newInstance();
                        fmd.setScoFactory(factory);
                    } catch (Exception ex) {
                        RuntimeException exception = BindingSupportImpl.getInstance().runtime(
                                "Unable to add SCO factory mapping:\n" + ex.getMessage(),
                                ex);
                        fmd.addError(exception, quite);
                    }
                    break;
                default:
                    if (e.isCommon()) {
                        RuntimeException ex = BindingSupportImpl.getInstance().runtime(
                                "Unexpected extension\n " + e.getContext());
                        fmd.addError(ex, quite);
                    }
            }
        }
    }


    /**
     * Convert a meta data type name (int, String, Integer, za.co.hemtech.Blah
     * etc.) into a Class.
     *
     * @param name    The name as in the meta data (e.g. Blah)
     * @param qname   The name with package added (e.g. za.co.hemtech.Blah)
     * @param descr   Description of name for error messages (e.g. value-class)
     * @param context Element to supply context for error messages
     * @return Class or null if name is not found and qname is null
     */
    private Class resolveTypeName(String name, String qname, String descr,
            JdoElement context) {
        Class type = MDStaticUtils.toSimpleClass(name);
        if (type == null) {
            try {
                if (name.endsWith(ARRAY_SUFFIX)) {
                    return /*CHFC*/java.lang.reflect.Array.newInstance(resolveTypeName(
                            name.substring(0, name.length() - ARRAY_SUFFIX.length()),
                            qname.substring(0, qname.length() - ARRAY_SUFFIX.length()),
                            descr, context), 0).getClass()/*RIGHTPAR*/;
                } else {
                    type = loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                //ignore
            }
            if (qname != null) {
                try {
                    type = loadClass(qname);
                } catch (ClassNotFoundException e) {
                    throw BindingSupportImpl.getInstance().runtime(descr + " class not found: " + qname + "\n" +
                            context.getContext(), e);
                }
            }
        }
        return type;
    }

    /**
     * Fill in collection, array or map related fields for the meta data.
     */
    private void fillCollectionMetaData(FieldMetaData fmd) {
        String msg = null;
        JdoExtension[] extensions = null;
        Class t = null;
        switch (fmd.category) {
            case CATEGORY_COLLECTION:
                if (fmd.jdoArray != null) {
                    msg = "array";
                } else if (fmd.jdoMap != null) msg = "map";
                if (msg != null) break;
                Class listClass = /*CHFC*/List.class/*RIGHTPAR*/;
                fmd.ordered = listClass.isAssignableFrom(fmd.type)

                        ;
                fmd.setElementType(/*CHFC*/Object.class/*RIGHTPAR*/);
                JdoCollection col = fmd.jdoCollection;
                if (col != null) {
                    extensions = col.extensions;
                    if (col.elementType != null) {
                        t = resolveTypeName(col.elementType,
                                col.getElementTypeQName(), "element-type", col);
                    }
                } else {
                    t = null;
                    extensions = null;
                }
                if (t == null) {
                    t = MetaDataUtils.getGenericElementType(
                            fmd.getReflectField());
                    if (t == null) t = /*CHFC*/Object.class/*RIGHTPAR*/;
                }

                fmd.setElementType(t);
                fmd.elementTypeMetaData = jmd.getClassMetaData(
                        fmd.elementType);
                if (col != null && mdutils.requiresEmbedding(fmd.elementTypeMetaData,jmd)) {

                }
                if (col != null && col.embeddedElement != NOT_SET) {
                    fmd.embeddedElement = (col.embeddedElement == TRUE);
                }
                break;

            case CATEGORY_ARRAY:
                if (fmd.jdoCollection != null) {
                    msg = "collection";
                } else if (fmd.jdoMap != null) msg = "map";
                if (msg != null) break;
                fmd.ordered = true;
                fmd.setElementType(fmd.componentType);
                fmd.elementTypeMetaData = jmd.getClassMetaData(
                        fmd.elementType);
                JdoArray ar = fmd.jdoArray;
                if (ar != null) {
                    extensions = ar.extensions;
                    if (mdutils.requiresEmbedding(fmd.elementTypeMetaData,jmd)) {

                    }
                    if (ar.embeddedElement != NOT_SET) {
                        fmd.embeddedElement = ar.embeddedElement == TRUE;
                    }
                }
                break;

            case CATEGORY_MAP:
                if (fmd.jdoArray != null) {
                    msg = "array";
                } else if (fmd.jdoCollection != null) msg = "collection";
                if (msg != null) break;
                fmd.setElementType(/*CHFC*/Object.class/*RIGHTPAR*/);
                fmd.setKeyType(/*CHFC*/Object.class/*RIGHTPAR*/);
                JdoMap map = fmd.jdoMap;
                extensions = map == null ? null : map.extensions;
                if (map != null) {

						t = resolveTypeName(map.valueType, map.getValueTypeQName(),
							"value-type", map);

                    extensions = map.extensions;
                } else {
                    t = null;
                    extensions = null;
                }
                if (t == null) {
                    t = MetaDataUtils.getGenericValueType(
                            fmd.getReflectField());
                    if (t == null) t = /*CHFC*/Object.class/*RIGHTPAR*/;
                }
                fmd.setElementType(t);
                fmd.elementTypeMetaData = jmd.getClassMetaData(
                        fmd.elementType);

                if (map != null && mdutils.requiresEmbedding(fmd.elementTypeMetaData,jmd)) {

                }

                if (map != null && map.embeddedValue != NOT_SET) {
                    fmd.embeddedElement = map.embeddedValue == TRUE;
                }
                if (map != null ) {
                    t = resolveTypeName(map.keyType, map.getKeyTypeQName(),
                            "key-type", map);
                } else 	{
                    t = null;
                }
                if (t == null) {
                    t = MetaDataUtils.getGenericKeyType(fmd.getReflectField());
                    if (t == null) t = /*CHFC*/Object.class/*RIGHTPAR*/;
                }
                fmd.setKeyType(t);
                fmd.keyTypeMetaData = jmd.getClassMetaData(fmd.keyType);

                if (map != null && mdutils.requiresEmbedding(fmd.keyTypeMetaData,jmd)) {

                }

                if (map != null && map.embeddedKey != NOT_SET) {
                    fmd.embeddedKey = map.embeddedKey == TRUE;
                }
                break;
        }
        if (msg != null) {
            throw BindingSupportImpl.getInstance().runtime("Element <" + msg + "> is " +
                    "not allowed for field " + fmd.name + "\n" +
                    fmd.jdoField.getContext());
        }

        // if field has dependent=true check that this is ok
        switch (fmd.category) {
            case CATEGORY_COLLECTION:
            case CATEGORY_ARRAY:
            case CATEGORY_MAP:
                // if dependent is true then the element/value must be PC class
                if (!fmd.dependentValues || fmd.isElementTypePC()) break;
            default:
                if (fmd.category == CATEGORY_REF
                        || fmd.category == CATEGORY_POLYREF
                        || !fmd.dependentValues) {
                    break;
                }
                throw BindingSupportImpl.getInstance().runtime("The dependent extension is only valid for " +
                        "references, collections and maps of persistent classes\n" +
                        fmd.jdoField.getContext());
        }

        // if field keys-dependent=true check that this is ok
        if (fmd.category == CATEGORY_MAP) {
            // if keys-dependent is true then the key must be PC class
            if (fmd.dependentKeys && fmd.keyTypeMetaData == null) {
                throw BindingSupportImpl.getInstance().runtime("The keys-dependent extension is only valid for " +
                        "maps with a persistent class key\n" +
                        fmd.jdoField.getContext());
            }
        } else if (fmd.dependentKeys) {
            throw BindingSupportImpl.getInstance().runtime("The keys-dependent extension is only valid for maps\n" +
                    fmd.jdoField.getContext());
        }

        // process extensions
        if (extensions != null) {
            int n = extensions.length;
            JdoExtension ordered = null;
            JdoExtension ordering = null;
            for (int i = 0; i < n; i++) {
                JdoExtension e = extensions[i];
                switch (e.key) {
                    case JdoExtensionKeys.ORDERED:
                        if (!fmd.ordered) {
                            throw BindingSupportImpl.getInstance().runtime("The ordered extension is not allowed here\n" +
                                    e.getContext());
                        }
                        ordered = e;
                        break;
                    case JdoExtensionKeys.ORDERING:
                        ordering = e;
                        break;
                    case JdoExtensionKeys.MANAGED:
                        // this is processed later by the JdbcMetaDataBuilder
                        break;
                    default:
                        if (e.isCommon()) {
                            throw BindingSupportImpl.getInstance().runtime(
                                    "Unexpected extension\n" + e.getContext());
                        }
                }
            }
            if (ordered != null) fmd.ordered = ordered.getBoolean();
            if (ordering != null) {
                if (ordered != null && ordered.getBoolean()) {
                    throw BindingSupportImpl.getInstance().runtime("You may not specify an ordering if you also have ordered=true\n" +
                            ordering.getContext());
                }
                fmd.ordered = false;
                try {
                    fmd.ordering = queryParser.parseOrdering(
                            fmd.elementTypeMetaData, ordering.getString());
                } catch (ParseException e) {
                    throw BindingSupportImpl.getInstance().runtime("Invalid ordering extension: " + e.getMessage() + "\n" +
                            ordering.getContext() + "\n", e);
                }
            }
        }
    }

    /**
     * Fill in collection, array or map related fields for the meta data.
     */
    private void setSCOFactory(FieldMetaData fmd) {
        if (fmd.checkCustomFactory()) {
            return;
        }
        if (fmd.scoField) {
            switch (fmd.category) {
                case CATEGORY_SIMPLE:
                    if (fmd.simpleSCOFactory == null) {
                        fmd.simpleSCOFactory = scoFactoryRegistry.getJdoGenieSCOFactory(
                                fmd);
                    }
                    break;
                case CATEGORY_COLLECTION:
                    if (fmd.collectionFactory == null) {
                        fmd.collectionFactory = scoFactoryRegistry.getJDOGenieSCOCollectionFactory(
                                fmd);
                    }
                    break;
                case CATEGORY_MAP:
                    if (fmd.mapFactory == null) {
                        fmd.mapFactory = scoFactoryRegistry.getJDOGenieSCOMapFactory(
                                fmd);
                    }
                    break;
            }
        }
    }

    /**
     * Get the ClassInfo for cmd. A new ClassInfo will be created if none
     * exists.
     */
    private ClassInfo getClassInfo(ClassMetaData cmd) {
        ClassInfo ans = (ClassInfo)classInfoMap.get(cmd);
        if (ans == null) classInfoMap.put(cmd, ans = new ClassInfo());
        return ans;
    }

    /**
     * Fill the externalizerMap from the configuration.
     */
    private void fillExternalizerMap() {
        externalizerMap.clear();
        int n = config.externalizers.size();
        for (int i = 0; i < n; i++) {
            ConfigInfo.ExternalizerInfo ei =
                    (ConfigInfo.ExternalizerInfo)config.externalizers.get(i);
            if (!ei.enabled) continue;
            try {
                Class key = loadClass(ei.typeName);
                Class cls = loadExternalizerClass(ei.externalizerName);
                externalizerMap.put(key, createExternalizer(cls, key, ei.args));
                mdutils.registerExternalizedType(key);
            } catch (Throwable x) {
                RuntimeException e;
                if (BindingSupportImpl.getInstance().isOwnException(x)) {
                    e = (RuntimeException)x;
                } else {
                    e = BindingSupportImpl.getInstance().runtime("Unable to create Externalizer for '" +
                            ei.typeName + "':\n" + x.toString(), x);
                }
                jmd.addError(e, quiet);
            }
        }
    }

    private Externalizer createExternalizer(Class externalizerCls,
            Class type, Map props) throws IllegalAccessException,
            InstantiationException, InvocationTargetException {
        Externalizer externalizer;
        try {
            Constructor m = externalizerCls.getConstructor(
                    new Class[]{/*CHFC*/Class.class/*RIGHTPAR*/});
            externalizer = (Externalizer)m.newInstance(new Object[]{type});
        } catch (NoSuchMethodException e) {
            externalizer = (Externalizer)externalizerCls.newInstance();
        }
        BeanUtils.setProperties(externalizer, props);
        return externalizer;
    }

    /**
     * Load an externalizer class. This will recognize the short names of
     * the built in externalizers and returns the default externalizer for
     * a null name.
     */
    private Class loadExternalizerClass(String name)
            throws ClassNotFoundException {
        if (name == null || SerializedExternalizer.SHORT_NAME.equals(name)) {
            return /*CHFC*/SerializedExternalizer.class/*RIGHTPAR*/;
        } else if (TypeAsBytesExternalizer.SHORT_NAME.equals(name)) {
            return /*CHFC*/TypeAsBytesExternalizer.class/*RIGHTPAR*/;
        } else if (TypeAsStringExternalizer.SHORT_NAME.equals(name)) {
            return /*CHFC*/TypeAsStringExternalizer.class/*RIGHTPAR*/;
        }
        return loadClass(name);
    }

    /**
     * Get a externalizer instance for a field type or Serializing Externalizer if null.
     */
    private Externalizer getExternalizerForType(Class type) {
        Externalizer ex = (Externalizer) externalizerMap.get(type);
        Class serializableClass = /*CHFC*/Serializable.class/*RIGHTPAR*/;
        if (ex == null && serializableClass.isAssignableFrom(type)) {
            try {
                ex = createExternalizer(/*CHFC*/SerializedExternalizer.class/*RIGHTPAR*/, type, null);
            } catch (Exception e) {
                throw BindingSupportImpl.getInstance().internal(e.getMessage(), e);
            }
        }
        return ex;
    }

    /**
     * Create a Externalizer instance from an extension. This will return
     * instances of standard externalizers if their SHORT_NAME's are used.
     */
    private Externalizer createExternalizer(Class type, JdoExtension e) {
        try {
            String cname = e.getString();
            Class cls = loadExternalizerClass(cname);
            return createExternalizer(cls, type, e.getPropertyMap());
        } catch (Throwable x) {
            x = BindingSupportImpl.getInstance().findCause(x);
            if (BindingSupportImpl.getInstance().isOwnException(x)) {
                throw (RuntimeException)x;
            }
            throw BindingSupportImpl.getInstance().runtime(x.toString(), x);
        }
    }

    /**
     * Should the current state information be sent with a request to load
     * any fetch group containing secondary fields?
     */
    public boolean isSendCurrentForFGWithSecFields() {
        return false;
    }

    /**
     * When writing to an object must it be completely read first?
     */
    public boolean isReadObjectBeforeWrite() {
        return false;
    }
}
