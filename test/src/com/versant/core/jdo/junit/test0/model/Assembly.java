
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

/**
 * @keep-all
 */
public class Assembly extends DesignObj {

    // <-> ComplexAssembly.subAssemblies
    private ComplexAssembly superAssembly;

    // <-> Module.assemblys
    private Module module;

    protected int aValue;

    public Assembly(Module module,
            ComplexAssembly parentAssembly) {
        this.module = module;
        superAssembly = parentAssembly;
    }

    public ComplexAssembly getSuperAssembly() {
        return superAssembly;
    }

    public void setSuperAssembly(ComplexAssembly superAssembly) {
        this.superAssembly = superAssembly;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public int getaValue() {
        return aValue;
    }

    public void setaValue(int aValue) {
        this.aValue = aValue;
    }

}

 
