
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
package com.versant.core.jdo.junit.test0.model.huyi;

import java.util.StringTokenizer;
import java.io.Serializable;

/**
 */
public class IDPrimaryKey
        implements Serializable {
    public String id;
    public IDPrimaryKey() {
    }
    
    public IDPrimaryKey(String value) {
        StringTokenizer token = new StringTokenizer(value, "::");
        token.nextToken(); // className
        id = token.nextToken(); // name
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (! (object instanceof IDPrimaryKey)) {
            return false;
        }
        IDPrimaryKey other = (IDPrimaryKey) object;
        return this.id.equals(other.id);
    }
    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.getClass().getName() + "::" + this.id;
    }
}
