
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

import org.jdom.Parent;
import com.versant.core.metadata.*;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.jdbc.JdbcMetaDataBuilder;
import com.versant.core.jdbc.JdbcStorageManagerFactory;
import com.versant.core.jdbc.metadata.JdbcClass;
import com.versant.core.jdbc.metadata.JdbcColumn;
import com.versant.core.jdbc.metadata.JdbcTable;
import com.versant.core.jdbc.metadata.JdbcMetaDataEnums;
import com.versant.core.jdo.externalizer.SerializedExternalizer;

import javax.jdo.spi.PersistenceCapable;

import java.lang.reflect.Field;


import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * Javabean wrapper for class meta data.
 */
public class MdClass extends MdClassOrInterface

{

    private static DatabaseMetaData databaseMetaData;

    private List fieldList = new ArrayList();
    private List treeFieldList = new ArrayList();
    private Set fakeFields = new HashSet();
    private List fetchGroupList = new ArrayList();
    private List indexList = new ArrayList();
    private MdConstraintDep constraintDep;

    private HashMap fieldMap = new HashMap(31);

    private MdField datastorePkField;
    private MdField[] appIdentityFields;
    private MdJdbcRef superRef;

    private Class cls;
    private String clsPackage;
    private List subclasses = new ArrayList();
    private List availablePCSuperclasses = new ArrayList();
    private ArrayList queryList = new ArrayList();
    private boolean noPCSuperclass;

    private String defJdbcClassId;

    private ClassMetaData classMetaData;
    private JdbcClass jdbcClass;

    public static final String ICON = "TreeClass16.gif";
    public static final String ICON_BASE = "TreeClassBase16.gif";
    public static final String ICON_MIDDLE = "TreeClassMiddle16.gif";
    public static final String ICON_LEAF = "TreeClassLeaf16.gif";

    public MdClass(MdProject mdProject, MdPackage mdPackage, MdElement element) {
        super(mdProject, mdPackage, element);
    }

    public MdClass(MdProject mdProject, MdPackage mdPackage, String name) {
        this(mdProject, mdPackage, new MdElement("class"));
        XmlUtils.setAttribute(element, "name", name);
    }

    /**
     * Analyze this class. This will attempt to load the class if not already
     * done. It will find all fields in the meta data and merge those with
     * fields found by reflection that will be persisted by default by JDO.
     */
    public void analyze(boolean notifyDatastore) {
        analyzeFields();

        List list = XmlUtils.findExtensions(element, FETCH_GROUP);
        fetchGroupList.clear();
        for (Iterator i = list.iterator(); i.hasNext();) {
            MdElement e = (MdElement)i.next();
            fetchGroupList.add(new MdFetchGroup(this, e));
        }
        fireMdClassChangeEvent();

        MdElement consDep = XmlUtils.findExtension(element, PERSIST_AFTER);
        if (consDep != null) {
            constraintDep = new MdConstraintDep(consDep);
        }

        list = XmlUtils.findExtensions(element, JDBC_INDEX);
        indexList.clear();
        for (Iterator i = list.iterator(); i.hasNext();) {
            MdElement e = (MdElement)i.next();
            indexList.add(new MdIndex(this, e));
        }
        fireMdClassChangeEvent();

        availablePCSuperclasses.clear();
        if (cls != null) {
            for (Class c = cls.getSuperclass(); c != null &&  c != Object.class  ;
                 c = c.getSuperclass()) {
                MdClass sc = mdProject.findClass(c.getName());
                if (sc != null && sc.mdDataStore == mdDataStore) {
//						noPCSuperclass = false;
                    if (sc.mdPackage == mdPackage) {
                        availablePCSuperclasses.add(sc.getName());
                        break;
                    } else {
                        availablePCSuperclasses.add(c.getName());
                        break;
                    }
                }
            }
        }

        if (!noPCSuperclass) {
//            element.removeAttribute("identity-type");
//            element.removeAttribute("objectid-class");
            XmlUtils.removeExtensions(element, JDBC_KEY_GENERATOR);
//            if (!isVerticalInheritance()) {
//                XmlUtils.removeExtensions(element, JDBC_TABLE_NAME);
//            }
        }

        queryList.clear();
        List qlist = element.getChildren("query");
        for (Iterator qIter = qlist.iterator(); qIter.hasNext();) {
            MdElement qe = (MdElement)qIter.next();
            MdQuery query = new MdQuery(mdProject, this, qe);
            queryList.add(query);
        }

        // notify out datastore (if any)
        if (notifyDatastore && mdDataStore != null) {
            mdDataStore.classAnalyzed(this);
        }


	}

    public MdConstraintDep getConstraintDep() {
        return constraintDep;
    }

    public void addConstraintDepClass(String name) {
        if (constraintDep == null) {
            constraintDep = new MdConstraintDep(this);
        }
        constraintDep.addDepClass(name);
    }

    public void removeConstraintDepClass(String e) {
        if (constraintDep != null) {
            constraintDep.removeField(e);
        }
        if (constraintDep.getDepList().isEmpty()) {
            XmlUtils.removeExtensions(element, PERSIST_AFTER);
        }
    }

    public List getConstraintDepList() {
        if (constraintDep == null) return new ArrayList();
        return constraintDep.getDepList();
    }

    /**
     * Add the field to fieldList and fieldMap. This does not trigger any
     * events from fieldList.
     */
    private void addFieldImp(MdField f) {
        fieldList.add(f);
        fieldMap.put(f.getName(), f);
    }

    /**
     * Removes a MdQuery element from this class
     */
    public void removeMdQuery(MdQuery query) {
        if (queryList.remove(query)) {
            element.removeContent(query.getElement());
            XmlUtils.makeDirty(element);
        }
    }

    /**
     * Adds a MdQuery to this class
     */
    public void addMdQuery(MdQuery query) {
        if (queryList.contains(query)) {
            return;
        }
        queryList.add(query);
        MdElement qe = query.getElement();
        MdElement classElem = (MdElement)qe.getParent();
        if (classElem != null) {
            classElem.removeContent(qe);
        }
        element.addContent(qe);
        XmlUtils.makeDirty(element);
        Collections.sort(queryList);
    }

    /**
     * Gets the queries that satisfy the search param (null returns all)
     */
    public ArrayList getQueries(String search) {
        if (search != null) {
            search = search.toLowerCase();
            ArrayList searchList = new ArrayList();
            String name = null;
            for (Iterator iter = queryList.iterator(); iter.hasNext();) {
                MdQuery query = (MdQuery)iter.next();
                name = query.getName().toLowerCase();
                if (name.startsWith(search)) {
                    searchList.add(query);
                }
            }
            Collections.sort(searchList);
            return searchList;
        } else {
            Collections.sort(queryList);
            return queryList;
        }
    }

