
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
package com.versant.core.jdo.junit.test2.model.poly;

/**
 * For testing polyref's holding references to composite primary key classes.
 * @keep-all
 */
public class PolyRefHolderComp {

    private String name;
    private Object adata;
    private Object data;    // persistent, dependent
        // CompoundPkClass, refpk.Customer

    public PolyRefHolderComp(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getAdata() {
        return adata;
    }

    public void setAdata(Object adata) {
        this.adata = adata;
    }

}
