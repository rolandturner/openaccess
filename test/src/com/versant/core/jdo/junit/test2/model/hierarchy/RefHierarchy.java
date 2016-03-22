
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
package com.versant.core.jdo.junit.test2.model.hierarchy;

/** 
 * For testing references to classes in a hierarchy.
 * @keep-all
 */
public class RefHierarchy {

    private String name;
    private Two refTwo;

    public RefHierarchy(String name, Two refTwo) {
        this.name = name;
        this.refTwo = refTwo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Two getRefTwo() {
        return refTwo;
    }

    public void setRefTwo(Two refTwo) {
        this.refTwo = refTwo;
    }

}
