
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

import com.versant.core.jdo.junit.test2.model.poly.PersistentIFace;

/** 
 * For testing Bin Suns lastDown + interval &gt; ? bug.
 * @keep-all
 */
public class Product implements PersistentIFace {

    private long lastDown;
    private int interval;

    public Product(long lastDown, int interval) {
        this.lastDown = lastDown;
        this.interval = interval;
    }

    public long getLastDown() {
        return lastDown;
    }

    public int getInterval() {
        return interval;
    }

}
