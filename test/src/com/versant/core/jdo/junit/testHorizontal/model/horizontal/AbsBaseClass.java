
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
package com.versant.core.jdo.junit.testHorizontal.model.horizontal;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class AbsBaseClass {
    private String baseString;
    private List stringList = new ArrayList();
    private List refClassList = new ArrayList();
    private Map stringRefMap = new HashMap();
    private String[] stringArray;
    private String txField;

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public Map getStringRefMap() {
        return stringRefMap;
    }

    public void setStringRefMap(Map stringRefMap) {
        this.stringRefMap = stringRefMap;
    }

    public String getTxField() {
        return txField;
    }

    public void setTxField(String txField) {
        this.txField = txField;
    }

    public List getRefClassList() {
        return refClassList;
    }

    public void setRefClassList(List refClassList) {
        this.refClassList = refClassList;
    }

    public String getBaseString() {
        return baseString;
    }

    public void setBaseString(String baseString) {
        this.baseString = baseString;
    }

    public List getStringList() {
        return stringList;
    }

    public void setStringList(List stringList) {
        this.stringList = stringList;
    }

}
