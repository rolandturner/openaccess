
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
public class ExtendStringAppIdBase1 extends StringAppIdBase {
    private String val1;

    public String getVal1() {
        return val1;
    }

    public void setVal1(String val1) {
        this.val1 = val1;
    }

    public static class ID extends StringAppIdBase.ID implements Serializable {

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ID)) return false;
            
            final ID id = (ID) o;

            if (!pk.equals(id.pk)) return false;

            return true;
        }

    }
}
