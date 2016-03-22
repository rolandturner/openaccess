
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
package com.versant.core.jdo.junit.test2.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * For testing lists and references to classes with compound PK.
 * @keep-all
 */
public class ContainerForCompoundPkClass {

    private String name;
    private List list = new ArrayList(); // of CompoundPkClass
    private Map map = new HashMap();     // CompoundPkClass -> CompoundPkClass

    public ContainerForCompoundPkClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List getList() {
        return list;
    }

    public Map getMap() {
        return map;
    }

}
