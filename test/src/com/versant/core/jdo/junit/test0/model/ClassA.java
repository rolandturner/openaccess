
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
 * @keep-all
 */
public class ClassA implements Cloneable{

    private String stringA;
    private ClassA classA;

    public String getStringA() {
        return stringA;
    }

    public void setStringA(String stringA) {
        this.stringA = stringA;
    }

    public ClassA getClassA() {
        return classA;
    }

    public void setClassA(ClassA classA) {
        this.classA = classA;
    }

    public Object clone() throws CloneNotSupportedException {
        return (ClassA)super.clone();
    }
}
