
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

import java.util.Vector;

/**
 * @author rgreene
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ClassB {

    Vector aInv = new Vector();
    Vector bInv = new Vector();
    Vector cInv = new Vector();
    Vector aFake = new Vector();
    Vector bFake = new Vector();
    Vector cFake = new Vector();

    public Vector getaInv() {
        return aInv;
    }

    public Vector getbInv() {
        return bInv;
    }

    public Vector getcInv() {
        return cInv;
    }

    public Vector getaFake() {
        return aFake;
    }

    public Vector getbFake() {
        return bFake;
    }

    public Vector getcFake() {
        return cFake;
    }

    public void addToAI(SubClassA a) {
        this.aInv.add(a);
    }

    public void addToBI(SubClassB b) {
        this.bInv.add(b);
    }

    public void addToCI(SubClassC c) {
        this.cInv.add(c);
    }

    public void addToAF(SubClassA a) {
        this.aFake.add(a);
    }

    public void addToBF(SubClassB b) {
        this.bFake.add(b);
    }

    public void addToCF(SubClassC c) {
        this.cFake.add(c);
    }

    public void setaInv(Vector aInv) {
        this.aInv = aInv;
    }

    public void setbInv(Vector bInv) {
        this.bInv = bInv;
    }

    public void setcInv(Vector cInv) {
        this.cInv = cInv;
    }

    public void setaFake(Vector aFake) {
        this.aFake = aFake;
    }

    public void setbFake(Vector bFake) {
        this.bFake = bFake;
    }

    public void setcFake(Vector cFake) {
        this.cFake = cFake;
    }
}
