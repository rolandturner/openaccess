
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
 * JavaBean wrapper for a persistent interface.
 *
 * @keep-all
 */
public class MdInterface extends MdClassOrInterface {

    private Class cls;
    private String clsPackage;

    public static final String ICON = "TreeInterface16.gif";

    public MdInterface(MdProject mdProject, MdPackage mdPackage,
            MdElement element) {
        super(mdProject, mdPackage, element);
    }

    public MdInterface(MdProject mdProject, MdPackage mdPackage, String name) {
        this(mdProject, mdPackage,
                XmlUtils.createExtension(JdoExtensionKeys.INTERFACE, name,
                        null));
    }

    public String getName() {
        return element.getAttributeValue("value");
    }

    public void setName(String name) {
        element.setAttribute("value", name);
        XmlUtils.makeDirty(element);
    }

    public Class getCls() {
        return cls;
    }

    public String getClsPackage() {
        return clsPackage;
    }

    public void analyze(boolean notifyDatastore) {
        cls = mdProject.loadClass(getQName());
        if (cls != null && !cls.isInterface()) {
            cls = null;
            mdProject.getLogger().error(
                    getQName() + " is a class (expected an interface)");
        }
        if (cls != null) {
            clsPackage = MdUtils.getPackage(cls.getName());
        } else {
            clsPackage = null;
        }
    }

    public String getTreeIcon() {
        return ICON;
    }

    public String getMappingInfo() {
        return "";
    }

    public boolean hasErrors() {
        return false;
    }
}
