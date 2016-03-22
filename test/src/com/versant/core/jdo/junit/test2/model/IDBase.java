
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
package com.versant.core.jdo.junit.test2.model;

import java.io.Serializable;

/** 
 * For testing objectid-class extending a base class.
 * @keep-all
 */
public class IDBase implements Serializable {

    public int pk;

    public IDBase() {
    }

    public IDBase(String s) {
        pk = Integer.parseInt(s);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IDBase)) return false;

        final IDBase idBase = (IDBase)o;

        if (pk != idBase.pk) return false;

        return true;
    }

    public int hashCode() {
        return pk;
    }

    public String toString() {
        return Integer.toString(pk);
    }

}

