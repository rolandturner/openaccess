
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
package com.versant.core.jdo.junit.test0.model;

import javax.jdo.PersistenceManager;
import java.util.HashSet;

/**
 * @keep-all
 */
public class Module extends DesignObj {

    private Manual man;  // <-> Manual.mod

    // <-> Assembly.module.  Just keep track of BaseAssemblys
    private HashSet assemblies = new HashSet();
    private Assembly designRoot;

    public Module(PersistenceManager pm, int numAssmLevels, int moduleNum) {
        assemblies = new HashSet();
        man = new Manual(this);
        if (numAssmLevels > 1) {
            designRoot = new ComplexAssembly(pm, this, moduleNum, null, 1);
        }
    }

    public Manual getMan() {
        return man;
    }

    public void setMan(Manual man) {
        this.man = man;
    }

    public Assembly getDesignRoot() {
        return designRoot;
    }

    public void setDesignRoot(Assembly designRoot) {
        this.designRoot = designRoot;
        designRoot.setModule(this);
    }

    public HashSet getAssemblies() {
        return assemblies;
    }

    public void addAssembly(BaseAssembly baseAssembly) {
        assemblies.add(baseAssembly);
    }

}

 
