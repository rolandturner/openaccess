
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
package com.versant.core.jdo.junit.testfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @keep-all
 */
public class InherC extends InherB {

    private List stringListC = new ArrayList();

    public List getStringListC() {
        return stringListC;
    }

    public void setStringListC(List stringListC) {
        this.stringListC = stringListC;
    }
}
