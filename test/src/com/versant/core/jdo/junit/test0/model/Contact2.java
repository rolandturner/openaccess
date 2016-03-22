
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

/**
 */
public class Contact2 {


    private String name;
	private java.util.List list1;
	private java.util.List list2;
	private java.util.List list3;


    private Contact2() {
    }

    public Contact2(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public java.util.List getList1() {
        return list1;
    }

    public java.util.List getList2() {
        return list2;
    }

    public java.util.List getList3() {
        return list3;
    }

    public void setList1(java.util.List value) {
        list1 = value;
    }

    public void setList2(java.util.List value) {
        list2 = value;
    }

    public void setList3(java.util.List value) {
        list3 = value;
    }
}
