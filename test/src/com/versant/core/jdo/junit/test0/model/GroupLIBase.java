
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
 * For testing many-to-many with List's and inheritance.
 *
 * @keep-all
 * @see GroupLI
 */
public class GroupLIBase {

    private int groupDummyA;
    private int groupDummyB;

    public int getGroupDummyA() {
        return groupDummyA;
    }

    public void setGroupDummyA(int groupDummyA) {
        this.groupDummyA = groupDummyA;
    }

    public int getGroupDummyB() {
        return groupDummyB;
    }

    public void setGroupDummyB(int groupDummyB) {
        this.groupDummyB = groupDummyB;
    }

}
