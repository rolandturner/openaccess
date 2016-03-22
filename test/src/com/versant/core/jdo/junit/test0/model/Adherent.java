
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
 * For dependent ref in subclass not nuked bug [164].
 *
 * @keep-all
 * @see com.versant.core.jdo.junit.test0.TestDeletes#testDependentRefInSuperclass
 */
public class Adherent extends Enterprise {

    private int age;

    public Adherent() {
    }

    public Adherent(String name, Address address, int age) {
        super(name, address);
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}

 
