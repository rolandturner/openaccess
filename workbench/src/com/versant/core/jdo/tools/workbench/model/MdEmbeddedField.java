
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

/**
 * JavaBean wrapper around XML for a field.
 */
public class MdEmbeddedField extends MdField {

    MdField parent;
    MdField defaultField;

    public MdEmbeddedField(MdField parent) {
        this.parent = parent;
    }

    public MdField getDefaultField() {
        return defaultField;
    }

    public void setDefaultField(MdField defaultField) {
        this.defaultField = defaultField;
    }

    public void init(MdClass mdClass, MdElement element) {
        super.init(mdClass, element);
        element.setVirtualParent(parent.getElement());
        element.setEmptyExtValueCount(3);
    }

    protected MdElement createElement() {
        MdElement element = XmlUtils.createExtension(JdoExtensionKeys.FIELD, parent.getElement());
        return element;
    }

    public String getName() {
        return element.getAttributeValue("value");
    }

    public void setName(String name) {
        element.setAttribute("value", name);
    }

    /**
     * Get the fully qualified name of this field.
     */
    public String getQName() {
        return parent.getQName() + "." + getName();
    }

    /**
     * Get the name of this field preceded by its classname without package.
     */
    public String getMiniQName() {
        return parent.getMiniQName() + "." + getName();
    }

    /**
     * Get the name of our class (not fully qualified) and our name or
     * just our name if our class has no PC superclass.
     */
    public String getShortQName() {
        return parent.getShortQName()+"."+getName();
    }

    /**
     * Get a 'Java source' fragment for this field (e.g.
     * 'public class Foo { private int bar; }'.
     */
    public String getMiniClassDef() {
        StringBuffer s = new StringBuffer();
        s.append("<html><b><code>&nbsp;&nbsp;");
        s.append(cleanupHTML(super.getMiniQName()));
        s.append("&nbsp;&nbsp;");
        s.append(cleanupHTML(super.getGenericTypeStr()));
        s.append("</code></b>");
        s.append("</body></html>");
        return s.toString();
    }

    /**
     * Get the persistence-modifier for this field. If it is null then return
     * the default persistence behaviour.
     */
    public int getPersistenceModifierInt() {
        return MDStatics.PERSISTENCE_MODIFIER_PERSISTENT;
    }

    /**
     * Set the peristence modifier for this field. If this is the same as
     * the default then remove the persistence-modifier attribute.
     */
    public void setPersistenceModifierInt(int pm) {
    }

