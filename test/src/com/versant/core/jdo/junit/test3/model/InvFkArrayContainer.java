
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

import java.util.*;

/**
 * For testing arrays mapped using an inverse foreign key.
 */
public class InvFkArrayContainer {

    private InvFkArrayElement[] elements; // ordered by name

    public InvFkArrayContainer() {
    }

    public InvFkArrayElement[] getElements() {
        return elements;
    }

    public void setElements(InvFkArrayElement[] elements) {
        this.elements = elements;
    }

    public String getElementsStr() {
        return Arrays.asList(elements).toString();
    }

}

