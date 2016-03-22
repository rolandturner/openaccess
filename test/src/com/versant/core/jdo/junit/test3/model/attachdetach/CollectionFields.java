
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
package com.versant.core.jdo.junit.test3.model.attachdetach;

import java.io.Serializable;
import java.util.*;

public class CollectionFields implements Serializable {

    private int no;
    private HashSet hashSet = new HashSet();
    private Set set = new HashSet();
    private TreeSet treeSet = new TreeSet();
    private SortedSet sortedSet = new TreeSet(new ReverseChildNoComp());
    private ArrayList arrayList = new ArrayList();
    private List list = new ArrayList();
    private Collection collection = new ArrayList();
    private LinkedList linkedList = new LinkedList();
    private Vector vector = new Vector();
    private HashMap hashMap = new HashMap();
    private Map map = new HashMap();
    private Hashtable hashtable = new Hashtable();
    private TreeMap treeMap = new TreeMap();
    private SortedMap sortedMap = new TreeMap(new ReverseStringComp());
    private CollectionChild[] collectionChildren = new CollectionChild[2];
    private Set dateSet = new HashSet();

    public Set getDateSet() {
        return dateSet;
    }

    public void setDateSet(Set dateSet) {
        this.dateSet = dateSet;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public HashSet getHashSet() {
        return hashSet;
    }

    public void setHashSet(HashSet cHashSet) {
        this.hashSet = cHashSet;
    }

    public TreeSet getTreeSet() {
        return treeSet;
    }

    public void setTreeSet(TreeSet cTreeSet) {
        this.treeSet = cTreeSet;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public ArrayList getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList cArrayList) {
        this.arrayList = cArrayList;
    }

    public LinkedList getLinkedList() {
        return linkedList;
    }

    public void setLinkedList(LinkedList cLinkedList) {
        this.linkedList = cLinkedList;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector cVector) {
        this.vector = cVector;
    }

    public HashMap getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap cHashMap) {
        this.hashMap = cHashMap;
    }

    public Hashtable getHashtable() {
        return hashtable;
    }

    public void setHashtable(Hashtable cHashtable) {
        this.hashtable = cHashtable;
    }

    public TreeMap getTreeMap() {
        return treeMap;
    }

    public void setTreeMap(TreeMap cTreeMap) {
        this.treeMap = cTreeMap;
    }

    public SortedMap getSortedMap() {
        return sortedMap;
    }

    public void setSortedMap(SortedMap sortedMap) {
        this.sortedMap = sortedMap;
    }

    public CollectionChild[] getCollectionChildren() {
        return collectionChildren;
    }

    public void setCollectionChildren(CollectionChild[] collectionChildren) {
        this.collectionChildren = collectionChildren;
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * Orders CollectionChild nodes in reverse no order. This is the reverse
     * of the natural ordering of CollectionChild so we can be sure the
     * comparator is actually used.
     */
    public static class ReverseChildNoComp implements Comparator, Serializable {

        public int compare(Object o1, Object o2) {
            CollectionChild a = (CollectionChild)o1;
            CollectionChild b = (CollectionChild)o2;
            return b.getNo() - a.getNo();
        }
    }

    /**
     * Puts Strings in reverse order.
     */
    public static class ReverseStringComp implements Comparator, Serializable {

        public int compare(Object o1, Object o2) {
            return ((String)o2).compareTo((String)o1);
        }
    }
}
