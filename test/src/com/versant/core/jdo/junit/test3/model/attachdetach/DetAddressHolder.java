
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
package com.versant.core.jdo.junit.test3.model.attachdetach;

/**
 * 
 */
public class DetAddressHolder {
    private DetAddress defaultAdd = new DetAddress();
    private DetAddress add;

    public DetAddressHolder() {
    }

    public DetAddress getAdd() {
        return add;
    }

    public void setAdd(DetAddress add) {
        this.add = add;
    }

    public DetAddress getDefaultAdd() {
        return defaultAdd;
    }

    public void setDefaultAdd(DetAddress defaultAdd) {
        this.defaultAdd = defaultAdd;
    }
}
