
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
public class Two extends One {

    private String fieldTwo0;
    private String fieldTwo1;

    public Two(int i) {
        super(i);
        fieldTwo0 = "fieldTwo0-" + i;
        fieldTwo1 = "fieldTwo1-" + i;
    }

    public String getFieldTwo0() {
        return fieldTwo0;
    }

    public void setFieldTwo0(String fieldTwo0) {
        this.fieldTwo0 = fieldTwo0;
    }

    public String getFieldTwo1() {
        return fieldTwo1;
    }

    public void setFieldTwo1(String fieldTwo1) {
        this.fieldTwo1 = fieldTwo1;
    }

    public void check(int i) {
        super.check(i);
        Assert.assertEquals(fieldTwo0, "fieldTwo0-" + i);
        Assert.assertEquals(fieldTwo1, "fieldTwo1-" + i);
    }

}

