
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
package com.versant.core.jdo.jca;

import javax.resource.spi.ConnectionRequestInfo;


public class PMRequestInfo implements ConnectionRequestInfo {
    private Object id;

    public PMRequestInfo() { }

    public PMRequestInfo(PMRequestInfo value) {
        id = value.id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void reset() {
        id = null;
    }

    public boolean match(PMRequestInfo info) {
        if (id != null) {
            return id.equals(info.id);
        }
        return info.id == null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PMRequestInfo)) return false;
        return match((PMRequestInfo) o);
    }

    public int hashCode() {
        return (id != null ? id.hashCode() : super.hashCode());
    }

    public void fillFrom(PMRequestInfo pmRequestInfo) {
        this.id = pmRequestInfo.id;
    }
}
