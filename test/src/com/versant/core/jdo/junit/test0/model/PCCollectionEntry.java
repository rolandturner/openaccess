
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
public class PCCollectionEntry {

    private int unique;

    public PCCollectionEntry(int unique) {
        this.unique = unique;
    }

    public int getUnique() {
        return unique;
    }

    public void setUnique(int unique) {
        this.unique = unique;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PCCollectionEntry)) return false;

        final PCCollectionEntry pcCollectionEntry = (PCCollectionEntry)o;

        if (unique != pcCollectionEntry.unique) return false;

        return true;
    }

    public int hashCode() {
        return unique;
    }
}