    public String getPersistenceModifierStr() {
        return MDStaticUtils.toPersistenceModifierString(MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
    }

    public MdValue getPersistenceModifier() {
        String pm = getPersistenceModifierStr();
        MdValue v = createMdValue(pm);
        v.setDefText(pm);
        v.setOnlyFromPickList(true);
        return v;
    }

    public void setPersistenceModifier(MdValue v) {
    }

    /**
     * Can this field be used as part of the primary key?
     */
    public boolean isPossiblePrimaryKeyField() {
        return false;
    }

    public boolean isPrimaryKeyField() {
		return false;
	}

    public String getPrimaryKeyStr() {
        return null;
    }

    public void setPrimaryKeyStr(String s) {
    }

    public MdValue getPrimaryKey() {
        MdValue v = createMdValue("false");
        v.setDefText("false");
        v.setOnlyFromPickList(true);
        return v;
    }

    public void setPrimaryKey(MdValue v) {
    }

    public String getNullValueStr() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.NULL_VALUE);
        if(temp == null && defaultField != null){
            return defaultField.getNullValueStr();
        }
        return temp;
    }

    public void setNullValueStr(String s) {
        String def = defaultField == null ? null : defaultField.getNullValueStr();
        s = fixDefaultString(def, s, "false");
        XmlUtils.setExtension(element, JdoExtensionKeys.NULL_VALUE, s);
    }

    private String getNullIndicatorStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.NULL_INDICATOR);
    }

    private void setNullIndicatorStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.NULL_INDICATOR, s);
    }

    public boolean isNullIndicator(){
        boolean def = defaultField == null ? false : defaultField.isNullIndicator();
        String nullIndicatorStr = getNullIndicatorStr();
        return nullIndicatorStr != null ? nullIndicatorStr.equals("true") : def;
    }

    public void setNullIndicator(boolean nullInd){
        boolean def = defaultField == null ? false : defaultField.isNullIndicator();
        setNullIndicatorStr(def == nullInd ? null : nullInd ? "true" : "false");
    }

    private String fixDefaultString(String def, String s, String defValue) {
        if (!equals(def, s)) {
            if (s == null) {
                s = defValue;
            }
        } else {
            s = null;
        }
        return s;
    }

    protected boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return b != null && a.equals(b);
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
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.DEFAULT_FETCH_GROUP);
        if(temp == null && defaultField != null){
            return defaultField.getDefaultFetchGroupStr();
        }
        return temp;
    }

    public void setDefaultFetchGroupStr(String s) {
        String def = defaultField == null ? null : defaultField.getDefaultFetchGroupStr();
        s = fixDefaultString(def, s, isDFGFieldByDefault() ? "true" : "none");
        XmlUtils.setExtension(element, JdoExtensionKeys.DEFAULT_FETCH_GROUP, s);
    }


    public String getEmbeddedStr() {
        MdElement embElement = XmlUtils.findExtension(element, JdoExtensionKeys.EMBEDDED);
        String temp = null;
        if (embElement != null) {
            temp = embElement.getAttributeValue("value");
            if(temp == null){
                temp = "true";
            }
        }
        if(temp == null && defaultField != null){
            String embeddedStr = defaultField.getEmbeddedStr();
            if("true".equals(embeddedStr) && isCyclic()){
                return "false";
            }
            return embeddedStr;
        }
        return temp;
    }


    private boolean isCyclic() {
        if(this.refClass == null) return false;
        HashSet parents = new HashSet(5);
        MdField field = parent;
        while(field != null){
            MdClass refClass = field.getRefClass();
            addAllClassesToSet(refClass, parents);
            if(field instanceof MdEmbeddedField){
                field = ((MdEmbeddedField)field).parent;
            }else{
                addAllClassesToSet(field.mdClass, parents);
                field = null;
            }
            if(parents.contains(this.refClass)){
                return true;
            }
        }
        return false;
//            return parent.getRefClass().isSameTable(refClass);
    }

    private void addAllClassesToSet(MdClass refClass, HashSet parents) {
        while(refClass != null){
            parents.add(refClass);
            refClass = refClass.getPcSuperclassMdClass();
        }
    }


    protected void setEmbeddedStr(String s) {
        String def = defaultField == null ? null : defaultField.getEmbeddedStr();
        if(isCyclic()){
            def = "false";
        }
        s = fixDefaultString(def, s, "false");
        XmlUtils.setExtension(element, JdoExtensionKeys.EMBEDDED, s);
    }



    public void setKeysDependent(MdValue v) {
        setKeysDependentStr(v.getText());
    }

    public void setKeysDependentStr(String s) {
        String def = defaultField == null ? null : defaultField.getKeysDependentStr();
        s = fixDefaultString(def, s, "false");
        XmlUtils.setExtension(element, JdoExtensionKeys.KEYS_DEPENDENT, s);
    }

    public String getColElementTypeStrRaw() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION);
        if(temp == null && defaultField != null){
            return defaultField.getColElementTypeStrRaw();
        }
        return temp;
    }

    public void setColElementType(MdValue v) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION, v.getText());
