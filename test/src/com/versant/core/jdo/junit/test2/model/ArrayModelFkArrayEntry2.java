
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
public class ArrayModelFkArrayEntry2 {
    private ArraysModel parent;
    private String val;

    public ArraysModel getParent() {
        return parent;
    }

    public void setParent(ArraysModel parent) {
        this.parent = parent;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
