
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
package com.versant.core.jdo.junit.test3.model.smart;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class SmartD extends SmartB {
    private List stringList = new ArrayList();  //dfg
    private String val;
    private SmartC smartC;                      //dfg

    public SmartC getSmartC() {
        return smartC;
    }

    public void setSmartC(SmartC smartC) {
        this.smartC = smartC;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public List getStringList() {
        return stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }
}
