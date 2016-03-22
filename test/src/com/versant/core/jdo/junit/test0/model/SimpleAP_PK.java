
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
public class SimpleAP_PK implements Serializable {

    public int idNo;

    public SimpleAP_PK() {
    }

    public SimpleAP_PK(String idNo) {
        this.idNo = Integer.parseInt(idNo);
    }

    public int hashCode() {
        return idNo;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SimpleAP_PK) {
            SimpleAP_PK other = (SimpleAP_PK)obj;
            if (other.idNo == idNo) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return String.valueOf(idNo);
    }
}

