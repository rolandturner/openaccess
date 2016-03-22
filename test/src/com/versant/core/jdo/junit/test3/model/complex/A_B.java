
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
package com.versant.core.jdo.junit.test3.model.complex;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class A_B extends A {

    private String a_b;
    private List stringListAB = new ArrayList();

    public String getA_b() {
        return a_b;
    }

    public void setA_b(String a_b) {
        this.a_b = a_b;
    }

    public List getStringListAB() {
        return stringListAB;
    }

    public void setStringListAB(List stringListAB) {
        this.stringListAB = stringListAB;
    }
}
