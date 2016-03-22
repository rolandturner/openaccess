
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
package com.versant.core.jdo.junit.test0.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @keep-all
 */
public class Master2 {

    private List details = new ArrayList();

    public void add(Detail2 detail) {
        details.add(detail);
        detail.setMaster(this);
    }

    public List getDetails() {
        return details;
    }
}
