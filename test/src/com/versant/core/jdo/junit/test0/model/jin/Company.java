
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
package com.versant.core.jdo.junit.test0.model.jin;

import java.util.Set;
import java.util.HashSet;

/**
 */
public class Company {
    private String compName;
    private Set depts = new HashSet();

    public Set getDepts() {
        return depts;
    }

    public void setDepts(Set depts) {
        this.depts = depts;
    }


    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }
}
