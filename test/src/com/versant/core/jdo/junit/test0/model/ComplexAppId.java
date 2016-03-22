
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
package com.versant.core.jdo.junit.test0.model;

import java.util.Locale;

/**
 * @keep-all
 */
public class ComplexAppId {

    private String sID;
    private Locale lID;
    private boolean bID;
    private boolean cID;

    private ComplexAppId com;
    private String name;

    public boolean iscID() {
        return cID;
    }

    public void setcID(boolean cID) {
        this.cID = cID;
    }

    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }

    public Locale getlID() {
        return lID;
    }

    public void setlID(Locale lID) {
        this.lID = lID;
    }

    public boolean isbID() {
        return bID;
    }

    public void setbID(boolean bID) {
        this.bID = bID;
    }

    public ComplexAppId getCom() {
        return com;
    }

    public void setCom(ComplexAppId com) {
        this.com = com;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
