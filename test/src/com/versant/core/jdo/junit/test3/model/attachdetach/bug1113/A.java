
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
package com.versant.core.jdo.junit.test3.model.attachdetach.bug1113;

import java.util.ArrayList;
import java.util.List;

public class A extends PersistentThingImpl {

    private List _myBs;

    public A() {
        _myBs = new ArrayList();
    }

    public void addElement(B b) {
        _myBs.add(b);
    }

    public int getNoBs() {
        return _myBs.size();
    }
}
