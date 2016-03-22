
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
package com.versant.core.jdo.junit.test3.model;

import java.util.ArrayList;
import java.util.List;

/**
 * For testing SQL generated to fetch link table collections.
 */
public class Order3 {

    private String name;
    private List lines = new ArrayList(); // inverse OrderLine3.order

    public Order3() {
    }

    public Order3(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getLines() {
        return lines;
    }

    public void add(OrderLine3 line) {
        lines.add(line);
        line.setOrder(this);
    }

    public String toString() {
        return name + " " + lines;
    }

}

