
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
 * An SCO type and the corresponding factory.
 */
public class MdSCOMapping implements Comparable {

    private final MdDataStore mdDataStore;
    private final String scoClassName;
    private String factoryClassName;

    public MdSCOMapping(MdDataStore mdDataStore, String scoClass,
            String factoryClass) {
        this.mdDataStore = mdDataStore;
        this.scoClassName = scoClass;
        this.factoryClassName = factoryClass;
    }

    public MdDataStore getMdDataStore() {
        return mdDataStore;
    }

    public String getScoClassName() {
        return scoClassName;
    }

    public String getFactoryClassNameStr() {
        return factoryClassName;
    }

    public MdValue getFactoryClassName() {
        MdClassNameValue v = new MdClassNameValue(factoryClassName);
        Class key = mdDataStore.getProject().loadClass(scoClassName);
        if (key != null) {
            v.setDefText(mdDataStore.getOriginalDefaultSCOFactory(key));
            v.setPickList(mdDataStore.getValidSCOFactoryList(key));
        }
        v.setOnlyFromPickList(false);
        return v;
    }

    public void setFactoryClassName(MdValue v) {
        this.factoryClassName = v.getText();
    }

    /**
     * Order by scoClassName.
     */
    public int compareTo(Object o) {
        return scoClassName.compareTo(((MdSCOMapping)o).scoClassName);
    }

}

