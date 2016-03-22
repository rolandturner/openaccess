
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
package com.versant.core.jdo.junit.test2.model;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

/**
 * This has an objectid-class that extends a base class.
 * @keep-all
 */
public class AppIdClass {

    private int pk;
    private String name;

    public AppIdClass(int pk, String name) {
        this.pk = pk;
        this.name = name;
    }

    public AppIdClass(String name) {
        this.name = name;
    }

    public int getPk() {
        return pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ID extends IDBase {

        public ID() {
        }

        public ID(String s) {
            super(s);
        }

    }

}
