
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
public class NavRootSubARef1 extends NavRootRef1 {

    private String navRootSubARef1;
    private NavRootSubARef2 navRootSubARef2;

    public NavRootSubARef1(String navRootSubARef1,
            NavRootSubARef2 navRootSubARef2) {
        this.navRootSubARef1 = navRootSubARef1;
        this.navRootSubARef2 = navRootSubARef2;
    }

    public NavRootSubARef1() {
    }

    public NavRootSubARef2 getNavRootSubARef2() {
        return navRootSubARef2;
    }

    public void setNavRootSubARef2(NavRootSubARef2 navRootSubARef2) {
        this.navRootSubARef2 = navRootSubARef2;
    }

    public String getNavRootSubARef1() {
        return navRootSubARef1;
    }

    public void setNavRootSubARef1(String navRootSubARef1) {
        this.navRootSubARef1 = navRootSubARef1;
    }
}
