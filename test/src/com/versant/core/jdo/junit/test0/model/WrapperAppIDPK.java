
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
public class WrapperAppIDPK implements java.io.Serializable {

    public Integer id;

    public WrapperAppIDPK() {
    }

    public WrapperAppIDPK(String id) {
        this.id = new Integer(id);
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WrapperAppIDPK)) return false;
        WrapperAppIDPK other = (WrapperAppIDPK)o;
        if (!other.id.equals(id)) return false;
        return true;
    }

    public String toString() {
        return id.toString();
    }
}
