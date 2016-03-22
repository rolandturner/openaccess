
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
package com.versant.core.jdo.junit.testHorizontal.model.horizontal;

import java.io.Serializable;

/**
 *
 */
public class StringAppIdBase {
    private String pk;
    private String name;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ID implements Serializable {
        public String pk;

        public ID() {
        }

        public ID(String pk) {
            this.pk = pk;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID id = (ID) o;

            if (!pk.equals(id.pk)) return false;

            return true;
        }

        public final int hashCode() {
            return pk.hashCode();
        }

        public String toString() {
            return pk;
        }
    }

}
