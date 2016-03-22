
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
public class SubB extends Base {

    private String subB;
    private Base bBaseRef;

    public String getSubB() {
        return subB;
    }

    public void setSubB(String subB) {
        this.subB = subB;
    }

    public Base getbBaseRef() {
        return bBaseRef;
    }

    public void setbBaseRef(Base bBaseRef) {
        this.bBaseRef = bBaseRef;
    }
}
