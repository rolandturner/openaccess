
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
public class Node {

    private String name;
    private Set/*<Node>*/ children = new HashSet();

    public Node() {
    }

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getChildren() {
        return children;
    }

    public void setChildren(Set children) {
        this.children = children;
    }

    /**
     * Get the names of all our children sorted alpha.
     */
    public String getChildrenStr() {
        ArrayList a = new ArrayList();
        for (Iterator i = children.iterator(); i.hasNext();) {
            Node n = (Node)i.next();
        	if (n != null)
        		a.add(n.getName());
        	else
        		a.add("<null>");
        }
        Collections.sort(a);
        return a.toString();
    }

}

