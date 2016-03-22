
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
package com.versant.core.jdo.junit.test2.model;

import java.util.List;
import java.util.ArrayList;

/** 
 * For testing one-to-many relationships.
 * @see Detail
 * @keep-all
 */
public class Master {

    private String descr;
    private List details = new ArrayList(); // inverse Detail.master, dependent

    public Master(String descr) {
        this.descr = descr;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public List getDetails() {
        return details;
    }

    public void addDetail(Detail d) {
        details.add(d);
    }

    public void removeDetail(Detail d) {
        details.remove(d);
    }

    public String toString() {
        return descr + details;
    }

}
