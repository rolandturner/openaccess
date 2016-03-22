
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
package com.versant.core.jdo.junit.test2.model.poly;

/**
 * For testing polyref's.
 * @keep-all
 */
public class PolyRefHolderIFace {

    private String name;
    private PersistentIFace data;    // persistent, dependent

    public PolyRefHolderIFace(String name, PersistentIFace data) {
        this.name = name;
        this.data = data;
    }

    public PersistentIFace getData() {
        return data;
    }

    public void setData(PersistentIFace data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
