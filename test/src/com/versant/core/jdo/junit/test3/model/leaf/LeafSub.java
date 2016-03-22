
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
package com.versant.core.jdo.junit.test3.model.leaf;

/**
 * For testing flat hierarchy with no descriminator.
 */
public class LeafSub extends LeafBase {

    private String subField;

    public LeafSub() {
    }

    public LeafSub(String baseField, String subField) {
        super(baseField);
        this.subField = subField;
    }

    public String getSubField() {
        return subField;
    }

    public void setSubField(String subField) {
        this.subField = subField;
    }

}

