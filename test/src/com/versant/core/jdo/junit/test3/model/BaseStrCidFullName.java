
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
 * Base class for hierarchy with String descriminator column values.
 */
public class BaseStrCidFullName {

    private String name;

    public BaseStrCidFullName(String name) {
        this.name = name;
    }

    public BaseStrCidFullName() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
