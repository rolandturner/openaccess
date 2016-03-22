
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

import com.versant.core.metadata.parser.JdoExtensionKeys;

/**
 * Java bean wrapper for fetch group field element.
 * @keep-all
 */
public class MdFetchGroupField implements JdoExtensionKeys {

    private MdFetchGroup mdFetchGroup;
    private MdElement element;

    public MdFetchGroupField(MdFetchGroup mdFetchGroup, MdElement element) {
        this.mdFetchGroup = mdFetchGroup;
        this.element = element;
    }

    public MdFetchGroupField(MdFetchGroup mdFetchGroup, String name) {
        this(mdFetchGroup, XmlUtils.createExtension(FIELD_NAME, name,
                mdFetchGroup.getElement()));
    }

    public MdFetchGroup getMdFetchGroup() {
        return mdFetchGroup;
    }

    public MdElement getElement() {
        return element;
    }

    public String getName() {
        return element.getAttributeValue("value");
    }

    public void setName(String name) {
        element.setAttribute("value", name);
        XmlUtils.makeDirty(element);
    }

    public String getNextFetchGroup() {
        return XmlUtils.getExtension(element, NEXT_FETCH_GROUP);
    }

    public void setNextFetchGroup(String v) {
        XmlUtils.setExtension(element, NEXT_FETCH_GROUP, v);
    }

    public String getNextKeyFetchGroup() {
        return XmlUtils.getExtension(element, NEXT_KEY_FETCH_GROUP);
    }

    public void setNextKeyFetchGroup(String v) {
        XmlUtils.setExtension(element, NEXT_KEY_FETCH_GROUP, v);
    }

    public MdValue getJdbcUseJoin() {
        MdValue v = new MdValue(
            XmlUtils.getExtension(element, JDBC_USE_JOIN));
        v.setPickList(PickLists.JDBC_USE_JOIN);
        v.setDefText("field");
        return v;
    }

    public void setJdbcUseJoin(MdValue v) {
        XmlUtils.setExtension(element, JDBC_USE_JOIN, v.getText());
    }

    public MdValue getJdbcUseKeyJoin() {
        MdValue v = new MdValue(
            XmlUtils.getExtension(element, JDBC_USE_KEY_JOIN));
        v.setPickList(PickLists.JDBC_USE_JOIN);
        v.setDefText("field");
        return v;
    }

    public void setJdbcUseKeyJoin(MdValue v) {
        XmlUtils.setExtension(element, JDBC_USE_KEY_JOIN, v.getText());
    }
}

