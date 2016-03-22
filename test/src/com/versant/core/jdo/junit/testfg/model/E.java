
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
package com.versant.core.jdo.junit.testfg.model;

/**
 * @keep-all
 */
public class E {

    private String eString;

    public E() {
    }

    public E(String eString) {
        this.eString = eString;
    }

    public String geteString() {
        return eString;
    }

    public void seteString(String eString) {
        this.eString = eString;
    }
}


