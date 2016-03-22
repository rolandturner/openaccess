
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
public class LeafMid extends LeafBase2 {

    private String midField;

    public LeafMid() {
    }

    public LeafMid(String baseField, String midField) {
        super(baseField);
        this.midField = midField;
    }

    public String getMidField() {
        return midField;
    }

    public void setMidField(String midField) {
        this.midField = midField;
    }

}

