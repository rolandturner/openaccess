
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
package com.versant.core.jdo.junit.test2.model.hierarchy;

import junit.framework.Assert;

/** 
 * For testing a hierarchy.
 * @keep-all
 */
public class One {

    private String fieldOne0;
    private String fieldOne1;

    public One(int i) {
        fieldOne0 = "fieldOne0-" + i;
        fieldOne1 = "fieldOne1-" + i;
    }

    public String getFieldOne0() {
        return fieldOne0;
    }

    public void setFieldOne0(String fieldOne0) {
        this.fieldOne0 = fieldOne0;
    }

    public String getFieldOne1() {
        return fieldOne1;
    }

    public void setFieldOne1(String fieldOne1) {
        this.fieldOne1 = fieldOne1;
    }

    public void check(int i) {
        Assert.assertEquals(fieldOne0, "fieldOne0-" + i);
        Assert.assertEquals(fieldOne1, "fieldOne1-" + i);
    }

}

