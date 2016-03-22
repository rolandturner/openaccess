
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
package com.versant.core.jdo.junit.test2.model.knowhow;

import java.util.Map;
import java.util.HashMap;

/** 
 * @keep-all
 */
public class User {

    private String name;
    private int age;
    private Map resourceLogMap = new HashMap();

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void put(Resource r, ResourceLog l) {
        resourceLogMap.put(r, l);
    }

    public Map getResourceLogMap() {
        return resourceLogMap;
    }

}
