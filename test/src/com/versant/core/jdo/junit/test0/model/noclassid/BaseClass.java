
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
package com.versant.core.jdo.junit.test0.model.noclassid;

/**
 * Created by IntelliJ IDEA.
 * User: jaco
 * Date: 14-Sep-2005
 * Time: 13:29:02
 * To change this template use File | Settings | File Templates.
 */
public class BaseClass {
    private ClassWithReferenceCol owner;
    private String value;

    public BaseClass(ClassWithReferenceCol owner, String value) {
        this.value = value;
        this.owner = owner;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ClassWithReferenceCol getOwner() {
        return owner;
    }

    public void setOwner(ClassWithReferenceCol owner) {
        this.owner = owner;
    }
}
