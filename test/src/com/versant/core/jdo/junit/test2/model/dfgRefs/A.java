
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
package com.versant.core.jdo.junit.test2.model.dfgRefs;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class A {
    private String valA;
    private List listOfA = new ArrayList();
    private B refFromAToB;

    public String getValA() {
        return valA;
    }

    public void setValA(String val) {
        this.valA = val;
    }

    public List getListOfA() {
        return listOfA;
    }

    public void setListOfA(List listOfA) {
        this.listOfA = listOfA;
    }

    public B getRefFromAToB() {
        return refFromAToB;
    }

    public void setRefFromAToB(B refFromAToB) {
        this.refFromAToB = refFromAToB;
    }
}