    /**
     * Merge fields found in meta data with those found by reflection. This
     * also creates special 'fields' for the datastore PK (if datastore
     * identity) and the version or timestamp column (if using these
     * optimistic locking modes and no version/timestamp field has been
     * specified).
     */
    private void analyzeFields() {
        
        MdClass pcSuperMdClass = getPcSuperclassMdClassInh();
        if (pcSuperMdClass != null && pcSuperMdClass.isHorizontalInheritance()) {
            noPCSuperclass = true;
        } else {
            noPCSuperclass = element.getAttributeValue(
                    "persistence-capable-superclass") == null;
        }
        


		boolean appIdentity = "application".equals(
                element.getAttributeValue("identity-type"));

        fieldList.clear();
        // dont clear fieldMap - we need to lookup + reuse old MdField instances
        appIdentityFields = null;

        boolean notHoriz = !isHorizontalInheritance();
        boolean notEmbeddedOnly = !isEmbeddedOnly();

        // datastore PK 'field'
        if (notHoriz && notEmbeddedOnly && noPCSuperclass && !appIdentity && isJDBC()) {
            MdElement e = XmlUtils.findOrCreateExtension(element,
                    JDBC_PRIMARY_KEY);
            datastorePkField = (MdDatastorePKField)findField(
                    JdbcMetaDataBuilder.DATASTORE_PK_FIELDNAME);
            if (datastorePkField == null) datastorePkField = new MdDatastorePKField();
            datastorePkField.init(this, e);
            addFieldImp(datastorePkField);
        } else {
            XmlUtils.removeExtensions(element, JDBC_PRIMARY_KEY);
            datastorePkField = null;
        }

        // optimistic locking 'field'
        MdElement e = XmlUtils.findExtension(element, JDBC_OPTIMISTIC_LOCKING);
        if (notHoriz && notEmbeddedOnly && noPCSuperclass) {
            String fname = null;
            if (e == null) {    // look at datastore default
                String s = mdDataStore.getJdbcOptimisticLockingStr();
                if (s != null && (s.equals("version") || s.equals("timestamp"))) {
                    e = XmlUtils.createExtension(JDBC_OPTIMISTIC_LOCKING,
                            element);
                }
            } else {
                String s = e.getAttributeValue("value");
                if (s == null) s = mdDataStore.getJdbcOptimisticLockingStr();
                if (s == null || !(s.equals("version") || s.equals("timestamp"))) {
                    if (e.getAttributeValue("value") == null) {
                        element.removeContent(e);
                    } else {
                        e.removeContent();
                    }
                    e = null;   // no need to create 'field'
                } else {
                    fname = XmlUtils.getExtension(e, FIELD_NAME);
                }
            }
            if (e != null) {
                if (fname == null && isJDBC()) {    // create 'field'
                    MdOptLockingField f =
                            (MdOptLockingField)findField(
                                    JdbcMetaDataBuilder.OPT_LOCK_FIELDNAME);
                    if (f == null) f = new MdOptLockingField();
                    f.init(this, e);
                    addFieldImp(f);
                } else {    // make sure there is no column
                    XmlUtils.removeExtensions(e, JDBC_COLUMN);
                }
            }
        } else if (e != null) {
            element.removeContent(e);
            XmlUtils.makeDirty(element);
        }

        // class ID 'field'
        if (notHoriz && notEmbeddedOnly && noPCSuperclass && !subclasses.isEmpty() && !isJdbcClassIdNo() && isJDBC()) {
            MdClassIdField f =
                    (MdClassIdField)findField(
                            JdbcMetaDataBuilder.CLASS_ID_FIELDNAME);
            if (f == null) f = new MdClassIdField();
            f.init(this,
                    XmlUtils.findOrCreateExtension(element, JDBC_CLASS_ID));
            addFieldImp(f);
        } else {
            e = XmlUtils.findExtension(element, JDBC_CLASS_ID);
            if (e != null && e.getContentSize() > 0) {
                e.removeContent();
                XmlUtils.makeDirty(e);
            }
        }

        // find all fields by reflection
        cls = mdProject.loadClass(getQName());
        if (cls != null && cls.isInterface()) {
            cls = null;
            mdProject.getLogger().error(
                    getQName() + " is an interface (expected a class)");
        }
        HashMap fmap = new HashMap();
        if (cls != null) {
            clsPackage = MdUtils.getPackage(cls.getName());
            try {
                Field[] all = cls.getDeclaredFields();
                int n = all.length;
                for (int i = 0; i < n; i++) {
                    Field f = all[i];
                    if (mdProject.isDefaultPersistentField(
                            mdDataStore.getName(), f)) {
                        fmap.put(f.getName(), f);
                    }
                }
            } catch (Throwable x) {
                mdProject.getLogger().error("Unable to get fields for " +
                        getQName(), x);
            }
        } else {
            clsPackage = null;
        }

        // put in fields from meta data
        int mfstart = fieldList.size();
        int pkcount = 0;
        List list = element.getChildren("field");
        for (Iterator i = list.iterator(); i.hasNext();) {
            e = (MdElement)i.next();
            String fname = e.getAttributeValue("name");
            int dotIndex = fname.indexOf('.');
            MdField mdf = findField(fname);
            if (mdf == null){
                mdf = new MdField();
            }
            mdf.init(this, e);
            Field rf = null;
            if (fname != null) {
                rf = (Field)fmap.get(fname);
                if (rf != null) {
                    fmap.remove(fname);
                } else {
                    try {
                        if (dotIndex >= 0) {
                            MdClass mdClass = findClass(
                                    fname.substring(0, dotIndex));
                            if (mdClass != null) {
                                rf = mdClass.getDeclaredField(
                                        fname.substring(dotIndex + 1));
                            }
                        } else {
                            rf = getDeclaredField(fname);
                        }
                    } catch (NoSuchFieldException e1) {
                        if (dotIndex < 0) {
                            mdProject.getLogger().error("Unable to find field " +
                                    fname + " on class " + getQName(), e1);
                        } else {
                            continue;
                        }
                    }
                }
            }
            mdf.setField(rf);
            addFieldImp(mdf);
            if (mdf.isPrimaryKeyField()) ++pkcount;
        }

		
		// build the appIdentityFields array
        if (pkcount > 0) {
            appIdentityFields = new MdField[pkcount];
            int c = 0;
            for (int i = mfstart; ; i++) {
                MdField f = (MdField)fieldList.get(i);
                if (f.isPrimaryKeyField()) {
                    appIdentityFields[c++] = f;
					if (c == pkcount) break;
                }
            }
        }
		

        // put in fields found by reflection that are not in meta data that
        // are persistent by default
        for (Iterator i = fmap.values().iterator(); i.hasNext();) {
            Field rf = (Field)i.next();
            if (mdProject.isDefaultPersistentField(mdDataStore.getName(), rf)) {
                MdField mdf = findField(rf.getName());
                if (mdf == null) mdf = new MdField();
                mdf.init(this, rf);
                addFieldImp(mdf);

            }
        }



	// rebuild fieldMap to get rid of fields no longer present
        fieldMap.clear();
        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            fieldMap.put(f.getName(), f);
        }

