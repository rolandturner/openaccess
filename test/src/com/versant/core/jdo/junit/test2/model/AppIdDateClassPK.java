
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
import java.util.Date;

/**
 * @keep-all
 */
public class AppIdDateClassPK implements Serializable {

    public Date id;

    public AppIdDateClassPK() { }

    public AppIdDateClassPK(String id) {
        this.id = new Date(Long.parseLong(id));
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppIdDateClassPK)) return false;
        AppIdDateClassPK other = (AppIdDateClassPK) o;
        if (!id.equals(other.id)) return false;
        return true;
    }

    public String toString() {
        return String.valueOf(id.getTime());
    }
}
