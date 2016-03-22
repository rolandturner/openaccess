
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
 * For testing ordering extension.
 *
 * @keep-all
 * @see Ordering
 */
public class Entry {

    private String name;
    private int notInDfg; // default-fetch-group=false
    private Address addr;

    public Entry() {
    }

    public Entry(String name, int notInDfg) {
        this.name = name;
        this.notInDfg = notInDfg;
    }

    public Entry(String name, int notInDfg, Address addr) {
        this(name, notInDfg);
        this.addr = addr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNotInDfg() {
        return notInDfg;
    }

    public void setNotInDfg(int notInDfg) {
        this.notInDfg = notInDfg;
    }

    public Address getAddr() {
        return addr;
    }

}