//        setColInverseStr(null);
//        if (getColElementTypeMdClass() == null) {
//            // not a pc element-type so dependent is not allowed
//            setDependentStr(null);
//        }
    }

    public String getColEmbeddedElementStr() {
//        return XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.EMBEDDED_ELEMENT);
        return null;
    }

    public void setColEmbeddedElementStr(String s) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.EMBEDDED_ELEMENT, s);
    }

    public String getKeyTypeStrRaw() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.KEY_TYPE);
        if(temp == null && defaultField != null){
            return defaultField.getKeyTypeStrRaw();
        }
        return temp;
    }

    public void setKeyType(MdValue v) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.KEY_TYPE, v.getText());
    }

    public String getEmbeddedKeyStr() {
//        return XmlUtils.getExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.EMBEDDED_KEY);
        return null;
    }

    public void setEmbeddedKeyStr(String s) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.EMBEDDED_KEY, s);
    }

    public String getValueTypeStrRaw() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.VALUE_TYPE);
        if(temp == null && defaultField != null){
            return defaultField.getValueTypeStrRaw();
        }
        return temp;
    }

    public void setValueTypeStr(String s) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.VALUE_TYPE, s);
    }

    public String getEmbeddedValueStr() {
//        return XmlUtils.getExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.EMBEDDED_VALUE);
        return null;
    }

    public void setEmbeddedValueStr(String s) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.EMBEDDED_VALUE, s);
    }

    public String getArrayEmbeddedElementStr() {
//        return XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY, JdoExtensionKeys.EMBEDDED_ELEMENT);
        return null;
    }

    public void setArrayEmbeddedElementStr(String s) {
//        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY, JdoExtensionKeys.EMBEDDED_ELEMENT, s);
    }

    public String getColOrderedStr() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.ORDERED);
        if(temp == null && defaultField != null){
            return defaultField.getColOrderedStr();
        }
        return temp;
    }

    public String getArrayOrderedStr() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY,JdoExtensionKeys.ORDERED);
        if(temp == null && defaultField != null){
            return defaultField.getArrayOrderedStr();
        }
        return temp;
    }

    public void setColOrderedStr(String s) {
        String def = defaultField == null ? null : defaultField.getColOrderedStr();
        s = fixDefaultString(def, s, getColOrderedDefault() ? "true" : "false");
        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION,
                JdoExtensionKeys.ORDERED, s);
    }

    public void setArrayOrderedStr(String s) {
        String def = defaultField == null ? null : defaultField.getArrayOrderedStr();
        s = fixDefaultString(def, s, "true");
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.ORDERED, s);
    }

    public String getColInverseStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.INVERSE);
    }

    public void setColInverseStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION,
                JdoExtensionKeys.INVERSE, s);
    }

    public void setArrayInverseStr(String s) {
        XmlUtils.setExtension(element,JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.INVERSE, s);
    }

    public String getColInverseJdbcIndexStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.INVERSE, JdoExtensionKeys.JDBC_INDEX);
    }

    public String getArrayInverseJdbcIndexStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY, JdoExtensionKeys.INVERSE, JdoExtensionKeys.JDBC_INDEX);
    }

    protected void setArrayInverseJdbcIndexStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX, s);
    }

    protected void setColInverseJdbcIndexStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION,
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX, s);
    }


    public String getJdbcColLinkTableNameStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.JDBC_LINK_TABLE, JdoExtensionKeys.JDBC_TABLE_NAME);
    }

    public MdElement getLinkTableElement() {
        MdElement ans = XmlUtils.findExtension(element,
                JdoExtensionKeys.JDBC_LINK_TABLE);
        if (ans == null) {
            MdElement ce = XmlUtils.findExtension(element, JdoExtensionKeys.COLLECTION);
            if (ce != null) {
                ans = XmlUtils.findExtension(ce,
                        JdoExtensionKeys.JDBC_LINK_TABLE);
            }
        }
        return ans;
    }

    public void setJdbcColLinkTableName(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION,
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME,
                v.getText());
    }

    public MdValue getArrayOrdered() {
        MdValue v = createMdValue(XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.ORDERED));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("true");
        return v;
    }

    public void setArrayOrdered(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.ORDERED, v.getText());
    }

    public String getArrayInverseStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY, JdoExtensionKeys.INVERSE);
    }

    public void setArrayInverse(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.INVERSE, v.getText());
    }

    public MdValue getArrayInverseJdbcIndex() {
        MdValue v = new MdValue(XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX));
        v.setPickList(PickLists.NO);
        v.setDefText("{auto}");
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setArrayInverseJdbcIndex(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.INVERSE,
                JdoExtensionKeys.JDBC_INDEX,
                v.getText());
    }

    public String getJdbcArrayLinkTableNameStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY, JdoExtensionKeys.JDBC_LINK_TABLE, JdoExtensionKeys.JDBC_TABLE_NAME);
    }

    public void setJdbcArrayLinkTableName(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME,
                v.getText());
    }

    public String getJdbcMapLinkTableNameStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.MAP, JdoExtensionKeys.JDBC_LINK_TABLE, JdoExtensionKeys.JDBC_TABLE_NAME);
    }

    public void setJdbcMapLinkTableName(MdValue v) {
        XmlUtils.setExtension(element, JdoExtensionKeys.MAP,
                JdoExtensionKeys.JDBC_LINK_TABLE,
                JdoExtensionKeys.JDBC_TABLE_NAME,
                v.getText());
    }

    public void setJdbcUseJoinStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.JDBC_USE_JOIN, s);
    }

    public String getJdbcUseJoinStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.JDBC_USE_JOIN);
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

    public void setFetchGroupStr(String s) {
        XmlUtils.setExtension(element, FETCH_GROUP, s);
    }

    public String getJdbcConstraintStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.JDBC_CONSTRAINT);
    }

    public void setJdbcConstraintStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.JDBC_CONSTRAINT, s);
    }

    /**
     * Get the collection, array or map element for this field depending on
     * the type of the field. Returns null if none found or not a collection,
     * array or map.
     */
    public MdElement getCollectionArrayOrMapElement() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return  XmlUtils.findOrCreateExtension(element, JdoExtensionKeys.COLLECTION);
            case MDStatics.CATEGORY_ARRAY:
                return  XmlUtils.findOrCreateExtension(element, JdoExtensionKeys.ARRAY);
            case MDStatics.CATEGORY_MAP:
                return  XmlUtils.findOrCreateExtension(element, JdoExtensionKeys.MAP);
        }
        return null;
    }

    public String getColOrderingStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.COLLECTION, JdoExtensionKeys.ORDERING);
    }

    public void setColOrderingStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.COLLECTION,
                JdoExtensionKeys.ORDERING, s);
    }

    public String getArrayOrderingStr() {
        return XmlUtils.getExtension(element, JdoExtensionKeys.ARRAY, JdoExtensionKeys.ORDERING);
    }

    public void setArrayOrderingStr(String s) {
        XmlUtils.setExtension(element, JdoExtensionKeys.ARRAY,
                JdoExtensionKeys.ORDERING, s);
    }


    public MdValue getOrdering() {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                return getOrdering(JdoExtensionKeys.COLLECTION);
            case MDStatics.CATEGORY_ARRAY:
                if (isEmbedded()) return MdValue.NA;
                return getOrdering(JdoExtensionKeys.ARRAY);
        }
        return MdValue.NA;
    }

    public void setOrdering(MdValue v) {
        switch (category) {
            case MDStatics.CATEGORY_COLLECTION:
                setOrdering(JdoExtensionKeys.COLLECTION, v);
                break;
            case MDStatics.CATEGORY_ARRAY:
                setOrdering(JdoExtensionKeys.ARRAY, v);
                break;
            default:
                return;
        }
    }

    private MdValue getOrdering(int key) {
        MdValue v = createMdValue(XmlUtils.getExtension(element, key,
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

    private void setOrdering(int key, MdValue v) {
        XmlUtils.setExtension(element, key,
                JdoExtensionKeys.ORDERING, v.getText());
    }

    protected MdElement findOrCreateLinkTableElement(String type) {
        MdElement base = XmlUtils.findOrCreateExtension(element, type);
        MdElement temp = XmlUtils.findOrCreateExtension(base, JdoExtensionKeys.JDBC_LINK_TABLE);
        if(temp == null && defaultField != null){
            return defaultField.findOrCreateLinkTableElement(type);
        }
        return temp;
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

    public String getExternalizerStr() {
        String temp = XmlUtils.getExtension(element, JdoExtensionKeys.EXTERNALIZER);
        if(temp == null && defaultField != null){
            return defaultField.getExternalizerStr();
        }
        return temp;
    }

    public void setExternalizerStr(String s) {
        if (s != null) {
            setPersistenceModifierInt(
                    MDStatics.PERSISTENCE_MODIFIER_PERSISTENT);
        }
        XmlUtils.setExtension(element, JdoExtensionKeys.EXTERNALIZER, s);
        analyze();
    }

    protected FieldMetaData findFieldMetaDataImpl(ClassMetaData classMetaData) {
        FieldMetaData parentFmd = parent.fmd;
        if(parentFmd != null){
            FieldMetaData embeddedFmd = parentFmd.findEmbeddedFmd(getName());
            return embeddedFmd;
        }
        return super.findFieldMetaDataImpl(classMetaData);
    }

    public MdField getParentField() {
        return parent;
    }
}
