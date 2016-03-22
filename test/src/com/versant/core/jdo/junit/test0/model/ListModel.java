
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
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * @keep-all
 */
public class ListModel {

    private String val;
    /**
     * LinkedList that takes String's
     */
    private LinkedList lList = new LinkedList();
    /**
     * LinkedList that takes PC instances
     */
    private LinkedList lList2 = new LinkedList();
    /**
     * This is an ordered list containing PCCollectionEntry instances
     */
    private List orderedList = new ArrayList();

    /**
     * This is an un-ordered list containing PCCollectionEntry instances
     */
    private List unOrderedList = new ArrayList();

    private ArrayList aList = new ArrayList();

    private Vector vector = new Vector();

    public LinkedList getlList() {
        return lList;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public void setlList(LinkedList lList) {
        this.lList = lList;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public List getOrderedList() {
        return orderedList;
    }

    public List getUnOrderedList() {
        return unOrderedList;
    }

    public void setUnOrderedList(List unOrderedList) {
        this.unOrderedList = unOrderedList;
    }

    public ArrayList getaList() {
        return aList;
    }

    public void setaList(ArrayList aList) {
        this.aList = aList;
    }

    public LinkedList getlList2() {
        return lList2;
    }

    public void setlList2(LinkedList lList2) {
        this.lList2 = lList2;
    }
}


