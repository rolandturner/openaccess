
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @keep-all
 */
public class ClassBB extends ClassA {

    private String stringBB;
    private Date dateBB;
    private List listBB = new ArrayList();

    public String getStringBB() {
        return stringBB;
    }

    public void setStringBB(String stringBB) {
        this.stringBB = stringBB;
    }

    public Date getDateBB() {
        return dateBB;
    }

    public void setDateBB(Date dateBB) {
        this.dateBB = dateBB;
    }

    public List getListBB() {
        return listBB;
    }

    public void setListBB(List listBB) {
        this.listBB = listBB;
    }
}
