
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
package com.versant.core.jdo.junit.test2.model;

/** 
 * For testing CLOB fields containing international characters.
 * @keep-all
 */
public class I18nClob {

    private String i18nClob;

    public I18nClob(String i18nClob) {
        this.i18nClob = i18nClob;
    }

    public String getI18nClob() {
        return i18nClob;
    }

    public void setI18nClob(String i18nClob) {
        this.i18nClob = i18nClob;
    }

}