        // analyze all the fields
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            f.analyze();


        }
        analyzeHorInh();
        Collections.sort(fieldList);
        fireMdClassChangeEvent();
    }

    protected Field getDeclaredField(String fname) throws NoSuchFieldException{
        if(cls != null){
            return cls.getDeclaredField(fname);
        }
        return null;
    }

    private boolean isJDBC() {
        if (mdDataStore != null) {
            return mdDataStore.isJDBC();
        }
        return true;
    }

    /**
     * Do pass 2 analysis on this class. This is called after analyze has
     * been called on all classes in the project.
     */
    public void analyzePass2(){
        analyzeHorInh();
        MdClass sc = getPcSuperclassMdClass();
        if (sc != null) {   // copy identity fields from superclass
            appIdentityFields = sc.appIdentityFields;
            datastorePkField = sc.datastorePkField;
            superRef = null;
        }

        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            f.analyze(); // re-analyze to fix composite ref problem
            f.analyzePass2();
        }
        List allFields = getFieldsIncEmbAndFake();
        // find and add fake fields
        for (int i = 0; i < allFields.size(); i++) {
            MdField f = (MdField)allFields.get(i);
            if (f.category == MDStatics.CATEGORY_COLLECTION &&
                    f.getPersistenceModifierInt() == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT
                    && f.getInverseStr() != null) {
                MdField mm = f.getInverseField();
                if (mm instanceof FakeOne2ManyField) {
                    String s = f.getColElementTypeStr();
                    if (s != null) {
                        MdClass c = findClass(s);
                        if (c != null) {
                            if (!c.fakeFields.contains(f)) {
                                c.fakeFields.add(f);
                            }
                        }
                    }
                }
            }
        }
        fireMdClassChangeEvent();
    }

    public void addMdClassChangeListener(MdClassChangeListener listener) {
        listenerList.addListener(listener);
    }

    public void removeMdClassChangeListener(MdClassChangeListener listener) {
        listenerList.removeListener(listener);
    }

    public void fireMdClassChangeEvent() {
        MdClassChangeEvent event = new MdClassChangeEvent(this, getMdProject(),
                getMdDataStore(), this);
        Iterator it = listenerList.getListeners(/*CHFC*/MdClassChangeListener.class/*RIGHTPAR*/);
        while (it.hasNext() && !event.isConsumed()) {
            MdClassChangeListener listener = (MdClassChangeListener)it.next();
            listener.fieldChanged(event);
        }
    }

    public ClassMetaData getClassMetaData() {
        return classMetaData;
    }

    /**
     * This is called when the meta data has been parsed. The param will be
     * null if meta data parsing failed.
     */
    public void setClassMetaData(ClassMetaData classMetaData, boolean quiet) {
        this.classMetaData = classMetaData;
        jdbcClass = null;
        if(classMetaData != null && classMetaData.storeClass instanceof JdbcClass){
            jdbcClass = (JdbcClass)classMetaData.storeClass;
        }
//        jdbcClass = classMetaData == null ? null : (JdbcClass)classMetaData.storeClass;
        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            MdField mdField = (MdField)fieldList.get(i);
            try {
                mdField.setClassMetaData(classMetaData);
            } catch (RuntimeException e) {
                classMetaData.addError(e, quiet);
            }
        }
    }

    /**
     * This is called when the database meta data has been parsed. The param will be
     * null if meta data parsing failed.
     */
    public static void setDatabaseMetaData(
            DatabaseMetaData newDatabaseMetaData) {
        databaseMetaData = newDatabaseMetaData;
    }

    /**
     * This returns the database meta data or null if it has not been parsed.
     */
    public static DatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    /**
     * This is called by our MdDataStore when the user chooses a new database
     * to work with. This message is passed on to all our fields so they can
     * edit the correct jdbc-column elements and so on.
     */
    public void selectedDBChanged(String selectedDB) {
        for (int i = fieldList.size() - 1; i >= 0; i--) {
            MdField f = (MdField)fieldList.get(i);
            f.selectedDBChanged(selectedDB);
        }
    }

    /**
     * Get the currently selected database from our datastore.
     */
    public String getSelectedDB() {
        return mdDataStore.getSelectedDBImp();
    }

    public void setDefJdbcClassId(String defJdbcClassId) {
        this.defJdbcClassId = defJdbcClassId;
    }

    public String getDefJdbcClassId() {
        if (jdbcClass != null && jdbcClass.jdbcClassId != null) return jdbcClass.jdbcClassId.toString();
        return null;
    }

    /**
     * Get the Class object or null if not loaded.
     */
    public Class getCls() {
        return cls;
    }

    public void setCls(Class cls) {
        this.cls = cls;
    }

    /**
     * Get the package of cls or null if not loaded.
     *
     * @see #getCls
     */
    public String getClsPackage() {
        return clsPackage;
    }

    public List getSubclasses() {
        return subclasses;
    }

    /**
     * Get all of our subclasses that are mapped into our table (i.e flat).
     */
    public List getFlatSubclasses() {
        ArrayList a = new ArrayList(subclasses.size());
        int n = subclasses.size();
        for (int i = 0; i < n; i++) {
            MdClass cls = (MdClass)subclasses.get(i);
            if (!cls.isVerticalInheritance()) a.add(cls);
        }
        return a;
    }

    /**
     * Get all of our subclasses that are not mapped into our table (i.e
     * vertical). Note that this will find the first subclass in the tree
     * rooted at each of our subclasses that is in a different table.
     */
    public List getVerticalSubclasses() {
        ArrayList a = new ArrayList(subclasses.size());
        getVerticalSubclassesImp(a);
        return a;
    }

    private void getVerticalSubclassesImp(ArrayList a) {
        int n = subclasses.size();
        for (int i = 0; i < n; i++) {
            MdClass cls = (MdClass)subclasses.get(i);
            if (cls.isVerticalInheritance()) {
                a.add(cls);
            } else {
                cls.getVerticalSubclassesImp(a);
            }
        }
    }

    public List getFieldList() {
        return fieldList;
    }

    public List getFieldsIncEmbAndFake() {
        ArrayList all = new ArrayList(fieldList.size() * 2);
        all.addAll(fieldList);
        for (Iterator it = fakeFields.iterator(); it.hasNext();) {
            MdField fakeField = (MdField)it.next();
            try {
                MdField inverseField = fakeField.getInverseField();
                if (inverseField != null) {
                    all.add(inverseField);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
//        all.addAll(fakeFields);
        for (int i = 0; i < all.size(); i++) {
            Object o = all.get(i);
//            if(o == null){
//                all.remove(i);
//                i--;
//                continue;
//            }
            MdField field = (MdField)o;
            List embeddedFields = field.getEmbeddedFields();
            if (embeddedFields != null) {
                all.addAll(embeddedFields);
            }

        }
        return all;
    }

    public void addField(String name) throws NoSuchFieldException {
        Class cls = getCls();
        if (cls != null) {
            MdProject project = getMdProject();
            Field field = cls.getDeclaredField(name);
            if (findField(name) == null
                    && project.isPersistableField(getMdDataStore().getName(),
                            field)) {
                MdField f = new MdField();
                f.init(this, field);
                addField(f);
                boolean defaultPersist = getMdProject().isDefaultPersistentField(
                        getMdDataStore().getName(), field);
                f.setPersistenceModifierInt(
                        MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
                if (!defaultPersist) {
                    f.setExternalizerStr(SerializedExternalizer.SHORT_NAME);
                }
            }
        }
    }

    public void addField(MdField f) {
        addFieldImp(f);
        mdDataStore.classAnalyzed(this);
    }

    public void removeField(String name) {
        MdField mdField = findField(name);
        Field rf = mdField.getField();
        if (rf != null && getMdProject().isDefaultPersistentField(
                getMdDataStore().getName(), rf)) {
            mdField.setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_NONE);
        } else {
            removeField(mdField);
        }
    }

    public void removeField(MdField f) {
        element.removeContent(f.getElement());
        XmlUtils.makeDirty(element);
        removeFieldImp(f);
        mdDataStore.classAnalyzed(this);
    }

    private void removeFieldImp(MdField f) {
        fieldList.remove(f);
        fieldMap.remove(f);
    }

    /**
     * Find field with name or null if none.
     */
    public MdField findField(String name) {
        return (MdField)fieldMap.get(name);
    }

    public int moveFieldUp(MdField f) {
        int i = fieldList.indexOf(f);
        if (i < 1) return i;
        MdField t = (MdField)fieldList.get(i - 1);
        fieldList.set(i - 1, f);
        fieldList.set(i, t);
        syncFieldElements();
        return i - 1;
    }

    public int moveFieldDown(MdField f) {
        int i = fieldList.indexOf(f);
        if (i < 0 || i >= fieldList.size() - 1) return i;
        MdField t = (MdField)fieldList.get(i + 1);
        fieldList.set(i + 1, f);
        fieldList.set(i, t);
        syncFieldElements();
        return i + 1;
    }

    private void syncFieldElements() {
        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            element.removeContent(f.getElement());
        }
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            element.addContent(f.getElement());
        }
        XmlUtils.makeDirty(element);
    }

    public List getIndexList() {
        return indexList;
    }

    /**
     * A list of all the fields that will create a db table
     */
    public List getTableFieldList() {
        if (isJDBC()) {
            List fieldList = new ArrayList(getFieldList());
            for (int i = 0; i < fieldList.size(); i++) {
                MdField field = (MdField)fieldList.get(i);
                int perMod = field.getPersistenceModifierInt();
                if (perMod == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT &&
                        field.getCategory() == MDStatics.CATEGORY_COLLECTION &&
                        field.getInverseStr() == null &&
                        field.getFieldWeAreInverseFor() == null) {
                    continue;
                } else if (field.isEmbeddedRef()) {
                    List embeddedFields = field.getEmbeddedFields();
                    if (embeddedFields != null) {
                        fieldList.addAll(embeddedFields);
                    }
                }
                fieldList.remove(i);
                i--;
            }
            return fieldList;
        }
        return Collections.EMPTY_LIST;
    }

    public JdbcTable getTable() {
        ClassMetaData cmd = getClassMetaData();
        if (cmd != null) {
            JdbcClass jdbcClass = (JdbcClass)cmd.storeClass;
            if (jdbcClass.doNotCreateTable) {
                return null;
            } else {
                return jdbcClass.table;
            }

        } else {
            return null;
        }
    }

    public void addIndex(MdIndex idx) {
        element.addContent(idx.getElement());
        XmlUtils.makeDirty(element);
        indexList.add(idx);
    }

    public void removeIndex(MdIndex idx) {
        element.removeContent(idx.getElement());
        XmlUtils.makeDirty(element);
        indexList.remove(idx);
    }

    public MdValue getIdentityType() {
        if (!noPCSuperclass) return MdValue.NA;

        MdValue v = new MdValue(element.getAttributeValue("identity-type"));


        v.setPickList(PickLists.IDENTITY_TYPE);
        v.setDefText("datastore");
        return v;
    }

    public void setIdentityType(MdValue v) {
        if (!noPCSuperclass) return;


        XmlUtils.setAttribute(element, "identity-type", v.getText());


/*START_JAVAONLY
		//todo: what type to use ???
		if( v.getText().equals( "datastore" ) )
		{
			cls.setAppIdClassName(null);
		}
		else
		{
			cls.setAppIdClassName("<to be specified>");
		}
END_JAVAONLY*/

		getMdProject().syncAllClassesAndInterfaces();
    }

    public String getObjectIdClassStr() {

        return element.getAttributeValue("objectid-class");


    }

    public MdValue getObjectIdClass() {
        if (!noPCSuperclass) return MdValue.NA;
        MdValue v = new MdClassNameValue(getObjectIdClassStr());

        v.setOnlyFromPickList(false);
        return v;
    }

    public void setObjectIdClass(MdValue v) {
        if (!noPCSuperclass) return;

		XmlUtils.setAttribute(element, "objectid-class", v.getText());
	}

    public MdValue getRequiresExtent() {
        MdValue v = new MdValue(element.getAttributeValue("requires-extent"));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("true");
        return v;
    }

    public void setRequiresExtent(MdValue v) {
        XmlUtils.setAttribute(element, "requires-extent", v.getText());
    }

    public String getPcSuperclassStr() {
        
        return element.getAttributeValue("persistence-capable-superclass");
        

    }

    public MdClass getPcSuperclassMdClass() {
        MdClass pcSuperMdClass = getPcSuperclassMdClassInh();
        if(pcSuperMdClass != null && pcSuperMdClass.isHorizontalInheritance()){
            return null;
         }
        return pcSuperMdClass;
    }

    public MdClass getPcSuperclassMdClassInh() {
        String s = getPcSuperclassStr();
        if (s == null) return null;
        return findClass(s);
    }

    public MdValue getPcSuperclass() {
        MdValue v = new MdClassNameValue(getPcSuperclassStr());
        v.setDefText("none");
        v.setPickList(availablePCSuperclasses);
        return v;
    }

    public void setPcSuperclass(MdValue v) {
        String oldpcs = getPcSuperclassStr();
        String pcs = v.getText();
        if (oldpcs != null) {
            if (pcs != null && oldpcs.equals(pcs)) return;
            MdClass c = mdPackage.findClass(oldpcs);
            if (c != null) {
                c.subclasses.remove(this);
                c.analyzeFields();
                c.mdDataStore.classAnalyzed(c);
            }
        }
        if (pcs != null) {
            MdClass c = mdPackage.findClass(pcs);
            if (c != null) {
                c.subclasses.add(this);
                c.analyzeFields();
                c.mdDataStore.classAnalyzed(c);
            }
        }
        XmlUtils.setAttribute(element, "persistence-capable-superclass", pcs);
        analyzeFields();
        mdDataStore.classAnalyzed(this);
    }

    public MdElement getJdbcInheritanceElement() {
        return XmlUtils.findOrCreateExtension(element,
                JdoExtensionKeys.JDBC_INHERITANCE);
    }

    public String getJdbcInheritanceStr() {
        return XmlUtils.getExtension(element,
                JdoExtensionKeys.JDBC_INHERITANCE);
    }

    /**
     * Get the default inheritance for this class. If the topmost class
     * in the hierarchy is flagged as not having a desciminator column then
     * this is vertical otherwise it uses the datastore default.
     */
    public String getJdbcInheritanceDefault() {
        MdClass topClass = getTopClass();
        /*if (MetaDataEnums.INHERITANCE_HORIZONTAL.equals(topClass.getJdbcInheritanceStr())) {
            return MetaDataEnums.INHERITANCE_HORIZONTAL;
        } else */if (topClass.isJdbcClassIdNo()) {
            return JdbcMetaDataEnums.INHERITANCE_VERTICAL;
        } else {
            if (mdDataStore == null) return null;
            return mdDataStore.getJdbcInheritanceStr();
        }
    }

    public MdValue getJdbcInheritance() {
        MdClass pcSuperMdClass = getPcSuperclassMdClass();
        if (pcSuperMdClass == null) {
            if((getSubclasses().size() == 0 && !isHorizontalInheritance()) || getPcSuperclassStr() != null){
                return MdValue.NA;
            }else{
                MdValue v = new MdValue(getJdbcInheritanceStr());
                v.setDefText("");
				ArrayList li = new ArrayList();
				li.add(JdbcMetaDataEnums.INHERITANCE_HORIZONTAL);
                v.setPickList(li);
                return v;
            }
        } else {
            if(pcSuperMdClass.isHorizontalInheritance()){
                return MdValue.NA;
            }else{
                MdValue v = new MdValue(getJdbcInheritanceStr());
                v.setDefText(getJdbcInheritanceDefault());
                v.setPickList(PickLists.JDBC_INHERITANCE);
                return v;
            }
        }
    }

    private boolean isHorizontalInheritance() {
        return JdbcMetaDataEnums.INHERITANCE_HORIZONTAL.equals(getJdbcInheritanceStr());
    }

    public void setJdbcInheritance(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.JDBC_INHERITANCE,
                v.getText());
        getMdProject().syncAllClassesAndInterfaces();
    }

    public boolean isVerticalInheritance() {
        String s = getJdbcInheritanceStr();
        if (s == null) s = getJdbcInheritanceDefault();
        return JdbcMetaDataEnums.INHERITANCE_VERTICAL.equals(s);
    }

    /**
     * Does the whole hierarchy use vertical inheritance?
     */
    public boolean isVerticalInheritanceHierarchy() {
        if (!isVerticalInheritance()) return false;
        for (int i = subclasses.size() - 1; i >= 0; i--) {
            MdClass c = (MdClass)subclasses.get(i);
            if (!c.isVerticalInheritanceHierarchy()) return false;
        }
        return true;
    }

    public MdValue getReadOnly() {
        MdValue v = new MdValue(XmlUtils.getExtension(element, READ_ONLY));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText(mdDataStore.getReadOnlyStr());
        return v;
    }

    public void setReadOnly(MdValue v) {
        XmlUtils.setExtension(element, READ_ONLY, v.getText());
    }

    public String getJdbcTableNameStr() {
        if (!noPCSuperclass && !isVerticalInheritance()) {
            return null;
        }
        return XmlUtils.getExtension(element, JDBC_TABLE_NAME);
    }

