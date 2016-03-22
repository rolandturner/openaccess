
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
package com.versant.core.jdo.junit.test0.model.robert1;

import java.util.Collection;
import java.util.Vector;

/**
 */
public class OwnerMany {
    String value;
    Collection ofNeedToImplement;


    public OwnerMany(String o) {
        this.value = o;
        ofNeedToImplement = new Vector();
    }

    /**
     * @return
     */
    public void addNeedToImplement(NeedToImplement ref) {
        ofNeedToImplement.add(ref);
    }

    /**
     * @return
     */

    public Collection getOfNeedToImplement() {
        return ofNeedToImplement;
    }

    /**
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * @param collection
     */
    public void setOfNeedToImplement(Collection collection) {
        ofNeedToImplement = collection;
    }

    /**
     * @param string
     */
    public void setValue(String string) {
        value = string;
    }
}
