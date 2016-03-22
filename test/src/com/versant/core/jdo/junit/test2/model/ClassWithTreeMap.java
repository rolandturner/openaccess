
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

import java.util.TreeMap;
import java.util.Comparator;
import java.util.Collection;
import java.util.Iterator;

/**
 * For testing TreeMap.
 */
public class ClassWithTreeMap {

    private String name;
    private TreeMap map = new TreeMap(); // String -> String
    private TreeMap revMap = new TreeMap(new RevComp()); // String -> String, reversed

    public ClassWithTreeMap() {
    }

    public ClassWithTreeMap(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TreeMap getMap() {
        return map;
    }

    public TreeMap getRevMap() {
        return revMap;
    }

    public void add(String key, String val) {
        map.put(key, val);
    }

    public void addRev(String key, String val) {
        revMap.put(key, val);
    }

    public String getMapString() {
        return tos(map.keySet());
    }

    public String getRevMapString() {
        return tos(revMap.keySet());
    }

    private static String tos(Collection c) {
        StringBuffer s = new StringBuffer();
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            s.append(i.next());
        }
        return s.toString();
    }

    public static class RevComp implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((String)o2).compareTo((String)o1);
        }
    }

}
