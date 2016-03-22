
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
public class SubClass2A extends SubClass2 {

    private String field2a;

    public SubClass2A(String name, int age, String field2a) {
        super(name, age);
        this.field2a = field2a;
    }

    public String getField2a() {
        return field2a;
    }

    public void setField2a(String field2a) {
        this.field2a = field2a;
    }

}
