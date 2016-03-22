
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

import java.util.Arrays;
import java.io.Serializable;

/**
 * Custom byte[] compatible type.
 */
public class CustomBytesType implements Serializable {

    private byte[] data;

    public CustomBytesType(byte[] data) {
        this.data = data;
    }

    public byte[] toBytes() {
        return data;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomBytesType)) return false;

        final CustomBytesType customBytesType = (CustomBytesType)o;

        if (!Arrays.equals(data, customBytesType.data)) return false;

        return true;
    }

    public int hashCode() {
        return 0;
    }

}
