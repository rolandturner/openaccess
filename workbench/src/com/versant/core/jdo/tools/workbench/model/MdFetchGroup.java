
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

import java.util.*;

import com.versant.core.metadata.parser.JdoExtensionKeys;
import org.jdom.Parent;

/**
 * @keep-all Java bean wrapper for fetch group meta data element.
 */
public class MdFetchGroup implements JdoExtensionKeys {

    private MdClass mdClass;
    private MdElement element;

    private List fieldList = new ArrayList();

    public MdFetchGroup(MdClass mdClass, MdElement element) {
        this.mdClass = mdClass;
        this.element = element;
        analyze();
    }

    public MdFetchGroup(MdClass mdClass) {
        this(mdClass, XmlUtils.createExtension(FETCH_GROUP, "new" +
                (mdClass.getFetchGroupList().size() + 1), mdClass.getElement()));
    }

    private void analyze() {
        fieldList.clear();
        List list = XmlUtils.findExtensions(element, FIELD_NAME);
        for (Iterator i = list.iterator(); i.hasNext();) {
            MdElement e = (MdElement)i.next();
            fieldList.add(new MdFetchGroupField(this, e));
        }
    }

    public MdClass getMdClass() {
        return mdClass;
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

    public List getFieldList() {
        return fieldList;
    }

    public void addField(MdFetchGroupField f) {
        Parent parent = f.getElement().getParent();
        if (parent != element) {
            if (parent != null) {
            	
                parent.addContent(f.getElement());

                
            }
            element.addContent(f.getElement());
        }
        XmlUtils.makeDirty(element);
        fieldList.add(f);
    }

    public void removeField(MdFetchGroupField f) {
        element.removeContent(f.getElement());
        XmlUtils.makeDirty(element);
        fieldList.remove(f);
    }

    /**
     * Find field with name or null if none.
     */
    public MdFetchGroupField findField(String name) {
        for (int i = fieldList.size() - 1; i >= 0; i--) {
            MdFetchGroupField f = (MdFetchGroupField)fieldList.get(i);
            String n = f.getName();
            if (n != null && n.equals(name)) return f;
        }
        return null;
    }

    public String toString() {
        return getName();
    }

}

