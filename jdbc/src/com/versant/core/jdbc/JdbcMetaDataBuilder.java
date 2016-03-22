
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
package com.versant.core.jdbc;

import com.versant.core.common.Debug;
import com.versant.core.metadata.*;
import com.versant.core.metadata.parser.*;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdbc.sql.AutoIncJdbcKeyGenerator;
import com.versant.core.jdbc.sql.HighLowJdbcKeyGenerator;
import com.versant.core.jdbc.sql.JdbcNameGenerator;
import com.versant.core.jdbc.sql.SqlDriver;
import com.versant.core.util.BeanUtils;
import com.versant.core.util.StringListParser;

import java.sql.Types;
import java.util.*;

import com.versant.core.common.BindingSupportImpl;
import com.versant.core.common.config.ConfigInfo;

/**
 * This builds the meta data for the JdbcStorageManager.
 */
public class JdbcMetaDataBuilder extends MetaDataBuilder
        implements JdoExtensionKeys {

    private final JdbcConfig jdbcConfig;
    private final SqlDriver sqlDriver;

    private JdbcNameGenerator nameGenerator;
    private JdbcMappingResolver mappingResolver;

    public final MetaDataEnums MDE = new MetaDataEnums();
    public final JdbcMetaDataEnums jdbcMDE = new JdbcMetaDataEnums();

    private JdbcKeyGeneratorFactoryRegistry keyGenRegistry;
    private JdbcConverterFactoryRegistry converterRegistry;
    private JdbcClassReferenceGraph jdbcClassReferenceGraph;

    private Map emptyArrayMap = new HashMap();

    /**
     * Maps ClassMetaData to ClassInfo.
     */
    private Map classInfoMap = new HashMap();

    // these are the "fieldnames" used when resolving mappings for these cols
    public static final String DATASTORE_PK_FIELDNAME = "<pk>";
	public static final String CLASS_ID_FIELDNAME = "<class-id>";
	public static final String OPT_LOCK_FIELDNAME = "<opt-lock>";
	public static final String OWNER_REF_FIELDNAME = "<owner>";
    public static final String SEQUENCE_FIELDNAME = "<sequence>";
    public static final String VALUE_FIELDNAME = "<value>";
    public static final String KEY_FIELDNAME = "<key>";

    // these are the names of the built in key generators
    public static final String KEYGEN_HIGHLOW = "HIGHLOW";
    public static final String KEYGEN_AUTOINC = "AUTOINC";

    /**
     * These hold tempory info we need to track for a class during meta data
     * generation. Each class for our store is associated with one of these
     * through the classInfoMap.
     */
    public static class ClassInfo {

        public ClassMetaData cmd;

        public ArrayList elements;
        public JdbcKeyGeneratorFactory keyGenFactory;
        public Object keyGenFactoryArgs;
        public String pkConstraintName;
        public JdoExtension optimisticLockingExt;
        public String pkFkConstraintName;
        public JdbcConstraint pkFkConstraint;
        public ArrayList indexExts = new ArrayList();
        public ArrayList autoIndexes = new ArrayList(); // of JdbcIndex
        public JdoExtension inheritance;
        public JdoExtension classIdExt;
        public boolean noClassIdCol;
        private Set createdAfterClient;

        public Set getCreatedAfterClient() {
            if (createdAfterClient == null) {
                createdAfterClient = new HashSet();
            }
            return createdAfterClient;
        }
    }

    public synchronized Object getEmptyArray(Class cls) {
        Object res = emptyArrayMap.get(cls);
        if (res == null) {
            res = java.lang.reflect.Array.newInstance(cls, 0);
            emptyArrayMap.put(cls, res);
        }
        return res;
    }

    /**
     * The jdbcDriver parameter may be null if this is not available.
     */
    public JdbcMetaDataBuilder(ConfigInfo config, JdbcConfig jdbcConfig,
            ClassLoader loader, SqlDriver sqlDriver, boolean quiet) {
        super(config, loader, quiet);
        this.jdbcConfig = jdbcConfig;
        this.sqlDriver = sqlDriver;
    }

    protected FetchGroupBuilder createFetchGroupBuilder() {
        return new JdbcFetchGroupBuilder(jmd);
    }

    public boolean isCdRefsInDefaultFetchGroup() {
        return jdbcConfig.oidsInDefaultFetchGroup;
    }

    public int getCdCacheStrategy() {
        return jdbcConfig.cacheStrategy;
    }

    public ModelMetaData buildMetaData(JdoRoot[] roots) {
        keyGenRegistry = new JdbcKeyGeneratorFactoryRegistry(loader);
        keyGenRegistry.add(KEYGEN_HIGHLOW,
                keyGenRegistry.getFactory(
                        HighLowJdbcKeyGenerator.Factory.class.getName()));
        keyGenRegistry.add(KEYGEN_AUTOINC,
                keyGenRegistry.getFactory(
                        AutoIncJdbcKeyGenerator.Factory.class.getName()));
        converterRegistry = new JdbcConverterFactoryRegistry(loader);

        mappingResolver = new JdbcMappingResolver();
        mappingResolver.init(sqlDriver, parseTypeMappings(),
                parseJavaTypeMappings());
        mappingResolver.registerStoreTypes(mdutils);

        if (jdbcConfig.jdbcKeyGenerator == null) {
            jdbcConfig.jdbcKeyGenerator = JdbcMetaDataBuilder.KEYGEN_HIGHLOW;
        }
        if (jdbcConfig.jdbcKeyGeneratorProps == null) {

            jdbcConfig.jdbcKeyGeneratorProps = Collections.EMPTY_MAP;


        }

        if (jdbcConfig.jdbcNameGenerator != null) {
            nameGenerator = (JdbcNameGenerator)BeanUtils.newInstance(
                    jdbcConfig.jdbcNameGenerator, loader, /*CHFC*/JdbcNameGenerator.class/*RIGHTPAR*/);
        } else {
            nameGenerator = sqlDriver.createJdbcNameGenerator();
        }
        BeanUtils.setProperties(nameGenerator, jdbcConfig.jdbcNameGeneratorProps);
        jmd.jdbcMetaData = new JdbcMetaData(jmd, jdbcConfig);

        return super.buildMetaData(roots);
    }

    protected void doHorizontal(ClassMetaData[] ca) {
        //create all the fake field for horizontal instances
        for (int j = 0; j < ca.length; j++) {
            ClassMetaData cmd = ca[j];
            try {
                createHorizontalFieldMetaData(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }
    }

    protected void doEmbedded(ClassMetaData[] ca) {
        //create all the fake field for embedded instances
        for (int j = 0; j < ca.length; j++) {
            ClassMetaData cmd = ca[j];
            try {
                createEmbeddeFieldMetaData(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }
    }

    protected void checkForHorizontal(JdoClass jdoCls, ClassMetaData cmd) {
        if (jdoCls.getInheritance(jdbcMDE.INHERITANCE_ENUM) == JdbcClass.INHERITANCE_HORIZONTAL) {
            cmd.horizontal = true;
        }
    }

    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public JdbcNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    /**
     * Get the ClassInfo for cmd.
     */
    public ClassInfo getClassInfo(ClassMetaData cmd) {
        return (ClassInfo)classInfoMap.get(cmd);
    }

    /**
     * Get the elements field of the ClassInfo for cmd.
     */
    private ArrayList getClassElements(ClassMetaData cmd) {
        return getClassInfo(cmd).elements;
    }

    protected  void preBuildFetchGroupsHook() {
        ClassMetaData[] classes = jmd.classes;
        int clen = classes.length;

        // find all classes that belong to us and do whatever we can without
        // having all the JdbcClass objects and tables yet
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating JdbcClass objects ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData != null) {
                continue;
            }
            try {
                createJdbcClass(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // Make sure that all classes have tables and table names. This is
        // done starting at the base classes and recursively processing
        // subclasses.
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating JdbcTable objects ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                processBaseClassTable(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }

        }

        // find the primary keys (datastore or application) for all classes
        // and make sure the pk columns have names
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Finding and naming primary keys ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData != null) continue;
            try {
                processPrimaryKey(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // create descriminator columns and figure out values
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating descriminator columns ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData != null) {
                continue;
            }
            try {
                processClassIdCol(cmd, quiet, new HashMap());
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // processing the persist after extensions
        for (int j = 0; j < clen; j++) {
            ClassMetaData cmd = classes[j];
            ClassInfo cInfo = getClassInfo(cmd.top);
            cInfo.getCreatedAfterClient().clear();
            JdoElement[] elements = cmd.jdoClass.elements;
            int n = elements.length;
            for (int i = 0; i < n; i++) {
                JdoElement o = elements[i];
                if (o instanceof JdoExtension) {
                    JdoExtension e = (JdoExtension)o;
                    if (e.key == PERSIST_AFTER) {
                        if (e.nested == null) continue;
                        for (int k = 0; k < e.nested.length; k++) {
                            JdoExtension jdoExtension = e.nested[k];
                            if (jdoExtension != null) {
                                cInfo.getCreatedAfterClient().add(jmd.getClassMetaData(
                                        jdoExtension.value).top);
                            }
                        }
                    }
                }
            }
        }

        // calc maxPkSimpleColumns and set datastoreIdentityType
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Calculating maxPkSimpleColumns ... ");
        }
        int maxPkSimpleColumns = 0;
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                if (cmd.horizontal) continue;
                if (cmd.embeddedOnly) continue;
                JdbcClass jdbcClass = ((JdbcClass)cmd.storeClass);
                int n = jdbcClass.table.pkSimpleColumnCount;
                if (n > maxPkSimpleColumns) {
                    maxPkSimpleColumns = n;
                }
                cmd.datastoreIdentityTypeCode = jdbcClass.table.pk[0].javaTypeCode;
                cmd.datastoreIdentityType = jdbcClass.table.pk[0].javaType;
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }
        ((JdbcMetaData)jmd.jdbcMetaData).maxPkSimpleColumns = maxPkSimpleColumns;

        // complete optimistic locking - this is done now so that use visible
        // fields can be used for rowversion and timestamp locking
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Completing optimistic locking ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData != null) continue;
            try {
                completeOptimisticLocking(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // complete REF fields now that we have all the classes
        // and primary keys
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating REF fields ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                processRefAndPolyRefFields(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // complete COLLECTION fields now that we have all the classes
        // and primary keys and refs
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating COLLECTION fields ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                if (cmd.horizontal) continue;
                if (cmd.embeddedOnly) continue;
                processCollectionFields(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // create the cols arrays on all class tables, name all field columns
        // without names and sanity check columns and add any extra fake
        // fields to the fields array on ClassMetaData
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "MDB-JDBC: Finalizing table column arrays and fake fields ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.horizontal) continue;
            if (cmd.embeddedOnly) continue;
            if (cmd.pcSuperMetaData != null) continue;
            try {
                finalizeFakesAndTableColumns(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        jdbcClassReferenceGraph = new JdbcClassReferenceGraph(jmd.classes);
        jdbcClassReferenceGraph.sort();

        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating constraints ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                doConstraints(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        //release resources that was used for sorting
        jdbcClassReferenceGraph.releaseMem();

        // build the fields array on JdbcClass for each class
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Finalizing fields arrays ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            FieldMetaData[] fa = cmd.fields;
            if (fa == null) continue;
            JdbcField[] a = ((JdbcClass)cmd.storeClass).fields = new JdbcField[fa.length];
            for (int j = fa.length - 1; j >= 0; j--) {
                FieldMetaData fmd = fa[j];
                fmd.fieldNo = j; // need to do this in case of fake fields
                JdbcField jdbcField = (JdbcField)fmd.storeField;
                a[j] = jdbcField;
                if (jdbcField != null) jdbcField.initMainTableCols();
            }
        }

        // figure out which columns should be marked as shared
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Choosing shared columns ... ");
        }
        SharedColumnChooser sharedColumnChooser = new SharedColumnChooser();
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData != null) continue;
            if (cmd.horizontal) continue;
            try {
                sharedColumnChooser.chooseSharedColumns(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // build the fields array on JdbcClass for each class
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "MDB-JDBC: Finalizing for update columns for fields ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            JdbcField[] fields = ((JdbcClass)cmd.storeClass).fields;
            if (fields == null) continue;
            for (int j = fields.length - 1; j >= 0; j--) {
                JdbcField f = fields[j];
                if (f != null) f.initMainTableColsForUpdate();
            }
        }

        // copy optimistic locking settings down each hierarchy
        if (Debug.DEBUG) {
            Debug.OUT.println(
                    "MDB-JDBC: Copying optimistic locking down heirachies ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            ((JdbcClass)cmd.storeClass).copyOptimisticLockingToSubs();
        }

        // finalize constraints
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Finalizing table constraints ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                finalizeConstraints(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }
        ArrayList tables =  ((JdbcMetaData)jmd.jdbcMetaData).getTables(true);
        int size = tables.size();
        for (int i = 0; i < size; i++) {
            JdbcTable jdbcTable = (JdbcTable)tables.get(i);
            jdbcTable.nameConstraints(nameGenerator);
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            try {
                finalizeConstraints(cmd);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // find index extensions and create them
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Processing index extensions ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.pcSuperMetaData != null) continue;
            try {
                createMainTableIndexes(cmd, quiet);
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }

        // make sure link table indexes are named
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Naming link table indexes ...");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            FieldMetaData[] fields = cmd.fields;
            if (fields == null) continue;
            for (int j = 0; j < fields.length; j++) {
                JdbcField jf = (JdbcField)fields[j].storeField;
                if (jf != null) {
                    try {
                        jf.nameLinkTableIndexes(nameGenerator);
                    } catch (RuntimeException e) {
                        cmd.addError(e, quiet);
                    }
                }
            }
        }

        // create key generator instances
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Creating key generators ... ");
        }
        createKeyGenerators(quiet);
    }

    /**
     * Create main table indexes for cmd and recursively all of its subclasses.
     * This is a NOP if cmd is not a base class. This also make sure that all
     * of the indexes have names and gets rid of duplicate indexes (only
     * the first one is kept).
     */
    private void createMainTableIndexes(ClassMetaData cmd, boolean quiet) {
        if (cmd.pcSuperMetaData != null) return;

        ArrayList all = new ArrayList();
        collectIndexes(cmd, all, quiet);
        ArrayList auto = new ArrayList();
        collectAutoIndexes(cmd, auto);

        // get rid of auto indexes that have same columns as manual indexes
        // or other auto indexes
        HashSet s = new HashSet();
        s.addAll(all);
        for (Iterator i = auto.iterator(); i.hasNext();) {
            Object o = i.next();
            if (s.contains(o)) {
                i.remove();
            } else {
                s.add(o);
            }
        }
        all.addAll(auto);

        Map tablesToIndexs = new HashMap();
        for (int i = 0; i < all.size(); i++) {
            JdbcIndex index = (JdbcIndex)all.get(i);
            JdbcTable table = null;
            boolean ignore = false;
            if (index.cols.length > 0) {
                table = index.cols[0].table;
                ignore = false;
                for (int j = 1; j < index.cols.length; j++) {
                    JdbcColumn col = index.cols[j];
                    if (col.table != table) {
                        System.out.println("\n\n WARNING: This composite index contains colums from " +
                                "2 different tables. Ignoring it");
                        ignore = true;
                    }
                }
            }
            if (ignore) continue;

            Set indexs = (Set)tablesToIndexs.get(table);
            if (indexs == null) {
                indexs = new HashSet();
                tablesToIndexs.put(table, indexs);
            }
            indexs.add(index);
        }

        for (Iterator iterator = tablesToIndexs.entrySet().iterator();
             iterator.hasNext();) {
            Map.Entry mapEntry = (Map.Entry)iterator.next();
            JdbcTable table = (JdbcTable)mapEntry.getKey();
            if (table != null) {
                Set indexs = (Set)mapEntry.getValue();
                JdbcIndex[] a = new JdbcIndex[indexs.size()];
                indexs.toArray(a);
                table.indexes = a;
                for (int i = 0; i < a.length; i++) {
                    if (a[i] != null && a[i].name == null) {
                        generateNameForIndex(nameGenerator, table.name,
                                a[i]);
                    }
                }
            }
        }
    }

    protected void postAllFieldsCreatedHook() {
        ClassMetaData[] ca = jmd.classes;

        // fill the stateFieldNo on each JdbcField
        for (int i = ca.length - 1; i >= 0; i--) {
            ClassMetaData cmd = ca[i];
            JdbcClass jc = (JdbcClass)cmd.storeClass;
            if (jc == null || jc.fields == null) continue;
            for (int j = jc.fields.length - 1; j >= 0; j--) {
                JdbcField f = jc.fields[j];
                if (f == null) {
                    continue;
                }
                f.stateFieldNo = f.fmd.fieldNo + cmd.superFieldCount;
            }
        }

        // fill the stateFields array on each JdbcClass
        for (int i = ca.length - 1; i >= 0; i--) {
            ClassMetaData cmd = ca[i];
            JdbcClass jc = (JdbcClass)cmd.storeClass;
            if (jc == null) continue;
            jc.buildStateFields();
        }
        fillFGMetaData();
    }

    private void collectIndexes(ClassMetaData cmd, ArrayList indexes,
            boolean quiet) {
        ArrayList indexExts = getClassInfo(cmd).indexExts;
        for (int j = 0; j < indexExts.size(); j++) {
            try {
                indexes.add(processIndexExtension(cmd,
                        (JdoExtension)indexExts.get(j), quiet));
            } catch (RuntimeException e) {
                cmd.addError(e, quiet);
            }
        }
        ClassMetaData[] subs = cmd.pcSubclasses;
        if (subs != null) {
            for (int i = 0; i < subs.length; i++) {
                try {
                    collectIndexes(subs[i], indexes, quiet);
                } catch (RuntimeException e) {
                    cmd.addError(e, quiet);
                }
            }
        }
    }

    private void collectAutoIndexes(ClassMetaData cmd, ArrayList indexes) {
        indexes.addAll(getClassInfo(cmd).autoIndexes);
        ClassMetaData[] subs = cmd.pcSubclasses;
        if (subs != null) {
            for (int i = 0; i < subs.length; i++) {
                collectAutoIndexes(subs[i], indexes);
            }
        }
    }

    /**
     * Generate the name for an index. The columns in the index must have
     * names.
     */
    public static void generateNameForIndex(JdbcNameGenerator namegen, String tableName,
            JdbcIndex idx) {
        int n = idx.cols.length;
        String[] cn = new String[n];
        for (int i = 0; i < n; i++) cn[i] = idx.cols[i].name;
        idx.name = namegen.generateIndexName(tableName, cn);
    }

    /**
     * Convert the type mapping strings for s into JdbcTypeMapping instances.
     */
    private ArrayList parseTypeMappings() {
        String sdb = sqlDriver.getName();
        ArrayList in = jdbcConfig.typeMappings;
        int n = in.size();
        ArrayList a = new ArrayList(n);
        StringListParser p = new StringListParser();
        for (int i = 0; i < n; i++) {
            String s = (String)in.get(i);
            p.setString(s);
            JdbcTypeMapping m = new JdbcTypeMapping();
            m.parse(p, converterRegistry);
            String mdb = m.getDatabase();
            if (mdb == null || mdb.equals(sdb)) a.add(m);
        }
        return a;
    }

    /**
     * Convert the java type mapping strings for s into JdbcJavaTypeMapping
     * instances.
     */
    private ArrayList parseJavaTypeMappings() {
        String sdb = sqlDriver.getName();
        ArrayList in = jdbcConfig.javaTypeMappings;
        int n = in.size();
        ArrayList a = new ArrayList(n);
        StringListParser p = new StringListParser();
        for (int i = 0; i < n; i++) {
            String s = (String)in.get(i);
            p.setString(s);
            JdbcJavaTypeMapping m = new JdbcJavaTypeMapping();
            m.parse(p, converterRegistry);
            String mdb = m.getDatabase();
            if (mdb == null || mdb.equals(sdb)) a.add(m);
        }
        return a;
    }

    /**
     * Process an index extension for cmd. This will resolve the names of
     * all fields in the index to their column names and register the name
     * of the index if one has been specified. The index is returned.
     */
    private JdbcIndex processIndexExtension(ClassMetaData cmd, JdoExtension e,
            boolean quiet) {
        JdbcIndex idx = new JdbcIndex();
        idx.name = e.value;
        ArrayList cols = new ArrayList();
        JdoExtension[] a = e.nested;
        int n = a == null ? 0 : a.length;
        for (int i = 0; i < n; i++) {
            JdoExtension ne = a[i];
            switch (ne.key) {
                case JDBC_CLUSTERED:
                    try {
                        idx.clustered = ne.getBoolean();
                    } catch (RuntimeException x) {
                        cmd.addError(x, quiet);
                    }
                    break;
                case JDBC_UNIQUE:
                    try {
                        idx.unique = ne.getBoolean();
                    } catch (RuntimeException x) {
                        cmd.addError(x, quiet);
                    }
                    break;
                case FIELD_NAME:
                    JdbcColumn[] mtc;
                    String fname = ne.getString();
                    if (fname.equals(DATASTORE_PK_FIELDNAME)) {
                        mtc = ((JdbcClass)cmd.storeClass).table.pk;
                    } else {
                        FieldMetaData fmd = cmd.getFieldMetaData(fname);
                        if (fmd == null) {
                            RuntimeException x = BindingSupportImpl.getInstance().runtime("Field '" + ne.value +
                                    "' not found\n" + ne.getContext());
                            cmd.addError(x, quiet);
                        }
                        JdbcField jf = (JdbcField)fmd.storeField;
                        if (jf == null || jf.mainTableCols == null) {
                            RuntimeException x = BindingSupportImpl.getInstance().runtime(
                                    "Field '" + ne.value +
                                    "' is not stored in the main table\n" + ne.getContext());
                            fmd.addError(x, quiet);
                        }
                        mtc = jf.mainTableCols;
                    }
                    for (int j = 0; j < mtc.length; j++) cols.add(mtc[j]);
                    break;
                default:
                    MetaDataBuilder.throwUnexpectedExtension(ne);
            }
        }
        n = cols.size();
        if (n == 0) {
            RuntimeException x = BindingSupportImpl.getInstance().runtime(
                    "Index does not include any fields\n" + e.getContext());
            cmd.addError(x, quiet);
            return null;
        }
        JdbcColumn[] idxCols = new JdbcColumn[n];
        cols.toArray(idxCols);
        idx.setCols(idxCols);
        if (idx.name != null) {
            JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
            try {
                nameGenerator.addIndexName(
                        jdbcClass.table.name,
                        idx.name);
            } catch (IllegalArgumentException x) {
                RuntimeException ex = BindingSupportImpl.getInstance().runtime(
                        x.getMessage(), x);
                cmd.addError(ex, quiet);
            }
        }
        return idx;
    }

    private void fillFGMetaData() {
        ClassMetaData[] classes = jmd.classes;
        int clen = classes.length;
        if (Debug.DEBUG) {
            Debug.OUT.println("MDB-JDBC: Filling fetch group meta data ... ");
        }
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            processFetchGroups(cmd);
        }
    }

    /**
     * Finish up optimistic locking for cmd.
     */
    private void completeOptimisticLocking(ClassMetaData cmd, boolean quiet) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        ClassInfo info = getClassInfo(cmd);
        switch (jdbcClass.optimisticLocking) {
            case JdbcClass.OPTIMISTIC_LOCKING_VERSION:
            case JdbcClass.OPTIMISTIC_LOCKING_TIMESTAMP:
                processTimestampOrRowVersionLocking(jdbcClass, cmd,
                        info.optimisticLockingExt, info.elements, quiet);
                break;
        }
    }

    private void processTimestampOrRowVersionLocking(JdbcClass jdbcClass,
            ClassMetaData cmd, JdoExtension e, ArrayList jdbcElements,
            boolean quiet) {
        boolean timestamp =
                jdbcClass.optimisticLocking == JdbcClass.OPTIMISTIC_LOCKING_TIMESTAMP;
        JdoExtension[] nested = e == null ? null : e.nested;
        if (nested != null && nested[0].key == JdoExtensionKeys.FIELD_NAME) {
            if (nested.length > 1) {
                RuntimeException x = BindingSupportImpl.getInstance().runtime("Unexpected extension: " +
                        nested[1].getContext());
                cmd.addError(x, quiet);
            }
            String fieldName = nested[0].value;
            FieldMetaData fmd = cmd.getFieldMetaData(fieldName);
            if (fmd == null) {
                RuntimeException x = BindingSupportImpl.getInstance().runtime("Field '" + fieldName +
                        " not found\n" + nested[0].getContext());
                cmd.addError(x, quiet);
            }
            int tc = fmd.typeCode;
            if (timestamp) {
                if (tc != MDStatics.DATE) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime(
                            "Field '" + fieldName +
                            " is not a java.util.Date\n" + nested[0].getContext());
                    fmd.addError(x, quiet);
                }
            } else {
                if (tc != MDStatics.INT && tc != MDStatics.SHORT
                        && tc != MDStatics.BYTE) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime(
                            "Field '" + fieldName +
                            " is not an int, short or byte\n" + nested[0].getContext());
                    fmd.addError(x, quiet);
                }
            }
            cmd.optimisticLockingField = fmd;
            jdbcClass.optimisticLockingField = (JdbcSimpleField)fmd.storeField;
            fmd.setAutoSet(MDStatics.AUTOSET_BOTH);
        } else {
            JdbcColumn tc = createColumn(nested,
                    OPT_LOCK_FIELDNAME, timestamp ? (Class)/*CHFC*/Date.class/*RIGHTPAR*/ : (Class)/*CHFC*/Short.TYPE/*RIGHTPAR*/);
            // generate a fake field for the column
            JdbcSimpleField f = jdbcClass.optimisticLockingField = new JdbcSimpleField();
            f.fake = true;
            f.col = tc;
            FieldMetaData fmd = f.fmd = new FieldMetaData();
            fmd.fake = true;
            fmd.category = MDStatics.CATEGORY_SIMPLE;
            fmd.primaryField = true;
            fmd.classMetaData = cmd;
            fmd.defaultFetchGroup = true;
            fmd.storeField = f;

			fmd.name = timestamp ? "jdoTimestamp" : "jdoVersion";


            fmd.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
            fmd.setType(tc.javaType);
            fmd.setAutoSet(MDStatics.AUTOSET_BOTH);
            jdbcElements.add(f);
            cmd.optimisticLockingField = fmd;
        }
    }

    /**
     * Fill in extra meta data for FetchGroup's.
     */
    private void processFetchGroups(ClassMetaData cmd) {
        FetchGroup[] groups = cmd.fetchGroups;
        int groupsLen = groups == null ? 0 : groups.length;
        for (int i = 0; i < groupsLen; i++) {
            FetchGroup g = groups[i];
            int totCols = 0;
            FetchGroupField[] fields = g.fields;
            if (fields == null) {
                continue;
            }
            int fieldsLen = fields.length;
            for (int j = 0; j < fieldsLen; j++) {
                FetchGroupField field = fields[j];
                FieldMetaData fmd = field.fmd;
                JdbcField jdbcField = (JdbcField)fmd.storeField;
                JdoExtension ext = field.extension;
                if (ext != null && ext.nested != null) {
                    JdoExtension[] nested = ext.nested;
                    int nestedLen = nested.length;
                    for (int k = 0; k < nestedLen; k++) {
                        JdoExtension e = nested[k];
                        switch (e.key) {
                            case JDBC_USE_JOIN:
                                try {
                                    field.jdbcUseJoin = e.getEnum(
                                            jdbcMDE.USE_JOIN_ENUM);
                                } catch (RuntimeException x) {
                                    cmd.addError(x, quiet);
                                }
                                break;
                            case JDBC_USE_KEY_JOIN:
                                if (fmd.category != MDStatics.CATEGORY_MAP) {
                                    RuntimeException x = BindingSupportImpl.getInstance().runtime("The jdbc-use-key-join option is only " +
                                            "valid for Map fields\n" +
                                            e.getContext());
                                    cmd.addError(x, quiet);
                                }
                                try {
                                    field.jdbcUseKeyJoin = e.getEnum(
                                            jdbcMDE.USE_JOIN_ENUM);
                                } catch (RuntimeException x) {
                                    cmd.addError(x, quiet);
                                }
                                break;
                            default:
                                if (e.isJdbc()) {
                                    MetaDataBuilder.throwUnexpectedExtension(e);
                                }
                        }
                    }
                }
                if (jdbcField == null) continue;
                if (field.jdbcUseJoin == 0) {
                    if (field.nextFetchGroup != null) {
                        field.jdbcUseJoin = jdbcField.useJoin;
                    } else {
                        field.jdbcUseJoin = JdbcField.USE_JOIN_NO;
                    }
                }
                if (field.jdbcUseKeyJoin == 0) {
                    if (field.nextKeyFetchGroup != null) {
                        field.jdbcUseKeyJoin = jdbcField.getUseKeyJoin();
                    } else {
                        field.jdbcUseKeyJoin = JdbcField.USE_JOIN_NO;
                    }
                }
                if (jdbcField.mainTableCols != null) {
                    totCols += jdbcField.mainTableCols.length;
                }
            }
            g.jdbcTotalCols = totCols;
        }
    }

    /**
     * Process all ref and polyref fields.
     */
    private void processRefAndPolyRefFields(ClassMetaData cmd, boolean quiet) {
        ArrayList elements = getClassElements(cmd);
        int nelements = elements.size();
        for (int i = 0; i < nelements; i++) {
            Object o = elements.get(i);
            if (o instanceof JdbcRefField) {
                processRefField((JdbcRefField)o, quiet);
            } else if (o instanceof JdbcPolyRefField) {
                JdbcPolyRefField f = (JdbcPolyRefField)o;
                f.processMetaData(f.fmd.jdoField, this);
            }
        }
    }

    private void doConstraints(ClassMetaData cmd) {
        for (int i = 0; i < cmd.fields.length; i++) {
            FieldMetaData fmd = cmd.fields[i];
            if (fmd.storeField instanceof JdbcRefField) {
                createConstraint((JdbcRefField) fmd.storeField);
            }
        }
    }

    /**
     * Process all ref and collection fields.
     */
    private void processCollectionFields(ClassMetaData cmd, boolean quiet) {
        if (cmd.horizontal) return;
        ArrayList elements = getClassElements(cmd);
        int nelements = elements.size();
        for (int i = 0; i < nelements; i++) {
            Object o = elements.get(i);
            if (o instanceof JdbcCollectionField) {
                processCollectionField((JdbcCollectionField)o, quiet);
            }
        }
    }

    /**
     * Complete a JdbcRefField except for column names.
     */
    private void processRefField(JdbcRefField f, boolean quiet) {
        FieldMetaData fmd = f.fmd;
        f.targetClass = fmd.typeMetaData;
        JdbcClass target = (JdbcClass)f.targetClass.storeClass;
        JdoField jdoField = fmd.jdoField;
        JdoElement context = jdoField == null
                ? (JdoElement)fmd.typeMetaData.jdoClass
                : (JdoElement)jdoField;
        processRefFieldImpl(target, f, fmd, context,
                jdoField == null ? null : jdoField.extensions, quiet);

    }

    public void processRefFieldImpl(JdbcClass target, JdbcRefField f,
            FieldMetaData fmd, JdoElement context, JdoExtension[] extensions,
            boolean quiet) {
        if (target != null) {
            f.useJoin = target.useJoin;
        } else {
            f.useJoin = JdbcRefField.USE_JOIN_NO;
        }
        if (f.useJoin == JdbcRefField.USE_JOIN_NO && fmd.isDefaultFetchGroupTrue()) {
            f.useJoin = JdbcRefField.USE_JOIN_OUTER;
        }

        JdbcRefMetaDataBuilder rdb = new JdbcRefMetaDataBuilder(
                fmd.classMetaData, this,
                f.targetClass, context, fmd.name,
                extensions, quiet);

        f.cols = rdb.getCols();
        if (rdb.getUseJoin() != 0) f.useJoin = rdb.getUseJoin();

        for (int i = 0; i < f.cols.length; i++) {
            f.cols[i].comment = fmd.getCommentName();
        }

        boolean nulls = fmd.nullValue != MDStatics.NULL_VALUE_EXCEPTION;
        int nc = f.cols.length;
        for (int i = 0; i < nc; i++) f.cols[i].nulls = nulls;

        // include this in the where clause for changed locking only if all
        // columns support equalityTest
        f.includeForChangedLocking = true;
        for (int i = 0; i < nc; i++) {
            if (!f.cols[i].equalityTest) {
                f.includeForChangedLocking = false;
                break;
            }
        }
        f.constraintName = rdb.getConstraintName();
        f.createConstraint = !rdb.isDoNotCreateConstraint();
    }

    private boolean isCircularRef(ClassMetaData cmd1, ClassMetaData cmd2) {
        return jdbcClassReferenceGraph.isCircularRef(cmd1, cmd2);
    }

    private void createConstraint(JdbcRefField f) {
        final JdbcClass jdbcClass = (JdbcClass)f.fmd.classMetaData.storeClass;
        boolean createConstraint = f.createConstraint && f.targetClass != null
                && (!(f.fmd.nullValue != MDStatics.NULL_VALUE_EXCEPTION)
                || sqlDriver.isNullForeignKeyOk());


        /**
         * If this class is involved in a cycle with the refering class then
         * don't create the constraint unless the user specifically specified a dependency.
         */
        if (createConstraint && isCircularRef(jdbcClass.cmd, f.fmd.typeMetaData)) {
            if (!getClassInfo(jdbcClass.cmd.top).getCreatedAfterClient().contains(
                    f.fmd.typeMetaData.top)) {
                createConstraint = false;
            }
        }

        if (createConstraint && jdbcClass.cmd.top == f.fmd.typeMetaData.top) {
            createConstraint = false;
        }

        // create constraint if required
        if (createConstraint) {
            JdbcConstraint c = new JdbcConstraint();
            c.name = f.constraintName;
            c.src = jdbcClass.table;
            c.srcCols = f.cols;
            c.dest = ((JdbcClass)f.targetClass.storeClass).table;
            f.constraint = c;
            if (c.name != null) {
                nameGenerator.addRefConstraintName(c.src.name,
                        c.name);
            }
        }
    }

    /**
     * Find the column in cols with the the supplied refField or null if none.
     */
    public JdbcColumn findColumn(List cols, JdbcSimpleField refField) {
        for (int i = cols.size() - 1; i >= 0; i--) {
            JdbcColumn col = (JdbcColumn)cols.get(i);
            if (col.refField == refField) return col;
        }
        return null;
    }

    /**
     * Complete a JdbcCollectionField.
     */
    private void processCollectionField(JdbcCollectionField f, boolean quiet) {
        FieldMetaData fmd = f.fmd;
        JdoField jdoField = fmd.jdoField;
        JdoElement context;
        if (jdoField == null) {
            if (fmd.typeMetaData == null) {
                context = fmd.classMetaData.jdoClass;
            } else {
                context = fmd.typeMetaData.jdoClass;
            }
        } else {
            context = jdoField;
        }
        try {
            f.processMetaData(context, this, quiet);
        } catch (RuntimeException e) {
            fmd.addError(e, quiet);
        }
    }

    /**
     * Creates key generators for all classes with keygen factories and
     * extracts keygen tables from them. Also set the autoinc flag on
     * the primary key of classes using a postInsert key generator.
     */
    private void createKeyGenerators(boolean quite) {
        HashSet keygenSet = new HashSet();
        HashSet keygenTables = new HashSet(17);
        ClassMetaData[] classes = jmd.classes;
        int clen = classes.length;
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.horizontal) continue;
            if (cmd.embeddedOnly) continue;
            if (cmd.pcSuperMetaData != null) continue;
            ClassInfo info = getClassInfo(cmd);
            JdbcKeyGeneratorFactory f = info.keyGenFactory;
            if (f == null) continue;
            JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
            JdbcKeyGenerator keygen;
            try {
                keygen = f.createJdbcKeyGenerator(cmd.qname, jdbcClass.table,
                        info.keyGenFactoryArgs);
            } catch (RuntimeException e) {
                cmd.addError(BindingSupportImpl.getInstance().runtime(e.getMessage() + "\n" +
                        cmd.jdoClass.getContext(), e), quite);
                continue;
            }

            // recursively set keygen on class and subclasses
            jdbcClass.setJdbcKeyGenerator(keygen);

            if (keygenSet.contains(keygen)) continue;
            keygenSet.add(keygen);
            keygen.addKeyGenTables(keygenTables, this);

            // set the autoinc flag on the primary key if postInsert keygen
            if (keygen.isPostInsertGenerator()) {
                jdbcClass.table.pk[0].autoinc = true;
                //for sybase this column's type must change to numeric
                //all subclass column's must also be updated
                sqlDriver.updateClassForPostInsertKeyGen(cmd, mappingResolver);
            }
        }
        JdbcMetaData jdbcMetaData = (JdbcMetaData)jmd.jdbcMetaData;
        jdbcMetaData.keyGenTables = new JdbcTable[keygenTables.size()];
        keygenTables.toArray(jdbcMetaData.keyGenTables);
    }

    /**
     * Name field columns without names and create the cols array on each
     * class table from the jdbcElements collected so far. It will
     * recursively process subclasses as required. Fake elements are
     * collected at the same time and added to ClassMetaData.fields.
     */
    private void finalizeFakesAndTableColumns(ClassMetaData cmd) {
        if (cmd.embeddedOnly) return;
        ArrayList elements = getClassElements(cmd);
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        if (elements != null) {
            collectSubclassElements(cmd, elements);

            JdbcNameGenerator nameGen = nameGenerator;
            String tableName = jdbcClass.tableName;

            // make sure the classIdCol has a name
            JdbcColumn classIdCol = jdbcClass.classIdCol;
            if (classIdCol != null) {
                if (classIdCol.name == null) {
                    classIdCol.name = nameGen.generateClassIdColumnName(
                            tableName);
                } else {
                    nameGen.addColumnName(tableName, classIdCol.name);
                }
            }

            // find all the JdbcColumn's in all elements and find fake
            // fields
            ArrayList cols = new ArrayList(elements.size());
            ArrayList fakes = new ArrayList();
            JdbcTable table = jdbcClass.table;
            for (int i = 0; i < elements.size(); i++) {
                Object o = elements.get(i);
                if (o instanceof JdbcField) {
                    JdbcField f = (JdbcField)o;
                    f.setMainTable(table);
                    if (!f.fmd.primaryKey) f.nameColumns(tableName, nameGen);
                    int pos = cols.size();
                    f.addMainTableCols(cols);
                    // make sure cols from subclasses allow nulls
                    if (f.fmd.classMetaData.pcSuperMetaData!= null) {
                        int pos2 = cols.size();
                        for (int j = pos; j < pos2; j++) {
                            JdbcColumn sc = (JdbcColumn)cols.get(j);
                            sc.nulls = true;
                        }
                    }
                    if (f.fake) {
                        fakes.add(f);
                    }
                } else {    // must be a column
                    JdbcColumn c = (JdbcColumn)o;
                    c.setTable(table);
                    cols.add(c);
                }
            }

            //this done after all the naming has taken place.
            doNullIndicatorColumn(cmd, elements);

            // add the fakes (if any) to cmd.fields
            int numFakes = fakes.size();
            if (numFakes > 0 && cmd.fields != null) {
                int n = cmd.fields.length;
                FieldMetaData[] a = new FieldMetaData[n + numFakes];
                System.arraycopy(cmd.fields, 0, a, 0, n);
                for (int i = 0; i < numFakes; i++, n++) {
                    JdbcField jdbcField = (JdbcField)fakes.get(i);
                    a[n] = jdbcField.fmd;
                }
                cmd.fields = a;
            }

            // do a final sanity check on each column and set the table reference
            int nc = cols.size();
            JdbcColumn[] a = new JdbcColumn[nc];
            cols.toArray(a);
            for (int i = 0; i < nc; i++) {
                JdbcColumn c = a[i];
                if (c.name == null) {
                    throw BindingSupportImpl.getInstance().internal("Column has no name: " + c + "\n" +
                            cmd.jdoClass.getContext());
                }
                if (c.jdbcType == Types.NULL) {
                    throw BindingSupportImpl.getInstance().internal("Column has NULL jdbcType: " + c + "\n" +
                            cmd.jdoClass.getContext());
                }
                if (c.table == null) {
                    throw BindingSupportImpl.getInstance().internal("Column null table: " + c + "\n" +
                            cmd.jdoClass.getContext());
                }
                if (!sqlDriver.isBatchingSupportedForJdbcType(c.jdbcType)) {
                    jdbcClass.noBatching = true;
                }
            }

            // set the cols array
            jdbcClass.table.cols = a;
        }

        // recursively process all of our subclasses
        ClassMetaData[] subclasses = cmd.pcSubclasses;
        if (subclasses == null) return;
        for (int i = 0; i < subclasses.length; i++) {
            ((JdbcClass)subclasses[i].storeClass).setClassIdCol(jdbcClass.classIdCol);
            finalizeFakesAndTableColumns(subclasses[i]);
        }
    }

    /**
     * Go through all the embedded reference fields on the supplied cmd and find
     * nullindicator columns.
     */
    private void doNullIndicatorColumn(ClassMetaData cmd, ArrayList elements) {
        //************** ignored for now **********************.
        if (true) return;
        //************** ignored for now **********************.
        FieldMetaData[] fmds = cmd.fields;
        for (int i = 0; i < fmds.length; i++) {
            FieldMetaData fmd = fmds[i];
            if (fmd.isEmbeddedRef()) {
                if (fmd.jdoField != null && fmd.jdoField.extensions != null) {
                    JdoExtension ext = JdoExtension.find(JdoExtensionKeys.NULL_INDICATOR, fmd.jdoField.extensions);
                    if (ext != null) {
                        boolean extFound = false;
                        //expect a jdbc-column-name
                        if (ext.nested != null) {
                            JdoExtension colNameExt = ext.nested[0];
                            if (colNameExt.key == JdoExtensionKeys.JDBC_COLUMN_NAME) {
                                extFound = true;
                                //must check to see if we have any column in the table with that name
                                boolean createColumn = true;
                                for (int l = 0; l < elements.size(); l++) {
                                    Object o1 = elements.get(l);
                                    if (o1 instanceof JdbcSimpleField) {
                                        JdbcSimpleField sf = (JdbcSimpleField) o1;
                                        if (sf.col.name == ext.value) {
                                            createColumn = false;
                                        }
                                    }
                                }
                                if (createColumn) {
                                    JdbcColumn col = createColumn(ext.nested,
                                            CLASS_ID_FIELDNAME, /*CHFC*/Integer.TYPE/*RIGHTPAR*/);
                                    JdbcSimpleField f = new JdbcSimpleField();
                                    f.fake = true;
                                    f.col = col;
                                    FieldMetaData nullFmd = f.fmd = new FieldMetaData();
                                    nullFmd.fake = true;
                                    nullFmd.category = MDStatics.CATEGORY_SIMPLE;
                                    nullFmd.classMetaData = cmd;
                                    nullFmd.defaultFetchGroup = true;
                                    nullFmd.storeField = f;
                                    nullFmd.name = fmd.name + "_null_indicator";
                                    nullFmd.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
                                    nullFmd.setType(col.javaType);
                                    fmd.setNullIndicatorFmd(nullFmd);
                                    elements.add(f);
                                }
                            }
                        }
                        if (!extFound) {
                            throw BindingSupportImpl.getInstance().unsupported(
                                    "Found a '"
                                    + JdoExtension.toKeyString(JdoExtensionKeys.NULL_INDICATOR)
                                    + "' extension with no '"
                                    + JdoExtension.toKeyString(JdoExtensionKeys.JDBC_COLUMN_NAME)
                                    + "' nested extension");
                        }
                    }
                }
            }
        }
    }

    /**
     * Name all constraints without names and create the constraint arrays
     * on class tables.
     */
    private void finalizeConstraints(ClassMetaData cmd) {
        ClassInfo info = getClassInfo(cmd);
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        JdbcField[] fields = jdbcClass.fields;
        int len = fields.length;
        ArrayList cons = new ArrayList();
        JdbcConstraint pkFkConstraint = info.pkFkConstraint;
        if (pkFkConstraint != null) {
            if (pkFkConstraint.name == null) {
                pkFkConstraint.generateName(nameGenerator);
            }
            cons.add(pkFkConstraint);
        }
        JdbcTable table = jdbcClass.table;
        for (int i = 0; i < len; i++) {
            JdbcField field = fields[i];
            if (field != null && field.mainTable == jdbcClass.table) {
                field.addConstraints(cons);
            }
        }
        int n = cons.size();
        if (n > 0) {
            table.addConstraints(cons);
        }
    }

    /**
     * Recursively add all elements from all subclasses of cmd that are
     * stored in the same table to elements. Any class so added has its
     * elements removed from the map indicating that it has been processed.
     */
    private void collectSubclassElements(ClassMetaData cmd, ArrayList elements) {
        ClassMetaData[] subclasses = cmd.pcSubclasses;
        if (subclasses == null) return;
        for (int i = 0; i < subclasses.length; i++) {
            ClassMetaData sc = subclasses[i];
            if (((JdbcClass)sc.storeClass).table == ((JdbcClass)cmd.storeClass).table) {
                ClassInfo scInfo = getClassInfo(sc);
                elements.addAll(scInfo.elements);
                scInfo.elements = null;
                collectSubclassElements(sc, elements);
            }
        }
    }

    /**
     * Find the primary key (datastore or application) for cmd and make sure
     * all the columns have names. If the identity-type is datastore and
     * there is no primary key column then create one. This will recursively
     * process any PC subclasses.
     */
    private void processPrimaryKey(ClassMetaData cmd, boolean quiet) {
        if (cmd.embeddedOnly) return;
        if (cmd.horizontal) return;
        if (cmd.identityType == MDStatics.IDENTITY_TYPE_APPLICATION) {
            processPrimaryKeyAppIdentity(cmd, quiet);
        } else {
            processPrimaryKeyDatastoreIdentity(cmd);
        }

        // create subclass table fk constraint to base table if needed
        ClassInfo info = getClassInfo(cmd);
        ClassMetaData pccmd = cmd.pcSuperMetaData;
        if (pccmd != null
                && ((JdbcClass)cmd.storeClass).table != ((JdbcClass)pccmd.storeClass).table) {
            JdbcConstraint c = new JdbcConstraint();
            c.name = info.pkFkConstraintName;
            c.src = ((JdbcClass)cmd.storeClass).table;
            c.srcCols = ((JdbcClass)cmd.storeClass).table.pk;
            c.dest = ((JdbcClass)pccmd.storeClass).table;
            if (c.name != null) {
                nameGenerator.addRefConstraintName(
                        c.src.name, c.name);
            }
            info.pkFkConstraint = c;
        }

        if (cmd.pcSubclasses == null) return;
        for (int j = 0; j < cmd.pcSubclasses.length; j++) {
            processPrimaryKey(cmd.pcSubclasses[j], quiet);
        }
    }

    /**
     * Find the primary key for datastore identity class for cmd and make sure
     * all the columns have names. If there is no primary key column then
     * create one.
     */
    private void processPrimaryKeyDatastoreIdentity(ClassMetaData cmd) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        JdbcTable table = jdbcClass.table;
        JdbcColumn pkcol = null;

        ArrayList jdbcElements = getClassElements(cmd);
        int elen = jdbcElements.size();
        for (int i = 0; i < elen; i++) {
            Object o = jdbcElements.get(i);
            if (!(o instanceof JdbcColumn)) continue;
            JdbcColumn c = (JdbcColumn)o;
            if (c.pk) {
                if (pkcol != null) {
                    throw BindingSupportImpl.getInstance().runtime("Class " + cmd.qname +
                            " has multiple jdbc-primary-key extensions\n" +
                            cmd.jdoClass.getContext());
                }
                pkcol = c;
            }
        }
        if (cmd.pcSuperMetaData != null) {
            if (pkcol != null) {
                throw BindingSupportImpl.getInstance().runtime("Class " + cmd.qname + " has a " +
                        "persistence-capable-superclass so use the inheritance " +
                        "extension to specify its primary-key\n" +
                        cmd.jdoClass.getContext());
            }
            // if subclass is stored in its own table then use the inheritance
            // extension to build a reference to the superclass
            if (table != ((JdbcClass)cmd.pcSuperMetaData.storeClass).table) {
                createVerticalInheritancePK(cmd);
            }
        } else {
            if (pkcol == null) {
                pkcol = createColumn(null,
                        DATASTORE_PK_FIELDNAME, /*CHFC*/Integer.TYPE/*RIGHTPAR*/);
                jdbcElements.add(0, pkcol);
            }
            // make sure the columns has a name
            if (pkcol.name != null) {
                try {
                    nameGenerator.addColumnName(table.name, pkcol.name);
                } catch (IllegalArgumentException e) {
                    throw BindingSupportImpl.getInstance().runtime("Invalid jdbc-column-name for datastore identity primary key: " +
                            e.getMessage() + "\n" + cmd.jdoClass.getContext());
                }
            } else {
                pkcol.name = nameGenerator.generateDatastorePKName(
                        table.name);
            }
            table.setPk(new JdbcColumn[]{pkcol});
        }
    }

    /**
     * Fill in the primary key for a subclass mapped using vertical
     * inheritance using the inheritance extension to build a reference to the
     * superclass.
     */
    private void createVerticalInheritancePK(ClassMetaData cmd) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        if (cmd.embeddedOnly) return;
        JdbcTable table = jdbcClass.table;
        JdbcTable superTable = ((JdbcClass)cmd.pcSuperMetaData.storeClass).table;
        JdoExtension inheritance = getClassInfo(cmd).inheritance;
        JdbcRefMetaDataBuilder rdb = new JdbcRefMetaDataBuilder(cmd, this,
                cmd.pcSuperMetaData, inheritance, null,
                inheritance == null ? null : inheritance.nested, quiet);
        JdbcColumn[] a = rdb.getCols();
        for (int i = 0; i < a.length; i++) {
            if (a[i].name == null) a[i].name = superTable.pk[i].name;
            a[i].pk = true;
        }
        table.setPk(a);
        ArrayList jdbcElements = getClassElements(cmd);
        for (int i = 0; i < a.length; i++) {
            try {
                a[i].addColumnNames(table.name, nameGenerator);
            } catch (IllegalArgumentException e) {
                throw BindingSupportImpl.getInstance().runtime("Invalid jdbc-column-name: " +
                        e.getMessage() + "\n" + inheritance.getContext());
            }
            jdbcElements.add(i, a[i]);
        }
    }

    /**
     * Find the primary key for application identity class cmd and make sure
     * all the columns have names.
     */
    private void processPrimaryKeyAppIdentity(ClassMetaData cmd, boolean quiet) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        JdbcTable table = jdbcClass.table;
        JdbcNameGenerator ng = nameGenerator;
        FieldMetaData[] fields = cmd.pkFields;
        JdbcColumn[] pkcols = null;
        int pkFieldCount = fields == null ? 0 : fields.length;
        if (fields != null) {
            pkcols = new JdbcColumn[pkFieldCount];
            for (int i = 0; i < pkFieldCount; i++) {
                try {
                    JdbcSimpleField jdbcSimpleField = (JdbcSimpleField)fields[i].storeField;
                    pkcols[i] = jdbcSimpleField.col;
                    pkcols[i].refField = jdbcSimpleField;
                } catch (ClassCastException e) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime("Only simple fields may be mapped as Primary Key's.\n" +
                            cmd.jdoClass.getContext());
                    cmd.addError(x, quiet);
                }
            }
        }
        ClassMetaData pccmd = cmd.pcSuperMetaData;
        if (pccmd != null && ((JdbcClass)pccmd.storeClass).inheritance != JdbcClass.INHERITANCE_HORIZONTAL) {
            if (pkcols != null) {
                throw BindingSupportImpl.getInstance().runtime("Class " + cmd.qname + " has a " +
                        "persistence-capable-superclass so use the inheritance " +
                        "extension to specify its primary-key\n" +
                        cmd.jdoClass.getContext());
            }
            // if subclass is stored in its own table then use the inheritance
            // extension to build a reference to the superclass
            if (table != ((JdbcClass)pccmd.storeClass).table) {
                createVerticalInheritancePK(cmd);
            }
        } else {
            if (pkcols == null) {
                RuntimeException e = BindingSupportImpl.getInstance().runtime("Class " +
                        cmd.qname + " has application identity " +
                        "but no primary-key fields\n" +
                        cmd.jdoClass.getContext());
                cmd.addError(e, quiet);
            } else {
                for (int i = 0; i < pkFieldCount; i++) {
                    String cn = pkcols[i].name;
                    if (cn == null) {
                        pkcols[i].name = ng.generateFieldColumnName(table.name,
                                fields[i].name, true);
                    } else {
                        try {
                            ng.addColumnName(table.name, cn);
                        } catch (IllegalArgumentException x) {
                            throw BindingSupportImpl.getInstance().runtime("Invalid jdbc-column-name: " + x.getMessage() + "\n" +
                                    cmd.jdoClass.getContext());
                        }
                    }
                }
                table.setPk(pkcols);
            }
        }
    }

    /**
     * Make sure the persistent capable class cmd has a table and
     * recursively process all of its subclasses. NOP if cmd is not
     * a base class.
     */
    private void processBaseClassTable(ClassMetaData cmd) {
        ClassMetaData pccmd = cmd.pcSuperMetaData;
        if (pccmd != null) return;
        createClassTable(cmd);
        JdbcClass jdbcClass = ((JdbcClass)cmd.storeClass);
        if (cmd.horizontal || cmd.embeddedOnly) {
            jdbcClass.doNotCreateTable = true;
        }
        if (cmd.pcSubclasses == null) return;
        for (int j = 0; j < cmd.pcSubclasses.length; j++) {
            processSubclassTable(cmd.pcSubclasses[j]);
        }
    }

    private void createClassTable(ClassMetaData cmd) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        JdbcTable table = new JdbcTable();
        table.sqlDriver = sqlDriver;
        table.name = jdbcClass.tableName;
        table.comment = cmd.qname;
        if (table.name == null) {
            table.name = nameGenerator.generateClassTableName(cmd.qname);
        } else {
            addTableName(table, cmd);
        }
        jdbcClass.setTable(table);
        fillTablePkConstraintName(cmd, table);
    }

    /**
     * Make sure a persistent capable subclass cmd has a table and
     * recursively process all of its subclasses.
     */
    private void processSubclassTable(ClassMetaData cmd) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        if (cmd.embeddedOnly) return;
        ClassMetaData pccmd = cmd.pcSuperMetaData;
        switch (jdbcClass.inheritance) {
            case JdbcClass.INHERITANCE_FLAT:
                jdbcClass.setTable(((JdbcClass)pccmd.storeClass).table);
                break;
            case JdbcClass.INHERITANCE_VERTICAL:
                createClassTable(cmd);
                break;
            default:
                throw BindingSupportImpl.getInstance().internal("Unknown inheritance strategy: " + jdbcClass.inheritance +
                        " for " + cmd.qname);
        }

        if (cmd.pcSubclasses == null) return;
        for (int j = 0; j < cmd.pcSubclasses.length; j++) {
            processSubclassTable(cmd.pcSubclasses[j]);
        }
    }

    private void addTableName(JdbcTable table,
            ClassMetaData cmd) {
        try {
            nameGenerator.addTableName(table.name);
        } catch (IllegalArgumentException x) {
            throw BindingSupportImpl.getInstance().runtime("Invalid jdbc-table-name: " + x.getMessage() + "\n" +
                    cmd.jdoClass.getContext());
        }
    }

    private void fillTablePkConstraintName(ClassMetaData cmd, JdbcTable table) {
        table.pkConstraintName = getClassInfo(cmd).pkConstraintName;
        if (table.pkConstraintName == null) {
            table.pkConstraintName = nameGenerator.generatePkConstraintName(
                    table.name);
        } else {
            try {
                nameGenerator.addPkConstraintName(table.name, table.pkConstraintName);
            } catch (IllegalArgumentException e) {
                throw BindingSupportImpl.getInstance().runtime("Invalid jdbc-pk-constraint-name: " +
                        e.getMessage() + "\n" + cmd.jdoClass.getContext(), e);
            }
        }
    }

    /**
     * Create meta data for cmd and all of its subclasses and figure out
     * everything that does not require access to JdbcClass objects other
     * than its superclasses as they may not exist yet.
     */
    private void createJdbcClass(ClassMetaData cmd, boolean quiet) {
        ClassInfo info = new ClassInfo();
        info.cmd = cmd;
        classInfoMap.put(cmd, info);
        JdbcClass jdbcClass = new JdbcClass(sqlDriver);
        cmd.storeClass = jdbcClass;
        jdbcClass.cmd = cmd;
        JdoClass jdoClass = cmd.jdoClass;

        // fill in defaults that may be overwritten by extensions
        jdbcClass.optimisticLocking = jdbcConfig.jdbcOptimisticLocking;
        info.optimisticLockingExt = null;

        jdbcClass.useJoin = JdbcRefField.USE_JOIN_OUTER;
        jdbcClass.doNotCreateTable = jdbcConfig.jdbcDoNotCreateTable;

        // default the descriminator value
        switch (jdbcConfig.defaultClassId) {
            case JdbcConfig.DEFAULT_CLASS_ID_FULLNAME:
                jdbcClass.jdbcClassId = cmd.qname;
                break;
            case JdbcConfig.DEFAULT_CLASS_ID_NAME:
                jdbcClass.jdbcClassId = cmd.jdoClass.name;
                break;
            default:
                jdbcClass.jdbcClassId = cmd.classIdString;
        }

        if (cmd.pcSuperMetaData != null) {
            if (getClassInfo(cmd.top).noClassIdCol) {
                jdbcClass.inheritance = JdbcClass.INHERITANCE_VERTICAL;
            } else {
                jdbcClass.inheritance = jdbcConfig.inheritance;
            }
        } else {
            jdbcClass.inheritance = jdbcConfig.inheritance;
            info.noClassIdCol = jdbcConfig.defaultClassId == JdbcConfig.DEFAULT_CLASS_ID_NO;
        }

        if (cmd.identityType == MDStatics.IDENTITY_TYPE_DATASTORE) {
            info.keyGenFactory = keyGenRegistry.getFactory(
                    jdbcConfig.jdbcKeyGenerator);
            info.keyGenFactoryArgs = info.keyGenFactory.createArgsBean();
            BeanUtils.setProperties(info.keyGenFactoryArgs,
                    jdbcConfig.jdbcKeyGeneratorProps);
        }

        // fill in whatever we can from extensions, fields etc
        JdoElement[] elements = jdoClass.elements;
        int n = elements.length;
        ArrayList jdbcElements = info.elements = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            JdoElement o = elements[i];
            if (o instanceof JdoExtension) {
                try {
                    processClassExtensionPass1(cmd, (JdoExtension)o,
                            jdbcElements, quiet);
                } catch (RuntimeException e) {
                    cmd.addError(e, quiet);
                }
            } else {    // must be a JdoField
                JdoField jdoField = (JdoField)o;
                FieldMetaData fmd = cmd.getFieldMetaData(jdoField.name);
                if (fmd == null) continue; // must be PERSISTENT_NONE

                if (fmd.isEmbeddedRef()) {
                    continue;
                }

                JdbcField f = null;
                f = createJdbcField(fmd, quiet);
                if (f != null) {
                    f.fmd = fmd;
                    fmd.storeField = f;
                    fmd.primaryField = true;
                    jdbcElements.add(f);
                }
            }
        }

        // create JdbcField's for all persistent fields not yet found
        FieldMetaData[] fields = cmd.fields;
        n = fields.length;
        for (int i = 0; i < n; i++) {
            FieldMetaData fmd = fields[i];
            if (fmd.storeField != null || fmd.isEmbeddedRef()) continue;
            JdbcField f = createJdbcField(fmd, quiet);
            if (f != null) {
                f.fmd = fmd;
                fmd.storeField = f;
                fmd.primaryField = true;
                jdbcElements.add(f);
            }
        }

        cmd.changedOptimisticLocking =
            jdbcClass.optimisticLocking == JdbcClass.OPTIMISTIC_LOCKING_CHANGED;

        // process subclasses
        if (cmd.pcSubclasses != null) {
            for (int i = cmd.pcSubclasses.length - 1; i >= 0; i--) {
                createJdbcClass(cmd.pcSubclasses[i], quiet);
            }
        }
    }

    /**
     * Create the classIdCol and initialise the jdbcClassId to the correct type.
     * This is called on the least derived class in the hierarchy and then
     * recursively called on subs.
     */
    private void processClassIdCol(ClassMetaData cmd, boolean quiet,
            HashMap classIdMap) {
        ClassInfo info = getClassInfo(cmd);
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;

        if (cmd.pcSuperMetaData == null) {
            if (!info.noClassIdCol && cmd.pcSubclasses != null) {
                // topmost class so we need to create the column
                boolean intClassId = jdbcClass.isIntJdbcClassIdHierarchy();
                Class javaType = intClassId ? (Class)/*CHFC*/Integer.TYPE/*RIGHTPAR*/ : (Class)/*CHFC*/String.class/*RIGHTPAR*/;

                if (info.classIdExt != null && info.classIdExt.nested != null) {
                    // if there is a jdbc-class-id extension with nested extensions
                    // then use these to create the column
                    JdbcColumn col = jdbcClass.classIdCol = createColumn(info.classIdExt.nested, CLASS_ID_FIELDNAME,
                            javaType);
                    if (col.pk) {
                        throw BindingSupportImpl.getInstance().runtime("The jdbc-primary-key option is " +
                                "not allowed for a jdbc-class-id column\n" +
                                info.classIdExt.getContext());
                    }
                    info.elements.add(col);
                } else {
                    // no extensions so just create a suitable column
                    jdbcClass.classIdCol = createColumn(null,
                            CLASS_ID_FIELDNAME, javaType);
                    info.elements.add(jdbcClass.classIdCol);
                }

                // convert hierarchy jdbcClassId to Integer if needed
                if (intClassId) jdbcClass.convertJdbcClassIdToInteger();
            } else {
                jdbcClass.jdbcClassId = null;
            }
        } else { // not the topmost class
            if (getClassInfo(cmd.top).noClassIdCol) {
                if (jdbcClass.inheritance != JdbcClass.INHERITANCE_VERTICAL) {
                    // Flat inheritance and no descriminator is allowed if
                    // there is no fanout in the hierarchy (i.e. each class
                    // has 0 or 1 subclasses). Only instances of the leaf
                    // class may be persisted and only instances of the leaf
                    // class will be returned from the database.
                    if (cmd.pcSuperMetaData.pcSubclasses.length > 1) {
                        throw BindingSupportImpl.getInstance().invalidOperation(
                                "Class " + cmd.qname +
                                " must use vertical inheritance as\n" +
                                "it has siblings and the base class " +
                                "does not have a descriminator (jdo_class) column\n" +
                                cmd.jdoClass.getContext());
                    }
                    // only leaf class instances allowed
                    cmd.pcSuperMetaData.instancesNotAllowed = true;
                    // read superclass(es) as us instead
                    for (ClassMetaData i = cmd.pcSuperMetaData; i != null;
                            i = i.pcSuperMetaData) {
                        ((JdbcClass)i.storeClass).readAsClass = cmd;
                    }
                }
                jdbcClass.jdbcClassId = null;
            }

            // subclass so fill in the classIdCol from the topmost class
            jdbcClass.classIdCol = ((JdbcClass)cmd.top.storeClass).classIdCol;
        }

        // check for duplicate descriminator values in the hierarchy
        if (jdbcClass.jdbcClassId != null) {
            ClassMetaData other =
                    (ClassMetaData)classIdMap.get(jdbcClass.jdbcClassId);
            if (other != null) {
                throw BindingSupportImpl.getInstance().invalidOperation("Class " + cmd.qname +
                        " has same jdbc-class-id as " + other.qname + ": '" +
                        jdbcClass.jdbcClassId + "'\n" +
                        cmd.jdoClass.getContext());
            }
            classIdMap.put(jdbcClass.jdbcClassId, cmd);
        }

        // process subclasses
        if (cmd.pcSubclasses != null) {
            ClassMetaData[] subs = cmd.pcSubclasses;
            for (int i = 0; i < subs.length; i++) {
                processClassIdCol(subs[i], quiet, classIdMap);
            }
        }
    }

    /**
     * Create a new JdbcField from a JdoField and its extensions. Returns
     * null if the JdoField is transactional i.e. we dont give a flying
     * banana as we do not store it!
     */
    private JdbcField createJdbcField(FieldMetaData fmd,
            boolean quiet) {
        try {
            switch (fmd.category) {
                case MDStatics.CATEGORY_TRANSACTIONAL:
                    return null;
                case MDStatics.CATEGORY_SIMPLE:
                    return createJdbcSimpleField(fmd);
                case MDStatics.CATEGORY_REF:
                    return new JdbcRefField();
                case MDStatics.CATEGORY_POLYREF:
                    return new JdbcPolyRefField();
                case MDStatics.CATEGORY_COLLECTION:
                    return createJdbcCollectionField(fmd);
                case MDStatics.CATEGORY_ARRAY:
                    return createFieldForArray(fmd);
                case MDStatics.CATEGORY_MAP:
                    return new JdbcMapField();
                case MDStatics.CATEGORY_EXTERNALIZED:
                    return createJdbcSerializedField(fmd);
            }
            throw BindingSupportImpl.getInstance().internal("Field " + fmd.name + " of " +
                    fmd.classMetaData.qname +
                    " has bad category: " +
                    MDStaticUtils.toCategoryString(fmd.category));
        } catch (RuntimeException e) {
            fmd.addError(e, quiet);
        }
        return null;
    }

    private JdbcCollectionField createJdbcCollectionField(FieldMetaData fmd) {
        return createJdbcField(fmd,
                fmd.jdoCollection == null ? null : fmd.jdoCollection.extensions);
    }

    private JdbcCollectionField createJdbcField(FieldMetaData fmd,
            JdoExtension[] exts) {
        if (exts != null) {
            // look for an extension indicating that this collection is stored
            // using a foreign key in the value class instead of a link table
            // or is part of a many-to-many relationship
            JdoExtension[] a = exts;
            int n = a == null ? 0 : a.length;
            boolean gotLink = false;
            JdoExtension ie = null;
            for (int i = 0; i < n; i++) {
                JdoExtension e = a[i];
                switch (e.key) {
                    case INVERSE:
                    case JDBC_LINK_FOREIGN_KEY:
                        if (ie != null || gotLink) {
                            throw BindingSupportImpl.getInstance().runtime("The " + (ie != null ? "inverse" : "jdbc-link-table") +
                                    " extension has already been specified\n" +
                                    e.getContext());
                        }
                        ie = e;
                        break;
                    case JDBC_LINK_TABLE:
                        if (ie != null || gotLink) {
                            throw BindingSupportImpl.getInstance().runtime("The " + (ie != null ? "inverse" : "jdbc-link-table") +
                                    " extension has already been specified\n" +
                                    e.getContext());
                        }
                        gotLink = true;
                        break;
                }
            }
            if (ie != null) {
                // see what type of field the inverse is to see if this is
                // is a many-to-many or one-to-many
                ClassMetaData ecmd = fmd.elementTypeMetaData;
                if (ecmd == null) {
                    throw BindingSupportImpl.getInstance().runtime("The inverse extension may only be used for " +
                            "collections of PC instances\n" + ie.getContext());
                }
                String fname = ie.getString();
                FieldMetaData f = ecmd.getFieldMetaData(fname);
                if (f == null) {
                    if (fname != null && (fname.equals("") || fname.equals(
                            FieldMetaData.NO_FIELD_TEXT))) {
                        return new JdbcFKCollectionField();
                    } else {
                        throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' not found on " +
                                fmd.elementType + "\n" + ie.getContext());
                    }
                }
                switch (f.category) {
                    case MDStatics.CATEGORY_REF:
                        return new JdbcFKCollectionField();
                    case MDStatics.CATEGORY_ARRAY:
                    case MDStatics.CATEGORY_COLLECTION:
                        return new JdbcLinkCollectionField();
                }
                throw BindingSupportImpl.getInstance().runtime("Field '" + fname + "' is not a reference, collection or array\n" +
                        ie.getContext());
            }
        }
        return new JdbcLinkCollectionField();
    }

    private JdbcSimpleField createJdbcSimpleField(FieldMetaData fmd) {
        JdbcSimpleField f = new JdbcSimpleField();
        JdoField jdoField = fmd.jdoField;
        JdoExtension[] a = jdoField == null ? null : jdoField.extensions;
        int n = a == null ? 0 : a.length;
        for (int i = 0; i < n; i++) {
            JdoExtension e = a[i];
            switch (e.key) {
                case JDBC_COLUMN:
                    // handled when column is created
                    break;
                default:
                    if (e.isJdbc()) MetaDataBuilder.throwUnexpectedExtension(e);
            }
        }
        f.col = createColumn(a, fmd.name, fmd.type);
        f.col.comment = fmd.getCommentName();
        if ((f.col.pk = fmd.primaryKey)
                || fmd.nullValue == MDStatics.NULL_VALUE_EXCEPTION) {
            f.col.nulls = false;
        } else if (fmd.nullValue == MDStatics.NULL_VALUE_NONE) {
            f.col.nulls = true;
        }
        if (fmd.embeddedFakeField) {
            f.col.nulls = true;
        }
        f.includeForChangedLocking = f.col.equalityTest;
        return f;
    }

    private JdbcSimpleField createJdbcSerializedField(FieldMetaData fmd) {
        JdbcSimpleField f = new JdbcSimpleField();
        JdoField jdoField = fmd.jdoField;
        JdoExtension[] a = jdoField == null ? null : jdoField.extensions;
        int n = a == null ? 0 : a.length;
        for (int i = 0; i < n; i++) {
            JdoExtension e = a[i];
            switch (e.key) {
                case JDBC_COLUMN:
                    // handled when column is created
                    break;
                default:
                    if (e.isJdbc()) MetaDataBuilder.throwUnexpectedExtension(e);
            }
        }
        f.col = createColumn(a, fmd.name,
                fmd.externalizer.getExternalType());
        f.col.comment = fmd.getCommentName();
        if (fmd.nullValue == MDStatics.NULL_VALUE_EXCEPTION) {
            f.col.nulls = false;
        } else if (fmd.nullValue == MDStatics.NULL_VALUE_NONE) {
            f.col.nulls = true;
        }
        f.includeForChangedLocking = f.col.equalityTest;
        return f;
    }

    /**
     * Create a JdbcField for an array[]. This will create a JdbcSimpleField
     * if the array is embedded or a JdbcLinkCollectionField if not.
     */
    private JdbcField createFieldForArray(FieldMetaData fmd) {
        if (fmd.embedded) {
            return createJdbcSimpleField(fmd);
        } else {
            return createJdbcField(fmd,
                    fmd.jdoArray == null ? null : fmd.jdoArray.extensions);
        }
    }

    /**
     * Process a pass 1 extension for a class. This ignores jdbc-datastore
     * extensions as these should have already been processed. Some
     * extensions are skipped to be processed later.
     *
     * @param jdbcElements These are the JdbcColumn's and JdbcField's that
     *                     have been created so far for the class
     */
    private void processClassExtensionPass1(ClassMetaData cmd, JdoExtension e,
            ArrayList jdbcElements, boolean quiet) {
        JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
        ClassInfo info;
        switch (e.key) {
            case DATASTORE:
                // do nothing as this should have already been processed
                break;
            case JDBC_INHERITANCE:
//                if (cmd.pcSuperMetaData == null) {
//                    RuntimeException x = BindingSupportImpl.getInstance().runtime("Class " +
//                            cmd.qname + " does not have a " +
//                            "persistence-capable-superclass\n" +
//                            e.getContext());
//                    cmd.addError(x, quiet);
//                }
                try {
                    if (e.value != null) {
                        jdbcClass.inheritance = e.getEnum(jdbcMDE.INHERITANCE_ENUM);
                    }
                    getClassInfo(cmd).inheritance = e;
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_TABLE_NAME:
                if (jdbcClass.tableName != null) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime("Class " +
                            cmd.qname + " already has a jdbc-table-name: " +
                            jdbcClass.tableName + "\n" +
                            e.getContext());
                    cmd.addError(x, quiet);
                }
                jdbcClass.tableName = e.getString();
                break;
            case JDBC_KEY_GENERATOR:
                if (cmd.pcSuperMetaData != null) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime("The jdbc-key-generator extension is only allowed for " +
                            "the least derived class in a hierarchy\n" +
                            e.getContext());
                    cmd.addError(x, quiet);
                }
                info = getClassInfo(cmd);
                if (e.value != null) {
                    JdbcKeyGeneratorFactory f = findKeyGenFactory(e);
                    if (info.keyGenFactory != f) {
                        info.keyGenFactory = f;
                        info.keyGenFactoryArgs = f.createArgsBean();
                    }
                } else if (info.keyGenFactory == null) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime("The jdbc-key-generator extension must specify a factory " +
                            "for application identity classes\n" +
                            e.getContext());
                    cmd.addError(x, quiet);
                }
                BeanUtils.setProperties(info.keyGenFactoryArgs,
                        e.getPropertyMap());
                break;
            case JDBC_INDEX:
                try {
                    info = getClassInfo(cmd);
                    info.indexExts.add(e);
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_OPTIMISTIC_LOCKING:
                try {
                    processClassOptimisticLocking(e, jdbcClass, cmd, quiet);
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_CLASS_ID:
                try {
                    processJdbcClassIdExtension(cmd, e, jdbcClass);
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_COLUMN:
                try {
                    processClassColumnExtension(e, cmd,
                            jdbcElements);
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_PRIMARY_KEY:
                try {
                    processClassPrimaryKeyExtension(e, cmd,
                            jdbcElements);
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_PK_FK_CONSTRAINT_NAME:
                info = getClassInfo(cmd);
                if (info.pkFkConstraintName != null) {
                    RuntimeException x = BindingSupportImpl.getInstance().runtime("The jdbc-pk-fk-constraint extension may only appear once\n" +
                            e.getContext());
                    cmd.addError(x, quiet);
                }
                info.pkFkConstraintName = e.getString();
                break;
            case JDBC_USE_JOIN:
                try {
                    jdbcClass.useJoin = e.getEnum(jdbcMDE.USE_JOIN_ENUM);
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            case JDBC_DO_NOT_CREATE_TABLE:
                try {
                    jdbcClass.doNotCreateTable = e.getBoolean();
                } catch (RuntimeException x) {
                    cmd.addError(x, quiet);
                }
                break;
            default:
                if (e.isJdbc()) MetaDataBuilder.throwUnexpectedExtension(e);
        }
    }

    private void processJdbcClassIdExtension(ClassMetaData cmd, JdoExtension e,
            JdbcClass jdbcClass) {
        ClassInfo info;
        info = getClassInfo(cmd);
        if (info.classIdExt != null) {
            throw BindingSupportImpl.getInstance().invalidOperation("The jdbc-class-id extension may " +
                    "only be specified once\n" + e.getContext());
        }
        info.classIdExt = e;
        if (e.value != null) {
            info.noClassIdCol = false;
            if (e.isNoValue()) {
                if (cmd.pcSuperMetaData != null) {
                    throw BindingSupportImpl.getInstance().invalidOperation(JdoExtension.NO_VALUE + " is only valid " +
                            "for the least derived class in the " +
                            "heircachy:\n" + e.getContext());
                }
                info.noClassIdCol = true;
            } else if (JdoExtension.NAME_VALUE.equals(e.value)) {
                jdbcClass.jdbcClassId = cmd.jdoClass.name;
            } else if (JdoExtension.FULLNAME_VALUE.equals(e.value)) {
                jdbcClass.jdbcClassId = cmd.qname;
            } else {
                jdbcClass.jdbcClassId = e.value;
            }
        }
        if (e.nested != null) {
            if (cmd.pcSuperMetaData != null) {
                throw BindingSupportImpl.getInstance().runtime("The jdbc-class-id extension may " +
                        "only define a column for the least derived class in the " +
                        "hierarchy\n" + e.getContext());
            }
        }
    }

    private void processClassOptimisticLocking(JdoExtension e,
            JdbcClass jdbcClass, ClassMetaData cmd, boolean quiet) {
        if (cmd.pcSuperMetaData != null) {
            RuntimeException x = BindingSupportImpl.getInstance().runtime("The jdbc-optimistic-locking " +
                    "option may only be specified for the least derived class " +
                    "in a hierarchy\n" + e.getContext());
            cmd.addError(x, quiet);
        }
        if (e.value != null) {
            try {
                jdbcClass.optimisticLocking = e.getEnum(
                        jdbcMDE.OPTIMISTIC_LOCKING_ENUM);
            } catch (RuntimeException x) {
                cmd.addError(x, quiet);
            }
        }
        getClassInfo(cmd).optimisticLockingExt = e;
    }

    private void processClassColumnExtension(JdoExtension e, ClassMetaData cmd,
            ArrayList jdbcElements) {
        JdbcColumn col = createColumn(e.nested,
                CLASS_ID_FIELDNAME, /*CHFC*/Integer.TYPE/*RIGHTPAR*/);
        JdbcSimpleField f = new JdbcSimpleField();
        f.fake = true;
        f.col = col;
        FieldMetaData fmd = f.fmd = new FieldMetaData();
        fmd.fake = true;
        fmd.category = MDStatics.CATEGORY_SIMPLE;
        fmd.classMetaData = cmd;
        fmd.defaultFetchGroup = true;
        fmd.storeField = f;
        fmd.name = "jdo" + jdbcElements.size();
        fmd.persistenceModifier = MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
        fmd.setType(col.javaType);
        jdbcElements.add(f);
    }

    private void processClassPrimaryKeyExtension(JdoExtension e,
            ClassMetaData cmd, ArrayList jdbcElements) {
        if (cmd.identityType != MDStatics.IDENTITY_TYPE_DATASTORE) {
            throw BindingSupportImpl.getInstance().runtime("jdbc-primary-key only allowed for classes " +
                    "with identity-type 'datastore'\n" +
                    e.getContext());
        }

        ClassInfo info;
        JdoExtension[] a = e.nested;
        int n = a == null ? 0 : a.length;
        for (int i = 0; i < n; i++) {
            JdoExtension f = a[i];
            switch (f.key) {
                case JDBC_COLUMN:
                    // handled when column is created
                    break;
                case JDBC_CONSTRAINT:
                    info = getClassInfo(cmd);
                    if (info.pkConstraintName != null) {
                        throw BindingSupportImpl.getInstance().runtime("The jdbc-constraint extension may only appear once\n" +
                                e.getContext());
                    }
                    info.pkConstraintName = e.getString();
                    break;
                default:
                    if (e.isJdbc()) MetaDataBuilder.throwUnexpectedExtension(e);
            }
        }

        JdbcColumn col = createColumn(a,
                DATASTORE_PK_FIELDNAME, /*CHFC*/Integer.TYPE/*RIGHTPAR*/);
        col.pk = true;
        col.nulls = false;
        jdbcElements.add(col);
    }

    /**
     * Find the factory specified in a jdbc-key-generator extension. If the
     * factory is new an instance is created and stored for future access.
     */
    private JdbcKeyGeneratorFactory findKeyGenFactory(JdoExtension e) {
        String fname = e.getString();
        try {
            return keyGenRegistry.getFactory(fname);
        } catch (Exception x) {
            throw BindingSupportImpl.getInstance().runtime(x.getMessage() +
                    "\n" + e.getContext(), x);
        }
    }

    /**
     * Create a JdbcConverterFactory instance from an extension.
     */
    private JdbcConverterFactory createJdbcConverterFactory(JdoExtension e) {
        try {
            return converterRegistry.getFactory(e.getString());
        } catch (Exception x) {
            throw BindingSupportImpl.getInstance().runtime(
                    "Unable to create JdbcConverterFactory\n" + e.getContext(),
                    x);
        }
    }

    /**
     * Create a column from an optional array of extensions, an optional
     * fieldName and a base column. The properties of the base column are
     * the defaults. If nested is not null then it is searched for a
     * jdbc-column extension with a value matching the database
     * property of the store. If none is found then a jdbc-column extension
     * with no value is used. If it contains no jdbc-column extensions then
     * it is ignored. If the jdbc-type has been set in the extension then
     * it is used to provide the defaults, not the column.
     */
    public JdbcColumn createColumn(JdoExtension[] nested, JdbcColumn base) {

        // look for a jdbc-column extension matching our db or with no db
        nested = findMatchingJdbcColumn(nested);

        // copy the base column and use the mapping to change properties
        JdbcColumn c = base.copy();
        if (nested != null) {
            JdbcJavaTypeMapping m = createFieldMapping(nested);
            mappingResolver.fillMappingForJdbcType(m);
            c.updateFrom(m, mappingResolver);
            updateColumnName(nested, c);
            updateConverter(nested, c);
        }
        return c;
    }

    /**
     * Create a column from an optional array of extensions, an optional
     * fieldName and an optional javaType. If nested is not null then it is
     * searched for a jdbc-column extension with a value matching the database
     * property of the store. If none is found then a jdbc-column extension
     * with no value is used. If it contains no jdbc-column extensions then
     * it is ignored.
     */
    public JdbcColumn createColumn(JdoExtension[] nested,
            String fieldName, Class javaType) {

        // look for a jdbc-column extension matching our db or with no db
        nested = findMatchingJdbcColumn(nested);

        // create a fully resolved mapping and use this to make the column
        JdbcJavaTypeMapping m = createFieldMapping(nested);
        m = mappingResolver.resolveMapping(m, fieldName, javaType);
        JdbcColumn c = new JdbcColumn(m, mappingResolver);
        updateColumnName(nested, c);
        updateConverter(nested, c);
        c.comment = fieldName;
        return c;
    }

    /**
     * Set the converter (if any).
     */
    private void updateConverter(JdoExtension[] nested,
            JdbcColumn c) {
        if (nested != null) {
            boolean done = false;
            int n = nested.length;
            for (int i = 0; i < n; i++) {
                JdoExtension e = nested[i];
                if (e.key == JDBC_CONVERTER) {
                    if (done) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-converter extension has already been used\n" +
                                e.getContext());
                    }
                    JdbcConverterFactory f = createJdbcConverterFactory(e);
                    HashMap p = e.getPropertyMap();
                    try {
                        c.converter = f.createJdbcConverter(c, p,
                                mappingResolver);
                    } catch (IllegalArgumentException x) {
                        throw BindingSupportImpl.getInstance().runtime("Unable to create JdbcConverter\n" +
                                e.getContext(), x);
                    }
                    done = true;
                }
            }
        }
    }

    /**
     * Set the column name (if any).
     */
    private void updateColumnName(JdoExtension[] nested, JdbcColumn c) {
        if (nested != null) {
            int n = nested.length;
            for (int i = 0; i < n; i++) {
                JdoExtension e = nested[i];
                if (e.key != JDBC_COLUMN_NAME) continue;
                if (c.name != null) {
                    throw BindingSupportImpl.getInstance().runtime("Only one jdbc-column-name extension is allowed\n" +
                            e.getContext());
                }
                c.name = e.getString();
            }
        }
    }

    /**
     * If nested is not null then it is searched for a jdbc-column extension
     * with a value matching the database property of the store. If none is
     * found then a jdbc-column extension with no value is used. If it
     * contains no jdbc-column extensions then it is ignored.
     */
    private JdoExtension[] findMatchingJdbcColumn(JdoExtension[] nested) {
        if (nested != null) {
            JdoExtension matchGeneral = null;
            JdoExtension matchSpecific = null;
            String sdb = sqlDriver.getName();
            int n = nested.length;
            for (int i = 0; i < n; i++) {
                JdoExtension e = nested[i];
                if (e.key != JDBC_COLUMN) continue;
                String db = e.value;
                if (db == null) {
                    if (matchGeneral != null) {
                        throw BindingSupportImpl.getInstance().runtime("Only one all databases jdbc-column extension is allowed\n" +
                                e.getContext());
                    }
                    matchGeneral = e;
                } else if (db.equals(sdb)) {
                    if (matchSpecific != null) {
                        throw BindingSupportImpl.getInstance().runtime("Only one jdbc-column extension is allowed per database\n" +
                                e.getContext());
                    }
                    matchSpecific = e;
                }
            }
            if (matchSpecific != null) {
                nested = matchSpecific.nested;
            } else if (matchGeneral != null) {
                nested = matchGeneral.nested;
            } else {
                nested = null;
            }
        }
        return nested;
    }

    /**
     * Create a field mapping from an array of extensions.
     *
     * @param nested Nested extensions (may be null)
     */
    private JdbcJavaTypeMapping createFieldMapping(JdoExtension[] nested) {
        JdbcJavaTypeMapping m = new JdbcJavaTypeMapping();
        if (nested == null) return m;
        int n = nested.length;
        for (int i = 0; i < n; i++) {
            JdoExtension e = nested[i];
            switch (e.key) {
                case JDBC_COLUMN_NAME:
                    if (m.getColumnName() != null) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-column-name has already been set\n" +
                                e.getContext());
                    }
                    m.setColumnName(e.getString());
                    break;
                case JDBC_TYPE:
                    if (m.getJdbcType() != 0) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-type has already been set\n" +
                                e.getContext());
                    }
                    m.setJdbcType(getJdbcType(e));
                    break;
                case JDBC_SQL_TYPE:
                    if (m.getSqlType() != null) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-sql-type has already been set\n" +
                                e.getContext());
                    }
                    m.setSqlType(e.getString());
                    break;
                case JDBC_LENGTH:
                    if (m.getLength() >= 0) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-length has already been set\n" +
                                e.getContext());
                    }
                    m.setLength(e.getInt());
                    break;
                case JDBC_SCALE:
                    if (m.getScale() >= 0) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-scale has already been set\n" +
                                e.getContext());
                    }
                    m.setScale(e.getInt());
                    break;
                case JDBC_NULLS:
                    if (m.getNulls() != JdbcJavaTypeMapping.NOT_SET) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-nulls has already been set\n" +
                                e.getContext());
                    }
                    m.setNulls(e.getBoolean());
                    break;
                case JDBC_SHARED:
                    // ignore - shared columns are now figured out automatically
                    break;
                case JDBC_JAVA_TYPE:
                    if (m.getJavaType() != null) {
                        throw BindingSupportImpl.getInstance().runtime("jdbc-java-type has already been set\n" +
                                e.getContext());
                    }
                    m.setJavaType(e.getType(loader));
                    break;
                case JDBC_CONVERTER:
                    // handled once the column has been created
                    break;
                default:
                    if (e.isJdbc()) MetaDataBuilder.throwUnexpectedExtension(e);
                    break;
            }
        }
        return m;
    }

    /**
     * Get the value of an extension that must be a JDBC type name.
     *
     * @see java.sql.Types
     */
    public int getJdbcType(JdoExtension e) {
        try {
            return JdbcTypes.parse(e.getString());
        } catch (IllegalArgumentException x) {
            throw BindingSupportImpl.getInstance().runtime(x.getMessage() +
                    "\n" + e.getContext());
        }
    }

    public ModelMetaData getJmd() {
        return jmd;
    }

    public SqlDriver getSqlDriver() {
        return sqlDriver;
    }

    public JdbcMappingResolver getMappingResolver() {
        return mappingResolver;
    }

    protected void setMasterDetailFlags(ClassMetaData[] classes) {
        if (Debug.DEBUG) {
            System.out.println(
                    "MDB-JDBC: Setting master detail flags");
        }
        int clen = classes.length;
        for (int i = 0; i < clen; i++) {
            ClassMetaData cmd = classes[i];
            if (cmd.stateFields == null) continue; // possible if prev error
            for (int j = 0; j < cmd.stateFields.length; j++) {
                FieldMetaData fmd = cmd.stateFields[j];
                switch (fmd.category) {
                    case MDStatics.CATEGORY_REF:
                        JdbcField jdbcField = (JdbcField)fmd.storeField;
                        if (jdbcField != null) {   // possible if prev error
                            JdbcFKCollectionField masterField =
                                    ((JdbcRefField)jdbcField).masterCollectionField;
                            if (masterField != null) {
                                fmd.inverseFieldMetaData = masterField.fmd;
                                fmd.isDetail = true;
                                fmd.managed = masterField.fmd.managed;
                            }
                        }
                        break;
                    case MDStatics.CATEGORY_COLLECTION:
                        if (fmd.storeField instanceof JdbcFKCollectionField) {
                            fmd.inverseFieldMetaData = ((JdbcFKCollectionField)fmd.storeField).fkField.fmd;
                            fmd.isMaster = true;
                        }
                        break;
                }
            }
        }
    }

    protected void calcSuperCounts(ClassMetaData[] classes) {
        super.calcSuperCounts(classes);
        // find all the references that are being used to complete
        // collections mapped using a foreign key
        for (int j = 0; j < classes.length; j++) {
            ClassMetaData cmd = classes[j];
            if (cmd == null || cmd.stateFields == null) {
                continue;
            }
            int[] a = new int[cmd.stateFields.length];
            int c = 0;
            for (int i = cmd.stateFields.length - 1; i >= 0; i--) {
                FieldMetaData f = cmd.stateFields[i];
                if (f.storeField instanceof JdbcRefField) {
                    JdbcRefField rf = (JdbcRefField)f.storeField;
                    if (rf.masterCollectionField != null) {
                        a[c++] = i;
                    }
                }
            }
            if (c > 0) {
                int[] b = new int[c];
                System.arraycopy(a, 0, b, 0, c);
                cmd.fkCollectionRefStateFieldNos = b;
            } else {
                cmd.fkCollectionRefStateFieldNos = null;
            }
        }
    }

}
