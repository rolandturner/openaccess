
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
 */
public class ConcreteSub3 extends AbsBaseClass {
    private int id;
    private String concreteSub3;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConcreteSub3() {
        return concreteSub3;
    }

    public void setConcreteSub3(String concreteSub3) {
        this.concreteSub3 = concreteSub3;
    }

    public static class ID implements Serializable {
        public int id;

        public ID(String id) {
            this.id = Integer.parseInt(id);
        }

        public ID() {
        }

        public String toString() {
            return "" + id;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;

            final ID pk = (ID) o;

            if (id != pk.id) return false;

            return true;
        }

        public int hashCode() {
            return id;
        }
    }
}
