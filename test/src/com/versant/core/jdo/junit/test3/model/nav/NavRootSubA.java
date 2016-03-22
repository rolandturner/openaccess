
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
package com.versant.core.jdo.junit.test3.model.nav;

/**
 */
public class NavRootSubA extends NavRoot {

    private String navRootSubAVal;
    private NavRootSubARef1 navRootSubARef1;

    public NavRootSubA(String navRootSubAVal, NavRootSubARef1 navRootSubARef1) {
        this.navRootSubAVal = navRootSubAVal;
        this.navRootSubARef1 = navRootSubARef1;
    }

    public NavRootSubA() {
    }

    public NavRootSubARef1 getNavRootSubARef1() {
        return navRootSubARef1;
    }

    public void setNavRootSubARef1(NavRootSubARef1 navRootSubARef1) {
        this.navRootSubARef1 = navRootSubARef1;
    }

    public String getNavRootSubAVal() {
        return navRootSubAVal;
    }

    public void setNavRootSubAVal(String navRootSubAVal) {
        this.navRootSubAVal = navRootSubAVal;
    }
}
