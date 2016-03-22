
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
package com.versant.core.jdo.tools.workbench.model;

/**
 * A wrapper for the OID for a PersistenceCapable object and it's display
 * string (as used in the jdo query results tree)
 *
 * @keep-all
 */
public class OidWrapper {

    private Object oid;
    private String displayString;

    public Object getOid() {
        return oid;
    }

    public void setOid(Object oid) {
        this.oid = oid;
    }

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public String toString() {
        return getDisplayString();
    }

}

