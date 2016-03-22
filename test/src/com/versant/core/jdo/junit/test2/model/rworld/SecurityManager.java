
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
package com.versant.core.jdo.junit.test2.model.rworld;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SecurityManager {
    private String val;
    private List administratorsUserGroups = new ArrayList();
    private List administrators = new ArrayList();

    public List getAdministratorsUserGroups() {
        return administratorsUserGroups;
    }

    public void setAdministratorsUserGroups(List administratorsUserGroups) {
        this.administratorsUserGroups = administratorsUserGroups;
    }

    public List getAdministrators() {
        return administrators;
    }

    public void setAdministrators(List administrators) {
        this.administrators = administrators;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

}
