
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
package com.versant.core.jdo.junit.test1.model;

import java.util.*;

/**
 * A tree.
 */
public class IndexedNode {

    private String name;
    private List/*<Node>*/ children = new ArrayList();

    public IndexedNode() {
    }

    public IndexedNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getChildren() {
        return children;
    }

    public void setChildren(List children) {
        this.children = children;
    }

    /**
     * Get the names of all our children sorted alpha.
     */
    public String getChildrenStr() {
        ArrayList a = new ArrayList();
        for (int i = 0; i<children.size();i++) {
        	IndexedNode n = (IndexedNode)children.get(i); 
        	if (n != null)
        		a.add(n.getName());
        	else
        		a.add("<null>");
        }
        Collections.sort(a);
        return a.toString();
    }

}

