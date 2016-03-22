
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

/**
 * @keep-all
 */
public class PCComparable implements Comparable {

    private int val;

    public PCComparable(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PCComparable)) return false;

        final PCComparable pcComparable = (PCComparable)o;

        if (val != pcComparable.val) return false;

        return true;
    }

    public int hashCode() {
        return val;
    }

    public int compareTo(Object o) {
        return val - ((PCComparable)o).val;
    }
}

