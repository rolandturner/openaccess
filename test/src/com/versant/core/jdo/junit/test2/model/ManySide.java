
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
 * Class on the 'many' side of an inverse one-to-many relationship.
 * @keep-all
 */
public class ManySide {

    private String name;
    private OneSide oneSide = null;

    public ManySide(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public OneSide getOneSide() {
        return oneSide;
    }

}
