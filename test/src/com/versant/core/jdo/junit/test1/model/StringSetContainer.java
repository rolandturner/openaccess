
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
package com.versant.core.jdo.junit.test1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Has a set of String and an int.
 */
public class StringSetContainer {

    private int age;
    private Set strings = new HashSet();

    public StringSetContainer() {
    }

    public StringSetContainer(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Set getStrings() {
        return strings;
    }

    public void setStrings(Set strings) {
        this.strings = strings;
    }

    public String getStringsStr() {
        ArrayList a = new ArrayList(strings);
        Collections.sort(a);
        return a.toString();
    }

}

