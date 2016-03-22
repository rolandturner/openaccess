
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
public class Four extends Three {

    private String fieldFour0;
    private String fieldFour1;

    public Four(int i) {
        super(i);
        fieldFour0 = "fieldFour0-" + i;
        fieldFour1 = "fieldFour1-" + i;
    }

    public String getFieldFour0() {
        return fieldFour0;
    }

    public void setFieldFour0(String fieldFour0) {
        this.fieldFour0 = fieldFour0;
    }

    public String getFieldFour1() {
        return fieldFour1;
    }

    public void setFieldFour1(String fieldFour1) {
        this.fieldFour1 = fieldFour1;
    }

    public void check(int i) {
        super.check(i);
        Assert.assertEquals(fieldFour0, "fieldFour0-" + i);
        Assert.assertEquals(fieldFour1, "fieldFour1-" + i);
    }

}

