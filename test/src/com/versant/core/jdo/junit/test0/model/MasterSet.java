
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

import java.util.HashSet;
import java.util.Iterator;

/**
 * @keep-all
 */
public class MasterSet {

    private HashSet details = new HashSet();

    public HashSet getDetails() {
        return details;
    }

    public int getDetailSize() {
        return details.size();
    }

    public DetailSet findDetail(int num) {
        for (Iterator iterator = details.iterator(); iterator.hasNext();) {
            DetailSet detail = (DetailSet)iterator.next();
            if (detail.getNum() == num) return detail;
        }
        return null;
    }

}
