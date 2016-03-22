
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
package com.versant.core.jdo.junit.test3.model;

/**
 * For testing indexing of columns in a hierarchy.
 *
 * @keep-all
 */
public class SubClassLOB extends BaseClassLOB {

    private int age;
    private String subLob;

    public SubClassLOB(String name, String baseNotes, int age, String subLob) {
        super(name, baseNotes);
        this.age = age;
        this.subLob = subLob;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSubLob() {
        return subLob;
    }

    public void setSubLob(String subLob) {
        this.subLob = subLob;
    }

}
