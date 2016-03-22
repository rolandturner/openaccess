
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
package com.versant.core.jdo.junit.test3.model;

import java.util.List;
import java.util.ArrayList;

/**
 * For testing link table collection fetching.
 */
public class TreeNode {

    private String name;
    private List children = new ArrayList();
    private TreeNode firstChild;

    public TreeNode(String name) {
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

    public void add(TreeNode n) {
        if (children.isEmpty()) firstChild = n;
        children.add(n);
    }

    public String toString() {
        return name + " " + children;
    }

}

