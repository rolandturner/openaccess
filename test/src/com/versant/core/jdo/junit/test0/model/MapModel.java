
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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

/**
 * @keep-all
 */
public class MapModel {

    /**
     * this is a basic map of String to String.
     */
    private Map basicMap = new HashMap();

    /**
     * a map ref objects as keys.
     */
    private Map refKeyMap = new HashMap();

    /**
     * Map with key and value of type Ref.
     */
    private Map refRefMap = new HashMap();

    /**
     * a map ref objects as keys.
     */
    private Hashtable refKeyTable = new Hashtable();

    /**
     * a Hashtable filled with String to String.
     */
    private Hashtable basicTable = new Hashtable();

    /**
     * A basic tree map.
     */
    private TreeMap basicTreeMap = new TreeMap();

    private Map dependentMap = new HashMap();

    private Map localeMap = new HashMap();

    public Map getBasicMap() {
        return basicMap;
    }

    public void setBasicMap(Map basicMap) {
        this.basicMap = basicMap;
    }

    public Map getRefKeyMap() {
        return refKeyMap;
    }

    public void setRefKeyMap(Map refKeyMap) {
        this.refKeyMap = refKeyMap;
    }

    public Map getRefRefMap() {
        return refRefMap;
    }

    public void setRefRefMap(Map refRefMap) {
        this.refRefMap = refRefMap;
    }

    public Hashtable getBasicTable() {
        return basicTable;
    }

    public void setBasicTable(Hashtable basicTable) {
        this.basicTable = basicTable;
    }

    public Hashtable getRefKeyTable() {
        return refKeyTable;
    }

    public void setRefKeyTable(Hashtable refKeyTable) {
        this.refKeyTable = refKeyTable;
    }

    public TreeMap getBasicTreeMap() {
        return basicTreeMap;
    }

    public void setBasicTreeMap(TreeMap basicTreeMap) {
        this.basicTreeMap = basicTreeMap;
    }

    public Map getDependentMap() {
        return dependentMap;
    }

    public void setDependentMap(Map dependentMap) {
        this.dependentMap = dependentMap;
    }

    public Map getLocaleMap() {
        return localeMap;
    }

    public void setLocaleMap(Map localeMap) {
        this.localeMap = localeMap;
    }
}

