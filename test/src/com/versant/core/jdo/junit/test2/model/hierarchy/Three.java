
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
public class Three extends Two {

    private String fieldThree0;
    private String fieldThree1;

    public Three(int i) {
        super(i);
        fieldThree0 = "fieldThree0-" + i;
        fieldThree1 = "fieldThree1-" + i;
    }

    public String getFieldThree0() {
        return fieldThree0;
    }

    public void setFieldThree0(String fieldThree0) {
        this.fieldThree0 = fieldThree0;
    }

    public String getFieldThree1() {
        return fieldThree1;
    }

    public void setFieldThree1(String fieldThree1) {
        this.fieldThree1 = fieldThree1;
    }

    public void check(int i) {
        super.check(i);
        Assert.assertEquals(fieldThree0, "fieldThree0-" + i);
        Assert.assertEquals(fieldThree1, "fieldThree1-" + i);
    }

}

