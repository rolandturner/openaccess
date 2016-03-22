
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
package com.versant.core.jdo.junit.test3.model.fabian;

import java.util.List;
import java.util.ArrayList;

/**
 * For testing Fabian Ceballos's bug.
 */
public class LiquidSub extends Liquid {

    private List actos = new ArrayList(); // of Acto

    public LiquidSub(String pkLic, String pkSol, int data) {
        super(pkLic, pkSol, data);
    }

    public LiquidSub() {
    }

    public List getActos() {
        return actos;
    }

    public void add(Acto acto) {
        actos.add(acto);
    }

    public void remove(Acto acto) {
        actos.remove(acto);
    }
}

