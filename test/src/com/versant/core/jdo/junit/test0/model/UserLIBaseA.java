
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
 * @see UserLI
 */
public class UserLIBaseA {

    private int userDummyA;

    public int getUserDummyA() {
        return userDummyA;
    }

    public void setUserDummyA(int userDummyA) {
        this.userDummyA = userDummyA;
    }

}
