
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

import java.io.Serializable;

/**
 * Custom String compatible type. This uses the toExternalString method
 * instead of toString.
 * @keep-all
 */
public class CustomStringType2 implements Serializable {

    private String data;

    public CustomStringType2(String data) {
        this.data = data;
    }

    public String toExternalString() {
        return data;
    }

    public String getData() {
        return data;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomStringType2)) return false;

        final CustomStringType2 customStringType = (CustomStringType2)o;

        if (data != null
                ? !data.equals(customStringType.data)
                : customStringType.data != null) return false;

        return true;
    }

    public int hashCode() {
        return (data != null ? data.hashCode() : 0);
    }

}
