
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

import java.util.List;
import java.util.ArrayList;

import com.versant.core.metadata.parser.JdoExtensionKeys;

/**
 * @keep-all
 * This wraps an extension for a JDBC index.
 */
public class MdIndex implements JdoExtensionKeys {

    private MdClass mdClass;
    private MdElement element; // the jdbc-index extension

    private List fields = new ArrayList(); // of String
    private String fieldSummary;

    public MdIndex(MdClass mdClass, MdElement element) {
        this.mdClass = mdClass;
        this.element = element;
        analyze();
    }

    public MdIndex(MdClass mdClass) {
        this(mdClass, XmlUtils.createExtension(JDBC_INDEX, mdClass.getElement()));
    }

    private void analyze() {
        List list = XmlUtils.findExtensions(element, FIELD_NAME);
        int n = list.size();
        fields.clear();
        for (int i = 0; i < n; i++) {
            MdElement e = (MdElement)list.get(i);
            fields.add(e.getAttributeValue("value"));
        }
        fillFieldSummary();
    }

    private void fillFieldSummary() {
        StringBuffer s = new StringBuffer();
        int n = fields.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(", ");
            s.append(fields.get(i));
        }
        fieldSummary = s.toString();
    }

    public MdClass getMdClass() {
        return mdClass;
    }

    public MdElement getElement() {
        return element;
    }

    public MdValue getName() {
        MdValue v = new MdValue(element.getAttributeValue("value"));
        v.setPickList(PickLists.EMPTY);
        v.setOnlyFromPickList(false);
        v.setDefText("auto");
        return v;
    }

    public void setName(MdValue v) {
        XmlUtils.setAttribute(element, "value", v.getText());
    }

    public MdValue getUnique() {
        MdValue v = new MdValue(XmlUtils.getExtension(element, JDBC_UNIQUE));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setUnique(MdValue v) {
        XmlUtils.setExtension(element, JDBC_UNIQUE, v.getText());
    }

    public MdValue getClustered() {
        MdValue v = new MdValue(XmlUtils.getExtension(element, JDBC_CLUSTERED));
        v.setPickList(PickLists.BOOLEAN);
        v.setDefText("false");
        return v;
    }

    public void setClustered(MdValue v) {
        XmlUtils.setExtension(element, JDBC_CLUSTERED, v.getText());
    }

    public List getFields() {
        return fields;
    }

    public String getFieldSummary() {
        return fieldSummary;
    }

    public void addField(String name) {
        XmlUtils.createExtension(FIELD_NAME, name, element);
        fields.add(name);
        fillFieldSummary();
        XmlUtils.makeDirty(element);
    }

    public void removeField(String name) {
        fields.remove(name);
        syncFields();
    }

    public void moveFieldUp(String f) {
        int i = fields.indexOf(f);
        if (i < 1) return;
        String t = (String)fields.get(i - 1);
        fields.set(i - 1, f);
        fields.set(i, t);
        syncFields();
    }

    public void moveFieldDown(String f) {
        int i = fields.indexOf(f);
        if (i >= fields.size() - 1) return;
        String t = (String)fields.get(i + 1);
        fields.set(i + 1, f);
        fields.set(i, t);
        syncFields();
    }

    private void syncFields() {
        XmlUtils.removeExtensions(element, FIELD_NAME);
        int n = fields.size();
        for (int i = 0; i < n; i++) {
            String name = (String)fields.get(i);
            XmlUtils.createExtension(FIELD_NAME, name, element);
        }
        XmlUtils.makeDirty(element);
        fillFieldSummary();
    }

}

