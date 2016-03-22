
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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * For testing list SCOs.
 * @keep-all
 */
public class ClassWithList {

    private String name;
    private List list = new ArrayList();    // of String, SCOList
    private ArrayList arrayList = new ArrayList(); // of String, SCOArrayList
    private LinkedList linkedList = new LinkedList(); // of String, SCOLinkedList

    private List pcList = new ArrayList();  // of Country2, SCOList
    private List pcArrayList = new ArrayList(); // of Country2, SCOArrayList
    private List pcLinkedList = new LinkedList();   //of Country2, SCOLinkedList

    public ClassWithList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List getList() {
        return list;
    }

    public ArrayList getArrayList() {
        return arrayList;
    }

    public LinkedList getLinkedList() {
        return linkedList;
    }

    public void setLinkedList(LinkedList linkedList) {
        this.linkedList = linkedList;
    }

    public List getPcList() {
        return pcList;
    }

    public void setPcList(List pcList) {
        this.pcList = pcList;
    }

    public List getPcArrayList() {
        return pcArrayList;
    }

    public void setPcArrayList(List pcArrayList) {
        this.pcArrayList = pcArrayList;
    }

    public List getPcLinkedList() {
        return pcLinkedList;
    }

    public void setPcLinkedList(List pcLinkedList) {
        this.pcLinkedList = pcLinkedList;
    }

}
