
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @keep-all
 */
public class Master {

    private ArrayList details = new ArrayList();
    private String bla;

    public String getBla() {
        return bla;
    }

    public void setBla(String bla) {
        this.bla = bla;
    }

    public void add(Detail detail) {
        details.add(detail);
    }

    public void remove(Detail detail) {
        details.remove(detail);
    }

    public ArrayList getDetails() {
        return details;
    }

    public int getDetailSize() {
        return details.size();
    }

    public void addAllDetails(Collection colToBeAdded) {
        details.addAll(colToBeAdded);
    }

    public void removeAllDetails(Collection colToBeRemoved) {
        details.removeAll(colToBeRemoved);
    }

    public void retainAllDetails(Collection colToBeRetained) {
        details.retainAll(colToBeRetained);
    }

    public void clearDetails() {
        details.clear();
    }

    public Detail findDetail(int num) {
        for (Iterator iterator = details.iterator(); iterator.hasNext();) {
            Detail detail = (Detail)iterator.next();
            if (detail.getNum() == num) return detail;
        }
        return null;
    }

}
