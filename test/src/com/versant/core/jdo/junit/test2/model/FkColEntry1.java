
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
public class FkColEntry1 {
    private FkColHolder1 owner;
    private String val;

    public FkColHolder1 getOwner() {
        return owner;
    }

    public void setOwner(FkColHolder1 owner) {
        this.owner = owner;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
