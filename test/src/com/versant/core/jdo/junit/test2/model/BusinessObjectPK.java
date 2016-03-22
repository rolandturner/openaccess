
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
 * @keep-all
 */
public class BusinessObjectPK implements Serializable {

    public String id;

    public BusinessObjectPK() {
    }

    public BusinessObjectPK(String id) {
        this.id = id;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessObjectPK)) return false;
        BusinessObjectPK other = (BusinessObjectPK) o;
        if (!id.equals(other.id)) return false;
        return true;
    }

    public String toString() {
        return id;
    }
}
