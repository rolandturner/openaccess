
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
public class ShareB extends ShareBase {

    private String nameB; // name (ShareBase)
    private String phone;     // phone (ShareA)

    public ShareB(String nameB, String phone) {
        super(nameB);
        this.nameB = nameB;
        this.phone = phone;
    }

    public String getNameB() {
        return nameB;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
        setName(nameB);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}

