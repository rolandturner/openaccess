
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
 * For testing shared columns in a hierarchy.
 * @keep-all
 */
public class ShareAA extends ShareA {

    private String nameAA;  // name (ShareBase)
    private String phoneAA; // phone (ShareA)

    public ShareAA(String nameAA, String phoneAA) {
        super(nameAA, phoneAA);
        this.nameAA = nameAA;
        this.phoneAA = phoneAA;
    }

    public String getNameAA() {
        return nameAA;
    }

    public void setNameAA(String nameAA) {
        this.nameAA = nameAA;
        setNameA(nameAA);
    }

    public String getPhoneAA() {
        return phoneAA;
    }

    public void setPhoneAA(String phoneAA) {
        this.phoneAA = phoneAA;
        setPhone(phoneAA);
    }

}

