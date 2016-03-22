
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

import javax.jdo.PersistenceManager;
import java.util.ArrayList;

/**
 * @keep-all
 */
public class Complex {

    private ArrayList bases = new ArrayList();
    private PersistenceManager pm;

    public Complex(PersistenceManager pm) {
        this.pm = pm;
    }

    public void add(BaseAss baseAss) {
        bases.add(baseAss);
    }

    public void delete() {
        for (int i = 0; i < bases.size(); i++) {
            ((BaseAss)bases.get(i)).remove(this);
        }
        pm.deletePersistent(this);
    }
}


