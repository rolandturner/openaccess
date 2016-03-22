
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
 * For testing persisting custom type field types.
 * @keep-all
 */
public class CustomTypes {

    private CustomStringType stringType;
    private CustomStringType stringTypeClob;
    private CustomStringType2 stringType2;
    private CustomBytesType bytesType;
    private CustomBytesType bytesTypeBin;

    public CustomTypes() {
    }

    public CustomStringType getStringType() {
        return stringType;
    }

    public void setStringType(CustomStringType stringType) {
        this.stringType = stringType;
    }

    public CustomStringType2 getStringType2() {
        return stringType2;
    }

    public void setStringType2(CustomStringType2 stringType2) {
        this.stringType2 = stringType2;
    }

    public CustomStringType getStringTypeClob() {
        return stringTypeClob;
    }

    public void setStringTypeClob(CustomStringType stringTypeClob) {
        this.stringTypeClob = stringTypeClob;
    }

    public CustomBytesType getBytesType() {
        return bytesType;
    }

    public void setBytesType(CustomBytesType bytesType) {
        this.bytesType = bytesType;
    }

    public CustomBytesType getBytesTypeBin() {
        return bytesTypeBin;
    }

    public void setBytesTypeBin(CustomBytesType bytesTypeBin) {
        this.bytesTypeBin = bytesTypeBin;
    }

}