//    public String getJdbcSchemaNameStr() {
//        if (!noPCSuperclass && !isVerticalInheritance()) {
//            return null;
//        }
//        return XmlUtils.getExtension(element, JDBC_SCHEMA_NAME);
//    }

    public String getDefTableName() {
        if (jdbcClass != null && jdbcClass.table != null) return jdbcClass.table.name;
        return null;
    }

    public MdValue getJdbcTableName() {
        if (!noPCSuperclass) return MdValue.NA;
        MdValue v = new MdValue(getJdbcTableNameStr());
        v.setDefText(getDefTableName());
        if (databaseMetaData != null) {
            v.setCaseSensitive(false);
            v.setWarningOnError(true);
            v.setOnlyFromPickList(true);
//            if (v.text != null){
//                v.setPickList(databaseMetaData.getTableNames(v.text));
//            } else {
//                v.setPickList(databaseMetaData.getTableNames(v.defText));
//            }
            v.setPickList(databaseMetaData.getAllTableNames());
        }
        return v;
    }

    public String getJdbcTableFinalName() {
        MdClass sc = this;
        for (; sc.getPcSuperclassMdClass() != null;) {
            sc = sc.getPcSuperclassMdClass();
        }
        String t = sc.getJdbcTableNameStr();
        if (t == null) t = sc.getDefTableName();
        return t;
    }

    public void setJdbcTableName(MdValue v) {
        if (!noPCSuperclass) return;
        XmlUtils.setExtension(element, JDBC_TABLE_NAME, v.getText());
    }

