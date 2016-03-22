
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

import java.io.Serializable;

/**
 * @keep-all
 */
public class AppIdConcrete1Key implements Serializable {

    public int appIdConcKey;

    public AppIdConcrete1Key() {
    }

    public AppIdConcrete1Key(String appIdConcKeyString) {
        this.appIdConcKey = Integer.parseInt(appIdConcKeyString);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppIdConcrete1Key)) return false;

        AppIdConcrete1Key appIdConcrete1Key = (AppIdConcrete1Key)o;

        if (appIdConcKey != appIdConcrete1Key.appIdConcKey) return false;

        return true;
    }

    public int hashCode() {
        return appIdConcKey;
    }

    public String toString() {
        return String.valueOf(appIdConcKey);
    }
}
