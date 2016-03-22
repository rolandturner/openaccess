
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
public class SubClass2B extends SubClass2 {

    private String field2b;

    public SubClass2B(String name, int age, String field2b) {
        super(name, age);
        this.field2b = field2b;
    }

    public String getField2b() {
        return field2b;
    }

    public void setField2b(String field2b) {
        this.field2b = field2b;
    }

}
