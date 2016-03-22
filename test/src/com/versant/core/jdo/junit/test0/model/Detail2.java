
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
public class Detail2 {

    private Master2 master;

    public void setMaster(Master2 master) {
        this.master = master;
    }

    public Master2 getMaster() {
        return master;
    }

    public void remove() {
        master.getDetails().remove(this);
        master = null;
    }
}
