
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

public class DerivedClass extends BaseClass {

    public boolean derived;
    public BaseClass thirdObj;

    public DerivedClass() {
        super();
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public BaseClass getThirdObj() {
        return thirdObj;
    }

    public void setThirdObj(BaseClass thirdObj) {
        this.thirdObj = thirdObj;
    }

    public DerivedClass(String base, boolean derived) {
        super(base);
        this.derived = derived;

    }


    public String toString() {
        return new String(" Fields:- " + derived);
    }
}
