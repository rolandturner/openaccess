
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
package com.versant.core.jdo.junit.test2.model.embedded;

/**
 */
public class SubClass extends BaseClass {
    private String subString;
    private Currency subCurrency;

    public String getSubString() {
        return subString;
    }

    public void setSubString(String subString) {
        this.subString = subString;
    }

    public Currency getSubCurrency() {
        return subCurrency;
    }

    public void setSubCurrency(Currency subCurrency) {
        this.subCurrency = subCurrency;
    }

}