//    public void setJdbcSchemaName(MdValue v) {
//        if (!noPCSuperclass) return;
//        XmlUtils.setExtension(element, JDBC_SCHEMA_NAME, v.getText());
//    }

    public boolean isApplicationIdentity() {
        String s = element.getAttributeValue("identity-type");
        return s != null && s.equals("application");
    }

    public String getJdbcKeyGeneratorStr() {
        if (noPCSuperclass) {
            return XmlUtils.getExtension(element, JDBC_KEY_GENERATOR);
        } else {
            return null;
        }
    }

    public MdValue getJdbcKeyGenerator() {
        if (!noPCSuperclass) return MdValue.NA;
        MdValue v = new MdClassNameValue(getJdbcKeyGeneratorStr());
        v.setPickList(PickLists.JDBC_KEY_GENERATOR);
        if (isApplicationIdentity()) {
            v.setDefText("none");
        } else {
            v.setDefText(mdDataStore.getJdbcKeyGeneratorStr());
        }
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setJdbcKeyGenerator(MdValue v) {
        XmlUtils.setExtension(element, JDBC_KEY_GENERATOR, v.getText());
    }

    public String getCacheStrategyStr() {
        return XmlUtils.getExtension(element, CACHE_STRATEGY);
    }

    public MdValue getCacheStrategy() {
        MdValue v = new MdValue(getCacheStrategyStr());
        v.setPickList(PickLists.CACHE_STRATEGY);
        v.setDefText(mdDataStore.getCacheStrategyStr());
        return v;
    }

    public void setCacheStrategy(MdValue v) {
        XmlUtils.setExtension(element, CACHE_STRATEGY, v.getText());
    }

    public String getDeleteOrphansStr() {
        return XmlUtils.getExtension(element, DELETE_ORPHANS);
    }

    public boolean isDeleteOrphansBool() {
        String s = getDeleteOrphansStr();
        return s != null && s.equals("true");
    }

    public MdValue getDeleteOrphans() {
        MdValue v = new MdValue(getDeleteOrphansStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setDeleteOrphans(MdValue v) {
        setDeleteOrphansStr(v.getText());
    }

    public void setDeleteOrphansStr(String s) {
        XmlUtils.setExtension(element, DELETE_ORPHANS, s);
    }

    public MdValue getJdbcUseSubclassJoin() {
        MdValue v = new MdValue(
                XmlUtils.getExtension(element, JDBC_USE_SUBCLASS_JOIN));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("true");
        return v;
    }

    public void setJdbcUseSubclassJoin(MdValue v) {
        XmlUtils.setExtension(element, JDBC_USE_SUBCLASS_JOIN, v.getText());
    }

    public String getJdbcUseJoinStr() {
        return XmlUtils.getExtension(element, JDBC_USE_JOIN);
    }

    public MdValue getJdbcUseJoin() {
        MdValue v = new MdValue(getJdbcUseJoinStr());
        v.setPickList(PickLists.JDBC_USE_JOIN);
        v.setDefText("outer");
        return v;
    }

    public void setJdbcUseJoin(MdValue v) {
        XmlUtils.setExtension(element, JDBC_USE_JOIN, v.getText());
    }

    public String getJdbcClassIdStr() {
        return XmlUtils.getExtension(element, JDBC_CLASS_ID);
    }

    /**
     * Has the jdbc-class-id extension been set to {no} i.e. has the
     * jdo_class column been disabled?
     */
    public boolean isJdbcClassIdNo() {
        String s = getJdbcClassIdStr();
        if (s == null) {
            return mdDataStore != null
                    && mdDataStore.getInheritanceNoClassIdBool();
        }
        return JdoExtension.NO_VALUE.equals(s);
    }

    /**
     * Set the jdbc-class-id extension to no (if true) or default (if false).
     */
    public void setJdbcClassIdNo(boolean no) {
        String s;
        if (no) {
            s = JdoExtension.NO_VALUE;
        } else if (getMdDataStore().getInheritanceNoClassIdBool()) {
            s = defJdbcClassId;
        } else {
            s = null;
        }
        setJdbcClassId(new MdValue(s));
    }

    public MdValue getJdbcClassId() {
        MdValue v = new MdValue(getJdbcClassIdStr());
        if (getMdDataStore().getInheritanceNoClassIdBool()) {
            v.setPickList(PickLists.NO);
            v.setDefText(defJdbcClassId);
        } else {
            ArrayList a = new ArrayList();
            if (getPcSuperclassStr() == null) a.add(JdoExtension.NO_VALUE);
            a.add(defJdbcClassId);
            a.add(JdoExtension.NAME_VALUE);
            a.add(JdoExtension.FULLNAME_VALUE);
            v.setPickList(a);
            v.setDefText(defJdbcClassId);
            v.setOnlyFromPickList(false);
        }
        return v;
    }

    public void setJdbcClassId(MdValue v) {
        String s = v.getText();
        MdElement e = XmlUtils.findExtension(element, JDBC_CLASS_ID);
        if (XmlUtils.isEmpty(s)) {
            if (e != null) {
                if (e.getContentSize() > 0) {
                    e.removeAttribute("value");
                } else {
                    element.removeContent(e);
                }
            }
        } else {
            if (e == null) {
                e = XmlUtils.createExtension(JDBC_CLASS_ID, element);
                element.addContent(e);
            }
            e.setAttribute("value", s);
        }
        XmlUtils.makeDirty(element);
        analyzeFields();
        mdDataStore.classAnalyzed(this);
    }

    public String getJdbcOptimisticLockingStr() {
        if (noPCSuperclass) {
            return XmlUtils.getExtension(element, JDBC_OPTIMISTIC_LOCKING);
        } else {
            return null;
        }
    }

    public MdValue getJdbcOptimisticLocking() {
        if (noPCSuperclass) {
            MdValue v = new MdValue(getJdbcOptimisticLockingStr());
            v.setPickList(PickLists.JDBC_OPTIMISTIC_LOCKING);
            v.setDefText(mdDataStore.getJdbcOptimisticLockingStr());
            return v;
        } else {
            return MdValue.NA;
        }
    }

    public void setJdbcOptimisticLocking(MdValue v) {
        if (!noPCSuperclass) return;
        XmlUtils.setExtension(element, JDBC_OPTIMISTIC_LOCKING, v.getText());
        analyzeFields();
        mdDataStore.classAnalyzed(this);
    }

    public MdValue getJdbcOptimsticLockingField() {
        if (noPCSuperclass) {
            String s = XmlUtils.getExtension(element, JDBC_OPTIMISTIC_LOCKING);
            if (s == null) s = mdDataStore.getJdbcOptimisticLockingStr();
            MdClass c = null;
            String type = null;
            if (s != null) {
                if (s.equals(MetaDataEnums.OPTIMISTIC_LOCKING_VERSION)) {
                    c = this;
                    type = "int";
                } else if (s.equals(MetaDataEnums.OPTIMISTIC_LOCKING_TIMESTAMP)) {
                    c = this;
                    type = "java.util.Date";
                } else {
                    return MdValue.NA; // none or changed
                }
            }
            MdValue v = new MdFieldValue(XmlUtils.getExtension(element, JDBC_OPTIMISTIC_LOCKING,
                    FIELD_NAME),
                    c, type, false);
            v.setDefText(JdbcMetaDataBuilder.OPT_LOCK_FIELDNAME);
            return v;
        } else {
            return MdValue.NA;
        }
    }

    public void setJdbcOptimsticLockingField(MdValue v) {
        if (!noPCSuperclass) return;
        XmlUtils.setExtension(element, JDBC_OPTIMISTIC_LOCKING,
                FIELD_NAME, v.getText());
        analyzeFields();
        mdDataStore.classAnalyzed(this);
    }

    public List getFetchGroupList() {
        return fetchGroupList;
    }

    public List getFetchGroupNameList() {
        int n = fetchGroupList.size();
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            MdFetchGroup g = (MdFetchGroup)fetchGroupList.get(i);
            a.add(g.getName());
        }
        return a;
    }

    public void addFetchGroup(MdFetchGroup fg) {
        Parent parent = fg.getElement().getParent();
        if (parent != element) {
            if (parent != null) {

                parent.addContent(fg.getElement());		//this is not compatible with JDOM 1.0 !!!


            }
            element.addContent(fg.getElement());
        }
        XmlUtils.makeDirty(element);
        fetchGroupList.add(fg);
    }

    public void removeFetchGroup(MdFetchGroup fg) {
        element.removeContent(fg.getElement());
        XmlUtils.makeDirty(element);
        fetchGroupList.remove(fg);
    }

    public boolean isEmbeddedOnly() {
        return "true".equals(XmlUtils.getExtension(element, EMBEDDED_ONLY));
    }

    public void setEmbeddedOnly(boolean embeddedOnly) {
        XmlUtils.setExtension(element, EMBEDDED_ONLY, embeddedOnly ? "true" : "false");
        analyzeFields();
    }

    public MdValue getEmbeddedOnly() {
        MdValue v = new MdValue(
                XmlUtils.getExtension(element, EMBEDDED_ONLY));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setEmbeddedOnly(MdValue v) {
        XmlUtils.setExtension(element, EMBEDDED_ONLY, v.getText());
        analyzeFields();
    }

    public String getJdbcDoNotCreateTableStr() {
        return XmlUtils.getExtension(element, JDBC_DO_NOT_CREATE_TABLE);
    }

    public boolean getJdbcDoNotCreateTableBool() {
        String s = getJdbcDoNotCreateTableStr();
        if (s == null) s = mdDataStore.getJdbcDoNotCreateTableStr();
        return "true".equals(s);
    }

    public MdValue getJdbcDoNotCreateTable() {
        MdValue v = new MdValue(getJdbcDoNotCreateTableStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText(mdDataStore.getJdbcDoNotCreateTableStr());
        return v;
    }

    public void setJdbcDoNotCreateTable(MdValue v) {
        XmlUtils.setExtension(element, JDBC_DO_NOT_CREATE_TABLE, v.getText());
    }

    public MdValue getRefsInDefaultFetchGroup() {
        MdValue v = new MdValue(XmlUtils.getExtension(element,
                OIDS_IN_DEFAULT_FETCH_GROUP));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText(mdDataStore.getRefsInDefaultFetchGroupStr());
        return v;
    }

    public void setRefsInDefaultFetchGroup(MdValue v) {
        XmlUtils.setExtension(element, OIDS_IN_DEFAULT_FETCH_GROUP,
                v.getText());
    }

    /**
     * What is the category of the field with persistence-modifier pm and
     * type?
     */
    public int getFieldCategory(int pm, Class type) {
        return mdProject.getFieldCategory(pm, type);
    }

    /**
     * Is this a top level class?
     */
    public boolean isNoPCSuperclass() {
        return noPCSuperclass;
    }

    public MetaDataUtils getMdutils() {
        return mdProject.getMdutils();
    }

    /**
     * Get the primary key column of this class. If the class uses application
     * identity and there is more than one app identity field then null
     * is returned. This will return the pk for the topmost class in the
     * table that this class is mapped to.
     */
    public MdColumn getPkColumn() {
        if (datastorePkField != null) return datastorePkField.col;
        if (appIdentityFields == null || appIdentityFields.length != 1) {
            return null;
        }
        return appIdentityFields[0].col;
    }

    /**
     * Get the application identity fields of this class or null if the
     * class does not use application identity. This will return the pk
     * for the topmost class if this class is a derived class in a hierarchy.
     */
    public MdField[] getAppIdentityFields() {
        return appIdentityFields;
    }

    /**
     * Does this class have a composite primary key?
     */
    public boolean isCompositePrimaryKey() {
        return appIdentityFields != null && appIdentityFields.length > 1;
    }

    /**
     * Find a class in our package or another package. If the class name
     * does not specify a package it is assumed to be in this package.
     * Returns null if no class found.
     */
    public MdClass findClass(String cname) {
        return mdPackage.findClass(cname);
    }

    /**
     * Has this class been loaded and enhanced (i.e. implements PC)?
     */

    public boolean isEnhanced() {
        return cls != null && PersistenceCapable.class.isAssignableFrom(cls);
    }


    /**
     * Write the names of all tables and columns associated with this class
     * into the meta data. This is a NOP if the information is not available
     * (i.e. the meta data has errors).
     */
    public void writeMappingsToMetaData() throws Exception {
        if (jdbcClass == null) return;
        System.out.println("Locking meta data for " + getQName());
        if (getPcSuperclassStr() == null && getJdbcTableNameStr() == null) {
            setJdbcTableName(new MdValue(getDefTableName()));
        }
        for (Iterator i = fieldList.iterator(); i.hasNext();) {
            MdField f = (MdField)i.next();
            f.writeMappingsToMetaData();
        }
    }

    /**
     * Get our reference to our superclass or null if none (i.e. we have no
     * superclass or are mapped flat).
     */
    public MdJdbcRef getSuperRef(MdClassTable srcTable, MdClassTable destTable) {
        MdClass tc = getTableClass();
        if (tc != this) return tc.getSuperRef(srcTable, destTable);
        if (noPCSuperclass) return null;
        MdClass sc = getPcSuperclassMdClass();
        if (superRef == null) {
            superRef = new MdJdbcRef();
            superRef.setIgnoreJoin(true);
        }
        JdbcColumn[] cols = null;
        if (jdbcClass != null && jdbcClass.table != null) {
            cols = jdbcClass.table.pk;
        }
        superRef.init(getJdbcInheritanceElement(), srcTable, sc, destTable,
                getMdDataStore(), getMdPackage(), cols);
        return superRef;
    }

    /**
     * Add all the primary key columns (MdColumn instances) from the table for
     * this class to the supplied table.
     */
    public void addPKColumnsToTable(MdTable table) {
        MdClass tc = getTableClass();
        if (tc != this) {
            tc.addPKColumnsToTable(table);
            return;
        }
        if (!noPCSuperclass) {
            MdColumn[] pkColumns = getSuperRef(null, null).getRefCols();
            for (int i = 0; i < pkColumns.length; i++) {
                pkColumns[i].setPrimaryKey(true);
                pkColumns[i].setTable(table);
                table.addCol(pkColumns[i]);
            }
        } else if (isCompositePrimaryKey()) {
            MdField[] fa = getAppIdentityFields();
            for (int i = 0; i < fa.length; i++) {
                MdColumn col = fa[i].getCol();
                if (col != null) {
                    col.setPrimaryKey(true);
                    col.setTable(table);
                    table.addCol(col);
                }
            }
        } else {
            MdColumn col = getPkColumn();
            if (col != null) {
                col.setPrimaryKey(true);
                col.setTable(table);
                table.addCol(col);
            }
        }
    }

    /**
     * Add all the columns except primary key columns (MdColumn instances)
     * from the table for this class to the supplied table. This includes
     * all columns for the entire flat mapped hierarchy. This method can
     * only me called if this == this.getTableClass().
     */
    public void addColumnsToTable(MdTable table) {
        if (this != getTableClass()) {
            throw new IllegalStateException("this != getTableClass()");
        }
        addPKColumnsToTable(table);
        ArrayList a = new ArrayList();
        a.add(this);
        addFlatSubclassesBreadthFirst(a);
        int n = a.size();
        for (int i = 0; i < n; i++) {
            MdClass c = (MdClass)a.get(i);
            c.addColumnsToTableImp(table);
        }
    }

    /**
     * Add only the columns for fields in this class to table excluding primary
     * key fields.
     */
    private void addColumnsToTableImp(MdTable table) {
        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            if (!f.isPrimaryKeyField()) f.addColumnsToTable(table);
        }
        for (Iterator it = fakeFields.iterator(); it.hasNext();) {
            MdField mdField = (MdField)it.next();
            MdField inv = mdField.getInverseField();
            inv.mdClass = this;
            inv.addColumnsToTable(table);
        }
    }

    private void addFlatSubclassesBreadthFirst(ArrayList a) {
        List l = getFlatSubclasses();
        a.addAll(l);
        for (Iterator i = l.iterator(); i.hasNext();) {
            MdClass c = (MdClass)i.next();
            c.addFlatSubclassesBreadthFirst(a);
        }
    }

    /**
     * Get all the reference fields of this class and its superclasses that
     * are being used as the inverse field for a one-to-many.
     */
    public List getRefFieldsUsedAsInverse() {
        List ans = new ArrayList();
        for (MdClass c = this; c != null; c = c.getPcSuperclassMdClass()) {
            c.getRefFieldsUsedAsInverseImp(ans);
        }
        return ans;
    }

    private void getRefFieldsUsedAsInverseImp(List ans) {
        int n = fieldList.size();
        for (int i = 0; i < n; i++) {
            MdField f = (MdField)fieldList.get(i);
            if (f.getCategory() != MDStatics.CATEGORY_REF) continue;
            if (f.getFieldWeAreInverseFor() != null) ans.add(f);
        }
    }

    /**
     * Get the topmost class in the heircachy for this class. If this class
     * has no superclass then this is returned.
     */
    public MdClass getTopClass() {
        for (MdClass ans = this; ;) {
            MdClass c = ans.getPcSuperclassMdClass();
            if (c == null) return ans;
            ans = c;
        }
    }

    /**
     * Get the topmost class in the same table as this class. If this class
     * has no superclass then this is returned.
     */
    public MdClass getTableClass() {
        for (MdClass ans = this; ;) {
            if (ans.isVerticalInheritance()) return ans;
            MdClass c = ans.getPcSuperclassMdClass();
            if (c == null) return ans;
            ans = c;
        }
    }

    /**
     * Is this class mapped to the same table as c?
     */
    public boolean isSameTable(MdClass c) {
        return c != null && getTopClass() == c.getTopClass();
    }

    /**
     * Get an icon to represent this class on the tree view.
     */
    public String getTreeIcon() {
        if (getPcSuperclassStr() == null) {
            return hasSubclasses() ? ICON_BASE : ICON;
        } else {
            return hasSubclasses() ? ICON_MIDDLE : ICON_LEAF;
        }
    }

    /**
     * Does this class have any subclasses?
     */
    public boolean hasSubclasses() {
        return (subclasses != null && !subclasses.isEmpty()) || isHorizontalInheritance();
    }

    /**
     * Get info on how this class is mapped (e.g. table name) for the tree
     * view.
     */
    public String getMappingInfo() {
        String s = getJdbcTableNameStr();
        if (s == null) s = getDefTableName();
        return s == null ? "" : s;
    }

    public String getName() {
        return element.getAttributeValue("name");
    }

    public void setName(String name) {
        XmlUtils.setAttribute(element, "name", name);
        analyze(true);
    }

    public String getDDLText() throws Exception {
        MdProject mdProject = getMdProject();
        JdbcStorageManagerFactory smf = mdProject.getJdbcStorageManagerFactory();
        ArrayList tables = new ArrayList();
        JdbcTable table = getTable();
        if (table != null) {
            tables.add(table);
        }
        for (Iterator it = getFieldsIncEmbAndFake().iterator(); it.hasNext();) {
            MdField mdField = (MdField)it.next();
            table = mdField.getLinkTable();
            if (table != null) {
                tables.add(table);
            }

        }
        if (tables.size() == 0) {
            return "No tables will be generated";
        }
        Collections.sort(tables);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        PrintWriter pw = new PrintWriter(out, false);
        smf.getSqlDriver().generateDDL(tables, null, pw, true);
        pw.flush();
        out.flush();
        try {
            return out.toString();
        } finally {
            out.close();
            pw.close();
        }
    }

    public RuntimeException getFirstError() {
        return classMetaData == null ? null : classMetaData.getFirstError();
    }

    public boolean hasErrors() {
        return classMetaData != null && classMetaData.hasErrors();
    }

    public String getErrorText() {
        if (classMetaData != null && classMetaData.fields != null) {
            StringBuffer errors = new StringBuffer();
            int length = classMetaData.fields.length;
            for (int i = 0; i < length; i++) {
                FieldMetaData field = classMetaData.fields[i];
                if (field.hasErrors()) {
                    errors.append(field.name);
                    errors.append(", ");
                }
            }
            if (errors.length() > 0) {
                errors.insert(0, "Fields with errors: ");
                errors.setLength(errors.length() - 2);
                return MdUtils.getErrorHtml(
                        new RuntimeException(errors.toString()));
            }
        }
        return MdUtils.getErrorHtml(getFirstError());
    }


    public void analyzeHorInh()
	{
        MdClass pcSuperMdClass = getPcSuperclassMdClassInh();
        if(pcSuperMdClass != null && pcSuperMdClass.isHorizontalInheritance()){
            String superClassName = pcSuperMdClass.getName();
            List superFieldList = pcSuperMdClass.fieldList;
            List horizInhFieldList = new ArrayList(superFieldList.size());
            int size = superFieldList.size();
            for (int i = 0; i < size; i++) {
                MdField mdField = (MdField)superFieldList.get(i);
                if(mdField instanceof MdSpecialField){
                    continue;
                }
                String name = superClassName+"."+mdField.getName();
//                MdHorizontalField hmdField = (MdHorizontalField) fieldMap.get(name);
                MdField hmdField = (MdField) fieldMap.get(name);
                if (hmdField == null) {
//                    hmdField = new MdHorizontalField();
                    hmdField = new MdField();
                    hmdField.init(this, hmdField.createElement());
                    hmdField.setName(name);
                    Field field = mdField.field;
                    if (field != null) {
                        hmdField.setField(field);
                    }
                }else{
                    removeFieldImp(hmdField);
                    Field field = mdField.field;
                    if (field != null) {
                        hmdField.setField(field);
                    }
                }
//                hmdField.setDefaultField(mdField);
                if(mdField == pcSuperMdClass.datastorePkField){
                    datastorePkField = hmdField;
                }
                horizInhFieldList.add(hmdField);
                hmdField.analyze();
            }
            treeFieldList = new ArrayList(fieldList.size()+1);
            treeFieldList.add(horizInhFieldList);
            treeFieldList.addAll(fieldList);
            for(Iterator it = horizInhFieldList.iterator(); it.hasNext();){
                MdField newField= (MdField) it.next();
                addFieldImp(newField);
            }
            List appIdFieldList = new ArrayList(superFieldList.size());
            for (Iterator it = fieldList.iterator(); it.hasNext();) {
                MdField mdField = (MdField)it.next();
                if(mdField.isPrimaryKeyField()){
                    appIdFieldList.add(mdField);
                }
            }
            pcSuperMdClass.subclasses.remove(this);
            appIdentityFields = new MdField[appIdFieldList.size()];
            appIdFieldList.toArray(appIdentityFields);
            noPCSuperclass = true;
        }else{
            treeFieldList = fieldList;
            
            noPCSuperclass = element.getAttributeValue(
                    "persistence-capable-superclass") == null;
            
        }
    }

    public List getTreeFieldList() {
        return treeFieldList;
    }
}
