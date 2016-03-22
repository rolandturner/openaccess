
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

import java.util.Iterator;
import java.util.Vector;

/**
 * @keep-all
 */
public class MasterVector {

    private Vector details = new Vector();

    public Vector getDetails() {
        return details;
    }

    public int getDetailSize() {
        return details.size();
    }

    public DetailVector findDetail(int num) {
        for (Iterator iterator = details.iterator(); iterator.hasNext();) {
            DetailVector detail = (DetailVector)iterator.next();
            if (detail.getNum() == num) return detail;
        }
        return null;
    }

}
