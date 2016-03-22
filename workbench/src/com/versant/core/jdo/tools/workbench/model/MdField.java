
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

import com.versant.core.metadata.*;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.jdbc.metadata.*;
import com.versant.core.jdo.externalizer.SerializedExternalizer;
import com.versant.core.metadata.parser.MetaDataParser;

import java.awt.*;
import java.io.Serializable;

import java.lang.reflect.Field;


import java.util.*;
import java.util.List;

/**
 * JavaBean wrapper around XML for a field.
 */
public class MdField implements Serializable, MDStatics, JdoExtensionKeys,
        Comparable, MdColumnOwner, MdProjectProvider

{

    protected MdClass mdClass;
    protected MdElement element;
    protected MdColumn col;

    protected Field field;
    protected Class fieldType;
    protected String typeStr;
    protected String shortTypeStr;
    protected int category;

    protected MdClass refClass;

    protected FieldMetaData fmd;
    protected JdbcField jdbcField;
    private JdbcLinkCollectionField linkField;
    private JdbcTable linkTable;
    private List embeddedFields;

    public static final String ICON_CLASS_ID = "ClassID16.gif";
    public static final String ICON_PRIMARY_KEY = "PrimaryKey16.gif";
    public static final String ICON_VERSION = "Version16.gif";
    public static final String ICON_SIMPLE = "Simple16.gif";
    public static final String ICON_EXTERNALIZED = "Externalized16.gif";
    public static final String ICON_REF = "Reference16.gif";
    public static final String ICON_LINK_TABLE = "LinkTable16.gif";
    public static final String ICON_MAP = "TreeMap16.gif";
    public static final String ICON_TRANSACTIONAL = "Transactional16.gif";
    public static final String ICON_NOT_PERSISTENT = "NotPersistent16.gif";
    public static final String ICON_ONE_TO_MANY = "OneToMany16.gif";
    public static final String ICON_MANY_TO_MANY = "ManyToMany16.gif";
    public static final String ICON_POLYREF = "PolyRef16.gif";
    public static final String ICON_UNKNOWN = "Unknown16.gif";
    public static final String ICON_EMBEDDED = "Embedded16.gif";

    public MdField() {
    }

    public void init(MdClass mdClass, MdElement element) {
        this.mdClass = mdClass;
        this.element = element;
        element.setVirtualParent(mdClass.getElement());
        col = null;
        field = null;
        fieldType = null;
        typeStr = null;
        shortTypeStr = null;
        category = 0;
        refClass = null;
        fmd = null;
        jdbcField = null;
        linkField = null;
        linkTable = null;
    }

    public void init(MdClass mdClass, Field rf) {
        init(mdClass, createElement());
        setName(rf.getName());
        setField(rf);
    }

    protected MdElement createElement() {
        return new MdElement("field");
    }

    public JdbcTable getLinkTable() {
        return linkTable;
    }

    /**
     * Analyze this field including info from field found via reflection
     * (may be null).
     */
    public void analyze() {
        if (field == null) {
            fieldType = null;
            shortTypeStr = typeStr = "(unknown)";
            category = 0;
        } else {
            Class t = fieldType = field.getType();
            boolean array = t.isArray();
            if (array) t = t.getComponentType();
            shortTypeStr = typeStr = t.getName();
            int i = typeStr.lastIndexOf('.');
            if (i >= 0) {
                String pt = typeStr.substring(0, i);
                if (pt.equals(mdClass.getClsPackage())) {
                    shortTypeStr = typeStr.substring(i + 1);
                }
            }
            if (array) {
                shortTypeStr = shortTypeStr + "[]";
                typeStr = typeStr + "[]";
            }
            int pmi = getPersistenceModifierInt();
            if (pmi == 0 || pmi == MDStatics.PERSISTENCE_MODIFIER_NONE) {
                category = MDStatics.CATEGORY_NONE;
            } else {
                if (getExternalizerStr() != null) {
                    category = MDStatics.CATEGORY_EXTERNALIZED;
                } else {
                    category = mdClass.getFieldCategory(pmi, field.getType());
                }
            }
        }
        selectedDBChanged(mdClass.getSelectedDB());
    }

    /**
     * This is called by our class when the user chooses a new database
     * to work with. Switch to the matching elements.
     */
    public void selectedDBChanged(String selectedDB) {
        findCol(selectedDB);
        if(isEmbeddedRef() && embeddedFields != null){
            for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                MdEmbeddedField embeddedField = (MdEmbeddedField) it.next();
                embeddedField.selectedDBChanged(selectedDB);
            }
        }
    }

    /**
     * Find the jdbc-column element to match selectedDB if this is a field
     * mapped to a single column. Otherwise col is set to null.
     */
    private void findCol(String selectedDB) {
        boolean hasCol = true;
        switch (category) {
            case MDStatics.CATEGORY_ARRAY:
                hasCol = isEmbedded();
                break;
            case MDStatics.CATEGORY_COLLECTION:
            case MDStatics.CATEGORY_MAP:
            case MDStatics.CATEGORY_NONE:
            case MDStatics.CATEGORY_TRANSACTIONAL:
                hasCol = false;
                break;
            case MDStatics.CATEGORY_REF:
                hasCol = !isCompositePkRef() && !isEmbedded();
                break;
        }
        if (hasCol) {
            col = new MdColumn(
                    XmlUtils.findOrCreateColElement(element, selectedDB));
            col.setOwner(this);
            col.setJdbcColumn(
                    jdbcField == null
                    ? null
                    : jdbcField.mainTableCols == null ? null : jdbcField.mainTableCols[0]);
            if (category != CATEGORY_REF) {
                mdClass.getMdDataStore().updateColDefs(getMappingTypeStr(),
                        col);
            }
        } else {
            col = null;
        }
    }

    /**
     * Do second pass analysis on this field. This is only called when
     * first pass analysis has been done on all fields in all classes so
     * fields in other classes are accessable.
     */
    public void analyzePass2() {
        if (category == CATEGORY_REF) analyzePass2Ref();
    }

    /**
     * Set the defaults for col based on the referenced classes primary key.
     */
    void analyzePass2Ref() {
        refClass = mdClass.getMdProject().findClass(typeStr);
        if (isEmbeddedRef()){
            if(refClass != null){
                HashMap fields = new HashMap();
                embeddedFields = new ArrayList(refClass.getFieldList().size());
                for (Iterator i = getElement().getChildren("extension").iterator(); i.hasNext();) {
                    MdElement e = (MdElement)i.next();
                    String vn = e.getAttributeValue("vendor-name");
                    if (vn == null || !MetaDataParser.isOurVendorName(vn)) continue;
                    String k = e.getAttributeValue("key");
                    if (k != null && k.equals("field")){
                        MdEmbeddedField embField = new MdEmbeddedField(this);
                        embField.init(mdClass, e);
                        fields.put(embField.getName(), embField);
                        embeddedFields.add(embField);
                    }
                }
                for (Iterator it = refClass.getFieldList().iterator(); it.hasNext();) {
                    MdField mdField = (MdField) it.next();
                    if(mdField.isPrimaryKeyField() ||
                            mdField instanceof MdOptLockingField ||
                            mdField instanceof MdDatastorePKField ||
                            mdField instanceof MdClassIdField){
                        continue;
                    }
                    String name = mdField.getName();
                    MdEmbeddedField embField = (MdEmbeddedField) fields.get(name);
                    if (embField == null) {
                        embField = new MdEmbeddedField(this);
                        embField.init(mdClass, embField.createElement());
                        embField.setName(name);
                        embeddedFields.add(embField);
                    }
                    embField.setDefaultField(mdField);
                    Field field = mdField.field;
                    if (field != null) {
                        embField.setField(field);
                    }
                }
                for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                    MdEmbeddedField embField = (MdEmbeddedField) it.next();
                    embField.analyze();
                    embField.analyzePass2();
                }
            }
            col = null;
        };
        if (col == null) return;
        if (refClass == null) {
            col.setDef(null);
        } else {
            if (col.getTypeStr() != null) {
                MdColumn pkCol = refClass.getPkColumn();
                mdClass.getMdDataStore().updateColDefsJdbc(col,
                        pkCol == null ? null : pkCol.getType().toString());
            } else {
                col.setDef(new RefColDefaults(refClass));
            }
        }
    }

    /**
     * Get the class this field is referencing. This is null if the field
     * is not a reference.
     */
    public MdClass getRefClass() {
        return refClass;
    }

    /**
     * Get the class for the key if persistent. This is null if the field
     * is not a map.
     */
    public MdClass getKeyClass() {
        String key = getKeyTypeStr();
        if (key == null) return null;
        return mdClass.getMdPackage().findClass(key);
    }

    /**
     * Get the class for the key if persistent. This is null if the field
     * is not a map.
     */
    public MdClass getValueClass() {
        String value = getValueTypeStr();
        if (value == null) return null;
        return mdClass.getMdPackage().findClass(value);
    }

    public MdProject getMdProject() {
        return getProject();
    }

    /**
     * Defaults obtained from the primary key column of another class.
     * This does not provide defaults if the class has a composite pk.
     */
    public static class RefColDefaults extends MdColumn.Defaults {

        private MdClass c;

        public RefColDefaults(MdClass c) {
            this.c = c;
        }

        public String getType() {
            MdColumn pk = c.getPkColumn();
            return pk == null ? null : pk.getType().toString();
        }

        public String getSqlType() {
            MdColumn pk = c.getPkColumn();
            return pk == null ? null : pk.getSqlType().toString();
        }

        public String getLength() {
            MdColumn pk = c.getPkColumn();
            return pk == null ? null : pk.getLength().toString();
        }

        public String getScale() {
            MdColumn pk = c.getPkColumn();
            return pk == null ? null : pk.getScale().toString();
        }

        public String getNulls() {
            MdColumn pk = c.getPkColumn();
            return pk == null ? null : pk.getNulls().toString();
        }

        public String getShared() {
            return "false";
        }

        public String getConverter() {
            MdColumn pk = c.getPkColumn();
            return pk == null ? null : pk.getJdbcConverter().toString();
        }

    }

    /**
     * The empty status of col has changed. Attach or detach its element
     * from our element.
     */
    public void columnEmptyChanged(MdColumn c) {
    }

    /**
     * Find Collection that lists us as its inverse and has its element type
     * set to our class or null if none.
     */
    public MdField findCollectionFieldWithInverse() {
        String name = getName();
        String qName = getMdClass().getQName();
        MdClass et = getColElementTypeMdClass();
        if (et != null && name != null && qName != null) {
            List l = et.getFieldList();
            for (int i = l.size() - 1; i >= 0; i--) {
                MdField f = (MdField)l.get(i);
                String n = f.getInverseStr();
                if (n != null && n.equals(name) && qName.equals(
                        f.getElementQType())) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * The JDBC type of col has changed. Update the defaults.
     */
    public void columnTypeChanged(MdColumn col) {
        if (category != CATEGORY_REF) {
            mdClass.getMdDataStore().updateColDefs(getMappingTypeStr(), col);
        } else {
            analyzePass2Ref();
        }
    }

    public MdClass getMdClass() {
        return mdClass;
    }

    public MdElement getElement() {
        return element;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return element.getAttributeValue("name");
    }

    public void setName(String name) {
        element.setAttribute("name", name);
    }

    /**
     * Get the fully qualified name of this field.
     */
    public String getQName() {
        return mdClass.getQName() + "." + getName();
    }

    /**
     * Get the name of this field preceded by its classname without package.
     */
    public String getMiniQName() {
        return mdClass.getName() + "." + getName();
    }

    /**
     * Get the name of our class (not fully qualified) and our name or
     * just our name if our class has no PC superclass.
     */
    public String getShortQName() {
        if (mdClass.getPcSuperclassMdClass() != null) {
            return mdClass.getName() + "." + getName();
        } else {
            return getName();
        }
    }

    public String getTypeStr() {
        return typeStr;
    }

    /**
     * Get the fully qualified type of this field and its name (e.g.
     * 'java.lang.Object blah'.
     */
    public String getTypeAndName() {
        return typeStr + " " + getName();
    }

    /**
     * Get the short type of this field with added generic info if available.
     */
    public String getGenericTypeStr() {
        if (field == null) return shortTypeStr;
        if (category == MDStatics.CATEGORY_COLLECTION) {
            Class et = MetaDataUtils.getGenericElementType(field);
            if (et == null) return shortTypeStr;
            return shortTypeStr + "<" + removeJavaLang(et.getName()) + ">";
        } else if (category == MDStatics.CATEGORY_MAP) {
            Class key = MetaDataUtils.getGenericKeyType(field);
            if (key == null) return shortTypeStr;
            Class value = MetaDataUtils.getGenericValueType(field);
            if (value == null) return shortTypeStr;
            return shortTypeStr + "<" + removeJavaLang(key.getName()) + ", " +
                    removeJavaLang(value.getName()) + ">";
        } else {
            return shortTypeStr;
        }
    }

    /**
     * Remove a java.lang. prefix from s if present.
     */
    private String removeJavaLang(String s) {
        if (s.startsWith("java.lang.")) {
            return s.substring(10);
        } else {
            return s;
        }
    }

    /**
     * Get a 'Java source' fragment for this field (e.g.
     * 'public class Foo { private int bar; }'.
     */
    public String getMiniClassDef() {
        StringBuffer s = new StringBuffer();
        s.append("<html><b><code>&nbsp;&nbsp;");
        s.append(cleanupHTML(getMiniQName()));
        s.append("&nbsp;&nbsp;");
        s.append(cleanupHTML(getGenericTypeStr()));
        s.append("</code></b>");
        s.append("</body></html>");
        return s.toString();
    }

    protected String cleanupHTML(String s) {
        StringBuffer buf = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                default:
                    buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * This is the type used to resolve the JDBC mapping for this column.
     */
    public String getMappingTypeStr() {
        return getTypeStr();
    }

    public String getShortTypeStr() {
        return shortTypeStr;
    }

    protected MdValue createMdValue(String text) {
        return fix(new MdValue(text));
    }

    protected MdValue fix(MdValue v) {
        //if (isEmpty()) v.setDefaultColor(Color.gray);
        return v;
    }

    /**
     * Get the persistence-modifier for this field. If it is null then return
     * the default persistence behaviour.
     */
    public int getPersistenceModifierInt() {
        String s = getPersistenceModifierStr();
        if (s == null) {
            if (isDefaultPersistentField()) {
                return MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
            } else {
                return MDStatics.PERSISTENCE_MODIFIER_NONE;
            }
        }
        if (s.equals("persistent")) {
            return MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
        } else if (s.equals("transactional")) {
            return MDStatics.PERSISTENCE_MODIFIER_TRANSACTIONAL;
        } else if (s.equals("none")) {
            return MDStatics.PERSISTENCE_MODIFIER_NONE;
        } else {
            return 0;
        }
    }

    /**
     * Set the peristence modifier for this field. If this is the same as
     * the default then remove the persistence-modifier attribute.
     */
    public void setPersistenceModifierInt(int pm) {
        String s = MDStaticUtils.toPersistenceModifierString(pm);
        if (isDefaultPersistentField()) {
            if (pm == MDStatics.PERSISTENCE_MODIFIER_PERSISTENT) s = null;
        } else {
            if (pm == MDStatics.PERSISTENCE_MODIFIER_NONE) s = null;
        }
        XmlUtils.setAttribute(element, "persistence-modifier", s);
        analyze();
    }

    public String getPersistenceModifierStr() {
        return element.getAttributeValue("persistence-modifier");
    }

    public MdValue getPersistenceModifier() {
        MdValue v = createMdValue(getPersistenceModifierStr());
        v.setPickList(PickLists.PERSISTENCE_MODIFIER);
        v.setDefText("persistent");
        return v;
    }

    public void setPersistenceModifier(MdValue v) {
        XmlUtils.setAttribute(element, "persistence-modifier", v.getText());
        analyze();
    }

    /**
     * Can this field be used as part of the primary key?
     */
    public boolean isPossiblePrimaryKeyField() {
        return category == MDStatics.CATEGORY_SIMPLE;
    }

    public boolean isPrimaryKeyField() {

        String s = getPrimaryKeyStr();
        return s != null && s.equals("true");


	}



    public String getPrimaryKeyStr() {
        return element.getAttributeValue("primary-key");
    }

    public void setPrimaryKeyStr(String s) {
        XmlUtils.setAttribute(element, "primary-key", s);
        if (isPrimaryKeyField()) setAutoSetStr(null);
    }

    public MdValue getPrimaryKey() {
        MdValue v = createMdValue(getPrimaryKeyStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setPrimaryKey(MdValue v) {
        setPrimaryKeyStr(v.getText());
    }

    public String getNullValueStr() {
        return element.getAttributeValue("null-value");
    }

    public void setNullValueStr(String s) {
        XmlUtils.setAttribute(element, "null-value", s);
    }

    public MdValue getNullValue() {
        MdValue v = createMdValue(getNullValueStr());
        v.setPickList(PickLists.NULL_VALUE);
        v.setDefText("default");
        return v;
    }

    public void setNullValue(MdValue v) {
        setNullValueStr(v.getText());
    }

    public String getDefaultFetchGroupStr() {
        return element.getAttributeValue("default-fetch-group");
    }

    public MdValue getDefaultFetchGroup() {
        MdValue v = createMdValue(getDefaultFetchGroupStr());
        v.setPickList(PickLists.BOOLEAN);
        String dt;
        if (field != null) {
            dt = isDFGFieldByDefault() ? "true" : "false";
        } else {
            dt = "maybe";
        }
        v.setDefText(dt);
        return v;
    }

    /**
     * Should this field be considered persistent by default?
     */
    public boolean isDefaultPersistentField() {
        if (field == null) return true;
        return mdClass.getMdProject().isDefaultPersistentField(
                mdClass.getMdDataStore().getName(), field);
    }

    /**
     * Does this field default to being in the default fetch group (i.e. no
     * default-fetch-group attribute present)?
     */
    public boolean isDFGFieldByDefault() {
        if (field == null) return false;
        return mdClass.getMdutils().isDefaultFetchGroupType(field.getType());
    }

    public void setDefaultFetchGroup(MdValue v) {
        setDefaultFetchGroupStr(v.getText());
    }

    public void setDefaultFetchGroupStr(String s) {
        XmlUtils.setAttribute(element, "default-fetch-group", s);
    }


    public String getEmbeddedStr() {
        return element.getAttributeValue("embedded");
    }

    public MdValue getEmbedded() {
        MdValue v = createMdValue(getEmbeddedStr());
        if (isAlwaysEmbedded()) {
            v.setPickList(PickLists.TRUE);
        } else {
            v.setPickList(PickLists.BOOLEAN);
        }
        v.setDefText(isDefaultEmbedded() ? "true" : "false");
        return v;
    }

    public void setEmbedded(MdValue v) {
        setEmbeddedStr(v.getText());
    }

    protected void setEmbeddedStr(String s) {
        XmlUtils.setAttribute(element, "embedded", s);
    }

    protected String getEmbeddedStrForBool(boolean embedded) {
        if (isAlwaysEmbedded()) {
            if (isDefaultEmbedded()) {
                return null;
            } else {
                return "true";
            }
        } else if (embedded == isDefaultEmbedded()) {
            return null;
        } else {
            return embedded ? "true" : "false";
        }
    }

    public void setEmbedded(boolean embedded) {
        setEmbeddedStr(getEmbeddedStrForBool(embedded));
        if(isEmbeddedRef()){
            getProject().syncAllClassesAndInterfaces();
        }else{
            if(embeddedFields != null){
                for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                    MdEmbeddedField embeddedField = (MdEmbeddedField) it.next();
                    getElement().removeContent(embeddedField.getElement());
                }
                embeddedFields = null;
            }
        }
    }


    /**
     * Is this field embedded (i.e. specified or by default)?
     */
    public boolean isEmbedded() {
        if (isAlwaysEmbedded()) {
            return true;
        }
        if (isDefaultEmbedded()) {
            return true;
        }

        String s = getEmbeddedStr();


        return s != null && s.equals("true");
    }

    public List getEmbeddedFields(){
        return embeddedFields;
    }

    public boolean isEmbeddedRef() {
        return isEmbedded() && category == MDStatics.CATEGORY_REF;
    }

    /**
     * Is this field is embedded by default?
     */
    public boolean isDefaultEmbedded() {
        return fieldType != null
                && mdClass.getMdProject().isEmbeddedField(fieldType);
    }

    /**
     * Must this field be embedded?
     */
    public boolean isAlwaysEmbedded() {
        return category == CATEGORY_ARRAY
                && (getFieldType().getComponentType().isPrimitive());
    }

    /**
     * Is this field type valid for autoset?
     */
    public boolean isValidAutoSetField() {
        if (category != CATEGORY_SIMPLE) return false;
        return !isPrimaryKeyField() && (isJavaUtilDate() || isNumericAutoSetType());
    }

    /**
     * Is the type of this field 'java.util.Date'?
     */
    public boolean isJavaUtilDate() {
        return typeStr != null && typeStr.equals("java.util.Date");
    }

    /**
     * Is the type of this field a valid type for a numeric autoset field?
     */
    public boolean isNumericAutoSetType() {
        return typeStr != null && (typeStr.equals("int")
                || typeStr.equals("short") || typeStr.equals("byte"));
    }

    public boolean isAutoSetCreated() {
        String s = getAutoSetStr();
        return s != null && (s.equals("created") || s.equals("both"));
    }

    public void setAutoSetCreated(boolean on) {
        if (on == isAutoSetCreated()) return;
        String s = getAutoSetStr();
        if (on) {
            if ("modified".equals(s)) {
                s = "both";
            } else {
                s = "created";
            }
        } else {
            if ("both".equals(s)) {
                s = "modified";
            } else {
                s = null;
            }
        }
        setAutoSetStr(s);
    }

    public boolean isAutoSetModified() {
        String s = getAutoSetStr();
        return s != null && (s.equals("modified") || s.equals("both"));
    }

    public void setAutoSetModified(boolean on) {
        if (on == isAutoSetModified()) return;
        String s = getAutoSetStr();
        if (on) {
            if ("created".equals(s)) {
                s = "both";
            } else {
                s = "modified";
            }
        } else {
            if ("both".equals(s)) {
                s = "created";
            } else {
                s = null;
            }
        }
        setAutoSetStr(s);
    }

    public String getAutoSetStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.AUTOSET);
    }

    public void setAutoSetStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.AUTOSET, s);
    }

    public MdValue getAutoSet() {
        MdValue v = createMdValue(getAutoSetStr());
        v.setPickList(PickLists.AUTOSET);
        v.setDefText("no");
        return v;
    }

    public void setAutoSet(MdValue v) {
        setAutoSetStr(v.getText());
    }

    public MdValue getNullIfNotFound() {
        MdValue v = createMdValue(XmlUtils.getExtension(element,
                JdoExtensionKeys.NULL_IF_NOT_FOUND));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setNullIfNotFound(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.NULL_IF_NOT_FOUND,
                v.getText());
    }

    public String getDependentStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.DEPENDENT);
    }

    public boolean getDependentBool() {
        String s = getDependentStr();
        return s != null && s.equals("true");
    }

    public MdValue getDependent() {
        MdValue v = createMdValue(getDependentStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setDependent(MdValue v) {
        setDependentStr(v.getText());
    }

    public void setDependentStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.DEPENDENT, s);
    }

    public String getKeysDependentStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.KEYS_DEPENDENT);
    }

    public boolean getKeysDependentBool() {
        String s = getKeysDependentStr();
        return s != null && s.equals("true");
    }

    public MdValue getKeysDependent() {
        MdValue v = createMdValue(getKeysDependentStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setKeysDependent(MdValue v) {
        setKeysDependentStr(v.getText());
    }

    public void setKeysDependentStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.KEYS_DEPENDENT, s);
    }

    public String getColElementTypeStrRaw() {

        return XmlUtils.getAttribute(element, "collection", "element-type");


    }

    /**
     * Get the element type for a collection returning the JDK 1.5 generic type
     * if nothing is filled in and the generic type is available.
     */
    public String getColElementTypeStr() {
        String s = getColElementTypeStrRaw();
        if (field == null || s != null) return s;
        Class gt = MetaDataUtils.getGenericElementType(field);
        return gt == null ? null : gt.getName();
    }

    public String getArrayElementTypeStr() {
        if (field == null) return null;
        Class t = field.getType().getComponentType();
        return t == null ? null : t.getName().toString();
    }

    public MdValue getColElementType() {
        String et = getColElementTypeStrRaw();
        Class gt = field == null ? null : MetaDataUtils.getGenericElementType(
                field);
        if (gt == null) {
            return new MdElementValue(et, mdClass.getMdPackage());
        } else {
            return new MdGenericValue(et, mdClass.getMdPackage(), gt);
        }
    }

    public void setColElementType(MdValue v) {


        XmlUtils.setAttribute(element, "collection", "element-type",
                v.getText());

        setColInverseStr(null);
        if (getColElementTypeMdClass() == null) {
            // not a pc element-type so dependent is not allowed
            setDependentStr(null);
        }
    }

    /**
     * Get the MdClass for the element type of a collection if it is a PC class
     * or null if not.
     */
    public MdClass getColElementTypeMdClass() {
        String n = getColElementTypeStr();
        return n == null ? null : getMdClass().findClass(n);
    }

    public MdClass getElementTypeMdClass() {
        String n = getElementTypeStr();
        return n == null ? null : getMdClass().findClass(n);
    }

    public String getColEmbeddedElementStr() {
        return XmlUtils.getAttribute(element, "collection",
                        "embedded-element");
    }

    public void setColEmbeddedElementStr(String s) {
        XmlUtils.setAttribute(element, "collection", "embedded-element", s);
    }

    public MdValue getColEmbeddedElement() {
        MdValue v = createMdValue(getColEmbeddedElementStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setColEmbeddedElement(MdValue v) {
        setColEmbeddedElementStr(v.getText());
    }

    public String getKeyQType() {
        String s = getKeyTypeStr();
        if (s == null || s.indexOf('.') >= 0) return s;
        return getMdClass().getMdPackage().getName() + "." + s;
    }

    public String getKeyTypeStrRaw() {

        return XmlUtils.getAttribute(element, "map", "key-type");


    }

    public String getKeyTypeStr() {
        String s = getKeyTypeStrRaw();
        if (field == null || s != null) return s;
        Class gt = MetaDataUtils.getGenericKeyType(field);
        return gt == null ? null : gt.getName();
    }

    public MdValue getKeyType() {
        String kt = getKeyTypeStrRaw();
        Class gt = field == null ? null : MetaDataUtils.getGenericKeyType(
                field);
        if (gt == null) {
            return new MdElementValue(kt, mdClass.getMdPackage());
        } else {
            return new MdGenericValue(kt, mdClass.getMdPackage(), gt);
        }
    }

    public void setKeyType(MdValue v) {

        XmlUtils.setAttribute(element, "map", "key-type", v.getText());


    }

    public String getEmbeddedKeyStr() {
        return XmlUtils.getAttribute(element, "map", "embedded-key");
    }

    public void setEmbeddedKeyStr(String s) {
        XmlUtils.setAttribute(element, "map", "embedded-key", s);
    }

    public MdValue getEmbeddedKey() {
        MdValue v = createMdValue(getEmbeddedKeyStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setEmbeddedKey(MdValue v) {
        setEmbeddedKeyStr(v.getText());
    }

    public String getValueTypeStrRaw() {

        return XmlUtils.getAttribute(element, "map", "value-type");


    }

    public String getValueTypeStr() {
        String s = getValueTypeStrRaw();
        if (field == null || s != null) return s;
        Class gt = MetaDataUtils.getGenericValueType(field);
        return gt == null ? null : gt.getName();
    }

    public MdValue getValueType() {
        String et = getValueTypeStrRaw();
        Class vt = field == null ? null : MetaDataUtils.getGenericValueType(
                field);
        if (vt == null) {
            return new MdElementValue(et, mdClass.getMdPackage());
        } else {
            return new MdGenericValue(et, mdClass.getMdPackage(), vt);
        }
    }

    public void setValueTypeStr(String s) {
        XmlUtils.setAttribute(element, "map", "value-type", s);
    }

    public void setValueType(MdValue v) {

        setValueTypeStr(v.getText());


    }

    public String getEmbeddedValueStr() {
        return XmlUtils.getAttribute(element, "map", "embedded-value");
    }

    public void setEmbeddedValueStr(String s) {
        XmlUtils.setAttribute(element, "map", "embedded-value", s);
    }

    public MdValue getEmbeddedValue() {
        MdValue v = createMdValue(getEmbeddedValueStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setEmbeddedValue(MdValue v) {
        setEmbeddedValueStr(v.getText());
    }

    public String getArrayEmbeddedElementStr() {
        return XmlUtils.getAttribute(element, "array", "embedded-element");
    }

    public void setArrayEmbeddedElementStr(String s) {
        XmlUtils.setAttribute(element, "array", "embedded-element", s);
    }

    public MdValue getArrayEmbeddedElement() {
        MdValue v = createMdValue(getArrayEmbeddedElementStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setArrayEmbeddedElement(MdValue v) {
        setArrayEmbeddedElementStr(v.getText());
    }

    /**
     * Is the field a collection that is ordered by default?
     */
    public boolean getColOrderedDefault() {
        return category == MDStatics.CATEGORY_COLLECTION
                && fieldType != null

                && List.class.isAssignableFrom(fieldType);


    }

    public boolean getColOrderedBool() {
        String s = getColOrderedStr();
        if (s == null) return getColOrderedDefault();
        return s.equals("true");
    }

    public String getColOrderedStr() {
        return XmlUtils.getExtension(element, "collection",
                JdoExtensionKeys.ORDERED);
    }

    public String getArrayOrderedStr() {
        return XmlUtils.getExtension(element, "array",
                JdoExtensionKeys.ORDERED);
    }

    public void setColOrderedStr(String s) {
        XmlUtils.setExtension(element, "collection",
                JdoExtensionKeys.ORDERED, s);
    }

    public void setArrayOrderedStr(String s) {
        XmlUtils.setExtension(element, "array",
                JdoExtensionKeys.ORDERED, s);
    }

    public void setOrderedStr(String s) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColOrderedStr(s);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayOrderedStr(s);
                break;
            default:
                return;
        }
    }

    public String getOrderedStr() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColOrderedStr();
            case MDStatics.CATEGORY_ARRAY:
                return getArrayOrderedStr();
            default:
                return null;
        }
    }

    private MdValue getColOrdered() {
        MdValue v = createMdValue(getColOrderedStr());
        boolean od = getColOrderedDefault();
        v.setPickList(od ? PickLists.BOOLEAN : PickLists.FALSE);
        v.setDefText(od ? "true" : "false");
        return v;
    }

    private void setColOrdered(MdValue v) {
        setColOrderedStr(v.getText());
    }

    public String getColInverseStr() {
        return XmlUtils.getExtension(element, "collection",
                JdoExtensionKeys.INVERSE);
    }

    public void setColInverseStr(String s) {
        XmlUtils.setExtension(element, "collection",
                JdoExtensionKeys.INVERSE, s);
    }

    public void setInverseStr(String s) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColInverseStr(s);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayInverseStr(s);
                break;
            default:
                return;
        }
    }

    public void setArrayInverseStr(String s) {
        XmlUtils.setExtension(element, "array",
                JdoExtensionKeys.INVERSE, s);
    }

    private MdValue getColInverse() throws Exception {
        String et = getColElementTypeStr();
        return new MdFieldValue(getColInverseStr(),
                et == null ? null : mdClass.getMdPackage().findClass(et),
                mdClass.getQName(), true);
    }

    private void setColInverse(MdValue v) {
        setColInverseStr(v.getText());
    }

    public MdValue getInverseOneToMany() throws Exception {
        MdValue mdFieldValue = new MdValue(getInverseStr());
        List newPl = new ArrayList(10);
        newPl.add(FieldMetaData.NO_FIELD_TEXT);
        for (Iterator it = getPossibleOneToManyInverseFields().iterator(); it.hasNext();) {
            MdField mdField = (MdField) it.next();
            newPl.add(mdField.getName());
        }
        mdFieldValue.setPickList(newPl);
        return mdFieldValue;
    }
    /**
     * Find all the fields that could be used as our inverse for a one-to-many
     * mapping. This will include fields that have already been used as
     * an inverse for another field.
     */
    public List getPossibleOneToManyInverseFields() {
        ArrayList ans = new ArrayList();
        MdClass ourClass = getMdClass();
        for (MdClass et = getElementTypeMdClass(); et != null;
             et = et.getPcSuperclassMdClass()) {
            List list = et.getFieldList();
            int n = list.size();
            for (int i = 0; i < n; i++) {
                MdField f = (MdField)list.get(i);
                if (f.getCategory() == MDStatics.CATEGORY_REF
                        && f.getRefClass() == ourClass
                        && !f.isEmbeddedRef()) {
                    MdField inv = f.getFieldWeAreInverseFor();
                    if (inv == null || inv == this) ans.add(f);
                }
            }
        }
        return ans;
    }

    public void setInverseOneToMany(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColInverse(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayInverse(v);
                break;
            default:
                return;
        }
    }

    public void setInverseOneToMany(String v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColInverseStr(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayInverseStr(v);
                break;
            default:
                return;
        }
    }

    /**
     * Label to use for colInverseOneToMany property.
     */
    public String getColInverseOneToManyLabel() throws Exception {
        String etn;
        MdClass elementType = getElementTypeMdClass();
        if (elementType != null) {
            etn = elementType.getName();
        } else {
            etn = getElementTypeStr();
            if (etn == null) etn = "element-type";
        }
        return "Reference field on " + etn + " to use to complete the collection";
    }

    /**
     * Get lable for field on the other side of a many-to-many. This works on
     * both the inverse and non-inverse sides.
     *
     * @see #getOtherSideOfManyToMany
     */
    public String getOtherSideOfManyToManyLabel() throws Exception {
        MdClass c = getElementTypeMdClass();
        String etc = c == null ? getElementTypeStr() : c.getName();
        return "Collection field on " + etc + " side of the many-to-many";
    }

    /**
     * Get field on the other side of a many-to-many. This works on both the
     * inverse and non-inverse sides.
     *
     * @see #getOtherSideOfManyToManyLabel
     */
    public MdValue getOtherSideOfManyToMany() throws Exception {
        String fieldName = getColInverseStr();
        if (fieldName == null) {
            // we are non-inverse side so find the field on our element-type
            // that has us listed as its inverse
            MdField f = findCollectionFieldWithInverse();
            fieldName = f == null ? null : f.getName();
        }
        String et = getColElementTypeStr();
        return new MdFieldValue(fieldName,
                et == null ? null : mdClass.getMdPackage().findClass(et),
                mdClass.getQName(), true);
    }

    public void setOtherSideOfManyToMany(MdValue v) {
        String fieldName = v.getText();
        if (getColInverseStr() != null) {
            // we are inverse side so just change the inverse extension
            setColInverseStr(fieldName);
        } else {
            // we are the non-inverse side so change the inverse extension
            // for the old field listing us as inverse (if any) and for
            // the new field (if any)
            MdClass et = getColElementTypeMdClass();
            MdField nw = et == null ? null : et.findField(fieldName);
            if (nw == null) {
                throw new MdVetoException("Field '" + fieldName + "' not found " +
                        "on " + (et == null ? "element-type" : et.getName()));
            }
            MdField old = findCollectionFieldWithInverse();
            if (old != null) old.setColInverseStr(null);
            nw.setColInverseStr(getName());
        }
    }

    /**
     * Get the inverse field for this collection or null if none.
     */
    private MdField getColInverseField() {
        String s = getColElementTypeStr();
        if (s == null) return null;
        MdClass c = mdClass.findClass(s);
        if (c == null) return null;
        s = getColInverseStr();
        return s == null ? null : c.findField(s);
    }

    private MdField getArrayInverseField() {
        String s = getArrayElementTypeStr();
        if (s == null) return null;
        MdClass c = mdClass.findClass(s);
        if (c == null) return null;
        s = getArrayInverseStr();
        return s == null ? null : c.findField(s);
    }

    public String getInverseJdbcIndexStr() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColInverseJdbcIndexStr();
            case MDStatics.CATEGORY_ARRAY:
                return getArrayInverseJdbcIndexStr();
            default:
                return null;
        }
    }

    public void setInverseJdbcIndexStr(String s) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColInverseJdbcIndexStr(s);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayInverseJdbcIndexStr(s);
                break;
            default:
                return;
        }
    }

    public String getColInverseJdbcIndexStr() {
        return XmlUtils.getExtension(element, "collection",
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX);
    }

    public String getArrayInverseJdbcIndexStr() {
        return XmlUtils.getExtension(element, "array",
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX);
    }

    protected void setArrayInverseJdbcIndexStr(String s) {
        XmlUtils.setExtension(element, "array",
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX, s);
    }

    protected void setColInverseJdbcIndexStr(String s) {
        XmlUtils.setExtension(element, "collection",
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX, s);
    }

    public MdValue getColInverseJdbcIndex() {
        MdValue v = new MdValue(getColInverseJdbcIndexStr());
        v.setPickList(PickLists.NO);
        v.setDefText("{auto}");
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setColInverseJdbcIndex(MdValue v) {
        setColInverseJdbcIndexStr(v.getText());
    }

    /**
     * Get the inverse index setting for a many-to-many. This works even if
     * we are not the inverse side.
     */
    public MdValue getColInverseJdbcIndexMM() {
        if (getColInverseStr() != null) {
            // we are the inverse side
            return getColInverseJdbcIndex();
        } else {
            MdField inv = getFieldWeAreInverseFor();
            return inv == null ? null : inv.getColInverseJdbcIndex();
        }
    }

    /**
     * Set the inverse index setting for a many-to-many. This works even if
     * we are not the inverse side.
     */
    public void setColInverseJdbcIndexMM(MdValue v) {
        if (getColInverseStr() != null) {
            // we are the inverse side
            setColInverseJdbcIndex(v);
        } else {
            MdField inv = getFieldWeAreInverseFor();
            if (inv != null) inv.setColInverseJdbcIndex(v);
        }
    }

    public String getJdbcColLinkTableNameStr() {
        return XmlUtils.getExtension(element, "collection",
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME);
    }

    public MdValue getJdbcColLinkTableName() {
        MdValue v = createMdValue(getJdbcColLinkTableNameStr());
        v.setDefText(linkTable == null ? null : linkTable.name);
        v.setOnlyFromPickList(false);
        if (MdClass.getDatabaseMetaData() != null) {
            v.setCaseSensitive(false);
            v.setWarningOnError(true);
            v.setOnlyFromPickList(true);
            v.setPickList(MdClass.getDatabaseMetaData().getAllTableNames());
        }
        return v;
    }

    /**
     * Get the standalone (i.e. not under collection or array) link table
     * extension or the link table extension nested under a collection
     * element if any.
     */
    public MdElement getLinkTableElement() {
        MdElement ans = XmlUtils.findExtension(element,
                JdoExtensionKeys.JDBC_LINK_TABLE);
        if (ans == null) {
            MdElement ce = (MdElement)element.getChild("collection");
            if (ce != null) {
                ans = XmlUtils.findExtension(ce,
                        JdoExtensionKeys.JDBC_LINK_TABLE);
            }
        }
        return ans;
    }

    public void setJdbcColLinkTableName(MdValue v) {
        XmlUtils.setExtension(element, "collection",
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME,
                v.getText());
    }

    public MdValue getArrayOrdered() {
        MdValue v = createMdValue(getArrayOrderedStr());
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("true");
        return v;
    }

    public void setArrayOrdered(MdValue v) {
        setArrayOrderedStr(v.getText());
    }

    public String getArrayInverseStr() {
        return XmlUtils.getExtension(element, "array",
                JdoExtensionKeys.INVERSE);
    }

    public MdValue getArrayInverse() throws Exception {
        String et = getArrayElementTypeStr();
        return new MdFieldValue(getArrayInverseStr(),
                et == null ? null : mdClass.getMdPackage().findClass(et),
                mdClass.getQName(), true);
    }

    public void setArrayInverse(MdValue v) {
        setArrayInverseStr(v.getText());
    }

    public MdValue getArrayInverseJdbcIndex() {
        MdValue v = new MdValue(getArrayInverseJdbcIndexStr());
        v.setPickList(PickLists.NO);
        v.setDefText("{auto}");
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setArrayInverseJdbcIndex(MdValue v) {
        setArrayInverseJdbcIndexStr(v.getText());
    }

    public String getJdbcArrayLinkTableNameStr() {
        return XmlUtils.getExtension(element, "array",
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME);
    }

    public MdValue getJdbcArrayLinkTableName() {
        MdValue v = createMdValue(getJdbcArrayLinkTableNameStr());
        v.setDefText(linkTable == null ? null : linkTable.name);
        v.setOnlyFromPickList(false);
        if (MdClass.getDatabaseMetaData() != null) {
            v.setCaseSensitive(false);
            v.setWarningOnError(true);
            v.setOnlyFromPickList(true);
//            if (v.text != null) {
//                v.setPickList(MdClass.getDatabaseMetaData().getTableNames(v.text));
//            } else {
//                v.setPickList(MdClass.getDatabaseMetaData().getTableNames(v.defText));
//            }
            v.setPickList(MdClass.getDatabaseMetaData().getAllTableNames());
        }
        return v;
    }

    public void setJdbcArrayLinkTableName(MdValue v) {
        XmlUtils.setExtension(element, "array",
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME,
                v.getText());
    }

    public String getJdbcMapLinkTableNameStr() {
        return XmlUtils.getExtension(element, "map",
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME);
    }

    public MdValue getJdbcMapLinkTableName() {
        MdValue v = createMdValue(getJdbcMapLinkTableNameStr());
        v.setDefText(linkTable == null ? null : linkTable.name);
        v.setOnlyFromPickList(false);
        if (MdClass.getDatabaseMetaData() != null) {
            v.setCaseSensitive(false);
            v.setWarningOnError(true);
            v.setOnlyFromPickList(true);
//            if (v.text != null) {
//                v.setPickList(MdClass.getDatabaseMetaData().getTableNames(v.text));
//            } else {
//                v.setPickList(MdClass.getDatabaseMetaData().getTableNames(v.defText));
//            }
            v.setPickList(MdClass.getDatabaseMetaData().getAllTableNames());
        }
        return v;
    }

    public void setJdbcMapLinkTableName(MdValue v) {
        XmlUtils.setExtension(element, "map",
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME,
                v.getText());
    }

    public void setJdbcUseJoin(MdValue v) {
        setJdbcUseJoinStr(v.getText());
    }

    public void setJdbcUseJoinStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.JDBC_USE_JOIN, s);
    }

    public String getJdbcUseJoinStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.JDBC_USE_JOIN);
    }

    public MdValue getJdbcUseJoin() {
        MdValue v = createMdValue(getJdbcUseJoinStr());
        v.setPickList(PickLists.JDBC_USE_JOIN);
        String dt;
        if (category == MDStatics.CATEGORY_REF) {
            String dfg = getDefaultFetchGroupStr();
            if (dfg != null && dfg.equals("true")) {
                dt = "outer";
            } else {
                if (refClass == null) {
                    dt = "no";
                } else {
                    dt = refClass.getJdbcUseJoinStr();
                    if (dt == null) dt = "outer";
                }
            }
        } else {
            dt = "no";
        }
        v.setDefText(dt);
        return v;
    }

    public MdValue getFetchGroup() {
        MdValue v = createMdValue(XmlUtils.getExtension(element, FETCH_GROUP));
        List dsl = mdClass.getFetchGroupList();
        int n = dsl.size();
        ArrayList a = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            MdFetchGroup g = (MdFetchGroup)dsl.get(i);
            a.add(g.getName());
        }
        v.setPickList(a);
        return v;
    }

    public void setFetchGroup(MdValue v) {
        setFetchGroupStr(v.getText());
    }

    public void setFetchGroupStr(String s) {
        XmlUtils.setExtension(element, FETCH_GROUP, s);
    }

    public String getJdbcConstraintStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.JDBC_CONSTRAINT);
    }

    public MdValue getJdbcConstraint() {
        MdValue v = createMdValue(getJdbcConstraintStr());
        v.setPickList(PickLists.NO);
        v.setDefText("auto");
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setJdbcConstraint(MdValue v) {
        setJdbcConstraintStr(v.getText());
    }

    public void setJdbcConstraintStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.JDBC_CONSTRAINT, s);
    }

    /**
     * Order by name.
     */
    public int compareTo(Object o) {
        MdField f = (MdField)o;

        if (isPrimaryKeyField()) {
            if (!f.isPrimaryKeyField()) return -1;
        } else if (f.isPrimaryKeyField()) {
            return 1;
        }

        String a = getName();
        String b = f.getName();
        if (a == null) {
            if (b == null) return 0;
            return -1;
        } else {
            if (b == null) return 1;
            return a.compareTo(b);
        }
    }

    public int getCategory() {
        return category;
    }

    /**
     * Get the category of this field has when it is persistent. This is
     * useful to edit fields that have been marked as not-persistent or
     * transactional.
     */
    public int getCategoryWhenPersistent() {
        return mdClass.getFieldCategory(
                MDStatics.PERSISTENCE_MODIFIER_PERSISTENT,
                field.getType());
    }

    public String getCategoryStr() {
        if (category == 0) {
            return "?";
        } else {
            return MDStaticUtils.toCategoryString(category);
        }
    }

    /**
     * Get fully qualified name of our element-type or null if none.
     */
    public String getElementQType() {
        String s = getElementTypeStr();
        if (s == null || s.indexOf('.') >= 0) return s;
        return getMdClass().getMdPackage().getName() + "." + s;
    }

    public String getElementTypeStr() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColElementTypeStr();
            case MDStatics.CATEGORY_MAP:
                return getValueTypeStr();
            case MDStatics.CATEGORY_ARRAY:
                return getArrayElementTypeStr();
        }
        return null;
    }

    public MdValue getElementType() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColElementType();
            case MDStatics.CATEGORY_MAP:
                return getValueType();
        }
        return MdValue.NA;
    }

    public void setElementType(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColElementType(v);
                break;
            case MDStatics.CATEGORY_MAP:
                setValueType(v);
                break;
        }
    }

    /**
     * Get the collection, array or map element for this field depending on
     * the type of the field. Returns null if none found or not a collection,
     * array or map.
     */
    public MdElement getCollectionArrayOrMapElement() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return (MdElement)element.getChild("collection");
            case MDStatics.CATEGORY_ARRAY:
                return (MdElement)element.getChild("array");
            case MDStatics.CATEGORY_MAP:
                return (MdElement)element.getChild("map");
        }
        return null;
    }

    public MdValue getEmbeddedElement() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColEmbeddedElement();
            case MDStatics.CATEGORY_ARRAY:
                return getArrayEmbeddedElement();
            case MDStatics.CATEGORY_MAP:
                return getEmbeddedValue();
        }
        return MdValue.NA;
    }

    public void setEmbeddedElement(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColEmbeddedElement(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayEmbeddedElement(v);
                break;
            case MDStatics.CATEGORY_MAP:
                setEmbeddedValue(v);
                break;
        }
    }

    public MdValue getOrdered() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColOrdered();
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return MdValue.NA;
                return getArrayOrdered();
        }
        return MdValue.NA;
    }

    public void setOrdered(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColOrdered(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayOrdered(v);
                break;
        }
    }

    public MdValue getInverse() throws Exception {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColInverse();
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) {
                    return MdValue.NA;
                }
                return getArrayInverse();
        }
        return MdValue.NA;
    }

    public MdField getInverseField() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                MdField inv = getColInverseField();
                if (inv == null) {
                    inv = new FakeOne2ManyField(this);
                }
                return inv;
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return null;
                return getArrayInverseField();
        }
        return null;
    }

    public String getInverseStr() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColInverseStr();
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) {
                    return null;
                }
                return getArrayInverseStr();
        }
        return null;
    }

    public void setInverse(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColInverse(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayInverse(v);
                break;
        }
    }

    public MdValue getInverseJdbcIndex() throws Exception {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColInverseJdbcIndex();
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return MdValue.NA;
                return getArrayInverseJdbcIndex();
        }
        return MdValue.NA;
    }

    public void setInverseJdbcIndex(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColInverseJdbcIndex(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayInverseJdbcIndex(v);
                break;
        }
    }

    public String getJdbcLinkTableNameStr() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getJdbcColLinkTableNameStr();
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return null;
                return getJdbcArrayLinkTableNameStr();
            case MDStatics.CATEGORY_MAP:
                return getJdbcMapLinkTableNameStr();
        }
        return null;
    }

    public MdValue getJdbcLinkTableName() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getJdbcColLinkTableName();
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return MdValue.NA;
                return getJdbcArrayLinkTableName();
            case MDStatics.CATEGORY_MAP:
                return getJdbcMapLinkTableName();
        }
        return MdValue.NA;
    }

    public void setJdbcLinkTableName(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setJdbcColLinkTableName(v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setJdbcArrayLinkTableName(v);
                break;
            case MDStatics.CATEGORY_MAP:
                setJdbcMapLinkTableName(v);
                break;
        }
    }

    /**
     * If the selectedDB of our class is not null and our col is for all
     * databases then set a copy of it for the selectedDB. This should
     * be called before all write operations to col.
     */
    private void checkColDb() {
        String sdb = mdClass.getSelectedDB();
        if (sdb == null || col == null) return;
        if (col.getDb() == null) {
            col = new MdColumn(col);
            col.setOwner(this);
            col.setDb(sdb);
        }
    }

    public String getColumnDefName() {
        return col == null ? null : col.getDefName();
    }

    public String getColumnNameStr() {
        return col == null ? null : col.getNameStr();
    }

    public MdValue getColumnName() {
        if (col == null) return MdValue.NA;
        MdValue columnName = fix(col.getName());
        if (MdClass.getDatabaseMetaData() != null) {
            columnName.setCaseSensitive(false);
            columnName.setWarningOnError(true);
            columnName.setOnlyFromPickList(true);
            String tableName = mdClass.getJdbcTableNameStr();
            if (tableName == null) {
                tableName = mdClass.getDefTableName();
            }
            columnName.setPickList(
                    MdClass.getDatabaseMetaData().getAllColumnNames(tableName));
        }
        return columnName;
    }

    public void setColumnName(MdValue v) {
        checkColDb();
        col.setName(v);
    }

    public MdValue getJdbcType() {
        if (col == null) return MdValue.NA;
        return fix(col.getType());
    }

    public void setJdbcType(MdValue v) {
        checkColDb();
        col.setType(v);
    }

    public MdValue getSqlType() {
        if (col == null) return MdValue.NA;
        return fix(col.getSqlType());
    }

    public void setSqlType(MdValue v) {
        checkColDb();
        col.setSqlType(v);
    }

    public String getSqlDDL() {
        if (col == null) return "";
        return col.getSqlDDL();
    }

    public MdValue getLength() {
        if (col == null) return MdValue.NA;
        return fix(col.getLength());
    }

    public void setLength(MdValue v) {
        checkColDb();
        col.setLength(v);
    }

    public MdValue getScale() {
        if (col == null) return MdValue.NA;
        return fix(col.getScale());
    }

    public void setScale(MdValue v) {
        checkColDb();
        col.setScale(v);
    }

    public MdValue getConverter() {
        if (col == null) return MdValue.NA;
        return fix(col.getJdbcConverter());
    }

    public void setConverter(MdValue v) {
        checkColDb();
        col.setJdbcConverter(v);
    }

    public MdValue getNulls() {
        if (col == null) return MdValue.NA;
        return fix(col.getNulls());
    }

    public void setNulls(MdValue v) {
        checkColDb();
        col.setNulls(v);
    }

    public MdValue getShared() {
        if (col == null) return MdValue.NA;
        return fix(col.getShared());
    }

    public void setShared(MdValue v) {
        checkColDb();
        col.setShared(v);
    }

    /**
     * Is this field a reference to a class with a composite primary key?
     */
    public boolean isCompositePkRef() {
        MdClass rc = mdClass.getMdProject().findClass(typeStr);
        return category == MDStatics.CATEGORY_REF
                && rc != null
                && rc.isCompositePrimaryKey();
    }

    public MdColumn getCol() {
        return col;
    }

    /**
     * Is this field a collection or array mapped using a foreign key?
     */
    public boolean isForeignKeyCollectionOrArray() {
        return getInverseStr() != null;
    }

    /**
     * Get the field that we are an inverse for or null if none (i.e. the field
     * that lists us as its inverse).
     */
    public MdField getFieldWeAreInverseFor() {
        String name = getName();
        MdClass myMdClass = getMdClass();
        String qName = null;
        if (myMdClass != null) {
            qName = myMdClass.getQName();
        }
        MdClass refClass = null;
        if (category == MDStatics.CATEGORY_REF) {
            refClass = getRefClass();
        } else if (category == MDStatics.CATEGORY_COLLECTION) {
            refClass = getColElementTypeMdClass();
        }
        if (refClass != null && name != null && qName != null) {
            List l = refClass.getFieldList();
            for (int i = l.size() - 1; i >= 0; i--) {
                MdField f = (MdField)l.get(i);
                String n = f.getInverseStr();
                if (n != null && n.equals(name) && this == f.getColInverseField()) {
                    return f;
                }
            }
        }
        return null;
    }

    public String toString() {
        return "Field " + getName();
    }

    /**
     * Must this field be displayed when cat is selected in a datastore
     * window? This is not just a simple category check as embedded fields
     * of any type are shown as simple fields.
     */
    public boolean isDisplayedForCategory(int cat) {
        if (cat == 0 || cat == category) return true;
        if (cat == MDStatics.CATEGORY_SIMPLE
                || cat == MDStatics.CATEGORY_EXTERNALIZED) {
            return isEmbedded();
        }
        return false;
    }

    public String getColOrderingStr() {
        return XmlUtils.getExtension(element, "collection",
                JdoExtensionKeys.ORDERING);
    }

    public void setColOrderingStr(String s) {
        XmlUtils.setExtension(element, "collection",
                JdoExtensionKeys.ORDERING, s);
    }

    public String getArrayOrderingStr() {
        return XmlUtils.getExtension(element, "array",
                JdoExtensionKeys.ORDERING);
    }

    public void setArrayOrderingStr(String s) {
        XmlUtils.setExtension(element, "array",
                JdoExtensionKeys.ORDERING, s);
    }

    public void setOrderingStr(String s) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setColOrderingStr(s);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setArrayOrderingStr(s);
                break;
            default:
                return;
        }
    }

    public String getOrderingStr() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getColOrderingStr();
            case MDStatics.CATEGORY_ARRAY:
                return getArrayOrderingStr();
        }
        return "";
    }

    public MdValue getOrdering() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getOrdering("collection");
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return MdValue.NA;
                return getOrdering("array");
        }
        return MdValue.NA;
    }

    public void setOrdering(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setOrdering("collection", v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setOrdering("array", v);
                break;
            default:
                return;
        }
    }

    private MdValue getOrdering(String nested) {
        MdValue v = createMdValue(XmlUtils.getExtension(element, nested,
                JdoExtensionKeys.ORDERING));
        MdValue o = getOrdered();
        boolean ordered = o.getBool();
        boolean orderedDef = o.getDefBool();
        boolean orderedEff = o.getText() == null ? orderedDef : ordered;
        if (ordered || orderedDef) {
            v.setDefText(orderedEff ? "{order by index in list}" : "{none}");
            if (ordered) v.setColor(Color.red);
        } else {
            v.setDefText("{none}");
            v.setOnlyFromPickList(false);
        }
        return v;
    }

    public MdValue getNullIndicatorField() {
        String name = null;
        ArrayList a = new ArrayList();
        if (embeddedFields != null) {
            for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                MdEmbeddedField mdEmbeddedField = (MdEmbeddedField)it.next();
                switch (mdEmbeddedField.getCategory()) {
                    case MDStatics.CATEGORY_COLLECTION:
                    case MDStatics.CATEGORY_ARRAY:
                    case MDStatics.CATEGORY_CLASS_ID:
                    case MDStatics.CATEGORY_DATASTORE_PK:
                    case MDStatics.CATEGORY_MAP:
                    case MDStatics.CATEGORY_NONE:
                    case MDStatics.CATEGORY_OPT_LOCKING:
                    case MDStatics.CATEGORY_TRANSACTIONAL:
                        continue;
                    case MDStatics.CATEGORY_EXTERNALIZED:
                    case MDStatics.CATEGORY_POLYREF:
                    case MDStatics.CATEGORY_REF:
                        if(isEmbeddedRef()){
                            continue;
                        }
                    case MDStatics.CATEGORY_SIMPLE:
                        a.add(mdEmbeddedField.getName());
                        if(mdEmbeddedField.isNullIndicator()){
                            if(name == null){
                                name = mdEmbeddedField.getName();
                            }else{
                                mdEmbeddedField.setNullIndicator(false);
                            }
                        }
                }
            }
        }
        MdValue v = new MdValue(name);
        v.setPickList(a);
        v.setDefText("");
        v.setOnlyFromPickList(true);
        return v;
    }

    public void setNullIndicatorField(MdValue value) {
        String text = value.getText();
        if (embeddedFields != null) {
            for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                MdEmbeddedField mdEmbeddedField = (MdEmbeddedField)it.next();
                mdEmbeddedField.setNullIndicator(mdEmbeddedField.getName().equals(text));
            }
        }
    }

    public boolean isNullIndicator(){
        return false;
    }

    public void setNullIndicator(boolean nullInd){
    }

    public String getNullIndicatorFieldLabel(){
        return "This reference ("+getName()+") is retrieved as null if this field is null in the db";
    }

    private void setOrdering(String nested, MdValue v) {
        XmlUtils.setExtension(element, nested,
                JdoExtensionKeys.ORDERING, v.getText());
    }

    /**
     * This is called when the meta data has been parsed. The param will be
     * null if meta data parsing failed.
     */
    public void setClassMetaData(ClassMetaData classMetaData) {
        if (classMetaData == null) {
            fmd = null;
            jdbcField = null;
        } else {
            findFieldMetaData(classMetaData);
        }
        if(isEmbeddedRef() && embeddedFields != null){
            for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                MdEmbeddedField embeddedField = (MdEmbeddedField) it.next();
                embeddedField.setClassMetaData(classMetaData);
            }
        }
        if (col == null) return;
        if (jdbcField == null) {
            col.setJdbcColumn(null);
        } else if (col != null && jdbcField.mainTableCols != null) {
            col.setJdbcColumn(
                    jdbcField == null ? null : jdbcField.mainTableCols[0]);
        }
    }

    protected void findFieldMetaData(ClassMetaData classMetaData) {
        fmd = findFieldMetaDataImpl(classMetaData);
        jdbcField = fmd == null ? null : (JdbcField)fmd.storeField;
        if (jdbcField instanceof JdbcLinkCollectionField) {
            linkField = (JdbcLinkCollectionField)jdbcField;
            linkTable = linkField.linkTable;
        } else {
            linkField = null;
            linkTable = null;
        }
    }

    protected FieldMetaData findFieldMetaDataImpl(ClassMetaData classMetaData) {
        return classMetaData.getFieldMetaData(getName());
    }

    /**
     * If this field is stored in JDBC and its runtime meta data is available
     * then return the runtime meta data.
     */
    public JdbcField getJdbcField() {
        return jdbcField;
    }

    /**
     * If this field is a collection or map using a link table and the runtime
     * meta data has been set then this is the runtime structure for the field.
     * Otherwise null is returned.
     */
    public JdbcLinkCollectionField getJdbcLinkCollectionField() {
        return linkField;
    }

    /**
     * Is this field a List or something else that has an index?
     */
    public boolean fieldHasIndex() {
        if (getFieldType().getComponentType() != null) {
            return !isEmbedded();
        } else {
            Class t = getFieldType();
            return t != null

            	&& List.class.isAssignableFrom(t);


        }
    }

    /**
     * Does the mapping for this field preserve the order of elements in
     * the collection or array?
     */
    public boolean isPreserveOrder() {
        if (category == MDStatics.CATEGORY_ARRAY) {
            if (getArrayOrderingStr() != null || getArrayInverseStr() != null) {
                // JDOQL ordering expression or an inverse field
                return false;
            } else {
                String s = getArrayOrderedStr();
                return s == null || s.equals("true");
            }
        } else {
            if (getColOrderingStr() != null || getColInverseStr() != null
                    || !fieldHasIndex()) {
                // JDOQL ordering expression or an inverse field or a Set
                return false;
            } else {
                String s = getColOrderedStr();
                return s == null || s.equals("true");
            }
        }
    }

    /**
     * Initialize a link table for this field. If this field does not use
     * a link table then this is a NOP.
     */
    public void initMdLinkTable(MdLinkTable link) {
        switch (category) {
            case MDStatics.CATEGORY_MAP:
                link.init(findOrCreateLinkTableElement("map"), linkTable, getName(),
                        "Link or join table holding the key and value for each " +
                        "entry in the map and a reference to the owning class",
                        getMdClass(), getKeyTypeStr(), getValueTypeStr(),
                        false, (JdbcMapField)getJdbcField());
                return;

            case MDStatics.CATEGORY_COLLECTION:
                link.init(findOrCreateLinkTableElement("collection"), linkTable, getName(),
                        "Link or join table holding the value for each " +
                        "element in the collection and a reference to the owning class",
                        getMdClass(), null, getElementTypeStr(),
                        isPreserveOrder(), getJdbcLinkCollectionField());
                return;
            case MDStatics.CATEGORY_ARRAY:
                link.init(findOrCreateLinkTableElement("array"), linkTable, getName(),
                        "Link or join table holding the value for each " +
                        "element in the array and a reference to the owning class",
                        getMdClass(), null, getElementTypeStr(),
                        isPreserveOrder(), getJdbcLinkCollectionField());
        }
    }

    protected MdElement findOrCreateLinkTableElement(String type) {
        MdElement base = (MdElement)element.getChild(type);
        if (base == null) {
            base = new MdElement(type);
            base.setVirtualParent(element);
        }
        return XmlUtils.findOrCreateExtension(base,
                JdoExtensionKeys.JDBC_LINK_TABLE);
    }

    /**
     * Write the names of all tables and columns associated with this field
     * into the meta data. This is a NOP if the information is not available
     * (i.e. the meta data has errors).
     */
    public void writeMappingsToMetaData() throws Exception {
        if (col != null) {
            col.writeNameToMetaData();
            return;
        }
        if (linkTable != null && getInverseStr() == null) {
            MdLinkTable lt = new MdLinkTable();
            initMdLinkTable(lt);
            lt.writeMappingsToMetaData();
            return;
        }
        if (jdbcField instanceof JdbcPolyRefField) {
            MdJdbcRef ref = new MdJdbcRef();
            ref.init(getElement(), null, null, null,
                    mdClass.getMdDataStore(), mdClass.getMdPackage(),
                    ((JdbcPolyRefField)jdbcField).cols);
            ref.writeMappingsToMetaData();
            return;
        }
        if (jdbcField instanceof JdbcRefField) {
            MdJdbcRef ref = new MdJdbcRef();
            ref.init(getElement(), null, refClass, null,
                    mdClass.getMdDataStore(), mdClass.getMdPackage(),
                    jdbcField == null ? null : jdbcField.mainTableCols);
            ref.writeMappingsToMetaData();
        }
    }

    /**
     * Get largish help for this field. This is used by some of the field
     * property dialogs that have spare space to use up.
     */
    public String getDialogHelp() {
        return null;
    }

    public Class getFieldType() {
        return fieldType;
    }

    /**
     * Get an icon to represent this field on the tree view.
     */
    public String getTreeIcon() {
        switch (category) {
            case MDStatics.CATEGORY_CLASS_ID:
                return ICON_CLASS_ID;
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return ICON_SIMPLE;
            case MDStatics.CATEGORY_COLLECTION:
                if (getInverseStr() != null) {
                    MdField mm = getInverseField();
                    if (mm != null && mm.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                        return ICON_MANY_TO_MANY;
                    } else {
                        return ICON_ONE_TO_MANY;
                    }
                } else {
                    // we might be referenced as an inverse for a many-to-many
                    MdField f = getFieldWeAreInverseFor();
                    if (f != null && f.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                        return ICON_MANY_TO_MANY;
                    }
                }
                return ICON_LINK_TABLE;
            case MDStatics.CATEGORY_DATASTORE_PK:
                return ICON_PRIMARY_KEY;
            case MDStatics.CATEGORY_MAP:
                return ICON_MAP;
            case MDStatics.CATEGORY_NONE:
                return ICON_NOT_PERSISTENT;
            case MDStatics.CATEGORY_OPT_LOCKING:
                return ICON_VERSION;
            case MDStatics.CATEGORY_POLYREF:
                return ICON_POLYREF;
            case MDStatics.CATEGORY_REF:
                if(isEmbeddedRef()){
                    return ICON_EMBEDDED;
                }
                return ICON_REF;
            case MDStatics.CATEGORY_EXTERNALIZED:
                return ICON_EXTERNALIZED;
            case MDStatics.CATEGORY_SIMPLE:
                if (isPrimaryKeyField()) {
                    return ICON_PRIMARY_KEY;
                }
                return ICON_SIMPLE;
            case MDStatics.CATEGORY_TRANSACTIONAL:
                return ICON_TRANSACTIONAL;
        }
        return ICON_UNKNOWN;
    }

    /**
     * Get SQL related info on how this field is mapped (e.g. column name or
     * link table name).
     */
    public String getMappingInfo() {
        if (isEmbeddedRef()) {
            return "";
        }
        String t;
        switch (category) {
            case MDStatics.CATEGORY_NONE:
            case MDStatics.CATEGORY_TRANSACTIONAL:
                return "";
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) break;
            case MDStatics.CATEGORY_COLLECTION:
                if (getColInverseStr() != null) {
                    MdField inv = getColInverseField();
                    if (inv != null && inv.getCategory() == MDStatics.CATEGORY_REF) {
                        return inv.getMdClass().getMappingInfo() + "." +
                                inv.getMappingInfo();
                    }
                }
            case MDStatics.CATEGORY_MAP: // or link table collection
                t = getJdbcLinkTableNameStr();
                if (t == null && getJdbcLinkCollectionField() != null && getJdbcLinkCollectionField().linkTable != null) {
                    t = getJdbcLinkCollectionField().linkTable.name;
                }
                return t;
        }
        if (isCompositePkRef() || category == MDStatics.CATEGORY_POLYREF) {
            return getColumnNames();
        } else {
            t = getColumnNameStr();
            if (t == null) t = getColumnDefName();
            if (t == null) t = "{auto}";
            return t + " " + getSqlDDL();
        }
    }

    /**
     * Get the names of this fields columns in a comma list if available or null if not.
     */
    public String getColumnNames() {
        JdbcColumn[] cols = jdbcField == null ? null : jdbcField.mainTableCols;
        if (cols == null) {
            return null;
        } else {
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) {
                    s.append(',');
                    s.append(' ');
                }
                s.append(cols[i].name);
            }
            return s.toString();
        }
    }

    /**
     * Add all columns for this field to table.
     */
    public void addColumnsToTable(MdTable table) {
        if (col != null) {
           table.addCol(col);
            col.setTable(table);
        } else {
            MdJdbcRef ref;
            switch (category) {

                case MDStatics.CATEGORY_REF:
                    if(isEmbeddedRef() && embeddedFields != null){
                        for (Iterator it = embeddedFields.iterator(); it.hasNext();) {
                            MdEmbeddedField embeddedField = (MdEmbeddedField) it.next();
                            embeddedField.addColumnsToTable(table);
                        }
                    }else{
                        ref = new MdJdbcRef();
                        ref.init(getElement(), table, refClass, null,
                                mdClass.getMdDataStore(), mdClass.getMdPackage(),
                                jdbcField == null ? null : jdbcField.mainTableCols);
                        ref.addColsToTable(table);
                    }
                    break;
                case MDStatics.CATEGORY_POLYREF:
                    ref = new MdJdbcRef();
                    ref.init(getElement(), table, null, null,
                            mdClass.getMdDataStore(), mdClass.getMdPackage(),
                            jdbcField instanceof JdbcPolyRefField
                            ? ((JdbcPolyRefField)jdbcField).cols
                            : null);
                    ref.addColsToTable(table);
                    break;
            }
        }
    }

    public RuntimeException getFirstError() {
        return fmd == null ? null : fmd.getFirstError();
    }

    public boolean hasErrors() {
        return fmd != null && fmd.hasErrors();
    }

    public String getErrorText() {
        return MdUtils.getErrorHtml(getFirstError());
    }

    /**
     * If the managed extension is not used will this field by managed anyway
     * due to datastore defaults?
     */
    public boolean isManagedByDefault() {
        if (category == MDStatics.CATEGORY_ARRAY) return false;
        MdDataStore ds = mdClass.getMdDataStore();
        if (getInverseStr() != null) {
            MdField mm = getColInverseField();
            if (mm != null && mm.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                return ds.getManagedManyToManyBool();
            } else {
                return ds.getManagedOneToManyBool();
            }
        } else {
            // we might be referenced as an inverse for a many-to-many
            MdField f = getFieldWeAreInverseFor();
            if (f != null && f.getCategory() == MDStatics.CATEGORY_COLLECTION) {
                return ds.getManagedManyToManyBool();
            }
        }
        return false;
    }

    /**
     * Is the relationship this field is in automatically managed by the SCO
     * classes? This returns false if this is not applicable to the field
     * type.
     */
    public boolean isManaged() {
        MdElement ce = getCollectionArrayOrMapElement();
        if (ce == null) return false;
        String e = XmlUtils.getExtension(ce, JdoExtensionKeys.MANAGED);
        return e != null ? "true".equals(e) : isManagedByDefault();
    }

    /**
     * Change the managed status of this field.
     */
    public void setManaged(boolean on) {
        MdElement ce = getCollectionArrayOrMapElement();
        if (ce == null) return;
        XmlUtils.setExtension(ce, JdoExtensionKeys.MANAGED,
                on ? "true" : "false");
    }

    /**
     * Remove the managed extension on this field (if any) reverting to the
     * default setting for the datastore.
     */
    public void clearManaged() {
        MdElement ce = getCollectionArrayOrMapElement();
        if (ce == null) return;
        XmlUtils.setExtension(ce, JdoExtensionKeys.MANAGED, null);
    }

    public MdClassNameValue getScoFactory() {
        MdClassNameValue v = new MdClassNameValue(
                XmlUtils.getExtension(element, JdoExtensionKeys.SCO_FACTORY));
        MdDataStore mdDataStore = mdClass.getMdDataStore();
        if (mdDataStore != null) {
            v.setDefText(mdDataStore.getDefaultSCOFactory(fieldType));
            v.setPickList(mdDataStore.getValidSCOFactoryList(fieldType));
        }
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setScoFactory(MdClassNameValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.SCO_FACTORY,
                v.getText());
    }

    /**
     * Is a field of a type that can only be persisted through
     * externalization?
     */
    public boolean isPersistableOnlyUsingExternalization() {
        return mdClass.getMdProject().isPersistableOnlyUsingExternalization(
                mdClass.getMdDataStore().getName(), fieldType);
    }

    public MdProject getProject() {
        return mdClass.getMdDataStore().getProject();
    }

    public MdClassNameValue getExternalizer() {
        MdClassNameValue v = new MdClassNameValue(getExternalizerStr());
        v.setPickList(PickLists.EXTERNALIZER);
        v.setOnlyFromPickList(false);
        MdExternalizer e = getProject().getDefaultExternalizer(getTypeStr());
        if (e == null) {
            v.setDefText(SerializedExternalizer.SHORT_NAME);
        } else {
            v.setDefText(e.getExternalizerStr());
        }
        return v;
    }

    public void setExternalizer(MdClassNameValue v) {
        setExternalizerStr(v.getText());
    }

    public String getExternalizerStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.EXTERNALIZER);
    }

    public void setExternalizerStr(String s) {
        if (s != null) {
            setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
        }
        XmlUtils.setExtension(element, JdoExtensionKeys.EXTERNALIZER, s);
        analyze();
    }

    /**
     * Swap the many-to-many inverse (field) with the non-inverse (fieldMM).
     * This moves all the content of the &lt;collection&gt; element and the
     * &lt;jdbc-link-table&gt; element.
     */
    public void swapManyToManyAndInverse(MdField fieldMM) {
        moveLinkTable(fieldMM.getLinkTableElement(), getCollectionArrayOrMapElement());
        MdElement ce = fieldMM.getCollectionArrayOrMapElement();
        if (ce != null) {
            MdElement managed = XmlUtils.findExtension(ce, JdoExtensionKeys.MANAGED);
            if (managed != null) {
                managed.getParent().removeContent(managed);
                getCollectionArrayOrMapElement().addContent(managed);
            }
        }
        setInverseStr(null);
        fieldMM.setInverseStr(field.getName());
    }

    protected void moveLinkTable(MdElement linkTableElement, MdElement newParent) {
        if (linkTableElement == null) return;
        linkTableElement.getParent().removeContent(linkTableElement);
        // swap owner and value around
        MdElement owner = XmlUtils.findOrCreateExtension(linkTableElement,
                JdoExtensionKeys.JDBC_OWNER_REF);
        MdElement value = XmlUtils.findOrCreateExtension(linkTableElement,
                JdoExtensionKeys.JDBC_VALUE);
        if (owner != null) {
            owner.setAttribute("key",
                JdoExtension.toKeyString(JdoExtensionKeys.JDBC_VALUE));
        }
        if (value != null) {
            value.setAttribute("key",
                JdoExtension.toKeyString(JdoExtensionKeys.JDBC_OWNER_REF));
        }
        newParent.addContent(linkTableElement);
    }


}
