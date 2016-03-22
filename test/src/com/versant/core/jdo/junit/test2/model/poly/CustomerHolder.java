
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
package com.versant.core.jdo.junit.test2.model.poly;

import com.versant.core.jdo.junit.test2.model.Customer;

/**
 * For testing polyref's.
 * @keep-all
 */
public class CustomerHolder {

    private String name;
    private Customer data;
    private CustomerHolder holder;

    public CustomerHolder(String name, Customer data) {
        this.name = name;
        this.data = data;
    }

    public Customer getData() {
        return data;
    }

    public void setData(Customer data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomerHolder getHolder() {
        return holder;
    }

    public void setHolder(CustomerHolder holder) {
        this.holder = holder;
    }

}
