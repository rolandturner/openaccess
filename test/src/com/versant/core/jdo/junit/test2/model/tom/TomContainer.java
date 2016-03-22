
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
package com.versant.core.jdo.junit.test2.model.tom;

import java.util.*;
import java.util.ArrayList;

/**
 * @keep-all
 */
public class TomContainer {

    private List listElements;


    public void addElement(AbstractListable newElement) {
        if (this.listElements == null) {
            this.listElements = new ArrayList();
        }
        this.listElements.add(newElement);
    }


    public List getElements() {
        return listElements;
    }
}

