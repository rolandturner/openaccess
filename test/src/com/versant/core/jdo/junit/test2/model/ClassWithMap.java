
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

import java.util.Map;
import java.util.HashMap;

/**
 * For testing that maps get made transient properly.
 * @keep-all
 */
public class ClassWithMap {

    private String name;
    private Map map = new HashMap(); // String -> String

    public ClassWithMap(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void setData(String key, String value) {
        map.put(key, value);
    }

    public String getData(String key) {
        return (String)map.get(key);
    }

}
