
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
public class ClassAB extends ClassA {

    private String stringA;
    private String stringAB;
    private ClassAB classAB;
    private Date dateAB;
    private List listAB = new ArrayList();

    public String getStringAA() {
        return stringA;
    }

    public void setStringAA(String stringA) {
        this.stringA = stringA;
    }

    public String getStringAB() {
        return stringAB;
    }

    public void setStringAB(String stringAB) {
        this.stringAB = stringAB;
    }

    public ClassAB getClassAB() {
        return classAB;
    }

    public void setClassAB(ClassAB classAB) {
        this.classAB = classAB;
    }

    public Date getDateAB() {
        return dateAB;
    }

    public void setDateAB(Date dateAB) {
        this.dateAB = dateAB;
    }

    public List getListAB() {
        return listAB;
    }

    public void setListAB(List listAB) {
        this.listAB = listAB;
    }

    public Object clone() throws CloneNotSupportedException {
        return (ClassAB)super.clone();
    }
}
