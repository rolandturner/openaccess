
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
package com.versant.core.jdo.junit.test2.model;

/**
 */
public class SerialisedFieldModel {
    //String
    private Object stringVal;
    //PC
    private Object addressVal;
    //pc collection
    private Object pcCollection;

    public Object getStringVal() {
        return stringVal;
    }

    public void setStringVal(Object stringVal) {
        this.stringVal = stringVal;
    }

    public Object getAddressVal() {
        return addressVal;
    }

    public void setAddressVal(Object addressVal) {
        this.addressVal = addressVal;
    }

    public Object getPcCollection() {
        return pcCollection;
    }

    public void setPcCollection(Object pcCollection) {
        this.pcCollection = pcCollection;
    }

}
