
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
package com.versant.core.jdo.junit.test3.model;

/**
 * For testing null primitives and null wrapper types
 */
public class NullWrapper {
    private boolean primBool;
    private Boolean wrapBool;
    private char primChar;
    private Character wrapChar;

    public NullWrapper() {
    }

    public boolean isPrimBool() {
        return primBool;
    }

    public void setPrimBool(boolean primBool) {
        this.primBool = primBool;
    }

    public char getPrimChar() {
        return primChar;
    }

    public void setPrimChar(char primChar) {
        this.primChar = primChar;
    }

    public Boolean getWrapBool() {
        return wrapBool;
    }

    public void setWrapBool(Boolean wrapBool) {
        this.wrapBool = wrapBool;
    }

    public Character getWrapChar() {
        return wrapChar;
    }

    public void setWrapChar(Character wrapChar) {
        this.wrapChar = wrapChar;
    }

}

