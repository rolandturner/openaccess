
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
package com.versant.core.jdo.junit.test3.model.leaf;

import java.util.List;
import java.util.ArrayList;

/**
 * For testing flat hierarchy with no descriminator.
 */
public class LeafBaseHolder {

    private LeafBase ref;
    private List list = new ArrayList(); // of LeafBase

    public LeafBaseHolder() {
    }

    public LeafBaseHolder(LeafBase ref) {
        this.ref = ref;
    }

    public LeafBase getRef() {
        return ref;
    }

    public void setRef(LeafBase ref) {
        this.ref = ref;
    }

    public List getList() {
        return list;
    }

}

