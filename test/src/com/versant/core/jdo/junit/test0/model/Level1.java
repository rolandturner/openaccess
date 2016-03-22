
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

/**
 * @keep-all
 */
public class Level1 {

    private String name;
    private Level2 level2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Level2 getLevel2() {
        return level2;
    }

    public void setLevel2(Level2 level2) {
        this.level2 = level2;
    }
}

