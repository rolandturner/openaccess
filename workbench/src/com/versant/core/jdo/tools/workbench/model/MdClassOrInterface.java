
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
 * Javabean wrapper for class or interface meta data.
 */
public abstract class MdClassOrInterface extends MdBase
        implements MDStatics, JdoExtensionKeys, Comparable, MdProjectProvider {

    protected MdElement element;
    protected MdProject mdProject;
    protected MdPackage mdPackage;
    protected MdDataStore mdDataStore;

    public MdClassOrInterface(MdProject mdProject, MdPackage mdPackage,
            MdElement element) {
        this.element = element;
        this.mdProject = mdProject;
        this.mdPackage = mdPackage;
    }

    public MdJdoFile getMdJdoFile() {
        return mdPackage.getJdoFile();
    }

    public MdProject getMdProject() {
        return mdProject;
    }

    public MdPackage getMdPackage() {
        return mdPackage;
    }

    public MdDataStore getMdDataStore() {
        return mdDataStore;
    }

    public void setMdDataStore(MdDataStore mdDataStore) {
        this.mdDataStore = mdDataStore;
    }

    /**
     * Get the Class object or null if not loaded.
     */
    public abstract Class getCls();

    /**
     * Get the package of cls or null if not loaded.
     *
     * @see #getCls
     */
    public abstract String getClsPackage();

    public MdElement getElement() {
        return element;
    }

    public abstract String getName();

    public abstract void setName(String name);

    /**
     * Get the fully qualfied name of this class or interface.
     */
    public String getQName() {
        return mdPackage.getName() + "." + getName();
    }

    public String toString() {
        return getName();
    }

    /**
     * Order by name,
     */
    public int compareTo(Object o) {
        String a = getName();
        String b = ((MdClassOrInterface)o).getName();
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return +1;
        return a.compareTo(b);
    }

    /**
     * Analyze this class or interface. This will attempt to load the class
     * or interface if not already done.
     */
    public abstract void analyze(boolean notifyDatastore);

    /**
     * Get an icon to represent this class on the tree view.
     */
    public abstract String getTreeIcon();

    /**
     * Get info on how this class is mapped (e.g. table name) for the tree
     * view.
     */
    public abstract String getMappingInfo();

    public abstract boolean hasErrors();
}

