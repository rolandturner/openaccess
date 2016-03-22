
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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * This is a util class for managing the persist-after extensions.
 */
public class MdConstraintDep implements JdoExtensionKeys {

    private MdElement element;

    private List fieldList = new ArrayList();

    public MdConstraintDep(MdElement element) {
        this.element = element;
        analyze();
    }

    public MdConstraintDep(MdClass mdClass) {
        this(XmlUtils.createExtension(PERSIST_AFTER,
                mdClass.getElement()));
    }

    public void addDepClass(String name) {
        List list = element.getChildren("extension");
        for (Iterator i = list.iterator(); i.hasNext();) {
            MdElement mdElement = (MdElement)i.next();
            if (mdElement.getAttributeValue("value").equals(name)) {
                return;
            }
        }

        XmlUtils.createExtension(CLASS, name, element);
        XmlUtils.makeDirty(element);
        fieldList.add(name);
    }

    public void removeField(String name) {
        List list = element.getChildren("extension");
        for (Iterator i = list.iterator(); i.hasNext();) {
            MdElement mdElement = (MdElement)i.next();
            if (mdElement.getAttributeValue("value").equals(name)) {
                element.removeContent(mdElement);
                XmlUtils.makeDirty(element);
                fieldList.remove(name);
                return;
            }
        }
    }

    private void analyze() {
        fieldList.clear();
        List list = element.getChildren("extension");
        for (Iterator i = list.iterator(); i.hasNext();) {
            fieldList.add(((MdElement)i.next()).getAttributeValue("value"));
        }
    }

    public List getDepList() {
        List depList = new ArrayList();
        depList.addAll(fieldList);
        return depList;
    }

}
