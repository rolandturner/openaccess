
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
/*
 * Created on Sep 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.versant.core.jdo.junit.test3.model.colreuse;

/**
 * @author rgreene
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SubClassA extends SuperOfClass {

    String aAttr;
    ClassB classb;

    public SubClassA(String v) {
        aAttr = v;
    }

    /**
     * @return
     */
    public String getAAttr() {
        return aAttr;
    }

    /**
     * @param string
     */
    public void setAAttr(String string) {
        aAttr = string;
    }

    public String toString() {
        return aAttr;
    }
}
