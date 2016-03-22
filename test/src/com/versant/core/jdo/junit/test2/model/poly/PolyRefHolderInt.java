
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
package com.versant.core.jdo.junit.test2.model.poly;

/**
 * For testing polyref's with custom int class-id values.
 * @keep-all
 */
public class PolyRefHolderInt {

    private String name;
    private Object data;    // persistent, dependent
        // Address=10 Product=20 Customer=30

    public PolyRefHolderInt(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
