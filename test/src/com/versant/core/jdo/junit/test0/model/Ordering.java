
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * For testing the ordering extension.
 *
 * @keep-all
 * @see Entry
 */
public class Ordering {

    private int age;
    private List entryList = new ArrayList();
    // of Entry, ordering = "notInDfg ascending, name descending"
    private List fkEntryList = new ArrayList();
    // of FkEntry, ordering = "notInDfg ascending, name descending"
    private List stringList = new ArrayList();
    // of String, ordering = "this descending"
    private Set stringSet = new HashSet();
    // of String
    private List entryAddrList = new ArrayList();
    // of Entry, ordering = "addr.street ascending"
    private List entryDetailList = new ArrayList();
    // inverse EntryDetail.ordering, ordering = "name ascending"

    public Ordering() {
    }

    public Ordering(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List getEntryList() {
        return entryList;
    }

    public List getStringList() {
        return stringList;
    }

    public Set getStringSet() {
        return stringSet;
    }

    public List getEntryAddrList() {
        return entryAddrList;
    }

    public List getEntryDetailList() {
        return entryDetailList;
    }

    public List getFkEntryList() {
        return fkEntryList;
    }

    /**
     * Get the names of contents of entryList space separated.
     */
    public String getEntryListString() {
        StringBuffer s = new StringBuffer();
        int n = entryList.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(' ');
            s.append(((Entry)entryList.get(i)).getName());
        }
        return s.toString();
    }

    public String getFkEntryListString() {
        StringBuffer s = new StringBuffer();
        int n = fkEntryList.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(' ');
            s.append(((FkEntry)fkEntryList.get(i)).getName());
        }
        return s.toString();
    }

    /**
     * Get the contents of stringList space separated.
     */
    public String getStringListString() {
        StringBuffer s = new StringBuffer();
        int n = stringList.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(' ');
            s.append(stringList.get(i));
        }
        return s.toString();
    }

    /**
     * Get the addr.street of contents of entryAddrList space separated.
     */
    public String getEntryAddrListString() {
        StringBuffer s = new StringBuffer();
        int n = entryAddrList.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(' ');
            Entry entry = (Entry)entryAddrList.get(i);
            s.append(entry.getAddr().getStreet());
        }
        return s.toString();
    }

    /**
     * Get the names of contents of entryDetailList space separated.
     */
    public String getEntryDetailListString() {
        StringBuffer s = new StringBuffer();
        int n = entryDetailList.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(' ');
            s.append(((EntryDetail)entryDetailList.get(i)).getName());
        }
        return s.toString();
    }

}
