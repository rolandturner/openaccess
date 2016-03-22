
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

/**
 * JavaBean wrapper around XML for a field.
 */
public class MdHorizontalField extends MdField {

    MdField defaultField;

    public MdHorizontalField() {
    }

    public void setDefaultField(MdField defaultField) {
        this.defaultField = defaultField;
    }

    public MdField getDefaultField() {
        return defaultField;
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

    public String getPrimaryKeyStr() {
        String temp = super.getPrimaryKeyStr();
        if(temp == null && defaultField != null){
            return defaultField.getPrimaryKeyStr();
        }
        return temp;
    }

    public void setPrimaryKeyStr(String s) {
        String def = defaultField == null ? null : defaultField.getPrimaryKeyStr();
        s = fixDefaultString(def, s, "false");
        super.setPrimaryKeyStr(s);
    }

    public String getNullValueStr() {
        String temp = super.getNullValueStr();
        if(temp == null && defaultField != null){
            return defaultField.getNullValueStr();
        }
        return temp;
    }

    public void setNullValueStr(String s) {
        String def = defaultField == null ? null : defaultField.getNullValueStr();
        s = fixDefaultString(def, s, "false");
        super.setNullValueStr(s);
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
        String temp = super.getDefaultFetchGroupStr();
        if(temp == null && defaultField != null){
            return defaultField.getDefaultFetchGroupStr();
        }
        return temp;
    }

    public void setDefaultFetchGroupStr(String s) {
        String def = defaultField == null ? null : defaultField.getDefaultFetchGroupStr();
        s = fixDefaultString(def, s, isDFGFieldByDefault() ? "true" : "none");
        super.setDefaultFetchGroupStr(s);
    }

    public void setKeysDependentStr(String s) {
        String def = defaultField == null ? null : defaultField.getKeysDependentStr();
        s = fixDefaultString(def, s, "false");
        super.setKeysDependentStr(s);
    }

    public String getColElementTypeStrRaw() {
        String temp = super.getColElementTypeStrRaw();
        if(temp == null && defaultField != null){
            return defaultField.getColElementTypeStrRaw();
        }
        return temp;
    }

    public void setColElementType(MdValue v) {
//        super.setColElementType(s);
//        setColInverseStr(null);
//        if (getColElementTypeMdClass() == null) {
//            // not a pc element-type so dependent is not allowed
//            setDependentStr(null);
//        }
    }

    public String getColEmbeddedElementStr() {
//        return super.getColEmbeddedElementStr();
        return null;
    }

    public void setColEmbeddedElementStr(String s) {
//        super.setColEmbeddedElementStr(s);
    }

    public String getKeyTypeStrRaw() {
        String temp = super.getKeyTypeStrRaw();
        if(temp == null && defaultField != null){
            return defaultField.getKeyTypeStrRaw();
        }
        return temp;
    }

    public void setKeyType(MdValue v) {
//        super.setKeyType(s);
    }

    public String getEmbeddedKeyStr() {
//        return super.getEmbeddedKeyStr();
        return null;
    }

    public void setEmbeddedKeyStr(String s) {
//        super.setEmbeddedKeyStr(s);
    }

    public String getValueTypeStrRaw() {
        String temp = super.getValueTypeStrRaw();
        if(temp == null && defaultField != null){
            return defaultField.getValueTypeStrRaw();
        }
        return temp;
    }

    public void setValueTypeStr(String s) {
//        super.setValueTypeStr(s);
    }

    public String getEmbeddedValueStr() {
//        return super.getEmbeddedValueStr();
        return null;
    }

    public void setEmbeddedValueStr(String s) {
//        super.setEmbeddedValueStr(s);
    }

    public String getArrayEmbeddedElementStr() {
//        return super.getArrayEmbeddedElementStr();
        return null;
    }

    public void setArrayEmbeddedElementStr(String s) {
//        super.setArrayEmbeddedElementStr(s);
    }

    public String getColOrderedStr() {
        String temp = super.getColOrderedStr();
        if(temp == null && defaultField != null){
            return defaultField.getColOrderedStr();
        }
        return temp;
    }

    public String getArrayOrderedStr() {
        String temp = super.getArrayOrderedStr();
        if(temp == null && defaultField != null){
            return defaultField.getArrayOrderedStr();
        }
        return temp;
    }

    public void setColOrderedStr(String s) {
        String def = defaultField == null ? null : defaultField.getColOrderedStr();
        s = fixDefaultString(def, s, getColOrderedDefault() ? "true" : "false");
        super.setColOrderedStr(s);
    }

    public void setArrayOrderedStr(String s) {
        String def = defaultField == null ? null : defaultField.getArrayOrderedStr();
        s = fixDefaultString(def, s, "true");
        super.setArrayOrderedStr(s);
    }

    public String getColInverseStr() {
        return super.getColInverseStr();
    }

    public void setColInverseStr(String s) {
        super.setColInverseStr(s);
    }

    public void setArrayInverseStr(String s) {
        super.setArrayInverseStr(s);
    }

    public String getColInverseJdbcIndexStr() {
        return super.getColInverseJdbcIndexStr();
    }

    public String getArrayInverseJdbcIndexStr() {
        return super.getArrayInverseJdbcIndexStr();
    }

    protected void setArrayInverseJdbcIndexStr(String s) {
        super.setArrayInverseJdbcIndexStr(s);
    }

    protected void setColInverseJdbcIndexStr(String s) {
        super.setColInverseJdbcIndexStr(s);
    }


    public String getJdbcColLinkTableNameStr() {
        return super.getJdbcColLinkTableNameStr();
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

    public String getArrayInverseStr() {
        return super.getArrayInverseStr();
    }

    public String getJdbcArrayLinkTableNameStr() {
        return super.getJdbcArrayLinkTableNameStr();
    }

    public String getJdbcMapLinkTableNameStr() {
        return super.getJdbcMapLinkTableNameStr();
    }

    public void setJdbcUseJoinStr(String s) {
        super.setJdbcUseJoinStr(s);
    }

    public String getJdbcUseJoinStr() {
        return super.getJdbcUseJoinStr();
    }

    public String getJdbcConstraintStr() {
        return super.getJdbcConstraintStr();
    }

    public void setJdbcConstraintStr(String s) {
        super.setJdbcConstraintStr(s);
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
        return super.getColOrderingStr();
    }

    public void setColOrderingStr(String s) {
        super.setColOrderingStr(s);
    }

    public String getArrayOrderingStr() {
        return super.getArrayOrderingStr();
    }

    public void setArrayOrderingStr(String s) {
        super.setArrayOrderingStr(s);
    }

    protected MdElement findOrCreateLinkTableElement(String type) {
        MdElement base = XmlUtils.findOrCreateExtension(element, type);
        MdElement temp = XmlUtils.findOrCreateExtension(base, JdoExtensionKeys.JDBC_LINK_TABLE);
        if(temp == null && defaultField != null){
            return defaultField.findOrCreateLinkTableElement(type);
        }
        return temp;
    }

    public String getExternalizerStr() {
        String temp = super.getExternalizerStr();
        if(temp == null && defaultField != null){
            return defaultField.getExternalizerStr();
        }
        return temp;
    }
}
