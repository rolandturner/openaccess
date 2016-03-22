
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
package com.versant.core.jdo.junit.test0.model.horizontal;

import com.versant.core.jdo.junit.test0.model.Address;

public class HorizSubB extends HorizBase {

    private String valueB;
    
    public HorizSubB() {
    }
    
    public HorizSubB(int age, Address address, String valueb) {
        super(age, address);
        valueB = valueb;
    }

    public String getValueB() {
        return valueB;
    }

    public void setValueB(String valueA) {
        this.valueB = valueA;
    }
    
}
