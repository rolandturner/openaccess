
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
 * Custom String compatible type.
 * @keep-all
 */
public class CustomStringType implements Serializable {

    private String data;

    public CustomStringType(String data) {
        this.data = data;
    }

    public String toString() {
        return data;
    }

    public String getData() {
        return data;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomStringType)) return false;

        final CustomStringType customStringType = (CustomStringType)o;

        if (data != null
                ? !data.equals(customStringType.data)
                : customStringType.data != null) return false;

        return true;
    }

    public int hashCode() {
        return (data != null ? data.hashCode() : 0);
    }

}
