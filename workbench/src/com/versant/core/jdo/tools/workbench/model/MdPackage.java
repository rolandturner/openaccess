
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

import com.versant.core.metadata.MDStatics;
import com.versant.core.metadata.parser.JdoExtensionKeys;

import java.util.*;

/**
 * Javabean wrapper for package meta data.
 */
public class MdPackage implements MDStatics, JdoExtensionKeys, Comparable, MdProjectProvider {

    private MdJdoFile jdoFile;
    private MdElement element;
    private List classList = new ArrayList();
    private HashMap classMap = new HashMap(); // qname -> MdClass
    private List interfaceList = new ArrayList();
    private HashMap interfaceMap = new HashMap(); // qname -> MdInterface

    public MdPackage(MdJdoFile jdoFile, MdElement element) {
        this.jdoFile = jdoFile;
        this.element = element;
        analyze();
    }

    public MdPackage(MdJdoFile jdoFile, String name) {
        this.jdoFile = jdoFile;
        element = new MdElement(ModelConstants.PACKAGE_NODE_NAME);
        setName(name);
        analyze();
    }

    public MdElement getElement() {
        return element;
    }

    public String getName() {
        return element.getAttributeValue("name");
    }

    public void setName(String name) {
        element.setAttribute("name", name);
    }

    private void analyze() {

        // find classes
        classList.clear();
        classMap.clear();
        List clist = element.getChildren("class");
        for (Iterator i = clist.iterator(); i.hasNext(); ) {
            MdElement ce = (MdElement)i.next();
            MdClass c = new MdClass(jdoFile.getProject(), this, ce);
            classList.add(c);
            classMap.put(c.getQName(), c);
        }
        Collections.sort(classList);

        // find interfaces
        interfaceList.clear();
        interfaceMap.clear();
        List l = XmlUtils.findExtensions(element, JdoExtensionKeys.INTERFACE);
        for (Iterator i = l.iterator(); i.hasNext(); ) {
            MdElement ce = (MdElement)i.next();
            MdInterface f = new MdInterface(jdoFile.getProject(), this, ce);
            interfaceList.add(f);
            interfaceMap.put(f.getQName(), f);
        }
    }

    public MdJdoFile getJdoFile() {
        return jdoFile;
    }

    public List getClassList() {
        return classList;
    }

    public List getInterfaceList() {
        return interfaceList;
    }

    public void addClassOrInterface(MdClassOrInterface c) {
        element.addContent(c.getElement());
        XmlUtils.makeDirty(element);
        if (c instanceof MdClass) {
            classList.add(c);
            classMap.put(c.getQName(), c);
        } else {
            interfaceList.add(c);
            interfaceMap.put(c.getQName(), c);
        }
    }

    public void removeClassOrInterface(MdClassOrInterface c) {
        element.removeContent(c.getElement());
        XmlUtils.makeDirty(element);
        if (c instanceof MdClass) {
            classList.remove(c);
            classMap.remove(c.getQName());
        } else {
            interfaceList.remove(c);
            interfaceMap.remove(c.getQName());
        }
    }

    public String toString() {
        return getName();
    }

    /**
     * Find a class in this package or another package. If the class name
     * does not specify a package it is assumed to be in this package.
     * Returns null if no class found.
     */
    public MdClass findClass(String cname) {
        boolean relative = cname.indexOf('.') < 0;
        if (relative) cname = getName() + "." + cname;
        MdClass c = (MdClass)classMap.get(cname);
        if (relative || c != null) return c;
        return jdoFile.getProject().findClass(cname);
    }

    /**
     * Order packages by name.
     */
    public int compareTo(Object o) {
        String a = getName();
        String b = ((MdPackage)o).getName();
        return a.compareTo(b);
    }

    public MdProject getMdProject() {
        return getJdoFile().getMdProject();
    }
}

