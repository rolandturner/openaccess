
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
package com.versant.core.jdo.junit.test0.model;

/**
 */
public class Gene {
    public int mGeneCount;
    private String bla;

    public int getmGeneCount() {
        return mGeneCount;
    }

    public void setmGeneCount(int mGeneCount) {
        this.mGeneCount = mGeneCount;
    }

    public String getBla() {
        return bla;
    }

    public void setBla(String bla) {
        this.bla = bla;
    }
}
