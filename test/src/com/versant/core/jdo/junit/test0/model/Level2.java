
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
public class Level2 {

    private String name;
    private Level3 level3;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Level3 getLevel3() {
        return level3;
    }

    public void setLevel3(Level3 level3) {
        this.level3 = level3;
    }
}

