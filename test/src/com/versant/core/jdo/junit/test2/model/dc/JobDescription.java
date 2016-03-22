
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
package com.versant.core.jdo.junit.test2.model.dc;

import java.util.Collection;
import java.util.ArrayList;

/**
 * 
 */
public class JobDescription {

    private int id;
    private Collection functionalAreaKeys = new ArrayList(); // of String

    public JobDescription(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addArea(String s) {
        functionalAreaKeys.add(s);
    }

    public Collection getFunctionalAreaKeys() {
        return functionalAreaKeys;
    }

}

