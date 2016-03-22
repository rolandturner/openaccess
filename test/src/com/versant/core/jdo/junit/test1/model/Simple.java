
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
package com.versant.core.jdo.junit.test1.model;

/**
 * Very simple class with an integer value used for sanity check.
 */
public class Simple {

    private int age;

    public Simple() {
    }

    public Simple(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    public boolean equals(Object other){
        if (this==other) return true;
        if (other instanceof Simple == false) return false;
        Simple that = (Simple)other;
        return age==that.age;
    }
}

