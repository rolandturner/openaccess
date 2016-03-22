
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

import javax.jdo.JDOUserException;

/**
 * @keep-all
 */
public class A {

    private B b;
    private String aString;
    private E e;

    public A(String aString, B b, E e) {
        if (b == null) {
            throw new JDOUserException(
                    "must supply non-null 'b' as we are using an inner join to pick it up");
        }
        if (e == null) {
            throw new JDOUserException(
                    "must supply non-null 'e' as we are using an inner join to pick it up");
        }
        this.aString = aString;
        this.b = b;
        this.e = e;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public E getE() {
        return e;
    }

    public void setE(E e) {
        this.e = e;
    }

    public String getaString() {
        return aString;
    }

    public void setaString(String aString) {
        this.aString = aString;
    }
}


