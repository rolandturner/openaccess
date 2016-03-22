
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
public class TestB {

    private int count;

    private int ownCount;

    private int totalCount;

    public TestB(int count) {
        this.count = count;
        this.totalCount += count;
    }

    public int getCount() {
        return count;
    }

    public void modifyOwnCount(int count) {
        this.ownCount += count;
        this.totalCount += count;
    }

    public void validate() throws Exception {
        if (count + ownCount != totalCount) {
            throw new Exception("TestB is out of synch");
        }
    }
}


