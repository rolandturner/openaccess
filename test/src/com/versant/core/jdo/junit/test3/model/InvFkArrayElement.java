
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
package com.versant.core.jdo.junit.test3.model;

/**
 * For testing arrays mapped using an inverse foreign key.
 */
public class InvFkArrayElement {

    private InvFkArrayContainer parent;
    private String name;

    public InvFkArrayElement(InvFkArrayContainer parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public InvFkArrayElement() {
    }

    public InvFkArrayContainer getParent() {
        return parent;
    }

    public void setParent(InvFkArrayContainer parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}

