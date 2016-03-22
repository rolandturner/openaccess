
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
package com.versant.core.jdo.junit.test2.model.bair;

import java.util.List;
import java.util.ArrayList;

/**
 * @keep-all
 */
public class LaborItem {

    private String name;
    private List  parts = new ArrayList();  // of Part

    public LaborItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List getParts() {
        return parts;
    }

}
