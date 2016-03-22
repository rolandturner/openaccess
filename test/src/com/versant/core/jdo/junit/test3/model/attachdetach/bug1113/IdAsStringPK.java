
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
package com.versant.core.jdo.junit.test3.model.attachdetach.bug1113;

import java.io.Serializable;

public class IdAsStringPK implements Serializable {

    // each primary key field in the parent class must be listed as a public
    // field here
    public String _id;

    public IdAsStringPK() {

    }

    public IdAsStringPK(String str) {
        _id = str;
    }

    public boolean equals(Object ob) {
        return ob != null && ob instanceof IdAsStringPK && ((IdAsStringPK)ob)._id.equals(
                _id);
    }

    public int hashCode() {
        return ((this._id == null) ? 0 : this._id.hashCode());
    }

    public String toString() {
        return String.valueOf(this._id);
    }
}
