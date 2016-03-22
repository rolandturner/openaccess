
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

/**
 */
public class RefA extends RefBase {

    private String refA;
    private RefA refARef;

    public RefA getRefARef() {
        return refARef;
    }

    public void setRefARef(RefA refARef) {
        this.refARef = refARef;
    }

    public String getRefA() {
        return refA;
    }

    public void setRefA(String refA) {
        this.refA = refA;
    }
}
