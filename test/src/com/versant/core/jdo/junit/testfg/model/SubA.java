
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
package com.versant.core.jdo.junit.testfg.model;

/**
 * @keep-all
 */
public class SubA extends Base {

    private String subA;
    private Base aBaseRef;

    public String getSubA() {
        return subA;
    }

    public void setSubA(String subA) {
        this.subA = subA;
    }

    public Base getaBaseRef() {
        return aBaseRef;
    }

    public void setaBaseRef(Base aBaseRef) {
        this.aBaseRef = aBaseRef;
    }
}
