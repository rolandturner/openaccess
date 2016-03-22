
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

/**
 */
public class B {
    private String valB;
    private C refToC;

    public C getRefToC() {
        return refToC;
    }

    public void setRefToC(C refToC) {
        this.refToC = refToC;
    }

    public String getValB() {
        return valB;
    }

    public void setValB(String valB) {
        this.valB = valB;
    }
}
