
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

/**
 * This is a javabean wrapper for special 'fields' class that are not
 * really fields but need to be treated as such by the gui (datastore pk,
 * version/timestamp and class-id).
 * @keep-all
 */
public abstract class MdSpecialField extends MdField {

    /**
     * Get the value attribute of our element.
     */
    public String getValue() {
        return element.getAttributeValue("value");
    }

    public void setValue(String s) {
        element.setAttribute("value", s);
    }

    public String getPersistenceModifierStr() {
        return MdValue.NAS;
    }

    public MdValue getPersistenceModifier() {
        return MdValue.NA;
    }

    public MdValue getNullValue() {
        return MdValue.NA;
    }

    public MdValue getDefaultFetchGroup() {
        return MdValue.NA;
    }

    public MdValue getEmbedded() {
        return MdValue.NA;
    }

    public MdValue getAutoSet() {
        return MdValue.NA;
    }

    public MdValue getDependent() {
        return MdValue.NA;
    }

    public MdValue getKeysDependent() {
        return MdValue.NA;
    }

    public MdValue getKeyType() {
        return MdValue.NA;
    }

    public MdValue getEmbeddedKey() {
        return MdValue.NA;
    }

    public MdValue getValueType() {
        return MdValue.NA;
    }

    public MdValue getEmbeddedValue() {
        return MdValue.NA;
    }

    public MdValue getJdbcUseJoin() {
        return MdValue.NA;
    }

    public MdValue getFetchGroup() {
        return MdValue.NA;
    }

    public MdValue getElementType() {
        return MdValue.NA;
    }

    public MdValue getEmbeddedElement() {
        return MdValue.NA;
    }

    public MdValue getOrdered() {
        return MdValue.NA;
    }

    public MdValue getInverse() throws Exception {
        return MdValue.NA;
    }

    public MdValue getJdbcLinkTableName() {
        return MdValue.NA;
    }

    public MdValue getNulls() {
        MdValue v = createMdValue(null);
        v.setDefText("false");
        return v;
    }

    public boolean isPrimaryKeyField() {
        return false;
    }

    public MdValue getPrimaryKey() {
        return MdValue.NA;
    }
}

