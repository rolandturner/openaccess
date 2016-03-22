
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
public class BaseClassLOB {

    private String name;
    private String baseLob;

    public BaseClassLOB(String name, String baseLob) {
        this.name = name;
        this.baseLob = baseLob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseLob() {
        return baseLob;
    }

    public void setBaseLob(String baseLob) {
        this.baseLob = baseLob;
    }

}

