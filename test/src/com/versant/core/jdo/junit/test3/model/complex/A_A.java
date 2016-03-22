
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
public class A_A extends A {

    private String a_a;
    private List listRefA = new ArrayList();

    public String getA_a() {
        return a_a;
    }

    public void setA_a(String a_a) {
        this.a_a = a_a;
    }

    public List getListRefA() {
        return listRefA;
    }

    public void setListRefA(List listRefA) {
        this.listRefA = listRefA;
    }
}
