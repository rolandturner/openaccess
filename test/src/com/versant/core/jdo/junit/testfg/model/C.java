
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
public class C {

    private D d;
    private String cString;

    public C() {
    }

    public C(String cString, D d) {
        this.cString = cString;
        this.d = d;
    }

    public D getD() {
        return d;
    }

    public void setD(D d) {
        this.d = d;
    }

    public String getcString() {
        return cString;
    }

    public void setcString(String cString) {
        this.cString = cString;
    }
}


