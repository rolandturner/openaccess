
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

public class HorizSubA extends HorizBase {

    private String valueA;
    
    public HorizSubA() {
    }
    
    public HorizSubA(int age, Address address, String valueA) {
        super(age, address);
        this.valueA = valueA;
    }

    public String getValueA() {
        return valueA;
    }

    public void setValueA(String valueA) {
        this.valueA = valueA;
    }
    
}
