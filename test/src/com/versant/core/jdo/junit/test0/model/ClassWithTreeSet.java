
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
package com.versant.core.jdo.junit.test0.model;

import java.util.TreeSet;

/**
 * For testing TreeSet.
 */
public class ClassWithTreeSet {

    private String sValue;
    /**
     * set of Address's
     */
    private TreeSet tSet = new TreeSet();

    public TreeSet gettSet() {
        return tSet;
    }

    public void settSet(TreeSet tSet) {
        this.tSet = tSet;
    }

    public String getsValue() {
        return sValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }
}
