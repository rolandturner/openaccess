
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
public class ListContainer {

    private String name;
    private List listA = new ArrayList();   // of AbstractListable
    private List listB = new ArrayList();   // of AbstractListable

    public ListContainer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addA(AbstractListable newElement) {
        listA.add(newElement);
    }

    public void addB(AbstractListable newElement) {
        listB.add(newElement);
    }

    public List getListA() {
        return listA;
    }

    public List getListB() {
        return listB;
    }

}

