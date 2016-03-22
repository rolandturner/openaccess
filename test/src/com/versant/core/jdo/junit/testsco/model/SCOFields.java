
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
package com.versant.core.jdo.junit.testsco.model;

import java.io.Serializable;
import java.util.*;

public class SCOFields implements Serializable {

    private Collection collection = new TreeSet();
    private Collection hashSet = new HashSet();
    private Collection treeSet = new TreeSet();
    private List list = new ArrayList();
    private Collection arrayList = new ArrayList();
    private Collection linkedList = new LinkedList();
    private Collection vector = new Vector();
    private Map hashMap = new HashMap();
    private Map map = new HashMap();
    private Map hashtable = new Hashtable();
    private Map treeMap = new TreeMap();
    private Set set = new HashSet();
    private Date date = new Date();

    public Collection getHashSet() {
        return hashSet;
    }

    public void setHashSet(Collection hashSet) {
        this.hashSet = hashSet;
    }

    public Collection getTreeSet() {
        return treeSet;
    }

    public void setTreeSet(Collection treeSet) {
        this.treeSet = treeSet;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Collection getArrayList() {
        return arrayList;
    }

    public void setArrayList(Collection arrayList) {
        this.arrayList = arrayList;
    }

    public Collection getLinkedList() {
        return linkedList;
    }

    public void setLinkedList(Collection linkedList) {
        this.linkedList = linkedList;
    }

    public Collection getVector() {
        return vector;
    }

    public void setVector(Collection vector) {
        this.vector = vector;
    }

    public Map getHashMap() {
        return hashMap;
    }

    public void setHashMap(Map hashMap) {
        this.hashMap = hashMap;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getHashtable() {
        return hashtable;
    }

    public void setHashtable(Map hashtable) {
        this.hashtable = hashtable;
    }

    public Map getTreeMap() {
        return treeMap;
    }

    public void setTreeMap(Map treeMap) {
        this.treeMap = treeMap;
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
