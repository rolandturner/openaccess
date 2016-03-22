
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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

/**
 * Simple property.
 * @keep-all
 */
public class Property {

    private String name;

    public Property(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}

