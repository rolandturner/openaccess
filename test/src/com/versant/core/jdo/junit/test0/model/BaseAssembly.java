
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
import java.util.ArrayList;

/**
 * @keep-all
 */
public class BaseAssembly extends Assembly {

    // <->CompositePart.usedInPriv
    private ArrayList componentsPriv = new ArrayList();

    // <->CompositePart.usedInShar
    private ArrayList componentsShar = new ArrayList();

    private int baValue;

    public BaseAssembly(PersistenceManager pm, Module module,
            ComplexAssembly parentAssembly) {
        super(module, parentAssembly);

        baValue = 1;
        componentsPriv = new ArrayList();
        componentsShar = new ArrayList();
        module.addAssembly(this);  // Module only keeps track of BaseAssemblys
    }

    public BaseAssembly(Module module, ComplexAssembly parentAssembly) {
        super(module, parentAssembly);
    }

    public void removeSharedComponent(CompositePart compositePart) {
        componentsShar.remove(compositePart);
    }

    public int getBaValue() {
        return baValue;
    }

    public void setBaValue(int baValue) {
        this.baValue = baValue;
    }

    public ArrayList getComponentsPriv() {
        return componentsPriv;
    }

    public ArrayList getComponentsShar() {
        return componentsShar;
    }

    public void addSharedComponent(CompositePart p) {
        componentsShar.add(p);
        p.getUsedInShar().add(this);
    }

    public void addPivComponent(CompositePart p) {
        componentsPriv.add(p);
        p.getUsedInPriv().add(this);
    }

    public void removePrivateComponent(CompositePart compositePart) {
        componentsPriv.remove(compositePart);
    }

}

 
