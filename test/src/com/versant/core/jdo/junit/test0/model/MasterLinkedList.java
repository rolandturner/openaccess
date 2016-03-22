
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
import java.util.LinkedList;

/**
 * @keep-all
 */
public class MasterLinkedList {

    private LinkedList details = new LinkedList();

    public LinkedList getDetails() {
        return details;
    }

    public int getDetailSize() {
        return details.size();
    }

    public DetailLinkedList findDetail(int num) {
        for (Iterator iterator = details.iterator(); iterator.hasNext();) {
            DetailLinkedList detail = (DetailLinkedList)iterator.next();
            if (detail.getNum() == num) return detail;
        }
        return null;
    }

}
