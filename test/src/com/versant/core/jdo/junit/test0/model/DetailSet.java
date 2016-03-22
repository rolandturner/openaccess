
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
public class DetailSet {

    private MasterSet master;
    private int num;

    public DetailSet() {

    }

    public DetailSet(int i) {
        num = i;

    }

    public void setMaster(MasterSet master) {
        this.master = master;
    }

    public MasterSet getMaster() {
        return master;
    }

    public String toString() {
        return "" + num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

}
