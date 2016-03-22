
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
 * @keep-all
 */
public abstract class AppIDAbstract1 {

    private int appIdConcKey;

    private String appIdAbs1;

    public String getAppIdAbs1() {
        return appIdAbs1;
    }

    public void setAppIdAbs1(String appIdAbs1) {
        this.appIdAbs1 = appIdAbs1;
    }

    public int getAppIdConcKey() {
        return appIdConcKey;
    }

    public void setAppIdConcKey(int appIdConcKey) {
        this.appIdConcKey = appIdConcKey;
    }
}
