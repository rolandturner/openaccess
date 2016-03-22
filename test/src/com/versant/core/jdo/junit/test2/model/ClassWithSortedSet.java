
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

import java.util.*;

/**
 * For testing SortedSet's.
 */
public class ClassWithSortedSet {

    private String name;
    private SortedSet set = new TreeSet();    // of String
    private SortedSet revSet = new TreeSet(new RevComp()); // of String, reversed

    public ClassWithSortedSet() {
    }

    public ClassWithSortedSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SortedSet getSet() {
        return set;
    }

    public SortedSet getRevSet() {
        return revSet;
    }

    public void add(String s) {
        set.add(s);
    }

    public void addRev(String s) {
        revSet.add(s);
    }

    public String getSetString() {
        return tos(set);
    }

    public String getRevSetString() {
        return tos(revSet);
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
